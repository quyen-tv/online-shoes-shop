package com.prm392.onlineshoesshop.model;

public class Brand {
    private int id;
    private String picUrl  ;
    private String title;

    public Brand() {
    }

    public Brand(int id, String title, String picUrl) {
        this.id = id;
        this.title = title;
        this.picUrl = picUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
