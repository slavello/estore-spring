package org.example.estore.service;

import org.example.estore.dto.ImportResultDto;
import org.example.estore.entity.Electronics;
import org.example.estore.entity.ElectronicsType;
import org.example.estore.entity.Employee;
import org.example.estore.entity.Position;
import org.example.estore.entity.Purchase;
import org.example.estore.entity.PurchaseType;
import org.example.estore.entity.Shop;
import org.example.estore.entity.ShopStock;
import org.example.estore.repository.ElectronicsRepository;
import org.example.estore.repository.ElectronicsTypeRepository;
import org.example.estore.repository.EmployeeRepository;
import org.example.estore.repository.PositionRepository;
import org.example.estore.repository.PurchaseRepository;
import org.example.estore.repository.PurchaseTypeRepository;
import org.example.estore.repository.ShopRepository;
import org.example.estore.repository.ShopStockRepository;
import org.example.estore.util.CsvParser;
import org.example.estore.util.Dates;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Импорт данных из zip-архива с CSV-файлами либо одиночного CSV-файла
 * для конкретной таблицы.
 * <p>
 * Имена файлов внутри архива и названия таблиц распознаются по алиасам
 * (например, positions.csv, сотрудники.csv, shops.csv и т.д.). Заголовки колонок
 * распознаются на русском и английском языках, в том числе варианты с суффиксом
 * «_ID». Справочники сопоставляются по наименованию (обновляются), записи реестров
 * добавляются. Ссылки могут задаваться идентификатором строки этого же импорта
 * или наименованием.
 */
@Service
public class CsvImportService {

    private static final int MAX_ENTRIES = 100;
    private static final long MAX_ENTRY_BYTES = 20L * 1024 * 1024;

    private final PositionRepository positions;
    private final ShopRepository shops;
    private final ElectronicsTypeRepository electronicsTypes;
    private final PurchaseTypeRepository purchaseTypes;
    private final EmployeeRepository employees;
    private final ElectronicsRepository electronics;
    private final ShopStockRepository stocks;
    private final PurchaseRepository purchases;
    private final PurchaseService purchaseService;

    /** Соответствие идентификаторов из CSV реально сохранённым записям текущего импорта */
    private final Map<Class<?>, Map<Long, Object>> idMappings = new HashMap<>();

    private final Map<String, TableStep> steps;

    public CsvImportService(
        PositionRepository positions,
        ShopRepository shops,
        ElectronicsTypeRepository electronicsTypes,
        PurchaseTypeRepository purchaseTypes,
        EmployeeRepository employees,
        ElectronicsRepository electronics,
        ShopStockRepository stocks,
        PurchaseRepository purchases,
        PurchaseService purchaseService)
    {
        this.positions = positions;
        this.shops = shops;
        this.electronicsTypes = electronicsTypes;
        this.purchaseTypes = purchaseTypes;
        this.employees = employees;
        this.electronics = electronics;
        this.stocks = stocks;
        this.purchases = purchases;
        this.purchaseService = purchaseService;

        this.steps = steps();
    }

    @Transactional
    public ImportResultDto importZip(MultipartFile file) {
        ImportResultDto result = new ImportResultDto();
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Файл не передан или пуст");
            }
            Map<String, List<String>> files = readZip(file.getInputStream());
            if (files.isEmpty()) {
                result.setMessage("В архиве не найдено ни одного CSV-файла");
                return result;
            }
            idMappings.clear();
            process(files, result);

