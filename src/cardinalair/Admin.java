package cardinalair;

public class Admin {
    private String username;
    private String password;

    public Admin() {
        this.username = "admin12345";
        this.password = "12345";
    }

    public boolean login(String user, String pass) {
        return this.username.equals(user) && this.password.equals(pass);
    }

    public String generateReport(int totalBookings, int totalFlights) {
        return "=== SYSTEM AUDIT REPORT ===\n" +
               "Total Active Flights: " + totalFlights + "\n" +
               "Total Active Bookings: " + totalBookings + "\n" +
               "System Status: Operational";
    }
}