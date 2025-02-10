package africa.epump.connect.efueling.models

import java.security.SecureRandom
import java.util.Random


class RandomGenerator(length: Int, random: Random, symbols: String) {
    /**
     * Generate a random string.
     */
    fun nextString(): String {
        for (idx in buf.indices) buf[idx] = symbols[random.nextInt(symbols.size)]
        return String(buf)
    }

    private val random: Random

    private val symbols: CharArray

    private val buf: CharArray

    init {
        require(length >= 1)
        require(symbols.length >= 2)
        this.random = random
        this.symbols = symbols.toCharArray()
        this.buf = CharArray(length)
    }

    /**
     * Create an numeric string generator.
     */
    /**
     * Create an numeric strings from a secure generator.
     */
    /**
     * Create session identifiers.
     */
    @JvmOverloads
    constructor(length: Int = 21, random: Random = SecureRandom()) : this(length, random, digits)

    companion object {
        private const val digits = "0123456789"
    }
}