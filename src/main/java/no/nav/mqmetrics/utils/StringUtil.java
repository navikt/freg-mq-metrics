package no.nav.mqmetrics.utils;


import org.apache.tomcat.util.codec.binary.Base64;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class StringUtil {
    private static final long K = 1024L;
    private static final String[] MEM_UNITS = new String[]{" bytes", " Kb", " Mb", " Gb", " Tb"};
    private static final int MEM_DECIMALS = 3;
    private static final int MS_DECIMALS = 3;
    private static final long NS_IN_A_MS = 1000000L;
    private static final int UNICODE_DOUBLECHAR_LENGTH = 2;
    private static final int UNICODE_TRIPLECHAR_LENGTH = 3;
    private static final int UNICODE_QUADCHAR_LENGTH = 4;
    private static final char UNICODE_SINGLECHAR_MAX = '\u007f';
    private static final char UNICODE_DOUBLECHAR_MAX = '\u07ff';

    private StringUtil() {
    }

    public static String formatDecimal(long number, int digits) {
        if (digits <= 0) {
            throw new IllegalArgumentException("number of leading zeros must be greater than 0");
        } else {
            String format = String.format("%%0%dd", digits);
            return String.format(format, number);
        }
    }

    public static String formatDouble(double number, int decimals, boolean thousandsSeparator) {
        if (decimals < 0) {
            throw new IllegalArgumentException("number of decimals must be greater than -1");
        } else {
            String hashes = "#################################################";
            String format;
            if (thousandsSeparator && Locale.getDefault().getCountry().equals("NO")) {
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
                symbols.setGroupingSeparator(' ');
                format = decimals > 0 ? "#,###." + "#################################################".substring(0, decimals) : "#,###";
                DecimalFormat formatter = new DecimalFormat(format, symbols);
                return formatter.format(number);
            } else {
                String sep = thousandsSeparator ? "," : "";
                format = String.format("%%1$%2$s.%df", decimals, sep);
                return String.format(format, number);
            }
        }
    }

    public static boolean isPrintable(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return !Character.isISOControl(c) && block != null && block != Character.UnicodeBlock.SPECIALS && c != '\uffff';
    }

    public static String getDuration(long ss) {
        return getDurationMs(ss * 1000L, false);
    }

    public static String getDurationNs(long ns, boolean formatMs) {
        return getDurationMs(ns / 1000000L, formatMs);
    }

    public static String getDurationMs(long ms, boolean formatMs) {
        long secs = ms / 1000L;
        long d = secs / 86400L;
        long r = secs - d * 86400L;
        long h = r / 3600L;
        r -= h * 3600L;
        long m = r / 60L;
        r -= m * 60L;
        StringBuilder sb = new StringBuilder();
        if (d > 0L) {
            sb.append(d).append(' ');
        }

        if (h > 0L || d > 0L) {
            sb.append(formatDecimal(h, 2)).append(':');
        }

        sb.append(formatDecimal(m, 2)).append(':').append(formatDecimal(r, 2));
        if (formatMs) {
            sb.append('.').append(formatDecimal(ms % 1000L, 3));
        }

        return sb.toString();
    }

    public static String getMemorySize(long mem) {
        return getMemorySize(mem, 3, false);
    }

    public static String getMemorySize(long mem, int decimals, boolean thousandsSeparator) {
        int i = 0;

        double m;
        for(m = (double)mem; m >= 1024.0D && i < MEM_UNITS.length; ++i) {
            m /= 1024.0D;
        }

        return formatDouble(m, decimals, thousandsSeparator) + MEM_UNITS[i];
    }

    public static String base64encodeStringNoChunking(String str) {
        return base64encodeStringNoChunking(str.getBytes());
    }

    public static String base64encodeStringNoChunking(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes));
    }

    public static String base64encodeWithChunking(byte[] bytes) {
        return new String(Base64.encodeBase64Chunked(bytes));
    }

    public static int length(CharSequence sequence) {
        int count = 0;
        int i = 0;

        for(int len = sequence.length(); i < len; ++i) {
            char ch = sequence.charAt(i);
            if (ch <= 127) {
                ++count;
            } else if (ch <= 2047) {
                count += 2;
            } else if (Character.isHighSurrogate(ch)) {
                count += 4;
                ++i;
            } else {
                count += 3;
            }
        }

        return count;
    }
}
