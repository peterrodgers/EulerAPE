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

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;


/**
 * To instantiate, define and handle an Euler diagram drawn with ellipses
 * 
 */


public class EllipseDiagram {
    
	public enum FitnessMeasure {BIASED_FOR_SMALL_AREA_DIMENSIONLESS_NOTSQ, BIASED_FOR_SMALL_AREA_DIMENSIONLESS_SQ, BIASED_FOR_SMALL_AREA_NOTDIMENSIONLESS, STRESS, 
		                        CHOWRODGERS_IDEAL_MAXREGFIT, CHOWRODGERS_IDEAL_MEANREGFIT, CHOWRODGERS_IDEAL_SUMREGFIT, 
		                        ABSERROR_OVER_REQTOTAREA_MAXREGFIT,  ABSERROR_OVER_REQTOTAREA_MEANREGFIT,  ABSERROR_OVER_REQTOTAREA_SUMREGFIT,
		                        ABSERROR_OVER_REQTOTAREA_MAXREGFIT_SCALEACTTOREQ,  ABSERROR_OVER_REQTOTAREA_MEANREGFIT_SCALEACTTOREQ,  ABSERROR_OVER_REQTOTAREA_SUMREGFIT_SCALEACTTOREQ,
		                        RELATIVE_ERROR_MAXREGFIT, RELATIVE_ERROR_MEANREGFIT, RELATIVE_ERROR_SUMREGFIT, 
		                        RELATIVE_ERROR_MAXREGFIT_SCALEACTTOREQ, RELATIVE_ERROR_MEANREGFIT_SCALEACTTOREQ, RELATIVE_ERROR_SUMREGFIT_SCALEACTTOREQ, 
		                        RELATIVE_ERROR_NORMALIZED_MAXREGFIT, RELATIVE_ERROR_NORMALIZED_MEANREGFIT, RELATIVE_ERROR_NORMALIZED_SUMREGFIT, 
		                        LOG_MAXREGFIT, LOG_MEANREGFIT, LOG_SUMREGFIT, 
		                        LOG_NORMALIZED_MAXREGFIT, LOG_NORMALIZED_MEANREGFIT, LOG_NORMALIZED_SUMREGFIT, 
		                        ASPECT_RATIO_MAXREGFIT, ASPECT_RATIO_MEANREGFIT, ASPECT_RATIO_SUMREGFIT,
		                        ASPECT_RATIO_NORMALIZED_MAXREGFIT, ASPECT_RATIO_NORMALIZED_MEANREGFIT, ASPECT_RATIO_NORMALIZED_SUMREGFIT};
	public static final FitnessMeasure DEFAULT_FITNESS_MEASURE = FitnessMeasure.BIASED_FOR_SMALL_AREA_NOTDIMENSIONLESS;
	
	
	// Static fields
	// ... might want to include the empty zone in the lists below
	public static char ellipse1Label = 'a';

	public static final int UNDEFINED = 0;
	public static final int POLYGONS = 1;
	public static final int INTEGRATION = 2;
	public static final int SEGMENTS = 3;
	
	public static final double PRECISION_ISZERO = 1e-15;

	
	// Data fields 
	protected ArrayList<Ellipse> ellipses = null;
	protected String[] zoneLabels = {};
	protected boolean toPolysForIntPnts = false;
	protected int methodToComputeRegionAreas;
	protected HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>> intPntsOfEllipses = null;
	protected HashMap<String, Double> zoneAreas = null; 
	public Boolean zoneAreasComputedSuccessfully = null;
	protected boolean incorrectIntPnts = false;
	protected boolean considerLessPrecisePossibleIntPnts; 
	
	protected HashMap<String, Double> wilkinson_yHats;  //could not output this out from the method and thus had to declare it as a global instance method -> used to calculate Wilkinson's residual and stress measure
	
	
	// Constructor
	public EllipseDiagram(ArrayList<Ellipse> ellipses, boolean considerLessPrecisePossibleIntPnts){
		this.toPolysForIntPnts = false;
		this.methodToComputeRegionAreas = UNDEFINED;
		init(ellipses, considerLessPrecisePossibleIntPnts, methodToComputeRegionAreas, false);
	}
	public EllipseDiagram(ArrayList<Ellipse> ellipses, boolean considerLessPrecisePossibleIntPnts, int methodToComputeRegionAreas){
		this.toPolysForIntPnts = false;
		init(ellipses, considerLessPrecisePossibleIntPnts, methodToComputeRegionAreas, false);
	}
	public EllipseDiagram(ArrayList<Ellipse> ellipses, boolean considerLessPrecisePossibleIntPnts, boolean toPolysForIntPnts, int methodToComputeRegionAreas){
		this.toPolysForIntPnts = toPolysForIntPnts;
		init(ellipses, considerLessPrecisePossibleIntPnts, methodToComputeRegionAreas, false);
	}
	private void init(ArrayList<Ellipse> ellipses, boolean considerLessPrecisePossibleIntPnts, int methodToComputeRegionAreas, boolean findZoneAreas){
		this.ellipses = ellipses;
		initIPsHashMap(); // this must be invoked after the ellipses are set
		initZoneLabelsAndAreasHashMap(); // zone labels are set in this method
		this.considerLessPrecisePossibleIntPnts=considerLessPrecisePossibleIntPnts;
		this.methodToComputeRegionAreas = methodToComputeRegionAreas;
		if (findZoneAreas){ 
			recomputeZoneAreas(); 
		} else {
			zoneAreasComputedSuccessfully = null;
		}
	}
	private void initIPsHashMap() { 
		// initializing hashmaps to handle all possible zone areas and intersection points
		// => if n ellipses -> max no. of zones = (2^n) -1 (since we are excluding the empty zone)
		//                  -> intersection points = a pair for every two ellipses 
		this.intPntsOfEllipses = new HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>>();
		
		Ellipse e1;
		Ellipse e2;
		int i; 
		int j;
		ArrayList<Point2D.Double> ipsEllPair;
		ArrayList<Ellipse> ellPair;
		for (i=0; i<this.ellipses.size(); i++){
			for (j=i+1; j<this.ellipses.size(); j++){
				e1 = this.ellipses.get(i);
				e2 = this.ellipses.get(j);
				if (Utilities.convertCharToASCIIint(e1.label.toLowerCase()) > Utilities.convertCharToASCIIint(e2.label.toLowerCase())){
					e1 = this.ellipses.get(j);
					e2 = this.ellipses.get(i);
				}
			
				ipsEllPair = new ArrayList<Point2D.Double>(4);
				ipsEllPair.add(null);
				ipsEllPair.add(null); 
				ipsEllPair.add(null); 
				ipsEllPair.add(null);
				
				// sort the ellipses in ascending order based on their label
				ellPair = new ArrayList<Ellipse>(2);
				ellPair.add (e1);
				ellPair.add (e2);
				this.intPntsOfEllipses.put(ellPair, ipsEllPair);
			}
		}	
	}
	private void initZoneLabelsAndAreasHashMap() { 
		// Set zone labels
		this.zoneLabels = EllipseDiagramOps.getZoneLabels(ellipses.size(), false); 
		// ... assuming that the ellipses are labelled as a,b etc (with first ellipse = a)
		// ... assuming that the labels are sorted in ascending order
		
		// Init hashmap which will store the areas of the zones
		this.zoneAreas = new HashMap<String, Double>();
		for (String zl : zoneLabels){
			this.zoneAreas.put(zl, null);
		}
	}
	
	
	// Properties - getters and setters
	
	public ArrayList<Ellipse> getEllipses() {
		return ellipses;
	}
	public void setEllipses(ArrayList<Ellipse> in_ellipses) {
		
		if ((in_ellipses == null) || (in_ellipses.size() != this.ellipses.size()) || (!isValid(in_ellipses, this.considerLessPrecisePossibleIntPnts))){return;}
		// to check validity every time might be too time consuming and resourceful => in such cases, do not carry out this check
		
		this.ellipses = in_ellipses;
		recomputeZoneAreas();
	}
	public String[] getZoneLabels() {
		return zoneLabels;
	}
	public HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>> getIntPntsOfEllipses() {
		return intPntsOfEllipses;
	}
	public HashMap<String, Double> getZoneAreas() {
		if (this.zoneAreasComputedSuccessfully == null){
			recomputeZoneAreas();
		}
		return zoneAreas;
	}
	
	
	private void recomputeIntPntsOfEllipses(){
		computeIntPntsOfEllipses(); // intpnts are saved in this.intPntsOfEllipses immediately to avoid creating extra objects
	}
	
	public void recomputeZoneAreas(){
	    computeZoneAreas(true); // areas are saved in this.zoneAreas immediately to avoid creating extra objects 

		if (this.methodToComputeRegionAreas != POLYGONS){
			if (this.zoneAreasComputedSuccessfully){
				this.zoneAreas = EllipseDiagramOps.sortZoneAreasAsInLabelArray(zoneLabels, this.zoneAreas);
			}
		}
		
	}
	
