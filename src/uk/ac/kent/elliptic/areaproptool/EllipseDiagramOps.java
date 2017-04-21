/* 
 * 
 * eulerAPE -- Drawing Area-Proportional Euler and Venn Diagrams Using Ellipses	    
 * 		http://www.eulerdiagrams.org/eulerAPE
 * 
 * 
 * 		Copyright (C) Luana Micallef and Peter Rodgers. 
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

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
/* org.w3c.dom
 *      providing the Document Object Model (DOM) interfaces, 
 *      available at http://download.oracle.com/javase/1.4.2/docs/api/org/w3c/dom/package-summary.html
 */

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
/* org.apache.batik, the Apache Batik Project and Java SVG Toolkit, 
 *  	is available for download at
 *  	http://xmlgraphics.apache.org/batik/download.html
 */

import elliptic.areaproptool.TestingWithPolygons.DiagramZoneCondition;



/**
 * 
 * Operations to handle and manage Euler diagrams drawn with ellipses
 *
 */

 

public class EllipseDiagramOps {
	
	// Data members
	public static String[] zoneLabels_Venn2 = {"a", "b", "ab"};
	public static String[] zoneLabels_Venn3 = {"a", "b", "c", "ab", "ac", "bc", "abc"};
	public static String[] zoneLabels_Venn4 = {"a", "b", "c", "d", "ab", "ac", "ad", "bc", "bd", "cd", "abc", "abd", "acd", "bcd", "abcd"};
	
	// ... loading and saving diagram to file
 	public static final String FILESTARTABSTRACTDESCRIPTION  = "ABSTRACTDESCRIPTION";
 	public static final String FILESTARTDIAGRAM = "DIAGRAM";
 	public static final String FILESTARTELLIPSES = "ELLIPSES";
 	public static final String FILEELLIPSESPROPS = " - label | semi-major axis | semi-minor axis | centre - x | centre - y | rotation |";  //use semi-minor and major because that is the norm for defining ellipses 
 	public static final String FILESTARTCIRCLES = "CIRCLES";
 	public static final char FILESEPARATOR = '|';
 	
	
 	
 	// Methods
	
 	
	// ... related to zones
	public static int getNoOfZonesInVenn (int noOfEllipses, boolean includeEmptyZone){
		int noOfZonesInVenn = (int) Math.pow(2, noOfEllipses);
		if (!includeEmptyZone){
			noOfZonesInVenn -= 1;
		}
		return noOfZonesInVenn;
	}
	
	
    /* Getting zone labels -> static to be able to retrieve zone labels even if a diagram is not yet instantiated
	 * 	  Method not fully automatic -> not worth working out the zone labels because most of the diagrams will not have more than 3 ellipses 
	 * 	  => might as well define the zone labels as a hard coded array
	 */
	public static String[] getZoneLabels (int noOfEllipses, boolean includeEmptyZone){
		String[] emptyZone = {""};
		String[] zoneLabels = {}; 
		
		if (noOfEllipses == 2){
			zoneLabels = zoneLabels_Venn2;
		} else if (noOfEllipses == 3){
			zoneLabels = zoneLabels_Venn3;
		} else if (noOfEllipses == 4){
			zoneLabels = zoneLabels_Venn4;
		} else {
			return null;
		}
		
		return (!includeEmptyZone ? zoneLabels : (String[]) Utilities.concatArrays((Object[]) emptyZone, (Object[]) zoneLabels));
	}

	
	public static HashMap<String, Double> sortZoneAreasAsInLabelArray(String[] zoneLabels, HashMap<String, Double> unsortedZoneAreas){
	
		if (zoneLabels == null){return null;}
		if (unsortedZoneAreas == null){return null;}
		
		HashMap<String, Double> sortedZoneAreas = new HashMap<String, Double>();
		
		for (String zl : zoneLabels){
			Double zoneArea = unsortedZoneAreas.get(zl);
			sortedZoneAreas.put(zl, zoneArea);
		}
		
		return sortedZoneAreas;
	}
	
	
	public static Area[] getZonesAsAreas_3EllDiag (EllipseDiagram diagram, Point2D.Double centreOfSystem) {
		// empty zone is included (index=0) but is left empty/null
		// try to do this generic or at least, appropriate for 2 and 3 ellipses
		
		if (diagram.getEllipses().size() != 3){
			System.out.println("EllipseDiagramOps.getZonesAsAreas_3EllDiag: the diagram is not made up of 3-ellipses");
			return null;
		}
		
		ArrayList<Ellipse> diagEllipses = diagram.getEllipses();  
		
		Area areaA = null;
		Area areaB = null;
		Area areaC = null;
		
		for (Ellipse e : diagEllipses){
			if (e.label.toLowerCase().equals("a")){
				areaA = e.getShapeAreaForDisplay(centreOfSystem);
			} else if (e.label.toLowerCase().equals("b")){
				areaB = e.getShapeAreaForDisplay(centreOfSystem);
			} else if (e.label.toLowerCase().equals("c")){
				areaC = e.getShapeAreaForDisplay(centreOfSystem);
			}  
		}
	
		Area zones[] = new Area[8];

		// zone a
		zones[1] = new Area(areaA);
		zones[1].subtract(areaB);
		zones[1].subtract(areaC);

		// zone b
		zones[2] = new Area(areaB);
		zones[2].subtract(areaA);
		zones[2].subtract(areaC);

		// zone c
		zones[3] = new Area(areaC);
		zones[3].subtract(areaA);
		zones[3].subtract(areaB);
		
		// zone ab
		zones[4] = new Area(areaA);
		zones[4].intersect(areaB);
		zones[4].subtract(areaC);

		// zone ac
		zones[5] = new Area(areaA);
		zones[5].intersect(areaC);
		zones[5].subtract(areaB);

		// zone bc
		zones[6] = new Area(areaB);
		zones[6].intersect(areaC);
		zones[6].subtract(areaA);
			
		// zone abc
		zones[7] = new Area(areaA);
		zones[7].intersect(areaB);
		zones[7].intersect(areaC);
		
		return zones;
	}
	
	
	public static Area getSetAsArea (Ellipse e, Point2D.Double centreOfSystem) {
		// empty zone is included (index=0) but is left empty/null
		// try to do this generic or at least, appropriate for 2 and 3 ellipses
		return (e.getShapeAreaForDisplay(centreOfSystem));
	}
	
	
	public static Area[] getSetsAsAreas_3EllDiag (EllipseDiagram diagram, Point2D.Double centreOfSystem) {
		// empty zone is included (index=0) but is left empty/null
		// try to do this generic or at least, appropriate for 2 and 3 ellipses
		
		if (diagram.getEllipses().size() != 3){
			System.out.println("EllipseDiagramOps.getSetsAsAreas_3EllDiag: the diagram is not made up of 3-ellipses");
			return null;
		}
		
		ArrayList<Ellipse> diagEllipses = diagram.getEllipses();  
		
		Area areaA = null;
		Area areaB = null;
		Area areaC = null;
		
		for (Ellipse e : diagEllipses){
			if (e.label.toLowerCase().equals("a")){
				areaA = e.getShapeAreaForDisplay(centreOfSystem);
			} else if (e.label.toLowerCase().equals("b")){
				areaB = e.getShapeAreaForDisplay(centreOfSystem);
			} else if (e.label.toLowerCase().equals("c")){
				areaC = e.getShapeAreaForDisplay(centreOfSystem);
			}  
		}
		
		Area zones[] = new Area[]{areaA, areaB, areaC};
		
		return zones;
	}
	
	
	
	
	// ... load and save diagram (or its details and properties) from/to file 

