package com.restaurante.model;

import java.util.ArrayList;
import java.util.List;

import com.restaurante.strategy.DiscountStrategy;
import com.restaurante.strategy.NoDiscount;
import com.restaurante.observer.OrderObserver;

public class Order {
    private final int id;
    private final String clientName;
    private final List<Item> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.RECEIVED;

    // PATTERN STRATEGY: pluggable discount rule. Starts as NoDiscount (Null Object)
    // so the order always has a valid strategy, avoiding null checks in calculateTotal().
    private DiscountStrategy discountStrategy = new NoDiscount();

    // PATTERN OBSERVER: list of subscribers notified on every status change.
    private final List<OrderObserver> observers = new ArrayList<>();

    public Order(int id, String clientName) {
        this.id = id;
        this.clientName = clientName;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public double calculateSubtotal() {
        double total = 0.0;
        for (Item item : items) {
            total += item.getPrice();
        }
        return total;
    }

    // STRATEGY: delegates the discount calculation to whichever strategy is plugged in.
    // Order does NOT know which rule is being applied.
    public double calculateTotal() {
        return discountStrategy.apply(calculateSubtotal());
    }

    // OBSERVER: Subject API — subscribe / unsubscribe
    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        // Iterates over a copy to avoid ConcurrentModificationException
        // if an observer unsubscribes during notification.
        for (OrderObserver observer : new ArrayList<>(observers)) {
            observer.onStatusChanged(this);
        }
    }

    // OBSERVER: setStatus now automatically notifies all subscribed observers.
    public void setStatus(OrderStatus status) {
        this.status = status;
        notifyObservers();
    }

    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return clientName;
    }

    public List<Item> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public DiscountStrategy getDiscountStrategy() {
        return discountStrategy;
    }

    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }
}
