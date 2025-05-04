package com.ueadmission.payment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.application.Platform;

/**
 * A simplified version of SSLCommerz payment integration for demo purposes.
 * In a production environment, this would make actual API calls to the SSLCommerz gateway.
 */
public class SSLCommerzPayment {
    
    private static final Logger LOGGER = Logger.getLogger(SSLCommerzPayment.class.getName());
    
    // Sandbox store credentials - provided by SSLCommerz
    private static final String STORE_ID = "phant681795f05a0fb";
    private static final String STORE_PASSWORD = "phant681795f05a0fb@ssl";
    
    // SSLCommerz Sandbox API URLs
    private static final String SESSION_API_URL = "https://sandbox.sslcommerz.com/gwprocess/v3/api.php";
    private static final String VALIDATION_API_URL = "https://sandbox.sslcommerz.com/validator/api/validationserverAPI.php";
    private static final String MERCHANT_PANEL_URL = "https://sandbox.sslcommerz.com/manage/";
    
    private final double amount;
    private final String productInfo;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private final Map<String, String> additionalParams = new HashMap<>();
    
    /**
     * Create a new payment with required parameters
     * 
     * @param amount The payment amount
     * @param productInfo Brief product information
     */
    public SSLCommerzPayment(double amount, String productInfo) {
        this.amount = amount;
        this.productInfo = productInfo;
    }
    
    /**
     * Set customer information using a builder pattern
     * 
     * @param name Customer name
     * @param email Customer email
     * @param phone Customer phone
     * @return This payment object for chaining
     */
    public SSLCommerzPayment withCustomer(String name, String email, String phone) {
        this.customerName = name;
        this.customerEmail = email;
        this.customerPhone = phone;
        return this;
    }
    
    /**
     * Add additional parameter
     * 
     * @param key Parameter key
     * @param value Parameter value
     * @return This payment object for chaining
     */
    public SSLCommerzPayment withParam(String key, String value) {
        additionalParams.put(key, value);
        return this;
    }
    
    /**
     * Start the payment process
     * 
     * @param callback Callback that will be invoked when payment process completes
     */
    public void startPayment(Consumer<PaymentResult> callback) {
        LOGGER.info("Starting payment for " + customerName + " (" + customerEmail + ") amount: " + amount);
        
        // Generate a unique transaction ID
        String transactionId = generateTransactionId();
        
        LOGGER.info("Generated transaction ID: " + transactionId);
        
        // Create and show payment window
        Platform.runLater(() -> {
            SSLCommerzPaymentWindow paymentWindow = new SSLCommerzPaymentWindow(
                    transactionId, 
                    amount,
                    "BDT",
                    customerName,
                    customerEmail,
                    customerPhone,
                    productInfo,
                    additionalParams);
            
            paymentWindow.setCallback(result -> {
                if (callback != null) {
                    callback.accept(result);
                }
            });
            
            paymentWindow.show();
        });
    }
    
    /**
     * Generate a unique transaction ID for this payment
     */
    private String generateTransactionId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        
        // Add a random number to ensure uniqueness
        int randomNum = new Random().nextInt(10000);
        
        // Using the store name prefix as suggested in SSLCommerz documentation
        return "UIU" + timestamp + randomNum;
    }
    
    /**
     * Payment result data
     */
    public static class PaymentResult {
        private final boolean successful;
        private final String transactionId;
        private final String message;
        private final String bankTransactionId;
        
        public PaymentResult(boolean successful, String transactionId, String message, String bankTransactionId) {
            this.successful = successful;
            this.transactionId = transactionId;
            this.message = message;
            this.bankTransactionId = bankTransactionId;
        }
        
        public boolean isSuccessful() {
            return successful;
        }
        
        public String getTransactionId() {
            return transactionId;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getBankTransactionId() {
            return bankTransactionId;
        }
    }
}