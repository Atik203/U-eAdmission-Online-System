package com.ueadmission.payment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.logging.Logger;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Simplified SSLCommerz payment window UI for demonstration purposes.
 * In a real application, this would redirect to the SSLCommerz payment gateway.
 */
public class SSLCommerzPaymentWindow {
    private static final Logger LOGGER = Logger.getLogger(SSLCommerzPaymentWindow.class.getName());
    
    private final String transactionId;
    private final double amount;
    private final String currency;
    private final String customerName;
    private final String customerEmail;
    private final String customerPhone;
    private final String productInfo;
    private final Map<String, String> additionalParams;
    
    private Consumer<SSLCommerzPayment.PaymentResult> callback;
    private Stage stage;
    private MFXTextField cardNumberField;
    private MFXTextField nameOnCardField;
    private MFXTextField expiryField;
    private MFXTextField cvvField;
    
    /**
     * Create a new payment window
     * 
     * @param transactionId Unique transaction ID
     * @param amount Payment amount
     * @param currency Currency code
     * @param customerName Customer name
     * @param customerEmail Customer email
     * @param customerPhone Customer phone
     * @param productInfo Product description
     * @param additionalParams Additional parameters
     */
    public SSLCommerzPaymentWindow(
            String transactionId,
            double amount,
            String currency,
            String customerName,
            String customerEmail,
            String customerPhone,
            String productInfo,
            Map<String, String> additionalParams) {
        
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.productInfo = productInfo;
        this.additionalParams = additionalParams;
    }
    
    /**
     * Set the callback for payment completion
     * 
     * @param callback Callback that will be invoked when payment completes
     */
    public void setCallback(Consumer<SSLCommerzPayment.PaymentResult> callback) {
        this.callback = callback;
    }
    
    /**
     * Show the payment window
     */
    public void show() {
        // Create a new stage for the payment window
        stage = new Stage();
        stage.setTitle("SSLCommerz Payment Gateway");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setMinWidth(900);  // Reduced from 1024
        stage.setMinHeight(650); // Reduced from 700
        
        // Create the main layout
        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().addAll("payment-window", "ssl-commerz-root");
        
        // Header with logo
        HBox header = createHeader();
        borderPane.setTop(header);
        
        // Payment form with scroll pane
        VBox content = createContent();
        
        // Create a scroll pane and set the content
        MFXScrollPane scrollPane = new MFXScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("payment-scroll-pane");
        
        borderPane.setCenter(scrollPane);
        
        // Footer with payment buttons
        HBox footer = createFooter();
        borderPane.setBottom(footer);
        
        // Create the scene and set stylesheets
        Scene scene = new Scene(borderPane);
        scene.getStylesheets().add(getClass().getResource("/com.ueadmission/common.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/com.ueadmission/payment/payment.css").toExternalForm());
        
        // Set the scene and show the stage
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * Create the header with SSLCommerz logo
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("payment-header");
        
        // SSLCommerz Logo (placeholder - in a real app would use an actual image)
        Label logoLabel = new Label("SSLCommerz");
        logoLabel.getStyleClass().add("logo-text");
        
        // Transaction ID and date
        VBox transactionInfo = new VBox(5);
        transactionInfo.setAlignment(Pos.CENTER_RIGHT);
        
        Label transactionLabel = new Label("Transaction ID: " + transactionId);
        transactionLabel.getStyleClass().add("transaction-id");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String dateTimeStr = LocalDateTime.now().format(formatter);
        Label dateLabel = new Label(dateTimeStr);
        dateLabel.getStyleClass().add("transaction-date");
        
        transactionInfo.getChildren().addAll(transactionLabel, dateLabel);
        
        // Add spacing between logo and transaction info
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(logoLabel, spacer, transactionInfo);
        
        return header;
    }
    
    /**
     * Create the main content with payment details and form
     */
    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.getStyleClass().add("payment-content");
        
        // Payment Summary
        VBox summaryBox = new VBox(10);
        summaryBox.getStyleClass().add("payment-summary");
        summaryBox.setPadding(new Insets(15));
        
        Label summaryTitle = new Label("Payment Summary");
        summaryTitle.getStyleClass().add("section-title");
        
        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(15);
        summaryGrid.setVgap(8);
        
        Label merchantLabel = new Label("Merchant:");
        merchantLabel.getStyleClass().add("summary-label");
        Label merchantValue = new Label("United International University");
        merchantValue.getStyleClass().add("summary-value");
        
        Label amountLabel = new Label("Amount:");
        amountLabel.getStyleClass().add("summary-label");
        Label amountValue = new Label(String.format("%.2f %s", amount, currency));
        amountValue.getStyleClass().add("summary-value");
        
        Label productLabel = new Label("Purpose:");
        productLabel.getStyleClass().add("summary-label");
        Label productValue = new Label(productInfo);
        productValue.getStyleClass().add("summary-value");
        
        Label customerLabel = new Label("Customer:");
        customerLabel.getStyleClass().add("summary-label");
        Label customerValue = new Label(customerName);
        customerValue.getStyleClass().add("summary-value");
        
        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().add("summary-label");
        Label emailValue = new Label(customerEmail);
        emailValue.getStyleClass().add("summary-value");
        
        // Add to grid
        summaryGrid.add(merchantLabel, 0, 0);
        summaryGrid.add(merchantValue, 1, 0);
        summaryGrid.add(amountLabel, 0, 1);
        summaryGrid.add(amountValue, 1, 1);
        summaryGrid.add(productLabel, 0, 2);
        summaryGrid.add(productValue, 1, 2);
        summaryGrid.add(customerLabel, 0, 3);
        summaryGrid.add(customerValue, 1, 3);
        summaryGrid.add(emailLabel, 0, 4);
        summaryGrid.add(emailValue, 1, 4);
        
