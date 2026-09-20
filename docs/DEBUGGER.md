# Debugger

O debugger atual é cooperativo e baseado em probes injetados no Java compilado.

    /gafiscript debug enable
    /gafiscript debug break Main 42
    /gafiscript debug hits Main
    /gafiscript debug clear Main 42
    /gafiscript debug disable

O compilador injeta chamadas a GafiDebugger.check(script, line) antes de statements Java.

Isto fornece breakpoints e trace de execução sem exigir uma porta JDI.

O modo atual não suspende a JVM nem oferece inspeção de variáveis locais. É uma implementação cooperativa segura para servidores.
