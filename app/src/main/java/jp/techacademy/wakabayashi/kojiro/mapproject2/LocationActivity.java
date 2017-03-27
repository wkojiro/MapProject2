package jp.techacademy.wakabayashi.kojiro.mapproject2;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class LocationActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // GoogleApiClient.ConnectionCallbacks をConnectionCallbacksだけにすると
    // べつのクラスが呼ばれてGoogleApiClient.Builderでエラーになる

    private TextView textView;
    private String textLog = "start \n";

    // LocationClient の代わりにGoogleApiClientを使います
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    private FusedLocationProviderApi fusedLocationProviderApi;

    private LocationRequest locationRequest;
    private Location location;
    private long lastLocationTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Log.d("LocationActivity", "onCreate");

        textView = (TextView) findViewById(R.id.text_view);

        // LocationRequest を生成して精度、インターバルを設定
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(16);

        fusedLocationProviderApi = LocationServices.FusedLocationApi;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Log.d("LocationActivity", "mGoogleApiClient");

        textLog += "onCreate() \n";
        textView.setText(textLog);

        // 測位開始
        Button buttonStart = (Button)findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFusedLocation();
            }
        });

        // 測位終了
        Button buttonStop = (Button)findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopFusedLocation();
            }
        });

    }


    private void startFusedLocation(){
        Log.d("LocationActivity", "onStart");

        // Connect the client.
        if (!mResolvingError) {
            // Connect the client.
            mGoogleApiClient.connect();

            textLog += "onStart(), connect() \n";
            textView.setText(textLog);
        } else {
            textLog += "onStart(), mResolvingError \n";
            textView.setText(textLog);
        }

    }

    private void stopFusedLocation(){
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        textLog += "onStop()\n";
        textView.setText(textLog);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopFusedLocation();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d("LocationActivity", "onConnected");

        textLog += "onConnected()\n";
        textView.setText(textLog);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location currentLocation = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);

        if (currentLocation != null && currentLocation.getTime() > 20000) {
            location = currentLocation;

            textLog += "---------- onConnected \n";
            textLog += "Latitude=" + String.valueOf(location.getLatitude()) + "\n";
            textLog += "Longitude=" + String.valueOf(location.getLongitude()) + "\n";
            textLog += "Accuracy=" + String.valueOf(location.getAccuracy()) + "\n";
            textLog += "Altitude=" + String.valueOf(location.getAltitude()) + "\n";
            textLog += "Time=" + String.valueOf(location.getTime()) + "\n";
            textLog += "Speed=" + String.valueOf(location.getSpeed()) + "\n";
            textLog += "Bearing=" + String.valueOf(location.getBearing()) + "\n";
            textView.setText(textLog);

            Log.d("debug", textLog);

        } else {
            // バックグラウンドから戻ってしまうと例外が発生する場合がある
            try {
                //
                fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
                // Schedule a Thread to unregister location listeners
                Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                    @Override
                    public void run() {
                        fusedLocationProviderApi.removeLocationUpdates(mGoogleApiClient, LocationActivity.this);
                    }
                }, 60000, TimeUnit.MILLISECONDS);

                textLog += "onConnected(), requestLocationUpdates \n";
                textView.setText(textLog);

            } catch (Exception e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(this, "例外が発生、位置情報のPermissionを許可していますか？", Toast.LENGTH_SHORT);
                toast.show();

                //MainActivityに戻す
                finish();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocationTime = location.getTime() - lastLocationTime;

        textLog += "---------- onLocationChanged \n";
        textLog += "Latitude=" + String.valueOf(location.getLatitude()) + "\n";
        textLog += "Longitude=" + String.valueOf(location.getLongitude()) + "\n";
        textLog += "Accuracy=" + String.valueOf(location.getAccuracy()) + "\n";
        textLog += "Altitude=" + String.valueOf(location.getAltitude()) + "\n";
        textLog += "Time=" + String.valueOf(location.getTime()) + "\n";
        textLog += "Speed=" + String.valueOf(location.getSpeed()) + "\n";
        textLog += "Bearing=" + String.valueOf(location.getBearing()) + "\n";
        textLog += "time= " + String.valueOf(lastLocationTime) + " msec \n";
        textView.setText(textLog);
    }

    @Override
    public void onConnectionSuspended(int i) {
        textLog += "onConnectionSuspended() \n";
        textView.setText(textLog);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        textLog += "onConnectionFailed()n";
        textView.setText(textLog);

        if (mResolvingError) {
            // Already attempting to resolve an error.
            Log.d("", "Already attempting to resolve an error");

            return;
        } else if (connectionResult.hasResolution()) {

        } else {
            mResolvingError = true;
        }
    }
}