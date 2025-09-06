package Models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Emergency {
    private int id;
    private String guideName;
    private String guideEmail;
    private String emergencyType;
    private String description;
    private String location;
    private String severity; // "Low", "Medium", "High", "Critical"
    private String status; // "Reported", "In Progress", "Resolved"
    private transient LocalDateTime reportedAt;
    private String reportedAtStr;
    private transient LocalDateTime resolvedAt;
    private String resolvedAtStr;
    private String contactNumber;
    private String additionalNotes;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Emergency() {
        this.reportedAt = LocalDateTime.now();
        this.reportedAtStr = this.reportedAt.format(FORMATTER);
        this.status = "Reported";
        this.severity = "Medium";
    }

    public Emergency(String guideName, String guideEmail, String emergencyType, String description,
                     String location, String severity, String contactNumber) {
        this();
        this.guideName = guideName;
        this.guideEmail = guideEmail;
        this.emergencyType = emergencyType;
        this.description = description;
        this.location = location;
        this.severity = severity;
        this.contactNumber = contactNumber;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getGuideName() { return guideName; }

    public String getGuideEmail() { return guideEmail; }
    public void setGuideEmail(String guideEmail) { this.guideEmail = guideEmail; }

    public String getEmergencyType() { return emergencyType; }

    public String getDescription() { return description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSeverity() { return severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getReportedAt() {
        if (reportedAt == null && reportedAtStr != null) {
            reportedAt = LocalDateTime.parse(reportedAtStr, FORMATTER);
        }
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
        this.reportedAtStr = (reportedAt != null) ? reportedAt.format(FORMATTER) : null;
    }

    public String getReportedAtStr() { return reportedAtStr; }
    public void setReportedAtStr(String reportedAtStr) {
        this.reportedAtStr = reportedAtStr;
        if (reportedAtStr != null) {
            this.reportedAt = LocalDateTime.parse(reportedAtStr, FORMATTER);
        }
    }

    public LocalDateTime getResolvedAt() {
        if (resolvedAt == null && resolvedAtStr != null) {
            resolvedAt = LocalDateTime.parse(resolvedAtStr, FORMATTER);
        }
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
        this.resolvedAtStr = (resolvedAt != null) ? resolvedAt.format(FORMATTER) : null;
    }

    public String getResolvedAtStr() { return resolvedAtStr; }
    public void setResolvedAtStr(String resolvedAtStr) {
        this.resolvedAtStr = resolvedAtStr;
        if (resolvedAtStr != null) {
            this.resolvedAt = LocalDateTime.parse(resolvedAtStr, FORMATTER);
        }
    }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    @Override
    public String toString() {
        return "Emergency{" +
                "id=" + id +
                ", guideName='" + guideName + '\'' +
                ", emergencyType='" + emergencyType + '\'' +
                ", severity='" + severity + '\'' +
                ", status='" + status + '\'' +
                ", location='" + location + '\'' +
                ", reportedAt=" + getReportedAt() +
                '}';
    }
}
