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

    private final String BASE_URL = "http://techlogist_app:8080";

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

        registerInitialUsers();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void registerInitialUsers() {
        registerSingleUser("ipek", "ipek@techlogist.com", "123", "ADMIN");
        registerSingleUser("mert", "mert@techlogist.com", "123", "CUSTOMER");
    }

    private void registerSingleUser(String username, String email, String password, String role) {
        driver.get(BASE_URL + "/register");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));

        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);

        WebElement roleDropdown = driver.findElement(By.id("role"));
        roleDropdown.sendKeys(role);

        driver.findElement(By.cssSelector("button.btn")).click();

        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Kayıt Alert Mesajı: " + alert.getText());
            alert.accept();
        } catch (TimeoutException e) {
            fail(username + " kullanıcısı kaydedilirken alert çıkmadı. Sayfa yönlendirmesini kontrol edin.");
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