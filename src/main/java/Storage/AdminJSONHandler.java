package Storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import Models.Attraction;
import Models.Trek;
import Models.Booking;
import Models.Guide;
import Models.Emergency;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminJSONHandler {
    private static final String ATTRACTIONS_FILE = "data/attractions.json";
    private static final String TREKS_FILE = "data/treks.json";
    private static final String BOOKINGS_FILE = "data/bookings.json";
    private static final String GUIDES_FILE = "data/guides.json";
    private static final String EMERGENCIES_FILE = "data/emergencies.json";

    private final Gson gson;

    public AdminJSONHandler() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        initializeFiles();
    }

    private void initializeFiles() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        createFileIfNotExists(ATTRACTIONS_FILE);
        createFileIfNotExists(TREKS_FILE);
        createFileIfNotExists(BOOKINGS_FILE);
        createFileIfNotExists(GUIDES_FILE);
        createFileIfNotExists(EMERGENCIES_FILE);
    }

    private void createFileIfNotExists(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("[]");
                }
            } catch (IOException e) {
                System.err.println("Error creating file " + filename + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ==================== TREK METHODS ====================

    public List<Trek> loadTreks() {
        return loadFromFile(TREKS_FILE, new TypeToken<List<Trek>>(){}.getType());
    }

    public Trek getTrekById(int id) {
        List<Trek> treks = loadTreks();
        return treks.stream()
                .filter(trek -> trek.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Trek> getTreksByGuideEmail(String guideEmail) {
        List<Trek> allTreks = loadTreks();
        return allTreks.stream()
                .filter(trek -> trek.getGuideEmail() != null &&
                        trek.getGuideEmail().equalsIgnoreCase(guideEmail))
                .collect(Collectors.toList());
    }

    public boolean addTrek(Trek trek) {
        try {
            List<Trek> treks = loadTreks();

            // Generate next ID
            int nextId = treks.stream()
                    .mapToInt(Trek::getId)
                    .max()
                    .orElse(0) + 1;

            trek.setId(nextId);
            treks.add(trek);

            return saveToFile(TREKS_FILE, treks);
        } catch (Exception e) {
            System.err.println("Error adding trek: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTrek(Trek updatedTrek) {
        try {
            List<Trek> treks = loadTreks();

            for (int i = 0; i < treks.size(); i++) {
                if (treks.get(i).getId() == updatedTrek.getId()) {
                    treks.set(i, updatedTrek);
                    return saveToFile(TREKS_FILE, treks);
                }
            }
            return false; // Trek not found
        } catch (Exception e) {
            System.err.println("Error updating trek: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTrek(int trekId) {
        try {
            List<Trek> treks = loadTreks();
            boolean removed = treks.removeIf(trek -> trek.getId() == trekId);

            if (removed) {
                return saveToFile(TREKS_FILE, treks);
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting trek: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== ATTRACTION METHODS ====================

    public List<Attraction> loadAttractions() {
        return loadFromFile(ATTRACTIONS_FILE, new TypeToken<List<Attraction>>(){}.getType());
    }

    public Attraction getAttractionById(int id) {
        List<Attraction> attractions = loadAttractions();
        return attractions.stream()
                .filter(attraction -> attraction.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public boolean addAttraction(Attraction attraction) {
        try {
            List<Attraction> attractions = loadAttractions();

            // Generate next ID
            int nextId = attractions.stream()
                    .mapToInt(Attraction::getId)
                    .max()
                    .orElse(0) + 1;

            attraction.setId(nextId);
            attractions.add(attraction);

            return saveToFile(ATTRACTIONS_FILE, attractions);
        } catch (Exception e) {
            System.err.println("Error adding attraction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAttraction(int attractionId) {
        try {
            List<Attraction> attractions = loadAttractions();
            boolean removed = attractions.removeIf(attraction -> attraction.getId() == attractionId);

            if (removed) {
                return saveToFile(ATTRACTIONS_FILE, attractions);
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting attraction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== BOOKING METHODS ====================

    public List<Booking> loadBookings() {
        return loadFromFile(BOOKINGS_FILE, new TypeToken<List<Booking>>(){}.getType());
    }

    public boolean addBooking(Booking booking) {
        try {
            List<Booking> bookings = loadBookings();

            // Generate next ID
            int nextId = bookings.stream()
                    .mapToInt(Booking::getId)
                    .max()
                    .orElse(0) + 1;

            booking.setId(nextId);
            bookings.add(booking);

            return saveToFile(BOOKINGS_FILE, bookings);
        } catch (Exception e) {
            System.err.println("Error adding booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Booking> getBookingsByUserEmail(String userEmail) {
        List<Booking> allBookings = loadBookings();
        return allBookings.stream()
                .filter(booking -> booking.getUserEmail() != null &&
                        booking.getUserEmail().equalsIgnoreCase(userEmail))
                .collect(Collectors.toList());
    }

    public boolean deleteBooking(int bookingId) {
        try {
            List<Booking> bookings = loadBookings();
            boolean removed = bookings.removeIf(booking -> booking.getId() == bookingId);

            if (removed) {
                return saveToFile(BOOKINGS_FILE, bookings);
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== GUIDE METHODS ====================

    public List<Guide> loadGuides() {
        return loadFromFile(GUIDES_FILE, new TypeToken<List<Guide>>(){}.getType());
    }

    public Guide getGuideByEmail(String email) {
        List<Guide> guides = loadGuides();
        return guides.stream()
                .filter(guide -> guide.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    // ==================== EMERGENCY METHODS ====================

    public List<Emergency> loadEmergencies() {
        return loadFromFile(EMERGENCIES_FILE, new TypeToken<List<Emergency>>(){}.getType());
    }

    public boolean addEmergency(Emergency emergency) {
        try {
            List<Emergency> emergencies = loadEmergencies();

            // Generate next ID
            int nextId = emergencies.stream()
                    .mapToInt(Emergency::getId)
                    .max()
                    .orElse(0) + 1;

            emergency.setId(nextId);
            emergencies.add(emergency);

            return saveToFile(EMERGENCIES_FILE, emergencies);
        } catch (Exception e) {
            System.err.println("Error adding emergency: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEmergency(Emergency updatedEmergency) {
        try {
            List<Emergency> emergencies = loadEmergencies();

            for (int i = 0; i < emergencies.size(); i++) {
                if (emergencies.get(i).getId() == updatedEmergency.getId()) {
                    emergencies.set(i, updatedEmergency);
                    return saveToFile(EMERGENCIES_FILE, emergencies);
                }
            }
            return false; // Emergency not found
        } catch (Exception e) {
            System.err.println("Error updating emergency: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Emergency> getEmergenciesByGuideEmail(String guideEmail) {
        List<Emergency> allEmergencies = loadEmergencies();
        return allEmergencies.stream()
                .filter(emergency -> emergency.getGuideEmail() != null &&
                        emergency.getGuideEmail().equalsIgnoreCase(guideEmail))
                .sorted((e1, e2) -> e2.getReportedAt().compareTo(e1.getReportedAt())) // Most recent first
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private <T> List<T> loadFromFile(String filename, Type type) {
        try (FileReader reader = new FileReader(filename)) {
            List<T> items = gson.fromJson(reader, type);
            return items != null ? items : new ArrayList<>();
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename + ". Creating new file.");
            createFileIfNotExists(filename);
            return new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error parsing JSON from " + filename + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private <T> boolean saveToFile(String filename, List<T> items) {
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(items, writer);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to file " + filename + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
