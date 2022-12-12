package com.project.gaim.ui;

import static android.location.GnssMeasurement.ADR_STATE_CYCLE_SLIP;
import static android.location.GnssMeasurement.ADR_STATE_VALID;

import static com.project.gaim.ui.functions.GetError;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationBarView;
import com.project.gaim.R;
import com.project.gaim.databinding.ActivityMainBinding;
import com.project.gaim.ui.ViewModel.EXViewModel;
import com.project.gaim.ui.ViewModel.EXViewModel2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static com.project.gaim.ui.functions.GetDot;
import static com.project.gaim.ui.functions.GetNorm;
import static com.project.gaim.ui.functions.GetObs;
import static com.project.gaim.ui.functions.GetRefSat;
import static com.project.gaim.ui.functions.GetRhoVec;
import static com.project.gaim.ui.functions.GetSTTbrdc;
import static com.project.gaim.ui.functions.GetSatPos;
import static com.project.gaim.ui.functions.GetSats1e;
import static com.project.gaim.ui.functions.PickEPH;
import static com.project.gaim.ui.functions.RotSatPos;
import static com.project.gaim.ui.functions.xyz2azel;
import static com.project.gaim.ui.functions.xyz2gd;

import androidx.lifecycle.ViewModelProvider;

import com.project.gaim.ui.mapscreen;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMainBinding binding;

    public LocationManager locationManager;
    // float[] R = new float[16];

    private boolean locreceive = false;

    private boolean first_log = true;
    private boolean xyz_mode = true;
    private double FirstFullBiasNaNos;
    TextView utc_view, gs_view, kst_view, pphq_view, gaim_view;
    TextView he_view, ve_view, he_view_before, ve_view_before;

    Button x_lat_button, y_lon_button, z_h_button;

    TextView x_lat_view, y_lon_view, z_h_view;

    double[][] eph = new double[0][29];
    double[][] qm_obs = new double[0][4];
    double[][] qm_obs_old = new double[0][4];
    double[][] qm_rov = new double[0][4];
    EXViewModel exViewModel;
    EXViewModel2 exViewModel2;

    int startepoch = 0;
    int baseepoch = 0;

    String filename, PPHQ_QM = "", SP_QM = "";
    int screennum = 0;

    Switch mSwitch;

    double[] SPP_pos = new double[3];
    double[] DD_pos = new double[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavigationBarView navView = findViewById(R.id.nav_view);
        navView.setOnItemSelectedListener(
                new BottomNavigationView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_SHORT).show();
                        // 어떤 메뉴 아이템이 터치되었는지 확인합니다.
                        switch (item.getItemId()) {

                            case R.id.navigation_home:

                                screennum =1;
                                break;

                            case R.id.navigation_dashboard:
                                screennum =2;

                                break;

                            case R.id.navigation_notifications:

                                screennum =3;
                                break;
                        }
                        return false;
                    }
                });

        mSwitch = findViewById(R.id.DataStartSwitch);

        mSwitch.setOnCheckedChangeListener(new mSwitchListener());



        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        exViewModel = new ViewModelProvider(this).get(EXViewModel.class);
        exViewModel2 = new ViewModelProvider(this).get(EXViewModel2.class);


        LocationManager locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show();
            return;
        }
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        locManager.registerGnssMeasurementsCallback(gnssEventListener);


        he_view = findViewById(R.id.horizontalerrorNum);
        ve_view = findViewById(R.id.vertiaclerrorNum);
        he_view_before = findViewById(R.id.beforeHorizontalerrorNum);
        ve_view_before = findViewById(R.id.beforeVertiaclerrorNum);

        x_lat_button = findViewById(R.id.button5);
        y_lon_button = findViewById(R.id.button6);
        z_h_button = findViewById(R.id.button11);

        pphq_view = findViewById(R.id.PPHQList);
        gaim_view = findViewById(R.id.GaimResultList);
        utc_view = findViewById(R.id.UtcTimeList);
        gs_view = findViewById(R.id.GSTimeList);
        kst_view = findViewById(R.id.KSTtimeList);

        pphq_view.setMovementMethod(new ScrollingMovementMethod());
        gaim_view.setMovementMethod(new ScrollingMovementMethod());

        x_lat_view = findViewById(R.id.ResultX);
        y_lon_view = findViewById(R.id.ResultY);
        z_h_view =findViewById(R.id.button17);



        schedule_nav();
        exViewModel.getExModel().observe(this, exModel -> {
            // double[][] out = exModel;

            eph = exModel;
            /*for (int i = 0; i < exModel.length; i++) {
                /System.out.println(Arrays.toString(out[i]));
            }*/
        });


    }

    class mSwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked)
            {
                screennum = 1;
            }
                // textView.setVisibility(View.INVISIBLE);
            else{
                screennum = 0;
            }
                // textView.setVisibility(View.VISIBLE);
        }
    }


    public String QM(GnssClock gnssClock, GnssMeasurement measurement, double FullBiasNanosFirstEpoch) {
        // final String format = "   %-4s = %s\n";

        int WeekSecond = 604800;
        int CCC = 299792458;

        double f = 1575.42e6;
        double WL = CCC / f;

        int C1 = 03;
        int L1 = 11;
        int D1 = 31;
        int S1 = 41;

        // StringBuilder builder = new StringBuilder();
        String line = "";

        double TimeNanos = gnssClock.getTimeNanos();
        double FullBiasNanos = gnssClock.getFullBiasNanos();
        if (FullBiasNanosFirstEpoch == 0.0) {
            return line;
        }
        double BiasNanos = gnssClock.getBiasNanos();
        double TimeOffsetNanos = measurement.getTimeOffsetNanos();
        double ReceivedSvTimeNanos = measurement.getReceivedSvTimeNanos();
        int ConstellationType = measurement.getConstellationType();
        double PseudorangeRateMetersPerSecond = measurement.getPseudorangeRateMetersPerSecond();
        int PRN = measurement.getSvid();
        double AccumulatedDeltaRangeMeters = measurement.getAccumulatedDeltaRangeMeters();
        if ((int) ConstellationType == 1) {

            double NumberNanoSecondsWeek = WeekSecond * 1e9;
            double WeekNumberNanos = Math.floor(-FullBiasNanos / NumberNanoSecondsWeek) * NumberNanoSecondsWeek;

            double tRxNanos = (TimeNanos + TimeOffsetNanos) - (FullBiasNanosFirstEpoch + BiasNanos) - WeekNumberNanos;
            double tRxSeconds = tRxNanos * 1e-9;
            double PseudorangeSecond = tRxSeconds - ReceivedSvTimeNanos * 1e-9;

            double Pseudorange = PseudorangeSecond * CCC;
            line = String.format("%d", (int) Math.round(tRxSeconds)) + " "
                    + String.format("%d", PRN + 100) + " "
                    + String.format("%d", ConstellationType * 100 + C1) + " "
                    + String.format("%16.5f", Pseudorange) + "\n";
        }

        return line;

    }

    public double[] GetgsNobs(GnssClock gnssClock, GnssMeasurement measurement, double FullBiasNanosFirstEpoch) {

        int WeekSecond = 604800;
        int CCC = 299792458;

        double f = 1575.42e6;
        double WL = CCC / f;

        int C1 = 03;
        int L1 = 11;
        int D1 = 31;
        int S1 = 41;

        double[] gsNobs = new double[2];

        double TimeNanos = gnssClock.getTimeNanos();
        double FullBiasNanos = gnssClock.getFullBiasNanos();
        if (FullBiasNanosFirstEpoch == 0.0) {
            gsNobs = new double[]{0, 0};
            return gsNobs;
        }
        double BiasNanos = gnssClock.getBiasNanos();
        double TimeOffsetNanos = measurement.getTimeOffsetNanos();
        double ReceivedSvTimeNanos = measurement.getReceivedSvTimeNanos();
        double PseudorangeRateMetersPerSecond = measurement.getPseudorangeRateMetersPerSecond();
        double AccumulatedDeltaRangeMeters = measurement.getAccumulatedDeltaRangeMeters();

        double NumberNanoSecondsWeek = WeekSecond * 1e9;
        double WeekNumberNanos = Math.floor(-FullBiasNanos / NumberNanoSecondsWeek) * NumberNanoSecondsWeek;

        double tRxNanos = (TimeNanos + TimeOffsetNanos) - (FullBiasNanosFirstEpoch + BiasNanos) - WeekNumberNanos;
        double tRxSeconds = tRxNanos * 1e-9;
        double PseudorangeSecond = tRxSeconds - ReceivedSvTimeNanos * 1e-9;

        double Pseudorange = PseudorangeSecond * CCC;
        double gs = tRxSeconds;
        double obs = Pseudorange;

        gsNobs = new double[]{gs, obs};
        return gsNobs;

    }

    public double[][] GetQM(GnssClock gnssClock, GnssMeasurement measurement, double FullBiasNanosFirstEpoch, double[][] p_QM, int n) {

        int WeekSecond = 604800;
        int CCC = 299792458;

        double f = 1575.42e6;
        double WL = CCC / f;

        int C1 = 03;
        int L1 = 11;
        int D1 = 31;
        int S1 = 41;

        // StringBuilder builder = new StringBuilder();

        double TimeNanos = gnssClock.getTimeNanos();
        double FullBiasNanos = gnssClock.getFullBiasNanos();
        if (FullBiasNanosFirstEpoch == 0.0) {
            return p_QM;
        }
        double BiasNanos = gnssClock.getBiasNanos();
        double TimeOffsetNanos = measurement.getTimeOffsetNanos();
        double ReceivedSvTimeNanos = measurement.getReceivedSvTimeNanos();
        int ConstellationType = measurement.getConstellationType();
        double PseudorangeRateMetersPerSecond = measurement.getPseudorangeRateMetersPerSecond();
        int PRN = measurement.getSvid();
        double AccumulatedDeltaRangeMeters = measurement.getAccumulatedDeltaRangeMeters();
        if ((int) ConstellationType == 1) {

            double NumberNanoSecondsWeek = WeekSecond * 1e9;
            double WeekNumberNanos = Math.floor(-FullBiasNanos / NumberNanoSecondsWeek) * NumberNanoSecondsWeek;

            double tRxNanos = (TimeNanos + TimeOffsetNanos) - (FullBiasNanosFirstEpoch + BiasNanos) - WeekNumberNanos;
            double tRxSeconds = tRxNanos * 1e-9;
            double PseudorangeSecond = tRxSeconds - ReceivedSvTimeNanos * 1e-9;

            double Pseudorange = PseudorangeSecond * CCC;
            p_QM[n][0] = tRxSeconds;
            p_QM[n][1] = PRN + 100;
            p_QM[n][2] = ConstellationType * 100 + C1;
            p_QM[n][3] = Pseudorange;

        }

        return p_QM;
    }

    public double getFullBiasNanosFirstEpoch(GnssClock gnssClock) {
        double FullBiasedNanos = gnssClock.getFullBiasNanos();
        return FullBiasedNanos;
    }

    public boolean MeasurementFilter(GnssMeasurement measurement) {

        int ADR_state = measurement.getAccumulatedDeltaRangeState();
        if (ADR_state == ADR_STATE_CYCLE_SLIP || ADR_state == ADR_STATE_VALID) {
            return false;
        }

        int multipath_indicator = measurement.getMultipathIndicator();
        if (multipath_indicator == GnssMeasurement.MULTIPATH_INDICATOR_DETECTED) {
            return false;
        }

        int state = measurement.getState();
        if (state == GnssMeasurement.STATE_TOW_DECODED) {
            return false;
        }

        return true;
    }


    private final GnssMeasurementsEvent.Callback gnssEventListener = new GnssMeasurementsEvent.Callback() {

        /**
         *  Automatically retrieves gnss values when there is a change.
         *  calls the toStringMeasurement() and toStringClock() functions that displays and stores the measurements retrieved
         *  To display the data on the view, you have to call the  notifyGnssObserver() function
         * @param eventArgs
         */
        @SuppressLint("DefaultLocale")
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {

            if (screennum != 3) {
                mSwitch = findViewById(R.id.DataStartSwitch);
                mSwitch.setOnCheckedChangeListener(new mSwitchListener());
            }
            StringBuilder builder = new StringBuilder();
            StringBuilder builder2 = new StringBuilder();
            double[][] QM;
            String QQM = "";
            Log.d("where", "aaa");
            ArrayList<Double> prn_list = new ArrayList<>();
            ArrayList<Double> obs_list = new ArrayList<>();
            ArrayList<Double> prn_list_filter = new ArrayList<>();
            ArrayList<Double> obs_list_filter = new ArrayList<>();
            double gs = 0;
            for (GnssMeasurement measurement : eventArgs.getMeasurements()) {

                // filtering 추가
                // GnssClock gnssClock = eventArgs.getClock();
                // double TimeNanos = gnssClock.getTimeNanos();
                // double FullBiasNanos = gnssClock.getFullBiasNanos();

                if (first_log) {
                    FirstFullBiasNaNos = getFullBiasNanosFirstEpoch(eventArgs.getClock());
                    first_log = false;
                    // return;
                }

                String myQM;
                try {
                    myQM = QM(eventArgs.getClock(), measurement, FirstFullBiasNaNos);
                    builder.append(myQM);

                    // textview.setText(QQM);

                    int ConType = measurement.getConstellationType();
                    if (ConType != 1) continue;
                    String CodeType = measurement.getCodeType();
                    if (!CodeType.equals("C")) continue;
                    int PRN = measurement.getSvid() + 100;
                    prn_list.add((double) PRN);

                    double[] gsNobs = GetgsNobs(eventArgs.getClock(), measurement, FirstFullBiasNaNos);
                    gs = gsNobs[0];
                    double obs = gsNobs[1];
                    obs_list.add(obs);

                    // QM = GetQM(eventArgs.getClock(), measurement, FirstFullBiasNaNos, QM, i);
                    // i += 1;
                    // textview.setText(Arrays.toString(QM));

                } catch (Exception e) {
                    e.printStackTrace();
                }

                boolean filter_state = MeasurementFilter(measurement);
                if (!filter_state) continue;

                try {

                    int ConType = measurement.getConstellationType();
                    if (ConType != 1) continue;
                    int PRN = measurement.getSvid() + 100;
                    prn_list_filter.add((double) PRN);

                    double[] gsNobs = GetgsNobs(eventArgs.getClock(), measurement, FirstFullBiasNaNos);
                    gs = gsNobs[0];
                    double obs = gsNobs[1];
                    obs_list_filter.add(obs);


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            startepoch += 1;
            Log.d("where2", "a\t" + startepoch);
            // textview.setText(Integer.toString(startepoch));

            StringBuilder str = new StringBuilder();
            int NoSats = prn_list_filter.size();
            QM = new double[NoSats][4];
            for (int i = 0; i < NoSats; i++) {
                // if (i == 0) str.append(Double.toString(gs)).append("\n");
                QM[i][0] = Math.round(gs);
                QM[i][1] = prn_list_filter.get(i);
                QM[i][2] = 103.0;
                QM[i][3] = obs_list_filter.get(i);
                str.append(Arrays.toString(QM[i])).append("\n");
               //  SP_QM += String.format("%d %d %d %.7f\n", (int) QM[i][0], (int) QM[i][1], 103,QM[i][3]);

            }

            Log.d("check_sn", "screennum: "+ screennum);
            //if(screennum ==1){
                //gaim_view = findViewById(R.id.GaimResultList);
                //gaim_view.setText(str);
            //}

            int gpst = (int)QM[0][0];
            Log.d("check", "gpst: " + gpst);

            //qm_rov = QM;
            // qm_obs = connect_obs();
            double[][] pphq_qm = connect_obs(gpst);

            // textview.setText(str);
            // textview.setText(QQM);

            StringBuilder str2 = new StringBuilder();
            // Toast.makeText(this, "toe: " + eph[0][0] + " prn: " + eph[0][1], Toast.LENGTH_SHORT).show();
            // Toast.makeText(this, str.toString() + qm_obs.length + " " + eph.length, Toast.LENGTH_SHORT).show();
            for (int i = 0; i < pphq_qm.length; i++) {
                str2.append(Arrays.toString(pphq_qm[i])).append("\n");
            }
            // Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
            /*gaim_view = findViewById(R.id.GaimResultList);
            gaim_view.setText(str);
            pphq_view = findViewById(R.id.PPHQList);
            pphq_view.setText(str2);*/

            int utct = (int)pphq_qm[0][0];
            int minute = utct / 60;
            int hour = utct / 3600;

            minute = minute - hour * 60;
            utct = utct - hour * 3600 - minute *60;
            hour = hour % 24;
            int kstHour = (hour+9) % 24;
            DecimalFormat df = new DecimalFormat("00");
            // System.out.println(df.format(n)); // 출력값 : 12346

           // String utc = String.format("%2d시 %2d분 %2d초", hour, minute, sec);
            String utc = df.format(hour) + "시 " + df.format(minute) + "분 " + df.format(utct) + "초";
            String kst = df.format(kstHour) + "시 " + df.format(minute) + "분 " + df.format(utct) + "초";

            /*gaim_view = findViewById(R.id.GaimResultList);
            gaim_view.setText(str);
            pphq_view = findViewById(R.id.PPHQList);
            pphq_view.setText(str2);

            utc_view = findViewById(R.id.UtcTimeList);
            utc_view.setText(utc + String.format("\t(%d)", (int)pphq_qm[0][0]));

            kst_view = findViewById(R.id.KSTtimeList);
            kst_view.setText(kst);

            gs_view = findViewById(R.id.GSTimeList);
            gs_view.setText(String.format("%d", gpst));*/

            double[] NEV_Err_DD = DD_Positioning(QM, pphq_qm, eph);
            double[] NEV_Err_SPP = SPP(QM, eph);

             // he_view = findViewById(R.id.horizontalerrorNum);
             // ve_view = findViewById(R.id.vertiaclerrorNum);

            double HE = Math.sqrt(NEV_Err_DD[0] * NEV_Err_DD[0] + NEV_Err_DD[1] * NEV_Err_DD[1]);
            double VE = Math.abs(NEV_Err_DD[2]);

            double HE_bf = Math.sqrt(NEV_Err_SPP[0] * NEV_Err_SPP[0] + NEV_Err_SPP[1] * NEV_Err_SPP[1]);
            double VE_bf = Math.abs(NEV_Err_SPP[2]);

            double[] xyz = {NEV_Err_DD[3], NEV_Err_DD[4], NEV_Err_DD[5]};
            double[] llh = xyz2gd(xyz);

            DD_pos = llh;

            if (screennum == 3) {
                he_view = findViewById(R.id.horizontalerrorNum);
                ve_view = findViewById(R.id.vertiaclerrorNum);
                he_view_before = findViewById(R.id.beforeHorizontalerrorNum);
                ve_view_before = findViewById(R.id.beforeVertiaclerrorNum);
                he_view.setText(String.format("%.2fm", HE));
                ve_view.setText(String.format("%.2fm", VE));
                he_view_before.setText(String.format("%.2fm", HE_bf));
                ve_view_before.setText(String.format("%.2fm", VE_bf));

                x_lat_view = findViewById(R.id.ResultX);
                y_lon_view = findViewById(R.id.ResultY);
                z_h_view =findViewById(R.id.button17);


                if (xyz_mode) {
                    x_lat_view.setText(String.format("%.4fm", xyz[0]));
                    y_lon_view.setText(String.format("%.4fm", xyz[1]));
                    z_h_view.setText(String.format("%.4fm", xyz[2]));
                }
                else {
                    x_lat_view.setText(String.format("%.5f°", llh[0]));
                    y_lon_view.setText(String.format("%.5f°", llh[1]));
                    z_h_view.setText(String.format("%.5fm", llh[2]));
                }
            }

            if (screennum == 1) {
                gaim_view = findViewById(R.id.GaimResultList);
                gaim_view.setText(str);
                pphq_view = findViewById(R.id.PPHQList);
                pphq_view.setText(str2);

                utc_view = findViewById(R.id.UtcTimeList);
                utc_view.setText(utc + String.format("\t(%d)", (int) pphq_qm[0][0]));

                kst_view = findViewById(R.id.KSTtimeList);
                kst_view.setText(kst);

                gs_view = findViewById(R.id.GSTimeList);
                gs_view.setText(String.format("%d", gpst));

            }


        }

        @Override
        public void onStatusChanged(int status) {
            super.onStatusChanged(status);
        }
    };


    private LocationListener locationListener = new LocationListener() {

        /**
         * Automatically retrieves location values when there is a change.
         * calls the toStringLocation() function that displays and stores the measurements retrieved
         * To display the data on the view, you have to call the  notifyLocationObserver() function
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            if (!locreceive) {

                // textview.setText("GPS Recieved");
                locreceive = true;
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public void schedule_nav() {
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                connect_nav();
            }
        };
        timer.schedule(task, 0, 600 * 1000);
    }

    public void schedule_obs() {
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                connect_obs(0);
            }
        };
        timer.schedule(task, 0, 750);
    }

    public void connect_nav() {
        double[][] EPH_temp = new double[3000][29];
        double[][] EPH_;
        int i = 0;

        try {

            URL url = new URL("https://api.dev.gnsson.com/api/support/nav");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn != null) {
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");

                // 서버와 정상적으로 연결된지 확인
                int resCode = conn.getResponseCode();
                int HTTP_OK = HttpURLConnection.HTTP_OK;// 서버와 정상적으로 통신 되었을때 응답 코드 200

                // 데이터 버퍼에 담음
                if (resCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    // while문 통해 값 불러옴
                    String line = "";
                    while (true) {
                        line = reader.readLine();
                        Log.d("JsonParsing_eph", "line : " + line);

                        if (line == null) {
                            break;
                        }
                        EPH_temp = JsonParser.jsonParser(line);
                        i = EPH_temp.length;
                    }
                    reader.close(); // 통신후 연결했던 버퍼와 커넥션 닫음
                    EPH_ = new double[i + 1][29];
                    for (int j = 0; j < i; j++) {
                        for (int k = 0; k < 29; k++) {
                            EPH_[j][k] = EPH_temp[j][k];
                        }
                        // Log.d("EPH_", "EPH_prn: " + EPH_[j][0]);
                        // Log.d("EPH_", "EPH_toe: " + EPH_[j][1]);
                    }

                    // exViewModel.setExModel(eph);
                    exViewModel.setExModel(EPH_);
                }
                conn.disconnect();

            }

        } catch (Exception e) {
            Log.d("Exception", "ExceptionErr");
        }
    }

    @SuppressLint("DefaultLocale")
    double[][] connect_obs(int gs) {

        double[][] QM_temp = new double[0][];
        int i = 0;
        try {

            URL url = new URL("https://api.dev.gnsson.com/api/support/obs");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn != null) {
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");

                // 서버와 정상적으로 연결된지 확인
                int resCode = conn.getResponseCode();
                int HTTP_OK = HttpURLConnection.HTTP_OK;// 서버와 정상적으로 통신 되었을때 응답 코드 200

                // 데이터 버퍼에 담음
                if (resCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    // while문 통해 값 불러옴
                    String line = "";
                    while (true) {
                        line = reader.readLine();
                        Log.d("JsonParsing_QM", "line : " + line);
                        if (line == null) {
                            break;
                        }
                        QM_temp = JsonParser.jsonParser2(line, gs);
                        i = QM_temp.length;
                        //  Log.d("JsonParsingQM_result", "num: " + i);
                    }
                    reader.close(); // 통신후 연결했던 버퍼와 커넥션 닫음
                    double[][] QM_ = new double[i][4];
                    for (int j = 0; j < i; j++) {
                        PPHQ_QM += String.format("%d %d %d %.7f\n", (int) QM_temp[j][0], (int) QM_temp[j][1], 103, QM_temp[j][3]);
                        for (int k = 0; k < 4; k++) {
                            QM_[j][k] = QM_temp[j][k];
                        }
                        // Log.d("EPH_", "EPH_prn: " + EPH_[j][0]);
                        // Log.d("EPH_", "EPH_toe: " + EPH_[j][1]);
                    }

                    // exViewModel.setExModel(eph);
                    exViewModel2.setExModel(QM_);

                }

                conn.disconnect();

            }

        } catch (Exception e) {
            Log.d("Exception", "ExceptionErr");
        }

        return QM_temp;

    }

    public double[] SPP(double[][] QM1e, double[][] eph) {

        double[] TruePos = {-3026675.73661700,4067187.98825494,3857246.98481191};
        double[] TruePos_LLH = xyz2gd(TruePos);

        // double[] TruePos_A = { -3041235.578, 4053941.677, 3859881.013 };
        // double[] TruePos_B = { -3041241.741, 4053944.143, 3859873.640 };

        int MaxIter = 4;
        int obsType = 103; // GPS L1
        double EpsStop = 1e-5;
        int vc = 299792458;

        double[][] QM = QM1e;
        double[][] EPH = eph;

        double[] estm = new double[3];

        int gs = (int)QM[0][0];

        int[] Sats1e = GetSats1e(QM, QM);
        int NoSatsle = Sats1e.length;
        int UseSats = 0;

        double[] x_double = {TruePos[0], TruePos[1], TruePos[2], 1};
        RealMatrix x = new Array2DRowRealMatrix(x_double);

        for (int iter = 1; iter <= MaxIter; iter++) {

            UseSats = 0;

            double[][] vec_sta_double_temp = x.getData();
            double[] vec_sta_double = {vec_sta_double_temp[0][0], vec_sta_double_temp[1][0], vec_sta_double_temp[2][0]};
            RealMatrix vec_sta = new Array2DRowRealMatrix(vec_sta_double).transpose();

            // * Reset HTH & HTy *

            double[][] hth = new double[4][4];
            double[][] hty = new double[4][1];

            RealMatrix HTH = new Array2DRowRealMatrix(hth);
            RealMatrix HTy = new Array2DRowRealMatrix(hty);

            for (int j = 0; j < NoSatsle; j++) {

                int prn = Sats1e[j];
                if (prn > 200) continue;
                int icol = PickEPH(EPH, prn, gs);
                if (icol == -1) continue;
                if (EPH[icol][18] != 0) continue;

                double toe = EPH[icol][0];
                double a = EPH[icol][2];
                double b = EPH[icol][3];
                double c = EPH[icol][4];
                double Tgd = EPH[icol][5];

                // * Get Observation from QM about other Sat *

                double obs = GetObs(QM, prn);
                if (Double.isNaN(obs)) continue;

                double STT = GetSTTbrdc(gs, EPH, icol, new Array2DRowRealMatrix(TruePos).transpose());

                // * Get Computed Value about other Sat *

                RealMatrix vec_sat = GetSatPos(EPH, icol, gs - STT);
                vec_sat = RotSatPos(vec_sat, STT);

                RealMatrix vec_rho = GetRhoVec(vec_sat, vec_sta);
                double[][] vec_rho_temp = vec_rho.getData();
                double[] vec_rho_double = {vec_rho_temp[0][0], vec_rho_temp[0][1], vec_rho_temp[0][2]};
                double el = xyz2azel(vec_rho_double, TruePos_LLH[0], TruePos_LLH[1])[1];

                // if (el<15) continue;

                double rho = GetNorm(vec_rho);

                RealMatrix vec_sat_2 = GetSatPos(EPH, icol, gs - STT + 1e-3);
                vec_sat_2 = RotSatPos(vec_sat_2, STT);

                RealMatrix vel_sat = vec_sat_2.add(vec_sat.scalarMultiply(-1)).scalarMultiply(1e3);

                double dRel = -2 * GetDot(vel_sat, vec_sat) / (Math.pow(vc, 2));
                double dtSat = a + b * (gs - STT - toe) + c * (gs - STT - toe) * (gs - STT - toe) - Tgd + dRel;

                // * Set H Matrix *

                double[][] hh_temp = new double[1][4];
                RealMatrix h_temp = vec_rho.scalarMultiply(-1 / rho);
                hh_temp[0][0] = h_temp.getData()[0][0];
                hh_temp[0][1] = h_temp.getData()[0][1];
                hh_temp[0][2] = h_temp.getData()[0][2];
                hh_temp[0][3] = 1.0;

                RealMatrix H = new Array2DRowRealMatrix(hh_temp);

                // * Set Y Matrix *

                // double obs = (obs_ref_rov - obs_rov) - (obs_ref_bas - obs_bas);
                double com = rho + x.getData()[3][0] - vc * dtSat;
                double Y = obs - com;

                // * Least Square *

                RealMatrix HT = H.transpose();
                HTH = HTH.add(HT.multiply(H));
                HTy = HTy.add(HT.scalarMultiply(Y));

                UseSats += 1;

            }

            RealMatrix inv_HTH = new LUDecomposition(HTH).getSolver().getInverse();
            RealMatrix xhat = inv_HTH.multiply(HTy);
            double xhat_norm = GetNorm(xhat.transpose());
            // Log.d("estm", "xhat" + String.valueOf(xhat_norm) + " iter: " + iter);

            if (xhat_norm < EpsStop) {

                double[][] position = x.getData();

                // estm[0] = gs;
                estm[0] = position[0][0];
                estm[1] = position[1][0];
                estm[2] = position[2][0];
                // Log.d("NEV", "position: " + Arrays.toString(estm));
                break;

            }

            x = x.add(xhat);
            // Log.d("estm", "estm" + Arrays.toString(estm));

        }

        Log.d("NEV_Err_SPP", "NoSats: " + String.valueOf(NoSatsle) + " Using: " + String.valueOf(UseSats));
        double[] err = GetError(gs, gs, estm, TruePos);
        double[] output = new double[6];
        output[0] = err[0]; output[1] = err[1]; output[2] = err[2];
        output[3] = estm[0]; output[4] = estm[1]; output[5] = estm[2];
        return output;

    }

    public void Record_QM(String Base, String Rover) {

        String filename = Base.split(" ")[0];
        Log.d("check", "PPHQ First Epoch: " + filename);

        try {
            OutputStreamWriter oStreamWriter = new OutputStreamWriter(openFileOutput(filename+"_Base.txt", Context.MODE_APPEND)); // APPEND: 이전에 있던 내용에 추가하여 새로 씀
            oStreamWriter.write(Base);
            oStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show();
        // PPHQ_QM = "";

        try {
            OutputStreamWriter oStreamWriter = new OutputStreamWriter(openFileOutput(filename+"_Rover.txt", Context.MODE_APPEND)); // APPEND: 이전에 있던 내용에 추가하여 새로 씀
            oStreamWriter.write(Rover);
            oStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show();
        // SP_QM = "";
    }

    public double[] DD_Positioning(double[][] QM_Rover, double[][] QM_Base, double[][] eph) {

        double[] TruePos_R = {-3.026676290573975e+06, 4.067187712050182e+06, 3.857246792189259e+06};
        double[] TruePos_B = {-3041231.8821, 4053906.9380, 3859927.5707};
        double[] TruePos_LLH = xyz2gd(TruePos_R);

        int MaxIter = 4;
        int obsType = 103; // GPS L1
        double EpsStop = 1e-5;
        int vc = 299792458;

        int gs_rover = (int) Math.round(QM_Rover[0][0]);
        int gs_base = (int)QM_Base[0][0];
        // Log.d("NEV_gs_base", "gs_base: " + gs_base);
        double[] estm = new double[3];

        RealMatrix x_A = new Array2DRowRealMatrix(TruePos_R);
        RealMatrix x_Rover = x_A;

        RealMatrix x_B = new Array2DRowRealMatrix(TruePos_B);

        int[] Sats1e = GetSats1e(QM_Base, QM_Rover);
        int NoSats1e = Sats1e.length;
        int UseSats = 0;

        int ref_sat_prn = GetRefSat(Sats1e, TruePos_R, QM_Rover, eph);

        double obs_ref_rov = GetObs(QM_Rover, ref_sat_prn);
        if (obs_ref_rov == 0) return new double[3];

        double obs_ref_bas = GetObs(QM_Base, ref_sat_prn);
        if (obs_ref_bas == 0) return new double[3];

        int icol = PickEPH(eph, ref_sat_prn, gs_rover);

        if (icol == -1) return new double[3];
        if (eph[icol][18] != 0) return new double[3];

        double STT_ref = GetSTTbrdc(gs_rover, eph, icol, x_Rover.transpose());
        RealMatrix ref_vec_sat = GetSatPos(eph, icol, gs_rover - STT_ref);
        ref_vec_sat = RotSatPos(ref_vec_sat, STT_ref);

        for (int iter = 1; iter <= MaxIter; iter++) {

            UseSats = 0;

            RealMatrix vec_rov = x_Rover.transpose();
            RealMatrix vec_bas = x_B.transpose();

            // * Reset HTH & HTy *

            double[][] hth = new double[3][3];
            double[][] hty = new double[3][1];

            RealMatrix HTH = new Array2DRowRealMatrix(hth);
            RealMatrix HTy = new Array2DRowRealMatrix(hty);

            // * Get Computed Value about ref. Sat *

            RealMatrix vec_rho_ref_rov = GetRhoVec(ref_vec_sat, vec_rov);
            RealMatrix vec_rho_ref_bas = GetRhoVec(ref_vec_sat, vec_bas);

            double com_ref_rov = GetNorm(vec_rho_ref_rov);
            double com_ref_bas = GetNorm(vec_rho_ref_bas);

            for (int j = 0; j < NoSats1e; j++) {

                int prn = Sats1e[j];
                icol = PickEPH(eph, prn, gs_rover);
                if (icol == -1) continue;

                // * Get Observation from QM about other Sat *

                double obs_rov = GetObs(QM_Rover, prn);
                if (obs_rov == 0) continue;
                double obs_bas = GetObs(QM_Base, prn);
                if (obs_bas == 0) continue;
                double STT = GetSTTbrdc(gs_rover, eph, icol, x_Rover.transpose());

                // * Get Computed Value about other Sat *

                RealMatrix vec_sat = GetSatPos(eph, icol, gs_rover - STT);
                vec_sat = RotSatPos(vec_sat, STT);

                RealMatrix vec_rho_rov = GetRhoVec(vec_sat, vec_rov);
                RealMatrix vec_rho_bas = GetRhoVec(vec_sat, vec_bas);

                double[][] vec_rho_temp = vec_rho_rov.getData();
                double[] vec_rho_double = {vec_rho_temp[0][0], vec_rho_temp[0][1], vec_rho_temp[0][2]};
                double el = xyz2azel(vec_rho_double, TruePos_LLH[0], TruePos_LLH[1])[1];

                if (el<15) continue;

                double com_rov = GetNorm(vec_rho_rov);
                double com_bas = GetNorm(vec_rho_bas);

                // * Set H Matrix *

                RealMatrix aa = vec_rho_ref_rov.scalarMultiply(-1 / com_ref_rov);
                RealMatrix bb = vec_rho_rov.scalarMultiply(1 / com_rov);
                RealMatrix H = aa.add(bb);

                // * Set Y Matrix *

                double obs = (obs_ref_rov - obs_rov) - (obs_ref_bas - obs_bas);
                double com = (com_ref_rov - com_rov) - (com_ref_bas - com_bas);
                double Y = obs - com;
                // Log.d("yy", "y: " + Y);

                // * Least Square *

                RealMatrix HT = H.transpose();
                HTH = HTH.add(HT.multiply(H));
                HTy = HTy.add(HT.scalarMultiply(Y));

                UseSats += 1;

            }

            RealMatrix inv_HTH = new LUDecomposition(HTH).getSolver().getInverse();
            RealMatrix xhat = inv_HTH.multiply(HTy);
            double xhat_norm = GetNorm(xhat.transpose());
            // Log.d("xhat", "xhat: " + xhat_norm);

            if (xhat_norm < EpsStop) {

                double[][] position = x_Rover.getData();

                // estm[0] = gs;
                estm[0] = position[0][0];
                estm[1] = position[1][0];
                estm[2] = position[2][0];
                break;
            }

            x_Rover = x_Rover.add(xhat);

        }

        Log.d("NEV_Err_DD", "NoSats: " + String.valueOf(NoSats1e)+ " Using: " + String.valueOf(UseSats));
        double[] err = GetError(gs_rover, gs_base, estm, TruePos_R);
        double[] output = new double[6];
        output[0] = err[0]; output[1] = err[1]; output[2] = err[2];
        output[3] = estm[0]; output[4] = estm[1]; output[5] = estm[2];


        return output;

    }

    public void onButtonClear(View view){
        Toast.makeText(getApplicationContext(),"계산을 시작합니다.",Toast.LENGTH_SHORT).show();
        screennum = 3;
    }

    public void onButtonRegist(View view){
        Toast.makeText(getApplicationContext(),"계산을 멈춥니다.",Toast.LENGTH_SHORT).show();
        screennum = 0;
    }

    public void onMapButton(View view) {

        screennum = 2;
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
         //       .findFragmentById(R.id.map);
        // mapFragment.getMapAsync(this);
        Intent intent = new Intent(this, mapscreen.class);
        intent.putExtra("DD_pos", DD_pos);
        startActivity(intent);


    }

    GoogleMap mMap;

    @Override
    public void onMapReady(@NonNull final GoogleMap googleMap) {

        Log.d("map", "map is ready");
        mMap = googleMap;
        double inha_lon = 126.6530444;
        double inha_lat = 37.44865000;
        LatLng inhauniv = new LatLng(inha_lat, inha_lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(inhauniv, 17));

        // mMap.setOnMarkerClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    public void onClickLLH(View view) {

        x_lat_button = findViewById(R.id.button5);
        y_lon_button = findViewById(R.id.button6);
        z_h_button = findViewById(R.id.button11);

        switch (view.getId()) {
            case R.id.btn_LLH:
                if (xyz_mode) {
                    // Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
                    x_lat_button.setText("LAT");
                    y_lon_button.setText("LON");
                    z_h_button.setText("H");
                    xyz_mode = false;
                }
                break;
            case R.id.btn_XYZ:
                if (!xyz_mode) {
                    x_lat_button.setText("X");
                    y_lon_button.setText("Y");
                    z_h_button.setText("Z");
                    xyz_mode = true;
                }

        }
    }




}

