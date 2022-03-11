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
        try {
            in.lines().forEach(line -> {
                System.out.println( "Ligne = " + line);
            });
        } catch (Exception e) {
        } finally {
            try {
                projetReseaux.delClient(numClient);
                s.close();
            } catch (IOException e) {
            }
        }
    }
}
