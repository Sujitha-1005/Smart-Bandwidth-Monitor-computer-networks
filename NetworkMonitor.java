import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class NetworkMonitor {
    private final AtomicLong lastRxBytes = new AtomicLong(0);
    private final AtomicLong lastTxBytes = new AtomicLong(0);
    private final AtomicLong totalRxBytes = new AtomicLong(0);
    private final AtomicLong totalTxBytes = new AtomicLong(0);
    private final List<NetworkDataListener> listeners = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final int updateInterval;

    private String selectedInterface = null;

    public NetworkMonitor(int updateIntervalMs) {
        this.updateInterval = updateIntervalMs;
    }

    public void start() {
        // Initialize the base values
        updateNetworkStats(false);

        // Schedule regular updates
        scheduler.scheduleAtFixedRate(() -> {
            updateNetworkStats(true);
        }, updateInterval, updateInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    public void addListener(NetworkDataListener listener) {
        listeners.add(listener);
    }

    public void removeListener(NetworkDataListener listener) {
        listeners.remove(listener);
    }

    public void setNetworkInterface(String interfaceName) {
        this.selectedInterface = interfaceName;
    }

    public List<String> getNetworkInterfaces() {
        List<String> interfaces = new ArrayList<>();
        if (isLinux()) {
            // Read network interfaces from /proc/net/dev
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ProcessBuilder("cat", "/proc/net/dev").start().getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(":")) {
                        String iface = line.split(":")[0].trim();
                        interfaces.add(iface);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (isWindows()) {
            // Use PowerShell to list network interfaces
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ProcessBuilder("powershell", "Get-NetAdapter | Select-Object -ExpandProperty Name").start().getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    interfaces.add(line.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return interfaces;
    }

    private void updateNetworkStats(boolean notifyListeners) {
        long rxBytes = 0;
        long txBytes = 0;

        if (isLinux()) {
            // Read /proc/net/dev for Linux
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ProcessBuilder("cat", "/proc/net/dev").start().getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(":")) {
                        String[] parts = line.trim().split("\\s+");
                        String iface = parts[0].replace(":", "");
                        if (selectedInterface == null || iface.equals(selectedInterface)) {
                            rxBytes += Long.parseLong(parts[1]); // Received bytes
                            txBytes += Long.parseLong(parts[9]); // Transmitted bytes
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (isWindows()) {
            // Use netstat -e for Windows
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ProcessBuilder("netstat", "-e").start().getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Bytes")) {
                        String[] parts = line.trim().split("\\s+");
                        rxBytes = Long.parseLong(parts[1]); // Received bytes
                        txBytes = Long.parseLong(parts[2]); // Transmitted bytes
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (notifyListeners) {
            // Calculate speeds (bytes per second)
            double downloadSpeed = (rxBytes - lastRxBytes.get()) / (updateInterval / 1000.0) / 1024.0; // KB/s
            double uploadSpeed = (txBytes - lastTxBytes.get()) / (updateInterval / 1000.0) / 1024.0; // KB/s

            totalRxBytes.set(rxBytes);
            totalTxBytes.set(txBytes);

            // Simulate latency and packet loss for demo purposes
            int latency = (int) (Math.random() * 50) + 20; // 20-70ms
            int packetLoss = (int) (Math.random() * 5); // 0-5%

            NetworkData data = new NetworkData(
                downloadSpeed,
                uploadSpeed,
                totalRxBytes.get() / 1024, // Convert to KB
                totalTxBytes.get() / 1024, // Convert to KB
                latency,
                packetLoss
            );

            // Notify all listeners
            for (NetworkDataListener listener : listeners) {
                listener.onNetworkDataUpdated(data);
            }
        }

        lastRxBytes.set(rxBytes);
        lastTxBytes.set(txBytes);
    }

    private boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public interface NetworkDataListener {
        void onNetworkDataUpdated(NetworkData data);
    }
}