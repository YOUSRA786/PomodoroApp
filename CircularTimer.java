import javax.swing.*;
import java.awt.*;

public class CircularTimer extends JPanel {
    private int totalTime;
    private int remainingTime;
    private boolean glowing = false;
    private float glowSize = 10f;
    private javax.swing.Timer glowTimer;
    private boolean glowIncreasing = true;

    private double scaleFactor = 1.3; 

    public CircularTimer(int totalTime, double scaleFactor) {
        this.totalTime = totalTime;
        this.remainingTime = totalTime;
        this.scaleFactor = scaleFactor;
        setOpaque(false);
        setPreferredSize(new Dimension((int) (200 * scaleFactor), (int) (200 * scaleFactor)));
    }

    public void updateTime(int timeLeft) {
        this.remainingTime = timeLeft;
        repaint();
    }

    public void startGlow() {
        glowing = true;
        glowTimer = new javax.swing.Timer(50, e -> {
            if (glowIncreasing) {
                glowSize += 0.5f;
                if (glowSize >= 20f) glowIncreasing = false;
            } else {
                glowSize -= 0.5f;
                if (glowSize <= 10f) glowIncreasing = true;
            }
            repaint();
        });
        glowTimer.start();
    }

    public void stopGlow() {
        glowing = false;
        if (glowTimer != null) glowTimer.stop();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();
        int size = (int) ((Math.min(width, height) - 40));

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = (width - size) / 2;
        int y = (height - size) / 2;

        if (glowing) {
            g2d.setColor(new Color(0, 0, 0, 120));
            g2d.setStroke(new BasicStroke(glowSize));
            g2d.drawOval(x - 10, y - 10, size + 20, size + 20);
        }

        g2d.setColor(Color.RED);
        g2d.fillOval(x, y, size, size);

        g2d.setColor(new Color(0, 0, 0));
        int angle = (int) (360.0 * remainingTime / totalTime);
        g2d.fillArc(x, y, size, size, 90, -angle);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Poppins", Font.BOLD, (int) (40 * scaleFactor)));
        String timeString = String.format("%02d:%02d", remainingTime / 60, remainingTime % 60);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(timeString);
        g2d.drawString(timeString, (width - textWidth) / 2, height / 2 + 10);
    }
}
