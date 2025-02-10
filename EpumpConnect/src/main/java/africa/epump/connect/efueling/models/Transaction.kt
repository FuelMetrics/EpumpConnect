package africa.epump.connect.efueling.models

import java.util.UUID


class Transaction(
    var type: String,
    var voucherCardNumber: String,
    var amount: Double,
    var volume: Double,
    var time: String?,
    var driverId: UUID?
) {
    var id: String? = null
    var totalizer: Double = 0.0
    var pumpName: String? = null
    var valueType: String? = null
    private var Status: String? = null
    var isPresent: Boolean = false

    var isCompleted: Boolean
        get() = Status.equals("completed", ignoreCase = true)
        set(completed) {
            Status = if (completed) "Completed" else "Not Completed"
        }
}