# Architecture

GafiScript is divided into five major layers.

## 1. Minecraft integration

Registers the block, BlockEntity, networking, Fabric events and commands.

Packages:
- block
- registry
- net
- command

## 2. Public API

The user-facing Java layer.

Packages:
- api
- scheduler

## 3. Runtime

Pipeline:

    Java source
        |
        v
    validation
        |
        v
    JavaParser syntax check
        |
        v
    Java compiler
        |
        v
    restricted class loader
        |
        v
    script lifecycle

## 4. Security

Source validation and class loading restrictions are separate from the public API so the security model can become stronger without changing scripts.

## 5. Client IDE

The client editor communicates with the server using typed networking payloads. The server is authoritative for stored source and execution.

## Lifecycle

A server attaches to Gafi on startup and detaches on shutdown.

The scheduler receives server ticks.

A Script Block stores its source in a BlockEntity.

The client requests that source and displays it.

Save sends source back to the server.

Run saves, compiles and executes the source.

## Extensibility

Future modules can add richer completion, multi-file projects, stronger security, GUI APIs, command APIs, storage, profiling and debugging without changing Java syntax.
