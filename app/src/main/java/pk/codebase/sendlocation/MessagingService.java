package pk.codebase.sendlocation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import pk.codebase.requests.HttpError;
import pk.codebase.requests.HttpRequest;
import pk.codebase.requests.HttpResponse;

import static pk.codebase.sendlocation.App.CHANNEL_ID;

public class MessagingService extends FirebaseMessagingService {
    public MessagingService() {
    }
    public static int NOTIFICATION_ID = 1;
    public boolean GPS_status = false;
    public boolean network_status = false;
    private LocationManager locationManager;


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        startService();
        generateNotification(remoteMessage.getNotification().getBody(),remoteMessage.getNotification().getTitle());
        checkPermissions();
    }



    private void generateNotification(String body, String title) {
        Intent intent = new Intent(MessagingService.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
    private void checkPermissions(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            GPS_status = true;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            network_status = true;
        }
        HttpRequest request = new HttpRequest();
        request.setOnResponseListener(new HttpRequest.OnResponseListener() {
            @Override
            public void onResponse(HttpResponse response) {
                if (response.code == HttpResponse.HTTP_OK) {
                    Toast.makeText(MessagingService.this, "Success in getting the GPS condition", Toast.LENGTH_SHORT).show();
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpError error) {
                Toast.makeText(MessagingService.this, "Error in getting the GPS condition", Toast.LENGTH_SHORT).show();
            }
        });
        JSONObject json;
        try {
            json = new JSONObject();
            json.put("status", GPS_status + "," + network_status);

        } catch (JSONException ignore) {
            return;
        }
        request.post("http://codebase.pk:7000/api/status/", json);
    }
    private void startService(){
        Intent serviceIntent = new Intent(getApplicationContext(), LocationService.class);
        startService(serviceIntent);
    }
}
