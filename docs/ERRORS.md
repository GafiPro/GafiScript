# GafiScript errors

## Compile errors

Compilation errors are returned with the Java compiler diagnostic where available.

Typical structure:

    Compile error:
    ERROR line 12, column 18: ...

Check:
1. line and column;
2. imports;
3. Java types;
4. GafiScript API names;
5. the script entry point.

## Runtime errors

A script whose start() or main() throws an exception is stopped and the error is logged.

Common causes:
- null values;
- invalid Minecraft identifiers;
- incorrect assumptions about a player/world state;
- application exceptions in your own code.

## Security errors

A source validator can reject references to blocked APIs before compilation.

Example:

    Blocked API/reference: ProcessBuilder

Do not work around security validation. Use the documented GafiScript APIs.

## Scheduler errors

Exceptions thrown inside scheduled tasks are logged by the scheduler rather than propagated to the Minecraft process.

## Script block errors

The editor can report:
- oversized source;
- invalid block;
- permission failure;
- server-side save failure.

## Error reporting rule

Never swallow exceptions silently. Use logging or a deliberate recovery strategy.
