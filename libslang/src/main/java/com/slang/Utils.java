package com.slang;

/**
 * Created by syanochara on 27/07/2017.
 */

public class Utils {
    public static final boolean DEBUG_READER = false;

    public static final char NULL_CHAR = Character.MAX_VALUE;
    public static final long NULL_INT = Long.MAX_VALUE;
//    public static long NULL_UINT = Long.MAX_VALUE;
    public static final double NULL_DOUBLE = Double.NaN;

    public static final long MAX_UINT = (1L << 36) - 1;
    public static final long MAX_INT = (long)(MAX_UINT / 2);
    public static final long MIN_INT = -MAX_INT - 1;

}
