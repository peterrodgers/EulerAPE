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

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import elliptic.areaproptool.EllipseDiagram.FitnessMeasure;


/**
 * 
 * To instantiate, define and manage a hill climber used by eulerAPE
 *
 */


public class HillClimber {
	
	public static final int MULTIPLE_CHANGES_PER_ITER = 0;
	public static final int SINGLE_CHANGE_PER_ITER = 1;
	public static final String MULTIPLE_CHANGES_FILENAME_SUFFIX = "MC";
	public static final String SINGLE_CHANGE_FILENAME_SUFFIX = "SC";
	public static final String LOCKED_PARAMS_FILENAME_SUFFIX = "LP";
	public static final String UNLOCKED_PARAMS_FILENAME_SUFFIX = "ULP";

	public static final double DEFAULT_D = 10;
	public static final double DEFAULT_S = 5;
	public static final double DEFAULT_R = 60;
	
	public static final boolean DEFAULT_RESTRICT_TO_CIRLCES = false;
	
	public static final boolean DEFAULT_IMPROVEFITNESS_CHANGEIFEQUAL = false;
	public static final boolean DEFAULT_LOGRUNDETAILS = true;
	public static final double DEFAULT_MAXMINSFOR1RUN = 5; 
	
	public static final double PRECISION_ISZERO = 1e-6; 
	
	public static enum RunType {RunWithInitParamsOnly, RunWithIncParamsOnly, RunWithInitAndIncParams};

	public static double DEFAULT_INCPARAMSBY = 0.2; 
	public static double DEFAULT_MAX_TIMES_PARAMS_ARE_INC = 10; 
	
	
	
	// Data fields
	
	// ... constants to explore other possible properties for the ellipses 
	protected double d = DEFAULT_D;  // distance between the current centre and potential (neighbouring) centres  
	protected double s = DEFAULT_S;  // rate (scaling percentage) by which the semi-major and minor axes are altered
	protected double r = DEFAULT_R;  // degrees by which the ellipse is rotated clockwise and anti-clockwise
	protected boolean restrictToCircles = DEFAULT_RESTRICT_TO_CIRLCES; // decide whether use circles or ellipses for curves
	protected boolean improveFitness_changeIfEqual = DEFAULT_IMPROVEFITNESS_CHANGEIFEQUAL;
	protected boolean logRunDetails = false; //DEFAULT_LOGRUNDETAILS; 
	protected double maxMinsFor1Run = DEFAULT_MAXMINSFOR1RUN; //terminate the run after maxMinsFor1Run minutes even though it did not converge
	
	// ... diagram 
	protected EllipseDiagram diagram;
	protected HashMap<String, Double> requiredAreaSpecs;
	protected Double fitness;
	protected double fitness_try1;
	protected double fitness_bestretry=0;
	
	// ... panels that need to be updated to animate the running hill climber 
	protected EllipseDiagramPanel ellipseDiagramPanel;
	protected SwitchBoardPanel switchBoardPanel;
	
	// ... logfile to log hill climbing data 
	protected File logfileFile;
	
	// ... stopwatch to time optimizer
	protected StopWatch stopWatch = new StopWatch(); 
	protected StopWatch stopWatch_forCurrRun = new StopWatch(); 

	// ... log string -> used mainly for logging of intermediate data eg to record when the max area abs error is <= threshold of exactness 
	protected String logAbsErrStr; 
	protected String logRelErrStr; 
	protected String logDiffBetweenReqActualAreaProportionsStr;
	protected String logAbsErrOverReqTotStr;
	protected String logAllErrorMeasuresStr;
	protected String logStr; 
	protected String logAbsErrStr_bestretry; 
	protected String logRelErrStr_bestretry; 
	protected String logDiffBetweenReqActualAreaProportionsStr_bestretry;
	protected String logAbsErrOverReqTotStr_bestretry;
	protected String logStr_bestretry; 
	
	// ... log time -> better than allowing switchboard to get the time from the stopwatch itself because the time was not being recorded properly
	public String elapsedTimeString;
	public double elapsedTimeMilliSecs;
	public String elapsedTimeString_forCurrRun;
	public double elapsedTimeMilliSecs_forCurrRun;
	public int elapsedIterations_forCurrRun;
	public int elapsedIterations_includingRetries;
	
	public String elapsedTimeString_bestretry="";
	public double elapsedTimeMilliSecs_bestretry=0;
	public int elapsedIterations_bestretry=0;
	
	public String reasonForTermination_bestretry="";
	
	public double d_bestretry=0;
	public double s_bestretry=0;
	public double r_bestretry=0;
	
	public double bestretry_index=0;
	
	public EllipseDiagram diagram_bestRetry;
	
	// ... fitness measure
	protected FitnessMeasure fitnessMeasure = EllipseDiagram.DEFAULT_FITNESS_MEASURE; 

	// ... force to terminate by a number of iterations 
	protected int maxNoOfIterations;
	protected boolean forceToTerm = true;
    
    // ... try other increased parameters
    protected RunType runType = RunType.RunWithInitParamsOnly;
    protected double incParamsBy = DEFAULT_INCPARAMSBY;
    protected double maxTimesParamsAreInc = DEFAULT_MAX_TIMES_PARAMS_ARE_INC;
    
    protected int noOfTimesParamsAreInc; 
	
    
	// Constructor
	
	// ... when have a diagram made of circles 
	public HillClimber (double d, double s, EllipseDiagram diagram, HashMap<String, Double> requiredAreaSpecs, 
		      								EllipseDiagramPanel ellipseDiagramPanel, SwitchBoardPanel switchBoardPanel, 
		      								File logfileFile, FitnessMeasure fitnessMeasure, boolean forceToTerm, int maxNoOfIterations, RunType runType){
		init (d, s, 0, true, diagram, requiredAreaSpecs, ellipseDiagramPanel, switchBoardPanel, logfileFile, fitnessMeasure, forceToTerm, maxNoOfIterations, runType);
	}	
	
	// ... when have a diagram made of ellipses  
	public HillClimber (double d, double s, double r, EllipseDiagram diagram, HashMap<String, Double> requiredAreaSpecs, 
			 									      EllipseDiagramPanel ellipseDiagramPanel, SwitchBoardPanel switchBoardPanel, 
			 									      File logfileFile, FitnessMeasure fitnessMeasure, boolean forceToTerm, int maxNoOfIterations, RunType runType){
		init (d, s, r, false, diagram, requiredAreaSpecs, ellipseDiagramPanel, switchBoardPanel, logfileFile, fitnessMeasure, forceToTerm, maxNoOfIterations, runType);
	}
	
	// ... initiating an instance of the hill climber
	private void init (double d, double s, double r, boolean restrictToCircles, EllipseDiagram diagram, HashMap<String, Double> requiredAreaSpecs, 
		      		  														   EllipseDiagramPanel ellipseDiagramPanel, SwitchBoardPanel switchBoardPanel, 
		      																   File logfileFile, FitnessMeasure fitnessMeasure, boolean forceToTerm, int maxNoOfIterations, RunType runType){
		this.d = d;
		this.s = s;
		this.r = r;
		this.restrictToCircles = restrictToCircles;
		this.diagram = diagram;
		this.requiredAreaSpecs = requiredAreaSpecs;
		this.fitness = null;
		this.ellipseDiagramPanel = ellipseDiagramPanel;
		this.switchBoardPanel = switchBoardPanel;
		this.logfileFile = logfileFile;
		this.fitnessMeasure = fitnessMeasure;
		this.forceToTerm = forceToTerm;
		this.maxNoOfIterations = maxNoOfIterations;
		this.runType = runType;
	}
	
	
	// Properties -> getters
	public double getD() {
		return d;
	}
	public double getS() {
		return s;
	}
	public double getR() {
		return r;
	}
	public boolean getRestrictToCircles() {
		return restrictToCircles;
	}
	public EllipseDiagram getDiagram() {
		return diagram;
	}
	public EllipseDiagramPanel getEllipseDiagramPanel() {
		return ellipseDiagramPanel;
	}
	public SwitchBoardPanel getSwitchBoardPanel() {
		return switchBoardPanel;
	}
	
	
	
