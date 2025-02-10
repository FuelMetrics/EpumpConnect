package africa.epump.connect.efueling.ui.activities

import africa.epump.connect.R
import africa.epump.connect.efueling.interfaces.TransactionCallback
import africa.epump.connect.efueling.models.Error
import africa.epump.connect.efueling.models.PumpState
import africa.epump.connect.efueling.models.Transaction
import africa.epump.connect.efueling.models.TransactionState
import africa.epump.connect.efueling.models.TransactionValueType
import africa.epump.connect.efueling.models.Utility.convert2DecimalString
import africa.epump.connect.efueling.ui.theme.BLEDemoTheme
import africa.epump.connect.efueling.ui.theme.MillikTypography
import africa.epump.connect.efueling.ui.theme.SatoshiTypography
import africa.epump.connect.efueling.ui.theme.black
import africa.epump.connect.efueling.ui.theme.gray300
import africa.epump.connect.efueling.ui.theme.gray50
import africa.epump.connect.efueling.ui.theme.gray700
import africa.epump.connect.efueling.ui.theme.green50
import africa.epump.connect.efueling.ui.theme.orange300
import africa.epump.connect.efueling.ui.theme.red500
import africa.epump.connect.efueling.ui.theme.white
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.function.Predicate
import java.util.stream.Collectors


class TransactionActivity : ComponentActivity() {
    private var mCallback: TransactionCallback? = null
    lateinit var settings: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings = this.getSharedPreferences("credentialFile", 0)
        editor = settings.edit()

