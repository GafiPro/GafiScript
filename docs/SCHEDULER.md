# Scheduler

O scheduler do GafiScript existe para tornar operações temporais possíveis sem criar cadeias de command blocks.

## Ticks

Um tick é uma atualização do servidor. Para a temporização normal do Minecraft, 20 ticks correspondem aproximadamente a um segundo.

Exemplo:

    Gafi.scheduler().delayTicks(20, () -> {
        Gafi.broadcast("Passou aproximadamente um segundo.");
    });

## nextTick

Executa no próximo ciclo do servidor.

    Gafi.scheduler().nextTick(() -> {
        // ...
    });

## delayTicks

Executa depois de uma quantidade de ticks.

    GafiTask task = Gafi.scheduler().delayTicks(60, () -> {
        // ...
    });

## delaySeconds

Atalho que converte segundos para ticks.

    Gafi.scheduler().delaySeconds(3, () -> {
        // ...
    });

## repeatTicks

Cria uma tarefa periódica.

    GafiTask task =
        Gafi.scheduler().repeatTicks(20, () -> {
            update();
        });

## repeatSeconds

    GafiTask task =
        Gafi.scheduler().repeatSeconds(1, () -> {
            update();
        });

## Cancelamento

Todas as tasks podem ser canceladas:

    task.cancel();

Uma task cancelada deixa de executar.

## runSync

Agenda uma operação no servidor:

    Gafi.scheduler().runSync(() -> {
        // Estado do Minecraft
    });

## runAsync

Executa trabalho fora do thread principal:

    Gafi.scheduler().runAsync(() -> {
        String result = expensiveCalculation();
        Gafi.scheduler().runSync(() -> {
            Gafi.broadcast(result);
        });
    });

## supplyAsync

Permite devolver um valor:

    Gafi.scheduler()
        .supplyAsync(() -> calculate())
        .thenAccept(result ->
            Gafi.scheduler().runSync(() ->
                Gafi.broadcast(result)
            )
        );

## Threading

runAsync não deve ser usado para manipular diretamente o estado interno do mundo.

A abordagem recomendada é:

1. obter ou calcular dados fora do thread do servidor;
2. mudar o mundo através de runSync ou de uma API GafiScript que já faça esse agendamento.

## Cleanup

Quando o servidor termina, o scheduler cancela as tasks existentes.

Durante o lifecycle de um script, o próximo passo da arquitetura é associar tasks ao próprio script para que reload também faça cleanup automático.

## Erros

Uma exceção lançada numa task não deve derrubar o servidor. O scheduler regista a falha através do logger GafiScript e continua a processar as restantes tasks.

## Limites

O scheduler atual é baseado em ticks para tasks sincronas e num executor de virtual threads para trabalho assíncrono. Não deve ser utilizado para criar milhares de tarefas desnecessárias por tick.
