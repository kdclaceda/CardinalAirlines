package cardinalair;

public class Booking {
    private String bookingId;
    private Flight flight;
    private Seat seat;
    private Passenger passenger;
    private double amountPaid;
    private boolean cancelled;

    public Booking(String bookingId, Flight flight, Seat seat, Passenger passenger, double amountPaid) {
        this.bookingId = bookingId;
        this.flight = flight;
        this.seat = seat;
        this.passenger = passenger;
        this.amountPaid = amountPaid;
        this.cancelled = false;
    }

    public String getBookingId() { return bookingId; }
    public Flight getFlight() { return flight; }
    public Seat getSeat() { return seat; }
    public Passenger getPassenger() { return passenger; }
    public double getAmountPaid() { return amountPaid; }
    public boolean isCancelled() { return cancelled; }

    public void cancelBooking() {
        this.cancelled = true;
        if (seat != null) {
            seat.unreserveSeat();
        }
    }
}