package walker.ghost.com.ghostwalker;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * Empty overlay than can be used to detect events on the map,
 * and to throw them to a MapEventsReceiver.
 * @see MapEventsReceiver
 * @author M.Kergall
 */
public class MapEventsOverlay extends Overlay {

    private MapEventsReceiver mReceiver;

    /**
     * @param ctx the context
     * @param receiver the object that will receive/handle the events.
     * It must implement MapEventsReceiver interface.
     */
    public MapEventsOverlay(Context ctx, MapEventsReceiver receiver) {
        super(ctx);
        mReceiver = receiver;
    }

    @Override protected void draw(Canvas c, MapView osmv, boolean shadow) {
    }

    @Override public boolean onSingleTapUp(MotionEvent e, MapView mapView){
        Projection proj = mapView.getProjection();
        IGeoPoint p = proj.fromPixels((int)e.getX(), (int)e.getY());
        return mReceiver.singleTapUpHelper(p);
    }

    @Override public boolean onLongPress(MotionEvent e, MapView mapView) {
        Projection proj = mapView.getProjection();
        IGeoPoint p = proj.fromPixels((int)e.getX(), (int)e.getY());
        return mReceiver.longPressHelper(p);
    }

}

