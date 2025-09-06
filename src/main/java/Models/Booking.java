package Models;

import Storage.AdminJSONHandler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Booking {
    private int id;
    private String bookingId;
    private int trekId;
    private String userEmail;
    private String guideEmail;
    private String trekStartDateStr;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public Booking() {
    }

    public Booking(int trekId, String userEmail, String guideEmail) {
        this.trekId = trekId;
        this.userEmail = userEmail;
        this.guideEmail = guideEmail;
        this.bookingId = generateBookingId();

        AdminJSONHandler jsonHandler = new AdminJSONHandler();
        Trek matchedTrek = jsonHandler.getTrekById(trekId);

        if (matchedTrek != null && matchedTrek.getStartDate() != null) {
            this.trekStartDateStr = matchedTrek.getStartDate().format(FORMATTER);
        } else {
            System.err.println("Trek with ID " + trekId + " not found or has no start date.");
        }
    }

    public Booking(int trekId, String userEmail, String guideEmail, LocalDate trekStartDate) {
        this(trekId, userEmail, guideEmail);
        if (trekStartDate != null) {
            this.trekStartDateStr = trekStartDate.format(FORMATTER);
        }
    }

    private String generateBookingId() {
        return "BK" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBookingId() { return bookingId; }

    public int getTrekId() { return trekId; }

    public String getUserEmail() { return userEmail; }

    public String getGuideEmail() { return guideEmail; }
    public void setGuideEmail(String guideEmail) { this.guideEmail = guideEmail; }

    public LocalDate getTrekStartDate() {
        return (trekStartDateStr != null) ? LocalDate.parse(trekStartDateStr, FORMATTER) : null;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", trekId=" + trekId +
                ", userEmail='" + userEmail + '\'' +
                ", guideEmail='" + guideEmail + '\'' +
                ", trekStartDateStr='" + trekStartDateStr + '\'' +
                '}';
    }
}
