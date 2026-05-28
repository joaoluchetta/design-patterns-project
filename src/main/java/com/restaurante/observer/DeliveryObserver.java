package com.restaurante.observer;

import com.restaurante.model.Order;
import com.restaurante.model.OrderStatus;

public class DeliveryObserver implements  OrderObserver{
    private final String courierName;
    
    public DeliveryObserver(String courierName) {
        this.courierName = courierName;
    }
    
    @Override
    public void onStatusChanged(Order order) {
        OrderStatus status = order.getStatus();
        
        if (status == OrderStatus.OUT_FOR_DELIVERY) {
            System.out.printf(
                    "[Entregador %s] Saindo para entregar o pedido #%d%n",
                    courierName, order.getId()
            );
        } else if (status == OrderStatus.DELIVERED) {
            System.out.printf(
                    "[Entregador %s] Pedido #%d entregue com sucesso%n",
                    courierName, order.getId()
            );
        }
    }
}
