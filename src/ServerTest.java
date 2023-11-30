import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.io.*;
import java.net.Socket;
import java.util.Map;

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
    public void testHandleGetValidRFC() throws IOException {
        setInput("GET\r\n" +
                "RFC 1234 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n");

        // Create a temporary file with the RFC content
        File tempFile = File.createTempFile("rfc1234", ".txt");
        FileWriter writer = new FileWriter(tempFile);
        writer.write("Sample RFC content");
        writer.close();

        // Mock the FileReader to read from the temporary file
        Mockito.when(new FileReader("rfc1234.txt")).thenReturn(new FileReader(tempFile));

        String expectedResponse = "P2P-CI/1.0 200 OK\r\n" +
                "Date: <current date>\r\n" +
                "OS: <current OS>\r\n" +
                "Content-Length: 19\r\n" +
                "Content-Type: text/plain\r\n\r\n" +
                "Sample RFC content";

        String actualResponse = server.handleGet("1234", inputReader);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testHandleGetInvalidRFC() throws IOException {
        setInput("GET\r\n" +
                "RFC 5678 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n");

        String expectedResponse = "P2P-CI/1.0 404 Not Found\r\n\r\n";
        Map<String, String> headers = server.parseRequest(input);

        String actualResponse = server.handleGet("5679", headers);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testHandleAdd() throws IOException {
        setInput("ADD\r\n" +
                "RFC 1234 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n" +
                "Title: Sample RFC\r\n");
        Map<String, String> headers = server.parseRequest(input);

        server.handleAdd("1234", headers);

        assertEquals(1, server.rfcIndex.size());
        assertEquals(1, server.peersList.size());
        assertEquals(1234, server.rfcIndex.get(0).getRfcNumber());
        assertEquals("Sample RFC", server.rfcIndex.get(0).getTitle());
        assertEquals("localhost", server.rfcIndex.get(0).getPeerHostname());
        assertEquals("localhost", server.peersList.get(0).getHostname());
        assertEquals(12345, server.peersList.get(0).getUploadPort());
    }

    @Test
    public void testHandleLookupFound() throws IOException {
        setInput("LOOKUP\r\n" +
                "RFC 1234 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n" +
                "Title: Sample\r\n");

        server.rfcIndex.add(new RFC(1234, "Sample RFC", "localhost"));

        String expectedResponse = "P2P-CI/1.0 200 OK\r\n\r\n" +
                "RFC 1234 Sample RFC localhost 12345";
        Map<String, String> headers = server.parseRequest(input);

        String actualResponse = server.handleLookup("1234", headers);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testHandleLookupNotFound() throws IOException {
        setInput("LOOKUP\r\n" +
                "RFC 1234 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n" +
                "Title: Sample RFC\r\n");

        String expectedResponse = "P2P-CI/1.0 404 Not Found\r\n\r\n";
        Map<String, String> headers = server.parseRequest(input);
        String actualResponse = server.handleLookup("1234", headers);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testHandleList() throws IOException {
        server.rfcIndex.add(new RFC(1234, "Sample RFC 1", "localhost"));
        server.rfcIndex.add(new RFC(5678, "Sample RFC 2", "localhost"));

        setInput("ADD\r\n" +
                "RFC 1234 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n" +
                "Title: Sample RFC 1\r\n" +
                "ADD\r\n" +
                "RFC 5678 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n" +
                "Title: Sample RFC 2\r\n");

        String expectedResponse = "P2P-CI/1.0 200 OK";
        Map<String, String> headers = server.parseRequest(input);
        String actualResponse = server.handleList(headers);
        assertEquals(expectedResponse, actualResponse);
    }
}