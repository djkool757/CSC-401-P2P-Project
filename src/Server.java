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

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(7734)) {
            System.out.println("Server is listening on port 7734...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket cSocket) {
            this.clientSocket = cSocket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(clientSocket.getOutputStream()))) {
                 handleNewClient(clientSocket);
                String requestLine = reader.readLine();
                if (requestLine != null && requestLine.startsWith("INIT")) {
                    handleInitialRFC(reader);
                }
                else if (requestLine != null && requestLine.startsWith("ADD")) {
                    handleAdd(clientSocket, reader);
                } else if (requestLine != null && requestLine.startsWith("LOOKUP")) {
                    handleLookup(reader, writer);
                } else if (requestLine != null && requestLine.startsWith("LIST")) {
                    handleList(writer);
                } else {
                    System.err.println("Invalid request: " + requestLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void handleNewClient(Socket client) {
            // Get the client's hostname
            String hostname = client.getInetAddress().getHostName();
            // Log the hostname
            System.out.println("New client connected: " + hostname);
            // Add the hostname to the peerList
            peerList.add(new Peer(hostname, client.getPort()));
        }
    }

    /**
     * Handles the addition of a new RFC and Peer to the server's index and list.
     * 
     * @param reader the BufferedReader used to read the input from the client
     * @throws IOException if an I/O error occurs while reading the input
     */
    private void handleAdd(Socket clientSocket, BufferedReader reader) throws IOException {
        int rfcNumber = Integer.parseInt(reader.readLine().split(" ")[3]);
        String hostname = reader.readLine().split(" ")[0];
        int port = Integer.parseInt(reader.readLine().split(" ")[1]);
        String title = reader.readLine().split(" ")[4];

        RFC rfcInfo = new RFC(rfcNumber, title, hostname);
        Peer peerInfo = new Peer(hostname, port);

        // Add records to the linked lists
        rfcIndex.add(rfcInfo);
        // Send response to client
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        writer.write("P2P-CI/1.0 200 OK\r\n");
        writer.write("RFC " + rfcInfo.getRfcNumber() + " " + rfcInfo.getTitle() + " " + rfcInfo.getPeerHostname() +
                " " + peerInfo.getUploadPort() + "\r\n");
        writer.flush();
    }

    private void handleInitialRFC(BufferedReader reader) throws IOException {
        int rfcNumber = Integer.parseInt(reader.readLine().split(" ")[3]);
        String hostname = reader.readLine().split(" ")[0];
        int port = Integer.parseInt(reader.readLine().split(" ")[1]);
        String title = reader.readLine().split(" ")[4];

        RFC rfcInfo = new RFC(rfcNumber, title, hostname);
        Peer peerInfo = new Peer(hostname, port);
        rfcIndex.add(rfcInfo);
        peerList.add(peerInfo);
    }

    private void handleLookup(BufferedReader reader, BufferedWriter writer) throws IOException {
        String requestLine = reader.readLine(); // Read the LOOKUP request line
        String[] parts = requestLine.split(" ");

        int rfcNumber = Integer.parseInt(parts[2]);
        String title = reader.readLine().split(" ")[1];

        // Check if RFC is present in the index
        boolean found = false;
        for (RFC r : rfcIndex) {
            if (r.getRfcNumber() == rfcNumber && r.getTitle().equals(title)) {
                // Send response to client
                writer.write("P2P-CI/1.0 200 OK\r\n");
                writer.write("RFC " + r.getRfcNumber() + " " + r.getTitle() + " " + r.getPeerHostname() +
                        " " + getPeerUploadPort(r.getPeerHostname()) + "\r\n");
                writer.flush();
                found = true;
                break;
            }
        }

        // If the RFC is not found, send a 404 Not Found response
        if (!found) {
            writer.write("P2P-CI/1.0 404 Not Found\r\n");
            writer.flush();
        }
    }

    private void handleList(BufferedWriter writer) throws IOException {
        // Send response to client
        writer.write("P2P-CI/1.0 200 OK\r\n");
        for (RFC r : rfcIndex) {
            writer.write("RFC " + r.getRfcNumber() + " " + r.getTitle() + " " + r.getPeerHostname() + " " +
                    getPeerUploadPort(r.getPeerHostname()) + "\r\n");
        }
        writer.flush();
    }

    private int getPeerUploadPort(String peerHostname) {
        // Find and return the upload port of the specified peer
        for (Peer peer : peerList) {
            if (peer.getHostname().equals(peerHostname)) {
                return peer.getUploadPort();
            }
        }
        return -1; // Return -1 if peer not found (handle appropriately in your application)
    }
}
