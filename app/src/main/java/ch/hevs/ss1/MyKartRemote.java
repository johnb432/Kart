package ch.hevs.ss1;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import ch.hevs.kart.AbstractKartControlActivity;
import ch.hevs.kart.Kart;
import ch.hevs.kart.KartStatusRegister;
import ch.hevs.kart.KartStatusRegisterListener;

public class MyKartRemote extends AbstractKartControlActivity {
    //public KartSetup kartSetup = new KartSetup();
    public boolean lightsOn = false;

    private long oldTime = 0;
    private long newTime;

    private Switch throttleToggle;
    private Switch steeringToggle;

    public TextView speedDisplay;
    public TextView throttleDisplay;

    public ProgressBar batteryLevelBar;
    public ProgressBar steeringAngleBar;
    public ProgressBar speedBarPos;
    public ProgressBar speedBarNeg;

    public SensorManager SensorManager;

    final float alpha = 0.8f;
    float[] gravity = new float[3];
    float[] linear_acceleration = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_kart_remote);

        //showKartSetupPopup();

        //kart.setup();

        throttleToggle = findViewById(R.id.throttleToggle);
        steeringToggle = findViewById(R.id.steeringToggle);

        batteryLevelBar = findViewById(R.id.batteryLevelBar);
        steeringAngleBar = findViewById(R.id.steeringAngleBar);
        speedBarPos = findViewById(R.id.speedBarPos);
        speedBarNeg = findViewById(R.id.speedBarNeg);

        speedDisplay = findViewById(R.id.speedDisplay);
        throttleDisplay = findViewById(R.id.throttleDisplay);

        kart.addStatusRegisterListener(new KartStatusRegisterListener() {
            @Override
            public void statusRegisterHasChanged(Kart kart, KartStatusRegister kartStatusRegister, int i) {
                switch (kartStatusRegister.name()) {
                    case "HallSensorCounter1": {
                        Log.d("statusRegisterHasChanged", "speedDisplay: " + i);
                        newTime = System.nanoTime();

                        float speed = 0.08f / (newTime - oldTime) / 1000;
                        speed = (int) speed * 100;
                        speed /= 100;

                        speedDisplay.setText("" + speed);
                        oldTime = newTime;

                        if (i >= 0) {
                            speedBarPos.setProgress((int) speed);
                            speedBarNeg.setProgress(0);
                        } else {
                            speedBarPos.setProgress(0);
                            speedBarNeg.setProgress((int) speed);
                        }
                    }
                }
            }
        });

        final SeekBar steeringAngleSlider = findViewById(R.id.steeringAngleSlider);
        steeringAngleSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Code here executes on main thread after user moves slider
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                kart.setSteeringPosition(i);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        final SeekBar throttleLevelSlider = findViewById(R.id.throttleLevelSlider);
        throttleLevelSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Code here executes on main thread after user moves slider
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                kart.setDriveSpeed(i);
                throttleDisplay.setText("" + i);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        final Button resetSteering = findViewById(R.id.resetSteering);
        resetSteering.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                //kart.resetSteering();
                kart.setSteeringPosition(300);
                steeringAngleSlider.setProgress(300, true);
                Log.d("resetSteering", "Reset steering");
            }
        });

        final Button lightSwitch = findViewById(R.id.lightSwitch);
        lightSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                kart.toggleLed(0);
                Log.d("lightSwitch", "Toggled light 0");
            }
        });

        SensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        SensorManager.registerListener(new SensorEventListener() {
            @Override
            public final void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    Log.d("Accelerometer", "Sensor Changed value:" + event.values[0] + ":" + event.values[1] + ":" + event.values[2]);

                    if (throttleToggle.isChecked()) {
                        int speed = (int) (event.values[2] * 1.75);
                        throttleLevelSlider.setProgress(speed);
                        //kart.setDriveSpeed(speed);
                    }

                    if (steeringToggle.isChecked()) {
                        int angle = (int) (event.values[1]) * 30 + 300;
                        steeringAngleSlider.setProgress(angle);
                        //kart.setSteeringPosition(angle);
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        }, SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onResume() {
        super.onResume();

        //throttleAccelerometerOn = throttleToggle.isChecked();
        //steeringAccelerometerOn = steeringToggle.isChecked();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        requestFullscreen();
    }

    @Override
    public void steeringPositionChanged(Kart kart, int i, float v) {
        steeringAngleBar.setProgress(kart.getSteeringPosition(), true);
    }

    @Override
    public void steeringPositionReachedChanged(Kart kart, boolean b) { }

    @Override
    public void batteryVoltageChanged(Kart kart, float v) {
        batteryLevelBar.setProgress((int) (100 * v), true);
    }

    @Override
    public void sequenceCompleted(Kart kart) {}

    @Override
    public void connectionStatusChanged(Kart kart, boolean b) {
        if (b) {
            //kart.setup();
            kart.resetSteering();
        }
    }

    @Override
    public void message(Kart kart, String s) {}
}