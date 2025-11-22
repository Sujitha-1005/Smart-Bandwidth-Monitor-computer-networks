import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class BandwidthMonitorServer {
    private static final int PORT = 9999;
    private static final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Bandwidth Monitor Server starting on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverSocket.setReuseAddress(true);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                String clientId = clientSocket.getInetAddress().getHostAddress();
                clients.put(clientId, clientHandler);

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private boolean running = true;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                this.output = new ObjectOutputStream(socket.getOutputStream());
                this.input = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (running) {
                    Object message = input.readObject();
                    if (message instanceof NetworkData) {
                        NetworkData data = (NetworkData) message;
                        System.out.println("Received from " + clientSocket.getInetAddress().getHostAddress() +
                                ": Download=" + data.getDownloadSpeed() + "KB/s, Upload=" +
                                data.getUploadSpeed() + "KB/s");

                        // Echo back the data (or you could process it and send different data)
                        output.writeObject(data);
                        output.flush();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
                clients.remove(clientSocket.getInetAddress().getHostAddress());
                running = false;
            } finally {
                closeConnection();
            }
        }

        public void closeConnection() {
            running = false;
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}