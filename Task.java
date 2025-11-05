import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;

public class Task implements Serializable {
    private String name;
    private String description;
    private int pomodoroCount;
    private boolean isCompleted;
    private String priority; // "High", "Medium", "Low"
    private LocalDateTime createdTime;
    private LocalDate deadline;

    public Task(String name, String description, String priority) {
    this.name = name;
    this.description = description;
    this.priority = priority;
    this.pomodoroCount = 0;
    this.isCompleted = false;
    this.createdTime = LocalDateTime.now();
}

@Override
public String toString() {
    return name + " (" + priority + ") - "   + description;
}

public void setCompleted(boolean completed) {
    this.isCompleted = completed;
}
public void setName(String name) {
    this.name = name;
}

public void setDescription(String description) {
    this.description = description;
}

public void setPriority(String priority) {
    this.priority = priority;
}
public int getPriorityValue() {
    switch (priority.toLowerCase()) {
        case "high": return 1;
        case "medium": return 2;
        case "low": return 3;
        default: return 4;
    }
}
public void toggleCompleted() {
    this.isCompleted = !this.isCompleted;
}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPomodoroCount() {
        return pomodoroCount;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public String getPriority() {
        return priority;
    }

    public void incrementPomodoro() {
        pomodoroCount++;
    }

    public void markCompleted() {
        isCompleted = true;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

   
}
