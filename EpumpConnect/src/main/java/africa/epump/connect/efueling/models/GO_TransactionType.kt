package africa.epump.connect.efueling.models

enum class GO_TransactionType(val label: String) {
    Undefined("Unknown"),
    Voucher("Voucher"),
    Remis("Remis"),
    Card("Card"),
    POS("POS"),
    Attendant("Attendant"),
    Offline_Attendant("Offline"),
    Last("");

    companion object {
        fun get(index: Int): String {
            return entries[index].label
        }
    }
}

enum class TransactionType(val label: String) {
    Voucher("Voucher"),
    Remis("Remis"),
    Card("Card"),
    POS("POS"),
    Offline_Voucher("Offline Voucher"),
    Offline_Remis("Offline Remis"),
    Offline_Card("Offline Card"),
    Offline_POS("Offline POS"),
    Resume_Transaction("Resume Transaction");

    companion object {
        fun get(index: Int): String {
            return entries[index].label
        }
    }
}