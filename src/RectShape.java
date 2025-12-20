
// FILE: RectShape.java
import java.awt.*;
import java.awt.geom.*;

public class RectShape extends ShapeBase {
    Point2D.Double a, b;

    public RectShape(Color color, float stroke, Point2D.Double a, Point2D.Double b) {
        super(color, stroke, Tool.RECT);
        this.a = a;
        this.b = b;
    }

    @Override
    public void moveTo(double dx, double dy) {
        a.x += dx;
        a.y += dy;
        b.x += dx;
        b.y += dy;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        double x = Math.min(a.x, b.x);
        double y = Math.min(a.y, b.y);
        double w = Math.abs(a.x - b.x);
        double h = Math.abs(a.y - b.y);
        g2.draw(new Rectangle2D.Double(x, y, w, h));
    }

    @Override
    public boolean contains(Point2D.Double p) {
        Rectangle2D r = new Rectangle2D.Double(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.abs(a.x - b.x),
                Math.abs(a.y - b.y));
        return r.contains(p);
    }

    @Override
    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }

    @Override
    public void setEnd(Point2D.Double p) {
        this.b = p;
    }

    @Override
    public boolean isNear(Point2D.Double p, double radius) {
        return contains(p);
    }
}