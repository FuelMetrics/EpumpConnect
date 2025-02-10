package com.fuelmetrics.epumpwifitool;

/** This java source file connects the native c++ source to java
 * i.e. the functions are merely declared here, but are defined in
 * the c++ source file
 * it serves as the Header file of the "native-lib.cpp" source */
public class NativeLibJava {
    JNICallbackInterface cbInterface;

    static {
        System.loadLibrary("native-lib");
    }

    public NativeLibJava(JNICallbackInterface cbi){
        this.cbInterface = cbi;
    }

    /** -------------------------
     * User on_child_event methods
     *
     * See the ep_interface.h file for
     * each API description
     * ------------------------- */
    public native int ep_init(String did, String dk);
    public native int ep_deinit();
    public native int ep_run();
    public native void ep_ms_timer();
    public native int ep_get_time_int(int ss, int mm, int hh,
                                      int dd, int mn, int yy);
    public native int ep_start_trans(String pname, int transTy,
                                     String uid, byte val_ty,
                                     float value, int time_int, String term_id);

    public native int ep_end_trans();
    public native int ep_get_cur_state();
    public native String ep_get_cur_state_string();
    public native int ep_get_pump_state();
    public native float ep_get_vol_sold();
    public native float ep_get_amo_sold();
    public native float ep_get_value();
    public native byte ep_get_value_ty();
    public native String ep_get_err_details();
    public native String ep_get_session_id();
    public native String ep_get_transaction_id();
    public native float ep_read_totalizer();
    public native String get_totaliser_s();
    public native int ep_is_command_acked();

    public native int ep_rx_data(String data, int len);

    public native Object ep_get_connection_data(String pumpname);

    /** APIs for getting previous transaction and
     * reading out each variable */
    public native int ep_get_transaction(int index, int log_ty, String pumpname); //returns index read out
    public native byte ep_read_trans_ty();
    public native String ep_read_trans_uid();
    public native double ep_read_trans_value();
    public native byte ep_read_trans_value_ty();
    public native double ep_read_trans_vol();
    public native double ep_read_trans_ini_tot();
    public native double ep_read_trans_final_tot();
    public native long ep_read_trans_time();
    public native String ep_read_trans_pumpName();

    /** for registering callback */
    public native int registerCallbacks();
    private void ep_trans_cb_java(String data, int len){
        this.cbInterface.tx_data(data, len);
    }

}

