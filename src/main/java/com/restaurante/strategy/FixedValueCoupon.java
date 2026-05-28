package com.restaurante.strategy;

/**
 * Cupom que subtrai um valor fixo em reais do subtotal.
 * Ex.: new CupomValorFixo(5.00) -> R$ 5,00 de desconto.
 *
 * Se o valor do desconto for maior que o subtotal, o total minimo e zero.
 */

public class FixedValueCoupon implements DiscountStrategy {
    private final double value;
    
    public FixedValueCoupon(double value) {
        if (value < 0.0) {
            throw new IllegalArgumentException(
                    "Valor do desconto nao pode ser negativo (recebido: " + value + ")"
            );
        }
        this.value = value;
    }
    
    @Override
    public double apply(double subtotal) {
        return Math.max(0.0, subtotal - value);
    }
    
    @Override
    public String getDescription() {
        return String.format("Cupom de R$ %.2f de desconto", value);
    }
}
