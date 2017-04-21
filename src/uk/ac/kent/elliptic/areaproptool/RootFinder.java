/* 
 * eulerAPE v3.0.0
 * 
 * 2013-11-18
 *
 * 
 * 
 * eulerAPE -- Drawing Area-Proportional Euler and Venn Diagrams Using Ellipses	    
 * 		http://www.eulerdiagrams.org/eulerAPE
 * 
 * 
 * 		Copyright (C) 2011-2013, Luana Micallef and Peter Rodgers. 
 * 		All rights reserved.
 * 		
 * 
 * 		This file is part of eulerAPE.
 * 			
 * 		eulerAPE is free software: you can redistribute it and/or modify
 * 		it under the terms of the GNU General Public License as published 
 * 		by the Free Software Foundation, either version 3 of the License, 
 * 		or (at your option) any later version.
	
 * 		eulerAPE is distributed in the hope that it will be useful,
 * 		but WITHOUT ANY WARRANTY; without even the implied warranty of
 * 		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * 		GNU General Public License for more details.
	
 * 		A copy of the GNU General Public License is provided with 
 * 		eulerAPE (in a file named ÔCOPYINGÕ). Alternatively, see 
 * 		<http://www.gnu.org/licenses/gpl.html>.
 * 			
 */
 


package uk.ac.kent.elliptic.areaproptool;

import java.util.ArrayList;


/**
 * 
 * To find the roots of equations
 *
 */


public class RootFinder {
	
	
	public static ArrayList<Double> solveLinear (ArrayList<Double> cs){
		return solveLinear(Utilities.convertDblArrayListToArray(cs));
	}
	public static ArrayList<Double> solveLinear (double[] c){
		ArrayList<Double> s = new ArrayList<Double>();
		
		if (isZero(c[1])){return null;}
		
		s.add(-c[0]/c[1]);
		
		return s;
	}
	
	
	public static ArrayList<ArrayList<Double>> solveCubic (ArrayList<Double> cs){
		return solveCubic(Utilities.convertDblArrayListToArray(cs));
	}
		
	public static ArrayList<ArrayList<Double>> solveCubic (double[] c){
		// This is based on the algorithm in: 
		// Kenneth J Hill (1995). Matrix-based Ellipse Geometry. In Alan W Paeth (ed.), Graphics Gems V, Morgan Kaufmann, San Francisco, CA, USA, pp. 72-77.
			
		
		// normalizing the equation:x ^ 3 + Ax ^ 2 + Bx  + C = 0
		double A = c[2] / c[3];
		double B = c[1] / c[3];
		double C = c[0] / c[3];

		// substituting x = y - (A / 3) to eliminate the quadric term: x^3 + px + q = 0
		double sq_A = A * A;
		double p = (1.0/3.0) * (((-1.0/3.0) * sq_A) + B);
		double q = (1.0/2.0) * (((2.0/27.0) * A *sq_A) - ((1.0/3.0) * A * B) + C);
		
		
		// using Cardano's formula
		
		double cb_p = Math.pow(p,3);
		double D = Math.pow(q, 2) + cb_p;
		
	
		ArrayList<Double> s = new ArrayList<Double>(3); //3 is the initial capacity which will grow automatically if necessary
		
		if (isZero(D)) {
			if (isZero(q)) {
				// one triple solution
				s.add(0.0);
			} else {
				// one single and one double solution
				double u = Math.cbrt(-q);
				s.add(2.0 * u);
				s.add(- u);
			}
			
	    } else if (D < 0.0) {
	    	// when get three real solutions
	    	double phi = (1.0/3.0) * Math.acos(-q / Math.sqrt(-cb_p)); //output of Math.acos is in radians
	    	double t = 2.0 * Math.sqrt(-p);
	    	s.add (t * Math.cos(phi));
	    	s.add (-t * Math.cos(phi + (Math.PI / 3.0)));
	    	s.add(-t * Math.cos(phi - (Math.PI / 3.0)));
	    	
		} else {
			
			// one real solution
			double sqrt_D = Math.sqrt(D);
			double u = Math.cbrt(sqrt_D + Math.abs(q)); //instead of Math.cbrt
			if (q > 0.0){
				s.add(- u + (p / u));
			} else {
				s.add(u - (p / u));
			}
		}
		
	    // resubstitute
	    double sub = 1.0 / 3.0 * A;
	    int i = 0;
	   	for (Double s_1 : s){
	   	    s.set(i, s_1-sub);
	   	    i++;
	   	}
	   	
	   	ArrayList<Double> s_testFromClosePoints = null;
	    if (!isZero(D)){
		   	if (isZero(D,1)){ //0.6
		   		s_testFromClosePoints = new ArrayList<Double>(3);
		   		if (isZero(q)) {
					// one triple solution
		   			s_testFromClosePoints.add(0.0);
				} else {
					// one single and one double solution
					double u = Math.cbrt(-q);
					s_testFromClosePoints.add(2.0 * u);
					s_testFromClosePoints.add(- u);
				}
		   		
		   		sub= 1.0 / 3.0 * A;
			    i = 0;
			   	for (Double s_1 : s_testFromClosePoints){
			   		s_testFromClosePoints.set(i, s_1-sub);
			   	    i++;
			   	}
		   	}
	    }
	   	
	   	ArrayList<ArrayList<Double>> roots = new ArrayList<ArrayList<Double>>(2);
	   	roots.add(s);
	    roots.add(s_testFromClosePoints);
	   	return roots;
	}
	
	
	public static ArrayList<Double> solveQuadric (ArrayList<Double> cs){
		return solveQuadric(Utilities.convertDblArrayListToArray(cs));
	}
	public static ArrayList<Double> solveQuadric (double[] coeff){
		
		if (isZero(coeff[2])){
			return solveLinear(coeff);
		}
		
		ArrayList<Double> s = new ArrayList<Double>(2); //2 is the initial capacity which will grow automatically if necessary
		
		// normal for: x^2 + px + q
		double p = coeff[1] / (2.0 * coeff[2]);
		double q = coeff[0] / coeff[2]; 
		double D = (p * p) - q;

		if (isZero(D)){
			// one double root
			s.add(-p);
			s.add(-p);
			return s;
	    }

		if (D < 0.0){
			// no real root
			return null;
		} else {
			// two real roots
			double sqrt_D = Math.sqrt(D);
			s.add(sqrt_D - p);
			s.add(-sqrt_D - p);			
			return s;
	    }
	}

	
	
	private static boolean isZero(double x){
		double EQN_EPS = 1e-11; 
		return isZero(x, EQN_EPS);
	}
	  
	private static boolean isZero(double x, double l){
		return ((x > -l) && (x < l));
	}
	
	
	
}
	

