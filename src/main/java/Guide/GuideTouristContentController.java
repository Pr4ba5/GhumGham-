package Guide;

import Models.*;
import Session.UserSession;
import Storage.AdminJSONHandler;
import Storage.JSONHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GuideTouristContentController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private VBox touristsList;

    private AdminJSONHandler adminJsonHandler;
    private User currentGuide;

    private List<TouristData> allTourists;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        adminJsonHandler = new AdminJSONHandler();
        JSONHandler jsonHandler = new JSONHandler();
        UserSession userSession = UserSession.getInstance();
        currentGuide = userSession.getCurrentUser();

        setupSearchField();
        loadGuideTourists();

        // Debug log
        if (currentGuide != null) {
            System.out.println("GuideTouristContentController initialized for guide: " + currentGuide.getEmail());
        } else {
            System.err.println("WARNING: No guide found in session!");
        }
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTourists(newValue.trim().toLowerCase());
        });
    }

    private void loadGuideTourists() {
        try {
            if (currentGuide == null) {
                System.err.println("No guide logged in");
                displayTourists(new ArrayList<>());
                return;
            }

            String guideEmail = currentGuide.getEmail();
            System.out.println("Loading tourists for guide: " + guideEmail);

            // Step 1: Get all treks assigned to this guide
            List<Trek> guideTreks = adminJsonHandler.getTreksByGuideEmail(guideEmail);
            System.out.println("Found " + guideTreks.size() + " treks for guide");

            if (guideTreks.isEmpty()) {
                displayTourists(new ArrayList<>());
                return;
            }

            // Step 2: Get all bookings for these treks
            List<Booking> allBookings = adminJsonHandler.loadBookings();
            Set<Integer> trekIds = guideTreks.stream()
                    .map(Trek::getId)
                    .collect(Collectors.toSet());

            List<Booking> guideBookings = allBookings.stream()
                    .filter(booking -> trekIds.contains(booking.getTrekId()))
                    .toList();

            System.out.println("Found " + guideBookings.size() + " bookings for guide's treks");

            // Step 3: Create tourist data from bookings
            allTourists = new ArrayList<>();
            List<User> allUsers = JSONHandler.loadUsers();

            for (Booking booking : guideBookings) {
                TouristData touristData = createTouristData(booking, guideTreks, allUsers);
                if (touristData != null) {
                    allTourists.add(touristData);
                }
            }

            // Remove duplicates based on tourist email
            allTourists = new ArrayList<>(allTourists.stream()
                    .collect(Collectors.toMap(
                            TouristData::getTouristEmail,
                            tourist -> tourist,
                            (existing, replacement) -> existing))
                    .values());

            System.out.println("Created data for " + allTourists.size() + " unique tourists");

            // Step 4: Display tourists
            displayTourists(allTourists);

        } catch (Exception e) {
            System.err.println("Error loading guide tourists: " + e.getMessage());
            e.printStackTrace();
            showAlert();
        }
    }

    private TouristData createTouristData(Booking booking, List<Trek> guideTreks, List<User> allUsers) {
        try {
            // Find the trek for this booking
            Trek trek = guideTreks.stream()
                    .filter(t -> t.getId() == booking.getTrekId())
                    .findFirst()
                    .orElse(null);

            if (trek == null) {
                System.err.println("Trek not found for booking: " + booking.getBookingId());
                return null;
            }

            // Find the tourist user
            User tourist = allUsers.stream()
                    .filter(user -> user.getEmail().equalsIgnoreCase(booking.getUserEmail()))
                    .findFirst()
                    .orElse(null);

            if (tourist == null) {
                System.err.println("Tourist not found for email: " + booking.getUserEmail());
                return null;
            }

            // Get attraction information
            Attraction attraction = adminJsonHandler.getAttractionById(trek.getAttractionId());
            String attractionName = (attraction != null) ? attraction.getName() : "Unknown Attraction";
            String location = (attraction != null) ? attraction.getLocation() : "Unknown Location";

            return new TouristData(
                    tourist,
                    trek,
                    attraction,
                    booking,
                    attractionName,
                    location
            );

        } catch (Exception e) {
            System.err.println("Error creating tourist data for booking: " + booking.getBookingId());
            e.printStackTrace();
            return null;
        }
    }

    private void filterTourists(String searchText) {
        if (allTourists == null) {
            return;
        }

        List<TouristData> filteredTourists;
        if (searchText.isEmpty()) {
            filteredTourists = new ArrayList<>(allTourists);
        } else {
            filteredTourists = allTourists.stream()
                    .filter(tourist ->
                            tourist.getTouristName().toLowerCase().contains(searchText) ||
                                    tourist.getTouristEmail().toLowerCase().contains(searchText) ||
                                    tourist.getTrekName().toLowerCase().contains(searchText) ||
                                    tourist.getAttractionName().toLowerCase().contains(searchText) ||
                                    tourist.getLocation().toLowerCase().contains(searchText) ||
                                    tourist.getTrek().getDifficulty().toLowerCase().contains(searchText)
                    )
                    .collect(Collectors.toList());
        }

        displayTourists(filteredTourists);
    }

    private void displayTourists(List<TouristData> tourists) {
        touristsList.getChildren().clear();

        if (tourists.isEmpty()) {
            Label noTouristsLabel = new Label("No tourists found for your treks.");
            noTouristsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 16px; -fx-padding: 20;");
            touristsList.getChildren().add(noTouristsLabel);
            return;
        }

        for (TouristData tourist : tourists) {
            VBox touristCard = createTouristCard(tourist);
            touristsList.getChildren().add(touristCard);
        }
    }

    private VBox createTouristCard(TouristData touristData) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 15; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);

        // Tourist Header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Tourist Avatar and Info
        VBox touristInfo = new VBox(5);

        // Avatar
        Label avatarLabel = new Label(touristData.getTouristName().substring(0, 1).toUpperCase());
        avatarLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 25; -fx-font-size: 18px; -fx-font-weight: bold; " +
                "-fx-alignment: center; -fx-pref-width: 50; -fx-pref-height: 50;");

        // Tourist Name and Email
        Label nameLabel = new Label(touristData.getTouristName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #333333;");

        Label emailLabel = new Label(touristData.getTouristEmail());
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        VBox nameEmailBox = new VBox(3);
        nameEmailBox.getChildren().addAll(nameLabel, emailLabel);

        HBox touristHeaderBox = new HBox(12);
        touristHeaderBox.setAlignment(Pos.CENTER_LEFT);
        touristHeaderBox.getChildren().addAll(avatarLabel, nameEmailBox);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Status Badge
        Label statusLabel = new Label("Active");
        statusLabel.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-background-radius: 12; -fx-padding: 4 8; -fx-font-size: 12px; -fx-font-weight: bold;");

        headerBox.getChildren().addAll(touristHeaderBox, spacer, statusLabel);

        // Trek Information
        VBox trekInfoBox = new VBox(8);
        trekInfoBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 10;");

        Label trekTitleLabel = new Label("Trek Information");
        trekTitleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");

        // Trek Details Row 1
        HBox trekRow1 = new HBox(20);
        Label trekNameLabel = new Label("🏔️ " + touristData.getTrekName());
        trekNameLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label attractionLabel = new Label("🎯 " + touristData.getAttractionName());
        attractionLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");

        trekRow1.getChildren().addAll(trekNameLabel, attractionLabel);

        // Trek Details Row 2
        HBox trekRow2 = new HBox(20);
        Label locationLabel = new Label("📍 " + touristData.getLocation());
        locationLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");

        Label difficultyLabel = new Label("⚡ " + touristData.getTrek().getDifficulty());
        difficultyLabel.setStyle(getDifficultyStyle(touristData.getTrek().getDifficulty()));

        trekRow2.getChildren().addAll(locationLabel, difficultyLabel);

        // Trek Details Row 3
        HBox trekRow3 = new HBox(20);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        Label dateLabel = new Label("📅 " + touristData.getTrek().getStartDate().format(formatter));
        dateLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");

        Label durationLabel = new Label("⏰ " + touristData.getTrek().getDuration());
        durationLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");

        trekRow3.getChildren().addAll(dateLabel, durationLabel);

        trekInfoBox.getChildren().addAll(trekTitleLabel, trekRow1, trekRow2, trekRow3);

        // Action Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button contactButton = new Button("Contact Tourist");
        contactButton.setStyle("-fx-background-color: transparent; -fx-border-color: #3498db; " +
                "-fx-border-radius: 20; -fx-text-fill: #3498db; -fx-padding: 8 16; " +
                "-fx-font-size: 14px; -fx-cursor: hand;");
        contactButton.setOnAction(e -> contactTourist(touristData));

        Button viewDetailsButton = new Button("View Details");
        viewDetailsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-padding: 8 16; -fx-font-size: 14px; " +
                "-fx-cursor: hand; -fx-font-weight: bold;");
        viewDetailsButton.setOnAction(e -> viewTouristDetails(touristData));

        buttonBox.getChildren().addAll(contactButton, viewDetailsButton);

        // Add hover effects
        addHoverEffect(contactButton);
        addHoverEffect(viewDetailsButton);

        card.getChildren().addAll(headerBox, trekInfoBox, buttonBox);
        return card;
    }

    private String getDifficultyStyle(String difficulty) {
        String baseStyle = "-fx-background-radius: 12; -fx-padding: 4 8; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-font-weight: bold;";

        switch (difficulty.toLowerCase()) {
            case "easy":
                return baseStyle + " -fx-background-color: #4CAF50;";
            case "moderate":
                return baseStyle + " -fx-background-color: #FF9800;";
            case "hard":
                return baseStyle + " -fx-background-color: #F44336;";
            case "extreme":
                return baseStyle + " -fx-background-color: #9C27B0;";
            default:
                return baseStyle + " -fx-background-color: #9E9E9E;";
        }
    }

    private void addHoverEffect(Button button) {
        String originalStyle = button.getStyle();

        button.setOnMouseEntered(e -> {
            if (button.getText().equals("View Details")) {
                button.setStyle(originalStyle + " -fx-background-color: #2980b9;");
            } else {
                button.setStyle(originalStyle + " -fx-background-color: #3498db; -fx-text-fill: white;");
            }
        });

        button.setOnMouseExited(e -> button.setStyle(originalStyle));
    }

    private void contactTourist(TouristData touristData) {
        Alert contactAlert = new Alert(Alert.AlertType.INFORMATION);
        contactAlert.setTitle("Contact Tourist");
        contactAlert.setHeaderText("Contact " + touristData.getTouristName());

        String contactInfo = String.format(
                """
                        Tourist Contact Information:
                        
                        👤 Name: %s
                        📧 Email: %s
                        📞 Phone: %s
                        
                        Trek Information:
                        🏔️ Trek: %s
                        📅 Date: %s
                        📍 Location: %s
                        
                        You can contact this tourist directly using the provided email or phone number.""",
                touristData.getTouristName(),
                touristData.getTouristEmail(),
                touristData.getTourist().getPhone() != null ? touristData.getTourist().getPhone() : "Not provided",
                touristData.getTrekName(),
                touristData.getTrek().getStartDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                touristData.getLocation()
        );

        contactAlert.setContentText(contactInfo);
        contactAlert.getDialogPane().setPrefWidth(450);
        contactAlert.showAndWait();
    }

    private void viewTouristDetails(TouristData touristData) {
        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("Tourist Details");
        detailsAlert.setHeaderText(touristData.getTouristName() + " - " + touristData.getTrekName());

        String details = String.format(
                """
                        👤 TOURIST INFORMATION:
                        Name: %s
                        Email: %s
                        Phone: %s
                        
                        🏔️ TREK INFORMATION:
                        Trek: %s
                        Attraction: %s
                        Location: %s
                        Difficulty: %s
                        Duration: %s
                        Start Date: %s
                        Max Altitude: %s
                        Cost: $%.0f
                        Best Season: %s
                        
                        📋 BOOKING INFORMATION:
                        Booking ID: %s
                        Trek ID: %d""",
                touristData.getTouristName(),
                touristData.getTouristEmail(),
                touristData.getTourist().getPhone() != null ? touristData.getTourist().getPhone() : "Not provided",
                touristData.getTrekName(),
                touristData.getAttractionName(),
                touristData.getLocation(),
                touristData.getTrek().getDifficulty(),
                touristData.getTrek().getDuration(),
                touristData.getTrek().getStartDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                touristData.getTrek().getMaxAltitude(),
                touristData.getTrek().getCost(),
                touristData.getTrek().getBestSeason(),
                touristData.getBooking().getBookingId(),
                touristData.getTrek().getId()
        );

        detailsAlert.setContentText(details);
        detailsAlert.getDialogPane().setPrefWidth(500);
        detailsAlert.showAndWait();
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Failed to load tourists. Please try again.");
        alert.showAndWait();
    }

    // Inner class to hold tourist data
    public static class TouristData {
        private final User tourist;
        private final Trek trek;
        private final Attraction attraction;
        private final Booking booking;
        private final String attractionName;
        private final String location;

        public TouristData(User tourist, Trek trek, Attraction attraction, Booking booking,
                           String attractionName, String location) {
            this.tourist = tourist;
            this.trek = trek;
            this.attraction = attraction;
            this.booking = booking;
            this.attractionName = attractionName;
            this.location = location;
        }

        // Getters
        public User getTourist() { return tourist; }
        public Trek getTrek() { return trek; }
        public Attraction getAttraction() { return attraction; }
        public Booking getBooking() { return booking; }
        public String getAttractionName() { return attractionName; }
        public String getLocation() { return location; }

        public String getTouristName() { return tourist.getFullName(); }
        public String getTouristEmail() { return tourist.getEmail(); }
        public String getTrekName() { return trek.getTrekName(); }
    }
}
