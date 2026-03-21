import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final int PORT = 12345;
    private static final int UDP_BUFFER = 8192;
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static DatagramSocket udpSocket;

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        private String nick;
        private InetAddress udpAddress;
        private int udpPort;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                nick = in.readLine();
                String udpPortLine = in.readLine();

                if (nick == null || nick.isBlank() || udpPortLine == null) {
                    return;
                }

                udpPort = Integer.parseInt(udpPortLine.trim());
                udpAddress = socket.getInetAddress();

                clients.add(this);
                System.out.println("Client connected: " + nick + " UDP=" + udpAddress.getHostAddress() + ":" + udpPort);

                String msg;
                while ((msg = in.readLine()) != null) {
                    if (!msg.isBlank()) {
                        broadcastTcp(nick + " [TCP]: " + msg, this);
                    }
                }
            } catch (Exception e) {
                System.out.println("Connection error for " + nick + ": " + e.getMessage());
            } finally {
                clients.remove(this);
                close();
                System.out.println("Client disconnected: " + nick);
            }
        }

        void sendTcp(String msg) {
            out.println(msg);
        }

        void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void broadcastTcp(String msg, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendTcp(msg);
            }
        }
    }

    private static void broadcastUdp(String msg, InetAddress senderAddr, int senderPort) {
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        for (ClientHandler client : clients) {
            if (client.udpAddress == null || client.udpPort <= 0) {
                continue;
            }
            if (client.udpAddress.equals(senderAddr) && client.udpPort == senderPort) {
                continue;
            }
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length, client.udpAddress, client.udpPort);
                udpSocket.send(packet);
            } catch (IOException e) {
                System.out.println("UDP send error to " + client.nick + ": " + e.getMessage());
            }
        }
    }

    private static void startUdpReceiver() {
        Thread udpThread = new Thread(() -> {
            byte[] buffer = new byte[UDP_BUFFER];
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);

                    String raw = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                    String msg = "[UDP] " + raw;

                    System.out.println("UDP received from " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
                    broadcastUdp(msg, packet.getAddress(), packet.getPort());
                } catch (IOException e) {
                    System.out.println("UDP receiver stopped: " + e.getMessage());
                    break;
                }
            }
        });
        udpThread.setDaemon(true);
        udpThread.start();
    }

    public static void main(String[] args) {
        System.out.println("JAVA TCP,UDP CHAT SERVER on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            udpSocket = new DatagramSocket(PORT);
            startUdpReceiver();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.close();
            }
        }
    }
}