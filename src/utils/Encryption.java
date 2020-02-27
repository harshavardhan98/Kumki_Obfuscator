package utils;

import utils.Constants.*;

public class Encryption {
    public static String getHexValue(String value) {
        String keyValue = Constants.keyValue;
        String tempKey = keyValue;

        int len1 = value.length();
        int len2 = keyValue.length();
        int len3 = (int) Math.ceil(len1 / (double) len2);

        for (int i = 1; i < len3; i++)
            keyValue += tempKey;

        String text = "";
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            char k = keyValue.charAt(i);
            if(Character.isAlphabetic(c)) {

                if(Character.isUpperCase(c)){
                    int cix = c - 65;
                    int key = k - 65;
                    cix = (cix + key) % 26 + 65;
                    text += (char) (cix);

                }else{
                    int cix = c - 97;
                    int key = k - 97;
                    cix = (cix + key) % 26 + 97;
                    text += (char) (cix);
                }
            }
            else
                text += c;
        }

        return text;
    }

    public static String appendUnderScore(String value){
        return value + "_";
    }

    /******************************************************************/

    public static String getUnicodeExpression(String val, Mode m){

        String str="";

        if(m==Mode.INTEGER_CONSTANT_OBFUSCATION || m==Mode.DOUBLE_CONSTANT_OBFUSCATION || m==Mode.CHAR_CONSTANT_OBFUSCATION) {
            return toUnicode (val);
        }
        else if(m==Mode.STRING_CONSTANT_OBFUSCATION){
            val="\""+val+"\"";
            return toUnicode(val);
        }

        return str;
    }

    private static String toUnicode(String uString) {
        String unicode = "";
        for (char c: uString.toCharArray()) {
            unicode += toUnicode(c);
        }
        return unicode;
    }

    private static String toUnicode(Character uChar) {
        return "\\u" + Integer.toHexString(uChar | 0x10000).substring(1);
    }

    /******************************************************************/

}
