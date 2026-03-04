
// FILE: FreehandShape.java
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class FreehandShape extends ShapeBase {
    private final List<Point2D.Double> points = new ArrayList<>();

    public FreehandShape(Color color, float stroke) {
        super(color, stroke, Tool.PEN);
    }

    public void addPoint(Point2D.Double p) {
        points.add(p);
    }

    public List<Point2D.Double> getPoints() {
        return new ArrayList<>(points);
    }

    @Override
    public void moveTo(double x, double y) {
        Rectangle2D bounds = getBounds();

        double dx = x - bounds.getX();
        double dy = y - bounds.getY();

        for (Point2D.Double pt : points) {
            pt.x += dx;
            pt.y += dy;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (points.size() < 2)
            return;
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);

        Path2D.Double path = new Path2D.Double();
        Point2D.Double p0 = points.get(0);
        path.moveTo(p0.x, p0.y);
        // Quadratic bezier smoothing
        for (int i = 1; i < points.size() - 1; i++) {
            Point2D.Double p1 = points.get(i);
            Point2D.Double p2 = points.get(i + 1);
            double cx = (p1.x + p2.x) / 2.0;
            double cy = (p1.y + p2.y) / 2.0;
            path.quadTo(p1.x, p1.y, cx, cy);
        }
        // draw last segment as line
        Point2D.Double last = points.get(points.size() - 1);
        path.lineTo(last.x, last.y);
        g2.draw(path);
    }

    @Override
    public boolean contains(Point2D.Double p) {
        // check distance to any segment point
        for (Point2D.Double pt : points) {
            if (pt.distance(p) <= stroke * 1.5)
                return true;
        }
        return false;
    }

    @Override
    public Rectangle2D getBounds() {
        double minx = Double.POSITIVE_INFINITY, miny = Double.POSITIVE_INFINITY, maxx = Double.NEGATIVE_INFINITY,
                maxy = Double.NEGATIVE_INFINITY;
        for (Point2D.Double pt : points) {
            minx = Math.min(minx, pt.x);
            miny = Math.min(miny, pt.y);
            maxx = Math.max(maxx, pt.x);
            maxy = Math.max(maxy, pt.y);
        }
        if (points.isEmpty())
            return new Rectangle2D.Double(0, 0, 0, 0);
        return new Rectangle2D.Double(minx - stroke, miny - stroke, (maxx - minx) + stroke * 2,
                (maxy - miny) + stroke * 2);
    }

    @Override
    public void setEnd(Point2D.Double p) {
        /* not used for freehand */ }

    @Override
    public boolean isNear(Point2D.Double p, double radius) {
        for (Point2D.Double pt : points)
            if (pt.distance(p) <= radius)
                return true;
        return false;
    }
}
