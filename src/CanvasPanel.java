
// FILE: CanvasPanel.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;

public class CanvasPanel extends JComponent {
    private final List<ShapeBase> shapes = new ArrayList<>();
    private final java.util.Deque<Runnable> undoStack = new ArrayDeque<>();
    private Point2D.Double dragOffset = null;
    private ShapeBase current = null;
    private ShapeBase selected = null;

    // world transform: screen = world * scale + translate
    private double scale = 1.0;
    private double translateX = 0;
    private double translateY = 0;
    private boolean panning = false;

    private Point lastPanScreen = null;
    private Tool activeTool = Tool.PEN;
    private Color activeColor = Color.WHITE;
    private float activeStroke = 3f;

    private transient JTextField liveTextField = null;

    public void undo() {
        if (!undoStack.isEmpty()) {
            undoStack.pop().run();
            repaint();
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public void clear() {
        if (shapes.isEmpty())
            return;

        List<ShapeBase> snapshot = new ArrayList<>(shapes);
        undoStack.push(() -> {
            shapes.clear();
            shapes.addAll(snapshot);
        });

        shapes.clear();
        selected = null;
        repaint();
    }

    public CanvasPanel() {
        setBackground(new Color(30, 30, 30));
        setOpaque(true);
        setFocusable(true);
        enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK); // needed for gestures

        MouseAdapter ma = new MouseAdapter() {
            Point2D.Double startWorld = null;

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();

                // Alt + click-drag panning (for laptops)
                if (e.isAltDown()) {
                    panning = true;
                    lastPanScreen = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    return;
                }

                // middle or right+Shift panning (for mouse users)
                if (SwingUtilities.isMiddleMouseButton(e)
                        || (SwingUtilities.isRightMouseButton(e) && e.isShiftDown())) {
                    panning = true;
                    lastPanScreen = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    return;
                }

                Point2D.Double w = screenToWorld(e.getPoint());
                startWorld = w;
                // ---- SELECT TOOL LOGIC ----
                if (activeTool == Tool.SELECT) {
                    ShapeBase hit = hitTest(w);
                    selected = hit;

                    if (hit != null) {
                        Rectangle2D bounds = hit.getBounds();
                        dragOffset = new Point2D.Double(
                                w.x - bounds.getX(),
                                w.y - bounds.getY());
                    } else {
                        dragOffset = null;
                    }

                    repaint();
                    return; // IMPORTANT: stop other tools
                }

                switch (activeTool) {
                    case PEN:
                        current = new FreehandShape(activeColor, activeStroke);
                        ((FreehandShape) current).addPoint(w);
                        shapes.add(current);
                        {
                            final ShapeBase created = current;
                            undoStack.push(() -> shapes.remove(created));
                        }
                        break;
                    case LINE:
                        current = new LineShape(activeColor, activeStroke, w, w);
                        shapes.add(current);
                        {
                            final ShapeBase created = current;
                            undoStack.push(() -> shapes.remove(created));
                        }
                        break;
                    case RECT:
                        current = new RectShape(activeColor, activeStroke, w, w);
                        shapes.add(current);
                        {
                            final ShapeBase created = current;
                            undoStack.push(() -> shapes.remove(created));
                        }
                        break;
                    case CIRCLE:
                        current = new EllipseShape(activeColor, activeStroke, w, w);
                        shapes.add(current);
                        {
                            final ShapeBase created = current;
                            undoStack.push(() -> shapes.remove(created));
                        }
                        break;
                    case ERASER:
                        eraseAt(w);
                        break;
                    case TEXT:
                        beginInlineText(w);
                        break;
                }

                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // ---- DRAG SELECTED SHAPE ----
                if (activeTool == Tool.SELECT && selected != null && dragOffset != null) {
                    Point2D.Double w = screenToWorld(e.getPoint());
                    selected.moveTo(
                            w.x - dragOffset.x,
                            w.y - dragOffset.y);
                    repaint();
                    return;
                }

                if (panning && lastPanScreen != null) {
                    Point now = e.getPoint();
                    double dx = now.x - lastPanScreen.x;
                    double dy = now.y - lastPanScreen.y;
                    translateX += dx;
                    translateY += dy;
                    lastPanScreen = now;
                    repaint();
                    return;
                }

                Point2D.Double w = screenToWorld(e.getPoint());
                if (current != null) {
                    if (current instanceof FreehandShape) {
                        ((FreehandShape) current).addPoint(w);
                    } else {
                        current.setEnd(w);
                    }
                    repaint();
                } else if (activeTool == Tool.ERASER) {
                    eraseAt(w);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (panning) {
                    panning = false;
                    lastPanScreen = null;
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }
                dragOffset = null;
                current = null;
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {

                // ----- PINCH ZOOM -----
                if (e.isControlDown() || e.isMetaDown()) {
                    double zoomFactor = 1 - e.getPreciseWheelRotation() * 0.1;
                    double newScale = Math.max(0.1, Math.min(8.0, scale * zoomFactor));

                    Point mouse = e.getPoint();
                    Point2D.Double worldBefore = screenToWorld(mouse);

                    scale = newScale;
                    translateX = mouse.x - worldBefore.x * scale;
                    translateY = mouse.y - worldBefore.y * scale;

                    repaint();
                    return;
                }

                // ----- TWO-FINGER PAN -----
                double delta = e.getPreciseWheelRotation();

                if (e.isShiftDown()) {
                    translateX -= delta * 60;
                } else {
                    translateY -= delta * 60;
                }

                repaint();
            }

        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);

        setupKeyBindings();

    }

    private void setupKeyBindings() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cancelText");
        am.put("cancelText", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelInlineText();
            }
        });

