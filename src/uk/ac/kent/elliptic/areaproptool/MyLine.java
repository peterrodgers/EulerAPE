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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * 
 * To instantiate, define and manage lines
 *
 */


public class MyLine {

	public static final double PRECISION_ISZERO_FOR_GRAD = 1e-8; 
	
	private Double x1 = null;
	private Double y1 = null;
	private Double x2 = null;
	private Double y2 = null;

	private Double m = null; //gradient
	private Double c = null; //y-intercept

	private boolean m_roundIfCloseToZero = false;
	
	
	public MyLine (double gradient, double yIntercept, boolean gradient_roundIfCloseToZero){
		setPropertiesGivenGradYInt(gradient, yIntercept, gradient_roundIfCloseToZero);
	}
	public MyLine (double in_x1, double in_y1, double in_x2, double in_y2, boolean gradient_roundIfCloseToZero){
		setPropertiesForLineSeg (in_x1, in_y1, in_x2, in_y2, gradient_roundIfCloseToZero);
	}
	public MyLine (Point2D.Double inPnt1, Point2D.Double inPnt2, boolean gradient_roundIfCloseToZero){
		setPropertiesForLineSeg (inPnt1.x, inPnt1.y, inPnt2.x, inPnt2.y, gradient_roundIfCloseToZero);
	}
	public MyLine (Line2D.Double line, boolean gradient_roundIfCloseToZero){
		setPropertiesForLineSeg (line.x1, line.y1, line.x2, line.y2, gradient_roundIfCloseToZero);
	}
	
	private void setPropertiesGivenGradYInt(double in_gradient, double in_yIntercept, boolean gradient_roundIfCloseToZero){
		m = in_gradient;
		c = in_yIntercept;
		m_roundIfCloseToZero = gradient_roundIfCloseToZero;
		
		x1 = null; //these declarations are especially important when resetting the properties
		y1 = null;
		x2 = null;
		y2 = null;
	}
	private void setPropertiesForLineSeg(double in_x1, double in_y1, double in_x2, double in_y2, boolean gradient_roundIfCloseToZero){
		x1 = in_x1;
		y1 = in_y1;
		x2 = in_x2;
		y2 = in_y2;
		m_roundIfCloseToZero = gradient_roundIfCloseToZero;
		
		m = null; //these declarations are especially important when resetting the properties
		c = null;
		
		computeGradient();
		computeYIntercept();
	}
	
	public void resetProperties (double in_gradient, double in_yIntercept, boolean gradient_roundIfCloseToZero){
		setPropertiesGivenGradYInt(in_gradient, in_yIntercept, gradient_roundIfCloseToZero);
	}
	public void resetProperties (double in_x1, double in_y1, double in_x2, double in_y2, boolean gradient_roundIfCloseToZero){
		setPropertiesForLineSeg(in_x1, in_y1, in_x2, in_y2, gradient_roundIfCloseToZero);
	}



	//get set


	//Methods

	public Point2D.Double getxyAbsDistances (){
		Point2D.Double xyAbsDistances = getxySignedDistances();
		xyAbsDistances.x = Math.abs(xyAbsDistances.x);
		xyAbsDistances.y = Math.abs(xyAbsDistances.y);
		return xyAbsDistances;
	}
	public Point2D.Double getxySignedDistances (){
		Point2D.Double xyDistances = new Point2D.Double();
		xyDistances.x = x2-x1;
		xyDistances.y = y2-y1;
		return xyDistances;
	}
	public Double getLineLengthSqrd (){
		Point2D.Double xyAbsDistances = getxyAbsDistances();
		return (Math.pow(xyAbsDistances.x, 2)+ Math.pow(xyAbsDistances.y, 2));
	}
	public Double getLineLength (){
		return (Math.sqrt(getLineLengthSqrd()));
	}


	public double getVectorPntsDeterminant(){
		return ((x1*y2)-(x2*y1));
	}

	private void computeGradient(){
		if ((this.x1 != null) && (this.y1 != null) && (this.x2 != null) && (this.y2 != null)) {
			this.m = (this.y2-this.y1)/(this.x2-this.x1);
			
			if (m_roundIfCloseToZero && isZero(this.m, PRECISION_ISZERO_FOR_GRAD)){this.m = 0.0;} 
			//this is required to find the intersection points of the perpendicular line of ips 
			//...to ellipse c: the gradient of the line is nearly 0 (~1e-9), thus gradient of perpendicular line is very large => nearly a vertical line
			//...and for some reason, was getting in correct intersection points, leading to incorrect areas
		}
	}

