package edu.temple.contacttracingtrytwo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CheckPermissionListener, DashboardButtonListener {

    LinkedList<MyUUID> linkedList;
    MyUUID myUUID = new MyUUID();

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private MyLocationService.TrackingServiceBinder trackingServiceBinder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        editor = preferences.edit();

        if(!loadData()){
            linkedList = new LinkedList<>();
            generateUUID();
            linkedList.add(myUUID);
            clearLinkedList();
        }
        if(!checkIfUUIDTodayExists()){
            MyUUID tempMyUUID = new MyUUID();
            tempMyUUID.setUuid(myUUID.getUuid());
            tempMyUUID.setDate(myUUID.getDate());
            linkedList.remove(myUUID);
            linkedList.add(tempMyUUID);
            generateUUID();
        }
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (fragment == null) fragment = DashboardFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();


        saveData();

    }



    public Date getCurrentDate(){
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public void clearLinkedList(){
        for(int i = 0; i < linkedList.size(); i ++){
            if(compareToLast14Days(linkedList.get(i).getDate())){
                linkedList.removeFirst();
            }
        }
    }

    public boolean compareToLast14Days(Date testDate){ //returns true if OLDER THAN 14 DAYS
        Calendar fourteenDaysAgo = new GregorianCalendar(); //returns false if NEWER THAN 14 DAYS
        fourteenDaysAgo.set(Calendar.HOUR, 0);
        fourteenDaysAgo.set(Calendar.MINUTE, 0);
        fourteenDaysAgo.set(Calendar.SECOND, 0);
        fourteenDaysAgo.set(Calendar.MILLISECOND, 0);
        fourteenDaysAgo.add(Calendar.DAY_OF_MONTH, -14);

        Log.d("Compare", "Fourteen Days Ago: " + fourteenDaysAgo.getTime().toString());

        if(testDate.compareTo(fourteenDaysAgo.getTime()) < 0){
            return true;
        }
        else return false;

    }
    public boolean checkIfUUIDTodayExists(){
        if(myUUID.getDate().compareTo(getCurrentDate()) == 0){
            return true; //returns true if latest UUID is from today
        }
        else return false;
    }


    public void generateUUID(){

        myUUID.setUuid(UUID.randomUUID());
        myUUID.setDate(getCurrentDate());

    }
    public void saveData(){
        Gson gson = new Gson();
        String json = gson.toJson(linkedList);
        editor.putString("linkedList", json);
        editor.apply();

    }
    public boolean loadData(){

        Gson gson = new Gson();
        String json = preferences.getString("linkedList", "");
        Type type = new TypeToken<LinkedList<MyUUID>>(){}.getType();
        if(!json.equals("")){
            linkedList = gson.fromJson(json, type);
            myUUID = linkedList.getLast();
            return true;
        }
        else{
            return false;
        }

    }
    // Permission management

    @Override
    public boolean hasPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void acquirePermission() {
        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, CheckPermissionListener.REQUEST_ID);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CheckPermissionListener.REQUEST_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(this)
                            .setTitle("YOOOOOO")
                            .setMessage("i need to use your location ")
                            .setPositiveButton("Ok", (dialogInterface, i) -> requestPermissions(
                                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                                    CheckPermissionListener.REQUEST_ID
                            ))
                            .create().show();
                } else {
                    Toast.makeText(this, "we need your location in order to run this app", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            trackingServiceBinder.startTracking();
        }
    }

    // Tracing service management
    private ServiceConnection tracingServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            trackingServiceBinder = (MyLocationService.TrackingServiceBinder) iBinder;
            trackingServiceBinder.setPermissionListener(MainActivity.this);
            trackingServiceBinder.startTracking();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            trackingServiceBinder = null;
        }
    };

    @Override
    public void onStartTracking() {
        if (trackingServiceBinder != null) {
            Toast.makeText(this, "Service Already Running", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, MyLocationService.class);
        startService(intent);
        bindService(intent, tracingServiceConn, BIND_AUTO_CREATE);
    }

    @Override
    public void onStopTracking() {
        if (trackingServiceBinder == null) {
            Toast.makeText(this, "Service Not Running", Toast.LENGTH_LONG).show();
            return;
        }

        trackingServiceBinder.stopTracking();
        unbindService(tracingServiceConn);
        trackingServiceBinder = null;
    }
}