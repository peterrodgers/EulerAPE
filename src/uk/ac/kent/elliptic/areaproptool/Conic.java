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
import java.awt.geom.Point2D;
import java.util.ArrayList;


/**
 * To instantiate, define and handle conics
 * 
 */


public class Conic {
	
	// coefficients 
	protected double A;
	protected double B;
	protected double C;
	protected double D;
	protected double E;
	protected double F;
	
	
	public Conic (Ellipse e){
		generateEllipseConicCoeffs(e);
	}
	
	public Conic(double in_A, double in_B, double in_C, double in_D, double in_E, double in_F){
		setAllProps (in_A, in_B, in_C, in_D, in_E, in_F);
	}
	public void resetPropAsIfNew(double in_A, double in_B, double in_C, double in_D, double in_E, double in_F){
		setAllProps (in_A, in_B, in_C, in_D, in_E, in_F);
	}
	private void setAllProps (double in_A, double in_B, double in_C, double in_D, double in_E, double in_F){
		A = in_A;
		B = in_B;
		C = in_C;
		D = in_D;
		E = in_E;
		F = in_F;
	}
	
	
	
	private void generateEllipseConicCoeffs (Ellipse e){
		
		// common coefficients
		
		double sqr_r1 = e.getA();
		double sqr_r2 = e.getB();
		
		sqr_r1 *= sqr_r1;
		sqr_r2 *= sqr_r2;
		
		double sin_rot = Math.sin(Math.toRadians(e.getRot()));
		double cos_rot = Math.cos(Math.toRadians(e.getRot())); 
		
		double sin_2rot = 2.0 * sin_rot * cos_rot;
		double sqr_sin_rot = Math.pow(sin_rot, 2);
		double sqr_cos_rot = Math.pow(cos_rot, 2);
		
		double centre_x = e.getXc();
		double centre_y = e.getYc();
		double sqr_centre_x = Math.pow(centre_x, 2);
		double sqr_centre_y = Math.pow(centre_y, 2);
		
		double inv_sqr_r1 = 1.0/sqr_r1;
		double inv_sqr_r2 = 1.0/sqr_r2;	
		
		// Compute the coefficients. These formulae are the transformations
	    //   on the unit circle written out long hand
		this.A = (sqr_cos_rot/sqr_r1) + (sqr_sin_rot/sqr_r2);
		this.B = (sqr_r2 - sqr_r1) * (sin_2rot / (2.0 * sqr_r1 * sqr_r2));
		this.C = (sqr_cos_rot/sqr_r2) + (sqr_sin_rot/sqr_r1);
		this.D = - (centre_y * this.B) - (centre_x * this.A);
		this.E = - (centre_x * this.B) - (centre_y * this.C);
		this.F = -1.0 + ((sqr_centre_x + sqr_centre_y) * (inv_sqr_r1 + inv_sqr_r2) / 2.0) + 
				 ((sqr_cos_rot - sqr_sin_rot) * (sqr_centre_x - sqr_centre_y) * (inv_sqr_r1 - inv_sqr_r2)/2.0) +
				 (centre_x * centre_y * (inv_sqr_r1 - inv_sqr_r2) * sin_2rot);
	}
	
	
	public void transform (TransformationMatrix transMatrix){
	
		// compute the transformation using matrix muliplication
		double[][] conicMatrix = new double[3][3];
		conicMatrix[0][0] = this.A;
		conicMatrix[0][1] = this.B;
		conicMatrix[1][0] = this.B;
		conicMatrix[1][1] = this.C;
		conicMatrix[0][2] = this.D;
		conicMatrix[2][0] = this.D;
		conicMatrix[1][2] = this.E;
		conicMatrix[2][1] = this.E;
		conicMatrix[2][2] = this.F;
		
		// inverse transformation
		D = (transMatrix.a * transMatrix.d) - (transMatrix.b * transMatrix.c);
		
		double[][] inverseMatrix = new double[3][3];
		inverseMatrix[0][0] = transMatrix.d / D;
		inverseMatrix[0][1] = - transMatrix.b / D;
		inverseMatrix[0][2] = 0.0;
		inverseMatrix[1][0] = - transMatrix.c / D;
		inverseMatrix[1][1] = transMatrix.a / D;
		inverseMatrix[1][2] = 0.0;
		inverseMatrix[2][0] = ((transMatrix.c * transMatrix.n) - (transMatrix.d * transMatrix.m)) / D;
		inverseMatrix[2][1] = ((transMatrix.b * transMatrix.m) - (transMatrix.a * transMatrix.n)) / D;
		inverseMatrix[2][2] = 1.0;		
	
		// compute transpose
		double[][] transInverseMatrix = new double[3][3];
		int i; int j;
		for (i = 0; i < 3; i++){
			for (j = 0; j < 3; j++){
				transInverseMatrix[j][i] = inverseMatrix[i][j];
			}
		}

	    // multiply the matrices
		double[][] result1 = new double[3][3];
		double[][] result2 = new double[3][3];
			
		result1 = MatricesOperations.mult2Mat(inverseMatrix, conicMatrix);
		result2 = MatricesOperations.mult2Mat(result1, transInverseMatrix);
		
	    this.A = result2[0][0];	       // return to conic form
		this.B = result2[0][1];
		this.C = result2[1][1];
		this.D = result2[0][2];
		this.E = result2[1][2];
		this.F = result2[2][2];
		
		//cleanup
		conicMatrix = null;
		inverseMatrix = null;
		transInverseMatrix = null;
		result1 = null;
		result2 = null;
	}
	
	
	public ArrayList<Double> getYKnowingX (double x){
		
		double[] polynomialCoeff = new double[3];
		polynomialCoeff[0] = (this.A*x*x) + (2*this.D*x) + this.F;
		polynomialCoeff[1] = (this.B*x) + (2*this.E);
		polynomialCoeff[2] = this.C;
		
		ArrayList<Double> ys = RootFinder.solveQuadric(polynomialCoeff);
		
		// cleanup
		polynomialCoeff = null;
		
		// return result
		if ((ys==null)||(ys.size() == 0)){
			return null;
		} else {
			return ys;
		}
	}
	
	
	public ArrayList<Double> getXKnowingY (double y){
		
		double[] polynomialCoeff = new double[3];
		polynomialCoeff[0] = (this.C*y*y) + (2*this.E*y) + this.F;
		polynomialCoeff[1] = (this.B*y) + (2*this.D);
		polynomialCoeff[2] = this.A;
		
		ArrayList<Double> xs = RootFinder.solveQuadric(polynomialCoeff);
		
		// cleanup
		polynomialCoeff = null;
		
		// return result
		if ((xs==null)||(xs.size() == 0)){
			return null;
		} else {
			return xs;
		}
	}
	
	
	public Polygon toPolygon (){
	
		ArrayList<Point2D.Double> pnts = new ArrayList<Point2D.Double>();
		double y;
		ArrayList<Double> xs;
		for (y = -200; y <= 200; y++){
			xs = getXKnowingY(y);
			if (!(xs==null)&& (xs.size()>0)){
				for (double x : xs){
					pnts.add(new Point2D.Double(x, y));
				}
			}
		}
		
		int poly_pnts = pnts.size();
		int[] poly_xs = new int[poly_pnts];
		int[] poly_ys = new int[poly_pnts];
		
		int i = 0;
		for (Point2D.Double pnt : pnts){
			poly_xs[i]= (int)Math.round(pnt.x);
			poly_xs[i]= (int)Math.round(pnt.y);
			i++;
		}
		
		return new Polygon (poly_xs, poly_ys, poly_pnts);
	}

}
