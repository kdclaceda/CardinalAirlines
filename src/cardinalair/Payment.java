package cardinalair;

public class Payment {
    private String paymentId;
    private double amount;
    private boolean isProcessed;

   
    public Payment(String paymentId, double amount) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.isProcessed = false;
    }

    public boolean processPayment() {
        this.isProcessed = true;
        return true;
    }

    public String getPaymentId() { return paymentId; }
    public double getAmount() { return amount; }
    public boolean isProcessed() { return isProcessed; }
}