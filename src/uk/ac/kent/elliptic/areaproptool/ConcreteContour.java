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

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.*;

import uk.ac.kent.euler.comparators.*;


/**
 * To instantiate, define and handle the curves of an Euler diagram using polygons
 * 
 */



public class ConcreteContour {

	protected String abstractContour;
	protected Polygon polygon;
	protected Area area;
	protected Polygon maxArea;
	protected Polygon minArea;
	protected boolean isCircle =false;
	protected String concurrentLabels= "";
	protected boolean isConcurrent = false;

	public ConcreteContour(String abstractContour, Polygon polygon) {
		this.abstractContour = abstractContour;
		this.polygon = polygon;
		resetArea();
	}
	public void addConcurrentLabel(String s){
		if(!concurrentLabels.contains(s))
			concurrentLabels+=s;
	}
	public String getConcurrentLabels(){
		return concurrentLabels;
	}
	public void setConcurrent(boolean concurrent){
		isConcurrent = concurrent;
	}
	public boolean getIsConcurrent(){
		return isConcurrent;
	}
	public void setLabel(String aLabel){abstractContour=aLabel;}

	public String getAbstractContour() {
		return abstractContour;
	}
	public Polygon getPolygon() {
		return polygon;
	}

	public Area getArea() {
		return area;
	}

