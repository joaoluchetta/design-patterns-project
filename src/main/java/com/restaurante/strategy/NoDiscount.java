package com.restaurante.strategy;

/**
 * Estrategia "neutra": nao aplica nenhum desconto.
 *
 * Funciona tambem como Null Object: deixa o Pedido sempre com uma estrategia
 * valida, evitando checagens de null em calcularTotal(). Quando o cliente
 * nao tem cupom, esta e a estrategia que fica plugada no pedido.
 */

public class NoDiscount implements  DiscountStrategy{
    
    @Override
    public double apply(double subtotal) {
        return subtotal;
    }
    
    @Override
    public String getDescription() {
        return "Sem desconto";
    }
}
