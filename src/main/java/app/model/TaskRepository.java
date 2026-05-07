package app.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskRepository {
    private final AtomicInteger errors = new AtomicInteger(0);
    private final Map<String, Task> db = new ConcurrentHashMap<>();
    private final AtomicInteger idGen = new AtomicInteger(1);
    // Stores the physical order of the board!
    private final List<String> taskOrder = new CopyOnWriteArrayList<>();

    public TaskBoard getBoardState() {
        var orderedTasks = taskOrder.stream()
                .map(db::get)
                .filter(Objects::nonNull)
                .toList();
        return new TaskBoard(getActiveCount(), orderedTasks);
    }

    public Task save(TaskDto dto) {
        if (errors.incrementAndGet() % 3 == 0) {
            // 500 error are targeted to global error handler
            errors.set(0);
            // fake unexpected error
            throw new IllegalStateException("Connection error! Please try again.");
        }
        if (db.values().stream().anyMatch(it -> it.title().equalsIgnoreCase(dto.title()))) {
            // 400 error are scoped to local error handler (if any) or to global error handler
            throw new IllegalArgumentException("Duplicated Task");
        }
        String id = String.valueOf(idGen.getAndIncrement());
        Task task = new Task(id, dto.title(), false);
        db.put(id, task);
        taskOrder.add(id);
        return task;
    }

    public void delete(String id) {
        db.remove(id);
        taskOrder.remove(id);
    }

    public int getActiveCount() {
        return db.size(); // Simplified for the demo
    }

    public void updateOrder(List<String> newOrder) {
        taskOrder.clear();
        taskOrder.addAll(newOrder);
    }
}