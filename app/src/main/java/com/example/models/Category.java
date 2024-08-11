package com.example.models;

public class Category {
    private String categoryId;
    private String categoryImg;
    private String categoryName;

    public Category() {
    }

    public Category(String categoryImg, String categoryName) {
        this.categoryImg = categoryImg;
        this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryImg() {
        return categoryImg;
    }

    public void setCategoryImg(String categoryImg) {
        this.categoryImg = categoryImg;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
