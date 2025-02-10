package africa.epump.connect.efueling.models

import android.util.Log
import java.util.Calendar
import java.util.Locale


object Error {
    fun getError(error: String): String {
        var error = error
        var errorString = ""
        if (error.startsWith("ERROR[")) {
            error = error.substring(6, error.length - 1)
        }
        error = error.trim { it <= ' ' }
        if (error.uppercase(Locale.getDefault()).contains("NULL")) {
            return errorString
        }
        val err = error.split(":".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val errorType = err[0]
        Log.i("TAG", "getError: $error")
        val errorCode = if (err.size > 1) err[1].toInt() else -1
        val errorMessageCode = if (err.size > 2) err[2].toInt() else -1
        if (errorType.equals(ErrorType.G.name, ignoreCase = true)) {
            errorString = if (errorCode == GoErrorType.EVT_SERVER_ERROR.ordinal) {
                ServerErrorType.getString(errorMessageCode)
            } else if (errorCode == GoErrorType.EVT_SOCKET_ERROR.ordinal) {
                "Network Error"
            } else if (errorCode == GoErrorType.EVT_VALUE_IS_ZERO.ordinal) {
                "Value cannot be zero"
            } else if (errorCode == GoErrorType.EVT_VALUE_IS_TOO_LOW.ordinal) {
                "Transaction value too low"
            } else {
                "Unknown Error"
            }
        } else if (errorType.equals(ErrorType.L.name, ignoreCase = true)) {
            errorString = "Library - " + LibraryErrorType.getString(errorCode)
        }
        val time: String =
            Utility.parseDate(Calendar.getInstance().getTime(), "EEE MMM dd, yyyy hh:mm aa")
        return "$errorString - $error\n$time"
    }
}

enum class ErrorType{
    L, G
}

enum class GoErrorType {
    EVT_INIT,
    EVT_RESET,
    EVT_OFFLINE_TRANS,
    EVT_ONLINE_TRANS,
    EVT_SERVER_RESP_RECV,
    EVT_SOCKET_ERROR,
    EVT_SERVER_ERROR,
    EVT_PUMP_BUSY_FROM_ONLINE_ERROR,
    EVT_PUMP_BUSY_FROM_OFFLINE_ERROR,
    EVT_CONTINUE_TRANS,
    EVT_PUMP_NOZZLE_UP,
    EVT_PUMP_NOZZLE_DOWN,
    EVT_PUMP_FILLING,
    EVT_PUMP_FILLING_COMPLETE,
    EVT_END_TRANS_CMD,
    EVT_STATE_TIMEOUT,
    EVT_VALUE_IS_ZERO,
    EVT_VALUE_IS_TOO_LOW
}

object LibraryErrorType {
    const val EP_FSM_EVT_UNDEF: Int = 0
    const val EP_FSM_EVT_INIT: Int = 1
    const val EP_FSM_EVT_DEINIT: Int = 2
    const val EP_FSM_EVT_START_TR: Int = 3
    const val EP_FSM_EVT_RESUME_TR: Int = 4
    const val EP_FSM_EVT_STOP_TR: Int = 5
    const val EP_FSM_EVT_TIMEOUT: Int = 6
    const val EP_FSM_EVT_NO_RESP_TIMEOUT: Int = 7
    const val EP_FSM_EVT_UNACK_ERROR_WRONG_STATE: Int = 8
    const val EP_FSM_EVT_UNACK_ERROR_WRONG_SESSION: Int = 9
    const val EP_FSM_EVT_GO_ERROR: Int =
        10 //on this event, the app gets the correct error message from go
    const val EP_FSM_EVT_UNDEF_GO_ERROR: Int = 11
    const val EP_FSM_EVT_CLEAR_ERROR: Int = 12


