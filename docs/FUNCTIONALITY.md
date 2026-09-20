# GafiScript — functionality reference

Esta página descreve o estado real da plataforma. Uma funcionalidade só aparece como "implemented" quando existe código funcional no repositório.

## Language

### Implemented
- Java como linguagem de script.
- classes normais;
- métodos;
- fields;
- static methods;
- lambdas;
- collections;
- generics;
- loops;
- conditions;
- exceptions;
- imports;
- packages, quando aceites pelo compilador;
- records e enums suportados pelo Java compiler;
- Java 21.

O runtime não traduz Java para comandos Minecraft.

## Script lifecycle

### Implemented
- source Java;
- syntax parsing;
- source validation;
- Java compilation;
- restricted class loading;
- start();
- main(String[]);
- active script registry;
- stop;
- file-based run;
- cleanup de output temporário.

### Planned
- lifecycle events completos;
- script task ownership automático;
- full reload state restoration;
- dependencies entre projetos.

## Script Block

### Implemented
O GafiScript Block é um bloco com BlockEntity própria.

Guarda:
- nome;
- source Java.

A fonte é sincronizada para o cliente através de payloads.

O editor pode:
- pedir a fonte;
- guardar;
- executar.

O bloco e os comandos de edição exigem permission level 2 na implementação inicial.

## Editor

### Implemented
- janela de editor dentro de Minecraft;
- linha por linha;
- números das linhas;
- cursor;
- navegação por teclado;
- Enter;
- Tab;
- Backspace;
- Delete;
- copy/paste;
- Ctrl+S;
- F5;
- Ctrl+Space;
- highlighting inicial;
- completion básica;
- problems panel.

### Planned
- seleção de ranges;
- undo/redo completo;
- formatter Java;
- semantic completion completa;
- hover;
- signature help;
- go-to-definition;
- search/replace;
- multi-file tabs;
- project tree;
- inline diagnostics completos;
- debugger.

## Autocomplete

### Implemented
O completion atual conhece:
- palavras-chave Java;
- Gafi;
- Gafi.scheduler();
- principais métodos de Gafi;
- operações básicas de GafiWorld;
- operações básicas de GafiPlayer.

### Planned
- type inference completo;
- overload resolution;
- completions de collections;
- imports automáticos;
- símbolos de classes do utilizador;
- documentação contextual completa.

## Diagnostics

### Implemented
A camada local deteta alguns problemas óbvios antes da compilação.

O compilador Java continua a ser a autoridade para erros de sintaxe e tipo.

### Planned
- diagnostics semânticos em tempo real;
- quick fixes;
- underline por token;
- warnings de null;
- imports não usados;
- APIs deprecated;
- análise de fluxo.

## Scheduler

### Implemented

- nextTick;
- delayTicks;
- delaySeconds;
- repeatTicks;
- repeatSeconds;
- runSync;
- runAsync;
- supplyAsync;
- GafiTask.cancel();
- GafiTask.isCancelled().

### Semântica

Minecraft trabalha naturalmente com ticks.

Como regra de temporização:
20 ticks correspondem aproximadamente a um segundo.

### Limitações atuais

A associação automática de tasks a cada script ainda precisa de ser aprofundada para tornar reload completamente isolado.

## Events

### Implemented
- player join;
- player disconnect;
- morte observada no ciclo de respawn;
- block break;
- block use;
- server tick.

### Block use cancellation

GafiBlockUseEvent possui:
- player();
- position();
- isCancelled();
- cancel().

### Planned
- chat;
- command;
- item use;
- entity spawn;
- entity damage;
- attack;
- inventory;
- teleport;
- respawn dedicated event;
- world load/unload;
- event priority;
- listener removal.

## World API

### Implemented
- raw();
- dimension();
- time();
- setTime();
- isAir();
- block();
- setBlock();
- breakBlock();
- fill();
- findBlocks().

### Region limits

fill() possui limite de segurança de 250.000 blocos por operação na implementação inicial.

Este limite existe para reduzir o risco de scripts bloquearem o servidor com operações de região demasiado grandes.

### Planned
- replace;
- copy;
- paste;
- clone;
- chunk helpers;
- biome;
- weather;
- gamerules;
- world border;
- spawn point;
- structures;
- dimension selection;
- batch APIs mais eficientes.

## Player API

### Implemented
- name();
- uuid();
- position();
- world();
- health();
- setHealth();
- level();
- setLevel();
- isSneaking();
- isSprinting();
- isFlying();
- isOnGround();
- sendMessage();
- sendActionBar();
- teleport();
- giveItem();
- removeItem();
- addEffect();
- raw().

