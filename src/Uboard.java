// FILE: Uboard.java
import javax.swing.*;
import java.awt.*;

public class Uboard {

    static {
        // ---- Global Dark UI defaults ----
        UIManager.put("ToolTip.background", new Color(50, 50, 50));
        UIManager.put("ToolTip.foreground", Color.WHITE);

        UIManager.put("ComboBox.background", new Color(40, 40, 40));
        UIManager.put("ComboBox.foreground", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", new Color(70, 100, 180));
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);

        UIManager.put("PopupMenu.border",
                BorderFactory.createLineBorder(new Color(70, 70, 70)));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Uboard – Your Space");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);

            CanvasPanel canvas = new CanvasPanel();
            ToolbarPanel toolbar = new ToolbarPanel(canvas);

            frame.setLayout(new BorderLayout());
            frame.add(toolbar, BorderLayout.NORTH);
            frame.add(canvas, BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }
}
