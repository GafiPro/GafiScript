# REPL

O GafiScript inclui um REPL Java one-shot.

Exemplos:

    /gafiscript repl 1 + 2
    /gafiscript repl Gafi.players().size()
    /gafiscript repl Gafi.broadcast("hello")

Expressões devolvem o resultado. Statements executam e devolvem ok.

O REPL compila Java real em cada avaliação e usa as mesmas validações do compilador.
