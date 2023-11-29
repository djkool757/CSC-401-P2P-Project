import java.io.*;
import java.net.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    private static ArrayList<RFC> rfcIndex;
    private static int serverPort = 7734;
    private static int peerPort;
    private static String serverHost;
    private ServerSocket uploadServerSocket;

    public Client(String serverHost, int peerPort, ArrayList<RFC> rfcIndex) throws IOException {
        Client.serverHost = serverHost;
        Client.rfcIndex = rfcIndex;
        uploadServerSocket = new ServerSocket(peerPort);
        new Thread(this::runUploadServer).start();
    }

    private static void joinP2PSystem(String serverIP) throws UnknownHostException, IOException {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter peer port: ");
            int peerPort = Integer.parseInt(scanner.nextLine());
            rfcIndex = new ArrayList<>();
            // Assuming the user provides information about RFCs
            System.out.print("Enter number of RFCs: ");
            int numRFCs = Integer.parseInt(scanner.nextLine());
            if (numRFCs <= 0) {
                System.out.println("You must enter at least one RFC");
                return;
            }
            for (int i = 0; i < numRFCs; i++) {
                System.out.print("Enter RFC number for RFC " + (i + 1) + ": ");
                int rfcNumber = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter RFC title for RFC " + (i + 1) + ": ");
                String rfcTitle = scanner.nextLine();
                // Assuming you have an RFC constructor that takes number, title, and hostname
                RFC rfc = new RFC(rfcNumber, rfcTitle, InetAddress.getLocalHost().getHostName());
                rfcIndex.add(rfc);
            }
            new Client(InetAddress.getLocalHost().getHostName(), peerPort, rfcIndex);
            try (Socket socket = new Socket(serverIP, serverPort))
             {
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }

    private void runUploadServer() {
        try {
            while (true) {
                Socket clientSocket = uploadServerSocket.accept();
                // Handle the incoming connection in a new thread
                new Thread(() -> handleGetRequest(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void handleGetRequest(Socket clientSocket) {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            // Read the request line
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty() || !requestLine.contains("GET")) {
                // Bad request
                out.println("400 Bad Request");
                return;
            } else if (!requestLine.contains("P2P-CI/1.0")) {
                // Bad request
                out.println("505 P2P-CI Version Not Supported");
                return;
            } else {
                out.println("200 OK");
            }

            // Extract the RFC number from the request line
            int rfcNumber = Integer.parseInt(requestLine.split(" ")[2]);
            // Send the RFC to the client
            sendRFC(out, rfcNumber);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRFC(PrintWriter out, int rfcNumber) {
        RFC rfc = getRFCByNumber(rfcNumber);
        if (rfc == null) {
            // RFC not found
            out.println("404 Not Found");
            return;
        }
        // RFC found
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z");
        String date = ZonedDateTime.now().format(formatter);
        out.println("P2P-CI/1.0 200 OK" + "\n" + "Date: " + date + "\n" + "OS: " + System.getProperty("os.name")
                + "\n" + System.getProperty("os.version") + "\n" + "Last-Modified: " + date + "\n" + "Content-Length: "
                + "12345" + "\n" + "Content-Type: text/text" + "\n" + rfc.getTitle());
    }

    private RFC getRFCByNumber(int rfcNumber) {
        for (RFC rfc : rfcIndex) {
            if (rfc.getRfcNumber() == rfcNumber) {
                return rfc;
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        if (args.length!= 1) {
            System.out.println("Usage: java Client <server IP address>");
            System.exit(1);
        }
        String givenIP = args[0];
        Scanner s = new Scanner(System.in);

        joinP2PSystem(givenIP);
        
    }
}
