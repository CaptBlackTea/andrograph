package org.agp8x.android.lib.andrograph.model.defaults;

import org.agp8x.android.lib.andrograph.model.Coordinate;
import org.agp8x.android.lib.andrograph.model.PositionProvider;

import java.util.Map;

/**
 * Store {@link Coordinate}s for Vertices in a Map. Might be inefficient
 *
 * @author clemensk
 *         <p>
 *         30.11.16.
 */

public class MapPositionProvider<V> implements PositionProvider<V> {
    protected final Coordinate fallback;
    protected Map<V, Coordinate> positionMap;

    public MapPositionProvider(Map<V, Coordinate> positionMap, Coordinate fallback) {
        this.positionMap = positionMap;
        this.fallback = fallback;
    }

    @Override
    public Coordinate getPosition(V vertex) {
        if (!positionMap.containsKey(vertex)) {
            System.out.println("not in map " + fallback);
            positionMap.put(vertex, fallback);
        }
        return positionMap.get(vertex);
    }

    @Override
    public boolean update(Coordinate old, Coordinate updated) {
        if (positionMap.containsValue(old)) {
            V key = null;
            for (Map.Entry<V, Coordinate> entry : positionMap.entrySet()) {
                if (entry.getValue().equals(old)) {
                    key = entry.getKey();
                }
            }
            if (key != null) {
                positionMap.put(key, updated);
            }
            return true;
        }
        return false;
    }

    @Override
    public V getSelected(Coordinate action) {
        for (Map.Entry<V, Coordinate> entry : positionMap.entrySet()) {
            if (action.intersects(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public boolean setPosition(V vertex, Coordinate position) {
        positionMap.put(vertex, position);
        return true;
    }

    @Override
    public void remove(V vertex) {
        positionMap.remove(vertex);
    }
}
