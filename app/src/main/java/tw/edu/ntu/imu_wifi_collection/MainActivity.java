package tw.edu.ntu.imu_wifi_collection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView wifi_tv;
    private TextView acc_tv, gyro_tv, game_tv;
    private EditText position_x_et;
    private EditText position_y_et;
    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope, gameRotationVector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR) != null){
            gameRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        }
    }

    private void findViews(){
        wifi_tv = findViewById(R.id.wifi_tv);
        acc_tv = findViewById(R.id.acc_tv);
        gyro_tv = findViewById(R.id.gyro_tv);
        game_tv = findViewById(R.id.game_tv);
        wifi_tv.setMovementMethod(new ScrollingMovementMethod());
        position_x_et = findViewById(R.id.position_x_et);
        position_y_et = findViewById(R.id.position_y_et);
    }


    public void record(View view){
        String position_x = position_x_et.getText().toString();
        String position_y = position_y_et.getText().toString();
        if(position_x.isEmpty() || position_y.isEmpty()){
            Toast.makeText(this, "please enter the position", Toast.LENGTH_LONG).show();
        }else{
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            registerReceiver(wifiScanReceiver, intentFilter);

            boolean success = wifiManager.startScan();
            if (!success) {
                // scan failure handling
                scanFailure();
            }
        }
    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();

        String show = "";
        for(ScanResult result: results){
            show += result.BSSID + "," + result.level + "," + result.SSID + "\n";
        }
        wifi_tv.setText(show);
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        String show = "";
        for(ScanResult result: results){
            show += result.BSSID + "," + result.level + "," + result.SSID + "\n";
        }
        wifi_tv.setText(show);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor == accelerometer){
            float x = event.values[0];
            float y = event.values[0];
            float z = event.values[0];
            String show = String.format("x: %f, y:%f, z:%f", x, y, z);
            acc_tv.setText(show);
        }else if(event.sensor == gyroscope){
            float x = event.values[0];
            float y = event.values[0];
            float z = event.values[0];
            String show = String.format("x: %f, y:%f, z:%f", x, y, z);
            gyro_tv.setText(show);
        }else if(event.sensor == gameRotationVector){
            float x = event.values[0];
            float y = event.values[0];
            float z = event.values[0];
            float sum = 1 - (x*x + y*y + z*z);
            float w = sum > 0 ? (float)Math.sqrt(sum) : 0;
            String show = String.format("w: %f, x: %f, y:%f, z:%f", w, x, y, z);
            game_tv.setText(show);
        }else{
            Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, 5000);
        sensorManager.registerListener(this, gyroscope, 5000);
        sensorManager.registerListener(this, gameRotationVector, 5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}