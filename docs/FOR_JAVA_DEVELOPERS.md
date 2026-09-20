# GafiScript para programadores Java

GafiScript usa Java como linguagem dos scripts. As funcionalidades próprias do Minecraft são fornecidas pela API GafiScript.

## Primeiro script

    import static com.gafipro.gafiscript.api.Gafi.*;

    public class Main {
        public static void start() {
            broadcast("Olá, Minecraft!");
        }
    }

O método de entrada recomendado é um start() estático. Também é aceite main(String[]).

## Mundo

    GafiWorld world = Gafi.world();

    world.setBlock(
        GafiPosition.of(10, 64, 10),
        "minecraft:redstone_block"
    );

## Jogadores

    for (GafiPlayer player : Gafi.players()) {
        player.sendMessage("Olá!");
    }

Também podes obter um jogador diretamente:

    GafiPlayer player = Gafi.player("Gabriel");

## Eventos

    Gafi.events().onPlayerJoin(player -> {
        player.sendMessage("Bem-vindo!");
    });

A camada atual inclui entrada/saída de jogadores, morte observada através do ciclo de respawn, break de bloco, utilização de bloco e tick do servidor.

## Delay

Minecraft usa ticks como unidade natural de atualização.

    Gafi.scheduler().delayTicks(20, () -> {
        Gafi.broadcast("Passou aproximadamente 1 segundo.");
    });

Também existe a forma em segundos:

    Gafi.scheduler().delaySeconds(3, () -> {
        Gafi.broadcast("Passaram 3 segundos.");
    });

## Repetição

    GafiTask task =
        Gafi.scheduler().repeatSeconds(1, () -> {
            Gafi.broadcast("Tick temporal.");
        });

Cancelar:

    task.cancel();

## Sync e async

Trabalho pesado pode correr fora do thread do servidor.

    Gafi.scheduler().runAsync(() -> {
        String result = expensiveCalculation();

        Gafi.scheduler().runSync(() -> {
            Gafi.broadcast(result);
        });
    });

Operações que tocam diretamente no estado de Minecraft devem respeitar as restrições de thread documentadas pelo projeto.

## Blocks

    world.setBlock(position, "minecraft:stone");
    world.breakBlock(position, true);
    world.fill(
        GafiPosition.of(0, 64, 0),
        GafiPosition.of(10, 64, 10),
        "minecraft:air"
    );

## Itens

    player.giveItem("minecraft:diamond", 5);
    player.removeItem("minecraft:diamond", 1);

## Efeitos

    player.addEffect("minecraft:speed", 10, 1);

## Classes continuam a ser Java

    public class RaceManager {
        private boolean running;

        public void start() {
            running = true;
        }

        public boolean isRunning() {
            return running;
        }
    }

Não existe uma sintaxe especial para classes, métodos, loops, interfaces, records, enums, generics, exceptions ou lambdas.

## Java vs GafiScript

Java normal:
- classes;
- métodos;
- interfaces;
- records;
- enums;
- generics;
- loops;
- exceptions;
- lambdas;
- collections.

GafiScript:
- Gafi.world();
- Gafi.players();
- Gafi.scheduler();
- Gafi.events();
- GafiPosition;
- GafiPlayer;
- GafiWorld;
- Script Block;
- runtime;
- compilação em runtime;
- permissões;
- armazenamento de scripts.

A intenção é aprender a API do Minecraft sem ter de aprender outra linguagem.
