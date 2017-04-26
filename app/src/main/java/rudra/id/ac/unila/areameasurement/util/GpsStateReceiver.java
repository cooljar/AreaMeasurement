package rudra.id.ac.unila.areameasurement.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import rudra.id.ac.unila.areameasurement.AppController;

public class GpsStateReceiver extends BroadcastReceiver {
    public static GpsStateReceiverListner gpsStateReceiverListner;

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
        boolean isGpsOn = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(gpsStateReceiverListner != null){
            gpsStateReceiverListner.onGpsStateChanged(isGpsOn);
        }
    }

    public static boolean isGpsOn(){
        LocationManager manager = (LocationManager) AppController.getInstance().getApplicationContext().
                getSystemService( Context.LOCATION_SERVICE );

        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public interface GpsStateReceiverListner {
        void onGpsStateChanged(boolean isGpsOn);
    }
}
