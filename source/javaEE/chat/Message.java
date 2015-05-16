package javaEE.chat;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Таня on 14.05.2015.
 */
public class Message implements Serializable{

    private String message;
    private String time;


    public Message(){
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        time = df.format(new Date());
    }

    public synchronized void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }
}
