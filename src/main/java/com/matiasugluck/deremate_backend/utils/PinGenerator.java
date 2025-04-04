package com.matiasugluck.deremate_backend.utils;

import java.util.Random;

public class PinGenerator {
    public static String generatePin() {
        Random random = new Random();
        int pin = 100000 + random.nextInt(900000);
        return String.valueOf(pin);
    }
}
