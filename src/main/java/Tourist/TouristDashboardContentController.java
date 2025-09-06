package Tourist;

import Language.LanguageManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Comparator;

import Models.Booking;
import Models.Trek;
import Models.Attraction;
import Models.User;
import Session.UserSession;
import Storage.AdminJSONHandler;
import Language.LanguageManager;
import Services.WeatherService;
import javafx.application.Platform;

public class TouristDashboardContentController implements Initializable, Language.LanguageManager.LanguageChangeListener {

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox upcomingTrekContainer;

    @FXML
    private Label trekNameLabel;

    @FXML
    private Label trekLocationLabel;

    @FXML
    private Label difficultyLabel;

    @FXML
    private Label trekDescriptionLabel;

    @FXML
    private Label trekWarningLabel;

    @FXML
    private Label activeTripsLabel;

    @FXML
    private GridPane calendarGrid;

    // Weather-related FXML elements
    @FXML private Label weatherLocationLabel;
    @FXML private Label weatherDescriptionLabel;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherIconLabel;
    @FXML private Label weatherHumidityLabel;
    @FXML private Label weatherWindLabel;

    // Sample data - in real application, this would come from database
    private TrekBooking upcomingTrek;
    private int activeBookingsCount = 0;

    private AdminJSONHandler jsonHandler;
    private UserSession userSession;
    private User currentUser;
    private LanguageManager languageManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize handlers and session
        jsonHandler = new AdminJSONHandler();
        userSession = UserSession.getInstance();
        currentUser = userSession.getCurrentUser();
        languageManager = LanguageManager.getInstance();
        languageManager.addLanguageChangeListener(this);

