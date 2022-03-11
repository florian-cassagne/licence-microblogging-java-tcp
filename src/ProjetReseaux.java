import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Vector;

public class ProjetReseaux {
    private final Vector tabClients = new Vector();
    private int nbClients = 0;


    public static void main(String[] args) {
        ProjetReseaux projetReseaux = new ProjetReseaux();
        try {
            Integer port;
            if (args.length <= 0) port = new Integer("18000");
            else port = new Integer(args[0]);

            new CommandHandler(projetReseaux);

            ServerSocket ss = new ServerSocket(port.intValue());
            printWelcome(port);
            while (true) {
                new ClientHandler(ss.accept(), projetReseaux);
            }
        } catch (Exception e) {
        }
    }


    static private void printWelcome(Integer port) {
        System.out.println("--------");
        System.out.println("Demarre sur le port : " + port.toString());
        System.out.println("--------");
        System.out.println("Quitter : tapez \"quit\"");
        System.out.println("Nombre de connectés : tapez \"total\"");
        System.out.println("--------");
    }


    synchronized public void sendAll(String message, String sLast) {
        PrintWriter out;
        for (int i = 0; i < tabClients.size(); i++) {
            out = (PrintWriter) tabClients.elementAt(i);
            if (out != null) {

                out.print(message + sLast);
                out.flush();
            }
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
