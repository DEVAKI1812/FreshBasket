package com.freshbasket.model;

public class Product {
    private int productId;
    private String name;
    private String category;
    private String unitWeight;
    private double mrp;
    private double sellingPrice;
    private int stockQty;
    private String badgeTag;

    public Product() {}

    public Product(int productId, String name, String category, String unitWeight, double mrp, double sellingPrice, int stockQty, String badgeTag) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.unitWeight = unitWeight;
        this.mrp = mrp;
        this.sellingPrice = sellingPrice;
        this.stockQty = stockQty;
        this.badgeTag = badgeTag;
    }

    public int getDiscountPercent() {
        if (mrp <= sellingPrice || mrp <= 0) return 0;
        return (int) Math.round(((mrp - sellingPrice) / mrp) * 100.0);
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUnitWeight() { return unitWeight; }
    public void setUnitWeight(String unitWeight) { this.unitWeight = unitWeight; }
    public double getMrp() { return mrp; }
    public void setMrp(double mrp) { this.mrp = mrp; }
    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }
    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }
    public String getBadgeTag() { return badgeTag; }
    public void setBadgeTag(String badgeTag) { this.badgeTag = badgeTag; }
}
