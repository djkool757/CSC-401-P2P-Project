import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private String serverHost;
    private int serverPort = 7734;
    private String peerHostname;
    private int peerPort;

    public Client(String peerHostname, int peerPort) {
        this.peerHostname = peerHostname;
        this.peerPort = peerPort;
    }

    public void joinP2PSystem() {
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send join request to the server
            out.println("JOIN " + peerHostname + " " + peerPort);

            // Receive and process the server's response
            String response = in.readLine();
            processServerResponse(response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadRFC(int rfcNumber) {
        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send download request to the server
            out.println("DOWNLOAD " + rfcNumber + " " + peerHostname + " " + peerPort);

            // Receive and process the server's response
            String response = in.readLine();
            processServerResponse(response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add other methods for sending LIST, LOOKUP, etc. requests
    
    private void processServerResponse(String response) {
        // Process the server's response here
        System.out.println("Server Response: " + response);
    }

    public static void main(String[] args) {
        // Create a P2PClient instance and perform actions
        Client client = new Client( "Peer1", 1234);
        client.joinP2PSystem();
        client.downloadRFC(1234);
    }
}
