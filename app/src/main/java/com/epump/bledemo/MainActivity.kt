package com.epump.bledemo

import africa.epump.connect.efueling.EfuelingConnect
import africa.epump.connect.efueling.models.TransactionType
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.epump.bledemo.ui.theme.BLEDemoTheme

class MainActivity : ComponentActivity() {
    lateinit var activity: Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this
        enableEdgeToEdge()
        setContent {
            BLEDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        val resultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                data?.let {
                    val sessionId = it.getStringExtra("sessionId").toString()
                    val volume = it.getDoubleExtra("volume", 0.0)
                    val amount = it.getDoubleExtra("amount", 0.0)
                    val transactionValue = it.getDoubleExtra("transactionValue", 0.0)
                    val transactionStarted = it.getBooleanExtra("transactionStarted", false)
                    val transactionDate = it.getLongExtra("transactionDate", 0L)
                    val voucherCardNumber = it.getStringExtra("voucherCardNumber").toString()
                    val transactionCompleted = it.getBooleanExtra("transactionCompleted", false)

                    print("Activity Launcher Result")
                }
            }
        }

        Text(
            text = "Hello $name!",
            modifier = modifier.clickable {
                val connect = EfuelingConnect.getInstance(activity)
                connect.init("", "2101LH95")
                Thread.sleep(1000)
                connect.startTransaction(
                    resultLauncher,
                    transactionType = TransactionType.Offline_Voucher,
                    pumpName = "",
                    pumpDisplayName = "",
                    tag = "",
                    amount = 100.0,
                    callback = null
                )
            }
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        BLEDemoTheme {
            Greeting("Android")
        }
    }
}