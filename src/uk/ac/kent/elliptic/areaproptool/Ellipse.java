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

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


/**
 * To instantiate, define and handle ellipses
 * 
 */


public class Ellipse {

	// Data fields
	protected String label= " ";

	protected double a = 0;   //semi-major axis (from ellipse's centre to edge) - NB: in some cases (eg when running an optimizer) 'a' might not necessarily be smaller than 'b', BUT rot will always be the angle between a-axis and x-axis
	protected double b = 0;   //semi-minor axis (from ellipse's centre to edge)

	protected double xc = 0;  //x-coordinate for centre of ellipse
	protected double yc = 0;  //y-coordinate for centre of ellipse

	protected double rot = 0; //angle between the ellipse's a-axis (usually - but not necessarily - representing the semi-major axis) and the line parallel to the x-axis

	protected Color colour;
	protected Shape shape;
	protected Polygon asPolygon = null;
	protected ConcreteContour asConcreteContour = null;
	protected double xIntervalPoly = .5;


	// Constructor 
	public Ellipse (String in_label, double in_a, double in_b, double in_xc, double in_yc, double in_rot){
		label = in_label;
		a = in_a;
		b = in_b;
		xc = in_xc;
		yc = in_yc;
		rot = in_rot;
		asPolygon = null;
	}


