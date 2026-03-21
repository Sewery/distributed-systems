import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    private static final int UDP_BUFFER = 8192;

    private static final String MCAST_ADDR = "239.10.10.10";
    private static final int MCAST_PORT = 12346;

    public static void main(String[] args) {
        BufferedReader consoleIn = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));

        try {
            System.out.print("Podaj nick: ");
            String nick = consoleIn.readLine();
            if (nick == null || nick.isBlank()) {
                System.out.println("Nick nie może być pusty.");
                return;
            }

            InetAddress group = InetAddress.getByName(MCAST_ADDR);

            try (
                    Socket tcpSocket = new Socket(HOST, PORT);
                    PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(tcpSocket.getInputStream(), StandardCharsets.UTF_8));
                    DatagramSocket udpSocket = new DatagramSocket();
                    MulticastSocket mcastSocket = new MulticastSocket(null)
            ) {
                mcastSocket.setReuseAddress(true);
                mcastSocket.bind(new InetSocketAddress(MCAST_PORT));
                mcastSocket.joinGroup(group);

                System.out.println("Połączono TCP + UDP + Multicast.");
                System.out.println("Komendy: U (UDP), M (Multicast), cokolwiek innego = TCP");

                out.println(nick);
                out.println(udpSocket.getLocalPort());

                Thread tcpReader = new Thread(() -> {
                    try {
                        String msg;
                        while ((msg = in.readLine()) != null) {
                            System.out.println(msg);
                        }
                    } catch (IOException e) {
                        System.out.println("TCP rozłączono.");
                    }
                });
                tcpReader.setDaemon(true);
                tcpReader.start();

                Thread udpReader = new Thread(() -> {
                    byte[] buffer = new byte[UDP_BUFFER];
                    while (true) {
                        try {
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            udpSocket.receive(packet);
                            String msg = new String(
                                    packet.getData(),
                                    packet.getOffset(),
                                    packet.getLength(),
                                    StandardCharsets.UTF_8
                            );
                            System.out.println(msg);
                        } catch (IOException e) {
                            System.out.println("UDP rozłączono.");
                            break;
                        }
                    }
                });
                udpReader.setDaemon(true);
                udpReader.start();

                Thread mcastReader = new Thread(() -> {
                    byte[] buffer = new byte[UDP_BUFFER];
                    while (true) {
                        try {
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            mcastSocket.receive(packet);
                            String msg = new String(
                                    packet.getData(),
                                    packet.getOffset(),
                                    packet.getLength(),
                                    StandardCharsets.UTF_8
                            );
                            System.out.println(msg);
                        } catch (IOException e) {
                            System.out.println("Multicast rozłączono.");
                            break;
                        }
                    }
                });
                mcastReader.setDaemon(true);
                mcastReader.start();

                String line;
                while ((line = consoleIn.readLine()) != null) {
                    if (line.equals("U")) {
                        System.out.println("Wpisz treść UDP:");
                        String multimedia = consoleIn.readLine();
                        if (multimedia != null && !multimedia.isBlank()) {
                            String udpMsg = nick + " [UDP]: " + multimedia;
                            byte[] data = udpMsg.getBytes(StandardCharsets.UTF_8);
                            DatagramPacket packet = new DatagramPacket(
                                    data, data.length, InetAddress.getByName(HOST), PORT
                            );
                            udpSocket.send(packet);
                        }
                    } else if (line.equals("M")) {
                        System.out.println("Wpisz treść MULTICAST:");
                        String multimedia = consoleIn.readLine();
                        if (multimedia != null && !multimedia.isBlank()) {
                            String mcastMsg = nick + " [MCAST]: " + multimedia;
                            byte[] data = mcastMsg.getBytes(StandardCharsets.UTF_8);
                            DatagramPacket packet = new DatagramPacket(
                                    data, data.length, group, MCAST_PORT
                            );
                            mcastSocket.send(packet);
                        }
                    } else {
                        out.println(line);
                    }
                }

                mcastSocket.leaveGroup(group);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}