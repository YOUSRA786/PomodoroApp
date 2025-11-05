import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import utils.SoundPlayer;

public class PomodoroTimer {
    private static final int WORK_TIME = 25 * 60;
    private static final int SHORT_BREAK = 5 * 60;
    private static final int LONG_BREAK = 15 * 60;

    private int timeLeft = WORK_TIME;
    private int sessionCount = 0;
    private boolean isRunning = false;
    private boolean isWorkSession = true;

    private Timer timer;
    private JButton startButton, pauseButton, resetButton, addTaskButton, deleteTaskButton, viewStatsButton;
    private JTextField taskInput, descriptionInput;
    private JComboBox<String> priorityComboBox;
    private JPanel taskListPanel;
    private JLabel quoteLabel;

    private TaskManager taskManager = new TaskManager();
    private CircularTimer circularTimer;
    private List<String> quotes;

    public PomodoroTimer() {
        loadQuotes();
        JFrame frame = new JFrame("\uD83C\uDF45 FOCUSLY");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        AnimatedGradientPanel animatedPanel = new AnimatedGradientPanel();
        frame.setContentPane(animatedPanel);
        frame.setLayout(new BorderLayout(20, 20));

        setupUI(frame);
        frame.setVisible(true);
    }

    private void loadQuotes() {
        try {
            quotes = Files.readAllLines(Paths.get("assets/quotes.txt"));
        } catch (IOException e) {
            quotes = List.of("Stay focused and never give up!");
        }
    }

