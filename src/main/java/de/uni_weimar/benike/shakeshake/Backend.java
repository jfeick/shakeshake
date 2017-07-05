package de.uni_weimar.benike.shakeshake;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

import static android.content.ContentValues.TAG;

public class Backend {
    private static final Backend ourInstance = new Backend();
    private final XMLRPCCallback listener;
    private static final String backendURL = "http://m18.uni-weimar.de:8000/RPC2";
    private XMLRPCClient client = null;

    public static Backend getInstance() {
        return ourInstance;
    }

    private Backend() {
        listener = new XMLRPCCallback() {
            public void onResponse(long id, Object result) {
                Log.d(TAG, "received response" + result);
            }
            public void onError(long id, XMLRPCException error) {
                Log.d(TAG, "received error: " + error);
            }
            public void onServerError(long id, XMLRPCServerException error) {
                Log.d(TAG, "received server error: " + error);
            }
        };
        try {
            URL url = new URL(backendURL);
            client = new XMLRPCClient(url, XMLRPCClient.FLAGS_8BYTE_INT);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void registerToken(String token) {
        client.callAsync(listener, "register_token", token);
    }

    public void registerActivity(String token, long timestamp, double lat, double lon) {
        client.callAsync(listener, "register_activity", token, timestamp, lat, lon);
    }
}