        im.put(KeyStroke.getKeyStroke("DELETE"), "deleteSelected");
        am.put("deleteSelected", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selected != null) {
                    final ShapeBase removed = selected;
                    final int index = shapes.indexOf(removed);
                    if (index >= 0) {
                        undoStack.push(() -> {
                            if (!shapes.contains(removed)) {
                                if (index >= 0 && index <= shapes.size()) {
                                    shapes.add(index, removed);
                                } else {
                                    shapes.add(removed);
                                }
                            }
                        });
                        shapes.remove(index);
                    }
                    selected = null;
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        AffineTransform at = new AffineTransform();
        at.translate(translateX, translateY);
        at.scale(scale, scale);
        g2.transform(at);

        for (ShapeBase s : shapes) {
            s.draw(g2);
            if (s == selected) {
                Rectangle2D b = s.getBounds();
                g2.setStroke(new BasicStroke(1f / (float) scale));
                g2.setColor(new Color(180, 180, 255, 180));
                g2.draw(b);
            }
        }

        g2.dispose();
    }

    private ShapeBase hitTest(Point2D.Double worldPt) {
        for (int i = shapes.size() - 1; i >= 0; i--) {
            ShapeBase s = shapes.get(i);
            if (s.contains(worldPt))
                return s;
        }
        return null;
    }

    private void eraseAt(Point2D.Double worldPt) {
        List<ShapeBase> toRemove = new ArrayList<>();
        double radius = 20 / Math.max(scale, 0.1);
        List<Integer> originalIndices = new ArrayList<>();
        for (int i = 0; i < shapes.size(); i++) {
            ShapeBase s = shapes.get(i);
            if (s.isNear(worldPt, radius)) {
                toRemove.add(s);
                originalIndices.add(i);
            }
        }
        if (!toRemove.isEmpty()) {
            // capture snapshot for undo
            final List<ShapeBase> removedShapes = new ArrayList<>(toRemove);
            final List<Integer> indices = new ArrayList<>(originalIndices);
            undoStack.push(() -> {
                // restore in ascending index order
                for (int i = 0; i < indices.size(); i++) {
                    int idx = indices.get(i);
                    ShapeBase s = removedShapes.get(i);
                    if (idx >= 0 && idx <= shapes.size()) {
                        shapes.add(idx, s);
                    } else {
                        shapes.add(s);
                    }
                }
            });

            shapes.removeAll(toRemove);
            if (toRemove.contains(selected)) {
                selected = null;
            }
            repaint();
        }
    }

    // coordinate conversions
    private Point2D.Double screenToWorld(Point screen) {
        double wx = (screen.x - translateX) / scale;
        double wy = (screen.y - translateY) / scale;
        return new Point2D.Double(wx, wy);
    }

    private Point worldToScreen(Point2D.Double world) {
        int sx = (int) Math.round(world.x * scale + translateX);
        int sy = (int) Math.round(world.y * scale + translateY);
        return new Point(sx, sy);
    }

