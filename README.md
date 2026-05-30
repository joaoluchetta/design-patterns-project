# Documento Técnico — Sistema de Pedidos de Restaurante / Delivery

**Aplicação de Padrões de Projeto (Design Patterns) em Java**

- **Autor:** _________________________
- **Disciplina:** _________________________
- **Data:** ____ / ____ / __________

---

## 1. Tema Escolhido

O projeto consiste em um **sistema de pedidos para um restaurante com serviço de delivery**, desenvolvido em Java e executado em modo console. A aplicação simula o fluxo real de um pedido: o cliente monta seu pedido escolhendo itens de um cardápio; o proprietário da loja define o desconto a ser aplicado; o sistema calcula o total e acompanha o pedido ao longo do seu ciclo de vida (recebido, em preparo, saiu para entrega e entregue), com a possibilidade de cancelamento dentro de uma janela de tempo durante o preparo.

O objetivo central é demonstrar, de forma correta e contextualizada, a aplicação de **três padrões de projeto distintos**, evidenciando o valor de cada um no design orientado a objetos.

---

## 2. Arquitetura Geral

O código é organizado em pacotes coesos, cada um com uma responsabilidade clara, sob o pacote base `com.restaurante`:

```
src/
└── com/restaurante/
    ├── Main.java          → ponto de entrada interativo
    ├── model/             → Order, Item, OrderStatus
    ├── singleton/         → OrderRepository
    ├── strategy/          → DiscountStrategy, NoDiscount,
    │                         PercentageCoupon, FixedValueCoupon
    └── observer/          → OrderObserver, CustomerObserver,
                              KitchenObserver, DeliveryObserver
```

- **`model`** — o domínio da aplicação: `Order` (pedido), `Item` (item do cardápio) e `OrderStatus` (enum com os estados do pedido).
- **`singleton`** — `OrderRepository`, o repositório em memória que guarda todos os pedidos.
- **`strategy`** — a interface `DiscountStrategy` e suas implementações de regra de desconto.
- **`observer`** — a interface `OrderObserver` e seus observadores concretos (cliente, cozinha e entregador).
- **`Main`** — ponto de entrada interativo, que orquestra a montagem do pedido e o seu ciclo de vida.

A classe `Order` é o **agregado central** e acumula dois papéis importantes: atua como *Context* do padrão **Strategy** (delegando o cálculo do desconto) e como *Subject* do padrão **Observer** (notificando os interessados quando seu status muda). O `OrderRepository`, por sua vez, garante que exista um único ponto de armazenamento dos pedidos em toda a aplicação.

---

## 3. Padrões de Projeto Aplicados

Os três padrões foram escolhidos deliberadamente para **cobrir categorias diferentes** do catálogo clássico do GoF (*Gang of Four*), demonstrando amplitude de aplicação.

| Padrão | Categoria (GoF) | Onde (classes) | Papel no sistema |
|--------|-----------------|----------------|------------------|
| **Singleton** | Criacional | `OrderRepository` | Instância única que armazena todos os pedidos |
| **Strategy** | Comportamental | `DiscountStrategy` e implementações | Regra de desconto plugável e trocável em runtime |
| **Observer** | Comportamental | `OrderObserver` e implementações | Notificações automáticas a cada mudança de status |

### 3.1. Singleton

**Onde:** na classe `OrderRepository`, no pacote `singleton`.

**Como:** o construtor da classe é privado, impedindo a criação de instâncias externas com `new`. Um campo estático guarda a única instância existente, e o método `getInstance()` a cria na primeira chamada (inicialização tardia) e devolve sempre a mesma referência a partir daí. O método é sincronizado para garantir segurança em ambientes com múltiplas threads.

**Por quê:** o repositório de pedidos é um recurso que precisa ser único e compartilhado por todo o sistema. Se cada parte da aplicação (caixa, cozinha, entregador) criasse o seu próprio repositório, cada uma teria uma lista de pedidos separada e inconsistente — a cozinha jamais enxergaria os pedidos registrados pelo caixa. O Singleton elimina esse risco na raiz, oferecendo um ponto de acesso único e garantindo uma só **fonte de verdade** para os dados.

### 3.2. Strategy

**Onde:** na interface `DiscountStrategy` e nas implementações `NoDiscount`, `PercentageCoupon` e `FixedValueCoupon` (pacote `strategy`), consumidas pela classe `Order`.

**Como:** a classe `Order` mantém uma referência ao tipo abstrato `DiscountStrategy` e delega o cálculo do total a ela, no método `calculateTotal()`, sem conhecer a regra concreta. A estratégia pode ser trocada em tempo de execução pelo método `setDiscountStrategy()`. No programa interativo, é o próprio usuário (no papel de proprietário) quem escolhe a estratégia e seus parâmetros no momento do pedido.

**Por quê:** o cálculo do valor final pode variar de muitas formas (sem desconto, percentual, valor fixo, e futuramente outras). Encapsular cada regra em uma classe própria evita um bloco de condicionais (`if/else`) extenso dentro do `Order` e permite adicionar novas regras sem alterar nenhuma linha do código existente — aplicando o princípio **Aberto/Fechado** (*Open/Closed*) do SOLID. Como bônus, a classe `NoDiscount` também funciona como um **Null Object**: por ser a estratégia padrão, dispensa verificações de nulo e garante que o pedido sempre tenha uma regra válida.

