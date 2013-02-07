package comm;

import java.util.Date;

public class Message {
    String content;
    Date time;
    public Message next;
    
    public Message(String content) {
        this.content = content;
        time = new Date();
        next = null;
    }
    
    /* linked list utility functions */
    
    public void append(Message m) {
        //add a message to the end of the list
        if(next == null) {
            next = m;
        } else {
            next.append(m);
        }
    }
    
    public Message last() {
        if(next == null) return this;
        return next.last();
    }
    
    /* accessors */
    
    public String getContent() {
        return content;
    }
    public void setContent(String newContent) {
        content = newContent;
    }
    
    public Date getTime() {
        return time;
    }
    public void setTime(Date newTimestamp) {
        time = newTimestamp;
    }
    public void timestamp() {
        time = new Date();
    }
    
}
