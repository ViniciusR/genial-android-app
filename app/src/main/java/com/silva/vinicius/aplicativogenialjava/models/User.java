package com.silva.vinicius.aplicativogenialjava.models;

import android.support.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.silva.vinicius.aplicativogenialjava.dao.UserDAO;

public class User {

    private String id;
    private String name;
    private String photo_url;
    private String birthday;
    private String gender;
    private Boolean updated;

    public User() {
    }

    public User(String id, String name, @Nullable String photo_url, @Nullable String birthday, @Nullable String gender, Boolean updated) {
        this.id = id;
        this.name = name;
        this.photo_url = photo_url;
        this.birthday = birthday;
        this.gender = gender;
        this.updated = updated;
    }

    public User(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Boolean getUpdated() {
        return updated;
    }

    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public void save() {
        UserDAO.saveIfNotExists(this);
    }

    public void delete() {
        UserDAO.delete(this.getId());
    }

    public void update() {
        UserDAO.update(this);
    }
}
