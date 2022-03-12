package clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class publisher {
    // driver code
    public static void main(String[] args) {
        // establish a connection by providing host and port
        // number
        try (Socket socket = new Socket("localhost", 1234)) {

            // writing to server
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            // reading from server
            BufferedReader in
                    = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            // object of scanner class
            Scanner sc = new Scanner(System.in);
            String line = null;

            System.out.print("Entrez un pseudo : ");

            String user = sc.nextLine();

            System.out.println("Maintenant envoyez des messages, écrire STOP fermera le client");
            while (!"stop".equalsIgnoreCase(line)) {
                line = sc.nextLine();
                out.println("PUBLISH author:@" + user + "\\r\\n" + line );
                out.flush();
                System.out.println("Server replied "
                        + in.readLine());
            }

            // closing the scanner object
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
