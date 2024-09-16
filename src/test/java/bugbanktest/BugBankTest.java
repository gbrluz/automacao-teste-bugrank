package bugbanktest;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BugBankTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() {
        System.setProperty("webdriver.chrome.driver", "C://chromedriver-win64//chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Cenário: Realizar Transferência entre Contas")
    public void realizarTransferenciaEntreContas() {
        driver.get("https://bugbank.netlify.app/");

        createAccount("user1", "user1@email.com", "password123");
        closeModal(driver, wait);
        cleanFields(driver);

        createAccount("user2", "user2@email.com", "password123");
        String[] accountNumber = getAccountNumber(wait);
        closeModal(driver, wait);

        loginUser(wait,"user1@email.com","password123");
        makeTransfer(driver, wait, accountNumber[0], accountNumber[1]);
        closeModal(driver, wait);
        returnFromTransferArea(wait);
        chackBalance(wait, 900.0);
        logout(wait);

        loginUser(wait, "user2@email.com", "password123");
        chackBalance(wait, 100.0);
        logout(wait); 
    }

    private void createAccount(String Name, String email, String password) {
        Actions actions = new Actions(driver);

        driver.findElement(By.xpath("/html/body/div/div/div[2]/div/div[1]/form/div[3]/button[2]")).click();
        driver.findElement(By.xpath("/html/body/div/div/div[2]/div/div[2]/form/div[2]/input")).sendKeys(email);
        driver.findElement(By.xpath("/html/body/div/div/div[2]/div/div[2]/form/div[3]/input")).sendKeys(Name);
        driver.findElement(By.xpath("/html/body/div/div/div[2]/div/div[2]/form/div[4]/div/input")).sendKeys(password);
        driver.findElement(By.xpath("/html/body/div/div/div[2]/div/div[2]/form/div[5]/div/input")).sendKeys(password);
        wait(500);
        driver.findElement(By.xpath("/html/body/div/div/div[2]/div/div[2]/form/div[6]/label/label")).click();

        actions.moveToElement(driver.findElement(By.xpath("/html/body/div/div/div[2]/div/div[2]/form/button"))).click().perform();

        wait(1000);
    }

    private void makeTransfer(WebDriver driver, WebDriverWait wait, String numeroConta, String digitoConta) {
        WebElement transferencia = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-TRANSFERÊNCIA")));
        transferencia.click();

        WebElement inputaccountNumber = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("accountNumber")));
        inputaccountNumber.sendKeys(numeroConta);

        WebElement inputDigito = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("digit")));
        inputDigito.sendKeys(digitoConta);

        WebElement inputValorTransferencia = driver.findElement(By.name("transferValue"));
        inputValorTransferencia.sendKeys("100");

        WebElement inputDescricao = driver.findElement(By.name("description"));
        inputDescricao.sendKeys("100 reais");

        WebElement transferirButton = driver.findElement(By.xpath("//button[contains(text(), 'Transferir agora')]"));
        transferirButton.click();
    }

    public void wait(int milisegundos){
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String[] getAccountNumber(WebDriverWait wait) {
        WebElement modalTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText")));
        String successMessage = modalTextElement.getText();
        String[] parts = successMessage.split(" ");
        String accountInfo = parts[2];
        return accountInfo.split("-");
    }

    private void closeModal(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnCloseModal")));
            closeButton.click();
            System.out.println("Modal fechado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao fechar modal: " + e.getMessage());
        }
    }

    private void cleanFields(WebDriver driver) {
        WebElement inputEmail = driver.findElement(By.xpath("(//input[@name='email'])[2]"));
        inputEmail.clear();

        WebElement inputName = driver.findElement(By.cssSelector("input[name='name']"));
        inputName.clear();

        WebElement inputPassword = driver.findElement(By.xpath("(//input[@name='password'])[2]"));
        inputPassword.clear();

        WebElement inputConfirmarPassword = driver.findElement(By.cssSelector("input[name='passwordConfirmation']"));
        inputConfirmarPassword.clear();
    }

    private void loginUser(WebDriverWait wait, String email, String password) {
        WebElement inputEmailLogin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        inputEmailLogin.sendKeys(email);

        WebElement inputPasswordLogin =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
        inputPasswordLogin.sendKeys(password);

        WebElement acessarButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.style__ContainerButton-sc-1wsixal-0")));
        acessarButton.click();
    }

    private void returnFromTransferArea(WebDriverWait wait) {
        WebElement backButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnBack")));
        backButton.click();
    }

    private void logout(WebDriverWait wait) {
        WebElement exitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnExit")));
        exitButton.click();
    }

    private void chackBalance(WebDriverWait wait, double expectedBalance) {
        WebElement balance = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textBalance")));
        String balanceText = balance.getText();
        String balanceTextValue = balanceText.split("R\\$")[1].trim().replace(".", "").replace(",", ".");
        double balanceValue = Double.parseDouble(balanceTextValue);
        assertEquals(expectedBalance, balanceValue, 0.01, "O saldo após a transferência deve ser R$ " + expectedBalance);
    }



    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