	// Properties - getters and setters
	public String getLabel() {
		return this.label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public double getA() {
		return a;
	}
	public void setA(double a) {
		this.a = a;
		//resetAsPolygon();  OR  set polygon as null -> to avoid extra computation since polygon might not be used 
		//                       and getter of polygon (getAsPolygon()) recomputes the asPolygon if polygon is null 
		asPolygon = null;
	}
	public double getB() {
		return b;
	}
	public void setB(double b) {
		this.b = b;
		//resetAsPolygon();  OR  set polygon as null -> to avoid extra computation since polygon might not be used 
		//                       and getter of polygon (getAsPolygon()) recomputes the asPolygon if polygon is null 
		asPolygon = null;
	}
	public double getXc() {
		return xc;
	}
	public void setXc(double xc) {
		this.xc = xc;
		//resetAsPolygon();  OR  set polygon as null -> to avoid extra computation since polygon might not be used 
		//                       and getter of polygon (getAsPolygon()) recomputes the asPolygon if polygon is null 
		asPolygon = null;
	}
	public double getYc() {
		return yc;
	}
	public void setYc(double yc) {
		this.yc = yc;
		//resetAsPolygon();  OR  set polygon as null -> to avoid extra computation since polygon might not be used 
		//                       and getter of polygon (getAsPolygon()) recomputes the asPolygon if polygon is null 
		asPolygon = null;
	}
	public double getRot() {
		return rot;
	}
	public void setRot(double rot) {
		this.rot = rot;
		//resetAsPolygon();  OR  set polygon as null -> to avoid extra computation since polygon might not be used 
		//                       and getter of polygon (getAsPolygon()) recomputes the asPolygon if polygon is null 
		asPolygon = null;
	}
	
	
	public Ellipse clone(){
		return (new Ellipse(new String(label), a, b, xc, yc, rot));
	}
		
	
	public boolean isCircle(){ 
		return (this.a == this.b);
	}
	
	public double getRotInDeg(){
		return rot;
	}
	public double getRotInRad(){
		return (rot*Math.PI/180);
	}
	
	
	public void setColour (Color in_colour){
		colour = in_colour;
	}
	public void setShape(Shape in_shape){
		shape = in_shape;
	}

	public void setXIntervalPoly(int in_xInterval){
		xIntervalPoly = in_xInterval;
		resetAsPolygon();
	}
	
	
	public double[] getY (double x){
		return getY(x, this.rot);
	}
	public double[] getY (double x, double in_rot){
		// assume general eq: Ax^2 + Bxy + Cy^2 + 2Dx + 2Ey + F = 0
		// then using formula to find the roots of a quadratic equation
		// y = (-b +- sqrt(b^2 - 4ac)) / 2a
		// => y = (-(Bx+2E) +- sqrt((Bx+2E)^2 - 4C(Ax^2 + 2Dx + F))) / 2C;

		Conic c_e = new Conic(new Ellipse(this.label, this.a, this.b, this.xc, this.yc, in_rot));
		
		double aTerm = c_e.C;
		double bTerm = (c_e.B * x) + (2 * c_e.E);
		double cTerm = (c_e.A * x * x) + (2 * c_e.D * x) + c_e.F;
		
		double[] ys = new double[2];
		ys[0] = (-bTerm + Math.sqrt(Math.pow(bTerm, 2) - (4 * aTerm * cTerm) )) / (2 * aTerm);
		ys[1] = (-bTerm - Math.sqrt(Math.pow(bTerm, 2) - (4 * aTerm * cTerm) )) / (2 * aTerm);

		return (ys);
	}
	
	public double[] getX(double y){		
		return getX(y, this.rot);
	}
	public double[] getX (double y, double in_rot){
		// this is worked out in a similar manner as getY but adapted to get X knowing Y]
		// =>
		// assume general eq: Ax^2 + Bxy + Cy^2 + 2Dx + 2Ey + F = 0
		// then using formula to find the roots of a quadratic equation
		// y = (-b +- sqrt(b^2 - 4ac)) / 2a
		// => y = (-(By+2D) +- sqrt((By+2D)^2 - 4A(Cy^2 + 2Ey + F))) / 2A;
		
		Conic c_e = new Conic(new Ellipse(this.label, this.a, this.b, this.xc, this.yc, in_rot));
		
		double aTerm = c_e.A;
		double bTerm = (c_e.B * y) + (2 * c_e.D);
		double cTerm = (c_e.C * y * y) + (2 * c_e.E * y) + c_e.F;
		
		double[] xs = new double[2];
		xs[0] = (-bTerm + Math.sqrt(Math.pow(bTerm, 2) - (4 * aTerm * cTerm) )) / (2 * aTerm);
		xs[1] = (-bTerm - Math.sqrt(Math.pow(bTerm, 2) - (4 * aTerm * cTerm) )) / (2 * aTerm);

		return (xs);
	}

	public double getX_parametric (double t, boolean tInRadians){
		if (!tInRadians){
			t = Math.toRadians(t);
		}
		return (this.xc + (this.a*Math.cos(Math.toRadians(this.rot))*Math.cos(t)) - (this.b*Math.sin(Math.toRadians(this.rot))*Math.sin(t)));
	}
	public double getY_parametric (double t, boolean tInRadians){
		
		if (!tInRadians){
			t = Math.toRadians(t);
		}
		return (this.yc + (this.a*Math.sin(Math.toRadians(this.rot))*Math.cos(t)) + (this.b*Math.cos(Math.toRadians(this.rot))*Math.sin(t)));
	}

	public double getArea(){
		return (Math.PI * this.a * this.b);
	}

	public Area getShapeAreaForDisplay (Point2D.Double centreOfSystem){
		
		Ellipse2D e2D = new Ellipse2D.Double(-a, -b, (2*a), (2*b));
		
		AffineTransform at = AffineTransform.getTranslateInstance(xc+centreOfSystem.x, -yc+centreOfSystem.y);
        at.rotate(Math.toRadians(-rot));
        Shape rotEllipse = at.createTransformedShape(e2D);
        
        return (new Area(rotEllipse));
	}
	
	public Area getShapeAreaAsIs (){
		Ellipse2D e2D = new Ellipse2D.Double(-a, -b, (2*a), (2*b));
		
		AffineTransform at = AffineTransform.getTranslateInstance(xc, yc);
        at.rotate(Math.toRadians(rot));
        Shape rotEllipse = at.createTransformedShape(e2D);
        
        return (new Area(rotEllipse));
	}
	

	public Rectangle getBoundingBox (boolean coordSys_originMid_NOT_originTopLeft){ 
		//since we are using the getBoundingBox of Java then the coordinate system is as follows: x incr to right, y incr down
		Area setAsArea = EllipseDiagramOps.getSetAsArea(this, EllipseDiagramPanel.centreOfSystem);
		Rectangle setAsAreaBounds_yIncrDown = setAsArea.getBounds();
		
		if (!coordSys_originMid_NOT_originTopLeft){
			return setAsAreaBounds_yIncrDown;
		} else {
			Point2D.Double convertedPnt = Utilities.changeCoorSys_originTopLeft_to_originMid(new Point2D.Double(new Double(setAsAreaBounds_yIncrDown.x),new Double(setAsAreaBounds_yIncrDown.y)), EllipseDiagramPanel.centreOfSystem);
			return (new Rectangle((int)convertedPnt.x, (int)convertedPnt.y, setAsAreaBounds_yIncrDown.width, setAsAreaBounds_yIncrDown.height));
		}
	}
	

	public ConcreteContour getAsConcreteContour(){
		if (asConcreteContour == null){
			asConcreteContour = new ConcreteContour(this.label, getAsPolygon());
		}
		return asConcreteContour;
	}
	
	public void resetAsPolygon(){
		asPolygon = null;
		getAsPolygon();
	}
	public Polygon getAsPolygon(){
		if (asPolygon == null){
			asPolygon = toPolygon();
			asConcreteContour = new ConcreteContour(this.label, asPolygon);
		}
		return asPolygon;
	}

	private Polygon toPolygon(){
	
		int npnts = (int)(((2 * a) / xIntervalPoly) * 2);
		
		int[] xpnts = new int[npnts];
		int[] ypnts = new int[npnts];
		
		double[] xpntsDbl = new double[npnts];
		double[] ypntsDbl = new double[npnts];

		double leftmost_x = xc - a;

		double x = leftmost_x - xIntervalPoly;
		int midpntIndex = ((int)(npnts / 2) - 1) + (npnts % 2);
		double [] ys;
		for (int i=0; i <= midpntIndex; i++){
			x += xIntervalPoly;
			
			ys = getY(x,0); //get y when the ellipse has centre (xc,yc) but rotation 0
			// getY never returns null -> thus some points might be NaN and these are handled further down
			
			if ((Double.isNaN(ys[0]) || Double.isNaN(ys[1])) && (i<=0)){
				npnts -= 2;
				xpnts = new int[npnts];
				ypnts = new int[npnts];
				xpntsDbl = new double[npnts];
				ypntsDbl = new double[npnts];
				midpntIndex--;
				i--;
				continue;
			}
			if (Double.isNaN(ys[0])){
				ys[0] = ypntsDbl[i-1];
			}
			if (Double.isNaN(ys[1])){
				int indexCurrY_topHalf = npnts - i - 2;
				int indexPrevY_bottomHalf = npnts - i - 2;
				if (indexPrevY_bottomHalf == indexCurrY_topHalf){
					ys[1] = ys[0];
				} else {
					ys[1] = ypntsDbl[npnts - i - 2];
				}
			}

			Arrays.sort(ys);				
				
			xpntsDbl[i] = x;
			ypntsDbl[i] = ys[0];
	
			xpntsDbl[npnts - i - 1] = x; 
			ypntsDbl[npnts - i - 1] = ys[1]; 
		}
	
		double t_x; 
		double t_y;
		double rotRad;
		for (int p = 0; p < npnts; p++){
			//rotate the ellipse according to its specifications 
			//   -> the points obtained at this point are those of an ellipse with centre (xc,yc) and rotation 0 
			
			t_x = xpntsDbl[p]-this.xc;
			t_y = ypntsDbl[p]-this.yc;
			
			rotRad = Math.toRadians(-this.rot);
			
			xpnts[p] = (int) Math.round(this.xc + ( (t_x * Math.cos(rotRad)) + (t_y * Math.sin(rotRad))));
			ypnts[p] = (int) Math.round(this.yc + ( -(t_x * Math.sin(rotRad)) + (t_y * Math.cos(rotRad))));
		}

		return (new Polygon(xpnts, ypnts, npnts));
	}


	
	// Get equation for ellipse as a String 
	
	public String getStandardEq(){
		if ((rot == 0) && (xc == 0) && (yc ==0)){
			return ("( (x ^ 2 / " + Double.toString(a) + "^2 ) + " +
					"( (y ^ 2 / " + Double.toString(b) + "^2 )" +
					" = 1");
		} else if ( (rot == 0) && ((xc != 0) || (yc != 0)) ){
			return ("( (x - " + Double.toString(xc) + ") ^ 2 / " + Double.toString(a) + "^2 ) + " +
					"( (y - " + Double.toString(yc) + ") ^ 2 / " + Double.toString(b) + "^2 )" +
					" = 1");
		} else if ( (rot != 0) && (xc == 0) && (yc == 0) ){
			return ("( (x Cos" + Double.toString(rot) + " + y Sin" + Double.toString(rot) + ") ^ 2 / " + Double.toString(a) + "^2 ) + " +
					"( (y Cos" + Double.toString(rot) + " - x Sin" + Double.toString(rot) + ") ^ 2 / " + Double.toString(b) + "^2 )" +
					" = 1");
		} else {
			return ("( (x Cos" + Double.toString(rot) + " + y Sin" + Double.toString(rot) + " - " + Double.toString(xc) + ") ^ 2 / " + Double.toString(a) + "^2 ) + " +
					"( (y Cos" + Double.toString(rot) + " - x Sin" + Double.toString(rot) + " - " + Double.toString(yc) + ") ^ 2 / " + Double.toString(b) + "^2 )" +
					" = 1");
		}
	}

	private String sign(double n, String prevStr){
		if ((n > 0) && (prevStr != "")) {return "+ ";}
		else {return "";}
	}
	
	public String getGeneralEq (){
		double A = Math.pow(b* Math.cos(rot), 2) + Math.pow(a* Math.sin(rot), 2);
		double B = 2 * (Math.pow(b, 2) - Math.pow(a, 2)) * Math.cos(rot) * Math.sin(rot);
		double C = Math.pow(b* Math.sin(rot), 2) + Math.pow(a* Math.cos(rot), 2);

		double D = -2 * ( (Math.pow(b, 2) * xc * Math.cos(rot)) + (yc * Math.sin(rot)) );
		double E = -2 * ( (Math.pow(a, 2) * yc * Math.cos(rot)) + (xc * Math.sin(rot)) );

		double F = Math.pow(b * xc, 2) + Math.pow(a * yc, 2) - Math.pow(a * b, 2);

		String Astr = Double.toString(A) + " x^2 ";
		String Bstr = Double.toString(B) + " xy ";
		String Cstr = Double.toString(C) + " y^2 ";
		String Dstr = Double.toString(D) + " x ";
		String Estr = Double.toString(E) + " y ";
		String Fstr = Double.toString(F);
		String eqStr = "";
		if (A != 0) {eqStr += Astr;};
		if (B != 0) {eqStr += (sign(B, eqStr) + Bstr);};
		if (C != 0) {eqStr += (sign(C, eqStr) + Cstr);};
		if (D != 0) {eqStr += (sign(D, eqStr) + Dstr);};
		if (E != 0) {eqStr += (sign(E, eqStr) + Estr);};
		if (F != 0) {eqStr += (sign(F, eqStr) + Fstr);};
		eqStr += " = 0";

		return eqStr;
	}

	public String getParametricEqX(){
		// X(t) = xc + aCos(rot)Cos(t) - bSin(rot)Sin(t)

		String xcStr = Double.toString(xc);
		if (xc == 0) {xcStr = "";}

		double cosRot = Math.cos(rot);
		double sinRot = Math.sin(rot);

		double costTerm = a * cosRot;
		double sintTerm = b * sinRot;

		String costTermStr = " + " + Double.toString(costTerm) + " Cos(t)";
		String sintTermStr = " - " + Double.toString(sintTerm) + " Sin(t)";

		if (cosRot == 0){costTermStr = "";}
		else if ( costTerm < 0) {costTermStr = Double.toString(costTerm) + " Cos(t)";}
		else if (xc == 0) {costTermStr = Double.toString(costTerm) + " Cos(t)";}

		if (sinRot == 0){sintTermStr = "";}
		else if ( sintTerm < 0) {sintTermStr = " + " + Double.toString(Math.abs(sintTerm)) + " Sin(t)";}
		else if ((xc == 0) && (costTermStr == "")) {sintTermStr = Double.toString(sintTerm) + " Sin(t)";}

		return ("X(t) = " + xcStr + costTermStr + sintTermStr);
	}

	public String getParametricEqY(){
		// Y(t) = yc + aSin(rot)Cos(t) + bCos(rot)Sin(t)

		String ycStr = Double.toString(yc);
		if (yc == 0) {ycStr = "";}

		double sinRot = Math.sin(rot);
		double cosRot = Math.cos(rot);

		double costTerm = a * sinRot;
		double sintTerm = b * cosRot;

		String costTermStr = " + " + Double.toString(costTerm) + " Cos(t)";
		String sintTermStr = " + " + Double.toString(sintTerm) + " Sin(t)";

		if (sinRot == 0){costTermStr = "";}
		else if ( costTerm < 0) {costTermStr = Double.toString(costTerm) + " Cos(t)";}
		else if (yc == 0) {costTermStr = Double.toString(costTerm) + " Cos(t)";}

		if (cosRot == 0){sintTermStr = "";}
		else if ( sintTerm < 0) {sintTermStr = Double.toString(sintTerm) + " Sin(t)";}
		else if ((yc == 0) && (costTermStr == "")) {sintTermStr = Double.toString(sintTerm) + " Sin(t)";}

		return ("Y(t) = " + ycStr + costTermStr + sintTermStr);
	}

	public String[] getParametricEqs(){
		String [] eqs = new String [2];
		eqs[0] = getParametricEqX();
		eqs[1] = getParametricEqY();
		return eqs;
	}
	

	// Geometric operations/checks on ellipses
	
	public Boolean isPointInEllipse(Point2D.Double pnt, boolean includeEllipseEdge, double l){
		// before proceeding to the general and longer method, check whether: 1) the pnt is equal to the centre, 
		// 2) if the ellipse is rotated at some perpendicular angle, in which case a simple analysis using the semi-minor and major axis can be carried out 
		// => avoiding extra computation required in the general case
		
		DecimalFormat df = new DecimalFormat("##############################.####");
		double this_a_df = this.a;
		double this_b_df = this.b;
		double this_xc_df = this.xc;
		double this_yc_df = this.yc;
		double pnt_x_df = pnt.x;
		double pnt_y_df = pnt.y;	
		try {
			this_a_df = df.parse(df.format(this.a)).doubleValue();
			this_b_df = df.parse(df.format(this.b)).doubleValue();
			this_xc_df = df.parse(df.format(this.xc)).doubleValue();
			this_yc_df = df.parse(df.format(this.yc)).doubleValue();
			pnt_x_df = df.parse(df.format(pnt.x)).doubleValue();
			pnt_y_df = df.parse(df.format(pnt.y)).doubleValue();
		} catch (ParseException exception) {
			exception.printStackTrace();
		}
		
		
		// if the point is the same as the centre of the ellipse 
		if (areEqual(pnt_x_df, this_xc_df, l) && areEqual(pnt_y_df, this_yc_df, l)){
			return true;
		}
		
		// if the ellipse is not rotated or rotated at an angle of the form 180n (major axis -> horizontal),
		// or rotated at an angle of the form 90+180n (major axis -> vertical), use axis to determine whether in ellipse
		if ( isZero((this.rot%180),l) || isZero(((this.rot-90)%180),l) ){ 
			double distEllCentreToPnt = -1;
			double axisLength = -1; 
			
			if (areEqual (pnt_x_df, this_xc_df, l)){
				distEllCentreToPnt = Math.abs(this_yc_df- pnt_y_df);
				if (isZero ((this.rot%180),l)){
					axisLength = this_b_df;
				} else if (isZero(((this.rot-90)%180),l)) {
					axisLength = this_a_df;
				}
				
			} else if (areEqual (pnt_y_df, this_yc_df, l)){
				distEllCentreToPnt = Math.abs(this_xc_df- pnt_x_df);
				if (isZero((this.rot%180),l)){
					axisLength = this_a_df;
				} else if (isZero(((this.rot-90)%180),l)) {
					axisLength = this_b_df;
				}
				
			}
			
			if ((distEllCentreToPnt > -1) && (axisLength > -1)){
				boolean distEllCentreToPnt_axisLen_areEqual = areEqual(distEllCentreToPnt, axisLength, l);
				return (((distEllCentreToPnt < axisLength) && (!distEllCentreToPnt_axisLen_areEqual)) || 
						(includeEllipseEdge && distEllCentreToPnt_axisLen_areEqual));
				
				// NB: to check if the pnt is in the ellipse and outside or on the edge then need to carry out both of the following checks 
				//     ((distEllCentreToPnt < axisLength) && (!distEllCentreToPnt_axisLen_areEqual))
				//     as there might be cases where (distEllCentreToPnt < axisLength) is true, but in terms of the precision limits, 
				//     the 2 values are considered = rather than <
			}
		}
		
		
		
		// if ellipse is rotated at some angle,
		Point2D.Double ellCentreAsPnt = new Point2D.Double(this_xc_df, this_yc_df);
		double distCentreToPnt = GeometricOps.distanceBetween2Points(ellCentreAsPnt, pnt);
		try {
			distCentreToPnt = df.parse(df.format(distCentreToPnt)).doubleValue();
		} catch (ParseException exception) {
			exception.printStackTrace();
			System.out.println("Ellipse.isPointInEllipse: formatting distance 1");
		}

		
		// if circle 
		if (this.isCircle()){
			//doesn't make a difference whether use this.a or this.b for radius because they are equal since it is a circle
			boolean distCentreToPnt_radius_areEqual = areEqual(distCentreToPnt, this_a_df, l); //avoid declaring a variable for the radius i.e. avoid double radius = this.a
			return (((distCentreToPnt < this_a_df) && (!distCentreToPnt_radius_areEqual)) || 
					 (includeEllipseEdge && distCentreToPnt_radius_areEqual));
		}
	
		
		
		// if < min semi-major axis or if > max semi-major axis
		
		// ... compare distance between centre of ellipse and the point to semi-minor axis b
		// ... remember, distance between centre of ellipse and any point on the ellipse must be between b and a (b <= dist <= a)
		// ... thus if it is less than b it is in ellipse, but if it is equal to b, the point could be on the edge of the ellipse 
		// ... and => if points on the edge should not be considered 'in the ellipse', then the longer general method should be used 
		double minSemiAxis = Math.min(this_a_df, this_b_df); 
		// due to changes to the ellipses' properties by optimizer, b might not necessarily be smaller than a    
		boolean distCentreToPnt_minSemiAxis_areEqual = areEqual(distCentreToPnt, minSemiAxis, l);
		if (((distCentreToPnt < minSemiAxis) && (!distCentreToPnt_minSemiAxis_areEqual)) || 
			(includeEllipseEdge && distCentreToPnt_minSemiAxis_areEqual)){
			return true;
			
			// NB: to check if the pnt is in the ellipse and outside or on the edge then need to carry out both of the following checks 
			//     ((distCentreToPnt < minSemiAxis) && (!distCentreToPnt_minSemiAxis_areEqual))
			//     as there might be cases where (distCentreToPnt < minSemiAxis) is true, but in terms of the precision limits, 
			//     the 2 values are considered = rather than <	
		}
		
		double maxSemiAxis = Math.max(this_a_df, this_b_df); 
		// due to changes to the ellipses' properties by optimizer, b might not necessarily be smaller than a    
	
		if (distCentreToPnt > maxSemiAxis){
			return false;	
		}
		// the above 2 optimization are linked to the idea that an ellipse is constructed mainly of 2 circles: 1 with radius=this.a; 1 with radius=this.b 
		
		

		// ... else proceed using a longer more general method	
		MyLine line = new MyLine(pnt, ellCentreAsPnt, true);
		ArrayList<Point2D.Double> ipsLineWithEllipse = getIntPnts_WithLine(line);
		// should have 2 points where the line intersects with the ellipse
		
		Point2D.Double ipLineWithEllipse = null;
		
		// use this for loop instead of the commented large chunk below
		for (Point2D.Double ip : ipsLineWithEllipse){
			
			try {
				if (isZero(df.parse(df.format( GeometricOps.getPolarCoordAngForPnt (pnt, ellCentreAsPnt) )).doubleValue() - 
						   df.parse(df.format( GeometricOps.getPolarCoordAngForPnt (ip,  ellCentreAsPnt) )).doubleValue(), 1e-2)){//I think it must be 1e-4 or more if df is set to 4dp due to possible rounding //before 23rd Oct 2011  1e-6)){
					ipLineWithEllipse = ip;
					break;
				}
			} catch (ParseException exception) {
				exception.printStackTrace();
				System.out.println("Ellipse.isPointInEllipse: formatting distance 2");
			}
		}
		
		
		if (ipLineWithEllipse == null){
			System.out.println("Ellipse.isPointInEllipse: cannot determine whether point ("+pnt_x_df+","+pnt_y_df+") is in ellipse "+this.label);
			return null;
		}
		
		double distCentreToIpLine = GeometricOps.distanceBetween2Points(ellCentreAsPnt, ipLineWithEllipse);	
		try {
			distCentreToIpLine = df.parse(df.format(distCentreToIpLine)).doubleValue();
		} catch (ParseException exception) {
			exception.printStackTrace();
		}
		boolean distCentreToPnt_distCentreToIpLine_areEqual = areEqual(distCentreToPnt, distCentreToIpLine, l);
		
		return (((distCentreToPnt < distCentreToIpLine) && (!distCentreToPnt_distCentreToIpLine_areEqual)) || 
				(includeEllipseEdge && distCentreToPnt_distCentreToIpLine_areEqual));
		
		// NB: to check if the pnt is in the ellipse and outside or on the edge then need to carry out both of the following checks 
		//     ((distCentreToPnt < distCentreToIpLine) && (!distCentreToPnt_distCentreToIpLine_areEqual))
		//     as there might be cases where (distCentreToPnt < distCentreToIpLine) is true, but in terms of the precision limits, 
		//     the 2 values are considered = rather than <		
	}
	

	
	// Finding intersection points with other geometric structures such as lines and other ellipses

	public ArrayList<Point2D.Double> getIntPnts_WithLine (Point2D.Double endPnt1, Point2D.Double endPnt2){
		return (getIntPnts_WithLine(new MyLine(endPnt1, endPnt2, true)));
	}
	public ArrayList<Point2D.Double> getIntPnts_WithLine (MyLine line){
		
		double maxSemiAxis = Math.max(this.a, this.b); 		
		Line2D.Double t_line = null;
		if (Double.isInfinite(line.getGradient())){ //Double.isInfinite(line.getGradient())){
			double lineYInt = line.getYIntercept();
			double minY = this.yc - (maxSemiAxis + 10);
			double maxY = this.yc + (maxSemiAxis + 10);
			t_line = new Line2D.Double(lineYInt, minY, lineYInt, maxY);
			
		} else {
			double minX = this.xc - (maxSemiAxis + 10);
			double maxX = this.xc + (maxSemiAxis + 10);
			t_line = new Line2D.Double(minX, line.getYKnowingX(minX), maxX, line.getYKnowingX(maxX));

		}
		//get the appropriate line segment such that if the line really crosses the ellipse, 
		//...then it is long enough to possibly cross the ellipse twice 
		//-> thus line endpnts must be maxSemiAxis away from the centre + an allowance (in this case 10) to ensure that the line is long enough
		
		
		// transform the line to consider the intersection of the transformed line with a unit circle (the transformed ellipse)		
		
		TransformationMatrix cirMatrix_e = TransformationMatrix.ellipseToCircleTransMatrix (this);
		Point2D.Double lnpnt = new Point2D.Double();
		
		lnpnt.x = t_line.x1; 
		lnpnt.y = t_line.y1;		
		lnpnt = cirMatrix_e.transformPoint(lnpnt);
		t_line.x1 = lnpnt.x;
		t_line.y1 = lnpnt.y;
		// reuse lnpnt and thus avoid instantiating extra objects
		lnpnt.x = t_line.x2; 
		lnpnt.y = t_line.y2;
		lnpnt = cirMatrix_e.transformPoint(lnpnt);
		t_line.x2 = lnpnt.x;
		t_line.y2 = lnpnt.y;

		
		// had to do the following because even though the gradient in the original line was infinite, 
		// when transform the line end up with eg: x1=0.500000000002 and x2=0.5 getting a gradient(for the transformed line) eg m=5.123e+15
		DecimalFormat df = new DecimalFormat("##############################.########"); //8 digits after the decimal point
		try {
			t_line.x1 = df.parse(df.format(t_line.x1)).doubleValue();
			t_line.x2 = df.parse(df.format(t_line.x2)).doubleValue();
			t_line.y1 = df.parse(df.format(t_line.y1)).doubleValue();
			t_line.y2 = df.parse(df.format(t_line.y2)).doubleValue();
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		
		//  the number of intersections of the transformed line and test circle
		Circle testCircle = Circle.unitCircle();
		ArrayList<Point2D.Double> intpntsCirLn = testCircle.intpnts_WithLine(t_line);
		TransformationMatrix invMat = TransformationMatrix.invEllipseToCircle(this, cirMatrix_e); //reuse transf matrix object cirMatrix_e since it is no longer needed
		
		ArrayList<Point2D.Double> intpnts = new ArrayList<Point2D.Double>(2); //usually max ips = 2 -> this is the initial capacity and grows automatically if more space is needed
		if ((intpntsCirLn!=null) && (intpntsCirLn.size()>0)){
			for (Point2D.Double ip : intpntsCirLn){
				intpnts.add(invMat.transformPoint(ip));
			}
		}

		return intpnts;
	}
	

	public ArrayList<Point2D.Double> getIntPnts_WithEllipse_AsPoly (Ellipse e2){
		ArrayList<Point2D.Double> intPnts = new ArrayList<Point2D.Double>(2); //usually ips = 2 -> this is the initial capacity and grows automatically if more space is needed

		Polygon e1Poly = this.getAsPolygon(); 
		Polygon e2Poly = e2.getAsPolygon();

		Line2D.Double[] e1PolyLines = new Line2D.Double [e1Poly.npoints];
		Line2D.Double[] e2PolyLines = new Line2D.Double [e2Poly.npoints];
		for (int i=0; i<e1Poly.npoints; i++){
			Point2D.Double pnt2;
			if ((i+1)==e1Poly.npoints){
				pnt2 = new Point2D.Double(e1Poly.xpoints[0], e1Poly.ypoints[0]);
			} else {
				pnt2 = new Point2D.Double(e1Poly.xpoints[i+1], e1Poly.ypoints[i+1]);
			}
			e1PolyLines[i] = new Line2D.Double(new Point2D.Double(e1Poly.xpoints[i],e1Poly.ypoints[i]), pnt2);
		}
		for (int i=0; i<e2Poly.npoints; i++){
			Point2D.Double pnt2;
			if ((i+1)==e2Poly.npoints){
				pnt2 = new Point2D.Double(e2Poly.xpoints[0], e2Poly.ypoints[0]);
			} else {
				pnt2 = new Point2D.Double(e2Poly.xpoints[i+1], e2Poly.ypoints[i+1]);
			}
			e2PolyLines[i] = new Line2D.Double(new Point2D.Double(e2Poly.xpoints[i],e2Poly.ypoints[i]), pnt2);
		}

		Point2D.Double intPnt;
		for (Line2D.Double e1L : e1PolyLines){
			for (Line2D.Double e2L : e2PolyLines){
				intPnt = GeometricOps.getIntPntsOfLines(e1L, e2L);
				if ((intPnt != null) && (! intPnts.contains(intPnt))){
					intPnts.add(intPnt);
				}
			}
		}

		return intPnts;
	}
	
	

	public ArrayList<Point2D.Double> getIntPnts_WithEllipse (Ellipse e2, boolean considerLessPrecisePossibleIntPnts){
		
		int d_forPreciseIntPnts = 2;
		int d_forLessPreciseIntPnts = 10;
		
		ArrayList<Point2D.Double> intpnts = new ArrayList<Point2D.Double>();
		Point2D.Double similarIntPnt = null;
		
		
		if (!considerLessPrecisePossibleIntPnts){
			return getIntPnts_WithEllipse(e2,0);
			
		} else {
			// get the intersection points using highest precision possible and remove any duplicate intersection point 
			// -> 2 intersection points are considered similar and thus the same if the distance between the 2 points is <= l
			// -> if 2 points are similar, the midpoint of the two points is considered as an intersection point of the ellipses
			ArrayList<Point2D.Double> intpnts_validated_0 = getIntPnts_WithEllipse(e2,0);
			for (Point2D.Double ip0a : intpnts_validated_0){
				if (intpnts.size()==0){
					intpnts.add(ip0a);
				} else{
					similarIntPnt = null;
					for(Point2D.Double ip0b : intpnts){
						if ( Math.round(GeometricOps.distanceBetween2Points(ip0a, ip0b)) <= d_forPreciseIntPnts ){
							similarIntPnt = ip0b;
							break;
						} 
					}
					if (similarIntPnt == null){
						intpnts.add(ip0a);
					} else {
						intpnts.add(GeometricOps.midPointOf2Points(ip0a, similarIntPnt));
						intpnts.remove(similarIntPnt);
					}
				}
			}
		}
		
		// get the intersection points using lower precision 
	    // and remove any duplicate intersection point, 
		// giving preference to the precise intersection points in intpnts_validated_0
		if (considerLessPrecisePossibleIntPnts && (intpnts.size()>=2)){
			//added (intpnts.size()>=2) to eliminate automatically those with 1 int pnt and those with no int pnts 
			
			ArrayList<Point2D.Double> intpnts_validated_1 = getIntPnts_WithEllipse(e2,1);
			ArrayList<Point2D.Double> intpnts_validated_1_exclIntPnts0_nodup = new ArrayList<Point2D.Double>();
			
			if (intpnts_validated_1 != null){
				ArrayList<Point2D.Double> intpnts_validated_1_exclIntPnts0 = new ArrayList<Point2D.Double>();
				boolean foundASimilarIntPnt = false;
				// check if there are intersection points that are NOT similar to any of those in intpnts_validated_0 and if so add them
				for (Point2D.Double ip1 : intpnts_validated_1){
					foundASimilarIntPnt = false;
					for (Point2D.Double ip0 : intpnts){
						if ( Math.round(GeometricOps.distanceBetween2Points(ip1, ip0)) <= d_forLessPreciseIntPnts ){
							foundASimilarIntPnt = true;
							break;
						}
					}
					if (!foundASimilarIntPnt){
						intpnts_validated_1_exclIntPnts0.add(ip1);
					}
				}
			    
				// check if there are any intersection points in intpnts_validated_1_exclIntPnts0 that are similar and 
				// if so, consider their mid point as an intersection point of the ellipses
				for (Point2D.Double ip1a : intpnts_validated_1_exclIntPnts0){
					if (intpnts_validated_1_exclIntPnts0_nodup.size()==0){
						intpnts_validated_1_exclIntPnts0_nodup.add(ip1a);
					} else{
						similarIntPnt = null;
						for(Point2D.Double ip1b : intpnts_validated_1_exclIntPnts0_nodup){
							if ( Math.round(GeometricOps.distanceBetween2Points(ip1a, ip1b)) <= d_forLessPreciseIntPnts ){
								similarIntPnt = ip1b;
								break;
							} 
						}
						if (similarIntPnt == null){
							intpnts_validated_1_exclIntPnts0_nodup.add(ip1a);
						} else {
							intpnts_validated_1_exclIntPnts0_nodup.add(GeometricOps.midPointOf2Points(ip1a, similarIntPnt));
							intpnts_validated_1_exclIntPnts0_nodup.remove(similarIntPnt);
						}
					}
				}
				
				// add the intersection points obtained when precision was lowered (that is int pnts in intpnts_validated_1_exclIntPnts0_nodup)
				// to other intersection points obtained from intpnts_validated_0
				// and exclude any that are similar
				for (Point2D.Double ip1 : intpnts_validated_1_exclIntPnts0_nodup){
					
					similarIntPnt = null;
					for(Point2D.Double ip : intpnts){
						if ( Math.round(GeometricOps.distanceBetween2Points(ip1, ip)) <= d_forPreciseIntPnts ){
							similarIntPnt = ip;
							break;
						} 
					}
					if (similarIntPnt == null){
						intpnts.add(ip1);
					}					
				}
			}
		}
		return intpnts;
	}
	
	public ArrayList<Point2D.Double> getIntPnts_WithEllipse (Ellipse e2, int considerPreciseOrLessPrecisePossibleIntPnts){
		// This is based on the algorithm in: 
		// Kenneth J Hill (1995). Matrix-based Ellipse Geometry. In Alan W Paeth (ed.), Graphics Gems V, Morgan Kaufmann, San Francisco, CA, USA, pp. 72-77.
		
		//if considerPreciseOrLessPrecisePossibleIntPnts = 0, consider precise int pnts
		//if considerPreciseOrLessPrecisePossibleIntPnts = 1, consider less precise possible int pnts
		
		
		TransformationMatrix transMatrix = TransformationMatrix.IdMatrix(); // a reusuable transformation matrix within the method
		
		//  the characteristic matrices
		Conic conic_e1 = new Conic (this);
		Conic conic_e2 = new Conic (e2);
		
		
		ArrayList<Double> polynomialCoeff = new ArrayList<Double>(4);

		polynomialCoeff.add (- (conic_e1.C * conic_e1.D * conic_e1.D) + (2.0 * conic_e1.B * conic_e1.D * conic_e1.E)
							 - (conic_e1.A * conic_e1.E * conic_e1.E) - (conic_e1.B * conic_e1.B * conic_e1.F)
							 + (conic_e1.A * conic_e1.C * conic_e1.F));

		polynomialCoeff.add (- (conic_e2.C * conic_e1.D * conic_e1.D) - (2.0 * conic_e1.C * conic_e1.D * conic_e2.D)
							 + (2.0 * conic_e2.B * conic_e1.D * conic_e1.E) + (2.0 * conic_e1.B * conic_e2.D * conic_e1.E)
							 - (conic_e2.A * conic_e1.E * conic_e1.E) + (2.0 * conic_e1.B * conic_e1.D * conic_e2.E)
							 - (2.0 * conic_e1.A * conic_e1.E * conic_e2.E) - (2.0 * conic_e1.B * conic_e2.B * conic_e1.F)
							 + (conic_e2.A * conic_e1.C * conic_e1.F) + (conic_e1.A * conic_e2.C * conic_e1.F)
							 - (conic_e1.B * conic_e1.B * conic_e2.F) + (conic_e1.A * conic_e1.C * conic_e2.F));

		polynomialCoeff.add (- (2.0 * conic_e2.C * conic_e1.D * conic_e2.D) - (conic_e1.C * conic_e2.D * conic_e2.D)
		 					 + (2.0 * conic_e2.B * conic_e2.D * conic_e1.E) + (2.0 * conic_e2.B * conic_e1.D * conic_e2.E)
		 					 + (2.0 * conic_e1.B * conic_e2.D * conic_e2.E) - (2.0 * conic_e2.A * conic_e1.E * conic_e2.E)
		 					 - (conic_e1.A * conic_e2.E * conic_e2.E) - (conic_e2.B * conic_e2.B * conic_e1.F)
		 					 + (conic_e2.A * conic_e2.C * conic_e1.F) - (2.0 * conic_e1.B * conic_e2.B * conic_e2.F)
		 					 + (conic_e2.A * conic_e1.C * conic_e2.F) + (conic_e1.A * conic_e2.C * conic_e2.F));

		polynomialCoeff.add (- (conic_e2.C * conic_e2.D * conic_e2.D) + (2.0 * conic_e2.B * conic_e2.D * conic_e2.E)
		 					 - (conic_e2.A * conic_e2.E * conic_e2.E) - (conic_e2.B * conic_e2.B * conic_e2.F)
		 					 + (conic_e2.A * conic_e2.C * conic_e2.F));
		
		
		ArrayList<ArrayList<Double>> rootsplus = RootFinder.solveCubic(polynomialCoeff);
		ArrayList<Double> roots = rootsplus.get(considerPreciseOrLessPrecisePossibleIntPnts);
		if (roots == null){return null;}
		if (roots.size() == 0) { return null;}

		// try all the roots
		int noLines = 0;
		Line2D.Double[] testLine = new Line2D.Double[2];
		for (int i=0; i<2; i++){
			testLine[i] = new Line2D.Double(0.0,0.0,0.0,0.0);
		}

		double D;
		double phi;
		ArrayList<Double> qroots;
		Point2D.Double lnpnt = null;
		TransformationMatrix tempMat; 
		Conic tempConic;
		
		double[] conic_3_props = {0,0,0,0,0,0};
		Conic conic_3 = null;
		
		for (Double r : roots){			
			noLines = 0;
			
			conic_3_props[0] = conic_e1.A + (r * conic_e2.A);
			conic_3_props[1] = conic_e1.B + (r * conic_e2.B);
			conic_3_props[2] = conic_e1.C + (r * conic_e2.C);
			conic_3_props[3] = conic_e1.D + (r * conic_e2.D);	
			conic_3_props[4] = conic_e1.E + (r * conic_e2.E);
			conic_3_props[5] = conic_e1.F + (r * conic_e2.F);
						
			if (conic_3 == null){
				conic_3 = new Conic (conic_3_props[0], conic_3_props[1], conic_3_props[2], 
						             conic_3_props[3], conic_3_props[4], conic_3_props[5]);
			} else {
				conic_3.resetPropAsIfNew(conic_3_props[0], conic_3_props[1], conic_3_props[2], 
						                 conic_3_props[3], conic_3_props[4], conic_3_props[5]);
			}
			

			D = (conic_3.B * conic_3.B) - (conic_3.A * conic_3.C);

			if (isZero(conic_3.A) && isZero(conic_3.B) && isZero(conic_3.C)){

				 // (1) Having a single line
				 noLines = 1;

				 if (Math.abs(conic_3.D) > Math.abs(conic_3.E)){
					 testLine[0].y1 = 0.0;
					 testLine[0].x1 = -conic_3.F/(conic_3.D + conic_3.D);
					 testLine[0].y2 = 1.0;
					 testLine[0].x2 = -(conic_3.E + conic_3.E + conic_3.F)/(conic_3.D + conic_3.D);
				 } else {
					 testLine[0].x1 = 0.0;
					 testLine[0].y1 = -conic_3.F/(conic_3.E + conic_3.E);
					 testLine[0].x2 = 1.0;
					 testLine[0].y2 = -(conic_3.D + conic_3.D + conic_3.F)/(conic_3.E + conic_3.E);
				 }
				

			} else {

				 phi = 0;
				 if (Math.abs(conic_3.B + conic_3.B) < Math.abs(conic_3.A - conic_3.C)){
					 phi = Math.toDegrees(Math.atan((conic_3.B + conic_3.B)/(conic_3.A - conic_3.C))/2.0);
				 } else {
				 	 phi = Math.toDegrees( ((Math.PI/2) - Math.atan((conic_3.A - conic_3.C)/(conic_3.B + conic_3.B))) /2.0);
				 }
				 
				 if (isZero(D)){
					 // (2) Having parallel lines
					 conic_e1.resetPropAsIfNew(conic_3.A, conic_3.B, conic_3.C, conic_3.D, conic_3.E, conic_3.F); //reuse the Conic object conic_e1
					 tempConic = conic_e1;
					 
					 transMatrix.resetToIdMatrix(); //reuse instead of creating new
					 tempMat = transMatrix;
					 
					 tempMat.rotate(-phi);  //input angle must be in degrees -> this is converted to radians in the method rotate
					 tempConic.transform(tempMat);

					 polynomialCoeff.remove(3); //remove the arraylist<double> -> remove the last element because a 3 element arraylist is necessary at this point 

					 if (Math.abs(tempConic.A) > Math.abs(tempConic.C)){ 
						 
						 polynomialCoeff.set(0, tempConic.F);
						 polynomialCoeff.set(1, 2 * tempConic.D);
						 polynomialCoeff.set(2, tempConic.A);
						 
						 qroots = RootFinder.solveQuadric(polynomialCoeff);
						 noLines = (qroots==null) ? 0 : qroots.size();
						 if (noLines != 0){
							 testLine[0].x1 = qroots.get(0);
							 testLine[0].y1 = -1.0;     
							 testLine[0].x2 = qroots.get(0);
							 testLine[0].y2 = 1.0;

							 if (noLines == 2){
								 testLine[1].x1 = qroots.get(1);
								 testLine[1].y1 = -1.0; 
								 testLine[1].x2 = qroots.get(1);
								 testLine[1].y2 = 1.0;  							 	 
							 }
						 }

					 } else {  
						 
						 polynomialCoeff.set(0, tempConic.F);
						 polynomialCoeff.set(1, 2 * tempConic.E);
						 polynomialCoeff.set(2, tempConic.C);
						 
						 qroots = RootFinder.solveQuadric(polynomialCoeff);
						 noLines = (qroots==null) ? 0 : qroots.size();
						 if (noLines != 0){
							 testLine[0].x1 = -1.0;
							 testLine[0].y1 = qroots.get(0);
							 testLine[0].x2 = 1.0; 
							 testLine[0].y2 = qroots.get(0);

							 if (noLines == 2){
								 testLine[1].x1 = -1.0; 
								 testLine[1].y1 = qroots.get(1);
								 testLine[1].x2 = 1.0; 
								 testLine[1].y2 = qroots.get(1);
							 }
						 }
					 }
					 
					 
					 transMatrix.resetToIdMatrix();  
					 tempMat = transMatrix; 
					 tempMat.rotate(phi);
					 
					 
					 if (lnpnt == null){
						 lnpnt = new Point2D.Double();
					 }
					 lnpnt.x = testLine[0].x1;
					 lnpnt.y = testLine[0].y1;
					 lnpnt = tempMat.transformPoint(lnpnt);
					 testLine[0].x1 = lnpnt.x;
					 testLine[0].y1 = lnpnt.y;
					 
					 lnpnt.x = testLine[0].x2;
					 lnpnt.y = testLine[0].y2;
					 lnpnt = tempMat.transformPoint(lnpnt); 
					 testLine[0].x2 = lnpnt.x;
					 testLine[0].y2 = lnpnt.y;
					 
					 if (noLines == 2){
						 
						 lnpnt.x = testLine[1].x1;
						 lnpnt.y = testLine[1].y1;
						 lnpnt = tempMat.transformPoint(lnpnt); 
						 testLine[1].x1 = lnpnt.x;
						 testLine[1].y1 = lnpnt.y;
						 
						 lnpnt.x = testLine[1].x2;
						 lnpnt.y = testLine[1].y2;
						 lnpnt = tempMat.transformPoint(lnpnt); 
						 testLine[1].x2 = lnpnt.x;
						 testLine[1].y2 = lnpnt.y;
						 
					 }

				 } else {
					 // (3) Having crossing lines
				     noLines = 2;

					 // translate the system so that the intersection of the lines is at the origin
					 tempConic = conic_3; 
					 
					 
					 double m = ((conic_3.C * conic_3.D) - (conic_3.B * conic_3.E))/D;
					 double n = ((conic_3.A * conic_3.E) - (conic_3.B * conic_3.D))/D;
					 
					 transMatrix.resetToIdMatrix();  //reusing the method object instead of creating a new one
					 tempMat = transMatrix;
					 tempMat.translate(-m, -n);
					 tempMat.rotate(-phi);
					 tempConic.transform(tempMat);

					 //  the line endpoints
					 testLine[0].x1 = Math.sqrt(Math.abs(1.0/tempConic.A));
					 testLine[0].y1 = Math.sqrt(Math.abs(1.0/tempConic.C));
					 double scl = Math.max(testLine[0].x1,testLine[0].y1);
					 testLine[0].x1 /= scl;
					 testLine[0].y1 /= scl;
					 testLine[0].x2 = - testLine[0].x1;
					 testLine[0].y2 = - testLine[0].y1;
					 testLine[1].x1 = testLine[0].x1;
					 testLine[1].y1 = - testLine[0].y1;
					 testLine[1].x2 = - testLine[1].x1;
					 testLine[1].y2 = - testLine[1].y1;

					 // translate the lines back
					 transMatrix.resetToIdMatrix();  //reusing the method object instead of creating a new one
					 tempMat = transMatrix; 
					 tempMat.rotate(phi); 
					 tempMat.translate(m,n); 
					 
					 
					 if (lnpnt == null){
						 lnpnt = new Point2D.Double();
					 }
					 lnpnt.x = testLine[0].x1;
					 lnpnt.y = testLine[0].y1;
					 lnpnt = tempMat.transformPoint(lnpnt); 
					 testLine[0].x1 = lnpnt.x;
					 testLine[0].y1 = lnpnt.y;
					 
					 lnpnt.x = testLine[0].x2;
					 lnpnt.y = testLine[0].y2;
					 lnpnt = tempMat.transformPoint(lnpnt); 
					 testLine[0].x2 = lnpnt.x;
					 testLine[0].y2 = lnpnt.y;

					 lnpnt.x = testLine[1].x1;
					 lnpnt.y = testLine[1].y1;
					 lnpnt = tempMat.transformPoint(lnpnt); 
					 testLine[1].x1 = lnpnt.x;
					 testLine[1].y1 = lnpnt.y;
					 
					 lnpnt.x = testLine[1].x2;
					 lnpnt.y = testLine[1].y2;
					 lnpnt = tempMat.transformPoint(lnpnt);
					 testLine[1].x2 = lnpnt.x;
					 testLine[1].y2 = lnpnt.y;
				 }
			}
		}
		//cleanup
		conic_e1 = null; conic_e2 = null;
		tempMat = null; 
		polynomialCoeff = null;
		
		
		
		ArrayList<Point2D.Double> intpnts_notValidated = new ArrayList<Point2D.Double>(2); //usually max ips = 2 -> this is the initial capacity and grows automatically if more space is needed

		//  the transformation which turns e1 into circle
		TransformationMatrix cirMatrix_e1 = TransformationMatrix.ellipseToCircleTransMatrix (this);

		//  the inverse transformation of cirMatrix_e1
	    TransformationMatrix invMat = TransformationMatrix.invEllipseToCircle(this, transMatrix);
		

		// find the ellipse line intersections
		Circle testCircle = Circle.unitCircle();
		ArrayList<Point2D.Double> intpntsCirLn;
		for (int j = 0; j < noLines; j++) {

			
			
			// transform the line endpts into the circle space of the ellipse
			
			if (lnpnt == null){
				lnpnt = new Point2D.Double();
			}
			lnpnt.x = testLine[j].x1;
			lnpnt.y = testLine[j].y1;
			lnpnt = cirMatrix_e1.transformPoint(lnpnt); 
			testLine[j].x1 = lnpnt.x;
			testLine[j].y1 = lnpnt.y;
			 
			lnpnt.x = testLine[j].x2;
			lnpnt.y = testLine[j].y2;
			lnpnt = cirMatrix_e1.transformPoint(lnpnt); 
			testLine[j].x2 = lnpnt.x;
			testLine[j].y2 = lnpnt.y;

			
			
			//  the number of intersections of the transformed line and test circle
			testCircle.resetToUnitCircle();
			intpntsCirLn = testCircle.intpnts_WithLine(testLine[j]);

			
			if ((intpntsCirLn!=null) && (intpntsCirLn.size()>0)){

				for (Point2D.Double ip : intpntsCirLn){
					intpnts_notValidated.add(invMat.transformPoint(ip));
				}

			}
		}

		// validate the points
		ArrayList<Point2D.Double> intpnts_validated = new ArrayList<Point2D.Double>(2); //usually max ips = 2 -> this is the initial capacity and grows automatically if more space is needed

	   //  the transformation which turns e2 into circle
		TransformationMatrix cirMatrix_e2 = TransformationMatrix.ellipseToCircleTransMatrix (e2, cirMatrix_e1); //reusing transf matrix cirMatrix_e1
	
		int j = intpnts_notValidated.size();
		Point2D.Double testPnt;
		double l = (considerPreciseOrLessPrecisePossibleIntPnts==0 ? 1e-6 : 0.05);
		for (int i = 0; i < j; i++){
			testPnt = intpnts_notValidated.get(i);
			testPnt = cirMatrix_e2.transformPoint(testPnt);
	
			if (testPnt.x < 2.0 && testPnt.y < 2.0 && isZero(1.0 - Math.sqrt((testPnt.x * testPnt.x) + (testPnt.y*testPnt.y)), l)){     
				intpnts_validated.add(intpnts_notValidated.get(i));
			}
		}

		//remove duplicates
		ArrayList<Point2D.Double> intpnts_validatedNoDup = new ArrayList<Point2D.Double>(2); //usually max ips = 2 -> this is the initial capacity and grows automatically if more space is needed
		for (Point2D.Double ip : intpnts_validated){
			if (!intpnts_validatedNoDup.contains(ip)){
				intpnts_validatedNoDup.add(ip);
			}
		}
		
		//cleanup
		invMat = null; //invMat is reusing transMat transf matrix object
		cirMatrix_e2 = null; //cirMatrix_e2 is reusing cirMatrix_e1 transf matrix object
		intpnts_notValidated = null;
		intpnts_validated = null;
		
		return intpnts_validatedNoDup;
	}

	
	
	
	// Finding areas ...
	
	// ... finding areas in terms of areas of elliptic segments 
	
	public double computeAreaOfEllipticSegment (Point2D.Double[] arcEndPnts){
		//the arc end points have to be ordered appropriately according to the area that needs to be covered

		double eRotRad = Math.toRadians(this.rot); 
		Point2D.Double[] newArcEndPnts = new Point2D.Double[2];

		//calculate arcendpoints when the ellipse is translated to the origin and rotation angle to the major axis is set to 0
		Point2D.Double centre = new Point2D.Double(xc, yc);
		double polarAngForPnt0 = GeometricOps.getPolarCoordAngForPnt(arcEndPnts[0], centre);
		double polarAngForPnt1 = GeometricOps.getPolarCoordAngForPnt(arcEndPnts[1], centre);

		double polarRForPnt0 = GeometricOps.getPolarCoordRForPnt(arcEndPnts[0], centre);
		double polarRForPnt1 = GeometricOps.getPolarCoordRForPnt(arcEndPnts[1], centre);

		
		newArcEndPnts[0] = new Point2D.Double((polarRForPnt0 * Math.cos(polarAngForPnt0 - eRotRad)),
				                              (polarRForPnt0 * Math.sin(polarAngForPnt0 - eRotRad))); 
		newArcEndPnts[1] = new Point2D.Double((polarRForPnt1 * Math.cos(polarAngForPnt1 - eRotRad)), 
                                              (polarRForPnt1 * Math.sin(polarAngForPnt1 - eRotRad)));
		

		double t1 = polarAngForPnt0 - eRotRad;
		double t2 = polarAngForPnt1 - eRotRad;
		
		// t1 and t2 must be (polarAngForPnt-eRotRad) because we are going to find the area of the elliptic segment after the ellipse is tranformed st rot=0 (centre is the same the original)
		// Later on, then check whether we need to add 2pi to t2 (as done below)

		if (t2 < t1){ // assuming that we already know the correct order of the end points and thus in such cases the arc would be starting in -y axis
			t2 += (2 * Math.PI);
		}
		
		/*
		 * This considers cases where the area to be handled is greater than 180degrees 
		 * Such cases are not handled explicitly by the formula and thus the following is required  
		 * => this is correct -> for more details check the article on areas of elliptic segments by David Eberly
		 * eg: random diagram 20100216_102732_2 -> has such a special case and thus requires this code
		 */ 
		boolean specialCase = false;
		double angBetweenTs = t2 - t1; 
		if (angBetweenTs > Math.PI){
			double temp_t = t1;
			t1 = t2;
			t2 = temp_t;
			Point2D.Double temp_newArcEndPnts0 = new Point2D.Double (newArcEndPnts[0].x,newArcEndPnts[0].y); 
			newArcEndPnts[0] = new Point2D.Double (newArcEndPnts[1].x,newArcEndPnts[1].y); 
			newArcEndPnts[1] = new Point2D.Double (temp_newArcEndPnts0.x,temp_newArcEndPnts0.y); 
			specialCase = true;
		}

		if (t2 < t1){ // assuming that we already know the correct order of the end points and thus in such cases the arc would be starting in -y axis
			t2 += (2 * Math.PI);
		}
		
		
		
		// area of elliptical sector
		double interm_t1 = Math.atan((this.a/this.b) * Math.tan(t1));
		double interm_t2 = Math.atan((this.a/this.b) * Math.tan(t2));

		// to get the angle appropriate to our original range
		// -> note the above formula for interm_t1 and interm_t2
		//    -> first find the y-value Math.tan(t1); inc/dec y-value depending on (this.a/this.b)
		//       meaning that the corresponding x-value range is not really change
		//       then get the new x-value using atan
		//       atan gives a value between -pi/2 to pi/2 so then we need to adapt result to get in the original range
		//       To do this, the following formula seems to apply for all
		interm_t1 += (Math.PI * (Math.round(t1/Math.PI)));
		interm_t2 += (Math.PI * (Math.round(t2/Math.PI)));

		double areaEllipticalSector = ((this.a * this.b)/2) * (interm_t2 - interm_t1);


		// area of triangle -> base point is origin; other two points are the arc endpoints
		//     -> find area of triangle in this way
		//        do not use Math.abs(Math.sin(t2-t1)) -> this did not work in most cases: the resulting area was > area of elliptic sector
		double detNewArcPnts =Math.abs((newArcEndPnts[0].x * newArcEndPnts[1].y) - (newArcEndPnts[1].x * newArcEndPnts[0].y));
		double areaTriangle = 0.5 * detNewArcPnts;

		// area bounded by line segment and elliptic arc
		double area = areaEllipticalSector - areaTriangle;

		double areaFinal = area;
		
		if (specialCase){
			areaFinal = this.getArea() - Math.abs(area);  
		}
	
		return areaFinal;
	}

	
	
	public Double getSharedZoneArea_BySeg (Ellipse other_e, Point2D.Double[] intpnts){

		// line between intersection points, its mid-point, line perpendicular to it
		MyLine ipsLine = new MyLine(intpnts[0], intpnts[1], true);
		Point2D.Double ipsMidPnt = new Point2D.Double ((intpnts[0].x + intpnts[1].x)/2, (intpnts[0].y + intpnts[1].y)/2);
		MyLine ipsPerpLine = new MyLine(ipsLine.getPerpLineGradient(), ipsLine.getPerpLineYIntercept(ipsMidPnt), true);

		// get intersection points of every ellipse with the line perpendicular to that passing through the 2 ellipses intersection points
		ArrayList<Point2D.Double> thisE_IPsWithPerpLine = this.getIntPnts_WithLine(ipsPerpLine);
		ArrayList<Point2D.Double> otherE_IPsWithPerpLine = other_e.getIntPnts_WithLine(ipsPerpLine);

		// get the 2 intersection points of interest that is 1 point on every ellipse that is also in the area of the other ellipse
		//    => on the edge of the overlapping region
		Point2D.Double thisE_pnt = new Point2D.Double();
		Point2D.Double otherE_pnt = new Point2D.Double(); ;

		for (Point2D.Double pnt : thisE_IPsWithPerpLine){
			if (other_e.isPointInEllipse(pnt, false, 1e-6)){ //true //false
				thisE_pnt = pnt;
				break;
			}
		}
		for (Point2D.Double pnt : otherE_IPsWithPerpLine){
			if (this.isPointInEllipse(pnt, false, 1e-6)){
				otherE_pnt = pnt;
				break;
			}
		}

		

		//order the intersection points -> the leftmost (in x-axis) is the first one to ensure anticlockwise rotation
		//if x-value of both pnts is equal (=> have line x=a), check the y-value st ip1.y < ip2.y
		//     to ensure an anti-clockwise order for the int pnts and thus ensure that it corresponds appropriately to the
		//     selection of the elliptic segment which is above or below the line
		Point2D.Double[] arcEndPnts = new Point2D.Double[2];
		arcEndPnts[0] = intpnts[0];
		arcEndPnts[1] = intpnts[1];
		if (arcEndPnts[0].x == arcEndPnts[1].x){
			if (arcEndPnts[0].y > arcEndPnts[1].y){
				arcEndPnts[0] = intpnts[1];
				arcEndPnts[1] = intpnts[0];
			}
		} else if (arcEndPnts[0].x > arcEndPnts[1].x){
				arcEndPnts[0] = intpnts[1];
				arcEndPnts[1] = intpnts[0];
		}


		// find polar coord angle for arcEndPnts[1], pnt on ellipse a, pnt on ellipse b, from arcEndPnts[0]
		//Point2D.Double origin = new Point2D.Double (0,0);
		double ang_arcEndPnt1 = GeometricOps.getPolarCoordAngForPnt (arcEndPnts[1], arcEndPnts[0]);
		double ang_thisE_pnt = GeometricOps.getPolarCoordAngForPnt (thisE_pnt, arcEndPnts[0]);
		double ang_otherE_pnt = GeometricOps.getPolarCoordAngForPnt (otherE_pnt, arcEndPnts[0]);

		// to handle cases where a point on 1 of the ellipses is below and another above the +ve x-axis, carry out the following
		// this works also in cases where both points are below the +ve x-axis
		if (ang_arcEndPnt1 > Math.PI){
			ang_arcEndPnt1 -= (2 * Math.PI);
		}
		if (ang_thisE_pnt > Math.PI){
			ang_thisE_pnt -= (2 * Math.PI);
		}
		if (ang_otherE_pnt > Math.PI){
			ang_otherE_pnt -= (2 * Math.PI);
		}

		// identify the overlapping elliptic segment above and below the line joining the 2 intersection points
		Ellipse e1;
		Ellipse e2;
		if ((ang_thisE_pnt < ang_arcEndPnt1) && (ang_otherE_pnt > ang_arcEndPnt1)){
			e1 = this;
			e2 = other_e;
		} else if ((ang_thisE_pnt > ang_arcEndPnt1) && (ang_otherE_pnt < ang_arcEndPnt1)){
			e1 = other_e;
			e2 = this;
		} else {
			System.out.println("Ellipse.getSharedZoneArea_BySeg: Finding area of intersecting ellipses using elliptic segments: cannot order ellipses");
			return null;
		}



		Point2D.Double[] arcEndPnts_rev = new Point2D.Double[2];
		arcEndPnts_rev[0] = arcEndPnts[1];
		arcEndPnts_rev[1] = arcEndPnts[0];

		return (e1.computeAreaOfEllipticSegment(arcEndPnts)+ e2.computeAreaOfEllipticSegment(arcEndPnts_rev));
	}
	
	
	
	// ... finding areas using integration 

	private double getParametricTForPnt (Point2D.Double pnt){
		// invoked by computeAreaUnderEllipticArc_ByInteg and is specific for ellipses 
		// might want to set it as public for use with points on ellipse
		
		// outputs angle in radians form

		double ang = 0.0;

		Point2D.Double centre = new Point2D.Double(this.xc, this.yc);
		
		// make sure that if a number is extremely small and close to 0, define it as 0 
		// this helps to avoid get NaN eg if dx = 1000E-13 when this.a is 100 (had this problem with 2-ellipse 2 int pnts sample 2a)
		if (isZero(pnt.x)){
			pnt.x = 0;
		}
		if (isZero(pnt.y)){
			pnt.y = 0;
		}

		double dx = Math.abs(pnt.x -centre.x);
		double dy = Math.abs(pnt.y -centre.y);

		double unverifiedAng_dy = Math.abs(Math.asin(dy/this.b));
		double unverifiedAng_dx = Math.abs(Math.acos(dx/this.a));
		
		//carry out the following to try to avoid NaNs due to the precision of some results (as defined above)
		double unverifiedAng = unverifiedAng_dy;
		if (Double.isNaN(unverifiedAng)){
			unverifiedAng = unverifiedAng_dx;
		}
		
		//note that if pnt is in eg (+ve x, +ve y) axis, then the point which is t away from x-axis, is there too
		if ((pnt.y > centre.y) && (pnt.x > centre.x)){  // right, top -> +ve x, +ve y
			ang = unverifiedAng;
		} else if ((pnt.y > centre.y) && (pnt.x < centre.x)){    // left, top -> -ve x, +ve y
			ang = Math.PI - unverifiedAng;
		} else if ((pnt.y < centre.y) && (pnt.x < centre.x)){    // left, bottom -> -ve x, -ve y
			ang = Math.PI + unverifiedAng;
		} else if ((pnt.y < centre.y) && (pnt.x > centre.x)){    // right, bottom -> +ve x, -ve y
			ang = (2*Math.PI) - unverifiedAng;
		} else if ((dx == 0) && (pnt.y > centre.y)){   // same x, pnt ABOVE centre => 90 degrees
			ang = (Math.PI /2);
		} else if ((dx == 0) && (pnt.y < centre.y)){   // same x, pnt BELOW centre => 270 degrees
			ang = (3 * Math.PI /2); 
		} else if ((dy == 0) && (pnt.x > centre.x)){   // same y, pnt RIGHT to centre => 0 degrees
			ang = 0; 
		} else if ((dy == 0) && (pnt.x < centre.x)){   // same y, pnt LEFT to centre => 180 degrees
			ang = Math.PI; 
		}

		return  ang;
	}
	
	
	
	public double computeAreaUnderEllipticArc_ByInteg (Point2D.Double[] arcEndPnts, boolean dx){
		//all the outputted angles are in radians form
		double eRotRad = Math.toRadians(this.rot); 


		// transform the arc end points to eliminate rotation of ellipse and thus calculate appropriate t (eccentric anomly) for parametric curve
		Point2D.Double centre = new Point2D.Double(xc, yc);
		double polarAngForPnt0 = GeometricOps.getPolarCoordAngForPnt(arcEndPnts[0], centre);
		double polarAngForPnt1 = GeometricOps.getPolarCoordAngForPnt(arcEndPnts[1], centre);

		double polarRForPnt0 = GeometricOps.getPolarCoordRForPnt(arcEndPnts[0], centre);
		double polarRForPnt1 = GeometricOps.getPolarCoordRForPnt(arcEndPnts[1], centre);

		Point2D.Double[] newArcEndPnts = new Point2D.Double[2];
		
		double polarAngForPnt0_noEllRot = polarAngForPnt0 - eRotRad;
		if (polarAngForPnt0_noEllRot<0){
			polarAngForPnt0_noEllRot += (2*Math.PI);
		}
		double polarAngForPnt1_noEllRot = polarAngForPnt1 - eRotRad;
		if (polarAngForPnt1_noEllRot<0){
			polarAngForPnt1_noEllRot += (2*Math.PI);
		}
		newArcEndPnts[0] = new Point2D.Double((polarRForPnt0 * Math.cos(polarAngForPnt0_noEllRot)) + this.xc,
                							  (polarRForPnt0 * Math.sin(polarAngForPnt0_noEllRot)) + this.yc);
		newArcEndPnts[1] = new Point2D.Double((polarRForPnt1 * Math.cos(polarAngForPnt1_noEllRot)) + this.xc,
                                              (polarRForPnt1 * Math.sin(polarAngForPnt1_noEllRot)) + this.yc);		
		
		

		//limits in terms of t (angles corresponding to parametric equation)
		//   the angle t must start from the major axis => get polar ang and reduce rot of ellipse
		//double t1 = Math.atan((arcEndPnts[0].y-this.yc)/(arcEndPnts[0].x-this.xc)) - eRotRad;
		//double t2 = Math.atan((arcEndPnts[1].y-this.yc)/(arcEndPnts[1].x-this.xc)) - eRotRad;
	    double t1 = this.getParametricTForPnt(newArcEndPnts[0]);
	    double t2 = this.getParametricTForPnt(newArcEndPnts[1]);

	    
		double l1 = 0;
		double l2 = 0;
		if (dx){
			l1 = getX_parametric(t1, true);
			l2 = getX_parametric(t2, true);
		} else {
			l1 = getY_parametric(t1, true);
			l2 = getY_parametric(t2, true);
		}

		
		
		//to handle cases when arcpnt1 is below +ve x-axis and arcpnt2 is above +ve x-axis
		//   NB: do not use parametric t to carry out this check (i.e. if (t2 < t1)) because there are cases where t2 is slightly smaller than t1 
		//       and to get the right direction, they should be left as is -> this is especially the case when handling really small areas 
		//       eg. 20100216_102932_10_MC-LP hill climber iteration 80 (as on 2010-03-31)
		
		// check the angle of the transformed pnt because like the new transformed pnt based on transformed ellipse with rot = 0, the angle for t starts from the semi-major axis 
		// (which is the same axis from which the measurement of the polar ang for any point on the ellipse is calculated when the ellipse has rot=0)  
		
		double polarAngForNewPnt0 = GeometricOps.getPolarCoordAngForPnt(newArcEndPnts[0], centre);
		double polarAngForNewPnt1 = GeometricOps.getPolarCoordAngForPnt(newArcEndPnts[1], centre);
		if (polarAngForNewPnt1 < polarAngForNewPnt0){
			t2 += (2 * Math.PI);	
		}


		// had to round off values because eg: l2 was being set to a 14-dp value, whereas arcEndPnt was set to the same value but with just 12-dp
		if (dx){
			l1 = getX_parametric(t1, true);
			l2 = getX_parametric(t2, true);

			double diff_l1EndPnt0 = Math.abs(arcEndPnts[0].x-l1);
			double diff_l2EndPnt0 = Math.abs(arcEndPnts[0].x-l2);
			
			double diff_l1EndPnt1 = Math.abs(arcEndPnts[1].x-l1);
			double diff_l2EndPnt1 = Math.abs(arcEndPnts[1].x-l2);
			
			if ((diff_l1EndPnt0 > diff_l2EndPnt0) && (diff_l2EndPnt1 > diff_l1EndPnt1)){
				double prev_t1 = t1;
				double prev_t2 = t2;
				t1 = prev_t2;
				t2 = prev_t1;
			}
			
		} else {
			l1 = getY_parametric(t1, true);
			l2 = getY_parametric(t2, true);
			
			double diff_l1EndPnt0 = Math.abs(arcEndPnts[0].y-l1);
			double diff_l2EndPnt0 = Math.abs(arcEndPnts[0].y-l2);
			
			double diff_l1EndPnt1 = Math.abs(arcEndPnts[1].y-l1);
			double diff_l2EndPnt1 = Math.abs(arcEndPnts[1].y-l2);
			
			if ((diff_l1EndPnt0 > diff_l2EndPnt0) && (diff_l2EndPnt1 > diff_l1EndPnt1)){
				double prev_t1 = t1;
				double prev_t2 = t2;
				t1 = prev_t2;
				t2 = prev_t1;
			}		
		}


		//coeffs of final formula
		double M = 0.0;
		double N = 0.0;
		double L = 0.0;
		double O = 0.0;
		double P = 0.0;
		
		if (dx){
			M = (this.a * this.b * Math.cos(2*eRotRad)) / 4;
			N = -this.yc * this.b * Math.sin(eRotRad);
			L = (((this.a*this.a)+(this.b*this.b)) * Math.sin(2*eRotRad)) / 8;
			O = this.yc * this.a * Math.cos(eRotRad);
			P = -(this.a * this.b) / 2 ;
		} else {  //dy
			M = (this.a * this.b * Math.cos(2*eRotRad)) / 4;
			N = this.xc * this.b * Math.cos(eRotRad);
			L = (((this.a*this.a)+(this.b*this.b)) * Math.sin(2*eRotRad)) / 8;
			O = this.xc * this.a * Math.sin(eRotRad);
			P = (this.a * this.b) / 2 ;
		}

		double result_t2 = (M * Math.sin(2*t2)) + (N * Math.sin(t2)) + (L * Math.cos(2*t2)) + (O * Math.cos(t2)) + (P * t2);
		double result_t1 = (M * Math.sin(2*t1)) + (N * Math.sin(t1)) + (L * Math.cos(2*t1)) + (O * Math.cos(t1)) + (P * t1);
		double area = result_t2 - result_t1;

		return area;
	}
	
	

	public Double getSharedZoneArea_ByInteg (Ellipse other_e, Point2D.Double[] intpnts, int dx_or_dy){

		double areaOfDefiniteRegion = 0.0;
		boolean dx = true;


		if ((dx_or_dy != 1) || (dx_or_dy != 2)){
			double diff_x = Math.abs (intpnts[1].x - intpnts[0].x);
			double diff_y = Math.abs (intpnts[1].y - intpnts[0].y);

			if (diff_x > diff_y){
				dx_or_dy = 1;  //dx
			} else {
				dx_or_dy = 2;   //dy
			}
		}

		if (dx_or_dy == 1) {  //dx
			dx = true;
		} else {  // dy
			dx = false;
		}

		// line between intersection points, its mid-point, line perpendicular to it
		MyLine ipsLine = new MyLine(intpnts[0], intpnts[1], true);
		Point2D.Double ipsMidPnt = new Point2D.Double ((intpnts[0].x + intpnts[1].x)/2, (intpnts[0].y + intpnts[1].y)/2);
		MyLine ipsPerpLine = new MyLine(ipsLine.getPerpLineGradient(), ipsLine.getPerpLineYIntercept(ipsMidPnt), true);
		
		// get intersection points of every ellipse with the line perpendicular to that passing through the 2 ellipses intersection points
		ArrayList<Point2D.Double> thisE_IPsWithPerpLine = this.getIntPnts_WithLine(ipsPerpLine);
		ArrayList<Point2D.Double> otherE_IPsWithPerpLine = other_e.getIntPnts_WithLine(ipsPerpLine);

		
		// get the 2 intersection points of interest that is 1 point on every ellipse that is also in the area of the other ellipse
		//    => on the edge of the overlapping region
		Point2D.Double thisE_pnt = new Point2D.Double();
		Point2D.Double otherE_pnt = new Point2D.Double();

		boolean foundPntInThisE = false;
		boolean foundPntInOtherE = false;
		
		for (Point2D.Double pnt : thisE_IPsWithPerpLine){
			if (other_e.isPointInEllipse(pnt, false, 1e-6)){ 
				thisE_pnt = pnt;
				foundPntInThisE=true;
				break;
			}
		}
		
		for (Point2D.Double pnt : otherE_IPsWithPerpLine){
			if (this.isPointInEllipse(pnt, false, 1e-6)){ 
				otherE_pnt = pnt;
				foundPntInOtherE=true;
				break;
			}
		}
		
		if (!foundPntInThisE || !foundPntInOtherE){
			return null;
		}
		
	
		//order the intersection points -> the leftmost (in x-axis) is the first one to ensure anticlockwise rotation
		//if x-value of both pnts is equal (=> have line x=a), check the y-value st ip1.y < ip2.y
		//     to ensure an anti-clockwise order for the int pnts and thus ensure that it corresponds appropriately to the
		//     selection of the elliptic segment which is above or below the line
		Point2D.Double[] arcEndPnts = new Point2D.Double[2];
		arcEndPnts[0] = intpnts[0];
		arcEndPnts[1] = intpnts[1];
		if (arcEndPnts[0].x == arcEndPnts[1].x){
			if (arcEndPnts[0].y > arcEndPnts[1].y){
				arcEndPnts[0] = intpnts[1];
				arcEndPnts[1] = intpnts[0];
			}
		} else if (arcEndPnts[0].x > arcEndPnts[1].x){
				arcEndPnts[0] = intpnts[1];
				arcEndPnts[1] = intpnts[0];
		}


		// find polar coord angle for arcEndPnts[1], pnt on ellipse a, pnt on ellipse b, from arcEndPnts[0]
		double ang_arcEndPnt1 = GeometricOps.getPolarCoordAngForPnt (arcEndPnts[1], arcEndPnts[0]);
		double ang_thisE_pnt = GeometricOps.getPolarCoordAngForPnt (thisE_pnt, arcEndPnts[0]);
		double ang_otherE_pnt = GeometricOps.getPolarCoordAngForPnt (otherE_pnt, arcEndPnts[0]);

		// to handle cases where a point on 1 of the ellipses is below and another above the +ve x-axis, carry out the following
		// this works also in cases where both points are below the +ve x-axis
		if (ang_arcEndPnt1 > Math.PI){
			ang_arcEndPnt1 -= (2 * Math.PI);
		}
		if (ang_thisE_pnt > Math.PI){
			ang_thisE_pnt -= (2 * Math.PI);
		}
		if (ang_otherE_pnt > Math.PI){
			ang_otherE_pnt -= (2 * Math.PI);
		}

		// identify the overlapping elliptic segment above and below the line joining the 2 intersection points
		Ellipse e1;
		Ellipse e2;
		if ((ang_thisE_pnt < ang_arcEndPnt1) && (ang_otherE_pnt > ang_arcEndPnt1)){
			e1 = this;
			e2 = other_e;
		} else if ((ang_thisE_pnt > ang_arcEndPnt1) && (ang_otherE_pnt < ang_arcEndPnt1)){
			e1 = other_e;
			e2 = this;
		} else {
			System.out.println("Ellipse.getSharedZoneArea_ByInteg: Finding area of intersecting ellipses using integration: cannot order ellipses");
			return null;
		}
		
		Point2D.Double[] arcEndPnts_rev = new Point2D.Double[2];
		arcEndPnts_rev[0] = arcEndPnts[1];
		arcEndPnts_rev[1] = arcEndPnts[0];

		double areaE1 = e1.computeAreaUnderEllipticArc_ByInteg(arcEndPnts, dx);
		double areaE2 = e2.computeAreaUnderEllipticArc_ByInteg(arcEndPnts_rev, dx);
		areaOfDefiniteRegion = Math.abs(areaE1 + areaE2);
		
		DecimalFormat df = new DecimalFormat("##############################.########");

		try {
			return df.parse(df.format(areaOfDefiniteRegion)).doubleValue();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}


	
	
	// Private methods to determine precision -> used mainly to find the intersection points of ellipses 
	
	private boolean isInRange_excl(double x, double range_min, double range_max){
		return ((range_min < x) && (x < range_max)); 
	}
	
	private boolean areEqual(double x, double y, double l){
		return isZero(Math.abs(x-y), l); 
	}
	
	private boolean isEqualTo(double x, double reqVal, double l){
		return isZero(Math.abs(x-reqVal), l); 
	}
	
	private boolean isZero(double x){
		return isZero(x, 1e-16);
	}	
	
	private boolean isZero(double x, double l){
		return ((x > -l) && (x < l));
	}
	


	
	// Static methods 
	
	public static double computeSemiVerticalAxisGivenRotationZero(double x, double y, double xc, double yc, double semiHorizontalAxis){
		return (Math.abs( (semiHorizontalAxis*(y-yc)) / (Math.sqrt( Math.pow(semiHorizontalAxis,2)-Math.pow(x-xc,2) )) ));
	}
	public static double computeSemiHorizontalAxisGivenRotationZero(double x, double y, double xc, double yc, double semiVerticalAxis){
		return (Math.abs( (semiVerticalAxis*(x-xc)) / (Math.sqrt( Math.pow(semiVerticalAxis,2)-Math.pow(y-yc,2) )) ));
	}

	public static Ellipse generateARandomEllipse(String label, double a_b_min, double a_b_max, Point2D.Double axisRange_min, Point2D.Double axisRange_max, Random random){
		return generateARandomEllipse(label, a_b_min, a_b_max, axisRange_min, axisRange_max, false, random);
	}
		
	public static Ellipse generateARandomEllipse(String label, double a_b_min, double a_b_max, Point2D.Double axisRange_min, Point2D.Double axisRange_max, boolean restrictToCircles, Random random){
		
		// semi-major and semi-minor axis of ellipse
		double a = 0; //semi-major axis
		double b = 0; //semi-minor axis
		
		if (a_b_min < 0){
			a_b_min = Math.abs(a_b_min);
			System.out.println("Ellipse.generateARandomEllipse: the value of a_b_min was negative " +
							   "=> the absolute value was considered as the minimum possible width for the semi-major (a) and semi-minor (b) axis of the ellipse");
		}
		if (a_b_max < 0){
			a_b_max = Math.abs(a_b_max);
			System.out.println("Ellipse.generateARandomEllipse: the value of a_b_max was negative " +
							   "=> the absolute value was considered as the maximum possible width for the semi-major (a) and semi-minor (b) axis of the ellipse");
		}
		
		double min_xc = 0;
		double max_xc = 0;
		double min_yc = 0;
		double max_yc = 0;
		
		do{
			a = Utilities.randomNumberInRange(a_b_min, a_b_max, random);
			if (restrictToCircles){
				b = a;
			} else {
				b = Utilities.randomNumberInRange(a_b_min, a_b_max, random);
			}
			min_xc = axisRange_min.x + a;
			max_xc = axisRange_max.x - a;
			min_yc = axisRange_min.y + b;
			max_yc = axisRange_max.y - b;

		} while ((a < b) || (min_xc > max_xc) || (min_yc > max_yc));
		
		// centre of ellipse
		double xc = Utilities.randomNumberInRange(min_xc, max_xc, random);
		double yc = Utilities.randomNumberInRange(min_yc, max_yc, random);
		
		// rotation of ellipse 
		double rot = 0;
		if (!restrictToCircles){
			rot = Math.toDegrees(Utilities.randomNumberInRange(0, Math.PI, random));
		}
		
		return (new Ellipse (label, a, b, xc, yc, rot));
	}

	
}







