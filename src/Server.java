import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private String host = "0.0.0.0"; // BE OMNIPRESENT
    private int port = 7734;
    private ServerSocket serverSocket;
    LinkedList<Peer> peersList; // Client linked list
    LinkedList<RFC> rfcIndex;  // RFC linked list
    private ExecutorService executor;

    public Server() {
        this.peersList = new LinkedList<>();
        this.rfcIndex = new LinkedList<>();
        this.executor = Executors.newFixedThreadPool(10); // Adjust the pool size as needed
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
            System.out.println("Server listening on " + host + ":" + port);
            while (true) {
                Socket client = serverSocket.accept();
                executor.execute(new ClientHandler(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                handleClient(clientSocket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        while (true) {
            try {
                StringBuilder requestBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    requestBuilder.append(line).append("\r\n");
                }
                String request = requestBuilder.toString();

                if (request.isEmpty()) {
                    System.out.println("Client disconnected");
                    break; // Client disconnected
                }

                Map<String, String> headers = parseRequest(request);
                String method = headers.get("Method");
                String rfcNumber = headers.get("RFCNumber");
                String version = headers.get("Version");

                String response;
                if (method.equals("ADD")) {
                        response = handleAdd(rfcNumber, headers);
                        break;
                    }
                    else if (method.equals("LOOKUP")) {
                    response = handleLookup(rfcNumber, headers);
                    break;
                    }
                    else if (method.equals("LIST")) {
                        response = handleList(headers);
                        break;
                    }
                    else if (method.equals("GET")) {
                        response = handleGet(rfcNumber, headers);
                        break;
                    }
                    else {
                        System.out.println("Unsupported method: " + method);
                        response = "P2P-CI/1.0 400 Bad Request\r\n\r\n";
                    }
                    writer.write(response);
                    writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
                break;
        }
    }
        reader.close();
        writer.close();
        clientSocket.close();
    }

    Map<String, String> parseRequest(String request) {
        Map<String, String> headers = new HashMap<>();
        String[] lines = request.split("\r\n");

        String[] firstLineParts = lines[0].split(" ");
        headers.put("Method", firstLineParts[0]);

        // RFC number is in the second position for ADD, LOOKUP, and GET
        if (firstLineParts.length > 2) {
            headers.put("RFCNumber", firstLineParts[2]);
        }

        if (firstLineParts.length > 3) {
            headers.put("Version", firstLineParts[3]);
        }

        for (int i = 1; i < lines.length; i++) {
            String[] headerParts = lines[i].split(": ");
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }

        return headers;
    }

    String handleGet(String rfcNumber, Map<String, String> headers) {
        // Assume rfcNumber is valid and is an integer
        try {
            String rfcPath = "rfc" + rfcNumber + ".txt";
            BufferedReader fileReader = new BufferedReader(new FileReader(rfcPath));
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = fileReader.readLine()) != null) {
                data.append(line).append("\n");
            }
            fileReader.close();

            String response = "P2P-CI/1.0 200 OK\r\n" +
                    "Date: " + getDate() + "\r\n" +
                    "OS: " + getOS() + "\r\n" +
                    "Content-Length: " + data.length() + "\r\n" +
                    "Content-Type: text/plain\r\n\r\n" +
                    data.toString();
            return response;
        } catch (FileNotFoundException e) {
            System.out.println("P2P-CI/1.0 404 Not Found\r\n\r\n");
            return "P2P-CI/1.0 404 Not Found\r\n\r\n";
        } catch (IOException e) {
            System.out.println("Error reading RFC " + rfcNumber + ": " + e.getMessage());
            return "P2P-CI/1.0 500 Internal Server Error\r\n\r\n";
        }
    }

    String handleAdd(String rfcNumber, Map<String, String> headers) {
        String host = headers.get("Host");
        String port = headers.get("Port");
        String title = headers.get("Title");

        int rfcNum = Integer.parseInt(rfcNumber);

        if (peersList.size() == 0) {
            peersList.addFirst(new Peer(host, Integer.parseInt(port)));
        } else {
            peersList.add(new Peer(host, Integer.parseInt(port)));
        }

        rfcIndex.addFirst(new RFC(rfcNum, title, host));

        return "P2P-CI/1.0 200 OK\r\n\r\nRFC " + rfcNumber + " " + title + " " + host + " " + port + "\r\n";
    }

    String handleLookup(String rfcNumber, Map<String, String> headers) {
        if (rfcNumber == null) {
            return "P2P-CI/1.0 400 Bad Request\r\n\r\n";
        }

        try {
            int rfcNum = Integer.parseInt(rfcNumber);
            if (rfcIndex.size() == 0) {
                return "P2P-CI/1.0 404 Not Found\r\n\r\n";
            }
            RFC curr = rfcIndex.getFirst();
            StringBuilder response = new StringBuilder("P2P-CI/1.0 404 Not Found\r\n\r\n");
            while (curr != null) {
                String[] record = headers.values().toArray(new String[0]);
                if (Integer.parseInt(record[0]) == rfcNum) {
                    String host = record[2];
                    Peer p = getPeerByHost(host);
                    if (p != null) {
                        String[] peerData = peersList.toArray(new String[0]);
                        response = new StringBuilder("P2P-CI/1.0 200 OK\r\n\r\n");
                        response.append("RFC ").append(rfcNum).append(" ").append(record[1]).append(" ")
                                .append(host).append(" ").append(peerData[1]).append("\r\n");
                        break;
                    }
                }
                int index = rfcIndex.indexOf(curr);
                curr = (index == -1 || index == rfcIndex.size() - 1) ? null : rfcIndex.get(index + 1);
            }
            return response.toString();
        } catch (NumberFormatException e) {
            return "P2P-CI/1.0 400 Bad Request\r\n\r\n";
        }
    }

    String handleList(Map<String, String> headers) {
        StringBuilder response = new StringBuilder("P2P-CI/1.0 200 OK\r\n\r\n");

        RFC curr = rfcIndex.getFirst();
        while (curr != null) {
            String[] record = headers.values().toArray(new String[0]);
            Peer peer = getPeerByHost(record[2]);

            if (peer != null) {
                  String[] peerData = peersList.toArray(new String[0]);
                response.append("RFC ").append(record[0]).append(" ").append(record[1]).append(" ")
                        .append(record[2]).append(" ").append(peerData[1]).append("\r\n");
            } 
            int index = rfcIndex.indexOf(curr);
            curr = (index == -1 || index == rfcIndex.size() - 1) ? null : rfcIndex.get(index + 1);
        }
        return response.toString();
    }

    private String getDate() {
        return java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
    }

    private String getOS() {
        return System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");
    }

    private Peer getPeerByHost(String host) {
        for (Peer peer : peersList) {
            if (peer.getHostname().equals(host)) {
                return peer;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
