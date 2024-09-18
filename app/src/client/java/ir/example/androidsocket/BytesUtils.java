package ir.example.androidsocket;

import java.nio.charset.Charset;

public class BytesUtils
{
    private static final String GBK = "GBK";
    public static final String UTF_8 = "utf-8";
    private static final char[] ASCII = "0123456789ABCDEF".toCharArray();
    private static char[] HEX_VOCABLE = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public BytesUtils() {
    }

    private static byte[] getBytes(short data) {
        byte[] bytes = new byte[]{(byte)((data & '\uff00') >> 8), (byte)(data & 255)};
        return bytes;
    }

    private static byte[] getBytes(char data) {
        byte[] bytes = new byte[]{(byte)(data >> 8), (byte)data};
        return bytes;
    }

    private static byte[] getBytes(boolean data) {
        byte[] bytes = new byte[]{(byte)(data ? 1 : 0)};
        return bytes;
    }

    private static byte[] getBytes(int data) {
        byte[] bytes = new byte[]{(byte)((data & -16777216) >> 24), (byte)((data & 16711680) >> 16), (byte)((data & '\uff00') >> 8), (byte)(data & 255)};
        return bytes;
    }

    private static byte[] getBytes(long data) {
        byte[] bytes = new byte[]{(byte)((int)(data >> 56 & 255L)), (byte)((int)(data >> 48 & 255L)), (byte)((int)(data >> 40 & 255L)), (byte)((int)(data >> 32 & 255L)), (byte)((int)(data >> 24 & 255L)), (byte)((int)(data >> 16 & 255L)), (byte)((int)(data >> 8 & 255L)), (byte)((int)(data & 255L))};
        return bytes;
    }

    private static byte[] getBytes(float data) {
        int intBits = Float.floatToIntBits(data);
        return getBytes(intBits);
    }

    private static byte[] getBytes(double data) {
        long intBits = Double.doubleToLongBits(data);
        return getBytes(intBits);
    }

    private static byte[] getBytes(String data, String charsetName) {
        Charset charset = Charset.forName(charsetName);
        return data.getBytes(charset);
    }

    public static byte[] getBytes(String data) {
        return getBytes(data, "GBK");
    }

    private static boolean getBoolean(byte[] bytes) {
        return bytes[0] == 1;
    }

    private static boolean getBoolean(byte[] bytes, int index) {
        return bytes[index] == 1;
    }

    public static short getShort(byte[] bytes) {
        return (short)('\uff00' & bytes[0] << 8 | 255 & bytes[1]);
    }

    private static short getShort(byte[] bytes, int startIndex) {
        return (short)('\uff00' & bytes[startIndex] << 8 | 255 & bytes[startIndex + 1]);
    }

    private static char getChar(byte[] bytes) {
        return (char)('\uff00' & bytes[0] << 8 | 255 & bytes[1]);
    }

    private static char getChar(byte[] bytes, int startIndex) {
        return (char)('\uff00' & bytes[startIndex] << 8 | 255 & bytes[startIndex + 1]);
    }

    private static int getInt(byte[] bytes) {
        return -16777216 & bytes[0] << 24 | 16711680 & bytes[1] << 16 | '\uff00' & bytes[2] << 8 | 255 & bytes[3];
    }

    private static int getInt(byte[] bytes, int startIndex) {
        return -16777216 & bytes[startIndex] << 24 | 16711680 & bytes[startIndex + 1] << 16 | '\uff00' & bytes[startIndex + 2] << 8 | 255 & bytes[startIndex + 3];
    }

    private static long getLong(byte[] bytes) {
        return -72057594037927936L & (long)bytes[0] << 56 | 71776119061217280L & (long)bytes[1] << 48 | 280375465082880L & (long)bytes[2] << 40 | 1095216660480L & (long)bytes[3] << 32 | 4278190080L & (long)bytes[4] << 24 | 16711680L & (long)bytes[5] << 16 | 65280L & (long)bytes[6] << 8 | 255L & (long)bytes[7];
    }

    private static long getLong(byte[] bytes, int startIndex) {
        return -72057594037927936L & (long)bytes[startIndex] << 56 | 71776119061217280L & (long)bytes[startIndex + 1] << 48 | 280375465082880L & (long)bytes[startIndex + 2] << 40 | 1095216660480L & (long)bytes[startIndex + 3] << 32 | 4278190080L & (long)bytes[startIndex + 4] << 24 | 16711680L & (long)bytes[startIndex + 5] << 16 | 65280L & (long)bytes[startIndex + 6] << 8 | 255L & (long)bytes[startIndex + 7];
    }

