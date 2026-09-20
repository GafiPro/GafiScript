# Debugging

## Compile diagnostics

Use the editor Problems area for local diagnostics. The Java compiler remains authoritative.

## Logs

Scripts can write to the GafiScript logger:

    Gafi.logInfo("Race started");
    Gafi.logWarn("Unexpected state");
    Gafi.logError("Something failed");

## Script state

Use:

    /gafiscript list

to see active scripts.

And:

    /gafiscript info <script>

to inspect whether a script is running.

## Stop a failing script

    /gafiscript stop <script>

## Run a file script

Place:

    <server-run-directory>/gafiscript/scripts/Example.java

then:

    /gafiscript run Example

## Runtime stack traces

When script execution throws an exception, the server log contains the underlying exception and stack trace.

## Current debugger limitation

A full breakpoint debugger, step execution and variable inspector are not implemented yet. This is intentionally documented instead of presented as a fake feature.
