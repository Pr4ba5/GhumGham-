package Tourist;

import Models.Booking;
import Models.Trek;
import Models.Attraction;
import Models.User;
import Session.UserSession;
import Storage.AdminJSONHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TouristBookingController implements Initializable {

    @FXML
    private TableView<BookingDisplayData> bookingsTable;

    @FXML
    private TableColumn<BookingDisplayData, String> touristColumn;

    @FXML
    private TableColumn<BookingDisplayData, String> guideColumn;

    @FXML
    private TableColumn<BookingDisplayData, String> attractionColumn;

    @FXML
    private TableColumn<BookingDisplayData, String> dateColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button refreshButton;

    @FXML
    private Button viewDetailsButton;

    private AdminJSONHandler jsonHandler;
    private User currentUser;
    private ObservableList<BookingDisplayData> allBookings;
    private ObservableList<BookingDisplayData> filteredBookings;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jsonHandler = new AdminJSONHandler();
        UserSession userSession = UserSession.getInstance();
        currentUser = userSession.getCurrentUser();

        setupTable();
        setupEventHandlers();
        loadUserBookings();

        if (currentUser != null) {
            System.out.println("TouristBookingController initialized for user: " + currentUser.getEmail());
        } else {
            System.err.println("WARNING: No user found in session!");
        }
    }

    private void setupTable() {
        touristColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTouristName()));

        guideColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getGuideName()));

        attractionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAttractionName()));

        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDate()));
    }

    private void setupEventHandlers() {
        // Search field functionality
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applySearchFilter(newValue.trim().toLowerCase());
            });
        }

        // Refresh button functionality
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> handleRefresh());
        }

        // View details button functionality
        if (viewDetailsButton != null) {
            viewDetailsButton.setOnAction(e -> handleViewDetails());

            // Enable/disable view details button based on selection
            bookingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                viewDetailsButton.setDisable(newSelection == null);
            });
        }
    }

    private void loadUserBookings() {
        try {
            if (currentUser == null) {
                System.err.println("No user logged in");
                showEmptyState();
                return;
            }

            String userEmail = currentUser.getEmail();
            System.out.println("Loading bookings for user: " + userEmail);

            List<Booking> userBookings = jsonHandler.getBookingsByUserEmail(userEmail);
            System.out.println("Found " + userBookings.size() + " bookings for user");

            allBookings = FXCollections.observableArrayList();

            for (Booking booking : userBookings) {
                BookingDisplayData displayData = createDisplayData(booking);
                if (displayData != null) {
                    allBookings.add(displayData);
                }
            }

            // Initialize filtered bookings with all bookings
            filteredBookings = FXCollections.observableArrayList(allBookings);
            bookingsTable.setItems(filteredBookings);

            if (allBookings.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
            }

        } catch (Exception e) {
            System.err.println("Error loading user bookings: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load bookings. Please try again.", Alert.AlertType.ERROR);
        }
    }

    private void applySearchFilter(String searchText) {
        if (allBookings == null) return;

        if (searchText.isEmpty()) {
            filteredBookings = FXCollections.observableArrayList(allBookings);
        } else {
            filteredBookings = allBookings.filtered(booking ->
                    booking.getTouristName().toLowerCase().contains(searchText) ||
                            booking.getGuideName().toLowerCase().contains(searchText) ||
                            booking.getAttractionName().toLowerCase().contains(searchText) ||
                            booking.getTrekName().toLowerCase().contains(searchText) ||
                            booking.getFormattedDate().toLowerCase().contains(searchText)
            );
        }

        bookingsTable.setItems(filteredBookings);

        // Update empty state based on filtered results
        if (filteredBookings.isEmpty() && !searchText.isEmpty()) {
            showNoResultsState();
        } else if (filteredBookings.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void handleRefresh() {
        try {
            System.out.println("Refreshing bookings data...");

            // Clear search field
            if (searchField != null) {
                searchField.clear();
            }

            // Reload bookings
            loadUserBookings();

            // Show success message
            showAlert("Success", "Bookings data refreshed successfully!", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            System.err.println("Error refreshing bookings: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to refresh bookings. Please try again.", Alert.AlertType.ERROR);
        }
    }

    private void handleViewDetails() {
        BookingDisplayData selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();

        if (selectedBooking == null) {
            showAlert("No Selection", "Please select a booking to view details.", Alert.AlertType.WARNING);
            return;
        }

        showBookingDetails(selectedBooking);
    }

    private void showBookingDetails(BookingDisplayData bookingData) {
        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("Booking Details");
        detailsAlert.setHeaderText(bookingData.getTrekName());

        String details = String.format(
                "TREK INFORMATION:\n" +
                        "Trek: %s\n" +
                        "Attraction: %s\n" +
                        "Start Date: %s\n\n" +
                        "TOURIST INFORMATION:\n" +
                        "Name: %s\n\n" +
                        "GUIDE INFORMATION:\n" +
                        "Guide: %s\n" +
                        "Email: %s\n\n" +
                        "BOOKING INFORMATION:\n" +
                        "Booking ID: %s\n" +
                        "Status: %s\n" +
                        "Cost: %s\n\n" +
                        "CONTACT:\n" +
                        "For any queries, please contact your guide or our support team.",
                bookingData.getTrekName(),
                bookingData.getAttractionName(),
                bookingData.getFormattedDate(),
                bookingData.getTouristName(),
                bookingData.getGuideName(),
                bookingData.getGuideEmail(),
                bookingData.getBookingId(),
                bookingData.getStatus(),
                bookingData.getAmount()
        );

        detailsAlert.setContentText(details);
        detailsAlert.getDialogPane().setPrefWidth(450);
        detailsAlert.getDialogPane().setStyle("-fx-font-family: 'System'; -fx-font-size: 13px;");

        detailsAlert.showAndWait();
    }

    private BookingDisplayData createDisplayData(Booking booking) {
        try {
            Trek trek = jsonHandler.getTrekById(booking.getTrekId());
            if (trek == null) {
                System.err.println("Trek not found for booking: " + booking.getBookingId());
                return null;
            }

            Attraction attraction = jsonHandler.getAttractionById(trek.getAttractionId());
            String attractionName = (attraction != null) ? attraction.getName() : "Unknown Attraction";

            String guideName = extractGuideName(booking.getGuideEmail());
            String formattedDate = formatDate(booking.getTrekStartDate());
            String amount = String.format("$%.0f", trek.getCost());
            String status = "Confirmed"; // Default status since Booking model doesn't have status field

            return new BookingDisplayData(
                    booking,
                    currentUser.getFullName(),
                    guideName,
                    attractionName,
                    formattedDate,
                    trek.getTrekName(),
                    booking.getBookingId(),
                    booking.getGuideEmail(),
                    status,
                    amount
            );

        } catch (Exception e) {
            System.err.println("Error creating display data for booking: " + booking.getBookingId());
            e.printStackTrace();
            return null;
        }
    }

    private String extractGuideName(String email) {
        if (email == null || email.isEmpty()) {
            return "Not assigned";
        }
        String name = email.split("@")[0];
        // Capitalize first letter and replace dots/underscores with spaces
        name = name.replace(".", " ").replace("_", " ");
        String[] parts = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    private String formatDate(java.time.LocalDate date) {
        if (date == null) return "Not set";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return date.format(formatter);
    }

    private void showEmptyState() {
        bookingsTable.setVisible(true);
        // The table will show its placeholder automatically when empty
    }

    private void hideEmptyState() {
        bookingsTable.setVisible(true);
    }

    private void showNoResultsState() {
        // Table will show empty with current filtered results
        bookingsTable.setVisible(true);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Public method to refresh bookings (can be called externally)
    public void refreshBookings() {
        handleRefresh();
    }

    // Method to refresh user session (can be called from main controller)
    public void refreshUserSession() {
        UserSession userSession = UserSession.getInstance();
        currentUser = userSession.getCurrentUser();
        loadUserBookings();
    }

    // Inner class for table display data
    public static class BookingDisplayData {
        private final Booking originalBooking;
        private final String touristName;
        private final String guideName;
        private final String attractionName;
        private final String formattedDate;
        private final String trekName;
        private final String bookingId;
        private final String guideEmail;
        private final String status;
        private final String amount;

        public BookingDisplayData(Booking originalBooking, String touristName, String guideName,
                                  String attractionName, String formattedDate, String trekName,
                                  String bookingId, String guideEmail, String status, String amount) {
            this.originalBooking = originalBooking;
            this.touristName = touristName;
            this.guideName = guideName;
            this.attractionName = attractionName;
            this.formattedDate = formattedDate;
            this.trekName = trekName;
            this.bookingId = bookingId;
            this.guideEmail = guideEmail;
            this.status = status;
            this.amount = amount;
        }

        // Getters
        public Booking getOriginalBooking() { return originalBooking; }
        public String getTouristName() { return touristName; }
        public String getGuideName() { return guideName; }
        public String getAttractionName() { return attractionName; }
        public String getFormattedDate() { return formattedDate; }
        public String getTrekName() { return trekName; }
        public String getBookingId() { return bookingId; }
        public String getGuideEmail() { return guideEmail; }
        public String getStatus() { return status; }
        public String getAmount() { return amount; }
    }
}
