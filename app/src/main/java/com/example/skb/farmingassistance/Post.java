package com.example.skb.farmingassistance;

public class Post {


    public Post(){



    }

    public Post(String date, String time, String uid, String fullname, String description, String postimage, String profileimage,String postimagename) {
        this.date = date;
        this.time = time;
        this.uid = uid;
        this.fullname = fullname;
        this.description = description;
        this.postimage = postimage;
        this.profileimage = profileimage;
        this.postimagename = postimagename;

    }

    String date, time, uid, fullname, description, postimage, profileimage,postimagename;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostimage() {
        return postimage;
    }

    public String getPostimagename() {
        return postimagename;
    }

    public void setPostimagename(String postimagename) {
        this.postimagename = postimagename;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}
