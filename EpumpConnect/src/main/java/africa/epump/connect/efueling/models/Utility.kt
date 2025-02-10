package africa.epump.connect.efueling.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.net.ParseException
import java.io.IOException
import java.io.InputStream
import java.security.SecureRandom
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Utility {
    var ConnectionStarted: Boolean = false
    fun convert2DecimalString(value: Double, groupThousands: Boolean): String {
        return if (groupThousands) {
            String.format("%,.2f", value)
        } else {
            String.format("%.2f", value)
        }
    }

    fun padPassword(password: String): String {
        val rdg = RandomGenerator(1, SecureRandom())
        val pass = StringBuilder(rdg.nextString())

        val passSplit = password.split("(?<=\\G.{2})".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        for (s in passSplit) {
            pass.append(s).append(rdg.nextString())
        }
        return pass.toString()
    }

    fun bytesToHexString(source: ByteArray?): String? {
        val stringBuilder = StringBuilder("")
        if (source == null || source.size <= 0) {
            return null
        }
        val buffer = CharArray(2)
        for (src in source) {
            buffer[0] = Character.forDigit((src.toInt() ushr 4) and 0x0F, 16)
            buffer[1] = Character.forDigit(src.toInt() and 0x0F, 16)
            stringBuilder.append(buffer)
        }
        return stringBuilder.toString()
    }

    fun parseDates(csDate: String?, vararg pattern: String): Date {
        if (csDate == null) {
            return Date()
        }

        if (!pattern[0].isEmpty()) {
            val inputFormat: DateFormat = SimpleDateFormat(pattern[0], Locale.US)
            try {
                return inputFormat.parse(csDate)
            } catch (e: ParseException) {
            }
        }

        val possibleDateFormats =
            arrayOf(
                "yyyy.MM.dd G 'at' HH:mm:ss z",
                "EEE, MMM d, ''yy",
                "h:mm a",
                "hh 'o''clock' a, zzzz",
                "K:mm a, z",
                "yyyyy.MMMMM.dd GGG hh:mm aaa",
                "EEE, d MMM yyyy HH:mm:ss Z",
                "yyMMddHHmmssZ",
                "dd/MM/yy HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "YYYY-'W'ww-u",
                "EEE, dd MMM yyyy HH:mm:ss z",
                "EEE, dd MMM yyyy HH:mm zzzz",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSSzzzz",
                "yyyy-MM-dd'T'HH:mm:sszzzz",
                "yyyy-MM-dd'T'HH:mm:ss z",
                "yyyy-MM-dd'T'HH:mm:ssz",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HHmmss.SSSz",
                "yyyy-MM-dd",
                "yyyyMMdd",
                "MM/dd/yy",
                "MM/dd/yyyy",
                "dd/MM/yy hh:mm a"
            )
        for (formatString in possibleDateFormats) {
            try {
                //.replace("T", " ")
                val inputFormat: DateFormat = SimpleDateFormat(formatString)
                return inputFormat.parse(csDate)
            } catch (e: ParseException) {
            } catch (e: IllegalArgumentException) {
            }
        }
        return Date()
    }

    fun parseDate(date: Date?, pattern: String?): String {
        val format: SimpleDateFormat = SimpleDateFormat(pattern)
        return format.format(date)
    }

    fun parseDate(csDate: String?, pattern: String?): String {
        if (csDate == null) {
            return ""
        }
        val possibleDateFormats =
            arrayOf(
                "yyyy.MM.dd G 'at' HH:mm:ss z",
                "EEE, MMM d, ''yy",
                "h:mm a",
                "hh 'o''clock' a, zzzz",
                "K:mm a, z",
                "yyyyy.MMMMM.dd GGG hh:mm aaa",
                "EEE, d MMM yyyy HH:mm:ss Z",
                "yyMMddHHmmssZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "YYYY-'W'ww-u",
                "EEE, dd MMM yyyy HH:mm:ss z",
                "EEE, dd MMM yyyy HH:mm zzzz",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSSzzzz",
                "yyyy-MM-dd'T'HH:mm:sszzzz",
                "yyyy-MM-dd'T'HH:mm:ss z",
                "yyyy-MM-dd'T'HH:mm:ssz",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HHmmss.SSSz",
                "yyyy-MM-dd",
                "yyyyMMdd",
                "MM/dd/yy",
                "MM/dd/yyyy",
                "dd/MM/yy hh:mm a"
            )
        for (formatString in possibleDateFormats) {
            try {
                val inputFormat: DateFormat = SimpleDateFormat(formatString)
                val date: Date = inputFormat.parse(csDate)
                return SimpleDateFormat(pattern, Locale.US).format(date)
            } catch (e: ParseException) {
            } catch (e: IllegalArgumentException) {
            }
        }
        return ""
    }

    fun lefPadZero(data: String?, length: Int): String {
        return String.format("%" + length + "s", data).replace(' ', '0')
    }

    fun maskPan(data: String): String {
        val mask = data.substring(6, data.length - 4)
        val sb = StringBuilder()
        for (i in 0 until mask.length) {
            sb.append("*")
        }
        return data.replace(mask, sb.toString())
    }

    fun getPrintHtmlFromAsset(context: Context, fileName: String?): String? {
        var `is`: InputStream? = null
        try {
            `is` = context.assets.open(fileName!!)
            val size = `is`.available()

            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()

            return String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun getEpumpLogoBitmapFromAsset(context: Context): Bitmap? {
        val assetManager = context.assets

        val istr: InputStream
        var bitmap: Bitmap? = null
        try {
            istr = assetManager.open("print_logo.png")
            bitmap = BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bitmap
    }

    fun getIswLogoBitmapFromAsset(context: Context): Bitmap? {
        val assetManager = context.assets

        val istr: InputStream
        var bitmap: Bitmap? = null
        try {
            istr = assetManager.open("isw_logo.png")
            bitmap = BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bitmap
    }
}