    // inline text editor
    private void beginInlineText(Point2D.Double worldPt) {
        cancelInlineText();
        Point screenPt = worldToScreen(worldPt);
        liveTextField = new JTextField();
        liveTextField.setColumns(20);
        liveTextField.setForeground(Color.WHITE);
        liveTextField.setBackground(new Color(40, 40, 40));
        liveTextField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));

        JLayeredPane lp = getRootPane().getLayeredPane();
        Point paneLoc = SwingUtilities.convertPoint(this, screenPt, lp);
        liveTextField.setBounds(paneLoc.x, paneLoc.y - 10, 300, 26);
        lp.add(liveTextField, JLayeredPane.POPUP_LAYER);
        liveTextField.requestFocusInWindow();

        liveTextField.addActionListener(ev -> commitInlineText(worldPt));
        liveTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(FocusEvent e) {
                commitInlineText(worldPt);
            }
        });

        liveTextField.registerKeyboardAction(ev -> cancelInlineText(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_FOCUSED);
    }

    private void commitInlineText(Point2D.Double worldPt) {
        if (liveTextField == null)
            return;
        String txt = liveTextField.getText();
        removeLiveTextField();
        if (txt != null && !txt.trim().isEmpty()) {
            TextShape t = new TextShape(activeColor, activeStroke, worldPt, txt);
            shapes.add(t);
            undoStack.push(() -> shapes.remove(t));
            repaint();
        }
    }

    private void cancelInlineText() {
        removeLiveTextField();
        repaint();
    }

    private void removeLiveTextField() {
        if (liveTextField != null) {
            JLayeredPane lp = getRootPane().getLayeredPane();
            lp.remove(liveTextField);
            lp.revalidate();
            lp.repaint();
            liveTextField = null;
        }
    }

    // external setters
    public void setTool(Tool t) {
        this.activeTool = t;
    }

    public void setColor(Color c) {
        this.activeColor = c;
    }

    public void setStroke(float s) {
        this.activeStroke = s;
    }

    // --------- Persistence (save/load) ---------

    private static String colorToHex(Color c) {
        return String.format("#%06X", (0xFFFFFF & c.getRGB()));
    }

    private static Color parseColor(String hex) {
        String h = hex.trim();
        if (h.startsWith("#")) {
            h = h.substring(1);
        }
        int rgb = (int) Long.parseLong(h, 16);
        return new Color(rgb);
    }

    public void saveToFile(File file) throws IOException {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            for (ShapeBase s : shapes) {
                String line = serializeShape(s);
                if (line != null && !line.isEmpty()) {
                    out.println(line);
                }
            }
        }
    }

    public void loadFromFile(File file) throws IOException {
        List<ShapeBase> loaded = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                ShapeBase s = deserializeShape(line);
                if (s != null) {
                    loaded.add(s);
                }
            }
        }

        shapes.clear();
        shapes.addAll(loaded);
        selected = null;
        undoStack.clear();
        repaint();
    }

    private String serializeShape(ShapeBase s) {
        String colorHex = colorToHex(s.color);
        float strokeWidth = s.stroke;

        if (s instanceof FreehandShape) {
            FreehandShape f = (FreehandShape) s;
            java.util.List<Point2D.Double> pts = f.getPoints();
            int n = pts.size();
            if (n == 0) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("FREEHAND ").append(colorHex).append(" ").append(strokeWidth).append(" ").append(n);
            for (Point2D.Double pt : pts) {
                sb.append(" ").append(pt.x).append(" ").append(pt.y);
            }
            return sb.toString();
        } else if (s instanceof LineShape) {
            LineShape l = (LineShape) s;
            Rectangle2D b = l.getBounds();
            double x1 = b.getX();
            double y1 = b.getY();
            double x2 = b.getX() + b.getWidth();
            double y2 = b.getY() + b.getHeight();
            return String.format("LINE %s %f %f %f %f", colorHex, strokeWidth, x1, y1, x2, y2);
        } else if (s instanceof RectShape) {
            RectShape r = (RectShape) s;
            Rectangle2D b = r.getBounds();
            double x1 = b.getX();
            double y1 = b.getY();
            double x2 = b.getX() + b.getWidth();
            double y2 = b.getY() + b.getHeight();
            return String.format("RECT %s %f %f %f %f", colorHex, strokeWidth, x1, y1, x2, y2);
        } else if (s instanceof EllipseShape) {
            EllipseShape e = (EllipseShape) s;
            Rectangle2D b = e.getBounds();
            double x1 = b.getX();
            double y1 = b.getY();
            double x2 = b.getX() + b.getWidth();
            double y2 = b.getY() + b.getHeight();
            return String.format("ELLIPSE %s %f %f %f %f", colorHex, strokeWidth, x1, y1, x2, y2);
        } else if (s instanceof TextShape) {
            TextShape t = (TextShape) s;
            Rectangle2D b = t.getBounds();
            double x = b.getX();
            double y = b.getY() + b.getHeight(); // approximate baseline
            return String.format("TEXT %s %f %f %f %s", colorHex, strokeWidth, x, y, t.text.replace("\n", " "));
        }

        return null;
    }

    private ShapeBase deserializeShape(String line) {
        StringTokenizer st = new StringTokenizer(line, " ");
        if (!st.hasMoreTokens()) {
            return null;
        }
        String type = st.nextToken();
        if (!st.hasMoreTokens()) {
            return null;
        }
        String colorToken = st.nextToken();
        if (!st.hasMoreTokens()) {
            return null;
        }

        float strokeWidth;
        try {
            strokeWidth = Float.parseFloat(st.nextToken());
        } catch (NumberFormatException ex) {
            return null;
        }

        Color c;
        try {
            c = parseColor(colorToken);
        } catch (Exception ex) {
            return null;
        }

        try {
            if ("FREEHAND".equals(type)) {
                int n = Integer.parseInt(st.nextToken());
                List<Point2D.Double> pts = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    double x = Double.parseDouble(st.nextToken());
                    double y = Double.parseDouble(st.nextToken());
                    pts.add(new Point2D.Double(x, y));
                }
                FreehandShape f = new FreehandShape(c, strokeWidth);
                for (Point2D.Double p : pts) {
                    f.addPoint(p);
                }
                return f;
            } else if ("LINE".equals(type)) {
                double x1 = Double.parseDouble(st.nextToken());
                double y1 = Double.parseDouble(st.nextToken());
                double x2 = Double.parseDouble(st.nextToken());
                double y2 = Double.parseDouble(st.nextToken());
                Point2D.Double a = new Point2D.Double(x1, y1);
                Point2D.Double b = new Point2D.Double(x2, y2);
                return new LineShape(c, strokeWidth, a, b);
            } else if ("RECT".equals(type)) {
                double x1 = Double.parseDouble(st.nextToken());
                double y1 = Double.parseDouble(st.nextToken());
                double x2 = Double.parseDouble(st.nextToken());
                double y2 = Double.parseDouble(st.nextToken());
                Point2D.Double a = new Point2D.Double(x1, y1);
                Point2D.Double b = new Point2D.Double(x2, y2);
                return new RectShape(c, strokeWidth, a, b);
            } else if ("ELLIPSE".equals(type)) {
                double x1 = Double.parseDouble(st.nextToken());
                double y1 = Double.parseDouble(st.nextToken());
                double x2 = Double.parseDouble(st.nextToken());
                double y2 = Double.parseDouble(st.nextToken());
                Point2D.Double a = new Point2D.Double(x1, y1);
                Point2D.Double b = new Point2D.Double(x2, y2);
                return new EllipseShape(c, strokeWidth, a, b);
            } else if ("TEXT".equals(type)) {
                double x = Double.parseDouble(st.nextToken());
                double y = Double.parseDouble(st.nextToken());
                StringBuilder sb = new StringBuilder();
                while (st.hasMoreTokens()) {
                    if (sb.length() > 0) {
                        sb.append(' ');
                    }
                    sb.append(st.nextToken());
                }
                String txt = sb.toString();
                Point2D.Double pos = new Point2D.Double(x, y);
                return new TextShape(c, strokeWidth, pos, txt);
            }
        } catch (Exception ex) {
            return null;
        }

        return null;
    }

    // --------- PNG export ---------

    public void exportToPng(File file) throws IOException {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // background
        g2.setColor(getBackground());
        g2.fillRect(0, 0, w, h);

        // apply same world transform as paintComponent, but without selection rectangles
        AffineTransform at = new AffineTransform();
        at.translate(translateX, translateY);
        at.scale(scale, scale);
        g2.transform(at);

        for (ShapeBase s : shapes) {
            s.draw(g2);
        }

        g2.dispose();
        ImageIO.write(img, "png", file);
    }
}
