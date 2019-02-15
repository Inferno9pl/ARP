import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Enumeration;

public class EchoClient {
	
	/*
    public static void main(String[] args) throws IOException {
    	int port = 4444;
    	ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByAddress(new byte[] {0x00,0x00,0x00,0x00}));
        System.err.println("Started server on port " + port);
        
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        
        for (NetworkInterface networkInterface : Collections.list(nets)) {
            for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                Socket socket = null;
                try {
                    socket = new Socket(inetAddress, 4444);
                    System.out.println(String.format("Connected using %s [%s]", networkInterface.getDisplayName(), inetAddress));
                } catch (ConnectException ex) {
                    System.out.println(String.format("Failed to connect using %s [%s]", networkInterface.getDisplayName(), inetAddress));
                } finally {
                    if (socket != null) {
                        socket.close();
                    }
                }
            }
        }

    }
    
    */
}