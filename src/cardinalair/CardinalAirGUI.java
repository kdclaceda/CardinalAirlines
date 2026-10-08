package cardinalair;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CardinalAirGUI extends JFrame {

    private final BookingManager bookingManager;
    private final Admin admin = new Admin();
    private final Connection conn;

    // --- State Variables ---
    private String selectedFlightNum = null;
    private String selectedSeatNum = null;
    private JButton selectedSeatButton = null;
    private double currentPrice = 0.0;
    private Booking lastSearchedBooking = null;

    // --- UI Colors ---
    private final Color COLOR_FIRST = new Color(255, 235, 115);
    private final Color COLOR_BUSINESS = new Color(160, 216, 239);
    private final Color COLOR_ECONOMY = new Color(153, 255, 153);
    private final Color COLOR_SELECTED = new Color(77, 121, 255);
    private final Color COLOR_BOOKED = new Color(255, 128, 128);

    // --- UI Components (Book Flight tab) ---
    private final JTextField txtOrigin = new JTextField(8);
    private final JTextField txtDestination = new JTextField(8);
    private final JTextField txtDate = new JTextField(8);
    private final JButton btnApplyFilter = new JButton("Apply Filter");
    private final JButton btnShowAll = new JButton("Show All");

    private final DefaultListModel<String> flightListModel =
        new DefaultListModel<>();

    private final JList<String> flightList =
        new JList<>(flightListModel);

    private final JPanel seatGridPanel = new JPanel();

    private final List<JButton> seatButtons =
        new ArrayList<>();

    private final JLabel lblSelectedSeat =
        new JLabel("None");

    private final JTextField txtFullName =
        new JTextField(15);

    private final JTextField txtEmail =
        new JTextField(15);

    private final JTextField txtPhone =
        new JTextField(15);

    private final JLabel lblTotalPrice =
        new JLabel("PHP 0.00");

    private final JButton btnConfirmBook =
        new JButton("Confirm & Book");

    // --- UI Components (Manage Booking / Print Ticket tab) ---
    private final JTextField txtSearchBookingId =
        new JTextField(15);

    private final JButton btnSearchBooking =
        new JButton("Search");

    private final JButton btnPrintTicket =
        new JButton("Print Ticket");

    private final JButton btnCancelBooking =
        new JButton("Cancel Booking");

    private final JTextArea taBookingDetails =
        new JTextArea();

    public CardinalAirGUI(Connection conn) {

        this.conn = conn;
        this.bookingManager = new BookingManager();

        setTitle(
            "Cardinal Airlines - Flight Ticket System"
        );

        setSize(1100, 720);

        setDefaultCloseOperation(
            JFrame.EXIT_ON_CLOSE
        );

        setLocationRelativeTo(null);

        initComponents();
        loadFlightsToList();
    }

    private void initComponents() {

        JPanel rootPanel =
            new JPanel(
                new BorderLayout(10, 10)
            );

        rootPanel.setBorder(
            BorderFactory.createEmptyBorder(
                10, 15, 10, 15
            )
        );

        JPanel headerPanel =
            new JPanel(
                new BorderLayout()
            );

        JLabel lblTitle =
            new JLabel(
                "Flight Reservation and Booking"
            );

        lblTitle.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                18
            )
        );

        JButton btnSwitchMode =
            new JButton("<- Switch Mode");

        headerPanel.add(
            lblTitle,
            BorderLayout.WEST
        );

        headerPanel.add(
            btnSwitchMode,
            BorderLayout.EAST
        );

        rootPanel.add(
            headerPanel,
            BorderLayout.NORTH
        );

        btnSwitchMode.addActionListener(
            e -> attemptAdminLogin()
        );

        JTabbedPane tabbedPane =
            new JTabbedPane();

        JPanel tabBook =
            new JPanel(
                new BorderLayout(10, 10)
            );

        tabBook.setBorder(
            BorderFactory.createEmptyBorder(
                10, 5, 5, 5
            )
        );

        // Filter Bar
        JPanel filterPanel =
            new JPanel(
                new FlowLayout(
                    FlowLayout.LEFT,
                    10,
                    5
                )
            );

        filterPanel.setBorder(
            BorderFactory.createTitledBorder(
                "Filter Available Flights"
            )
        );

        filterPanel.add(
            new JLabel("Origin:")
        );

        filterPanel.add(
            txtOrigin
        );

        filterPanel.add(
            new JLabel("Destination:")
        );

        filterPanel.add(
            txtDestination
        );

        filterPanel.add(
            new JLabel("Date:")
        );

        filterPanel.add(
            txtDate
        );

        filterPanel.add(
            btnApplyFilter
        );

        filterPanel.add(
            btnShowAll
        );

        tabBook.add(
            filterPanel,
            BorderLayout.NORTH
        );

        // Content Area
        JPanel contentPanel =
            new JPanel(
                new GridLayout(
                    1,
                    3,
                    10,
                    10
                )
            );

        // Column 1: Flights
        JPanel col1 =
            new JPanel(
                new BorderLayout()
            );

        col1.setBorder(
            BorderFactory.createTitledBorder(
                "1. Select Available Flight"
            )
        );

        flightList.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        flightList.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                12
            )
        );

        col1.add(
            new JScrollPane(flightList),
            BorderLayout.CENTER
        );

        // Column 2: Seats
        JPanel col2 =
            new JPanel(
                new BorderLayout(
                    5,
                    5
                )
            );

        col2.setBorder(
            BorderFactory.createTitledBorder(
                "2. Seats"
            )
        );

        JPanel legendPanel =
            new JPanel(
                new GridLayout(
                    2,
                    3,
                    5,
                    5
                )
            );

        legendPanel.setBorder(
            BorderFactory.createEmptyBorder(
                5,
                5,
                5,
                5
            )
        );

        legendPanel.add(
            createLegendBadge(
                "★ First",
                COLOR_FIRST
            )
        );

        legendPanel.add(
            createLegendBadge(
                "◆ Business",
                COLOR_BUSINESS
            )
        );

        legendPanel.add(
            createLegendBadge(
                "● Economy",
                COLOR_ECONOMY
            )
        );

        legendPanel.add(
            createLegendBadge(
                "✓ Selected",
                COLOR_SELECTED,
                Color.WHITE
            )
        );

        legendPanel.add(
            createLegendBadge(
                "Taken",
                COLOR_BOOKED,
                Color.WHITE
            )
        );

        col2.add(
            legendPanel,
            BorderLayout.NORTH
        );

        seatGridPanel.setLayout(
            new GridLayout(
                9,
                7,
                4,
                4
            )
        );

        seatGridPanel.setBorder(
            BorderFactory.createEmptyBorder(
                5,
                5,
                5,
                5
            )
        );

        col2.add(
            seatGridPanel,
            BorderLayout.CENTER
        );

        // Column 3: Passenger
        JPanel col3 =
            new JPanel();

        col3.setLayout(
            new BoxLayout(
                col3,
                BoxLayout.Y_AXIS
            )
        );

        col3.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    "3. Passenger Details"
                ),
                BorderFactory.createEmptyBorder(
                    10,
                    10,
                    10,
                    10
                )
            )
        );

        lblSelectedSeat.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                14
            )
        );

        lblSelectedSeat.setForeground(
            new Color(0, 0, 153)
        );

        lblTotalPrice.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                14
            )
        );

        lblTotalPrice.setForeground(
            new Color(0, 0, 153)
        );

        addFormField(
            col3,
            "Selected Seat:",
            lblSelectedSeat
        );

        col3.add(
            Box.createRigidArea(
                new Dimension(0, 15)
            )
        );

        addFormField(
            col3,
            "Full Name:",
            txtFullName
        );

        col3.add(
            Box.createRigidArea(
                new Dimension(0, 10)
            )
        );

        addFormField(
            col3,
            "Email Address:",
            txtEmail
        );

        col3.add(
            Box.createRigidArea(
                new Dimension(0, 10)
            )
        );

        addFormField(
            col3,
            "Phone Number:",
            txtPhone
        );

        col3.add(
            Box.createRigidArea(
                new Dimension(0, 15)
            )
        );

        addFormField(
            col3,
            "Total Price:",
            lblTotalPrice
        );

        col3.add(
            Box.createRigidArea(
                new Dimension(0, 25)
            )
        );

        btnConfirmBook.setMaximumSize(
            new Dimension(
                Integer.MAX_VALUE,
                40
            )
        );

        btnConfirmBook.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                12
            )
        );

        col3.add(
            btnConfirmBook
        );

        contentPanel.add(col1);
        contentPanel.add(col2);
        contentPanel.add(col3);

        tabBook.add(
            contentPanel,
            BorderLayout.CENTER
        );

        tabbedPane.addTab(
            "Book Flight & Reserve",
            tabBook
        );

        tabbedPane.addTab(
            "Manage Booking / Print Ticket",
            buildManageBookingTab()
        );

        tabbedPane.addTab(
            "Flight Status",
            buildFlightStatusTab()
        );

        rootPanel.add(
            tabbedPane,
            BorderLayout.CENTER
        );

        add(rootPanel);

        // Flight selection
        flightList.addListSelectionListener(
            e -> {

                if (!e.getValueIsAdjusting()) {

                    String selectedValue =
                        flightList.getSelectedValue();

                    if (selectedValue != null) {

                        selectedFlightNum =
                            selectedValue
                                .split("\\|")[0]
                                .trim();

                        if (
                            isFlightFullyBooked(
                                selectedFlightNum
                            )
                        ) {

                            JOptionPane.showMessageDialog(
                                this,
                                "Flight is fully booked.",
                                "Fully Booked",
                                JOptionPane.WARNING_MESSAGE
                            );

                            return;
                        }

                        resetBookingForm();
                        buildAirplaneSeatGrid();
                    }
                }
            }
        );

        btnApplyFilter.addActionListener(
            e -> filterFlights()
        );

        btnShowAll.addActionListener(
            e -> {

                txtOrigin.setText("");
                txtDestination.setText("");
                txtDate.setText("");

                loadFlightsToList();
            }
        );

        btnConfirmBook.addActionListener(
            e -> executeBooking()
        );
    }

    // ============================================================
    // ADMIN
    // ============================================================

    private void attemptAdminLogin() {

        JTextField txtUser =
            new JTextField();

        JPasswordField txtPass =
            new JPasswordField();

        JPanel loginPanel =
            new JPanel(
                new GridLayout(
                    2,
                    2,
                    5,
                    5
                )
            );

        loginPanel.add(
            new JLabel("Username:")
        );

        loginPanel.add(txtUser);

        loginPanel.add(
            new JLabel("Password:")
        );

        loginPanel.add(txtPass);

        int result =
            JOptionPane.showConfirmDialog(
                this,
                loginPanel,
                "Admin Login",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

        if (
            result != JOptionPane.OK_OPTION
        ) {
            return;
        }

        String user =
            txtUser
                .getText()
                .trim();

        String pass =
            new String(
                txtPass.getPassword()
            );

        if (
            admin.login(
                user,
                pass
            )
        ) {

            showAdminReport();

        } else {

            JOptionPane.showMessageDialog(
                this,
                "Invalid admin credentials.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showAdminReport() {

        int totalFlights =
            bookingManager
                .getFlights()
                .size();

        int totalBookings = 0;

        for (
            Booking booking :
            bookingManager.getBookings()
        ) {

            if (
                !booking.isCancelled()
            ) {
                totalBookings++;
            }
        }

        String report =
            admin.generateReport(
                totalBookings,
                totalFlights
            );

        JOptionPane.showMessageDialog(
            this,
            report,
            "Admin Report",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ============================================================
    // MANAGE BOOKING
    // ============================================================

    private JPanel buildManageBookingTab() {

        JPanel tabManage =
            new JPanel(
                new BorderLayout(
                    10,
                    10
                )
            );

        tabManage.setBorder(
            BorderFactory.createEmptyBorder(
                10,
                5,
                5,
                5
            )
        );

        JPanel searchPanel =
            new JPanel(
                new FlowLayout(
                    FlowLayout.LEFT,
                    10,
                    5
                )
            );

        searchPanel.setBorder(
            BorderFactory.createTitledBorder(
                "Look Up Booking"
            )
        );

        searchPanel.add(
            new JLabel("Booking ID:")
        );

        searchPanel.add(
            txtSearchBookingId
        );

        searchPanel.add(
            btnSearchBooking
        );

        tabManage.add(
            searchPanel,
            BorderLayout.NORTH
        );

        taBookingDetails.setEditable(
            false
        );

        taBookingDetails.setFont(
            new Font(
                "Monospaced",
                Font.PLAIN,
                13
            )
        );

        taBookingDetails.setText(
            "Enter a Booking ID above and click Search to view details."
        );

        JScrollPane detailsScroll =
            new JScrollPane(
                taBookingDetails
            );

        detailsScroll.setBorder(
            BorderFactory.createTitledBorder(
                "Booking Details"
            )
        );

        tabManage.add(
            detailsScroll,
            BorderLayout.CENTER
        );

        JPanel actionPanel =
            new JPanel(
                new FlowLayout(
                    FlowLayout.RIGHT,
                    10,
                    5
                )
            );

        btnPrintTicket.setEnabled(
            false
        );

        btnCancelBooking.setEnabled(
            false
        );

        actionPanel.add(
            btnCancelBooking
        );

        actionPanel.add(
            btnPrintTicket
        );

        tabManage.add(
            actionPanel,
            BorderLayout.SOUTH
        );

        btnSearchBooking.addActionListener(
            e -> searchBooking()
        );

        btnPrintTicket.addActionListener(
            e -> printTicket()
        );

        btnCancelBooking.addActionListener(
            e -> cancelBooking()
        );

        return tabManage;
    }

    private void searchBooking() {

        String bookingId =
            txtSearchBookingId
                .getText()
                .trim();

        if (
            bookingId.isEmpty()
        ) {

            JOptionPane.showMessageDialog(
                this,
                "Please enter a Booking ID.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Booking booking =
            bookingManager.findBooking(
                bookingId
            );

        if (booking == null) {

            lastSearchedBooking = null;

            taBookingDetails.setText(
                "No active booking found with ID: "
                    + bookingId
            );

            btnPrintTicket.setEnabled(
                false
            );

            btnCancelBooking.setEnabled(
                false
            );

            return;
        }

        lastSearchedBooking =
            booking;

        taBookingDetails.setText(
            buildBookingDetails(
                booking
            )
        );

        btnPrintTicket.setEnabled(
            true
        );

        btnCancelBooking.setEnabled(
            true
        );
    }

    private String buildBookingDetails(
        Booking booking
    ) {

        Flight flight =
            booking.getFlight();

        Passenger passenger =
            booking.getPassenger();

        Seat seat =
            booking.getSeat();

        return String.format(
            "=============================================\\n"
          + "            CARDINAL AIRLINES TICKET          \\n"
          + "=============================================\\n"
          + "Booking Ref : %s\\n"
          + "Status      : %s\\n"
          + "Passenger   : %s\\n"
          + "Email       : %s\\n"
          + "Phone       : %s\\n"
          + "---------------------------------------------\\n"
          + "Flight      : %s\\n"
          + "Route       : %s -> %s\\n"
          + "Departure   : %s\\n"
          + "Arrival     : %s\\n"
          + "Seat Number : %s (%s)\\n"
          + "Amount Paid : PHP %.2f\\n"
          + "=============================================",
            booking.getBookingId(),
            booking.isCancelled()
                ? "CANCELLED"
                : "CONFIRMED",
            passenger.getName(),
            passenger.getEmail(),
            passenger.getPhoneNum(),
            flight.getFlightNum(),
            flight.getOrigin(),
            flight.getDestination(),
            flight.getDepTime(),
            flight.getArrTime(),
            seat.getSeatNum(),
            seat.getSeatClass(),
            booking.getAmountPaid()
        );
    }

    private void printTicket() {

        if (
            lastSearchedBooking == null
        ) {

            JOptionPane.showMessageDialog(
                this,
                "Please search for a booking first.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Ticket ticket =
            new Ticket(
                "TK-"
                    + lastSearchedBooking
                        .getBookingId(),
                lastSearchedBooking
            );

        JTextArea printArea =
            new JTextArea(
                ticket.generateTicketSummary()
            );

        printArea.setFont(
            new Font(
                "Monospaced",
                Font.PLAIN,
                12
            )
        );

        try {

            printArea.print();

        } catch (
            PrinterException pe
        ) {

            JOptionPane.showMessageDialog(
                this,
                "Printing failed: "
                    + pe.getMessage(),
                "Print Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cancelBooking() {

        if (
            lastSearchedBooking == null
        ) {
            return;
        }

        int confirm =
            JOptionPane.showConfirmDialog(
                this,
                "Cancel booking "
                    + lastSearchedBooking
                        .getBookingId()
                    + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
            );

        if (
            confirm != JOptionPane.YES_OPTION
        ) {
            return;
        }

        String flightNum =
            lastSearchedBooking
                .getFlight()
                .getFlightNum();

        String bookingId =
            lastSearchedBooking
                .getBookingId();

        if (
            bookingManager.cancelReservation(
                bookingId
            )
        ) {

            JOptionPane.showMessageDialog(
                this,
                "Booking "
                    + bookingId
                    + " cancelled. "
                    + "The seat is now open."
            );

            taBookingDetails.setText(
                "Booking "
                    + bookingId
                    + " has been cancelled."
            );

            btnPrintTicket.setEnabled(
                false
            );

            btnCancelBooking.setEnabled(
                false
            );

            lastSearchedBooking = null;

            selectedFlightNum =
                flightNum;

            resetBookingForm();

            buildAirplaneSeatGrid();

        } else {

            JOptionPane.showMessageDialog(
                this,
                "Booking could not be cancelled.",
                "Cancellation Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ============================================================
    // FLIGHT STATUS - UI ONLY
    // ============================================================

    private JPanel buildFlightStatusTab() {

        JPanel tab =
            new JPanel(
                new BorderLayout(
                    10,
                    10
                )
            );

        tab.setBorder(
            BorderFactory.createEmptyBorder(
                15,
                15,
                15,
                15
            )
        );

        JPanel headerPanel =
            new JPanel();

        headerPanel.setLayout(
            new BoxLayout(
                headerPanel,
                BoxLayout.Y_AXIS
            )
        );

        JLabel title =
            new JLabel(
                "Flight Status"
            );

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

        tab.add(
            headerPanel,
            BorderLayout.NORTH
        );

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
                10,
                0,
                10,
                0
            )
        );

        addFlightStatusCard(
            flightListPanel,
            "PR-801",
            "Manila, Philippines",
            "Kyoto, Japan",
            "DELAYED",
            "45 minutes",
            "Estimated departure: 8:45 AM"
        );

        addFlightStatusCard(
            flightListPanel,
            "BA-112",
            "Manila, Philippines",
            "Davao, Philippines",
            "ON TIME",
            "On schedule",
            "No operational issues reported"
        );

        addFlightStatusCard(
            flightListPanel,
            "QF-505",
            "Manila, Philippines",
            "Los Angeles, United States",
            "CANCELLED",
            "Flight cancelled",
            "Please contact the airline for further assistance"
        );

        addFlightStatusCard(
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

        tab.add(
            scrollPane,
            BorderLayout.CENTER
        );

        JLabel demoLabel =
            new JLabel(
                "DEMO ONLY — These flight statuses are hardcoded sample data "
              + "and are not connected to the database."
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

        tab.add(
            demoLabel,
            BorderLayout.SOUTH
        );

        return tab;
    }

    private void addFlightStatusCard(
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

        JPanel informationPanel =
            new JPanel();

        informationPanel.setLayout(
            new BoxLayout(
                informationPanel,
                BoxLayout.Y_AXIS
            )
        );

        informationPanel.setOpaque(
            false
        );

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

        JPanel statusPanel =
            new JPanel();

        statusPanel.setLayout(
            new BoxLayout(
                statusPanel,
                BoxLayout.Y_AXIS
            )
        );

        statusPanel.setOpaque(
            false
        );

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

        statusLabel.setOpaque(
            true
        );

        statusLabel.setBorder(
            BorderFactory.createEmptyBorder(
                8,
                18,
                8,
                18
            )
        );

        if (
            status.equals("DELAYED")
        ) {

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

        card.add(
            informationPanel,
            BorderLayout.CENTER
        );

        card.add(
            statusPanel,
            BorderLayout.EAST
        );

        parent.add(card);

        parent.add(
            Box.createRigidArea(
                new Dimension(0, 10)
            )
        );
    }

    // ============================================================
    // SEAT ICONS - UI ONLY
    // ============================================================

    private enum SeatIconType {
        FIRST,
        BUSINESS,
        ECONOMY,
        TAKEN
    }

    private static class SeatIcon
        implements Icon {

        private final SeatIconType type;

        private final int width = 24;
        private final int height = 24;

        public SeatIcon(
            SeatIconType type
        ) {
            this.type = type;
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }

        @Override
        public void paintIcon(
            Component c,
            Graphics g,
            int x,
            int y
        ) {

            Graphics2D g2 =
                (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (
                type == SeatIconType.TAKEN
            ) {

                // HEAD
                g2.setColor(
                    new Color(
                        55,
                        55,
                        55
                    )
                );

                g2.fillOval(
                    x + 8,
                    y + 1,
                    8,
                    8
                );

                // BODY
                g2.fillRoundRect(
                    x + 7,
                    y + 8,
                    10,
                    9,
                    4,
                    4
                );

                // SEAT
                g2.setColor(
                    new Color(
                        120,
                        40,
                        40
                    )
                );

                g2.fillRoundRect(
                    x + 5,
                    y + 14,
                    14,
                    6,
                    3,
                    3
                );

                // SEAT BACK
                g2.fillRoundRect(
                    x + 4,
                    y + 9,
                    4,
                    12,
                    3,
                    3
                );

                // LEGS
                g2.setColor(
                    new Color(
                        55,
                        55,
                        55
                    )
                );

                g2.setStroke(
                    new BasicStroke(2)
                );

                g2.drawLine(
                    x + 10,
                    y + 16,
                    x + 8,
                    y + 22
                );

                g2.drawLine(
                    x + 15,
                    y + 16,
                    x + 18,
                    y + 22
                );

            } else {

                String symbol;

                switch (
                    type
                ) {

                    case FIRST:
                        symbol = "★";
                        break;

                    case BUSINESS:
                        symbol = "◆";
                        break;

                    case ECONOMY:
                    default:
                        symbol = "●";
                        break;
                }

                g2.setColor(
                    Color.DARK_GRAY
                );

                g2.setFont(
                    new Font(
                        "SansSerif",
                        Font.BOLD,
                        18
                    )
                );

                FontMetrics fm =
                    g2.getFontMetrics();

                int textWidth =
                    fm.stringWidth(
                        symbol
                    );

                int drawX =
                    x
                        + (
                            width
                            - textWidth
                        ) / 2;

                int drawY =
                    y + 18;

                g2.drawString(
                    symbol,
                    drawX,
                    drawY
                );
            }

            g2.dispose();
        }
    }

    // ============================================================
    // BOOK FLIGHT UI / CONTROLLER
    // ============================================================

    private JLabel createLegendBadge(
        String text,
        Color bg
    ) {

        return createLegendBadge(
            text,
            bg,
            Color.BLACK
        );
    }

    private JLabel createLegendBadge(
        String text,
        Color bg,
        Color fg
    ) {

        JLabel label =
            new JLabel(
                text,
                SwingConstants.CENTER
            );

        label.setOpaque(
            true
        );

        label.setBackground(
            bg
        );

        label.setForeground(
            fg
        );

        label.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                10
            )
        );

        label.setBorder(
            BorderFactory.createLineBorder(
                Color.GRAY,
                1
            )
        );

        return label;
    }

    private void addFormField(
        JPanel panel,
        String labelText,
        JComponent component
    ) {

        JLabel label =
            new JLabel(
                labelText
            );

        label.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        component.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        if (
            component instanceof JTextField
        ) {

            component.setMaximumSize(
                new Dimension(
                    Integer.MAX_VALUE,
                    28
                )
            );
        }

        panel.add(
            label
        );

        panel.add(
            Box.createRigidArea(
                new Dimension(
                    0,
                    3
                )
            )
        );

        panel.add(
            component
        );
    }

    private void loadFlightsToList() {

        flightListModel.clear();

        List<Flight> flights =
            bookingManager.getFlights();

        if (
            flights != null
        ) {

            for (
                Flight f :
                flights
            ) {

                flightListModel.addElement(
                    String.format(
                        "%s | %s -> %s",
                        f.getFlightNum(),
                        f.getOrigin(),
                        f.getDestination()
                    )
                );
            }
        }
    }

    private void filterFlights() {

        String origin =
            txtOrigin
                .getText()
                .trim();

        String destination =
            txtDestination
                .getText()
                .trim();

        String dateText =
            txtDate
                .getText()
                .trim();

        LocalDate date = null;

        if (
            !dateText.isEmpty()
        ) {

            try {

                date =
                    LocalDate.parse(
                        dateText
                    );

            } catch (
                Exception e
            ) {

                JOptionPane.showMessageDialog(
                    this,
                    "Invalid date. Use YYYY-MM-DD.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
                );

                return;
            }
        }

        flightListModel.clear();

        List<Flight> flights =
            bookingManager.searchFlights(
                origin,
                destination,
                date
            );

        for (
            Flight f :
            flights
        ) {

            flightListModel.addElement(
                String.format(
                    "%s | %s -> %s",
                    f.getFlightNum(),
                    f.getOrigin(),
                    f.getDestination()
                )
            );
        }
    }

    private Flight getSelectedFlight() {

        if (
            selectedFlightNum == null
        ) {
            return null;
        }

        for (
            Flight flight :
            bookingManager.getFlights()
        ) {

            if (
                flight
                    .getFlightNum()
                    .equalsIgnoreCase(
                        selectedFlightNum
                    )
            ) {

                return flight;
            }
        }

        return null;
    }

    private boolean isFlightFullyBooked(
        String flightNum
    ) {

        Flight flight = null;

        for (
            Flight f :
            bookingManager.getFlights()
        ) {

            if (
                f.getFlightNum()
                    .equalsIgnoreCase(
                        flightNum
                    )
            ) {

                flight = f;
                break;
            }
        }

        return
            flight != null
            &&
            flight
                .getAvailSeats()
                .isEmpty();
    }

    private void buildAirplaneSeatGrid() {

        seatGridPanel.removeAll();

        seatButtons.clear();

        Flight flight =
            getSelectedFlight();

        if (
            flight == null
        ) {

            seatGridPanel.revalidate();
            seatGridPanel.repaint();

            return;
        }

        String[] cols = {
            "A",
            "B",
            "C",
            "AISLE",
            "D",
            "E",
            "F"
        };

        for (
            int row = 1;
            row <= 9;
            row++
        ) {

            for (
                String col :
                cols
            ) {

                if (
                    col.equals(
                        "AISLE"
                    )
                ) {

                    JLabel lblRow =
                        new JLabel(
                            String.valueOf(row),
                            SwingConstants.CENTER
                        );

                    lblRow.setFont(
                        new Font(
                            "SansSerif",
                            Font.BOLD,
                            11
                        )
                    );

                    seatGridPanel.add(
                        lblRow
                    );

                    continue;
                }

                Seat seat =
                    flight.findSeatNum(
                        row + col
                    );

                if (
                    seat == null
                ) {

                    seatGridPanel.add(
                        new JLabel()
                    );

                    continue;
                }

                JButton btnSeat =
                    new JButton(
                        seat.getSeatNum()
                    );

                btnSeat.setFont(
                    new Font(
                        "SansSerif",
                        Font.BOLD,
                        9
                    )
                );

                btnSeat.setMargin(
                    new Insets(
                        2,
                        2,
                        2,
                        2
                    )
                );

                btnSeat.setVerticalTextPosition(
                    SwingConstants.BOTTOM
                );

                btnSeat.setHorizontalTextPosition(
                    SwingConstants.CENTER
                );

                if (
                    seat.isReserved()
                ) {

                    btnSeat.setBackground(
                        COLOR_BOOKED
                    );

                    btnSeat.setForeground(
                        Color.WHITE
                    );

                    btnSeat.setEnabled(
                        false
                    );

                    btnSeat.setIcon(
                        new SeatIcon(
                            SeatIconType.TAKEN
                        )
                    );

                } else {

                    btnSeat.setBackground(
                        getSeatClassColor(
                            seat.getSeatClass()
                        )
                    );

                    btnSeat.setForeground(
                        Color.BLACK
                    );

                    btnSeat.setIcon(
                        new SeatIcon(
                            getSeatIconType(
                                seat.getSeatClass()
                            )
                        )
                    );

                    btnSeat.addActionListener(
                        e ->
                            selectSeat(
                                flight,
                                seat,
                                btnSeat
                            )
                    );
                }

                seatButtons.add(
                    btnSeat
                );

                seatGridPanel.add(
                    btnSeat
                );
            }
        }

        seatGridPanel.revalidate();
        seatGridPanel.repaint();
    }

    private void selectSeat(
        Flight flight,
        Seat seat,
        JButton button
    ) {

        if (
            selectedSeatButton != null
            &&
            selectedSeatButton.isEnabled()
        ) {

            Seat previousSeat =
                flight.findSeatNum(
                    selectedSeatButton
                        .getText()
                );

            if (
                previousSeat != null
            ) {

                selectedSeatButton
                    .setBackground(
                        getSeatClassColor(
                            previousSeat
                                .getSeatClass()
                        )
                    );

                selectedSeatButton
                    .setForeground(
                        Color.BLACK
                    );
            }
        }

        selectedSeatNum =
            seat.getSeatNum();

        selectedSeatButton =
            button;

        currentPrice =
            bookingManager.getTicketPrice(
                seat.getSeatClass()
            );

        button.setBackground(
            COLOR_SELECTED
        );

        button.setForeground(
            Color.WHITE
        );

        lblSelectedSeat.setText(
            seat.getSeatNum()
        );

        lblTotalPrice.setText(
            String.format(
                "PHP %.2f",
                currentPrice
            )
        );
    }

    private Color getSeatClassColor(
        SeatClass seatClass
    ) {

        switch (
            seatClass
        ) {

            case FIRST:
                return COLOR_FIRST;

            case BUSINESS:
                return COLOR_BUSINESS;

            case ECONOMY:
            default:
                return COLOR_ECONOMY;
        }
    }

    private SeatIconType getSeatIconType(
        SeatClass seatClass
    ) {

        switch (
            seatClass
        ) {

            case FIRST:
                return SeatIconType.FIRST;

            case BUSINESS:
                return SeatIconType.BUSINESS;

            case ECONOMY:
            default:
                return SeatIconType.ECONOMY;
        }
    }

    private void resetBookingForm() {

        selectedSeatNum = null;
        selectedSeatButton = null;
        currentPrice = 0.0;

        lblSelectedSeat.setText(
            "None"
        );

        lblTotalPrice.setText(
            "PHP 0.00"
        );
    }

    // ============================================================
    // BOOKING THROUGH BookingManager
    // ============================================================

    private void executeBooking() {

        String fullName =
            txtFullName
                .getText()
                .trim();

        String email =
            txtEmail
                .getText()
                .trim();

        String phone =
            txtPhone
                .getText()
                .trim();

        if (
            fullName.isEmpty()
        ) {

            JOptionPane.showMessageDialog(
                this,
                "Please enter passenger Full Name.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (
            selectedFlightNum == null
            ||
            selectedSeatNum == null
        ) {

            JOptionPane.showMessageDialog(
                this,
                "Please select a flight and an available seat first.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Flight flight =
            getSelectedFlight();

        if (
            flight == null
        ) {
            return;
        }

        Seat seat =
            flight.findSeatNum(
                selectedSeatNum
            );

        if (
            seat == null
            ||
            seat.isReserved()
        ) {

            JOptionPane.showMessageDialog(
                this,
                "That seat is no longer available.",
                "Seat Taken",
                JOptionPane.WARNING_MESSAGE
            );

            buildAirplaneSeatGrid();
            resetBookingForm();

            return;
        }

        PaymentDialog paymentDialog =
            new PaymentDialog(
                this,
                currentPrice,
                conn
            );

        paymentDialog.setVisible(
            true
        );

        if (
            !paymentDialog
                .isPaymentSuccessful()
        ) {

            JOptionPane.showMessageDialog(
                this,
                "Booking cancelled. No payment was processed."
            );

            return;
        }

        String passengerId =
            "P-"
                + (
                    System.currentTimeMillis()
                    % 10000
                );

        Passenger passenger =
            new Passenger(
                passengerId,
                fullName,
                email,
                phone
            );

        Booking booking =
            bookingManager.makeReservation(
                flight,
                seat,
                passenger,
                paymentDialog
                    .getFinalPrice()
            );

        if (
            booking == null
        ) {

            JOptionPane.showMessageDialog(
                this,
                "Booking could not be completed.",
                "Booking Error",
                JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        Ticket ticket =
            new Ticket(
                "TK-"
                    + booking
                        .getBookingId(),
                booking
            );

        JOptionPane.showMessageDialog(
            this,
            "Payment Successful!\n\n"
                + ticket
                    .generateTicketSummary(),
            "Booking Confirmed",
            JOptionPane.INFORMATION_MESSAGE
        );

        txtFullName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");

        resetBookingForm();

        buildAirplaneSeatGrid();
    }
}