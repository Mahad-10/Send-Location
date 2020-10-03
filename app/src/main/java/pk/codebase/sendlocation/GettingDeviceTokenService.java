package pk.codebase.sendlocation;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class GettingDeviceTokenService extends FirebaseInstanceIdService {
    public GettingDeviceTokenService() {
    }

    @Override
    public void onTokenRefresh() {
        String DeviceToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Device Token", DeviceToken);
    }

}
