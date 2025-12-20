
// FILE: CanvasPanel.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

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
                        break;
                    case LINE:
                        current = new LineShape(activeColor, activeStroke, w, w);
                        shapes.add(current);
                        break;
                    case RECT:
                        current = new RectShape(activeColor, activeStroke, w, w);
                        shapes.add(current);
                        break;
                    case CIRCLE:
                        current = new EllipseShape(activeColor, activeStroke, w, w);
                        shapes.add(current);
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
                    shapes.remove(selected);
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
        for (ShapeBase s : shapes) {
            if (s.isNear(worldPt, radius))
                toRemove.add(s);
        }
        if (!toRemove.isEmpty()) {
            shapes.removeAll(toRemove);
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
}