	private void computeYIntercept(){
		if ((this.x1 != null) && (this.y1 != null) && (this.x2 != null) && (this.y2 != null)) {
			double m = this.getGradient();
			if (Double.isInfinite(m)){
				this.c = this.x1; // if gradient is infinite (line is vertical => x1=x2), return x-intercept instead, that is value of x when y=0
			} else {
				this.c = this.y1 - (this.getGradient()*this.x1);
			}
		}
	}

	public Double getGradient(){
		if (this.m != null){
			return this.m;
		} else if ((this.x1 != null) && (this.y1 != null) && (this.x2 != null) && (this.y2 != null)) {
			computeGradient();
			return this.m;
		} else {
			return null;
		}
	}
	public Double getYIntercept(){
		if (this.c != null){
			return this.c;
		} else if ((this.x1 != null) && (this.y1 != null) && (this.x2 != null) && (this.y2 != null)) {
			computeYIntercept();
			return this.c;
		} else {
			return null;
		}
	}

	public double getYKnowingX (double x){
		if (Double.isInfinite(this.getGradient())){
			return this.getYIntercept();
		} else {
			return ((this.getGradient()*x)+this.getYIntercept());
		}
	}
	public double getXKnowingY (double y){
		//not sure if it works when have lines such as x=a 
		if (Double.isInfinite(this.getGradient())){ 
			return this.getYIntercept();
		} else {
			return ((y-this.getYIntercept())/this.getGradient());
		}
	}

	public boolean isPointOnLine(Point2D.Double pnt){
		return (pnt.y == getYKnowingX(pnt.x));
	}
	public boolean isPointOnLineSeg(Point2D.Double pnt) {
		return isPointOnLineSeg(pnt, new Point2D.Double(0.0,0.0));
	}
	public boolean isPointOnLineSeg(Point2D.Double pnt, Point2D.Double xyAllowance){
		if (!isPointOnLine(pnt)){return false;}
		double xbound1 = this.x1;
		double xbound2 = this.x2;
		double ybound1 = this.y1;
		double ybound2 = this.y2;
		if (this.x1 > this.x2){
			xbound1 = this.x2;
			xbound2 = this.x1;
		}
		if (this.y1 > this.y2){
			ybound1 = this.y2;
			ybound2 = this.y1;
		}
		xbound1 -= xyAllowance.x;
		xbound2 += xyAllowance.x;
		ybound1 -= xyAllowance.y;
		ybound2 += xyAllowance.y;

		return ((pnt.x >= xbound1) && (pnt.x <= xbound2) && (pnt.y >= ybound1) && (pnt.y <= ybound2));
	}

	public boolean doesLineIntersectLine(MyLine lineSeg2){
	// here consider actual lines (represented by an equation) NOT line segments
		return (this.getGradient() != lineSeg2.getGradient());
	}
	public Point2D.Double getLineIntersectionPointWithLine(MyLine lineSeg2){
	// here consider actual lines (represented by an equation) NOT line segments

		if (!doesLineIntersectLine(lineSeg2)){return null;}

		Point2D.Double intPnt = new Point2D.Double(0,0);
		double l1Grad = this.getGradient();
		double l1YInt = this.getYIntercept();
		double l2Grad = lineSeg2.getGradient();
		double l2YInt = lineSeg2.getYIntercept();

		// equation: x = (c2-c1)/(m1-m2) , y -> substitute in one of the line equations
		intPnt.x = (l2YInt-l1YInt) / (l1Grad-l2Grad);
		intPnt.y = getYKnowingX(intPnt.x);

		return intPnt;
	}
	public Point2D.Double getLineIntersectionPointWithLineSeg(MyLine lineSeg2){
		Point2D.Double intPnt = getLineIntersectionPointWithLine(lineSeg2);
		if (intPnt == null){return null;}
		if (lineSeg2.isPointOnLineSeg(intPnt)){
			return intPnt;
		}else{
			return null;
		}
	}
	public Point2D.Double getLineSegIntersectionPointWithLine(MyLine lineSeg2){
		Point2D.Double intPnt = getLineIntersectionPointWithLine(lineSeg2);
		if (intPnt == null){return null;}
		if (this.isPointOnLineSeg(intPnt)){
			return intPnt;
		}else{
			return null;
		}
	}
	public Point2D.Double getLineSegIntersectionPointWithLineSeg(MyLine lineSeg2){
		Point2D.Double intPnt = getLineIntersectionPointWithLine(lineSeg2);
		if (intPnt == null){return null;}
		if (this.isPointOnLineSeg(intPnt)&&lineSeg2.isPointOnLineSeg(intPnt)){
			return intPnt;
		}else{
			return null;
		}
	}

