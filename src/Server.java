import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private String host = "0.0.0.0";
    private int port = 7734;
    private ServerSocket serverSocket;
    private List<Peer> peersList;
    private List<RFC> rfcIndex;
    private ExecutorService executor;

    public Server() {
        this.peersList = new CopyOnWriteArrayList<>();
        this.rfcIndex = new CopyOnWriteArrayList<>();
        this.executor = Executors.newFixedThreadPool(10);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
            System.out.println("Server is listening on " + host + " and port: " + port);
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            while (true) {
                StringBuilder requestBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    requestBuilder.append(line).append("\r\n");
                }

                String request = requestBuilder.toString();
                System.out.println("Request from client:" + request);

                Map<String, String> headers = parseRequest(request);

                if (headers.get("Method") == null) {
                    System.out.println("Invalid request: " + request);
                    writer.write("P2P-CI/1.0 400 Bad Request\r\n\r\n");
                    writer.flush();
                    continue;
                }
                String method = request.split(" ")[0];

                String response;
                switch (method) {
                    case "ADD":
                    String rfcNumber = request.split(" ")[2];
                        response = handleAdd(rfcNumber, headers);
                        break;
                    case "LOOKUP":
                        rfcNumber = headers.get("RFCNumber");
                        response = handleLookup(rfcNumber, headers);
                        break;
                    case "LIST":
                        response = handleList(headers);
                        break;
                    case "GET":
                        rfcNumber = headers.get("RFCNumber");
                        response = handleGet(rfcNumber);
                        break;
                    default:
                        continue;
                }
                writer.write(response);
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clientSocket.close();
        }
    }

    Map<String, String> parseRequest(String request) {
        Map<String, String> headers = new HashMap<>();
        String[] lines = request.split("\r\n");

        String[] firstLineParts = lines[0].split(" ");
        headers.put("Method", firstLineParts[0]);

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

    String handleGet(String rfcNumber) {
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

        if (peersList.isEmpty()) {
            peersList.add(new Peer(host, Integer.parseInt(port)));
        } else {
            peersList.add(new Peer(host, Integer.parseInt(port)));
        }

        rfcIndex.add(new RFC(rfcNum, title, host));

        return "P2P-CI/1.0 200 OK\r\n\r\nRFC " + rfcNumber + " " + title + " " + host + " " + port + "\r\n";
    }

    String handleLookup(String rfcNumber, Map<String, String> headers) {
        if (rfcNumber == null) {
            return "P2P-CI/1.0 400 Bad Request\r\n\r\n";
        }

        try {
            int rfcNum = Integer.parseInt(rfcNumber);
            if (rfcIndex.isEmpty()) {
                return "P2P-CI/1.0 404 Not Found\r\n\r\n";
            }

            for (RFC curr : rfcIndex) {
                if (curr.getRfcNumber() == rfcNum) {
                    String host = curr.getPeerHostname();
                    Peer p = getPeerByHost(host);
                    if (p != null) {
                        String response = "P2P-CI/1.0 200 OK\r\n\r\n" +
                                "RFC " + rfcNum + " " + curr.getTitle() + " " +
                                host + " " + p.getUploadPort() + "\r\n";
                        return response;
                    }
                }
            }
            return "P2P-CI/1.0 404 Not Found\r\n\r\n";
        } catch (NumberFormatException e) {
            return "P2P-CI/1.0 400 Bad Request\r\n\r\n";
        }
    }

    String handleList(Map<String, String> headers) {
        StringBuilder response = new StringBuilder("P2P-CI/1.0 200 OK\r\n\r\n");

        for (RFC curr : rfcIndex) {
            Peer peer = getPeerByHost(curr.getPeerHostname());
            if (peer != null) {
                response.append("RFC ").append(curr.getRfcNumber()).append(" ").append(curr.getTitle()).append(" ")
                        .append(curr.getPeerHostname()).append(" ").append(peer.getUploadPort()).append("\r\n");
            }
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
