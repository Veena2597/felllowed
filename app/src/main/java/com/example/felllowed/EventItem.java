package com.example.felllowed;
public class EventItem {
    private String eventName;
    private String eventDate;
    private String eventTime;
    private String eventDes;
    private String userName;

    public String getEventName() {
        return eventName;
    }
    public String getEventDate() {
        return eventDate;
    }
    public String getEventTime() {
        return eventTime;
    }
    public String getEventDes() {
        return eventDes;
    }
    public String getUserName(){
        return userName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public void setEventDes(String eventDes) {
        this.eventDes = eventDes;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}