package com.restaurante.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private final int id;
    private final String clientName;
    private final List<Item> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.RECEIVED;

    public Order(int id, String clientName){
        this.id = id;
        this.clientName = clientName;
    }

    public void addItem(Item item){
        items.add(item);
    }

    public double calculateSubtotal() {
        double total = 0.0;
        for(Item item : items){
            total += item.getPrice();
        }
        return total;
    }

    public int getId() {
        return id;
    }

    public String getClientName() {
        return clientName;
    }

    public List<Item> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
