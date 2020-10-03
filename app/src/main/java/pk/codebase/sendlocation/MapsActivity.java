package pk.codebase.sendlocation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;

import pk.codebase.requests.HttpError;
import pk.codebase.requests.HttpRequest;
import pk.codebase.requests.HttpResponse;

public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    public GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 5000;
    private long FASTEST_INTERVAL = 8000;
    private LocationManager locationManager;
    private LatLng latLng;
    private boolean isPermission;
    public double latitude;
    public double longitude;
    public String latLong;
    TextView textView1;
    TextView textView2;
    Button btnStop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        textView1 = findViewById(R.id.text_view1);
        textView2 = findViewById(R.id.text_view2);
        Intent serviceIntent = new Intent(MapsActivity.this,LocationService.class);
        startService(serviceIntent);

        if (requestSinglePermission()) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            FirebaseMessaging.getInstance().subscribeToTopic("Location")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()){
                                Toast.makeText(MapsActivity.this, "Subscription Failed", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(MapsActivity.this, "Subscription Success", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            checkLocation();
        }
        btnStop = findViewById(R.id.stop_btn);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(MapsActivity.this,LocationService.class);
                stopService(serviceIntent);
            }
        });

    }
    private boolean checkLocation() {
        if (!isLocationEnabled()) {
            showAlert();
        }
        return isLocationEnabled();
    }
    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location ")
                .setMessage("Your Location is Turned 'OFF'. Please Enable location to use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    private boolean requestSinglePermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        isPermission = true;
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        isPermission = false;
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                    }
                }).check();
        return isPermission;
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startLocationUpdated();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation == null) {
            startLocationUpdated();
        }
    }


    public  void startLocationUpdated() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .setSmallestDisplacement(1);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        textView1.setText("Latitude : " + latitude);
        textView2.setText("Longitude : " + longitude);
        latLong = String.valueOf(latitude) + "," + String.valueOf(longitude);
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + " , " +
                Double.toString((location.getLongitude()));
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        HttpRequest request = new HttpRequest();
        request.setOnResponseListener(new HttpRequest.OnResponseListener() {
            @Override
            public void onResponse(HttpResponse response) {
                if (response.code == HttpResponse.HTTP_OK) {
                    Toast.makeText(MapsActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    System.out.println( "Response : Successful");
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpError error) {
                Toast.makeText(MapsActivity.this, "Error in internet", Toast.LENGTH_SHORT).show();
                System.out.println("Response : Error");
            }
        });

        JSONObject json;
        try {
            json = new JSONObject();
            json.put("latlong", latLong);
        } catch (JSONException ignore) {
            return;
        }
        request.post("http://codebase.pk:7000/api/location", json);
   }
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        textView1.setText("");
        textView2.setText("");
    }
}