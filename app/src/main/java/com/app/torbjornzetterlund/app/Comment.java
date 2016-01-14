package com.app.torbjornzetterlund.app;

public class Comment {
    private int id;
    private String author, author_email, content, profilePic, timeStamp;

    public Comment() {
    }

    public Comment(int id, String author, String author_email, String profilePic, String content, String timeStamp) {
        super();
        this.id = id;
        this.author = author;
        this.author_email = author_email;
        this.profilePic = profilePic;
        this.timeStamp = timeStamp;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorEmail() {
        return author_email;
    }

    public void setAuthorEmail(String author_email) {
        this.author_email = author_email;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String text) {
        this.content = text;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
