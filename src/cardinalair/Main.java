package cardinalair;

import java.sql.Connection;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Establish the database connection
                Connection conn = DatabaseHelper.getConnection();
                
                if (conn == null) {
                    JOptionPane.showMessageDialog(null, "Could not connect to database.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 2. Pass the connection into the GUI constructor
                CardinalAirGUI mainApp = new CardinalAirGUI(conn);
                mainApp.setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Startup Error: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}