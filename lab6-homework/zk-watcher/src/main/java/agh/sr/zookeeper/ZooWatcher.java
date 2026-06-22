package agh.sr.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZooWatcher implements Watcher {

    private static final String ZK_HOSTS = "localhost:2181,localhost:2182,localhost:2183";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String WATCHED_NODE = "/a";

    private ZooKeeper zk;
    private Process externalProcess;
    private String externalCommand;

    private final CountDownLatch connectedLatch = new CountDownLatch(1);

    public ZooWatcher(String externalCommand) {
        this.externalCommand = externalCommand;
    }

    public void connect() throws IOException, InterruptedException {
        System.out.println("[ZK] Łączę z: " + ZK_HOSTS);
        zk = new ZooKeeper(ZK_HOSTS, SESSION_TIMEOUT, this);
        connectedLatch.await();
        System.out.println("[ZK] Połączono.");
    }

    @Override
    public void process(WatchedEvent event) {
        Event.KeeperState state = event.getState();
        Event.EventType type    = event.getType();
        String path             = event.getPath();

        if (type == Event.EventType.None) {
            if (state == Event.KeeperState.SyncConnected) {
                connectedLatch.countDown();
            } else if (state == Event.KeeperState.Disconnected
                    || state == Event.KeeperState.Expired) {
                System.out.println("[ZK] Sesja zakończona/rozłączono.");
            }
            return;
        }

        if (WATCHED_NODE.equals(path)) {
            switch (type) {
                case NodeCreated:
                    System.out.println("[ZK] /a CREATED → uruchamiam aplikację");
                    launchExternalApp();
                    watchNode();
                    break;
                case NodeDeleted:
                    System.out.println("[ZK] /a DELETED → zatrzymuję aplikację");
                    killExternalApp();
                    watchForCreation();
                    break;
                case NodeChildrenChanged:
                    System.out.println("[ZK] /a CHILDREN CHANGED → liczę potomków");
                    showChildCount();
                    watchChildren();
                    break;
                default:
                    watchNode();
                    break;
            }
        }
    }

    private void watchForCreation() {
        try {
            Stat stat = zk.exists(WATCHED_NODE, this);
            if (stat != null) {
                System.out.println("[ZK] /a już istnieje → uruchamiam apkę");
                launchExternalApp();
                watchNode();
            }
        } catch (KeeperException | InterruptedException e) {
            System.err.println("[ZK] Błąd watchForCreation: " + e.getMessage());
        }
    }

    private void watchNode() {
        watchDataAndDelete();
        watchChildren();
    }

    private void watchDataAndDelete() {
        try {
            Stat stat = zk.exists(WATCHED_NODE, this);
            if (stat == null) {
                watchForCreation();
            }
        } catch (KeeperException | InterruptedException e) {
            System.err.println("[ZK] Błąd watchDataAndDelete: " + e.getMessage());
        }
    }

    private void watchChildren() {
        try {
            zk.getChildren(WATCHED_NODE, this);
        } catch (KeeperException.NoNodeException e) {
            watchForCreation();
        } catch (KeeperException | InterruptedException e) {
            System.err.println("[ZK] Błąd watchChildren: " + e.getMessage());
        }
    }

    private void launchExternalApp() {
        if (externalProcess != null && externalProcess.isAlive()) {
            System.out.println("[APP] Aplikacja już działa, pomijam.");
            return;
        }
        try {
            System.out.println("[APP] Uruchamiam: " + externalCommand);
            String[] parts = externalCommand.split("\\s+");
            externalProcess = new ProcessBuilder(parts).inheritIO().start();
        } catch (IOException e) {
            System.err.println("[APP] Nie udało się uruchomić: " + e.getMessage());
            showError("Nie można uruchomić:\n" + externalCommand + "\n\n" + e.getMessage());
        }
    }

    private void killExternalApp() {
        if (externalProcess != null && externalProcess.isAlive()) {
            System.out.println("[APP] Zatrzymuję aplikację.");
            externalProcess.destroyForcibly();
            externalProcess = null;
        } else {
            System.out.println("[APP] Aplikacja i tak nie działa.");
        }
    }

    private void showChildCount() {
        try {
            List<String> children = zk.getChildren(WATCHED_NODE, false);
            int count = children.size();
            System.out.println("[ZK] Aktualna liczba potomków /a: " + count);
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(
                            null,
                            "<html><h2>Węzeł: /a</h2>"
                                    + "<p>Aktualna liczba potomków: <b>" + count + "</b></p>"
                                    + "<p>Dzieci: " + children + "</p></html>",
                            "ZooKeeper — potomkowie /a",
                            JOptionPane.INFORMATION_MESSAGE
                    )
            );
        } catch (KeeperException.NoNodeException e) {
            // /a znikło - ignoruj
        } catch (KeeperException | InterruptedException e) {
            System.err.println("[ZK] Błąd showChildCount: " + e.getMessage());
        }
    }

    public void showTree() {
        StringBuilder sb = new StringBuilder();
        buildTree(WATCHED_NODE, sb, "");
        SwingUtilities.invokeLater(() -> {
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            textArea.setEditable(false);
            JScrollPane scroll = new JScrollPane(textArea);
            scroll.setPreferredSize(new Dimension(500, 400));
            JFrame frame = new JFrame("Drzewo ZooKeeper: /a");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(scroll);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private void buildTree(String path, StringBuilder sb, String indent) {
        try {
            Stat stat = zk.exists(path, false);
            if (stat == null) {
                sb.append(indent).append(path).append(" [nie istnieje]\n");
                return;
            }
            byte[] data = zk.getData(path, false, stat);
            String dataStr = (data != null && data.length > 0) ? new String(data) : "<brak danych>";
            sb.append(indent).append(path).append(" → ").append(dataStr).append("\n");
            for (String child : zk.getChildren(path, false)) {
                buildTree(path + "/" + child, sb, indent + "  ");
            }
        } catch (KeeperException | InterruptedException e) {
            sb.append(indent).append(path).append(" [błąd: ").append(e.getMessage()).append("]\n");
        }
    }

    private void showError(String msg) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, msg, "Błąd", JOptionPane.ERROR_MESSAGE));
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Użycie: ZooWatcher <komenda_graficzna> [tree]");
            System.exit(1);
        }

        String command = args[0];
        boolean showTree = args.length >= 2 && "tree".equalsIgnoreCase(args[1]);

        System.setProperty("org.slf4j.simpleLogger.log.org.apache.zookeeper", "WARN");

        ZooWatcher app = new ZooWatcher(command);
        app.connect();

        if (showTree) app.showTree();

        app.watchForCreation();

        System.out.println("[INFO] Monitoruję /a. Ctrl+C aby wyjść, wpisz 'tree' + Enter aby pokazać drzewo.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[INFO] Zamykam...");
            app.killExternalApp();
            try { app.zk.close(); } catch (InterruptedException ignored) {}
        }));

        Thread inputThread = new Thread(() -> {
            try {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(System.in));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if ("tree".equalsIgnoreCase(line)) {
                        app.showTree();
                    } else if ("quit".equalsIgnoreCase(line) || "exit".equalsIgnoreCase(line)) {
                        System.exit(0);
                    }
                }
            } catch (java.io.IOException ignored) {}
        });
        inputThread.setDaemon(true);
        inputThread.start();

        Thread.currentThread().join();
    }
}