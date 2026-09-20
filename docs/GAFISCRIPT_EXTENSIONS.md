# GafiScript Extensions

Esta é a referência das funcionalidades que o GafiScript adiciona ao Java.

## Gafi

Gafi é a porta de entrada para serviços do runtime.

Atualmente implementado:

- Gafi.server()
- Gafi.scheduler()
- Gafi.world()
- Gafi.players()
- Gafi.player(String)
- Gafi.playerByUuid(String)
- Gafi.broadcast(String)
- Gafi.logInfo(String)
- Gafi.logWarn(String)
- Gafi.logError(String)
- Gafi.runSync(Runnable)

Exemplo:

    Gafi.broadcast("A corrida começou!");

## GafiPosition

Representa coordenadas do mundo.

Operações:

- of(x, y, z)
- add(dx, dy, dz)
- subtract(dx, dy, dz)
- distanceTo(other)

Exemplo:

    GafiPosition start = GafiPosition.of(0, 64, 0);

## GafiPlayer

Wrapper seguro de um jogador do servidor.

Inclui:

- name()
- uuid()
- position()
- world()
- health()
- setHealth()
- level()
- setLevel()
- isSneaking()
- isSprinting()
- isFlying()
- isOnGround()
- sendMessage()
- sendActionBar()
- teleport()
- giveItem()
- removeItem()
- addEffect()
- raw()

## GafiWorld

Wrapper para operações do mundo.

Inclui:

- raw()
- dimension()
- time()
- setTime()
- isAir()
- block()
- setBlock()
- breakBlock()
- fill()
- findBlocks()

As operações de alteração de mundo são agendadas para o servidor.

## GafiEvents

A camada de eventos permite reagir a acontecimentos reais do Minecraft.

Eventos atualmente ligados:

- onPlayerJoin
- onPlayerLeave
- onPlayerDeath
- onBlockBreak
- onBlockUse
- onTick

Eventos de bloco de uso podem ser cancelados através de cancel().

## GafiScheduler

O scheduler acrescenta operações temporais.

Atualmente:

- nextTick
- delayTicks
- delaySeconds
- repeatTicks
- repeatSeconds
- runSync
- runAsync
- supplyAsync

As tasks são canceláveis.

## GafiTask

Representa uma operação agendada.

Métodos:

- cancel()
- isCancelled()

O cancelamento é usado automaticamente durante o encerramento do servidor e pode ser usado pelos scripts.

## Runtime

O runtime trata:

- validação;
- parsing;
- compilação;
- class loading;
- lifecycle;
- execução assíncrona;
- cleanup.

O ponto de entrada de um script é start() estático ou main(String[]).

## Script Block

O GafiScript Block guarda:

- nome do script;
- fonte Java;
- estado no mundo através de BlockEntity.

A edição é feita através da interface cliente e o conteúdo é enviado ao servidor por CustomPayload.

## Comandos

O comando principal é /gafiscript.

Subcomandos implementados:

- help
- list
- run
- stop
- info

## Editor

A IDE integrada inclui:

- múltiplas linhas;
- números de linha;
- indentation;
- undo/redo base do modelo do texto;
- copy/paste;
- atalhos;
- highlighting inicial;
- autocomplete;
- painel de problemas;
- Run;
- Save;
- Reload.

## Funcionalidades ainda não implementadas

As seguintes ideias da especificação original continuam separadas da implementação atual:

- debugger com breakpoints;
- REPL completa;
- GUI builder;
- scoreboard API;
- bossbar API;
- item builder avançado;
- command builder completo com todos os tipos de argumentos;
- sistema de dependências entre projetos;
- export/import .gafiscript;
- database abstraction;
- profiler visual completo;
- bytecode verification avançada;
- suporte multi-ficheiro diretamente no editor.

Estas funcionalidades devem ser adicionadas sem fingir que já existem.