        enableEdgeToEdge()
        setContent {
            BLEDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box{
                        TransactionPage(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun TransactionPage(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        var showError by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf("") }
        var errorOccurred by remember { mutableStateOf(false) }
        var errorString by remember { mutableStateOf("") }
        var drawableId by remember { mutableIntStateOf(R.drawable.ic_bluetooth) }

        var pumpState by remember { mutableIntStateOf(0) }
        var transactionStateInt by remember { mutableIntStateOf(0) }
        var transactionAck by remember { mutableIntStateOf(0) }
        var completeReported by remember { mutableStateOf(false) }
        var transactionStarted by remember { mutableStateOf(false) }
        var transactionComplete by remember { mutableStateOf(false) }
        var transValue by remember { mutableDoubleStateOf(0.0) }
        var amountSold by remember { mutableDoubleStateOf(0.0) }
        var volumeSold by remember { mutableDoubleStateOf(0.0) }
        var totalizer by remember { mutableDoubleStateOf(0.0) }
        var percentage by remember { mutableIntStateOf(0) }
        var transType by remember { mutableStateOf(0x00.toByte()) }
        var sessionId by remember { mutableStateOf("") }
        var transactionId by remember { mutableStateOf("") }


        var connectionMode by remember { mutableStateOf("") }
        var transactionDate by remember { mutableLongStateOf(0) }
        var pumpName by remember { mutableStateOf("") }
        var pumpDisplayName by remember { mutableStateOf("") }
        var voucherCardNumber by remember { mutableStateOf("") }
        var transactionType by remember { mutableStateOf("") }

        var valueAuthorizedString by remember { mutableStateOf("Amount Authorized") }
        var valueAuthorized by remember { mutableStateOf("") }
        var transactionState by remember { mutableStateOf("Transaction in progress") }
        var currency by remember { mutableStateOf("NGN") }

        LaunchedEffect(true) {
            transactionDate = intent.getLongExtra("Transaction_Date", 0)
            pumpName = intent.getStringExtra("Pump_Name").toString()
            pumpDisplayName = intent.getStringExtra("Pump_Display_Name").toString()
            voucherCardNumber = intent.getStringExtra("voucher_card_number").toString()
            connectionMode = intent.getStringExtra("connection_mode").toString()
            transactionType = intent.getStringExtra("transaction_type").toString()
            currency = intent.getStringExtra("currency").toString()
            val amt = intent.getDoubleExtra("transaction_amount", 0.0)
            valueAuthorized = "$currency ${convert2DecimalString(amt, true)}"

            if (connectionMode.isNotEmpty()){
                if(connectionMode.equals("bluetooth", true)){
                    drawableId = R.drawable.ic_bluetooth
                }
                else if(connectionMode.equals("wifi", true)){
                    drawableId = R.drawable.ic_wifi
                }
            }
        }

        DisposableEffect(context) {
            val receiver = object : BroadcastReceiver(){
                @SuppressLint("DefaultLocale")
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent != null){
                        if (intent.hasExtra("crash_info")){
                            error = intent.getStringExtra("crash_info").toString()

                            if (error.isNotEmpty()){
                                showError = true
                            }
                        }
                        pumpState = intent.getIntExtra("pump_state", 0)
                        transactionStateInt = intent.getIntExtra("transaction_state", 0)
                        errorString = intent.getStringExtra("transaction_error_string").toString()
                        transactionAck = intent.getIntExtra("transaction_acknowledged", 0)
                        if (transactionStateInt == TransactionState.ST_PUMP_AUTH || transactionStateInt == TransactionState.ST_PUMP_FILLING) {
                            completeReported = false
                            transValue =
                                intent.getFloatExtra("transaction_value", 0f)
                                    .toString().toDouble()
                            transType = intent.getByteExtra("transaction_type", 0x00.toByte())
                            sessionId = intent.getStringExtra("transaction_session_id").toString()
                            val saved: ArrayList<Transaction> = Gson().fromJson(
                                settings.getString("E_TRANSACTIONS", "[]"),
                                object : TypeToken<List<Transaction?>?>() {
                                }.type
                            )

                            var found = false
                            val foundTrans: MutableList<Any> =
                                saved.stream().filter(Predicate<Transaction> { c: Transaction ->
                                    c.voucherCardNumber.equals(voucherCardNumber, true)
                                }).collect(Collectors.toList<Any>())
                            found = foundTrans.isNotEmpty()
                            if (!found) {
                                val bckTrans = Transaction(
                                    transactionType,
                                    voucherCardNumber,
                                    amountSold,
                                    volumeSold,
                                    null,
                                    null
                                )
                                bckTrans.isCompleted = false
                                bckTrans.isPresent = true
                                bckTrans.pumpName = pumpName
                                bckTrans.totalizer = totalizer
                                saved.add(bckTrans)
                                editor.putString("E_TRANSACTIONS", Gson().toJson(saved))
                                editor.apply()
                            }
                        }
                        if (transactionStateInt == TransactionState.ST_PUMP_FILLING || transactionStateInt == TransactionState.ST_PUMP_FILL_COMP) {
                            amountSold = intent.getFloatExtra("amount_sold", 0f)
                                .toString().toDouble()
                            volumeSold = intent.getFloatExtra("volume_sold", 0f)
                                .toString().toDouble()
                            transactionStarted = true
                            if (transType.toInt() == TransactionValueType.Amount.ordinal) {
                                if (amountSold >= transValue) {
                                    transactionComplete = true
                                }
                                if (transValue > 0 && amountSold > 0) {
                                    percentage = ((amountSold / transValue) * 100).toInt()
                                }
                            } else if (transType.toInt() == TransactionValueType.Volume.ordinal) {
                                if (volumeSold >= transValue) {
                                    transactionComplete = true
                                }
                                if (transValue > 0 && volumeSold > 0) {
                                    percentage = ((volumeSold / transValue) * 100).toInt()
                                }
                            }

                            if (transactionStateInt == TransactionState.ST_PUMP_FILL_COMP) {
                                transactionComplete = true
                            }
                        }

                        if (transactionComplete && !completeReported) {
                            totalizer =
                                intent.getFloatExtra("transaction_tz", 0f)
                                    .toString().toDouble()
                            transactionId = intent.getStringExtra("transaction_id").toString()
                            completeReported = true

                            val completeIntent = Intent("trans_complete")
                            completeIntent.putExtra("amount", amountSold)
                            completeIntent.putExtra("volume", volumeSold)
                            completeIntent.putExtra("session_id", sessionId)
                            completeIntent.putExtra("transaction_id", transactionId)
                            completeIntent.putExtra("transaction_tz", totalizer)
                            completeIntent.putExtra("transaction_type", transactionType)
                            completeIntent.putExtra("pump_name", pumpName)
                            completeIntent.putExtra("voucher_card_number", voucherCardNumber)
                            LocalBroadcastManager.getInstance(context!!)
                                .sendBroadcastSync(completeIntent)
                        }

                        runOnUiThread {
                            if (transType.toInt() == TransactionValueType.Amount.ordinal) {
                                valueAuthorizedString = "Amount Authorized"
                            } else if (transType.toInt() == TransactionValueType.Volume.ordinal) {
                                valueAuthorizedString = "Volume Authorized"
                            }
                            if (transactionStateInt == TransactionState.ST_PUMP_AUTH
                                || transactionStateInt == TransactionState.ST_PUMP_FILLING
                                || transactionStateInt == TransactionState.ST_PUMP_FILL_COMP) {

                                val value = convert2DecimalString(
                                    transValue,
                                    false
                                )
                                if (transType.toInt() == TransactionValueType.Amount.ordinal) {
                                    valueAuthorized = "$currency $value"
                                } else if (transType.toInt() == TransactionValueType.Volume.ordinal) {
                                    valueAuthorized = "$value Ltrs"
                                }
                            }

                            transactionState =
                                TransactionState.getString(
                                    transactionStateInt,
                                    pumpDisplayName
                                )
                            val pState = PumpState.getString(pumpState)

                            if (transactionStateInt == TransactionState.ST_ERROR
                                || transactionStateInt == TransactionState.ST_LIB_ERROR
                                || transactionStateInt == TransactionState.ST_PUMP_BUSY
                                /*|| transactionState == TransactionState.ST_IDLE*/) {
                                if (transactionStateInt == TransactionState.ST_PUMP_BUSY) {
                                    transactionState = pState
                                } else {
                                    transactionState = Error.getError(errorString)
                                }

                                errorOccurred = true
                            }
                            else{
                                errorOccurred = false
                            }

                            if (transactionAck == 1) {
                                mCallback?.onStarted()
                            }

                            if (transactionStateInt == TransactionState.ST_PUMP_AUTH &&
                                (pumpState == PumpState.NOZZLE_HANG_UP
                                        || pumpState == PumpState.PUMP_AUTH_NOZZLE_HANG_UP)
                            ) {
                                transactionState = PumpState.getString(pumpState)
                                mCallback?.onStarted()
                            }
                        }
                    }
                }
            }

            val intentFilter = IntentFilter("get_states")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.registerReceiver(receiver, intentFilter, RECEIVER_NOT_EXPORTED)
            }
            else{
                context.registerReceiver(receiver, intentFilter)
            }

            onDispose {
                context.unregisterReceiver(receiver)
            }
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .background(white)){
            Box(modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)){
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(Modifier.weight(1f))
                    Column(verticalArrangement = Arrangement.spacedBy(36.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .background(white, RoundedCornerShape(12.dp))
                                .border(1.dp, green50, RoundedCornerShape(12.dp))
                                .padding(vertical = 24.dp))
                        {
                            Text(
                                text = valueAuthorizedString.uppercase(),
                                modifier = Modifier.fillMaxWidth(),
                                color = black,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                style = SatoshiTypography.labelSmall,
                            )

                            Text(
                                text = valueAuthorized,
                                modifier = Modifier.fillMaxWidth(),
                                color = gray700,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Normal,
                                style = MillikTypography.headlineMedium,
                            )
                        }

                        Box(modifier = Modifier
                            .size(84.dp)
                            .align(alignment = Alignment.CenterHorizontally)) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(84.dp)
                                    .align(Alignment.Center),
                                strokeWidth = 5.dp,
                                color = orange300,
                                trackColor = gray50,
                            )

                            Box(
                                Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center)
                                    .background(gray50, CircleShape)){
                                Text(
                                    text = "$percentage%",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center),
                                    color = orange300,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Normal,
                                    style = MillikTypography.labelSmall,
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier)
                        {
                            Text(
                                text = transactionState,
                                modifier = Modifier.fillMaxWidth(),
                                color = black,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Normal,
                                style = MillikTypography.labelLarge,
                            )

                            Text(
                                text = "Car engine should be turned off while \n" +
                                        "fueling is on-going",
                                modifier = Modifier.fillMaxWidth(),
                                color = black,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Normal,
                                style = SatoshiTypography.labelMedium,
                            )
                        }
                    }


                    Spacer(Modifier.weight(1f))
                }

                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = "connection mode - $connectionMode",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 80.dp)
                        .size(100.dp)
                        .alpha(0.6f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 20.dp)
                        .background(gray50, RoundedCornerShape(8.dp))
                        .border(0.5.dp, gray300, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable {
                            var return_value = 0
                            val returnData = Intent()
                            returnData.putExtra("sessionId", sessionId)
                            returnData.putExtra("volume", volumeSold)
                            returnData.putExtra("amount", amountSold)
                            returnData.putExtra("transactionValue", transValue)
                            returnData.putExtra("transactionStarted", transactionStarted)
                            returnData.putExtra("transactionDate", transactionDate)
                            returnData.putExtra("voucherCardNumber", voucherCardNumber)
                            returnData.putExtra("transactionCompleted", transactionComplete)
                            if (transactionComplete || errorOccurred) {
                                return_value = -1
                            }

                            mCallback?.onCompleted(return_value, returnData)

                            setResult(return_value, returnData)
                            finish()
                        })
                {
                    Image(
                        painter = painterResource(id = R.drawable.ic_close_circle),
                        contentDescription = "close page",
                        modifier = Modifier
                    )

                    Text(
                        text = "Close page",
                        modifier = Modifier,
                        color = gray700,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = SatoshiTypography.labelLarge,
                    )
                }

                if (showError) {
                    Column {
                        Spacer(Modifier.weight(1f))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .background(red500)
                                .padding(16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Error",
                                    modifier = Modifier,
                                    color = white,
                                    textAlign = TextAlign.Start,
                                    fontWeight = FontWeight.SemiBold,
                                    style = SatoshiTypography.labelLarge,
                                )

                                Image(
                                    painter = painterResource(id = R.drawable.ic_close_circle),
                                    contentDescription = "close page",
                                    colorFilter = ColorFilter.tint(white),
                                    modifier = Modifier.clickable {
                                        showError.not()
                                    }
                                )
                            }

                            Text(
                                text = error,
                                modifier = Modifier.fillMaxWidth(),
                                color = white,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Normal,
                                style = SatoshiTypography.labelMedium,
                            )
                        }
                    }
                }
            }
        }
    }

    fun setCallback(callback: TransactionCallback?) {
        mCallback = callback
    }


    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        BLEDemoTheme {
            TransactionPage()
        }
    }
}