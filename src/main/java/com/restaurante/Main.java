package com.restaurante;

import com.restaurante.model.Order;
import com.restaurante.model.OrderStatus;
import com.restaurante.model.Item;
import com.restaurante.singleton.OrderRepository;
import com.restaurante.strategy.DiscountStrategy;
import com.restaurante.strategy.NoDiscount;
import com.restaurante.strategy.PercentageCoupon;
import com.restaurante.strategy.FixedValueCoupon;
import com.restaurante.observer.CustomerObserver;
import com.restaurante.observer.KitchenObserver;
import com.restaurante.observer.DeliveryObserver;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Ponto de entrada INTERATIVO da aplicacao.
 *
 * Fluxo:
 *   1. O cliente monta o pedido escolhendo itens de um cardapio.
 *   2. O proprietario escolhe o tipo e o valor do desconto (STRATEGY em runtime).
 *   3. O pedido e RECEBIDO e entra EM PREPARO por ate 7 segundos.
 *   4. Durante o preparo, o cliente pode CANCELAR digitando 'c' + Enter.
 *      Se o tempo esgotar sem resposta, o pedido segue sozinho ate ENTREGUE.
 *      Cada mudanca de status dispara as notificacoes (OBSERVER).
 *   5. Pode-se criar varios pedidos; todos vivem na mesma instancia (SINGLETON).
 *
 * Observacao tecnica: para conseguir ler do teclado COM tempo-limite (algo que
 * o Scanner sozinho nao oferece), uma thread leitora dedicada coloca as linhas
 * digitadas numa fila (inputQueue), e o programa principal le dessa fila de
 * forma bloqueante (take) ou com timeout (poll). Isso e apenas um detalhe de
 * I/O na camada de interface; nao afeta os padroes de projeto.
 */
public class Main {

    private static final String[] MENU_NAMES = {
            "Hamburguer", "Refrigerante", "Pizza Grande", "Batata Frita", "Suco Natural"
    };
    private static final double[] MENU_PRICES = {
            28.00, 8.00, 45.00, 15.00, 10.00
    };

    private static final int CANCEL_WINDOW_SECONDS = 10;

    private static final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        iniciarLeitorDeEntrada();

        OrderRepository repository = OrderRepository.getInstance();
        int nextId = 1;

        System.out.println("==============================================");
        System.out.println("   SISTEMA DE PEDIDOS - RESTAURANTE/DELIVERY");
        System.out.println("==============================================");

        boolean continuar = true;
        while (continuar) {
            System.out.print("\nNome do cliente: ");
            String clientName = readLine().trim();
            if (clientName.isEmpty()) {
                clientName = "Cliente " + nextId;
            }

            Order order = new Order(nextId, clientName);

            // 1) Cliente escolhe os itens do cardapio
            escolherItens(order);

            if (order.getItems().isEmpty()) {
                System.out.println("Nenhum item escolhido, pedido descartado.");
            } else {
                // 2) Proprietario escolhe o desconto (STRATEGY em runtime)
                order.setDiscountStrategy(escolherDesconto());

                // OBSERVER: inscreve os interessados no ciclo de vida
                order.addObserver(new CustomerObserver(clientName));
                order.addObserver(new KitchenObserver());
                order.addObserver(new DeliveryObserver("iFood"));

                repository.save(order);

                imprimirResumo(order);

                // 3, 4) Ciclo de vida com janela de cancelamento por tempo
                processarCicloDeVida(order);

                nextId++;
            }

            // 5) Novo pedido?
            System.out.print("\nDeseja fazer outro pedido? (s/n): ");
            String outro = readLine().trim().toLowerCase();
            continuar = outro.equals("s") || outro.equals("sim");
        }