### Planned
- inventory wrapper;
- food;
- experience;
- game mode;
- sounds;
- particles;
- effects completos;
- attributes;
- equipment;
- raycast;
- permissions.

## Position API

### Implemented

GafiPosition é um Java record.

Operações:
- of();
- add();
- subtract();
- distanceTo().

## Items

### Implemented
A Player API suporta operações simples de give/remove por identifier.

### Planned
- ItemBuilder;
- nomes;
- lore;
- components;
- enchantments;
- attributes;
- custom model data;
- consumables;
- equipment;
- comparison helpers.

## Entities

### Implemented
A camada pública ainda é pequena.

### Planned
- query;
- spawn;
- remove;
- teleport;
- velocity;
- rotation;
- health;
- equipment;
- tags;
- target;
- passengers;
- vehicle;
- bounding boxes;
- raycast.

## Commands

### Implemented

O comando de administração:

    /gafiscript help
    /gafiscript list
    /gafiscript run <script>
    /gafiscript stop <script>
    /gafiscript info <script>

### Planned

API para scripts registarem comandos Minecraft próprios com Brigadier.

## Networking

### Implemented
O editor utiliza CustomPayload tipado para:
- request;
- save;
- run;
- script data;
- messages.

O servidor permanece autoritativo sobre o BlockEntity e a execução.

## Security

### Implemented
- validation de source;
- limite de tamanho;
- blocked APIs óbvias;
- restricted class loader;
- permission level 2 para edição/execução.

### Important

O sandbox atual é defensivo, não formalmente verificado.

Não deve ser considerado uma boundary de segurança perfeita.

## Persistence

### Implemented
A fonte do Script Block é guardada com BlockEntity data.

Scripts de ficheiro usam:

    <server-run-directory>/gafiscript/scripts/

## Storage

### Planned
A abstração de storage persistente descrita na especificação ainda não foi exposta na API pública.

## Config

### Planned
A API Config descrita na especificação ainda não foi exposta na API pública.

## GUI

### Planned
O editor é uma GUI do próprio mod, mas a API de GUIs para scripts ainda não está implementada.

## Scoreboard

### Planned
A API de scoreboard ainda não está implementada.

## BossBar

### Planned
A API de bossbars ainda não está implementada.

## Particles and sounds

### Planned
A API dedicada ainda não está exposta no runtime de scripts.

## Debugging

### Implemented
- logs;
- compile diagnostics;
- problems panel;
- runtime error logging.

### Planned
- breakpoints;
- pause/resume;
- step;
- stack viewer;
- variable inspection;
- event monitor;
- visual profiler.

## REPL

### Planned

Uma REPL Java/GafiScript completa ainda não está implementada.

## Multi-file projects

### Current state

O runtime consegue compilar source Java individual.

A arquitetura ainda precisa de suporte completo para:
- project manifest;
- source tree;
- multiple compilation units;
- project dependencies;
- editor tabs.

## Export / import

### Planned

O formato .gafiscript ainda não está implementado.

## Performance

### Implemented
- source size cap;
- region volume cap;
- async executor para trabalho fora do thread do servidor;
- profiling hooks iniciais no desenho da arquitetura.

### Planned
- visual profiler;
- tick budget;
- task statistics;
- cache diagnostics;
- batch world operations.

## Documentation

Documentação principal:
- FOR_JAVA_DEVELOPERS.md
- GAFISCRIPT_EXTENSIONS.md
- SCHEDULER.md
- THREADING.md
- SECURITY.md
- EDITOR.md
- FROM_COMMAND_BLOCKS.md
- ARCHITECTURE.md
- LIMITATIONS.md
- API_INDEX.md

## Examples

A pasta examples/ contém scripts para:
- Hello World;
- countdown;
- player join;
- block use;
- area cleaning;
- boat race.

Os exemplos devem ser atualizados sempre que a API mudar.

## Design rule

O GafiScript adiciona APIs, não uma nova sintaxe.

Quando uma funcionalidade pode ser expressa em Java normal, deve continuar a ser Java normal.

Quando uma funcionalidade for específica do GafiScript, deve ter:
- API;
- Javadoc;
- documentação;
- example;
- testes quando apropriado.

## Next expansion areas

A sequência de evolução recomendada é:
1. corrigir e estabilizar o runtime;
2. fortalecer o sandbox;
3. semantic editor;
4. multi-file projects;
5. richer event API;
6. command API;
7. inventory/item/GUI APIs;
8. storage/config;
9. debugger/profiler;
10. full documentation automation.
