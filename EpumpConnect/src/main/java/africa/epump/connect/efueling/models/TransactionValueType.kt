package africa.epump.connect.efueling.models

enum class TransactionValueType(val label: String) {
    Amount("Amount"), Volume("Volume");

    companion object {
        fun get(index: Int): String {
            var index = index
            index = if ((index == 0 || index == 97)) 0 else 1
            return entries[index].label
        }
    }
}