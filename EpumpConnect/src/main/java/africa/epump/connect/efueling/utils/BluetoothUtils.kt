package africa.epump.connect.efueling.utils

import africa.epump.connect.efueling.interfaces.BluetoothUtilsCallback
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.UUID


class BluetoothUtils(
    private val mContext: Activity,
    private val mMacAddr: String?,
    private val mUtilsCallback: BluetoothUtilsCallback
) {
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mLEScanner: BluetoothLeScanner? = null
    private var mGatt: BluetoothGatt? = null
    private var settings: ScanSettings? = null
    private var filters: ArrayList<ScanFilter>? = null
    private var readCharacteristics: BluetoothGattCharacteristic? = null
    private var writeCharacteristics: BluetoothGattCharacteristic? = null
    private var stopScan = false
    private var bluetoothFound = false
    private var resp = false

    fun bLESupported(): Boolean {
        return mContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    fun startBLE() {
        if (mMacAddr.isNullOrEmpty()) {
            return
        }
        val bluetoothManager =
            mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            mContext.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        else {
            if (ActivityCompat.checkSelfPermission(
                    this.mContext,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mBluetoothAdapter!!.startDiscovery()
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter!!.bluetoothLeScanner
                settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()
                filters = ArrayList()
            }
            startScan()
        }
    }

    fun stopScan() {
        if (!stopScan) {
            if (mLEScanner != null) {
                if (ActivityCompat.checkSelfPermission(
                        this.mContext,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mLEScanner!!.stopScan(mScanCallback)
            }
            stopScan = true
        }
    }

    /*To be called on Stop*/
    fun closeGatt() {
        stopScan()
        if (mGatt != null) {
            if (ActivityCompat.checkSelfPermission(
                    this.mContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mGatt!!.close()
            mGatt!!.disconnect()
            mGatt = null
        }
    }

    fun write(data: String?) {
        if (mBluetoothAdapter != null && mGatt != null) {
            //mGatt.beginReliableWrite();
            if (writeCharacteristics != null && data != null) {
                writeCharacteristics!!.setValue(data.toByteArray())
                if (ActivityCompat.checkSelfPermission(
                        this.mContext,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                if (!mGatt!!.writeCharacteristic(writeCharacteristics)) {
                    Log.e(
                        "TAG",
                        "Failed to write characteristics: " + writeCharacteristics.toString()
                    )
                }
            }
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (mGatt == null) {
            if (ActivityCompat.checkSelfPermission(
                    this.mContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mGatt =  device.connectGatt(mContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        }
    }

    private fun startScan() {
        if (ActivityCompat.checkSelfPermission(
                this.mContext,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mLEScanner!!.startScan(filters, settings, mScanCallback)

        Handler(Looper.getMainLooper()).postDelayed(Runnable { stopScan() }, 20000)
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            /*Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());*/

            if (ActivityCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }

            val btDevice: BluetoothDevice = result.getDevice()
            val devName = btDevice.name
            val devAddress = btDevice.address // result.getScanRecord().getDeviceName();

            if (!bluetoothFound && !stopScan) {
                if ((devName != null && devName.equals(mMacAddr, ignoreCase = true))
                    || (devAddress != null && devAddress.equals(mMacAddr, ignoreCase = true))
                ) {
                    bluetoothFound = true
                    connectToDevice(btDevice)
                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>) {
            for (sr in results) {
                Log.i("ScanResult - Results", sr.toString())
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("Scan Failed", "Error Code: $errorCode")
        }
    }

    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (ActivityCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */

                gatt.discoverServices()
            }
            else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                /*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 */
                gatt.close()
                gatt.disconnect()
                val intent = Intent("init_complete")
                intent.putExtra("status", false)
                LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent)
            }
            else if (status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If there is a failure at any stage, simply disconnect
                 */
                gatt.close()
                gatt.disconnect()
                val intent = Intent("init_complete")
                intent.putExtra("status", false)
                LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            //super.onServicesDiscovered(gatt, status);
            if (ActivityCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val gattService = gatt.getService(SERVICE_UUID)
                if (gattService != null) {
                    readCharacteristics = gattService.getCharacteristic(READ_XTC_UUID)

                    if (readCharacteristics != null) {
                        /*if(!gatt.setCharacteristicNotification(readCharacteristics, true))
                        {
                            Log.e("TAG", "Failed to set notification for: " + readCharacteristics.toString());
                        }*/
                        gatt.setCharacteristicNotification(readCharacteristics, true)
                        // Enable notification descriptor
                        val CCC_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                        val descriptor = readCharacteristics!!.getDescriptor(CCC_UUID)
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                            gatt.writeDescriptor(descriptor)
                        }

                        Handler(Looper.getMainLooper()).post(Runnable { mUtilsCallback.onConnected() })

                        val intent = Intent("init_complete")
                        intent.putExtra("status", true)
                        LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent)
                    }

                    var trials = 5
                    do {
                        try {
                            mGatt!!.requestMtu(512)
                            Log.i("TAG", "onTick: $resp mtu - $trials")
                            Thread.sleep(100)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    } while (!resp && trials-- > 0)
                }
                stopScan()
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic.value != null) {
                val value = characteristic.value.toString()
                Log.i("TAG", "onCharacteristicRead: $value")
                mUtilsCallback.onRead(value)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            //super.onCharacteristicWrite(gatt, characteristic, status);
            /*if(characteristic.getValue() != null) {
                if (!gatt.executeReliableWrite()){
                    Log.e("TAG", "Failed to reliable write characteristics: " + characteristic.toString());
                }
            }*/
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            val value = String(characteristic.value)
            Log.i("TAG", "onCharacteristicChanged: $value")
            mUtilsCallback.onRead(value)
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            writeCharacteristics = gatt.getService(SERVICE_UUID).getCharacteristic(WRITE_XTC_UUID)
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
            super.onReliableWriteCompleted(gatt, status)
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.i("TAG", "onMtuChanged: $status, mtu: $mtu")
            resp = true
        }
    }

    companion object {
        const val REQUEST_ENABLE_BT: Int = 516
        private val SERVICE_UUID: UUID = UUID.fromString("0000a002-0000-1000-8000-00805f9b34fb")
        private val READ_XTC_UUID: UUID = UUID.fromString("0000c305-0000-1000-8000-00805f9b34fb")
        private val WRITE_XTC_UUID: UUID = UUID.fromString("0000c304-0000-1000-8000-00805f9b34fb")
    }
}