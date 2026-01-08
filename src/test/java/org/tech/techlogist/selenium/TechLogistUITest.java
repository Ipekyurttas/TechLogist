package org.tech.techlogist.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver; // ÖNEMLİ: RemoteWebDriver eklendi
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TechLogistUITest {

    private WebDriver driver;
    private WebDriverWait wait;

    private final String BASE_URL = "http://techlogist_app:8080";
    private final String GRID_URL = "http://selenium-chrome:4444/wd/hub";

    @BeforeAll
    static void waitForApp() throws Exception {
        String health = "http://techlogist_app:8080/login";
        int attempts = 30;

        System.out.println("⌛ Bekleniyor: Uygulama ayaga kalksin...");

        while (attempts-- > 0) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(health).openConnection();
                conn.setConnectTimeout(2000);
                if (conn.getResponseCode() < 500) {
                    System.out.println("🚀 Uygulama hazir!");
                    return;
                }
            } catch (Exception ignored) {}
            Thread.sleep(2000);
        }
        fail("❌ Uygulama zamanında ayağa kalkmadı!");
    }

    @BeforeEach
    void setUp() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--disable-gpu", "--no-sandbox",
                "--disable-dev-shm-usage", "--window-size=1920,1080");

        driver = new RemoteWebDriver(new URL(GRID_URL), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        registerUsers();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    private void registerUsers() {
        register("ipek", "ipek@techlogist.com", "123", "ADMIN");
        register("mert", "mert@techlogist.com", "123", "CUSTOMER");
    }

    private void register(String u, String e, String p, String r) {
        driver.get(BASE_URL + "/register");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
            driver.findElement(By.id("username")).sendKeys(u);
            driver.findElement(By.id("email")).sendKeys(e);
            driver.findElement(By.id("password")).sendKeys(p);
            driver.findElement(By.id("role")).sendKeys(r);
            driver.findElement(By.cssSelector("button.btn")).click();
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (Exception ignored) {
            System.out.println("⚠ Kullanıcı zaten kayıtlı olabilir: " + u);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Admin Giriş ve Yönlendirme Testi")
    void testAdminLoginRedirect() {
        loginAsAdmin();
        assertTrue(driver.getCurrentUrl().contains("/admin"), "Admin paneline yönlendirme başarısız!");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        assertTrue(header.getText().contains("Yönetim"));
    }

    @Test
    @Order(2)
    @DisplayName("Admin Ürün Ekleme Testi")
    void testAdminAddProduct() {
        loginAsAdmin();
        waitForElement(By.id("pName"));
        driver.findElement(By.id("pName")).sendKeys("Jenkins Test Ürünü");
        driver.findElement(By.id("pDesc")).sendKeys("Pipeline üzerinden otomatik eklendi.");
        driver.findElement(By.id("pPrice")).sendKeys("25000");
        driver.findElement(By.id("pCatId")).sendKeys("1");
        driver.findElement(By.id("pStockQty")).sendKeys("100");
        driver.findElement(By.id("pMinStock")).sendKeys("5");

        jsClick(driver.findElement(By.cssSelector("#product-tab button.admin-btn")));
        alertTextControl("başarıyla oluşturuldu");
    }

    @Test
    @Order(3)
    @DisplayName("Müşteri Sipariş Akışı Testi")
    void testCustomerPurchaseFlow() {
        loginUser("mert", "123");
        waitForElement(By.id("nav-products"));
        driver.findElement(By.id("nav-products")).click();

        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".admin-btn")));
        jsClick(addToCartBtn);
        handleSimpleAlert();

        driver.get(BASE_URL + "/cart");
        WebElement checkoutBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[onclick='checkout()']")));
        jsClick(checkoutBtn);

        WebElement creditCardOpt = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Kredi Kartı']")));
        creditCardOpt.click();
        alertTextControl("Ödeme başarılı");
    }

    @Test
    @Order(4)
    @DisplayName("Bildirim Okundu İşaretleme Testi")
    void testUserNotificationAndMarkAsRead() {
        loginUser("mert", "123");
        driver.findElement(By.id("nav-profile")).click();

        try {
            WebElement unreadBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".notification-card.unread button.btn-small")
            ));
            jsClick(unreadBtn);
            WebElement readStatus = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//span[contains(text(), 'Okundu')]")
            ));
            assertTrue(readStatus.isDisplayed());
        } catch (TimeoutException e) {
            System.out.println("Okunmamış bildirim bulunamadı.");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Sipariş İptal Testi")
    void testCustomerOrderCancellation() {
        loginUser("mert", "123");
        driver.findElement(By.id("nav-orders")).click();
        try {
            WebElement cancelBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'İptal Et')]")
            ));
            cancelBtn.click();
            handleSimpleAlert();
            alertTextControl("iptal edildi");
        } catch (TimeoutException e) {
            System.out.println("İptal edilecek sipariş bulunamadı.");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Admin Kategori Oluşturma Testi")
    void testAdminCreateCategory() {
        loginAsAdmin();
        jsClick(driver.findElement(By.xpath("//div[contains(text(), 'Kategori Ekle')]")));
        waitForElement(By.id("cName"));
        driver.findElement(By.id("cName")).sendKeys("Yapay Zeka Donanımları");
        driver.findElement(By.cssSelector("#categoryForm button.admin-btn")).click();
        alertTextControl("Kategori başarıyla eklendi");
    }

    @Test
    @Order(7)
    @DisplayName("Stok Güncelleme Testi")
    void testAdminIncreaseStock() {
        loginAsAdmin();
        jsClick(driver.findElement(By.xpath("//div[contains(text(), 'Stok Güncelle')]")));
        waitForElement(By.id("sProdId"));
        driver.findElement(By.id("sProdId")).sendKeys("1");
        driver.findElement(By.xpath("//button[text()='Sorgula']")).click();
        waitForElement(By.id("updateQty"));
        driver.findElement(By.id("updateQty")).sendKeys("20");
        driver.findElement(By.xpath("//button[text()='Stok Ekle']")).click();
        alertTextControl("Stok başarıyla güncellendi");
    }

    @Test
    @Order(8)
    @DisplayName("Bildirim Gönderme Testi")
    void testAdminSendNotification() {
        loginAsAdmin();
        jsClick(driver.findElement(By.xpath("//div[contains(., 'Bildirim Gönder')]")));
        waitForElement(By.id("nTitle"));
        driver.findElement(By.id("nTitle")).sendKeys("Pipeline Duyurusu");
        driver.findElement(By.id("nMessage")).sendKeys("Selenium testleri başarıyla tamamlanıyor!");
        jsClick(driver.findElement(By.cssSelector("#notifyForm button.admin-btn")));
        alertTextControl("Bildirim gönderildi");
    }

    private void loginAsAdmin() {
        loginUser("ipek", "123");
        wait.until(ExpectedConditions.urlContains("/admin"));
    }

    private void loginUser(String user, String pass) {
        driver.get(BASE_URL + "/login");
        waitForElement(By.id("username"));
        driver.findElement(By.id("username")).sendKeys(user);
        driver.findElement(By.id("password")).sendKeys(pass);
        driver.findElement(By.cssSelector("button.btn")).click();
        try {
            Alert alert = driver.switchTo().alert();
            String text = alert.getText();
            alert.accept();
            fail("Login başarısız: " + text);
        } catch (NoAlertPresentException ignored) {}

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlToBe(BASE_URL + "/"),
                ExpectedConditions.urlContains("/admin")
        ));
    }

    private void waitForElement(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    private void handleSimpleAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
    }

    private void alertTextControl(String expectedText) {
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        String text = alert.getText();
        assertTrue(text.contains(expectedText), "Beklenen alert metni bulunamadı: " + expectedText);
        alert.accept();
    }
}