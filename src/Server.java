import structure.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {
    private final Socket clientSocket;
    private final ProjetReseaux projetReseaux;
    private final ArrayList<Message> messages;

    // Constructor
    public Server(Socket socket, ProjetReseaux projetReseaux) {
        this.clientSocket = socket;
        this.projetReseaux = projetReseaux;
        this.messages = new ArrayList<>();
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
                    switch (command[0]) {
                        case "PUBLISH":
                            if (content.length() != 0) {
                                if (command.length == 2) {
                                    String author = command[1].replaceAll("author:@", "");
                                    System.out.println("PUBLISH de la part de @" + author + " : " + content);
                                    out.println("OK");
                                } else {
                                    out.println("ERROR");
                                }
                            } else {
                                out.println("ERROR");
                            }
                            break;
                        case "RCV_IDS":
                            if (content.length() == 0) {
                                String author = null;
                                String tag = null;
                                int sinceId = -1;
                                int limit = 5;
                                for (String arg : command) {
                                    String[] argSplitted = arg.split(":");
                                    switch (argSplitted[0]) {
                                        case "author":
                                            author = argSplitted[1].replaceAll("@", "");
                                            break;
                                        case "tag":
                                            tag = argSplitted[1].replaceAll("#", "");
                                            break;
                                        case "since_id":
                                            try {
                                                sinceId = Integer.parseInt(argSplitted[1]);
                                            } catch (NumberFormatException e) {
                                                out.println("ERROR");
                                                break;
                                            }
                                            break;
                                        case "limit":
                                            try {
                                                limit = Integer.parseInt(argSplitted[1]);
                                            } catch (NumberFormatException e) {
                                                out.println("ERROR");
                                                break;
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                                ArrayList<Message> result = new ArrayList<>();
                                boolean selected = false;
                                int counter = 0;
                                Message sinceIdMessage = null;
                                if (sinceId != -1) {
                                    for (Message message : messages) {
                                        if (message.getId() == sinceId) {
                                            sinceIdMessage = message;
                                        }
                                    }
                                }
                                for (Message message : messages) {
                                    if (author != null && tag != null && sinceId != -1) {
                                        if (author.equalsIgnoreCase(message.getAuthor()) && tag.equalsIgnoreCase(message.getTag()) && sinceIdMessage.compareTo(message) < 0) {
                                            result.add(message);
                                            counter++;
                                        }
                                    }
                                }
                            } else {
                                out.println("ERROR");
                            }
                            break;
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
