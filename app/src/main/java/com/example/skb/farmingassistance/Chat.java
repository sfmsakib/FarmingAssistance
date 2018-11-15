package com.example.skb.farmingassistance;

public class Chat {

    public String date, time, from, message, type, isadmin;


    public Chat(String date, String time, String from, String message, String type, String isadmin) {
        this.date = date;
        this.time = time;
        this.from = from;
        this.message = message;
        this.type = type;
        this.isadmin = isadmin;

    }

    public Chat(){



    }

    public String getIsadmin() {
        return isadmin;
    }

    public void setIsadmin(String isadmin) {
        this.isadmin = isadmin;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

