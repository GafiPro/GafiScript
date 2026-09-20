# Profiling

O GafiScript tem um profiler leve para medir operações próprias do script.

Exemplo:

    double? result = profiler().measure("pathfinding", () -> {
        return calculate();
    });

A forma genérica atual é:

    var result = Gafi.profiler().measure(
        "calculation",
        () -> expensiveCalculation()
    );

Ou:

    Gafi.profiler().measure(
        "build-region",
        () -> world().fill(...)
    );

Consultar:

    Gafi.profiler().snapshot("build-region");

Ou:

    Gafi.profiler().snapshotAll();

Cada métrica apresenta:
- número de chamadas;
- média em milissegundos;
- máximo observado em milissegundos.

O profiler não é ainda um profiler de JVM completo. Não substitui Java Flight Recorder ou ferramentas externas de profiling.
