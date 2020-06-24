package com.appbestsmile.voicelikeme.chat;

public class TopicItem {

    private String title;
    private String createdDate;

    public TopicItem(String title, String createdDate){
        this.title = title;
        this.createdDate = createdDate;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){
        return this.title;
    }

    public void setCreatedDate(String createdDate){
        this.createdDate = createdDate;
    }

    public String getCreatedDate(){
        return this.createdDate;
    }
}
