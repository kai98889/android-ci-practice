package com.kai98889.lighttasks;

import org.junit.Test;
import static org.junit.Assert.*;

public class TaskListTest {
    @Test public void addingTrimsAndRejectsBlank() {
        TaskList list = new TaskList();
        list.add("  阅读  ");
        assertEquals("阅读", list.all().get(0).text);
        assertThrows(IllegalArgumentException.class, () -> list.add("  "));
        assertThrows(IllegalArgumentException.class, () -> list.add(null));
        assertEquals(1, list.all().size());
    }
    @Test public void toggleAndDeleteKeepCountsCorrect() {
        TaskList list = new TaskList();
        list.add("A"); list.add("B");
        list.toggle(0); assertEquals(1, list.completed());
        list.toggle(0); assertEquals(0, list.completed());
        list.toggle(1); list.remove(0);
        assertEquals("B", list.all().get(0).text);
        assertEquals(1, list.completed());
    }
    @Test public void restorePreservesCompletedStateAndSnapshotsAreStable() {
        TaskList list = new TaskList();
        list.restore("Saved", true);
        java.util.List<TaskList.Task> snapshot = list.all();
        list.toggle(0);
        assertTrue(snapshot.get(0).done);
        assertEquals(0, list.completed());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
    }
}
