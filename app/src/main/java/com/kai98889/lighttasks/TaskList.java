package com.kai98889.lighttasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Small Android-independent model, also exercised by JVM tests. */
public final class TaskList {
    public static final class Task {
        public final String text;
        public final boolean done;
        public Task(String text, boolean done) { this.text = text; this.done = done; }
    }
    private final ArrayList<Task> tasks = new ArrayList<>();
    public void add(String text) { restore(text, false); }
    public void restore(String text, boolean done) {
        if (text == null || text.trim().isEmpty()) throw new IllegalArgumentException("Empty task");
        tasks.add(new Task(text.trim(), done));
    }
    public void toggle(int index) {
        Task old = tasks.get(index);
        tasks.set(index, new Task(old.text, !old.done));
    }
    public void remove(int index) { tasks.remove(index); }
    public List<Task> all() { return Collections.unmodifiableList(new ArrayList<>(tasks)); }
    public int completed() {
        int count = 0;
        for (Task task : tasks) if (task.done) count++;
        return count;
    }
}
