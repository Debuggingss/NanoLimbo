package ua.nanit.limbo.util;

public class Utils {

    public static double square(double number) {
        return number * number;
    }

    public static int floor(double number) {
        return (int) Math.floor(number);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