	// Methods 
	
	// ... potential (neighbouring) solutions 
	
	private ArrayList<Point2D.Double> computePotentialCentres (Ellipse e){
		
		if (d == 0) {return null;}  

		double[] potentialXs = {e.getXc()-d, e.getXc(), e.getXc()+d};
		double[] potentialYs = {e.getYc()-d, e.getYc(), e.getYc()+d};
		
		ArrayList<Point2D.Double> potentialCentres = new ArrayList<Point2D.Double>(8); //usually max 8 -> this is the initial capacity and grows automatically if more space is needed
		Point2D.Double centre;
		for (double x : potentialXs){
			for (double y : potentialYs){
				if (areEqual(x, e.getXc()) && areEqual(y, e.getYc())){continue;}
				centre = new Point2D.Double(x,y);
				if (!arrayListContains_Point2D(potentialCentres, centre)){
					potentialCentres.add(centre);
				}
			}
		}
		
		return potentialCentres;
	}
	
	private ArrayList<Double> computePotentialAxes (double currSemiAxis){
		
		if (s == 0) {return null;} 
	
		double scaleVal = (s/100)*currSemiAxis;
		double[] potentialAxes_unchecked = {currSemiAxis - scaleVal, currSemiAxis + scaleVal};
		
		ArrayList<Double> potentialAxes = new ArrayList<Double>(2); //usually max 2 -> this is the initial capacity and grows automatically if more space is needed
		for (double pa : potentialAxes_unchecked){
			if (!areEqual(pa, currSemiAxis) && !arrayListContains(potentialAxes, pa)){
				potentialAxes.add(pa);
			}
		}
		
		return potentialAxes; 
	}
	
	private ArrayList<Double> computePotentialRots (double currRot){
		
		if (r == 0) {return null;}
		
		double[] potentialRots_unchecked = {currRot - r, currRot + r};
		
		ArrayList<Double> potentialRots = new ArrayList<Double>(2);//usually max 2 -> this is the initial capacity and grows automatically if more space is needed
		for (double pr : potentialRots_unchecked){
			
			if (pr >360){pr = pr % 360;} // % returns the remainder eg: 1260 % 360 = 180
			if (pr < 0) {pr += 360;}     // to get rot between 0-360 degs and thus avoid -ves
			if (pr==360){pr =  0;}
			
			if (!areEqual(pr, currRot) && !arrayListContains(potentialRots, pr)){
				potentialRots.add(pr);
			}
		}
		
		return potentialRots; 
	}
	
	
	
	private ArrayList<Point2D.Double> computePotentialCentres (Ellipse e, double specific_d){
		
		if (specific_d == 0) {return null;}

		double[] potentialXs = {e.getXc()-specific_d, e.getXc(),e.getXc()+specific_d};
		double[] potentialYs = {e.getYc()-specific_d, e.getYc(),e.getYc()+specific_d};
		
		ArrayList<Point2D.Double> potentialCentres = new ArrayList<Point2D.Double>(8); //usually max 8 -> this is the initial capacity and grows automatically if more space is needed
		Point2D.Double centre;
		for (double x : potentialXs){
			for (double y : potentialYs){
				if (areEqual(x, e.getXc()) && areEqual(y, e.getYc())){continue;}
				centre = new Point2D.Double(x,y);
				if (!arrayListContains_Point2D(potentialCentres, centre)){
					potentialCentres.add(centre);
				}
			}
		}
		
		return potentialCentres;
	}
	
	private ArrayList<Double> computePotentialAxes (double currSemiAxis, double specific_s){
		
		if (specific_s == 0) {return null;} 
		if (specific_s > 100){specific_s=100;}
	
		double scaleVal = (specific_s/100)*currSemiAxis;
		double[] potentialAxes_unchecked = {currSemiAxis - scaleVal, currSemiAxis + scaleVal};
		
		ArrayList<Double> potentialAxes = new ArrayList<Double>(2); //usually max 2 -> this is the initial capacity and grows automatically if more space is needed
		for (double pa : potentialAxes_unchecked){
			if (!areEqual(pa, currSemiAxis) && !arrayListContains(potentialAxes, pa)){
				potentialAxes.add(pa);
			}
		}
		
		return potentialAxes; 
	}
	
	private ArrayList<Double> computePotentialRots (double currRot, double specific_r){
		
		if (specific_r == 0) {return null;}
		
		double[] potentialRots_unchecked = {currRot - specific_r, currRot + specific_r};
		
		ArrayList<Double> potentialRots = new ArrayList<Double>(2);//usually max 2 -> this is the initial capacity and grows automatically if more space is needed
		for (double pr : potentialRots_unchecked){
			
			if (pr >360){pr = pr % 360;} // % returns the remainder eg: 1260 % 360 = 180
			if (pr < 0) {pr += 360;}     // to get rot between 0-360 degs and thus avoid -ves
			if (pr==360){pr =  0;}
			
			if (!areEqual(pr, currRot) && !arrayListContains(potentialRots, pr)){
				potentialRots.add(pr);
			}
		}
		
		return potentialRots; 
	}
	
	private boolean propChangedForAllEllipses (boolean[] propChangeForEllipses){
		for (boolean pce: propChangeForEllipses){
			if (!pce){return false;}
		}
		return true;
	}
	
