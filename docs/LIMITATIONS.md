# Current limitations

GafiScript is under active development.

## Runtime

The current runtime compiles and loads Java source but does not provide a formally isolated JVM sandbox.

## Security

Source filtering and class loading restrictions are defensive heuristics, not a verified security boundary.

## Editor

The editor has basic completion and diagnostics. It is not yet equivalent to a full desktop IDE.

## Multi-file projects

The Script Block stores one Java source file. File-based scripts can be loaded from the server scripts directory.

## Events

The current event set is intentionally small.

## API breadth

World, player and scheduler APIs exist. Advanced item components, scoreboards, boss bars, complete GUI systems and many entity APIs remain future modules.

## Debugger

A complete debugger is not implemented yet.

## REPL

A full Java REPL is not implemented yet.

## Documentation

Implemented public features must be documented. Planned features must remain clearly marked as planned.