        System.out.println("\n==============================================");
        System.out.println("Pedidos no repositorio (Singleton): " + repository.quantity());
        System.out.println("Obrigado! Ate a proxima.");
    }

    /**
     * RECEBIDO -> EM PREPARO (janela de 10s para cancelar) -> ENTREGUE / CANCELADO.
     * Cada setStatus dispara automaticamente o OBSERVER.
     */
    private static void processarCicloDeVida(Order order) {
        System.out.println("\n--- Acompanhamento do pedido ---");

        order.setStatus(OrderStatus.RECEIVED);
        order.setStatus(OrderStatus.PREPARING);

        System.out.println("\n=======================================================\n");
        System.out.println("Seu pedido esta EM PREPARO. Digite 'c' e Enter em ate "
                + CANCEL_WINDOW_SECONDS + "s para CANCELAR (ou aguarde a entrega)...");
        System.out.println("\n=======================================================\n");

        if (aguardarCancelamento()) {
            order.setStatus(OrderStatus.CANCELLED);
        } else {
            System.out.println("Tempo de preparo concluido sem cancelamento. Saindo para entrega!");
            order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            order.setStatus(OrderStatus.DELIVERED);
        }
    }

    /**
     * Espera ate CANCEL_WINDOW_SECONDS por uma resposta, com contagem regressiva.
     * Retorna true se o usuario pedir cancelamento dentro da janela.
     */
    private static boolean aguardarCancelamento() {
        for (int s = CANCEL_WINDOW_SECONDS; s >= 1; s--) {
            System.out.println("  ... preparando (" + s + "s restantes para cancelar)");
            String resp = poll(1);
            if (resp != null) {
                String r = resp.trim().toLowerCase();
                return r.equals("c") || r.equals("s") || r.equals("cancelar");
            }
        }
        return false;
    }

    /** Mostra o cardapio e deixa o usuario adicionar itens ate encerrar. */
    private static void escolherItens(Order order) {
        boolean adicionando = true;
        while (adicionando) {
            System.out.println("\n--- Cardapio ---");
            for (int i = 0; i < MENU_NAMES.length; i++) {
                System.out.printf("  %d) %-15s R$ %.2f%n", i + 1, MENU_NAMES[i], MENU_PRICES[i]);
            }
            System.out.println("  0) Encerrar escolha de itens");
            System.out.print("Escolha um item: ");

            int op = lerInteiro();
            if (op == 0) {
                adicionando = false;
            } else if (op >= 1 && op <= MENU_NAMES.length) {
                int idx = op - 1;
                order.addItem(new Item(MENU_NAMES[idx], MENU_PRICES[idx]));
                System.out.println("  + " + MENU_NAMES[idx] + " adicionado.");
            } else {
                System.out.println("  Opcao invalida, tente novamente.");
            }
        }
    }

    /** Menu de descontos: o proprietario escolhe a estrategia e seus parametros. */
    private static DiscountStrategy escolherDesconto() {
        while (true) {
            System.out.println("\n--- Desconto a aplicar (proprietario) ---");
            System.out.println("  1) Sem desconto");
            System.out.println("  2) Cupom percentual (ex: 10%)");
            System.out.println("  3) Cupom de valor fixo (ex: R$ 5,00)");
            System.out.print("Escolha o desconto: ");

            int op = lerInteiro();
            switch (op) {
                case 1:
                    return new NoDiscount();
                case 2:
                    System.out.print("Percentual (ex: 10 para 10%): ");
                    double pct = lerDecimal() / 100.0;
                    try {
                        return new PercentageCoupon(pct);
                    } catch (IllegalArgumentException e) {
                        System.out.println("  " + e.getMessage());
                    }
                    break;
                case 3:
                    System.out.print("Valor em reais (ex: 5): ");
                    double val = lerDecimal();
                    try {
                        return new FixedValueCoupon(val);
                    } catch (IllegalArgumentException e) {
                        System.out.println("  " + e.getMessage());
                    }
                    break;
                default:
                    System.out.println("  Opcao invalida, tente novamente.");
            }
        }
    }

    private static void imprimirResumo(Order order) {
        System.out.println("\n=== Resumo do Pedido #" + order.getId() + " - " + order.getCustomerName() + " ===");
        for (Item item : order.getItems()) {
            System.out.println("  - " + item);
        }
        System.out.printf("Subtotal: R$ %.2f%n", order.calculateSubtotal());
        System.out.println("Desconto: " + order.getDiscountStrategy().getDescription());
        System.out.printf("TOTAL:    R$ %.2f%n%n", order.calculateTotal());
    }

    // ===== Infraestrutura de entrada (thread leitora + fila) =====

    /** Inicia uma thread daemon que le linhas do teclado e as enfileira. */
    private static void iniciarLeitorDeEntrada() {
        Thread reader = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                inputQueue.offer(sc.nextLine());
            }
        });
        reader.setDaemon(true);
        reader.start();
    }

    /** Leitura bloqueante: espera ate uma linha estar disponivel. */
    private static String readLine() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    /** Leitura com tempo-limite: devolve a linha digitada ou null se expirar. */
    private static String poll(int seconds) {
        try {
            String line = inputQueue.poll(seconds, TimeUnit.SECONDS);
            return line;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static int lerInteiro() {
        while (true) {
            String linha = readLine().trim();
            try {
                return Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.print("  Digite um numero valido: ");
            }
        }
    }

    private static double lerDecimal() {
        while (true) {
            String linha = readLine().trim().replace(",", ".");
            try {
                return Double.parseDouble(linha);
            } catch (NumberFormatException e) {
                System.out.print("  Digite um valor valido: ");
            }
        }
    }
}