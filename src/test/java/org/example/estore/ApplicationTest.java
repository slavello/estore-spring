package org.example.estore;

import org.example.estore.dto.BestEmployeeDto;
import org.example.estore.dto.CashTotalDto;
import org.example.estore.dto.ImportResultDto;
import org.example.estore.dto.PurchaseDto;
import org.example.estore.entity.Electronics;
import org.example.estore.entity.Employee;
import org.example.estore.entity.Shop;
import org.example.estore.entity.ShopStock;
import org.example.estore.exception.OutOfStockException;
import org.example.estore.repository.*;
import org.example.estore.service.CsvImportService;
import org.example.estore.service.PurchaseService;
import org.example.estore.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

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
    @Autowired private ShopRepository shops;
    @Autowired private ShopStockRepository stocks;
    @Autowired private EmployeeRepository employees;
    @Autowired private PurchaseTypeRepository purchaseTypes;
    @Autowired private PurchaseRepository purchases;

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
        ShopStock st = stocks.findAll().stream()
                .filter(s -> s.getQuantity() != null && s.getQuantity() > 1)
                .findFirst().orElseThrow();

        Long stockId = st.getId();
        Long shopId = st.getShop().getId();
        Electronics product = electronics.findById(st.getElectronics().getId()).orElseThrow();
        assertFalse(Boolean.TRUE.equals(product.getArchived()), "товар должен быть в продаже");

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
                + "Иванов;Иван;Иванович;1990-05-05;Кассир;ТехноМир;м\n");
        ImportResultDto result = csvImportService.importCsv(file, "employees");

        assertTrue(result.isSuccess(), "импорт сотрудников не должен иметь ошибок: " + dump(result));
        assertTrue(employees.findByLastNameIgnoreCase("Иванов").isPresent(),
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
        ShopStock after = stocks.findByElectronicsIdAndShopId(st.getElectronics().getId(), st.getShop().getId())
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
        r.getFiles().forEach(f -> sb.append(" | ").append(f.getFile()).append(": ")
                .append(f.getAdded()).append("/").append(f.getErrors()));
        return sb.toString();
    }
}
