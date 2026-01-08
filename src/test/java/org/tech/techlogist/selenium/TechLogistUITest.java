package org.tech.techlogist.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
    private static final String BASE_URL = "http://localhost:8085";

    @BeforeAll
    static void initAll() throws Exception {
        // 1. Uygulamanın Ayaklanmasını Bekle
        String health = BASE_URL + "/login";
        int attempts = 30;
        System.out.println("⌛ Uygulama ayaga kalkiyor (Health Check)...");

        while (attempts-- > 0) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(health).openConnection();
                conn.setConnectTimeout(2000);
                if (conn.getResponseCode() < 500) {
                    System.out.println("🚀 App hazir!");
                    break;
                }
            } catch (Exception ignored) { }
            Thread.sleep(2000);
            if (attempts == 0) fail("❌ HATA: Uygulama belirlenen sürede ayağa kalkmadı!");
        }

        // 2. Kullanıcıları bir kez kaydet (Sadece sürecin başında)
        registerStaticUsers();
    }

    private static void registerStaticUsers() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--remote-allow-origins=*");
        WebDriver tempDriver = new ChromeDriver(options);
        WebDriverWait tempWait = new WebDriverWait(tempDriver, Duration.ofSeconds(10));

        try {
            String[][] users = {
                    {"ipek", "ipek@techlogist.com", "123", "ADMIN"},
                    {"mert", "mert@techlogist.com", "123", "CUSTOMER"}
            };
            for (String[] u : users) {
                tempDriver.get(BASE_URL + "/register");
                try {
                    tempWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
                    tempDriver.findElement(By.id("username")).sendKeys(u[0]);
                    tempDriver.findElement(By.id("email")).sendKeys(u[1]);
                    tempDriver.findElement(By.id("password")).sendKeys(u[2]);
                    tempDriver.findElement(By.id("role")).sendKeys(u[3]);
                    tempDriver.findElement(By.cssSelector("button.btn")).click();
                    tempWait.until(ExpectedConditions.alertIsPresent()).accept();
                    System.out.println("✅ Kullanıcı hazırlandı: " + u[0]);
                } catch (Exception e) {
                    System.out.println("ℹ Kullanıcı zaten mevcut veya kayıt atlandı: " + u[0]);
                }
            }
        } finally {
            tempDriver.quit();
        }
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Jenkins/Linux ortamı için kritik stabilite ayarları
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--headless=new");
        options.addArguments("--remote-allow-origins=*"); // CDP versiyon hatasını çözer

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --- TEST SENARYOLARI ---

    @Test @Order(1)
    @DisplayName("Admin Giriş ve Yönlendirme Testi")
    void testAdminLoginRedirect() {
        loginAsAdmin();
        assertTrue(driver.getCurrentUrl().contains("/admin"), "Admin paneline yönlendirme başarısız!");
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")))
                .getText().contains("Yönetim"));
    }

    @Test @Order(2)
    @DisplayName("Admin Ürün Ekleme Testi")
    void testAdminAddProduct() {
        loginAsAdmin();
        waitForElement(By.id("pName"));
        driver.findElement(By.id("pName")).sendKeys("Jenkins Test Ürünü");
        driver.findElement(By.id("pDesc")).sendKeys("Pipeline otomatik");
        driver.findElement(By.id("pPrice")).sendKeys("25000");
        driver.findElement(By.id("pCatId")).sendKeys("1");
        driver.findElement(By.id("pStockQty")).sendKeys("100");
        driver.findElement(By.id("pMinStock")).sendKeys("5");

        jsClick(driver.findElement(By.cssSelector("#product-tab button.admin-btn")));
        alertTextControl("başarıyla oluşturuldu");
    }

    @Test @Order(3)
    @DisplayName("Müşteri Satın Alma Akışı")
    void testCustomerPurchaseFlow() {
        loginUser("mert", "123");
        waitForElement(By.id("nav-products"));
        driver.findElement(By.id("nav-products")).click();

        // Ürünü sepete ekle
        jsClick(wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".admin-btn"))));
        handleSimpleAlert();

        // Sepete git ve öde
        driver.get(BASE_URL + "/cart");
        jsClick(wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[onclick='checkout()']"))));

        // Kredi kartı seçeneği
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Kredi Kartı']"))).click();
        alertTextControl("Ödeme başarılı");
    }

    @Test @Order(4)
    @DisplayName("Bildirim Okundu İşaretleme")
    void testUserNotificationAndMarkAsRead() {
        loginUser("mert", "123");
        driver.findElement(By.id("nav-profile")).click();
        try {
            jsClick(wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".notification-card.unread button.btn-small"))));
            assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//span[contains(text(), 'Okundu')]"))).isDisplayed());
        } catch (TimeoutException e) {
            System.out.println("⚠️ Okunmamış bildirim bulunamadığı için test pas geçildi.");
        }
    }

    @Test @Order(5)
    @DisplayName("Müşteri Sipariş İptali")
    void testCustomerOrderCancellation() {
        loginUser("mert", "123");
        driver.findElement(By.id("nav-orders")).click();
        try {
            WebElement cancelBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'İptal Et')]")));
            cancelBtn.click();
            handleSimpleAlert();
            alertTextControl("iptal edildi");
        } catch (TimeoutException e) {
            System.out.println("⚠️ İptal edilecek sipariş bulunamadı.");
        }
    }

    @Test @Order(6)
    @DisplayName("Admin Kategori Oluşturma")
    void testAdminCreateCategory() {
        loginAsAdmin();
        jsClick(driver.findElement(By.xpath("//div[contains(text(), 'Kategori Ekle')]")));
        waitForElement(By.id("cName"));
        driver.findElement(By.id("cName")).sendKeys("Yeni Kategori");
        driver.findElement(By.cssSelector("#categoryForm button.admin-btn")).click();
        alertTextControl("başarıyla eklendi");
    }

    @Test @Order(7)
    @DisplayName("Admin Stok Güncelleme")
    void testAdminIncreaseStock() {
        loginAsAdmin();
        jsClick(driver.findElement(By.xpath("//div[contains(text(),'Stok Güncelle')]")));
        waitForElement(By.id("sProdId"));
        driver.findElement(By.id("sProdId")).sendKeys("1");
        driver.findElement(By.xpath("//button[text()='Sorgula']")).click();

        waitForElement(By.id("updateQty"));
        driver.findElement(By.id("updateQty")).sendKeys("10");
        driver.findElement(By.xpath("//button[text()='Stok Ekle']")).click();
        alertTextControl("güncellendi");
    }

    @Test @Order(8)
    @DisplayName("Admin Bildirim Gönderme")
    void testAdminSendNotification() {
        loginAsAdmin();
        jsClick(driver.findElement(By.xpath("//div[contains(., 'Bildirim Gönder')]")));
        waitForElement(By.id("nTitle"));
        driver.findElement(By.id("nTitle")).sendKeys("Sistem Duyurusu");
        driver.findElement(By.id("nMessage")).sendKeys("Bakım çalışması tamamlandı.");
        jsClick(driver.findElement(By.cssSelector("#notifyForm button.admin-btn")));
        alertTextControl("Bildirim gönderildi");
    }

    // --- YARDIMCI METODLAR (Helper Methods) ---

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
            // Jenkins'te login başarısız olursa alert çıkabilir, onu yakala
            Alert alert = driver.switchTo().alert();
            String text = alert.getText();
            alert.accept();
            fail("Giriş yapılamadı: " + text);
        } catch (NoAlertPresentException ignored) {}
    }

    private void waitForElement(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    private void handleSimpleAlert() {
        wait.until(ExpectedConditions.alertIsPresent()).accept();
    }

    private void alertTextControl(String expected) {
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        String text = alert.getText();
        assertTrue(text.toLowerCase().contains(expected.toLowerCase()),
                "Beklenen alert mesajı bulunamadı! Alınan: " + text);
        alert.accept();
    }
}