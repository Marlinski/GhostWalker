package walker.ghost.com.ghostwalker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class MapsActivity extends Activity {

    private static final String TAG = "MapsActivity";

    MyLocationOverlay myLocationOverlay;
    ItemizedOverlayWithFocus<OverlayItem> itemOverlay;
    ArrayList<ParcelableGeoPoint> positions;
    ArrayList<OverlayItem> items;
    MapView map;
    ResourceProxy mResourceProxy;

    FloatingActionButton fab;
    LinearLayout mButtonLayout;
    Button       mButton;
    boolean      walking = false;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("positions", positions);
        outState.putBoolean("walking", walking);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(savedInstanceState != null) {
            positions = savedInstanceState.getParcelableArrayList("positions");
            walking   = savedInstanceState.getBoolean("walking");
        } else {
            positions = new ArrayList<>();
            walking = false;
        }

        mButtonLayout = (LinearLayout)findViewById(R.id.button_layout);
        mButton = (Button)findViewById(R.id.start_stop_button);
        map = (MapView) findViewById(R.id.map);
        fab = (FloatingActionButton) findViewById(R.id.clear_path);

        //important! set your user agent to prevent getting banned from the osm servers
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        final IMapController mapController = map.getController();
        mapController.setZoom(9);
        GeoPoint startPoint = new GeoPoint(1.3521, 103.8198);
        mapController.setCenter(startPoint);
        mResourceProxy = new CustomResourceProxy(getApplicationContext());

        items = new ArrayList<OverlayItem>();
        for(ParcelableGeoPoint geopoint : positions) {
            items.add(new OverlayItem("path", "position: " + items.size() + 1, geopoint.getGeoPoint()));
        }

        itemOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return false;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, mResourceProxy);
        itemOverlay.setFocusItemsOnTap(true);
        map.getOverlays().add(itemOverlay);


        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this.getApplicationContext(), new MapEventsReceiver() {
            @Override
            public boolean singleTapUpHelper(IGeoPoint p) {
                if(!walking) {
                    positions.add(new ParcelableGeoPoint((GeoPoint) p));
                    itemOverlay.addItem(new OverlayItem("path", "position: " + items.size() + 1, p));
                    map.invalidate();
                    Toast.makeText(getApplicationContext(), "Added location to path", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    Toast.makeText(getApplicationContext(), "Stop the walker to modify path", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            @Override
            public boolean longPressHelper(IGeoPoint p) {
                //ignored
                return false;
            }
        });
        map.getOverlays().add(0,mapEventsOverlay);


        myLocationOverlay = new MyLocationOverlay(this, map);
        myLocationOverlay.enableMyLocation(); // not on by default
        myLocationOverlay.enableCompass();
        myLocationOverlay.disableFollowLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);
        myLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mapController.animateTo(myLocationOverlay.getMyLocation());
            }
        });


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!walking) {
                    startWalking();
                } else {
                    stopWalking();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemOverlay.removeAllItems();
                items.clear();
                positions.clear();
                map.invalidate();
                Toast.makeText(getApplicationContext(), "Path Cleared", Toast.LENGTH_SHORT).show();
                if(walking)
                    stopWalking();
            }
        });

        /*
        Method[] methods = this.getSystemService(Context.LOCATION_SERVICE).getClass().getDeclaredMethods();
        for(Method method : methods) {
            Log.d(TAG, "method: " + method.getName() + method);
            if(method.getName().equals("getAllProviders")) {
                try {
                    ArrayList<LocationProvider> providers = (ArrayList<LocationProvider>)method.invoke(null);
                } catch (IllegalAccessException e) {

                } catch( InvocationTargetException e) {

                }
            }
        }
        */
    }

    void startWalking() {
        if(walking)
            return;

        if(items.size() < 2) {
            Toast.makeText(getApplicationContext(), "should have at least 2 location in the path", Toast.LENGTH_SHORT).show();
            return;
        }

        mButtonLayout.setBackgroundColor(0xff78ff81);
        mButton.setText("Stop walking!");
        walking = true;
        Intent startWalker = new Intent(MapsActivity.this, GhostWalker.class);
        startWalker.setAction(GhostWalker.ACTION_START_GHOST);
        startWalker.putParcelableArrayListExtra("positions", positions);
        startWalker.putExtra("speed", 3);
        startService(startWalker);
    }

    void stopWalking() {
        if(!walking)
            return;

        mButtonLayout.setBackgroundColor(0xffff7878);
        mButton.setText("Let's go for a walk!");
        walking = false;
        Intent stopWalker = new Intent(MapsActivity.this, GhostWalker.class);
        stopService(stopWalker);
    }

}