	private boolean propNotChangedForAnyEllipse (boolean[] propChangeForEllipses){
		for (boolean pce: propChangeForEllipses){
			if (pce){return false;}
		}
		return true;
	}
	
	
	
	
	// ... run hill climber -> multiple changes per iteration 
	public String run_multipleChangesPerIter(boolean lockHCparams, boolean animate, boolean updateStopWatchDisplay, 
			File savingIntermediateDiagImgFile_absErr, File savingIntermediateDiagFile_absErr, File savingIntermediateDiagImgFile_relErr, File savingIntermediateDiagFile_relErr, 
			double maxRegionAreaAbsErrThreshold, double maxRegionAreaRelErrThreshold, double maxRegionAreaAbsDiffBetweenReqActualProportionsThreshold, double maxRegionAreaAbsErrorOverReqTotThreshold){
		
		String reasonForTermination = "";
		
		
		if (restrictToCircles && !EllipseDiagramOps.isCircleDiagram(diagram)){
			System.out.println ("HillClimber.run_multipleChangesPerIter: cannot run hill climber -> it must restrict curves to circles but the initial diagram contains some ellipses");
			reasonForTermination = "Error: An initial diagram consisting of circles is expected!";
			return reasonForTermination;
		}
		
		
		boolean saveDetailsWhenReachMaxRegionAreaThreshold = true;
		boolean foundMaxAreaAbsErrorBelowThreshold = false;
		boolean foundMaxAreaRelErrorBelowThreshold = false;
		boolean foundMaxAreaDiffBetweenReqActualAreaProportionsThreshold = false;
		boolean foundMaxAreaAbsErrorOverReqTotAreaBelowThreshold = false;
		
		double currAbsError;
		Double maxAbsError;
		String maxAbsErrorZoneLabel;
		double fitnessAtMaxAbsError = 0;
		
		double currReqValue;
		double currRelError;
		Double maxRelError;
		String maxRelErrorZoneLabel;
		double fitnessAtMaxRelError = 0;
		
		double totCurrDiagArea = 0;
		double totReqDiagArea = 0;
		
		double totAbsErrorForDiag = 0;
		double totRelErrorForDiag=0;
		double totAbsDiffBetweenReqActualAreaProportionsForDiag=0;
		double totAbsErrorOverReqTotForDiag=0;
		
		double avgAbsErrorForDiag=0;
		double avgRelErrorForDiag=0;
		double avgAbsDiffBetweenReqActualAreaProportionsForDiag=0;
		double avgAbsErrorOverReqTotForDiag=0;
		
		double currAbsDiffBetweenReqActualAreaProportions;
		Double maxAbsDiffBetweenReqActualAreaProportions;
	    String maxAbsDiffBetweenReqActualAreaProportionsZoneLabel;
	    double fitnessAtMaxAbsDiffBetweenReqActualAreaProportions = 0;
		
		double currAbsErrorOverReqTot;
		Double maxAbsErrorOverReqTot;
		String maxAbsErrorOverReqTotZoneLabel;
		double fitnessAtMaxAbsErrorOverReqTot = 0;
		
		String errorsPerRegion="";
		

		ArrayList<Ellipse> diagEllipses = null;
		
		HashMap<String, Double> zoneVars = null;
		zoneVars=diagram.computeFitnessOfAllRegions(requiredAreaSpecs, fitnessMeasure);
		fitness = diagram.computeFitnessOfDiagram(requiredAreaSpecs, zoneVars, fitnessMeasure);

		
		boolean[][] specificPropChanged = new boolean[4][diagram.getEllipses().size()]; 
		boolean someChangeOccurred = false;
		boolean diagPropChanged = false;
		boolean noMorePossibleChanges = true;
		
		String startDateTime = Utilities.getCurrentDateTime();
		BufferedWriter bwLogger = (logRunDetails ? initLogger(startDateTime, MULTIPLE_CHANGES_PER_ITER, lockHCparams) : null);
		int loggerInitAlignSpaceCount = 0;
		
		
		
		// declare these vars here to avoid redeclaring them over and over again in the loop => reuse (to avoid memory issues)
		HashMap<String,Double> zoneVarsBeforeChange = null;
		double fitnessBeforeChange;
		HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>> intPntsOfEllipsesBeforeChange = null;
		HashMap<String, Double> zoneAreasBeforeChange = null;
		Boolean zoneAreasComputedSuccessfullyBeforeChange = null;
		
		Point2D.Double centreBeforeChange = new Point2D.Double();
		double aBeforeChange;
		double bBeforeChange;
		double rotBeforeChange;

		
		ArrayList<Point2D.Double> potentialCentres;
		ArrayList<Double> potentialAs;
		ArrayList<Double> potentialBs;
		ArrayList<Double> potentialRots;

		boolean foundSomePotentialCentres = false;
		boolean foundSomePotentialAs = false;
		boolean foundSomePotentialBs = false;
		boolean foundSomePotentialRots = false;
		
		boolean currDiagTryIsValid = false;
		
		HashMap<String, Double> zoneVarsCurrentDiag = null;
		double fitnessCurrentDiag;
		boolean keepChange = false;
		
		double dv=2;

		
		int eI;
		int sc; int se; //used for specificPropChanged flags
		
		EllipseDiagram initDiag = diagram.clone();
		
		// get pointer to ellipses
		diagEllipses = diagram.getEllipses();
		
		// init hashmaps storing ips and zone areas before change
		intPntsOfEllipsesBeforeChange = new HashMap<ArrayList<Ellipse>, ArrayList<Point2D.Double>>();
		Ellipse temp_e1; Ellipse temp_e2;
		int j; int k;
		ArrayList<Point2D.Double> ipsEllPair;
		ArrayList<Ellipse> ellPair;
		for (j=0; j<diagEllipses.size(); j++){
			for (k=j+1; k<diagEllipses.size(); k++){
			
				temp_e1 = diagEllipses.get(j);
				temp_e2 = diagEllipses.get(k);
				if (Utilities.convertCharToASCIIint(temp_e1.label.toLowerCase()) > Utilities.convertCharToASCIIint(temp_e2.label.toLowerCase())){
					temp_e1 = diagEllipses.get(k);
					temp_e2 = diagEllipses.get(j);
				}
				
				ipsEllPair = new ArrayList<Point2D.Double>(4);
				ipsEllPair.add(null);
				ipsEllPair.add(null);
				ipsEllPair.add(null);
				ipsEllPair.add(null); 
				
				// sort the ellipses in ascending order based on their label
				ellPair = new ArrayList<Ellipse>(2);
				ellPair.add (temp_e1);
				ellPair.add (temp_e2);
				intPntsOfEllipsesBeforeChange.put(ellPair, ipsEllPair);
			}
		}
		zoneAreasBeforeChange = new HashMap<String, Double>();
		for (String zl : diagram.zoneLabels){
			zoneAreasBeforeChange.put(zl, null);
		}
	
		boolean stopHC = false;
		int i = 0;	
		
		double d_atRun1 = d;
		double s_atRun1 = s;
		double r_atRun1 = r;
		
		noOfTimesParamsAreInc = 0;
		
		switch (runType){
			case RunWithInitParamsOnly: case RunWithInitAndIncParams: break;
			case RunWithIncParamsOnly:{
				noOfTimesParamsAreInc++;
				d = d_atRun1 * Math.pow((1 + incParamsBy), noOfTimesParamsAreInc);
				s = s_atRun1 * Math.pow((1 + incParamsBy), noOfTimesParamsAreInc);
				r = r_atRun1 * Math.pow((1 + incParamsBy), noOfTimesParamsAreInc);
				break; 
			}
		}
		
		EllipseDiagram currBestRetryDiag_diagram = null;
		HashMap<String,Double> currBestRetryDiag_zoneVars = null;
		String currBestRetryDiag_reasonForTerminating = null;
		Double currBestRetryDiag_fitness = null;
		double currBestRetryDiag_maxRelError = 0;
		double currBestRetryDiag_maxAbsErroOverReqTot = 0;
		double currBestRetryDiag_maxDiffBetweenReqActualAreaProportions = 0;
		int currBestRetryDiag_elapsedIterations=0;

		boolean tryOtherParams=false;
		
		double currInit_d = d;
		double currInit_s = s;
		double currInit_r = r;
		
		diagram_bestRetry=null;
		
		elapsedIterations_includingRetries=0;
		

		stopWatch.reset(); 
		
		
		do {
			
			stopHC=false;
			dv=2;
			i=0;
			foundMaxAreaAbsErrorBelowThreshold = false;
			foundMaxAreaRelErrorBelowThreshold = false;
			foundMaxAreaDiffBetweenReqActualAreaProportionsThreshold = false;
			foundMaxAreaAbsErrorOverReqTotAreaBelowThreshold = false;
			
			currInit_d = d;
			currInit_s = s;
			currInit_r = r;

			stopWatch_forCurrRun.reset(); 
			
			// put here to avoid discrepancies with stopWatch_forCurrRun when no there are no retries
			if (elapsedIterations_includingRetries == 0){
				stopWatch.start();
			} else {
				stopWatch.resume();
			}
			stopWatch_forCurrRun.start();
						
		
			do {

				// initialization of flags
				for (sc=0; sc < specificPropChanged.length; sc++){
					for (se=0; se < specificPropChanged[sc].length; se++){
						specificPropChanged[sc][se] = false;
					}
				}
	
				someChangeOccurred = false;
				noMorePossibleChanges = true;

				
				// change properties to individual ellipses
				eI = 0;
				for (Ellipse e : diagEllipses){
					
					fitnessCurrentDiag = 0;
					fitnessBeforeChange = 0;
		
					// Variable 1: centre of ellipse
					
					potentialCentres = computePotentialCentres(e); 
					
					foundSomePotentialCentres = false;
					if ((potentialCentres == null) || (potentialCentres.size() == 0)){
						noMorePossibleChanges = noMorePossibleChanges && true;

					} else {
						noMorePossibleChanges = false;
						foundSomePotentialCentres = true;
					}
					
					
					if (foundSomePotentialCentres){
						for (Point2D.Double pc : potentialCentres){

							zoneVarsBeforeChange = Utilities.cloneHashMap(zoneVars);
						    fitnessBeforeChange = fitness.doubleValue(); 

						    centreBeforeChange.x = e.getXc();
						    centreBeforeChange.y = e.getYc();
							
						    intPntsOfEllipsesBeforeChange = diagram.getIntPntsClone(intPntsOfEllipsesBeforeChange);
							zoneAreasBeforeChange = diagram.getZoneAreasClone(zoneAreasBeforeChange);
							zoneAreasComputedSuccessfullyBeforeChange = diagram.zoneAreasComputedSuccessfully;
							
							
							e.setXc(pc.x);
							e.setYc(pc.y);
							diagram.zoneAreasComputedSuccessfully=null;
							
							keepChange = false;
							zoneVarsCurrentDiag = null;
							fitnessCurrentDiag = 0;

							currDiagTryIsValid = diagram.isValid();
							if (currDiagTryIsValid){
								
								zoneVarsCurrentDiag = diagram.computeFitnessOfAllRegions(requiredAreaSpecs, fitnessMeasure);
								fitnessCurrentDiag = diagram.computeFitnessOfDiagram(requiredAreaSpecs, zoneVarsCurrentDiag, fitnessMeasure).doubleValue();
								
								if (EllipseDiagram.improvedFitness(fitnessCurrentDiag, fitnessBeforeChange, improveFitness_changeIfEqual, zoneVarsCurrentDiag, zoneAreasBeforeChange,diagram)){
									keepChange = true;
								} else {
									keepChange = false;
								}
							} else {
								keepChange = false;
							}
							
							if (keepChange){
							
								zoneVars =  Utilities.cloneHashMap(zoneVarsCurrentDiag);
								fitness = fitnessCurrentDiag;
								diagPropChanged = true;
								someChangeOccurred = true;
								specificPropChanged[0][eI] = true;
								

							} else {
								e.setXc(centreBeforeChange.x); 
								e.setYc(centreBeforeChange.y);
								
								diagram.intPntsOfEllipses = EllipseDiagram.cloneIntPntsFromTo(intPntsOfEllipsesBeforeChange, diagram.intPntsOfEllipses); //intPntsOfEllipsesBeforeChange;
								diagram.zoneAreas = EllipseDiagram.cloneZoneAreasFromTo(zoneAreasBeforeChange, diagram.zoneAreas);
								diagram.zoneAreasComputedSuccessfully = zoneAreasComputedSuccessfullyBeforeChange;
								
								zoneVars =  Utilities.cloneHashMap(zoneVarsBeforeChange);
								fitness = fitnessBeforeChange;
								diagPropChanged = false;
								specificPropChanged[0][eI] |= false;
								
							}
							diagPropChanged = false;
							
						}
					}
					
	
					// change a and b together				
					
					// Variable 2: scaling the semi-major and minor axes of the ellipse
					potentialAs = computePotentialAxes(e.getA());
					
					foundSomePotentialAs = false;
					if ((potentialAs == null) || (potentialAs.size() == 0)){
						noMorePossibleChanges = noMorePossibleChanges && true;
					} else {
						noMorePossibleChanges = false;
						foundSomePotentialAs = true;
						potentialAs.add(e.getA());
					}
					 
					if (!restrictToCircles){
						potentialBs = computePotentialAxes(e.getB());
						
						foundSomePotentialBs = false;
						if ((potentialBs == null) || (potentialBs.size() == 0)){
							noMorePossibleChanges = noMorePossibleChanges && true;
						} else {
							noMorePossibleChanges = false;
							foundSomePotentialBs = true;
							potentialBs.add(e.getB());
						}
						
						
						
						if (foundSomePotentialAs){
							for (double pa : potentialAs){
														
								for (double pb : potentialBs){
									
									if ((pa==e.getA()) && (pb==e.getB())){continue;} //if both are equal to current then no need to change it and check it out
									
									zoneVarsBeforeChange = Utilities.cloneHashMap(zoneVars);
								    fitnessBeforeChange = fitness.doubleValue();
								    aBeforeChange = e.getA();
								    bBeforeChange = e.getB();
								    intPntsOfEllipsesBeforeChange = diagram.getIntPntsClone(intPntsOfEllipsesBeforeChange);
									zoneAreasBeforeChange = diagram.getZoneAreasClone(zoneAreasBeforeChange);
									zoneAreasComputedSuccessfullyBeforeChange = diagram.zoneAreasComputedSuccessfully;
								
									
									e.setA(pa); 
									e.setB(pb);
									if (restrictToCircles){ 
										e.setB(e.getA());   
									}               
									diagram.zoneAreasComputedSuccessfully=null;
									
									keepChange = false;
									zoneVarsCurrentDiag = null;
									fitnessCurrentDiag = 0;
									currDiagTryIsValid = diagram.isValid();
									if (currDiagTryIsValid){ 
										zoneVarsCurrentDiag=diagram.computeFitnessOfAllRegions(requiredAreaSpecs, fitnessMeasure);
										fitnessCurrentDiag = diagram.computeFitnessOfDiagram(requiredAreaSpecs, zoneVarsCurrentDiag,fitnessMeasure);
										if (EllipseDiagram.improvedFitness(fitnessCurrentDiag, fitnessBeforeChange, improveFitness_changeIfEqual, zoneVarsCurrentDiag, zoneAreasBeforeChange,diagram)){
											keepChange = true;
										} else {
											keepChange = false;
										}
									} else {
										
										keepChange = false;
									}
									
									if (keepChange){
										zoneVars = Utilities.cloneHashMap(zoneVarsCurrentDiag);
										fitness = fitnessCurrentDiag;
										diagPropChanged = true;
										someChangeOccurred = true;
										specificPropChanged[1][eI] = true;
									} else {
										e.setA(aBeforeChange);
										e.setB(bBeforeChange);
										if (restrictToCircles){e.setB(e.getA());} 
									
										diagram.intPntsOfEllipses = EllipseDiagram.cloneIntPntsFromTo(intPntsOfEllipsesBeforeChange, diagram.intPntsOfEllipses); //intPntsOfEllipsesBeforeChange;
										diagram.zoneAreas = EllipseDiagram.cloneZoneAreasFromTo(zoneAreasBeforeChange, diagram.zoneAreas);
										diagram.zoneAreasComputedSuccessfully = zoneAreasComputedSuccessfullyBeforeChange;
										
										
										zoneVars = Utilities.cloneHashMap(zoneVarsBeforeChange);
										fitness = fitnessBeforeChange;
										diagPropChanged = false;
										specificPropChanged[1][eI] |= false;
									}
						
									diagPropChanged = false;

								}	
							}
						}	
					} else { //restrictToCircles
						
						if (foundSomePotentialAs){
							for (double pa : potentialAs){
								
								if ((pa==e.getA())){continue;} //if equal to current then no need to change it and check it out
								
								zoneVarsBeforeChange = Utilities.cloneHashMap(zoneVars);
								fitnessBeforeChange = fitness.doubleValue();
							    aBeforeChange = e.getA();	
								intPntsOfEllipsesBeforeChange = diagram.getIntPntsClone(intPntsOfEllipsesBeforeChange);
								zoneAreasBeforeChange = diagram.getZoneAreasClone(zoneAreasBeforeChange);
								zoneAreasComputedSuccessfullyBeforeChange = diagram.zoneAreasComputedSuccessfully;		
								
								e.setA(pa); 
								e.setB(pa);
								diagram.zoneAreasComputedSuccessfully=null;
								
								keepChange = false;
								zoneVarsCurrentDiag = null;
								fitnessCurrentDiag = 0;
								currDiagTryIsValid = diagram.isValid();
								if (currDiagTryIsValid){ 
									zoneVarsCurrentDiag = diagram.computeFitnessOfAllRegions(requiredAreaSpecs, fitnessMeasure);
									fitnessCurrentDiag = diagram.computeFitnessOfDiagram(requiredAreaSpecs, zoneVarsCurrentDiag, fitnessMeasure);
									if (EllipseDiagram.improvedFitness(fitnessCurrentDiag, fitnessBeforeChange, improveFitness_changeIfEqual, zoneVarsCurrentDiag, zoneAreasBeforeChange, diagram)){
										keepChange = true;
									} else {
										keepChange = false;
									}
								} else {
									keepChange = false;
								}

								if (keepChange){
									zoneVars = Utilities.cloneHashMap(zoneVarsCurrentDiag);
									fitness = fitnessCurrentDiag;
									diagPropChanged = true;
									someChangeOccurred = true;
									specificPropChanged[1][eI] = true;
									
								} else {
									e.setA(aBeforeChange);
									e.setB(aBeforeChange);
									
									diagram.intPntsOfEllipses = EllipseDiagram.cloneIntPntsFromTo(intPntsOfEllipsesBeforeChange, diagram.intPntsOfEllipses); //intPntsOfEllipsesBeforeChange;
									diagram.zoneAreas = EllipseDiagram.cloneZoneAreasFromTo(zoneAreasBeforeChange, diagram.zoneAreas);
									diagram.zoneAreasComputedSuccessfully = zoneAreasComputedSuccessfullyBeforeChange;
									
									zoneVars = Utilities.cloneHashMap(zoneVarsBeforeChange);
									fitness = fitnessBeforeChange;
									diagPropChanged = false;
									specificPropChanged[1][eI] |= false;
								}			
							
								diagPropChanged = false;
							}	
						}	
						
					}
		
	
					
	  			    // Variable 3: rotation of the ellipse
					if (!restrictToCircles){ // rotation is not required for circles
						
						potentialRots = computePotentialRots(e.getRot());
						
						foundSomePotentialRots = false;
						if ((potentialRots == null) || (potentialRots.size() == 0)){
							noMorePossibleChanges = noMorePossibleChanges && true;
		
						} else {
							noMorePossibleChanges = false;
							foundSomePotentialRots = true;
						}
						
						if (foundSomePotentialRots){
							for (double pr : potentialRots){
							
								zoneVarsBeforeChange = Utilities.cloneHashMap(zoneVars);
								fitnessBeforeChange = fitness.doubleValue(); //diagram.computeFitnessOfDiagram(requiredAreaSpecs);
								rotBeforeChange = e.getRot(); 
								intPntsOfEllipsesBeforeChange = diagram.getIntPntsClone(intPntsOfEllipsesBeforeChange);
								zoneAreasBeforeChange = diagram.getZoneAreasClone(zoneAreasBeforeChange);
								zoneAreasComputedSuccessfullyBeforeChange = diagram.zoneAreasComputedSuccessfully;
									
								
								e.setRot(pr); 
								diagram.zoneAreasComputedSuccessfully=null;
								
								keepChange = false;
								zoneVarsCurrentDiag = null;
								fitnessCurrentDiag = 0;
								currDiagTryIsValid = diagram.isValid();
								if (currDiagTryIsValid){ //if (diagram.isValid()){  //diagram.zoneAreasComputedSuccessfully is checked in diagram.isValid() and in this way 'cannot order ellipses' error would be trapped in isValid and in that case isValid=false
									zoneVarsCurrentDiag=diagram.computeFitnessOfAllRegions(requiredAreaSpecs, fitnessMeasure);
									fitnessCurrentDiag = diagram.computeFitnessOfDiagram(requiredAreaSpecs, zoneVarsCurrentDiag, fitnessMeasure);
									if (EllipseDiagram.improvedFitness(fitnessCurrentDiag, fitnessBeforeChange, improveFitness_changeIfEqual, zoneVarsCurrentDiag, zoneAreasBeforeChange, diagram)){
										keepChange = true;
									} else {
										keepChange = false;
									}
								} else {
									keepChange = false;
								}

								if (keepChange){
				
									zoneVars = Utilities.cloneHashMap(zoneVarsCurrentDiag);
									fitness = fitnessCurrentDiag;
									diagPropChanged = true;
									someChangeOccurred = true;
									specificPropChanged[3][eI] = true;
																	
								} else {
									e.setRot(rotBeforeChange);

									diagram.intPntsOfEllipses = EllipseDiagram.cloneIntPntsFromTo(intPntsOfEllipsesBeforeChange, diagram.intPntsOfEllipses); //intPntsOfEllipsesBeforeChange;
									diagram.zoneAreas = EllipseDiagram.cloneZoneAreasFromTo(zoneAreasBeforeChange, diagram.zoneAreas);
									diagram.zoneAreasComputedSuccessfully = zoneAreasComputedSuccessfullyBeforeChange;
									
									zoneVars = Utilities.cloneHashMap(zoneVarsBeforeChange);
									fitness = fitnessBeforeChange;
									diagPropChanged = false;
									specificPropChanged[3][eI] |= false;
								}
								diagPropChanged = false;

							}			
						}
					}		
					eI++;
					
				}
				
				
				
				// update parameters 
				dv = 2;
				double l = PRECISION_ISZERO;
				double l2 = 1e-5; 
				double diff = 0;
				boolean checkIfParamDiffIsBigEnough = false;
				// ... locked parameters => change all at once
				if (lockHCparams){
					if (!someChangeOccurred){
					
						d = (isZero(d,l) ? 0 : (d/dv));
						s = (isZero(s,l) ? 0 : (s/dv));
						r = (isZero(r,l) ? 0 : (r/dv));
						if (animate){
						}
							
					}
					
				// ... unlocked parameters => change independently	
				} else {
					boolean someParamChanged = false;
					if (propNotChangedForAnyEllipse(specificPropChanged[0])) {
						
						if (!checkIfParamDiffIsBigEnough){
							d = (isZero(d,l) ? 0 : (d/dv));
						} else {
							do{
								d = (isZero(d,l) ? 0 : (d/dv));
							    diff=Math.abs(d-(d*dv));
							} while (isZero(diff,l2) && (d>0));
						}
						someParamChanged = true;
					}			
					if ((propNotChangedForAnyEllipse(specificPropChanged[1]) && propNotChangedForAnyEllipse(specificPropChanged[2]) && !restrictToCircles) ||
					    (propNotChangedForAnyEllipse(specificPropChanged[1]) && restrictToCircles)){
						if (!checkIfParamDiffIsBigEnough){
							s = (isZero(s,l) ? 0 : (s/dv));
						} else {
							do{
								s = (isZero(s,l) ? 0 : (s/dv));
							    diff=Math.abs(s-(s*dv));
							} while (isZero(diff,l2) && (s>0));
						}

						someParamChanged = true;
 
					}
					if ((!restrictToCircles)&&(propNotChangedForAnyEllipse(specificPropChanged[3]))) {
						if (!checkIfParamDiffIsBigEnough){
							r = (isZero(r,l) ? 0 : (r/dv));
						} else {
							do{
								r = (isZero(r,l) ? 0 : (r/dv));
								diff=Math.abs(r-(r*dv));
							} while (isZero(diff,l2) && (r>0));
						}
						someParamChanged = true;
					}				
				}
				
				if (someChangeOccurred && animate){			
					stopWatch.pause();
					stopWatch_forCurrRun.pause();
					switchBoardPanel.updateEllipseDiagramPanel_updatedDiagram(zoneVars, fitness); //zoneVarsCurrentDiag, fitnessCurrentDiag);
					stopWatch_forCurrRun.resume();
					stopWatch.resume();	
				}
	
				stopWatch.pause();
				stopWatch_forCurrRun.pause();
				elapsedTimeMilliSecs = stopWatch.getElapsedTimeMilliSecs();
				elapsedTimeString = stopWatch.getElapsedTimeString();
				elapsedTimeMilliSecs_forCurrRun = stopWatch_forCurrRun.getElapsedTimeMilliSecs();
				elapsedTimeString_forCurrRun = stopWatch_forCurrRun.getElapsedTimeString();
				//elapsedIterations = i+1; //+1 since start from 0
				switchBoardPanel.updateHCStopWatch(updateStopWatchDisplay);
				
				
				// compute current errors measures
				
				currReqValue = 0;
				currAbsError = 0;
				currRelError = 0; 
				currAbsDiffBetweenReqActualAreaProportions = 0;
				currAbsErrorOverReqTot = 0;
				
				maxAbsError = null;
				maxRelError = null;
				maxAbsDiffBetweenReqActualAreaProportions = null;
				maxAbsErrorOverReqTot = null;
				
				maxAbsErrorZoneLabel="";
				maxRelErrorZoneLabel=""; 
				maxAbsDiffBetweenReqActualAreaProportionsZoneLabel = "";
				maxAbsErrorOverReqTotZoneLabel = "";

				totCurrDiagArea = 0;
				totReqDiagArea = 0;
				
				totAbsErrorForDiag=0;
				totRelErrorForDiag=0;
				totAbsDiffBetweenReqActualAreaProportionsForDiag=0;
				totAbsErrorOverReqTotForDiag=0;
				
				avgAbsErrorForDiag=0;
				avgRelErrorForDiag=0;
				avgAbsDiffBetweenReqActualAreaProportionsForDiag=0;
				avgAbsErrorOverReqTotForDiag=0;
				
				errorsPerRegion="";
				
				
				for (String zl : diagram.zoneLabels){
					currReqValue = requiredAreaSpecs.get(zl);
				
					totCurrDiagArea += diagram.zoneAreas.get(zl);
					totReqDiagArea += currReqValue;
				}
				for (String zl : diagram.zoneLabels){
					currReqValue = requiredAreaSpecs.get(zl);
					
					// final error measures for the current region
					currAbsError = Math.abs(currReqValue - diagram.zoneAreas.get(zl));
					currRelError = currAbsError / currReqValue;
					currAbsDiffBetweenReqActualAreaProportions = Math.abs((currReqValue/totReqDiagArea) - (diagram.zoneAreas.get(zl)/totCurrDiagArea));
					currAbsErrorOverReqTot = currAbsError / totReqDiagArea;
					
					switch (fitnessMeasure){
						case STRESS: 
						case CHOWRODGERS_IDEAL_SUMREGFIT: case CHOWRODGERS_IDEAL_MAXREGFIT: case CHOWRODGERS_IDEAL_MEANREGFIT:
						case RELATIVE_ERROR_SUMREGFIT_SCALEACTTOREQ: case RELATIVE_ERROR_MAXREGFIT_SCALEACTTOREQ: case RELATIVE_ERROR_MEANREGFIT_SCALEACTTOREQ:
						case ABSERROR_OVER_REQTOTAREA_SUMREGFIT_SCALEACTTOREQ: case ABSERROR_OVER_REQTOTAREA_MAXREGFIT_SCALEACTTOREQ: case ABSERROR_OVER_REQTOTAREA_MEANREGFIT_SCALEACTTOREQ:{
							
							currAbsError = Math.abs(currReqValue - (diagram.zoneAreas.get(zl) * (totReqDiagArea/totCurrDiagArea)));
							currRelError = currAbsError / currReqValue;
							currAbsErrorOverReqTot = currAbsError / totReqDiagArea;
							
							break;
						}
					}
					
					
					errorsPerRegion += currReqValue +" | "+ diagram.zoneAreas.get(zl) +" | "+ currAbsError +" | "+ currRelError+" | "+ currAbsDiffBetweenReqActualAreaProportions +" | " + currAbsErrorOverReqTot  +" | ";
					
					// computing total error
					totAbsErrorForDiag+=currAbsError;
					totRelErrorForDiag+=currRelError;
					totAbsDiffBetweenReqActualAreaProportionsForDiag+=currAbsDiffBetweenReqActualAreaProportions;
					totAbsErrorOverReqTotForDiag+=currAbsErrorOverReqTot;
					
					// finding max error
					if ((maxAbsError==null)||(maxAbsError<currAbsError)){
							maxAbsError = currAbsError;
							maxAbsErrorZoneLabel = zl;
					}
					if ((maxRelError==null)||(maxRelError<currRelError)){
							maxRelError = currRelError;
							maxRelErrorZoneLabel = zl;
					}
					if ((maxAbsDiffBetweenReqActualAreaProportions==null)||(maxAbsDiffBetweenReqActualAreaProportions<currAbsDiffBetweenReqActualAreaProportions)){
							maxAbsDiffBetweenReqActualAreaProportions = currAbsDiffBetweenReqActualAreaProportions;
							maxAbsDiffBetweenReqActualAreaProportionsZoneLabel = zl;
					}
					if ((maxAbsErrorOverReqTot==null)||(maxAbsErrorOverReqTot<currAbsErrorOverReqTot)){
							maxAbsErrorOverReqTot = currAbsErrorOverReqTot;
							maxAbsErrorOverReqTotZoneLabel = zl;
					}
				}
				// compute average error
				avgAbsErrorForDiag = totAbsErrorForDiag / diagram.zoneLabels.length;
				avgRelErrorForDiag = totRelErrorForDiag / diagram.zoneLabels.length;
				avgAbsDiffBetweenReqActualAreaProportionsForDiag = totAbsDiffBetweenReqActualAreaProportionsForDiag / diagram.zoneLabels.length;
				avgAbsErrorOverReqTotForDiag = totAbsErrorOverReqTotForDiag / diagram.zoneLabels.length;
			
				
				
				logAllErrorMeasuresStr = maxAbsError + " | " + maxAbsErrorZoneLabel + " | " + avgAbsErrorForDiag + " | " + totAbsErrorForDiag + " | " + 
		                 maxRelError + " | " + maxRelErrorZoneLabel + " | " + avgRelErrorForDiag + " | " + totRelErrorForDiag + " | " + 
		                 maxAbsDiffBetweenReqActualAreaProportions + " | " + maxAbsDiffBetweenReqActualAreaProportionsZoneLabel + " | " + avgAbsDiffBetweenReqActualAreaProportionsForDiag + " | " + totAbsDiffBetweenReqActualAreaProportionsForDiag + " | " + 
		                 maxAbsErrorOverReqTot + " | " + maxAbsErrorOverReqTotZoneLabel + " | " + avgAbsErrorOverReqTotForDiag + " | " + totAbsErrorOverReqTotForDiag + " | " + 
		                 totCurrDiagArea + " | " + totReqDiagArea;
				
				
				
				// check if any thresholds are met

				if (saveDetailsWhenReachMaxRegionAreaThreshold && (!foundMaxAreaAbsErrorBelowThreshold || !foundMaxAreaRelErrorBelowThreshold || !foundMaxAreaDiffBetweenReqActualAreaProportionsThreshold || !foundMaxAreaAbsErrorOverReqTotAreaBelowThreshold)){
					
					if (!foundMaxAreaAbsErrorBelowThreshold){
						fitnessAtMaxAbsError = fitness.doubleValue();
					}
					if(!foundMaxAreaRelErrorBelowThreshold){
						fitnessAtMaxRelError = fitness.doubleValue();
					}
					if(!foundMaxAreaDiffBetweenReqActualAreaProportionsThreshold){
						fitnessAtMaxAbsDiffBetweenReqActualAreaProportions = fitness.doubleValue();
					}
					if(!foundMaxAreaAbsErrorOverReqTotAreaBelowThreshold){
						fitnessAtMaxAbsErrorOverReqTot = fitness.doubleValue();
					}
				
					// if the max area abs error is <= the abs error threshold for a diagram to be considered acceptable, save details 
					if (!foundMaxAreaAbsErrorBelowThreshold && (maxAbsError <= maxRegionAreaAbsErrThreshold)){
						
						foundMaxAreaAbsErrorBelowThreshold = true;
						
						logAbsErrStr = maxRegionAreaAbsErrThreshold + " | " + maxAbsError + " | " + fitnessAtMaxAbsError + " | " + maxAbsErrorZoneLabel + " | before | " + elapsedTimeString  + " | " + elapsedTimeMilliSecs;
						
						// save diagram files as soon as the reach an exact diagram based on the max absolute error of the region areas of the diagram and the required exactness threshold
						if (savingIntermediateDiagImgFile_absErr != null){
							EllipseDiagramOps.saveDiagImgToFile(ellipseDiagramPanel, savingIntermediateDiagImgFile_absErr);
						}
						if (savingIntermediateDiagFile_absErr != null){
							EllipseDiagramOps.saveDiagToFile(diagram, savingIntermediateDiagFile_absErr);
						}
					}				
					if (!foundMaxAreaRelErrorBelowThreshold && (maxRelError <= maxRegionAreaRelErrThreshold)){//if (maxAbsError <= maxRegionAreaDiffThreshold){
						
						foundMaxAreaRelErrorBelowThreshold = true; 
						
						logRelErrStr = maxRegionAreaRelErrThreshold + " | " + maxRelError + " | " + fitnessAtMaxRelError + " | " + maxRelErrorZoneLabel + " | before | " + elapsedTimeString  + " | " + elapsedTimeMilliSecs;
						
						// save diagram files as soon as the reach an exact diagram based on the max absolute error of the region areas of the diagram and the required exactness threshold
						if (savingIntermediateDiagImgFile_relErr != null){
							EllipseDiagramOps.saveDiagImgToFile(ellipseDiagramPanel, savingIntermediateDiagImgFile_relErr);
						}
						if (savingIntermediateDiagFile_relErr != null){
							EllipseDiagramOps.saveDiagToFile(diagram, savingIntermediateDiagFile_relErr);
						}
					}	
					if (!foundMaxAreaDiffBetweenReqActualAreaProportionsThreshold && (maxAbsDiffBetweenReqActualAreaProportions <= maxRegionAreaAbsDiffBetweenReqActualProportionsThreshold)){
						
						foundMaxAreaDiffBetweenReqActualAreaProportionsThreshold = true; 							
						
						logDiffBetweenReqActualAreaProportionsStr = maxRegionAreaAbsDiffBetweenReqActualProportionsThreshold + " | " + maxAbsDiffBetweenReqActualAreaProportions + " | " + 
																	fitnessAtMaxAbsDiffBetweenReqActualAreaProportions + " | " + maxAbsDiffBetweenReqActualAreaProportionsZoneLabel +
																	" | before | " + elapsedTimeString  + " | " + elapsedTimeMilliSecs;
						
						//if foundMaxAreaDiffBetweenReqActualAreaProportions then HC terminates and thus we don't need to save intermediate diagram files as for the abs error and rel error
					}
					if (!foundMaxAreaAbsErrorOverReqTotAreaBelowThreshold && (maxAbsErrorOverReqTot <= maxRegionAreaAbsErrorOverReqTotThreshold)){
						
						foundMaxAreaAbsErrorOverReqTotAreaBelowThreshold = true;
						
						logAbsErrOverReqTotStr = maxRegionAreaAbsErrorOverReqTotThreshold + " | " + maxAbsErrorOverReqTot + " | " + fitnessAtMaxAbsErrorOverReqTot + " | " + maxAbsErrorOverReqTotZoneLabel + " | before | " + elapsedTimeString  + " | " + elapsedTimeMilliSecs;
						
						// save diagram files as soon as the reach an exact diagram based on the max absolute error of the region areas of the diagram and the required exactness threshold
					}	
					
				}
	
				stopWatch.resume();	
				stopWatch_forCurrRun.resume();

								
					
				// determine whether should stop and the reason for termination
				
				if (foundMaxAreaDiffBetweenReqActualAreaProportionsThreshold){ 
					stopHC = true;
					reasonForTermination = "Obtained desired area specification: maximum difference between the regions' required and actual area proportion is <="+maxRegionAreaAbsDiffBetweenReqActualProportionsThreshold; 
					
				} else if (isZero(d,l) && isZero(s,l) && isZero(r,l)){
					stopHC = true;
					reasonForTermination = "Cannot change parameters further: all are 0";
	
				} else if (noMorePossibleChanges){
					stopHC = true;
					reasonForTermination = "No more possible changes can be made: exhausted all the possible solutions";

				} else if (forceToTerm && ((i+1) >= maxNoOfIterations)){ 
					stopHC = true;
					reasonForTermination = "Forced to terminate: "+maxNoOfIterations+" iterations were performed";
	            }	

				
				i++;
				elapsedIterations_includingRetries++;
				elapsedIterations_forCurrRun = i;

			} while (!stopHC);
			
			stopWatch.pause();
		    stopWatch_forCurrRun.pause();
			
		    
		    logAllErrorMeasuresStr = maxAbsError + " | " + maxAbsErrorZoneLabel + " | " + avgAbsErrorForDiag + " | " + totAbsErrorForDiag + " | " + 
		    		                 maxRelError + " | " + maxRelErrorZoneLabel + " | " + avgRelErrorForDiag + " | " + totRelErrorForDiag + " | " + 
		    		                 maxAbsDiffBetweenReqActualAreaProportions + " | " + maxAbsDiffBetweenReqActualAreaProportionsZoneLabel + " | " + avgAbsDiffBetweenReqActualAreaProportionsForDiag + " | " + totAbsDiffBetweenReqActualAreaProportionsForDiag + " | " + 
		    		                 maxAbsErrorOverReqTot + " | " + maxAbsErrorOverReqTotZoneLabel + " | " + avgAbsErrorOverReqTotForDiag + " | " + totAbsErrorOverReqTotForDiag + " | " + 
		    		                 totCurrDiagArea + " | " + totReqDiagArea;
		    
			
			if (saveDetailsWhenReachMaxRegionAreaThreshold && (!foundMaxAreaAbsErrorBelowThreshold || !foundMaxAreaRelErrorBelowThreshold || !foundMaxAreaAbsErrorBelowThreshold || !foundMaxAreaAbsErrorOverReqTotAreaBelowThreshold)){		//if (saveDetailsWhenReachMaxRegionAreaThreshold && !foundMaxAreaAbsErrorBelowThreshold){
			
				if (!foundMaxAreaAbsErrorBelowThreshold){
					fitnessAtMaxAbsError = fitness.doubleValue();
				}
				if(!foundMaxAreaRelErrorBelowThreshold){
					fitnessAtMaxRelError = fitness.doubleValue();
				}
				if(!foundMaxAreaDiffBetweenReqActualAreaProportionsThreshold){
					fitnessAtMaxAbsDiffBetweenReqActualAreaProportions = fitness.doubleValue();
				}
				if (!foundMaxAreaAbsErrorOverReqTotAreaBelowThreshold){
					fitnessAtMaxAbsErrorOverReqTot = fitness.doubleValue();
				}
				
				// record details of max area abs error
				// if the max area abs error is <= the abs error threshold for a diagram to be considered acceptable, save details 
				if (!foundMaxAreaAbsErrorBelowThreshold){
					logAbsErrStr = maxRegionAreaAbsErrThreshold + " | " + maxAbsError + " | " + fitnessAtMaxAbsError + " | " + maxAbsErrorZoneLabel + " | after | " + elapsedTimeString  + " | " + elapsedTimeMilliSecs;
				}				
				if (!foundMaxAreaRelErrorBelowThreshold){
					logRelErrStr = maxRegionAreaRelErrThreshold + " | " + maxRelError + " | " + fitnessAtMaxRelError + " | " + maxRelErrorZoneLabel + " | after | " + elapsedTimeString  + " | " + elapsedTimeMilliSecs;
				}	
				if (!foundMaxAreaDiffBetweenReqActualAreaProportionsThreshold){
					logDiffBetweenReqActualAreaProportionsStr = maxRegionAreaAbsDiffBetweenReqActualProportionsThreshold + " | " + maxAbsDiffBetweenReqActualAreaProportions + " | " + 
															    fitnessAtMaxAbsDiffBetweenReqActualAreaProportions + " | " + maxAbsDiffBetweenReqActualAreaProportionsZoneLabel + " | after | " + elapsedTimeString  + " | " + elapsedTimeMilliSecs;
				}
				if (!foundMaxAreaAbsErrorOverReqTotAreaBelowThreshold){
					logAbsErrOverReqTotStr = maxRegionAreaAbsErrorOverReqTotThreshold + " | " + maxAbsErrorOverReqTot + " | " + fitnessAtMaxAbsErrorOverReqTot + " | " + maxAbsErrorOverReqTotZoneLabel + " | after | " + elapsedTimeString  + " | " + elapsedTimeMilliSecs;
				}
			}
			
			
			logStr = logAllErrorMeasuresStr + " | " + logAbsErrStr + " | " + logRelErrStr + " | " + logDiffBetweenReqActualAreaProportionsStr + " | " + logAbsErrOverReqTotStr;
			
			
			switch (runType){
				case RunWithInitParamsOnly: {
					tryOtherParams=false; 
					break;
				}
				case RunWithIncParamsOnly: 
				case RunWithInitAndIncParams:
				{	
					totReqDiagArea = 0;
					for (String zl : diagram.zoneLabels){
						currReqValue = requiredAreaSpecs.get(zl);
						totReqDiagArea += currReqValue;
					}
					maxAbsErrorOverReqTot=null;
					for (String zl : diagram.zoneLabels){
						currReqValue = requiredAreaSpecs.get(zl);
						currAbsError = Math.abs(currReqValue - diagram.zoneAreas.get(zl));
						currAbsErrorOverReqTot = currAbsError / totReqDiagArea; 
						if ((maxAbsErrorOverReqTot==null)||(maxAbsErrorOverReqTot<currAbsErrorOverReqTot)){
							maxAbsErrorOverReqTot = currAbsErrorOverReqTot;
						}
					}
	
					if ((currBestRetryDiag_fitness==null)||
						(maxAbsDiffBetweenReqActualAreaProportions < currBestRetryDiag_maxDiffBetweenReqActualAreaProportions)){ 
							
						currBestRetryDiag_fitness = new Double(fitness.doubleValue());
						currBestRetryDiag_diagram = diagram.clone();
						currBestRetryDiag_zoneVars = Utilities.cloneHashMap(zoneVars);
						currBestRetryDiag_reasonForTerminating = reasonForTermination;
						currBestRetryDiag_elapsedIterations = elapsedIterations_forCurrRun;
						currBestRetryDiag_maxDiffBetweenReqActualAreaProportions = new Double(maxAbsDiffBetweenReqActualAreaProportions.doubleValue());
						
						elapsedTimeMilliSecs_forCurrRun = stopWatch_forCurrRun.getElapsedTimeMilliSecs();
						elapsedTimeString_forCurrRun = stopWatch_forCurrRun.getElapsedTimeString();
						
						bestretry_index = noOfTimesParamsAreInc;
						elapsedTimeString_bestretry = elapsedTimeString_forCurrRun;
						elapsedTimeMilliSecs_bestretry = elapsedTimeMilliSecs_forCurrRun;
						elapsedIterations_bestretry = elapsedIterations_forCurrRun;
						fitness_bestretry = fitness.doubleValue(); 
						reasonForTermination_bestretry = reasonForTermination;
						logAbsErrStr_bestretry = logAbsErrStr;
						logRelErrStr_bestretry = logRelErrStr;
						logDiffBetweenReqActualAreaProportionsStr_bestretry = logDiffBetweenReqActualAreaProportionsStr;
						logAbsErrOverReqTotStr_bestretry = logAbsErrOverReqTotStr;
						logStr_bestretry = logStr;						
						d_bestretry = currInit_d;
						s_bestretry = currInit_s;
						r_bestretry = currInit_r;
						
						diagram_bestRetry = currBestRetryDiag_diagram;
					}
					
					if (reasonForTermination.contains("Obtained desired area specification")){
						tryOtherParams = false;
						
					} else {
						
						if (noOfTimesParamsAreInc >= maxTimesParamsAreInc){
					
							tryOtherParams = false;		
						
							fitness = new Double (currBestRetryDiag_fitness.doubleValue());
							diagram = currBestRetryDiag_diagram.clone();
							zoneVars = Utilities.cloneHashMap(currBestRetryDiag_zoneVars);
							reasonForTermination = currBestRetryDiag_reasonForTerminating;
							elapsedIterations_forCurrRun = currBestRetryDiag_elapsedIterations;
							
							if (animate){			
								switchBoardPanel.updateEllipseDiagramPanel_updatedDiagram(zoneVars, fitness); 	
							}
						} else {
							
							tryOtherParams = true;
							
							noOfTimesParamsAreInc++;
							d = d_atRun1 * Math.pow((1 + incParamsBy), noOfTimesParamsAreInc);
							s = s_atRun1 * Math.pow((1 + incParamsBy), noOfTimesParamsAreInc);
							r = r_atRun1 * Math.pow((1 + incParamsBy), noOfTimesParamsAreInc);
							
							diagram = initDiag.clone();
							for (int c=0; c<diagEllipses.size(); c++){
								diagEllipses.get(c).setA(diagram.getEllipses().get(c).a);
								diagEllipses.get(c).setB(diagram.getEllipses().get(c).b);
								diagEllipses.get(c).setXc(diagram.getEllipses().get(c).xc); 
								diagEllipses.get(c).setYc(diagram.getEllipses().get(c).yc);
								diagEllipses.get(c).setRot(diagram.getEllipses().get(c).rot);
							}
							diagram.ellipses=diagEllipses;
							diagram.zoneAreasComputedSuccessfully = null;
							zoneVars=diagram.computeFitnessOfAllRegions(requiredAreaSpecs, fitnessMeasure);
							fitness = diagram.computeFitnessOfDiagram(requiredAreaSpecs, zoneVars, fitnessMeasure);
							
							
							if (animate){			
								switchBoardPanel.updateEllipseDiagramPanel_updatedDiagram(zoneVars, fitness); 
							}
						}	
					}
					break;
				}
			}
				
			stopWatch_forCurrRun.stop();
					
		} while (tryOtherParams);
		stopWatch.stop(); 
		
		elapsedTimeMilliSecs = stopWatch.getElapsedTimeMilliSecs();
		elapsedTimeString = stopWatch.getElapsedTimeString();

		return reasonForTermination;
	}
	    
    
	
