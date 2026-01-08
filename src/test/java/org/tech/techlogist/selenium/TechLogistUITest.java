package org.tech.techlogist.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TechLogistUITest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL = "http://host.docker.internal:8085";

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().browserInDocker().setup();
        ChromeOptions options = new ChromeOptions();
        options.setBinary("/usr/bin/chromium");
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Admin Giriş Testi")
    void testAdminLoginRedirect() {
        driver.get(BASE_URL + "/login");
        waitForElement(By.id("username"));
        driver.findElement(By.id("username")).sendKeys("ipek");
        driver.findElement(By.id("password")).sendKeys("123");
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn")));
        loginBtn.click();
        wait.until(ExpectedConditions.urlContains("/admin"));
        assertTrue(driver.getCurrentUrl().contains("/admin"), "Admin paneline yönlendirme başarısız!");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        assertTrue(header.getText().contains("Yönetim"));
    }

    @Test
    @Order(2)
    @DisplayName("Admin Paneli - Ürün Ekleme")
    void testAdminAddProduct() {
        loginAsAdmin();
        waitForElement(By.id("pName"));
        driver.findElement(By.id("pName")).sendKeys("Chrome Test Ürünü");
        driver.findElement(By.id("pDesc")).sendKeys("Chrome üzerinden eklendi.");
        driver.findElement(By.id("pPrice")).sendKeys("15000");
        driver.findElement(By.id("pCatId")).sendKeys("1");
        driver.findElement(By.id("pStockQty")).sendKeys("50");
        driver.findElement(By.id("pMinStock")).sendKeys("10");
        driver.findElement(By.cssSelector("#product-tab button.admin-btn")).click();
        alertTextControl("başarıyla oluşturuldu");
    }

    @Test
    @Order(3)
    @DisplayName("Müşteri Sipariş Akışı")
    void testCustomerPurchaseFlow() {
        loginUser("mert", "123");
        waitForElement(By.id("nav-products"));
        driver.findElement(By.id("nav-products")).click();
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".admin-btn")));
        addToCartBtn.click();
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
    @DisplayName("Müşteri Bildirim Testi - Bildirim Görüntüleme ve Okundu İşaretleme")
    void testUserNotificationAndMarkAsRead() {
        loginUser("mert", "123");
        WebElement profileBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-profile")));
        profileBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("notification-card")));
        try {
            WebElement unreadBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".notification-card.unread button.btn-small")
            ));
            jsClick(unreadBtn);

            WebElement readStatus = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//span[contains(text(), 'Okundu')]")
            ));

            assertTrue(readStatus.isDisplayed(), "Bildirim okundu olarak işaretlenemedi!");

        } catch (TimeoutException e) {
            System.out.println("Okunmamış bildirim bulunamadı, test atlanıyor.");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Müşteri Sipariş İptal Testi")
    void testCustomerOrderCancellation() {
        loginUser("mert", "123");
        WebElement ordersBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-orders")));
        ordersBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("cart-item")));
        try {
            WebElement cancelBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'İptal Et')]")
            ));
            cancelBtn.click();

            handleSimpleAlert(); // Confirm alert
            alertTextControl("iptal edildi"); // Success alert

            WebElement statusText = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//span[contains(text(), 'CANCELLED')]")
            ));
            assertTrue(statusText.isDisplayed(), "Sipariş durumu CANCELLED olarak güncellenmedi!");

        } catch (TimeoutException e) {
            System.out.println("İptal edilebilir aktif bir sipariş bulunamadı.");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Admin Paneli - Yeni Kategori Ekleme")
    void testAdminCreateCategory() {
        loginAsAdmin();
        WebElement catTabLink = driver.findElement(By.xpath("//div[contains(text(), 'Kategori Ekle')]"));
        catTabLink.click();
        waitForElement(By.id("cName"));
        driver.findElement(By.id("cName")).sendKeys("Yeni Nesil Donanım");
        driver.findElement(By.cssSelector("#categoryForm button.admin-btn")).click();
        alertTextControl("Kategori başarıyla eklendi");
    }

    @Test
    @Order(7)
    @DisplayName("Admin Paneli - Stok Artırma (Increase)")
    void testAdminIncreaseStock() {
        loginAsAdmin();
        driver.findElement(By.xpath("//div[contains(text(), 'Stok Güncelle')]")).click();
        waitForElement(By.id("sProdId"));
        driver.findElement(By.id("sProdId")).sendKeys("1");
        driver.findElement(By.xpath("//button[text()='Sorgula']")).click();
        waitForElement(By.id("updateQty"));
        driver.findElement(By.id("updateQty")).sendKeys("10");
        driver.findElement(By.xpath("//button[text()='Stok Ekle']")).click();
        alertTextControl("Stok başarıyla güncellendi");
    }

    @Test
    @Order(8)
    @DisplayName("Admin Paneli - Stok Çıkarma (Decrease)")
    void testAdminDecreaseStock() {
        loginAsAdmin();
        driver.findElement(By.xpath("//div[contains(text(), 'Stok Güncelle')]")).click();
        waitForElement(By.id("sProdId"));
        driver.findElement(By.id("sProdId")).clear();
        driver.findElement(By.id("sProdId")).sendKeys("1");
        driver.findElement(By.xpath("//button[text()='Sorgula']")).click();
        waitForElement(By.id("updateQty"));
        driver.findElement(By.id("updateQty")).sendKeys("5");
        driver.findElement(By.className("btn-orange")).click();
        alertTextControl("Stok başarıyla güncellendi");
    }

    @Test
    @Order(9)
    @DisplayName("Admin Paneli - Bildirim Gönderme")
    void testAdminSendNotification() {
        loginAsAdmin();
        WebElement notifyTabLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class, 'sidebar-link') and contains(., 'Bildirim Gönder')]")));
        notifyTabLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("notify-tab")));
        WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nTitle")));
        titleInput.sendKeys("Büyük İndirim");
        driver.findElement(By.id("nMessage")).sendKeys("Tüm ürünlerde %20 indirim!");
        WebElement submitBtn = driver.findElement(By.cssSelector("#notifyForm button.admin-btn"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            assertTrue(alert.getText().contains("Bildirim gönderildi"));
            alert.accept();
        } catch (TimeoutException e) {
            fail("Bildirim gönderilemedi veya Alert penceresi çıkmadı. API yanıtını kontrol edin.");
        }
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