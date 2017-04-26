package rudra.id.ac.unila.areameasurement;

import android.app.Application;
import android.content.res.Configuration;

import rudra.id.ac.unila.areameasurement.util.ConnectivityReceiver;
import rudra.id.ac.unila.areameasurement.util.GpsStateReceiver;

/**
 * Created by japra_awok on 12/11/2016.
 */

public class AppController extends Application {
    private static AppController mInstance;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    public void setGpsStateListener(GpsStateReceiver.GpsStateReceiverListner listener) {
        GpsStateReceiver.gpsStateReceiverListner = listener;
    }
}
