package com.restaurante.observer;

import com.restaurante.model.Order;


/**
 * PADRAO OBSERVER - Interface (o "Observer").
 *
 * Contrato para qualquer parte do sistema interessada em ser notificada
 * quando um Order muda de status. O Order (o "Subject") mantem uma lista
 * destes observadores e chama onStatusChanged(...) em todos sempre que
 * setStatus(...) for invocado.
 */

public interface OrderObserver {
    /**
     * Chamado automaticamente pelo Order toda vez que o status muda.
     * O proprio pedido e passado como parametro para o observer ter acesso
     * a todas as informacoes (id, cliente, novo status, total, etc.).
     */
    void onStatusChanged(Order order);
}
