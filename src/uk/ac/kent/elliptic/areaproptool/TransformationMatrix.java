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
 * 
 * To transform matrices
 *
 */


public class TransformationMatrix {

	
	public static final double[] propIdMatrix = {1.0,0.0,0.0,1.0,0.0,0.0};
	
	// Data fields
	
	// tranformation coefficients 
	protected double a;
	protected double b;
	protected double c;
	protected double d;
	
	// translation coefficients
	protected double m;
	protected double n;
	
	/*
	 * where transf matrix has the form 
	 *  -       -
	 * | a  b  0 |
	 * | c  d  0 |
	 * | m  n  1 |
	 *  -       -
	 */
	

	// Constructor
	
	public TransformationMatrix(double in_a, double in_b, double in_c, double in_d, double in_m, double in_n){
		a = in_a;
		b = in_b;
		c = in_c;
		d = in_d;
		m = in_m;
		n = in_n;
	}
	
	public void resetToIdMatrix(){
		a = propIdMatrix[0];
		b = propIdMatrix[1];
		c = propIdMatrix[2];
		d = propIdMatrix[3];
		m = propIdMatrix[4];
		n = propIdMatrix[5];
	}
	
	
	
	
	// Methods 
	
	public void translate(double in_m, double in_n){
		this.m += in_m;
		this.n += in_n;
	}
	
	public void rotate (double rot){

		double SinRot = Math.sin(Math.toRadians(rot));
		double CosRot = Math.cos(Math.toRadians(rot));
		
		double a_orig = this.a;
		double b_orig = this.b;
		double c_orig = this.c;
		double d_orig = this.d;
		double m_orig = this.m;
		double n_orig = this.n;
	
		
		this.a = (a_orig*CosRot) - (b_orig*SinRot);
		this.b = (b_orig*CosRot) + (a_orig*SinRot); 
		this.c = (c_orig*CosRot) - (d_orig*SinRot); 
		this.d = (d_orig*CosRot) + (c_orig*SinRot); 
		this.m = (m_orig*CosRot) - (n_orig*SinRot); 
		this.n = (n_orig*CosRot) + (m_orig*SinRot);
	}

	
	public void scale (double scale_x, double scale_y){
		this.a *= scale_x;
		this.b *= scale_y;
		this.c *= scale_x;
		this.d *= scale_y;
		this.m *= scale_x;
		this.n *= scale_y;
    }
	
	public Point2D.Double transformPoint (Point2D.Double pnt){
		Point2D.Double transPnt = new Point2D.Double();
		
		transPnt.x = (pnt.x * this.a) + (pnt.y * this.c) + this.m;
		transPnt.y = (pnt.x * this.b) + (pnt.y * this.d) + this.n;

		return transPnt;
		
	}
	

	
	
	// Specific transformation matrices
	
	// ... identity matrix
	
	public static TransformationMatrix IdMatrix(){
		return new TransformationMatrix (propIdMatrix[0], propIdMatrix[1], propIdMatrix[2],
				                         propIdMatrix[3], propIdMatrix[4], propIdMatrix[5]);
	}
	
	// ... matrix to transform an ellipse to a circle and vice-versa 
	//      -> these are invoked by Ellipse.getIntPnts_WithEllipse() (to find the intersection points of 2 ellipses)
	public static TransformationMatrix ellipseToCircleTransMatrix (Ellipse e){
		return ellipseToCircleTransMatrix(e, TransformationMatrix.IdMatrix());
	}
	public static TransformationMatrix ellipseToCircleTransMatrix (Ellipse e, TransformationMatrix cirMatrix){
		// with this method a transformation matrix instance can be reused to avoid getting out of memory 
		
		// Start with identity matrix
		cirMatrix.resetToIdMatrix();

		// Translate to origin
		cirMatrix.translate (-e.getXc(), -e.getYc());

		// Rotate into standard position
		cirMatrix.rotate(-e.getRot()); 
		
		// Scale into a circle
		cirMatrix.scale(1.0/e.getA(), 1.0/e.getB());

		return cirMatrix;
	}
	
	
	public static TransformationMatrix invEllipseToCircle (Ellipse e){
		return invEllipseToCircle(e, TransformationMatrix.IdMatrix());
	}
	
	public static TransformationMatrix invEllipseToCircle (Ellipse e, TransformationMatrix invMat){
		// with this method a transformation matrix instance can be reused to avoid getting out of memory  
		
		// Start with identity matrix
		invMat.resetToIdMatrix();

		// Scale back into an ellipse
		invMat.scale(e.getA(), e.getB());

		// Rotate
		invMat.rotate(e.getRot());
		
		// Translate from origin
		invMat.translate(e.getXc(), e.getYc());

		return invMat;
	}


}
