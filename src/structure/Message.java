package structure;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message implements Comparable<Message> {
    private long id;
    private String content;

    public String getAuthor() {
        return author;
    }

    private final String author;
    private Date createdOn;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    private String tag;

    public Message(long id, String content, String author, Date createdOn) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.createdOn = createdOn;
        Pattern p = Pattern.compile("(#\\S+)");
        Matcher m = p.matcher(content);
        if (m.find()) this.tag = m.group(1);
        else this.tag = "";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public int compareTo(Message msg) {
        if (getCreatedOn() == null || msg.getCreatedOn() == null) {
            return 0;
        }
        return getCreatedOn().compareTo(msg.getCreatedOn());
    }
}
