import structure.Message;
import structure.Tag;
import structure.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

public class ProjetReseaux {
    public static final ArrayList<Message> messages = new ArrayList<>();
    public static final ArrayList<User> users = new ArrayList<>();
    public static final ArrayList<Tag> tags = new ArrayList<>();
    public static final ArrayList<User> connected_users = new ArrayList<>();


    public static ArrayList<Message> getMessages(){
        //Sorting messages by date before giving them.
        Collections.sort(messages);
        return messages;
    }

    public static void addMessage(Message message){
        messages.add(message);
    }
    public static ArrayList<User> getUsers(){
        return users;
    }
    public static void addUser(User user){
        users.add(user);
    }
    public static ArrayList<Tag> getTags(){
        return tags;
    }
    public static void addTag(Tag tag){
        tags.add(tag);
    }
    public static ArrayList<User> getConnected_users(){
        return connected_users;
    }
    public static void addConnected_user(User user){
        connected_users.add(user);
    }
    public static void removeConnected_user(User user){
        connected_users.remove(user);
    }
    public static void removeMessage(Message message){
        messages.remove(message);
    }
    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(12345);
            server.setReuseAddress(true);
            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected"
                        + client.getInetAddress()
                        .getHostAddress());
                microblogamu_central clientSock
                        = new microblogamu_central(client);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}