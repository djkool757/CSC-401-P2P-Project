import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientTest {
    private Client client;
    private ArrayList<RFC> rfcIndex;

    @Before
    public void setup() throws IOException {
        rfcIndex = new ArrayList<>();
        client = new Client("localhost", rfcIndex);
    }

    @Test
    public void testHandleGetRequestValidRFC() throws IOException {
        Socket clientSocket = new Socket();
        BufferedReader in = new BufferedReader(new StringReader("GET /RFC 1234 P2P-CI/1.0\r\n"));
        PrintWriter out = new PrintWriter(new ByteArrayOutputStream());
        when(clientSocket.getInputStream()new ByteArrayInputStream("".getBytes()));
        clientSocket.getInputStream().
        when(clientSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        client.handleGetRequest(clientSocket);

        verify(out).println("P2P-CI/1.0 200 OK");
        verify(out).println("Content-Type: text/text");
        verify(out).println("Sample RFC");
    }

    @Test
    public void testHandleGetRequestInvalidRFC() throws IOException {
        Socket clientSocket = mock(Socket.class);
        BufferedReader in = new BufferedReader(new StringReader("GET /RFC 5678 P2P-CI/1.0\r\n"));
        PrintWriter out = mock(PrintWriter.class);
        when(clientSocket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(clientSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        client.handleGetRequest(clientSocket);

        verify(out).println("404 Not Found");
    }

    @Test
    public void testSendRFCValidRFC() {
        PrintWriter out = mock(PrintWriter.class);
        RFC rfc = new RFC(1234, "Sample RFC", "localhost");

        client.sendRFC(out, 1234);

        verify(out).println("P2P-CI/1.0 200 OK");
        verify(out).println("Content-Type: text/text");
        verify(out).println("Sample RFC");
    }

    @Test
    public void testSendRFCInvalidRFC() {
        PrintWriter out = mock(PrintWriter.class);

        client.sendRFC(out, 5678);

        verify(out).println("404 Not Found");
    }

    @Test
    public void testGetRFCByNumberValidRFC() {
        RFC rfc1 = new RFC(1234, "Sample RFC 1", "localhost");
        RFC rfc2 = new RFC(5678, "Sample RFC 2", "localhost");
        rfcIndex.add(rfc1);
        rfcIndex.add(rfc2);

        RFC result = client.getRFCByNumber(1234);

        assertEquals(rfc1, result);
    }

    @Test
    public void testGetRFCByNumberInvalidRFC() {
        RFC rfc1 = new RFC(1234, "Sample RFC 1", "localhost");
        RFC rfc2 = new RFC(5678, "Sample RFC 2", "localhost");
        rfcIndex.add(rfc1);
        rfcIndex.add(rfc2);

        RFC result = client.getRFCByNumber(9999);

        assertNull(result);
    }
}