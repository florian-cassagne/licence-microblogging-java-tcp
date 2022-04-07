package clients;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class microblogamu {
    public static Socket clientSocket;
    public static BufferedReader in;
    public static PrintWriter out;
    public static String user;
    public static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            clientSocket = new Socket("localhost", 12345);
            //flux pour envoyer
            out = new PrintWriter(clientSocket.getOutputStream());
            //flux pour recevoir
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.print("Entrez un pseudo : ");

            user = sc.nextLine();
            out.print("CONNECT user:@" + user + "\r\n\r\n");
            out.flush();
            System.out.println("Vous êtes connecté, écrire STOP fermera le client");
            Thread envoyer = new Thread(new Runnable() {
                String msg;
                @Override
                public void run() {
                    while(true){
                        msg = sc.nextLine();
                        out.println(msg);
                        out.flush();
                    }
                }
            });
            envoyer.start();

            Thread recevoir = new Thread(new Runnable() {
                String msg;
                @Override
                public void run() {
                    try {
                        msg = in.readLine();
                        while(msg!=null){
                            if(!msg.isEmpty())
                                System.out.println("Serveur : "+msg);
                            msg = in.readLine();
                        }
                        System.out.println("Serveur déconecté");
                        out.close();
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            recevoir.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


