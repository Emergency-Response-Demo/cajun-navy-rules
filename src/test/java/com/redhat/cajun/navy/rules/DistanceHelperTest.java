package com.redhat.cajun.navy.rules;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class DistanceHelperTest {

    @Test
    public void testDistance() {
        BigDecimal lat1 = new BigDecimal("34.00000");
        BigDecimal long1 = new BigDecimal("-77.00000");
        BigDecimal lat2 = new BigDecimal("34.03000");
        BigDecimal long2 = new BigDecimal("-77.04000");

        System.out.println(DistanceHelper.calculateDistance(lat1, lat2, long1, long2));


    }
}
