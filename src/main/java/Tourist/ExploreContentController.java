package Tourist;

import Models.Trek;
import Models.Booking;
import Models.User;
import Session.UserSession;
import Storage.AdminJSONHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import Tourist.TouristBookingController;

public class ExploreContentController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private VBox treksContainer;

    private List<Trek> allTreks;
    private List<Trek> filteredTreks;
    private AdminJSONHandler jsonHandler;

    private String currentUserEmail = "tourist@example.com";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jsonHandler = new AdminJSONHandler();
        setupSearchField();
        loadTreksFromJSON();
        displayTreks(allTreks);
        updateUserEmailFromSession();
    }

    private void updateUserEmailFromSession() {
        try {
            UserSession userSession = UserSession.getInstance();
            User currentUser = userSession.getCurrentUser();

            if (currentUser != null && currentUser.getEmail() != null) {
                currentUserEmail = currentUser.getEmail();
                System.out.println("Updated user email from session: " + currentUserEmail);
            } else {
                System.out.println("No user in session, using default email: " + currentUserEmail);
            }
        } catch (Exception e) {
            System.err.println("Error getting user from session: " + e.getMessage());
            System.out.println("Using default email: " + currentUserEmail);
        }
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTreks(newValue.trim().toLowerCase());
        });
    }

    private void loadTreksFromJSON() {
        try {
            allTreks = jsonHandler.loadTreks();
            System.out.println("Loaded " + allTreks.size() + " treks from JSON");
        } catch (Exception e) {
            System.err.println("Error loading treks from JSON: " + e.getMessage());
            e.printStackTrace();
            allTreks = List.of();
        }
    }

    private void filterTreks(String searchText) {
        if (searchText.isEmpty()) {
            filteredTreks = allTreks;
        } else {
            filteredTreks = allTreks.stream()
                    .filter(trek ->
                            trek.getTrekName().toLowerCase().contains(searchText) ||
                                    trek.getDifficulty().toLowerCase().contains(searchText) ||
                                    trek.getBestSeason().toLowerCase().contains(searchText) ||
                                    String.valueOf(trek.getMaxAltitude()).contains(searchText)
                    )
                    .collect(Collectors.toList());
        }
        displayTreks(filteredTreks);
    }

    private void displayTreks(List<Trek> treks) {
        treksContainer.getChildren().clear();

        if (treks.isEmpty()) {
            Label noResultsLabel = new Label("No treks found matching your search criteria.");
            noResultsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 16px; -fx-padding: 20;");
            treksContainer.getChildren().add(noResultsLabel);
            return;
        }

        for (Trek trek : treks) {
            VBox trekCard = createTrekCard(trek);
            treksContainer.getChildren().add(trekCard);
        }
    }

    private VBox createTrekCard(Trek trek) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 15; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);

        // Trek Header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Trek icon and name
        VBox nameSection = new VBox(5);
        Label trekIcon = new Label("🏔️");
        trekIcon.setStyle("-fx-font-size: 24px;");

        Label nameLabel = new Label(trek.getTrekName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #333333;");

        nameSection.getChildren().addAll(trekIcon, nameLabel);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Badges container (difficulty + discount + altitude warning)
        HBox badgesBox = new HBox(8);
        badgesBox.setAlignment(Pos.CENTER_RIGHT);

        // Add discount badge if trek has discount
        if (trek.hasDiscount()) {
            Label discountBadge = createDiscountBadge(trek);
            badgesBox.getChildren().add(discountBadge);
        }

        // NEW: Add altitude warning badge if trek is above 3000m
        if (trek.isHighAltitude()) {
            Label altitudeWarningBadge = createAltitudeWarningBadge(trek);
            badgesBox.getChildren().add(altitudeWarningBadge);
        }

        // Difficulty badge
        Label difficultyLabel = new Label(trek.getDifficulty());
        difficultyLabel.setStyle(getDifficultyStyle(trek.getDifficulty()));
        badgesBox.getChildren().add(difficultyLabel);

        headerBox.getChildren().addAll(nameSection, spacer, badgesBox);

        // Trek Details
        VBox detailsBox = new VBox(8);

        // Duration and Date
        HBox durationDateBox = new HBox(20);
        Label durationLabel = new Label("Duration: " + trek.getDuration());
        durationLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        Label dateLabel = new Label("Start Date: " + trek.getStartDate().format(formatter));
        dateLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");

        durationDateBox.getChildren().addAll(durationLabel, dateLabel);

        // Altitude and Season with enhanced altitude display
        HBox altitudeSeasonBox = new HBox(20);

        // Enhanced altitude display with warning text for high altitude
        VBox altitudeSection = new VBox(2);
        Label altitudeLabel = new Label("Max Altitude: " + trek.getMaxAltitudeString());
        altitudeLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
        altitudeSection.getChildren().add(altitudeLabel);

        if (trek.isHighAltitude()) {
            Label altitudeWarningText = new Label("⚠️ High altitude - acclimatization required");
            altitudeWarningText.setStyle("-fx-text-fill: #ff6b35; -fx-font-size: 12px; -fx-font-weight: bold;");
            altitudeSection.getChildren().add(altitudeWarningText);
        }

        Label seasonLabel = new Label("Best Season: " + trek.getBestSeason());
        seasonLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");

        altitudeSeasonBox.getChildren().addAll(altitudeSection, seasonLabel);

        // Cost and Guide
        HBox costGuideBox = new HBox(20);
        VBox costSection = createCostSection(trek);
        Label guideLabel = new Label("Guide: " + extractGuideName(trek.getGuideEmail()));
        guideLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");

        costGuideBox.getChildren().addAll(costSection, guideLabel);

        detailsBox.getChildren().addAll(durationDateBox, altitudeSeasonBox, costGuideBox);

        // Action Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewDetailsButton = new Button("View Details");
        viewDetailsButton.setStyle("-fx-background-color: transparent; -fx-border-color: #e53e3e; " +
                "-fx-border-radius: 20; -fx-text-fill: #e53e3e; -fx-padding: 8 16; " +
                "-fx-font-size: 14px; -fx-cursor: hand;");
        viewDetailsButton.setOnAction(e -> viewTrekDetails(trek));

        Button bookNowButton = new Button("Book Now");
        bookNowButton.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-padding: 8 16; -fx-font-size: 14px; " +
                "-fx-cursor: hand; -fx-font-weight: bold;");
        bookNowButton.setOnAction(e -> bookTrek(trek));

        buttonBox.getChildren().addAll(viewDetailsButton, bookNowButton);

        // Add hover effects
        addHoverEffect(viewDetailsButton);
        addHoverEffect(bookNowButton);

        card.getChildren().addAll(headerBox, detailsBox, buttonBox);
        return card;
    }

    // NEW: Create altitude warning badge
    private Label createAltitudeWarningBadge(Trek trek) {
        Label altitudeWarningBadge = new Label("HIGH ALTITUDE");

        // Warning badge styling similar to discount but with warning colors
        altitudeWarningBadge.setStyle(
                "-fx-background-color: linear-gradient(to right, #FF4444, #CC0000); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 10px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #AA0000; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(255,68,68,0.4), 4, 0, 0, 1);"
        );

        return altitudeWarningBadge;
    }

    private Label createDiscountBadge(Trek trek) {
        String discountText = String.format("%.0f%% OFF", trek.getDiscountPercent());
        Label discountBadge = new Label(discountText);

        discountBadge.setStyle(
                "-fx-background-color: linear-gradient(to right, #FF6B35, #F7931E); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 11px; " +
                        "-fx-padding: 4 8; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #FF4500; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(255,107,53,0.4), 4, 0, 0, 1);"
        );
        return discountBadge;
    }

    private VBox createCostSection(Trek trek) {
        VBox costSection = new VBox(2);

        if (trek.hasDiscount()) {
            Label originalCostLabel = new Label("Was: $" + String.format("%.0f", trek.getOriginalCost()));
            originalCostLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 12px; " +
                    "-fx-strikethrough: true;");

            Label finalCostLabel = new Label("Now: $" + String.format("%.0f", trek.getCost()));
            finalCostLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold; -fx-font-size: 16px;");

            costSection.getChildren().addAll(originalCostLabel, finalCostLabel);
        } else {
            Label costLabel = new Label("Cost: $" + String.format("%.0f", trek.getCost()));
            costLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-weight: bold; -fx-font-size: 16px;");
            costSection.getChildren().add(costLabel);
        }

        return costSection;
    }

    private String getDifficultyStyle(String difficulty) {
        String baseStyle = "-fx-background-radius: 15; -fx-padding: 6 12; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-font-weight: bold;";

        return switch (difficulty.toLowerCase()) {
            case "easy" -> baseStyle + " -fx-background-color: #4CAF50;";
            case "moderate" -> baseStyle + " -fx-background-color: #FF9800;";
            case "hard" -> baseStyle + " -fx-background-color: #F44336;";
            case "extreme" -> baseStyle + " -fx-background-color: #9C27B0;";
            default -> baseStyle + " -fx-background-color: #9E9E9E;";
        };
    }

    private String extractGuideName(String email) {
        if (email == null || email.isEmpty()) {
            return "Not assigned";
        }
        String name = email.split("@")[0];
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private void addHoverEffect(Button button) {
        String originalStyle = button.getStyle();

        button.setOnMouseEntered(e -> {
            if (button.getText().equals("Book Now")) {
                button.setStyle(originalStyle + " -fx-background-color: #d32f2f;");
            } else {
                button.setStyle(originalStyle + " -fx-background-color: #e53e3e; -fx-text-fill: white;");
            }
        });

        button.setOnMouseExited(e -> button.setStyle(originalStyle));
    }

    private void viewTrekDetails(Trek trek) {
        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("Trek Details");
        detailsAlert.setHeaderText(trek.getTrekName());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

        String details;
        if (trek.hasDiscount()) {
            details = String.format(
                    """
                    Trek: %s
                    
                    Duration: %s
                    Start Date: %s
                    Difficulty: %s
                    Maximum Altitude: %d meters%s
                    
                    SPECIAL OFFER!
                    Original Price: $%.0f
                    Discount: %.1f%% OFF
                    You Save: $%.0f
                    Final Price: $%.0f
                    
                    Best Season: %s
                    Guide: %s
                    Trek ID: %d
                    Attraction ID: %d""",
                    trek.getTrekName(),
                    trek.getDuration(),
                    trek.getStartDate().format(formatter),
                    trek.getDifficulty(),
                    trek.getMaxAltitude(),
                    trek.isHighAltitude() ? " ⚠️ HIGH ALTITUDE" : "",
                    trek.getOriginalCost(),
                    trek.getDiscountPercent(),
                    trek.getDiscountAmount(),
                    trek.getCost(),
                    trek.getBestSeason(),
                    extractGuideName(trek.getGuideEmail()),
                    trek.getId(),
                    trek.getAttractionId()
            );
        } else {
            details = String.format(
                    """
                    Trek: %s
                    
                    Duration: %s
                    Start Date: %s
                    Difficulty: %s
                    Maximum Altitude: %d meters%s
                    Cost: $%.0f
                    Best Season: %s
                    Guide: %s
                    Trek ID: %d
                    Attraction ID: %d""",
                    trek.getTrekName(),
                    trek.getDuration(),
                    trek.getStartDate().format(formatter),
                    trek.getDifficulty(),
                    trek.getMaxAltitude(),
                    trek.isHighAltitude() ? " ⚠️ HIGH ALTITUDE" : "",
                    trek.getCost(),
                    trek.getBestSeason(),
                    extractGuideName(trek.getGuideEmail()),
                    trek.getId(),
                    trek.getAttractionId()
            );
        }

        // Add altitude warning to details if applicable
        if (trek.isHighAltitude()) {
            details += "\n\n⚠️ HIGH ALTITUDE WARNING:\nThis trek reaches above 3000 meters. " +
                    "Proper acclimatization and physical preparation are essential. " +
                    "Consult with your doctor before booking if you have any health concerns.";
        }

        detailsAlert.setContentText(details);
        detailsAlert.getDialogPane().setPrefWidth(450);
        detailsAlert.getDialogPane().setStyle("-fx-font-family: 'System'; -fx-font-size: 13px;");

        detailsAlert.showAndWait();
    }

    private boolean showHighAltitudeWarning(Trek trek) {
        Alert warningAlert = new Alert(Alert.AlertType.WARNING);
        warningAlert.setTitle("⚠️ HIGH ALTITUDE DANGER WARNING");
        warningAlert.setHeaderText("EXTREME CAUTION REQUIRED");

        String riskLevel = getRiskLevel(trek.getMaxAltitude());

        String warningMessage = String.format(
                "🚨 DANGER: This trek involves extreme altitude!\n\n" +
                        "TREK DETAILS:\n" +
                        "• Trek: %s\n" +
                        "• Maximum Altitude: %dm\n" +
                        "• Risk Level: %s\n\n" +
                        "⚠️ SERIOUS HEALTH RISKS:\n" +
                        "• Acute Mountain Sickness (AMS)\n" +
                        "• High Altitude Pulmonary Edema (HAPE)\n" +
                        "• High Altitude Cerebral Edema (HACE)\n" +
                        "• Severe breathing difficulties\n" +
                        "• Risk of death if not properly managed\n\n" +
                        "🏥 MANDATORY REQUIREMENTS:\n" +
                        "• Medical clearance from a doctor\n" +
                        "• Previous high-altitude experience recommended\n" +
                        "• Comprehensive travel insurance with helicopter evacuation\n" +
                        "• Physical fitness certification\n" +
                        "• Proper acclimatization schedule\n\n" +
                        "📋 SAFETY CHECKLIST:\n" +
                        "☐ Medical consultation completed\n" +
                        "☐ Insurance with evacuation coverage\n" +
                        "☐ Emergency contact information provided\n" +
                        "☐ Altitude sickness medication available\n" +
                        "☐ Experienced guide confirmed\n\n" +
                        "By continuing, you acknowledge that you understand these risks and take full responsibility for your safety.\n\n" +
                        "Do you want to proceed to the booking screen?",
                trek.getTrekName(), trek.getMaxAltitude(), riskLevel
        );

        warningAlert.setContentText(warningMessage);

        // Custom buttons
        ButtonType proceedButton = new ButtonType("I Accept All Risks - Continue to Booking", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        warningAlert.getButtonTypes().setAll(proceedButton, cancelButton);

        // Style the dialog
        warningAlert.getDialogPane().setPrefWidth(600);
        warningAlert.getDialogPane().setPrefHeight(500);
        warningAlert.getDialogPane().setStyle(
                "-fx-font-family: 'System'; -fx-font-size: 12px; -fx-background-color: #fff3cd; -fx-border-color: #ffc107; -fx-border-width: 2px;"
        );

        // Style the proceed button to be very prominent and warning-colored
        warningAlert.getDialogPane().lookupButton(proceedButton).setStyle(
                "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 20px;"
        );

        // Style the cancel button
        warningAlert.getDialogPane().lookupButton(cancelButton).setStyle(
                "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px;"
        );

        // Show dialog and return result
        Optional<ButtonType> result = warningAlert.showAndWait();
        boolean userAccepted = result.filter(response -> response == proceedButton).isPresent();

        if (userAccepted) {
            System.out.println("User accepted altitude risks for trek: " + trek.getTrekName());
        }

        return userAccepted;
    }
    private void processBooking(Trek trek) {
        boolean success = createBooking(trek);

        if (success) {
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Booking Confirmed!");
            successAlert.setHeaderText("🎉 SUCCESS!");

            String successMessage;
            if (trek.hasDiscount()) {
                successMessage = String.format(
                        "Your booking for %s has been confirmed!\n\n" +
                                "🎉 Congratulations! You saved $%.0f with our %.1f%% discount!\n" +
                                "💰 Final Amount Paid: $%.0f\n\n" +
                                "📧 Confirmation details will be sent to your email\n" +
                                "📞 Your guide will contact you soon\n" +
                                "%s" +
                                "\nThank you for choosing our trekking service!",
                        trek.getTrekName(),
                        trek.getDiscountAmount(),
                        trek.getDiscountPercent(),
                        trek.getCost(),
                        trek.isHighAltitude() ? "\n⚠️ IMPORTANT: Start your high-altitude preparation immediately!\n" +
                                "• Schedule medical consultation\n" +
                                "• Arrange travel insurance\n" +
                                "• Begin physical training\n" : ""
                );
            } else {
                successMessage = String.format(
                        "Your booking for %s has been confirmed!\n\n" +
                                "💰 Amount Paid: $%.0f\n" +
                                "📧 Confirmation details will be sent to your email\n" +
                                "📞 Your guide will contact you soon\n" +
                                "%s" +
                                "\nThank you for choosing our trekking service!",
                        trek.getTrekName(),
                        trek.getCost(),
                        trek.isHighAltitude() ? "\n⚠️ IMPORTANT: Start your high-altitude preparation immediately!\n" +
                                "• Schedule medical consultation\n" +
                                "• Arrange travel insurance\n" +
                                "• Begin physical training\n" : ""
                );
            }

            successAlert.setContentText(successMessage);
            successAlert.getDialogPane().setPrefWidth(500);
            successAlert.showAndWait();
        } else {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Booking Failed");
            errorAlert.setHeaderText("❌ Error");
            errorAlert.setContentText("Sorry, there was an error processing your booking. Please try again later or contact our support team.");
            errorAlert.showAndWait();
        }
    }

    private String getRiskLevel(int altitude) {
        if (altitude < 2500) {
            return "LOW RISK";
        } else if (altitude < 3500) {
            return "MODERATE RISK";
        } else if (altitude < 4500) {
            return "HIGH RISK";
        } else if (altitude < 5500) {
            return "VERY HIGH RISK";
        } else {
            return "EXTREME RISK";
        }
    }
    private boolean createBooking(Trek trek) {
        try {
            String userEmailToUse = getCurrentUserEmailFromSession();
            System.out.println("Creating booking with user email: " + userEmailToUse);

            Booking booking = new Booking(trek.getId(), userEmailToUse, trek.getGuideEmail(), trek.getStartDate());
            boolean success = jsonHandler.addBooking(booking);

            if (success) {
                System.out.println("Booking created successfully: " + booking);
                return true;
            } else {
                System.err.println("Failed to save booking to JSON file");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error creating booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String getCurrentUserEmailFromSession() {
        try {
            UserSession userSession = UserSession.getInstance();
            User currentUser = userSession.getCurrentUser();

            if (currentUser != null && currentUser.getEmail() != null) {
                return currentUser.getEmail();
            } else {
                System.err.println("No user found in session, using fallback email");
                return currentUserEmail;
            }
        } catch (Exception e) {
            System.err.println("Error getting user email from session: " + e.getMessage());
            return currentUserEmail;
        }
    }
    private void bookTrek(Trek trek) {
        updateUserEmailFromSession();

        if (trek.isHighAltitude() && !showRiskAcknowledgment(trek)) {
            return;
        }

        showBookingConfirmation(trek);
    }

    private boolean showRiskAcknowledgment(Trek trek) {
        Alert warning = new Alert(Alert.AlertType.WARNING);
        warning.setTitle("High Altitude Warning");
        warning.setHeaderText("⚠️ " + trek.getTrekName() + " reaches " + trek.getMaxAltitude() + "m");
        warning.setContentText(
                "This trek is classified as a high-altitude trek.\n" +
                        "Risks include AMS, HAPE, and HACE.\n\n" +
                        "Medical fitness and proper acclimatization are required.\n\n" +
                        "Do you acknowledge these risks and want to continue?"
        );

        ButtonType proceed = new ButtonType("I Accept the Risks", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        warning.getButtonTypes().setAll(proceed, cancel);

        return warning.showAndWait().filter(r -> r == proceed).isPresent();
    }

    private void showBookingConfirmation(Trek trek) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Booking");
        confirm.setHeaderText("Booking: " + trek.getTrekName());
        String msg = String.format(
                "Start Date: %s\nDuration: %s\nMax Altitude: %dm %s\n\nTotal Cost: $%.0f\n\nProceed with this booking?",
                trek.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                trek.getDuration(),
                trek.getMaxAltitude(),
                trek.isHighAltitude() ? "(High Altitude)" : "",
                trek.getCost()
        );
        confirm.setContentText(msg);

        ButtonType confirmBtn = new ButtonType("Confirm Booking", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(confirmBtn, cancelBtn);

        confirm.showAndWait().ifPresent(response -> {
            if (response == confirmBtn) {
                boolean success = createBooking(trek);
                showBookingResult(success, trek);
            }
        });
    }

    private void showBookingResult(boolean success, Trek trek) {
        Alert result = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        result.setTitle(success ? "Booking Confirmed" : "Booking Failed");
        result.setHeaderText(success ? "🎉 Your booking is confirmed!" : "❌ Booking failed");
        result.setContentText(success
                ? String.format("Trek: %s\nAmount Paid: $%.0f\n\nYou’ll receive an email confirmation shortly.",
                trek.getTrekName(), trek.getCost())
                : "An error occurred during booking. Please try again later."
        );
        result.showAndWait();
    }

}
