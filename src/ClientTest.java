import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.io.*;
import java.net.Socket;

public class ClientTest {

    @Test
    public void testAddRfcFromFile() {
        Client client = new Client("localhost", 1234, 5678);
        client.addRfcFromFile("path/to/rfc.txt");
        // Add your assertions here to verify the expected behavior
    }

    @Test
    public void testSendAddRequest() {
        Client client = new Client("localhost", 1234, 5678);
        String response = client.sendAddRequest("123", "RFC Title");
    }

    @Test
    public void testSendLookupRequest() {
        Client client = new Client("localhost", 1234, 5678);
        String response = client.sendLookupRequest("123", "RFC Title");
        // Add your assertions here to verify the expected behavior
    }

    @Test
    public void testSendListRequest() {
        Client client = new Client("localhost", 1234, 5678);
        String response = client.sendListRequest();
        
    }

    // Add more test methods for other public methods in the Client class

}