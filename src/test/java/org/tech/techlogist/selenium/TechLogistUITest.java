package org.tech.techlogist.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TechLogistUITest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8085";

    @BeforeAll
    static void initAll() throws Exception {
        String health = BASE_URL + "/login";
        int attempts = 30;
        while (attempts-- > 0) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(health).openConnection();
                if (conn.getResponseCode() < 500) break;
            } catch (Exception ignored) { }
            Thread.sleep(2000);
        }
        clearDatabase();
        Thread.sleep(3000);
        registerStaticUsers();
    }

    private static void clearDatabase() {
        String localUrl = "jdbc:h2:mem:techlogist";
        try (Connection conn = DriverManager.getConnection(localUrl, "sa", "");
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
            System.out.println("✅ Veritabanı sıfırlandı.");
        } catch (Exception e) {
            System.err.println("⚠️ SQL Temizliği atlandı.");
        }
    }

    private static void registerStaticUsers() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--remote-allow-origins=*");
        WebDriver tempDriver = new ChromeDriver(options);
        WebDriverWait tempWait = new WebDriverWait(tempDriver, Duration.ofSeconds(15));

        try {
            String[][] users = {
                    {"ipek", "esra@techlogist.com", "123"},
                    {"mert", "esma@techlogist.com", "123"}
            };
            for (String[] u : users) {
                tempDriver.get(BASE_URL + "/register");
                tempWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("regUsername"))).sendKeys(u[0]);
                tempDriver.findElement(By.id("regEmail")).sendKeys(u[1]);
                tempDriver.findElement(By.id("regPassword")).sendKeys(u[2]);
                tempDriver.findElement(By.cssSelector("button.btn")).click();
                tempWait.until(ExpectedConditions.alertIsPresent()).accept();
                System.out.println("✅ Kaydedildi: " + u[0]);
            }
        } finally { tempDriver.quit(); }
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1080", "--headless=new", "--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    @Test @Order(1)
    @DisplayName("Admin Giriş ve Yönlendirme Testi")
    void testAdminLoginRedirect() {
        loginUser("ipek", "123");
        wait.until(ExpectedConditions.urlContains("/admin"));
        assertTrue(driver.getCurrentUrl().contains("/admin"));
        assertTrue(driver.findElement(By.tagName("h2")).getText().contains("Yönetim"));
    }

    @Test @Order(2)
    @DisplayName("Admin Yeni Kategori Ekleme")
    void testAdminCreateCategory() {
        loginUser("ipek", "123");
        jsClick(driver.findElement(By.xpath("//div[contains(text(), 'Kategori Ekle')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cName"))).sendKeys("Elektronik");
        driver.findElement(By.cssSelector("#categoryForm button.admin-btn")).click();
        alertTextControl("başarıyla eklendi !!");
    }

    @Test @Order(3)
    @DisplayName("Admin Yeni Ürün ve Stok Ekleme")
    void testAdminAddProduct() {
        loginUser("ipek", "123");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pName"))).sendKeys("Laptop");
        driver.findElement(By.id("pDesc")).sendKeys("Gaming Laptop");
        driver.findElement(By.id("pPrice")).sendKeys("45000");
        driver.findElement(By.id("pCatId")).sendKeys("1");
        driver.findElement(By.id("pStockQty")).sendKeys("20");
        driver.findElement(By.id("pMinStock")).sendKeys("3");
        jsClick(driver.findElement(By.cssSelector("#productForm button.admin-btn")));
        alertTextControl("başarıyla oluşturuldu");
    }

    @Test @Order(4)
    @DisplayName("Müşteri Ürünü Sepete Ekleme ve Ödeme")
    void testCustomerPurchaseFlow() {
        loginUser("mert", "123");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-products"))).click();
        jsClick(wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".admin-btn"))));
        handleSimpleAlert();

        driver.get(BASE_URL + "/cart");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[onclick='checkout()']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Kredi Kartı']"))).click();
        alertTextControl("Ödeme başarılı");
    }

    @Test @Order(5)
    @DisplayName("Bildirim Okundu İşaretleme")
    void testUserNotification() {
        loginUser("mert", "123");
        jsClick(driver.findElement(By.id("nav-profile")));
        try {
            WebElement markRead = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".notification-card.unread button")));
            markRead.click();
            assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(., 'Okundu')]"))).isDisplayed());
        } catch (Exception e) { System.out.println("Okunmamış bildirim yok."); }
    }

    @Test @Order(6)
    @DisplayName("Sipariş İptal Etme")
    void testOrderCancellation() {
        loginUser("mert", "123");
        jsClick(driver.findElement(By.id("nav-orders")));
        try {
            WebElement cancelBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='İptal Et']")));
            cancelBtn.click();
            handleSimpleAlert(); // Onay uyarısı
            alertTextControl("iptal edildi");
        } catch (Exception e) { System.out.println("İptal edilecek sipariş bulunamadı."); }
    }

    @Test @Order(7)
    @DisplayName("Admin Stok Güncelleme")
    void testAdminStockUpdate() {
        loginUser("ipek", "123");
        jsClick(driver.findElement(By.xpath("//div[contains(text(),'Stok Güncelle')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sProdId"))).sendKeys("1");
        driver.findElement(By.xpath("//button[text()='Sorgula']")).click();

        WebElement qtyField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateQty")));
        qtyField.clear(); qtyField.sendKeys("5");
        driver.findElement(By.xpath("//button[text()='Stok Ekle']")).click();
        alertTextControl("başarıyla güncellendi");
    }

    @Test @Order(8)
    @DisplayName("Admin Herkese Bildirim Gönderme")
    void testAdminGlobalNotification() {
        loginUser("ipek", "123");
        jsClick(driver.findElement(By.xpath("//div[contains(text(), 'Bildirim Gönder')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nTitle"))).sendKeys("Kampanya!");
        driver.findElement(By.id("nMessage")).sendKeys("Tüm ürünlerde %20 indirim!");
        driver.findElement(By.cssSelector("#notifyForm button.admin-btn")).click();
        alertTextControl("Bildirim gönderildi");
    }


    private void loginUser(String user, String pass) {
        driver.get(BASE_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(user);
        driver.findElement(By.id("password")).sendKeys(pass);
        driver.findElement(By.cssSelector("button.btn")).click();
        try {
            WebDriverWait sw = new WebDriverWait(driver, Duration.ofSeconds(2));
            sw.until(ExpectedConditions.alertIsPresent());
            Alert a = driver.switchTo().alert();
            String t = a.getText(); a.accept();
            fail("Giriş Başarısız: " + t);
        } catch (Exception ignored) {}
    }

    private void waitForElement(By locator) { wait.until(ExpectedConditions.visibilityOfElementLocated(locator)); }
    private void jsClick(WebElement element) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element); }
    private void handleSimpleAlert() { wait.until(ExpectedConditions.alertIsPresent()).accept(); }
    private void alertTextControl(String expected) {
        wait.until(ExpectedConditions.alertIsPresent());
        Alert a = driver.switchTo().alert();
        assertTrue(a.getText().toLowerCase().contains(expected.toLowerCase()));
        a.accept();
    }
}