        summaryBox.getChildren().addAll(summaryTitle, summaryGrid);
        
        // Payment Options
        VBox paymentOptionsBox = new VBox(15);
        paymentOptionsBox.getStyleClass().add("payment-options");
        paymentOptionsBox.setPadding(new Insets(15));
        
        Label paymentOptionsTitle = new Label("Payment Method");
        paymentOptionsTitle.getStyleClass().add("section-title");
        
        MFXComboBox<String> paymentMethodCombo = new MFXComboBox<>();
        paymentMethodCombo.getItems().addAll(
                "Credit / Debit Card", 
                "Mobile Banking", 
                "Internet Banking"
        );
        paymentMethodCombo.setValue("Credit / Debit Card");
        paymentMethodCombo.setPrefWidth(300);
        
        // Card details form
        VBox cardDetailsBox = new VBox(15);
        cardDetailsBox.getStyleClass().add("card-details");
        cardDetailsBox.setPadding(new Insets(15, 0, 0, 0));
        
        Label cardDetailsTitle = new Label("Card Details");
        cardDetailsTitle.getStyleClass().add("subsection-title");
        
        cardNumberField = new MFXTextField();
        cardNumberField.setFloatingText("Card Number");
        cardNumberField.setPrefWidth(300);
        cardNumberField.setText("4111 1111 1111 1111"); // Demo card number
        
        nameOnCardField = new MFXTextField();
        nameOnCardField.setFloatingText("Name on Card");
        nameOnCardField.setPrefWidth(300);
        nameOnCardField.setText(customerName);
        
        HBox cardExtraDetails = new HBox(10);
        
        expiryField = new MFXTextField();
        expiryField.setFloatingText("Expiry (MM/YY)");
        expiryField.setPrefWidth(140);
        expiryField.setText("12/25");
        
        cvvField = new MFXTextField();
        cvvField.setFloatingText("CVV");
        cvvField.setPrefWidth(140);
        cvvField.setText("123");
        
        cardExtraDetails.getChildren().addAll(expiryField, cvvField);
        
        // Bank logos
        HBox bankLogos = new HBox(15);
        bankLogos.setPadding(new Insets(20, 0, 0, 0));
        bankLogos.setAlignment(Pos.CENTER_LEFT);
        
        // These would be actual card brand logos in a real app
        Label visaLabel = new Label("VISA");
        visaLabel.getStyleClass().add("card-logo");
        
        Label mastercardLabel = new Label("MASTERCARD");
        mastercardLabel.getStyleClass().add("card-logo");
        
        Label amexLabel = new Label("AMEX");
        amexLabel.getStyleClass().add("card-logo");
        
        bankLogos.getChildren().addAll(visaLabel, mastercardLabel, amexLabel);
        
        cardDetailsBox.getChildren().addAll(
                cardDetailsTitle, 
                cardNumberField, 
                nameOnCardField, 
                cardExtraDetails,
                bankLogos
        );
        
        paymentOptionsBox.getChildren().addAll(
                paymentOptionsTitle,
                paymentMethodCombo,
                cardDetailsBox
        );
        
        // Add all sections to content
        content.getChildren().addAll(summaryBox, new Separator(), paymentOptionsBox);
        
        return content;
    }
    
    /**
     * Create the footer with payment buttons
     */
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(15, 25, 25, 25));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getStyleClass().add("payment-footer");
        
        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.getStyleClass().add("cancel-button");
        cancelButton.setPrefWidth(120);
        
        MFXButton payButton = new MFXButton(String.format("Pay %.2f %s", amount, currency));
        payButton.getStyleClass().add("pay-button");
        payButton.setPrefWidth(180);
        
        // Set up actions
        cancelButton.setOnAction(e -> {
            // Close window without payment
            if (callback != null) {
                SSLCommerzPayment.PaymentResult result = new SSLCommerzPayment.PaymentResult(
                        false,
                        transactionId,
                        "Payment cancelled by user",
                        ""
                );
                callback.accept(result);
            }
            stage.close();
        });
        
        payButton.setOnAction(e -> {
            // Simulate successful payment
            processPayment();
        });
        
        footer.getChildren().addAll(cancelButton, payButton);
        
        return footer;
    }
    
    /**
     * Process the payment and show result
     */
    private void processPayment() {
        // Basic validation
        if (cardNumberField.getText().isEmpty() || 
            nameOnCardField.getText().isEmpty() ||
            expiryField.getText().isEmpty() ||
            cvvField.getText().isEmpty()) {
            
            showMessage("Please fill in all card details");
            return;
        }
        
        // Simple credit card format validation (for demo)
        String cardNumber = cardNumberField.getText().replaceAll("\\s+", "");
        if (cardNumber.length() < 13 || cardNumber.length() > 19 || !cardNumber.matches("\\d+")) {
            showMessage("Invalid card number format");
            return;
        }
        
        // Show processing message
        showMessage("Processing payment...");
        
        // Simulate network delay (1.5 seconds)
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                
                // Generate a random bank transaction ID
                String bankTxnId = "BNK" + new Random().nextInt(1000000);
                
                // Create success result
                SSLCommerzPayment.PaymentResult result = new SSLCommerzPayment.PaymentResult(
                        true,
                        transactionId,
                        "Payment processed successfully",
                        bankTxnId
                );
                
                // Close window and notify callback on FX thread
                Platform.runLater(() -> {
                    if (callback != null) {
                        callback.accept(result);
                    }
                    stage.close();
                });
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Show a temporary message in the window
     */
    private void showMessage(String message) {
        // In a real app, this would display a proper message overlay
        System.out.println("Payment window message: " + message);
    }
}