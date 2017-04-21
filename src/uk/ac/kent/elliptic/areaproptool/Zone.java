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
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;



/**
 * 
 * Instantiate, define and handle zones
 *
 */


public class Zone {
	
	// Private Data Members
	private String abstractDescription;
	private Area area;
	private ArrayList<Polygon> polygons;
	
	
	// Constructors 
	public Zone (String inAbstractDescription){
		abstractDescription = inAbstractDescription;
		area = new Area();
		polygons = new ArrayList<Polygon>();
	}
	public Zone (String inAbstractDescription, Area inArea, ArrayList<Polygon> inPolygons){
		abstractDescription = inAbstractDescription;
		setArea(inArea);
		setPolygons(inPolygons);
	}
	
	
	// Properties
	public String getAbstractDescription(){
		return abstractDescription;
	}
	
	public void setArea (Area inArea){
		area = inArea;
	}
	public Area getArea(){
		return area;
	}
	
	public void setPolygons(ArrayList<Polygon> inPolygons){
		polygons = inPolygons;
	}
	public ArrayList<Polygon> getPolygons(){
		return polygons;
	}
	

	
	// Instance Methods
	
	//    Checks 
	public boolean isTheZoneEmpty(){
		return (this.abstractDescription == "");
	}
	
	public boolean containsPoint(Point2D.Double pnt){
		for (Polygon poly : this.polygons){
			if (GeometricOps.isPointInPolygon(poly, pnt)){
				return true;
			}
		}
		return false;
	}
	
	
	//    Computing Zone Area
	public double computePartialArea(int polygonIndex){
		return (computePolygonArea(polygons.get(polygonIndex)));
	}
	
	public double computeTotalArea(){
		double totalArea = 0;
		
		for (Polygon p : polygons){
			totalArea += computePolygonArea(p);}
		
		return totalArea;
	}
	
	private double computePolygonArea (Polygon p){
		double area = 0.0;
		for (int i = 0; i < p.npoints - 1; i++) {
			area += (p.xpoints[i] * p.ypoints[i+1]) - (p.xpoints[i+1] * p.ypoints[i]);
		}
		area += (p.xpoints[p.npoints-1] * p.ypoints[0]) - (p.xpoints[0] * p.ypoints[p.npoints-1]);  

		area *= 0.5;
		
		if(area<0) {
			area =-area;
		}
		return area;
	}

	
	// Class Methods 
	public static ArrayList<Zone> removeEmptyZones (ArrayList<Zone> zones){
		ArrayList<Zone> newZones = (ArrayList<Zone>) zones.clone();
		
		for (Zone z : zones){
			if (z.isTheZoneEmpty()){
				newZones.remove(z);
			}
		}
		return newZones; 
	}
	
	public static Zone getZoneFromAbstractDescription (String zoneAbstractDescription, ArrayList<Zone> zones){
		
		for (Zone z: zones){
			if (z.getAbstractDescription().equals(zoneAbstractDescription))
				return z;
		}
		return null;
	}
	
	
	
	// More general methods
	
	public static Zone getZoneFromListWithName (ArrayList<Zone> zones, String zoneName){
		for (Zone z : zones){
			if (z.abstractDescription.equals(zoneName)){
				return z;
			}
		}
		return null;
	}
	
	public static ArrayList<Zone> getZones(ArrayList<ConcreteContour> ccs, boolean includeEmptyZones){
		
		HashMap<String, Area> zoneAreas = ConcreteContour.generateZoneAreas(ccs);
		Iterator itr = zoneAreas.entrySet().iterator();
		
		ArrayList<Zone> zones = new ArrayList<Zone>();
		
		while (itr.hasNext()){
			Entry<String,Area> strAreaPair = (Entry<String,Area>)itr.next();
			ArrayList<Polygon> zPolys = (ConcreteContour.polygonsFromArea(strAreaPair.getValue()));
			zones.add(new Zone(strAreaPair.getKey(), strAreaPair.getValue(), zPolys));
		}
		
		if (!includeEmptyZones){
			zones = Zone.removeEmptyZones(zones);
		}
		
		return zones;
	}
}