	public double getPerpLineGradient(){
		return (-1/getGradient());
	}
	public double getPerpLineYIntercept(Point2D.Double pntOnPerpLine){
		// when m = 0, then line has the form y = c
		// when m = infinity, then line has the form x = d
		// here we use the yIntercept to keep the value of c or d in such cases
		double m = getPerpLineGradient();
		if (Double.isInfinite(m)){ 
			return (pntOnPerpLine.x); // if gradient is infinite (because the line is vertical), return x-intercept instead, that is value of x when y=0
		} else {
			return (pntOnPerpLine.y - (getPerpLineGradient()*pntOnPerpLine.x));
		}
	}
	public Point2D.Double getIntPntWithPerpLine(Point2D.Double pntOnPerpLine){
		double lineGrad = getGradient();
		double lineYInt = getYIntercept();

		double perpLineGrad = getPerpLineGradient();
		double perpLineYInt = getPerpLineYIntercept(pntOnPerpLine);

		double x = (perpLineYInt - lineYInt) / (lineGrad - perpLineGrad);
		double y = getYKnowingX(x); 

		return (new Point2D.Double(x,y));
	}

	
	public Point2D.Double getVectorGradient(){
		// computed as pnt2 - pnt 1 => assuming that the fixed point in the equation of the line in pnt1
		
		if ((this.x1 == null) || (this.x2 == null) || (this.y1 == null) || (this.y2 == null)){
			System.out.println("MyLine.getVectorGradient(): cannot compute vector gradient -> two points on the line are required and currently these are missing");
			return null;
		}
		
		return (new Point2D.Double (x2-x1, y2-y1));
	}
	
	public Double getX_ParamVectorEq (double t){
		// vector gradient computed as pnt2 - pnt 1 => assuming that the fixed point in the equation of the line in pnt1
		
		Point2D.Double m_vector = getVectorGradient();
		if (m_vector == null){return null;}
		
		return (this.x1 + (m_vector.x * t));
	}
	
	public Double getY_ParamVectorEq (double t){
		// vector gradient computed as pnt2 - pnt 1 => assuming that the fixed point in the equation of the line in pnt1
		
		Point2D.Double m_vector = getVectorGradient();
		if (m_vector == null){return null;}
		
		return (this.y1 + (m_vector.y * t));
	}
	
	// using a Parametric form for the line
	public Double definiteArea_IntegParamDx (double[] t_limits){
	
		Point2D.Double m_vector = getVectorGradient();
		if (m_vector == null){return null;}
		
		double result_l0 = (x1 * t_limits[0]) + ((m_vector.x * Math.pow(t_limits[0], 2))/2); 
		double result_l1 = (x1 * t_limits[1]) + ((m_vector.x * Math.pow(t_limits[1], 2))/2); 
		
		return Math.abs(result_l1 - result_l0);
	}
		
	// using a Parametric form for the line
	public Double definiteArea_IntegParamDy (double[] t_limits){
		
		Point2D.Double m_vector = getVectorGradient();
		if (m_vector == null){return null;}
		
		double result_l0 = (y1 * t_limits[0]) + ((m_vector.y * Math.pow(t_limits[0], 2))/2); 
		double result_l1 = (y1 * t_limits[1]) + ((m_vector.y * Math.pow(t_limits[1], 2))/2); 
		
		return Math.abs(result_l1 - result_l0);
	}
	
	// using a Cartesian form for the line
	public Double definiteArea_IntegCartDx (double[] limits){
		
		double m = getGradient();  
		double c = getYIntercept();
		
		double result_l0 = ((m/2) * Math.pow(limits[0], 2)) + (c * limits[0]); 
		double result_l1 = ((m/2) * Math.pow(limits[1], 2)) + (c * limits[1]);
		
		return (result_l1 - result_l0);
	}
	
	
	// using a Cartesian form for the line
	public Double definiteArea_IntegCartDy (double[] limits){
		
		double m = getGradient();
		double c = getYIntercept();
		
		
		double result_l0 = (1/m) * ((Math.pow(limits[0], 2)/2) - (c * limits[0])); 
		double result_l1 = (1/m) * ((Math.pow(limits[1], 2)/2) - (c * limits[1])); 
		
		double area = result_l1 - result_l0;
		
		return area;
	}
	

	// dealing with precision issues
	private boolean isZero(double x, double l){
		return ((x > -l) && (x < l));
	}


}
