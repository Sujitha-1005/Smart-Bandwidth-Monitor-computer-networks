import java.io.Serializable;
import java.time.LocalDateTime;

public class NetworkData implements Serializable {
    private static final long serialVersionUID = 1L;

    private double downloadSpeed; // in KB/s
    private double uploadSpeed; // in KB/s
    private long totalDownloaded; // in KB
    private long totalUploaded; // in KB
    private int latency; // in ms
    private int packetLoss; // percentage
    private LocalDateTime timestamp;

    public NetworkData(double downloadSpeed, double uploadSpeed, long totalDownloaded,
                       long totalUploaded, int latency, int packetLoss) {
        this.downloadSpeed = downloadSpeed;
        this.uploadSpeed = uploadSpeed;
        this.totalDownloaded = totalDownloaded;
        this.totalUploaded = totalUploaded;
        this.latency = latency;
        this.packetLoss = packetLoss;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public double getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(double downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public double getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(double uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

    public long getTotalDownloaded() {
        return totalDownloaded;
    }

    public void setTotalDownloaded(long totalDownloaded) {
        this.totalDownloaded = totalDownloaded;
    }

    public long getTotalUploaded() {
        return totalUploaded;
    }

    public void setTotalUploaded(long totalUploaded) {
        this.totalUploaded = totalUploaded;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public int getPacketLoss() {
        return packetLoss;
    }

    public void setPacketLoss(int packetLoss) {
        this.packetLoss = packetLoss;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}