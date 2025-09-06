package Guide;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import Models.Guide;
import Session.UserSession;
import Storage.AdminJSONHandler;

import java.net.URL;
import java.util.ResourceBundle;

public class GuideProfileContentController implements Initializable {

    // Profile Header Elements
    @FXML private Label avatarLabel;
    @FXML private Label guideNameLabel;

    // Personal Information Form Elements
    @FXML private Label emailField;
    @FXML private Label languageField;
    @FXML private Label dobField;
    @FXML private Label phoneField;
    @FXML private Label experienceField;

    private AdminJSONHandler adminHandler;
    private Guide currentGuide;
    private String currentGuideEmail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        adminHandler = new AdminJSONHandler();

        // Get current guide's email from session
        if (UserSession.getInstance().getCurrentUser() != null) {
            currentGuideEmail = UserSession.getInstance().getCurrentUser().getEmail();
        }

        loadGuideProfile();
        setupFieldStyles();
    }

    private void loadGuideProfile() {
        if (currentGuideEmail == null) {
            showDefaultProfile();
            return;
        }

        // Load guide data from JSON file
        currentGuide = adminHandler.getGuideByEmail(currentGuideEmail);

        if (currentGuide != null) {
            populateProfileFields();
        } else {
            showDefaultProfile();
        }
    }

    private void populateProfileFields() {
        // Set profile header information
        String fullName = currentGuide.getFirstName() + " " + currentGuide.getLastName();
        guideNameLabel.setText(fullName);

        // Set avatar with initials
        String initials = getInitials(currentGuide.getFirstName(), currentGuide.getLastName());
        avatarLabel.setText(initials);

        // Populate form fields
        emailField.setText(currentGuide.getEmail() != null ? currentGuide.getEmail() : "");
        phoneField.setText(currentGuide.getPhone() != null ? currentGuide.getPhone() : "");
        languageField.setText(currentGuide.getProficiencyLanguage() != null ?
                currentGuide.getProficiencyLanguage() : "");
        experienceField.setText(currentGuide.getExperience() != null ?
                currentGuide.getExperience() : "");

        // Date of Birth is not in the Guide model, so set a placeholder
        dobField.setText("Not specified");

        // Make fields read-only for now (can be made editable later)
    }

    private void showDefaultProfile() {
        // Show default values if guide data is not found
        guideNameLabel.setText("Guide Profile");
        avatarLabel.setText("👤");

        emailField.setText("No email available");
        phoneField.setText("No phone available");
        languageField.setText("Languages not specified");
        experienceField.setText("Experience information not available");
        dobField.setText("Not specified");
    }

    private String getInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();

        if (firstName != null && !firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }

        if (lastName != null && !lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }

        return !initials.isEmpty() ? initials.toString().toUpperCase() : "👤";
    }

    private void setupFieldStyles() {
        // Additional styling for better appearance
        if (avatarLabel != null) {
            avatarLabel.setStyle(
                    "-fx-background-color: #9C27B0; " +
                            "-fx-background-radius: 50; " +
                            "-fx-padding: 30; " +
                            "-fx-font-size: 32; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-alignment: center;"
            );
        }

        // Style the experience text area
        if (experienceField != null) {
            experienceField.setWrapText(true);
            experienceField.setStyle(
                    "-fx-background-color: #f8f9fa; " +
                            "-fx-border-color: transparent; " +
                            "-fx-padding: 10; " +
                            "-fx-font-size: 13;"
            );
        }
    }
}
