package Storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import Models.User;
import Models.Guide;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JSONHandler {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.json";
    private static final String GUIDES_FILE = DATA_DIR + "/guides.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static {
        createDataDirectory();
        initializeFile(USERS_FILE);
        initializeFile(GUIDES_FILE);
    }

    private static void createDataDirectory() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    private static void initializeFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
                Files.write(Paths.get(filename), "[]".getBytes()); // Initialize with empty JSON array
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // User operations (includes admins because they are in the same file)
    public static List<User> loadUsers() {
        try {
            String json = new String(Files.readAllBytes(Paths.get(USERS_FILE)));
            Type listType = new TypeToken<List<User>>() {}.getType();
            List<User> users = gson.fromJson(json, listType);
            return users != null ? users : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static boolean saveUsers(List<User> users) {
        try {
            String json = gson.toJson(users);
            Files.write(Paths.get(USERS_FILE), json.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean userExists(String email) {
        List<User> users = loadUsers();
        return users.stream().anyMatch(user -> user.getEmail().equals(email));
    }

    // Guide operations
    public static List<Guide> loadGuides() {
        try {
            String json = new String(Files.readAllBytes(Paths.get(GUIDES_FILE)));
            Type listType = new TypeToken<List<Guide>>() {}.getType();
            List<Guide> guides = gson.fromJson(json, listType);
            return guides != null ? guides : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static boolean saveGuides(List<Guide> guides) {
        try {
            String json = gson.toJson(guides);
            Files.write(Paths.get(GUIDES_FILE), json.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean guideExists(String email) {
        List<Guide> guides = loadGuides();
        return guides.stream().anyMatch(guide -> guide.getEmail().equals(email));
    }

    public List<Guide> getGuides() {
        return loadGuides();
    }

    public static boolean addUser(User user) {
        try {
            List<User> users = loadUsers();

            // Check if user already exists
            if (users.stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
                return false; // User already exists
            }

            users.add(user);
            return saveUsers(users);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addGuide(Guide guide) {
        try {
            List<Guide> guides = loadGuides();

            // Check if guide already exists
            if (guides.stream().anyMatch(g -> g.getEmail().equals(guide.getEmail()))) {
                return false; // Guide already exists
            }

            guides.add(guide);
            return saveGuides(guides);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteGuide(String email) {
        try {
            List<Guide> guides = loadGuides();
            boolean removed = guides.removeIf(guide ->
                    guide.getEmail().equalsIgnoreCase(email));

            if (removed) {
                return saveGuides(guides);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteUser(String email) {
        try {
            List<User> users = loadUsers();
            boolean removed = users.removeIf(user ->
                    user.getEmail().equalsIgnoreCase(email) &&
                            "user".equalsIgnoreCase(user.getUserType()));

            if (removed) {
                return saveUsers(users);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
