package com.project.gaim.ui;

import static java.lang.Math.floor;
import static java.lang.Math.round;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonParser {

    // 매개변수로 Json 데이터를 받는 매서드 생성
    public static double[][] jsonParser(String resultJson){

        double[][] eph1e = new double[0][];

        try {
            JSONObject jsonObject = new JSONObject(resultJson); // 매개변수로 받은 전체 Json 데이터를 객체에 담아준 후 해당 변수에서 "eph" 이름의 배열을 담도록 하겠음
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject2 = new JSONObject(resultJson); // timestmap
            JSONObject jsonObject3 = new JSONObject(resultJson); // no
            JSONObject jsonObject4 = new JSONObject(resultJson); // gw
            double svAccuracy, iDot, iode, toc, c, b, a, iodc, crs, deltaN, m0, cuc, e;
            double cus, rootOfA, toe, cic, omega0, cis, crc, omega, omegaDot, tgd, tgd2, svHealth;

            jsonArray = jsonObject.getJSONArray("eph");
            int len = jsonArray.length();
            Log.d("JsonParsing_eph", "len : " + len);
            eph1e = new double[len][29];
            // eph 배열을 크기만큼 for문 돌리며 각각의 인덱스 값을 jsonobject2 객체에 담아준 후
            // 해당 객체에서 "timestmap"만 갖고오도록 하겠음
            for (int i=0; i<jsonArray.length();i++){
                jsonObject2 = jsonArray.getJSONObject(i);
                String timestamp = jsonObject2.getString("timestamp");
                // Log.d("JsonParsing_eph","timestamp : "+timestamp);

                jsonObject3 = jsonArray.getJSONObject(i);
                String no = jsonObject3.getString("no");
                // Log.d("JsonParsing_eph","no : "+no);

                String PRN_s = jsonObject2.getString("prn");
                double PRN = Double.parseDouble(PRN_s);
                // Log.d("JsonParsing_eph","prn : "+PRN);
                eph1e[i][1] = PRN;

                jsonObject4 = jsonArray.getJSONObject(i);
                String gw = jsonObject4.getString("gw");
                // Log.d("JsonParsing","gw : "+gw);

                String svAccuracy_s = jsonObject2.getString("svAccuracy");
                svAccuracy = Double.parseDouble(svAccuracy_s);
                eph1e[i][28] = svAccuracy;
                // Log.d("JsonParsing","svAccuracy : "+svAccuracy);

                String iDot_s = jsonObject2.getString("iDot");
                iDot = Double.parseDouble(iDot_s);
                eph1e[i][15] = iDot;
                // Log.d("JsonParsing","iDot : "+iDot);

                String iode_s = jsonObject2.getString("iode");
                iode = Double.parseDouble(iode_s);
                eph1e[i][7] = iode;
                // Log.d("JsonParsing","D : "+iode);

                String toc_s = jsonObject2.getString("toc");
                // Log.d("JsonParsing","toc : "+toc);

                String c_s = jsonObject2.getString("c");
                c = Double.parseDouble(c_s);
                eph1e[i][4] = c;
                // Log.d("JsonParsing","c : "+c);

                String b_s = jsonObject2.getString("b");
                b = Double.parseDouble(b_s);
                eph1e[i][3] = b;
                // Log.d("JsonParsing","b : "+b);

                String a_s = jsonObject2.getString("a");
                a = Double.parseDouble(a_s);
                eph1e[i][2] = a;
                // Log.d("JsonParsing","a : "+a);

                String iodc_s = jsonObject2.getString("iodc");
                iode= Double.parseDouble(iodc_s);
                eph1e[i][8] = iode;
                // Log.d("JsonParsing","iodc : "+iodc);

                String crs_s = jsonObject2.getString("crs");
                crs = Double.parseDouble(crs_s);
                eph1e[i][22] = crs;
                // Log.d("JsonParsing","crs : "+crs);

                String deltaN_s = jsonObject2.getString("deltaN");
                deltaN = Double.parseDouble(deltaN_s);
                eph1e[i][17] = deltaN;
                // Log.d("JsonParsing","deltaN : "+deltaN);

                String m0_s = jsonObject2.getString("m0");
                m0 = Double.parseDouble(m0_s);
                eph1e[i][14] = m0;
                // Log.d("JsonParsing","m0 : "+m0);

                String cuc_s = jsonObject2.getString("cuc");
                cuc = Double.parseDouble(cuc_s);
                eph1e[i][19] = cuc;
                // Log.d("JsonParsing","cuc : "+cuc);

                String e_s = jsonObject2.getString("e");
                e = Double.parseDouble(e_s);
                eph1e[i][10] = e;
                // Log.d("JsonParsing","e : "+e);

                String cus_s = jsonObject2.getString("cus");
                cus = Double.parseDouble(cus_s);
                eph1e[i][20] = cus;
                // Log.d("JsonParsing","cus : "+cus);

                String rootOfA_s = jsonObject2.getString("rootOfA");
                rootOfA = Double.parseDouble(rootOfA_s);
                eph1e[i][9] = rootOfA;
                // Log.d("JsonParsing","rootOfA : "+rootOfA);

                String toe_s = jsonObject2.getString("toe");
                toe = Double.parseDouble(toe_s);
                eph1e[i][0] = toe;
                // Log.d("JsonParsing","toe : "+toe);

                String cic_s = jsonObject2.getString("cic");
                cic = Double.parseDouble(cic_s);
                eph1e[i][23] = cic;
                // Log.d("JsonParsing","cic : "+cic);

                String omega0_s = jsonObject2.getString("omega0");
                omega0 = Double.parseDouble(omega0_s);
                eph1e[i][13] = omega0;
                // Log.d("JsonParsing","omega0 : "+omega0);

                String cis_s = jsonObject2.getString("cis");
                cis = Double.parseDouble(cis_s);
                eph1e[i][24] = cis;
                // Log.d("JsonParsing","cis : "+cis);

                String i0_s = jsonObject2.getString("i0");
                double i0 = Double.parseDouble(i0_s);
                eph1e[i][11] = i0;

                String crc_s = jsonObject2.getString("crc");
                crc = Double.parseDouble(crc_s);
                eph1e[i][21] = crc;
                // Log.d("JsonParsing","crc : "+crc);

                String omega_s = jsonObject2.getString("omega");
                omega = Double.parseDouble(omega_s);
                eph1e[i][12] = omega;
                // Log.d("JsonParsing","omega : "+omega);

                String omegaDot_s = jsonObject2.getString("omegaDot");
                omegaDot = Double.parseDouble(omegaDot_s);
                eph1e[i][16] = omegaDot;
                // Log.d("JsonParsing","omegaDot : "+omegaDot);

                String tgd_s = jsonObject2.getString("tgd");
                tgd = Double.parseDouble(tgd_s);
                eph1e[i][5] = tgd;
                // Log.d("JsonParsing","tgd : "+tgd);

                String tgd2_s = jsonObject2.getString("tgd2");
                // Log.d("JsonParsing","tgd2 : "+tgd2);

                String svHealth_s = jsonObject2.getString("svHealth");
                svHealth = Double.parseDouble(svHealth_s);
                eph1e[i][18] = svHealth;
                // Log.d("JsonParsing","svHealth : "+svHealth);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return eph1e;
    }

    public static double[][] jsonParser2(String resultJson, int gpst) {

        double[][] QM_temp = new double[0][];
        double[][] QM1e = new double[0][];
        ArrayList<Double> gs_list= new ArrayList<>();
        ArrayList<Double> prn_list= new ArrayList<>();
        ArrayList<Double> obs_list= new ArrayList<>();
        String ts = "";

        try {
            JSONObject jsonObject = new JSONObject(resultJson); // 매개변수로 받은 전체 Json 데이터를 객체에 담아준 후 해당 변수에서 "eph" 이름의 배열을 담도록 하겠음
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject2 = new JSONObject(resultJson); // timestmap
            double gs = 0, prn = 0, obs = 0;

            jsonArray = jsonObject.getJSONArray("obs");
            int len = jsonArray.length();
            // Log.d("JsonParsing_QM","len : "+len);

            // eph 배열을 크기만큼 for문 돌리며 각각의 인덱스 값을 jsonobject2 객체에 담아준 후
            // 해당 객체에서 "timestmap"만 갖고오도록 하겠음
            int k = 0;
            for (int i=0; i<jsonArray.length();i++){
                Log.d("JsonParsing_QM","QMle_length : "+ k);
                jsonObject2 = jsonArray.getJSONObject(i);
                String timestamp = jsonObject2.getString("timestamp");
                ts = timestamp;
                // Log.d("JsonParsing","timestamp : "+timestamp);
                double yy = Double.parseDouble(timestamp.substring(0, 4));
                if ((int)yy != 2022) continue;
                double mm = Double.parseDouble(timestamp.substring(4, 6));
                double dd = Double.parseDouble(timestamp.substring(6, 8));
                double hh = Double.parseDouble(timestamp.substring(8, 10));
                double mi = Double.parseDouble(timestamp.substring(10, 12));
                double ss = Double.parseDouble(timestamp.substring(12, 14));

                gs = (double)date2gwgs(yy, mm, dd, hh, mi, ss)[1];
                if (Math.abs(gpst - gs) > 20) continue;
                // gs += 19;
                // Log.d("JsonParsingQM_Fx","gs : "+ gs);

                String sat = jsonObject2.getString("sat");
                prn = Double.parseDouble(sat);
                // Log.d("JsonParsingQM_Fx","prn : "+ prn);
                if ((int)prn >= 33) continue;

                // Log.d("JsonParsingQM_Fo","gs : "+ gs);
                // Log.d("JsonParsingQM_Fo","prn : "+ prn);
                String p_list = jsonObject2.getString("P");
                obs = GetL1P(p_list);
                if ((int)obs == 0) continue;
                // Log.d("JsonParsingQM_Fo","obs : "+ obs);

                Log.d("JsonParsingQM_Fo","gs : "+ gs);
                Log.d("JsonParsingQM_Fo","prn : "+ prn);
                Log.d("JsonParsingQM_Fo","obs : "+ obs);
                gs_list.add(gs);
                prn_list.add(prn);
                obs_list.add(obs);
                // QM_temp[i][0] = gs;
                // QM_temp[i][1] = prn;
                // QM_temp[i][2] = (double)103;
                // QM_temp[i][3] = obs;

                k += 1;

            }

            QM1e = new double[k][4];
            for (int j = 0; j<k; j++) {
                QM1e[j][0] = gs_list.get(j);
                QM1e[j][1] = prn_list.get(j) + 100.0;
                QM1e[j][2] = 103.0;
                QM1e[j][3] = obs_list.get(j);
            }

            // Log.d("JsonParsing_QM","QMle_length : "+ k);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Log.d("timestampp", "timestamp_aws: " + ts);
        return QM1e;
    }

    public static double GetL1P(String p_list) {
        String[] list1 = p_list.split(",");
        String d1 = list1[0];
        d1 = d1.substring(1);
        list1[0] = d1;

        return Double.parseDouble(d1);

    }

    static double date2jd(double yy, double mo, double dd, double h, double m, double s){
        double JD = 367*yy - floor(7*(yy + floor((mo + 9)/12))/4) + floor(275*mo/9) + dd + 1721013.5 + ((s/60 + m)/60 + h)/24;
        return JD;
    }

    static double[] jd2gwgs(double JD){

        double a,b,c,e,f,d,day_of_week,gw, gs;

        a = floor(JD + .5);
        b = a + 1537;
        c = floor((b - 122.1) / 365.25);
        e = floor(365.25*c);
        f = floor((b - e) / 30.6001);
        d = b - e - floor(30.6001*f) + ((JD + .5) % 1);
        day_of_week = (floor(JD + .5) % 7);
        gw = floor((JD - 2444244.5) / 7);
        gs = ((d % 1) + day_of_week + 1) * 86400; //gs = (rem(d, 1) + day_of_week + 1) * 86400;
        gs = round(gs % (86400 * 7)); //수정 190927
        double[] gwgs = {gw, gs};

        return gwgs;
    }

    static double[] date2gwgs(double yy, double mo, double dd, double h, double m, double s) {
        double JD = date2jd(yy, mo, dd, h, m, s);
        double[] gwgs = jd2gwgs(JD);
        return gwgs;
    }






}

