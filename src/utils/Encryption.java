package utils;

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
}
