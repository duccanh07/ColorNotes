package com.fivestars.colornotes.model;

import com.google.firebase.Timestamp;

public class Note {
    private String title;
    private String content;
    private Timestamp createDate;
    private Timestamp editDate;
    private boolean complete;
    private boolean priority;
    private Timestamp expiredDate;
    private boolean expired;
    private String count;

    public Note(){}

    public Note(String title, String content, Timestamp createDate, boolean complete, Timestamp editDate, boolean priority, Timestamp expiredDate, boolean expired, String count){
        this.title = title;
        this.content = content;
        this.createDate = createDate;
        this.editDate = editDate;
        this.complete = complete;
        this.priority = priority;
        this.expiredDate = expiredDate;
        this.expired = expired;
        this.count = count;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public boolean getComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }


    public Timestamp getEditDate() {
        return editDate;
    }

    public void setEditDate(Timestamp editDate) {
        this.editDate = editDate;
    }

    @Override
    public String toString() {
        return "Note{" +
                "title='" + title + '\'' +
                ", content=" + content +
                ", createDate=" + createDate +
                ", editDate=" + editDate +
                ", complete='" + complete + '\'' +
                '}';
    }

    public boolean getPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public Timestamp getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(Timestamp expiredDate) {
        this.expiredDate = expiredDate;
    }

    public boolean getExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}