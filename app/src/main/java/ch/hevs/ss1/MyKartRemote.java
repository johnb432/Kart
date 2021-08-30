package ch.hevs.ss1;

import android.hardware.SensorEvent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import ch.hevs.kart.AbstractKartControlActivity;
import ch.hevs.kart.Kart;
import ch.hevs.kart.KartStatusRegister;

public class MyKartRemote extends AbstractKartControlActivity {
    //public KartSetup kartSetup = new KartSetup();
    //public int batteryLevel = 0;
    public int throttleLevel = 0;
    public int steeringAngle = 0;
    public boolean lightsOn = false;

    public ProgressBar batteryLevelBar;
    public ProgressBar steeringAngleBar;

    //public ch.hevs.kart.KartStatusRegister KartStatusRegister = new KartStatusRegister();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_kart_remote);

        showKartSetupPopup();

        //batteryLevel = (int) kart.getBatteryLevel();
        //batteryLevel = 10;

        //kart.addStatusRegisterListener(KartStatusRegisterListener k);

        //kart.setup();
        //kart

        batteryLevelBar = findViewById(R.id.batteryLevelBar);
        steeringAngleBar = findViewById(R.id.steeringAngleBar);


        /*

        final TextView speedDisplay = findViewById(R.id.speedDisplay);
        speedDisplay.setText(kart.getDriveSpeed());


        final ProgressBar speedBarPos = findViewById(R.id.speedBarPos);
        speedBarPos.setProgress(kart.getDriveSpeed(), true);

         */

        final Button resetSteering = findViewById(R.id.resetSteering);
        resetSteering.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                kart.resetSteering();
                System.out.println("Reset steering");
            }
        });

        final Button throttleToggle = findViewById(R.id.throttleToggle);
        throttleToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                System.out.println("Toggled throttle control");
            }
        });

        final Button steeringToggle = findViewById(R.id.steeringToggle);
        steeringToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                System.out.println("Toggled steering control");
            }
        });

        final Button lightSwitch = findViewById(R.id.lightSwitch);
        lightSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                kart.toggleLed(0);
                System.out.println("Toggled light 0");
            }
        });

        final SeekBar steeringAngleSlider = findViewById(R.id.steeringAngleSlider);
        steeringAngleSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Code here executes on main thread after user moves slider
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    steeringAngle = i;
                    kart.setSteeringPosition(i);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        final SeekBar throttleLevelSlider = findViewById(R.id.throttleLevelSlider);
        throttleLevelSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Code here executes on main thread after user moves slider
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    throttleLevel = i;

                    //if (kart.getDriveSpeed() >= i) {
                        kart.setDriveSpeed(i);
                        /*
                    } else {
                        kart.decreaseDriveSpeed();
                    }
                    */
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    protected void onResume() {
        super.onResume();

        //kart.addStatusRegisterListener();
    }

    void statusRegisterHasChanged(Kart kart, KartStatusRegister register, int newValue) {
        switch (register.name()) {
            case HallSensorCounter1:

                break;
            //case 1
        }
    }

    @Override
    public void steeringPositionChanged(Kart kart, int i, float v) {
        steeringAngleBar.setProgress(i, true);
    }

    @Override
    public void steeringPositionReachedChanged(Kart kart, boolean b) { }

    @Override
    public void batteryVoltageChanged(Kart kart, float v) {
        batteryLevelBar.setProgress((int) (100 * v), true);
    }

    @Override
    public void sequenceCompleted(Kart kart) { }

    @Override
    public void connectionStatusChanged(Kart kart, boolean b) {
        if (b) {
            //kart.setup();
            kart.resetSteering();
        }
    }

    @Override
    public void message(Kart kart, String s) { }

    /*
    public void onSensorChanged(SensorEvent event)  {
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        final float alpha = 0.8f;
        float[] gravity = new float[3];
        float[] linear_acceleration = new float[3];

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];
    }
    */
}