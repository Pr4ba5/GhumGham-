package Admin;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Separator;
import Session.UserSession;
import Storage.JSONHandler;
import Storage.AdminJSONHandler;
import Models.Emergency;
import Models.Booking;
import Models.Trek;
import Models.Attraction;
import Models.User;
import Services.WeatherService;
import javafx.scene.chart.PieChart;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardContentController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label totalTouristsLabel;
    @FXML private Label activeGuidesLabel;
    @FXML private Label attractionsCount;
    @FXML private Label totalEmergenciesLabel;
    @FXML private VBox activityList;
    @FXML private BarChart<String, Number> bookingTrendsChart;
    @FXML private PieChart touristsPieChart;
    @FXML private VBox nationalityLegend;
    @FXML private Label nationalityStatsLabel;

    // Weather-related FXML elements
    @FXML private Label weatherLocationLabel;
    @FXML private Label weatherDescriptionLabel;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherIconLabel;
    @FXML private Label weatherHumidityLabel;
    @FXML private Label weatherWindLabel;

    private JSONHandler fileManager;
    private AdminJSONHandler adminJsonHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileManager = new JSONHandler();
        adminJsonHandler = new AdminJSONHandler();

        updateCurrentDate();
        loadDashboardData();
        setupSimplifiedBookingTrendsChart();
        loadSimplifiedRecentActivities();
        loadWeatherData();
        setupNationalityPieChart();
    }

    private void updateCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        currentDateLabel.setText(now.format(formatter) + " - Dashboard Overview");
    }

    private void loadDashboardData() {
        // Set welcome message
        if (UserSession.getInstance().getCurrentUser() != null) {
            welcomeLabel.setText("Namaste " + UserSession.getInstance().getCurrentUserFullName() + ",");
        }

        // Load statistics
        totalTouristsLabel.setText(String.valueOf(getTotalTourists()));
        activeGuidesLabel.setText(String.valueOf(getTotalGuides()));
        attractionsCount.setText(String.valueOf(getTotalAttractions()));
        totalEmergenciesLabel.setText(String.valueOf(getTotalEmergencies()));
    }

    private void loadWeatherData() {
        // Set initial loading state
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

    private void updateWeatherUI(WeatherService.WeatherData weatherData) {
        try {
            weatherLocationLabel.setText(weatherData.getLocation());
            weatherDescriptionLabel.setText(weatherData.getDescription());
            weatherTempLabel.setText(String.format("%.1f°C", weatherData.getTemperature()));
            weatherIconLabel.setText(weatherData.getWeatherIcon());
            weatherHumidityLabel.setText(weatherData.getHumidity() + "%");
            weatherWindLabel.setText(String.format("%.1f km/h", weatherData.getWindSpeed()));

            System.out.println("Weather data updated successfully for " + weatherData.getLocation());
        } catch (Exception e) {
            System.err.println("Error updating weather UI: " + e.getMessage());
            weatherDescriptionLabel.setText("Display error");
            weatherIconLabel.setText("❌");
        }
    }

    private int getTotalTourists() {
        try {
            return fileManager.loadUsers().size();
        } catch (Exception e) {
            System.err.println("Error loading tourists count: " + e.getMessage());
            return 0;
        }
    }

    private int getTotalGuides() {
        try {
            return adminJsonHandler.loadGuides().size();
        } catch (Exception e) {
            System.err.println("Error loading guides count: " + e.getMessage());
            return 0;
        }
    }

    private int getTotalAttractions() {
        try {
            return adminJsonHandler.loadAttractions().size();
        } catch (Exception e) {
            System.err.println("Error loading attractions count: " + e.getMessage());
            return 0;
        }
    }

    private int getTotalEmergencies() {
        try {
            return adminJsonHandler.loadEmergencies().size();
        } catch (Exception e) {
            System.err.println("Error loading emergencies count: " + e.getMessage());
            return 0;
        }
    }

    private void setupSimplifiedBookingTrendsChart() {
        try {
            // Load bookings and related data
            List<Booking> allBookings = adminJsonHandler.loadBookings();
            List<Trek> allTreks = adminJsonHandler.loadTreks();
            List<Attraction> allAttractions = adminJsonHandler.loadAttractions();

            // Create a map of trek ID to attraction name
            Map<Integer, String> trekToAttractionMap = allTreks.stream()
                    .collect(Collectors.toMap(
                            Trek::getId,
                            trek -> {
                                Attraction attraction = allAttractions.stream()
                                        .filter(a -> a.getId() == trek.getAttractionId())
                                        .findFirst()
                                        .orElse(null);
                                return attraction != null ? attraction.getName() : "Unknown";
                            }
                    ));

            // Count bookings by attraction
            Map<String, Long> attractionBookingCounts = allBookings.stream()
                    .collect(Collectors.groupingBy(
                            booking -> trekToAttractionMap.getOrDefault(booking.getTrekId(), "Unknown"),
                            Collectors.counting()
                    ));

            // Clear existing data
            bookingTrendsChart.getData().clear();

            // Create chart series with better colors
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Bookings");

            // Add data to series (limit to top 8 attractions for readability)
            attractionBookingCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(8)
                    .forEach(entry -> {
                        // Truncate long attraction names for better display
                        String displayName = entry.getKey().length() > 15
                                ? entry.getKey().substring(0, 12) + "..."
                                : entry.getKey();
                        series.getData().add(new XYChart.Data<>(displayName, entry.getValue()));
                    });

            // Add series to chart
            bookingTrendsChart.getData().add(series);

            // Better chart styling with custom colors
            bookingTrendsChart.setAnimated(true);
            bookingTrendsChart.setLegendVisible(false);

            // Apply custom CSS for better colors
            bookingTrendsChart.setStyle(
                    ".chart-bar { " +
                            "    -fx-bar-fill: linear-gradient(to top, #667eea, #764ba2); " +
                            "} " +
                            ".chart-plot-background { " +
                            "    -fx-background-color: transparent; " +
                            "}"
            );

            System.out.println("Simplified booking trends chart updated with " + attractionBookingCounts.size() + " attractions");

        } catch (Exception e) {
            System.err.println("Error setting up simplified booking trends chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSimplifiedRecentActivities() {
        try {
            activityList.getChildren().clear();

            // Load recent emergencies
            List<Emergency> recentEmergencies = adminJsonHandler.loadEmergencies().stream()
                    .sorted((e1, e2) -> e2.getReportedAt().compareTo(e1.getReportedAt()))
                    .limit(6)
                    .toList();

            for (Emergency emergency : recentEmergencies) {
                VBox activityItem = createSimplifiedActivityItem(
                        getEmergencyIcon(emergency.getEmergencyType()),
                        "Emergency: " + emergency.getEmergencyType(),
                        "Reported by " + emergency.getGuideName() + " - " + emergency.getDescription(),
                        formatTimeAgo(emergency.getReportedAt()),
                        getEmergencyStatusColor(emergency.getEmergencyType())
                );
                activityList.getChildren().add(activityItem);
            }

            // Add some sample non-emergency activities for variety
            if (recentEmergencies.size() < 6) {
                activityList.getChildren().add(createSimplifiedActivityItem(
                        "👤", "New User Registration",
                        "Tourist registered for Everest Base Camp trek",
                        "2 hours ago", "#4CAF50"));

                activityList.getChildren().add(createSimplifiedActivityItem(
                        "📅", "Trek Booking",
                        "Annapurna Circuit trek booking confirmed",
                        "4 hours ago", "#2196F3"));
            }

        } catch (Exception e) {
            System.err.println("Error loading simplified recent activities: " + e.getMessage());
            e.printStackTrace();

            // Add error activity item
            activityList.getChildren().add(createSimplifiedActivityItem(
                    "❌", "System Error",
                    "Failed to load recent activities",
                    "Just now", "#F44336"));
        }
    }

    private VBox createSimplifiedActivityItem(String icon, String title, String description, String time, String statusColor) {
        VBox activityItem = new VBox(12);
        activityItem.setStyle(
                "-fx-background-color: #fafbfc; " +
                        "-fx-padding: 20; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-color: #e1e5e9; " +
                        "-fx-border-radius: 15; " +
                        "-fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );

        // Header with icon, title and time
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");

        VBox titleSection = new VBox(3);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333333;");

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999999;");

        titleSection.getChildren().addAll(titleLabel, timeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status indicator
        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-font-size: 12px; -fx-text-fill: " + statusColor + ";");

        header.getChildren().addAll(iconLabel, titleSection, spacer, statusDot);

        // Description
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-wrap-text: true;");
        descLabel.setWrapText(true);

        activityItem.getChildren().addAll(header, descLabel);
        return activityItem;
    }

    private String getEmergencyIcon(String emergencyType) {
        return switch (emergencyType.toLowerCase()) {
            case "medical emergency", "medical" -> "🏥";
            case "lost tourist", "lost" -> "🔍";
            case "weather emergency", "weather" -> "🌩️";
            case "equipment failure", "equipment" -> "⚙️";
            default -> "🚨";
        };
    }

    private String getEmergencyStatusColor(String emergencyType) {
        return switch (emergencyType.toLowerCase()) {
            case "medical emergency", "medical" -> "#F44336";
            case "lost tourist", "lost" -> "#FF9800";
            case "weather emergency", "weather" -> "#2196F3";
            default -> "#9C27B0";
        };
    }

    private void setupNationalityPieChart() {
        try {
            // Load all users (tourists)
            List<User> allUsers = fileManager.loadUsers();

            // Filter only tourists (exclude admins)
            List<User> tourists = allUsers.stream()
                    .filter(user -> "user".equalsIgnoreCase(user.getUserType()))
                    .collect(Collectors.toList());

            // Count nationalities
            Map<String, Long> nationalityCounts = tourists.stream()
                    .filter(user -> user.getNationality() != null && !user.getNationality().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            user -> user.getNationality().trim(),
                            Collectors.counting()
                    ));

            // Clear existing data
            touristsPieChart.getData().clear();
            nationalityLegend.getChildren().clear();

            // Add legend header back
            Label legendTitle = new Label("Legend");
            legendTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333333;");
            nationalityLegend.getChildren().add(legendTitle);

            Separator separator = new Separator();
            separator.setStyle("-fx-background-color: #dee2e6;");
            nationalityLegend.getChildren().add(separator);

            // Define colors for consistency
            String[] colors = {
                    "#667eea", "#764ba2", "#f093fb", "#f5576c",
                    "#4facfe", "#00f2fe", "#43e97b", "#38f9d7",
                    "#ffecd2", "#fcb69f", "#a8edea", "#fed6e3",
                    "#ff9a9e", "#fecfef", "#ffeaa7", "#fab1a0"
            };

            if (nationalityCounts.isEmpty()) {
                // Show placeholder if no data
                PieChart.Data noDataSlice = new PieChart.Data("No Data Available", 1);
                touristsPieChart.getData().add(noDataSlice);

                // Add no data legend item
                HBox legendItem = createLegendItem("#cccccc", "No Data Available", 0, 0.0);
                nationalityLegend.getChildren().add(legendItem);

                // Style the no-data slice
                Platform.runLater(() -> {
                    noDataSlice.getNode().setStyle("-fx-pie-color: #cccccc;");
                });

                nationalityStatsLabel.setText("No nationality data available");
            } else {
                // Sort nationalities by count (descending)
                List<Map.Entry<String, Long>> sortedEntries = nationalityCounts.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .collect(Collectors.toList());

                // Add nationality data to pie chart and create legend
                for (int i = 0; i < sortedEntries.size(); i++) {
                    Map.Entry<String, Long> entry = sortedEntries.get(i);
                    String nationality = entry.getKey();
                    Long count = entry.getValue();
                    String color = colors[i % colors.length];

                    // Calculate percentage
                    double percentage = (count * 100.0) / tourists.size();

                    // Create pie chart slice (without percentage in label for cleaner look)
                    PieChart.Data slice = new PieChart.Data(nationality, count);
                    touristsPieChart.getData().add(slice);

                    // Create legend item
                    HBox legendItem = createLegendItem(color, nationality, count, percentage);
                    nationalityLegend.getChildren().add(legendItem);

                    // Apply color to slice
                    final String finalColor = color;
                    Platform.runLater(() -> {
                        slice.getNode().setStyle("-fx-pie-color: " + finalColor + ";");
                    });
                }

                // Update stats label
                nationalityStatsLabel.setText(String.format("Showing %d nationalities from %d tourists",
                        nationalityCounts.size(), tourists.size()));
            }

            // Style the pie chart for better responsiveness
            touristsPieChart.setLegendVisible(false); // We have our custom legend
            touristsPieChart.setLabelsVisible(true);
            touristsPieChart.setStartAngle(90);
            touristsPieChart.setClockwise(true);

            // Make pie chart responsive
            touristsPieChart.setMinSize(300, 300);
            touristsPieChart.setPrefSize(400, 400);
            touristsPieChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // Apply custom styling
            touristsPieChart.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-border-color: transparent;"
            );

            int totalTourists = tourists.size();
            int totalNationalities = nationalityCounts.size();

            System.out.println("Enhanced nationality pie chart updated:");
            System.out.println("Total tourists: " + totalTourists);
            System.out.println("Different nationalities: " + totalNationalities);
            nationalityCounts.forEach((nationality, count) ->
                    System.out.println("  " + nationality + ": " + count + " tourists"));

        } catch (Exception e) {
            System.err.println("Error setting up nationality pie chart: " + e.getMessage());
            e.printStackTrace();

            // Show error in pie chart
            touristsPieChart.getData().clear();
            nationalityLegend.getChildren().clear();

            PieChart.Data errorSlice = new PieChart.Data("Error Loading Data", 1);
            touristsPieChart.getData().add(errorSlice);

            // Add error legend
            Label legendTitle = new Label("Legend");
            legendTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333333;");
            nationalityLegend.getChildren().add(legendTitle);

            HBox errorLegend = createLegendItem("#ff6b6b", "Error Loading Data", 1, 100.0);
            nationalityLegend.getChildren().add(errorLegend);

            Platform.runLater(() -> {
                errorSlice.getNode().setStyle("-fx-pie-color: #ff6b6b;");
            });

            nationalityStatsLabel.setText("Error loading nationality data");
        }
    }

    private HBox createLegendItem(String color, String nationality, long count, double percentage) {
        HBox legendItem = new HBox(10);
        legendItem.setAlignment(Pos.CENTER_LEFT);
        legendItem.setStyle("-fx-padding: 5 0;");

        // Color indicator (small rectangle)
        Region colorBox = new Region();
        colorBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 3;");
        colorBox.setPrefSize(16, 16);
        colorBox.setMinSize(16, 16);
        colorBox.setMaxSize(16, 16);

        // Nationality label with count and percentage
        Label nationalityLabel = new Label(nationality);
        nationalityLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333333;");

        // Stats label
        Label statsLabel = new Label(String.format("(%d - %.1f%%)", count, percentage));
        statsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        // Spacer to push stats to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        legendItem.getChildren().addAll(colorBox, nationalityLabel, spacer, statsLabel);
        return legendItem;
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        // Simplified time formatting - you can enhance this
        return "Recently";
    }
}
