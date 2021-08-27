package tw.edu.ntu.imu_wifi_collection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView wifi_tv;
    private EditText file_et;
    private TextView acc_tv, gyro_tv, game_tv;
    private EditText position_x_et;
    private EditText position_y_et;
    private Button record_bt;


    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope, gameRotationVector;
    private boolean start = false;
    private File folder;
    private File file;
    private FileOutputStream fos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
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
        file_et = findViewById(R.id.file_et);
        acc_tv = findViewById(R.id.acc_tv);
        gyro_tv = findViewById(R.id.gyro_tv);
        game_tv = findViewById(R.id.game_tv);
        wifi_tv.setMovementMethod(new ScrollingMovementMethod());
        position_x_et = findViewById(R.id.position_x_et);
        position_y_et = findViewById(R.id.position_y_et);
        record_bt = findViewById(R.id.record_bt);
        record_bt.setClickable(false);
    }


    public void checkFileName(View view){
        if(file_et.getText().toString().isEmpty()){
            Toast.makeText(this, "please enter the saving name", Toast.LENGTH_LONG).show();
        }else{
            String fileName = file_et.getText().toString();
            record_bt.setClickable(true);
            file = new File(folder, fileName);
            try {
                fos = new FileOutputStream(file, true);
            }catch (IOException e){
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
            start = true;
            Toast.makeText(this, "Now you can collect data", Toast.LENGTH_LONG).show();
        }
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
        long appTimestamp = System.currentTimeMillis();
        String position_x = position_x_et.getText().toString();
        String position_y = position_y_et.getText().toString();
        String show = "Position" + "," +  appTimestamp + "," + position_x + "," + position_y + "\n";
        for(ScanResult result: results){
            show += "WiFi" + "," +result.BSSID + "," + result.level + "\n";
        }
        wifi_tv.setText(show);
        try {
            fos.write(show.getBytes());
        }catch (IOException e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        String position_x = position_x_et.getText().toString();
        String position_y = position_y_et.getText().toString();
        long appTimestamp = System.currentTimeMillis();
        String show = "Position" + "," +  appTimestamp + "," + position_x + "," + position_y + "\n";
        for(ScanResult result: results){
            show += "WiFi" + "," +result.BSSID + "," + result.level + "\n";
        }
        wifi_tv.setText(show);

        try {
            fos.write(show.getBytes());
        }catch (IOException e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String record;
        long timestamp = event.timestamp;
        long appTimestamp = System.currentTimeMillis();
        if(!start){
            return;
        }
        else if(event.sensor == accelerometer){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            String show = String.format("x: %f, y:%f, z:%f", x, y, z);
            acc_tv.setText(show);
            record = String.format("ACC,%d,%d,%11f,%11f,%11f\n",appTimestamp,timestamp,x,y,z);

        }else if(event.sensor == gyroscope){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            String show = String.format("x: %f, y:%f, z:%f", x, y, z);
            gyro_tv.setText(show);
            record = String.format("GYRO,%d,%d,%11f,%11f,%11f\n",appTimestamp,timestamp,x,y,z);
        }else if(event.sensor == gameRotationVector){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float sum = 1 - (x*x + y*y + z*z);
            float w = sum > 0 ? (float)Math.sqrt(sum) : 0;
            String show = String.format("w: %f, x: %f, y:%f, z:%f", w, x, y, z);
            game_tv.setText(show);
            record = String.format("GRV,%d,%d,%11f,%11f,%11f,%11f\n",appTimestamp,timestamp,w,x,y,z);
        }else{
            Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            fos.write(record.getBytes());
        }catch (IOException e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            fos.close();
        }catch (IOException e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}