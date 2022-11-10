package com.example.demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserCondition
{
    private String chatId;
    private String condition;
    private String language;
    private String name;
    private List<String>userCart = new ArrayList<>();

    public List<String> getUserCart() {
        return userCart;
    }

    public void setUserCart(List<String> userCart) {
        this.userCart = userCart;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "UserCondition{" +
                "chatId='" + chatId + '\'' +
                ", condition='" + condition + '\'' +
                ", language='" + language + '\'' +
                ", name='" + name + '\'' +
                ", userCart=" + userCart +
                '}';
    }
}
