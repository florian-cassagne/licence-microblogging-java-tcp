import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandHandler implements Runnable {
    ProjetReseaux projetReseaux;
    BufferedReader in;
    String strCommande = "";
    Thread t;

    CommandHandler(ProjetReseaux projetReseaux) {
        this.projetReseaux = projetReseaux;

        this.in = new BufferedReader(new InputStreamReader(System.in));
        this.t = new Thread(this);
        this.t.start();
    }


    public void run() {
        try {

            while ((strCommande = in.readLine()) != null) {
                if (strCommande.equalsIgnoreCase("quit"))
                    System.exit(0);
                else if (strCommande.equalsIgnoreCase("total")) {

                    System.out.println("Nombre de connectes : " + projetReseaux.getNbClients());
                    System.out.println("--------");
                } else {

                    System.out.println("Cette commande n'est pas supportee");
                    System.out.println("Quitter : \"quit\"");
                    System.out.println("Nombre de connectes : \"total\"");
                    System.out.println("--------");
                }
                System.out.flush();
            }
        } catch (IOException e) {
            System.out.println("Erreur CommandHandler : " + e.getMessage());
        }
    }
}
