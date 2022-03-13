import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProjetReseaux {

    public void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(12345);
            server.setReuseAddress(true);
            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected"
                        + client.getInetAddress()
                        .getHostAddress());
                Server clientSock
                        = new Server(client);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}