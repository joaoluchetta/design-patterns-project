package com.restaurante.observer;

import com.restaurante.model.Order;
import com.restaurante.model.OrderStatus;

/**
 * Observer da cozinha do restaurante.
 *
 * Reage SOMENTE aos status que interessam ao preparo da comida. Os demais
 * sao ignorados. Isso ilustra um ponto importante do Observer: cada
 * observador pode filtrar os eventos do seu proprio interesse, sem que o
 * Subject (Order) precise saber disso.
 */

public class KitchenObserver implements  OrderObserver{
    @Override
    public void onStatusChanged(Order order) {
        OrderStatus status = order.getStatus();
        
        if (status == OrderStatus.PREPARING) {
            System.out.printf(
                    "[Cozinha] Iniciando preparo do pedido #%d para %s%n",
                    order.getId(), order.getCustomerName()
            );
        } else if (status == OrderStatus.CANCELLED) {
            System.out.printf(
                    "[Cozinha] Pedido #%d cancelado, parar preparo%n",
                    order.getId()
            );
        }
        // demais status nao sao do interesse da cozinha
    }
}
