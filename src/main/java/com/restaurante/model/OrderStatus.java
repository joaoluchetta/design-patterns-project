package com.restaurante.model;

public enum OrderStatus {
    RECEIVED("Recebido"),
    PREPARING("Em Preparo"),
    OUT_FOR_DELEVIRY("Saiu para Entrega"),
    DELIVERED("Entregue"),
    CANCELED("Cancelado");

    public final String description;

    OrderStatus(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
