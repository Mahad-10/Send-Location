package pk.codebase.sendlocation;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Button;

import androidx.core.app.NotificationCompat;

import static pk.codebase.sendlocation.App.CHANNEL_ID;


public class LocationService extends Service
{
    public LocationService() {
    }



    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,
                notificationIntent,0);
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Location Track")
                .setContentText("Service is running in foreground")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1,notification);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null;}
}
