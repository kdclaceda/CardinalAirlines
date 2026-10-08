package cardinalair;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookingManager {
    private List<Flight> flights;
    private List<Booking> bookings;

    public BookingManager() {
        this.bookings = new ArrayList<>();
        // Load flights from MySQL instead of hardcoding
        loadFlightsFromDatabase();
    }

    // LOAD FLIGHTS FROM MYSQL
    private void loadFlightsFromDatabase() {
        this.flights = DatabaseHelper.loadFlights();

        // Generate seat grid for each flight retrieved from MySQL
        for (Flight f : this.flights) {
            generateAirplaneSeats(f);
        }
    }
     
    // ALL OF THE SEATS, EACH DIVIDED BY CLASS, AND IF IT IS ALREADY RESERVED
    private void generateAirplaneSeats(Flight flight) {
        String[] cols = {"A", "B", "C", "D", "E", "F"};
        Random rand = new Random();

        // 9 Rows * 6 Seats = 54 total seats per flight
        for (int row = 1; row <= 9; row++) {
            SeatClass sClass;
            if (row <= 2) {
                sClass = SeatClass.FIRST;       // Rows 1-2
            } else if (row <= 4) {
                sClass = SeatClass.BUSINESS;    // Rows 3-4
            } else {
                sClass = SeatClass.ECONOMY;     // Rows 5-9
            }

            for (String col : cols) {
                Seat seat = new Seat(row + col, sClass);
                
                // Randomly pre-book ~30% of the plane to make it realistic
                if (rand.nextDouble() < 0.30) {
                    seat.setStatus(SeatStatus.RESERVED);
                }
                
                flight.addSeat(seat);
            }
        }
    }

    public double getTicketPrice(SeatClass seatClass) {
        switch (seatClass) {
            case FIRST: return 45000.00;
            case BUSINESS: return 28000.00;
            case ECONOMY: return 12500.00;
            default: return 12500.00;
        }
    }

    public void addFlight(Flight flight) { flights.add(flight); }

    public List<Flight> searchFlights(String origin, String destination, LocalDate date) {
        List<Flight> results = new ArrayList<>();
        for (Flight f : flights) {
            boolean matchesOrigin = origin.isEmpty() || f.getOrigin().toLowerCase().contains(origin.toLowerCase());
            boolean matchesDest = destination.isEmpty() || f.getDestination().toLowerCase().contains(destination.toLowerCase());
            boolean matchesDate = (date == null) || f.getDepTime().toLocalDate().equals(date);
            
            if (matchesOrigin && matchesDest && matchesDate) {
                results.add(f);
            }
        }
        return results;
    }

    public Booking makeReservation(Flight flight, Seat seat, Passenger passenger, double amount) {
        String bookingId = "BK" + (bookings.size() + 100);
        Booking booking = new Booking(bookingId, flight, seat, passenger, amount);
        seat.reserveSeat();
        seat.confirmSeat();
        bookings.add(booking);

        // Save to MySQL database via DatabaseHelper
        DatabaseHelper.saveBooking(booking);

        return booking;
    }

    public Booking findBooking(String bookingId) {
        for (Booking b : bookings) {
            if (b.getBookingId().equalsIgnoreCase(bookingId) && !b.isCancelled()) {
                return b;
            }
        }
        return null;
    }

    public boolean cancelReservation(String bookingId) {
        Booking b = findBooking(bookingId);
        if (b != null) {
            b.cancelBooking();

            // Update cancellation in MySQL database via DatabaseHelper
            DatabaseHelper.cancelBooking(bookingId);

            return true;
        }
        return false;
    }

    public List<Flight> getFlights() { return flights; }
    public List<Booking> getBookings() { return bookings; }
}