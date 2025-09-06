package Models;

public class Admin extends User {

    public Admin() {
        super();
        setUserType("admin");
    }

    public Admin(String firstName, String lastName, String email, String phone, String password, String nationality) {
        super(firstName, lastName, email, phone, password, nationality);
        setUserType("admin");
    }
}
