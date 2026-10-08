package cardinalair;

public class Seat {
    private String seatNum;
    private SeatClass seatClass;
    private SeatStatus status;

    public Seat(String seatNum, SeatClass seatClass) {
        this.seatNum = seatNum;
        this.seatClass = seatClass;
        this.status = SeatStatus.AVAILABLE;
    }

    public String getSeatNum() { return seatNum; }
    public SeatClass getSeatClass() { return seatClass; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    public boolean isReserved() {
        return this.status == SeatStatus.RESERVED;
    }

    public void reserveSeat() {
        this.status = SeatStatus.RESERVED;
    }

    public void confirmSeat() {
        this.status = SeatStatus.RESERVED;
    }

    public void unreserveSeat() {
        this.status = SeatStatus.AVAILABLE;
    }
}