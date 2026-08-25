package org.example.estore;

import org.example.estore.dto.BestEmployeeDto;
import org.example.estore.dto.CashTotalDto;
import org.example.estore.dto.ImportResultDto;
import org.example.estore.dto.PurchaseDto;
import org.example.estore.entity.Electronics;
import org.example.estore.entity.ElectronicsType;
import org.example.estore.entity.Employee;
import org.example.estore.entity.Purchase;
import org.example.estore.entity.PurchaseType;
import org.example.estore.entity.Position;
import org.example.estore.entity.Shop;
import org.example.estore.entity.ShopStock;
import org.example.estore.exception.OutOfStockException;
import org.example.estore.repository.*;
import org.example.estore.service.CsvImportService;
import org.example.estore.service.PurchaseService;
import org.example.estore.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:estore_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ApplicationTest {

    @Autowired private PositionRepository positions;
    @Autowired private ReportService reportService;
    @Autowired private PurchaseService purchaseService;
    @Autowired private CsvImportService csvImportService;
    @Autowired private ElectronicsRepository electronics;
    @Autowired private ElectronicsTypeRepository electronicsTypes;
    @Autowired private ShopRepository shops;
    @Autowired private ShopStockRepository stocks;
    @Autowired private EmployeeRepository employees;
    @Autowired private PurchaseTypeRepository purchaseTypes;
    @Autowired private PurchaseRepository purchases;

    /**
     * Демонстрационные данные вместо удалённого DataSeeder:
     * справочники, сотрудники, товары, остатки и покупки за последний год,
     * чтобы отчёты из ТЗ возвращали результат.
     */
    @BeforeEach
    void seedTestDataIfEmpty() {
        if (positions.count() > 0) {
            return;
        }

        Position director = positions.save(new Position("Директор магазина"));
        Position senior = positions.save(new Position("Старший продавец"));
        Position consultant = positions.save(new Position("Продавец-консультант"));
        Position junior = positions.save(new Position("Младший продавец-консультант"));
        positions.save(new Position("Кассир"));

        ElectronicsType watches = electronicsTypes.save(new ElectronicsType("Умные часы"));
        ElectronicsType phones = electronicsTypes.save(new ElectronicsType("Смартфоны"));
        ElectronicsType laptops = electronicsTypes.save(new ElectronicsType("Ноутбуки"));
        ElectronicsType tvs = electronicsTypes.save(new ElectronicsType("Телевизоры"));
        ElectronicsType headphones = electronicsTypes.save(new ElectronicsType("Наушники"));

        PurchaseType cash = purchaseTypes.save(new PurchaseType("Наличные"));
        PurchaseType card = purchaseTypes.save(new PurchaseType("Безналичные"));

        Shop lenina = shops.save(new Shop("Электроника на Ленина", "г. Москва, ул. Ленина, д. 1"));
        Shop mir = shops.save(new Shop("ТехноМир", "г. Москва, пр-т Мира, д. 25"));
        Shop digital = shops.save(new Shop("Digital House", "г. Москва, ул. Строителей, д. 7"));

        Employee ivanov = new Employee("Иванов", "Иван", "Иванович", LocalDate.of(1998, 4, 12), junior, lenina, true);
        Employee petrov = new Employee("Петров", "Пётр", "Петрович", LocalDate.of(1996, 9, 3), junior, mir, true);
        Employee sidorova = new Employee("Сидорова", "Анна", "Сергеевна", LocalDate.of(1999, 1, 22), junior, digital, false);
        Employee kuznetsov = new Employee("Кузнецов", "Артём", "Викторович", LocalDate.of(1990, 6, 15), consultant, lenina, true);
        Employee smirnova = new Employee("Смирнова", "Ольга", "Павловна", LocalDate.of(1992, 11, 8), senior, mir, false);
        Employee popov = new Employee("Попов", "Дмитрий", "Андреевич", LocalDate.of(1985, 2, 27), director, lenina, true);
        Employee vasileva = new Employee("Васильева", "Елена", "Игоревна", LocalDate.of(1995, 7, 19), consultant, digital, false);
        employees.saveAll(List.of(ivanov, petrov, sidorova, kuznetsov, smirnova, popov, vasileva));

        Electronics appleWatch = new Electronics("Apple Watch Series 9", watches, "46990.00", 60, false, "Умные часы, корпус 45 мм");
        Electronics galaxyWatch = new Electronics("Samsung Galaxy Watch6 Classic", watches, "34990.00", 50, false, "Умные часы с поворотным безелем");
        Electronics xiaomiWatch = new Electronics("Xiaomi Watch S3", watches, "12990.00", 70, false, "Умные часы, AMOLED");
        Electronics iphone = new Electronics("Apple iPhone 15", phones, "89990.00", 40, false, "128 ГБ");
        Electronics samsungTv = new Electronics("Samsung Crystal UHD 55\"", tvs, "54990.00", 25, false, "4K Smart TV");
        Electronics lenovoLaptop = new Electronics("Lenovo IdeaPad 5", laptops, "62990.00", 30, false, "16 ГБ ОЗУ, SSD 512 ГБ");
        Electronics jbl = new Electronics("JBL Tune 760NC", headphones, "9990.00", 80, true, "Снят с продаж");
        electronics.saveAll(List.of(appleWatch, galaxyWatch, xiaomiWatch, iphone, samsungTv, lenovoLaptop, jbl));

        // остатки по магазинам
        for (Electronics p : List.of(appleWatch, galaxyWatch, xiaomiWatch, iphone, samsungTv, lenovoLaptop)) {
            stocks.save(new ShopStock(p, lenina, 10));
            stocks.save(new ShopStock(p, mir, 10));
            stocks.save(new ShopStock(p, digital, 10));
        }
        stocks.save(new ShopStock(jbl, lenina, 5));

        // покупки за последний год.
        // Умных часов больше всех продал Иванов (младший продавец-консультант)
        Random rnd = new Random(42);
        LocalDateTime now = LocalDateTime.now();
        Employee[] sellers = {kuznetsov, smirnova, vasileva, popov};
        Electronics[] goods = {appleWatch, iphone, samsungTv, lenovoLaptop};

        for (int monthBack = 11; monthBack >= 0; monthBack--) {
            // продажи умных часов младшими продавцами-консультантами
            sell(rnd.nextBoolean() ? appleWatch : xiaomiWatch, ivanov, lenina,
                rnd.nextBoolean() ? cash : card, monthBack, rnd); // Иванов: базовые продажи
            sell(galaxyWatch, petrov, mir, rnd.nextBoolean() ? cash : card, monthBack, rnd);
            if (monthBack % 2 == 0) {
                sell(xiaomiWatch, sidorova, digital, rnd.nextBoolean() ? cash : card, monthBack, rnd);
            }
            if (monthBack % 3 == 0) {
                sell(rnd.nextBoolean() ? appleWatch : xiaomiWatch, ivanov, lenina,
                    rnd.nextBoolean() ? cash : card, monthBack, rnd); // доп. продажи Иванова
            }

            // прочие продажи другими сотрудниками
            for (int i = 0; i < 3; i++) {
                Employee seller = sellers[rnd.nextInt(sellers.length)];
                purchases.save(new Purchase(goods[rnd.nextInt(goods.length)], seller, seller.getShop(),
                    rnd.nextBoolean() ? cash : card,
                    now.minusMonths(monthBack).minusDays(rnd.nextInt(20)).minusHours(rnd.nextInt(10))));
            }
        }
    }

    private void sell(Electronics p, Employee e, Shop s, PurchaseType t, int monthsAgo, Random rnd) {
        purchases.save(new Purchase(p, e, s, t, LocalDateTime.now().minusMonths(monthsAgo).minusDays(rnd.nextInt(25)).minusHours(rnd.nextInt(9))));
    }

    @Test
    void contextStartsAndSeedsDemoData() {
        assertTrue(positions.count() >= 5, "справочник \"Должности\" должен быть заполнен");
        assertTrue(electronics.count() >= 5, "реестр электротоваров должен быть заполнен");
    }

    @Test
    void bestEmployeesReportsReturnData() {
        List<BestEmployeeDto> byCount = reportService.bestEmployeesByPosition("count");
        assertFalse(byCount.isEmpty());
        assertTrue(byCount.get(0).getSoldCount() > 0);

        List<BestEmployeeDto> bySum = reportService.bestEmployeesByPosition("sum");
        assertFalse(bySum.isEmpty());
        assertTrue(bySum.get(0).getSoldSum().signum() > 0);

        // у каждой должности — свой лучший сотрудник
        long distinctPositions = byCount.stream().map(BestEmployeeDto::getPositionId).distinct().count();
        assertEquals(byCount.size(), distinctPositions);
    }

    @Test
    void bestSmartWatchSellerIsIvanov() {
        List<BestEmployeeDto> sellers = reportService.bestSmartWatchSellers();
        assertFalse(sellers.isEmpty(), "должен найтись лучший продавец умных часов");
        assertTrue(sellers.get(0).getFullName().startsWith("Иванов"),
            "лучшим должен быть Иванов, получено: " + sellers.get(0).getFullName());
        if (sellers.size() > 1) {
            assertTrue(sellers.get(0).getSoldCount() >= sellers.get(1).getSoldCount(),
                "список должен быть отсортирован по убыванию");
        }
    }

    @Test
    void cashTotalIsPositive() {
        CashTotalDto total = reportService.cashTotal(null);
        assertNotNull(total.getAmount());
        assertTrue(total.getAmount().signum() > 0, "выручка наличными должна быть положительной");

        Long shopId = shops.findAll().get(0).getId();
        CashTotalDto perShop = reportService.cashTotal(shopId);
        assertNotNull(perShop.getAmount());
        assertEquals(shopId, perShop.getShopId());
    }

    @Test
    void purchaseWithoutStockIsRejectedAndNotSaved() {
        Electronics product = electronics.findAll().get(0);
        Shop shopWithoutStock = shops.save(new Shop("Магазин без остатков", "тестовый адрес"));
        Employee employee = employees.findAll().get(0);
        var type = purchaseTypes.findAll().get(0);

        PurchaseDto dto = new PurchaseDto();
        dto.setElectronicsId(product.getId());
        dto.setEmployeeId(employee.getId());
        dto.setShopId(shopWithoutStock.getId());
        dto.setPurchaseTypeId(type.getId());

        long before = purchases.count();
        OutOfStockException ex = assertThrows(OutOfStockException.class, () -> purchaseService.create(dto));
        assertTrue(ex.getMessage().contains("нет в наличии"), "сообщение должно говорить об отсутствии товара");
        assertEquals(before, purchases.count(), "запись о покупке не должна создаваться");
    }

    @Test
    void successfulPurchaseDecrementsStock() {
        ShopStock st = stocks
            .findAll()
            .stream()
            .filter(s -> s.getQuantity() != null && s.getQuantity() > 1)
            .findFirst().orElseThrow();

        Long stockId = st.getId();
        Long shopId = st.getShop().getId();
        Electronics product = electronics.findById(st.getElectronics().getId()).orElseThrow();
        assertNotEquals(Boolean.TRUE, product.getArchived(), "товар должен быть в продаже");

        int shopQtyBefore = st.getQuantity();
        int totalQtyBefore = product.getQuantity();

        PurchaseDto dto = new PurchaseDto();
        dto.setElectronicsId(product.getId());
        dto.setEmployeeId(employees.findAll().get(0).getId());
        dto.setShopId(shopId);
        dto.setPurchaseTypeId(purchaseTypes.findAll().get(0).getId());
        dto.setDateTime(LocalDateTime.now());

        var created = purchaseService.create(dto);
        assertNotNull(created.getId());

        ShopStock after = stocks.findById(stockId).orElseThrow();
        assertEquals(shopQtyBefore - 1, after.getQuantity(), "остаток магазина должен уменьшиться на 1");
        assertEquals(totalQtyBefore - 1, electronics.findById(product.getId()).orElseThrow().getQuantity(),
            "общий остаток должен уменьшиться на 1");
    }

    @Test
    void zipImportFromSampleArchive() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/import/sample.zip")) {
            assertNotNull(is, "тестовый архив /import/sample.zip должен существовать");
            MockMultipartFile file = new MockMultipartFile(
                "file", "sample.zip", "application/zip", is.readAllBytes());
            ImportResultDto result = csvImportService.importZip(file);

            assertTrue(result.isSuccess(), "импорт не должен иметь ошибок: " + dump(result));

            assertTrue(shops.findByNameIgnoreCase("ТехноСити").isPresent(), "магазин из архива должен быть добавлен");
            assertTrue(electronics.findByNameIgnoreCase("Huawei MatePad SE").isPresent(),
                "товар из архива должен быть добавлен");
            assertTrue(purchases.count() > 0);
        }
    }

    @Test
    void csvImportSingleTablePositions() {
        MockMultipartFile file = csvFile("positions.csv",
            "ID;НАИМЕНОВАНИЕ\n200;Инженер поддержки\n");
        ImportResultDto result = csvImportService.importCsv(file, "positions");

        assertTrue(result.isSuccess(), "импорт позиций не должен иметь ошибок: " + dump(result));
        assertTrue(positions.findByNameIgnoreCase("Инженер поддержки").isPresent(),
            "должность из CSV должна быть добавлена");
        assertEquals(1, result.getFiles().size());
    }

    @Test
    void csvImportEmployeesResolvesReferencesByName() {
        MockMultipartFile file = csvFile("employees.csv",
            "ФАМИЛИЯ;ИМЯ;ОТЧЕСТВО;ДАТА_РОЖДЕНИЯ;ДОЛЖНОСТЬ;МАГАЗИН;ПОЛ\n"
            + "Федоров;Федор;Федорович;1990-05-05;Кассир;ТехноМир;м\n");
        ImportResultDto result = csvImportService.importCsv(file, "employees");

        assertTrue(result.isSuccess(), "импорт сотрудников не должен иметь ошибок: " + dump(result));
        assertTrue(employees.findByLastNameIgnoreCase("Петров").isPresent(),
            "сотрудник из CSV должен быть добавлен");
    }

    @Test
    void csvImportStocksUpdatesExistingPair() {
        ShopStock st = stocks.findAll().get(0);
        String productName = electronics.findById(st.getElectronics().getId()).orElseThrow().getName();
        String shopName = shops.findById(st.getShop().getId()).orElseThrow().getName();

        MockMultipartFile file = csvFile("stocks.csv",
            "ЭЛЕКТРОТОВАР_ID;МАГАЗИН_ID;КОЛИЧЕСТВО\n"
            + productName + ";" + shopName + ";42\n");
        ImportResultDto result = csvImportService.importCsv(file, "stocks");

        assertTrue(result.isSuccess(), "импорт остатков не должен иметь ошибок: " + dump(result));
        ShopStock after = stocks
            .findByElectronicsIdAndShopId(st.getElectronics().getId(), st.getShop().getId())
            .orElseThrow();
        assertEquals(42, after.getQuantity(), "остаток по паре \"товар-магазин\" должен обновиться");
    }

    @Test
    void csvImportUnknownTableFailsGracefully() {
        MockMultipartFile file = csvFile("nope.csv", "A;B\n1;2\n");
        ImportResultDto result = csvImportService.importCsv(file, "unknown_table");

        assertFalse(result.isSuccess(), "неизвестная таблица должна вернуть ошибку");
        assertTrue(result.getMessage().contains("Неизвестная таблица"),
            "сообщение должно называть проблему: " + result.getMessage());
    }

    private MockMultipartFile csvFile(String fileName, String content) {
        return new MockMultipartFile("file", fileName, "text/csv",
            content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String dump(ImportResultDto r) {
        StringBuilder sb = new StringBuilder(r.getMessage() == null ? "" : r.getMessage());
        r.getFiles()
            .forEach(
                f -> sb
                    .append(" | ")
                    .append(f.getFile())
                    .append(": ")
                    .append(f.getAdded())
                    .append("/")
                    .append(f.getErrors()));
        return sb.toString();
    }
}
