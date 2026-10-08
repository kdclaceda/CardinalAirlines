package cardinalair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    // Database connection string
    private static final String DB_URL = "jdbc:mysql://localhost:3306/cardinal_airlines?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Hel1kopter"; // Update to your MySQL root password if needed PASSWORD KO Hel1kopter 

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Load all flights from the MySQL Flight table
    public static List<Flight> loadFlights() {
        List<Flight> flightList = new ArrayList<>();
        String sql = "SELECT flight_num, origin, destination, dep_time, arr_time FROM Flight";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String flightNum = rs.getString("flight_num");
                String origin = rs.getString("origin");
                String destination = rs.getString("destination");
                LocalDateTime depTime = rs.getTimestamp("dep_time").toLocalDateTime();
                LocalDateTime arrTime = rs.getTimestamp("arr_time").toLocalDateTime();

                Flight flight = new Flight(flightNum, origin, destination, depTime, arrTime);
                flightList.add(flight);
            }
            System.out.println("Loaded " + flightList.size() + " flights from MySQL database!");
        } catch (SQLException e) {
            System.err.println("Error loading flights from MySQL: " + e.getMessage());
            e.printStackTrace();
        }

        return flightList;
    }

    // Insert Passenger, Booking, and Payment into MySQL
    public static void saveBooking(Booking booking) {
        String sqlPassenger = "INSERT INTO Passenger (passenger_id, name, email, phone_num) " +
                              "VALUES (?, ?, ?, ?) " +
                              "ON DUPLICATE KEY UPDATE name = VALUES(name), email = VALUES(email), phone_num = VALUES(phone_num)";

        String sqlBooking = "INSERT INTO Booking (booking_id, flight_num, seat_num, passenger_id, amount_paid, is_cancelled) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";

        String sqlPayment = "INSERT INTO Payment (payment_id, booking_id, amount, is_processed) " +
                            "VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            // 1. Insert Passenger
            try (PreparedStatement psP = conn.prepareStatement(sqlPassenger)) {
                psP.setString(1, booking.getPassenger().getPassengerID());
                psP.setString(2, booking.getPassenger().getName());
                psP.setString(3, booking.getPassenger().getEmail());
                psP.setString(4, booking.getPassenger().getPhoneNum());
                psP.executeUpdate();
            }

            // 2. Insert Booking
            try (PreparedStatement psB = conn.prepareStatement(sqlBooking)) {
                psB.setString(1, booking.getBookingId());
                psB.setString(2, booking.getFlight().getFlightNum());
                psB.setString(3, booking.getSeat().getSeatNum());
                psB.setString(4, booking.getPassenger().getPassengerID());
                psB.setDouble(5, booking.getAmountPaid());
                psB.setBoolean(6, booking.isCancelled());
                psB.executeUpdate();
            }

            // 3. Insert Payment
            try (PreparedStatement psPay = conn.prepareStatement(sqlPayment)) {
                psPay.setString(1, "PAY-" + booking.getBookingId());
                psPay.setString(2, booking.getBookingId());
                psPay.setDouble(3, booking.getAmountPaid());
                psPay.setBoolean(4, true);
                psPay.executeUpdate();
            }

            System.out.println("Booking successfully saved to MySQL database!");
        } catch (SQLException e) {
            System.err.println("MySQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update the booking status to cancelled
    public static void cancelBooking(String bookingId) {
        String sql = "UPDATE Booking SET is_cancelled = TRUE WHERE booking_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId);
            ps.executeUpdate();
            System.out.println("Booking marked as cancelled in MySQL!");
        } catch (SQLException e) {
            System.err.println("MySQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}