import structure.Message;
import structure.Tag;
import structure.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Server implements Runnable {

    private final Socket clientSocket;
    private final ProjetReseaux instance;
    private User user_connected;

    public Server(Socket socket, ProjetReseaux instance) {
        this.clientSocket = socket;
        this.instance = instance;
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
                if (user_connected != null) {
                    for (Message msg : user_connected.getQueued_messages()) {
                        String response = "author:@" + msg.getAuthor() + " msg_id:" + msg.getId();
                        if (msg.getReply_to() != null) response = response.concat("reply_to_id:" + msg.getReply_to());
                        if (msg.isRepublished()) response = response.concat("republished:true");
                        out.println(response + "\r\n" + msg.getContent());
                    }
                }
                String[] request = line.split("\\\\r\\\\n", 2);
                String[] header = request[0].split("\\s+", 2);
                String content = request[1];
                String command = header[0];
                String[] arguments = header[1].split("\\s+");
                switch (command) {
                    case "CONNECT":
                        if (content.length() == 0) {
                            String user = "";
                            for (String arg : arguments) {
                                String[] argSplitted = arg.split(":");

                                switch (argSplitted[0]) {
                                    case "user":
                                        user = argSplitted[1].replaceAll("@", "");
                                        break;
                                    default:
                                        break;
                                }
                                if (!user.equals("")) {
                                    for (User userloop : instance.getUsers()) {
                                        if (userloop.getName().equalsIgnoreCase(user)) {
                                            user_connected = userloop;
                                            out.println("OK \r\n");
                                            break;
                                        }

                                    }
                                    user_connected = new User(user);
                                    out.println("OK \r\n");
                                    break;
                                }
                                out.println("ERROR \r\n Please enter a user to connect");
                                break;
                            }
                        } else {
                            out.println("ERROR \r\n No content needed, remove it please");
                            break;
                        }
                        break;
                    case "SUBSCRIBE":
                        if (user_connected == null) {
                            out.println("ERROR \r\n Please use CONNECT command to identify yourself");
                            break;
                        }
                        if (content.length() == 0) {
                            String author = "";
                            String tag = "";
                            for (String arg : arguments) {
                                String[] argSplitted = arg.split(":");

                                switch (argSplitted[0]) {
                                    case "author":
                                        author = argSplitted[1].replaceAll("@", "");
                                        break;
                                    case "tag":
                                        tag = argSplitted[1].replaceAll("#", "");
                                        break;
                                    default:
                                        break;
                                }

                                if (!author.equals("")) {
                                    for (User user : instance.getUsers()) {
                                        if (user.getName().equalsIgnoreCase(author)) {
                                            user_connected.subscribe(user);
                                            out.println("OK \r\n");
                                            break;
                                        }
                                    }
                                    out.println("ERROR \r\n User not found");
                                    break;
                                }

                                if (!tag.equals("")) {
                                    for (Tag _tag : instance.getTags()) {
                                        if (_tag.getName().equalsIgnoreCase(tag)) {
                                            _tag.subscribe(user_connected);
                                            out.println("OK \r\n");
                                            break;
                                        }
                                    }
                                    Tag _tag = new Tag(tag);
                                    _tag.subscribe(user_connected);
                                    instance.addTag(_tag);
                                }
                                out.println("ERROR \r\n Please add a tag or author to follow");
                                break;

                            }
                        } else {
                            out.println("ERROR \r\n No content needed, remove it please");
                            break;
                        }
                        break;
                    case "UNSUBSCRIBE":
                        if (user_connected == null) {
                            out.println("ERROR \r\n Please use CONNECT command before trying to subscribe");
                            break;
                        }
                        if (content.length() == 0) {
                            String author = "";
                            String tag = "";
                            for (String arg : arguments) {
                                String[] argSplitted = arg.split(":");

                                switch (argSplitted[0]) {
                                    case "author":
                                        author = argSplitted[1].replaceAll("@", "");
                                        break;
                                    case "tag":
                                        tag = argSplitted[1].replaceAll("#", "");
                                        break;
                                    default:
                                        break;
                                }

                                if (!author.equals("")) {
                                    for (User user : instance.getUsers()) {
                                        if (user.getName().equalsIgnoreCase(author)) {
                                            if(user.getFollowing().contains(user_connected)) {
                                                user_connected.removeSubscriber(user);
                                                user.unsubscribe(user_connected);
                                                out.println("OK \r\n");
                                            }else {
                                                out.println("ERROR \r\n You are not following this user");
                                            }
                                            break;
                                        }
                                    }
                                    out.println("ERROR \r\n User not found");
                                    break;
                                }

                                if (!tag.equals("")) {
                                    for (Tag _tag : instance.getTags()) {
                                        if (_tag.getName().equalsIgnoreCase(tag)) {
                                            if(_tag.getFollowing().contains(user_connected)) {
                                                _tag.unsubscribe(user_connected);
                                                out.println("OK \r\n");
                                            } else {
                                                out.println("ERROR \r\n You are not following this tag");
                                            }
                                            break;
                                        }
                                    }
                                    Tag _tag = new Tag(tag);
                                    _tag.subscribe(user_connected);
                                    instance.addTag(_tag);
                                }
                                out.println("ERROR \r\n Please add a tag or author to follow");
                                break;

                            }
                        } else {
                            out.println("ERROR \r\n No content needed, remove it please");
                            break;
                        }
                        break;
                    case "PUBLISH":
                        if (content.length() != 0) {
                            String author = "";
                            for (String arg : arguments) {
                                String[] argSplitted = arg.split(":");

                                if ("author".equals(argSplitted[0])) {
                                    author = argSplitted[1].replaceAll("@", "");
                                }
                                Message message = new Message(SecureRandom.getInstanceStrong().nextLong(), content, author, new Date());
                                instance.addMessage(message);
                                out.println("OK \r\n");
                                break;
                            }
                        } else {
                            out.println("ERROR \r\n Content needed, add it please");
                            break;
                        }
                        break;
                    case "RCV_IDS":
                        if (content.length() == 0) {
                            String author = "";
                            String tag = "";
                            int sinceId = -1;
                            int limit = 5;
                            for (String arg : arguments) {
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
                                            out.println("ERROR \r\n Wrong number format, please enter an integer");
                                            break;
                                        }
                                        break;
                                    case "limit":
                                        try {
                                            limit = Integer.parseInt(argSplitted[1]);
                                        } catch (NumberFormatException e) {
                                            out.println("ERROR \r\n Wrong number format, please enter an integer");
                                            break;
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                            Message sinceIdMessage = null;
                            if (sinceId != -1) {
                                for (Message message : instance.getMessages()) {
                                    if (message.getId() == sinceId) {
                                        sinceIdMessage = message;
                                    }
                                }
                            }
                            ArrayList<Message> msgs = new ArrayList<>(instance.getMessages());
                            for (Message msg : instance.getMessages()) {
                                if (msg.getAuthor().equalsIgnoreCase(author)) msgs.remove(msg);
                                if (sinceId != -1) {
                                    assert sinceIdMessage != null;
                                    if (msg.getCreatedOn().compareTo(sinceIdMessage.getCreatedOn()) < 0)
                                        msgs.remove(msg);
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
                            out.println("ERROR \r\n No content needed, remove it please");
                            break;
                        }
                        break;
                    case "RCV_MSG":
                        if (content.length() == 0) {
                            int id = -1;
                            for (String arg : arguments) {
                                String[] argSplitted = arg.split(":");
                                if ("msg_id".equals(argSplitted[0])) {
                                    try {
                                        id = Integer.parseInt(argSplitted[1]);
                                    } catch (NumberFormatException e) {
                                        out.println("ERROR \r\n Wrong number format, please enter an integer");
                                        break;
                                    }
                                }
                            }
                            if (id != -1) {
                                for (Message msg : instance.getMessages()) {
                                    if (msg.getId() == id) {
                                        String response = "author:@" + msg.getAuthor() + " msg_id:" + msg.getId();
                                        if (msg.getReply_to() != null)
                                            response = response.concat("reply_to_id:" + msg.getReply_to());
                                        if (msg.isRepublished()) response = response.concat("republished:true");
                                        out.println(response + "\r\n" + msg.getContent());
                                        break;
                                    }
                                }
                            }
                            out.println("ERROR \r\n Message not found");
                        } else {
                            out.println("ERROR \r\n No content needed, remove it please");
                        }
                        break;
                    case "REPLY":
                        if (content.length() != 0) {
                            String author = "";
                            int id = -1;
                            for (String arg : arguments) {
                                String[] argSplitted = arg.split(":");
                                if ("author".equals(argSplitted[0])) {
                                    author = argSplitted[0];
                                }
                                if ("reply_to_id".equals(argSplitted[1])) {
                                    try {
                                        id = Integer.parseInt(argSplitted[1]);
                                    } catch (NumberFormatException e) {
                                        out.println("ERROR \r\n Wrong number format, please enter an integer");
                                        break;
                                    }
                                }
                            }
                            for (Message msg : instance.getMessages()) {
                                if (msg.getId() == id) {
                                    Message message = new Message(SecureRandom.getInstanceStrong().nextLong(), content, author, new Date());
                                    message.setReply_to(msg);
                                    instance.addMessage(message);
                                    break;
                                }
                            }
                            out.println("ERROR \r\n Message not found");
                        } else {
                            out.println("ERROR \r\n Content needed, add it please");
                        }
                        break;
                    case "REPUBLISH":
                        if (content.length() == 0) {
                            String author = "";
                            int id = -1;
                            for (String arg : arguments) {
                                String[] argSplitted = arg.split(":");
                                if ("author".equals(argSplitted[0])) {
                                    author = argSplitted[0];
                                }
                                if ("msg_id".equals(argSplitted[1])) {
                                    try {
                                        id = Integer.parseInt(argSplitted[1]);
                                    } catch (NumberFormatException e) {
                                        out.println("ERROR \r\n Wrong number format, please enter an integer");
                                        break;
                                    }
                                }
                            }
                            for (Message msg : instance.getMessages()) {
                                if (msg.getId() == id) {
                                    Message message = new Message(SecureRandom.getInstanceStrong().nextLong(), msg.getContent(), author, new Date());
                                    message.setRepublished(true);
                                    instance.addMessage(message);
                                    break;
                                }
                            }
                            out.println("ERROR \r\n Message not found");
                        } else {
                            out.println("ERROR \r\n No content needed, remove it please");
                        }
                        break;

                }

            }
        } catch (IOException | NoSuchAlgorithmException e) {
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
