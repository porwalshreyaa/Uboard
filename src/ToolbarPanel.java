
// FILE: ToolbarPanel.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ToolbarPanel extends JPanel {

    class ToolButton extends JToggleButton {

        private static final Color BG = new Color(45, 45, 45);
        private static final Color BG_HOVER = new Color(60, 60, 60);
        private static final Color BG_ACTIVE = new Color(70, 100, 180);

        public ToolButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("Inter", Font.PLAIN, 13));
            setPreferredSize(new Dimension(40, 40));
            setRolloverEnabled(true);
            setAlignmentY(Component.CENTER_ALIGNMENT);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 35, 35)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = BG;
            if (getModel().isSelected())
                bg = BG_ACTIVE;
            else if (getModel().isRollover())
                bg = BG_HOVER;

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 2;

            g2.setColor(getForeground());
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    class ColorSwatch extends JToggleButton {

        private final Color color;

        public ColorSwatch(Color c) {
            this.color = c;
            setPreferredSize(new Dimension(22, 22));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setToolTipText(
                    String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setColor(color);
            g2.fillOval(x, y, size, size);

            if (getModel().isRollover()) {
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(255, 255, 255, 120));
                g2.drawOval(x, y, size, size);
            }

            if (getModel().isSelected()) {
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(Color.WHITE);
                g2.drawOval(x - 1, y - 1, size + 2, size + 2);
            }

            g2.dispose();
        }

        public Color getColor() {
            return color;
        }
    }

    class ColorChooserButton extends JButton {

        private Color color = Color.WHITE;

        public ColorChooserButton() {
            setPreferredSize(new Dimension(26, 26));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setToolTipText("More colors…");
        }

        public void setColor(Color c) {
            this.color = c;
            repaint();
        }

        public Color getColor() {
            return color;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int r = 6;
            int pad = 2;
            int w = getWidth() - pad * 2;
            int h = getHeight() - pad * 2;

            // background
            g2.setColor(color);
            g2.fillRoundRect(pad, pad, w, h, r, r);

            // border
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawRoundRect(pad, pad, w, h, r, r);

            // hover glow
            if (getModel().isRollover()) {
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(pad - 1, pad - 1, w + 2, h + 2, r + 2, r + 2);
            }

            // plus icon
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(Color.WHITE);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g2.drawLine(cx - 4, cy, cx + 4, cy);
            g2.drawLine(cx, cy - 4, cx, cy + 4);

            g2.dispose();
        }
    }

    class StrokePreview extends JComponent {

        private float stroke = 4f;

        public StrokePreview() {
            setPreferredSize(new Dimension(26, 26));
        }

        public void setStroke(float s) {
            stroke = s;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g2.setColor(Color.WHITE);
            g2.fillOval(
                    (int) (cx - stroke / 2),
                    (int) (cy - stroke / 2),
                    (int) stroke,
                    (int) stroke);

            g2.dispose();
        }
    }

    class ActionButton extends JButton {

        private final Color bg;
        private final Color bgHover;
        private final Color fg;
    
        public ActionButton(String text, Color bg, Color bgHover, Color fg) {
            super(text);
            this.bg = bg;
            this.bgHover = bgHover;
            this.fg = fg;
    
            setPreferredSize(new Dimension(56, 28));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setFont(new Font("Inter", Font.PLAIN, 12));
            setForeground(fg);
            setRolloverEnabled(true);
        }
    
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
    
            Color fill = getModel().isRollover() ? bgHover : bg;
    
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
    
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 2;
    
            g2.setColor(fg);
            g2.drawString(getText(), x, y);
    
            g2.dispose();
        }
    }
    

    private JSlider sizeSlider;

    private Component divider() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setMaximumSize(new Dimension(1, 32));
        sep.setForeground(new Color(60, 60, 60));
        return sep;
    }

    public ToolbarPanel(CanvasPanel canvas) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        setBackground(new Color(22, 22, 22));
        sizeSlider = new JSlider(1, 20, 4);
        sizeSlider.setPreferredSize(new Dimension(90, 26));
        sizeSlider.setOpaque(false);
        sizeSlider.setFocusable(false);
        sizeSlider.setToolTipText("Stroke size");

        sizeSlider.setUI(new javax.swing.plaf.basic.BasicSliderUI(sizeSlider) {

            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(80, 80, 80));
                g2.fillRoundRect(
                        trackRect.x,
                        trackRect.y + trackRect.height / 2 - 2,
                        trackRect.width,
                        4,
                        4,
                        4);
            }

            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
            
                int cx = thumbRect.x + thumbRect.width / 2;
                int cy = trackRect.y + trackRect.height / 2;
            
                int r = thumbRect.width / 2;
            
                g2.setColor(new Color(220, 220, 220));
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            }
            
        });

        JToggleButton penBtn = new ToolButton("✏");
        JToggleButton lineBtn = new ToolButton("／");
        JToggleButton rectBtn = new ToolButton("▭");
        JToggleButton circleBtn = new ToolButton("◯");
        JToggleButton textBtn = new ToolButton("T");
        JToggleButton eraserBtn = new ToolButton("⌫");
        JToggleButton selectBtn = new ToolButton("⤢");

        ButtonGroup g = new ButtonGroup();
        g.add(penBtn);
        g.add(lineBtn);
        g.add(rectBtn);
        g.add(circleBtn);
        g.add(textBtn);
        g.add(eraserBtn);
        g.add(selectBtn);
        penBtn.setSelected(true);

        lineBtn.addActionListener(e -> canvas.setTool(Tool.LINE));
        textBtn.addActionListener(e -> canvas.setTool(Tool.TEXT));
        eraserBtn.addActionListener(e -> canvas.setTool(Tool.ERASER));
        penBtn.addActionListener(e -> canvas.setTool(Tool.PEN));
        rectBtn.addActionListener(e -> canvas.setTool(Tool.RECT));
        circleBtn.addActionListener(e -> canvas.setTool(Tool.CIRCLE));
        selectBtn.addActionListener(e -> canvas.setTool(Tool.SELECT));

        add(penBtn);
        add(Box.createHorizontalStrut(6));
        add(lineBtn);
        add(Box.createHorizontalStrut(6));
        add(rectBtn);
        add(Box.createHorizontalStrut(6));
        add(circleBtn);
        add(Box.createHorizontalStrut(6));
        add(textBtn);
        add(Box.createHorizontalStrut(6));
        add(eraserBtn);
        add(Box.createHorizontalStrut(6));

        add(selectBtn);
        add(Box.createHorizontalStrut(12));
        add(divider());
        add(Box.createHorizontalStrut(12));

        add(new JLabel("Color:") {
            {
                setForeground(Color.LIGHT_GRAY);
            }
        });
        Color[] presets = {
                Color.WHITE,
                new Color(0x00E5FF),
                new Color(0xFF4081),
                new Color(0xFFC107),
                new Color(0x69F0AE)
        };

        ButtonGroup colorGroup = new ButtonGroup();

        ColorChooserButton chooser = new ColorChooserButton();

        chooser.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Choose color", chooser.getColor());

            if (chosen != null) {
                canvas.setColor(chosen);
                chooser.setColor(chosen);
                colorGroup.clearSelection();

            }
        });

        add(chooser);

        for (Color c : presets) {
            ColorSwatch swatch = new ColorSwatch(c);
            colorGroup.add(swatch);
            swatch.addActionListener(e -> {
                canvas.setColor(c);
                chooser.setColor(c);
            });

            add(swatch);
        }
        StrokePreview preview = new StrokePreview();

        float initial = sizeSlider.getValue();
        canvas.setStroke(initial);
        preview.setStroke(initial);

        sizeSlider.addMouseWheelListener(e -> {
            int delta = -e.getWheelRotation();
            sizeSlider.setValue(
                    Math.max(sizeSlider.getMinimum(),
                    Math.min(sizeSlider.getMaximum(),
                    sizeSlider.getValue() + delta))
            );
        });
        

        sizeSlider.addChangeListener(e -> {
            float s = sizeSlider.getValue();
            canvas.setStroke(s);
            preview.setStroke(s);
        });

        add(Box.createHorizontalStrut(12));
        add(new JLabel("Size:") {
            {
                setForeground(Color.LIGHT_GRAY);
            }
        });
        add(Box.createHorizontalStrut(6));
        add(preview);
        add(Box.createHorizontalStrut(6));
        add(sizeSlider);

        add(Box.createHorizontalStrut(12));
        JButton undo = new JButton("Undo");
        undo.addActionListener(e -> canvas.repaint());
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Clear?", "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                canvas.setTool(Tool.PEN);
            }
        });
        add(undo);
        add(clear);
    }
}
