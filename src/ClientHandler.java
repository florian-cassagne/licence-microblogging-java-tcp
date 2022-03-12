import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    // Constructor
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        try {

            // get the outputstream of client
            out = new PrintWriter(
                    clientSocket.getOutputStream(), true);

            // get the inputstream of client
            in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {

                String[] request = line.split("\\\\r\\\\n");
                if (request.length == 2) {
                    String commandNonSplitted = request[0];
                    String content = request[1];
                    String[] command = commandNonSplitted.split("\\s+");
                    if (command[0].equalsIgnoreCase("PUBLISH") && content.length() != 0) {
                        if (command.length == 2) {
                            String author = command[1].replaceAll("author:@", "");
                            out.println("PUBLISH de la part de @" + author + " : " + content);
                        } else {
                            out.println("Commande ou paramètres malformées");
                        }
                    }
                } else {

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    System.out.println("out != null");
                    out.close();
                }
                if (in != null) {
                    System.out.println("in != null");
                    in.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