### 3.3. Observer

**Onde:** na interface `OrderObserver` e nas implementações `CustomerObserver`, `KitchenObserver` e `DeliveryObserver` (pacote `observer`). A classe `Order` desempenha o papel de *Subject*.

**Como:** o `Order` mantém uma lista de observadores inscritos (`addObserver` / `removeObserver`). Sempre que o método `setStatus()` é chamado, ele invoca `notifyObservers()`, que percorre a lista e chama `onStatusChanged()` em cada observador, passando o próprio pedido (modelo *push*). A notificação ocorre, portanto, de forma **automática** a cada mudança de status.

**Por quê:** várias partes do sistema precisam reagir quando um pedido muda de estado, e cada uma com um interesse diferente. O Observer **desacopla** completamente quem dispara o evento (o pedido) de quem reage a ele (cliente, cozinha, entregador). Adicionar um novo interessado — por exemplo, um painel administrativo — não exige nenhuma alteração na classe `Order`. Cada observador, ainda, filtra apenas os status que lhe interessam: a cozinha reage ao preparo e ao cancelamento, o entregador à saída para entrega e à entrega, e o cliente a todas as mudanças.

---

## 4. Diagrama de Classes (UML Simplificado)

O diagrama abaixo apresenta as principais classes e seus relacionamentos. As cores agrupam os elementos por padrão: **verde** para o Singleton, **amarelo** para o Strategy e **rosa** para o Observer; o **cinza** representa o núcleo do domínio.

![Diagrama de classes do sistema de pedidos](docs/uml.png)

*Figura 1 — Diagrama de classes do sistema de pedidos.*

**Notação utilizada:** linha tracejada com seta vazada indica *realização* (implementação de interface); seta sólida indica *associação/uso*; e o losango vazado indica *agregação* (uma classe contém referências a objetos de outra).

---

## 5. Fluxo de Funcionamento

A execução interativa segue as etapas:

1. O cliente informa seu nome e monta o pedido escolhendo itens do cardápio (pode adicionar vários itens).
2. O proprietário escolhe a estratégia de desconto e seus parâmetros — é aqui que o padrão **Strategy** é definido em tempo de execução.
3. O sistema exibe o resumo do pedido (itens, subtotal, desconto aplicado e total).
4. O pedido é **Recebido** e entra **Em Preparo**. Durante o preparo, abre-se uma **janela de 7 segundos**, com contagem regressiva, em que o cliente pode cancelar o pedido digitando um comando.
5. Se o cliente cancelar dentro da janela, o pedido vai para **Cancelado**; se o tempo se esgotar sem resposta, o pedido segue automaticamente para **Saiu para Entrega** e, por fim, **Entregue**.
6. Cada mudança de status dispara, via **Observer**, as notificações para o cliente, a cozinha e o entregador.

> A janela de cancelamento por tempo é implementada com uma *thread* leitora dedicada que enfileira a entrada do teclado, permitindo a leitura com tempo-limite (recurso que o `Scanner` sozinho não oferece). Trata-se de um detalhe de entrada/saída na camada de interface (`Main`), que não afeta a estrutura dos padrões de projeto.

---

## 6. Como Executar

Pré-requisito: **JDK 17+** instalado.

Pela linha de comando, a partir da raiz do projeto:

```bash
# compilar
javac -d out $(find src -name "*.java")

# executar
java -cp out com.restaurante.Main
```

Ou, pelo **IntelliJ IDEA**: abra o projeto, marque a pasta `src` como *Sources Root* (se necessário) e execute a classe `Main` pela seta verde ao lado do método `main`. Use o console da aba *Run* para digitar as respostas.

---

## 7. Considerações Finais

Os três padrões aplicados resolvem problemas **reais e distintos** do sistema, e foram escolhidos de modo a cobrir categorias diferentes do GoF: o **Singleton** (criacional) controla a criação e o compartilhamento de um recurso único; o **Strategy** e o **Observer** (comportamentais) tratam, respectivamente, do encapsulamento de algoritmos intercambiáveis e da comunicação desacoplada entre objetos.

O ponto central do projeto é perceber como os padrões **colaboram entre si**: o `Order` serve simultaneamente de *Context* para o Strategy e de *Subject* para o Observer, enquanto o `OrderRepository` (Singleton) sustenta a persistência em memória de todos os pedidos. Essa colaboração mostra que padrões de projeto não são técnicas isoladas, mas peças que se encaixam para produzir um design flexível e de fácil manutenção.

Como evolução natural, a aplicação foi projetada para receber, em uma fase posterior, mais quatro padrões (**Factory Method** e **Abstract Factory** para a criação dos itens e pedidos, **Decorator** para adicionais nos itens e **Facade** para simplificar a orquestração de todo o fluxo), completando os sete padrões previstos e tornando o projeto uma vitrine ainda mais completa para portfólio.
