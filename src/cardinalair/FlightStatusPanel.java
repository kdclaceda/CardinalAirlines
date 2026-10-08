package cardinalair;

import javax.swing.*;
import java.awt.*;

public class FlightStatusPanel {

    public static JPanel createPanel() {

        JPanel mainPanel =
            new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(
            BorderFactory.createEmptyBorder(
                15, 15, 15, 15
            )
        );

        // ========================================================
        // Header
        // ========================================================

        JPanel headerPanel =
            new JPanel();

        headerPanel.setLayout(
            new BoxLayout(
                headerPanel,
                BoxLayout.Y_AXIS
            )
        );

        JLabel title =
            new JLabel("Flight Status");

        title.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                24
            )
        );

        JLabel subtitle =
            new JLabel(
                "Flight information and operational notices"
            );

        subtitle.setFont(
            new Font(
                "SansSerif",
                Font.PLAIN,
                13
            )
        );

        headerPanel.add(title);

        headerPanel.add(
            Box.createRigidArea(
                new Dimension(0, 5)
            )
        );

        headerPanel.add(subtitle);

        mainPanel.add(
            headerPanel,
            BorderLayout.NORTH
        );

        // ========================================================
        // Flight Status List
        // ========================================================

        JPanel flightListPanel =
            new JPanel();

        flightListPanel.setLayout(
            new BoxLayout(
                flightListPanel,
                BoxLayout.Y_AXIS
            )
        );

        flightListPanel.setBorder(
            BorderFactory.createEmptyBorder(
                10, 0, 10, 0
            )
        );

        // --------------------------------------------------------
        // HARD-CODED DEMO FLIGHT #1
        // --------------------------------------------------------

        addFlightCard(
            flightListPanel,
            "PR-801",
            "Manila, Philippines",
            "Kyoto, Japan",
            "DELAYED",
            "45 minutes",
            "Estimated departure: 8:45 AM"
        );

        // --------------------------------------------------------
        // HARD-CODED DEMO FLIGHT #2
        // --------------------------------------------------------

        addFlightCard(
            flightListPanel,
            "BA-112",
            "Manila, Philippines",
            "Davao, Philippines",
            "ON TIME",
            "On schedule",
            "No operational issues reported"
        );

        // --------------------------------------------------------
        // HARD-CODED DEMO FLIGHT #3
        // --------------------------------------------------------

        addFlightCard(
            flightListPanel,
            "QF-505",
            "Manila, Philippines",
            "Los Angeles, United States",
            "CANCELLED",
            "Flight cancelled",
            "Please contact the airline for further assistance"
        );

        // --------------------------------------------------------
        // HARD-CODED DEMO FLIGHT #4
        // --------------------------------------------------------

        addFlightCard(
            flightListPanel,
            "SQ-202",
            "Manila, Philippines",
            "Seoul, South Korea",
            "DELAYED",
            "2 hours",
            "Aircraft servicing delay"
        );

        JScrollPane scrollPane =
            new JScrollPane(
                flightListPanel
            );

        scrollPane.setBorder(
            BorderFactory.createEmptyBorder()
        );

        mainPanel.add(
            scrollPane,
            BorderLayout.CENTER
        );

        // ========================================================
        // Demo Notice
        // ========================================================

        JLabel demoLabel =
            new JLabel(
                "DEMO ONLY — These flight statuses are hardcoded "
                + "sample data and are not connected to the database."
            );

        demoLabel.setHorizontalAlignment(
            SwingConstants.CENTER
        );

        demoLabel.setFont(
            new Font(
                "SansSerif",
                Font.ITALIC,
                11
            )
        );

        mainPanel.add(
            demoLabel,
            BorderLayout.SOUTH
        );

        return mainPanel;
    }

    // ============================================================
    // Creates one flight status card
    // ============================================================

    private static void addFlightCard(
        JPanel parent,
        String flightNumber,
        String origin,
        String destination,
        String status,
        String timing,
        String message
    ) {

        JPanel card =
            new JPanel(
                new BorderLayout(
                    20,
                    5
                )
            );

        card.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    Color.LIGHT_GRAY
                ),
                BorderFactory.createEmptyBorder(
                    15,
                    15,
                    15,
                    15
                )
            )
        );

        card.setBackground(
            Color.WHITE
        );

        card.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        // --------------------------------------------------------
        // Left section
        // --------------------------------------------------------

        JPanel informationPanel =
            new JPanel();

        informationPanel.setLayout(
            new BoxLayout(
                informationPanel,
                BoxLayout.Y_AXIS
            )
        );

        informationPanel.setOpaque(false);

        JLabel flightLabel =
            new JLabel(
                flightNumber
            );

        flightLabel.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                20
            )
        );

        JLabel routeLabel =
            new JLabel(
                origin
                    + "  →  "
                    + destination
            );

        routeLabel.setFont(
            new Font(
                "SansSerif",
                Font.PLAIN,
                14
            )
        );

        JLabel messageLabel =
            new JLabel(
                message
            );

        messageLabel.setFont(
            new Font(
                "SansSerif",
                Font.PLAIN,
                12
            )
        );

        informationPanel.add(
            flightLabel
        );

        informationPanel.add(
            Box.createRigidArea(
                new Dimension(0, 5)
            )
        );

        informationPanel.add(
            routeLabel
        );

        informationPanel.add(
            Box.createRigidArea(
                new Dimension(0, 7)
            )
        );

        informationPanel.add(
            messageLabel
        );

        // --------------------------------------------------------
        // Right section
        // --------------------------------------------------------

        JPanel statusPanel =
            new JPanel();

        statusPanel.setLayout(
            new BoxLayout(
                statusPanel,
                BoxLayout.Y_AXIS
            )
        );

        statusPanel.setOpaque(false);

        JLabel statusLabel =
            new JLabel(
                status
            );

        statusLabel.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                12
            )
        );

        statusLabel.setHorizontalAlignment(
            SwingConstants.CENTER
        );

        statusLabel.setAlignmentX(
            Component.CENTER_ALIGNMENT
        );

        statusLabel.setOpaque(true);

        statusLabel.setBorder(
            BorderFactory.createEmptyBorder(
                8,
                18,
                8,
                18
            )
        );

        // --------------------------------------------------------
        // Status appearance
        // --------------------------------------------------------

        if (status.equals("DELAYED")) {

            statusLabel.setBackground(
                new Color(
                    255,
                    193,
                    7
                )
            );

            statusLabel.setForeground(
                Color.BLACK
            );

        } else if (
            status.equals("CANCELLED")
        ) {

            statusLabel.setBackground(
                new Color(
                    220,
                    53,
                    69
                )
            );

            statusLabel.setForeground(
                Color.WHITE
            );

        } else {

            statusLabel.setBackground(
                new Color(
                    40,
                    167,
                    69
                )
            );

            statusLabel.setForeground(
                Color.WHITE
            );
        }

        JLabel timingLabel =
            new JLabel(
                timing
            );

        timingLabel.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                11
            )
        );

        timingLabel.setAlignmentX(
            Component.CENTER_ALIGNMENT
        );

        statusPanel.add(
            statusLabel
        );

        statusPanel.add(
            Box.createRigidArea(
                new Dimension(0, 7)
            )
        );

        statusPanel.add(
            timingLabel
        );

        // --------------------------------------------------------
        // Add sections to card
        // --------------------------------------------------------

        card.add(
            informationPanel,
            BorderLayout.CENTER
        );

        card.add(
            statusPanel,
            BorderLayout.EAST
        );

        parent.add(
            card
        );

        parent.add(
            Box.createRigidArea(
                new Dimension(0, 10)
            )
        );
    }
}