    private static float getFloat(byte[] bytes) {
        return Float.intBitsToFloat(getInt(bytes));
    }

    private static float getFloat(byte[] bytes, int startIndex) {
        byte[] result = new byte[4];
        System.arraycopy(bytes, startIndex, result, 0, 4);
        return Float.intBitsToFloat(getInt(result));
    }

    private static double getDouble(byte[] bytes) {
        long l = getLong(bytes);
        return Double.longBitsToDouble(l);
    }

    private static double getDouble(byte[] bytes, int startIndex) {
        byte[] result = new byte[8];
        System.arraycopy(bytes, startIndex, result, 0, 8);
        long l = getLong(result);
        return Double.longBitsToDouble(l);
    }

    private static String getString(byte[] bytes, String charsetName) {
        return new String(bytes, Charset.forName(charsetName));
    }

    private static String getString(byte[] bytes) {
        return getString(bytes, "GBK");
    }

    private static byte[] str2bcd(String hex, boolean isLeft) {
        if (hex != null && !"".equals(hex)) {
            if (hex.length() % 2 != 0) {
                if (StringUtils.isEmpty("0")) {
                    hex = hex + "0";
                } else if (isLeft) {
                    hex = "0" + hex;
                } else {
                    hex = hex + "0";
                }
            }

            int len = hex.length() / 2;
            byte[] result = new byte[len];
            char[] chArr = hex.toCharArray();

            for(int i = 0; i < len; ++i) {
                int pos = i * 2;
                result[i] = (byte)(toByte(chArr[pos]) << 4 | toByte(chArr[pos + 1]));
            }

            return result;
        } else {
            return null;
        }
    }

    public static byte[] hexStringToBytes(String s) {
        byte[] ret;

        if (s == null) return null;

        int sz = s.length();

        char c;
        for (int i = 0; i < sz; ++i) {
            c = s.charAt(i);
            if (!((c >= '0') && (c <= '9'))
                    && !((c >= 'A') && (c <= 'F'))
                    && !((c >= 'a') && (c <= 'f'))) {
                s = s.replaceAll("[^[0-9][A-F][a-f]]", "");
                sz = s.length();
                break;
            }
        }

        ret = new byte[sz/2];

        for (int i=0 ; i <sz ; i+=2) {
            ret[i/2] = (byte) ((hexCharToInt(s.charAt(i)) << 4)
                    | hexCharToInt(s.charAt(i+1)));
        }

        return ret;
    }

    public static byte[] hexStringToDecryptBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    private static byte[] hexStringToBytes2(String hex) {
        if (hex != null && !"".equals(hex)) {
            int len = hex.length() / 2;
            byte[] result = new byte[len];
            char[] chArr = hex.toCharArray();

            for(int i = 0; i < len; ++i) {
                int pos = i * 2;
                result[i] = (byte)(toByte(chArr[pos]) << 4 | toByte(chArr[pos + 1]));
            }

            return result;
        } else {
            return null;
        }
    }

    private static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') return (c - '0');
        if (c >= 'A' && c <= 'F') return (c - 'A' + 10);
        if (c >= 'a' && c <= 'f') return (c - 'a' + 10);

