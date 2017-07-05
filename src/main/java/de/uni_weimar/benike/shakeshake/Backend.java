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
    private static final String backendURL = "http://192.168.2.102:8000/RPC2";
    private final XMLRPCClient client;

    public static Backend getInstance() {
        return ourInstance;
    }

    private Backend() {
        client = null;
        listener = new XMLRPCCallback() {
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
            URL url = new URL(backendURL);
            client = new XMLRPCClient(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void registerToken(String token) {
        client.callAsync(listener, "register_token", token);
    }
}
