import structure.Message;
import structure.Tag;
import structure.User;

import java.awt.desktop.SystemEventListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class microblogamu_central implements Runnable {

    private final Socket clientSocket;
    private User user_connected;

    public microblogamu_central(Socket socket) {
        this.clientSocket = socket;
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
                StringBuilder sb = new StringBuilder();
                sb.append(line);
                while(in.ready()){
                    sb.append(in.readLine());
                }
                line = sb.toString();
                if(line.isEmpty()) continue;
                if (user_connected != null) {
                    for (Message msg : user_connected.getQueued_messages()) {
                        String response = "author:@" + msg.getAuthor() + " msg_id:" + msg.getId();
                        if (msg.getReply_to() != null) response = response.concat("reply_to_id:" + msg.getReply_to());
                        if (msg.isRepublished()) response = response.concat("republished:true");
                        out.println(response + "\r\n" + msg.getContent());
                    }
                }
                System.out.println("Command entered : " + line);
                String[] request = line.split("\\\\r\\\\n");
                String[] header = request[0].split("\\s+");
                String content = "";
                if(request.length > 1) {
                    content = request[1];
                }
                if(header.length <= 1) {
                    out.println("ERROR \r\n Please use commands\r\n");
                    continue;
                }
                String command = header[0];
                String[] arguments;
                arguments = header[1].split("\\s+");
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
                                    for (User userloop : ProjetReseaux.getUsers()) {
                                        if (userloop.getName().equalsIgnoreCase(user)) {
                                            user_connected = userloop;
                                            out.println("OK \r\n\r\n");
                                            break;
                                        }

                                    }
                                    user_connected = new User(user);
                                    out.println("OK \r\n\r\n");
                                    break;
                                }
                                out.println("ERROR \r\n Please enter a user to connect \r\n");
                                break;
                            }
                        } else {
                            out.println("ERROR \r\n No content needed, remove it please \r\n");
                            break;
                        }
                        break;
                    case "SUBSCRIBE":
                        if (user_connected == null) {
                            out.println("ERROR \r\n Please use CONNECT command to identify yourself \r\n");
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
                                    for (User user : ProjetReseaux.getUsers()) {
                                        if (user.getName().equalsIgnoreCase(author)) {
                                            user_connected.subscribe(user);
                                            out.println("OK \r\n\r\n");
                                            break;
                                        }
                                    }
                                    out.println("ERROR \r\n User not found \r\n");
                                    break;
                                }

                                if (!tag.equals("")) {
                                    for (Tag _tag : ProjetReseaux.getTags()) {
                                        if (_tag.getName().equalsIgnoreCase(tag)) {
                                            _tag.subscribe(user_connected);
                                            out.println("OK \r\n\r\n");
                                            break;
                                        }
                                    }
                                    Tag _tag = new Tag(tag);
                                    _tag.subscribe(user_connected);
                                    ProjetReseaux.addTag(_tag);
                                }
                                out.println("ERROR \r\n Please add a tag or author to follow\r\n");
                                break;

                            }
                        } else {
                            out.println("ERROR \r\n No content needed, remove it please\r\n");
                            break;
                        }
                        break;
                    case "UNSUBSCRIBE":
                        if (user_connected == null) {
                            out.println("ERROR \r\n Please use CONNECT command before trying to subscribe\r\n");
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
                                    for (User user : ProjetReseaux.getUsers()) {
                                        if (user.getName().equalsIgnoreCase(author)) {
                                            if(user.getFollowing().contains(user_connected)) {
                                                user_connected.removeSubscriber(user);
                                                user.unsubscribe(user_connected);
                                                out.println("OK \r\n\r\n");
                                            }else {
                                                out.println("ERROR \r\n You are not following this user\r\n");
                                             }
                                            break;
                                        }
                                    }
                                    out.println("ERROR \r\n User not found\r\n");
                                    break;
                                }

                                if (!tag.equals("")) {
                                    for (Tag _tag : ProjetReseaux.getTags()) {
                                        if (_tag.getName().equalsIgnoreCase(tag)) {
                                            if(_tag.getFollowing().contains(user_connected)) {
                                                _tag.unsubscribe(user_connected);
                                                out.println("OK \r\n\r\n");
                                            } else {
                                                out.println("ERROR \r\n You are not following this tag\r\n");
                                            }
                                            break;
                                        }
                                    }
                                    Tag _tag = new Tag(tag);
                                    _tag.subscribe(user_connected);
                                    ProjetReseaux.addTag(_tag);
                                }
                                out.println("ERROR \r\n Please add a tag or author to follow\r\n");
                                break;

                            }
                        } else {
                            out.println("ERROR \r\n No content needed, remove it please\r\n");
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
                                Message message = new Message(Math.abs(SecureRandom.getInstanceStrong().nextLong()), content, author, new Date());
                                ProjetReseaux.addMessage(message);
                                System.out.println("size messages:" + ProjetReseaux.getMessages().size());
                                out.println("OK \r\n\r\n");
                                break;
                            }
                        } else {
                            out.println("ERROR \r\n Content needed, add it please\r\n");
                            break;
                        }
                        break;
                    case "RCV_IDS":
                        if (content.length() == 0) {
                            List<Message> returned_messages = new ArrayList<>();
                            String author = "";
                            String tag = "";
                            int sinceId = -1;
                            int limit = 5;
                            for (String arg : arguments) {
                                String[] argSplitted = arg.split(":");

                                //Read all arguments
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
                                            out.println("ERROR \r\n Wrong number format, please enter an integer\r\n");
                                            break;
                                        }
                                        break;
                                    case "limit":
                                        try {
                                            limit = Integer.parseInt(argSplitted[1]);
                                        } catch (NumberFormatException e) {
                                            out.println("ERROR \r\n Wrong number format, please enter an integer\r\n");
                                            break;
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                            Message sinceIdMessage = null;
                            if (sinceId != -1) {
                                for (Message message : ProjetReseaux.getMessages()) {
                                    if (message.getId() == sinceId) {
                                        sinceIdMessage = message;
                                    }
                                }
                                if(sinceIdMessage == null){
                                    out.println("ERROR \r\n No message found with an id of " + sinceId + " \r\n");
                                }
                            }

                            //Let's loop over all messages and put the right ones into returned_messages
                            for (Message msg : ProjetReseaux.getMessages()) {
                                //Break the loop if we have enough messages (defined by limit parameter)
                                if(returned_messages.size() >= limit){
                                    break;
                                }
                                System.out.println("test1");
                                //Check for author
                                if (!author.isEmpty() && msg.getAuthor().equalsIgnoreCase(author) && !returned_messages.contains(msg)){
                                    returned_messages.add(msg);
                                    System.out.println("test223");
                                }
                                System.out.println("test2");
                                //Check for tag and remove the message from returned_messages if necessary
                                if (!tag.isEmpty()){
                                    System.out.println("test5");
                                    if(msg.getTag().equalsIgnoreCase(tag)){
                                        if(!returned_messages.contains(msg)) returned_messages.add(msg);
                                        System.out.println("test7");
                                    } else {
                                        System.out.println("test6");
                                        returned_messages.remove(msg);
                                    }
                                }
                                //Compare it's date to since_id message's date and remove the message from returned_messages if necessary
                                System.out.println("test8");
                                if (sinceId != -1) {
                                    System.out.println("test9");
                                    if (msg.getCreatedOn().compareTo(sinceIdMessage.getCreatedOn()) < 0) {
                                        if (!returned_messages.contains(msg)) returned_messages.add(msg);
                                    } else {
                                        returned_messages.remove(msg);
                                    }
                                }

                            }
                            System.out.println("size=" + returned_messages.size());
                            System.out.println("test10");
                            Collections.sort(returned_messages);
                            StringBuilder returned = new StringBuilder("MSG_IDS\r\n");
                            for (Message msg : returned_messages) {
                                System.out.println("test11");
                                returned.append(msg.getId()).append(",");
                            }
                            //Remove "," from the end of the list
                            returned = new StringBuilder(removeLastChar(returned.toString()));
                            returned.append("\r\n");
                            //Send the response to the client.
                            out.println(returned);
                        } else {
                            out.println("ERROR \r\n No content needed, remove it please\r\n");
                            break;
                        }
                        break;
                    case "RCV_MSG":
                        if (content.length() == 0) {
                            long id = -1;
                            for (String arg : arguments) {
                                String[] argSplitted = arg.split(":");
                                if ("msg_id".equals(argSplitted[0])) {
                                    try {
                                        id = Long.parseLong(argSplitted[1]);
                                    } catch (NumberFormatException e) {
                                        out.println("ERROR \r\n Wrong number format, please enter an integer\r\n");
                                        break;
                                    }
                                }
                            }
                            boolean found = false;
                            if (id != -1) {
                                for (Message msg : ProjetReseaux.getMessages()) {
                                    if (msg.getId() == id) {
                                        String response = "author:@" + msg.getAuthor() + " msg_id:" + msg.getId();
                                        if (msg.getReply_to() != null)
                                            response = response.concat("reply_to_id:" + msg.getReply_to());
                                        if (msg.isRepublished()) response = response.concat("republished:true");
                                        out.println(response + "\r\n" + msg.getContent());
                                        found = true;
                                        break;

                                    }
                                }
                            }
                            if(!found) out.println("ERROR \r\n Message not found\r\n");
                        } else {
                            out.println("ERROR \r\n No content needed, remove it please\r\n");
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
                                        out.println("ERROR \r\n Wrong number format, please enter an integer\r\n");
                                        break;
                                    }
                                }
                            }
                            for (Message msg : ProjetReseaux.getMessages()) {
                                if (msg.getId() == id) {
                                    Message message = new Message(SecureRandom.getInstanceStrong().nextLong(), content, author, new Date());
                                    message.setReply_to(msg);
                                    ProjetReseaux.addMessage(message);
                                    break;
                                }
                            }
                            out.println("ERROR \r\n Message not found\r\n");
                        } else {
                            out.println("ERROR \r\n Content needed, add it please\r\n");
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
                                        out.println("ERROR \r\n Wrong number format, please enter an integer\r\n");
                                        break;
                                    }
                                }
                            }
                            for (Message msg : ProjetReseaux.getMessages()) {
                                if (msg.getId() == id) {
                                    Message message = new Message(SecureRandom.getInstanceStrong().nextLong(), msg.getContent(), author, new Date());
                                    message.setRepublished(true);
                                    ProjetReseaux.addMessage(message);
                                    break;
                                }
                            }
                            out.println("ERROR \r\n Message not found\r\n");
                        } else {
                            out.println("ERROR \r\n No content needed, remove it please\r\n");
                        }
                        break;
                    default:
                        break;
                        
                }
                out.flush();
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
    public static <T> T[] asArray(T... items){
        return items;
    }
}