    private String getRandomQuote() {
        if (quotes == null || quotes.isEmpty()) return "Stay focused and never give up!";
        Random rand = new Random();
        return quotes.get(rand.nextInt(quotes.size()));
    }
    private JLabel createLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("Poppins", Font.BOLD, 16));
    label.setForeground(Color.BLACK);
    return label;
}

    private void setupUI(JFrame frame) {
        Color background = new Color(20, 20, 30);

        frame.getContentPane().setBackground(background);
        frame.setLayout(new BorderLayout(20, 20));

        circularTimer = new CircularTimer(WORK_TIME, 1.3);

        startButton = createGradientButton("▶ Start");
        pauseButton = createGradientButton("⏸ Pause");
        resetButton = createGradientButton("🔁 Reset");
        viewStatsButton = createGradientButton("📊 View Statistics");

        startButton.addActionListener(e -> startTimer());
        pauseButton.addActionListener(e -> pauseTimer());
        resetButton.addActionListener(e -> resetTimer());
        viewStatsButton.addActionListener(e -> showStatisticsWindow());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(viewStatsButton);

        taskInput = new JTextField(10);
        taskInput.setFont(new Font("Poppins", Font.BOLD, 16));
        taskInput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        descriptionInput = new JTextField(10);
        descriptionInput.setFont(new Font("Poppins", Font.BOLD, 16));
        descriptionInput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] priorities = {"High", "Medium", "Low"};
        priorityComboBox = new JComboBox<>(priorities);
        priorityComboBox.setFont(new Font("Poppins", Font.BOLD, 16));

        addTaskButton = createGradientButton("📝 Add Task");
        addTaskButton.addActionListener(e -> addTask());

        deleteTaskButton = createGradientButton("🗑️ Delete Task");
        deleteTaskButton.addActionListener(e -> deleteTask());

        JButton editTaskButton = createGradientButton("✏️ Edit Task");
        editTaskButton.addActionListener(e -> editTask());

        JLabel taskLabel = createLabel("Task:");
        JLabel descLabel = createLabel("Description:");
        JLabel priorityLabel = createLabel("Priority:");

        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        taskPanel.setOpaque(false);
        taskPanel.add(taskLabel);
        taskPanel.add(taskInput);
        taskPanel.add(descLabel);
        taskPanel.add(descriptionInput);
        taskPanel.add(priorityLabel);
        taskPanel.add(priorityComboBox);
        taskPanel.add(addTaskButton);
        taskPanel.add(deleteTaskButton);
        taskPanel.add(editTaskButton);

        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setPreferredSize(new Dimension(300, 0));

        quoteLabel = new JLabel(" ");
        quoteLabel.setFont(new Font("Georgia", Font.ITALIC, 18));
        quoteLabel.setForeground(Color.BLACK);
        quoteLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel quotePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        quotePanel.setOpaque(false);
        quotePanel.add(quoteLabel);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(taskPanel, BorderLayout.NORTH);
        bottomPanel.add(quotePanel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(circularTimer, BorderLayout.NORTH);
        centerPanel.add(buttonPanel, BorderLayout.CENTER);
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.EAST);

        refreshTaskList();
    }

    private void refreshTaskList() {
        taskListPanel.removeAll();

        for (int i = 0; i < taskManager.getTasks().size(); i++) {
            Task task = taskManager.getTasks().get(i);

            JCheckBox checkBox = new JCheckBox(task.toString(), task.isCompleted());
            checkBox.setFont(new Font("Poppins", Font.BOLD, 14));
            checkBox.setForeground(Color.WHITE);
            checkBox.setBackground(Color.BLACK);
            final int index = i;

            checkBox.addActionListener(e -> {
                taskManager.toggleTaskCompletion(index);
                taskManager.saveTasks();
                refreshTaskList();
            });

            taskListPanel.add(checkBox);
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private void showStatisticsWindow() {
        JFrame statsFrame = new JFrame("📊 Pomodoro Statistics");
        statsFrame.setSize(400, 300);
        statsFrame.setLocationRelativeTo(null);

        AnimatedGradientPanel statsPanel = new AnimatedGradientPanel();
        statsPanel.setLayout(new BorderLayout(10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Poppins", Font.BOLD, 18));
        statsArea.setForeground(Color.BLACK);
        statsArea.setOpaque(false);
        statsArea.setLineWrap(true);
        statsArea.setWrapStyleWord(true);

        int totalPomodoros = taskManager.getTasks().stream().mapToInt(Task::getPomodoroCount).sum();
        int completedTasks = (int) taskManager.getTasks().stream().filter(Task::isCompleted).count();
        int totalTasks = taskManager.getTasks().size();

        statsArea.setText("🍅 Total Pomodoros Completed: " + totalPomodoros +
                "\n✅ Completed Tasks: " + completedTasks +
                "\n📋 Total Tasks: " + totalTasks);

        statsArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton closeButton = createGradientButton("❌ Close");
        closeButton.addActionListener(e -> statsFrame.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);

        statsPanel.add(new JScrollPane(statsArea), BorderLayout.CENTER);
        statsPanel.add(buttonPanel, BorderLayout.SOUTH);

        statsFrame.setContentPane(statsPanel);
        statsFrame.setVisible(true);
    }

    private JButton createGradientButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Poppins", Font.BOLD, 16));
        button.setBackground(new Color(58, 123, 213));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(38, 103, 193));
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(58, 123, 213));
            }
        });

        return button;
    }

    private void startTimer() {
        if (!isRunning) {
            isRunning = true;
            circularTimer.startGlow();
            quoteLabel.setText("<html><center>" + getRandomQuote() + "</center></html>");
            SoundPlayer.playSound("assets/sounds/work_start.wav");
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> updateTimer());
                }
            }, 0, 1000);
        }
    }

    private void pauseTimer() {
        if (isRunning) {
            timer.cancel();
            isRunning = false;
            circularTimer.stopGlow();
        }
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();
        isRunning = false;
        timeLeft = isWorkSession ? WORK_TIME : (sessionCount % 4 == 0 ? LONG_BREAK : SHORT_BREAK);
        circularTimer.updateTime(timeLeft);
        circularTimer.stopGlow();
    }

    private void updateTimer() {
        if (timeLeft > 0) {
            timeLeft--;
            circularTimer.updateTime(timeLeft);
        } else {
            timer.cancel();
            isRunning = false;
            circularTimer.stopGlow();

            if (isWorkSession) {
                SoundPlayer.playSound("assets/sounds/alarm.wav");
                sessionCount++;
                incrementTaskSession();
            } else {
                SoundPlayer.playSound("assets/sounds/break_start.wav");
            }

            isWorkSession = !isWorkSession;
            timeLeft = isWorkSession ? WORK_TIME : (sessionCount % 4 == 0 ? LONG_BREAK : SHORT_BREAK);
            circularTimer.updateTime(timeLeft);
        }
    }

    private void addTask() {
        String taskName = taskInput.getText().trim();
        String description = descriptionInput.getText().trim();
        String priority = (String) priorityComboBox.getSelectedItem();

        if (!taskName.isEmpty()) {
            taskManager.addTask(taskName, description, priority);
            taskManager.saveTasks();
            refreshTaskList();
            taskInput.setText("");
            descriptionInput.setText("");
            priorityComboBox.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(null, "Task name cannot be empty.");
        }
    }

    private void incrementTaskSession() {
        if (!taskManager.getTasks().isEmpty()) {
            taskManager.incrementPomodoroForTask(taskManager.getTasks().size() - 1);
            taskManager.saveTasks();
            refreshTaskList();
        }
    }

    private void editTask() {
        String taskName = JOptionPane.showInputDialog(null, "Enter the task name to edit:");
        if (taskName == null || taskName.trim().isEmpty()) return;

        for (int i = 0; i < taskManager.getTasks().size(); i++) {
            Task task = taskManager.getTasks().get(i);
            if (task.getName().equalsIgnoreCase(taskName.trim())) {
                String newName = JOptionPane.showInputDialog(null, "Edit Task Name:", task.getName());
                if (newName != null && !newName.trim().isEmpty()) task.setName(newName.trim());

                String newDescription = JOptionPane.showInputDialog(null, "Edit Description:", task.getDescription());
                if (newDescription != null && !newDescription.trim().isEmpty())
                    task.setDescription(newDescription.trim());

                String[] priorities = {"High", "Medium", "Low"};
                String newPriority = (String) JOptionPane.showInputDialog(null, "Select Priority:", "Priority",
                        JOptionPane.QUESTION_MESSAGE, null, priorities, task.getPriority());
                if (newPriority != null) task.setPriority(newPriority);

                taskManager.saveTasks();
                refreshTaskList();
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "No matching task found.");
    }

    private void deleteTask() {
        String taskName = JOptionPane.showInputDialog(null, "Enter the task name to delete:");
        if (taskName == null || taskName.trim().isEmpty()) return;

        for (int i = 0; i < taskManager.getTasks().size(); i++) {
            Task task = taskManager.getTasks().get(i);
            if (task.getName().equalsIgnoreCase(taskName.trim())) {
                taskManager.deleteTask(i);
                taskManager.saveTasks();
                refreshTaskList();
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "No matching task found.");
    }

    class AnimatedGradientPanel extends JPanel {
        private float hue = 0f;

        public AnimatedGradientPanel() {
            javax.swing.Timer timer = new javax.swing.Timer(50, e -> {
                hue += 0.001f;
                if (hue > 1f) hue = 0f;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();

            Color color1 = Color.getHSBColor(hue, 0.5f, 0.8f);
            Color color2 = Color.getHSBColor((hue + 0.2f) % 1f, 0.5f, 0.8f);

            GradientPaint gp = new GradientPaint(0, 0, color1, width, height, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
        }
    }

}
