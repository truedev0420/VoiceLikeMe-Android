package com.appbestsmile.voicelikeme.chat;

public class TopicItem {

    private String title;
    private String createdDate;
    private String topic_id;

    public TopicItem(String topic_id, String title, String createdDate){
        this.topic_id = topic_id;
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

    public void setTopicId(String topic_id){
        this.topic_id = topic_id;
    }

    public String getTopicId(){
        return this.topic_id;
    }
}
