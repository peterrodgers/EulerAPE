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
 


package elliptic.areaproptool;


import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;


/**
 * Geometric Operations
 * 
 */


public class GeometricOps {


	// Change coordinate systems 

	public static double getPolarCoordAngForPnt (Point2D.Double pnt, Point2D.Double centre){
		// outputs angle in radians form
		
		// check if null to avoid exceptions
		if (centre == null){
			centre = new Point2D.Double (0, 0);
		}

		double ang = 0.0;

		double dx = Math.abs (pnt.x - centre.x);
		double dy = Math.abs (pnt.y - centre.y);

		double unverifiedAng = Math.abs(Math.atan(dy/dx));

		
		if ((centre.x < pnt.x) && (centre.y < pnt.y)){   // this should apply even in cases when dx is very close to 0 as tan^-1(dy/a value close to 0) should still return a value very close to 1
			ang = unverifiedAng;
			
		} else if ((dx==0) && (centre.x == pnt.x) && (centre.y < pnt.y)){
			ang = Math.PI / 2;
		
		} else if ((centre.x > pnt.x) && (centre.y < pnt.y)){
			ang = Math.PI - unverifiedAng;

		} else if ((dy==0) && (centre.x > pnt.x) && (centre.y == pnt.y)){
			ang = Math.PI;
		
		} else if ((centre.x > pnt.x) && (centre.y > pnt.y)){
			ang = Math.PI + unverifiedAng;
		
		} else if ((dx==0) && (centre.x == pnt.x) && (centre.y > pnt.y)){
			ang = (3 * Math.PI /2);
		
		} else if ((centre.x < pnt.x) && (centre.y > pnt.y)){
			ang = (2*Math.PI) - unverifiedAng;
		
		} else if ((dy==0) && (centre.x < pnt.x) && (centre.y == pnt.y)){
			ang = 0;
		}
		
		return  ang;
	}

	public static double getPolarCoordRForPnt (Point2D.Double pnt, Point2D.Double centre){

		// check if null to avoid exceptions
		if (centre == null){
			centre = new Point2D.Double (0, 0);
		}

		double dx = Math.abs (pnt.x - centre.x);
		double dy = Math.abs (pnt.y - centre.y);

		return (Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)));
	}

	
	// Points
	
	public static double distanceBetween2Points (Point2D.Double pnt1, Point2D.Double pnt2){
		
		double diff_x = Math.abs(pnt2.x - pnt1.x);
		double diff_y = Math.abs(pnt2.y - pnt1.y);
	
		return Math.sqrt(Math.pow(diff_x,2) + Math.pow(diff_y,2));	
	}
	
	public static Point2D.Double midPointOf2Points (Point2D.Double pnt1, Point2D.Double pnt2){
		return new Point2D.Double((pnt1.x+pnt2.x)/2, (pnt1.y+pnt2.y)/2);	
	}
	
	
	public static Point2D.Double getPerpPntOnParallelLine (Line2D.Double line, double perpDistBetLines, Point2D.Double pnt, boolean newPntAboveLine){		
		
		double ydDIVxd = (line.y1-line.y2)/(line.x1-line.x2);
		
		double lnSlopeAsAng_rad = Math.atan(Math.abs(ydDIVxd));

		// the slope of the parallel line that is perpDistBetLines away from the given line as an angle
		double parLnSlopeAsAng_rad = Math.toRadians(90) - lnSlopeAsAng_rad;
		
		double diffInX = perpDistBetLines * Math.cos(parLnSlopeAsAng_rad);
		double diffInY = perpDistBetLines * Math.sin(parLnSlopeAsAng_rad);
		if (ydDIVxd > 0){ // => +ve gradient
			if (newPntAboveLine){
				return (new Point2D.Double(pnt.x - diffInX, pnt.y + diffInY));
			} else {
				return (new Point2D.Double(pnt.x + diffInX, pnt.y - diffInY));
			}

		} else { // => -ve gradient
			if (newPntAboveLine){
				return (new Point2D.Double(pnt.x + diffInX, pnt.y + diffInY));
			} else {
				return (new Point2D.Double(pnt.x - diffInX, pnt.y - diffInY));
			}
		}
		
	}
	
	
	

	// Lines 
	
	public static Point2D.Double getIntPntsOfLines (Line2D.Double line1, Line2D.Double line2) {
	    Point2D.Double intpnt = new Point2D.Double(0.0,0.0);

		if (! line1.intersectsLine(line2) ){
			return null;

		} else {

			double l1x1 = line1.x1;
			double l1y1 = line1.y1;
			double l1xd = line1.x2-l1x1;
			double l1yd = line1.y2-l1y1;

			double l2x1 = line2.x1;
			double l2y1 = line2.y1;
			double l2xd = line2.x2-l2x1;
			double l2yd = line2.y2-l2y1;

			double det = - (l1yd * l2xd) + (l2yd * l1xd);

			if (det == 0) {
				return null;
			} else {
				double A1 = l1yd;
				double A2 = l2yd;
				double B1 = - l1xd;
				double B2 = - l2xd;
				double C1 = (l1yd * l1x1) - (l1xd * l1y1);
				double C2 = (l2yd * l2x1) - (l2xd * l2y1);

				intpnt.x = ((B2*C1) - (B1*C2))/det;
				intpnt.y = ((A1*C2) - (A2*C1))/det;

				return intpnt;
			}
		}
	}
	
	
	// Triangles 
	public static double areaOfTriangle (Point2D.Double pnt1, Point2D.Double pnt2, Point2D.Double base_pnt){
		Point2D.Double pnt1FromBasePnt = new Point2D.Double (pnt1.x - base_pnt.x, pnt1.y - base_pnt.y);
		Point2D.Double pnt2FromBasePnt = new Point2D.Double (pnt2.x - base_pnt.x, pnt2.y - base_pnt.y);
		double area = 0.5 * Math.abs((pnt1FromBasePnt.x * pnt2FromBasePnt.y) - (pnt1FromBasePnt.y * pnt2FromBasePnt.x));
		return area;
	}
	
	
	// Polygons 
	public static boolean isPointInPolygon(Polygon poly, Point2D.Double pnt){		
		double pntPosVar = 0.5;
		double sqPntPosVarWidth = pntPosVar*2;
		
		return ( poly.contains(pnt.x, pnt.y) || 
			     poly.intersects(pnt.x-pntPosVar, pnt.y-pntPosVar, sqPntPosVarWidth, sqPntPosVarWidth) );
	}
	
	
	
	

	
}
