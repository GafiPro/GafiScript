package com.gafipro.gafiscript.scheduler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class GafiTaskGroup {
    private final String name;
    private final Set<GafiTask> tasks = ConcurrentHashMap.newKeySet();

    GafiTaskGroup(String name) {
        this.name = name == null || name.isBlank() ? "unnamed" : name;
    }

    public String name() {
        return name;
    }

    public GafiTaskGroup add(GafiTask task) {
        if (task != null && !task.isCancelled()) {
            tasks.add(task);
        }
        return this;
    }

    public void cancelAll() {
        tasks.forEach(GafiTask::cancel);
        tasks.clear();
    }

    public int activeCount() {
        tasks.removeIf(GafiTask::isCancelled);
        return tasks.size();
    }
}