        setupWelcomeMessage();
        loadUpcomingTrek();
        updateActiveTripsCount();
        setupCalendar();
        loadWeatherData();
    }

    private void setupWelcomeMessage() {
        String timeGreeting = getTimeBasedGreeting();
        if (currentUser != null) {
            welcomeLabel.setText(languageManager.getString("dashboard.welcome", timeGreeting, currentUser.getFirstName()));
        } else {
            welcomeLabel.setText(languageManager.getString("dashboard.welcome", timeGreeting, "Tourist"));
        }
    }

    private String getTimeBasedGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) {
            return languageManager.getString("good.morning");
        } else if (hour < 17) {
            return languageManager.getString("good.afternoon");
        } else {
            return languageManager.getString("good.evening");
        }
    }

    private void loadUpcomingTrek() {
        // In a real application, you would fetch this from a database
        upcomingTrek = getUpcomingTrekFromDatabase();

        if (upcomingTrek != null) {
            displayTrekInfo(upcomingTrek);
        } else {
            displayNoTrekInfo();
        }
    }

    private void displayTrekInfo(TrekBooking trek) {
        trekNameLabel.setText(trek.getName());
        trekLocationLabel.setText(trek.getLocation());
        trekDescriptionLabel.setText(trek.getDescription());

        // Set difficulty label with appropriate color
        String localizedDifficulty = getLocalizedDifficulty(trek.getDifficulty());
        difficultyLabel.setText(localizedDifficulty);
        setDifficultyLabelStyle(trek.getDifficulty());

        // Hide warning label when trek is available
        trekWarningLabel.setVisible(false);
    }

    private void displayNoTrekInfo() {
        trekNameLabel.setText(languageManager.getString("dashboard.no.upcoming.trek"));
        trekLocationLabel.setText(languageManager.getString("dashboard.location.not.available"));
        trekDescriptionLabel.setText(languageManager.getString("dashboard.no.trek.info"));
        difficultyLabel.setText("N/A");
        difficultyLabel.setStyle("-fx-background-color: #9E9E9E; -fx-background-radius: 15; -fx-padding: 5 10;");
        trekWarningLabel.setVisible(true);
    }

    private String getLocalizedDifficulty(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy":
                return languageManager.getString("dashboard.difficulty.easy");
            case "moderate":
                return languageManager.getString("dashboard.difficulty.moderate");
            case "hard":
                return languageManager.getString("dashboard.difficulty.hard");
            case "extreme":
                return languageManager.getString("dashboard.difficulty.extreme");
            default:
                return difficulty;
        }
    }

    private void setDifficultyLabelStyle(String difficulty) {
        String style = "-fx-background-radius: 15; -fx-padding: 5 10; -fx-text-fill: white;";

        switch (difficulty.toLowerCase()) {
            case "easy":
                difficultyLabel.setStyle(style + " -fx-background-color: #4CAF50;");
                break;
            case "moderate":
                difficultyLabel.setStyle(style + " -fx-background-color: #FF9800;");
                break;
            case "hard":
                difficultyLabel.setStyle(style + " -fx-background-color: #F44336;");
                break;
            case "extreme":
                difficultyLabel.setStyle(style + " -fx-background-color: #9C27B0;");
                break;
            default:
                difficultyLabel.setStyle(style + " -fx-background-color: #9E9E9E;");
        }
    }

    private void updateActiveTripsCount() {
        // In a real application, you would fetch this from a database
        activeBookingsCount = getActiveBookingsCount();
        activeTripsLabel.setText(String.valueOf(activeBookingsCount));
    }

    private void setupCalendar() {
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        int daysInMonth = now.lengthOfMonth();
        int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0

        // Clear existing calendar
        calendarGrid.getChildren().clear();

        // Add day headers - these could be localized too
        String[] dayHeaders = {"S", "M", "T", "W", "T", "F", "S"};
        for (int i = 0; i < 7; i++) {
            Label dayHeader = new Label(dayHeaders[i]);
            dayHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666; -fx-font-size: 10px;");
            dayHeader.setAlignment(Pos.CENTER);
            dayHeader.setPrefSize(25, 20);
            calendarGrid.add(dayHeader, i, 0);
        }

        // Add calendar days
        int row = 1;
        int col = startDayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefSize(25, 20);
            dayLabel.setStyle("-fx-font-size: 10px;");

            // Highlight current day
            if (day == now.getDayOfMonth()) {
                dayLabel.setStyle("-fx-font-size: 10px; -fx-background-color: #e53e3e; -fx-text-fill: white; -fx-background-radius: 10;");
            }

            calendarGrid.add(dayLabel, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    @Override
    public void onLanguageChanged() {
        setupWelcomeMessage();
        if (upcomingTrek != null) {
            displayTrekInfo(upcomingTrek);
        } else {
            displayNoTrekInfo();
        }
        // Refresh weather data with new language
        loadWeatherData();
    }

    // Rest of the methods remain the same as in your original code...
    private TrekBooking getUpcomingTrekFromDatabase() {
        try {
            if (currentUser == null) {
                System.err.println("No user logged in");
                return null;
            }

            String userEmail = currentUser.getEmail();
            System.out.println("Loading upcoming trek for user: " + userEmail);

            List<Booking> userBookings = jsonHandler.getBookingsByUserEmail(userEmail);

            if (userBookings.isEmpty()) {
                System.out.println("No bookings found for user");
                return null;
            }

            LocalDate today = LocalDate.now();

            Booking upcomingBooking = userBookings.stream()
                    .filter(booking -> {
                        Trek trek = jsonHandler.getTrekById(booking.getTrekId());
                        return trek != null && !trek.getStartDate().isBefore(today);
                    })
                    .min(Comparator.comparing(booking -> {
                        Trek trek = jsonHandler.getTrekById(booking.getTrekId());
                        return trek != null ? trek.getStartDate() : LocalDate.MAX;
                    }))
                    .orElse(null);

            if (upcomingBooking == null) {
                System.out.println("No upcoming treks found");
                return null;
            }

            Trek trek = jsonHandler.getTrekById(upcomingBooking.getTrekId());
            if (trek == null) {
                System.err.println("Trek not found for booking: " + upcomingBooking.getBookingId());
                return null;
            }

            Attraction attraction = jsonHandler.getAttractionById(trek.getAttractionId());
            String location = (attraction != null) ? attraction.getLocation() : "Unknown Location";
            String description = (attraction != null) ? attraction.getRemarks() : "No description available.";

            TrekBooking trekBooking = new TrekBooking(
                    trek.getTrekName(),
                    location,
                    trek.getDifficulty(),
                    description
            );

            trekBooking.setStartDate(trek.getStartDate());

            System.out.println("Found upcoming trek: " + trek.getTrekName());
            return trekBooking;

        } catch (Exception e) {
            System.err.println("Error loading upcoming trek: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private int getActiveBookingsCount() {
        try {
            if (currentUser == null) {
                System.err.println("No user logged in");
                return 0;
            }

            String userEmail = currentUser.getEmail();
            List<Booking> userBookings = jsonHandler.getBookingsByUserEmail(userEmail);

            System.out.println("Found " + userBookings.size() + " total bookings for user");
            return userBookings.size();

        } catch (Exception e) {
            System.err.println("Error loading active bookings count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // Clean up listener when controller is destroyed
    public void cleanup() {
        if (languageManager != null) {
            languageManager.removeLanguageChangeListener(this);
        }
    }

    // Inner class remains the same...
    public static class TrekBooking {
        private String name;
        private String location;
        private String difficulty;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;

        public TrekBooking(String name, String location, String difficulty, String description) {
            this.name = name;
            this.location = location;
            this.difficulty = difficulty;
            this.description = description;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }

    public void refreshDashboardData() {
        currentUser = userSession.getCurrentUser();
        setupWelcomeMessage();
        loadUpcomingTrek();
        updateActiveTripsCount();
        setupCalendar();

        System.out.println("Dashboard data refreshed for user: " +
                (currentUser != null ? currentUser.getEmail() : "null"));
    }

    private void loadWeatherData() {
        // Set initial loading state
        if (weatherDescriptionLabel != null) {
            weatherDescriptionLabel.setText(languageManager.getString("weather.loading", "Loading weather..."));
            weatherTempLabel.setText("--°C");
            weatherIconLabel.setText("🔄");
            weatherHumidityLabel.setText("--%");
            weatherWindLabel.setText("-- km/h");

            // Fetch weather data for Kathmandu (main city in Nepal)
            WeatherService.getCurrentWeather("Kathmandu")
                    .thenAccept(weatherData -> {
                        // Update UI on JavaFX Application Thread
                        Platform.runLater(() -> {
                            updateWeatherUI(weatherData);
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            weatherDescriptionLabel.setText(languageManager.getString("weather.unavailable", "Weather unavailable"));
                            weatherIconLabel.setText("❌");
                        });
                        System.err.println("Failed to load weather: " + throwable.getMessage());
                        return null;
                    });
        }
    }

    private void updateWeatherUI(WeatherService.WeatherData weatherData) {
        try {
            if (weatherLocationLabel != null) {
                weatherLocationLabel.setText(weatherData.getLocation());
                weatherDescriptionLabel.setText(weatherData.getDescription());
                weatherTempLabel.setText(String.format("%.1f°C", weatherData.getTemperature()));
                weatherIconLabel.setText(weatherData.getWeatherIcon());
                weatherHumidityLabel.setText(weatherData.getHumidity() + "%");
                weatherWindLabel.setText(String.format("%.1f km/h", weatherData.getWindSpeed()));

                System.out.println("Weather data updated successfully for " + weatherData.getLocation());
            }
        } catch (Exception e) {
            System.err.println("Error updating weather UI: " + e.getMessage());
            if (weatherDescriptionLabel != null) {
                weatherDescriptionLabel.setText(languageManager.getString("weather.error", "Display error"));
                weatherIconLabel.setText("❌");
            }
        }
    }
}
