package com.restaurante.strategy;

/**
 * PADRAO STRATEGY - Interface (a "Strategy").
 *
 * Define o contrato que TODAS as regras de calculo de desconto devem seguir.
 * O Pedido (o "Context") guarda uma referencia desta interface e delega o
 * calculo a ela, sem saber qual implementacao concreta esta plugada.
 *
 * E exatamente isto que permite trocar a regra de desconto em tempo de
 * execucao sem modificar a classe Pedido (principio Open/Closed do SOLID).
 */

public interface DiscountStrategy {
    /**
     * Aplica a regra desta estrategia sobre o subtotal e devolve o total final.
     *
     * @param subtotal valor bruto dos itens, sem desconto
     * @return valor a pagar depois da regra desta estrategia
     */
    double apply(double subtotal);
    
    /** Descricao curta da estrategia, util para logs e notificacoes. */
    String getDescription();
}