    fun getString(code: Int): String {
        return when (code) {
            EP_FSM_EVT_UNDEF -> "Undefined Error"
            EP_FSM_EVT_INIT -> "Initialization"
            EP_FSM_EVT_DEINIT -> "De-Initialization"
            EP_FSM_EVT_START_TR -> "Start Transaction"
            EP_FSM_EVT_RESUME_TR -> "Resume Transaction"
            EP_FSM_EVT_STOP_TR -> "Stop Transaction"
            EP_FSM_EVT_TIMEOUT -> "Timeout"
            EP_FSM_EVT_NO_RESP_TIMEOUT -> "No Response Timeout"
            EP_FSM_EVT_UNACK_ERROR_WRONG_STATE -> "Wrong State Error"
            EP_FSM_EVT_UNACK_ERROR_WRONG_SESSION -> "Wrong Session Error"
            EP_FSM_EVT_GO_ERROR -> "GO Error"
            EP_FSM_EVT_UNDEF_GO_ERROR -> "Undefined GO Error"
            EP_FSM_EVT_CLEAR_ERROR -> "Clear Error"
            else -> ""
        }
    }
}

object ServerErrorType {
    const val Successful: Int = 0
    const val NoValidEndPoint: Int = 101
    const val InternalServerError: Int = 102
    const val InvalidJsonString: Int = 103
    const val DeviceNotFound: Int = 104
    const val TankNotFound: Int = 105
    const val PumpNotFound: Int = 106
    const val ExistingSales: Int = 107
    const val NoDeviceSales: Int = 1016
    const val NOScanStartRecord: Int = 108
    const val EventNotRecognized: Int = 109
    const val ExpiredCard: Int = 110
    const val CardNotTrusted: Int = 111
    const val CardLocked: Int = 112
    const val CardNotFound: Int = 113
    const val CardNotActivated: Int = 125
    const val CardNotAssigned: Int = 126
    const val CardTransactionExist: Int = 127
    const val VoucherUsed: Int = -114
    const val VoucherNotFound: Int = -115
    const val VoucherCannotBeProcessed: Int = -116
    const val VoucherNotAllowed: Int = -118
    const val VoucherCanceledByOwner: Int = -117
    const val VoucherExpired: Int = -128
    const val InsufficientBalance: Int = 118
    const val UserWalletNotFound: Int = 119
    const val IncorrectPassword: Int = 120
    const val QuickFuelNotFound: Int = 121
    const val UserNotFound: Int = 122
    const val POSReferenceUsed: Int = 123
    const val POSReferenceNotFound: Int = 124

    fun getString(errorCode: Int): String {
        return when (errorCode) {
            Successful -> "Successful"
            NoValidEndPoint -> "No Valid EndPoint"
            InternalServerError -> "Internal Server Error"
            InvalidJsonString -> "Invalid Json String"
            DeviceNotFound -> "Device Not Found"
            TankNotFound -> "Tank Not Found"
            PumpNotFound -> "Pump Not Found"
            ExistingSales -> "Existing Sales"
            NoDeviceSales -> "No Device Sales"
            NOScanStartRecord -> "NO Scan Start Record"
            EventNotRecognized -> "Event Not Recognized"
            ExpiredCard -> "Expired Card"
            CardNotTrusted -> "Card Not Trusted"
            CardLocked -> "Card Locked"
            CardNotFound -> "Card Not Found"
            CardNotActivated -> "Card Not Activated"
            CardNotAssigned -> "Card Not Assigned"
            CardTransactionExist -> "Card Transaction Exist"
            VoucherUsed -> "Voucher Previously Used"
            VoucherNotFound -> "Voucher Not Found"
            VoucherCannotBeProcessed -> "Voucher Cannot Be Processed"
            VoucherNotAllowed -> "Voucher Not Allowed"
            VoucherCanceledByOwner -> "Voucher Canceled"
            VoucherExpired -> "Voucher Expired"
            InsufficientBalance -> "Insufficient Balance"
            UserWalletNotFound -> "User Wallet Not Found"
            IncorrectPassword -> "Incorrect Password"
            QuickFuelNotFound -> "QuickFuel Not Found"
            UserNotFound -> "User Not Found"
            POSReferenceUsed -> "POS Reference Previously Used"
            POSReferenceNotFound -> "POS Reference Not Found"
            else -> "Unknown server error"
        }
    }
}