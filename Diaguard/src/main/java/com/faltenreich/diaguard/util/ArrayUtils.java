package com.faltenreich.diaguard.util;

/**
 * Created by Faltenreich on 18.10.2015.
 */
public class ArrayUtils {

    public static long sum(long[] array) {
        long sum = 0L;
        for (long number : array) {
            sum += number;
        }
        return sum;
    }

    public static float sum(float[] array) {
        float sum = 0f;
        for (float number : array) {
            sum += number;
        }
        return sum;
    }

    public static int sum(int[] array) {
        int sum = 0;
        for (int number : array) {
            sum += number;
        }
        return sum;
    }

    public static float avg(float[] avg) {
        return sum(avg) / avg.length;
    }

    public static String[] toStringArray(float[] array) {
        String[] stringArray = new String[array.length];
        for (int position = 0; position < array.length; position++) {
            stringArray[position] = Float.toString(array[position]);
        }
        return stringArray;
    }
}