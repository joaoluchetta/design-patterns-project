package com.restaurante.strategy;

import java.util.ArrayList;
import java.util.List;

import com.restaurante.model.Item;
import com.restaurante.model.OrderStatus;
import com.restaurante.strategy.DiscountStrategy;
import com.restaurante.strategy.NoDiscount;

/**
 * Agregado central do dominio: representa um pedido feito por um cliente.
 *
 * Versao da FASE 2: agora delega o calculo do desconto a uma EstrategiaDesconto
 * plugavel (padrao Strategy), atraves do metodo calcularTotal().
 * Na FASE 3 (Observer) esta classe vai passar a notificar observadores quando
 * o status muda.
 */
public class Order {
    
    private final int id;
    private final String customerName;
    private final List<Item> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.RECEIVED;
    
    // PADRAO STRATEGY: o pedido guarda uma referencia para uma estrategia de
    // desconto, mas nao conhece a regra concreta. Comeca como SemDesconto
    // (que funciona como Null Object) para o pedido sempre ter uma estrategia
    // valida, dispensando checagens de null em calcularTotal().
    private DiscountStrategy discountStrategy = new NoDiscount();
    
    public Order(int id, String customerName) {
        this.id = id;
        this.customerName = customerName;
    }
    
    public void addItem(Item item) {
        items.add(item);
    }
    
    /** Soma o preco de todos os itens, sem descontos ou frete. */
    public double calculateSubtotal() {
        double total = 0.0;
        for (Item item : items) {
            total += item.getPrice();
        }
        return total;
    }
    
    /**
     * Calcula o total a pagar aplicando a estrategia de desconto vigente.
     * O Pedido NAO sabe como o desconto e calculado: apenas delega a regra
     * a estrategia plugada via setEstrategiaDesconto(...).
     */
    public double calculateTotal() {
        return discountStrategy.apply(calculateSubtotal());
    }
    
    public int getId() {
        return id;
    }
    
    public String getCustomerName() {
        return customerName;
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
    
    public DiscountStrategy getDiscountStrategy() {
        return discountStrategy;
    }
    
    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }
}