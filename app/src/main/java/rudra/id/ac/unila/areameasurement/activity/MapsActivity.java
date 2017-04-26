package rudra.id.ac.unila.areameasurement.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rudra.id.ac.unila.areameasurement.R;
import rudra.id.ac.unila.areameasurement.fragment.ResultFragment;
import rudra.id.ac.unila.areameasurement.util.Common;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener {

    private static final String TAG = "MapsActivity";
    private static final long POLLING_FREQ = 1000 * 30;
    private static final long FASTEST_UPDATE_FREQ = 1000 * 5;

    private Common commonClass;
    private GoogleMap mMap;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Polygon myPolygon;
    private LinkedHashMap<String, Marker> areaMarkers;
    private List<LatLng> latlngPath;
    private ArrayList<String> pointDistances;
    private boolean
            locationRequestIsRun = false,
            mapWasReady = false,
            googleApiClientIsConnected = false,
            mapWasSet = false,
            setupMarkersDone = false;
    private char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private int alphabetIndex = 0;

    @BindView(R.id.btHitung) Button btHitung;
    @BindView(R.id.rgMapType) RadioGroup rgMapType;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.tvProgressStatus) TextView tvProgressStatus;
    @BindView(R.id.ibReset) ImageButton ibReset;
    @BindView(R.id.ibDone) ImageButton ibDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

        areaMarkers = new LinkedHashMap<String, Marker>();
        latlngPath = new ArrayList<LatLng>();
        pointDistances = new ArrayList<String>();

        rgMapType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if(mapWasSet){
                    if(checkedId == R.id.rb_normal){
                        if(mMap != null && mMap.getMapType() != GoogleMap.MAP_TYPE_NORMAL) mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    }else if(checkedId == R.id.rb_satellite){
                        if(mMap != null && mMap.getMapType() != GoogleMap.MAP_TYPE_SATELLITE) mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    }
                }else{
                    Toast.makeText(MapsActivity.this, "Map belum siap", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btHitung.setEnabled(false);
        btHitung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double luasArea = SphericalUtil.computeArea(latlngPath);
                NumberFormat numFmt = new DecimalFormat("#,###.00");
                String luasAreaString = numFmt.format(luasArea) + " m2";

                Log.e(TAG, "Hitung - Luas Area: " + luasArea + " m2");
                for(String dst : pointDistances){
                    Log.e(TAG, dst);
                }

                FragmentManager fm = getSupportFragmentManager();
                ResultFragment fragment = ResultFragment.newInstance(luasAreaString, pointDistances);
                fragment.show(fm, "FRAGMENT_RESULT");
                btHitung.setEnabled(false);
            }
        });

        ibReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMap != null){
                    mMap.clear();
                    areaMarkers.clear();
                    alphabetIndex = 0;
                    setupMarkersDone = false;
                    btHitung.setEnabled(false);
                    ibDone.setEnabled(false);
                }
            }
        });

        ibDone.setEnabled(false);
        ibDone.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(myPolygon != null){
                    myPolygon.remove();
                }
                setPolygon();
                setupMarkersDone = true;
                btHitung.setEnabled(true);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        commonClass = new Common(this);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(POLLING_FREQ);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_FREQ);

        buildGoogleApiClient();
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
                    MapsActivity.this.finish();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            commonClass.checkLocationPermission();
        }

        if(commonClass.checkInternetConnection()){
            if(commonClass.checkGpsActive()){
                if(googleApiClientIsConnected){
                    if(!mapWasSet){
                        startLocationUpdates();
                    }
                }
            }
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        if(locationRequestIsRun){
            removeLocationUpdates();
        }

        super.onPause();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e(TAG, "onMapReady");
        mMap = googleMap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);

        mapWasReady = true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "onConnected");
        googleApiClientIsConnected = true;
        startLocationUpdates();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.e(TAG, "onMapClick " + areaMarkers);
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setColor(Color.GREEN);

        if(!setupMarkersDone){
            String markerTitle = String.valueOf(alphabet[alphabetIndex]);
            MarkerOptions markerOption = new MarkerOptions();
            markerOption.position(latLng)
            .draggable(true);
            markerOption.title(markerTitle);
            markerOption.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(markerTitle)));
            Marker mkr = mMap.addMarker(markerOption);
            areaMarkers.put(mkr.getId(), mkr);
            alphabetIndex++;

            if(areaMarkers.size() > 2){
                ibDone.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(TAG, "onMarkerClick");
        /*if(!onCalculateResult){
            areaMarkers.remove(marker.getId());
            marker.remove();
            return true;
        }else{
            return false;
        }*/
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.e(TAG, "onMarkerDragEnd");
        if(areaMarkers.size() > 0){
            areaMarkers.put(marker.getId(), marker);
            Log.e(TAG, "onMarkerDragEnd = areaMarkers Size = " + areaMarkers.size());
        }
        btHitung.setEnabled(false);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged - Lat:" + String.valueOf(location.getLatitude()) + "Lng:"+ String.valueOf(location.getLongitude()));
        mLastLocation = location;
        if(mapWasReady){
            if(!mapWasSet){
                setupMap();
            }
        }
    }

    private void setupMap(){
        Log.e(TAG, "setupMap");

        LatLng personLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(personLocation, 17);
        mMap.animateCamera(cameraUpdate,new GoogleMap.CancelableCallback(){
            @Override
            public void onFinish() {
                mapWasSet = true;
                removeLocationUpdates();
                progressBar.setIndeterminate(false);
                tvProgressStatus.setText("Ready");
            }

            @Override
            public void onCancel() {
                MapsActivity.this.finish();
            }
        });


    }

    private void setPolygon(){
        Log.e(TAG, "setPolygon");
        if(areaMarkers.size() > 2){
            latlngPath.clear();
            pointDistances.clear();

            Marker beforeMarker = null, firstMarker = null;
            Iterator it = areaMarkers.entrySet().iterator();
            while (it.hasNext()) {
                NumberFormat numFmt = new DecimalFormat("#,###.00");

                Map.Entry pairs = (Map.Entry) it.next();
                Marker mMarker = (Marker) pairs.getValue();
                LatLng currentPosition = mMarker.getPosition();
                latlngPath.add(currentPosition);

                Log.e(TAG, "setPolygon - " + mMarker.getTitle());
                if(beforeMarker != null){
                    double jarakTitik = SphericalUtil.computeDistanceBetween(beforeMarker.getPosition(), currentPosition);
                    String distanceInfo = "Jarak titik " + beforeMarker.getTitle() +
                            " Ke " + mMarker.getTitle() + " = " +
                            numFmt.format(jarakTitik) +
                            " m";
                    pointDistances.add(distanceInfo);
                    Log.e(TAG, "setPolygon Before - " + beforeMarker.getTitle());
                }else{
                    firstMarker = mMarker;
                    Log.e(TAG, "setPolygon First - " + firstMarker.getTitle());
                }

                if(!it.hasNext()){
                    double jarakTitik = SphericalUtil.computeDistanceBetween(currentPosition, firstMarker.getPosition());
                    String distanceInfo = "Jarak titik " +
                            mMarker.getTitle() + " Ke " +
                            firstMarker.getTitle() + " = " +
                            numFmt.format(jarakTitik) +
                            " m";
                    pointDistances.add(distanceInfo);
                }
                beforeMarker = mMarker;
            }

            PolygonOptions plo = new PolygonOptions()
                    .addAll(latlngPath)
                    .strokeColor(Color.RED)
                    .fillColor(Color.BLUE);

            myPolygon = mMap.addPolygon(plo);
        }else{
            String notifText = "";
            if(areaMarkers.size() == 0){
                notifText = "Anda belum menandai area yang akan dihitung..!";
            }else notifText = "Tandai minimal 3 kordinat..!";
            Toast.makeText(MapsActivity.this, notifText, Toast.LENGTH_SHORT).show();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.e(TAG, "buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void startLocationUpdates() {
        Log.e(TAG, "startLocationUpdates");
        if(!locationRequestIsRun){
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                }
            }else{
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }

            locationRequestIsRun = true;
        }
    }

    private void removeLocationUpdates() {
        Log.e(TAG, "removeLocationUpdates");
        if(locationRequestIsRun){
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapsActivity.this);
                }
            }else{
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapsActivity.this);
            }

            locationRequestIsRun = false;
        }
    }
}
