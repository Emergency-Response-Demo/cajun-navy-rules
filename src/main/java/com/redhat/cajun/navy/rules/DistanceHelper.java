package com.redhat.cajun.navy.rules;

import java.math.BigDecimal;

public class DistanceHelper {
	/***
	 * Calculate distance in meters between two
	 * 
	 * @param lat1
	 * @param lat2
	 * @param lon1
	 * @param lon2
	 * @return
	 */
	public static double calculateDistance(double lat1, double lat2, double lon1, double lon2) {

		final int R = 6371; // Radius of the earth

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		return distance;
	}

	public static double calculateDistance(BigDecimal lat1, BigDecimal lat2, BigDecimal lon1, BigDecimal lon2) {
		return DistanceHelper.calculateDistance(lat1.doubleValue(), lat2.doubleValue(), lon1.doubleValue(), lon2.doubleValue());
	}
}
