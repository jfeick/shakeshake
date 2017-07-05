package de.uni_weimar.benike.shakeshake;

import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.net.MalformedURLException;
import java.net.URL;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;


public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FBInstanceIDService";

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // Send token to server and store locally
        PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                .edit().putString("TOKEN", refreshedToken).apply();

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        Backend.getInstance().registerToken(token);
    }
}
