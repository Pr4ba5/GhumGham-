package Models;

public class Guide extends User {
    private String proficiencyLanguage;
    private String experience;

    public Guide() {
        super();
        setUserType("guide");
    }

    public Guide(String firstName, String lastName, String email, String phone,
                 String password, String nationality, String proficiencyLanguage, String experience) {
        super(firstName, lastName, email, phone, password, nationality);
        setUserType("guide");
        this.proficiencyLanguage = proficiencyLanguage;
        this.experience = experience;
    }

    // Getters and Setters
    public String getProficiencyLanguage() { return proficiencyLanguage; }

    public String getExperience() { return experience; }
}
