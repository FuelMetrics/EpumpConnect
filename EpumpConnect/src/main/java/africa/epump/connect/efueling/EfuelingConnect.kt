package africa.epump.connect.efueling

import africa.epump.connect.efueling.interfaces.BluetoothUtilsCallback
import africa.epump.connect.efueling.interfaces.IData
import africa.epump.connect.efueling.interfaces.TransactionCallback
import africa.epump.connect.efueling.models.Ep_Run
import africa.epump.connect.efueling.models.GO_TransactionType
import africa.epump.connect.efueling.models.Transaction
import africa.epump.connect.efueling.models.TransactionType
import africa.epump.connect.efueling.models.TransactionValueType
import africa.epump.connect.efueling.models.Utility
import africa.epump.connect.efueling.ui.activities.TransactionActivity
import africa.epump.connect.efueling.utils.BluetoothUtils
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fuelmetrics.epumpwifitool.JNICallbackInterface
import com.fuelmetrics.epumpwifitool.NativeLibJava
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.Calendar
import java.util.Date


class EfuelingConnect private constructor(private val mContext: Activity?) : JNICallbackInterface {
    var nativeLibJava: NativeLibJava? = null
    private val data_interface: IData? = null
    private var wifiManager: WifiManager? = null
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: NetworkCallback? = null
    private var mBluetoothUtils: BluetoothUtils? = null
    private var output: PrintWriter? = null
    private var socket: Socket? = null
    private var countDownTimer: CountDownTimer? = null
    private var messageCountDownTimer: CountDownTimer? = null
    private var wifiAvailability = -1
    private var disposed = false
    private var activity: Activity? = null
    private var mDailyKey = ""
    private var mTerminalId = ""
    private var transactionDate: Date? = null
    private var connectionTrial = 0
    private var thread: Thread? = null
    private var epRun: Thread? = null
    private var handler: Handler? = null
    private var connectionMode: String? = null

    @JvmOverloads
    fun init(dailyKey: String, terminalId: String = "") {
        mDailyKey = dailyKey
        if (terminalId.isNotEmpty()) {
            mTerminalId = terminalId
        }
        if (disposed) {
            disposed = false
        }
        /*if (nativeLibJava != null){
            nativeLibJava.ep_end_trans();
        }*/
        nativeLibJava = NativeLibJava(this)
        /*data_interface = (IData) mContext;*/
    }

    override fun tx_data(data: String, len: Int) {
        if (mBluetoothUtils != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBluetoothUtils!!.write(data)
            }
        } else if (output != null) {
            output!!.println(data)
            output!!.flush()
        }

