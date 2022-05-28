package com.penelope.seatforyou.utils;

public class AuthUtils {

    public static String emailize(String phone) {
        return phone + "@seatforu.com";
    }

    public static boolean isSequential(String password) {
        String[] patterns = {
          "012", "123", "234", "345", "456", "567", "678", "789", "890",
          "111", "222", "333", "444", "555", "666", "777", "888", "999", "000",
        };
        for (String pattern : patterns) {
            if (password.contains(pattern)) {
                return true;
            }
        }
        return false;
    }


    public static boolean hasAnySpecial(String password) {
        String specials = "`~!@#$%^&*()-_=+\\|,<.>/?[{]};:'\"";
        for (char special : specials.toCharArray()) {
            if (password.contains(String.valueOf(special))) {
                return true;
            }
        }
        return false;
    }

}
