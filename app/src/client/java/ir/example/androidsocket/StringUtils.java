package ir.example.androidsocket;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

class StringUtils
{
    public StringUtils() {
    }

    public static boolean isMacthIp(String ip) {
        if (ip != null && !ip.isEmpty()) {
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            return ip.matches(regex);
        } else {
            return false;
        }
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || "".equals(str) || "null".equals(str);
    }

    public static boolean isDigital(String str) {
        return !isEmpty(str) && str.matches("[0-9]+");
    }

    public static int length(String value) {
        int valueLength = 0;
        String chinese = "[Α-￥]";

        for(int i = 0; i < value.length(); ++i) {
            String temp = value.substring(i, i + 1);
            if (temp.matches(chinese)) {
                valueLength += 2;
            } else {
                ++valueLength;
            }
        }

        return valueLength;
    }

    public static List<String> stringsToList(String[] items) {
        List<String> list = new ArrayList();
        String[] var2 = items;
        int var3 = items.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String item = var2[var4];
            list.add(item);
        }

        return list;
    }

    public static String fill(String sour, String fillStr, int len, boolean isLeft) {
        if (sour == null) {
            sour = "";
        }

        int fillLen = len - length(sour);
        StringBuilder fill = new StringBuilder();

        for(int i = 0; i < fillLen; ++i) {
            fill.append(fillStr);
        }

        return isLeft ? fill + sour : sour + fill;
    }

    public static String paddingString(String strData, int nLen, String subStr, int nOption) {
        StringBuilder strHead = new StringBuilder();
        StringBuilder strEnd = new StringBuilder();
        int i = strData.length();
        if (i >= nLen) {
            return strData;
        } else {
            int addCharLen;
            switch(nOption) {
                case 0:
                    addCharLen = (nLen - i) / subStr.length();

                    for(i = 0; i < addCharLen; ++i) {
                        strHead.append(subStr);
                    }

                    strHead.append(strData);
                    return strHead.toString();
                case 1:
                    addCharLen = (nLen - i) / subStr.length();

                    for(i = 0; i < addCharLen; ++i) {
                        strEnd.append(subStr);
                    }

                    return strData + strEnd;
                case 2:
                    addCharLen = (nLen - i) / (subStr.length() * 2);

                    for(i = 0; i < addCharLen; ++i) {
                        strHead.append(subStr);
                        strEnd.append(subStr);
                    }

                    return strHead + strData + strEnd;
                default:
                    return strData;
            }
        }
    }

    public static String intToBcd(int value, int bytesNum) {
        switch(bytesNum) {
            case 1:
                if (value >= 0 && value <= 99) {
                    return paddingString(String.valueOf(value), 2, "0", 0);
                }
                break;
            case 2:
                if (value >= 0 && value <= 999) {
                    return paddingString(String.valueOf(value), 4, "0", 0);
                }
                break;
            case 3:
                if (value >= 0 && value <= 999) {
                    return paddingString(String.valueOf(value), 3, "0", 0);
                }
        }

        return "";
    }

    public static String hexToStr(String value) throws UnsupportedEncodingException
    {
        return new String(BytesUtils.hexToBytes(value), "GBK");
    }

    public static String strToHex(String value) {
        return BytesUtils.bytesToHex(BytesUtils.getBytes(value));
    }

    public static String paddingZeroToHexStr(String value, int option) {
        if (value.length() % 2 == 0) {
            return value;
        } else if (option == 0) {
            return "0" + value;
        } else {
            return option == 1 ? value + "0" : value;
        }
    }

    public static boolean checkHexStr(String value) {
        if (value == null) {
            return false;
        } else {
            int len = value.length();
            if (len == 0) {
                return false;
            } else {
                for(int i = 0; i < len; ++i) {
                    boolean isHexChar = value.charAt(i) >= '0' && value.charAt(i) <= '9' || value.charAt(i) >= 'a' && value.charAt(i) <= 'f' || value.charAt(i) >= 'A' && value.charAt(i) <= 'F';
                    if (!isHexChar) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public static String binaryToHex(String value) {
        char[] hexVocable = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        String[] binString = new String[]{"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};
        int len = value.length();
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < len; i += 4) {
            for(int j = 0; j < 16; ++j) {
                if (binString[j].equals(value.substring(i, i + 4))) {
                    result.append(hexVocable[j]);
                    break;
                }
            }
        }

        return result.toString();
    }

    public static String hexToBinary(String value) {
        char[] hexVocable = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        String[] binString = new String[]{"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};
        int len = value.length();
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < len; ++i) {
            for(int j = 0; j < 16; ++j) {
                if (value.charAt(i) == hexVocable[j]) {
                    result.append(binString[j]);
                    break;
                }
            }
        }

        return result.toString();
    }

    public static String getBinaryString(byte[] value) {
        int len = value.length;
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < len; ++i) {
            result.append(value[i]);
        }

        return result.toString();
    }
}