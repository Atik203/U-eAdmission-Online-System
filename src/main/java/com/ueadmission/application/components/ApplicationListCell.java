package com.ueadmission.application.components;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import com.ueadmission.application.model.Application;
import com.ueadmission.application.model.ApplicationStatus;
import com.ueadmission.application.model.PaymentStatus;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Custom list cell for displaying applications in a ListView
 */
public class ApplicationListCell extends ListCell<Application> {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");
    
    private final GridPane gridPane = new GridPane();
    private final Label programLabel = new Label();
    private final Label semesterLabel = new Label();
    private final Label applicationDateLabel = new Label();
    private final Label statusLabel = new Label();
    private final Label paymentStatusLabel = new Label();
    private final Label feeInfoLabel = new Label();
    private final MFXButton viewDetailsButton = new MFXButton("View Details");
    private final MFXButton makePaymentButton = new MFXButton("Make Payment");
    
    private final Consumer<Application> onViewDetails;
    private final Consumer<Application> onMakePayment;
    
    public ApplicationListCell(Consumer<Application> onViewDetails, Consumer<Application> onMakePayment) {
        this.onViewDetails = onViewDetails;
        this.onMakePayment = onMakePayment;
        
        // Configure the grid layout
        gridPane.setHgap(15);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(15));
        gridPane.getStyleClass().add("application-cell");
        
        // Configure the labels
        programLabel.getStyleClass().add("program-label");
        semesterLabel.getStyleClass().add("semester-label");
        applicationDateLabel.getStyleClass().add("date-label");
        statusLabel.getStyleClass().add("status-label");
        paymentStatusLabel.getStyleClass().add("payment-status-label");
        feeInfoLabel.getStyleClass().add("fee-info-label");
        
        // Configure the buttons
        viewDetailsButton.getStyleClass().add("view-details-button");
        makePaymentButton.getStyleClass().add("make-payment-button");
        
        // Set up button actions
        viewDetailsButton.setOnAction(e -> {
            Application item = getItem();
            if (item != null && onViewDetails != null) {
                onViewDetails.accept(item);
            }
        });
        
        makePaymentButton.setOnAction(e -> {
            Application item = getItem();
            if (item != null && onMakePayment != null) {
                onMakePayment.accept(item);
            }
        });
        
        // Left side: Program info
        VBox programInfo = new VBox(5);
        programInfo.getChildren().addAll(programLabel, semesterLabel, applicationDateLabel);
        
        // Right side: Status info
        VBox statusInfo = new VBox(5);
        statusInfo.getChildren().addAll(statusLabel, paymentStatusLabel, feeInfoLabel);
        statusInfo.setAlignment(Pos.TOP_RIGHT);
        
        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(viewDetailsButton, makePaymentButton);
        buttonBox.setAlignment(Pos.CENTER);
        
        // Add components to the grid
        gridPane.add(programInfo, 0, 0);
        gridPane.add(statusInfo, 1, 0);
        gridPane.add(buttonBox, 0, 1, 2, 1);
        
        // Configure grid constraints
        GridPane.setHgrow(programInfo, Priority.ALWAYS);
        
        // Set the cell's graphic
        setText(null);
    }
    
    @Override
    protected void updateItem(Application application, boolean empty) {
        super.updateItem(application, empty);
        
        if (empty || application == null) {
            setGraphic(null);
        } else {
            // Update program information
            programLabel.setText(application.getProgramName());
            semesterLabel.setText("Semester: " + application.getSemesterName());
            applicationDateLabel.setText("Applied: " + application.getApplicationDate().format(DATE_FORMATTER));
            
            // Update status information
            ApplicationStatus status = application.getStatus();
            statusLabel.setText("Status: " + status.getDisplayName());
            statusLabel.getStyleClass().setAll("status-label", status.getStyleClass());
            
            PaymentStatus paymentStatus = application.getPaymentStatus();
            paymentStatusLabel.setText("Payment: " + paymentStatus.getDisplayName());
            paymentStatusLabel.getStyleClass().setAll("payment-status-label", paymentStatus.getStyleClass());
            
            // Set fee information
            feeInfoLabel.setText(String.format("Fee: $%.2f / Paid: $%.2f", 
                                              application.getTotalFee(), 
                                              application.getPaidAmount()));
            
            // Configure payment button visibility
            boolean showPaymentButton = application.getStatus() != ApplicationStatus.REJECTED 
                                      && !application.isPaymentComplete();
            makePaymentButton.setVisible(showPaymentButton);
            makePaymentButton.setManaged(showPaymentButton);
            
            setGraphic(gridPane);
        }
    }
}