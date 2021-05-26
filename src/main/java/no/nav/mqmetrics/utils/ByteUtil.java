package no.nav.mqmetrics.utils;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class ByteUtil { public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_PKCS7_SIGNATURE = "application/pkcs7-signature";
    public static final String APPLICATION_PKCS7_MIME = "application/pkcs7-mime";
    public static final String APPLICATION_X_GZIP = "application/x-gzip";
    public static final String APPLICATION_X_COMPRESSED = "application/x-compressed";
    public static final String APPLICATION_ZIP = "application/zip";
    public static final String APPLICATION_X_BZIP2 = "application/x-bzip2";
    private static final byte[] HEX_CHAR_TABLE = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
    private static final int UNSIGNED_SHIFT_RIGHT_LENGTH = 4;
    private static final int SHIFT_LEFT_LENGTH = 4;
    private static final int BYTES_PR_LINE = 16;
    private static final byte HEX_F = 15;
    private static final int HEX_FF = 255;
    private static final byte HEX_UTF8_BYTE = -61;
    private static final byte[] PKCS7_MIME = new byte[]{6, 9, 42, -122, 72, -122, -9, 13, 1, 7, 3};
    private static final Magic PKCS7_MIME_1;
    private static final Magic PKCS7_MIME_2;
    private static final Magic PKCS7_MIME_3;
    private static final Magic PKCS7_MIME_4;
    private static final Magic PKCS7_MIME_5;
    private static final Magic PKCS7_MIME_6;
    private static final Magic PKCS7_MIME_7;
    private static final Magic PKCS7_MIME_8;
    private static final Magic PKCS7_MIME_9;
    private static final byte[] PKCS7_SIGNATURE;
    private static final Magic PKCS7_SIGNATURE_1;
    private static final Magic PKCS7_SIGNATURE_2;
    private static final Magic PKCS7_SIGNATURE_3;
    private static final Magic PKCS7_SIGNATURE_4;
    private static final Magic PKCS7_SIGNATURE_5;
    private static final Magic PKCS7_SIGNATURE_6;
    private static final Magic PKCS7_SIGNATURE_7;
    private static final Magic PKCS7_SIGNATURE_8;
    private static final Magic PKCS7_SIGNATURE_9;
    private static final Magic GZIP;
    private static final Magic BZIP2;
    private static final Magic ZIP;
    private static final Magic COMPRESSED;
    private static final Magic XML;
    private static final Magic XML_UTF16BE;
    private static final Magic XML_UTF16LE;
    private static final Magic XML_UTF32BE;
    private static final Magic XML_UTF32LE;
    private static final Magic XML_BOM;
    private static final Magic XML_BOM_UTF16BE;
    private static final Magic XML_BOM_UTF16LE;
    private static final Magic XML_BOM_UTF32BE;
    private static final Magic XML_BOM_UTF32LE;
    private static final Magic XML_UPPER;
    private static final Magic XML_UPPER_UTF16BE;
    private static final Magic XML_UPPER_UTF16LE;
    private static final Magic XML_UPPER_UTF32BE;
    private static final Magic XML_UPPER_UTF32LE;
    private static final Magic XML_UPPER_BOM;
    private static final Magic XML_UPPER_BOM_UTF16BE;
    private static final Magic XML_UPPER_BOM_UTF16LE;
    private static final Magic XML_UPPER_BOM_UTF32BE;
    private static final Magic XML_UPPER_BOM_UTF32LE;
    private static final Magic PDF;
    private static final Magic[] FILETYPES;

    private ByteUtil() {
    }

    public static String byteArrayToHexString(byte[] raw) {
        byte[] hex = new byte[2 * raw.length];
        int index = 0;
        byte[] arr$ = raw;
        int len$ = raw.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            byte b = arr$[i$];
            int v = b & 255;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 15];
        }

        return new String(hex);
    }

    public static byte[] hexStringToByteArray(String str) {
        int numberOfChars = str.length();
        byte[] raw = new byte[numberOfChars / 2];

        for(int i = 0; i < numberOfChars; i += 2) {
            raw[i / 2] = (byte)((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }

        return raw;
    }

    public static String hexView(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int remaining = bytes.length;
        int start = 0;

        while(remaining > 0) {
            sb.append(String.format("%08d", start)).append(" - ");
            appendHexLine(bytes, sb, start);
            int j = appendPrintableLine(bytes, sb, remaining, start);
            remaining -= j;
            start += j;
            if (remaining > 0) {
                sb.append('\n');
            }
        }

        return sb.toString();
    }

    public static String getMimeContentType(byte[] input) {
        if (input != null && input.length > 0) {
            Magic[] arr$ = FILETYPES;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                Magic m = arr$[i$];
                if (m.match(input)) {
                    return m.mime;
                }
            }
        }

        return "application/octet-stream";
    }


    private static void appendHexLine(byte[] bytes, StringBuilder sb, int start) {
        int j = start;

        int i;
        for(i = 0; i < 16 && j < bytes.length; ++j) {
            byte[] hex = new byte[2];
            int v = bytes[j] & 255;
            hex[0] = HEX_CHAR_TABLE[v >>> 4];
            hex[1] = HEX_CHAR_TABLE[v & 15];
            sb.append(new String(hex)).append(' ');
            ++i;
        }

        while(i < 16) {
            sb.append("   ");
            ++i;
        }

    }

    private static int appendPrintableLine(byte[] bytes, StringBuilder sb, int remaining, int start) {
        sb.append(" - ");
        String tmp = new String(bytes, start, remaining > 16 ? 16 : remaining);

        int j;
        for(j = 0; j < tmp.length(); ++j) {
            char c = tmp.charAt(j);
            if (!StringUtil.isPrintable(c)) {
                c = 0;
            }

            sb.append(c);
        }

        return j;
    }

    static {
        PKCS7_MIME_1 = new Magic("application/pkcs7-mime", 2, PKCS7_MIME);
        PKCS7_MIME_2 = new Magic("application/pkcs7-mime", 3, PKCS7_MIME);
        PKCS7_MIME_3 = new Magic("application/pkcs7-mime", 4, PKCS7_MIME);
        PKCS7_MIME_4 = new Magic("application/pkcs7-mime", 5, PKCS7_MIME);
        PKCS7_MIME_5 = new Magic("application/pkcs7-mime", 6, PKCS7_MIME);
        PKCS7_MIME_6 = new Magic("application/pkcs7-mime", 7, PKCS7_MIME);
        PKCS7_MIME_7 = new Magic("application/pkcs7-mime", 8, PKCS7_MIME);
        PKCS7_MIME_8 = new Magic("application/pkcs7-mime", 9, PKCS7_MIME);
        PKCS7_MIME_9 = new Magic("application/pkcs7-mime", 10, PKCS7_MIME);
        PKCS7_SIGNATURE = new byte[]{6, 9, 42, -122, 72, -122, -9, 13, 1, 7, 2};
        PKCS7_SIGNATURE_1 = new Magic("application/pkcs7-signature", 2, PKCS7_SIGNATURE);
        PKCS7_SIGNATURE_2 = new Magic("application/pkcs7-signature", 3, PKCS7_SIGNATURE);
        PKCS7_SIGNATURE_3 = new Magic("application/pkcs7-signature", 4, PKCS7_SIGNATURE);
        PKCS7_SIGNATURE_4 = new Magic("application/pkcs7-signature", 5, PKCS7_SIGNATURE);
        PKCS7_SIGNATURE_5 = new Magic("application/pkcs7-signature", 6, PKCS7_SIGNATURE);
        PKCS7_SIGNATURE_6 = new Magic("application/pkcs7-signature", 7, PKCS7_SIGNATURE);
        PKCS7_SIGNATURE_7 = new Magic("application/pkcs7-signature", 8, PKCS7_SIGNATURE);
        PKCS7_SIGNATURE_8 = new Magic("application/pkcs7-signature", 9, PKCS7_SIGNATURE);
        PKCS7_SIGNATURE_9 = new Magic("application/pkcs7-signature", 10, PKCS7_SIGNATURE);
        GZIP = new Magic("application/x-gzip", 0, new byte[]{31, -117, 8});
        BZIP2 = new Magic("application/x-bzip2", 0, new byte[]{66, 90});
        ZIP = new Magic("application/zip", 0, new byte[]{80, 75, 3, 4});
        COMPRESSED = new Magic("application/x-compressed", 0, new byte[]{31, -99});
        XML = new Magic("application/xml", 0, new byte[]{60, 63, 120, 109, 108});
        XML_UTF16BE = new Magic("application/xml", 0, new byte[]{0, 60, 0, 63, 0, 120, 0, 109, 0, 108});
        XML_UTF16LE = new Magic("application/xml", 0, new byte[]{60, 0, 63, 0, 120, 0, 109, 0, 108, 0});
        XML_UTF32BE = new Magic("application/xml", 0, new byte[]{0, 0, 0, 60, 0, 0, 0, 63, 0, 0, 0, 120, 0, 0, 0, 109, 0, 0, 0, 108});
        XML_UTF32LE = new Magic("application/xml", 0, new byte[]{60, 0, 0, 0, 63, 0, 0, 0, 120, 0, 0, 0, 109, 0, 0, 0, 108, 0, 0, 0});
        XML_BOM = new Magic("application/xml", 3, new byte[]{60, 63, 120, 109, 108});
        XML_BOM_UTF16BE = new Magic("application/xml", 2, new byte[]{0, 60, 0, 63, 0, 120, 0, 109, 0, 108});
        XML_BOM_UTF16LE = new Magic("application/xml", 2, new byte[]{60, 0, 63, 0, 120, 0, 109, 0, 108, 0});
        XML_BOM_UTF32BE = new Magic("application/xml", 4, new byte[]{0, 0, 0, 60, 0, 0, 0, 63, 0, 0, 0, 120, 0, 0, 0, 109, 0, 0, 0, 108});
        XML_BOM_UTF32LE = new Magic("application/xml", 4, new byte[]{60, 0, 0, 0, 63, 0, 0, 0, 120, 0, 0, 0, 109, 0, 0, 0, 108, 0, 0, 0});
        XML_UPPER = new Magic("application/xml", 0, new byte[]{60, 63, 88, 77, 76});
        XML_UPPER_UTF16BE = new Magic("application/xml", 0, new byte[]{0, 60, 0, 63, 0, 88, 0, 77, 0, 76});
        XML_UPPER_UTF16LE = new Magic("application/xml", 0, new byte[]{60, 0, 63, 0, 88, 0, 77, 0, 76, 0});
        XML_UPPER_UTF32BE = new Magic("application/xml", 0, new byte[]{0, 0, 0, 60, 0, 0, 0, 63, 0, 0, 0, 88, 0, 0, 0, 77, 0, 0, 0, 76});
        XML_UPPER_UTF32LE = new Magic("application/xml", 0, new byte[]{60, 0, 0, 0, 63, 0, 0, 0, 88, 0, 0, 0, 77, 0, 0, 0, 76, 0, 0, 0});
        XML_UPPER_BOM = new Magic("application/xml", 3, new byte[]{60, 63, 88, 77, 76});
        XML_UPPER_BOM_UTF16BE = new Magic("application/xml", 2, new byte[]{0, 60, 0, 63, 0, 88, 0, 77, 0, 76});
        XML_UPPER_BOM_UTF16LE = new Magic("application/xml", 2, new byte[]{60, 0, 63, 0, 72, 0, 77, 0, 76, 0});
        XML_UPPER_BOM_UTF32BE = new Magic("application/xml", 4, new byte[]{0, 0, 0, 60, 0, 0, 0, 63, 0, 0, 0, 88, 0, 0, 0, 77, 0, 0, 0, 76});
        XML_UPPER_BOM_UTF32LE = new Magic("application/xml", 4, new byte[]{60, 0, 0, 0, 63, 0, 0, 0, 88, 0, 0, 0, 77, 0, 0, 0, 76, 0, 0, 0});
        PDF = new Magic("application/pdf", 0, new byte[]{37, 80, 68, 70});
        FILETYPES = new Magic[]{PKCS7_MIME_1, PKCS7_MIME_2, PKCS7_MIME_3, PKCS7_MIME_4, PKCS7_MIME_5, PKCS7_MIME_6, PKCS7_MIME_7, PKCS7_MIME_8, PKCS7_MIME_9, GZIP, XML, XML_BOM, XML_UPPER, XML_UPPER_BOM, XML_UTF16BE, XML_BOM_UTF16BE, XML_UPPER_UTF16BE, XML_UPPER_BOM_UTF16BE, XML_UTF16LE, XML_BOM_UTF16LE, XML_UPPER_UTF16LE, XML_UPPER_BOM_UTF16LE, XML_UTF32BE, XML_BOM_UTF32BE, XML_UPPER_UTF32BE, XML_UPPER_BOM_UTF32BE, XML_UTF32LE, XML_BOM_UTF32LE, XML_UPPER_UTF32LE, XML_UPPER_BOM_UTF32LE, BZIP2, ZIP, COMPRESSED, PKCS7_SIGNATURE_1, PKCS7_SIGNATURE_2, PKCS7_SIGNATURE_3, PKCS7_SIGNATURE_4, PKCS7_SIGNATURE_5, PKCS7_SIGNATURE_6, PKCS7_SIGNATURE_7, PKCS7_SIGNATURE_8, PKCS7_SIGNATURE_9, PDF};
    }

    private static final class Magic {
        private byte[] magic;
        private String mime;
        private int offset;

        private Magic(String mime, int offset, byte[] magic) {
            this.mime = mime;
            this.offset = offset;
            this.magic = ArrayUtils.clone(magic);
        }

        private boolean match(byte[] input) {
            int length = this.offset + this.magic.length;
            if (input.length < length) {
                return false;
            } else {
                byte[] a = Arrays.copyOfRange(input, this.offset, length);
                return Arrays.equals(a, this.magic);
            }
        }
    }
}
