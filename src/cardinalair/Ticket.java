package cardinalair;

public class Ticket {
    private String ticketID;
    private Booking booking;

    public Ticket(String ticketID, Booking booking) {
        this.ticketID = ticketID;
        this.booking = booking;
    }

    public String generateTicketSummary() {
        return "==========================================\n" +
               "           CARDINAL AIRLINES TICKET        \n" +
               "==========================================\n" +
               "Ticket ID    : " + ticketID + "\n" +
               "Booking Ref  : " + booking.getBookingId() + "\n" +
               "Passenger    : " + booking.getPassenger().getName() + "\n" +
               "Flight       : " + booking.getFlight().getFlightNum() + "\n" +
               "Route        : " + booking.getFlight().getOrigin() + " -> " + booking.getFlight().getDestination() + "\n" +
               "Departure    : " + booking.getFlight().getDepTime() + "\n" +
               "Seat Number  : " + booking.getSeat().getSeatNum() + " (" + booking.getSeat().getSeatClass() + ")\n" +
               "Amount Paid  : PHP " + booking.getAmountPaid() + "\n" +
               "==========================================";
    }

    public String getTicketID() { return ticketID; }
    public Booking getBooking() { return booking; }
}