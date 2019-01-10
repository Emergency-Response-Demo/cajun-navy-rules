package com.redhat.cajun.navy.rules.test.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DistanceHelper {

    public static BigDecimal longitude( double longitude ) {
        return valueOf( longitude );
    }

    public static BigDecimal latitude( double latitude ) {
        return valueOf( latitude );
    }

    private static BigDecimal valueOf( double value ) {
        // rounding mode half up is the one they teach in school
        return new BigDecimal( value ).setScale( 6, RoundingMode.HALF_UP );
    }

}
