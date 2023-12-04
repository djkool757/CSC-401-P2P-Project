import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String serverHost;
    private int serverPort;
    private int uploadPort;
    private Socket clientSocket;
    private String hostname;

    public Client(String serverHost, int serverPort, int uploadPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.uploadPort = uploadPort;
        this.hostname = getHostName();
    }

    private String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Modified to align with the protocol
    public void addRfcFromFile(String filePath) {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(new File(filePath)))) {
            String firstLine = fileReader.readLine().trim();
            String[] parts = firstLine.split(":");
            String rfcNumber = parts[0].replace("RFC", "").trim();
            String title = parts[1].trim();
            sendAddRequest(rfcNumber, title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void joinP2PServer() {
        try {
            clientSocket = new Socket(serverHost, serverPort);
            System.out.println("Connected to server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Modified to align with the protocol
    public String sendAddRequest(String rfcNumber, String title) {
        String request = formatRequest("ADD", rfcNumber, title);
        return sendRequest(request);
    }

    // Modified to align with the protocol
    public String sendLookupRequest(String rfcNumber, String title) {
        String request = formatRequest("LOOKUP", rfcNumber, title);
        return sendRequest(request);
    }

    // Modified to align with the protocol
    public String sendListRequest() {
        String request = formatRequest("LIST", "ALL", null);
        return sendRequest(request);
    }

    public void executeCommand() {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                System.out.print("Enter command (ADD, LOOKUP, LIST, GET, EXIT): ");
                String command = consoleReader.readLine().trim().toUpperCase();

                if ("ADD".equals(command)) {
                    System.out.print("Enter RFC number: ");
                    String number = consoleReader.readLine().trim();
                    System.out.print("Enter RFC title: ");
                    String title = consoleReader.readLine().trim();
                    sendAddRequest(number, title);
                } else if ("LOOKUP".equals(command)) {
                    System.out.print("Enter RFC number to lookup: ");
                    String rfcNumber = consoleReader.readLine().trim();
                    System.out.print("Enter title of the RFC: ");
                    String title = consoleReader.readLine().trim();
                    sendLookupRequest(rfcNumber, title);
                } else if ("LIST".equals(command)) {
                    sendListRequest();
                } else if ("GET".equals(command)) {
                    System.out.print("Enter RFC number to get: ");
                    String getRfcNumber = consoleReader.readLine().trim();
                    // Assuming sendGetRequest needs to be implemented based on the protocol
                    // sendGetRequest(getRfcNumber);
                    sendGet(clientSocket, getRfcNumber);
                } else if ("EXIT".equals(command)) {
                    System.out.println("Exiting...");
                    return;
                } else {
                    System.out.println("Invalid command. Please try again.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Existing methods remain unchanged

    private void sendGet(Socket s, String getRfcNumber) {
        try {
            // Assuming you need to send a GET request to the server
            String request = "GET RFC " + getRfcNumber + " P2P-CI/1.0\r\n\r\n";
            PrintWriter writer = new PrintWriter(s.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                // Send the GET request to the server
                writer.write(request);
                writer.flush();
    
                // Read and print the server's response
                String responseLine;
                while ((responseLine = reader.readLine()) != null) {
                    System.out.println(responseLine);
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    private String formatRequest(String method, String rfcNumber, String title) {
        String requestLine = String.format("%s RFC %s P2P-CI/1.0", method, rfcNumber);
        String hostLine = String.format("Host: %s", hostname);
        String portLine = String.format("Port: %d", uploadPort);
        String titleLine = (title != null) ? String.format("Title: %s", title) : "";
        return requestLine + "\r\n" + hostLine + "\r\n" + portLine + "\r\n" + titleLine + "\r\n\r\n";
    }

    private String sendRequest(String message) {
        try {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            writer.println(message);

            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }

            String response = responseBuilder.toString().trim();
            System.out.println("Response from server:");
            System.out.println(response);

            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your peer port: ");
        Client peer = new Client("localhost", 7734, scanner.nextInt());
        peer.joinP2PServer();
        peer.executeCommand();
    }
}
