package org.agp8x.android.lib.andrograph.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import org.agp8x.android.lib.andrograph.Coordinate;
import org.agp8x.android.lib.andrograph.GraphViewController;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public class GraphView<V, E extends DefaultEdge> extends View {
    private GraphViewController<V, E> controller;
    private Paint paint;
    private int radius;
    private Dragging dragging;
    private int contentWidth;
    private int contentHeight;

    public GraphView(Context context) {
        super(context);
        init(null, 0);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void setController(GraphViewController<V, E> controller) {
        this.controller = controller;
        invalidate();
    }

    private void init(AttributeSet attrs, int defStyle) {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);

        radius = 25;
        dragging = new Dragging();
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                boolean update = false;
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (dragging.object == null) {
                            Coordinate action = event2coordinate(motionEvent);
                            V obj = controller.getSelected(action);
                            System.out.println("find new object@" + action);
                            if (obj != null) {
                                System.out.println("found new obj: " + obj);
                                dragging.object = obj;
                                dragging.old = controller.getPosition(obj);
                                dragging.xy = event2pair(motionEvent);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (dragging.object != null) {
                            dragging.xy = event2pair(motionEvent);
                            update = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (dragging.object != null) {
                            dragging.xy = event2pair(motionEvent);
                            controller.update(dragging.old, pair2coordinate(dragging.xy));
                            dragging.object = null;
                            update = true;
                        }
                        break;
                }
                if (update) {
                    invalidate();
                }
                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (controller == null) {
            return;
        }

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        contentWidth = getWidth() - paddingLeft - paddingRight;
        contentHeight = getHeight() - paddingTop - paddingBottom;
        Coordinate c;
        Pair<Float, Float> xy;
        Graph<V, E> g = controller.getGraph();
        for (V v : g.vertexSet()) {
            if (v.equals(dragging.object)) {
                xy = dragging.xy;
            } else {
                c = controller.getPosition(v);
                xy = coordinate2view(c);
            }
            canvas.drawCircle(xy.first, xy.second, radius, paint);
            for (E edge : g.edgeSet()) {
                Pair<Float, Float> xy1 = vertex2view(g.getEdgeSource(edge));
                Pair<Float, Float> xy2 = vertex2view(g.getEdgeTarget(edge));
                Paint p = controller.getEdgePaint(edge);
                System.out.println(xy1+"; "+xy2+" - "+ p);
                canvas.drawLine(xy1.first, xy1.second, xy2.first, xy2.second, p);
            }
        }
    }

    private Pair<Float, Float> vertex2view(V vertex) {
        if (vertex.equals(dragging.object)){
            return dragging.xy;
        }
        return coordinate2view(controller.getPosition(vertex));
    }

    private Pair<Float, Float> coordinate2view(Coordinate coordinate) {
        return new Pair<>((float) (coordinate.getX() * contentWidth), (float) (coordinate.getY() * contentHeight));
    }

    private Pair<Float, Float> event2pair(MotionEvent event) {
        return new Pair<>(event.getX(), event.getY());
    }

    private Coordinate pair2coordinate(Pair<Float, Float> xy) {
        return new Coordinate(xy.first / contentWidth, xy.second / contentHeight);
    }

    private Coordinate event2coordinate(MotionEvent event) {
        return new Coordinate(event.getX() / contentWidth, event.getY() / contentHeight);
    }

    private class Dragging {
        V object;
        Coordinate old;
        Pair<Float, Float> xy;
    }
}
