import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.ArrayList;

public class ClientTest {
    private Client client;
    private ByteArrayOutputStream outputStream;
    private ByteArrayInputStream inputStream;

    @Before
    public void setUp() {
        ArrayList<RFC> rfcIndex = new ArrayList<>();
        rfcIndex.add(new RFC(1, "RFC 1", "localhost"));
        rfcIndex.add(new RFC(2, "RFC 2", "localhost"));
        rfcIndex.add(new RFC(3, "RFC 3", "localhost"));
        client = new Client("localhost", rfcIndex);
        outputStream = new ByteArrayOutputStream();
        inputStream = new ByteArrayInputStream("Test Response".getBytes());
    }

    @Test
public void testJoinP2PSystem() throws IOException {
    // Setup
    ArrayList<RFC> rfcIndex = new ArrayList<>();
    rfcIndex.add(new RFC(1, "RFC 1", "localhost"));
    Client client = new Client("localhost", rfcIndex);
    // Execute
    client.joinP2PSystem();

    // Verify
    assertEquals("P2P-CI/1.0 200 OK", outputStream.toString().trim());
}

@Test
public void testSendRFC() throws IOException {
    // Setup
    ArrayList<RFC> rfcIndex = new ArrayList<>();
    rfcIndex.add(new RFC(1, "RFC 1", "localhost"));
    Client client = new Client("localhost", rfcIndex);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    // Execute
    client.sendRFC(new PrintWriter(outputStream, true), 1);

    // Verify
    assertTrue(outputStream.toString().contains("P2P-CI/1.0 200 OK"));
}

@Test
public void testGetRFC() throws IOException {
    // Setup
    ArrayList<RFC> rfcIndex = new ArrayList<>();
    rfcIndex.add(new RFC(1, "RFC 1", "localhost"));
    Client client = new Client("localhost", rfcIndex);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    // Execute
    client.getRFC(1, "localhost", "Windows");

    // Verify
    assertTrue(outputStream.toString().contains("GET RFC 1 P2P-CI/1.0"));
}
}