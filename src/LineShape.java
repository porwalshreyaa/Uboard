
// FILE: LineShape.java
import java.awt.*;
import java.awt.geom.*;

public class LineShape extends ShapeBase {
    Point2D.Double a, b;

    public LineShape(Color color, float stroke, Point2D.Double a, Point2D.Double b) {
        super(color, stroke, Tool.LINE);
        this.a = a;
        this.b = b;
    }

    @Override
    public void moveTo(double x, double y) {
        Rectangle2D bounds = getBounds();

        double dx = x - bounds.getX();
        double dy = y - bounds.getY();

        a.x += dx;
        a.y += dy;
        b.x += dx;
        b.y += dy;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        g2.draw(new Line2D.Double(a, b));
    }

    @Override
    public boolean contains(Point2D.Double p) {
        Line2D.Double line = new Line2D.Double(a, b);
        return line.ptSegDist(p) <= stroke * 1.5;
    }

    @Override
    public Rectangle2D getBounds() {
        return new Line2D.Double(a, b).getBounds2D();
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
