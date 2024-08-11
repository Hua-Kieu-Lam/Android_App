package com.example.models;

import java.io.Serializable;

public class Product implements Serializable {
    private int productId;
    private String productName;
    private String productImg;
    private double productCalo;
    private double productCarb;
    private double productFat;
    private double productProtein;
    private double productMoisture;
    private String firebaseId;

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }


    public Product() {
    }

    public Product(int productId, String productName, String productImg, double productCalo, double productCarb, double productFat, double productProtein, double productMoisture) {
        this.productId = productId;
        this.productName = productName;
        this.productImg = productImg;
        this.productCalo = productCalo;
        this.productCarb = productCarb;
        this.productFat = productFat;
        this.productProtein = productProtein;
        this.productMoisture = productMoisture;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImg() {
        return productImg;
    }

    public void setProductImg(String productImg) {
        this.productImg = productImg;
    }

    public double getProductCalo() {
        return productCalo;
    }

    public void setProductCalo(double productCalo) {
        this.productCalo = productCalo;
    }

    public double getProductCarb() {
        return productCarb;
    }

    public void setProductCarb(double productCarb) {
        this.productCarb = productCarb;
    }

    public double getProductFat() {
        return productFat;
    }

    public void setProductFat(double productFat) {
        this.productFat = productFat;
    }

    public double getProductProtein() {
        return productProtein;
    }

    public void setProductProtein(double productProtein) {
        this.productProtein = productProtein;
    }

    public double getProductMoisture() {
        return productMoisture;
    }

    public void setProductMoisture(double productMoisture) {
        this.productMoisture = productMoisture;
    }
}
