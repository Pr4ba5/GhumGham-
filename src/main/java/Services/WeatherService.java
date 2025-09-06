package Services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class WeatherService {

    // Free API key - you should get your own from openweathermap.org
    private static final String API_KEY = "your_api_key_here"; // Replace with your actual API key
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather";

    public static class WeatherData {
        private String location;
        private String description;
        private double temperature;
        private int humidity;
        private double windSpeed;
        private String weatherIcon;
        private String mainWeather;

        // Constructors
        public WeatherData() {}

        public WeatherData(String location, String description, double temperature,
                           int humidity, double windSpeed, String weatherIcon, String mainWeather) {
            this.location = location;
            this.description = description;
            this.temperature = temperature;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.weatherIcon = weatherIcon;
            this.mainWeather = mainWeather;
        }

        // Getters and setters
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }

        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }

        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

        public String getWeatherIcon() { return weatherIcon; }
        public void setWeatherIcon(String weatherIcon) { this.weatherIcon = weatherIcon; }

        public String getMainWeather() { return mainWeather; }
        public void setMainWeather(String mainWeather) { this.mainWeather = mainWeather; }
    }

    public static CompletableFuture<WeatherData> getCurrentWeather(String city) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // If no API key is provided, return mock data
                if (API_KEY.equals("your_api_key_here")) {
                    return getMockWeatherData(city);
                }

                String urlString = String.format("%s?q=%s&appid=%s&units=metric",
                        BASE_URL, city, API_KEY);

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    return parseWeatherData(response.toString(), city);
                } else {
                    System.err.println("Weather API Error: " + responseCode);
                    return getMockWeatherData(city);
                }

            } catch (Exception e) {
                System.err.println("Error fetching weather data: " + e.getMessage());
                return getMockWeatherData(city);
            }
        });
    }

    private static WeatherData parseWeatherData(String jsonResponse, String city) {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

            WeatherData weatherData = new WeatherData();
            weatherData.setLocation(city);

            // Parse main weather data
            JsonObject main = jsonObject.getAsJsonObject("main");
            weatherData.setTemperature(main.get("temp").getAsDouble());
            weatherData.setHumidity(main.get("humidity").getAsInt());

            // Parse weather description
            JsonObject weather = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject();
            weatherData.setDescription(capitalizeWords(weather.get("description").getAsString()));
            weatherData.setMainWeather(weather.get("main").getAsString());

            // Parse wind data
            if (jsonObject.has("wind")) {
                JsonObject wind = jsonObject.getAsJsonObject("wind");
                weatherData.setWindSpeed(wind.get("speed").getAsDouble() * 3.6); // Convert m/s to km/h
            }

            // Set weather icon based on main weather condition
            weatherData.setWeatherIcon(getWeatherEmoji(weatherData.getMainWeather()));

            return weatherData;

        } catch (Exception e) {
            System.err.println("Error parsing weather data: " + e.getMessage());
            return getMockWeatherData(city);
        }
    }

    private static WeatherData getMockWeatherData(String city) {
        // Return realistic mock data for Nepal/Kathmandu
        return new WeatherData(
                city,
                "Partly Cloudy",
                22.5,
                65,
                8.5,
                "⛅",
                "Clouds"
        );
    }

    private static String getWeatherEmoji(String mainWeather) {
        return switch (mainWeather.toLowerCase()) {
            case "clear" -> "☀️";
            case "clouds" -> "⛅";
            case "rain" -> "🌧️";
            case "drizzle" -> "🌦️";
            case "thunderstorm" -> "⛈️";
            case "snow" -> "❄️";
            case "mist", "fog" -> "🌫️";
            case "haze" -> "🌫️";
            default -> "🌤️";
        };
    }

    private static String capitalizeWords(String str) {
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }
}
