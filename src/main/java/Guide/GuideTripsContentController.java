package Guide;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import Models.*;
import Session.UserSession;
import Storage.AdminJSONHandler;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GuideTripsContentController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Label upcomingCountLabel;
    @FXML private VBox upcomingTripsList;

    private AdminJSONHandler adminHandler;
    private List<Trek> allGuideTreks;
    private String currentGuideEmail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        adminHandler = new AdminJSONHandler();

        // Get current guide's email
        if (UserSession.getInstance().getCurrentUser() != null) {
            currentGuideEmail = UserSession.getInstance().getCurrentUser().getEmail();
        }

        setupFilter();
        setupSearch();
        loadTrips();
    }

    private void setupFilter() {
        filterCombo.getItems().addAll("All", "Easy", "Moderate", "Hard");
        filterCombo.setValue("All");
        filterCombo.setOnAction(e -> filterTrips());
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTrips();
        });
    }

    private void loadTrips() {
        if (currentGuideEmail == null) {
            upcomingCountLabel.setText("0");
            upcomingTripsList.getChildren().clear();
            return;
        }

        // Load all treks for this guide
        allGuideTreks = adminHandler.getTreksByGuideEmail(currentGuideEmail);

        // Apply current filter
        filterTrips();
    }

    private void filterTrips() {
        if (allGuideTreks == null) {
            return;
        }

        List<Trek> filteredTreks = allGuideTreks.stream()
                .filter(this::matchesSearchCriteria)
                .filter(this::matchesFilterCriteria)
                .collect(Collectors.toList());

        displayTrips(filteredTreks);
        upcomingCountLabel.setText(String.valueOf(filteredTreks.size()));
    }

    private boolean matchesSearchCriteria(Trek trek) {
        String searchText = searchField.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            return true;
        }

        searchText = searchText.toLowerCase().trim();

        // Search in trek name
        if (trek.getTrekName().toLowerCase().contains(searchText)) {
            return true;
        }

        // Search in attraction location
        Attraction attraction = adminHandler.getAttractionById(trek.getAttractionId());
        if (attraction != null && attraction.getLocation().toLowerCase().contains(searchText)) {
            return true;
        }

        // Search in difficulty
        return trek.getDifficulty().toLowerCase().contains(searchText);
    }

    private boolean matchesFilterCriteria(Trek trek) {
        String filter = filterCombo.getValue();
        if (filter == null || "All".equals(filter)) {
            return true;
        }

        // Filter by difficulty level
        return filter.equalsIgnoreCase(trek.getDifficulty());
    }

    private void displayTrips(List<Trek> treks) {
        upcomingTripsList.getChildren().clear();

        if (treks.isEmpty()) {
            Label noTripsLabel = new Label("No treks found matching your criteria");
            noTripsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14; -fx-padding: 20;");
            upcomingTripsList.getChildren().add(noTripsLabel);
            return;
        }

        for (Trek trek : treks) {
            VBox tripCard = createTripCard(trek);
            upcomingTripsList.getChildren().add(tripCard);
        }
    }

    private VBox createTripCard(Trek trek) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");

        // Header with trek name and difficulty
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label trekNameLabel = new Label(trek.getTrekName());
        trekNameLabel.setFont(Font.font("System Bold", 16));
        trekNameLabel.setStyle("-fx-text-fill: #333333;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label difficultyLabel = new Label(trek.getDifficulty());
        difficultyLabel.setStyle(getDifficultyStyle(trek.getDifficulty()));

        header.getChildren().addAll(trekNameLabel, spacer, difficultyLabel);

        // Location and attraction info
        Attraction attraction = adminHandler.getAttractionById(trek.getAttractionId());
        String locationText = attraction != null ? attraction.getLocation() : "Location not specified";

        HBox locationBox = new HBox(8);
        locationBox.setAlignment(Pos.CENTER_LEFT);

        Label locationIcon = new Label("");
        Label locationLabel = new Label(locationText);
        locationLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 13;");

        locationBox.getChildren().addAll(locationIcon, locationLabel);

        // Trek details
        HBox detailsBox = new HBox(20);
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        // Duration
        HBox durationBox = new HBox(5);
        durationBox.setAlignment(Pos.CENTER_LEFT);
        Label durationIcon = new Label("");
        Label durationLabel = new Label(trek.getDuration());
        durationLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12;");
        durationBox.getChildren().addAll(durationIcon, durationLabel);

        // Start date
        HBox dateBox = new HBox(5);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        Label dateIcon = new Label("");
        String dateText = trek.getStartDate() != null ?
                trek.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) :
                "Date TBD";
        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12;");
        dateBox.getChildren().addAll(dateIcon, dateLabel);

        // Cost
        HBox costBox = new HBox(5);
        costBox.setAlignment(Pos.CENTER_LEFT);
        Label costIcon = new Label("");
        Label costLabel = new Label("Rs. " + String.format("%.0f", trek.getCost()));
        costLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12; -fx-font-weight: bold;");
        costBox.getChildren().addAll(costIcon, costLabel);

        detailsBox.getChildren().addAll(durationBox, dateBox, costBox);

        // Altitude warning if high altitude
        VBox warningBox = new VBox();

        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 16; -fx-font-size: 12;");
        viewDetailsBtn.setOnAction(e -> viewTrekDetails(trek));

        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, javafx.scene.layout.Priority.ALWAYS);

        buttonBox.getChildren().addAll(viewDetailsBtn, buttonSpacer);

        // Add all components to card
        card.getChildren().addAll(header, locationBox, detailsBox);

        if (!warningBox.getChildren().isEmpty()) {
            card.getChildren().add(warningBox);
        }

        card.getChildren().add(buttonBox);

        return card;
    }

    private String getDifficultyStyle(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> "-fx-background-color: #4CAF50; -fx-background-radius: 12; " +
                    "-fx-padding: 4 12; -fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold;";
            case "moderate", "medium" -> "-fx-background-color: #FF9800; -fx-background-radius: 12; " +
                    "-fx-padding: 4 12; -fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold;";
            case "hard" -> "-fx-background-color: #F44336; -fx-background-radius: 12; " +
                    "-fx-padding: 4 12; -fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold;";
            default -> "-fx-background-color: #9E9E9E; -fx-background-radius: 12; " +
                    "-fx-padding: 4 12; -fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold;";
        };
    }

    private void viewTrekDetails(Trek trek) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Trek Details");
        alert.setHeaderText(trek.getTrekName());

        Attraction attraction = adminHandler.getAttractionById(trek.getAttractionId());
        String attractionName = attraction != null ? attraction.getName() : "Unknown Attraction";
        String location = attraction != null ? attraction.getLocation() : "Location not specified";

        String details = String.format(
                """
                        Trek ID: %d
                        Duration: %s
                        Start Date: %s
                        Difficulty: %s
                        Maximum Altitude: %s
                        Cost: Rs. %.0f
                        Best Season: %s
                        Attraction: %s
                        Location: %s
                        Guide Email: %s""",
                trek.getId(),
                trek.getDuration(),
                trek.getStartDate() != null ? trek.getStartDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) : "Not set",
                trek.getDifficulty(),
                (trek.getMaxAltitude() == 0) ? trek.getMaxAltitude() : "Not specified",
                trek.getCost(),
                trek.getBestSeason() != null ? trek.getBestSeason() : "Not specified",
                attractionName,
                location,
                trek.getGuideEmail()
        );

        alert.setContentText(details);
        alert.getDialogPane().setPrefWidth(400);
        alert.showAndWait();
    }
}
