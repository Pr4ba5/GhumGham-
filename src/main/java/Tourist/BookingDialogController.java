package Tourist;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.ObservableList;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class BookingDialogController implements Initializable {

    @FXML
    private TableView<Booking> bookingsTable;

    @FXML
    private TableColumn<Booking, String> trekNameColumn;

    @FXML
    private TableColumn<Booking, String> statusColumn;

    @FXML
    private TableColumn<Booking, LocalDate> startDateColumn;

    @FXML
    private TableColumn<Booking, LocalDate> endDateColumn;

    @FXML
    private TableColumn<Booking, String> guideColumn;

    @FXML
    private Button cancelBookingButton;

    @FXML
    private Button viewDetailsButton;

    @FXML
    private ComboBox<String> statusFilter;

    private ObservableList<Booking> allBookings;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadBookings();
        setupEventHandlers();
    }

    private void setupTable() {
        trekNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTrekName()));

        statusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));

        startDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStartDate()));

        endDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getEndDate()));

        guideColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGuideName()));
    }



    private void loadBookings() {
        bookingsTable.setItems(allBookings);
    }

    private void setupEventHandlers() {
        cancelBookingButton.setOnAction(e -> cancelSelectedBooking());
        viewDetailsButton.setOnAction(e -> viewBookingDetails());

        // Enable/disable buttons based on selection
        bookingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            cancelBookingButton.setDisable(!hasSelection);
            viewDetailsButton.setDisable(!hasSelection);
        });
    }

    @FXML
    private void cancelSelectedBooking() {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Cancel Booking");
            confirmAlert.setHeaderText("Cancel " + selectedBooking.getTrekName());
            confirmAlert.setContentText("Are you sure you want to cancel this booking?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    selectedBooking.setStatus("Cancelled");
                    bookingsTable.refresh();
                    showAlert();
                }
            });
        }
    }

    @FXML
    private void viewBookingDetails() {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            showBookingDetails(selectedBooking);
        }
    }

    private void showBookingDetails(Booking booking) {
        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("Booking Details");
        detailsAlert.setHeaderText(booking.getTrekName());

        String details = String.format(
                "Status: %s\nStart Date: %s\nEnd Date: %s\nGuide: %s\nBooking ID: %s",
                booking.getStatus(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getGuideName(),
                booking.getBookingId()
        );

        detailsAlert.setContentText(details);
        detailsAlert.showAndWait();
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Booking cancelled successfully!");
        alert.showAndWait();
    }

    // Inner class for booking data
    public static class Booking {
        private final String bookingId;
        private final String trekName;
        private String status;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final String guideName;

        public Booking(String trekName, String status, LocalDate startDate, LocalDate endDate, String guideName) {
            this.bookingId = "BK" + System.currentTimeMillis();
            this.trekName = trekName;
            this.status = status;
            this.startDate = startDate;
            this.endDate = endDate;
            this.guideName = guideName;
        }

        // Getters and setters
        public String getBookingId() { return bookingId; }
        public String getTrekName() { return trekName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public String getGuideName() { return guideName; }
    }
}