	// ... logging details of hill climber while running 
	
	private BufferedWriter initLogger(String dateTime, int mode, boolean lockHCparams){
		String runModeStr = "";
		if (mode == MULTIPLE_CHANGES_PER_ITER){
			runModeStr="Multiple Changes per Iteration";
		} else if (mode == SINGLE_CHANGE_PER_ITER){
			runModeStr="Single Change per Iteration";
		}
		runModeStr += " - " + (lockHCparams ? "locked" : "unlocked (independent)") ;
		
		BufferedWriter bwLogger = null;
		if (logfileFile != null){
			try{
				bwLogger = new BufferedWriter(new FileWriter(logfileFile.getAbsoluteFile()));

				bwLogger.newLine();
				bwLogger.append("Hill Climber Logger -> " + dateTime); bwLogger.newLine(); bwLogger.newLine();
				bwLogger.append("- " + runModeStr); bwLogger.newLine();
				bwLogger.append("- parameters: d=" + d + ", s=" + s + ", r=" + r); bwLogger.newLine();
				bwLogger.append("- fitness improved: if same fitness, accept changes = " + improveFitness_changeIfEqual); bwLogger.newLine();
				bwLogger.append("- precision to determine if Zero: " + PRECISION_ISZERO); bwLogger.newLine(); bwLogger.newLine();
				bwLogger.append("-------------------------------------------------------------------------------------------------------------------------------------"); 
				bwLogger.newLine();bwLogger.newLine();bwLogger.newLine();bwLogger.newLine();
				bwLogger.append("Iter |  Fitness  |  Diagram -> label | a | b | xc | yc | e.rot |"); bwLogger.newLine();
			}catch(IOException e) {
				System.out.println("HillClimber.initLogger: An IO exception occured -> " + e);
				return null;
			}	
		}
		return bwLogger;
	}


	// Static and Private methods to determine precision 

	private boolean arrayListContains(ArrayList<Double> potentialAxes, double elemToFind){
		
		for (Double currElem : potentialAxes){
			if (areEqual(currElem.doubleValue(), elemToFind)){
				return true; 
			}
		}
		return false;
	}
	private boolean arrayListContains_Point2D(ArrayList<Point2D.Double> array, Point2D.Double elemToFind){
	
		for (Point2D.Double currElem : array){
			if (areEqual(currElem.x, elemToFind.x) && areEqual(currElem.y, elemToFind.y)){
				return true; 
			}
		}
		return false;
	}

	public static boolean areEqual(double x, double y){
		return isZero(Math.abs(x-y)); 
	}
	public static boolean areEqual(double x, double y, double l){
		return isZero(Math.abs(x-y),l); 
	}
	public static boolean isZero(double x){
		return isZero(x, PRECISION_ISZERO);
	}
	public static boolean isZero(double x, double l){
		double EQN_EPS = l;
		return ((x >= -EQN_EPS) && (x <= EQN_EPS));
	}
	

}
