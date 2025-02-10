package africa.epump.connect.efueling.interfaces

import android.content.Intent

public interface TransactionCallback {
    fun onStarted()
    fun onCompleted(resultCode: Int, intent: Intent)
}