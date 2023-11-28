import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.io.*;
import java.net.Socket;

public class ServerTest {
    private Server server;
    private Socket clientSocket;
    private BufferedReader inputReader;
    private String input;

    @Before
    public void setup() {
        server = new Server();
        clientSocket = new Socket();
        server.new ClientHandler(clientSocket);
    }

    private void setInput(String input) {
        this.input = input;
        this.inputReader = new BufferedReader(new StringReader(input));
    }

    @Test
    public void testHandleAdd() throws IOException {
        setInput("ADD\r\n" +
                "RFC 1234 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n" +
                "Title: Sample RFC\r\n");

        server.handleAdd(inputReader);

        assertEquals(1, server.rfcIndex.size());
        assertEquals(1, server.peerMap.size());
        assertEquals(-1, server.rfcIndex.get(0).getNumber());
        assertEquals("Sample", server.rfcIndex.get(0).getTitle());
        assertEquals("localhost", server.rfcIndex.get(0).getHostname());
        assertEquals("localhost", server.peerMap.get(0).get(0).getHostname());
        }

    @Test
    public void testHandleLookupFound() throws IOException {
        setInput("LOOKUP\r\n" +
                "RFC 1234 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n" +
                "Title: Sample\r\n");

        server.rfcIndex.add ( new RFC(1234, "Sample RFC", "localhost"));

        server.handleLookup(inputReader);

        String response = inputReader.readLine();
        assertEquals("P2P-CI/1.0 200 OK", response);
        response = inputReader.readLine();
        assertEquals("RFC 1234 Sample RFC localhost 12345", response);
    }

    @Test
    public void testHandleLookupNotFound() throws IOException {
        setInput("LOOKUP\r\n" +
                "RFC 1234 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n" +
                "Title: Sample RFC\r\n");

        server.handleLookup(inputReader);

        String response = inputReader.readLine();
        assertEquals("P2P-CI/1.0 404 Not Found", response);
    }

    @Test
    public void testHandleList() throws IOException {
        server.handleList();

        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String response = reader.readLine();
        assertEquals("P2P-CI/1.0 200 OK", response);
        response = reader.readLine();
        assertEquals("RFC 1234 Sample RFC 1 localhost", response);
        response = reader.readLine();
        assertEquals("RFC 5678 Sample RFC 2 localhost", response);
    }
}