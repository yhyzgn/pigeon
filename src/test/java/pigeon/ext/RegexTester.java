package pigeon.ext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-03 15:04
 * version: 1.0.0
 * desc   :
 */
public class RegexTester {
    private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}.*?");

    public static void main(String[] args) {
        String url = "/account/book/{id}/{rand}/";
        Matcher matcher = PARAM_URL_REGEX.matcher(url);

        while (matcher.find()) {
            System.out.println(matcher.group(1));
        }
    }
}
