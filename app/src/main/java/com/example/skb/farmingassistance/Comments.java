package com.example.skb.farmingassistance;

public class Comments {

    public Comments(){



    }

    String uid, fullname, date,time, comment;

    public Comments(String uid, String fullname, String date, String time, String comment) {
        this.uid = uid;
        this.fullname = fullname;
        this.date = date;
        this.time = time;
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
