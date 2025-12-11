package entite;

// Main product class
public class User {
    private final String name; // required
    private final String email; // required
    private String phone; // optional
    private String city; // optional
    
    public static class UserBuilder { // inner class
        private String name;
        private String email;
        private String phone;
        private String city;
        public UserBuilder(String name, String email) {
            this.name = name;
            this.email = email;
        }
        public UserBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }
        public UserBuilder city(String city) {
            this.city = city;
            return this;
        }
        public User build() {
            return new User(this);
        }
    } //end innerclass
    
    
    
    private User(UserBuilder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.phone = builder.phone;
        this.city = builder.city;
    }
    
    
    @Override
    public String toString() {
        return "User{" +
               "name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", phone='" + phone + '\'' +
               ", city='" + city + '\'' +
               '}';
    }

//Using the Builder:
//Here’s how we can use the Builder to create different user profiles:

//public class Main {
    public static void main(String[] args) {
        User user1 = new User.UserBuilder("John Doe", "john.doe@example.com")
                            .phone("1234567890")
                            .city("New York")
                            .build();
        User user2 = new User.UserBuilder("Jane Roe", "jane.roe@example.com")
                            .build();
        
        System.out.println(user1);
        System.out.println(user2);
    }
}