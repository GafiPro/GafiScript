# Mental model

A GafiScript program is ordinary Java.

The platform is:

    Java
      +
    GafiScript API
      +
    Fabric/Minecraft runtime
      +
    scheduler/events
      +
    integrated editor

Scripts are compiled into classes. The public API is responsible for Minecraft-facing operations.

A script should generally:
1. register listeners/tasks in start();
2. use Gafi.runSync or the scheduler for server-thread work;
3. keep its own state in fields/storage;
4. use lifecycle cleanup rather than static global listeners;
5. stop without leaving tasks, commands, GUIs or listeners behind.
