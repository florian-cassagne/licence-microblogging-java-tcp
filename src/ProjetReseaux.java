import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Vector;

public class ProjetReseaux {
    private final Vector<PrintWriter> tabClients = new Vector<PrintWriter>();
    private int nbClients = 0;
    private static final int port = 14000;


    public static void main(String[] args) {
        ProjetReseaux projetReseaux = new ProjetReseaux();
        try {

            ServerSocket ss = new ServerSocket(port);
            while (true) {
                new ClientHandler(ss.accept(), projetReseaux);
            }
        } catch (Exception e) {
            System.out.println("Erreur ProjetReseaux : " + e.getMessage());
        }
    }

    synchronized public void delClient(int i) {
        nbClients--;
        if (tabClients.elementAt(i) != null) {
            tabClients.removeElementAt(i);
        }
    }


    synchronized public int addClient(PrintWriter out) {
        nbClients++;
        tabClients.addElement(out);
        return tabClients.size() - 1;
    }

    synchronized public int getNbClients() {
        return nbClients;
    }
}
