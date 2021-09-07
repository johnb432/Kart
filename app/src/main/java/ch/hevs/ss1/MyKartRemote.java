package ch.hevs.ss1;

import java.lang.Math;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import ch.hevs.kart.AbstractKartControlActivity;
import ch.hevs.kart.Kart;
import ch.hevs.kart.KartStatusRegister;
import ch.hevs.kart.KartStatusRegisterListener;
import ch.hevs.kart.utils.Timer;

// For manifest values, see https://developer.android.com/guide/topics/manifest/activity-element.html#screen
@SuppressWarnings("ConstantConditions")
public class MyKartRemote extends AbstractKartControlActivity {
    //private long oldTime = 0;
    //private long newTime;

    private int hallCounter = 0;
    private final double RADIUS = 0.04; // 4cm
    private final double CIRCUMFERENCE_WHEELS = 2 * Math.PI * RADIUS;
    private final double COG_WHEEL_MULTIPLIER = 80.0 / 56;
    private final double LENGTH_ONE_TURN = CIRCUMFERENCE_WHEELS * COG_WHEEL_MULTIPLIER;
    private final int TIMER_INTERVAL = 2000;

    private final String[] checkboxes = {"Use Accelerometer for Steering", "Use Accelerometer for Throttle", "Revert Steering", "Revert Throttle"};
    private boolean[] checkedItems = {false, false, false, false};

    private ProgressBar batteryLevelBar;
    private ProgressBar steeringBarLeft;
    private ProgressBar steeringBarRight;

    private SeekBar throttleLevelSlider;
    private SeekBar steeringAngleSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_kart_remote);

        final TextView speedDisplay = findViewById(R.id.speedDisplay);
        final TextView throttleDisplay = findViewById(R.id.throttleDisplay);

        final ProgressBar speedBarPos = findViewById(R.id.speedBarPos);
        final ProgressBar speedBarNeg = findViewById(R.id.speedBarNeg);

        batteryLevelBar = findViewById(R.id.batteryLevelBar);
        steeringBarLeft = findViewById(R.id.steeringBarLeft);
        steeringBarRight = findViewById(R.id.steeringBarRight);

        Timer timer = new Timer() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTimeout() {
                double speed = LENGTH_ONE_TURN * hallCounter / TIMER_INTERVAL;

                //speedDisplay.setText(String.format("%.2f", speed));
                speedDisplay.setText(String.format("%d", hallCounter));

                if (kart.getDriveSpeed() >= 0) {
                    speedBarPos.setProgress((int) speed, true);
                    speedBarNeg.setProgress(0, true);
                } else {
                    speedBarPos.setProgress(0, true);
                    speedBarNeg.setProgress((int) speed, true);
                }

                hallCounter = 0;
            }
        };
        timer.schedulePeriodically(TIMER_INTERVAL);

        kart.addStatusRegisterListener(new KartStatusRegisterListener() {
            //@SuppressLint("DefaultLocale")
            @Override
            public void statusRegisterHasChanged(Kart kart, KartStatusRegister kartStatusRegister, int i) {
                switch (kartStatusRegister.name()) {
                    case "HallSensorCounter1":
                        hallCounter++;
                        /*
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
                        */

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

        steeringAngleSlider = findViewById(R.id.steeringAngleSlider);
        steeringAngleSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Code here executes on main thread after user moves slider
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                kart.setSteeringPosition(i);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (checkedItems[2]) {
                    //kart.setSteeringPosition(kart.setup().steeringMaxPosition());
                    seekBar.setProgress(kart.setup().steeringMaxPosition() / 2, true);
                }
            }
        });

        throttleLevelSlider = findViewById(R.id.throttleLevelSlider);
        throttleLevelSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Code here executes on main thread after user moves slider
            @SuppressLint("DefaultLocale")
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                kart.setDriveSpeed(i);
                throttleDisplay.setText(String.format("%d", i));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (checkedItems[3]) {
                    //kart.setDriveSpeed(0);
                    //throttleDisplay.setText(String.format("%d", 0));
                    seekBar.setProgress(0, true);
                }
            }
        });

        findViewById(R.id.resetSteering).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                steeringAngleSlider.setProgress(kart.setup().steeringMaxPosition() / 2, true);
                //kart.setSteeringPosition(kart.setup().steeringMaxPosition() / 2);
            }
        });

        // Button to open kart configuration
        findViewById(R.id.openKartSettings).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                steeringAngleSlider.setProgress(kart.setup().steeringMaxPosition() / 2, true);
                showKartSetupPopup();
            }
        });

        // Button to open kart configuration
        findViewById(R.id.openConfigureSettings).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                showPhoneSetupPopup();
            }
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        // Switch to turn off all lights
        final Switch lightSwitch = findViewById(R.id.lightSwitch);
        lightSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                for (int i = 0; i < 1; i++) {
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
                    if (checkedItems[1]) {
                        throttleLevelSlider.setProgress((int) (event.values[2] * 1.75), true);
                    }

                    if (checkedItems[0]) {
                        steeringAngleSlider.setProgress((int) (event.values[1]) * 30 + kart.setup().steeringMaxPosition() / 2, true);
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        }, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onResume() {
        super.onResume();

        requestFullscreen();
    }

    protected void onPause() {
        super.onPause();

        throttleLevelSlider.setProgress(0);
        steeringAngleSlider.setProgress(kart.setup().steeringMaxPosition() / 2);
    }

    @Override
    public void steeringPositionChanged(Kart kart, int i, float v) {
        steeringBarLeft.setMax(kart.setup().steeringMaxPosition() / 2);
        steeringBarRight.setMax(kart.setup().steeringMaxPosition() / 2);

        if (i <= kart.setup().steeringMaxPosition() / 2) {
            steeringBarLeft.setProgress(kart.setup().steeringMaxPosition() / 2 - kart.getSteeringPosition(), true);
            steeringBarRight.setProgress(0, true);
        } else {
            steeringBarLeft.setProgress(0, true);
            steeringBarRight.setProgress(kart.getSteeringPosition() - kart.setup().steeringMaxPosition() / 2, true);
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
            kart.resetSteering();
        } else {
            kart.setDriveSpeed(0);
        }
    }

    @Override
    public void message(Kart kart, String s) {}

    // https://stackoverflow.com/questions/15762905/how-can-i-display-a-list-view-in-an-android-alert-dialog
    protected void showPhoneSetupPopup() {
        // Setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Configure Phone");

        final boolean[] checkItemsTemp = checkedItems.clone();

        // Add a checkbox list
        builder.setMultiChoiceItems(checkboxes, checkItemsTemp, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // User checked or unchecked a box
                checkItemsTemp[which] = isChecked;
            }
        });

        // Add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedItems = checkItemsTemp.clone();
            }
        });
        builder.setNegativeButton("Cancel", null);

        // Create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}