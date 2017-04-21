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

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;



/**
 * 
 * Testing whether polygons have specific properties
 *
 */


public class TestingWithPolygons {
	

	public static double SignedPolygonArea(Point[] polygon,int N) {
	// based on http://paulbourke.net/geometry/polyarea
		
		Polygon P;
		int i,j;
		double area = 0;

		for (i=0;i<N;i++) {
			j = (i + 1) % N;
			area += polygon[i].x * polygon[j].y;
			area -= polygon[i].y * polygon[j].x;
		}
		area /= 2.0;

	   return(area);
	}
	 
	 
	public static Point2D.Double PolygonCenterOfMass(Point[] polygon,int N){
	// based on http://paulbourke.net/geometry/polyarea
		
		float cx=0,cy=0;
		float A=(float)SignedPolygonArea(polygon,N);
		Point2D.Double res=new Point2D.Double();
		int i,j;

		float factor=0;
		for (i=0;i<N;i++) {
			j = (i + 1) % N;
			factor=(polygon[i].x*polygon[j].y-polygon[j].x*polygon[i].y);
			cx+=(polygon[i].x+polygon[j].x)*factor;
			cy+=(polygon[i].y+polygon[j].y)*factor;
		}
		A*=6.0f;
		factor=1/A;
		cx*=factor;
		cy*=factor;
		res.x=cx;
		res.y=cy;
		return res;
	}


	
	
	// Methods to obtain the polygons in the zones, find their areas and filter out or identify the inappropriate ones

	
	
	public static HashMap<String, double[]> getAreaOfPolygonsInEachZoneOfDiagram (EllipseDiagram diag){
		
		// note that with this method, returned method will only contain the zones for which polygons have been found 
		// => if a zone is very small and polygons are not detected for it, then it will not be included in the hashmap
		
		// get the zones (as Zone objects) for the diagram -> before, get contours for the ellipses in the diagrams to be able to work out the polygons for the zones in the diagram 
		ArrayList<ConcreteContour> es_Contours = new ArrayList<ConcreteContour>(diag.ellipses.size());
		for (Ellipse e : diag.ellipses){
			es_Contours.add(e.getAsConcreteContour());
		}
		ArrayList<Zone> zones = Zone.getZones(es_Contours, false);
		
		// HashMap with the zone label (or abstract desc) as key and an array with the area of each poly in the zone as value 
		HashMap<String, double[]> areaOfPolysInZones = new HashMap<String, double[]>();
		
		// consider every zone 
		for (int z=0; z<zones.size(); z++){
			
			Zone currZone = zones.get(z);
			
			// get the polygons for each zone
			ArrayList<Polygon> polysInZone = currZone.getPolygons();
			
			System.out.println("Zone " + currZone.getAbstractDescription()+":  "+polysInZone.size()+" polygons");
			System.out.println("");
			
			double[] areaOfPolysInZone = new double[polysInZone.size()];
			double sumOfPolyAreasInZone = 0;
			
			// consider every polygon in the current zone
			for (int pz=0; pz < polysInZone.size(); pz++){
				Polygon polyInZone = polysInZone.get(pz);
				Point[] pntsInPolygon = new Point[polyInZone.npoints];
				
				// construct an array of all the points in the current polygon
				for (int i=0; i<polyInZone.npoints; i++){
					pntsInPolygon[i] = new Point(polyInZone.xpoints[i], polyInZone.ypoints[i]); 
				}
				
				// calculate the area of the polygon
				double signedAreaOfPolyInZone = SignedPolygonArea(pntsInPolygon,polyInZone.npoints);
				double absAreaOfPolyInZone = Math.abs(signedAreaOfPolyInZone);
				
				// save the area of the current polygon in zone
				areaOfPolysInZone[pz]=absAreaOfPolyInZone;
				
				sumOfPolyAreasInZone += Math.abs(absAreaOfPolyInZone);
				
				System.out.println("     - Area of polygon " + pz + " = " + signedAreaOfPolyInZone + " (signed) -> "+absAreaOfPolyInZone + " (abs)");
			}
			
			areaOfPolysInZones.put(currZone.getAbstractDescription(), areaOfPolysInZone);
			
			System.out.println("");
			
			// calculate some stats 
			double meanPolyAreaInZone = sumOfPolyAreasInZone / polysInZone.size(); 
			
			// output values to console
			System.out.println("     -> Sum of abs area of polygons in zone = " + sumOfPolyAreasInZone);
			System.out.println("     -> Mean abs area of polygons in zone = " + meanPolyAreaInZone);
			
			System.out.println(""); System.out.println("");
		}
		
		return areaOfPolysInZones;
	}
	

