package com.example.necky0.randomlocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    TextView text;
    Random generator;

    LocationManager locationManager;
    boolean isGPSEnable;
    double latitude;
    double longitude;

    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private static final int SHAKE_THRESHOLD = 12;

    Localization[] LOCALIZATIONS = new Localization[] {
            new Localization("Hala Stulecia", 51.106944, 17.076944),
            new Localization("Sanok", 49.558333, 22.205556),
            new Localization("Sao Paulo", -23.5, -46.616667),
    };

    private static final int INITIAL_REQUEST = 1337;
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initFindViewById();
        initGPSSensor();
        initAccelSensor();
    }

    private void initAccelSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    private void initGPSSensor() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, INITIAL_PERMS, INITIAL_REQUEST);
            return;
        }

        isGPSEnable = false;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
    }

    public void initFindViewById() {
        text = findViewById(R.id.text);
    }

    public void init() {
        generator = new Random();
    }

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            isGPSEnable = true;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    private double countDist(double length1, double width1, double length2, double width2) {
        double dist = Math.sqrt(Math.pow(length2-length1,2)+Math.pow(Math.cos(((length1*Math.PI)/180))*(width2-width1),2))*(40075.704/360);
        return round(dist,2);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @SuppressLint("SetTextI18n")
    public void randomDistance() {
        if (isGPSEnable) {
            int index = generator.nextInt(LOCALIZATIONS.length);
            double dist = countDist(LOCALIZATIONS[index].getLength(), LOCALIZATIONS[index].getWidth(), latitude, longitude);

            text.setText(LOCALIZATIONS[index].getName()+" jest "+dist+" km od Ciebie.");
        }
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            if (mAccel > SHAKE_THRESHOLD) {
                randomDistance();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        randomDistance();
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }
}
