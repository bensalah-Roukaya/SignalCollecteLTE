package com.example.safsouf.signalcollecte;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.safsouf.signalcollecte.model.CompleteAddress;
import com.example.safsouf.signalcollecte.service.BackgroundDetectedActivitiesService;
import com.example.safsouf.signalcollecte.utils.Constants;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.os.Build.MANUFACTURER;
import static android.os.Build.MODEL;

public class MainActivity extends AppCompatActivity {

    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;
    String mSignalStrength = "";
    TextView txt_etat;

    private String TAG = MainActivity.class.getSimpleName();
    BroadcastReceiver broadcastReceiver;

    //LTE Infos
    TextView rsrp;
    TextView rsrq;
    TextView rssi;
    TextView cqi;
    TextView SS;

    //device infos
    TextView dn;
    TextView dm;
    TextView dv;
    TextView operatorTextView;

    //Cell infos
    TextView cellIDTextView;
    TextView cellMccTextView;
    TextView cellMncTextView;
    TextView cellPciTextView;
    TextView cellTacTextView;

    //location Infos
    TextView longitudeTextView;
    TextView latitudeTextView;
    TextView fullAddressTextView;
    private FusedLocationProviderClient mFusedLocationClient;

    //activity info
    private TextView txtActivity, txtConfidence;
    private ImageView imgActivity;

    List<CellInfo> cellInfoList;
    int cellSig, cellID, cellMcc, cellMnc, cellPci, cellTac = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            initComponent();

            initPhoneManager();

            getActivityinfo();

            getDeviceÌnfo();

            getCellInfo();

