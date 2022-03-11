import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Thread t;
    private final Socket s;
    private PrintWriter out;
    private BufferedReader in;
    private final ProjetReseaux projetReseaux;
    private int numClient = 0;

    ClientHandler(Socket s, ProjetReseaux projetReseaux) {
        this.projetReseaux = projetReseaux;
        this.s = s;
        try {
            out = new PrintWriter(s.getOutputStream());
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            this.numClient = projetReseaux.addClient(out);
        } catch (IOException e) {
            System.out.println("Erreur ClientHandler : " + e.getMessage());
        }

        this.t = new Thread(this);
        this.t.start();
    }

    public void run() {
        String message = "";
        System.out.println("Un nouveau client s'est connecte, no " + numClient);
        try {
            char[] charCur = new char[1];
            while (in.read(charCur, 0, 1) != -1) {
                if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r')
                    message += charCur[0];
                else if (!message.equalsIgnoreCase("")) {
                    if (charCur[0] == '\u0000')

                        projetReseaux.sendAll(message, "" + charCur[0]);
                    else projetReseaux.sendAll(message, "");
                    message = "";
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                System.out.println("Le client no " + numClient + " s'est deconnecte");
                projetReseaux.delClient(numClient);
                s.close();
            } catch (IOException e) {
            }
        }
    }
}
