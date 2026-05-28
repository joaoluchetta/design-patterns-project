package com.restaurante.strategy;

public class PercentageCoupon implements DiscountStrategy {
    
    private final double percentage;
    
    /**
     * @param percentage valor entre 0.0 e 1.0 (ex: 0.10 = 10%)
     */
    public PercentageCoupon(double percentage) {
        if (percentage < 0.0 || percentage > 1.0) {
            throw new IllegalArgumentException(
                    "Percentual deve estar entre 0.0 e 1.0 (recebido: " + percentage + ")"
            );
        }
        this.percentage = percentage;
    }
    
    @Override
    public double apply(double subtotal) {
        return subtotal * (1.0 - percentage);
    }
    
    @Override
    public String getDescription() {
        return String.format("Cupom de %.0f%% de desconto", percentage * 100);
    }
}