	public static HashMap<String, ArrayList<Double>> getAreaOfSignificantPolygonsInEachZoneOfDiagram(EllipseDiagram diag, double polyAreaDiscardThreshold){
		
		// PreDefine a hashmap to ensure that if the zone is so small that no polygons are identified, then it would still be included in the hashmap and thus this empty zone would still be detected
		
		String[] diagZoneLabels = diag.getZoneLabels();
		
		HashMap<String, ArrayList<Double>> areaOfSignificantPolysInZones = new HashMap<String, ArrayList<Double>>();
		for (String zoneLabel : diagZoneLabels){
			areaOfSignificantPolysInZones.put(zoneLabel, new ArrayList<Double>());
		}	
		
		HashMap<String, double[]> areaOfPolysInZones = getAreaOfPolygonsInEachZoneOfDiagram(diag);
		
		Iterator itrAreaOfPolysInZones = areaOfPolysInZones.entrySet().iterator();
		Entry<String, double[]> entryAreaOfPolysInZone;
		String zoneLabel;
		double[] areaOfPolysInZone;
		ArrayList<Double> areaOfSignificantPolysInZone;
		
		while (itrAreaOfPolysInZones.hasNext()){
			entryAreaOfPolysInZone = (Entry<String, double[]>)itrAreaOfPolysInZones.next();
			zoneLabel = entryAreaOfPolysInZone.getKey();
			areaOfPolysInZone = entryAreaOfPolysInZone.getValue();
			
			areaOfSignificantPolysInZone = new ArrayList<Double>();
			for (int i=0; i<areaOfPolysInZone.length; i++){
				if (areaOfPolysInZone[i] < polyAreaDiscardThreshold){
					continue;
				}
				areaOfSignificantPolysInZone.add(areaOfPolysInZone[i]);
			}
			areaOfSignificantPolysInZones.put(zoneLabel,areaOfSignificantPolysInZone);
		}
		
		return areaOfSignificantPolysInZones;
	}

	
	
	
	public static boolean doesDiagramContainDisconnectedLikeZones(EllipseDiagram diag, double polyAreaDiscardThreshold){
	
		HashMap<String, ArrayList<Double>> areaOfSignificantPolysInZones = getAreaOfSignificantPolygonsInEachZoneOfDiagram(diag, polyAreaDiscardThreshold);
		
		Iterator itrAreaOfSignificantPolysInZones = areaOfSignificantPolysInZones.entrySet().iterator();
		Entry<String, ArrayList<Double>> entryAreaOfSignificantPolysInZone;
		String zoneLabel;
		ArrayList<Double> areaOfSignificantPolysInZone;

		
		while (itrAreaOfSignificantPolysInZones.hasNext()){
			entryAreaOfSignificantPolysInZone = (Entry<String, ArrayList<Double>>)itrAreaOfSignificantPolysInZones.next();
			zoneLabel = entryAreaOfSignificantPolysInZone.getKey();
			areaOfSignificantPolysInZone = entryAreaOfSignificantPolysInZone.getValue();
			
			if (areaOfSignificantPolysInZone.size() > 1){
				return true;
			}
		}
		
		return false;
	}
	
	
	public static boolean doesDiagramContainSmallZones(EllipseDiagram diag, double polyAreaDiscardThreshold){
		
		HashMap<String, ArrayList<Double>> areaOfSignificantPolysInZones = getAreaOfSignificantPolygonsInEachZoneOfDiagram(diag, polyAreaDiscardThreshold);
		
		Iterator itrAreaOfSignificantPolysInZones = areaOfSignificantPolysInZones.entrySet().iterator();
		Entry<String, ArrayList<Double>> entryAreaOfSignificantPolysInZone;
		String zoneLabel;
		ArrayList<Double> areaOfSignificantPolysInZone;
		
		while (itrAreaOfSignificantPolysInZones.hasNext()){
			entryAreaOfSignificantPolysInZone = (Entry<String, ArrayList<Double>>)itrAreaOfSignificantPolysInZones.next();
			zoneLabel = entryAreaOfSignificantPolysInZone.getKey();
			areaOfSignificantPolysInZone = entryAreaOfSignificantPolysInZone.getValue();
			
			if (areaOfSignificantPolysInZone.size() <= 0){
				return true;
			}
		}
		
		return false;
	}
	
	
	public static enum DiagramZoneCondition {NOTEMPTYLIKE_CONNECTED, EMPTY_LIKE, DISCONNECTED_LIKE};
	
	public static DiagramZoneCondition doesDiagramContainEmptyLikeOrDisconnectedZones(EllipseDiagram diag, double polyAreaDiscardThreshold){
		
		HashMap<String, ArrayList<Double>> areaOfSignificantPolysInZones = getAreaOfSignificantPolygonsInEachZoneOfDiagram(diag, polyAreaDiscardThreshold);
		
		Iterator itrAreaOfSignificantPolysInZones = areaOfSignificantPolysInZones.entrySet().iterator();
		Entry<String, ArrayList<Double>> entryAreaOfSignificantPolysInZone;
		String zoneLabel;
		ArrayList<Double> areaOfSignificantPolysInZone;
		int noOfSignificantPolysInZone;
	
		
		while (itrAreaOfSignificantPolysInZones.hasNext()){
			entryAreaOfSignificantPolysInZone = (Entry<String, ArrayList<Double>>)itrAreaOfSignificantPolysInZones.next();
			zoneLabel = entryAreaOfSignificantPolysInZone.getKey();
			areaOfSignificantPolysInZone = entryAreaOfSignificantPolysInZone.getValue();
			noOfSignificantPolysInZone = areaOfSignificantPolysInZone.size();
			
			if (noOfSignificantPolysInZone < 1){
				return DiagramZoneCondition.EMPTY_LIKE;
			
			} else if (noOfSignificantPolysInZone > 1){
				return DiagramZoneCondition.DISCONNECTED_LIKE;
			}
		}
		
		return DiagramZoneCondition.NOTEMPTYLIKE_CONNECTED;
	}
	
}
