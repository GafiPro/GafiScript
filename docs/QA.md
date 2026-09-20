# QA

Gate de build:

    ./gradlew clean build --no-daemon --max-workers=1

No Windows:

    gradlew.bat clean build --no-daemon --max-workers=1

A CI valida Java 21, Loom/remapJar, testes JUnit e o artifact final.

A segurança distingue defesa em profundidade de sandbox formal. Source validation, bytecode inspection e restricted classloader não devem ser tratados como uma sandbox JVM perfeita.

Antes de release devem ser verificados GafiScript Block, save/run, projetos multi-ficheiro, GUI, commands, tasks/events e cleanup, REPL, debugger probes e import/export.
