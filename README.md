# HTMX Task Board

A blazing-fast, reactive Single Page Application (SPA) built with **Jooby**, **HTMX**, and **Handlebars**.

This application demonstrates the power of the (jooby-htmx)[https://jooby.io/modules/htmx/] module, delivering a complex, interactive user interface—including drag-and-drop sorting, dynamic counters, and toast notifications—without writing complex client-side JavaScript.

---

## 🚀 Getting Started

### Prerequisites

- Java 21 or newer
- Maven

### Running the App

- Clone the repository.
- Compile and run the application:

```bash
mvn jooby:run
```

<img width="739" height="390" alt="Screenshot 2026-05-07 at 18 09 11" src="https://github.com/user-attachments/assets/3c97f1cf-f3d4-484d-857e-1dc8cbfdf887" />


## ✨ Features

* **SPA Shell Navigation:** Full-page reloads on direct browser access, but lightning-fast partial fragment swaps during HTMX navigation.
* **Declarative UI Updates:** Adding a task updates the list, increments the global task counter, fires a JS event, and displays a success toast simultaneously.
* **Scoped Error Handling:** Inline form validation (HTTP 422) that catches errors, displays them next to the form, and automatically clears them upon a successful submission.
* **Drag-and-Drop Reordering:** Persists list order to the backend and triggers OOB (Out-Of-Band) toast notifications upon success.

---

## 🧠 How It Works (The HTMX Magic)

This project leverages the first-class HTMX annotations provided by Jooby. Here is a look at the core mechanics powering the app:

### 1. The SPA Shell Layout Engine
The application uses the `@HxView` annotation to automatically determine if a user is doing a full page load (F5/Bookmark) or an AJAX request.

```java
@GET("/tasks")
@HxView(value = "board.hbs", layout = "index.hbs")
public TaskBoard getBoard() {
    return db.getBoardState();
}
```

- Browser Request: Injects board.hbs into the `index.hbs` layout shell.

- HTMX Request: Returns only the `board.hbs` fragment.

### 2. Declarative Out-Of-Band (OOB) Updates

When a user adds a task, we need to update multiple parts of the screen at once. Instead of writing custom JSON endpoints and client-side state management, we just declare our UI targets:

```java
@POST("/tasks")
@HxView("task_row.hbs")             // 1. The main response (the new row)
@HxOob("task_counter.hbs")          // 2. Update the total count badge
@HxOob("toast.hbs")                 // 3. Show a success popup
@HxTrigger("taskAdded")             // 4. Fire a JS event for animations
public Task addTask(@Valid TaskDto dto) {
    var newTask = db.save(dto);
    return newTask;
}
```

### 3. The "UI Janitor" (Scoped Error Handling)

By annotating the controller with `@HxError("task_error.hbs")`, any Jakarta `@Valid` failures automatically render an inline error message next to the form.

When the user fixes the error and submits successfully, the framework automatically appends an empty `task_error.hbs` OOB swap to seamlessly wipe the error from the screen.

### 4. Imperative API for Action Endpoints
   
For endpoints that don't need to return a main view (like deleting or reordering tasks), the app uses the HtmxResponse builder to explicitly trigger updates:

```java
@DELETE("/tasks/{id}")
public HtmxResponse deleteTask(@PathParam String id) {
    db.delete(id);
    return HtmxResponse.empty()
            .addOob("task_counter.hbs", Map.of("activeCount", db.getActiveCount()))
            .addOob("toast.hbs", Map.of("message", "Task deleted!"));
}
```
