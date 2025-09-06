package Models;

public class Attraction {
    private int id;
    private String name;
    private String location;
    private String difficulty;
    private String type;
    private String remarks;

    public Attraction() {}

    public Attraction(String name, String location, String difficulty, String type, String remarks) {
        this.name = name;
        this.location = location;
        this.difficulty = difficulty;
        this.type = type;
        this.remarks = remarks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    @Override
    public String toString() {
        return "Attraction{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", location='" + location + '\'' +
                ", difficulty='" + difficulty +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}
