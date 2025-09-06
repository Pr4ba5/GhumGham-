package Admin;

import Models.Booking;
import Models.Trek;
import Models.Attraction;
import Models.User;
import Models.Guide;
import Storage.AdminJSONHandler;
import Storage.JSONHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminBookingsController implements Initializable {

    @FXML
    private TableView<BookingDisplayData> bookingsTable;

    @FXML
    private TableColumn<BookingDisplayData, String> bookingIdColumn;

    @FXML
    private TableColumn<BookingDisplayData, String> touristColumn;

    @FXML
    private TableColumn<BookingDisplayData, String> attractionColumn;

    @FXML
    private TableColumn<BookingDisplayData, String> guideColumn;

    @FXML
    private TableColumn<BookingDisplayData, String> dateColumn;

    @FXML
    private TableColumn<BookingDisplayData, String> amountColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button refreshButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Label totalBookingsLabel;

    @FXML
    private Label selectedBookingLabel;

    private ObservableList<BookingDisplayData> allBookings;
    private ObservableList<BookingDisplayData> filteredBookings;
    private AdminJSONHandler jsonHandler;
    private JSONHandler userJsonHandler;
    private BookingDisplayData selectedBooking;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jsonHandler = new AdminJSONHandler();
        userJsonHandler = new JSONHandler();

        setupTable();
        setupEventHandlers();
        setupTableSelection();
        loadAllBookingsInitial(); // Initial load without notification

        System.out.println("AdminBookingsController initialized - loading all bookings");
    }

    private void setupTable() {
        // Set up table columns
        bookingIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBookingId()));

        touristColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTouristName()));

        attractionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAttractionName()));

        guideColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getGuideName()));

        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDate()));

        amountColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAmount()));

        // Add row click handler
        bookingsTable.setRowFactory(tv -> {
            TableRow<BookingDisplayData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    BookingDisplayData clickedBooking = row.getItem();
                    selectBooking(clickedBooking);
                }
            });
            return row;
        });
    }

    private void setupTableSelection() {
        bookingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectBooking(newSelection);
            } else {
                clearSelection();
            }
        });
    }

    private void selectBooking(BookingDisplayData booking) {
        selectedBooking = booking;
        selectedBookingLabel.setText("Selected: " + booking.getBookingId() + " - " + booking.getTouristName());

        // Make delete button opaque and enabled
        deleteButton.setOpacity(1.0);
        deleteButton.setDisable(false);
    }

    private void clearSelection() {
        selectedBooking = null;
        selectedBookingLabel.setText("No booking selected");

        // Make delete button transparent and disabled
        deleteButton.setOpacity(0.3);
        deleteButton.setDisable(true);
    }

    @FXML
    private void deleteSelectedBooking() {
        if (selectedBooking == null) {
            showAlert("Warning", "Please select a booking to delete.");
            return;
        }

        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Booking");
        confirmAlert.setContentText("Are you sure you want to delete the booking: " +
                selectedBooking.getBookingId() + " for " + selectedBooking.getTouristName() +
                "?\n\nThis action cannot be undone.");

        // Add custom buttons
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);

        // Style the delete button to be red
        confirmAlert.getDialogPane().lookupButton(deleteButtonType).setStyle(
                "-fx-background-color: #DC3545; -fx-text-fill: white;"
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == deleteButtonType) {
            // Get the original booking ID from the display data
            int bookingId = Integer.parseInt(selectedBooking.getBookingId());

            // Proceed with deletion
            boolean success = jsonHandler.deleteBooking(bookingId);

            if (success) {
                showAlert("Success", "Booking '" + selectedBooking.getBookingId() + "' has been deleted successfully.");
                loadAllBookingsInitial(); // Refresh the table
                clearSelection(); // Clear the selection
            } else {
                showAlert("Error", "Failed to delete the booking. Please try again.");
            }
        }
    }

    private void setupEventHandlers() {
        // Refresh button shows notification
        refreshButton.setOnAction(e -> loadAllBookingsWithNotification());

        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
            clearSelection(); // Clear selection when searching
        });
    }

    // Initial loading without notification (called during initialization)
    private void loadAllBookingsInitial() {
        loadBookingsData(false);
    }

    // Refresh with notification (called when refresh button is pressed)
    private void loadAllBookingsWithNotification() {
        loadBookingsData(true);
    }

    // Core method that loads data with optional notification
    private void loadBookingsData(boolean showNotification) {
        try {
            System.out.println("Loading all bookings from JSON file...");

            // Get ALL bookings from JSON file
            List<Booking> allBookingsList = jsonHandler.loadBookings();
            System.out.println("Found " + allBookingsList.size() + " total bookings");

            // Convert to display data
            allBookings = FXCollections.observableArrayList();

            for (Booking booking : allBookingsList) {
                BookingDisplayData displayData = createDisplayData(booking);
                if (displayData != null) {
                    allBookings.add(displayData);
                }
            }

            updateBookingsDisplay(allBookings);
            applyFilters();
            clearSelection(); // Clear selection when data is reloaded

            // Only show success notification if requested (i.e., when refresh button is pressed)
            if (showNotification) {
                showSuccess();
            }

        } catch (Exception e) {
            System.err.println("Error loading all bookings: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load bookings. Please try again.");
        }
    }

    private BookingDisplayData createDisplayData(Booking booking) {
        try {
            // Get trek information
            Trek trek = jsonHandler.getTrekById(booking.getTrekId());
            if (trek == null) {
                System.err.println("Trek not found for booking: " + booking.getBookingId());
                return null;
            }

            // Get attraction information
            Attraction attraction = jsonHandler.getAttractionById(trek.getAttractionId());
            String attractionName = (attraction != null) ? attraction.getName() : "Unknown Attraction";

            // Get tourist name from user email
            String touristName = getTouristNameByEmail(booking.getUserEmail());

            // Extract guide name from email
            String guideName = extractGuideName(booking.getGuideEmail());

            // Format date
            String formattedDate = formatDate(booking.getTrekStartDate());

            // Format amount
            String amount = String.format("$%.0f", trek.getCost());

            return new BookingDisplayData(
                    booking,
                    String.valueOf(booking.getId()),
                    touristName,
                    attractionName,
                    guideName,
                    formattedDate,
                    amount,
                    trek.getTrekName()
            );

        } catch (Exception e) {
            System.err.println("Error creating display data for booking: " + booking.getBookingId());
            e.printStackTrace();
            return null;
        }
    }

    private String getTouristNameByEmail(String userEmail) {
        try {
            if (userEmail == null || userEmail.isEmpty()) {
                return "Unknown Tourist";
            }

            // Load users from JSON file
            List<User> users = userJsonHandler.loadUsers();

            // Find user by email
            User user = users.stream()
                    .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(userEmail))
                    .findFirst()
                    .orElse(null);

            if (user != null) {
                return user.getFullName();
            } else {
                // If user not found, extract name from email
                return extractNameFromEmail(userEmail);
            }

        } catch (Exception e) {
            System.err.println("Error getting tourist name for email: " + userEmail);
            e.printStackTrace();
            return extractNameFromEmail(userEmail);
        }
    }

    private String extractNameFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "Unknown Tourist";
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

    private String extractGuideName(String email) {
        if (email == null || email.isEmpty()) {
            return "Not assigned";
        }

        try {
            // Try to get guide from JSON first
            Guide guide = jsonHandler.getGuideByEmail(email);
            if (guide != null) {
                return guide.getFullName();
            }
        } catch (Exception e) {
            System.err.println("Error getting guide info: " + e.getMessage());
        }

        // Fallback to extracting from email
        return extractNameFromEmail(email);
    }

    private String formatDate(java.time.LocalDate date) {
        if (date == null) return "Not set";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return date.format(formatter);
    }

    private void applyFilters() {
        if (allBookings == null) return;

        String searchText = searchField.getText().toLowerCase().trim();

        filteredBookings = allBookings.filtered(booking -> {
            // Search filter
            boolean searchMatch = searchText.isEmpty() ||
                    booking.getBookingId().toLowerCase().contains(searchText) ||
                    booking.getTouristName().toLowerCase().contains(searchText) ||
                    booking.getAttractionName().toLowerCase().contains(searchText) ||
                    booking.getGuideName().toLowerCase().contains(searchText);

            return searchMatch;
        });

        bookingsTable.setItems(filteredBookings);
        updateTotalBookingsLabel();
    }

    private void updateBookingsDisplay(ObservableList<BookingDisplayData> bookings) {
        allBookings = bookings;
        bookingsTable.setItems(bookings);
        updateTotalBookingsLabel();
    }

    private void updateTotalBookingsLabel() {
        int totalCount = (filteredBookings != null) ? filteredBookings.size() :
                (allBookings != null) ? allBookings.size() : 0;
        totalBookingsLabel.setText("Total Bookings: " + totalCount);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess() {
        showAlert("Success", "Data refreshed successfully!");
    }

    @FXML
    public void refreshBookings() {
        loadAllBookingsWithNotification();
    }

    public static class BookingDisplayData {
        private final Booking originalBooking;
        private final String bookingId;
        private final String touristName;
        private final String attractionName;
        private final String guideName;
        private final String formattedDate;
        private final String amount;
        private final String trekName;

        public BookingDisplayData(Booking originalBooking, String bookingId, String touristName,
                                  String attractionName, String guideName, String formattedDate,
                                  String amount, String trekName) {
            this.originalBooking = originalBooking;
            this.bookingId = bookingId;
            this.touristName = touristName;
            this.attractionName = attractionName;
            this.guideName = guideName;
            this.formattedDate = formattedDate;
            this.amount = amount;
            this.trekName = trekName;
        }

        // Getters
        public String getBookingId() { return bookingId; }
        public String getTouristName() { return touristName; }
        public String getAttractionName() { return attractionName; }
        public String getGuideName() { return guideName; }
        public String getFormattedDate() { return formattedDate; }
        public String getAmount() { return amount; }
        public String getTrekName() { return trekName; }
        public Booking getOriginalBooking() { return originalBooking; }
    }
}
