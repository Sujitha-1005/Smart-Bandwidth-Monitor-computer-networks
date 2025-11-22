import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.awt.geom.Path2D;

public class BandwidthMonitorClient extends JFrame implements NetworkMonitor.NetworkDataListener {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9999;
    private static final int UPDATE_INTERVAL = 1000; // ms
    private static final int HISTORY_SIZE = 60; // Number of data points to keep

    private final NetworkMonitor networkMonitor;
    private final List<Double> downloadSpeedHistory = new ArrayList<>();
    private final List<Double> uploadSpeedHistory = new ArrayList<>();

    private BandwidthGraph bandwidthGraph;
    private JLabel downloadSpeedLabel;
    private JLabel uploadSpeedLabel;
    private JLabel totalDownloadedLabel;
    private JLabel totalUploadedLabel;
    private JLabel latencyLabel;
    private JLabel packetLossLabel;
    private JLabel publicIpLabel; // New label for Public IP
    private JLabel ispLabel; // New label for ISP
    private JLabel networkNameLabel; // New label for Network Name
    private JComboBox<String> interfaceSelector;

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private final ScheduledExecutorService serverCommunicationScheduler = Executors.newScheduledThreadPool(1);

    public BandwidthMonitorClient() {
        // Initialize the UI
        setTitle("Smart Bandwidth Monitor");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set up network monitoring
        networkMonitor = new NetworkMonitor(UPDATE_INTERVAL);
        networkMonitor.addListener(this);

        // Initialize UI components
        initializeUI();

        // Connect to the server
        connectToServer();

        // Start network monitoring
        networkMonitor.start();

        // Update network info (Public IP, ISP, and Network Name)
        updateNetworkInfo();

        // Set up window close event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }

    private void initializeUI() {
        // Set the look and feel to the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create the graph panel
        bandwidthGraph = new BandwidthGraph();
        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Network Traffic", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Sans-Serif", Font.BOLD, 14)));
        graphPanel.add(bandwidthGraph, BorderLayout.CENTER);

        // Create the stats panel
        JPanel statsPanel = createStatsPanel();

        // Create the control panel
        JPanel controlPanel = createControlPanel();

        // Add panels to the main panel
        mainPanel.add(graphPanel, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.EAST);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Network Statistics", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Sans-Serif", Font.BOLD, 14)));
        statsPanel.setPreferredSize(new Dimension(200, 0));

        // Create labels for stats
        downloadSpeedLabel = new JLabel("Download: 0 KB/s");
        uploadSpeedLabel = new JLabel("Upload: 0 KB/s");
        totalDownloadedLabel = new JLabel("Total Downloaded: 0 KB");
        totalUploadedLabel = new JLabel("Total Uploaded: 0 KB");
        latencyLabel = new JLabel("Latency: 0 ms");
        packetLossLabel = new JLabel("Packet Loss: 0%");
        publicIpLabel = new JLabel("Public IP: Unknown"); // New label
        ispLabel = new JLabel("ISP: Unknown"); // New label
        networkNameLabel = new JLabel("Network: Unknown"); // New label

        // Set font for all labels
        Font statFont = new Font("Sans-Serif", Font.PLAIN, 12);
        downloadSpeedLabel.setFont(statFont);
        uploadSpeedLabel.setFont(statFont);
        totalDownloadedLabel.setFont(statFont);
        totalUploadedLabel.setFont(statFont);
        latencyLabel.setFont(statFont);
        packetLossLabel.setFont(statFont);
        publicIpLabel.setFont(statFont); // Set font for Public IP label
        ispLabel.setFont(statFont); // Set font for ISP label
        networkNameLabel.setFont(statFont); // Set font for Network Name label

        // Add labels to panel with padding
        addLabelToPanel(statsPanel, downloadSpeedLabel);
        addLabelToPanel(statsPanel, uploadSpeedLabel);
        addLabelToPanel(statsPanel, totalDownloadedLabel);
        addLabelToPanel(statsPanel, totalUploadedLabel);
        addLabelToPanel(statsPanel, latencyLabel);
        addLabelToPanel(statsPanel, packetLossLabel);
        addLabelToPanel(statsPanel, publicIpLabel); // Add Public IP label
        addLabelToPanel(statsPanel, ispLabel); // Add ISP label
        addLabelToPanel(statsPanel, networkNameLabel); // Add Network Name label

        // Add filler to push everything to the top
        statsPanel.add(Box.createVerticalGlue());

        return statsPanel;
    }

    private void addLabelToPanel(JPanel panel, JLabel label) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.add(label);
        panel.add(wrapper);
        panel.add(Box.createVerticalStrut(5)); // Add some space between labels
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Controls", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Sans-Serif", Font.BOLD, 14)));

        // Interface selector
        JLabel interfaceLabel = new JLabel("Network Interface: ");
        interfaceSelector = new JComboBox<>();

        // Populate interface selector
        try {
            List<String> interfaces = networkMonitor.getNetworkInterfaces();
            for (String iface : interfaces) {
                interfaceSelector.addItem(iface);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add action listener to interface selector
        interfaceSelector.addActionListener(e -> {
            String selectedInterface = (String) interfaceSelector.getSelectedItem();
            networkMonitor.setNetworkInterface(selectedInterface);
        });

        // Add components to panel
        controlPanel.add(interfaceLabel);
        controlPanel.add(interfaceSelector);

        return controlPanel;
    }

    // Method to get the connected Wi-Fi network name (SSID)
    public String getConnectedWifiSSID() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            Process process;
            if (os.contains("win")) {
                // Windows: Use netsh command
                process = Runtime.getRuntime().exec("netsh wlan show interfaces");
            } else if (os.contains("mac") || os.contains("linux")) {
                // macOS/Linux: Use nmcli command
                process = Runtime.getRuntime().exec("nmcli -t -f active,ssid dev wifi");
            } else {
                throw new UnsupportedOperationException("Unsupported OS: " + os);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (os.contains("win") && line.trim().startsWith("SSID")) {
                    return line.split(":")[1].trim();
                } else if ((os.contains("mac") || os.contains("linux")) && line.startsWith("yes:")) {
                    return line.split(":")[1].trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    // Method to update Public IP, ISP, and Network Name information
    public void updateNetworkInfo() {
        // Temporary variables to hold the values
        String tempPublicIp = "Unknown";
        String tempIsp = "Unknown";
        String tempNetworkName = "Unknown";

        NetworkInfo networkInfo = new NetworkInfo();
        try {
            // Fetch values from the API
            tempPublicIp = networkInfo.getPublicIP();
            tempIsp = networkInfo.getISP();
            tempNetworkName = getConnectedWifiSSID(); // Get the network name
        } catch (Exception e) {
            System.out.println("Failed to fetch network info: " + e.getMessage());
        }

        // Assign the values to final variables
        final String publicIp = tempPublicIp;
        final String isp = tempIsp;
        final String networkName = tempNetworkName;

        // Update the labels in the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            networkNameLabel.setText("Network: " + networkName);
            publicIpLabel.setText("Public IP: " + publicIp);
            ispLabel.setText("ISP: " + isp);
        });
    }

    
    // Rest of the code remains the same...
    // (connectToServer, onNetworkDataUpdated, formatDataSize, cleanup, etc.)


    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);

            // Schedule sending data to server
            serverCommunicationScheduler.scheduleAtFixedRate(() -> {
                try {
                    if (socket != null && !socket.isClosed() && socket.isConnected()) {
                        synchronized (downloadSpeedHistory) {
                            if (!downloadSpeedHistory.isEmpty() && !uploadSpeedHistory.isEmpty()) {
                                double downloadSpeed = downloadSpeedHistory.get(downloadSpeedHistory.size() - 1);
                                double uploadSpeed = uploadSpeedHistory.get(uploadSpeedHistory.size() - 1);

                                NetworkData data = new NetworkData(
                                        downloadSpeed,
                                        uploadSpeed,
                                        Long.parseLong(totalDownloadedLabel.getText().split(":")[1].trim().split(" ")[0]),
                                        Long.parseLong(totalUploadedLabel.getText().split(":")[1].trim().split(" ")[0]),
                                        Integer.parseInt(latencyLabel.getText().split(":")[1].trim().split(" ")[0]),
                                        Integer.parseInt(packetLossLabel.getText().split(":")[1].trim().split("%")[0])
                                );

                                output.writeObject(data);
                                output.flush();

                                // Read response (not used in this simple example)
                                input.readObject();
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Server communication error: " + e.getMessage());
                    // Try to reconnect
                    try {
                        if (socket != null) socket.close();
                        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                        output = new ObjectOutputStream(socket.getOutputStream());
                        input = new ObjectInputStream(socket.getInputStream());
                        System.out.println("Reconnected to server");
                    } catch (IOException reconnectError) {
                        System.out.println("Failed to reconnect: " + reconnectError.getMessage());
                    }
                }
            }, 2000, 2000, TimeUnit.MILLISECONDS);

        } catch (IOException e) {
            System.out.println("Could not connect to server: " + e.getMessage());
        }
    }

    @Override
    public void onNetworkDataUpdated(NetworkData data) {
        SwingUtilities.invokeLater(() -> {
            // Update the UI with the new data
            DecimalFormat df = new DecimalFormat("#,###.##");

            // Update speed labels
            downloadSpeedLabel.setText("Download: " + df.format(data.getDownloadSpeed()) + " KB/s");
            uploadSpeedLabel.setText("Upload: " + df.format(data.getUploadSpeed()) + " KB/s");

            // Format total data with appropriate units
            String totalDownloadedText = formatDataSize(data.getTotalDownloaded());
            String totalUploadedText = formatDataSize(data.getTotalUploaded());

            totalDownloadedLabel.setText("Total Downloaded: " + totalDownloadedText);
            totalUploadedLabel.setText("Total Uploaded: " + totalUploadedText);

            // Update latency and packet loss
            latencyLabel.setText("Latency: " + data.getLatency() + " ms");
            packetLossLabel.setText("Packet Loss: " + data.getPacketLoss() + "%");

            // Update the graph data
            synchronized (downloadSpeedHistory) {
                downloadSpeedHistory.add(data.getDownloadSpeed());
                uploadSpeedHistory.add(data.getUploadSpeed());

                // Keep only the last HISTORY_SIZE points
                while (downloadSpeedHistory.size() > HISTORY_SIZE) {
                    downloadSpeedHistory.remove(0);
                }

                while (uploadSpeedHistory.size() > HISTORY_SIZE) {
                    uploadSpeedHistory.remove(0);
                }

                bandwidthGraph.updateData(downloadSpeedHistory, uploadSpeedHistory);
            }
        });
    }

    private String formatDataSize(long sizeInKB) {
        DecimalFormat df = new DecimalFormat("#,###.##");

        if (sizeInKB < 1024) {
            return df.format(sizeInKB) + " KB";
        } else if (sizeInKB < 1024 * 1024) {
            return df.format(sizeInKB / 1024.0) + " MB";
        } else {
            return df.format(sizeInKB / (1024.0 * 1024.0)) + " GB";
        }
    }

    private void cleanup() {
        // Stop the network monitor
        networkMonitor.stop();

        // Shutdown the scheduler
        serverCommunicationScheduler.shutdown();

        // Close the socket
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BandwidthMonitorClient().setVisible(true);
        });
    }

    // Inner class for the bandwidth graph
    private static class BandwidthGraph extends JPanel {
        private List<Double> downloadData = new ArrayList<>();
        private List<Double> uploadData = new ArrayList<>();
        private double maxValue = 100.0; // Initial max value in KB/s

        public BandwidthGraph() {
            setPreferredSize(new Dimension(600, 300));
            setBackground(Color.WHITE);
        }

        public void updateData(List<Double> downloadData, List<Double> uploadData) {
            this.downloadData = new ArrayList<>(downloadData);
            this.uploadData = new ArrayList<>(uploadData);

            // Find the max value for scaling
            maxValue = 100.0; // Default minimum
            for (Double value : downloadData) {
                maxValue = Math.max(maxValue, value * 1.1); // Add 10% margin
            }
            for (Double value : uploadData) {
                maxValue = Math.max(maxValue, value * 1.1); // Add 10% margin
            }

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();

            // Enable anti-aliasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 40;
            int labelPadding = 20;

            int chartWidth = width - 2 * padding;
            int chartHeight = height - 2 * padding;

            // Draw grid
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{5f}, 0f));

            // Draw horizontal grid lines
            int numHorizontalLines = 5;
            for (int i = 0; i <= numHorizontalLines; i++) {
                int y = padding + chartHeight - (i * chartHeight / numHorizontalLines);
                g2d.drawLine(padding, y, width - padding, y);

                // Draw labels
                DecimalFormat df = new DecimalFormat("#,###");
                String label = df.format((maxValue * i / numHorizontalLines)) + " KB/s";
                FontMetrics metrics = g2d.getFontMetrics();
                int labelWidth = metrics.stringWidth(label);
                g2d.setColor(Color.BLACK);
                g2d.drawString(label, padding - labelWidth - 5, y + (metrics.getHeight() / 2) - 3);
                g2d.setColor(Color.LIGHT_GRAY);
            }

            // Draw vertical grid lines
            int numVerticalLines = 6;
            for (int i = 0; i <= numVerticalLines; i++) {
                int x = padding + (i * chartWidth / numVerticalLines);
                g2d.drawLine(x, padding, x, height - padding);

                // Draw time labels (assuming 1-second intervals and HISTORY_SIZE points)
                if (i < numVerticalLines) {
                    String label = "-" + (HISTORY_SIZE - i * HISTORY_SIZE / numVerticalLines) + "s";
                    FontMetrics metrics = g2d.getFontMetrics();
                    int labelWidth = metrics.stringWidth(label);
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(label, x - labelWidth / 2, height - padding + metrics.getHeight() + 3);
                    g2d.setColor(Color.LIGHT_GRAY);
                }
            }

            // Draw axis labels
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Sans-Serif", Font.BOLD, 12));

            // Y-axis label
            String yLabel = "Speed (KB/s)";
            FontMetrics metrics = g2d.getFontMetrics();
            int labelWidth = metrics.stringWidth(yLabel);
            g2d.translate(15, height / 2 + labelWidth / 2);
            g2d.rotate(-Math.PI / 2);
            g2d.drawString(yLabel, 0, 0);
            g2d.rotate(Math.PI / 2);
            g2d.translate(-15, -(height / 2 + labelWidth / 2));

            // X-axis label
            String xLabel = "Time (seconds)";
            labelWidth = metrics.stringWidth(xLabel);
            g2d.drawString(xLabel, width / 2 - labelWidth / 2, height - 10);

            // Draw the data
            if (downloadData.size() > 1 && uploadData.size() > 1) {
                // Draw download speed (blue)
                g2d.setColor(new Color(0, 102, 204));
                g2d.setStroke(new BasicStroke(2f));
                drawLine(g2d, downloadData, padding, chartWidth, chartHeight);

                // Draw upload speed (red)
                g2d.setColor(new Color(204, 0, 0));
                g2d.setStroke(new BasicStroke(2f));
                drawLine(g2d, uploadData, padding, chartWidth, chartHeight);
            }

            // Draw legend
            int legendX = width - padding - 120;
            int legendY = padding + 20;
            int legendItemHeight = 20;

            g2d.setFont(new Font("Sans-Serif", Font.PLAIN, 12));

            // Download legend
            g2d.setColor(new Color(0, 102, 204));
            g2d.fillRect(legendX, legendY, 15, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Download", legendX + 20, legendY + 10);

            // Upload legend
            g2d.setColor(new Color(204, 0, 0));
            g2d.fillRect(legendX, legendY + legendItemHeight, 15, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Upload", legendX + 20, legendY + legendItemHeight + 10);

            g2d.dispose();
        }

        private void drawLine(Graphics2D g2d, List<Double> data, int padding, int chartWidth, int chartHeight) {
            int xPadding = padding;
            int yPadding = padding;

            int dataSize = data.size();

            // Create path for the line
            
            Path2D.Double path = new Path2D.Double();
            
            for (int i = 0; i < dataSize; i++) {
                double x = xPadding + ((double) i / (dataSize - 1)) * chartWidth;
                double y = yPadding + chartHeight - ((data.get(i) / maxValue) * chartHeight);
                
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
                
                // Draw points
                g2d.fillOval((int) x - 3, (int) y - 3, 6, 6);
            }
            
            // Draw the line
            g2d.draw(path);
            
            // Draw filled area beneath the line with transparency
            Color lineColor = g2d.getColor();
            Color fillColor = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 40);
            g2d.setColor(fillColor);
            
            Path2D.Double fillPath = new Path2D.Double(path);
            fillPath.lineTo(xPadding + chartWidth, yPadding + chartHeight);
            fillPath.lineTo(xPadding, yPadding + chartHeight);
            fillPath.closePath();
            
            g2d.fill(fillPath);
        }
    }
}