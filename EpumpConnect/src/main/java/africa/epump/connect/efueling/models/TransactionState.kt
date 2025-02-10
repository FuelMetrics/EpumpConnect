package africa.epump.connect.efueling.models

object TransactionState {
    const val ST_INIT: Int = 0
    const val ST_IDLE: Int = 1
    const val ST_PUMP_BUSY: Int = 2
    const val ST_REQUESTING_FROM_SERVER: Int = 3
    const val ST_PUMP_AUTH: Int = 4
    const val ST_PUMP_FILLING: Int = 5
    const val ST_PUMP_FILL_COMP: Int = 6
    const val ST_NULL: Int = 7
    const val ST_ERROR: Int = 8
    const val ST_LIB_ERROR: Int = 9

    fun getString(state: Int, pumpDisplayName: String?): String {
        var resp = ""
        resp = when (state) {
            ST_INIT -> "Go setting up"
            ST_IDLE -> String.format("Initializing %s...", pumpDisplayName)
            ST_PUMP_BUSY -> String.format("Pump (%s) not ready", pumpDisplayName)
            ST_REQUESTING_FROM_SERVER -> String.format(
                "Processing request on %s...",
                pumpDisplayName
            )

            ST_PUMP_AUTH -> String.format("Transaction authorized, Pick Up %s", pumpDisplayName)
            ST_PUMP_FILLING -> String.format("Transaction in progress on %s...", pumpDisplayName)
            ST_PUMP_FILL_COMP -> String.format("Transaction completed on %s", pumpDisplayName)
            ST_NULL -> String.format("Ready state on %s", pumpDisplayName)
            ST_ERROR -> String.format("Transaction error on %s:", pumpDisplayName)
            ST_LIB_ERROR -> String.format("Library error on %s:", pumpDisplayName)
            else -> "-$state"
        }
        return resp
    }

    private fun removeAlphabets(str: String): String {
        return str.replace("[^\\d.]".toRegex(), "")
    }
}