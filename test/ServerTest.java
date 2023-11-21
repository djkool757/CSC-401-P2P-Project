package test;
import org.junit.Before;

import org.junit.Test;

import src.Server;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;

public class ServerTest {
    private Server server;
    private ByteArrayOutputStream outputStream;
    private BufferedReader reader;
    private BufferedWriter writer;

    @Before
    public void setup() {
        server = new Server();
        outputStream = new ByteArrayOutputStream();
        reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));
        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    @Test
    public void testHandleAdd() throws IOException {
        // Prepare input data
        String input = "ADD\r\n" +
                "RFC 1234 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n" +
                "Title: Sample RFC\r\n";
        BufferedReader inputReader = new BufferedReader(new StringReader(input));

        // Set up the client socket
        Socket clientSocket = new Socket();
        server.new ClientHandler(clientSocket);

        // Redirect output to the writer
        server.ClientHandler.clientSocket = clientSocket;
        server.ClientHandler.clientSocket.getOutputStream().write(input.getBytes());
        server.ClientHandler.clientSocket.getOutputStream().flush();

        // Call the handleAdd method
        server.handleAdd(inputReader);

        // Verify the result
        assertEquals(1, server.rfcIndex.size());
        assertEquals(1, server.peerList.size());
        assertEquals(1234, server.rfcIndex.get(0).number);
        assertEquals("Sample RFC", server.rfcIndex.get(0).title);
        assertEquals("localhost", server.rfcIndex.get(0).hostname);
        assertEquals("localhost", server.peerList.get(0).hostname);
        assertEquals(12345, server.peerList.get(0).port);
    }

    @Test
    public void testHandleLookupFound() throws IOException {
        // Prepare input data
        String input = "LOOKUP\r\n" +
                "RFC 1234 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n" +
                "Title: Sample RFC\r\n";
        BufferedReader inputReader = new BufferedReader(new StringReader(input));

        // Set up the client socket
        Socket clientSocket = new Socket();
        server.new ClientHandler(clientSocket);

        // Redirect output to the writer
        server.clientSocket = clientSocket;
        server.ClientHandler.clientSocket.getOutputStream().write(input.getBytes());
        server.ClientHandler.clientSocket.getOutputStream().flush();

        // Add a sample RFC to the index
        server.rfcIndex.add(new Server.RFC(1234, "Sample RFC", "localhost"));

        // Call the handleLookup method
        server.handleLookup(inputReader);

        // Verify the result
        String response = reader.readLine();
        Assertions.assertEquals("P2P-CI/1.0 200 OK", response);
        response = reader.readLine();
        Assertions.assertEquals("RFC 1234 Sample RFC localhost 12345", response);
    }

    @Test
    public void testHandleLookupNotFound() throws IOException {
        // Prepare input data
        String input = "LOOKUP\r\n" +
                "RFC 1234 P2P-CI/1.0\r\n" +
                "Host: localhost\r\n" +
                "Port: 12345\r\n" +
                "Title: Sample RFC\r\n";
        BufferedReader inputReader = new BufferedReader(new StringReader(input));

        // Set up the client socket
        Socket clientSocket = new Socket();
        server.new ClientHandler(clientSocket);

        // Redirect output to the writer
        server.ClientHandler.clientSocket = clientSocket;
        server.ClientHandler.clientSocket.getOutputStream().write(input.getBytes());
        server.ClientHandler.clientSocket.getOutputStream().flush();

        // Call the handleLookup method
        server.handleLookup(inputReader);

        // Verify the result
        String response = reader.readLine();
        Assertions.assertEquals("P2P-CI/1.0 404 Not Found", response);
    }

    @Test
    public void testHandleList() throws IOException {
        // Set up the client socket
        Socket clientSocket = new Socket();
        server.new ClientHandler(clientSocket);

        // Redirect output to the writer
        server.ClientHandler.clientSocket = clientSocket;
        server.ClientHandler.clientSocket.getOutputStream().write("LIST\r\n".getBytes());
        server.ClientHandler.clientSocket.getOutputStream().flush();

        // Add some sample RFCs to the index
        server.rfcIndex.add(new Server.RFC(1234, "Sample RFC 1", "localhost"));
        server.rfcIndex.add(new Server.RFC(5678, "Sample RFC 2", "localhost"));

        // Call the handleList method
        server.handleList();

        // Verify the result
        String response = reader.readLine();
        Assertions.assertEquals("P2P-CI/1.0 200 OK", response);
        response = reader.readLine();
        Assertions.assertEquals("RFC 1234 Sample RFC 1 localhost", response);
        response = reader.readLine();
        Assertions.assertEquals("RFC 5678 Sample RFC 2 localhost", response);
    }
}