            int totalErrors = result
                .getFiles()
                .stream()
                .mapToInt(ImportResultDto.FileReport::getSkippedErrors)
                .sum();
            int totalAdded = result
                .getFiles()
                .stream()
                .mapToInt(ImportResultDto.FileReport::getAdded)
                .sum();
            int totalUpdated = result
                .getFiles()
                .stream()
                .mapToInt(ImportResultDto.FileReport::getUpdated)
                .sum();
            result.setMessage(
                String.format(
                    "Импорт завершён: добавлено %d, обновлено %d, пропущено с ошибками %d.",
                    totalAdded, totalUpdated, totalErrors));
            result.setSuccess(totalErrors == 0);
            return result;
        } catch (IllegalArgumentException e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            return result;
        } catch (IOException e) {
            result.setSuccess(false);
            result.setMessage("Ошибка чтения архива: " + e.getMessage());
            return result;
        } finally {
            idMappings.clear();
        }
    }

    /**
     * Импорт одиночного CSV-файла для конкретной таблицы.
     *
     * @param file      CSV-файл (multipart, поле "file")
     * @param tableName каноническое имя или алиас таблицы:
     *                  positions, shops, electronics_types, purchase_types,
     *                  employees, electronics, stocks, purchases
     */
    @Transactional
    public ImportResultDto importCsv(MultipartFile file, String tableName) {
        ImportResultDto result = new ImportResultDto();
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Файл не передан или пуст");
            }
            TableStep step = findStep(tableName);
            if (step == null) {
                throw new IllegalArgumentException(
                    "Неизвестная таблица \"" + tableName + "\". Доступные таблицы: " + stepNames());
            }
            String fileName = fileNameOrFallback(file);
            idMappings.clear();
            TableData table = parseTable(fileName, readCsvLines(file.getInputStream()));
            ImportResultDto.FileReport report = ImportResultDto.fileReport(fileName);
            result.getFiles().add(report);
            if (!table.errors.isEmpty()) {
                report.getErrors().addAll(table.errors);
            } else {
                step.handler.handle(table, report);
            }
            result.setMessage(
                String.format(
                    "Импорт таблицы «%s» завершён: добавлено %d, обновлено %d, пропущено с ошибками %d.",
                    step.name, report.getAdded(), report.getUpdated(), report.getSkippedErrors()));
            result.setSuccess(report.getSkippedErrors() == 0 && report.getErrors().isEmpty());
            return result;
        } catch (IllegalArgumentException e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            return result;
        } catch (IOException e) {
            result.setSuccess(false);
            result.setMessage("Ошибка чтения файла: " + e.getMessage());
            return result;
        } finally {
            idMappings.clear();
        }
    }

    // ---- обработка файлов ----

    private void process(Map<String, List<String>> filesByName, ImportResultDto result) {
        for (TableStep step : steps.values()) {
            String key = findFileKey(filesByName, step);
            if (key == null) {
                continue; // файла нет в архиве — шаг пропускается
            }
            runStep(step, key, parseTable(key, filesByName.get(key)), result);
        }
    }

    private interface TableHandler {
        void handle(TableData table, ImportResultDto.FileReport report);
    }

    /** Шаг импорта: каноническое имя таблицы, алиасы имён файла и обработчик */
    private static class TableStep {
        final String name;
        final List<String> aliases;
        final TableHandler handler;

        TableStep(String name, List<String> aliases, TableHandler handler) {
            this.name = name;
            this.aliases = aliases;
            this.handler = handler;
        }
    }

    /**
     * Реестр шагов импорта.
     * Порядок соответствует внешним ключам: сначала справочники, затем реестры.
     */
    private Map<String, TableStep> steps() {
        return Map.of(
            "positions", new TableStep("positions",
                List.of("POSITIONS", "ДОЛЖНОСТИ"), this::handlePositions),
            "shops", new TableStep("shops",
                List.of("SHOPS", "STORES", "МАГАЗИНЫ", "МАГАЗИН"), this::handleShops),
            "electronics_types", new TableStep("electronics_types",
                List.of(
                    "ELECTRONICSTYPES", "ETYPES", "TYPES", "ELECTRONICS_TYPES",
                    "ТИПЫЭЛЕКТРОНИКИ", "ТИПЫ_ЭЛЕКТРОНИКИ", "ТИП_ЭЛЕКТРОНИКИ", "ТИПЫ"),
                this::handleElectronicsTypes),
            "purchase_types", new TableStep("purchase_types",
                List.of(
                    "PURCHASETYPES", "PTYPES", "PAYMENTTYPES", "PURCHASE_TYPES",
                    "ТИПЫПОКУПОК", "ТИПЫ_ПОКУПКИ", "ТИП_ПОКУПКИ"),
                this::handlePurchaseTypes),
            "employees", new TableStep("employees",
                List.of("EMPLOYEES", "СОТРУДНИКИ", "СОТРУДНИК"), this::handleEmployees),
            "electronics", new TableStep("electronics",
                List.of("ELECTRONICS", "PRODUCTS", "GOODS", "ЭЛЕКТРОТОВАРЫ", "ТОВАРЫ"),
                this::handleElectronics),
            "stocks", new TableStep("stocks",
                List.of("STOCKS", "SHOPSTOCKS", "SHOP_STOCKS", "НАЛИЧИЕ", "ОСТАТКИ"),
                this::handleStocks),
            "purchases", new TableStep("purchases",
                List.of("PURCHASES", "ПОКУПКИ", "ПОКУПКА"), this::handlePurchases)
        );
    }

    private String stepNames() {
        StringBuilder sb = new StringBuilder();
        for (String name : steps.keySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        return sb.toString();
    }

    /** Поиск шага по каноническому имени или алиасу таблицы */
    private TableStep findStep(String tableName) {
        String normalized = normalizeFileName(tableName == null ? "" : tableName);
        //return steps.get(normalized);
        for (TableStep s : steps.values()) {
            if (s.aliases.contains(normalized)) {
                return s;
            }
        }
        return null;
    }

    private String fileNameOrFallback(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name == null || name.trim().isEmpty() ? "data.csv" : name;
    }

    private String findFileKey(Map<String, List<String>> filesByName, TableStep step) {
        for (String name : filesByName.keySet()) {
            if (step.aliases.contains(normalizeFileName(name))) {
                return name;
            }
        }
        return null;
    }

    private void runStep(TableStep step, String fileName, TableData table, ImportResultDto result) {
        ImportResultDto.FileReport report = ImportResultDto.fileReport(fileName);
        result.getFiles().add(report);
        if (!table.errors.isEmpty()) {
            report.getErrors().addAll(table.errors);
            return;
        }
        step.handler.handle(table, report);
    }

    // ---- обработчики таблиц ----

    private void handlePositions(TableData t, ImportResultDto.FileReport r) {
        if (!requireColumns(t, r, List.of("НАИМЕНОВАНИЕ"))) {
            return;
        }
        for (int i = 0; i < t.rows.size(); i++) {
            Map<String, String> row = t.rows.get(i);
            try {
                String name = required(val(t, row, "НАИМЕНОВАНИЕ"), "НАИМЕНОВАНИЕ");
                Position p = positions.findByNameIgnoreCase(name).orElseGet(Position::new);
                boolean isNew = p.getId() == null;
                p.setName(name);
                p = positions.save(p);
                rememberMapping(Position.class, val(t, row, "ID"), p);
                if (isNew) {
                    r.setAdded(r.getAdded() + 1);
                } else {
                    r.setUpdated(r.getUpdated() + 1);
                }
            } catch (RuntimeException e) {
                r.addError(i + 2, e.getMessage());
            }
        }
    }

    private void handleShops(TableData t, ImportResultDto.FileReport r) {
        if (!requireColumns(t, r, List.of("НАИМЕНОВАНИЕ"))) {
            return;
        }
        for (int i = 0; i < t.rows.size(); i++) {
            Map<String, String> row = t.rows.get(i);
            try {
                String name = required(val(t, row, "НАИМЕНОВАНИЕ"), "НАИМЕНОВАНИЕ");
                Shop s = shops.findByNameIgnoreCase(name).orElseGet(Shop::new);
                boolean isNew = s.getId() == null;
                s.setName(name);
                s.setAddress(optional(val(t, row, "АДРЕС")));
                s = shops.save(s);
                rememberMapping(Shop.class, val(t, row, "ID"), s);
                if (isNew) {
                    r.setAdded(r.getAdded() + 1);
                } else {
                    r.setUpdated(r.getUpdated() + 1);
                }
            } catch (RuntimeException e) {
                r.addError(i + 2, e.getMessage());
            }
        }
    }

    private void handleElectronicsTypes(TableData t, ImportResultDto.FileReport r) {
        if (!requireColumns(t, r, List.of("НАИМЕНОВАНИЕ"))) {
            return;
        }
        for (int i = 0; i < t.rows.size(); i++) {
            Map<String, String> row = t.rows.get(i);
            try {
                String name = required(val(t, row, "НАИМЕНОВАНИЕ"), "НАИМЕНОВАНИЕ");
                ElectronicsType et = electronicsTypes
                    .findByNameIgnoreCase(name)
                    .orElseGet(ElectronicsType::new);
                boolean isNew = et.getId() == null;
                et.setName(name);
                et = electronicsTypes.save(et);
                rememberMapping(ElectronicsType.class, val(t, row, "ID"), et);
                if (isNew) {
                    r.setAdded(r.getAdded() + 1);
                } else {
                    r.setUpdated(r.getUpdated() + 1);
                }
            } catch (RuntimeException e) {
                r.addError(i + 2, e.getMessage());
            }
        }
    }

    private void handlePurchaseTypes(TableData t, ImportResultDto.FileReport r) {
        if (!requireColumns(t, r, List.of("НАИМЕНОВАНИЕ"))) {
            return;
        }
        for (int i = 0; i < t.rows.size(); i++) {
            Map<String, String> row = t.rows.get(i);
            try {
                String name = required(val(t, row, "НАИМЕНОВАНИЕ"), "НАИМЕНОВАНИЕ");
                PurchaseType pt = purchaseTypes
                    .findByNameIgnoreCase(name)
                    .orElseGet(PurchaseType::new);
                boolean isNew = pt.getId() == null;
                pt.setName(name);
                pt = purchaseTypes.save(pt);
                rememberMapping(PurchaseType.class, val(t, row, "ID"), pt);
                if (isNew) {
                    r.setAdded(r.getAdded() + 1);
                } else {
                    r.setUpdated(r.getUpdated() + 1);
                }
            } catch (RuntimeException e) {
                r.addError(i + 2, e.getMessage());
            }
        }
    }

    private void handleEmployees(TableData t, ImportResultDto.FileReport r) {
        if (!requireColumns(t, r,
            List.of("ФАМИЛИЯ", "ИМЯ", "ДАТАРОЖДЕНИЯ", "ДОЛЖНОСТЬ", "МАГАЗИН", "ПОЛ"))) {
            return;
        }
        for (int i = 0; i < t.rows.size(); i++) {
            Map<String, String> row = t.rows.get(i);
            try {
                Employee e = new Employee();
                e.setLastName(required(val(t, row, "ФАМИЛИЯ"), "ФАМИЛИЯ"));
                e.setFirstName(required(val(t, row, "ИМЯ"), "ИМЯ"));
                e.setMiddleName(optional(val(t, row, "ОТЧЕСТВО")));
                LocalDate birthDate = Dates.parseDate(required(val(t, row, "ДАТАРОЖДЕНИЯ"), "ДАТАРОЖДЕНИЯ"));
                if (birthDate == null) {
                    throw new IllegalArgumentException("Дата рождения не заполнена");
                }
                e.setBirthDate(birthDate);
                e.setPosition(
                    resolveRef(
                        val(t, row, "ДОЛЖНОСТЬ"),
                        "Должность",
                        id -> positions.findById(id),
                        positions::findByNameIgnoreCase,
                        Position.class));
                e.setShop(
                    resolveRef(
                        val(t, row, "МАГАЗИН"),
                        "Магазин",
                        id -> shops.findById(id),
                        shops::findByNameIgnoreCase,
                        Shop.class
                    )
                );
                e.setMale(parseGender(required(val(t, row, "ПОЛ"), "ПОЛ")));
                e = employees.save(e);
                rememberMapping(Employee.class, val(t, row, "ID"), e);
                r.setAdded(r.getAdded() + 1);
            } catch (RuntimeException ex) {
                r.addError(i + 2, ex.getMessage());
            }
        }
    }

    private void handleElectronics(TableData t, ImportResultDto.FileReport r) {
        if (!requireColumns(t, r, List.of("НАЗВАНИЕ", "ТИП"))) {
            return;
        }
        for (int i = 0; i < t.rows.size(); i++) {
            Map<String, String> row = t.rows.get(i);
            try {
                String name = required(val(t, row, "НАЗВАНИЕ"), "НАЗВАНИЕ");
                Electronics el = electronics.findByNameIgnoreCase(name).orElseGet(Electronics::new);
                boolean isNew = el.getId() == null;
                el.setName(name);
                el.setType(
                    resolveRef(
                        required(val(t, row, "ТИП"), "ТИП"),
                        "Тип электроники",
                        electronicsTypes::findById,
                        electronicsTypes::findByNameIgnoreCase,
                        ElectronicsType.class));
                BigDecimal price = parseDecimal(val(t, row, "ЦЕНА"), "Цена");
                if (price != null) {
                    el.setPrice(price);
                }
                Integer qty = parseInt(val(t, row, "КОЛИЧЕСТВО"), "Количество");
                if (qty != null) {
                    el.setQuantity(Math.max(0, qty));
                }
                Boolean archived = parseTriBoolean(val(t, row, "АРХИВНЫЙ"));
                if (archived != null) {
                    el.setArchived(archived);
                }
                el.setDescription(optional(val(t, row, "ОПИСАНИЕ")));
                el = electronics.save(el);
                rememberMapping(Electronics.class, val(t, row, "ID"), el);
                if (isNew) {
                    r.setAdded(r.getAdded() + 1);
                } else {
                    r.setUpdated(r.getUpdated() + 1);
                }
            } catch (RuntimeException ex) {
                r.addError(i + 2, ex.getMessage());
            }
        }
    }

    private void handleStocks(TableData t, ImportResultDto.FileReport r) {
        if (!requireColumns(t, r, List.of("ЭЛЕКТРОТОВАР", "МАГАЗИН", "КОЛИЧЕСТВО"))) {
            return;
        }
        for (int i = 0; i < t.rows.size(); i++) {
            Map<String, String> row = t.rows.get(i);
            try {
                Electronics el = resolveRef(
                    required(val(t, row, "ЭЛЕКТРОТОВАР"), "ЭЛЕКТРОТОВАР"),
                    "Электротовар",
                    electronics::findById, electronics::findByNameIgnoreCase,
                    Electronics.class);
                Shop shop = resolveRef(
                    required(val(t, row, "МАГАЗИН"), "МАГАЗИН"),
                    "Магазин",
                    shops::findById,
                    shops::findByNameIgnoreCase,
                    Shop.class);
                Integer qty = parseInt(required(val(t, row, "КОЛИЧЕСТВО"), "КОЛИЧЕСТВО"), "Количество");
                if (qty == null) {
                    throw new IllegalArgumentException("Количество не заполнено");
                }
                ShopStock stock = stocks
                    .findByElectronicsIdAndShopId(el.getId(), shop.getId())
                    .orElseGet(() -> {
                        ShopStock s = new ShopStock();
                        s.setElectronics(el);
                        s.setShop(shop);
                        return s;
                    });
                stock.setQuantity(Math.max(0, qty));
                stocks.save(stock);
                r.setAdded(r.getAdded() + 1);
            } catch (RuntimeException ex) {
                r.addError(i + 2, ex.getMessage());
            }
        }
    }

    private void handlePurchases(TableData t, ImportResultDto.FileReport r) {
        if (!requireColumns(t, r, List.of("ЭЛЕКТРОТОВАР", "СОТРУДНИК", "МАГАЗИН", "ТИППОКУПКИ"))) {
            return;
        }
        for (int i = 0; i < t.rows.size(); i++) {
            Map<String, String> row = t.rows.get(i);
            try {
                Electronics el = resolveRef(
                    required(val(t, row, "ЭЛЕКТРОТОВАР"), "ЭЛЕКТРОТОВАР"),
                    "Электротовар",
                    electronics::findById,
                    electronics::findByNameIgnoreCase,
                    Electronics.class);
                Employee emp = resolveRef(
                    required(val(t, row, "СОТРУДНИК"), "СОТРУДНИК"),
                    "Сотрудник",
                    employees::findById,
                    employees::findByLastNameIgnoreCase,
                    Employee.class);
                Shop shop = resolveRef(
                    required(val(t, row, "МАГАЗИН"), "МАГАЗИН"), "Магазин",
                    shops::findById,
                    shops::findByNameIgnoreCase,
                    Shop.class);
                PurchaseType pt = resolveRef(required(val(t, row, "ТИППОКУПКИ"), "ТИППОКУПКИ"),
                    "Тип покупки",
                    purchaseTypes::findById,
                    purchaseTypes::findByNameIgnoreCase,
                    PurchaseType.class);
                LocalDateTime dt = Dates.parseDateTime(val(t, row, "ДАТАПОКУПКИ"));

                Purchase p = new Purchase();
                p.setElectronics(el);
                p.setEmployee(emp);
                p.setShop(shop);
                p.setPurchaseType(pt);
                p.setDateTime(dt != null ? dt : LocalDateTime.now());
                purchaseService.registerSale(el, shop); // проверка наличия + списание
                purchases.save(p);
                r.setAdded(r.getAdded() + 1);
            } catch (RuntimeException ex) {
                r.addError(i + 2, ex.getMessage());
            }
        }
    }

    // ---- разбор zip/csv ----

    private Map<String, List<String>> readZip(InputStream in) throws IOException {
        Map<String, List<String>> files = new LinkedHashMap<>();
        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName();
                String simpleName = entryName.contains("/")
                    ? entryName.substring(entryName.lastIndexOf('/') + 1)
                    : entryName;
                if (!simpleName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
                    continue;
                }
                if (++count > MAX_ENTRIES) {
                    throw new IllegalArgumentException(
                        "В архиве слишком много файлов (максимум " + MAX_ENTRIES + ")");
                }
                byte[] bytes = readAll(zis, MAX_ENTRY_BYTES);
                String content = decode(bytes);
                List<String> lines = new ArrayList<>();
                for (String line : content.split("\\r?\\n")) {
                    if (!line.trim().isEmpty()) {
                        lines.add(line);
                    }
                }
                files.put(simpleName, lines);
                zis.closeEntry();
            }
        }
        return files;
    }

    /** Чтение одиночного CSV-файла: байты -> текст (UTF-8/CP1251) -> непустые строки */
    private List<String> readCsvLines(InputStream in) throws IOException {
        byte[] bytes = readAll(in, MAX_ENTRY_BYTES);
        String content = decode(bytes);
        List<String> lines = new ArrayList<>();
        for (String line : content.split("\\r?\\n")) {
            if (!line.trim().isEmpty()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private byte[] readAll(InputStream in, long limit) throws IOException {
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        long total = 0;
        while ((n = in.read(chunk)) > 0) {
            total += n;
            if (total > limit) {
                throw new IllegalArgumentException("Файл в архиве слишком большой (лимит 20 МБ)");
            }
            buf.write(chunk, 0, n);
        }
        return buf.toByteArray();
    }

    /** Декодирование: сначала UTF-8, при неудаче — Windows-1251 */
    private String decode(byte[] bytes) {
        try {
            CharsetDecoder decoder = StandardCharsets
                .UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
            return decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, Charset.forName("windows-1251"));
        }
    }

    private TableData parseTable(String fileName, List<String> lines) {
        TableData table = new TableData(fileName);
        if (lines.isEmpty()) {
            table.errors.add("файл пуст");
            return table;
        }
        String headerLine = stripBom(lines.get(0));
        char delimiter = CsvParser.detectDelimiter(headerLine);
        List<String> headerCells = CsvParser.parseLine(headerLine, delimiter);
        for (String h : headerCells) {
            table.header.add(normalizeToken(h));
        }
        table.header.removeIf(String::isEmpty);

        for (int i = 1; i < lines.size(); i++) {
            List<String> cells = CsvParser.parseLine(stripBom(lines.get(i)), delimiter);
            if (cells.size() == 1 && cells.get(0).trim().isEmpty()) {
                continue;
            }
            Map<String, String> row = new LinkedHashMap<>();
            for (int c = 0; c < table.header.size(); c++) {
                String value = c < cells.size() ? cells.get(c) : "";
                row.put(table.header.get(c), value == null ? "" : value.trim());
            }
            table.rows.add(row);
        }
        return table;
    }

    // ---- доступ к колонкам ----

    /**
     * Поиск фактической колонки по логическому имени:
     * точное совпадение либо вариант с суффиксом ID/_ID (например, ДОЛЖНОСТЬ -> ДОЛЖНОСТЬID).
     */
    private String findColumn(TableData t, String logical) {
        if (t.header.contains(logical)) {
            return logical;
        }
        if (t.header.contains(logical + "ID")) {
            return logical + "ID";
        }
        for (String h : t.header) {
            if (h.length() > logical.length() && h.startsWith(logical)) {
                String rest = h.substring(logical.length()).replace("_", "");
                if (rest.matches("[IiDd]+")) {
                    return h;
                }
            }
        }
        return null;
    }

    /** Значение ячейки по логическому имени колонки */
    private String val(TableData t, Map<String, String> row, String column) {
        String actual = findColumn(t, column);
        return actual == null ? null : row.get(actual);
    }

    /** Проверка наличия обязательных колонок; true — можно обрабатывать строки */
    private boolean requireColumns(TableData t, ImportResultDto.FileReport r, List<String> required) {
        boolean missing = false;
        for (String col : required) {
            if (findColumn(t, col) == null) {
                r.getErrors().add("не найдена обязательная колонка «" + col + "»");
                missing = true;
            }
        }
        if (missing) {
            r.setSkippedErrors(r.getSkippedErrors() + 1);
        }
        return !missing;
    }

    // ---- утилиты значений ----

    private void rememberMapping(Class<?> type, String csvId, Object entity) {
        Long id = parseLongOrNull(csvId);
        if (id != null) {
            idMappings.computeIfAbsent(type, k -> new HashMap<>()).put(id, entity);
        }
    }

    private <T> T resolveRef(
        String value,
        String label,
        Function<Long, Optional<T>> byId,
        Function<String, Optional<T>> byName,
        Class<T> type)
    {
        String val = value == null ? "" : value.trim();
        if (val.isEmpty()) {
            throw new IllegalArgumentException(label + ": значение не заполнено");
        }
        Long csvId = parseLongOrNull(val);
        if (csvId != null) {
            Map<Long, Object> mapping = idMappings.get(type);
            if (mapping != null && mapping.containsKey(csvId)) {
                return type.cast(mapping.get(csvId));
            }
            return byId
                .apply(csvId)
                .orElseThrow(
                    () -> new IllegalArgumentException(label + " не найдена по идентификатору: " + csvId)
                );
        }
        if (byName == null) {
            throw new IllegalArgumentException(
                label + " должна задаваться идентификатором, получено: \"" + val + "\"");
        }
        return byName
            .apply(val)
            .orElseThrow(() -> new IllegalArgumentException(
                label + " не найдена по наименованию: \"" + val + "\""));
    }

    private String required(String value, String column) {
        String val = optional(value);
        if (val == null) {
            throw new IllegalArgumentException("Колонка \"" + column + "\" не заполнена");
        }
        return val;
    }

    private String optional(String val) {
        if (val == null) {
            return null;
        }
        String trimmed = val.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long parseLongOrNull(String val) {
        if (val == null) {
            return null;
        }
        try {
            return Long.parseLong(val.trim().replace(" ", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(String val, String label) {
        String s = optional(val);
        if (s == null) {
            return null;
        }
        try {
            return Integer.parseInt(s.replace(" ", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " должно быть целым числом: \"" + val + "\"");
        }
    }

    private BigDecimal parseDecimal(String val, String label) {
        String s = optional(val);
        if (s == null) {
            return null;
        }
        try {
            return new BigDecimal(s.replace(" ", "").replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " должна быть числом: \"" + val + "\"");
        }
    }

    /** Пол: м/m/male/true/1 — true (мужской); ж/f/female/false/0 — false (женский) */
    private Boolean parseGender(String val) {
        String s = val.trim().toLowerCase(Locale.ROOT);
        switch (s) {
            case "м":
            case "m":
            case "male":
            case "муж":
            case "мужской":
            case "true":
            case "1":
                return true;
            case "ж":
            case "f":
            case "female":
            case "жен":
            case "женский":
            case "false":
            case "0":
                return false;
            default:
                throw new IllegalArgumentException("Некорректное значение пола: \"" + val + "\"");
        }
    }

    /** Разбор булева значения: пусто -> null */
    private Boolean parseTriBoolean(String val) {
        String s = optional(val);
        if (s == null) {
            return null;
        }
        switch (s.toLowerCase(Locale.ROOT)) {
            case "да":
            case "true":
            case "1":
            case "+":
            case "x":
                return true;
            case "нет":
            case "false":
            case "0":
            case "-":
                return false;
            default:
                throw new IllegalArgumentException("Некорректное логическое значение: \"" + val + "\"");
        }
    }

    private String normalizeToken(String s) {
        if (s == null) {
            return "";
        }
        String src = stripBom(s).toUpperCase(Locale.ROOT).replace('Ё', 'Е');
        StringBuilder sb = new StringBuilder();
        for (char c : src.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String normalizeFileName(String name) {
        String base = name.toLowerCase(Locale.ROOT);
        if (base.endsWith(".csv")) {
            base = base.substring(0, base.length() - 4);
        }
        return normalizeToken(base);
    }

    private String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }

    /** Разобранная таблица CSV */
    private static class TableData {
        final String name;
        final List<String> header = new ArrayList<>();
        final List<Map<String, String>> rows = new ArrayList<>();
        final List<String> errors = new ArrayList<>();

        TableData(String name) {
            this.name = name;
        }
    }
}