	public void resetArea() {
		if (polygon == null) {
			area = new Area();
		} else {
			area = new Area(polygon);
		}
	}

	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;		
		resetArea();
	}
	

	
	/**
	 * Round to the given number of decimal places
	 */
	public static double round(double inAmount,int decimalPlaces) {

		long divider = 1;
		for(int i = 1; i<= decimalPlaces; i++) {
			divider *= 10;
		}
		double largeAmount = Math.rint(inAmount*divider);
		return(largeAmount/divider);
	}
	
	
	/**
	 * Generate the areas for each zone
	 */
	public static HashMap<String, Area> generateZoneAreas(ArrayList<ConcreteContour> concreteContours) {
		// We could try each possible intersection
		// but that is a guaranteed 2 power n algorithm
		// Here we take each intersecting pair and
		// test if any contours can be added to it.
		// The intersecting contours are then built up.
		// Then any intersections that are wholly contained
		// in the remaining contour set are removed.

		// the zones that  still may have further intersections
		ArrayList<String> activeZones = new ArrayList<String>();
		// all the zones tried for intersection
		ArrayList<String> triedZones = new ArrayList<String>();
		// the correct zones and areas
		HashMap<String, Area> currentZoneMap = new HashMap<String, Area>();
		// all tried maps
		HashMap<String, Area> zoneAreaMap = new HashMap<String, Area>(); 

		// create all existing intersections
		// then filter for those that dont exist except in other
		// zones - eg. the diagram "0 abc", first we create a b c ab ac abc
		// then remove all but abc by testing against contours not
		// in the intersection

		// start with the outside zone
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		 
		for (ConcreteContour concreteContour : concreteContours) {
			// String abstractContour = concreteContour.getAbstractContour();

			Area contourArea = new Area(concreteContour.getArea());

			Rectangle bounds = contourArea.getBounds();
			if (bounds.getX() < minX) {
				minX = bounds.getX();
			}
			if (bounds.getX() + bounds.getWidth() > maxX) {
				maxX = bounds.getX() + bounds.getWidth();
			}
			if (bounds.getY() < minY) {
				minY = bounds.getY();
			}
			if (bounds.getY() + bounds.getHeight() > maxY) {
				maxY = bounds.getY() + bounds.getHeight();
			}
		}
		if (minX > maxX) {
			minX = 0.0;
			maxX = 0.0;
			minY = 0.0;
			maxY = 0.0;
		}

		// use a rectangle that bounds all contours, to represent the outer zone
		int outerX1 = (int)round(minX,0) - 100;
		int outerX2 = (int)round(maxX,0) + 100;
		int outerY1 = (int)round(minY,0) - 100;
		int outerY2 = (int)round(maxY,0) + 100;
		
		Polygon outerPolygon = new Polygon();
		outerPolygon.addPoint(outerX1, outerY1);
		outerPolygon.addPoint(outerX2, outerY1);
		outerPolygon.addPoint(outerX2, outerY2);
		outerPolygon.addPoint(outerX1, outerY2);
		Area outerArea = new Area(outerPolygon);

		currentZoneMap.put("", outerArea);
		activeZones.add("");
		zoneAreaMap.put("", new Area(outerArea));

		
		// add the existing intersections
		while (activeZones.size() > 0) {
			while (activeZones.size() != 0) {

				String activeZone = activeZones.get(0);
				activeZones.remove(0);
				// test every contour intersection with a zone
				for (ConcreteContour concreteContour : concreteContours) {

					String abstractContour = concreteContour.getAbstractContour();
					ArrayList<ConcreteContour> duplicatedContours = new ArrayList<ConcreteContour>();
					for(ConcreteContour cc: concreteContours){
						if(cc.getAbstractContour().compareTo(abstractContour) == 0 && cc!= concreteContour)
							duplicatedContours.add(cc);
					}
					
					if (activeZone.indexOf(abstractContour) != -1) {
						// dont need to consider a zone that already contains the contour
						continue;
					}
					String testZone = activeZone + abstractContour;
					testZone = orderZone(testZone);

					if (triedZones.contains(testZone)) {
						// don't need to consider zones that have already been attempted
						continue;
					}
					triedZones.add(testZone);
					Area intersectArea = new Area(concreteContour.getArea());								 
					Area zoneArea = zoneAreaMap.get(activeZone);				
					intersectArea.intersect(zoneArea);
					if (!intersectArea.isEmpty()) {
						currentZoneMap.put(testZone, intersectArea);
						activeZones.add(testZone);
						zoneAreaMap.put(testZone, intersectArea);
					}	
					else{						
						if(duplicatedContours.size()!=0){
							for(ConcreteContour cc: duplicatedContours){
								intersectArea = new Area(cc.getArea());								 
								zoneArea = zoneAreaMap.get(activeZone);				
								intersectArea.intersect(zoneArea);
								if (!intersectArea.isEmpty()) {
									currentZoneMap.put(testZone, intersectArea);
									activeZones.add(testZone);
									zoneAreaMap.put(testZone, intersectArea);
								}	
							}
						}
					}
				}
			}
		}

		// filter out the intersections that are completely contained
		// in the other contours
		HashMap<String, Area> retZoneMap = new HashMap<String, Area>();
		for (String z : currentZoneMap.keySet()) {

			Area zoneArea = new Area(zoneAreaMap.get(z));
			for (ConcreteContour concreteContour : concreteContours) {
				String abstractContour = concreteContour.getAbstractContour();
				if (z.indexOf(abstractContour) == -1) {
					Area otherContoursArea = concreteContour.getArea();
					zoneArea.subtract(otherContoursArea);
				}
			}

			if (!zoneArea.isEmpty()) {
				retZoneMap.put(z, zoneArea);
			}
		}
	 

		return retZoneMap;
	}

	
	
	/**
	 * Generate an sorted list of zones from the interlinking polygons.
	 * Duplicate zones are not returned.
	 */
	public static String generateAbstractDiagramFromList(
			ArrayList<ConcreteContour> concreteContours) {

		if (concreteContours == null) {
			return "0";
		}

		HashMap<String, Area> zoneMap = generateZoneAreas(concreteContours);

		ArrayList<String> zones = new ArrayList<String>(zoneMap.keySet());

		sortZoneList(zones);

		StringBuffer zoneSB = new StringBuffer();
		Iterator<String> it = zones.iterator();
		while (it.hasNext()) {
			String z = it.next();
			if (z.equals("")) {
				z = "0";
			}
			zoneSB.append(z);
			if (it.hasNext()) {
				zoneSB.append(" ");
			}
		}

		return zoneSB.toString();
	}
	
	
	/**
	 * Deals with areas that have a nearly zero size section. This returns the
	 * area without that section. This repairs problems caused by
	 * Area.intersect.
	 */
	public static ArrayList<Polygon> polygonsFromArea(Area a) {
		if (!a.isPolygonal()) {
			// cant do anything if its not a polygon
			return null;
		}

		// create polygons, add them to the returned list if their area is large
		// enough
		ArrayList<Polygon> ret = new ArrayList<Polygon>();
		Polygon p = new Polygon();
		double[] coords = new double[6];
		PathIterator pi = a.getPathIterator(null);
		while (!pi.isDone()) {
			int coordType = pi.currentSegment(coords);
			if (coordType == PathIterator.SEG_CLOSE
					|| coordType == PathIterator.SEG_MOVETO) {
				if (coordType == PathIterator.SEG_CLOSE) {
					int x = (int)round(coords[0],0);
					int y = (int)round(coords[1],0);
					p.addPoint(x, y);
				}
				if (p.npoints > 2) { // no need to deal with empty polygons
					Rectangle2D boundingRectangle = p.getBounds2D();
					double boundingArea = boundingRectangle.getWidth()
							* boundingRectangle.getHeight();
					if (boundingArea >= 1.0) { // only add polygons of decent
												// size to returned area
						ret.add(p);
					}
				}
				p = new Polygon(); // start with the next polygon
				if (coordType == PathIterator.SEG_MOVETO) {
					int x = (int)round(coords[0],0);
					int y = (int)round(coords[1],0);
					p.addPoint(x, y);
				}
			}
			if (coordType == PathIterator.SEG_LINETO) {
				int x = (int)round(coords[0],0);
				int y = (int)round(coords[1],0);
				p.addPoint(x, y);
			}
			;
			if (coordType == PathIterator.SEG_CUBICTO) {
				System.out.println("Found a PathIterator.SEG_CUBICTO");
			}
			if (coordType == PathIterator.SEG_QUADTO) {
				System.out.println("Found a PathIterator.SEG_QUADTO");
			}

			pi.next();
		}
		return ret;
	}
	

	
	
	
	
	
	
	/** 
	 * Orders the zone string and detects duplicates. Returns null on duplicate.
	 */
	public static String orderZone(String zoneString) {
		
		ArrayList<String> splitZoneList = findContourList(zoneString);
		Collections.sort(splitZoneList);
		// check for duplicates
		if(hasDuplicatesInSortedList(splitZoneList)) {
			return null;
		}		
		// rebuild the string
		StringBuffer sortedZoneStringBuffer = new StringBuffer();
		for(String s: splitZoneList) {
			sortedZoneStringBuffer.append(s);
		}

		return sortedZoneStringBuffer.toString();
	}
	

	/** Takes a string and returns the list of characters in the string */
	public static ArrayList<String> findContourList(String zoneLabel) {
		String[] zones = zoneLabel.split("");
		ArrayList<String> zoneList = new ArrayList<String>(Arrays.asList(zones));
		zoneList.remove(""); // split adds a blank entry in index 0
		return zoneList;
	}
	
	
	/**
	 * Sorts the list of strings and checks for duplicates. Returns
	 * true if there are no duplicates, false if there are. The
	 * list is still sorted on a false return.
	 */
	public static boolean sortZoneList(ArrayList<String> zoneList) {
		ZoneStringComparator zComp = new ZoneStringComparator();
		Collections.sort(zoneList,zComp);
		
		if(hasDuplicatesInSortedList(zoneList)) {
			return false;
		}

		return true;

	}
		
	
	/**
	 * Finds duplicates in a the sorted List, returns true
	 * if duplicates found, false if not.
	 */
	public static boolean hasDuplicatesInSortedList(List list) {
		Object last = null;
		ListIterator li = list.listIterator();
		while(li.hasNext()) {
			Object o = li.next();
			if(last == null) {
				// first iteration
				last = o;
			} else {
				Object current = o;
				if(last.equals(current)) {
					return true;
				}
				last = current;
			}
		}
		return false;
	}

}

