package ch.hevs.ss1;

import java.lang.Math;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import ch.hevs.kart.AbstractKartControlActivity;
import ch.hevs.kart.Kart;
import ch.hevs.kart.KartHardwareSettings;
import ch.hevs.kart.KartStatusRegister;
import ch.hevs.kart.KartStatusRegisterListener;
import ch.hevs.kart.utils.Timer;

// For manifest values, see https://developer.android.com/guide/topics/manifest/activity-element.html#screen
public class MyKartRemote extends AbstractKartControlActivity {
    private int hallCounter = 0;
    private final double RADIUS = 0.04; // 4cm
    private final double CIRCUMFERENCE_WHEELS = 2 * Math.PI * RADIUS;
    private final double COG_WHEEL_MULTIPLIER = 80.0 / 56;
    private final double LENGTH_ONE_TURN = CIRCUMFERENCE_WHEELS * COG_WHEEL_MULTIPLIER;
    private final int TIMER_INTERVAL = 2000;

    private final String[] checkboxes = {"Use Accelerometer for Steering", "Use Accelerometer for Throttle", "Revert Steering", "Revert Throttle"};
    private boolean[] settings = {false, false, false, false};

    private boolean settingsOpened = false;

    private ProgressBar batteryLevelBar;
    private ProgressBar steeringBarLeft;
    private ProgressBar steeringBarRight;

    private SeekBar throttleLevelSlider;
    private SeekBar steeringAngleSlider;

