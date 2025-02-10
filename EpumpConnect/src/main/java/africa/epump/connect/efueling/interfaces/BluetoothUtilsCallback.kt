package africa.epump.connect.efueling.interfaces

public interface BluetoothUtilsCallback {
    fun onConnected()
    fun onRead(data: String)
}