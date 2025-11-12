package com.example.bankcards.util;

import java.security.SecureRandom;

public class CardNumber {

    private static final SecureRandom rnd = new SecureRandom();

    public static String generate16DigitNumber() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

}
