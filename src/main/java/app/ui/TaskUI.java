package app.ui;

import app.model.TaskBoard;
import app.model.TaskDto;
import app.model.TaskRepository;
import io.jooby.annotation.*;
import io.jooby.annotation.htmx.HxError;
import io.jooby.annotation.htmx.HxOob;
import io.jooby.annotation.htmx.HxTrigger;
import io.jooby.annotation.htmx.HxView;
import io.jooby.htmx.HtmxResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * A declarative HTMX controller for managing a Task Board.
 *
 * <p>This class demonstrates the power of the {@code jooby-htmx} module by utilizing
 * both the Declarative Annotation API and the Imperative Builder API to handle complex
 * UI state without heavy JavaScript.</p>
 *
 * <h3>Validation & Error Handling</h3>
 * <p>The class-level {@link HxError} annotation acts as a scoped "UI Janitor." If any endpoint
 * triggers a validation failure (such as the {@code @Valid} annotation in {@link #addTask}),
 * the framework catches the 422 exception and automatically renders the {@code task_error.hbs} template.
 * On a successful request, it appends an empty out-of-band swap to instantly clear any prior errors.</p>
 */
@HxError("task_error.hbs")
public class TaskUI {

    private final TaskRepository db;

    public TaskUI(TaskRepository db) {
        this.db = db;
    }

    /**
     * Serves the primary task board view, adapting its response based on the request origin
     * via the SPA Shell Layout Engine.
     *
     * <ul>
     * <li><b>Standard Browser Request:</b> Renders the outer SPA shell ({@code index.hbs})
     * and dynamically injects the {@code board.hbs} partial inside it.</li>
     * <li><b>HTMX AJAX Request:</b> Bypasses the layout and returns only the fast,
     * targeted {@code board.hbs} fragment.</li>
     * </ul>
     *
     * @return The current state of the task board.
     */
    @GET({"/", "/tasks"})
    @HxView(value = "board.hbs", layout = "index.hbs")
    public TaskBoard getBoard() {
        return db.getBoardState();
    }

    /**
     * Adds a new task and orchestrates multiple UI updates in a single response.
     *
     * <p>This method uses the Declarative API to achieve the following:</p>
     * <ul>
     * <li>Renders the newly created task via {@code task_row.hbs} as the primary response.</li>
     * <li>Updates the active task count via an Out-Of-Band (OOB) swap using {@code task_counter.hbs}.</li>
     * <li>Displays a success notification via an OOB swap using {@code toast.hbs}.</li>
     * <li>Triggers a client-side JavaScript event {@code taskAdded} via the {@code HX-Trigger} header.</li>
     * </ul>
     *
     * @param dto The validated task data from the submitted form.
     * @return A map containing the data model required for the primary and OOB templates.
     */
    @POST("/tasks")
    @HxView(value = "task_row.hbs")
    @HxOob("task_counter.hbs")
    @HxOob("toast.hbs")
    @HxTrigger("taskAdded")
    public Map<String, Object> addTask(@FormParam @Valid TaskDto dto) {
        var newTask = db.save(dto);
        return Map.of("id", newTask.id(),
                "title", newTask.title(),
                "completed", newTask.completed(),
                "activeCount", db.getActiveCount(),
                "message", "Task added successfully!");
    }

    /**
     * Deletes a task and updates the UI using the Imperative Builder API.
     *
     * <p>Because deleting a row typically removes an element from the DOM rather than replacing it
     * with a new primary view, this endpoint returns an empty primary response. However, it still leverages
     * OOB swaps to concurrently update the global task counter and show a toast notification.</p>
     *
     * @param id The unique identifier of the task to delete.
     * @return An imperative {@link HtmxResponse} containing the OOB rendering instructions.
     */
    @DELETE("/tasks/{id}")
    public HtmxResponse deleteTask(@PathParam String id) {
        db.delete(id);
        return HtmxResponse.empty()
                .addOob("task_counter.hbs", Map.of("activeCount", db.getActiveCount()))
                .addOob("toast.hbs", Map.of("message", "Task deleted!"));
    }

    /**
     * Persists the new sorting order after a drag-and-drop operation.
     *
     * <p>Similar to deletion, this endpoint does not require a primary view. It silently updates
     * the database and pushes an OOB toast notification to confirm the save operation to the user.</p>
     *
     * @param taskIds The ordered list of task IDs from the UI.
     * @return An imperative {@link HtmxResponse} triggering the toast notification.
     */
    @POST("/tasks/reorder")
    public HtmxResponse reorderTasks(@FormParam List<String> taskIds) {
        db.updateOrder(taskIds);
        return HtmxResponse.empty()
                .addOob("toast.hbs", Map.of("message", "Board saved."));
    }
}