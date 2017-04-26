package rudra.id.ac.unila.areameasurement.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import rudra.id.ac.unila.areameasurement.R;
import rudra.id.ac.unila.areameasurement.util.Common;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private Common commonClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        commonClass = new Common(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Common.MY_PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    SplashActivity.this.finish();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");

        if(commonClass.checkPlayServices()){
            if(commonClass.checkInternetConnection()){
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(commonClass.checkLocationPermission()){
                        resumeApplication();
                    }
                }else{
                    resumeApplication();
                }
            }
        }
    }

    private void resumeApplication(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
