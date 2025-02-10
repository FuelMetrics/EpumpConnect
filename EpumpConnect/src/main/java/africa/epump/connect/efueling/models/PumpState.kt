package africa.epump.connect.efueling.models

object PumpState {
    const val PUMP_NOT_ACTIVE: Int = 0
    const val PUMP_NOT_LOCKED: Int = 1
    const val NOZZLE_HANG_DOWN: Int = 2
    const val NOZZLE_HANG_UP: Int = 3
    const val PUMP_AUTH_NOZZLE_HANG_DOWN: Int = 4
    const val PUMP_AUTH_NOZZLE_HANG_UP: Int = 5
    const val PUMP_FILLING: Int = 6
    const val PUMP_FILLED_LIMIT: Int = 7
    const val PUMP_FILL_COMP_NOZZLE_HANG_DOWN: Int = 8
    const val PUMP_FILL_COMP_NOZZLE_HANG_UP: Int = 9
    const val PUMP_SWITCHED_OFF: Int = 252
    const val PUMP_STATUS_OTHERS: Int = 253
    const val PUMP_ERROR: Int = 254
    const val PUMP_STATUS_UNKNOWN: Int = 255

    fun getString(state: Int): String {
        var resp = ""
        resp = when (state) {
            PUMP_NOT_ACTIVE -> "Pump is not active"
            PUMP_NOT_LOCKED -> "Pump is not locked"
            NOZZLE_HANG_DOWN -> "Nozzle down"
            NOZZLE_HANG_UP -> "Nozzle up"
            PUMP_AUTH_NOZZLE_HANG_DOWN -> "Pump authorized, nozzle down"
            PUMP_AUTH_NOZZLE_HANG_UP -> "Pump authorized, nozzle up"
            PUMP_FILLING -> "Pump currently selling"
            PUMP_FILLED_LIMIT -> "Pump finished selling"
            PUMP_FILL_COMP_NOZZLE_HANG_DOWN -> "Pump finished selling, nozzle down"
            PUMP_FILL_COMP_NOZZLE_HANG_UP -> "Pump finished selling, nozzle up"
            PUMP_SWITCHED_OFF -> "Pump is switched off"
            PUMP_STATUS_OTHERS -> "Pump status unknown"
            PUMP_ERROR -> "Pump error"
            PUMP_STATUS_UNKNOWN -> "Pump offline"
            else -> "-$state"
        }
        return resp
    }
}