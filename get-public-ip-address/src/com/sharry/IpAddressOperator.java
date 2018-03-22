package com.sharry;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class IpAddressOperator {
    public static final String DefaultIpProvider = "http://ip.cn/";
    public static final int DefaultRetryTimes = 3;

    private static final Pattern IpPattern =
            Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])");

    public static boolean IpValidate(final String ipAddress) {
        return ipAddress != null && IpPattern.matcher(ipAddress).matches();
    }

    private static final Pattern UrlPattern =
            Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    public static boolean UrlValidate(final String url) {
        return url != null && UrlPattern.matcher(url).matches();
    }

    public String GetIpAddress(String ipProvider) throws IllegalArgumentException {
        if (UrlValidate(ipProvider)) {
            String ipAddress = null;

            try {
                Document doc = Jsoup.connect(ipProvider).get();

                Elements codeElements = doc.select("code");

                for (Element codeElement : codeElements) {
                    if (codeElement.parent().hasText() && codeElement.parent().text().contains("IP")) {
                        ipAddress = codeElement.text();
                    }
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage());
            }

            if (IpValidate(ipAddress)) {
                return ipAddress.trim();
            } else {
                throw new IllegalArgumentException("The format of this ip address is wrong.");
            }
        } else {
            throw new IllegalArgumentException("Please check the ip provider.");
        }
    }

    public String GetIpAddress() throws IllegalFormatException {
       return GetIpAddress(DefaultIpProvider);
    }

    public String GetIpAddressWithRetry() throws IllegalArgumentException {
        return GetIpAddressWithRetry(DefaultIpProvider);
    }

    public String GetIpAddressWithRetry(String ipProvider) throws IllegalArgumentException {
        return GetIpAddressWithRetry(ipProvider, DefaultRetryTimes);
    }

    public String GetIpAddressWithRetry(String ipProvider, int retryTimes) throws IllegalArgumentException {
        if (UrlValidate(ipProvider)) {
            String ipAddress = null;
            int remainRetryTimes = retryTimes < 1 ? DefaultRetryTimes : retryTimes;

            do {
                remainRetryTimes--;

                try {
                    ipAddress = GetIpAddress(ipProvider);

                    if (IpValidate(ipAddress)) {
                        return ipAddress.trim();
                    }
                } catch (IllegalArgumentException e) {
                    //Put log.
                }
            } while (remainRetryTimes != 0);

            throw new IllegalArgumentException("The format of this ip address is wrong.");
        } else {
            throw new IllegalArgumentException("Please check the ip provider.");
        }
    }
}
