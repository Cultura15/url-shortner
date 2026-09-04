package com.jesson.create_url_service.utils;

public final class Base62 {
    private static final String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encodeBase62(Long num){
        if (num == 0){
            return "0";
        }

        StringBuilder encoded = new StringBuilder();

        while (num > 0){
            int remainder = (int)(num % 62);
            encoded.append(characters.charAt(remainder));
            num /= 62;
        }
        return encoded.reverse().toString();
    }
}
