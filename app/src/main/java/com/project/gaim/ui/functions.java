package com.project.gaim.ui;

import android.util.Log;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;

public class functions {

    public static int[] GetSats1e(double[][] QM1e_A, double[][] QM1e_B) {

        int A_len = QM1e_A.length; int B_len = QM1e_B.length;

        int[] Sats_A = new int[A_len];
        for (int a = 0; a < A_len; a++) Sats_A[a] = (int)QM1e_A[a][1];

        int[] Sats_B = new int[B_len];
        for (int b = 0; b < B_len; b++) Sats_B[b] = (int)QM1e_B[b][1];

        ArrayList<Integer> RealSats = new ArrayList<Integer>();

        int max_Sats1e = Math.max(A_len, B_len);

        if (max_Sats1e >= A_len) {
            for (int s1 = 0; s1 < A_len; s1++) {

                int prn = Sats_A[s1];
                for (int s2 = 0; s2 < B_len; s2++) {
                    if (Sats_B[s2] == prn) {
                        RealSats.add(prn);
                        break;
                    }
                }

            }
        }

        else {
            for (int s1 = 0; s1 < B_len; s1++) {

                int prn = Sats_B[s1];
                for (int s2 = 0; s2 < A_len; s2++) {
                    if (Sats_A[s2] == prn) {
                        RealSats.add(prn);
                        break;
                    }
                }

            }
        }

        int NoSatsle = RealSats.size();

        int[] Sats1e = new int[NoSatsle];
        for (int i = 0; i < NoSatsle; i++) Sats1e[i] = RealSats.get(i);

        return Sats1e;

    }

    public static RealMatrix GetRhoVec(RealMatrix vec_sat, RealMatrix vec_sta) {
        vec_sta = vec_sta.scalarMultiply(-1);
        RealMatrix vec_rho = vec_sat.add(vec_sta);
        // vec_rho = vec_rho.transpose();
        return vec_rho;
    }

    public static double GetNorm(RealMatrix vec_rho) {

        double[][] xyz = vec_rho.getData();
        double Norm = Math.pow(xyz[0][0], 2) + Math.pow(xyz[0][1], 2) + Math.pow(xyz[0][2], 2);
        Norm = Math.sqrt(Norm);
        return Norm;
    }

    static double GetDot(RealMatrix a, RealMatrix b) {

        double[][] aa = a.getData();
        double[][] bb = b.getData();
        double dot = aa[0][0] * bb[0][0] + aa[0][1] * bb[0][1] + aa[0][2] * bb[0][2];
        return dot;

    }

    public static double[] xyz2topo(double[] xyz, double lat, double lon) {

        double latitude = lat * Math.PI / 180;
        double longitude = lon * Math.PI / 180;

        double cos_lat = Math.cos(latitude);
        double sin_lat = Math.sin(latitude);

        double cos_lon = Math.cos(longitude);
        double sin_lon = Math.sin(longitude);

        double x = xyz[0], y = xyz[1], z = xyz[2];

        double N = -sin_lat*cos_lon*x - sin_lat*sin_lon*y + cos_lat*z;
        double E = -sin_lon*x + cos_lon*y;
        double V = cos_lat*cos_lon*x + cos_lat*sin_lon*y + sin_lat*z;

        double[] NEV = {N, E, V};

        return NEV;
    }

