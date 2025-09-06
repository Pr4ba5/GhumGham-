package Session;

import Models.User;

public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("User session started for: " + (user != null ? user.getEmail() : "null"));
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentUserFullName() {
        return currentUser != null
                ? currentUser.getFirstName() + " " + currentUser.getLastName()
                : null;
    }

    // NEW: Logout method to clear session
    public void logout() {
        if (currentUser != null) {
            System.out.println("Logging out user: " + currentUser.getEmail());
            currentUser = null;
            System.out.println("User session cleared successfully");
        } else {
            System.out.println("No active session to logout");
        }
    }

    // NEW: Check if user is logged in
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
