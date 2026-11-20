package com.freshbasket.util;

import com.freshbasket.model.CartItem;
import com.freshbasket.model.Product;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CartManager {
    private static final CartManager INSTANCE = new CartManager();
    private final Map<Integer, CartItem> items = new LinkedHashMap<>();

    private CartManager() {}

    public static CartManager getInstance() {
        return INSTANCE;
    }

    public void addProduct(Product product) {
        CartItem existing = items.get(product.getProductId());
        if (existing != null) {
            if (existing.getQuantity() < product.getStockQty()) {
                existing.setQuantity(existing.getQuantity() + 1);
            }
        } else if (product.getStockQty() > 0) {
            items.put(product.getProductId(), new CartItem(product, 1));
        }
    }

    public void decrementProduct(int productId) {
        CartItem existing = items.get(productId);
        if (existing != null) {
            if (existing.getQuantity() > 1) {
                existing.setQuantity(existing.getQuantity() - 1);
            } else {
                items.remove(productId);
            }
        }
    }

    public int getQuantity(int productId) {
        CartItem item = items.get(productId);
        return item == null ? 0 : item.getQuantity();
    }

    public Collection<CartItem> getItems() { return items.values(); }
    public int getTotalItemCount() { return items.values().stream().mapToInt(CartItem::getQuantity).sum(); }
    public double getSubtotal() { return items.values().stream().mapToDouble(CartItem::getLineTotal).sum(); }
    public boolean isEmpty() { return items.isEmpty(); }
    public void clear() { items.clear(); }
}
