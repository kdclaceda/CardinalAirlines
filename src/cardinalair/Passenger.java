package cardinalair;

public class Passenger {
    private String passengerID;
    private String name;
    private String email;
    private String phoneNum;

    public Passenger(String passengerID, String name, String email, String phoneNum) {
        this.passengerID = passengerID;
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
    }

    public void updateDetails(String passengerID, String name, String email, String phoneNum) {
        this.passengerID = passengerID;
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
    }

    public String getPassengerID() { return passengerID; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhoneNum() { return phoneNum; }
}