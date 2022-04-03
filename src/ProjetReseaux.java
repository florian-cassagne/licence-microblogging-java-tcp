import structure.Message;
import structure.Tag;
import structure.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ProjetReseaux {
    private final ArrayList<Message> messages = new ArrayList<>();
    private final ArrayList<User> users = new ArrayList<>();
    private final ArrayList<Tag> tags = new ArrayList<>();
    private final ArrayList<User> connected_users = new ArrayList<>();


    public ArrayList<Message> getMessages(){
        return messages;
    }
    public void addMessage(Message message){
        this.messages.add(message);
    }
    public ArrayList<User> getUsers(){
        return users;
    }
    public void addUser(User user){
        this.users.add(user);
    }
    public ArrayList<Tag> getTags(){
        return tags;
    }
    public void addTag(Tag tag){
        this.tags.add(tag);
    }
    public ArrayList<User> getConnected_users(){
        return connected_users;
    }
    public void addConnected_user(User user){
        this.connected_users.add(user);
    }
    public void removeConnected_user(User user){
        this.connected_users.remove(user);
    }
    public void removeMessage(Message message){
        this.messages.remove(message);
    }
    public void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(12345);
            server.setReuseAddress(true);
            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected"
                        + client.getInetAddress()
                        .getHostAddress());
                Server clientSock
                        = new Server(client, this);
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