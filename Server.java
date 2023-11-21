import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Server {
    private LinkedList<Peer> peerList;
    private LinkedList<RFC> rfcIndex;

    public Server() {
        this.peerList = new LinkedList<>();
        this.rfcIndex = new LinkedList<>();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(7734)) {
            System.out.println("Server is listening on port 7734...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection accepted.");

                // Create a new thread to handle the communication with the client
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class to handle communication with a client
    public class ClientHandler implements Runnable {
        private static Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            Server.ClientHandler.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
            ) {
                // Assuming you have the following classes defined: RFC, Peer
                // Inside the ClientHandler class's run() method
String method = reader.readLine();

switch (method) {
    case "ADD":
    handleAdd(reader);
        break;
    case "LOOKUP":
    handleLookup(reader);
        break;
    case "LIST":
    // list all the RFCs
    handleList();
} 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

private void handleAdd(BufferedReader reader) throws IOException {
    // Parse RFC number
    String[] rfcLine = reader.readLine().split(" ");
    int rfcNumber = Integer.parseInt(rfcLine[2]);

    // Skip P2P-CI/1.0 line
    reader.readLine();

    // Parse Host line
    String[] hostLine = reader.readLine().split(" ");
    String hostname = hostLine[1];

    // Parse Port line
    String[] portLine = reader.readLine().split(" ");
    int port = Integer.parseInt(portLine[1]);

    // Parse Title line
    String[] titleLine = reader.readLine().split(" ");
    String title = titleLine[1];

    // Create RFC and Peer objects
    RFC rfc = new RFC(rfcNumber, title, hostname);
    Peer peer = new Peer(hostname, port);

    // Add records to the lists
    rfcIndex.add(rfc);
    peerList.add(peer);
}

private void handleLookup(BufferedReader reader) throws IOException {
    // Parse RFC number
    String[] rfcLine = reader.readLine().split(" ");
    int rfcNumber = Integer.parseInt(rfcLine[2]);

    // Skip P2P-CI/1.0 line
    reader.readLine();

    // Parse Host line
    String[] hostLine = reader.readLine().split(" ");
    String hostname = hostLine[1];

    // Parse Port line
    String[] portLine = reader.readLine().split(" ");
    int port = Integer.parseInt(portLine[1]);

    // Parse Title line
    String[] titleLine = reader.readLine().split(" ");
    String title = titleLine[1];

    // Create RFC and Peer objects
    RFC rfc = new RFC(rfcNumber, title, hostname);
    Peer peer = new Peer(hostname, port);

    // Check if RFC is present in the index
    boolean found = false;
    for (RFC r : rfcIndex) {
        if (r.number == rfc.number) {
            found = true;
            break;
        }
    }

    // Send response to client
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ClientHandler.clientSocket.getOutputStream()));
    if (found) {
        writer.write("P2P-CI/1.0 200 OK\r\n");
        writer.write("RFC " + rfc.number + " " + rfc.title + " " + rfc.hostname + " " + peer.port + "\r\n");
    } else {
        writer.write("P2P-CI/1.0 404 Not Found\r\n");
    }
    writer.flush();
}

private void handleList() throws IOException {
    // Send response to client
    // send all the RFCs to the client
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ClientHandler.clientSocket.getOutputStream()));
    writer.write("P2P-CI/1.0 200 OK\r\n");
    for (RFC r : rfcIndex) {
        writer.write("RFC " + r.number + " " + r.title + " " + r.hostname);
    }
    writer.flush();
}
private class Peer {
    public String hostname;
    public int port;
    public Peer(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
}

private class RFC {
    private int number;
    private String title;
    private String hostname;

    public RFC(int number, String title, String hostname) {
        this.number = number;
        this.title = title;
        this.hostname = hostname;
    }
}
}
