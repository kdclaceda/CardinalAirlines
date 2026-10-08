package cardinalair;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

//ETO YUNG PARANG UI NI PAYMRNY
public class PaymentDialog extends JDialog {

    private final double originalPrice;
    private double finalPrice;
    private boolean isPwdSelected = false;
    private String voucherCodeUsed = null;
    private String cardNumber = "";
    private boolean paymentSuccessful = false;
    private final Connection conn;

    // Blank Form Controls
    private final JCheckBox chkPwd = new JCheckBox();
    private final JTextField txtVoucher = new JTextField();
    private final JTextField txtCard = new JTextField();
    private final JLabel lblTotalVal = new JLabel();

    public PaymentDialog(Frame owner, double originalPrice, Connection conn) {
        super(owner, "Payment & Discount Options", true);
        this.originalPrice = originalPrice;
        this.finalPrice = originalPrice;
        this.conn = conn;

        initComponents();
    }

    private void initComponents() {
        setSize(440, 340);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblOriginal = new JLabel("Base Fare:");
        JLabel lblOriginalVal = new JLabel(String.format("PHP %.2f", originalPrice));

        JLabel lblPwd = new JLabel("PWD / Senior (20%):");
        JLabel lblVoucher = new JLabel("Voucher Code:");

        JLabel lblTotal = new JLabel("Final Amount Due:");
        lblTotalVal.setText(String.format("PHP %.2f", originalPrice));
        lblTotalVal.setFont(lblTotalVal.getFont().deriveFont(Font.BOLD, 14f));

        JLabel lblCard = new JLabel("Card Number:");

        form.add(lblOriginal); form.add(lblOriginalVal);
        form.add(lblPwd);      form.add(chkPwd);
        form.add(lblVoucher);  form.add(txtVoucher);
        form.add(lblTotal);    form.add(lblTotalVal);
        form.add(lblCard);     form.add(txtCard);

        // Recalculates final price whenever PWD is toggled or Voucher is applied
        Runnable recalculate = () -> {
            double current = originalPrice;

            if (chkPwd.isSelected()) {
                current *= 0.80; // 20% discount
            }

            String code = txtVoucher.getText().trim();
            if (!code.isEmpty()) {
                double discountPct = fetchVoucherDiscount(code);
                if (discountPct > 0) {
                    current *= (1.0 - (discountPct / 100.0));
                    voucherCodeUsed = code;
                } else {
                    voucherCodeUsed = null;
                }
            } else {
                voucherCodeUsed = null;
            }

            finalPrice = current;
            lblTotalVal.setText(String.format("PHP %.2f", finalPrice));
        };

        chkPwd.addActionListener(e -> recalculate.run());
        txtVoucher.addActionListener(e -> recalculate.run());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPay = new JButton("Confirm Payment");
        JButton btnCancel = new JButton("Cancel");
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnPay);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());

        btnPay.addActionListener(e -> {
            recalculate.run();

            String card = txtCard.getText().trim();
            if (card.isEmpty() || card.length() < 12) {
                JOptionPane.showMessageDialog(this, "Please enter a valid card number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            cardNumber = card;
            isPwdSelected = chkPwd.isSelected();
            paymentSuccessful = true;
            dispose();
        });
    }

    private double fetchVoucherDiscount(String code) {
        String sql = "SELECT discount_percent FROM voucher WHERE code = ? AND is_active = 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("discount_percent");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean isPaymentSuccessful() { return paymentSuccessful; }
    public double getFinalPrice() { return finalPrice; }
    public boolean isPwd() { return isPwdSelected; }
    public String getVoucherCode() { return voucherCodeUsed; }
    public String getCardNumber() { return cardNumber; }
}