package structure;

import java.util.List;

public class Tag {
    public Tag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public List<User> getFollowing() {
        return following;
    }

    private List<User> following; //People who follow this user.

    public void subscribe(User user){
        this.following.add(user);
    }
    public void unsubscribe(User user){
        this.following.remove(user);
    }
}
