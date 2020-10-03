package pk.codebase.sendlocation;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import java.io.PipedInputStream;

public class CheckConnectivity extends BroadcastReceiver {

    public CheckConnectivity() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        boolean isConnected = wifi != null && wifi.isConnectedOrConnecting() ||
                mobile != null && mobile.isConnectedOrConnecting();
        if (isConnected) {
            if (new MapsActivity().mGoogleApiClient !=null){
                new MapsActivity().mGoogleApiClient.connect();
            }


            } else {
            if (new MapsActivity().mGoogleApiClient.isConnected()) {
                new MapsActivity().mGoogleApiClient.disconnect();
            }
        }


    }

}