    public static double[] xyz2gd(double[] xyz) {
        double X, Y, Z, a, f, b, aSq, bSq, eSq, Lon, p, q, Phi0, N0, Lat;
        double Phi = 0, h = 0;

        X = xyz[0];
        Y = xyz[1];
        Z = xyz[2];

        a = 6378137.0;
        f = 1 / 298.257223563;
        b = a * (1.0 - f);

        aSq = Math.pow(a, 2);
        bSq = Math.pow(b, 2);
        eSq = (aSq - bSq) / aSq;

        Lon = Math.atan2(Y, X) * 180 / Math.PI;

        if (Lon > 180) {
            Lon = Lon - 360;
        } else if (Lon < -180) {
            Lon = Lon + 360;
        }

        p = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2));
        q = 0;
        Phi0 = Math.atan2(Z * Math.pow(1 - eSq, -1), p);

        while (q != 1) {
            N0 = aSq / Math.sqrt(aSq * Math.pow(Math.cos(Phi0), 2) + bSq * Math.pow(Math.sin(Phi0), 2));
            h = p / Math.cos(Phi0) - N0;
            Phi = Math.atan2(Z * Math.pow(1 - eSq * (N0 / (N0 + h)), -1), p);

            if (Math.abs(Phi - Phi0) <= 1e-13) {
                break;
            } else {
                Phi0 = Phi;
            }
        }

        Lat = Phi * 180 / Math.PI;

        double[] gd = { Lat, Lon, h };

        return gd;
    }

    public static double[] xyz2azel(double[] xyz, double lat, double lon) {

        double[] topo = xyz2topo(xyz, lat, lon);
        double N = topo[0];
        double E = topo[1];
        double V = topo[2];

        double proj = Math.sqrt(N*N + E*E);
        double el = Math.atan2(V, proj);
        el = Math.toDegrees(el);

        double az = 0;

        double[] azel = {az, el};
        return azel;
    }

    public static int GetRefSat(int[] RealSats, double[] TruePos, double[][] QM1e, double[][] eph) {

        // double[] TruePos = TruePos_Matrix.getData();
        int NoSats1e = RealSats.length;
        int vc = 299792458;
        double[] gd = xyz2gd(TruePos);
        double lat = gd[0]; double lon = gd[1];
        double[] eles = new double[NoSats1e];
        int gs = (int)QM1e[0][0];
        RealMatrix TruePos_Mat = new Array2DRowRealMatrix(TruePos);
        TruePos_Mat = TruePos_Mat.transpose();

        for (int i = 0; i < NoSats1e; i++) {
            int prn = RealSats[i];
            if (prn > 200) continue;
            int icol = PickEPH(eph, prn, gs);
            double STT = GetSTTbrdc(gs, eph, icol, TruePos_Mat);
            double tc = gs - STT;

            RealMatrix vec_sat = GetSatPos(eph, icol, tc);
            RealMatrix vec_rho = GetRhoVec(vec_sat, TruePos_Mat);

            double[] vec_rho_arr = {vec_rho.getData()[0][0], vec_rho.getData()[0][1], vec_rho.getData()[0][2] };

            double el = xyz2azel(vec_rho_arr, lat, lon)[1];
            eles[i] = el;

        }

        int indx = 0;
        double max = eles[0];
        for (int j = 1; j < NoSats1e; j++) {

            if (eles[j] > max) {
                max = eles[j];
                indx = j;
            }

        }

        int ref_sat_prn = (int)QM1e[indx][1];


        return ref_sat_prn;
    }

    public static double GetSTTbrdc(int gs, double[][] eph, int icol, RealMatrix vec_sta){
        int MaxIter = 10;
        double stt_0 = 0.0750;
        double eps = 1e-10;
        double STT;
        double tau_s, com;
        int CCC = 299792458;
        RealMatrix vec_sat;
        for (int i=0; i<MaxIter;i++){
            tau_s = gs - stt_0;
            vec_sat = GetSatPos(eph, icol, tau_s);
            RealMatrix vec_rho = GetRhoVec(vec_sat, vec_sta);
            com = GetNorm(vec_rho);
            STT = com/CCC;
            if (Math.abs(STT-stt_0) < eps) {
                return STT;
            }
            stt_0 = STT;
        }
        return -1;
    }

    public static RealMatrix GetSatPos(double[][] eph, int icol, double gs) {

        final double CCC = 2.99792458E8D;
        final double eps = 1e-10;

        final double MU_GPS = 3.986005e14;
        // final double MU_BDS = 3.986004418e14;
        // final double MU_GAL = 3.986004418e14;

        final double OMEGA_DOT_E_GPS = 7.2921151467e-5;
        // final double OMEGA_DOT_E_BDS = 7.2921150e-5;
        // final double OMEGA_DOT_E_GAL = 7.2921151467e-5;

        // final double SIN_5 = -0.0871557427476582; /* sin(-5.0 deg) */
        // final double COS_5 = 0.9961946980917456; /* cos(-5.0 deg) */

        double toe = eph[icol][0];
        double sqrtA = eph[icol][9];
        double e = eph[icol][10];
        double i0 = eph[icol][11];
        double omega = eph[icol][12];
        double omega0 = eph[icol][13];
        double m0 = eph[icol][14];
        double i_dot = eph[icol][15];
        double omega_dot = eph[icol][16];
        double delta_n = eph[icol][17];
        double C_uc = eph[icol][19];
        double C_us = eph[icol][20];
        double C_rc = eph[icol][21];
        double C_rs = eph[icol][22];
        double C_ic = eph[icol][23];
        double C_is = eph[icol][24];

        double mu = MU_GPS;
        double omge = OMEGA_DOT_E_GPS;

        double a = Math.pow(sqrtA, 2);
        double n0 = Math.sqrt(mu / Math.pow(a, 3));

        double n = n0 + delta_n;

        double t = gs;
        double tk = t - toe;

        double M_k = m0 + n * tk;

        double E_k = ecce_anom(M_k, e, 5);

        double nu_k = Math.atan2(Math.sqrt(1 - Math.pow(e, 2)) * Math.sin(E_k), Math.cos(E_k) - e);

        double Phi_k = nu_k + omega;

        double du_k = C_us * Math.sin(2 * Phi_k) + C_uc * Math.cos(2 * Phi_k);
        double dr_k = C_rs * Math.sin(2 * Phi_k) + C_rc * Math.cos(2 * Phi_k);
        double di_k = C_is * Math.sin(2 * Phi_k) + C_ic * Math.cos(2 * Phi_k);

        double u_k = Phi_k + du_k;
        double r_k = a * (1 - e * Math.cos(E_k)) + dr_k;
        double i_k = i0 + di_k + i_dot * tk;

        double xp_k = r_k * Math.cos(u_k);
        double yp_k = r_k * Math.sin(u_k);

        double Omega_k = omega0 + (omega_dot - omge) * tk - omge * toe;

        // double cosi = Math.cos(i_k);
        double xk = xp_k * Math.cos(Omega_k) - yp_k * Math.cos(i_k) * Math.sin(Omega_k);
        double yk = xp_k * Math.sin(Omega_k) + yp_k * Math.cos(i_k) * Math.cos(Omega_k);
        double zk = yp_k * Math.sin(i_k);

        double[] pos = { xk, yk, zk };
        RealMatrix SatPos = new Array2DRowRealMatrix(pos);
        SatPos = SatPos.transpose();
        return SatPos;
    }

    public static RealMatrix RotSatPos(RealMatrix vec_sat, double STT) {
        double omegaDotE = 7.2921151467e-5;

        double rota = omegaDotE * STT;
        double[][] rotateMat = new double[][] {
                { Math.cos(rota), Math.sin(rota), 0 },
                { -Math.sin(rota), Math.cos(rota), 0 },
                { 0, 0, 1 }
        };
        Array2DRowRealMatrix R_e = new Array2DRowRealMatrix(rotateMat);
        RealMatrix SatPos1 = R_e.multiply(vec_sat.transpose());
        return SatPos1.transpose();
    }

    private static double ecce_anom(double M, double e, int i) {
        double E = M;
        for (int j = 0; j < i; ++j) {
            double fE = M - E + e * Math.sin(E);
            double fpE = -1 + e * Math.cos(E);
            double E_next = E - fE / fpE;
            E = E_next;
        }
        return E;
    }

    public static double GetObs(double[][] QMle, int prn) {

        int QMle_len = QMle.length;
        double obs;

        for (int i = 0; i < QMle_len; i++) {
            if (QMle[i][1] == prn) {
                obs = QMle[i][3];
                return obs;
            }
        }

        return 0;

    }



    public static int PickEPH(double[][] eph, int prn, int gs) {

        // int icol;
        List<Integer> isat = new ArrayList<Integer>();
        for (int i = 0; i < eph.length; i++) {

            if ((int) eph[i][1] == prn)
                isat.add(i);
        }

        int n = isat.size();
        if (n == 0)
            return -1;

        int icol = isat.get(0);

        int dtmin = (int) eph[icol][0] - gs;

        for (int j = 1; j < n; j++) {
            int kk = isat.get(j);
            int dt = (int) eph[kk][0] - gs;
            if (Math.abs(dt) < Math.abs(dtmin)) {
                icol = kk;
                dtmin = dt;
            }
        }

        return icol;
    }


    public static double[] GetError(int gs_r, int gs_b, double[] estm, double[] TruePos) {

        double TrueLat = xyz2gd(TruePos)[0]; double TrueLon = xyz2gd(TruePos)[1];

        double HE = 0;
        double VE = 0;
        double TE = 0;

        double x = estm[0] - TruePos[0];
        double y = estm[1] - TruePos[1];
        double z = estm[2] - TruePos[2];

        double[] dxyz = {x, y, z};
        double N = xyz2topo(dxyz, TrueLat, TrueLon)[0];
        double E = xyz2topo(dxyz, TrueLat, TrueLon)[1];
        double V = xyz2topo(dxyz, TrueLat, TrueLon)[2];

        // double D = Math.sqrt(N*N + E*E + V*V);

        if (gs_r == gs_b) {
            String error = String.format("rover gs: %d\t\t\t\t\t\t\t\t\t\t N: %7.3fm\t E: %7.3fm\t V: %7.3fm\t", gs_b, N, E, V);
            Log.d("NEV_Err_SPP", error);
        }

        else {
            String error = String.format("rover gs: %d\t base gs: %d\t gs diff: %d\t N: %7.3fm\t E: %7.3fm\t V: %7.3fm\t", gs_r, gs_b, gs_r - gs_b, N, E, V);
            Log.d("NEV_Err_DD", error);
        }

        double[] err = {N, E, V};
        return err;

    }


}

