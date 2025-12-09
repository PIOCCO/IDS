package org.example.models;

public class Account {
    private String name;
    private String email;
    private String color;
    private boolean selected;

    public Account(String name, String email, String color) {
        this.name = name;
        this.email = email;
        this.color = color;
        this.selected = false;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}