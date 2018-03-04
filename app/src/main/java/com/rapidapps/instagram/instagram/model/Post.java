package com.rapidapps.instagram.instagram.model;

/**
 * Created by Tofiq Quadri on 25-02-2018.
 */

public class Post {

    private String caption_text;
    private String image;
    private String userid;
    private String username;

    public Post() {
    }

    public String getCaption_text() {
        return caption_text;
    }

    public void setCaption_text(String caption_text) {
        this.caption_text = caption_text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
