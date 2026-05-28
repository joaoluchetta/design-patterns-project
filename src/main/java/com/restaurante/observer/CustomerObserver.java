package com.restaurante.observer;

import com.restaurante.model.Order;

/**
 * Observer que representa o aplicativo do cliente.
 * Notifica o cliente sobre QUALQUER mudanca de status (ele quer acompanhar tudo).
 */

public class CustomerObserver implements  OrderObserver{
    private final String customerName;
    
    public CustomerObserver(String customerName) {
        this.customerName = customerName;
    }
    
    @Override
    public void onStatusChanged(Order order) {
        System.out.printf(
                "[App de %s] Seu pedido #%d agora esta: %s%n",
                customerName,
                order.getId(),
                order.getStatus().getDescription()
        );
    }
}
