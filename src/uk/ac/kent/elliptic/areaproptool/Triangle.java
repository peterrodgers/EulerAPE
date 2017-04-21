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

import java.awt.geom.Point2D;


/**
 * To instantiate, define and handle triangles
 * 
 */


public class Triangle {

	
	// Points defining the triangle 
	
	// ... pnt0 is considered to be the base point of the triangle -> this is especially important when computing the area of the triangle
	protected double pnt0_x;
	protected double pnt0_y;
	
	protected double pnt1_x;
	protected double pnt1_y;
	
	protected double pnt2_x;
	protected double pnt2_y;
	
	
	
	// Constructors
	
	public Triangle (double in_pnt0_x, double in_pnt0_y, double in_pnt1_x, double in_pnt1_y, double in_pnt2_x, double in_pnt2_y){
		pnt0_x = in_pnt0_x;
		pnt0_y = in_pnt0_y;
		
		pnt1_x = in_pnt1_x;
		pnt1_y = in_pnt1_y;
		
		pnt2_x = in_pnt2_x;
		pnt2_y = in_pnt2_y; 
	}
	
	public Triangle (Point2D.Double in_pnt0, Point2D.Double pnt1, Point2D.Double pnt2){
		pnt0_x = in_pnt0.x;
		pnt0_y = in_pnt0.y;
		
		pnt1_x = pnt1.x;
		pnt1_y = pnt1.y;
		
		pnt2_x = pnt2.x;
		pnt2_y = pnt2.y; 
	}
	
	
	
	// Properties

	public double getPnt0_x() {
		return pnt0_x;
	}
	public void setPnt0_x(double pnt0_x) {
		this.pnt0_x = pnt0_x;
	}
	public double getPnt0_y() {
		return pnt0_y;
	}
	public void setPnt0_y(double pnt0_y) {
		this.pnt0_y = pnt0_y;
	}
	public double getPnt1_x() {
		return pnt1_x;
	}
	public void setPnt1_x(double pnt1_x) {
		this.pnt1_x = pnt1_x;
	}
	public double getPnt1_y() {
		return pnt1_y;
	}
	public void setPnt1_y(double pnt1_y) {
		this.pnt1_y = pnt1_y;
	}
	public double getPnt2_x() {
		return pnt2_x;
	}
	public void setPnt2_x(double pnt2_x) {
		this.pnt2_x = pnt2_x;
	}
	public double getPnt2_y() {
		return pnt2_y;
	}
	public void setPnt2_y(double pnt2_y) {
		this.pnt2_y = pnt2_y;
	}
	
	
	
	// Methods
	
	public boolean isPointInTriangle (Point2D.Double pnt, boolean includeTriangleEdge){
		
		double leftMostPnt_x = Math.min(pnt0_x, Math.min(pnt1_x, pnt2_x));
		double rightMostPnt_x = Math.max(pnt0_x, Math.max(pnt1_x, pnt2_x));
		
		double bottomMostPnt_y = Math.min(pnt0_y, Math.min(pnt1_y, pnt2_y));
		double topMostPnt_y = Math.max(pnt0_y, Math.max(pnt1_y, pnt2_y));
	
		return ( ( ((leftMostPnt_x < pnt.x) && (pnt.x < rightMostPnt_x)) || (includeTriangleEdge && (leftMostPnt_x == pnt.x) && (pnt.x == rightMostPnt_x)) ) &&
				 ( ((bottomMostPnt_y < pnt.y) && (pnt.y < topMostPnt_y)) || (includeTriangleEdge && (bottomMostPnt_y == pnt.y) && (pnt.y == topMostPnt_y)) ) );
	}

	
	public Double computeArea (){

		// here assume that the base point is pnt0
		
		double base_pnt_x = pnt0_x;
		double base_pnt_y = pnt0_y;
		
		double pnt1FromBasePnt_x = pnt1_x - base_pnt_x;
		double pnt1FromBasePnt_y = pnt1_y - base_pnt_y;
		
		double pnt2FromBasePnt_x = pnt2_x - base_pnt_x;
		double pnt2FromBasePnt_y = pnt2_y - base_pnt_y;
		
		double area = 0.5 * Math.abs((pnt1FromBasePnt_x * pnt2FromBasePnt_y) - (pnt1FromBasePnt_y * pnt2FromBasePnt_x));
		
		return area;
	}
}























