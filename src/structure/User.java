package structure;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class User {
    private String name;
    private List<User> following; //People who follow this user.
    private List<User> followed; //People followed by this user.

    public ArrayBlockingQueue<Message> getQueued_messages() {
        return queued_messages;
    }
    public void addQueued_messages(Message message){
        this.queued_messages.add(message);
    }
    private ArrayBlockingQueue<Message> queued_messages;

    public User(String name) {
        this.name = name;
        this.queued_messages = new ArrayBlockingQueue<Message>(10);
    }
    public void subscribe(User user){
        this.following.add(user);
    }
    public void unsubscribe(User user){
        this.following.remove(user);
    }
    public void addSubscriber(User user){
        this.followed.add(user);
    }
    public void removeSubscriber(User user){
        this.followed.remove(user);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getFollowing() {
        return following;
    }

    public void setFollowing(List<User> following) {
        this.following = following;
    }

    public List<User> getFollowed() {
        return followed;
    }

    public void setFollowed(List<User> followed) {
        this.followed = followed;
    }
}