        /*Intent intent = new Intent("wifi_state");
        intent.putExtra("pump_state", nativeLibJava.ep_get_pump_state());
        intent.putExtra("transaction_state", nativeLibJava.ep_get_cur_state());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        data_interface.tx_data(data, len);*/
    }

    /*WiFi methods*/
    fun turnWifi(state: Boolean) {
        connectionMode = "wifi"
        Thread {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            wifiManager =
                activity!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            //if (!state) {
            if (ActivityCompat.checkSelfPermission(
                    mContext!!,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val list = wifiManager!!.configuredNetworks
                if (list != null) {
                    for (i in list) {
                        wifiManager!!.disableNetwork(i.networkId)
                        wifiManager!!.removeNetwork(i.networkId)
                    }
                }
                wifiManager!!.saveConfiguration()
            }
            //}
            if (wifiManager!!.isWifiEnabled != state) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val panelIntent: Intent =
                        Intent(Settings.Panel.ACTION_WIFI)
                    (mContext as Activity?)!!.startActivityForResult(
                        panelIntent,
                        223
                    )
                } else {
                    wifiManager!!.setWifiEnabled(false)
                    wifiManager!!.setWifiEnabled(state)
                }
            }
        }.start()
    }

    fun wifiEnabled(): Boolean {
        wifiManager =
            activity!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager!!.isWifiEnabled
    }

    fun disconnectCurrentNetwork(): Boolean {
        if (wifiManager != null && wifiManager!!.isWifiEnabled) {
            val netId = wifiManager!!.connectionInfo.networkId
            wifiManager!!.disableNetwork(netId)
            return wifiManager!!.disconnect()
        }
        return false
    }

    fun connect2WifiAndSocket(ssid: String, password: String, ipAddress: String?) {
        do {
            Log.i("TAG", "run: switching wifi on")
        } while (!wifiEnabled())

        disconnectCurrentNetwork()

        val networkRequest: NetworkRequest

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build()

            networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(wifiNetworkSpecifier).build()
        } else {
            val wifiConfig = WifiConfiguration()
            wifiConfig.SSID = "\"" + ssid + "\""
            wifiConfig.preSharedKey = "\"" + password + "\""
            wifiConfig.priority = 1000
            val netId = wifiManager!!.addNetwork(wifiConfig)
            wifiManager!!.disconnect()
            wifiManager!!.enableNetwork(netId, true)
            wifiManager!!.reconnect()
            networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
        }
        var cnt = true
        if (!Settings.System.canWrite(mContext)) {
            cnt = false
            val goToSettings: Intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            goToSettings.setData(Uri.parse("package:" + mContext!!.packageName))
            (mContext as Activity?)!!.startActivityForResult(
                goToSettings,
                EP_SETTINGS_REQUEST_CODE
            )
        }
        if (cnt) {
            connectivityManager =
                mContext!!.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback = object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    val info = wifiManager!!.connectionInfo
                    var connectedSSID = info.ssid
                    if (connectedSSID != null) {
                        connectedSSID = connectedSSID.replace("\"".toRegex(), "")
                    }
                    if (connectedSSID == ssid) {
                        connectivityManager!!.bindProcessToNetwork(network)
                        if (!Utility.ConnectionStarted) {
                            Utility.ConnectionStarted = true
                            handleConnect(ipAddress!!)
                        }
                    } else {
                        Utility.ConnectionStarted = false
                        Log.i("TAG", "onAvailable: wrong ssid connection")
                    }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    Utility.ConnectionStarted = false
                }

                override fun onLost(network: Network) {
                    if (!disposed) {
                        wifiAvailability = 2
                        super.onLost(network)
                        Utility.ConnectionStarted = false

                        val intent = Intent("init_complete")
                        intent.putExtra("status", false)
                        LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent)
                        /*if (data_interface != null){
                            data_interface.initComplete(false);
                        }*/
                    }
                }

                override fun onUnavailable() {
                    wifiAvailability = 1
                    super.onUnavailable()
                    Utility.ConnectionStarted = false
                }
            }
            connectivityManager!!.requestNetwork(networkRequest,
                networkCallback as NetworkCallback
            )
        }
    }

    private fun handleConnect(vararg ipAddress: String) {
        handler = Handler(Looper.getMainLooper())
        wifiAvailability = 0

        nativeLibJava!!.registerCallbacks()
        nativeLibJava!!.ep_init("", mDailyKey)

        countDownTimer = object : CountDownTimer(60000, 100) {
            override fun onTick(l: Long) {
                nativeLibJava!!.ep_ms_timer()
            }

            override fun onFinish() {
                start()
            }
        }
        countDownTimer?.start()

        messageCountDownTimer = object : CountDownTimer(60000, 500) {
            override fun onTick(l: Long) {
                val pumpState = nativeLibJava!!.ep_get_pump_state()
                val transState = nativeLibJava!!.ep_get_cur_state()
                val transError = nativeLibJava!!.ep_get_err_details()
                val volume = nativeLibJava!!.ep_get_vol_sold()
                val amount = nativeLibJava!!.ep_get_amo_sold()
                val transValue = nativeLibJava!!.ep_get_value()
                var totalizer = nativeLibJava!!.ep_read_totalizer()


                try {
                    val totlz = nativeLibJava!!._totaliser_s
                    Log.i("TAG", "readTransactions: $totlz")
                    if (!totlz.isEmpty()) {
                        totalizer = totlz.toDouble().toFloat()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val transType = nativeLibJava!!.ep_get_value_ty()
                val transSessionId = nativeLibJava!!.ep_get_session_id()
                val transactionId = nativeLibJava!!.ep_get_transaction_id()
                val transactionAck = nativeLibJava!!.ep_is_command_acked()

                val intent = Intent("get_States")
                try {
                    intent.putExtra("pump_state", pumpState)
                    intent.putExtra("transaction_state", transState)
                    intent.putExtra("transaction_error_string", transError)
                    intent.putExtra("volume_sold", volume)
                    intent.putExtra("amount_sold", amount)
                    intent.putExtra("transaction_value", transValue)
                    intent.putExtra("transaction_type", transType)
                    intent.putExtra("transaction_session_id", transSessionId)
                    intent.putExtra("transaction_id", transactionId)
                    intent.putExtra("transaction_acknowledged", transactionAck)
                    intent.putExtra("transaction_tz", totalizer)
                    intent.putExtra("crash_info", "")
                }
                catch (e: Exception){
                    intent.putExtra("crash_info", e.toString() + " - " + e.message)
                }
                LocalBroadcastManager.getInstance(mContext!!).sendBroadcastSync(intent)
                //LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }

            override fun onFinish() {
                start()
            }
        }
        messageCountDownTimer?.start()

        if (!runCalled) {
            runCalled = true
            /*if (executor == null || executor.isShutdown()){
                executor = Executors.newSingleThreadExecutor();
            }
            epRunFuture =  executor.submit(new Ep_Run(nativeLibJava));*/
            epRun = Thread(Ep_Run(nativeLibJava!!))
            epRun!!.start()
        }
        if (ipAddress.size > 0) {
            socketConnection(ipAddress[0])
        }
    }

    private fun socketConnection(ip: String) {
        val runnable = Runnable {
            try {
                socket = Socket(ip, 5555)
                /*socket.setKeepAlive(true);*/
                val out = socket!!.getOutputStream()

                output = PrintWriter(out)

                do {
                    Log.d("TAG", "run: connecting")
                } while (!socket!!.isBound && !socket!!.isConnected)
                /*if (data_interface != null){
                             data_interface.initComplete(true);
                         }*/
                val intent = Intent("init_complete")
                intent.putExtra("status", true)
                LocalBroadcastManager.getInstance(mContext!!).sendBroadcastSync(intent)

                while (socket != null && socket!!.isBound && socket!!.isConnected && !socket!!.isClosed) {
                    val input = BufferedReader(
                        InputStreamReader(
                            socket!!.getInputStream()
                        )
                    )
                    val st = input.readLine()

                    pushDataToLib(st)
                }

                if (socket != null && socket!!.isClosed) {
                    if (connectionTrial < 3) {
                        connectionTrial++
                        socketConnection(ip)
                    }
                }
            } catch (e: IOException) {
                //e.printStackTrace();
            }
        }
        /*if (executor == null || executor.isShutdown()){
            executor = Executors.newSingleThreadExecutor();
        }
        socketFuture = executor.submit(runnable);*/
        thread = Thread(runnable)
        thread!!.start()
    }

    /*WiFi methods ended*/
    private fun pushDataToLib(dataToSend: String?) {
        handler?.post(Runnable {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            if (dataToSend != null) {
                nativeLibJava!!.ep_rx_data(dataToSend, dataToSend.length)
            }
        })
    }

    /*BLE methods*/
    fun initBluetooth(macAddress: String?): Boolean {
        connectionMode = "bluetooth"
        mBluetoothUtils = BluetoothUtils(mContext!!, macAddress, object : BluetoothUtilsCallback {
            override fun onConnected() {
                handleConnect()
            }

            override fun onRead(data: String) {
                pushDataToLib(data)
            }
        })
        return mBluetoothUtils!!.bLESupported()
    }

    fun startBLE() {
        if (mBluetoothUtils != null) {
            mBluetoothUtils!!.startBLE()
        }
    }

    /*To be called on Stop*/
    fun closeGatt() {
        mBluetoothUtils!!.closeGatt()
    }

    /*BLE methods ended*/
    fun startTransaction(
        launcher: ActivityResultLauncher<Intent>,
        transactionType: TransactionType, pumpName: String?, pumpDisplayName: String?,
        tag: String?, amount: Double, currency: String = "NGN", callback: TransactionCallback?
    ) {
        disposed = false
        if (wifiAvailability <= 0) {
            val calendar: Calendar = Calendar.getInstance()
            transactionDate = calendar.time
            Thread {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
                var yy: Int = calendar.get(Calendar.YEAR)
                val mon: Int = calendar.get(Calendar.MONTH)
                val dd: Int = calendar.get(Calendar.DATE)
                val hh: Int = calendar.get(Calendar.HOUR)
                val mm: Int = calendar.get(Calendar.MINUTE)
                val ss: Int = calendar.get(Calendar.SECOND)
                yy = yy - 2000
                val time = nativeLibJava!!.ep_get_time_int(ss, mm, hh, dd, mon, yy)
                nativeLibJava!!.ep_start_trans(
                    pumpName,
                    transactionType.ordinal,
                    tag,
                    TransactionValueType.Amount.ordinal.toByte(),
                    amount.toFloat(),
                    time,
                    mTerminalId
                )
            }.start()

            val intent = Intent(
                mContext,
                TransactionActivity::class.java
            )
            intent.putExtra("Transaction_Date", transactionDate?.time)
            intent.putExtra("Pump_Name", pumpName)
            intent.putExtra("Pump_Display_Name", pumpDisplayName)
            intent.putExtra("voucher_card_number", tag)
            intent.putExtra("connection_mode", connectionMode)
            intent.putExtra("transaction_amount", amount)
            intent.putExtra("currency", currency)
            intent.putExtra("transaction_type", transactionType.label)

            launcher.launch(intent)
            //mContext!!.startActivityForResult(intent, TRANSACTION_START)

//            try {
//                TransactionActivity.setCallback(callback)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
        }
    }

    fun continueTransaction(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(mContext, TransactionActivity::class.java)
        launcher.launch(intent)
        //mContext!!.startActivityForResult(intent, TRANSACTION_START)
    }

    fun dispose() {
        if (!disposed) {
            disposed = true
            Utility.ConnectionStarted = false
            try {
                if (networkCallback != null && connectivityManager != null) {
                    connectivityManager!!.unregisterNetworkCallback(networkCallback!!)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            try {
                if (thread != null) {
                    thread!!.interrupt()
                }
                if (epRun != null) {
                    epRun!!.interrupt()
                }

                if (countDownTimer != null) {
                    countDownTimer!!.cancel()
                    countDownTimer = null
                }

                if (messageCountDownTimer != null) {
                    messageCountDownTimer!!.cancel()
                    messageCountDownTimer = null
                }
                if (nativeLibJava != null) {
                    nativeLibJava!!.ep_end_trans()
                    nativeLibJava!!.ep_deinit()
                }
                runCalled = false
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            try {
                if (socket != null && !socket!!.isClosed) {
                    socket!!.close()
                    output!!.close()
                }

                socket = null
                output = null
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (mBluetoothUtils != null) {
                mBluetoothUtils!!.closeGatt()
                mBluetoothUtils = null
            } else if (connectionMode.equals("wifi", ignoreCase = true)) {
                turnWifi(false)
            }
            _connect = null
            //data_interface = null;
        }
    }

    fun stopTransaction() {
        if (nativeLibJava != null) {
            nativeLibJava!!.ep_end_trans()
        }
    }

    private var readTrials = 0

    init {
        if (mContext != null) {
            this.activity = mContext
        }
    }

    fun readTransactions(
        count: Int,
        pumpName: String?,
        transactionMode: Int
    ): ArrayList<Transaction> {
        val myTransactions: ArrayList<Transaction> = ArrayList<Transaction>()
        try {
            var counter = 0
            var resp: Int
            while (counter < count) {
                resp = nativeLibJava!!.ep_get_transaction(counter, transactionMode, pumpName)
                if (resp != 0) {
                    if (resp == 20 && readTrials < 2) {
                        readTrials++
                        return readTransactions(count, pumpName, transactionMode)
                    } else {
                        break
                    }
                } else {
                    val name = nativeLibJava!!.ep_read_trans_pumpName()
                    if (name.equals(pumpName, ignoreCase = true)) {
                        Log.i("TAG", "readTransactions: ------------------------------------------")
                        val transType = nativeLibJava!!.ep_read_trans_ty()
                        val voucherCardNumber = nativeLibJava!!.ep_read_trans_uid()
                        val transAmount = nativeLibJava!!.ep_read_trans_value()

                        //byte transValueType = nativeLibJava.ep_read_trans_value_ty();
                        val transVolume = nativeLibJava!!.ep_read_trans_vol()
                        val transTime = nativeLibJava!!.ep_read_trans_time()
                        val totalizer = nativeLibJava!!.ep_read_trans_final_tot()

                        if (transType < GO_TransactionType.Last.ordinal) {
                            val transactionType = GO_TransactionType.get(transType.toInt())
                            //String transactionValueType = TransactionValueType.get(transValueType);
                            val tr: Transaction = Transaction(
                                transactionType,
                                voucherCardNumber,
                                transAmount,
                                transVolume,
                                transTime.toString(),
                                null
                            )
                            tr.isCompleted = true
                            tr.totalizer = totalizer
                            tr.pumpName = pumpName
                            myTransactions.add(tr)
                        }
                    }
                }
                counter++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        readTrials = 0
        return myTransactions
    }

    companion object {
        const val TRANSACTION_START: Int = 213
        const val EP_SETTINGS_REQUEST_CODE: Int = 64

        @SuppressLint("StaticFieldLeak")
        private var _connect: EfuelingConnect? = null
        private var runCalled = false
        fun getInstance(context: Activity?): EfuelingConnect {
            /*if (_connect == null) {
                _connect = new EfuelingConnect(context);
            }
            return _connect;*/
            return EfuelingConnect(context)
        }
    }
}