package de.uni_weimar.benike.shakeshake;

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
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {


        XMLRPCCallback listener = new XMLRPCCallback() {
            public void onResponse(long id, Object result) {
                Log.d(TAG, "received response");
            }
            public void onError(long id, XMLRPCException error) {
                Log.d(TAG, "received error");
            }
            public void onServerError(long id, XMLRPCServerException error) {
                Log.d(TAG, "received server error");
            }
        };

        try {
            URL url = new URL("http://192.168.2.102:8000/RPC2");
            XMLRPCClient client = new XMLRPCClient(url);
            long id = client.callAsync(listener, "register_token", token);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