            getLocationInfo();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 101) {
            if (permissions.length == 1 && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                initComponent();

                initPhoneManager();

                getActivityinfo();

                getDeviceÌnfo();

                getCellInfo();

                getLocationInfo();

                return;
            }
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            mSignalStrength = signalStrength.toString();
            String[] parts = mSignalStrength.split(" ");


            if (mTelephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                txt_etat.setText("Connected to LTE");
                txt_etat.setTextColor(Color.GREEN);
                SS.setVisibility(View.VISIBLE);
                rsrp.setVisibility(View.VISIBLE);
                rsrq.setVisibility(View.VISIBLE);
                rssi.setVisibility(View.VISIBLE);
                rssi.setVisibility(View.VISIBLE);
                cqi.setVisibility(View.VISIBLE);

                //LteSignalStrength PART 8
                SS.setText(convertToDbm(parts[8]));

                //RSRP PART 9
                rsrp.setText(convertToDbm(parts[9]));

                //RSRQPART 10
                rsrq.setText(convertToDbm(parts[10]));

                //RSSNR PART 11
                rssi.setText(convertToDbm(parts[11]));

                //CQI  PART 12
                cqi.setText(convertToDbm(parts[12]));
            } else {
                txt_etat.setText("Not connected to LTE");
                txt_etat.setTextColor(Color.RED);
                SS.setVisibility(View.GONE);
                rsrp.setVisibility(View.GONE);
                rsrq.setVisibility(View.GONE);
                rssi.setVisibility(View.GONE);
                rssi.setVisibility(View.GONE);
                cqi.setVisibility(View.GONE);
            }
        }


        private String convertToDbm(String value) {
            return String.valueOf(Integer.parseInt(value) - 140);
        }

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfoList) {
            super.onCellInfoChanged(cellInfoList);
            getCellInfo(cellInfoList);
        }
    }

    private void getDeviceÌnfo() {
        dn.setText(MANUFACTURER);
        dm.setText(MODEL);
        dv.setText("Android " + Build.VERSION.RELEASE);
        operatorTextView.setText(mTelephonyManager.getNetworkOperatorName());
    }

    //
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void getCellInfo(List<CellInfo> cellInfoList) {
        int cellID = 0, cellMcc = 0, cellMnc = 0, cellPci = 0, cellTac = 0, cellLac = 0;
        try {
            if (mTelephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                CellInfo cellInfo = cellInfoList.get(0);
                if (cellInfo instanceof CellInfoLte) {
                    cellID = ((CellInfoLte) cellInfo).getCellIdentity().getCi();
                    cellMcc = ((CellInfoLte) cellInfo).getCellIdentity().getMcc();
                    cellMnc = ((CellInfoLte) cellInfo).getCellIdentity().getMnc();
                    cellPci = ((CellInfoLte) cellInfo).getCellIdentity().getPci();
                    cellTac = ((CellInfoLte) cellInfo).getCellIdentity().getTac();
                    cellIDTextView.setVisibility(View.VISIBLE);
                    cellMccTextView.setVisibility(View.VISIBLE);
                    cellMncTextView.setVisibility(View.VISIBLE);
                    cellPciTextView.setVisibility(View.VISIBLE);
                    cellTacTextView.setVisibility(View.VISIBLE);
                    cellIDTextView.setText(String.valueOf(cellID));
                    cellMccTextView.setText(String.valueOf(cellMcc));
                    cellMncTextView.setText(String.valueOf(cellMnc));
                    cellPciTextView.setText(String.valueOf(cellPci));
                    cellTacTextView.setText(String.valueOf(cellTac));
                }

            } else {
                cellIDTextView.setVisibility(View.GONE);
                cellMccTextView.setVisibility(View.GONE);
                cellMncTextView.setVisibility(View.GONE);
                cellPciTextView.setVisibility(View.GONE);
                cellTacTextView.setVisibility(View.GONE);

            }
        } catch (Exception e) {
            Log.d("SignalStrength", "++++++++++++++++++++++ null array spot 2: " + e);
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getCellInfo() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            cellInfoList = mTelephonyManager.getAllCellInfo();
            getCellInfo(cellInfoList);
        } catch (Exception e) {
            Log.d("SignalStrength", "+++++++++++++++++++++++++++++++++++++++++ null array spot 1: " + e);
        }
    }

    private void initComponent() {

        txtActivity = findViewById(R.id.txt_activity);
        txtConfidence = findViewById(R.id.txt_confidence);
        imgActivity = findViewById(R.id.img_activity);

        txt_etat = (TextView) findViewById(R.id.state);
        rsrp = (TextView) findViewById(R.id.rsrp);
        rsrq = (TextView) findViewById(R.id.rsrq);
        rssi = (TextView) findViewById(R.id.rssi);
        cqi = (TextView) findViewById(R.id.cqi);
        SS = (TextView) findViewById(R.id.ss);

        dn = (TextView) findViewById(R.id.dn);
        dm = (TextView) findViewById(R.id.dm);
        dv = (TextView) findViewById(R.id.dv);
        operatorTextView = (TextView) findViewById(R.id.operator_textView);

        cellIDTextView = (TextView) findViewById(R.id.cellIDTextView);
        cellMccTextView = (TextView) findViewById(R.id.cellMccTextView);
        cellMncTextView = (TextView) findViewById(R.id.cellMncTextView);
        cellPciTextView = (TextView) findViewById(R.id.cellPciTextView);
        cellTacTextView = (TextView) findViewById(R.id.cellTacTextView);

        longitudeTextView = (TextView) findViewById(R.id.long_TextView);
        latitudeTextView = (TextView) findViewById(R.id.lat_TextView);
        fullAddressTextView = (TextView) findViewById(R.id.full_addr_TextView);
    }

    private void initPhoneManager() {
        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_INFO);
    }

    private void getLocationInfo() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            longitudeTextView.setText(String.valueOf(location.getLongitude()));
                            latitudeTextView.setText(String.valueOf(location.getLatitude()));
                            try {
                                fullAddressTextView.setText(getCompleteAddress(location.getLongitude(), location.getLatitude()).getAddress());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

    }


    private CompleteAddress getCompleteAddress(double longitude, double latitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());


        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();

        return new CompleteAddress(address, city, state, country, postalCode);

    }

    public void getActivityinfo() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };

        startTracking();
    }

    private void handleUserActivity(int type, int confidence) {
        String label = getString(R.string.activity_unknown);
        int icon = R.drawable.ic_still;

        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);
                icon = R.drawable.ic_driving;
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);
                icon = R.drawable.ic_on_bicycle;
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = getString(R.string.activity_on_foot);
                icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                icon = R.drawable.ic_running;
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = getString(R.string.activity_unknown);
                break;
            }
        }

        Log.e(TAG, "User activity: " + label + ", Confidence: " + confidence);

        if (confidence > Constants.CONFIDENCE) {
            txtActivity.setText(label);
            txtConfidence.setText("Confidence: " + confidence);
            imgActivity.setImageResource(icon);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void startTracking() {
        Intent intent1 = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent1);
    }

    private void stopTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }
}

