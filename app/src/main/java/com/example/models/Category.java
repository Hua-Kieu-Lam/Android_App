package com.example.models;

public class Category {
    private int categoryId;
    private String categoryImg;
    private String categoryName;

    public Category() {
    }

    public Category(int categoryId, String categoryImg, String categoryName) {
        this.categoryId = categoryId;
        this.categoryImg = categoryImg;
        this.categoryName = categoryName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
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
