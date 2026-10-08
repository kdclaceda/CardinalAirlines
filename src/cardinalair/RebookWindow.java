package cardinalair;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RebookWindow extends JFrame {

    private final Connection conn;

    private final JTextField txtSearchBookingId = new JTextField(15);
    private final JButton btnSearch = new JButton("Search Booking");

    private final JLabel lblPassengerVal = new JLabel("-");
    private final JLabel lblCurrentFlightVal = new JLabel("-");
    private final JLabel lblCurrentSeatVal = new JLabel("-");
    private final JLabel lblRebookCountVal = new JLabel("-");

    private final JTextField txtNewFlightNum = new JTextField();
    private final JTextField txtNewSeatNum = new JTextField();
    private final JButton btnProceedPayment = new JButton("Proceed to Payment");

    private String currentBookingId = null;
    private String currentPassengerId = null;
    private String currentFlightNum = null;
    private String currentSeatNum = null;
    private int currentRebookCount = 0;

    public RebookWindow(Connection conn) {
        this.conn = conn;

        setTitle("Cardinal Airlines - Flight Rebooking");
        setSize(500, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initComponents();
    }

    private void initComponents() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Find Booking"));
        searchPanel.add(new JLabel("Booking ID:"));
        searchPanel.add(txtSearchBookingId);
        searchPanel.add(btnSearch);

        JPanel centerPanel = new JPanel(new GridLayout(7, 2, 8, 8));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Rebooking Details"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        centerPanel.add(new JLabel("Passenger Name:"));
        centerPanel.add(lblPassengerVal);

        centerPanel.add(new JLabel("Current Flight:"));
        centerPanel.add(lblCurrentFlightVal);

        centerPanel.add(new JLabel("Current Seat:"));
        centerPanel.add(lblCurrentSeatVal);

        centerPanel.add(new JLabel("Previous Rebooks:"));
        centerPanel.add(lblRebookCountVal);

        centerPanel.add(new JLabel("New Flight Number:"));
        centerPanel.add(txtNewFlightNum);

        centerPanel.add(new JLabel("New Seat Number:"));
        centerPanel.add(txtNewSeatNum);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnProceedPayment.setEnabled(false);
        bottomPanel.add(btnProceedPayment);

        add(searchPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> fetchBookingDetails());
        btnProceedPayment.addActionListener(e -> processRebooking());
    }

    private void fetchBookingDetails() {
        String bookingIdInput = txtSearchBookingId.getText().trim();

        if (bookingIdInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Booking ID.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT b.booking_id, b.flight_num, b.seat_num, b.rebook_count, b.passenger_id, "
                   + "p.name "
                   + "FROM booking b "
                   + "JOIN passenger p ON b.passenger_id = p.passenger_id "
                   + "WHERE b.booking_id = ? AND b.is_cancelled = 0";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookingIdInput);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    currentBookingId = rs.getString("booking_id");
                    currentPassengerId = rs.getString("passenger_id");
                    currentFlightNum = rs.getString("flight_num");
                    currentSeatNum = rs.getString("seat_num");
                    currentRebookCount = rs.getInt("rebook_count");

                    lblPassengerVal.setText(rs.getString("name"));
                    lblCurrentFlightVal.setText(currentFlightNum);
                    lblCurrentSeatVal.setText(currentSeatNum);
                    lblRebookCountVal.setText(String.valueOf(currentRebookCount));

                    btnProceedPayment.setEnabled(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Booking ID not found or is cancelled.", "Search Result", JOptionPane.ERROR_MESSAGE);
                    resetDetails();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processRebooking() {
        String newFlightNum = txtNewFlightNum.getText().trim();
        String newSeatNum = txtNewSeatNum.getText().trim();

        if (newFlightNum.isEmpty() || newSeatNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both the new Flight Number and Seat Number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Verify flight exists & fetch base price
        double baseFlightPrice = fetchFlightPrice(newFlightNum);
        if (baseFlightPrice <= 0) {
            JOptionPane.showMessageDialog(this, "Target flight number not found or has invalid price.", "Flight Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Check if the target seat is already occupied on that flight in the booking table
        if (isSeatOccupied(newFlightNum, newSeatNum)) {
            JOptionPane.showMessageDialog(this, "Seat " + newSeatNum + " is already occupied on Flight " + newFlightNum + ".", "Seat Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Calculate penalty fare (15% per previous rebook + 1)
        double priceWithPenalty = baseFlightPrice * (1.0 + ((currentRebookCount + 1) * 0.15));

        PaymentDialog paymentDialog = new PaymentDialog(this, priceWithPenalty, conn);
        paymentDialog.setVisible(true);

        if (!paymentDialog.isPaymentSuccessful()) {
            JOptionPane.showMessageDialog(this, "Rebooking cancelled. No changes were saved.");
            return;
        }

        String updateBookingSql = "UPDATE booking SET flight_num = ?, seat_num = ?, amount_paid = ?, "
                                + "rebook_count = rebook_count + 1, is_pwd = ?, voucher_code = ? "
                                + "WHERE booking_id = ?";

        String insertPaymentSql = "INSERT INTO payment (payment_id, booking_id, amount, card_number, is_processed) "
                                + "VALUES (?, ?, ?, ?, 1)";

        String insertAdminLogSql = "INSERT INTO admin_log (action_type, description) VALUES ('REBOOK', ?)";

        try {
            conn.setAutoCommit(false);

            // Update booking table
            try (PreparedStatement pstmtBooking = conn.prepareStatement(updateBookingSql)) {
                pstmtBooking.setString(1, newFlightNum);
                pstmtBooking.setString(2, newSeatNum);
                pstmtBooking.setDouble(3, paymentDialog.getFinalPrice());
                pstmtBooking.setInt(4, paymentDialog.isPwd() ? 1 : 0);
                pstmtBooking.setString(5, paymentDialog.getVoucherCode());
                pstmtBooking.setString(6, currentBookingId);
                pstmtBooking.executeUpdate();
            }

            // Record payment transaction
            try (PreparedStatement pstmtPayment = conn.prepareStatement(insertPaymentSql)) {
                String paymentId = "PAY-" + currentBookingId + "-R" + (currentRebookCount + 1);
                pstmtPayment.setString(1, paymentId);
                pstmtPayment.setString(2, currentBookingId);
                pstmtPayment.setDouble(3, paymentDialog.getFinalPrice());
                pstmtPayment.setString(4, paymentDialog.getCardNumber());
                pstmtPayment.executeUpdate();
            }

            // Write entry to admin_log table
            try (PreparedStatement pstmtLog = conn.prepareStatement(insertAdminLogSql)) {
                String logDesc = "Rebooked Booking " + currentBookingId + " from Flight " 
                               + currentFlightNum + " (" + currentSeatNum + ") to Flight " 
                               + newFlightNum + " (" + newSeatNum + ")";
                pstmtLog.setString(1, logDesc);
                pstmtLog.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Rebooking successful! Ticket updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            resetDetails();

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Transaction failed: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private double fetchFlightPrice(String flightNum) {
        String sql = "SELECT price FROM flight WHERE flight_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, flightNum);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("price");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private boolean isSeatOccupied(String flightNum, String seatNum) {
        String sql = "SELECT COUNT(*) FROM booking WHERE flight_num = ? AND seat_num = ? AND is_cancelled = 0";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, flightNum);
            pstmt.setString(2, seatNum);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void resetDetails() {
        currentBookingId = null;
        currentPassengerId = null;
        currentFlightNum = null;
        currentSeatNum = null;
        currentRebookCount = 0;
        lblPassengerVal.setText("-");
        lblCurrentFlightVal.setText("-");
        lblCurrentSeatVal.setText("-");
        lblRebookCountVal.setText("-");
        txtSearchBookingId.setText("");
        txtNewFlightNum.setText("");
        txtNewSeatNum.setText("");
        btnProceedPayment.setEnabled(false);
    }
}