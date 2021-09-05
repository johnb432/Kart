package ch.hevs.ss1;

import android.annotation.SuppressLint;
import android.content.Context;
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

// For manifest values, see https://developer.android.com/guide/topics/manifest/activity-element.html#screen
public class MyKartRemote extends AbstractKartControlActivity {
    private final int STEERING_POS_MAX = 1000;
    private final int STEERING_POS_MIDDLE = STEERING_POS_MAX / 2;

    private long oldTime = 0;
    private long newTime;

    private ProgressBar batteryLevelBar;
    private ProgressBar steeringBarLeft;
    private ProgressBar steeringBarRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_kart_remote);

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        final Switch throttleToggle = findViewById(R.id.throttleToggle);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        final Switch steeringToggle = findViewById(R.id.steeringToggle);

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        final Switch revertThrottle = findViewById(R.id.revertThrottle);
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        final Switch revertSteering = findViewById(R.id.revertSteering);

        final TextView speedDisplay = findViewById(R.id.speedDisplay);
        final TextView throttleDisplay = findViewById(R.id.throttleDisplay);

        final ProgressBar speedBarPos = findViewById(R.id.speedBarPos);
        final ProgressBar speedBarNeg = findViewById(R.id.speedBarNeg);

        batteryLevelBar = findViewById(R.id.batteryLevelBar);
        steeringBarLeft = findViewById(R.id.steeringBarLeft);
        steeringBarRight = findViewById(R.id.steeringBarRight);

        kart.addStatusRegisterListener(new KartStatusRegisterListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void statusRegisterHasChanged(Kart kart, KartStatusRegister kartStatusRegister, int i) {
                switch (kartStatusRegister.name()) {
                    case "HallSensorCounter1":
                        newTime = System.nanoTime();

                        double speed = 800000000.0 / (newTime - oldTime); // should be in m/s; with 8cm circumference

                        Log.d("statusRegisterHasChanged", "speedDisplay: " + speed);

                        speedDisplay.setText(String.format("%.2f", speed));
                        oldTime = newTime;

                        if (kart.getDriveSpeed() >= 0) {
                            speedBarPos.setProgress((int) speed, true);
                            speedBarNeg.setProgress(0, true);
                        } else {
                            speedBarPos.setProgress(0, true);
                            speedBarNeg.setProgress((int) speed, true);
                        }

                        break;
                    case "DistanceSensor":
                        // Distance in cm; see http://wiki.hevs.ch/fsi/index.php5/Kart/sensors/HCSR04
                        double distance = 0.0017 * i;

                        if (distance < 10) {

                        }

                        break;

                    default: {}
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

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (revertSteering.isChecked()) {
                    //kart.setSteeringPosition(STEERING_POS_MIDDLE);
                    seekBar.setProgress(STEERING_POS_MIDDLE, true);
                }
            }
        });

        final SeekBar throttleLevelSlider = findViewById(R.id.throttleLevelSlider);
        throttleLevelSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Code here executes on main thread after user moves slider
            @SuppressLint("DefaultLocale")
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                kart.setDriveSpeed(i);
                throttleDisplay.setText(String.format("%d", i));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (revertThrottle.isChecked()) {
                    //kart.setDriveSpeed(0);
                    //throttleDisplay.setText(String.format("%d", 0));
                    seekBar.setProgress(0, true);
                }
            }
        });

        final Button resetSteering = findViewById(R.id.resetSteering);
        resetSteering.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                kart.resetSteering();
                //kart.setSteeringPosition(STEERING_POS_MIDDLE);
                //steeringAngleSlider.setProgress(STEERING_POS_MIDDLE, true);
                Log.d("resetSteering", "Reset steering");
            }
        });

        // Button to open kart configuration
        final Button openSettings = findViewById(R.id.openSettings);
        openSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                showKartSetupPopup();
            }
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        // Switch to turn off all lights
        final Switch lightSwitch = findViewById(R.id.lightSwitch);
        lightSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                for (int i = 0; i < 4; i++) {
                    kart.toggleLed(i);
                }

                Log.d("lightSwitch", "Toggled lights 0-3");
            }
        });

        // Sets up listener for using phone position as input
        final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public final void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    Log.d("Accelerometer", "Sensor Changed value:" + event.values[0] + ":" + event.values[1] + ":" + event.values[2]);

                    if (throttleToggle.isChecked()) {
                        throttleLevelSlider.setProgress((int) (event.values[2] * 1.75), true);
                    }

                    if (steeringToggle.isChecked()) {
                        steeringAngleSlider.setProgress((int) (event.values[1]) * 30 + STEERING_POS_MIDDLE, true);
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        }, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onResume() {
        super.onResume();

        requestFullscreen();
    }

    protected void onPause() {
        super.onPause();

        //throttleLevelSlider.setProgress(0);
        //steeringAngleSlider.setProgress(0);
    }

    @Override
    public void steeringPositionChanged(Kart kart, int i, float v) {
        if (i <= STEERING_POS_MIDDLE) {
            steeringBarLeft.setProgress(STEERING_POS_MIDDLE - kart.getSteeringPosition(), true);
            steeringBarRight.setProgress(0, true);
        } else {
            steeringBarLeft.setProgress(0, true);
            steeringBarRight.setProgress(kart.getSteeringPosition() - STEERING_POS_MIDDLE, true);
        }
    }

    @Override
    public void steeringPositionReachedChanged(Kart kart, boolean b) {}

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