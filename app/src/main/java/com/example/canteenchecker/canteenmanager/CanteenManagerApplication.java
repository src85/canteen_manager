package com.example.canteenchecker.canteenmanager;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

public class CanteenManagerApplication extends Application {

    private static final String FIREBASE_MESSAGING_TOPIC = "canteens";
    private static CanteenManagerApplication instance;

    public static CanteenManagerApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        //subscribe to FCM topic to receive push message
        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().subscribeToTopic(FIREBASE_MESSAGING_TOPIC);
    }

    private String authenticationToken;
    private String canteenId;

    public synchronized String getAuthenticationToken() {
        return authenticationToken;
    }

    public synchronized void setAuthenticationToken(String authenticationToken) {
        if (!authenticationToken.isEmpty()) {
            this.authenticationToken = authenticationToken;
        }
    }

    public synchronized boolean isAuthenticated() {
        return (this.getAuthenticationToken() != null);
    }

    public synchronized boolean logOut() {
        this.authenticationToken = null;
        return (this.authenticationToken == null);
    }

    public synchronized String getCanteenId() { return canteenId; }
    public synchronized void setCanteenId(String canteenId){
        if(!canteenId.isEmpty()){
            this.canteenId = canteenId;
        }
    }

}
