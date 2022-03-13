import structure.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Server implements Runnable {

    private final Socket clientSocket;
    private final ArrayList<Message> messages;

    public Server(Socket socket) {
        this.clientSocket = socket;
        this.messages = new ArrayList<>();
    }

    public static String removeLastChar(String s) {
        return (s == null || s.length() == 0)
                ? null
                : (s.substring(0, s.length() - 1));
    }

    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                String[] request = line.split("\\\\r\\\\n");
                String header = request[0];
                String content = request[1];
                String[] command = header.split("\\s+");
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
                            Message sinceIdMessage = null;
                            if (sinceId != -1) {
                                for (Message message : messages) {
                                    if (message.getId() == sinceId) {
                                        sinceIdMessage = message;
                                    }
                                }
                            }
                            ArrayList<Message> msgs = new ArrayList<>(messages);
                            for (Message msg : messages) {
                                if (msg.getAuthor().equalsIgnoreCase(author)) msgs.remove(msg);
                                if (sinceId != -1) {
                                    assert sinceIdMessage != null;
                                    if (msg.getCreatedOn().compareTo(sinceIdMessage.getCreatedOn()) < 0) msgs.remove(msg);
                                }
                                if (msg.getTag().equalsIgnoreCase(tag)) msgs.remove(msg);

                            }
                            Collections.sort(msgs);
                            List<Message> result = msgs.stream().limit(limit).collect(Collectors.toList());
                            StringBuilder returned = new StringBuilder("MSG_IDS\\r\\n");
                            for (Message msg : result) {
                                returned.append(msg.getId()).append(",");
                            }
                            returned = new StringBuilder(removeLastChar(returned.toString()));
                            returned.append("\\r\\n");
                            out.println(returned);
                        } else {
                            out.println("ERROR");
                        }
                        break;
                    case "RCV_MSG":
                        if (content.length() == 0) {
                            int id = -1;
                            for (String arg : command) {
                                String[] argSplitted = arg.split(":");
                                if ("msg_id".equals(argSplitted[0])) {
                                    try {
                                        id = Integer.parseInt(argSplitted[1]);
                                    } catch (NumberFormatException e) {
                                        out.println("ERROR");
                                    }
                                }
                            }
                            if (id != -1) {
                                for (Message msg : messages) {
                                    if (msg.getId() == id) {
                                        out.println(msg.getContent());
                                        return;
                                    }
                                }
                            }
                            out.println("ERROR");
                        }
                        break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
