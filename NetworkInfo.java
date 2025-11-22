import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class NetworkInfo {
    // Method to get public IP address
    public String getPublicIP() {
        try {
            URL ipApi = new URL("http://ipinfo.io/ip");
            BufferedReader ipReader = new BufferedReader(new InputStreamReader(ipApi.openStream()));
            return ipReader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    // Method to get ISP information
    public String getISP() {
        try {
            String publicIp = getPublicIP();
            URL ispApi = new URL("http://ipinfo.io/" + publicIp + "/org");
            BufferedReader ispReader = new BufferedReader(new InputStreamReader(ispApi.openStream()));
            return ispReader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}