	public static File[] getDiagFilesInDir (File dir){
		FilenameFilter filterDiagDescFiles = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".eld");
		}};
			
		return dir.listFiles(filterDiagDescFiles);
	}
	
	public static File[] getFinalDiagFilesInDir (File dir){
		FilenameFilter filterDiagDescFiles = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith("final.eld");
		}};
			
		return dir.listFiles(filterDiagDescFiles);
	}
	
	public static File[] getLWAreaSpecInputFilesInDir (File dir){
		FilenameFilter filterDiagDescFiles = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith("_areaspecs_input.lw");
		}};
			
		return dir.listFiles(filterDiagDescFiles);
	}
	
	public static File[] getLWGenDiagFilesInDir (File dir){
		FilenameFilter filterDiagDescFiles = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith("_genDiagDetails_final.lw");
		}};
			
		return dir.listFiles(filterDiagDescFiles);
	}
	
	public static HashMap<String, Double> getReqAreaSpecFromLWFile(File file){
		HashMap<String, Double> areaspecs = new HashMap<String, Double>();
		try {
			BufferedReader b = new BufferedReader(new FileReader(file));
			String line = b.readLine();
			String[] colData = new String[2];
			String[] setLabels; 
			String zoneareaName = "";
			Double zonearea=0.0;
			while(line != null) {
				line = line.trim();
				if(line.equals("")) {
					line = b.readLine();
					continue;
				}
				line = line.trim();
				colData = line.split(" ");
				
				zoneareaName = "";
		        setLabels =  colData[0].split("&");
		        Arrays.sort(setLabels); //to make sure that they are sorted in alphabetical order
		        for (int j = 0; j < setLabels.length; j++) {
		        	zoneareaName += setLabels[j];
		        }
		    	zonearea = Utilities.safeParseDouble(colData[1]);
		    	areaspecs.put(zoneareaName, zonearea);
		    	
				line = b.readLine();	
			}
			b.close();
		} catch(IOException e){
			System.out.println("EllipseDiagramOps.getReqAreaSpecFromLWFile: "+e+"\n");
			System.exit(1);
		}
		
		return (areaspecs);
	}
	
	
	
	// ... load and save the file names of the diagrams from a file (assuming that the names are put one below the other in the form of a list)
	public static ArrayList<String> getDiagFileNamesFromFile (File file){
		ArrayList<String> diagFileNames = new ArrayList<String>();
		try {
			BufferedReader b = new BufferedReader(new FileReader(file));
			String line = b.readLine();
			while(line != null) {
				line = line.trim();
				if(line.equals("")) {
					line = b.readLine();
					continue;
				}
				diagFileNames.add(line);
				line = b.readLine();	
			}
			b.close();
		} catch(IOException e){
			System.out.println("EllipseDiagramOps.loadDiagFromFile: An IO exception occured when executing loadAdjacencyFile("+file+") in SimpleConcreteDiagram.java: "+e+"\n");
			System.exit(1);
		}
		
		return (diagFileNames);
	}
	
	
	
	// ... ... handling the diagram itself

	public static EllipseDiagram loadDiagFromFile (File file, boolean considerLessPrecisePossibleIntPnts){
		// null is returned as soon at fails one of these checks that is 
		// - 3 ellipses 
		// - ellipses labelled as a,b,c (order not imp)
		// - the proper string of each ellipse    one of {a,b,c} | double for semi-major axis | double for semi-minor axis | double for centre x | double for centre y | double for rotation |
		
		// compareTo -> Return: "The value 0 if the argument is a string lexicographically equal to this string; a value less than 0 if the argument is a string lexicographically greater than this string; and a value greater than 0 if the argument is a string lexicographically less than this string."
		
		
		// checks 
		int ellipsesCount = 0;
		String[] expectedEllipseLabels = zoneLabels_Venn3;
		int ellPropSepCount=0;
		
		ArrayList<Ellipse> ellsInDiag = new ArrayList<Ellipse>(3); //3 is just an initial capacity which will grow automatically as soon as the arraylist is full and add is invoked
		
		try {
			BufferedReader b = new BufferedReader(new FileReader(file));
			Character c = new Character(FILESEPARATOR);
			String separatorString = new String(c.toString());

			boolean readingAbstractDescription = false;
			boolean readingEllipses = false;
			String line = b.readLine();
			
			int separatorInd;
			double[] prop = new double[5];
			int i;
			
			while(line != null) {
				if(line.equals("")) {
					line = b.readLine();
					continue;
				}
				if(readingAbstractDescription && line.compareTo(FILESTARTABSTRACTDESCRIPTION)!=0 && !line.startsWith(FILESTARTELLIPSES)) {
				}
				if(readingEllipses && !line.startsWith(FILESTARTELLIPSES) ){
					StringBuffer parseLine = new StringBuffer(line);
					separatorInd = 0;
					ellPropSepCount=0;
					
					// get ellipse label
					separatorInd = parseLine.indexOf(separatorString);
					if (separatorInd < 0){
						return null;}
					ellPropSepCount++;
					String label = parseLine.substring(0,separatorInd);
					if (!label.equals(expectedEllipseLabels[0]) && !label.equals(expectedEllipseLabels[1]) && !label.equals(expectedEllipseLabels[2])){ 
						return null;
					}
					parseLine.delete(0,separatorInd+1);
					i = 0;

					
					while(parseLine.length()!=0){
						separatorInd = parseLine.indexOf(separatorString);
						if (separatorInd<0){
							break;}
						ellPropSepCount++;
						try{
							prop[i] = Double.parseDouble(parseLine.substring(0,separatorInd));
						} catch (NumberFormatException e){
							return null;
						}
						parseLine.delete(0,separatorInd+1);
						i++;
					}
					if (ellPropSepCount!=6){
						return null;
					}
					
					
					
					ellsInDiag.add(new Ellipse(label,prop[0],prop[1],prop[2],prop[3],prop[4]));
					
					ellipsesCount++;
				}
				if(line.compareTo(FILESTARTABSTRACTDESCRIPTION)==0) {
					readingAbstractDescription = true;
					readingEllipses = false;
				}
				if((line.startsWith(FILESTARTELLIPSES))){
					readingEllipses = true;
				}
				line = b.readLine();
			}
			b.close();
		} catch(IOException e){
			System.out.println("EllipseDiagramOps.loadDiagFromFile: An IO exception occured with file "+file+": "+e+"\n");
			System.exit(1);
		}
		
		if ((ellsInDiag.size()!=3) || (ellipsesCount!=3)){ 
			return null;
		}
		
		return (new EllipseDiagram (ellsInDiag, considerLessPrecisePossibleIntPnts));
	}
	
	
	
	
	public static EllipseDiagram loadLWDiagFromFile (File file, boolean considerLessPrecisePossibleIntPnts){
		
		ArrayList<Ellipse> ellsInDiag = new ArrayList<Ellipse>(3); //3 is just an initial capacity which will grow automatically as soon as the arraylist is full and add is invoked
		
		try {
			BufferedReader b = new BufferedReader(new FileReader(file));
			Character c = new Character(FILESEPARATOR);
			String separatorString = new String(c.toString());

			boolean readingAbstractDescription = false;
			boolean readingEllipses = false;
			String line = b.readLine();
			
			int separatorInd;
			double[] prop = new double[3];
			int i;
			
			while(line != null) {
				if(line.equals("")) {
					line = b.readLine();
					continue;
				}
				if(readingAbstractDescription && line.compareTo(FILESTARTABSTRACTDESCRIPTION)!=0 && line.compareTo(FILESTARTELLIPSES)!=0) {
				}
				if(readingEllipses && !line.contains("CIRCLES")){
					StringBuffer parseLine = new StringBuffer(line);
					separatorInd = 0;

					// get ellipse label
					separatorInd = parseLine.indexOf(separatorString);
					String label = parseLine.substring(0,separatorInd);
					parseLine.delete(0,separatorInd+1);

					//get properties
					i = 0;
					for (i=0;i<3;i++){
						separatorInd = parseLine.indexOf(separatorString);
						prop[i] = Double.parseDouble(parseLine.substring(0,separatorInd));
						parseLine.delete(0,separatorInd+1);
					}

					ellsInDiag.add(new Ellipse(label,prop[0],prop[0],prop[1],prop[2],0.0));
				}
				if(line.compareTo(FILESTARTABSTRACTDESCRIPTION)==0) {
					readingAbstractDescription = true;
					readingEllipses = false;
				}
				if(line.contains("CIRCLES")) {
					readingEllipses = true;
				}
				line = b.readLine();
			}
			b.close();
		} catch(IOException e){
			System.out.println("EllipseDiagramOps.loadLWDiagFromFile: An IO exception occured when executing loadAdjacencyFile("+file+") in SimpleConcreteDiagram.java: "+e+"\n");
			System.exit(1);
		}
		
		return (new EllipseDiagram (ellsInDiag, considerLessPrecisePossibleIntPnts));
	}
	
	
	public static String getEllipseDetailsForFile(Ellipse e){
		if (e == null){return null;}
		return (e.label + FILESEPARATOR + e.getA() + FILESEPARATOR + e.getB() + FILESEPARATOR + e.getXc() + FILESEPARATOR + e.getYc() + FILESEPARATOR + e.getRot() + FILESEPARATOR);
	}
	
	
	public static boolean saveDiagToFile(EllipseDiagram diagram, File file) {

		ArrayList<ConcreteContour> es_Contours = diagram.getEllipsesAsContours();
		ArrayList<Ellipse> es = diagram.getEllipses();
		
		try {
			BufferedWriter b = new BufferedWriter(new FileWriter(file));
			b.append(FILESTARTDIAGRAM);
			b.newLine();
			b.newLine();
			b.append(FILESTARTABSTRACTDESCRIPTION);
			b.newLine();
			b.append(ConcreteContour.generateAbstractDiagramFromList(es_Contours));
			b.newLine();
			b.newLine();
			b.append(FILESTARTELLIPSES+FILEELLIPSESPROPS);
			b.newLine();
			for (Ellipse e : es){
				b.append(getEllipseDetailsForFile(e));
				b.newLine();
			}
			b.close();
		}
		catch(IOException e) {
			System.out.println("EllipseDiagramOps.saveDiagToFile: An IO exception occured -> " + e);
			return false;
		}

		return true;
	}

	
	
	public static boolean saveDiagImgToFile (EllipseDiagramPanel diagPanel, File file){
		try {
			BufferedImage image = new BufferedImage(diagPanel.getWidth(), diagPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
			diagPanel.paint(image.getGraphics());
			ImageIO.write(image,"png",file);
		} catch (Exception e){
			System.out.println("EllipseDiagramOps.saveDiagImgToFile: IO exception -> error message = " + e);
			return false;
		}
		return true;
	}

	public static boolean saveDiagToSVGFile (EllipseDiagramPanel diagPanel, File file){
		// using org.apache.batik http://xmlgraphics.apache.org/batik/
		
		// get a DOMImplementation
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // create an instance of org.w3c.dom.Document
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // draw
        diagPanel.paint(svgGenerator);

        // Stream out SVG to  standard output using UTF-8 encoding
        boolean useCSS = true; // we want to use CSS style attributes
        Writer out;
        try {
        	BufferedWriter b = new BufferedWriter(new FileWriter(file));
        	
        	out = new OutputStreamWriter(System.out, "UTF-8");
			svgGenerator.stream(b, useCSS);
			
			b.newLine();

        } catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return false;
		} catch (SVGGraphics2DIOException e) {
			e.printStackTrace();
			return false;
	    } catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	    return true;
	}
	
	
	
	public static boolean saveDiagToSVGFile_0 (EllipseDiagramPanel diagPanel, File file){		
	    try {
            BufferedWriter b = new BufferedWriter(new FileWriter(file));

            String diagramSVG = generateDiagSVG(diagPanel);

            b.write(diagramSVG);
            b.newLine();

            b.close();
	    }
	    catch(Exception e) {
            System.out.println("EllipseDiagramOps.saveDiagToSVGFile: IO exception -> error message = " + e);
            return false;
	    }
	    return true;
	}
	
	
	private static String generateDiagSVG(EllipseDiagramPanel diagPanel) {
		int ellEdgeThickness = 5;
		
        StringBuffer ret = new StringBuffer();
        ret.append("<?xml version=\"1.0\" standalone=\"no\"?>\n");
        ret.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\n");
        ret.append("\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");

        ret.append("<svg width=\"100%\" height=\"100%\" version=\"1.1\"\n");
        ret.append("xmlns=\"http://www.w3.org/2000/svg\">\n\n");
         
         
        ArrayList<Ellipse> diagElls = diagPanel.diagram.getEllipses();
        String eSVG = "";
        int i = 0; 
        for (Ellipse e : diagElls){
        	eSVG = "<ellipse cx=" + ((char)34) + e.xc + ((char)34) + 
        				   " cy=" + ((char)34) + e.yc + ((char)34) +
                           " rx=" + ((char)34) + e.a  + ((char)34) +
                           " ry=" + ((char)34) + e.b  + ((char)34) +
                           " stroke=" + ((char)34) + (diagPanel.useColor ? "fill:rgb("+diagPanel.ellipseColors[i].getRed()+","+diagPanel.ellipseColors[i].getGreen()+","+diagPanel.ellipseColors[i].getBlue()+") " : "fill:none") + ((char)34) +
                           " stroke-width=" + ((char)34) + ellEdgeThickness + ((char)34) +
                           " style=" + ((char)34) + (diagPanel.useColor ? "fill:rgb("+diagPanel.ellipseColors[i].getRed()+","+diagPanel.ellipseColors[i].getGreen()+","+diagPanel.ellipseColors[i].getBlue()+") " : "fill:none") + ((char)34) +
                           " />";
            ret.append(eSVG);
            ret.append("\n");
            i++;
        }
        ret.append("\n");
        ret.append("</svg>\n");

        return ret.toString();
    }
	
	
	
	// ... ... handling area specifications for diagram
	public static boolean saveAreaspecOfDiagsInDir(String diagsDirPath, String areaspecListDirPath, String areaspecListFileName, boolean considerLessPrecisePossibleIntPnts, int noOfEllsPerDiag){
		return saveAreaspecOfDiagsInDir(diagsDirPath, areaspecListDirPath, areaspecListFileName, null, null, considerLessPrecisePossibleIntPnts, noOfEllsPerDiag);
	}
	
	
	public static boolean saveAreaspecOfDiagsInDir(String diagsDirPath, String areaspecListDirPath, String areaspecListFileName, String startFromDiagNo, String endWithDiagNo, boolean considerLessPrecisePossibleIntPnts, int noOfEllsPerDiag){

		// check if the file exists 
		if (areaspecListDirPath.endsWith(File.separator)){
			areaspecListDirPath = areaspecListDirPath.substring(0,diagsDirPath.lastIndexOf(File.separator));
		}
		File areaspecListDirFile = new File(areaspecListDirPath);
		if (!areaspecListDirFile.exists()){
			Utilities.createDir(areaspecListDirPath); 
		}
		areaspecListFileName += "_randomAreaspecsList";
		
		// compute area specs 
		HashMap<String, HashMap<String, Double>> areaspecOfDiagsInDir = computeAreaspecOfDiagsInDir(diagsDirPath, startFromDiagNo, endWithDiagNo, considerLessPrecisePossibleIntPnts);
		if (areaspecOfDiagsInDir == null){return false;}
		
		// save to file
		return saveAreaSpecsToFile(areaspecOfDiagsInDir, areaspecListDirPath, areaspecListFileName, noOfEllsPerDiag);
	} 
	
	public static HashMap<String, HashMap<String, Double>> computeAreaspecOfDiagsInDir(String diagsDirPath, String startFromDiagNo, String endWithDiagNo, boolean considerLessPrecisePossibleIntPnts){
		
		// check if the file exists 
		
		if (diagsDirPath.endsWith(File.separator)){
			diagsDirPath = diagsDirPath.substring(0,diagsDirPath.lastIndexOf(File.separator));
		}
		
		File diagDirFile = new File(diagsDirPath);
		if (!diagDirFile.exists()){
			System.out.println("EllispeDiagramOps.computeAreaSpecOfDiagsInDir: Directory with diagram files, "+diagsDirPath+ ", does not exist.");
			return null;
		}
		
		
		// get diagram file names in directory
		File[] diagFilesInDir = EllipseDiagramOps.getDiagFilesInDir(new File(diagsDirPath));
		
		//filter to start from a specific diag
		ArrayList<File> diagFilesInDir_filtered = new ArrayList<File>();
		
		boolean copyFile = false;
		boolean foundStart = false;
		if ((startFromDiagNo == null)||(startFromDiagNo.trim().equals(""))){
			foundStart = true;
			copyFile = true;
		}
		for (File df : diagFilesInDir){
			if (!foundStart){
				if (df.getName().contains(startFromDiagNo)){
					foundStart = true;
					copyFile = true;
				}
			}
			if (copyFile){
				diagFilesInDir_filtered.add(df);
			}
			if ((endWithDiagNo != null)&&(!endWithDiagNo.trim().equals(""))){
				if (df.getName().contains(endWithDiagNo)){
					break;
				}
			}
		}
		
		
		// load area spec and save to file 
		HashMap<String, Double> areaspecOfDiagInFile;
		HashMap<String, HashMap<String, Double>> areaspecOfDiagsInDir = new HashMap<String, HashMap<String,Double>>();
		String areaspecNameForDiag;
		String diagFileName;
		for (File diagFile : diagFilesInDir_filtered){
			areaspecOfDiagInFile = computeAreaSpecOfDiagInFile(diagFile, considerLessPrecisePossibleIntPnts);
			diagFileName = diagFile.getName();
			areaspecNameForDiag = diagFileName.substring(0, diagFileName.lastIndexOf(".eld"));
			areaspecOfDiagsInDir.put(areaspecNameForDiag, areaspecOfDiagInFile);
		}
		
		return areaspecOfDiagsInDir;
	}
	
	
	public static HashMap<String, Double> computeAreaSpecOfDiagInFile (File diagFile, boolean considerLessPrecisePossibleIntPnts){
		EllipseDiagram diag = loadDiagFromFile(diagFile, considerLessPrecisePossibleIntPnts);
		if (diag == null){
			return null;
		}
				
		if (diag.isValid()){
			// disable the next line of code, if the above validation chunk of code is enabled
			return (diag.getZoneAreas());
		} else {
			return null;
		}
	}
	
	public static boolean saveAreaSpecsToFile (HashMap<String, HashMap<String, Double>> areaspecs, String areaspecsDirPath, String areaspecsFileName, int noOfElls){
	// none of the paths should end with "\\"
		if (areaspecsDirPath.endsWith(File.separator)){
			areaspecsDirPath = areaspecsDirPath.substring(0,areaspecsDirPath.lastIndexOf(File.separator));
		}
		
		File diagLibFile = new File(areaspecsDirPath);
		if (!diagLibFile.exists()){
			Utilities.createDir(areaspecsDirPath);
		}
	
		Set<String> areaspecNames;
		HashMap<String, Double> areaspecZoneAreas;
		String areaspecsStr = "";
		
		int noOfZones = (int)(Math.pow(2,noOfElls)) - 1;
		String[] zoneNames = EllipseDiagramOps.getZoneLabels(noOfElls, false);
		int z=0;
		
		
		try{
			BufferedWriter b = new BufferedWriter(new FileWriter(areaspecsDirPath+File.separator+areaspecsFileName+".eldl"));
			b.append("//a | b | c | ab | ac | bc | abc : diagram file name"); b.newLine();
		
			areaspecNames = new TreeSet<String>(areaspecs.keySet());  //need TreeSet to sort the area specs in ascending by their name as eg _0, _1, _10, _100 etc 
			for (String areaspecName : areaspecNames){
			
				areaspecZoneAreas = areaspecs.get(areaspecName);
				
				z=0;
				areaspecsStr="";
				for (String zoneName : zoneNames){
					areaspecsStr += areaspecZoneAreas.get(zoneName);
					if (z < (noOfZones-1)){ areaspecsStr += " | "; }
					z++;
				}
		        areaspecsStr += (" : " + areaspecName);	
				b.append(areaspecsStr); b.newLine();
				System.out.println("***"+areaspecsStr);
			}
			b.close();
		} 
		catch(IOException e) {
			System.out.println("EllipseDiagramOps.saveAreaSpecsToFile: An IO exception occured -> " + e);
			return false;
		}
		
		return true;
	}
	
	
	private static class AreaSpecNamesComparator implements Comparator{
		//the index number is just after the final _ so this comparator gets that number and compares the area spec indices
		//to sort them out in descending order
        public int compare(Object obj1,Object obj2)
        {
        	String areaspecname1 = obj1.toString();
        	String areaspecname2 = obj2.toString();
        	Integer areaspec1_index = Integer.parseInt(areaspecname1.substring(areaspecname1.lastIndexOf('_')+1, areaspecname1.length()));
        	Integer areaspec2_index = Integer.parseInt(areaspecname2.substring(areaspecname2.lastIndexOf('_')+1, areaspecname2.length()));
            return areaspec1_index.compareTo(areaspec2_index);
        }
    }
	public static SortedMap<String, HashMap<String, Double>> loadAreaSpecsLibFromFile_venn3 (File areaSpecsListFile){
		SortedMap<String, HashMap<String, Double>> areaspecLib = new TreeMap<String, HashMap<String, Double>>(new AreaSpecNamesComparator());

		String areaSpecsName = "";
		String areaSpecsStr = ""; 
		try{
			BufferedReader b = new BufferedReader(new FileReader(areaSpecsListFile));
			String line = b.readLine().trim();
			
			int specsLabelSepIndex;
			String[] zoneLabels = zoneLabels_Venn3;
			StringBuffer areaSpecsStrParse = new StringBuffer("");
			int i; int z;
			String areaStr;
			int sepIndex;
			HashMap<String, Double> areaspec; 
			
			i=0;
			while(line != null) {
				if(!line.equals("") && !line.startsWith("//")) {
					specsLabelSepIndex = line.indexOf(':');
					areaSpecsName = line.substring (specsLabelSepIndex+1,line.length()).trim();
					areaSpecsStr = line.substring (0, specsLabelSepIndex).trim();
					areaSpecsStrParse = new StringBuffer(areaSpecsStr);
					
					// extract the areas of every zone
					areaspec = new HashMap<String, Double>();
					for (z = 0; z < zoneLabels.length; z++){
						areaStr = "";
						
						if (z < (zoneLabels.length-1)){
							sepIndex = areaSpecsStrParse.indexOf("|");
							areaStr = areaSpecsStrParse.substring(0,sepIndex).trim();
							areaSpecsStrParse.delete(0,sepIndex+1);
						} else {
							areaStr = areaSpecsStrParse.toString().trim();
						}
					
						areaspec.put(zoneLabels[z], Double.parseDouble(areaStr));	
					}
					areaspecLib.put(areaSpecsName, areaspec); 
					i++;
				}
				line = b.readLine();
			}
			b.close();
			
		} catch(IOException e){
			System.out.println("EllipseDiagramOps.loadAreaSpecsFromFile_venn3: An IO exception occured when executing loadAreaSpecsFromFile("+areaSpecsListFile.getAbsolutePath()+") in EllipseDiagramOps: "+e+"\n");
			System.exit(1);
		} 
		return ((areaspecLib.size()<=0) ? null : areaspecLib);
	}
	
	
	
	public static HashMap<String, Double> loadAreaSpecsFromFile_venn3 (File areaSpecsFile){
		// file should have just one -> if have more than only the details of the first are loaded
		HashMap<String, Double> areaSpecs = new HashMap<String, Double>();
		
		// try to find the area specs of the diagram in the list provided in file
		String areaSpecsStr = ""; 
		boolean found = false;
		try{
			BufferedReader b = new BufferedReader(new FileReader(areaSpecsFile));
			String line = b.readLine().trim();
			
			int specsLabelSepIndex;
			String diagName;
			
			while(line != null) {
				if(!line.equals("") && !line.startsWith(File.separator)) {

					areaSpecsStr = line.trim();
					
					// extract the areas of every zone 
					String[] zoneLabels = zoneLabels_Venn3;
					StringBuffer areaSpecsStrParse = new StringBuffer(areaSpecsStr);

					int i;
					String areaStr;
					int sepIndex;
					for (i = 0; i < zoneLabels.length; i++){
						areaStr = "";
						
						// no | at the end of the line
						if (i < (zoneLabels.length-1)){
							sepIndex = areaSpecsStrParse.indexOf("|");
							areaStr = areaSpecsStrParse.substring(0,sepIndex).trim();
							areaSpecsStrParse.delete(0,sepIndex+1);
						} else {
							areaStr = areaSpecsStrParse.toString().trim();
						}
						
						areaStr=Utilities.changeLocaleDecimalSeparatorToDefault(areaStr);
											
						try{
							areaSpecs.put(zoneLabels[i], Double.parseDouble(areaStr));
							
						}catch(NumberFormatException e){
							return null;
						}
					}
					return ((areaSpecs.size()!=7) ? null : areaSpecs); 
				}
				line = b.readLine();
			}
			b.close();
			
		} catch(IOException e){
			System.out.println("EllipseDiagramOps.loadAreaSpecsFromFile_venn3: An IO exception occured with file "+areaSpecsFile.getAbsolutePath()+": "+e+"\n");
			System.exit(1);
		}

		return null;
	}
	
	public static HashMap<String, Double> loadAreaSpecsFromFile_venn3 (File areaSpecsListFile, String reqDiagName){
		HashMap<String, Double> areaSpecs = new HashMap<String, Double>();
		
		// try to find the area specs of the diagram in the list provided in file
		String areaSpecsStr = ""; 
		boolean found = false;
		try{
			BufferedReader b = new BufferedReader(new FileReader(areaSpecsListFile));
			String line = b.readLine().trim();
			
			int specsLabelSepIndex;
			String diagName;
			
			while(line != null) {
				if(!line.equals("") && !line.startsWith(File.separator)) {
					//int specsLabelSepIndex = line.indexOf(':');
					specsLabelSepIndex = line.indexOf(':');
					areaSpecsStr = line.substring (0, specsLabelSepIndex).trim();
					//String diagName = line.substring(specsLabelSepIndex+1).trim();
					diagName = line.substring(specsLabelSepIndex+1).trim();
					if (diagName.equals(reqDiagName)){ 
						found = true;
						break;
					}
				}
				line = b.readLine();
			}
			b.close();
			
		} catch(IOException e){
			System.out.println("EllipseDiagramOps.loadAreaSpecsFromFile_venn3: An IO exception occured when executing loadAreaSpecsFromFile("+areaSpecsListFile.getAbsolutePath()+") in EllipseDiagramOps: "+e+"\n");
			System.exit(1);
		}
		
		if (!found){return null;}
		
		
		// extract the areas of every zone 
		String[] zoneLabels = zoneLabels_Venn3;
		StringBuffer areaSpecsStrParse = new StringBuffer(areaSpecsStr);

		int i;
		String areaStr;
		int sepIndex;
		for (i = 0; i < zoneLabels.length; i++){
			areaStr = "";
			
			if (i < (zoneLabels.length-1)){
				sepIndex = areaSpecsStrParse.indexOf("|");
				areaStr = areaSpecsStrParse.substring(0,sepIndex).trim();
				areaSpecsStrParse.delete(0,sepIndex+1);
			} else {
				areaStr = areaSpecsStrParse.toString().trim();
			}
		
			areaSpecs.put(zoneLabels[i], Double.parseDouble(areaStr));	
		}
		
		return ((areaSpecs.size()<=0) ? null : areaSpecs);
	}
	
	
	
	
	// ... random diagram

	public static EllipseDiagram generateARandomDiagram (int noOfElls, double a_b_min, double a_b_max, Point2D.Double axisRange_min, Point2D.Double axisRange_max, boolean considerLessPrecisePossibleIntPnts, Random random){
		return generateARandomDiagram (noOfElls, a_b_min, a_b_max, axisRange_min, axisRange_max, false, considerLessPrecisePossibleIntPnts, random);
	}
	public static EllipseDiagram generateARandomDiagram (int noOfElls, double a_b_min, double a_b_max, Point2D.Double axisRange_min, Point2D.Double axisRange_max, boolean restrictToCircles, boolean considerLessPrecisePossibleIntPnts, Random random){
		ArrayList<Ellipse> ellipsesInDiag = new ArrayList<Ellipse>(noOfElls);
		
		String ellipseLabel;
		for (int i = 0; i< noOfElls; i++){
			ellipseLabel = Character.toString((char)(((int)EllipseDiagram.ellipse1Label) + i));
			ellipsesInDiag.add(Ellipse.generateARandomEllipse(ellipseLabel, a_b_min, a_b_max, axisRange_min, axisRange_max, restrictToCircles, random));
		}
		return (new EllipseDiagram (ellipsesInDiag, considerLessPrecisePossibleIntPnts));
	}
	
	public static EllipseDiagram generateAValidRandomDiagram (int noOfElls, double a_b_min, double a_b_max, Point2D.Double axisRange_min, Point2D.Double axisRange_max, boolean checkValidity, boolean considerLessPrecisePossibleIntPnts, Random random){
		return generateAValidRandomDiagram (noOfElls, a_b_min, a_b_max, axisRange_min, axisRange_max, false, checkValidity, false, false, null, considerLessPrecisePossibleIntPnts, random);	
	}
	
	public static EllipseDiagram generateAValidRandomDiagram (int noOfElls, double a_b_min, double a_b_max, Point2D.Double axisRange_min, Point2D.Double axisRange_max, boolean checkValidity, boolean disallowDiagsWithCloseToEmptyZones, boolean disallowDiagsWithCloseToDisconnectedZones, Double minPolyAreaInZone, boolean considerLessPrecisePossibleIntPnts, Random random){
		return generateAValidRandomDiagram (noOfElls, a_b_min, a_b_max, axisRange_min, axisRange_max, false, checkValidity, disallowDiagsWithCloseToEmptyZones, disallowDiagsWithCloseToDisconnectedZones, minPolyAreaInZone, considerLessPrecisePossibleIntPnts, random);	
	}
	
	public static EllipseDiagram generateAValidRandomDiagram (int noOfElls, double a_b_min, double a_b_max, Point2D.Double axisRange_min, Point2D.Double axisRange_max, boolean restrictToCircles, boolean checkValidity, boolean considerLessPrecisePossibleIntPnts, Random random){
		return generateAValidRandomDiagram (noOfElls, a_b_min, a_b_max, axisRange_min, axisRange_max, restrictToCircles, checkValidity, false, false, null, considerLessPrecisePossibleIntPnts, random);
	}
	
	public static EllipseDiagram generateAValidRandomDiagram (int noOfElls, double a_b_min, double a_b_max, Point2D.Double axisRange_min, Point2D.Double axisRange_max, 
														      boolean restrictToCircles, boolean checkValidity, boolean disallowDiagsWithCloseToEmptyZones, boolean disallowDiagsWithCloseToDisconnectedZones, Double minPolyAreaInZone, boolean considerLessPrecisePossibleIntPnts, Random random){
		// it must be valid to disallow emptylike and disconnected like zones using polygons or else will have some difficulties to find the concrete contours
		
		EllipseDiagram randomDiag;
		boolean diagIsValid;
		DiagramZoneCondition diagZoneCondition_analytic = DiagramZoneCondition.NOTEMPTYLIKE_CONNECTED;
		DiagramZoneCondition diagZoneCondition_approx = DiagramZoneCondition.NOTEMPTYLIKE_CONNECTED; 
		
		HashMap<String, Double> zoneAreas;
		Iterator itrZAs;
		Entry<String, Double> entryZA;
		Double za;
		
		do {
			
			diagZoneCondition_analytic = DiagramZoneCondition.NOTEMPTYLIKE_CONNECTED;
			diagZoneCondition_approx = DiagramZoneCondition.NOTEMPTYLIKE_CONNECTED;
			
			randomDiag = generateARandomDiagram(noOfElls, a_b_min, a_b_max, axisRange_min, axisRange_max, restrictToCircles, considerLessPrecisePossibleIntPnts, random);
			
			diagIsValid = randomDiag.isValid();
			
			if (diagIsValid){
			
				if (disallowDiagsWithCloseToEmptyZones){
					zoneAreas = randomDiag.getZoneAreas();
					itrZAs = zoneAreas.entrySet().iterator();
					while (itrZAs.hasNext()){
						entryZA = (Entry<String, Double>)itrZAs.next();
						za = entryZA.getValue();
						if ((za == null) || (za < minPolyAreaInZone)){
							diagZoneCondition_analytic = DiagramZoneCondition.EMPTY_LIKE;
							break;
						}
					}
				}
				
				if ( (disallowDiagsWithCloseToEmptyZones || disallowDiagsWithCloseToDisconnectedZones) && (minPolyAreaInZone != null)){
					diagZoneCondition_approx = TestingWithPolygons.doesDiagramContainEmptyLikeOrDisconnectedZones(randomDiag, minPolyAreaInZone.doubleValue());
				}
			}
			
		/* Tested this using the different values for the flags disallowDiagsWithCloseToEmptyZones and disallowDiagsWithCloseToDisconnectedZones, in the method Testing.generateRandomDiagLibrary()
		 * If any one of the subconditions in every line is false, then the condition is true and the loop will iterate again.
		 * Each subcondition has the following form: considering example the find validity subcondition:
		 * 			(checkValidity && (randomDiag != null)) => diagIsValid
		 * but cannot use => here so considering that   A => B   if A = true, B must be true   and   if A = false, B can be anything  for it to be true then the above subcondition can be expressed as 
		 * 			(checkValidity && (randomDiag != null) && diagIsValid) || !(checkValidity && (randomDiag != null)) 
		 */
		} while ( !( (checkValidity && (randomDiag != null) && diagIsValid) || !(checkValidity && (randomDiag != null)) ) ||  //randomDiag.isValid() ) ||
				  !( (disallowDiagsWithCloseToEmptyZones && (minPolyAreaInZone != null) && (diagZoneCondition_analytic != DiagramZoneCondition.EMPTY_LIKE) && (diagZoneCondition_approx != DiagramZoneCondition.EMPTY_LIKE)) || !(disallowDiagsWithCloseToEmptyZones && (minPolyAreaInZone != null)) ) || 
				  !( (disallowDiagsWithCloseToDisconnectedZones && (minPolyAreaInZone != null) && (diagZoneCondition_approx != DiagramZoneCondition.DISCONNECTED_LIKE)) || !(disallowDiagsWithCloseToDisconnectedZones && (minPolyAreaInZone != null)) ) );			
        
		// no need to check diagram.zoneAreasComputedSuccessfully because it is checked in isValid()

		return randomDiag;
	}
	
	
	public static boolean generateAValidRandomDiagramLib (int noOfElls, double a_b_min, double a_b_max, 
														  Point2D.Double axisRange_min, Point2D.Double axisRange_max, 
														  boolean restrictToCircles, boolean checkValidity, 
														  String diagLibPath, int noOfDiags, EllipseDiagramPanel diagPanel, boolean considerLessPrecisePossibleIntPnts){
		
		return generateAValidRandomDiagramLib (noOfElls, a_b_min, a_b_max, axisRange_min, axisRange_max, restrictToCircles, checkValidity, diagLibPath, noOfDiags, diagPanel, false, false, null, considerLessPrecisePossibleIntPnts);
	}
	
	
	public static boolean generateAValidRandomDiagramLib (int noOfElls, double a_b_min, double a_b_max, 
			  											  Point2D.Double axisRange_min, Point2D.Double axisRange_max, 
			  											  boolean restrictToCircles, boolean checkValidity, 
			  											  String diagLibPath, int noOfDiags, EllipseDiagramPanel diagPanel,
			  											  boolean disallowDiagsWithCloseToEmptyZones, boolean disallowDiagsWithCloseToDisconnectedZones, 
			  											  Double minPolyAreaInZone, boolean considerLessPrecisePossibleIntPnts){
		
		// none of the paths should end with "\\"
		if (diagLibPath.endsWith(File.separator)){
			diagLibPath = diagLibPath.substring(0,diagLibPath.lastIndexOf(File.separator));
		}

		File diagLibFile = new File(diagLibPath);
		if (!diagLibFile.exists()){
			Utilities.createDir(diagLibPath);
		}
		
		String eldDirName = "eld";
		String pngDirName = "png";
		String svgDirName = "svg";
		
	    String eldDirPath = diagLibPath+File.separator+eldDirName;
		String pngDirPath = diagLibPath+File.separator+pngDirName;
		String svgDirPath = diagLibPath+File.separator+svgDirName; 
		
		File eldDirFile = new File(eldDirPath);
		File pngDirFile = new File(pngDirPath);
		File svgDirFile = new File(svgDirPath);
		if (!eldDirFile.exists()){
			Utilities.createDir(eldDirPath);
		}
		if (!pngDirFile.exists()){
			Utilities.createDir(pngDirPath);
		}
		if (!svgDirFile.exists()){
			Utilities.createDir(svgDirPath);
		}
		
		
		String dateTime = Utilities.getCurrentDateTime();
		String diagFileName;
		EllipseDiagram randomValidDiag = null;
		int i=0;
		Random random = new Random();
		
		for (i=0; i<noOfDiags; i++){
			randomValidDiag = generateAValidRandomDiagram(noOfElls, a_b_min, a_b_max, axisRange_min, axisRange_max,restrictToCircles, checkValidity, disallowDiagsWithCloseToEmptyZones, disallowDiagsWithCloseToDisconnectedZones, minPolyAreaInZone, considerLessPrecisePossibleIntPnts, random);
			diagPanel.setDiagram(randomValidDiag);
			// update diag panel with the new diag before you save the diag to file
			diagFileName = dateTime+"_"+i;
			saveDiagToFile (randomValidDiag, new File (eldDirPath+File.separator+diagFileName+".eld"));
			saveDiagImgToFile(diagPanel, new File (pngDirPath+File.separator+diagFileName+".png"));
			saveDiagToSVGFile(diagPanel, new File (svgDirPath+File.separator+diagFileName+".svg"));
		}
		
		return true;
		
	}
	

	public static EllipseDiagram getARandomDiagFromLib(File diagLibPath, boolean considerLessPrecisePossibleIntPnts){
		
		if (!diagLibPath.exists()){
			System.out.println ("EllipseDiagramOps.getARandomDiagFromLib: directory " + diagLibPath.getAbsolutePath() + " does not exist");
			return null;
		}
		if (!diagLibPath.isDirectory()){
			System.out.println ("EllipseDiagramOps.getARandomDiagFromLib: " + diagLibPath.getAbsolutePath() + " is not a directory");
			return null;
		}
		
		File[] diagFilesInLib = getDiagFilesInDir(diagLibPath);
		int randomIndex = Utilities.randomIntegerInRange(0, diagFilesInLib.length-1);
		File randomDiagFile = diagFilesInLib[randomIndex];
		
		return (loadDiagFromFile(randomDiagFile, considerLessPrecisePossibleIntPnts));		
	}
	
	
	public static double[] generateARandomAreaSpecs(int noOfElls, double zonearea_min, double zonearea_max, double multiplyBy, Random random, int noOfDPs){
		
		int noOfZones = (int)(Math.pow(2,noOfElls)) - 1; //-1 to eliminate the empty zone
		double[] areaspecs = new double[noOfZones];
		for (int i=0; i<noOfZones; i++){
			areaspecs[i] = Utilities.roundToDps((Utilities.randomNumberInRange(zonearea_min, zonearea_max, random)*multiplyBy), noOfDPs);
		}
		return areaspecs;
	}
	
	
	public static boolean generateRandomAreaSpecsLib (int noOfElls, double zonearea_min, double zonearea_max, String areaspecsLibDirPath, int noOfDiags, double multiplyBy, int noOfDPs){
		// none of the paths should end with "\\"
		if (areaspecsLibDirPath.endsWith(File.separator)){
			areaspecsLibDirPath = areaspecsLibDirPath.substring(0,areaspecsLibDirPath.lastIndexOf(File.separator));
		}
		
		File diagLibFile = new File(areaspecsLibDirPath);
		if (!diagLibFile.exists()){
			Utilities.createDir(areaspecsLibDirPath);
		}
		
		String dateTime = Utilities.getCurrentDateTime();

		String areaspecFileName = dateTime+"_randomAreaspecsList";

		int noOfZones = (int)(Math.pow(2,noOfElls)) - 1;
		String[] zoneNames = EllipseDiagramOps.getZoneLabels(noOfElls, false);
		
		double[] randomAreaspecZoneAreas = new double[noOfZones];
		String randomAreaspecName;
		HashMap<String, Double> randomAreaspecZoneAreasLabelled;
		HashMap<String, HashMap<String, Double>> areaspecs = new HashMap<String, HashMap<String, Double>>();
		
		int z=0;
		Random random;
		for (int i=0; i<noOfDiags; i++){
			randomAreaspecName = dateTime+"_"+i;
			random = new Random();
			randomAreaspecZoneAreas = generateARandomAreaSpecs(noOfElls, zonearea_min, zonearea_max, multiplyBy, random, noOfDPs);
			
			z=0;
			randomAreaspecZoneAreasLabelled = new HashMap<String, Double>();
			for (String zoneName : zoneNames){
				randomAreaspecZoneAreasLabelled. put(zoneName, randomAreaspecZoneAreas[z]);
				z++;
			}
			areaspecs.put(randomAreaspecName, randomAreaspecZoneAreasLabelled);
		}
		
		return saveAreaSpecsToFile(areaspecs, areaspecsLibDirPath, areaspecFileName, noOfElls);
	}
		
	
	
	// ... circle diagram represented using ellipses 
	
	public static boolean isCircleDiagram (EllipseDiagram diagram){
		// returns true only if ALL the ellipses are circles (that is semi-major axis = semi-minor axis)
		
		ArrayList<Ellipse> es = diagram.getEllipses();
		
		int noOfCircles = 0;
		
		for (Ellipse e : es){
			if (e.isCircle()){
				noOfCircles++;
			}
		}
		return (noOfCircles == es.size()); 
	}
	
	
	public static enum InitDiagType {BISECTION_ON_Y_THEN_X, BISECTION_ON_SLOPE}; 
	public static EllipseDiagram generateAppropriateInitDiag(double[] circleRots, HashMap<String, Double> requiredAreaSpecs, InitDiagType initDiagType, double diagIsValidPolyAreaDiscardThreshold, boolean considerLessPrecisePossibleIntPnts){
		
		if ((requiredAreaSpecs==null)||(requiredAreaSpecs.size()==0)||
			Double.isNaN(requiredAreaSpecs.get("ab")) || Double.isNaN(requiredAreaSpecs.get("abc"))){
			return null;
		}
		
		String[] ellipseLabels = {"a", "b", "c"};
		int eI;
		
		// Get the areas of the ellipses from the required area specifications of the zones
		double[] ellipseAreas = {0.0,0.0,0.0}; 
	
		Double minZoneArea = null;
		Iterator itrZoneAreas;
		Entry<String, Double> currZoneArea;
		eI=0;
		for (String eL : ellipseLabels){
			itrZoneAreas = requiredAreaSpecs.entrySet().iterator();
			while (itrZoneAreas.hasNext()){
				currZoneArea = (Entry<String, Double>)itrZoneAreas.next();
				
				if ((minZoneArea==null)||(currZoneArea.getValue().doubleValue()<minZoneArea.doubleValue())){
					minZoneArea = currZoneArea.getValue().doubleValue();
				}
				
				if (currZoneArea.getKey().contains(eL) && (currZoneArea.getValue() != null)){
					ellipseAreas[eI] += currZoneArea.getValue();
				}
			}
			eI++;	
		}
		
		switch (initDiagType){
			case BISECTION_ON_Y_THEN_X: {
					// base 2 circles are those with the largest 2-set zone
					// **** it doesn't make a difference whether you add the area of abc or not because you are adding it to all of them => makes no difference then when compare the areas 
					double areaRegionAB = requiredAreaSpecs.get("ab") + requiredAreaSpecs.get("abc");
					double areaRegionBC = requiredAreaSpecs.get("bc") + requiredAreaSpecs.get("abc");
					double areaRegionAC = requiredAreaSpecs.get("ac") + requiredAreaSpecs.get("abc");
					
					// find the largest 2-set zone area in the diagram
					String max2setZone_str = "ab";
					double max2setZone_area = areaRegionAB;
					if (areaRegionAB < areaRegionBC){
						max2setZone_str = "bc";
						max2setZone_area = areaRegionBC; 
					}
					if (max2setZone_area < areaRegionAC){
						max2setZone_str = "ac";
						max2setZone_area = areaRegionAC;
					}
					
					// order the ellipses based on the largest 2-set zone area in the diagram
					if (max2setZone_str.equals("ab")){
						//leave as is
						
					} else if (max2setZone_str.equals("ac")){
						double tempDbl = ellipseAreas[1];
						ellipseAreas[1] = ellipseAreas[2];
						ellipseAreas[2] = tempDbl;
						
						String tempStr = ellipseLabels[1];
						ellipseLabels[1] = ellipseLabels[2];
						ellipseLabels[2] = tempStr;
						
						tempDbl = circleRots[1];
						circleRots[1] = circleRots[2];
						circleRots[2] = tempDbl;
						
					} else if(max2setZone_str.equals("bc")){
						double tempDbl = ellipseAreas[0];
						ellipseAreas[0] = ellipseAreas[1];
						ellipseAreas[1] = tempDbl;
						tempDbl = ellipseAreas[1];
						ellipseAreas[1] = ellipseAreas[2];
						ellipseAreas[2] = tempDbl;
						
						String tempStr = ellipseLabels[0];
						ellipseLabels[0] = ellipseLabels[1];
						ellipseLabels[1] = tempStr;
						tempStr = ellipseLabels[1];
						ellipseLabels[1] = ellipseLabels[2];
						ellipseLabels[2] = tempStr;
						
						tempDbl = circleRots[0];
						circleRots[0] = circleRots[1];
						circleRots[1] = tempDbl;
						tempDbl = circleRots[1];
						circleRots[1] = circleRots[2];
						circleRots[2] = tempDbl;
					}
					break;
				} 
			
			case BISECTION_ON_SLOPE:{
				// base 2 circles are the largest 2 circles in the diagram
				// **** it doesn't make a difference whether you add the area of abc or not because you are adding it to all of them => makes no difference then when compare the areas 
				double areaCircleA = requiredAreaSpecs.get("ab") + requiredAreaSpecs.get("abc") + requiredAreaSpecs.get("a") + requiredAreaSpecs.get("ac");
				double areaCircleB = requiredAreaSpecs.get("bc") + requiredAreaSpecs.get("abc") + requiredAreaSpecs.get("b") + requiredAreaSpecs.get("ab");
				double areaCircleC = requiredAreaSpecs.get("ac") + requiredAreaSpecs.get("abc") + requiredAreaSpecs.get("c") + requiredAreaSpecs.get("bc");
				
				// find the largest 2-set zone area in the diagram -> order not imp		
				String largest2Circles_str = "";
				if (areaCircleA > areaCircleB){
					if (areaCircleB > areaCircleC){
						largest2Circles_str = "ab";
					} else {
						largest2Circles_str = "ac";
					}
				} else {
					if (areaCircleA > areaCircleC){
						largest2Circles_str = "ab"; //actually "ba" but order is not imp here
					} else {
						largest2Circles_str = "bc"; //rem order is not imp => no need to check if b > c
					}
				}
			
				// order all the ellipses according to their area starting off with the one with the largest area -> based on the largest 2 circles in the diagram
				if (largest2Circles_str.equals("ab")){
					//leave the properties with index 2 (that is for c) as is
					
					int largerCircle_index = (Math.max(ellipseAreas[0], ellipseAreas[1]) == ellipseAreas[0]) ? 0 : 1;
					int smallerCircle_index = (Math.min(ellipseAreas[0], ellipseAreas[1]) == ellipseAreas[0]) ? 0 : 1;
					if (ellipseAreas[0]==ellipseAreas[1]){
						largerCircle_index = 0;
						smallerCircle_index = 1;
					}
					
					double[] tempDbl = new double[]{ellipseAreas[0], ellipseAreas[1]};
					
					ellipseAreas[0] = tempDbl[largerCircle_index];
					ellipseAreas[1] = tempDbl[smallerCircle_index];
					
					String[] tempStr = new String[]{ellipseLabels[0], ellipseLabels[1]};
					
					ellipseLabels[0] = tempStr[largerCircle_index];
					ellipseLabels[1] = tempStr[smallerCircle_index];
					
					tempDbl = new double[]{circleRots[0], circleRots[1]};
					
					circleRots[0] = tempDbl[largerCircle_index];
					circleRots[1] = tempDbl[smallerCircle_index];
					
				} else if (largest2Circles_str.equals("ac")){
					int largerCircle_index = (Math.max(ellipseAreas[0], ellipseAreas[2]) == ellipseAreas[0]) ? 0 : 2;
					int smallerCircle_index = (Math.min(ellipseAreas[0], ellipseAreas[2]) == ellipseAreas[0]) ? 0 : 2;
					if (ellipseAreas[0]==ellipseAreas[2]){
						largerCircle_index = 0;
						smallerCircle_index = 2;
					}
					
					double[] tempDbl = new double[]{ellipseAreas[0], ellipseAreas[1], ellipseAreas[2]};
					
					ellipseAreas[0] = tempDbl[largerCircle_index];
					ellipseAreas[1] = tempDbl[smallerCircle_index];
					ellipseAreas[2] = tempDbl[1];
					
					String[] tempStr = new String[]{ellipseLabels[0], ellipseLabels[1], ellipseLabels[2]};
					
					ellipseLabels[0] = tempStr[largerCircle_index];
					ellipseLabels[1] = tempStr[smallerCircle_index];
					ellipseLabels[2] = tempStr[1];
					
					tempDbl = new double[]{circleRots[0], circleRots[1], circleRots[2]};
					
					circleRots[0] = tempDbl[largerCircle_index];
					circleRots[1] = tempDbl[smallerCircle_index];
					circleRots[2] = tempDbl[1];
		
				} else if(largest2Circles_str.equals("bc")){
					int largerCircle_index = (Math.max(ellipseAreas[1], ellipseAreas[2]) == ellipseAreas[1]) ? 1 : 2;
					int smallerCircle_index = (Math.min(ellipseAreas[1], ellipseAreas[2]) == ellipseAreas[1]) ? 1 : 2;
					if (ellipseAreas[1]==ellipseAreas[2]){
						largerCircle_index = 1;
						smallerCircle_index = 2;
					}
					
					double[] tempDbl = new double[]{ellipseAreas[0], ellipseAreas[1], ellipseAreas[2]};
					ellipseAreas[0] = tempDbl[largerCircle_index];
					ellipseAreas[1] = tempDbl[smallerCircle_index];
					ellipseAreas[2] = tempDbl[0];
					
					String[] tempStr = new String[]{ellipseLabels[0], ellipseLabels[1], ellipseLabels[2]};
					ellipseLabels[0] = tempStr[largerCircle_index];
					ellipseLabels[1] = tempStr[smallerCircle_index];
					ellipseLabels[2] = tempStr[0];
					
					tempDbl = new double[]{circleRots[0], circleRots[1], circleRots[2]};
					circleRots[0] = tempDbl[largerCircle_index];
					circleRots[1] = tempDbl[smallerCircle_index];
					circleRots[2] = tempDbl[0];
				}
				break;
			}
		}
		
		// Find the radius of the all circles (which will be represented by ellipses with equal a and b) 
		double[] ellipseRadiuses = {0.0,0.0,0.0};
		for (eI=0; eI < 3; eI++){
			ellipseRadiuses[eI] = Math.sqrt(ellipseAreas[eI]/Math.PI);
		}
		
		
		// Find the centres of all the circles 
		Point2D.Double[] ellipseCentres = new Point2D.Double[3];
		ellipseCentres[0] = new Point2D.Double(0.0,0.0);
		
		String[] ell0_1_labels = new String[]{ellipseLabels[0], ellipseLabels[1]};
		Arrays.sort(ell0_1_labels);
		double reqAreaAB = requiredAreaSpecs.get(ell0_1_labels[0]+ell0_1_labels[1]) + requiredAreaSpecs.get("abc");
	
		Double distBetCentresAandB = getDistBetweenCentresOfAreaPropCircles(ellipseRadiuses[0], ellipseRadiuses[1], ellipseAreas[0], ellipseAreas[1], reqAreaAB, circleRots[0], circleRots[1], considerLessPrecisePossibleIntPnts);
		
		if (distBetCentresAandB==null){return null;}  
		
		ellipseCentres[1] = new Point2D.Double(distBetCentresAandB,0.0);
		
		
		// use the following to place the 3rd circle by using a vertical and later horizontal bisection 
		// -> to use this must ensure that the base 2 circles are those containing the largest 2-set region
		
		Point2D.Double[] ipsAB = getIPsBet2BaseOverlappingCircles(distBetCentresAandB, ellipseRadiuses[0], ellipseRadiuses[1]);
		switch(initDiagType){
			case BISECTION_ON_Y_THEN_X: {
				Double centre_c_y = getDistBetweenCentreOfSharedZoneAnd3rdCircle_orig(ipsAB, ellipseCentres[1], ellipseRadiuses[0], ellipseRadiuses[1], ellipseRadiuses[2], requiredAreaSpecs, circleRots[0], circleRots[1], circleRots[2], diagIsValidPolyAreaDiscardThreshold, considerLessPrecisePossibleIntPnts); //, dist_areaAB[1]); 
				if (centre_c_y==null){return null;}  
				
				Double centre_c_x = getDistXBetweenCentreOfSharedZoneAnd3rdCircle_orig(ipsAB, ellipseCentres[1], ellipseRadiuses[0], ellipseRadiuses[1], ellipseRadiuses[2], requiredAreaSpecs, centre_c_y, circleRots[0], circleRots[1], circleRots[2], diagIsValidPolyAreaDiscardThreshold, considerLessPrecisePossibleIntPnts); //, dist_areaAB[1]); 
				if (centre_c_x==null){return null;}  
				
				ellipseCentres[2] = new Point2D.Double(centre_c_x, centre_c_y);
			
				break;
			}

			
			case BISECTION_ON_SLOPE:{	
		    	//Bisection on a slope	
		        Point2D.Double centre_c = getCentreOfThirdEllipseInInitDiagUsingBisection_checkZAs(ipsAB, ellipseCentres[1], ellipseRadiuses[0], ellipseRadiuses[1], ellipseRadiuses[2], requiredAreaSpecs, circleRots[0], circleRots[1], circleRots[2], considerLessPrecisePossibleIntPnts); //pass on the rots because even though the rot does not theoretically matter for a circle, for the int pnt method, it does and thus to make sure that the gen init diag is valid, then to pass on the rot
				if (centre_c==null){return null;}
		        ellipseCentres[2] = new Point2D.Double(centre_c.x, centre_c.y);
				
				break;
			}
		}

		// Initialize the ellipses for the diagram 
		ArrayList<Ellipse> ellsInDiag = new ArrayList<Ellipse>(3); //3 is just an initial capacity which will grow automatically as soon as the arraylist is full and add is invoked
		for (eI=0; eI<3; eI++){
			ellsInDiag.add(new Ellipse(ellipseLabels[eI], ellipseRadiuses[eI], ellipseRadiuses[eI], ellipseCentres[eI].x, ellipseCentres[eI].y, circleRots[eI]));//0));//
		}
		EllipseDiagram diag_orig = new EllipseDiagram (ellsInDiag, considerLessPrecisePossibleIntPnts);
		
		return diag_orig;
	}
	
	public static Point2D.Double[] getIPsBet2BaseOverlappingCircles (double d, double r1, double r2){
		double arcAngBetIPsForA = getArcAngleForOverlappingCircles(d, r1, r2);
		
		double ips_x = r1 * Math.cos(arcAngBetIPsForA/2);
		double ips_y = r1 * Math.sin(arcAngBetIPsForA/2);
		
		Point2D.Double[] ips = new Point2D.Double[2];
		ips[0] = new Point2D.Double(ips_x,  ips_y);
		ips[1] = new Point2D.Double(ips_x, -ips_y);
		
		return ips; 
	}
	
	
	public static double getArcAngleForOverlappingCircles (double d, double r1, double r2){
		// this is the equation used by Stirling Chow in his thesis and paper to find the arc angle when the leftmost circle with radius r1 
		// has centre (0,0) and the rightmost circle has centre d units away from the centre of circle 1 thus having centre (d,0)
		// => this is NOT a general solution to find the angle of any arc
		double val = (Math.pow(d,2)+Math.pow(r1,2)-Math.pow(r2,2)) / (2*r1*d);
	
		return ( 2 * Math.acos(val));
	}
	
	public static double getCircularArcAngleBetIPs (Point2D.Double[] ips, Point2D.Double centre){
			
		double ang0 = Math.abs(Math.atan(Math.abs(ips[0].y - centre.y)/Math.abs(ips[0].x - centre.x)));
		double ang1 = Math.abs(Math.atan(Math.abs(ips[1].y - centre.y)/Math.abs(ips[1].x - centre.x)));
		double arcAng = 0;
		
		if ( (Math.signum(ips[0].x-centre.x) == Math.signum(ips[1].x-centre.x)) && 
			 (Math.signum(ips[0].y-centre.y) == Math.signum(ips[1].y-centre.y))){ 
		
			arcAng = Math.abs(ang1 - ang0);
			
		} else if ((Math.signum(ips[0].x-centre.x) == Math.signum(ips[1].x-centre.x)) && 
				   (Math.signum(ips[0].y-centre.y) != Math.signum(ips[1].y-centre.y))){  
			
			arcAng = ang0 + ang1; 
			
		} else if ((Math.signum(ips[0].x-centre.x) != Math.signum(ips[1].x-centre.x)) && 
				   (Math.signum(ips[0].y-centre.y) == Math.signum(ips[1].y-centre.y))){  	
			
			arcAng = Math.PI - (ang0 + ang1); //no need Math.abs because already done that when find ang0 and ang1
			
		} else { 
			arcAng = (Math.PI - ang0) + ang1; 
		}
		
		return arcAng;
	}
	
	public static double areaOverlapCircles (double d, double r1, double r2){
		double ang1 = Math.abs(getArcAngleForOverlappingCircles(d, r1, r2)); 
		double ang2 = Math.abs(getArcAngleForOverlappingCircles(d, r2, r1)); 
		
		double areaSeg1 = getAreaOfCircleSegment(r1, ang1);
		double areaSeg2 = getAreaOfCircleSegment(r2, ang2);
		
		return (areaSeg1 + areaSeg2);
	}
	
	public static double getAreaOfCircleSegment(double r, double ang){
		return (0.5 * Math.pow(r,2) * (ang - Math.sin(ang)));
	}
	
	public static double areaOverlapSharedZoneWith3rdCircle(Point2D.Double[] ips_ab, Point2D.Double centre_b, Point2D.Double centre_c, double r_a, double r_b, double r_c, boolean upper){
		
		// ip_ab
		Point2D.Double ip_ab;
		if (upper){
			ip_ab = (ips_ab[0].y > ips_ab[1].y) ? ips_ab[0] : ips_ab[1]; 
		} else {
			ip_ab = (ips_ab[0].y < ips_ab[1].y) ? ips_ab[0] : ips_ab[1];
		}
		
		// ip_ac 
		double distCentreAC = Math.sqrt(Math.pow(centre_c.x,2)+Math.pow(centre_c.y,2));
		Point2D.Double[] ips_ac_notRot = getIPsBet2BaseOverlappingCircles(distCentreAC, r_a, r_c);
		Point2D.Double ip_ac_notRot;
		Point2D.Double ip_ac_notRot2;
		// choose lower ip (smallest y) which when rotated would become the rightmost ip
		ip_ac_notRot  = (ips_ac_notRot[0].y < ips_ac_notRot[1].y) ? ips_ac_notRot[0] : ips_ac_notRot[1];
		ip_ac_notRot2 = (ips_ac_notRot[0].y < ips_ac_notRot[1].y) ? ips_ac_notRot[1] : ips_ac_notRot[0];
	
		double angBetCentresLineAndXaxis1 = GeometricOps.getPolarCoordAngForPnt(centre_c, new Point2D.Double(0.0,0.0));
		Point2D.Double ip_ac = new Point2D.Double ( (ip_ac_notRot.x*Math.cos(angBetCentresLineAndXaxis1)) - (ip_ac_notRot.y*Math.sin(angBetCentresLineAndXaxis1)),
													(ip_ac_notRot.x*Math.sin(angBetCentresLineAndXaxis1)) + (ip_ac_notRot.y*Math.cos(angBetCentresLineAndXaxis1)) ); 
		
		// ip_bc
		double distCentreBC = Math.sqrt(Math.pow(centre_c.x - centre_b.x,2)+Math.pow(centre_c.y - centre_b.y,2));
		Point2D.Double[] ips_bc_notRot_noTransl = getIPsBet2BaseOverlappingCircles(distCentreBC, r_c, r_b); 
		Point2D.Double ip_bc_notRot_noTransl;
		Point2D.Double ip_bc_notRot_noTransl2;
		// choose lower ip (smallest y) which when rotated would become the leftmost ip
		ip_bc_notRot_noTransl  = (ips_bc_notRot_noTransl[0].y < ips_bc_notRot_noTransl[1].y) ? ips_bc_notRot_noTransl[0] : ips_bc_notRot_noTransl[1]; 
		ip_bc_notRot_noTransl2 = (ips_bc_notRot_noTransl[0].y < ips_bc_notRot_noTransl[1].y) ? ips_bc_notRot_noTransl[1] : ips_bc_notRot_noTransl[0]; 
		
		double angBetCentresLineAndXaxis2 = GeometricOps.getPolarCoordAngForPnt(centre_b, centre_c);
		Point2D.Double ip_bc = new Point2D.Double ( (ip_bc_notRot_noTransl.x*Math.cos(angBetCentresLineAndXaxis2)) - (ip_bc_notRot_noTransl.y*Math.sin(angBetCentresLineAndXaxis2)) + centre_c.x,
													(ip_bc_notRot_noTransl.x*Math.sin(angBetCentresLineAndXaxis2)) + (ip_bc_notRot_noTransl.y*Math.cos(angBetCentresLineAndXaxis2)) + centre_c.y); 
		Point2D.Double ip_bc2 = new Point2D.Double ( (ip_bc_notRot_noTransl2.x*Math.cos(angBetCentresLineAndXaxis2)) - (ip_bc_notRot_noTransl2.y*Math.sin(angBetCentresLineAndXaxis2)) + centre_c.x,
				                                     (ip_bc_notRot_noTransl2.x*Math.sin(angBetCentresLineAndXaxis2)) + (ip_bc_notRot_noTransl2.y*Math.cos(angBetCentresLineAndXaxis2)) + centre_c.y);  
		
		//prefer to use the methods for elliptic segments because this takes into consideration cases were the angle between the ips is greater than 180deg
		Ellipse e_a = new Ellipse ("", r_a, r_a, 0, 0, 0);
		double areaSegm_a = e_a.computeAreaOfEllipticSegment(new Point2D.Double[]{ip_ac, ip_ab});
		Ellipse e_b = new Ellipse ("", r_b, r_b, centre_b.x, centre_b.y, 0);
		double areaSegm_b = e_b.computeAreaOfEllipticSegment(new Point2D.Double[]{ip_ab, ip_bc});
		Ellipse e_c = new Ellipse ("", r_c, r_c, centre_c.x, centre_c.y, 0);
		double areaSegm_c = e_c.computeAreaOfEllipticSegment(new Point2D.Double[]{ip_bc, ip_ac});
		
		// area of inner triangle
		Triangle triangle = new Triangle(ip_ab, ip_ac, ip_bc);
		
		return (areaSegm_a + areaSegm_b + areaSegm_c + triangle.computeArea());
	}
	
	
	
	public static Double getDistBetweenCentreOfSharedZoneAnd3rdCircle_orig(Point2D.Double[] ipsAB, Point2D.Double centre_b, double r_a, double r_b, double r_c, HashMap<String, Double> requiredAreaSpecs, double rot_a,double rot_b, double rot_c,double diagIsValidPolyAreaDiscardThreshold, boolean considerLessPrecisePossibleIntPnts){
	    // bisection method
		
		Ellipse e_a = new Ellipse ("a", r_a, r_a, 0, 0, rot_a); 
		Ellipse e_b = new Ellipse ("b", r_b, r_b, centre_b.x, centre_b.y, rot_b);
		Ellipse e_c = new Ellipse ("c", r_c, r_c, ipsAB[0].x, 0, rot_c);
		ArrayList<Ellipse> ells = new ArrayList<Ellipse>();
		ells.add(e_a);ells.add(e_b);ells.add(e_c); 
		EllipseDiagram diag = new EllipseDiagram(ells, considerLessPrecisePossibleIntPnts); 		
		
		double reqAreaABC = requiredAreaSpecs.get("abc"); //***be careful if you refer to any other zone because these zone labels are the actual ones and hence ellipse a is not necessarily at the bottom left corner, b not necessarily at bottom right and c not necessarily at the top
		
		
		double isZeroL = 1e-7;
	
		Point2D.Double ipsAB_lower = ipsAB[0];
		Point2D.Double ipsAB_upper = ipsAB[1];
		if (ipsAB[1].y < 0){
			ipsAB_lower = ipsAB[1];
			ipsAB_upper = ipsAB[0];
		}
			
		double diffReqCurrABC_lower = 0;
		double diffReqCurrABC_mid = 0;
		double diffReqCurrABC_upper = 0;
	
		
		double currABC_lower = 0;
		double currABC_mid = 0;
		double currABC_upper = 0;	
		
		boolean cannotGetExactABC=false;
		double constantToAvoidSmallRegions = 0.5;  //keep this 0 if check for empty-like and disconn-like zones and loop until find appropriate limits (this is done further down)
		double padding_lower = (constantToAvoidSmallRegions + isZeroL); 
		double padding_upper = (constantToAvoidSmallRegions + isZeroL);
	
		double y_upper = ipsAB_upper.y + r_c - padding_upper; 
		double y_lower = (ipsAB_lower.y > ((ipsAB_upper.y - r_c) - r_c)) ? (ipsAB_lower.y + r_c + padding_lower) : (ipsAB_upper.y - r_c + padding_lower);
		
		// the following is used ensure that the diagram does not have any empty-like or disconnected-like zones (checks done using the polygons inside the zones)
		
		boolean foundValidUpperLimit = false;
		boolean foundValidLowerLimit = false;
		double stepMoveIfValidLimitNotFound = 0.5;
		do {
			diag.getEllipses().get(2).setYc(y_upper); 
			diag.recomputeZoneAreas();
			
			if (diag.isValid()){ 
				foundValidUpperLimit = true;
			} else {
				y_upper = y_upper - stepMoveIfValidLimitNotFound;
			}
			
			if (y_upper < y_lower){   //this case should not really be possible
				return null;
			}
		} while (!foundValidUpperLimit);		
		
		do {
			diag.getEllipses().get(2).setYc(y_lower); 
			diag.recomputeZoneAreas();
			
			if (diag.isValid()){ 
				foundValidLowerLimit = true;
			} else {
				y_lower = y_lower + stepMoveIfValidLimitNotFound;
			}
			
			if (y_upper < y_lower){   
				return y_upper; //assuming that a valid upper has been found already
			}
		} while (!foundValidLowerLimit);	

		
		if (Math.abs(y_upper-y_lower) <= isZeroL){  //do not remove Math.abs because if upper<lower than it must be handled in a different manner
			//this is necessary due to various padding possibilities which could cause the 2 limits to be equal
			return y_upper; //or d_lower since they will be same
		}		
		
		currABC_lower = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, new Point2D.Double(ipsAB[0].x, y_lower), r_a, r_b, r_c, true); 		
		if (reqAreaABC > currABC_lower){  //currABC_lower is the greatest ABC I can get
			return y_lower;
		}
		
		
		double y_mid = 0;
		
	
		while (true){
			
			y_mid = (y_lower + y_upper)/2;
			
			
			currABC_lower = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, new Point2D.Double(ipsAB[0].x, y_lower), r_a, r_b, r_c, true); 
			currABC_mid   = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, new Point2D.Double(ipsAB[0].x, y_mid),   r_a, r_b, r_c, true); 
			currABC_upper = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, new Point2D.Double(ipsAB[0].x, y_upper), r_a, r_b, r_c, true); 
			
			if (Double.isNaN(currABC_lower)||Double.isNaN(currABC_mid)||Double.isNaN(currABC_upper)){
				return Double.NaN;
			}
			
			if (reqAreaABC > currABC_lower){  //currABC_lower is the greatest ABC I can get
				return y_lower;
			}
			
			diffReqCurrABC_lower = reqAreaABC - currABC_lower;
			diffReqCurrABC_mid   = reqAreaABC - currABC_mid;
			diffReqCurrABC_upper = reqAreaABC - currABC_upper;
			
			if (Math.abs(diffReqCurrABC_lower) <= isZeroL){
				return y_lower;
			} 
			if (Math.abs(diffReqCurrABC_mid) <= isZeroL){
				return y_mid;
			} 
			if (Math.abs(diffReqCurrABC_upper) <= isZeroL){
				return y_upper;
			} 
			
			if (cannotGetExactABC){
				if (Math.abs(diffReqCurrABC_lower) < Math.abs(diffReqCurrABC_mid)){// diff of lower and diff of mid have different signs => when * get -ve ie <0
					y_upper = y_mid;
				} else {       // diff of lower and diff of mid have same signs => when * get +ve ie >0
					y_lower = y_mid;
				}
			} else {
				if ((diffReqCurrABC_lower * diffReqCurrABC_mid) < 0){ // diff of lower and diff of mid have different signs => when * get -ve ie <0
					y_upper = y_mid;
				} else {       // diff of lower and diff of mid have same signs => when * get +ve ie >0
					y_lower = y_mid;
				} 
			}
			
			if (Math.abs(y_upper-y_lower) <= isZeroL){  //can remove Math.abs because upper must be > lower but leave it to be on the safe siden n// if ((d_upper-d_lower) < isZeroL){
				return y_mid;
			}
		}

	}
	
	public static Point2D.Double getCentreOfThirdEllipseInInitDiagUsingBisection (Point2D.Double[] ipsAB, Point2D.Double centre_b, double r_a, double r_b, double r_c, HashMap<String, Double> requiredAreaSpecs, boolean considerLessPrecisePossibleIntPnts){
		// bisection method
		
		// for this, best to have the largest 2 circles at the base -> to avoid as much as possible exceptions even though these are dealt with 
		// ... and have the circles ordered according to their total area starting off with the one with the largest area -> this would ensure that the acute angle for the slope on which the 3rd circle will slide on during the bisection, would always be acute and opening up towards the right
		
		Ellipse e_a = new Ellipse ("a", r_a, r_a, 0, 0, 0);
		Ellipse e_b = new Ellipse ("b", r_b, r_b, centre_b.x, centre_b.y, 0);
		Ellipse e_c = new Ellipse ("c", r_c, r_c, ipsAB[0].x, 0, 0);
		ArrayList<Ellipse> ells = new ArrayList<Ellipse>();
		ells.add(e_a);ells.add(e_b);ells.add(e_c); 
		EllipseDiagram diag = new EllipseDiagram(ells, considerLessPrecisePossibleIntPnts); 
		
		
		double reqAreaABC = requiredAreaSpecs.get("abc"); //***be careful if you refer to any other zone because these zone labels are the actual ones and hence ellipse a is not necessarily at the bottom left corner, b not necessarily at bottom right and c not necessarily at the top
		
		
		double isZeroL = 1e-7;
	
		Point2D.Double ipsAB_lower = ipsAB[0];
		Point2D.Double ipsAB_upper = ipsAB[1];
		if (ipsAB[1].y < 0){
			ipsAB_lower = ipsAB[1];
			ipsAB_upper = ipsAB[0];
		}
			
		double diffReqCurrABC_lower = 0;
		double diffReqCurrABC_mid = 0;
		double diffReqCurrABC_upper = 0;
		
		double currABC_lower = 0;
		double currABC_mid = 0;
		double currABC_upper = 0;	
		
		boolean cannotGetExactABC=false;
		
		double constantToAvoidSmallRegions = 0; 
		double padding_lower = (constantToAvoidSmallRegions + isZeroL); 
		double padding_upper = (constantToAvoidSmallRegions + isZeroL);
		
		Point2D.Double centre_upper = new Point2D.Double(0,0);
		Point2D.Double centre_lower = new Point2D.Double(0,0);
		
		// Math, geometry and trigonometry to find the slope on which the third circle should slide on during the bisection method and to set the initial limits for the bisection
		double angleXAxisAndTangOfLeftBaseCircle = Math.atan ( ipsAB_upper.y / (Math.sqrt( Math.pow(r_a,2) - Math.pow(ipsAB_upper.y,2)) ) ); //**in radians not degrees
		// this angle can be 0deg (when both base circles have the same size) BUT cannot be 90deg (because this would mean that we have only one intersecting point between the two circles => they would be brushing) => don't need to worry of tan90deg error being generated
		// ... however this is theoretical and if the area of the region shared by the two circles is very small, then this angle would be very small too and there is a practical possibility due to precision of having problems 
		if (angleXAxisAndTangOfLeftBaseCircle <= isZeroL){
			// set upper and lower limits as we were doing before that is just finding best y and x would be equal to that of the intersection points 
			centre_upper.x = ipsAB_upper.x;
			centre_upper.y = ipsAB_upper.y + r_c - padding_upper;
			
			centre_lower.x = ipsAB_upper.x;
			centre_lower.y = (ipsAB_lower.y > ((ipsAB_upper.y - r_c) - r_c)) ? (ipsAB_lower.y + r_c + padding_lower) : (ipsAB_upper.y - r_c + padding_lower); 
		
		} else {
			
			double gradientOfNormalOfTangOfLeftBaseCircle = ipsAB_upper.y / ipsAB_upper.x; 
			double gradientOfNormalOfTangOfRightBaseCircle = ipsAB_upper.y / (ipsAB_upper.x - centre_b.x);
			
			double angleNormalOfTangOfLeftBaseCircleAndVertLinePassingThroughIpsABUpper = Math.atan(ipsAB_upper.x / ipsAB_upper.y);
			double angleNormalOfTangOfRightBaseCircleAndVertLinePassingThroughIpsABUpper = Math.atan( Math.abs(ipsAB_upper.x - centre_b.x) / ipsAB_upper.y);
			double halfOfAngleBetweenNormalsOfTangentsOfBaseCircles = (angleNormalOfTangOfLeftBaseCircleAndVertLinePassingThroughIpsABUpper + angleNormalOfTangOfRightBaseCircleAndVertLinePassingThroughIpsABUpper) / 2;
			double angleSlopePassingThrough3rdCircleCentreAndVertLinePassingThroughIpsABUpper = 0; 
				
			if (Math.signum(gradientOfNormalOfTangOfLeftBaseCircle) == Math.signum(gradientOfNormalOfTangOfRightBaseCircle) ){ //what if the gradient of one of them is 0? signum will return 0 is gradient is 0, 1 if gradient is +ve, -1 if gradient is -ve 
				angleSlopePassingThrough3rdCircleCentreAndVertLinePassingThroughIpsABUpper = halfOfAngleBetweenNormalsOfTangentsOfBaseCircles;
			} else {
				angleSlopePassingThrough3rdCircleCentreAndVertLinePassingThroughIpsABUpper = Math.max(angleNormalOfTangOfLeftBaseCircleAndVertLinePassingThroughIpsABUpper, angleNormalOfTangOfRightBaseCircleAndVertLinePassingThroughIpsABUpper) -  halfOfAngleBetweenNormalsOfTangentsOfBaseCircles;
			}
			
		    double angleXAxisAndSlopePassingThrough3rdCircleCentre = (Math.PI/2) - angleSlopePassingThrough3rdCircleCentreAndVertLinePassingThroughIpsABUpper;
			
			centre_upper.x = ipsAB_upper.x + (r_c * Math.cos(angleXAxisAndSlopePassingThrough3rdCircleCentre)) - padding_upper;
			centre_upper.y = ipsAB_upper.y + (r_c * Math.sin(angleXAxisAndSlopePassingThrough3rdCircleCentre)) - padding_upper;
			
			centre_lower.x = ipsAB_upper.x - (r_c * Math.cos(angleXAxisAndSlopePassingThrough3rdCircleCentre)) + padding_lower;
			centre_lower.y = ipsAB_upper.y - (r_c * Math.sin(angleXAxisAndSlopePassingThrough3rdCircleCentre)) + padding_lower;
			
			
			if (ipsAB_lower.y > (centre_lower.y - r_c)){
				
				double xOn3rdCircleForIpLowerY = Math.sqrt(Math.pow(r_c,2) - Math.pow(ipsAB_lower.y-centre_lower.y, 2)) + centre_lower.x;

				if (xOn3rdCircleForIpLowerY > ipsAB_lower.x){
					double gradientSlope_m1 = Math.tan(angleXAxisAndSlopePassingThrough3rdCircleCentre);
					double gradientNormal_m2 = (-1) / Math.tan(angleXAxisAndSlopePassingThrough3rdCircleCentre);
					
					double yinterceptSlope_c1 = ipsAB_upper.y - (Math.tan(angleXAxisAndSlopePassingThrough3rdCircleCentre) * ipsAB_upper.x);
					double yinterceptNormal_c2 = ipsAB_lower.y + ( ipsAB_lower.x / Math.tan(angleXAxisAndSlopePassingThrough3rdCircleCentre));
					
					Point2D.Double pntOnSlopeAndItsNormalPassingThroughIpsABlower_p = new Point2D.Double( (yinterceptNormal_c2 - yinterceptSlope_c1) / (gradientSlope_m1 - gradientNormal_m2) , ( ( gradientSlope_m1 * (yinterceptNormal_c2 - yinterceptSlope_c1)) / (gradientSlope_m1 - gradientNormal_m2) ) + yinterceptSlope_c1);
					double dist_pntOnSlopeAndNormal_ipsABlower_dw = Math.sqrt( Math.pow(ipsAB_lower.y - pntOnSlopeAndItsNormalPassingThroughIpsABlower_p.y,2) + Math.pow(ipsAB_lower.x - pntOnSlopeAndItsNormalPassingThroughIpsABlower_p.x, 2)); 
					double dist_pntOnSlopeAndNormal_3rdCircleCentre_dz = Math.sqrt (Math.pow(r_c,2) - Math.pow(dist_pntOnSlopeAndNormal_ipsABlower_dw,2)); 
				
					centre_lower.x = pntOnSlopeAndItsNormalPassingThroughIpsABlower_p.x + (dist_pntOnSlopeAndNormal_3rdCircleCentre_dz * Math.cos(angleXAxisAndSlopePassingThrough3rdCircleCentre));
					centre_lower.y = pntOnSlopeAndItsNormalPassingThroughIpsABlower_p.y + (dist_pntOnSlopeAndNormal_3rdCircleCentre_dz * Math.sin(angleXAxisAndSlopePassingThrough3rdCircleCentre));
				}
			}
		}
		
		// not sure if this check is necessary
		if (centre_upper.y < centre_lower.y){
			return new Point2D.Double (centre_upper.x, ((ipsAB_upper.y - (Math.abs(ipsAB_upper.y-ipsAB_lower.y)/2)) + r_c)); //doesn't matter if have centre_lower.x instead of centre_upper.x because they should be equal -> the y is the same as I was using in the horizontal movement bisection  
		}
		
		
		if (GeometricOps.distanceBetween2Points(centre_upper, centre_lower) <= isZeroL){
			return centre_upper; //or centre_lower because they are nearly the same point
		}

	
		currABC_lower = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, centre_lower, r_a, r_b, r_c, true); 		
		if (reqAreaABC > currABC_lower){  //currABC_lower is the greatest ABC possible
			return centre_lower;
		}
		currABC_upper = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, centre_upper, r_a, r_b, r_c, true); 
		
		Point2D.Double centre_mid = new Point2D.Double(0,0);
		
		while (true){

			centre_mid = GeometricOps.midPointOf2Points(centre_upper, centre_lower);
			currABC_mid   = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, centre_mid,   r_a, r_b, r_c, true); 
			
			if (Double.isNaN(currABC_lower)||Double.isNaN(currABC_mid)||Double.isNaN(currABC_upper)){
				return new Point2D.Double(Double.NaN,Double.NaN);
			}
			
			if (reqAreaABC > currABC_lower){  //currABC_lower is the greatest ABC possible
				return centre_lower;
			}
			
			diffReqCurrABC_lower = reqAreaABC - currABC_lower;
			diffReqCurrABC_mid   = reqAreaABC - currABC_mid;
			diffReqCurrABC_upper = reqAreaABC - currABC_upper;
			
		
			if (Math.abs(diffReqCurrABC_lower) <= isZeroL){
				return centre_lower;
			} 
			if (Math.abs(diffReqCurrABC_mid) <= isZeroL){
				return centre_mid;
			} 
			if (Math.abs(diffReqCurrABC_upper) <= isZeroL){
				return centre_upper;
			} 
			if (cannotGetExactABC){
				if (Math.abs(diffReqCurrABC_lower) < Math.abs(diffReqCurrABC_mid)){// diff of lower and diff of mid have different signs => when * get -ve ie <0
					centre_upper = centre_mid;
					currABC_upper = currABC_mid;
				} else {       // diff of lower and diff of mid have same signs => when * get +ve ie >0
					centre_lower = centre_mid;
					currABC_lower = currABC_mid;
				}
			} else {
				if ((diffReqCurrABC_lower * diffReqCurrABC_mid) < 0){ // diff of lower and diff of mid have different signs => when * get -ve ie <0
					centre_upper = centre_mid;
					currABC_upper = currABC_mid;
				} else {       // diff of lower and diff of mid have same signs => when * get +ve ie >0
					centre_lower = centre_mid;
					currABC_lower = currABC_mid;
				} 
			}
			
			if (GeometricOps.distanceBetween2Points(centre_upper, centre_lower) <= isZeroL){  //can remove Math.abs because upper must be > lower but leave it to be on the safe siden n// if ((d_upper-d_lower) < isZeroL){
				return centre_mid;
			}
		}

	}	
	

	public static Double getDistXBetweenCentreOfSharedZoneAnd3rdCircle_orig(Point2D.Double[] ipsAB, Point2D.Double centre_b, double r_a, double r_b, double r_c, HashMap<String, Double> requiredAreaSpecs, double yc_c, 
																			double rot_a,double rot_b, double rot_c,double diagIsValidPolyAreaDiscardThreshold, boolean considerLessPrecisePossibleIntPnts){ 
	    // bisection method
		
		Ellipse e_a = new Ellipse ("a", r_a, r_a, 0, 0, rot_a);	
		Ellipse e_b = new Ellipse ("b", r_b, r_b, centre_b.x, centre_b.y, rot_b);
		Ellipse e_c = new Ellipse ("c", r_c, r_c, ipsAB[0].x, yc_c, rot_c);
		ArrayList<Ellipse> ells = new ArrayList<Ellipse>();
		ells.add(e_a);ells.add(e_b);ells.add(e_c); 
		EllipseDiagram diag = new EllipseDiagram(ells, considerLessPrecisePossibleIntPnts); 
		
		
		double reqAreaABC = requiredAreaSpecs.get("abc"); //***be careful if you refer to any other zone because these zone labels are the actual ones and hence ellipse a is not necessarily at the bottom left corner, b not necessarily at bottom right and c not necessarily at the top
		
		
		double isZeroL = 1e-7;
	
		Point2D.Double ipsAB_lower = ipsAB[0];
		Point2D.Double ipsAB_upper = ipsAB[1];
		if (ipsAB[1].y < 0){
			ipsAB_lower = ipsAB[1];
			ipsAB_upper = ipsAB[0];
		}
			
		double diffReqCurrABC_lower = 0;
		double diffReqCurrABC_mid = 0;
		double diffReqCurrABC_upper = 0;
		
		double currABC_lower = 0;
		double currABC_mid = 0;
		double currABC_upper = 0;	
		
		boolean cannotGetExactABC=false;
		
		double xd = Math.sqrt(Math.pow(r_c, 2) - Math.pow(ipsAB_upper.y-yc_c, 2));
		
		double x_rightmost = ipsAB_upper.x + xd; 
		double x_leftmost = ipsAB_upper.x - xd; 
		double constantToAvoidSmallRegions = 0.5; 
		x_leftmost += (constantToAvoidSmallRegions + isZeroL); 
		x_rightmost -= (constantToAvoidSmallRegions + isZeroL);
		
		
		// the following is used ensure that the diagram does not have any empty-like or disconnected-like zones (checks done using the polygons inside the zones)
		
		boolean foundValidRightmostLimit = false;
		boolean foundValidLeftmostLimit = false;
		double stepMoveIfValidLimitNotFound = 0.5;
		do {
			diag.getEllipses().get(2).setXc(x_rightmost); 
			diag.recomputeZoneAreas();
			
			if (diag.isValid()){ 
				foundValidRightmostLimit = true;
			} else {
				x_rightmost = x_rightmost - stepMoveIfValidLimitNotFound; 
			}
			
			if (x_rightmost < x_leftmost){   //this case should not really be possible
				return ipsAB_upper.x;
			}
		} while (!foundValidRightmostLimit);		
		
		do {
			diag.getEllipses().get(2).setXc(x_leftmost); 
			diag.recomputeZoneAreas();
			
			if (diag.isValid()){ 
				foundValidLeftmostLimit = true;
			} else {
				x_leftmost = x_leftmost + stepMoveIfValidLimitNotFound;
			}
			
			if (x_rightmost < x_leftmost){   
				return x_rightmost; //assuming that a valid upper has been found already
			}
		} while (!foundValidLeftmostLimit);	
		
		double x_mid = 0;
		int i=0;
		while (true){
			
			x_mid = (x_leftmost + x_rightmost)/2;
			
			
			currABC_lower = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, new Point2D.Double(x_leftmost, yc_c), r_a, r_b, r_c, true); 
			currABC_mid   = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, new Point2D.Double(x_mid, yc_c),   r_a, r_b, r_c, true); 
			currABC_upper = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, new Point2D.Double(x_rightmost, yc_c), r_a, r_b, r_c, true); 
			
			if (Double.isNaN(currABC_lower)||Double.isNaN(currABC_mid)||Double.isNaN(currABC_upper)){
				return Double.NaN;
			}
			
			
			diffReqCurrABC_lower = reqAreaABC - currABC_lower;
			diffReqCurrABC_mid   = reqAreaABC - currABC_mid;
			diffReqCurrABC_upper = reqAreaABC - currABC_upper;
		
			
			if ( (Math.signum(diffReqCurrABC_lower) == Math.signum(diffReqCurrABC_mid)) && 
				 (Math.signum(diffReqCurrABC_mid) == Math.signum(diffReqCurrABC_upper)) ){
				
				double minDiffABC = Math.min(Math.abs(diffReqCurrABC_lower), Math.min(Math.abs(diffReqCurrABC_mid),Math.abs(diffReqCurrABC_upper)));
				double diffInitABC = Math.abs(reqAreaABC - areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, new Point2D.Double(ipsAB_upper.x, yc_c), r_a, r_b, r_c, true)); 
				
				if ((i==0) && (diffInitABC <= minDiffABC)){
					return ipsAB_upper.x;
					
				} else {
					if (minDiffABC == Math.abs(diffReqCurrABC_lower)){
						return x_leftmost;
						
					} else if (minDiffABC == Math.abs(diffReqCurrABC_mid)){
						return x_mid;
						
					} else if (minDiffABC == Math.abs(diffReqCurrABC_upper)){
						return x_rightmost;
					}
				}
			}
		
			if (Math.abs(diffReqCurrABC_lower) <= isZeroL){
				return x_leftmost;
			} 
			if (Math.abs(diffReqCurrABC_mid) <= isZeroL){
				return x_mid;
			} 
			if (Math.abs(diffReqCurrABC_upper) <= isZeroL){
				return x_rightmost;
			} 
			
			if (cannotGetExactABC){
				if (Math.abs(diffReqCurrABC_lower) < Math.abs(diffReqCurrABC_mid)){// diff of lower and diff of mid have different signs => when * get -ve ie <0
					x_rightmost = x_mid;
				} else {       // diff of lower and diff of mid have same signs => when * get +ve ie >0
					x_leftmost = x_mid;
				}
			} else {
				if ((diffReqCurrABC_lower * diffReqCurrABC_mid) < 0){ // diff of lower and diff of mid have different signs => when * get -ve ie <0
					x_rightmost = x_mid;
				} else {       // diff of lower and diff of mid have same signs => when * get +ve ie >0
					x_leftmost = x_mid;
				} 
			}
			
			if (Math.abs(x_rightmost-x_leftmost) <= isZeroL){
				return x_mid;
			}	
			i++;
		}
	}
	

	public static Point2D.Double getCentreOfThirdEllipseInInitDiagUsingBisection_checkZAs (Point2D.Double[] ipsAB, Point2D.Double centre_b, double r_a, double r_b, double r_c, HashMap<String, Double> requiredAreaSpecs, double rot_a, double rot_b, double rot_c, boolean considerLessPrecisePossibleIntPnts){
		// bisection method
		
		//*** when find the lower and upper limits and the midpoint (since this could be a potential final diagram) ensure that they are valid diagrams (and hence all the zone areas can be computed) and that they do not have any empty or disconnected like zones (judgement based on the polygons in the zones - as defined by the methods in TestingWithPolygons) 
		
		// for this, best to have the largest 2 circles at the base -> to avoid as much as possible exceptions even though these are dealt with 
		// ... and have the circles ordered according to their total area starting off with the one with the largest area -> this would ensure that the acute angle for the slope on which the 3rd circle will slide on during the bisection, would always be acute and opening up towards the right
		
		Ellipse e_a = new Ellipse ("a", r_a, r_a, 0, 0, rot_a);
		Ellipse e_b = new Ellipse ("b", r_b, r_b, centre_b.x, centre_b.y, rot_b);
		Ellipse e_c = new Ellipse ("c", r_c, r_c, 0, 0, rot_c);
		ArrayList<Ellipse> ells = new ArrayList<Ellipse>();
		ells.add(e_a);ells.add(e_b);ells.add(e_c); 
		EllipseDiagram diag = new EllipseDiagram(ells,considerLessPrecisePossibleIntPnts); 
		
		double reqAreaABC = requiredAreaSpecs.get("abc"); //***be careful if you refer to any other zone because these zone labels are the actual ones and hence ellipse a is not necessarily at the bottom left corner, b not necessarily at bottom right and c not necessarily at the top
		
		
		double isZeroL = 1e-7;
	
		Point2D.Double ipsAB_lower = ipsAB[0];
		Point2D.Double ipsAB_upper = ipsAB[1];
		if (ipsAB[1].y < 0){
			ipsAB_lower = ipsAB[1];
			ipsAB_upper = ipsAB[0];
		}
	
			
		double diffReqCurrABC_lower = 0;
		Double diffReqCurrABC_mid = null;
		double diffReqCurrABC_upper = 0;
		
		double currABC_lower = 0;
		Double currABC_mid = null;
		double currABC_upper = 0;	
		
		boolean cannotGetExactABC=false;
		
		double constantToAvoidSmallRegions = 0.5; 
		double padding_lower = (constantToAvoidSmallRegions + isZeroL); 
		double padding_upper = (constantToAvoidSmallRegions + isZeroL);
		
		Point2D.Double centre_upper = new Point2D.Double(0,0);
		Point2D.Double centre_lower = new Point2D.Double(0,0);
		
		boolean foundValidUpperLimit = false;
		boolean foundValidLowerLimit = false;
		boolean foundValidMidLimit = false;
		double stepMoveIfNotLimitNotFound = 0.5;
		
		// Math, geometry and trigonometry to find the slope on which the third circle should slide on during the bisection method and to set the initial limits for the bisection
		double angleXAxisAndTangOfLeftBaseCircle = Math.atan ( ipsAB_upper.y / (Math.sqrt( Math.pow(r_a,2) - Math.pow(ipsAB_upper.y,2)) ) ); //**in radians not degrees
		// this angle can be 0deg (when both base circles have the same size) BUT cannot be 90deg (because this would mean that we have only one intersecting point between the two circles => they would be brushing) => don't need to worry of tan90deg error being generated
		// ... however this is theoretical and if the area of the region shared by the two circles is very small, then this angle would be very small too and there is a practical possibility due to precision of having problems 
		
		double angleXAxisAndSlopePassingThrough3rdCircleCentre = 0;  // used when (angleXAxisAndTangOfLeftBaseCircle > isZeroL)
		
		if (angleXAxisAndTangOfLeftBaseCircle <= isZeroL){
			// set upper and lower limits as we were doing before that is just finding best y and x would be equal to that of the intersection points 
			centre_upper.x = ipsAB_upper.x;
			centre_upper.y = ipsAB_upper.y + r_c - padding_upper;
			
			centre_lower.x = ipsAB_upper.x;
			centre_lower.y = (ipsAB_lower.y > ((ipsAB_upper.y - r_c) - r_c)) ? (ipsAB_lower.y + r_c + padding_lower) : (ipsAB_upper.y - r_c + padding_lower); 

			do {
				diag.getEllipses().get(2).setXc(centre_upper.x);diag.getEllipses().get(2).setYc(centre_upper.y);
				diag.recomputeZoneAreas();
				
				if (diag.isValid()){ 
					foundValidUpperLimit = true;
				} else {
					centre_upper.y = centre_upper.y - stepMoveIfNotLimitNotFound;
				}
				
				if((centre_upper.y - centre_lower.y)<=isZeroL){
					return null;
				}
			} while (!foundValidUpperLimit);		
			
			do {
				diag.getEllipses().get(2).setXc(centre_lower.x);diag.getEllipses().get(2).setYc(centre_lower.y);
				diag.recomputeZoneAreas();
				
				if (diag.isValid()){ 	
					foundValidLowerLimit = true;
				} else {
					centre_lower.y = centre_lower.y + stepMoveIfNotLimitNotFound;
				}
				
				if((centre_upper.y - centre_lower.y)<=isZeroL){
					return centre_upper; //assuming that a valid upper has been found already
				}
			} while (!foundValidLowerLimit);	
			
			
		} else {
			
			double gradientOfNormalOfTangOfLeftBaseCircle = ipsAB_upper.y / ipsAB_upper.x; 
			double gradientOfNormalOfTangOfRightBaseCircle = ipsAB_upper.y / (ipsAB_upper.x - centre_b.x);
			
			double angleNormalOfTangOfLeftBaseCircleAndVertLinePassingThroughIpsABUpper = Math.atan(ipsAB_upper.x / ipsAB_upper.y);
			double angleNormalOfTangOfRightBaseCircleAndVertLinePassingThroughIpsABUpper = Math.atan( Math.abs(ipsAB_upper.x - centre_b.x) / ipsAB_upper.y);
			double halfOfAngleBetweenNormalsOfTangentsOfBaseCircles = (angleNormalOfTangOfLeftBaseCircleAndVertLinePassingThroughIpsABUpper + angleNormalOfTangOfRightBaseCircleAndVertLinePassingThroughIpsABUpper) / 2;
			double angleSlopePassingThrough3rdCircleCentreAndVertLinePassingThroughIpsABUpper = 0; 
				
			if (Math.signum(gradientOfNormalOfTangOfLeftBaseCircle) == Math.signum(gradientOfNormalOfTangOfRightBaseCircle) ){ 
				angleSlopePassingThrough3rdCircleCentreAndVertLinePassingThroughIpsABUpper = halfOfAngleBetweenNormalsOfTangentsOfBaseCircles;
			} else {
				angleSlopePassingThrough3rdCircleCentreAndVertLinePassingThroughIpsABUpper = Math.max(angleNormalOfTangOfLeftBaseCircleAndVertLinePassingThroughIpsABUpper, angleNormalOfTangOfRightBaseCircleAndVertLinePassingThroughIpsABUpper) -  halfOfAngleBetweenNormalsOfTangentsOfBaseCircles;
			}
			
		    angleXAxisAndSlopePassingThrough3rdCircleCentre = (Math.PI/2) - angleSlopePassingThrough3rdCircleCentreAndVertLinePassingThroughIpsABUpper;
			
			centre_upper.x = ipsAB_upper.x + (r_c * Math.cos(angleXAxisAndSlopePassingThrough3rdCircleCentre)) - padding_upper;
			centre_upper.y = ipsAB_upper.y + (r_c * Math.sin(angleXAxisAndSlopePassingThrough3rdCircleCentre)) - padding_upper;
			
			centre_lower.x = ipsAB_upper.x - (r_c * Math.cos(angleXAxisAndSlopePassingThrough3rdCircleCentre)) + padding_lower;
			centre_lower.y = ipsAB_upper.y - (r_c * Math.sin(angleXAxisAndSlopePassingThrough3rdCircleCentre)) + padding_lower;

			
			if (Double.isNaN(centre_upper.x) || Double.isNaN(centre_upper.y) ||   //just as a precaution and completeness -> don't really need this though
			    Double.isNaN(centre_lower.x) || Double.isNaN(centre_lower.y)){
				return null;
			}
			
			
			if (ipsAB_lower.y > (centre_lower.y - r_c)){
				
				double xOn3rdCircleForIpLowerY = Math.sqrt(Math.pow(r_c,2) - Math.pow(ipsAB_lower.y-centre_lower.y, 2)) + centre_lower.x;

				if (xOn3rdCircleForIpLowerY > ipsAB_lower.x){
					double gradientSlope_m1 = Math.tan(angleXAxisAndSlopePassingThrough3rdCircleCentre);
					double gradientNormal_m2 = (-1) / Math.tan(angleXAxisAndSlopePassingThrough3rdCircleCentre);
					
					double yinterceptSlope_c1 = ipsAB_upper.y - (Math.tan(angleXAxisAndSlopePassingThrough3rdCircleCentre) * ipsAB_upper.x);
					double yinterceptNormal_c2 = ipsAB_lower.y + ( ipsAB_lower.x / Math.tan(angleXAxisAndSlopePassingThrough3rdCircleCentre));
					
					Point2D.Double pntOnSlopeAndItsNormalPassingThroughIpsABlower_p = new Point2D.Double((yinterceptNormal_c2 - yinterceptSlope_c1) / (gradientSlope_m1 - gradientNormal_m2) , ( ( gradientSlope_m1 * (yinterceptNormal_c2 - yinterceptSlope_c1)) / (gradientSlope_m1 - gradientNormal_m2) ) + yinterceptSlope_c1);
					double dist_pntOnSlopeAndNormal_ipsABlower_dw = Math.sqrt( Math.pow(ipsAB_lower.y - pntOnSlopeAndItsNormalPassingThroughIpsABlower_p.y,2) + Math.pow(ipsAB_lower.x - pntOnSlopeAndItsNormalPassingThroughIpsABlower_p.x, 2)); 
					double dist_pntOnSlopeAndNormal_3rdCircleCentre_dz = Math.sqrt (Math.pow(r_c,2) - Math.pow(dist_pntOnSlopeAndNormal_ipsABlower_dw,2)); 
				
					centre_lower.x = pntOnSlopeAndItsNormalPassingThroughIpsABlower_p.x + (dist_pntOnSlopeAndNormal_3rdCircleCentre_dz * Math.cos(angleXAxisAndSlopePassingThrough3rdCircleCentre));
					centre_lower.y = pntOnSlopeAndItsNormalPassingThroughIpsABlower_p.y + (dist_pntOnSlopeAndNormal_3rdCircleCentre_dz * Math.sin(angleXAxisAndSlopePassingThrough3rdCircleCentre));

					if (Double.isNaN(dist_pntOnSlopeAndNormal_3rdCircleCentre_dz) || Double.isNaN(centre_lower.x) || Double.isNaN(centre_lower.y)){
						double isZeroLimit = 1e-15;
						double isInfiniteLimit = 1e+15;
						if (isInfinite(gradientSlope_m1, isInfiniteLimit) && isZero(gradientNormal_m2, isZeroLimit)){
							pntOnSlopeAndItsNormalPassingThroughIpsABlower_p = new Point2D.Double(ipsAB_upper.x,ipsAB_lower.y);
						    dist_pntOnSlopeAndNormal_ipsABlower_dw = Math.sqrt( Math.pow(ipsAB_lower.y - pntOnSlopeAndItsNormalPassingThroughIpsABlower_p.y,2) + Math.pow(ipsAB_lower.x - pntOnSlopeAndItsNormalPassingThroughIpsABlower_p.x, 2)); 
							dist_pntOnSlopeAndNormal_3rdCircleCentre_dz = Math.sqrt (Math.pow(r_c,2) - Math.pow(dist_pntOnSlopeAndNormal_ipsABlower_dw,2)); 
						
							centre_lower.x = pntOnSlopeAndItsNormalPassingThroughIpsABlower_p.x + (dist_pntOnSlopeAndNormal_3rdCircleCentre_dz * Math.cos(angleXAxisAndSlopePassingThrough3rdCircleCentre));
							centre_lower.y = pntOnSlopeAndItsNormalPassingThroughIpsABlower_p.y + (dist_pntOnSlopeAndNormal_3rdCircleCentre_dz * Math.sin(angleXAxisAndSlopePassingThrough3rdCircleCentre));
						    
						} else {
							// should never have to get here but just in case 
							return null;
						}
					} 			
				}
			}
			
			do {
				diag.getEllipses().get(2).setXc(centre_upper.x);diag.getEllipses().get(2).setYc(centre_upper.y);
				diag.recomputeZoneAreas();
				
				if (diag.isValid()){
					foundValidUpperLimit = true;
				} else {
					centre_upper.x = centre_upper.x - (stepMoveIfNotLimitNotFound*Math.cos(angleXAxisAndSlopePassingThrough3rdCircleCentre));
					centre_upper.y = centre_upper.y - (stepMoveIfNotLimitNotFound*Math.sin(angleXAxisAndSlopePassingThrough3rdCircleCentre));
				}
				
				if ((centre_upper.y - centre_lower.y) <= isZeroL) {
					return null;	
				}
			} while (!foundValidUpperLimit);		
			
			do {
				diag.getEllipses().get(2).setXc(centre_lower.x);diag.getEllipses().get(2).setYc(centre_lower.y);
				diag.recomputeZoneAreas();
				
				if (diag.isValid()){ //isValidAndNoEmptyOrDisconnectedLikeZones(isValidPolyAreaDiscardThreshold)){  //isValid()){ //zoneAreasComputedSuccessfully){
					foundValidLowerLimit = true;
				} else {
					centre_lower.x = centre_lower.x + (stepMoveIfNotLimitNotFound*Math.cos(angleXAxisAndSlopePassingThrough3rdCircleCentre));
					centre_lower.y = centre_lower.y + (stepMoveIfNotLimitNotFound*Math.sin(angleXAxisAndSlopePassingThrough3rdCircleCentre));
				}
				
				if((centre_upper.y - centre_lower.y)<=isZeroL){
					return centre_upper; //assuming that a valid upper has been found already
				}
			} while (!foundValidLowerLimit);	
		}
		
		if (GeometricOps.distanceBetween2Points(centre_upper, centre_lower) <= isZeroL){
			return centre_upper; //or centre_lower because they are nearly the same point
		}

	
		// not sure about this check
		currABC_lower = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, centre_lower, r_a, r_b, r_c, true); 		
		if (reqAreaABC > currABC_lower){  //currABC_lower is the greatest ABC I can get
			return centre_lower;
		}		
		currABC_upper = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, centre_upper, r_a, r_b, r_c, true); 

		Point2D.Double centre_mid = new Point2D.Double(0,0);
		
	
		Point2D.Double centre_mid_expected = new Point2D.Double(centre_mid.x, centre_mid.y);
		Point2D.Double centre_mid_movedDown = new Point2D.Double(0,0);
		Point2D.Double centre_mid_movedUp = new Point2D.Double(0,0);
		double currABC_mid_movedDown = 0;
		double currABC_mid_movedUp = 0;
		double diffReqCurrABC_mid_movedDown = 0;
		double diffReqCurrABC_mid_movedUp = 0;

		int i=0;
		
		while (true){
			currABC_mid = null;
			diffReqCurrABC_mid = null;
		
			centre_mid = GeometricOps.midPointOf2Points(centre_upper, centre_lower);
			
			foundValidMidLimit=false;

			do {
				diag.getEllipses().get(2).setXc(centre_mid.x); diag.getEllipses().get(2).setYc(centre_mid.y);
				diag.recomputeZoneAreas();
				
				if (diag.isValid()){ 
					foundValidMidLimit = true;
				} else {
					centre_mid_expected.x = centre_mid.x; centre_mid_expected.y = centre_mid.y;
					
					
					if (angleXAxisAndTangOfLeftBaseCircle <= isZeroL){
						centre_mid_movedDown.x = centre_mid_expected.x;
						centre_mid_movedDown.y = centre_mid_expected.y + stepMoveIfNotLimitNotFound;
					} else {
						stepMoveIfNotLimitNotFound = 0.5*GeometricOps.distanceBetween2Points(centre_mid, centre_lower);
						centre_mid_movedDown.x = centre_mid_expected.x + (stepMoveIfNotLimitNotFound*Math.cos(angleXAxisAndSlopePassingThrough3rdCircleCentre));
					    centre_mid_movedDown.y = centre_mid_expected.y + (stepMoveIfNotLimitNotFound*Math.sin(angleXAxisAndSlopePassingThrough3rdCircleCentre));
					    
					}
					currABC_mid_movedDown = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, centre_mid_movedDown, r_a, r_b, r_c, true); 
					diffReqCurrABC_mid_movedDown = reqAreaABC - currABC_mid_movedDown;
					
					// mid moved up
					if (angleXAxisAndTangOfLeftBaseCircle <= isZeroL){
						centre_mid_movedUp.x = centre_mid_expected.x;
						centre_mid_movedUp.y = centre_mid_expected.y - stepMoveIfNotLimitNotFound;
					} else {
						stepMoveIfNotLimitNotFound = 0.5*GeometricOps.distanceBetween2Points(centre_upper, centre_mid);
						centre_mid_movedUp.x = centre_mid_expected.x - (stepMoveIfNotLimitNotFound*Math.cos(angleXAxisAndSlopePassingThrough3rdCircleCentre));
						centre_mid_movedUp.y = centre_mid_expected.y - (stepMoveIfNotLimitNotFound*Math.sin(angleXAxisAndSlopePassingThrough3rdCircleCentre));
					}
					currABC_mid_movedUp = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, centre_mid_movedUp, r_a, r_b, r_c, true); 
					diffReqCurrABC_mid_movedUp = reqAreaABC - currABC_mid_movedUp;
					
					// choosing the best mid -> the mid which helps to get a better abc
					if (Math.abs(diffReqCurrABC_mid_movedDown) < Math.abs(diffReqCurrABC_mid_movedUp)){
						centre_mid.x = centre_mid_movedDown.x; centre_mid.y = centre_mid_movedDown.y;
						currABC_mid = new Double (currABC_mid_movedDown);
						diffReqCurrABC_mid = new Double(diffReqCurrABC_mid_movedDown);
					} else {
						centre_mid.x = centre_mid_movedUp.x; centre_mid.y = centre_mid_movedUp.y;
						currABC_mid = new Double (currABC_mid_movedUp);
						diffReqCurrABC_mid = new Double (diffReqCurrABC_mid_movedUp);
					}
				}
				
				if((centre_mid.y - centre_lower.y) <= isZeroL){ // modified on 2012-08-23
					return centre_lower;
				}
				if ((centre_upper.y - centre_mid.y) <= isZeroL){ //as (centre_mid.y > centre_upper.y) is same as (centre_upper.y < centre_mid.y) // modified on 2012-08-23
					return centre_upper;
				}
			} while (!foundValidMidLimit);	
			
			
			if (currABC_mid == null){
				currABC_mid   = areaOverlapSharedZoneWith3rdCircle(ipsAB, centre_b, centre_mid,   r_a, r_b, r_c, true);
			}

			if (Double.isNaN(currABC_lower)||Double.isNaN(currABC_mid)||Double.isNaN(currABC_upper)){
				return new Point2D.Double(Double.NaN,Double.NaN);
			}
			
			if (reqAreaABC > currABC_lower){  //currABC_lower is the greatest ABC I can get
				return centre_lower; 
			}
			
			diffReqCurrABC_lower = reqAreaABC - currABC_lower;
			if (diffReqCurrABC_mid == null){ 
				diffReqCurrABC_mid   = reqAreaABC - currABC_mid;
			}
			diffReqCurrABC_upper = reqAreaABC - currABC_upper;
			
						
		
			if (Math.abs(diffReqCurrABC_lower) <= isZeroL){
				return centre_lower;
			} 
			if (Math.abs(diffReqCurrABC_mid) <= isZeroL){
				return centre_mid;
			} 
			if (Math.abs(diffReqCurrABC_upper) <= isZeroL){
				return centre_upper;
			} 
			
			
			if (cannotGetExactABC){
				if (Math.abs(diffReqCurrABC_lower) < Math.abs(diffReqCurrABC_mid)){// diff of lower and diff of mid have different signs => when * get -ve ie <0
					centre_upper.x = centre_mid.x; centre_upper.y = centre_mid.y;
					currABC_upper = currABC_mid;
				} else {       // diff of lower and diff of mid have same signs => when * get +ve ie >0
					centre_lower.x = centre_mid.x; centre_lower.y = centre_mid.y;
					currABC_lower = currABC_mid;
				}
			} else {
				if ((diffReqCurrABC_lower * diffReqCurrABC_mid) < 0){ // diff of lower and diff of mid have different signs => when * get -ve ie <0
					centre_upper.x = centre_mid.x; centre_upper.y = centre_mid.y;
					currABC_upper = currABC_mid;
				} else {       // diff of lower and diff of mid have same signs => when * get +ve ie >0
					centre_lower.x = centre_mid.x; centre_lower.y = centre_mid.y;
					currABC_lower = currABC_mid;
				} 
			}
			
			if (GeometricOps.distanceBetween2Points(centre_upper, centre_lower) <= isZeroL){ 
				return centre_mid;
			}
			i++;
		}

	}	
	
	
	
	public static Double getDistBetweenCentresOfAreaPropCircles(double r_a, double r_b, double area_a, double area_b, double reqAreaAB, double rot_a, double rot_b, boolean considerLessPrecisePossibleIntPnts){
		
		// bisection method
		
		double isZeroL = 1e-7;
	
		double diffReqCurrAB_lower = 0;
		double diffReqCurrAB_mid = 0;
		double diffReqCurrAB_upper = 0;
		
		double currAB_lower = 0;
		double currAB_mid = 0;
		double currAB_upper = 0;	
		
		double d_leftmost = (r_a >= r_b)? (r_a - r_b) : (r_b - r_a); 
		double d_rightmost = r_a + r_b; 
		double padding = 0.5;  
		d_leftmost += (padding + isZeroL);
		d_rightmost -= (padding + isZeroL);
		double d_mid = 0;
		
		double stepMoveIfValidLimitNotFound = 0.5;
		
		
		currAB_lower = areaOverlapCircles(d_leftmost, r_a, r_b);
		if (reqAreaAB > currAB_lower){  //currAB_lower is the greatest AB I can get
			return d_leftmost;
		}

       Double returnVal = null;
       int failCount=0;
       boolean modifyRightmost = true;
				
		while(true){ 
			
			d_mid = (d_leftmost + d_rightmost)/2;
			
			currAB_lower = areaOverlapCircles(d_leftmost, r_a, r_b);
			currAB_mid = areaOverlapCircles(d_mid, r_a, r_b);	
			currAB_upper = areaOverlapCircles(d_rightmost, r_a, r_b);
			
			if (reqAreaAB > currAB_lower){ 
				returnVal = d_leftmost;
			} else {

				// there is no need to check whether the diagram has empty-like or disconnected-like zones for the mid value because if the lower and the upper limits produce diagrams with the required 
				// non-empty-like and non-disconnected-like zones then anything in between should be also good considering that here we have a Venn-2 and hence 2 circles 
				
				diffReqCurrAB_lower = reqAreaAB-currAB_lower; 
				diffReqCurrAB_mid = reqAreaAB-currAB_mid;
				diffReqCurrAB_upper = reqAreaAB-currAB_upper;
				
				if (Math.abs(diffReqCurrAB_lower) <= isZeroL){
					returnVal = d_leftmost;
				
				} else if (Math.abs(diffReqCurrAB_mid) <= isZeroL){
					returnVal = d_mid;
				
				} else if (Math.abs(diffReqCurrAB_upper) <= isZeroL){
					returnVal = d_rightmost;
				} 

				else { 
					if ((diffReqCurrAB_lower * diffReqCurrAB_mid) < 0){ // diff of lower and diff of mid have different signs => when * get -ve ie <0
						d_rightmost = d_mid;
					} else {       // diff of lower and diff of mid have same signs => when * get +ve ie >0
						d_leftmost = d_mid;
					} 
					
					if (Math.abs(d_rightmost-d_leftmost) <= isZeroL){  //can remove Math.abs because upper must be > lower but leave it to be on the safe siden n// if ((d_upper-d_lower) < isZeroL){
						returnVal = d_mid;
					}
				}
			}
			if (returnVal != null){
				
				Ellipse e_a = new Ellipse ("a", r_a, r_a, 0, 0, rot_a);
				Ellipse e_b = new Ellipse ("b", r_b, r_b, 0, e_a.yc, rot_b);
				ArrayList<Ellipse> ells = new ArrayList<Ellipse>();
				ells.add(e_a);ells.add(e_b);
				EllipseDiagram diag = new EllipseDiagram(ells,considerLessPrecisePossibleIntPnts); 
				
				diag.getEllipses().get(1).setXc(diag.getEllipses().get(0).getXc()+returnVal); 
				diag.recomputeZoneAreas();
				
				if (diag.isValid()){
					return returnVal;
				} else {
					failCount++;
					d_leftmost = (r_a >= r_b)? (r_a - r_b) : (r_b - r_a); 
					d_rightmost = r_a + r_b;
					d_leftmost += (padding + isZeroL);
					d_rightmost -= (padding + isZeroL); 
					if (modifyRightmost){
						d_rightmost = d_rightmost - (stepMoveIfValidLimitNotFound * failCount);
						if (d_rightmost <= d_leftmost){
							modifyRightmost=false;
							failCount=1;
							d_rightmost = r_a + r_b;
							d_rightmost -= (padding + isZeroL);
						}
					}
					if (!modifyRightmost){
						d_leftmost = d_leftmost + (stepMoveIfValidLimitNotFound * failCount);
					}
					d_mid = 0;
					returnVal=null;
				}
			}
		}
	}
	
	
	private static boolean isZero(double x, double l){
		// this is primarily used when computing the gradient of a line
		double EQN_EPS = l;
		return ((x > -EQN_EPS) && (x < EQN_EPS));
	}

	
	private static boolean isInfinite(double x, double l){
		// this is primarily used when computing the gradient of a line
		return ((x < -l) || (x > l));
	}
		
}









