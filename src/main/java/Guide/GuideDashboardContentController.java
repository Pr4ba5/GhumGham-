package Guide;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import Models.Trek;
import Models.Attraction;
import Session.UserSession;
import Storage.AdminJSONHandler;
import Services.WeatherService;
import javafx.application.Platform;

import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GuideDashboardContentController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label activeTripsLabel;
    @FXML private GridPane calendarGrid;

    // Dynamic trek information labels
    @FXML private Label trekNameLabel;
    @FXML private Label trekLocationLabel;
    @FXML private Label trekDescriptionLabel;
    @FXML private Label trekWarningLabel;
    @FXML private Label difficultyLabel;
    @FXML private VBox upcomingTrekContainer;

    // Weather-related FXML elements
    @FXML private Label weatherLocationLabel;
    @FXML private Label weatherDescriptionLabel;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherIconLabel;
    @FXML private Label weatherHumidityLabel;
    @FXML private Label weatherWindLabel;

    private AdminJSONHandler adminHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        adminHandler = new AdminJSONHandler();
        loadDashboardData();
        loadWeatherData();
        setupCalendar();
    }

    private void loadDashboardData() {
        // Set welcome message
        if (UserSession.getInstance().getCurrentUser() != null) {
            welcomeLabel.setText("Namaste " + UserSession.getInstance().getCurrentUserFullName() + ",");
        }

        // Load upcoming trek information
        loadUpcomingTrek();

        // Load active trips count
        loadActiveTripsCount();
    }

    private void loadUpcomingTrek() {
        if (UserSession.getInstance().getCurrentUser() == null) {
            hideUpcomingTrek();
            return;
        }

        String currentGuideEmail = UserSession.getInstance().getCurrentUser().getEmail();

        // Get all treks for this guide
        List<Trek> guideTreks = adminHandler.getTreksByGuideEmail(currentGuideEmail);

        if (guideTreks.isEmpty()) {
            hideUpcomingTrek();
            return;
        }

        // Find the next upcoming trek (closest future date)
        LocalDate today = LocalDate.now();
        Trek nextTrek = guideTreks.stream()
                .filter(trek -> trek.getStartDate() != null && trek.getStartDate().isAfter(today))
                .min(Comparator.comparing(Trek::getStartDate))
                .orElse(null);

        if (nextTrek == null) {
            // If no future treks, show the most recent one
            nextTrek = guideTreks.stream()
                    .filter(trek -> trek.getStartDate() != null)
                    .max(Comparator.comparing(Trek::getStartDate))
                    .orElse(guideTreks.get(0));
        }

        if (nextTrek != null) {
            displayTrekInformation(nextTrek);
        } else {
            hideUpcomingTrek();
        }
    }

    private void displayTrekInformation(Trek trek) {
        // Set trek name
        trekNameLabel.setText(trek.getTrekName());

        // Get attraction information
        Attraction attraction = adminHandler.getAttractionById(trek.getAttractionId());
        if (attraction != null) {
            trekLocationLabel.setText(attraction.getLocation());

            // Create description from attraction info
            String description = String.format("Trek to %s is located in %s. %s",
                    trek.getTrekName(),
                    attraction.getLocation(),
                    attraction.getRemarks() != null && !attraction.getRemarks().isEmpty()
                            ? attraction.getRemarks()
                            : "An amazing trekking experience awaits you."
            );
            trekDescriptionLabel.setText(description);
        } else {
            trekLocationLabel.setText("Location not available");
            trekDescriptionLabel.setText("Trek information will be updated soon.");
        }

        // Set difficulty
        difficultyLabel.setText(trek.getDifficulty());

        // Set difficulty label style based on difficulty level
        String difficultyStyle = getDifficultyStyle(trek.getDifficulty());
        difficultyLabel.setStyle(difficultyStyle);

        // Set warning message based on altitude
        String altitudeWarning = String.format("⚠️ %s High Altitude location", trek.getMaxAltitude());
        trekWarningLabel.setText(altitudeWarning);

        // Show the container
        upcomingTrekContainer.setVisible(true);
        upcomingTrekContainer.setManaged(true);
    }

    private String getDifficultyStyle(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" ->
                    "-fx-background-color: #4CAF50; -fx-background-radius: 15; -fx-padding: 5 10; -fx-text-fill: white; -fx-font-size: 10;";
            case "moderate", "medium" ->
                    "-fx-background-color: #FF9800; -fx-background-radius: 15; -fx-padding: 5 10; -fx-text-fill: white; -fx-font-size: 10;";
            case "hard" ->
                    "-fx-background-color: #F44336; -fx-background-radius: 15; -fx-padding: 5 10; -fx-text-fill: white; -fx-font-size: 10;";
            default ->
                    "-fx-background-color: #9E9E9E; -fx-background-radius: 15; -fx-padding: 5 10; -fx-text-fill: white; -fx-font-size: 10;";
        };
    }

    private void hideUpcomingTrek() {
        // Hide the upcoming trek section if no treks are found
        if (upcomingTrekContainer != null) {
            upcomingTrekContainer.setVisible(false);
            upcomingTrekContainer.setManaged(false);
        }
    }

    private void loadActiveTripsCount() {
        if (UserSession.getInstance().getCurrentUser() == null) {
            activeTripsLabel.setText("0");
            return;
        }

        String currentGuideEmail = UserSession.getInstance().getCurrentUser().getEmail();
        List<Trek> guideTreks = adminHandler.getTreksByGuideEmail(currentGuideEmail);

        // Count active trips (treks that are ongoing or upcoming)
        LocalDate today = LocalDate.now();
        long activeTrips = guideTreks.stream()
                .filter(trek -> trek.getStartDate() != null)
                .filter(trek -> {
                    LocalDate startDate = trek.getStartDate();
                    // Consider a trek active if it starts within the next 30 days or is ongoing
                    return startDate.isAfter(today.minusDays(30)) && startDate.isBefore(today.plusDays(30));
                })
                .count();

        activeTripsLabel.setText(String.valueOf(activeTrips));
    }

    private void setupCalendar() {
        // Calendar headers
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666; -fx-font-size: 10;");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefWidth(30);
            calendarGrid.add(dayLabel, i, 0);
        }

        // Calendar days with trek dates highlighted
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1; // Monday = 0
        int daysInMonth = today.lengthOfMonth();

        // Get trek dates for current guide
        List<LocalDate> trekDates = getTrekDatesForCurrentMonth();

        int row = 1;
        int col = startDayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefWidth(30);
            dayLabel.setPrefHeight(25);

            LocalDate currentDate = today.withDayOfMonth(day);

            if (day == today.getDayOfMonth()) {
                dayLabel.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-background-radius: 15; -fx-font-weight: bold;");
            } else if (trekDates.contains(currentDate)) {
                dayLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 15; -fx-font-size: 10;");
            } else {
                dayLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 11;");
            }

            calendarGrid.add(dayLabel, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private List<LocalDate> getTrekDatesForCurrentMonth() {
        if (UserSession.getInstance().getCurrentUser() == null) {
            return List.of();
        }

        String currentGuideEmail = UserSession.getInstance().getCurrentUser().getEmail();
        List<Trek> guideTreks = adminHandler.getTreksByGuideEmail(currentGuideEmail);

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        return guideTreks.stream()
                .map(Trek::getStartDate)
                .filter(Objects::nonNull)
                .filter(date -> !date.isBefore(startOfMonth) && !date.isAfter(endOfMonth))
                .collect(Collectors.toList());
    }

    private void loadWeatherData() {
        // Set initial loading state
        if (weatherDescriptionLabel != null) {
            weatherDescriptionLabel.setText("Loading weather...");
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
                            weatherDescriptionLabel.setText("Weather unavailable");
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
                weatherDescriptionLabel.setText("Display error");
                weatherIconLabel.setText("❌");
            }
        }
    }
}
