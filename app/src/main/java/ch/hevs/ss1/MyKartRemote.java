package ch.hevs.ss1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ch.hevs.kart.AbstractKartControlActivity;
import ch.hevs.kart.Kart;
import ch.hevs.kart.KartSetup;
import ch.hevs.kart.KartStatusRegisterListener;

public class MyKartRemote extends AbstractKartControlActivity {
    //public KartSetup kartSetup = new KartSetup();
    public int batteryLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_kart_remote);

        batteryLevel = (int) kart.getBatteryLevel();
        batteryLevel = 10;

        //kart.addStatusRegisterListener(KartStatusRegisterListener k);

        //kart.setup();
        //kart.

        final Button throttleToggle = findViewById(R.id.throttleToggle);

        throttleToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                System.out.println("testing my program");
            }
        });

        final Button steeringToggle = findViewById(R.id.steeringToggle);

        steeringToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                System.out.println("testing my program");
            }
        });
    }

    @Override
    public void steeringPositionChanged(Kart kart, int i, float v) {

    }

    @Override
    public void steeringPositionReachedChanged(Kart kart, boolean b) { }

    @Override
    public void batteryVoltageChanged(Kart kart, float v) {
        //batteryLevel = (int) (100 * v);
    }

    @Override
    public void sequenceCompleted(Kart kart) { }

    @Override
    public void connectionStatusChanged(Kart kart, boolean b) { }

    @Override
    public void message(Kart kart, String s) { }
}