	public double diagError(HashMap<String,Double> reqAreaSpecs){
		String errorsStr = computeErrors(reqAreaSpecs, false);
		
		int diagErrorIndex = 9;
		int separatorIndex =0;
		StringBuffer errorsStrBuffer = new StringBuffer(errorsStr);
		for (int i=0; i < (diagErrorIndex-1); i++){
			separatorIndex = errorsStrBuffer.indexOf("|");
			errorsStrBuffer.delete(0,separatorIndex+1);
		}
		separatorIndex = errorsStrBuffer.indexOf("|");
		return (Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(errorsStrBuffer.substring(0,separatorIndex))));
	}

	
	
	

	// Methods

	public EllipseDiagram clone(){
		EllipseDiagram diagClone = new EllipseDiagram(getEllipsesClone(), this.considerLessPrecisePossibleIntPnts, this.toPolysForIntPnts, this.methodToComputeRegionAreas);
		diagClone.zoneLabels = this.zoneLabels.clone();
		diagClone.intPntsOfEllipses = getIntPntsClone();
		diagClone.zoneAreas = getZoneAreasClone();
		diagClone.zoneAreasComputedSuccessfully = (this.zoneAreasComputedSuccessfully==null)?null:new Boolean (this.zoneAreasComputedSuccessfully.booleanValue());
		return diagClone;
	}
	
	public ArrayList<Ellipse> getEllipsesClone(){
		return getEllipsesClone(this.ellipses);
	}
	public ArrayList<Ellipse> getEllipsesClone(ArrayList<Ellipse> es){
		ArrayList<Ellipse> ellipsesClone = new ArrayList<Ellipse>(es.size());  
		for (Ellipse e : es){
			ellipsesClone.add(e.clone());
		}
		return ellipsesClone;
	}
	
	private ArrayList<Point2D.Double> getArrayPoint2DDoubleClone(ArrayList<Point2D.Double> pnts){
		ArrayList<Point2D.Double> pntsClone = new ArrayList<Point2D.Double>(pnts.size());
		for (Point2D.Double p :pnts){

			if (p==null){
				pntsClone.add(null);
			}else{
				pntsClone.add(new Point2D.Double(p.x, p.y));
			}
		}
		return pntsClone;
	}
 
	
	public HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>> getIntPntsClone(){
		HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>> intPntsClone = new HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>>();
		
		Iterator itr = this.intPntsOfEllipses.entrySet().iterator();
		Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>> ip;
		while (itr.hasNext()){
			ip = (Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>>)itr.next();
			intPntsClone.put(getEllipsesClone((ArrayList<Ellipse>)ip.getKey()), getArrayPoint2DDoubleClone(((ArrayList<Point2D.Double>)ip.getValue())));
		}
		
		return intPntsClone;
	}
		
 
	public HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>> getIntPntsClone(HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>> ipsAndEllipsePairHashMap_toCopyTo){
		
		return cloneIntPntsFromTo (this.intPntsOfEllipses, ipsAndEllipsePairHashMap_toCopyTo);
		
	}

	public static HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>> cloneIntPntsFromTo(HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>> ipsAndEllipsePairHashMap_toCopyFrom, HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>> ipsAndEllipsePairHashMap_toCopyTo){
		
		ArrayList<Point2D.Double> ipsAndEllipsePair_toCopyTo_value = null;
		Point2D.Double temp_ip;
		
		
		Iterator itr = ipsAndEllipsePairHashMap_toCopyFrom.entrySet().iterator();
		Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>> ipsAndEllipsePair_toCopyFrom;
		while (itr.hasNext()){
			ipsAndEllipsePair_toCopyFrom = (Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>>)itr.next();
			ArrayList<Ellipse> ipsAndEllipsePair_toCopyFrom_key = ipsAndEllipsePair_toCopyFrom.getKey();
			ArrayList<Point2D.Double> ipsAndEllipsePair_toCopyFrom_value = ipsAndEllipsePair_toCopyFrom.getValue();
			
			Iterator itr2 = ipsAndEllipsePairHashMap_toCopyTo.entrySet().iterator();
			while(itr2.hasNext()){
				Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>> ipsAndEllipsePair_toCopyFrom_toCopyTo = (Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>>)itr2.next();
				ArrayList<Ellipse> ipsAndEllipsePair_toCopyFrom_toCopyTo_key = ipsAndEllipsePair_toCopyFrom_toCopyTo.getKey(); 
					
			   if ( ((ipsAndEllipsePair_toCopyFrom_toCopyTo_key.get(0).label.equals(ipsAndEllipsePair_toCopyFrom_key.get(0).label) && (ipsAndEllipsePair_toCopyFrom_toCopyTo_key.get(1).label.equals(ipsAndEllipsePair_toCopyFrom_key.get(1).label)))) ||
				     ((ipsAndEllipsePair_toCopyFrom_toCopyTo_key.get(0).label.equals(ipsAndEllipsePair_toCopyFrom_key.get(1).label) && (ipsAndEllipsePair_toCopyFrom_toCopyTo_key.get(1).label.equals(ipsAndEllipsePair_toCopyFrom_key.get(0).label)))) ){

					ipsAndEllipsePair_toCopyTo_value = ipsAndEllipsePair_toCopyFrom_toCopyTo.getValue();	
					break;
				}	
			}
			
			
		    int i=0;
			
			for (Point2D.Double ip : ipsAndEllipsePair_toCopyFrom_value){
				if (ip == null){
					ipsAndEllipsePair_toCopyTo_value.set(i,null);
				} else {
					ipsAndEllipsePair_toCopyTo_value.set(i,new Point2D.Double(new Double(ip.x), new Double(ip.y)));
				}
				i++;
			}		
		}
		
		return ipsAndEllipsePairHashMap_toCopyTo;
	}
	
	public HashMap<String, Double> getZoneAreasClone(){
		
		HashMap<String, Double> zoneAreasClone = new HashMap<String, Double>();
		return cloneZoneAreasFromTo(this.zoneAreas, zoneAreasClone);
	}
	
	public HashMap<String, Double> getZoneAreasClone(HashMap<String, Double> zoneAreasHashMap_toCopyTo){
		
		return cloneZoneAreasFromTo(this.zoneAreas, zoneAreasHashMap_toCopyTo);
	}
	
	public static HashMap<String, Double> cloneZoneAreasFromTo(HashMap<String, Double> zoneAreasHashMap_toCopyFrom, HashMap<String, Double> zoneAreasHashMap_toCopyTo){
		
		if (zoneAreasHashMap_toCopyFrom==null){return null;}
		
		Iterator itr = zoneAreasHashMap_toCopyFrom.entrySet().iterator();
		Entry<String, Double> za;
		while (itr.hasNext()){
			za = (Entry<String, Double>)itr.next();
			if ((za.getKey()!=null) &&(za.getValue()!=null)){
				zoneAreasHashMap_toCopyTo.put((String)za.getKey(), new Double(((Double)za.getValue()).doubleValue()));
			}
		}
		
		return zoneAreasHashMap_toCopyTo;
	}
	

	public ArrayList<ConcreteContour> getEllipsesAsContours (){
		// invokes the static method
		return (getEllipsesAsContours(this.ellipses));
	}


	//  Computing intersection points between ellipses
		
	public int[] getNoOfIntPntsPerEntryInHashMap(){
		int[] noOfIntPntsPerEntry_nonfiltered = new int[this.intPntsOfEllipses.size()];
		
		int c = 0;
		Iterator itr = this.intPntsOfEllipses.entrySet().iterator();
		Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>> intPntsForEs;
		ArrayList<Point2D.Double> currIps;
		while (itr.hasNext()){
			intPntsForEs = (Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>>)itr.next();
			currIps = intPntsForEs.getValue();
			noOfIntPntsPerEntry_nonfiltered[c] = 0;
			for (Point2D.Double p : currIps){
				if (p != null){
					noOfIntPntsPerEntry_nonfiltered[c]++;
				}
			} 
			c++;
		}
		
		return noOfIntPntsPerEntry_nonfiltered; 
	}
	
	public boolean correctNoOfIntPnts(){
		int[] noOfIntPntsPerEntryInHashMap = getNoOfIntPntsPerEntryInHashMap();
		
		int expectedNosOfEllPairs = Utilities.noOfCombinations(ellipses.size(), 2); 
			
		if (noOfIntPntsPerEntryInHashMap.length != expectedNosOfEllPairs){
			return false;
		}
		for (int noOfIps : noOfIntPntsPerEntryInHashMap){
			if (noOfIps != 2){
				return false;
			}
		}
		return true;
	}
	
	public boolean isIntPntsHashMapEmpty(){
		// to avoid running out of memory, most of the HashMap are reused. Hence, before the first sets of intersection points are found, the HashMap for these intersection points is initialised (rather than left null)
		// => for this reason, if we want to check whether an attempt has been made to compute the intersection points, it is not enough to check whether the HashMap for these intersection points is null 
		//(since the structure is initialised before an attempt is made to compute the intersection points)
		//    Instead we have to check whether the HashMap is empty. Thus is for all the possible pairwise ellipses that are no intersection points, then we can assume it is empty 
		int[] noOfIntPntsPerEntryInHashMap = getNoOfIntPntsPerEntryInHashMap();
		for (int noOfIps : noOfIntPntsPerEntryInHashMap){
			if (noOfIps > 0){
				return false;
			}
		}
		return true;
	}
	
	

	public void computeIntPntsOfEllipses (){
	
		ArrayList<Point2D.Double> intPnts;
		ArrayList<Ellipse> c_es = new ArrayList<Ellipse>(2);
		boolean firstEllPair = true; 

		Ellipse e1;
		Ellipse e2;
		int i;
		int j;
		int k;
		int l;
		int distinctIpsCount;
		int nextIpIndex;
		boolean ipAlreadyInList;
		Point2D.Double temp_ip;
		ArrayList<Point2D.Double> currIntPnts;
		for (i=0; i<this.ellipses.size(); i++){
			for (j=i+1; j<this.ellipses.size(); j++){
				e1 = this.ellipses.get(i);
				e2 = this.ellipses.get(j);
				
				intPnts = null;
				if (this.toPolysForIntPnts){
					intPnts = e1.getIntPnts_WithEllipse_AsPoly(e2);
				} else {
					intPnts = e1.getIntPnts_WithEllipse(e2, considerLessPrecisePossibleIntPnts);
				}
				
				if (firstEllPair){
					c_es.add(e1);
					c_es.add(e2);
					firstEllPair = false;
				} else {
					c_es.set(0, e1);
					c_es.set(1, e2);
				}
				
				Iterator itr2 = this.intPntsOfEllipses.entrySet().iterator();
				currIntPnts = new ArrayList<Point2D.Double>();
				while(itr2.hasNext()){
					Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>> iph = (Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>>)itr2.next();
					ArrayList<Ellipse> iphkey = iph.getKey(); 
					if ( (iphkey.get(0).label.equals(c_es.get(0).label) && (iphkey.get(1).label.equals(c_es.get(1).label))) ||
						 (iphkey.get(0).label.equals(c_es.get(1).label) && (iphkey.get(1).label.equals(c_es.get(0).label))) ){
						currIntPnts = iph.getValue();
						break;
					}	
				}
				
				if ((intPnts == null) || (intPnts.size() == 0)){
					currIntPnts.set(0,null);
					currIntPnts.set(1,null);
					currIntPnts.set(2,null);
					currIntPnts.set(3,null);
				} else {
					
					distinctIpsCount = intPnts.size();
					nextIpIndex = 0;
					for (k=0; (k < intPnts.size()) && (k < 4); k++){
						ipAlreadyInList = false;
						for (l=0; l < nextIpIndex; l++){
							if (areEqual(currIntPnts.get(l).x, intPnts.get(k).x) && areEqual(currIntPnts.get(l).y, intPnts.get(k).y)){
								ipAlreadyInList = true;
								break;
							}
						}
						if (ipAlreadyInList){
							distinctIpsCount--;
						} else {
							temp_ip = currIntPnts.get(nextIpIndex);
							if (temp_ip == null){
								temp_ip = new Point2D.Double();
								currIntPnts.set(k, temp_ip);
							}
							temp_ip.x = intPnts.get(k).x;
							temp_ip.y = intPnts.get(k).y;
							
							nextIpIndex++;
						}
					}
					
					if (distinctIpsCount < 4){
						for (k=distinctIpsCount; k < 4; k++){
							currIntPnts.set(k,null);
						}
					}
				} 
			}
		}
		
	}

	
	// ... compute zone areas in terms of polygons 
	//  Computing zone areas ...
	
	//  ... using polygons
	public void computeZoneAreas_AsPolys(){
		HashMap<String, Double> intAreasOfEllipses = computeZoneAreas_AsPolys(this);
		
		Iterator itr = intAreasOfEllipses.entrySet().iterator();
		while (itr.hasNext()){
			Entry<String, Double> za = (Entry<String, Double>)itr.next();
			this.zoneAreas.put(za.getKey(), za.getValue());
		}
	}
	
	public static HashMap<String, Double> computeZoneAreas_AsPolys(EllipseDiagram diag){
		HashMap<String, Double> intAreasOfEllipses = new HashMap<String, Double>();

		// find the zones in the diagram made up of polygon contour contours
		ArrayList<ConcreteContour> es_Contours = new ArrayList<ConcreteContour>(diag.ellipses.size());
		for (Ellipse e : diag.ellipses){
			es_Contours.add(e.getAsConcreteContour());
		}
		ArrayList<Zone> zones = Zone.getZones(es_Contours, false);
		
	
		// sort the zones in ascending order
		String[] zonesAbsDesc = new String[zones.size()];
		int i = 0;
		for (Zone z : zones){
			zonesAbsDesc[i] = z.getAbstractDescription().trim();
			i++;
		}
		Arrays.sort(zonesAbsDesc);
		
		Zone[] zonesInAscOrder = new Zone[zones.size()];
		i =0;
		for (String zStr : zonesAbsDesc){
			zonesInAscOrder[i] = Zone.getZoneFromListWithName(zones, zStr);
			i++;
		}

		//  the area of every zone
		for(Zone z : zonesInAscOrder){
			intAreasOfEllipses.put(z.getAbstractDescription(), z.computeTotalArea());
		}		
		
		//add the empty zones and set their area to 0
		String[] zoneLabels = diag.getZoneLabels();
		for(String zl : zoneLabels){
			if (!intAreasOfEllipses.keySet().contains(zl)){
				intAreasOfEllipses.put(zl, 0.0);
			}
		}
		
		return intAreasOfEllipses;
	}

	
	// ... compute zone areas by integration 
	//  ... by integration
	
		
	private double computeAreaOf3EllipseZone_ByInteg(ArrayList<Point2D.Double> ips, ArrayList<ArrayList<Ellipse>> ellsForIps, int dx_or_dy){
		double area =0;
			
		// Declare vars at this point to be able to reuse references => avoid extra usage of memory
		Point2D.Double ip1;
		Point2D.Double ip2;
		boolean dx;
		double diff_x;
		double diff_y;
		ArrayList<Ellipse> ellsForIp1;
		ArrayList<Ellipse> ellsForIp2;
		Ellipse eForArea = new Ellipse("",0,0,0,0,0);
		boolean eForAreaFound = false;
		MyLine ipsLine = null; //this must be null
		Point2D.Double ipsMidPnt = new Point2D.Double ();
		MyLine ipsPerpLine = null; //this must be null
		ArrayList<Point2D.Double> ipsWithPerpLine;
		ArrayList<Point2D.Double> ipsWithPerpLine_otherE = new ArrayList<Point2D.Double>();  
		Point2D.Double ipClosestToLine = new Point2D.Double();	
		int ipClosestToLine_index = 0;
		int pCnt=0;
		Point2D.Double ipClosestToLine_otherE = new Point2D.Double(); 

		
		Double distCurrClosest;
		Double distCurrClosest_otherE = null;
		double dist;
		Point2D.Double[] arcEndPnts = new Point2D.Double[2];
		double ang_arcEndPnt2;
		double ang_ipClostestToLine;
		Point2D.Double currArcEndPnt0;
		Point2D.Double currArcEndPnt1;
		double areaUnderArc;
		Point2D.Double[] arcEndPnts_rev = new Point2D.Double[2];
		double[] lineLimitsForArea_x = new double[2];
		double[] lineLimitsForArea_y = new double[2];
		double areaUnderLine;
		double areaCurrSegment;
		
		Triangle triangle = new Triangle(ips.get(0), ips.get(1), ips.get(2));
		
		int noOfPnts = ips.size();
		int i; int j;
		int c1; int c2;
		for (i = 0; i < noOfPnts; i++){
			for (j = i+1; j < noOfPnts; j++){
				ip1 = ips.get(i);
				ip2 = ips.get(j);
				

				dx = true;
				if ((dx_or_dy != 1) || (dx_or_dy != 2)){
					diff_x = Math.abs (ip2.x - ip1.x);
					diff_y = Math.abs (ip2.y - ip1.y);

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

				
				ellsForIp1 = ellsForIps.get(i);
				ellsForIp2 = ellsForIps.get(j);
				
				//find the ellipse which should be used to find the area of the elliptic segment
				eForAreaFound = false;
				for (c1 = 0; c1 < ellsForIp1.size(); c1++){
					for (c2 = 0; c2 < ellsForIp2.size(); c2++){
						if (ellsForIp1.get(c1).label.equals(ellsForIp2.get(c2).label)){
							eForArea = ellsForIp1.get(c1);
							eForAreaFound = true;
							break;
						}
					}
					if (eForAreaFound){break;}
				}
				if (!eForAreaFound){
					System.out.println ("EllipseDiagram.getAreaOf3EllipseZone_ByInteg: cannot identify the ellipse which should be used to find the relevant area");
					return 0;
				}
				
				
				//order the intersection points such that an anti-clockwise rotation is assumed and to this this use a similar manner and used to 2-ellips intersections
				
				// line between intersection points, its mid-point, line perpendicular to it
				if (ipsLine == null){ 
					ipsLine =  new MyLine(ip1, ip2, true);
				} else {
					ipsLine.resetProperties(ip1.x, ip1.y, ip2.x, ip2.y, true);
				}
				ipsMidPnt.x = (ip1.x + ip2.x)/2;
				ipsMidPnt.y = (ip1.y + ip2.y)/2;
				if (ipsPerpLine == null){ 
					ipsPerpLine = new MyLine(ipsLine.getPerpLineGradient(), ipsLine.getPerpLineYIntercept(ipsMidPnt), true);
				} else {
					ipsPerpLine.resetProperties(ipsLine.getPerpLineGradient(), ipsLine.getPerpLineYIntercept(ipsMidPnt), true);
				}
				

				// get intersection points of every ellipse with the line perpendicular to that passing through the 2 ellipses intersection points				
				//   choose the intersection point closest to the midpoint
				ipsWithPerpLine = eForArea.getIntPnts_WithLine(ipsPerpLine);
				distCurrClosest = null;
				ipClosestToLine_index = 0;
				pCnt=0;
				for (Point2D.Double p : ipsWithPerpLine){
					dist = GeometricOps.distanceBetween2Points(p, ipsMidPnt);
					if ((distCurrClosest == null) || (dist < distCurrClosest)){
						distCurrClosest = dist;
						ipClosestToLine = p;
						ipClosestToLine_index = pCnt;
						pCnt++;
					}
				}
				
				// Check whether there is a point on another ellipse which is on ipsPerpLine and closer to the triangle than the current ellipse 
				// ... is used later on
				distCurrClosest_otherE = null;
				for (Ellipse e : this.ellipses){
					if (e.label.equals(eForArea.label)){continue;}
					
					ipsWithPerpLine_otherE = e.getIntPnts_WithLine(ipsPerpLine);
					for (Point2D.Double p : ipsWithPerpLine_otherE){
						dist = GeometricOps.distanceBetween2Points(p, ipsMidPnt);
						if (((distCurrClosest_otherE == null) || (dist < distCurrClosest_otherE)) && (dist < distCurrClosest)){
							distCurrClosest_otherE = dist;
							ipClosestToLine_otherE = p;
						}
					}
				}
			

				//order the intersection points -> to be able to identify whether the area of the elliptic segment to be measured is above or below the line between ip1 and ip2 
				//-> the leftmost (in x-axis) is the first one to ensure anticlockwise rotation
				//if x-value of both pnts is equal (=> have line x=a), check the y-value st ip1.y < ip2.y
				//     to ensure an anti-clockwise order for the int pnts and thus ensure that it corresponds appropriately to the
				//     selection of the elliptic segment which is above or below the line
				arcEndPnts[0] = ip1;
				arcEndPnts[1] = ip2;
				if (arcEndPnts[0].x == arcEndPnts[1].x){
					if (arcEndPnts[0].y > arcEndPnts[1].y){
						arcEndPnts[0] = ip2;
						arcEndPnts[1] = ip1;
					}
				} else if (arcEndPnts[0].x > arcEndPnts[1].x){
						arcEndPnts[0] = ip2;
						arcEndPnts[1] = ip1;
				}

				// find polar coord angle for arcEndPnts[1], pnt on ellipse a, pnt on ellipse b, from arcEndPnts[0]
				ang_arcEndPnt2 = GeometricOps.getPolarCoordAngForPnt (arcEndPnts[1], arcEndPnts[0]);
				ang_ipClostestToLine = GeometricOps.getPolarCoordAngForPnt (ipClosestToLine, arcEndPnts[0]);

				
				// If there is a point on another ellipse which is on ipsPerpLine and closer to the triangle than the current ellipse 
				// ... then since the point is on ipsPerpLine then polar angle of point from ipsMidPnt should be the same as that of the point on the current ellipse
				// ... If this is the case then that means that there is another ellipse which is closer to the triangle and thus this point on the current ellipse 
				// ... is not correct even though it is closer and hence would need to consider the other point
				if (distCurrClosest_otherE != null){
					if (isZero(GeometricOps.getPolarCoordAngForPnt (ipClosestToLine_otherE, ipsMidPnt)-GeometricOps.getPolarCoordAngForPnt (ipClosestToLine, ipsMidPnt), 1e-6)){
						ipClosestToLine = ipsWithPerpLine.get(Math.abs(ipClosestToLine_index-1)); //Math.abs(ipClosestToLine_index-1) because if index=1 then we need 0 (i.e. 1-1) and if index=0 we need 1 |(0-1)| 
						ang_ipClostestToLine = GeometricOps.getPolarCoordAngForPnt (ipClosestToLine, arcEndPnts[0]);
					}
				}				
				
				
				if (((ipClosestToLine.x-arcEndPnts[0].x) > 0) && ((arcEndPnts[1].x-arcEndPnts[0].x) > 0) && 
					((ipClosestToLine.y-arcEndPnts[0].y) > 0) && ((arcEndPnts[1].y-arcEndPnts[0].y) < 0)){
					//swap round the current arcEndPnts 
					currArcEndPnt0 = arcEndPnts[0];
					currArcEndPnt1 = arcEndPnts[1];
					arcEndPnts[0] = currArcEndPnt1;
					arcEndPnts[1] = currArcEndPnt0; 
					
				} else if (((ipClosestToLine.x-arcEndPnts[0].x) > 0) && ((arcEndPnts[1].x-arcEndPnts[0].x) > 0) &&
						   ((ipClosestToLine.y-arcEndPnts[0].y) < 0) && ((arcEndPnts[1].y-arcEndPnts[0].y) > 0)){
					//leave points as is
					
				} else if ((ang_ipClostestToLine > ang_arcEndPnt2) && ((ang_ipClostestToLine-ang_arcEndPnt2)<Math.toRadians(180))){		
					//swap round the current arcEndPnts 
					currArcEndPnt0 = arcEndPnts[0];
					currArcEndPnt1 = arcEndPnts[1];
					arcEndPnts[0] = currArcEndPnt1;
					arcEndPnts[1] = currArcEndPnt0; 
				} //else if (ang_ipClostestToLine < ang_arcEndPnt2) leave the points as they are 
				

				// add the areas of all the elliptic segments and then at the end (after this iteration) add the area of the inner triangle
				areaUnderArc = eForArea.computeAreaUnderEllipticArc_ByInteg(arcEndPnts, dx); 
				
			
				arcEndPnts_rev[0] = arcEndPnts[1];
				arcEndPnts_rev[1] = arcEndPnts[0];
				areaUnderLine = 0;
				if (dx){
					lineLimitsForArea_x[0] = arcEndPnts_rev[0].x;
					lineLimitsForArea_x[1] = arcEndPnts_rev[1].x;
					areaUnderLine = ipsLine.definiteArea_IntegCartDx(lineLimitsForArea_x);
				} else {
					lineLimitsForArea_y[0] = arcEndPnts_rev[0].y;
					lineLimitsForArea_y[1] = arcEndPnts_rev[1].y;
					areaUnderLine = ipsLine.definiteArea_IntegCartDy(lineLimitsForArea_y);
				}
				
				areaCurrSegment = Math.abs(areaUnderArc + areaUnderLine);
			
				area += areaCurrSegment;			
			}
		}
		
		// find and add the area of the inner triangle
		area += triangle.computeArea();	
		
		DecimalFormat df = new DecimalFormat("##############################.########");
		try {
			area = df.parse(df.format(area)).doubleValue();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return area;
	}
	
	
	public void computeZoneAreas_ByInteg (boolean recomputeIntPnts){
		DecimalFormat df = new DecimalFormat("##############################.########");
	
		if (recomputeIntPnts ||
			(this.intPntsOfEllipses == null) ||
			(this.zoneAreasComputedSuccessfully == null)){
			recomputeIntPntsOfEllipses();
		}
		
		this.zoneAreasComputedSuccessfully = false;
		
		if (this.intPntsOfEllipses == null){return;}
		if (correctNoOfIntPnts() == false){return;}
		
		ArrayList<Point2D.Double> ipsForInnerZone = new ArrayList<Point2D.Double>(this.ellipses.size());
		ArrayList<ArrayList<Ellipse>> ellsForInnerZone = new ArrayList<ArrayList<Ellipse>>(this.ellipses.size()); 
		
		ArrayList<String> overlappingRegion2Ells_labels = new ArrayList<String>(this.ellipses.size());
		ArrayList<Double> overlappingRegion2Ells_areas = new ArrayList<Double>(this.ellipses.size()); 
		// the size of this should be nC2= n!/((n-2)!*r!) but since we are mostly dealing with venn-3, then we will only have 3 2-ellipse zones 
		// ...and still, this is the initial capacity and it will grow automatically if there is no more space and add is invoked 
		
		Iterator itr = this.intPntsOfEllipses.entrySet().iterator();
		Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>> intPntsForEs;
		ArrayList<Ellipse> curr_es;
		Ellipse e1;
		Ellipse e2;
		String overlappingRegionLabel;
		ArrayList<Point2D.Double> intpntsAsArrayList;
		Point2D.Double[] intpnts; 
		Ellipse e3 = new Ellipse("", 0, 0, 0, 0, 0);
		ArrayList<Ellipse> ellsForIP;
		Double overlappingRegionArea;
		double region_e1Area;
		double region_e2Area;
		String[] curr_esLabels_sorted = new String[2];
 		while (itr.hasNext()){
 			intPntsForEs = (Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>>)itr.next();
			//intersecting ellipses
			
			// if the ellipses are cloned then reference to the actual ellipse object which are changed by the optimizer are lost 
			// and thus, if use curr_es = intPntsForEs.getKey(); then the ellipse objects might not be fully updated
			// => best to use the intPntsForEs.getKey() just to get the label of the ellipse (to be on the safe side)
			// especially when rerun since refer back to a clone of the init diag
			
			curr_es = intPntsForEs.getKey(); //assume for every one have 2 ellipses
			
			e1 = null;
			e2 = null;
			for (Ellipse e : this.ellipses){
				if (e.label.equals(curr_es.get(0).label)){
					e1 = e;
				}
				if (e.label.equals(curr_es.get(1).label)){
					e2 = e;
				}
				if ((e1!=null) && (e2!=null)){break;}
			}
			
			overlappingRegionLabel = "";
			curr_esLabels_sorted[0] = e1.label;
			curr_esLabels_sorted[1] = e2.label;
			Arrays.sort(curr_esLabels_sorted);
			for (String eLabel : curr_esLabels_sorted){
				overlappingRegionLabel += eLabel;
			}
			intpntsAsArrayList = intPntsForEs.getValue();
			
			intpnts = new Point2D.Double[2]; 
			intpnts[0] = intpntsAsArrayList.get(0);
			intpnts[1] = intpntsAsArrayList.get(1);

			
			//decide which of these two intersecting point is within (not on the boundary) the third ellipse
			if (this.ellipses.size() == 3){
				//find the third ellipse first
				for (Ellipse e: this.ellipses){
					if ((!e.label.equals(e1.label)) && (!e.label.equals(e2.label))){
						e3 = e;
						break;
					}
				}
				
				
				if (intpnts[0]==null){
					int a =0;
				}
				if (e3.isPointInEllipse(intpnts[0], false,1e-2)){
					ipsForInnerZone.add(intpnts[0]);
					ellsForIP = new ArrayList<Ellipse>(2); 
					ellsForIP.add(e1);
					ellsForIP.add(e2);
					ellsForInnerZone.add(ellsForIP);
				} else if (e3.isPointInEllipse(intpnts[1], false,1e-2)){
					ipsForInnerZone.add(intpnts[1]);
					ellsForIP = new ArrayList<Ellipse>(2); 
					ellsForIP.add(e1);
					ellsForIP.add(e2);
					ellsForInnerZone.add(ellsForIP);
				}
			}		
			
			overlappingRegionArea = e1.getSharedZoneArea_ByInteg(e2, intpnts, 0); //assuming that handle 2 ellipses at a time   
			if(overlappingRegionArea==null){
				return;
			}

			if (this.ellipses.size() == 2){
				region_e1Area = e1.getArea() - overlappingRegionArea;
				region_e2Area = e2.getArea() - overlappingRegionArea;
			
				try {
					region_e1Area = df.parse(df.format(region_e1Area)).doubleValue();
					region_e2Area = df.parse(df.format(region_e2Area)).doubleValue();
				} catch (ParseException exception) {
					exception.printStackTrace();
				}
				
				if (region_e1Area < 0){
					System.out.println("EllipseDiagram.computeZoneAreas_ByInteg: 1-ellipse area "+e1.label+" < 0: "+region_e1Area); 
					System.out.println("Diagram ellipses:\n"+EllipseDiagramOps.getEllipseDetailsForFile(e1)+"\n"+EllipseDiagramOps.getEllipseDetailsForFile(e2)); 
					return;
				}
				if (region_e2Area < 0){
					System.out.println("EllipseDiagram.computeZoneAreas_ByInteg: 1-ellipse area "+e2.label+" < 0: "+region_e2Area); 
					System.out.println("Diagram ellipses:\n"+EllipseDiagramOps.getEllipseDetailsForFile(e1)+"\n"+EllipseDiagramOps.getEllipseDetailsForFile(e2)); 
					return;
				}
				if (overlappingRegionArea < 0){
					System.out.println("EllipseDiagram.computeZoneAreas_ByInteg: 2-ellipse area "+overlappingRegionLabel+" < 0: "+overlappingRegionArea); 
					System.out.println("Diagram ellipses:\n"+EllipseDiagramOps.getEllipseDetailsForFile(e1)+"\n"+EllipseDiagramOps.getEllipseDetailsForFile(e2)); 
					return;
				}
				
				this.zoneAreas.put(e1.label, region_e1Area);
				this.zoneAreas.put(e2.label, region_e2Area);
				
				this.zoneAreas.put(overlappingRegionLabel, overlappingRegionArea);
			} else {
				overlappingRegion2Ells_labels.add(overlappingRegionLabel);
				overlappingRegion2Ells_areas.add(overlappingRegionArea);
			}
		} 
	
		if (this.ellipses.size() > 2){
			if ((ipsForInnerZone.size() != 3) || (ellsForInnerZone.size() != 3)){
				return;
			} 
			
			if (this.ellipses.size() == 3){
				// find area of the innermost region
				double overlappingInnerRegionArea = computeAreaOf3EllipseZone_ByInteg(ipsForInnerZone, ellsForInnerZone, -1);  
				if (overlappingInnerRegionArea < 0){
					return;
				}
							
				// add the areas of 2-ellipse zones
				int index;
				double twoEllZoneArea;
				for (String label : overlappingRegion2Ells_labels){
					index = overlappingRegion2Ells_labels.indexOf(label);
					twoEllZoneArea = overlappingRegion2Ells_areas.get(index) - overlappingInnerRegionArea;
					try {
						twoEllZoneArea = df.parse(df.format(twoEllZoneArea)).doubleValue();
					} catch (ParseException exception) {
						exception.printStackTrace();
					}
					if (twoEllZoneArea < 0){
						return;
					}
					this.zoneAreas.put(label, twoEllZoneArea);
				}
				
				// add the area of 3-ellipse zone
				String[] label_sorted = {this.ellipses.get(0).label, this.ellipses.get(1).label, this.ellipses.get(2).label};
				Arrays.sort(label_sorted);
				String regionLabel = "";
				for (String label : label_sorted){
					regionLabel += label;
				}
				this.zoneAreas.put(regionLabel, overlappingInnerRegionArea);
				
				// add the areas of 1-ellipse zones
				double oneEllZoneArea;
				Iterator itrZoneAreas;
				Entry<String, Double> currZoneArea;
				for (Ellipse e : this.ellipses){
					oneEllZoneArea = e.getArea(); 
					itrZoneAreas = this.zoneAreas.entrySet().iterator();
					while (itrZoneAreas.hasNext()){
						currZoneArea = (Entry<String, Double>)itrZoneAreas.next();
						if (currZoneArea.getKey().contains(e.label) && !currZoneArea.getKey().equals(e.label) && (currZoneArea.getValue() != null)){
							oneEllZoneArea -= currZoneArea.getValue();
						}
					}
					try {
						oneEllZoneArea = df.parse(df.format(oneEllZoneArea)).doubleValue();
					} catch (ParseException exception) {
						exception.printStackTrace();
					}
					if (oneEllZoneArea < 0){
						return;
					}
					this.zoneAreas.put(e.label, oneEllZoneArea);
				}
			}
		}

		this.zoneAreas = Utilities.sortHashMapByKeys(this.zoneAreas);
		
		this.zoneAreasComputedSuccessfully = true;
	}

	
	
	// ... compute zone areas in terms of elliptic segments 
	
	//  ... in terms of elliptic segments 

	public double computeAreaOf3EllipseZone_BySeg(ArrayList<Point2D.Double> ips, ArrayList<ArrayList<Ellipse>> ellsForIps){
		double area =0;
		
		Double distCurrClosest;
		Double distCurrClosest_otherE = null;
		double dist;
		ArrayList<Point2D.Double> ipsWithPerpLine;
		ArrayList<Point2D.Double> ipsWithPerpLine_otherE = new ArrayList<Point2D.Double>();  
		Point2D.Double ipClosestToLine = new Point2D.Double();	
		int ipClosestToLine_index = 0;
		int pCnt=0;
		Point2D.Double ipClosestToLine_otherE = new Point2D.Double(); 

		
		int noOfPnts = ips.size();
		for (int i = 0; i < noOfPnts; i++){
			for (int j = i+1; j < noOfPnts; j++){
				Point2D.Double ip1 = ips.get(i);
				Point2D.Double ip2 = ips.get(j);
				
				ArrayList<Ellipse> ellsForIp1 = ellsForIps.get(i);
				ArrayList<Ellipse> ellsForIp2 = ellsForIps.get(j);
				
				//find the ellipse which should be used to find the area of the elliptic segment
				Ellipse eForArea = new Ellipse("",0,0,0,0,0);
				for (int c1 = 0; c1 < ellsForIp1.size(); c1++){
					for (int c2 = 0; c2 < ellsForIp2.size(); c2++){
						if (ellsForIp1.get(c1).label.equals(ellsForIp2.get(c2).label)){
							eForArea = ellsForIp1.get(c1);
							break;
						}
					}
					if (eForArea.label != ""){break;} //the ellipse is found and thus break
				}
				if (eForArea.label.equals("")){ //report error
				}
				
				
				//order the intersection points such that an anti-clockwise rotation is assumed and to this this use a similar manner and used to 2-ellips intersections
				
				// line between intersection points, its mid-point, line perpendicular to it
				MyLine ipsLine = new MyLine(ip1, ip2, true);
				Point2D.Double ipsMidPnt = new Point2D.Double ((ip1.x + ip2.x)/2, (ip1.y + ip2.y)/2);
				MyLine ipsPerpLine = new MyLine(ipsLine.getPerpLineGradient(), ipsLine.getPerpLineYIntercept(ipsMidPnt), true);

				// get intersection points of every ellipse with the line perpendicular to that passing through the 2 ellipses intersection points				
				//   choose the intersection point closest to the midpoint
				ipsWithPerpLine = eForArea.getIntPnts_WithLine(ipsPerpLine);
				ipClosestToLine = new Point2D.Double();
				distCurrClosest = null;
				for (Point2D.Double p : ipsWithPerpLine){
					dist = GeometricOps.distanceBetween2Points(p, ipsMidPnt);
					if ((distCurrClosest == null) || (dist < distCurrClosest)){
						distCurrClosest = dist;
						ipClosestToLine = p;
					}
				}
				
				
				// Check whether there is a point on another ellipse which is on ipsPerpLine and closer to the triangle than the current ellipse 
				// ... is used later on
				distCurrClosest_otherE = null;
				for (Ellipse e : this.ellipses){
					if (e.label.equals(eForArea.label)){continue;}
					
					ipsWithPerpLine_otherE = e.getIntPnts_WithLine(ipsPerpLine);
					for (Point2D.Double p : ipsWithPerpLine_otherE){
						dist = GeometricOps.distanceBetween2Points(p, ipsMidPnt);
						if (((distCurrClosest_otherE == null) || (dist < distCurrClosest_otherE)) && (dist < distCurrClosest)){
							distCurrClosest_otherE = dist;
							ipClosestToLine_otherE = p;
						}
					}
				}
				

				//order the intersection points -> to be able to identify whether the area of the elliptic segment to be measured is above or below the line between ip1 and ip2 
				//-> the leftmost (in x-axis) is the first one to ensure anticlockwise rotation
				//if x-value of both pnts is equal (=> have line x=a), check the y-value st ip1.y < ip2.y
				//     to ensure an anti-clockwise order for the int pnts and thus ensure that it corresponds appropriately to the
				//     selection of the elliptic segment which is above or below the line
				Point2D.Double[] arcEndPnts = new Point2D.Double[2];
				arcEndPnts[0] = ip1;
				arcEndPnts[1] = ip2;
				if (arcEndPnts[0].x == arcEndPnts[1].x){
					if (arcEndPnts[0].y > arcEndPnts[1].y){
						arcEndPnts[0] = ip2;
						arcEndPnts[1] = ip1;
					}
				} else if (arcEndPnts[0].x > arcEndPnts[1].x){
						arcEndPnts[0] = ip2;
						arcEndPnts[1] = ip1;
				}

				// find polar coord angle for arcEndPnts[1], pnt on ellipse a, pnt on ellipse b, from arcEndPnts[0]
				double ang_arcEndPnt2 = GeometricOps.getPolarCoordAngForPnt (arcEndPnts[1], arcEndPnts[0]);
				double ang_ipClostestToLine = GeometricOps.getPolarCoordAngForPnt (ipClosestToLine, arcEndPnts[0]);


				// If there is a point on another ellipse which is on ipsPerpLine and closer to the triangle than the current ellipse 
				// ... then since the point is on ipsPerpLine then polar angle of point from ipsMidPnt should be the same as that of the point on the current ellipse
				// ... If this is the case then that means that there is another ellipse which is closer to the triangle and thus this point on the current ellipse 
				// ... is not correct even though it is closer and hence would need to consider the other point
				if (distCurrClosest_otherE != null){
					if (isZero(GeometricOps.getPolarCoordAngForPnt (ipClosestToLine_otherE, ipsMidPnt)-GeometricOps.getPolarCoordAngForPnt (ipClosestToLine, ipsMidPnt),1e-6)){
						ipClosestToLine = ipsWithPerpLine.get(Math.abs(ipClosestToLine_index-1)); //Math.abs(ipClosestToLine_index-1) because if index=1 then we need 0 (i.e. 1-1) and if index=0 we need 1 |(0-1)| 
						ang_ipClostestToLine = GeometricOps.getPolarCoordAngForPnt (ipClosestToLine, arcEndPnts[0]);
					}
				}	
				
				
				if (((ipClosestToLine.x-arcEndPnts[0].x) > 0) && ((arcEndPnts[1].x-arcEndPnts[0].x) > 0) && 
					((ipClosestToLine.y-arcEndPnts[0].y) > 0) && ((arcEndPnts[1].y-arcEndPnts[0].y) < 0)){
					//swap round the current arcEndPnts 
					Point2D.Double currArcEndPnt0 = arcEndPnts[0];
					Point2D.Double currArcEndPnt1 = arcEndPnts[1];
					arcEndPnts[0] = currArcEndPnt1;
					arcEndPnts[1] = currArcEndPnt0; 
						
				} else if (((ipClosestToLine.x-arcEndPnts[0].x) > 0) && ((arcEndPnts[1].x-arcEndPnts[0].x) > 0) &&
					       ((ipClosestToLine.y-arcEndPnts[0].y) < 0) && ((arcEndPnts[1].y-arcEndPnts[0].y) > 0)){
					//leave points as is
						
				} else if ((ang_ipClostestToLine > ang_arcEndPnt2) && ((ang_ipClostestToLine-ang_arcEndPnt2)<Math.toRadians(180))){	
					//swap round the current arcEndPnts 
					Point2D.Double currArcEndPnt0 = arcEndPnts[0];
					Point2D.Double currArcEndPnt1 = arcEndPnts[1];
					arcEndPnts[0] = currArcEndPnt1;
					arcEndPnts[1] = currArcEndPnt0; 
				} //else if (ang_ipClostestToLine < ang_arcEndPnt2) leave the points as they are 
						

				// add area of this elliptical segment to the final area
				area += Math.abs(eForArea.computeAreaOfEllipticSegment(arcEndPnts));
			}
		}
		
		// find and add the area of the inner triangle
		area += GeometricOps.areaOfTriangle(ips.get(0), ips.get(1), ips.get(2));
		
		return area;
	}
	
	public void computeZoneAreas_BySeg (){
		
		this.zoneAreasComputedSuccessfully = false;  // -> to be added when method is modified for memory usage optimization
		
        if (this.intPntsOfEllipses == null){
			recomputeIntPntsOfEllipses();
		}
		if (this.intPntsOfEllipses == null){return;}
		if (correctNoOfIntPnts() == false){return;}
		
		ArrayList<Point2D.Double> ipsForInnerZone = new ArrayList<Point2D.Double>(this.ellipses.size());
		ArrayList<ArrayList<Ellipse>> ellsForInnerZone = new ArrayList<ArrayList<Ellipse>>(this.ellipses.size()); 
		
		ArrayList<String> overlappingRegion2Ells_labels = new ArrayList<String>(this.ellipses.size());
		ArrayList<Double> overlappingRegion2Ells_areas = new ArrayList<Double>(this.ellipses.size()); 
		
		Iterator itr = this.intPntsOfEllipses.entrySet().iterator();
		Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>> intPntsForEs;
		ArrayList<Ellipse> curr_es;
		Ellipse e1;
		Ellipse e2;
		String overlappingRegionLabel;
		ArrayList<Point2D.Double> intpntsAsArrayList;
		Point2D.Double[] intpnts;
		Ellipse e3 = new Ellipse("", 0, 0, 0, 0, 0);
		ArrayList<Ellipse> ellsForIP;
		double overlappingRegionArea;
		double region_e1Area;
		double region_e2Area;
		String[] curr_esLabels_sorted = new String[2];
		while (itr.hasNext()){
			intPntsForEs = (Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>>)itr.next();
			
			// if the ellipses are cloned then reference to the actual ellipse object which are changed by the optimizer are lost 
			// and thus, if use curr_es = intPntsForEs.getKey(); then the ellipse objects might not be fully updated
			// => best to use the intPntsForEs.getKey() just to get the label of the ellipse (to be on the safe side)
			// especially when rerun since refer back to a clone of the init diag
			
			curr_es = intPntsForEs.getKey(); //assume for every one have 2 ellipses
			e1 = null;
			e2 = null;
			for (Ellipse e : this.ellipses){
				if (e.label.equals(curr_es.get(0).label)){
					e1 = e;
				}
				if (e.label.equals(curr_es.get(1).label)){
					e2 = e;
				}
				if ((e1!=null) && (e2!=null)){break;}
			}
			
			overlappingRegionLabel = "";
		
			curr_esLabels_sorted[0] = e1.label;
			curr_esLabels_sorted[1] = e2.label;
			Arrays.sort(curr_esLabels_sorted);
			for (String eLabel : curr_esLabels_sorted){
				overlappingRegionLabel += eLabel;
			}
			intpntsAsArrayList = intPntsForEs.getValue();
			intpnts = new Point2D.Double[2]; //assume for every 2 intersecting ellipses have 2 intersection points
			intpnts[0] = intpntsAsArrayList.get(0);
			intpnts[1] = intpntsAsArrayList.get(1);
	
			
			//decide which of these two intersecting point is within (not on the boundary) the third ellipse
			if (this.ellipses.size() == 3){
				//find the third ellipse first
				for (Ellipse e: this.ellipses){
					if ((!e.label.equals(e1.label)) && (!e.label.equals(e2.label))){
						e3 = e;
						break;
					}
				}				
	
				if (intpnts[0]==null){
					int a =0;
				}
				if (e3.isPointInEllipse(intpnts[0], false, 1e-2)){	
				    ipsForInnerZone.add(intpnts[0]);
					ellsForIP = new ArrayList<Ellipse>(2);
					ellsForIP.add(e1);
					ellsForIP.add(e2);
					ellsForInnerZone.add(ellsForIP);
					
				} else if (e3.isPointInEllipse(intpnts[1], false, 1e-2)){
					ipsForInnerZone.add(intpnts[1]);
					ellsForIP = new ArrayList<Ellipse>(2);
					ellsForIP.add(e1);
					ellsForIP.add(e2);
					ellsForInnerZone.add(ellsForIP);
				}
			}
		
			overlappingRegionArea = e1.getSharedZoneArea_BySeg(e2, intpnts); //assuming that handle 2 ellipses at a time   

			if (this.ellipses.size() == 2){
				
				region_e1Area = e1.getArea() - overlappingRegionArea;
				region_e2Area = e2.getArea() - overlappingRegionArea;
				
				if (region_e1Area < 0){
					System.out.println("EllipseDiagram.computeZoneAreas_BySeg: 1-ellipse area "+e1.label+" < 0: "+region_e1Area); 
 				    System.out.println("Diagram ellipses:\n"+EllipseDiagramOps.getEllipseDetailsForFile(e1)+"\n"+EllipseDiagramOps.getEllipseDetailsForFile(e2));
					return;
				}
				if (region_e2Area < 0){
					System.out.println("EllipseDiagram.computeZoneAreas_BySeg: 1-ellipse area "+e2.label+" < 0: "+region_e2Area); 
					System.out.println("Diagram ellipses:\n"+EllipseDiagramOps.getEllipseDetailsForFile(e1)+"\n"+EllipseDiagramOps.getEllipseDetailsForFile(e2));
					return;
				}
				if (overlappingRegionArea < 0){
					System.out.println("EllipseDiagram.computeZoneAreas_BySeg: 2-ellipse area "+overlappingRegionLabel+" < 0: "+overlappingRegionArea); 
					System.out.println("Diagram ellipses:\n"+EllipseDiagramOps.getEllipseDetailsForFile(e1)+"\n"+EllipseDiagramOps.getEllipseDetailsForFile(e2));
					return;
				}
				this.zoneAreas.put(e1.label, region_e1Area);
				this.zoneAreas.put(e2.label, region_e2Area);
				
				this.zoneAreas.put(overlappingRegionLabel, overlappingRegionArea);

			} else {
				overlappingRegion2Ells_labels.add(overlappingRegionLabel);
				overlappingRegion2Ells_areas.add(overlappingRegionArea);
			}
		} 

		if (this.ellipses.size() == 2){
			if ((ipsForInnerZone.size() != 3) || (ellsForInnerZone.size() != 3)){
				return;
			}  
			
			if (this.ellipses.size() == 3){
				// find area of the innermost region
				double overlappingInnerRegionArea = computeAreaOf3EllipseZone_BySeg (ipsForInnerZone, ellsForInnerZone);  
				if (overlappingInnerRegionArea < 0){
					System.out.println("EllipseDiagram.computeZoneAreas_BySeg: 3-ellipse area abc < 0: "+overlappingInnerRegionArea);
					System.out.println("Diagram ellipses:\n"+EllipseDiagramOps.getEllipseDetailsForFile(this.ellipses.get(0))+"\n"+EllipseDiagramOps.getEllipseDetailsForFile(this.ellipses.get(1))+"\n"+EllipseDiagramOps.getEllipseDetailsForFile(this.ellipses.get(2)));
					return;
				}
							
				// add the areas of 2-ellipse zones
				int index;
				double twoEllZoneArea;
				for (String label : overlappingRegion2Ells_labels){
					index = overlappingRegion2Ells_labels.indexOf(label);
					twoEllZoneArea = overlappingRegion2Ells_areas.get(index) - overlappingInnerRegionArea;
					if (twoEllZoneArea < 0){
						System.out.println("EllipseDiagram.computeZoneAreas_BySeg: 2-ellipse area "+label+" < 0: "+label+"-abc="+twoEllZoneArea); 
						System.out.println("Diagram ellipses:\n"+EllipseDiagramOps.getEllipseDetailsForFile(this.ellipses.get(0))+"\n"+EllipseDiagramOps.getEllipseDetailsForFile(this.ellipses.get(1))+"\n"+EllipseDiagramOps.getEllipseDetailsForFile(this.ellipses.get(2)));
						return;
					}
					this.zoneAreas.put(label, twoEllZoneArea);
				}
				
				// add the area of 3-ellipse zone
				String[] label_sorted = {this.ellipses.get(0).label, this.ellipses.get(1).label, this.ellipses.get(2).label};
				Arrays.sort(label_sorted);
				String regionLabel = "";
				for (String label : label_sorted){
					regionLabel += label;
				}
				this.zoneAreas.put(regionLabel, overlappingInnerRegionArea);
				
				// add the areas of 1-ellipse zones
				double oneEllZoneArea;
				Iterator itrZoneAreas;
				Entry<String, Double> currZoneArea;
				for (Ellipse e : this.ellipses){
					oneEllZoneArea = e.getArea(); 
					itrZoneAreas = this.zoneAreas.entrySet().iterator();
					while (itrZoneAreas.hasNext()){
						currZoneArea = (Entry<String, Double>)itrZoneAreas.next();
						if (currZoneArea.getKey().contains(e.label) && !currZoneArea.getKey().equals(e.label) && (currZoneArea.getValue() != null)){
							oneEllZoneArea -= currZoneArea.getValue();
						}
					}
					if (oneEllZoneArea < 0){
						System.out.println("EllipseDiagram.computeZoneAreas_BySeg: 1-ellipse area "+e.label+" < 0: "+oneEllZoneArea); 
						System.out.println("Diagram ellipses:\n"+EllipseDiagramOps.getEllipseDetailsForFile(this.ellipses.get(0))+"\n"+EllipseDiagramOps.getEllipseDetailsForFile(this.ellipses.get(1))+"\n"+EllipseDiagramOps.getEllipseDetailsForFile(this.ellipses.get(2)));
						return;
					}
					this.zoneAreas.put(e.label, oneEllZoneArea);
				}
			}
		}
		
		this.zoneAreas = Utilities.sortHashMapByKeys(this.zoneAreas);
		
		this.zoneAreasComputedSuccessfully = true;  
	}

	
	// Compute zone areas 
	//  get zone areas in the diagram -> using any one of the above methods: polygons, integration or elliptic segments 
	
	public void computeZoneAreas (boolean recomputeIntPnts){
		HashMap<String, Double> areas = new HashMap<String, Double>();

		if (this.methodToComputeRegionAreas == UNDEFINED){
			this.methodToComputeRegionAreas = INTEGRATION;
		}
		
		if (this.methodToComputeRegionAreas != POLYGONS){
			Iterator itr = intPntsOfEllipses.entrySet().iterator();
			while (itr.hasNext()){
				Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>> ips_e = (Entry<ArrayList<Ellipse>, ArrayList<Point2D.Double>>)itr.next();
			}
		}
		
		if (this.methodToComputeRegionAreas == POLYGONS){
			computeZoneAreas_AsPolys ();
		} else if (this.methodToComputeRegionAreas == INTEGRATION){
			computeZoneAreas_ByInteg (recomputeIntPnts);
		} else if (this.methodToComputeRegionAreas == SEGMENTS){
			computeZoneAreas_BySeg();
		}
	}

	
	
	// Checks
	
	public Boolean isValid (){
		// invokes the static method
		return (isValid(this));
	}
	public Boolean isValidAndNoEmptyOrDisconnectedLikeZones (double polyAreaDiscardThreshold){
		// invokes the static method
		return (isValidAndNoEmptyOrDisconnectedLikeZones(this,polyAreaDiscardThreshold));
	}
	
	public Boolean isValid (boolean checkAlsoScaleToFitScreenVrs){
		// invokes the static method
		boolean diagValid = isValid(this); 
		
		if (checkAlsoScaleToFitScreenVrs && diagValid){	

			EllipseDiagram tempDiag = new EllipseDiagram(this.getEllipsesClone(),this.considerLessPrecisePossibleIntPnts);  
			tempDiag.scaleToFit(EllipseDiagramPanel.diagBBox);
			diagValid = isValid(tempDiag);
		}
			
		return diagValid; 
	}

	
	public boolean areZoneAreasOrderedAsInEstimate (HashMap<String, Double> zasEstimates){
		//Convert all nulls to 0 or else they will be missing in the sorted hash map
		Iterator itr = zasEstimates.entrySet().iterator();
		Entry<String, Double> zaEstimates;
		while (itr.hasNext()){
			zaEstimates = (Entry<String, Double>)itr.next();
			if (zaEstimates.getValue()==null){
				zaEstimates.setValue(0.0);
			}
		}
		
		//Sort the estimate zone areas in ascending order 
		HashMap<String, Double> zasEstimatesSorted = Utilities.sortHashMapByValue(zasEstimates);
		
		//Sort the actual zone areas in ascending order 
		HashMap<String, Double> zasActualSorted = Utilities.sortHashMapByValue(zoneAreas);
		
		Set<String> zasEstSortedKeys = zasEstimatesSorted.keySet();
		itr = zasActualSorted.entrySet().iterator();
		Entry<String, Double> zaActualSorted;
		for (String zaEstSortedKey : zasEstSortedKeys){
			zaActualSorted = (Entry<String, Double>)itr.next();
			if (!zaEstSortedKey.equals(zaActualSorted.getKey())){
				return false;
			}
		}
		return true;		
	}
	
	public boolean areZoneAreasCloseToEstimates (HashMap<String, Double> zasEstimates){
		HashMap<String, Double> zasAccuracy = compareZoneAreasAccuracyWith(zasEstimates);
		
		Double maxZaAccuracy = null;
		Iterator itr = zasAccuracy.entrySet().iterator();
		while (itr.hasNext()){
			Entry<String, Double> za = (Entry<String, Double>)itr.next();
			if ((maxZaAccuracy == null) || (maxZaAccuracy.doubleValue() < za.getValue())){
				maxZaAccuracy = za.getValue();
			}
		}
		
		return (maxZaAccuracy.doubleValue() <= 25);
	}
	
	
	
	// Metrics
	
	public String computeErrors (HashMap<String, Double> reqAreaSpecs, boolean addABCabserr){
		
		HashMap<String, Double> actualZoneAreas = this.getZoneAreas();
		
		double totReqDiagArea=0;
		for (String reqZoneAreaLabel : reqAreaSpecs.keySet()){
			totReqDiagArea += reqAreaSpecs.get(reqZoneAreaLabel);
		}
		
		double totCurrDiagArea=0;
		for (String currZoneAreaLabel : actualZoneAreas.keySet()){
			totCurrDiagArea += actualZoneAreas.get(currZoneAreaLabel);
		}
		
		
		double currRegArea_asInDiag;
		double reqRegArea_asInput;
		
		// abc error
		double reqABC = 0;
		double actualABC = 0;
		double abserrABC = 0;
		String extraStr = "";
		
		// error measures
		double currAbsError;
		Double maxAbsError;
		String maxAbsErrorZoneLabel;

		double currRelError;
		Double maxRelError;
		String maxRelErrorZoneLabel;
				
		double currAbsDiffBetweenReqActualAreaProportions;
		Double maxAbsDiffBetweenReqActualAreaProportions;
	    String maxAbsDiffBetweenReqActualAreaProportionsZoneLabel;
		
		double currAbsErrorOverReqTot;
		Double maxAbsErrorOverReqTot;
		String maxAbsErrorOverReqTotZoneLabel;
		
		double totAbsErrorForDiag = 0;
		double totRelErrorForDiag=0;
		double totAbsDiffBetweenReqActualAreaProportionsForDiag=0;
		double totAbsErrorOverReqTotForDiag=0;
		
		double avgAbsErrorForDiag=0;
		double avgRelErrorForDiag=0;
		double avgAbsDiffBetweenReqActualAreaProportionsForDiag=0;
		double avgAbsErrorOverReqTotForDiag=0;
		
		Iterator itrAreaspecZA = reqAreaSpecs.entrySet().iterator();
		Entry<String, Double> areaspecZA;
		String zoneareaName = "";
		double zonearea = 0.0;
		
		totAbsErrorForDiag=0;
		totRelErrorForDiag=0;
		totAbsDiffBetweenReqActualAreaProportionsForDiag=0;
		totAbsErrorOverReqTotForDiag=0;
		maxAbsError=null;
		maxRelError=null;
		maxAbsDiffBetweenReqActualAreaProportions=null;
		maxAbsErrorOverReqTot=null;
		maxAbsErrorZoneLabel="";
		maxRelErrorZoneLabel="";
		maxAbsDiffBetweenReqActualAreaProportionsZoneLabel="";
		maxAbsErrorOverReqTotZoneLabel="";
		
		while (itrAreaspecZA.hasNext()){
			
			areaspecZA = (Entry<String,Double>)itrAreaspecZA.next();
			zoneareaName = areaspecZA.getKey();
			zonearea = areaspecZA.getValue();
			
			currRegArea_asInDiag = actualZoneAreas.get(zoneareaName);
			reqRegArea_asInput = reqAreaSpecs.get(zoneareaName);
			
			if (addABCabserr && zoneareaName.equals("abc")){
				actualABC = currRegArea_asInDiag;
				reqABC = reqRegArea_asInput;
				abserrABC = Math.abs(actualABC-reqABC);
			}
		
			currAbsError = Math.abs(reqRegArea_asInput - currRegArea_asInDiag);
			currRelError = currAbsError / reqRegArea_asInput;
			currAbsDiffBetweenReqActualAreaProportions = Math.abs((currRegArea_asInDiag/totCurrDiagArea) - (reqRegArea_asInput/totReqDiagArea));
			currAbsErrorOverReqTot = currAbsError / totReqDiagArea;
			
			// computing total error
			totAbsErrorForDiag+=currAbsError;
			totRelErrorForDiag+=currRelError;
			totAbsDiffBetweenReqActualAreaProportionsForDiag+=currAbsDiffBetweenReqActualAreaProportions;
			totAbsErrorOverReqTotForDiag+=currAbsErrorOverReqTot;
			
			// finding max error
			if ((maxAbsError==null)||(maxAbsError<currAbsError)){
					maxAbsError = currAbsError;
					maxAbsErrorZoneLabel = zoneareaName;
			}
			if ((maxRelError==null)||(maxRelError<currRelError)){
					maxRelError = currRelError;
					maxRelErrorZoneLabel = zoneareaName;
			}
			if ((maxAbsDiffBetweenReqActualAreaProportions==null)||(maxAbsDiffBetweenReqActualAreaProportions<currAbsDiffBetweenReqActualAreaProportions)){
					maxAbsDiffBetweenReqActualAreaProportions = currAbsDiffBetweenReqActualAreaProportions;
					maxAbsDiffBetweenReqActualAreaProportionsZoneLabel = zoneareaName;
			}
			if ((maxAbsErrorOverReqTot==null)||(maxAbsErrorOverReqTot<currAbsErrorOverReqTot)){
					maxAbsErrorOverReqTot = currAbsErrorOverReqTot;
					maxAbsErrorOverReqTotZoneLabel = zoneareaName;
			}
		}
		// compute average error
		avgAbsErrorForDiag = totAbsErrorForDiag / 7;
		avgRelErrorForDiag = totRelErrorForDiag / 7;
		avgAbsDiffBetweenReqActualAreaProportionsForDiag = totAbsDiffBetweenReqActualAreaProportionsForDiag / 7;
		avgAbsErrorOverReqTotForDiag = totAbsErrorOverReqTotForDiag / 7;
		
		// abc error 
		extraStr = "";
		if (addABCabserr){
			extraStr = " | " + actualABC + " | " + reqABC + " | " + abserrABC;
		}
		
		return (maxAbsError + " | " + maxAbsErrorZoneLabel + " | " + avgAbsErrorForDiag + " | " + totAbsErrorForDiag + " | " + 
				maxRelError + " | " + maxRelErrorZoneLabel + " | " + avgRelErrorForDiag + " | " + totRelErrorForDiag + " | " + 
				maxAbsDiffBetweenReqActualAreaProportions + " | " + maxAbsDiffBetweenReqActualAreaProportionsZoneLabel + " | " + avgAbsDiffBetweenReqActualAreaProportionsForDiag + " | " + totAbsDiffBetweenReqActualAreaProportionsForDiag + " | " + 
				maxAbsErrorOverReqTot + " | " + maxAbsErrorOverReqTotZoneLabel + " | " + avgAbsErrorOverReqTotForDiag + " | " + totAbsErrorOverReqTotForDiag  + " | " +
				totCurrDiagArea + " | " + totReqDiagArea + extraStr);
		
	}

	
	public HashMap<String, Double> computeFitnessOfAllRegions (HashMap<String, Double> requiredAreaSpecs, FitnessMeasure fitnessMeasure){
		// static to be able to determine the fitness of a diagram even though a hill climber is not instantiated
		
		if (requiredAreaSpecs == null){return null;}
		
		HashMap<String, Double> fitnessOfAllRegions=new HashMap<String, Double>();
		HashMap<String, Double> currentZoneAreas=null;
		String[] zoneLabels = this.getZoneLabels();
		double sumOfRegionAreas_req=0;
		double sumOfRegionAreas_curr=0;
		double fitnessOfRegion=0;
		
		switch (fitnessMeasure) {
		
		case BIASED_FOR_SMALL_AREA_DIMENSIONLESS_NOTSQ: case BIASED_FOR_SMALL_AREA_DIMENSIONLESS_SQ:
			currentZoneAreas = this.getZoneAreas();
			for (String zl : zoneLabels){
				fitnessOfRegion = Math.abs(requiredAreaSpecs.get(zl) - currentZoneAreas.get(zl))/ currentZoneAreas.get(zl);
				if (fitnessMeasure == FitnessMeasure.BIASED_FOR_SMALL_AREA_DIMENSIONLESS_SQ){
					fitnessOfRegion = Math.pow(fitnessOfRegion, 2);
				}	
				fitnessOfAllRegions.put(zl, fitnessOfRegion);
			}
			break; 

		case BIASED_FOR_SMALL_AREA_NOTDIMENSIONLESS:
		    currentZoneAreas = this.getZoneAreas();
		    for (String zl : zoneLabels){
			    fitnessOfRegion = Math.pow(requiredAreaSpecs.get(zl) - currentZoneAreas.get(zl),2) / Math.pow(currentZoneAreas.get(zl), 1); //(Math.abs(Math.pow(currentZoneAreas.get(zl),1)));  // not dimension-less
				fitnessOfAllRegions.put(zl, fitnessOfRegion);
			}
			break; 

		case STRESS:
			for (String zl : zoneLabels){
				fitnessOfAllRegions.put(zl, new Double(0));
			}
			break;
			
		case CHOWRODGERS_IDEAL_MAXREGFIT: case CHOWRODGERS_IDEAL_MEANREGFIT: case CHOWRODGERS_IDEAL_SUMREGFIT :
			currentZoneAreas = this.getZoneAreas();
		    sumOfRegionAreas_curr = 0;
		    sumOfRegionAreas_req = 0;
		    for (String zl : zoneLabels){
		    	sumOfRegionAreas_curr += currentZoneAreas.get(zl);
		    	sumOfRegionAreas_req += requiredAreaSpecs.get(zl);
		    }
		    for (String zl : zoneLabels){
		    	fitnessOfRegion = Math.pow ((requiredAreaSpecs.get(zl)/sumOfRegionAreas_req) - (currentZoneAreas.get(zl)/sumOfRegionAreas_curr), 2);
		    	fitnessOfAllRegions.put(zl, fitnessOfRegion);
			}
			break;
			
		case ABSERROR_OVER_REQTOTAREA_MAXREGFIT: case ABSERROR_OVER_REQTOTAREA_MEANREGFIT: case ABSERROR_OVER_REQTOTAREA_SUMREGFIT:
			currentZoneAreas = this.getZoneAreas();
		    sumOfRegionAreas_req = 0;
		    for (String zl : zoneLabels){
		    	sumOfRegionAreas_req += requiredAreaSpecs.get(zl);
		    }
			for (String zl : zoneLabels){
				fitnessOfRegion = Math.abs(currentZoneAreas.get(zl) - requiredAreaSpecs.get(zl)) / sumOfRegionAreas_req;
				fitnessOfAllRegions.put(zl, fitnessOfRegion);
			}
			break;
			
		case ABSERROR_OVER_REQTOTAREA_MAXREGFIT_SCALEACTTOREQ: case ABSERROR_OVER_REQTOTAREA_MEANREGFIT_SCALEACTTOREQ: case ABSERROR_OVER_REQTOTAREA_SUMREGFIT_SCALEACTTOREQ:
			currentZoneAreas = this.getZoneAreas();
			sumOfRegionAreas_curr = 0;
		    sumOfRegionAreas_req = 0;
		    for (String zl : zoneLabels){
		    	sumOfRegionAreas_curr += currentZoneAreas.get(zl);
		    	sumOfRegionAreas_req += requiredAreaSpecs.get(zl);
		    }
			for (String zl : zoneLabels){
				fitnessOfRegion = Math.abs((currentZoneAreas.get(zl)*(sumOfRegionAreas_req/sumOfRegionAreas_curr)) - requiredAreaSpecs.get(zl)) / sumOfRegionAreas_req;
		    	fitnessOfAllRegions.put(zl, fitnessOfRegion);
			}
			break;
			
		case RELATIVE_ERROR_MAXREGFIT: case RELATIVE_ERROR_MEANREGFIT: case RELATIVE_ERROR_SUMREGFIT:
			currentZoneAreas = this.getZoneAreas();
			for (String zl : zoneLabels){
		    	fitnessOfRegion = Math.abs(currentZoneAreas.get(zl) - requiredAreaSpecs.get(zl)) / requiredAreaSpecs.get(zl);
				fitnessOfAllRegions.put(zl, fitnessOfRegion);
			}
			break;
			
			
		case RELATIVE_ERROR_MAXREGFIT_SCALEACTTOREQ: case RELATIVE_ERROR_MEANREGFIT_SCALEACTTOREQ: case RELATIVE_ERROR_SUMREGFIT_SCALEACTTOREQ:
			currentZoneAreas = this.getZoneAreas();
		    sumOfRegionAreas_curr = 0;
		    sumOfRegionAreas_req = 0;
		    for (String zl : zoneLabels){
		    	sumOfRegionAreas_curr += currentZoneAreas.get(zl);
		    	sumOfRegionAreas_req += requiredAreaSpecs.get(zl);
		    }
			for (String zl : zoneLabels){
		    	fitnessOfRegion = Math.abs((currentZoneAreas.get(zl)*(sumOfRegionAreas_req/sumOfRegionAreas_curr)) - requiredAreaSpecs.get(zl)) / requiredAreaSpecs.get(zl);
		    	fitnessOfAllRegions.put(zl, fitnessOfRegion);
			}
			break;
			
		case RELATIVE_ERROR_NORMALIZED_MAXREGFIT: case RELATIVE_ERROR_NORMALIZED_MEANREGFIT: case RELATIVE_ERROR_NORMALIZED_SUMREGFIT:
			currentZoneAreas = this.getZoneAreas();
		    sumOfRegionAreas_curr = 0;
		    sumOfRegionAreas_req = 0;
		    for (String zl : zoneLabels){
		    	sumOfRegionAreas_curr += currentZoneAreas.get(zl);
		    	sumOfRegionAreas_req += requiredAreaSpecs.get(zl);
		    }
		    for (String zl : zoneLabels){
		    	fitnessOfRegion = Math.abs((currentZoneAreas.get(zl)/sumOfRegionAreas_curr) - (requiredAreaSpecs.get(zl)/sumOfRegionAreas_req)) / (requiredAreaSpecs.get(zl)/sumOfRegionAreas_req);
		    	fitnessOfAllRegions.put(zl, fitnessOfRegion);
		    }
			break;	
		
		case LOG_MAXREGFIT: case LOG_MEANREGFIT: case LOG_SUMREGFIT:
		    currentZoneAreas = this.getZoneAreas();
		    for (String zl : zoneLabels){
		    	fitnessOfRegion = Math.abs(Math.log10(currentZoneAreas.get(zl) / requiredAreaSpecs.get(zl)));  //abs log
		    	fitnessOfAllRegions.put(zl, fitnessOfRegion);
			}
			break;
			
		case LOG_NORMALIZED_MAXREGFIT: case LOG_NORMALIZED_MEANREGFIT: case LOG_NORMALIZED_SUMREGFIT:
			currentZoneAreas = this.getZoneAreas();
		    sumOfRegionAreas_curr = 0;
		    sumOfRegionAreas_req = 0;
		    for (String zl : zoneLabels){
		    	sumOfRegionAreas_curr += currentZoneAreas.get(zl);
		    	sumOfRegionAreas_req += requiredAreaSpecs.get(zl);
		    }
		    for (String zl : zoneLabels){
		    	fitnessOfRegion = Math.abs(Math.log10((currentZoneAreas.get(zl)/sumOfRegionAreas_curr) / (requiredAreaSpecs.get(zl)/sumOfRegionAreas_req)));
		    	fitnessOfAllRegions.put(zl, fitnessOfRegion);
		    }
			break;	
		
		case ASPECT_RATIO_MAXREGFIT: case ASPECT_RATIO_MEANREGFIT: case ASPECT_RATIO_SUMREGFIT:
		    currentZoneAreas = this.getZoneAreas();
		    for (String zl : zoneLabels){
		    	fitnessOfRegion = currentZoneAreas.get(zl) / requiredAreaSpecs.get(zl);
		    	fitnessOfAllRegions.put(zl, fitnessOfRegion);
			}
			break;
			
		case ASPECT_RATIO_NORMALIZED_MAXREGFIT: case ASPECT_RATIO_NORMALIZED_MEANREGFIT: case ASPECT_RATIO_NORMALIZED_SUMREGFIT:
			currentZoneAreas = this.getZoneAreas();
		    sumOfRegionAreas_curr = 0;
		    sumOfRegionAreas_req = 0;
		    for (String zl : zoneLabels){
		    	sumOfRegionAreas_curr += currentZoneAreas.get(zl);
		    	sumOfRegionAreas_req += requiredAreaSpecs.get(zl);
		    }
		    for (String zl : zoneLabels){
		    	fitnessOfRegion = (currentZoneAreas.get(zl)/sumOfRegionAreas_curr) / (requiredAreaSpecs.get(zl)/sumOfRegionAreas_req);
		    	fitnessOfAllRegions.put(zl, fitnessOfRegion);
		    }
			break;	
		
		default:
			fitnessOfAllRegions = null;
			break;
		}
		
		return fitnessOfAllRegions;
	}


	public double computeFitnessOfDiagram(HashMap<String, Double> requiredAreaSpecs, FitnessMeasure fitnessMeasure){
		return computeFitnessOfDiagram(requiredAreaSpecs, computeFitnessOfAllRegions(requiredAreaSpecs, fitnessMeasure), fitnessMeasure); 
	}

	public Double computeFitnessOfDiagram(HashMap<String, Double> requiredAreaSpecs, HashMap<String, Double> fitnessOfAllRegions, FitnessMeasure fitnessMeasure){
		
		double fitnessValue = 0;
		
		
		switch (fitnessMeasure) {

		case STRESS:
			fitnessValue = this.computeWilkinsonStressMeasure(requiredAreaSpecs);
			break;
			
				
		case BIASED_FOR_SMALL_AREA_DIMENSIONLESS_NOTSQ: case BIASED_FOR_SMALL_AREA_DIMENSIONLESS_SQ: case BIASED_FOR_SMALL_AREA_NOTDIMENSIONLESS: case CHOWRODGERS_IDEAL_MEANREGFIT: 
		case ABSERROR_OVER_REQTOTAREA_MEANREGFIT: case ABSERROR_OVER_REQTOTAREA_MEANREGFIT_SCALEACTTOREQ: case RELATIVE_ERROR_MEANREGFIT: case RELATIVE_ERROR_MEANREGFIT_SCALEACTTOREQ: case RELATIVE_ERROR_NORMALIZED_MEANREGFIT: case LOG_MEANREGFIT: case LOG_NORMALIZED_MEANREGFIT: case ASPECT_RATIO_MEANREGFIT: case ASPECT_RATIO_NORMALIZED_MEANREGFIT:
	    	
			if (fitnessOfAllRegions == null){
				System.out.println("EllipseDiagram.computeFitness: hashmap with the fitness of all regions is null");
				return null;
			}
			if (zoneLabels.length != fitnessOfAllRegions.size()){
				System.out.println("EllipseDiagram.computeFitness: the fitness of " + fitnessOfAllRegions.size() + " regions has been computed, but " + zoneLabels.length + " regions were expected according to the number of zone labels");
				return null;
			}
			fitnessValue=0;
			for (String zl : zoneLabels){
				fitnessValue += fitnessOfAllRegions.get(zl);
			}
			fitnessValue = fitnessValue / zoneLabels.length;
			break;
			
			
		case CHOWRODGERS_IDEAL_MAXREGFIT: case ABSERROR_OVER_REQTOTAREA_MAXREGFIT:  case ABSERROR_OVER_REQTOTAREA_MAXREGFIT_SCALEACTTOREQ: 
		case RELATIVE_ERROR_MAXREGFIT: case RELATIVE_ERROR_MAXREGFIT_SCALEACTTOREQ: case RELATIVE_ERROR_NORMALIZED_MAXREGFIT: case LOG_MAXREGFIT: case LOG_NORMALIZED_MAXREGFIT: case ASPECT_RATIO_MAXREGFIT: case ASPECT_RATIO_NORMALIZED_MAXREGFIT:
			
			if (fitnessOfAllRegions == null){
				System.out.println("EllipseDiagram.computeFitness: hashmap with the fitness of all regions is null");
				return null;
			}
			if (zoneLabels.length != fitnessOfAllRegions.size()){
				System.out.println("EllipseDiagram.computeFitness: the fitness of " + fitnessOfAllRegions.size() + " regions has been computed, but " + zoneLabels.length + " regions were expected according to the number of zone labels");
				return null;
			}
			
			Double maxFitness_value = null;
			
			for (String zl : zoneLabels){
				if (maxFitness_value == null){
					maxFitness_value = fitnessOfAllRegions.get(zl);
				} else if (Math.max(maxFitness_value.doubleValue(), fitnessOfAllRegions.get(zl).doubleValue()) == fitnessOfAllRegions.get(zl).doubleValue()) {
					maxFitness_value = fitnessOfAllRegions.get(zl).doubleValue();
				}
			}
			fitnessValue = maxFitness_value;
			break;
		
			
		case CHOWRODGERS_IDEAL_SUMREGFIT: case ABSERROR_OVER_REQTOTAREA_SUMREGFIT: case ABSERROR_OVER_REQTOTAREA_SUMREGFIT_SCALEACTTOREQ:
		case RELATIVE_ERROR_SUMREGFIT: case RELATIVE_ERROR_SUMREGFIT_SCALEACTTOREQ: case RELATIVE_ERROR_NORMALIZED_SUMREGFIT: case LOG_SUMREGFIT: case LOG_NORMALIZED_SUMREGFIT: case ASPECT_RATIO_SUMREGFIT: case ASPECT_RATIO_NORMALIZED_SUMREGFIT:

			
			if (fitnessOfAllRegions == null){
				System.out.println("EllipseDiagram.computeFitness: hashmap with the fitness of all regions is null");
				return null;
			}
			if (zoneLabels.length != fitnessOfAllRegions.size()){
				System.out.println("EllipseDiagram.computeFitness: the fitness of " + fitnessOfAllRegions.size() + " regions has been computed, but " + zoneLabels.length + " regions were expected according to the number of zone labels");
				return null;
			}
			fitnessValue=0;
			for (String zl : zoneLabels){
				fitnessValue += fitnessOfAllRegions.get(zl);
			}
			break;
			

		default:
			return null;
		}
		
		return fitnessValue;		
	}
	

	
	public HashMap<String, Double> compareZoneAreasAccuracyWith (HashMap<String, Double> zasToCompareWith){
		HashMap<String, Double> zoneAreaAccuracy = new HashMap<String, Double>();
	
		Iterator itr = zoneAreas.entrySet().iterator();
		double zaAccuracy = 0; 
		while (itr.hasNext()){
			Entry<String, Double> za = (Entry<String, Double>)itr.next();
			zaAccuracy = (Math.abs( ((zasToCompareWith.get(za.getKey())==null)?0:zasToCompareWith.get(za.getKey())) - za.getValue())); /// za.getValue()) * 100;
			zoneAreaAccuracy.put(za.getKey(), zaAccuracy);
		}
		
		return zoneAreaAccuracy;
	}
	
	
	// ... transform and scale to fit
	
	public Rectangle2D.Double getBoundingBoxAsRect (boolean coordSys_originMid_NOT_originTopLeft){ 
		Point2D.Double[] leftBottom_rightTopPoints = getBoundingBox(coordSys_originMid_NOT_originTopLeft);
		double width = Math.abs (leftBottom_rightTopPoints[0].x - leftBottom_rightTopPoints[1].x);
		double height = Math.abs (leftBottom_rightTopPoints[0].y - leftBottom_rightTopPoints[1].y);
		
		return new Rectangle2D.Double(leftBottom_rightTopPoints[0].x, leftBottom_rightTopPoints[1].y, width, height);
	}
	public Point2D.Double[] getBoundingBox(boolean coordSys_originMid_NOT_originTopLeft){ 
		// work out the bounding box using the default coordinate system that is y incr down and then if the opposite required, change it before returning

	    Rectangle e0Box = ellipses.get(0).getBoundingBox(false);
	    
	    double leftmost_x = e0Box.x;
	    double topmost_y   = e0Box.y;
	    double rightmost_x   = e0Box.x + e0Box.width;
	    double bottommost_y = e0Box.y + e0Box.height;

	    Rectangle eIBox;
	    double currEll_left_x;
	    double currEll_top_y;
	    double currEll_right_x;
	    double currEll_bottom_y;
	    
	    for (int i = 1; i < ellipses.size(); i++) {
	    	eIBox = ellipses.get(i).getBoundingBox(false);
	    	
	    	currEll_left_x = eIBox.x;
	    	currEll_top_y   = eIBox.y;
	    	currEll_right_x   = eIBox.x + eIBox.width;
	    	currEll_bottom_y = eIBox.y + eIBox.height;
	    	
		    leftmost_x = (currEll_left_x < leftmost_x) ? currEll_left_x : leftmost_x;
		    topmost_y   = (currEll_top_y < topmost_y)   ? currEll_top_y : topmost_y;
		    rightmost_x   = (currEll_right_x > rightmost_x)   ? currEll_right_x : rightmost_x;
		    bottommost_y = (currEll_bottom_y > bottommost_y) ? currEll_bottom_y : bottommost_y;
	    }
		
	    Point2D.Double leftBottomPnt_originTopLeft = new Point2D.Double (leftmost_x, bottommost_y);
	    Point2D.Double rightTopPnt_originTopLeft = new Point2D.Double (rightmost_x, topmost_y);
	    if (!coordSys_originMid_NOT_originTopLeft){
	    	return new Point2D.Double[] {leftBottomPnt_originTopLeft, rightTopPnt_originTopLeft};
	    } else {
	    	return new Point2D.Double[] {Utilities.changeCoorSys_originTopLeft_to_originMid(leftBottomPnt_originTopLeft, EllipseDiagramPanel.centreOfSystem), 
	    			                     Utilities.changeCoorSys_originTopLeft_to_originMid(rightTopPnt_originTopLeft, EllipseDiagramPanel.centreOfSystem)};
	    }
	    	
	}
	
	
	
	public static double computeScaleToFitFactor (Rectangle2D bbox, HashMap<String,Double> zas, int noOfElls){
		
		double availableArea = bbox.getWidth() * bbox.getHeight();
		availableArea = availableArea * 0.7;
		
		double totalDiagArea =0;
		String[] zLs = EllipseDiagramOps.getZoneLabels(noOfElls, false);
		for (String zL : zLs){
			totalDiagArea += zas.get(zL);
		}		
			
		return (availableArea / totalDiagArea);		
	}
	
	public static HashMap<String,Double> scaleZAsToFit (Rectangle2D bbox, HashMap<String,Double> zas, int noOfElls){
		
		double scaleToFitFactor = computeScaleToFitFactor(bbox, zas, noOfElls);

		String[] zLs = EllipseDiagramOps.getZoneLabels(noOfElls, false);
		for (String zL : zLs){
			zas.put(zL, scaleToFitFactor * zas.get(zL));
		}		
			
		return zas;		
	}
		
	public void scaleToFit(Rectangle2D bbox) {
		scaleToFit(bbox,1);
	}
	public void scaleToFit(Rectangle2D bbox, double scalingFactor) {
		
	    // Compute the bounding box of the ellipses
		Point2D.Double[] thisDiagBBox = this.getBoundingBox(true);
		
	    double minx = thisDiagBBox[0].x;
	    double miny = thisDiagBBox[0].y;
	    double maxx = thisDiagBBox[1].x;
	    double maxy = thisDiagBBox[1].y;
	    
	    // Stretch the bounding box of the ellipses so that its dimensions match the specified bounding box
	    double scalex = (bbox.getWidth()-50)/(maxx-minx);
	    double scaley = (bbox.getHeight()-50)/(maxy-miny);
	    
	    // Make the scale uniform so that the specified bounding box is not exceeded
	    double scale = scalex < scaley ? scalex : scaley;
	    scale *= scalingFactor;

	    // Translate the diagram such that the centre of its bounding box is equal to the origin (0,0) (the centre of the system) 
	    double deltax = - ((minx + maxx) * scale) /2;
	    double deltay = - ((miny + maxy) * scale) /2;
	    
	    // Scale each ellipse and translate its centre
	    for (int i = 0; i < ellipses.size(); i++) {
	    	ellipses.get(i).a *= scale;
	    	ellipses.get(i).b *= scale;
	    	
	    	ellipses.get(i).xc *= scale;
	    	ellipses.get(i).yc *= scale;
	    	ellipses.get(i).xc += deltax;
	    	ellipses.get(i).yc += deltay;
	    }
	}
	
	public void centreToPanel () {
		
	    // Compute the bounding box of the ellipses.
		Point2D.Double[] thisDiagBBox = this.getBoundingBox(true);
		
	    double minx = thisDiagBBox[0].x;
	    double miny = thisDiagBBox[0].y;
	    double maxx = thisDiagBBox[1].x;
	    double maxy = thisDiagBBox[1].y;
	    
	    // Translate the diagram such that the centre of its bounding box is equal to the origin (0,0) (the centre of the system) 
	    double deltax = - (minx + maxx) /2;
	    double deltay = - (miny + maxy) /2;
		    
	    // Scale each ellipse and translate its centre
	    for (int i = 0; i < ellipses.size(); i++) {
	    	ellipses.get(i).xc += deltax;
	    	ellipses.get(i).yc += deltay;
	    }
	}
	
	
	
	protected Double computeWilkinsonStressMeasure(HashMap<String, Double> requiredAreaSpecs){
		// based on the code used for the stress measure in venneuler in R by Leland Wilkinson 
		// 		http://cran.r-project.org/web/packages/venneuler
		
		
		if (requiredAreaSpecs == null){return null;}

		// calculate zone areas
		if (this.methodToComputeRegionAreas != POLYGONS){
			String errmsg_computeZA = "Cannot compute zone areas (EllipseDiagram.computeWilkinsonStressMeasure)";
			if (zoneAreasComputedSuccessfully == null){
				try{
					recomputeZoneAreas();
				} catch (Exception e){
					System.out.println("Error: " + errmsg_computeZA + ": " + e);
					return null;
				}
			}
		
			if (!zoneAreasComputedSuccessfully){return null;}
		}
		HashMap<String, Double> zoneAreas = getZoneAreas();
		
		
        double xx = 0;
        double xy = 0;
        double sst = 0;
        String[] zoneAreaLabels = getZoneLabels();
        
        double x = 0;
        double y = 0;
        for (String zal : zoneAreaLabels) {
        	y = zoneAreas.get(zal);
        	x = requiredAreaSpecs.get(zal);
            xy += (x * y);
            xx += Math.pow(x,2);
            sst += Math.pow(y,2);
        }
        double slope = xy / xx;       
		
        double sse = 0;
        double yhat = 0;
        x = 0; y = 0;
        wilkinson_yHats = new HashMap<String, Double>(); //Wilkinson uses this to calculate the residuals
        for (String zal : zoneAreaLabels) {
        	y = zoneAreas.get(zal);
        	x = requiredAreaSpecs.get(zal);
            yhat = x * slope;
            wilkinson_yHats.put(zal, yhat);
            sse += (y - yhat) * (y - yhat);
        }
        return (sse / sst);	
	}
	
	
	
	// Static Methods

	// ... defined the following two as static to allow external use with possibly an array of ellipses rather than an EllipseDiagram  
	public static ArrayList<ConcreteContour> getEllipsesAsContours (ArrayList<Ellipse> ellipses){
		ArrayList<ConcreteContour> es_Contours = new ArrayList<ConcreteContour>(ellipses.size());
		for (Ellipse e : ellipses){
			es_Contours.add(e.getAsConcreteContour());
		}
		return es_Contours;
	}
	

	public static Boolean isValid (ArrayList<Ellipse> ellipses, boolean considerLessPrecisePossibleIntPnts){
		return (isValid(new EllipseDiagram(ellipses, considerLessPrecisePossibleIntPnts)));
	}
	public static Boolean isValid (EllipseDiagram diag){
		// In this case a valid diagram is one whereby:
		//    1) all the ellipses intersect (like venn diagrams)
		//    2) none of the zones are disconnected or duplicate -> i.e. there is only one polygon to define the zone
		// This excludes the empty zone
		// This check is the same as the one used to generate random appropriate n-ellipse diagrams
	
		if (diag.zoneAreasComputedSuccessfully == null){
			try{
				diag.recomputeZoneAreas();
			} catch (Exception e){
				System.out.println("Diagram is not a Venn-3 diagram [Error occurred (in EllipseDiagram.isValid) when computing the area of the regions: "+e+"]");
				return false;
			}
		}
		if (!diag.zoneAreasComputedSuccessfully){ return false; }

				
		HashMap<String, Double> zoneAreas = diag.getZoneAreas();
		
		int actualNoOfZones = zoneAreas.size();
		int expectedNoOfZones = (int) Math.pow(2, diag.getEllipses().size()) - 1;

		if (actualNoOfZones != expectedNoOfZones) {return false;}
		
		return true;
	}
	public static Boolean isValidAndNoEmptyOrDisconnectedLikeZones (EllipseDiagram diag, double polyAreaDiscardThreshold){//ArrayList<Ellipse> ellipses){

		if (!isValid(diag)){return false;}
		
		
		if (TestingWithPolygons.doesDiagramContainEmptyLikeOrDisconnectedZones(diag, polyAreaDiscardThreshold)!=TestingWithPolygons.DiagramZoneCondition.NOTEMPTYLIKE_CONNECTED){
			return false;
		}

		return true;
	}

	public static boolean improvedFitness (double current, double previous, boolean includeEqTo, EllipseDiagram diag){
		return improvedFitness(current, previous, includeEqTo, null, null, diag);
	}
	public static boolean improvedFitness (double current, double previous, boolean includeEqTo, 
			                               HashMap<String, Double> zoneVarsCurrentDiag, HashMap<String, Double> zoneVarsBeforeChange, EllipseDiagram diag ){
		// this method defines what an improved fitness meant in relation to our fitness function 
		// set as static to be independent of any diagram 
		
		boolean improvesZonesWithHighVar = false;
				
		return (improvesZonesWithHighVar || (
											  ((current < previous))|| 
											  (includeEqTo && HillClimber.areEqual(current, previous)) ) );
	}

	public static boolean areEqual(double x, double y){
		return isZero(Math.abs(x-y)); 
	}
	public static boolean isZero(double x){
		return isZero(x, PRECISION_ISZERO);
	}
	public static boolean isZero(double x, double l){
		double EQN_EPS = l;
		return ((x > -EQN_EPS) && (x < EQN_EPS));
	}
	
}












