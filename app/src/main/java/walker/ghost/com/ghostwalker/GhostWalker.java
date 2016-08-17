package walker.ghost.com.ghostwalker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class GhostWalker extends Service {

    public static final String ACTION_START_GHOST    = "walker.ghost.com.ghostwalker.start";
    public static final int    FOREGROUND_SERVICE_ID = 666;
    private static final String TAG = "GhostWalker";

    Thread mThread;
    boolean run;

    LocationManager lm;
    ArrayList<ParcelableGeoPoint> path;
    GeoPoint currentPosition;
    int      nextStop;
    long     lastPositionUpdate;
    int      speedMeterPerSec;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "GhostWalker Created");
    }

    public void updatePosition() {
        long currentTime = System.currentTimeMillis();
        long time = currentTime - lastPositionUpdate;

        float ratio;
        nextStop--;
        Location locA;
        Location locB;
        do {
            nextStop++;
            locA = new Location("current");
            locA.setLatitude(currentPosition.getLatitude());
            locA.setLongitude(currentPosition.getLongitude());
            locB = new Location("destination");
            locB.setLatitude(path.get(nextStop % path.size()).getGeoPoint().getLatitude());
            locB.setLongitude(path.get(nextStop % path.size()).getGeoPoint().getLongitude());

            float distanceWalked = speedMeterPerSec * (time / 1000);
            ratio = distanceWalked / locA.distanceTo(locB);
        } while(ratio > 0.98);

        float latitude = (float)(locA.getLatitude() + (locB.getLatitude()-locA.getLatitude())*ratio);
        float longitude = (float)(locA.getLongitude() + (locB.getLongitude()-locA.getLongitude())*ratio);
        currentPosition = new GeoPoint(latitude,longitude);

        double dLon = (locB.getLongitude()-locA.getLongitude());
        double y = Math.sin(dLon) * Math.cos(locB.getLatitude());
        double x = Math.cos(locA.getLatitude())*Math.sin(locB.getLatitude()) - Math.sin(locA.getLatitude())*Math.cos(locB.getLatitude())*Math.cos(dLon);
        double bearing = (float)Math.toDegrees((Math.atan2(y, x)));

        Location loc = new Location("Ghost");
        loc.setLatitude(currentPosition.getLatitude());
        loc.setLongitude(currentPosition.getLongitude());
        loc.setAltitude(0);
        loc.setAccuracy(10f);
        loc.setElapsedRealtimeNanos(System.nanoTime());
        loc.setTime(System.currentTimeMillis());
        loc.setSpeed(speedMeterPerSec);
        loc.setBearing((float)(360 - ((bearing + 360) % 360)));
        lm.setTestProviderLocation("Ghost", loc);

        lastPositionUpdate = System.currentTimeMillis();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null)
            return START_NOT_STICKY;
        if(intent.getAction() == null)
            return START_NOT_STICKY;

        if(intent.getAction().equals(ACTION_START_GHOST)) {
            path = intent.getParcelableArrayListExtra("positions");
            speedMeterPerSec = intent.getIntExtra("speed", 3);
            run = true;
            lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (lm.getProvider("Ghost") == null) {
                lm.addTestProvider("Ghost", true, true, true, false, true, true, true, 0, 1);
            }
            lm.setTestProviderEnabled("Ghost", true);

            currentPosition = path.get(0).getGeoPoint();
            nextStop = 1;
            lastPositionUpdate = System.currentTimeMillis();
            mThread = new Thread () {
                public void run() {
                    while(run) {
                        updatePosition();
                        try {
                            Thread.sleep(1000);
                        } catch(InterruptedException e) {
                            break;
                        }

                        Log.d(TAG, currentPosition.getLatitude()+","+currentPosition.getLongitude());
                    }
                }
            };
            mThread.start();

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Ghost Walker")
                    .setTicker("Walker Started")
                    .setContentText("Walker Started")
                    .setSmallIcon(R.mipmap.pokeball8bits)
                    .setOngoing(true).build();
            startForeground(FOREGROUND_SERVICE_ID, notification);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service Stopping!");
        run = false;
        mThread.interrupt();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
