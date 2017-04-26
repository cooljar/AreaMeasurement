package rudra.id.ac.unila.areameasurement.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by japra_awok on 12/11/2016.
 */

public class Common {
    private Context context;
    Activity activity;
    private AlertDialog.Builder builder;

    public Common(Context context) {
        this.context = context;
        this.activity = (Activity) context;
        builder = new AlertDialog.Builder(this.context);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }else return true;
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public boolean checkGpsActive(){
        boolean isGpsOn = GpsStateReceiver.isGpsOn();
        if (!isGpsOn) {
            /*
            * Memeriksa apakah GPS aktif
            * */
            // Call your Alert message
            builder.setCancelable(false);
            builder.setTitle("GPS tidak aktif");
            builder.setMessage("Masuk ke pengaturan untuk mengaktifkan GPS.");
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            activity.startActivity(intent);
                        }
                    });
            builder.setNegativeButton("Batal",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            activity.finish();
                        }
                    }
            );
            builder.show();
            return false;
        }else return true;
    }

    public boolean checkInternetConnection(){
        boolean isConnected = ConnectivityReceiver.isConnected();
        if(isConnected){
            return true;
        }else{
            builder.setCancelable(false);
            builder.setTitle("Notifikasi");
            builder.setMessage("Anda tidak terhubung dengan internet, aktifkan data seluler atau wifi. Aplikasi akan ditutup.");
            builder.setPositiveButton("Tutup Aplikasi",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                            dialog.dismiss();
                            activity.finish();
                        }
                    });
            builder.show();
            return false;
        }
    }
}