    private TextView angleDisplay;
    private TextView distanceDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_kart_remote);

        final TextView speedDisplay = findViewById(R.id.speedDisplay);
        final TextView throttleDisplay = findViewById(R.id.throttleDisplay);
        final TextView steeringDisplay = findViewById(R.id.steeringDisplay);

        final ProgressBar speedBarPos = findViewById(R.id.speedBarPos);
        final ProgressBar speedBarNeg = findViewById(R.id.speedBarNeg);

        batteryLevelBar = findViewById(R.id.batteryLevelBar);
        steeringBarLeft = findViewById(R.id.steeringBarLeft);
        steeringBarRight = findViewById(R.id.steeringBarRight);

        angleDisplay = findViewById(R.id.angleDisplay);
        distanceDisplay = findViewById(R.id.distanceDisplay);

        Timer timer = new Timer() {
            @Override
            public void onTimeout() {
                double speed = LENGTH_ONE_TURN * hallCounter / 2 / TIMER_INTERVAL;

                speedDisplay.setText(String.format("%.2s", speed));

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
                        break;
                    case "DistanceSensor":
                        // Distance in cm; see http://wiki.hevs.ch/fsi/index.php5/Kart/sensors/HCSR04
                        distanceDisplay.setText(String.format("%.2s", 0.0017 * i));
                        break;
                    default: {}
                }
            }
        });

        steeringAngleSlider = findViewById(R.id.steeringAngleSlider);
        steeringAngleSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Update steering position and UI
                setSteeringPosition(kart, i);
                steeringDisplay.setText(String.format("%s", i));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {
                // If the revert option is enabled, put the thumb back to neutral
                if (settings[2]) {
                    seekBar.setProgress(getPositionCenter(kart), true);
                }
            }
        });

        steeringAngleSlider.setProgress(getPositionCenter(kart));
        steeringAngleSlider.setMax(kart.setup().steeringMaxPosition());
        steeringBarLeft.setMax(getPositionCenter(kart));
        steeringBarRight.setMax(getPositionCenter(kart));

        throttleLevelSlider = findViewById(R.id.throttleLevelSlider);
        throttleLevelSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Update throttle position and UI
                kart.setDriveSpeed(i);
                throttleDisplay.setText(String.format("%s", i));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {
                // If the revert option is enabled, put the thumb back to neutral
                if (settings[3]) {
                    seekBar.setProgress(0, true);
                }
            }
        });

        throttleLevelSlider.setMax(kart.setup().driveMaxSpeed());
        throttleLevelSlider.setMin(-1 * kart.setup().driveMaxSpeed());

        findViewById(R.id.recenterSteering).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Recenter the steering
                steeringAngleSlider.setProgress(getPositionCenter(kart), true);
            }
        });

        // Button to open kart configuration
        findViewById(R.id.openKartSettings).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Set the steering angle to neutral to avoid problems if a steering reset is wanted
                steeringAngleSlider.setProgress(getPositionCenter(kart), true);
                showKartSetupPopup();

                // This is used to detect when the popup closes
                settingsOpened = true;
            }
        });

        // Button to open kart configuration
        findViewById(R.id.openPhoneSettings).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPhoneSetupPopup();
            }
        });

        // Switch to turn off all lights
        findViewById(R.id.lightSwitch).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Turn on all LEDs that are hooked up
                for (int i = 0; i < 4; i++) {
                    kart.toggleLed(i);
                }
            }
        });

        // Sets up listener for using phone position as input
        final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public final void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    // Use sensor data for steering if options are enabled
                    if (settings[0]) {
                        // 30 * 300
                        steeringAngleSlider.setProgress((int) (event.values[1]) * 30 + getPositionCenter(kart), true);
                    }

                    if (settings[1]) {
                        throttleLevelSlider.setProgress((int) (event.values[2] * 1.75), true);
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
        steeringAngleSlider.setProgress(getPositionCenter(kart));
    }

    @Override
    public void steeringPositionChanged(Kart kart, int i, float v) {
        angleDisplay.setText(String.format("%s", i));

        int positionCenter = getPositionCenter(kart);

        if (i <= positionCenter) {
            if (!kart.setup().hardwareSettings().contains(KartHardwareSettings.InverseSteeringEndContactPosition)) {
                steeringBarLeft.setProgress(positionCenter - kart.getSteeringPosition(), true);
                steeringBarRight.setProgress(0, true);
            } else {
                steeringBarLeft.setProgress(0, true);
                steeringBarRight.setProgress(kart.getSteeringPosition() - positionCenter, true);
            }
        } else {
            if (!kart.setup().hardwareSettings().contains(KartHardwareSettings.InverseSteeringEndContactPosition)) {
                steeringBarLeft.setProgress(0, true);
                steeringBarRight.setProgress(kart.getSteeringPosition() - positionCenter, true);
            } else {
                steeringBarLeft.setProgress(positionCenter - kart.getSteeringPosition(), true);
                steeringBarRight.setProgress(0, true);
            }

        }
    }

    @Override
    public void steeringPositionReachedChanged(Kart kart, boolean b) {}

    @Override
    public void batteryVoltageChanged(Kart kart, float v) {
        batteryLevelBar.setProgress((int) (100 * v), true);
        //batteryLevelBar.setProgressTintList(batteryLevelBar.getForegroundTintList();
        //...batteryLevelBar.getSecondaryProgressTintList();
    }

    @Override
    public void sequenceCompleted(Kart kart) {}

    @Override
    public void connectionStatusChanged(Kart kart, boolean b) {
        if (b) {
            // Reset steering on established connection
            kart.resetSteering();
            Toast.makeText(getApplicationContext(), "Successfully connected", Toast.LENGTH_LONG).show();
        } else {
            // Turn off kart motor if connection is lost
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

        final boolean[] checkItems = settings.clone();

        // Add a checkbox list
        builder.setMultiChoiceItems(checkboxes, checkItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // Save the checked item into the list
                checkItems[which] = isChecked;
            }
        });

        // Add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Apply settings only after 'OK' has been pressed
                settings = checkItems.clone();
            }
        });
        builder.setNegativeButton("Cancel", null);

        // Create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && settingsOpened) {
            settingsOpened = false;

            // Set the max of progress bars and sliders
            steeringBarLeft.setMax(getPositionCenter(kart));
            steeringBarRight.setMax(getPositionCenter(kart));
            steeringAngleSlider.setMax(kart.setup().steeringMaxPosition());

            throttleLevelSlider.setMax(kart.setup().driveMaxSpeed());
        }
    }

    protected int getPositionCenter(Kart kart) {
        return kart.setup().steeringMaxPosition() / 2;
    }

    protected void setSteeringPosition (Kart kart, int i) {
        if (!kart.setup().hardwareSettings().contains(KartHardwareSettings.InverseSteeringEndContactPosition)) {
            kart.setSteeringPosition(i);
        } else {
            kart.setSteeringPosition(kart.setup().steeringMaxPosition() - i);
        }
    }
    /*
    protected void turnOnBlinker(final int i) {
        int leds = 0;

        switch (i) {
            case 0:
                leds = 1;
                break;
            case 1:
        }

        Timer timer = new Timer() {
            @Override
            public void onTimeout() {
                kart.toggleLed(mode);

                if (stop) {
                    kart.setLed(i, false);
                }
            }
        };
    }
    */
}