        throw new RuntimeException ("invalid hex char '" + c + "'");
    }

    private static byte[] hexStringToBytes(String hex, String subStr, boolean isLeft) {
        if (hex != null && !"".equals(hex)) {
            if (hex.length() % 2 != 0) {
                if (StringUtils.isEmpty(subStr)) {
                    hex = hex + "0";
                } else if (isLeft) {
                    hex = subStr + hex;
                } else {
                    hex = hex + subStr;
                }
            }

            int len = hex.length() / 2;
            byte[] result = new byte[len];
            char[] chArr = hex.toCharArray();

            for(int i = 0; i < len; ++i) {
                int pos = i * 2;
                result[i] = (byte)(toByte(chArr[pos]) << 4 | toByte(chArr[pos + 1]));
            }

            return result;
        } else {
            return null;
        }
    }

    public static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("input string should be any multiple of 2!");
        } else {
            hex = hex.toUpperCase();
            byte[] byteBuffer = new byte[hex.length() / 2];
            byte padding = 0;
            boolean paddingTurning = false;

            for(int i = 0; i < hex.length(); ++i) {
                char c;
                int index;
                if (paddingTurning) {
                    c = hex.charAt(i);
                    index = indexOf(hex, c);
                    padding = (byte)(padding << 4 | index);
                    byteBuffer[i / 2] = padding;
                    padding = 0;
                    paddingTurning = false;
                } else {
                    c = hex.charAt(i);
                    index = indexOf(hex, c);
                    padding = (byte)(padding | index);
                    paddingTurning = true;
                }
            }

            return byteBuffer;
        }
    }

    public static String hexToString(String hex){
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i+=2) {
            String str = hex.substring(i, i+2);
            output.append((char)Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    private static int indexOf(String input, char c) {
        for(int i = 0; i < HEX_VOCABLE.length; ++i) {
            if (c == HEX_VOCABLE[i]) {
                return i;
            }
        }

        throw new IllegalArgumentException("err input:" + input);
    }

    private static String bcdToString(byte[] bcds) {
        if (bcds != null && bcds.length != 0) {
            byte[] temp = new byte[2 * bcds.length];

            for(int i = 0; i < bcds.length; ++i) {
                temp[i * 2] = (byte)(bcds[i] >> 4 & 15);
                temp[i * 2 + 1] = (byte)(bcds[i] & 15);
            }

            StringBuilder res = new StringBuilder();
            byte[] var3 = temp;
            int var4 = temp.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                byte b = var3[var5];
                res.append(ASCII[b]);
            }

            return res.toString();
        } else {
            return null;
        }
    }

    private static int bcdToInt(byte value) {
        return (value >> 4) * 10 + (value & 15);
    }

    public static String bytesToHex(byte[] bs) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bs) {
            int high = b >> 4 & 15;
            int low = b & 15;
            sb.append(HEX_VOCABLE[high]);
            sb.append(HEX_VOCABLE[low]);
        }

        return sb.toString();
    }

    private static String bytesToHex(byte[] bs, int len) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < len; ++i) {
            byte b = bs[i];
            int high = b >> 4 & 15;
            int low = b & 15;
            sb.append(HEX_VOCABLE[high]);
            sb.append(HEX_VOCABLE[low]);
        }

        return sb.toString();
    }

    public static String bytesToHex(byte[] bs, int offset, int len) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < len; ++i) {
            byte b = bs[offset + i];
            int high = b >> 4 & 15;
            int low = b & 15;
            sb.append(HEX_VOCABLE[high]);
            sb.append(HEX_VOCABLE[low]);
        }

        return sb.toString();
    }

     static String byteToHex(byte b) {
        new StringBuilder();
        int high = b >> 4 & 15;
        int low = b & 15;
        return HEX_VOCABLE[high] + String.valueOf(HEX_VOCABLE[low]);
    }

    private static String negate(byte[] src) {
        if (src != null && src.length != 0) {
            byte[] temp = new byte[2 * src.length];

            for(int i = 0; i < src.length; ++i) {
                byte tmp = (byte)(255 ^ src[i]);
                temp[i * 2] = (byte)(tmp >> 4 & 15);
                temp[i * 2 + 1] = (byte)(tmp & 15);
            }

            StringBuilder res = new StringBuilder();
            byte[] var8 = temp;
            int var4 = temp.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                byte b = var8[var5];
                res.append(ASCII[b]);
            }

            return res.toString();
        } else {
            return null;
        }
    }

    private static boolean compareBytes(byte[] a, byte[] b) {
        if (a != null && a.length != 0 && b != null && b.length != 0 && a.length == b.length) {
            for(int i = 0; i < a.length; ++i) {
                if (a[i] != b[i]) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private static boolean compareBytes(byte[] a, byte[] b, int len) {
        if (a != null && a.length != 0 && b != null && b.length != 0 && a.length >= len && b.length >= len) {
            for(int i = 0; i < len; ++i) {
                if (a[i] != b[i]) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static String bytesToBinaryString(byte[] items) {
        if (items != null && items.length != 0) {
            StringBuilder sb = new StringBuilder();
            byte[] var2 = items;
            int var3 = items.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                byte item = var2[var4];
                sb.append(byteToBinaryString(item));
            }

            return sb.toString();
        } else {
            return null;
        }
    }

    private static String byteToBinaryString(byte item) {
        byte a = item;
        StringBuffer buf = new StringBuffer();

        for(int i = 0; i < 8; ++i) {
            buf.insert(0, a % 2);
            a = (byte)(a >> 1);
        }

        return buf.toString();
    }

    public static String binaryToHex(String value) {
        return StringUtils.binaryToHex(value);
    }

    public static String hexToBinary(String value) {
        return StringUtils.hexToBinary(value);
    }

    private static byte[] xor(byte[] a, byte[] b) {
        if (a != null && a.length != 0 && b != null && b.length != 0 && a.length == b.length) {
            byte[] result = new byte[a.length];

            for(int i = 0; i < a.length; ++i) {
                result[i] = (byte)(a[i] ^ b[i]);
            }

            return result;
        } else {
            return null;
        }
    }

    private static byte[] xor(byte[] a, byte[] b, int len) {
        if (a != null && a.length != 0 && b != null && b.length != 0) {
            if (a.length >= len && b.length >= len) {
                byte[] result = new byte[len];

                for(int i = 0; i < len; ++i) {
                    result[i] = (byte)(a[i] ^ b[i]);
                }

                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static byte[] shortToBytes(int num) {
        byte[] temp = new byte[2];

        for(int i = 0; i < 2; ++i) {
            temp[i] = (byte)(num >>> 8 - i * 8 & 255);
        }

        return temp;
    }

    private static int bytesToShort(byte[] arr) {
        int mask = 255;
        int result = 0;

        for(int i = 0; i < 2; ++i) {
            result <<= 8;
            int temp = arr[i] & mask;
            result |= temp;
        }

        return result;
    }

    private static byte[] intToBytes(int num) {
        byte[] temp = new byte[4];

        for(int i = 0; i < 4; ++i) {
            temp[i] = (byte)(num >>> 24 - i * 8 & 255);
        }

        return temp;
    }

    private static byte[] intToBytes(int src, int len) {
        if (len >= 1 && len <= 4) {
            byte[] temp = new byte[len];

            for(int i = 0; i < len; ++i) {
                temp[len - 1 - i] = (byte)(src >>> 8 * i & 255);
            }

            return temp;
        } else {
            return null;
        }
    }

    private static int bytesToInt(byte[] arr) {
        int mask = 255;
        int result = 0;

        for(int i = 0; i < 4; ++i) {
            result <<= 8;
            int temp = arr[i] & mask;
            result |= temp;
        }

        return result;
    }

    private static byte[] longToBytes(long num) {
        byte[] temp = new byte[8];

        for(int i = 0; i < 8; ++i) {
            temp[i] = (byte)((int)(num >>> 56 - i * 8 & 255L));
        }

        return temp;
    }

    private static long bytesToLong(byte[] arr) {
        int mask = 255;
        long result = 0L;
        int len = Math.min(8, arr.length);

        for(int i = 0; i < len; ++i) {
            result <<= 8;
            int temp = arr[i] & mask;
            result |= (long)temp;
        }

        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte)"0123456789ABCDEF".indexOf(c);
        return b;
    }

    private static int bytesToIntWhereByteLengthEquals2(byte[] lenData) {
        if (lenData.length != 2) {
            return -1;
        } else {
            byte[] fill = new byte[]{0, 0};
            byte[] real = new byte[4];
            System.arraycopy(fill, 0, real, 0, 2);
            System.arraycopy(lenData, 0, real, 2, 2);
            return byteToInt(real);
        }
    }

    private static int byteToInt(byte[] byteVal) {
        int result = 0;

        for(int i = 0; i < byteVal.length; ++i) {
            int tmpVal = byteVal[i] << 8 * (3 - i);
            switch(i) {
                case 0:
                    tmpVal &= -16777216;
                    break;
                case 1:
                    tmpVal &= 16711680;
                    break;
                case 2:
                    tmpVal &= 65280;
                    break;
                case 3:
                    tmpVal &= 255;
            }

            result |= tmpVal;
        }

        return result;
    }

    private static byte checkXORSum(byte[] bData) {
        byte sum = 0;

        for(int i = 0; i < bData.length; ++i) {
            sum ^= bData[i];
        }

        return sum;
    }

    private static int bytesToInt(byte[] data, int offset, int len) {
        int mask = 255;
        int result = 0;
        len = Math.min(len, 4);

        for(int i = 0; i < len; ++i) {
            result <<= 8;
            int temp = data[offset + i] & mask;
            result |= temp;
        }

        return result;
    }

    private static String trimCharLeft(String source, char c) {
        String beTrim = String.valueOf(c);
        source = source.trim();

        for(String beginChar = source.substring(0, 1); beginChar.equalsIgnoreCase(beTrim); beginChar = source.substring(0, 1)) {
            source = source.substring(1, source.length());
        }

        return source;
    }

    private static String trimCharRight(String source, char c) {
        String beTrim = String.valueOf(c);
        source = source.trim();

        for(String endChar = source.substring(source.length() - 1, source.length()); endChar.equalsIgnoreCase(beTrim); endChar = source.substring(source.length() - 1, source.length())) {
            source = source.substring(0, source.length() - 1);
        }

        return source;
    }

    public static byte[] fromASCIIToBCD(String asciiStr, int asciiOffset, int asciiLen, boolean rightAlignFlag) {
        try {
            byte[] asciiBuf = asciiStr.getBytes("GBK");
            return fromASCIIToBCD(asciiBuf, asciiOffset, asciiLen, rightAlignFlag);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 从ASCII编码转换成BCD编码.
     * @param asciiBuf, ASCII编码缓冲区
     * @param asciiOffset, ASCII编码缓冲区的起始偏移
     * @param asciiLen, 统一采用ASCII编码时的信息长度
     * @param rightAlignFlag, 奇数个ASCII码时采用的右对齐方式标志
     * @return, BCD编码缓冲区
     */
    /**
     * 字母'A'的ASCII编码值
     */
    private final static byte ALPHA_A_ASCII_VALUE = 0x41;

    /**
     * 字母'a'的ASCII编码值
     */
    private final static byte ALPHA_a_ASCII_VALUE = 0x61;

    /**
     * 数字'0'的ASCII编码值
     */
    private final static byte DIGITAL_0_ASCII_VALUE = 0x30;
    private static byte[] fromASCIIToBCD(byte[] asciiBuf, int asciiOffset, int asciiLen, boolean rightAlignFlag) {
        byte[] bcdBuf = new byte[(asciiLen + 1) / 2];
        fromASCIIToBCD(asciiBuf, asciiOffset, asciiLen, bcdBuf, 0, rightAlignFlag);

        return bcdBuf;
    }

    private static void fromASCIIToBCD(byte[] asciiBuf, int asciiOffset, int asciiLen, byte[] bcdBuf, int bcdOffset,
                                      boolean rightAlignFlag) {
        int cnt;
        byte ch, ch1;

        if (((asciiLen & 1) == 1) && rightAlignFlag) {
            ch1 = 0;
        } else {
            ch1 = 0x55;
        }

        for (cnt = 0; cnt < asciiLen; cnt++, asciiOffset++) {
            if (asciiBuf[asciiOffset] >= ALPHA_a_ASCII_VALUE)
                ch = (byte) (asciiBuf[asciiOffset] - ALPHA_a_ASCII_VALUE + 10);
            else if (asciiBuf[asciiOffset] >= ALPHA_A_ASCII_VALUE)
                ch = (byte) (asciiBuf[asciiOffset] - ALPHA_A_ASCII_VALUE + 10);
            else if (asciiBuf[asciiOffset] >= DIGITAL_0_ASCII_VALUE)
                ch = (byte) (asciiBuf[asciiOffset] - DIGITAL_0_ASCII_VALUE);
            else
                ch = 0x00;

            if (ch1 == 0x55)
                ch1 = ch;
            else {
                bcdBuf[bcdOffset] = (byte) (ch1 << 4 | ch);
                bcdOffset++;
                ch1 = 0x55;
            }
        }

        if (ch1 != 0x55)
            bcdBuf[bcdOffset] = (byte) (ch1 << 4);
    }

    public static String StringToBCDArray(String s) {
        int size = s.length();

        byte[] bytes = new byte[(size+1)/2];
        int index = 0;
        boolean advance = size%2 != 0;

        for ( char c : s.toCharArray()) {
            byte b = (byte)( c - '0');
            if( advance ) {
                bytes[index++] |= b;
            }
            else {
                bytes[index] |= (byte)(b<<4);
            }
            advance = !advance;

        }
        return bytesToHex(bytes);
    }

    public static String bcd2str(byte[] bcds) {
        if (bcds == null)
            return "";
        char[] ascii = "0123456789abcdef".toCharArray();
        byte[] temp = new byte[bcds.length * 2];
        for (int i = 0; i < bcds.length; i++) {
            temp[i * 2] = (byte) ((bcds[i] >> 4) & 0x0f);
            temp[i * 2 + 1] = (byte) (bcds[i] & 0x0f);
        }
        StringBuffer res = new StringBuffer();

        for (int i = 0; i < temp.length; i++) {
            res.append(ascii[temp[i]]);
        }
        return res.toString().toUpperCase();
    }

    public static String toHexString(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            sb.append(toHexString(str.charAt(i)));
        }
        return sb.toString();
    }

    public static String toHexString(char ch) {
        return Integer.toHexString(ch).toUpperCase();
    }

}
