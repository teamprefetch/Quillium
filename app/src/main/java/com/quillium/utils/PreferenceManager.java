package com.quillium.utils;

public class PreferenceManager {
    private String username;
    private String email;
    private int age;

    // Constructor
    public PreferenceManager(String username, String email, int age) {
        this.username = username;
        this.email = email;
        this.age = age;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    // Additional methods if needed

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                '}';
    }
}
