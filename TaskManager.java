import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TaskManager {
    private ArrayList<Task> tasks = new ArrayList<>();
    private static final String FILE_PATH = System.getProperty("user.home") + File.separator + "tasks.dat";

    public void addTask(String name, String description, String priority) {
        tasks.add(new Task(name, description, priority));
        saveTasks();
    }

    public void deleteTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.remove(index);
            saveTasks();
        }
    }

    public void clearAllTasks() {
        tasks.clear();
        saveTasks();
    }

    public void editTask(int index, String name, String description, String priority) {
        if (index >= 0 && index < tasks.size()) {
            Task task = tasks.get(index);
            task.setName(name);
            task.setDescription(description);
            task.setPriority(priority);
            saveTasks();
        }
    }
    public void saveTasks() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
        oos.writeObject(tasks);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    public void toggleTaskCompletion(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.get(index).toggleCompleted();
            saveTasks();
        }
    }

    public void incrementPomodoroForTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.get(index).incrementPomodoro();
            saveTasks();
        }
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

   
    @SuppressWarnings("unchecked")
    public void loadTasks() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
                tasks = (ArrayList<Task>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void sortTasksByPriority() {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority());
            }

            private int getPriorityValue(String priority) {
                switch (priority) {
                    case "High": return 1;
                    case "Medium": return 2;
                    case "Low": return 3;
                    default: return 4;
                }
            }
        });
        saveTasks();
    }

    public int getTotalTasks() {
        return tasks.size();
    }

    public int getCompletedTasks() {
        int count = 0;
        for (Task task : tasks) {
            if (task.isCompleted()) count++;
        }
        return count;
    }

    public int getRemainingTasks() {
        return getTotalTasks() - getCompletedTasks();
    }
}
