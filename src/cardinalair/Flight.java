package cardinalair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Flight {
    private String flightNum;
    private String origin;
    private String destination;
    private LocalDateTime depTime;
    private LocalDateTime arrTime;
    private List<Seat> seats;

    public Flight(String flightNum, String origin, String destination, LocalDateTime depTime, LocalDateTime arrTime) {
        this.flightNum = flightNum;
        this.origin = origin;
        this.destination = destination;
        this.depTime = depTime;
        this.arrTime = arrTime;
        this.seats = new ArrayList<>();
    }

    public void addSeat(Seat seat) {
        seats.add(seat);
    }

    public List<Seat> getAvailSeats() {
        List<Seat> available = new ArrayList<>();
        for (Seat s : seats) {
            if (s.getStatus() == SeatStatus.AVAILABLE) {
                available.add(s);
            }
        }
        return available;
    }

    public Seat findSeatNum(String seatNum) {
        for (Seat s : seats) {
            if (s.getSeatNum().equalsIgnoreCase(seatNum)) {
                return s;
            }
        }
        return null;
    }

    public String getFlightNum() { return flightNum; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepTime() { return depTime; }
    public LocalDateTime getArrTime() { return arrTime; }
    public List<Seat> getSeats() { return seats; }
}