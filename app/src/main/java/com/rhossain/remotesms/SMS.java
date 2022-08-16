package com.rhossain.remotesms;

public class SMS {
    public SMS(String sender, String body, long datetime) {
        this.sender = sender;
        this.body = body;
        this.datetime = datetime;
    }

    private String sender;
    private String body;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    private long datetime;
}
