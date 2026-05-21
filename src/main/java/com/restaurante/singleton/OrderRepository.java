package com.restaurante.singleton;

import com.restaurante.model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepository {

    private static OrderRepository instance;
    private final List<Order> orders = new ArrayList<>();

    //private constructor: prevents the creation of instances outside the class.
    private  OrderRepository(){}

    //static method to get the instance
    public static OrderRepository getInstance(){
        if(instance == null){
            instance = new OrderRepository();
        }
        return instance;
    }

    public void save(Order order){
        orders.add(order);
    }

    public Optional<Order> searchForId(int id){
        return orders.stream()
                .filter(p -> p.getId() == id)
                .findFirst();
    }

    public List<Order> listAll(){
        return new ArrayList<>(orders);
    }

    public int quantity() {
        return orders.size();
    }

}
