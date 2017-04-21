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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * To instantiate, define and handle circles
 * 
 */

public class Circle {
	
	public static final double[] numPropUnitCircle = {1.0,0.0,0.0};
	
	// Data fields
	protected String label= "";

	protected double r = 1;   //radius

	protected double xc = 0;  //x-coordinate for centre of circle
	protected double yc = 0;  //y-coordinate for centre of circle
	
	
	// Specific types of circles
	public static Circle unitCircle(){
		return unitCircle("");
	}
	public static Circle unitCircle(String label){
		return new Circle(label, numPropUnitCircle[0], numPropUnitCircle[1], numPropUnitCircle[2]);
	}
	
	
	// Constructor	
	public Circle (String label, double r, double xc, double yc){
		setAllProps(label, r, xc, yc);
	}
	public void resetToUnitCircle(){
		resetToUnitCircle("");
	}
	public void resetToUnitCircle(String label){
		setAllProps(label, numPropUnitCircle[0], numPropUnitCircle[1], numPropUnitCircle[2]);
	}
	private void setAllProps(String label, double r, double xc, double yc){
		this.label = label;
		this.r = r;
		this.xc = xc;
		this.yc = yc;
	}

	
	// Properties -> getters and setters
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getR() {
		return r;
	}

	public void setR(double r) {
		this.r = r;
	}

	public double getXc() {
		return xc;
	}

	public void setXc(double xc) {
		this.xc = xc;
	}

	public double getYc() {
		return yc;
	}

	public void setYc(double yc) {
		this.yc = yc;
	}
	
	
	// Methods
	
	public ArrayList<Point2D.Double> intpnts_WithLine (Line2D.Double line){
		//  the intersection of a circle and a line
		
		ArrayList<Point2D.Double> intPnts = new ArrayList<Point2D.Double>(2); 
		
		// Based on the idea of putting the equation of the circle and that of the line equal to each other
		// ... and then using the standard formula to find the discriminant and the roots
		// ... This method also handles cases when the gradient is infinity (thus the equation of the line has the form x=c) and
		// ... when the gradient is 0 (in which case the general method holds)
		
		MyLine lineEq = new MyLine(line.x1, line.y1, line.x2, line.y2, true);
	
		double a;
		double b; 
		double c;
		
		if (Double.isInfinite(lineEq.getGradient())){
			// when the equation of the line is of the form x=c
			// the equation to find the roots will give us the y values (instead of the x values) corresponding to the x value c
			a = 1;
			b =  - 2 * this.yc;
			c = Math.pow(this.yc, 2) + Math.pow(lineEq.getYIntercept()-this.xc, 2) - Math.pow(this.r, 2);
		
		} else {
			// the following is also applicable when the gradient of the line is 0
			a = 1 + Math.pow(lineEq.getGradient(), 2);
			b = 2 * ((lineEq.getGradient() * (lineEq.getYIntercept() - this.yc)) - this.xc);
			c = Math.pow(this.xc, 2) + Math.pow(lineEq.getYIntercept()-this.yc, 2) - Math.pow(this.r, 2);
		}
		
		double d = (b*b) - (4.0*a*c); // discriminant 
		
		if (d < 0.0){return null;}    // no intersections
		
		double sqrt_d = Math.sqrt(d);
		
		if (Double.isInfinite(lineEq.getGradient())){
			double y = (-b + sqrt_d)/(a + a);
			intPnts.add(new Point2D.Double(lineEq.getYIntercept(), y));
			y = (-b - sqrt_d)/(a + a);
			intPnts.add(new Point2D.Double(lineEq.getYIntercept(), y));
		} else {
			double x = (-b + sqrt_d)/(a + a);
			intPnts.add(new Point2D.Double(x, lineEq.getYKnowingX(x)));
			x = (-b - sqrt_d)/(a + a);
			intPnts.add(new Point2D.Double(x, lineEq.getYKnowingX(x)));
		}

		return intPnts;
	}
}
