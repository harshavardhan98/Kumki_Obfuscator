package handler;

import obfuscator.Obfuscator;
import utils.Encryption;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.Constants.manifestPath;

public class ManifestHandler {

    public static void performObfuscation() {
        String fileContent = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(manifestPath));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            reader.close();
            fileContent = stringBuilder.toString();
        } catch (Exception ie) {
            System.out.println(ie.getMessage());
        }

        String patternString = "(";
        for(String i : Obfuscator.classNameList)
            patternString += "android:name.*=.*\".*[.]" + i + "\"" + "|";

        patternString = patternString.substring(0,patternString.length() - 1);
        patternString += ")";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(fileContent);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String fullMatcher = matcher.group(1);

            String patternString1 = "\"[.].*\"";
            Pattern pattern1 = Pattern.compile(patternString1);
            Matcher matcher1 = pattern1.matcher(fullMatcher);
            StringBuffer sb1 = new StringBuffer();

            while (matcher1.find())
                matcher1.appendReplacement(sb1, Encryption.getHexValue(matcher1.group(0)));
            matcher1.appendTail(sb1);
            matcher.appendReplacement(sb, sb1.toString());
        }
        matcher.appendTail(sb);

        try {
            PrintWriter out = new PrintWriter(manifestPath);
            out.println(sb.toString());
            out.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
