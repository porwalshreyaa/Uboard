// FILE: Uboard.java
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

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

            // Simple File menu
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");

            JMenuItem saveImageItem = new JMenuItem("Save…");

            saveImageItem.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Save image");
                chooser.setFileFilter(new FileNameExtensionFilter("PNG Images (*.png)", "png"));
                if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    if (!f.getName().toLowerCase().endsWith(".png")) {
                        f = new File(f.getParentFile(), f.getName() + ".png");
                    }
                    try {
                        canvas.exportToPng(f);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame,
                                "Failed to export PNG:\n" + ex.getMessage(),
                                "Export Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            fileMenu.add(saveImageItem);
            menuBar.add(fileMenu);
            frame.setJMenuBar(menuBar);

            // Auto-load last board from cache (if present)
            File cacheDir = new File(System.getProperty("user.home"), ".uboard");
            File cacheFile = new File(cacheDir, "last-board.uboard");
            if (cacheFile.isFile()) {
                try {
                    canvas.loadFromFile(cacheFile);
                } catch (Exception ignored) {
                    // ignore corrupted cache
                }
            }

            // Auto-save current board to cache on exit
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (!cacheDir.exists()) {
                        // noinspection ResultOfMethodCallIgnored
                        cacheDir.mkdirs();
                    }
                    try {
                        canvas.saveToFile(cacheFile);
                    } catch (Exception ignored) {
                        // best-effort cache only
                    }
                }
            });

            frame.setVisible(true);
        });
    }
}
