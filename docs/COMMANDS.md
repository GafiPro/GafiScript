# GafiScript command API

Scripts podem registar comandos Minecraft usando Brigadier através de Java normal.

Exemplo:

    import static com.gafipro.gafiscript.api.Gafi.*;

    public class Commands {
        public static void start() {
            commands().register("race", command ->
                command.then(
                    net.minecraft.server.command.CommandManager.literal("start")
                        .executes(context -> {
                            broadcast("§aRace started!");
                            return 1;
                        })
                )
            );
        }
    }

O método register devolve um GafiCommandHandle.

    GafiCommandHandle handle = commands().register(...);

Depois:

    handle.unregister();

Comandos criados durante a execução de um script ficam associados ao script atual e são removidos durante stop/reload.

O API expõe o próprio builder do Brigadier. Isto é intencional: a sintaxe do comando continua a ser Java + Brigadier, em vez de uma linguagem nova.

A árvore de comandos é atualizada para os jogadores ligados depois do registo ou remoção.
