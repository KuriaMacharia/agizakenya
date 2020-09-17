package com.center.emergency;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.center.emergency.Helper.HttpJsonParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;

public class Emergency extends LinearLayout
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String KEY_DATA = "data";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_ORIGIN_COORDINATES = "origin_matrix";
    static final String KEY_PROVIDER_NUMBER = "phone";
    static final String KEY_PROVIDER_NAME = "name";
    static final String KEY_PROVIDER_LATITUDE = "latitude";
    static final String KEY_PROVIDER_LONGITUDE = "longitude";
    static final String KEY_PROVIDER_DURATION = "duration";
    static final String KEY_PROVIDER_DISTANCE = "distance";

    static final String KEY_STAT_LATITUDE = "latitude";
    static final String KEY_STAT_LONGITUDE = "longitude";
    static final String KEY_PACKAGE = "package";
    private static final String BASE_FINAL_LIST = "http://www.agizakenya.com/agiza/final_emergency_list.php";

    LocationManager locationManager;
    boolean GpsStatus = false;
    int success, counter;
    String Latitude, Longitude, myLocation, pLat, pLong, pPhone, aLat, aLong, aPhone, fLat, fLong, fPhone, theSha1, thePackage;
    private Location mylocation;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    TextView policeTxt, ambulanceTxt, fireTxt, distancePolTxt, distanceAmbulanceTxt,
                distanceFireTxt, waitTxt;
    ConstraintLayout consPoliceCall, consPoliceVisit, consAmbulanceCall, consAmbulanceVisit, consFireCall, consFireVisit, consResult;
    private AlertDialog alertDialog;
    private ArrayList<HashMap<String, String>> distanceList;
    PackageInfo info;

    public Emergency(Context context) {
        super(context);
        initialize(context);
    }

    public Emergency(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }
    private void initialize(Context context){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.emergency, null);
        builder.setView(view);
        builder.setCancelable(true);
        alertDialog = builder.create();
        counter=0;

        CheckGpsStatus();
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        distanceList= new ArrayList<>();
        policeTxt=(TextView) view.findViewById(R.id.txt_police_station);
        ambulanceTxt=(TextView) view.findViewById(R.id.txt_ambulance);
        fireTxt=(TextView) view.findViewById(R.id.txt_fire_station);
        distancePolTxt=(TextView) view.findViewById(R.id.txt_police_distance);
        distanceAmbulanceTxt=(TextView) view.findViewById(R.id.txt_ambulance_distance);
        distanceFireTxt=(TextView) view.findViewById(R.id.txt_fire_distance);
        consPoliceCall=(ConstraintLayout) view.findViewById(R.id.cons_call_police);
        consPoliceVisit=(ConstraintLayout) view.findViewById(R.id.cons_visit_police);
        consAmbulanceCall=(ConstraintLayout) view.findViewById(R.id.cons_call_ambulance);
        consAmbulanceVisit=(ConstraintLayout) view.findViewById(R.id.cons_visit_ambulance);
        consFireCall=(ConstraintLayout) view.findViewById(R.id.cons_call_fire);
        consFireVisit=(ConstraintLayout) view.findViewById(R.id.cons_visit_fire);
        consResult=(ConstraintLayout) view.findViewById(R.id.cons_emergency_result);
        waitTxt=(TextView) view.findViewById(R.id.txt_wait_message);
        FetchPackageInfo();

        consPoliceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_CALL);
                intent1.setData(Uri.parse("tel:" + pPhone));
                getContext().startActivity (intent1);

            }
        });

        consPoliceVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(
                        Uri.parse("https://www.google.com/maps?saddr="+ Latitude +"," + Longitude + "&daddr=" +
                                (pLat + "," + pLong)));
                getContext().startActivity(intent);
            }
        });

        consAmbulanceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_CALL);
                intent1.setData(Uri.parse("tel:" + aPhone));
                getContext().startActivity (intent1);
            }
        });

        consAmbulanceVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(
                        Uri.parse("https://www.google.com/maps?saddr="+ Latitude +"," + Longitude + "&daddr=" +
                                (aLat + "," + aLong)));
                getContext().startActivity(intent);

            }
        });

        consFireCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_CALL);
                intent1.setData(Uri.parse("tel:" + fPhone));
                getContext().startActivity (intent1);

            }
        });

        consFireVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(
                        Uri.parse("https://www.google.com/maps?saddr="+ Latitude +"," + Longitude + "&daddr=" +
                                (fLat + "," + fLong)));
                getContext().startActivity(intent);

            }
        });
    }

    private void FetchPackageInfo(){
        ActivityManager am = (ActivityManager) getContext().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        thePackage = componentInfo.getPackageName();
    }

    public void show(){
        alertDialog.show();
        setUpGClient();
    }
    public void hide(){
        alertDialog.hide();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
        getMyLocation();
    }

    @Override
    public void onLocationChanged(Location location) {
        mylocation = location;
        counter++;
        Latitude=String.valueOf(mylocation.getLatitude());
        Longitude=String.valueOf(mylocation.getLongitude());

        if(counter==3){
            myLocation=" " + Latitude + ", " + Longitude;
            new PopulateEmergency().execute();
            waitTxt.setText("Loading... (80%)");
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void getMyLocation(){
        if(googleApiClient!=null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    locationRequest = new LocationRequest();
                    locationRequest.setInterval(20);
                    locationRequest.setFastestInterval(20);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                    PendingResult<LocationSettingsResult> result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    int permissionLocation = ContextCompat
                                            .checkSelfPermission(getContext(),
                                                    Manifest.permission.ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                        mylocation = LocationServices.FusedLocationApi
                                                .getLastLocation(googleApiClient);
                                    }
                                    break;

                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    break;
                            }
                        }
                    });
                }
            }
        }
    }

    private void CheckGpsStatus() {
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private class PopulateEmergency extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            waitTxt.setText("Loading... (85%)");
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_ORIGIN_COORDINATES, myLocation);
            httpParams.put(KEY_PACKAGE, thePackage);
            httpParams.put(KEY_STAT_LATITUDE, Latitude);
            httpParams.put(KEY_STAT_LONGITUDE, Longitude);

            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_FINAL_LIST, "POST", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);
                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);
                        String Name = incidence.getString(KEY_PROVIDER_NAME);
                        String Phone = incidence.getString(KEY_PROVIDER_NUMBER);
                        String Latitude = incidence.getString(KEY_PROVIDER_LATITUDE);
                        String Longitude = incidence.getString(KEY_PROVIDER_LONGITUDE);
                        String Distance = incidence.getString(KEY_PROVIDER_DISTANCE);
                        String Duration = incidence.getString(KEY_PROVIDER_DURATION);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_PROVIDER_NAME, Name);
                        map.put(KEY_PROVIDER_NUMBER, Phone);
                        map.put(KEY_PROVIDER_LATITUDE, Latitude);
                        map.put(KEY_PROVIDER_LONGITUDE, Longitude);
                        map.put(KEY_PROVIDER_DISTANCE, Distance);
                        map.put(KEY_PROVIDER_DURATION, Duration);
                        distanceList.add(map);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            if(distanceList.size()>0) {
                waitTxt.setText("Loading... (95%). Almost done!");
                policeTxt.setText(distanceList.get(1).get(KEY_PROVIDER_NAME));
                ambulanceTxt.setText(distanceList.get(0).get(KEY_PROVIDER_NAME));
                fireTxt.setText(distanceList.get(2).get(KEY_PROVIDER_NAME));

                distancePolTxt.setText(distanceList.get(1).get(KEY_PROVIDER_DISTANCE)+ "  |  " +
                        distanceList.get(1).get(KEY_PROVIDER_DURATION));
                distanceAmbulanceTxt.setText(distanceList.get(0).get(KEY_PROVIDER_DISTANCE) + "  |  " +
                        distanceList.get(0).get(KEY_PROVIDER_DURATION));
                distanceFireTxt.setText(distanceList.get(2).get(KEY_PROVIDER_DISTANCE) + "  |  "+
                        distanceList.get(2).get(KEY_PROVIDER_DURATION));

                pLat = distanceList.get(1).get(KEY_PROVIDER_LATITUDE);
                pLong = distanceList.get(1).get(KEY_PROVIDER_LONGITUDE);
                pPhone = distanceList.get(1).get(KEY_PROVIDER_NUMBER);

                aLat = distanceList.get(0).get(KEY_PROVIDER_LATITUDE);
                aLong = distanceList.get(0).get(KEY_PROVIDER_LONGITUDE);
                aPhone = distanceList.get(0).get(KEY_PROVIDER_NUMBER);

                fLat = distanceList.get(2).get(KEY_PROVIDER_LATITUDE);
                fLong = distanceList.get(2).get(KEY_PROVIDER_LONGITUDE);
                fPhone = distanceList.get(2).get(KEY_PROVIDER_NUMBER);

                waitTxt.setVisibility(View.GONE);
                consResult.setVisibility(View.VISIBLE);
            }else{

                waitTxt.setText("Error Loading! Contact support team.");

            }

        }
    }

}

