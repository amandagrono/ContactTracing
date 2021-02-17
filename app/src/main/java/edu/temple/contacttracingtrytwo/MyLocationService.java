package edu.temple.contacttracingtrytwo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class MyLocationService extends Service {

    public static final String CHANNEL_ID = "TrackingServiceChannel";
    private LocationManager locationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = getSystemService(LocationManager.class);
        createNotificationChannel();
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, notifIntent, 0);

        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Contact Tracer")
                .setContentText("Tracking your location for contact tracing purposes.")
                .build();

        startForeground(1, notif);

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tracking Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    public CheckPermissionListener permissionListener;

    public class TrackingServiceBinder extends Binder {
        public void startTracking() {
            MyLocationService.this.startTracking(10);
        }

        public void restartTracking() {
            MyLocationService.this.stopTracking();
            startTracking();
        }

        public void stopTracking() {
            MyLocationService.this.stopTracking();
            stopSelf();
        }

        public void setPermissionListener(CheckPermissionListener listener) {
            MyLocationService.this.permissionListener = listener;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TrackingServiceBinder();
    }
    @Override
    public void onDestroy() {
        stopTracking();
    }
    private Location lastLocation;
    private LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location nextLocation) {
            Log.d("Tracing", "Recorded Location Change");
            if (lastLocation != null) {
                Long last = lastLocation.getTime();
                Long next = nextLocation.getTime();
                long diffMin = (next - last) / 1000 / 60;

                if (diffMin > 0)
                    Log.d("Tracing", "User has been sedentary for " + diffMin + " minutes.");


            }
            lastLocation = nextLocation;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };
    @SuppressLint("MissingPermission") // Permission is definitely checked
    private void startTracking(int distance) {
        if (permissionListener == null) throw new RuntimeException("Tracking service requires a permission manager.");
        if (!permissionListener.hasPermission()) {
            permissionListener.acquirePermission();
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, distance, listener);
    }
    public void stopTracking() {
        locationManager.removeUpdates(listener);
    }
}
