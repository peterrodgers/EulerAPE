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

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * To instantiate, define and manage the panel where the Euler diagram with ellipses is displayed
 * 
 */

public class EllipseDiagramPanel extends JPanel  {

	// Static class fields
	public static final int DIAGRAM_WIDTH = 720;
	public static final int DIAGRAM_HEIGHT = 650;
	public static final int DIAGRAM_PADDING = 0; 

	public static final int LABELS_SIMPLE = 0;
	public static final int LABELS_ADVANCED = 1;
	public static final int LABELS_HIDE = 2;

	public static final String FONT_NAME = "Arial";
	public static final int FONT_STYLE = Font.BOLD;
	public static final int FONT_SIZE = 12;//12;
	protected static final Color monoBackgroundColor = Color.white;
	protected static final Color colorBackgroundColor = Color.black;
	protected static final Color textColor = Color.black;
	public static Color ellipseColors[] = {Color.gray,Color.red,Color.green,Color.yellow,Color.blue,Color.magenta,Color.cyan,Color.white};
	
	
	// ... for display -> to paint components
	
	public static Rectangle diagBBox = new Rectangle(DIAGRAM_PADDING, DIAGRAM_PADDING, DIAGRAM_WIDTH, DIAGRAM_HEIGHT);
	public static Dimension ellipsePanelSize = new Dimension(DIAGRAM_WIDTH+(DIAGRAM_PADDING*2),DIAGRAM_HEIGHT+(DIAGRAM_PADDING*2));

	public static double paddingW = 0;
	public static double paddingH = 0;
	public static Point2D.Double centreOfSystem =  new Point2D.Double (((ellipsePanelSize.getWidth()-paddingW)/2), (ellipsePanelSize.getHeight()-paddingH)/2);
	public static Point2D.Double max_xy = new Point2D.Double ((ellipsePanelSize.getWidth()-paddingW)/2, (ellipsePanelSize.getHeight()-paddingH)/2);
	public static Point2D.Double min_xy = new Point2D.Double (-(ellipsePanelSize.getWidth()-paddingW)/2, -(ellipsePanelSize.getHeight()-paddingH)/2);

	
	// Instance data members 
	public HashMap<String, Double> requiredAreaSpecs = null;
	public EllipseDiagram diagram;
	public Boolean updateDiagramLabelsOnly = null; 

	public boolean useColor = true;
	public boolean randomColors = false;
	public int labelsDisplayMode = LABELS_SIMPLE;
	
	
	// for paint component for the texture fill
	BufferedImage mImage;
	Rectangle2D tr;
	TexturePaint tp;
	
	
	
	//  Constructor
	public EllipseDiagramPanel(EllipseDiagram diagram, HashMap<String, Double> requiredAreaSpecs) {

		super();
		
		this.diagram = diagram;
		this.requiredAreaSpecs = requiredAreaSpecs;
		this.updateDiagramLabelsOnly = null; 
		
		setPreferredSize(ellipsePanelSize);
		
		// for paint component -> for texture fill
		try {
			//use the line of code below for it to work when jared
			mImage = ImageIO.read(getClass().getResourceAsStream("/elliptic/areaproptool/TextureStripes.jpg")); //do not use getResource instead of getResourceAsStream because it will not work
			tr = new Rectangle2D.Double(0, 0.5, 11.5,11);
		    tp = new TexturePaint(mImage, tr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	// Properties -> getters and setters
	
	public HashMap<String, Double> getRequiredAreaSpecs() {
		return requiredAreaSpecs;
	}
	public void setRequiredAreaSpecs(HashMap<String, Double> requiredAreaSpecs) {
		this.requiredAreaSpecs = requiredAreaSpecs;
	}

	public EllipseDiagram getDiagram() {
		return diagram;
	}
	public void setDiagram(EllipseDiagram in_diagram) {
		this.diagram = in_diagram;
	}
	  
	public int getLabelsDisplayMode() {
		return labelsDisplayMode;
	}
	public void setLabelsDisplayMode(int labelsDisplayMode) {
		this.labelsDisplayMode = labelsDisplayMode;
	}



	
	// Methods 

	public String getDisplayZoneLabel(String zoneName, int type) {
		String simpleLabel = zoneName; 
		
		if (type == LABELS_SIMPLE){
			return simpleLabel;
		} else if (type == LABELS_ADVANCED){
			return (simpleLabel + ";" + Utilities.roundToDps(diagram.getZoneAreas().get(zoneName),10,2));//4));
		} else {
			return null;
		}
		
	}
	
	public Rectangle findCompletelyContainedRectangle(Area a) {
		
		Rectangle bbox = a.getBounds();

		// cant find a center of an area with a small or zero bounding box
		if(bbox.width < 3) {
			return null;
		}
		int divider = 3;
		Rectangle containedRectangle = null;
		int width; int height;
		int xGrid; int yGrid;
		int x; int y;
		Rectangle r = new Rectangle();
		while(containedRectangle == null) {
			width = bbox.width/divider;
			height = bbox.height/divider;
			// if we get to a zero size, there is no suitable rectangle
			if(width == 0 || height == 0) {
				return null;
			}
			
			for(xGrid = 1; xGrid < divider-1; xGrid++) {
				for(yGrid = 1; yGrid < divider-1; yGrid++) {
					x = bbox.x+xGrid*width;
					y = bbox.y+yGrid*height;
					r.x = x;
					r.y = y;
					r.width = width;
					r.height = height;
					if(a.contains(r)) {
						return r;
					}
				}
			}
			divider++;
		}

		return null;
	}
	
	public Point getLabelCenter(Area a) {
		/*
		 * Attempts to find the centre of the largest rectangle
		 * completely contained in the area. This is not always found,
		 * but usually when it is not the largest rectangle it is still
		 * a good result (typically the second largest).
		 */		
		Rectangle r = findCompletelyContainedRectangle(a);
		if(r == null) {
			return null;
		}
			
		// expand rectangle as far as possible
		
		while(a.contains(r)) {
			r.x--;
			r.width++;
		}
		r.x++; // last decrement takes r out of a
		r.width--;
		while(a.contains(r)) {
			r.y--;
			r.height++;
		}
		r.y++; // last decrement takes r out of a
		r.height--;
		while(a.contains(r)) {
			r.width++;
		}
		r.width--; // last increment takes r out of a
		while(a.contains(r)) {
			r.height++;
		}
		r.height--; // last increment takes r out of a
		
		int x = r.x+r.width/2;
		int y = r.y+r.height/2;
		
		return new Point(x,y);
	}


	public void putZoneLabel(Graphics2D g2, int zoneIndex, Area zone, String label) {
			
		if(label.length() == 0) {
			return;
		}
			
		int x = 0;
		int y = 0;
		Point center = getLabelCenter(zone);
		if(center != null) {
			x = center.x;
			y = center.y;
		} else {
			x = 100+zoneIndex*50;
			y = 20;
		}

		Font font = new Font(FONT_NAME,FONT_STYLE,FONT_SIZE);
		FontRenderContext frc = g2.getFontRenderContext();
		TextLayout labelLayout = new TextLayout(label, font, frc);

		Rectangle2D textBBox = labelLayout.getBounds();
			
		x = x - (int)textBBox.getWidth()/2;
		y = y + (int)textBBox.getHeight()/2;
			
		textBBox.setRect(textBBox.getX()+x-2, textBBox.getY()+y-2, textBBox.getWidth()+4,textBBox.getHeight()+4);
		
		g2.setColor(textColor);
		labelLayout.draw(g2,x,y);
	}
	
	
	public void paintComponent(Graphics g) {
		
		final int SOLID = 0;
		final int TEXTURE = 1;
		final int OUTLINE = 2;
		int[] ellipseStyle = {SOLID, TEXTURE, OUTLINE};
		
		Color cPink_outline = new Color(231, 115, 94, 128); //cmyk -> (0,63,60,0) -> for outline; for filling cmyk->(0,54,52,0) rgb->(233,130,108) with 60% transparency
		Color cPink_fill = new Color(233, 130, 108, 128); //cmyk -> (0,63,60,0) -> for outline; for filling cmyk->(0,54,52,0) rgb->(233,130,108) with 60% transparency
		Color cGreen_texture = new Color(0, 147, 128, 128); //cmyk -> (100,0,59,0)
		Color cBlue_outline = new Color(33, 44, 119, 128);  //cmyk -> (100,88,0,5)
		
		int oulineThickness_solid = 1;
		int oulineThickness_texture = 1;
		int oulineThickness_outline = 2;
		
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, (RenderingHints.VALUE_ANTIALIAS_ON)); 
		
		boolean updateLabelsOnly = ((updateDiagramLabelsOnly!=null) && updateDiagramLabelsOnly) ? true : false;
		
		if (!updateLabelsOnly){
			if(useColor) {
				setBackground(Color.white);
			} else {
				setBackground(monoBackgroundColor);
			}
			
			// paint background
			super.paintComponent(g2);
		}
		if (diagram == null){return;}
		
		// Scale the displayed diagram not the actual one -> this avoid confusion with requiredAreaSpecs and currZoneAreas
		//might run slow or run out of memory esp if have batch tests
		EllipseDiagram diagram_scaledToFit = diagram.clone();
		scaleDiag(diagram_scaledToFit); // note that scaleDiag(); with no input param will scale the diag in the panel

		
		// paint zones 
		Area setArea[] = EllipseDiagramOps.getSetsAsAreas_3EllDiag(diagram_scaledToFit,centreOfSystem); 
		
        if (!updateLabelsOnly){
			if(useColor) {
				for(int i = 0; i < 3; i++) {
					
				    Composite prevComposite;
					switch (ellipseStyle[i]){
						case SOLID: {
							// fill
							g2.setColor(cPink_fill);
							g2.fill(setArea[i]);
							// outline
							g2.setPaint(cPink_outline);
							g2.setStroke(new BasicStroke(oulineThickness_solid));
						    g2.draw(setArea[i]);
						    
							break;
						}
						case TEXTURE: {
							g2.setColor(cGreen_texture);
						    prevComposite = g2.getComposite();
						    Composite c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f); //the smaller the value .4f the more transparent
						    g2.setComposite(c);
							g2.setPaint(tp);
							g2.fill(setArea[i]);
							g2.setComposite(prevComposite);
							
							// outline
							g2.setPaint(cGreen_texture);
							g2.setStroke(new BasicStroke(oulineThickness_texture));
						    g2.draw(setArea[i]);
							break;
						}
						case OUTLINE: {
							// outline
							g2.setPaint(cBlue_outline);
							g2.setStroke(new BasicStroke(oulineThickness_outline)); 
						    g2.draw(setArea[i]);
							break;
						}
					}
				}
			} else {
				g2.setColor(Color.black);
				g2.draw(setArea[0]);
				g2.draw(setArea[1]);
				g2.draw(setArea[2]);
			}
    	}

		// paint zone labels 
		if(labelsDisplayMode != LABELS_HIDE) {

			Area zoneArea[] = EllipseDiagramOps.getZonesAsAreas_3EllDiag(diagram_scaledToFit,centreOfSystem); 
			for(int i = 1; i < 8; i++) {
				if(labelsDisplayMode == LABELS_SIMPLE) {
					putZoneLabel (g2, i, zoneArea[i], getDisplayZoneLabel(diagram_scaledToFit.getZoneLabels()[i-1], LABELS_SIMPLE));
				} else if(labelsDisplayMode == LABELS_ADVANCED) {
					putZoneLabel (g2, i, zoneArea[i], getDisplayZoneLabel(diagram_scaledToFit.getZoneLabels()[i-1], LABELS_ADVANCED));
				}
			}
		}
	}
	
	public void paint(Graphics g) {
		paintComponent(g);
	}
	

	public void centreDiag(){
		diagram.centreToPanel();
	}
	public void scaleDiag(){
		diagram.scaleToFit(diagBBox);
	}
	
	public void scaleDiag(EllipseDiagram in_diag){
		in_diag.scaleToFit(diagBBox);
	}
	
	public void updateDisplay(int millisecondDelay, boolean scaleDiag) {
		
		if (millisecondDelay>0){sleep(millisecondDelay);}
		if (scaleDiag && (diagram != null)){
			scaleDiag();
		}
		
		paintImmediately(EllipseDiagramPanel.diagBBox); //perfect -> no flickers		
		updateDiagramLabelsOnly = null;
	}
	
	public boolean sleep(int time) {
		try {
			Thread.sleep(time);
		} catch(Exception e) {
			System.out.println("EllipseDiagramPanel.sleep: Exception occurred in Thread.sleep() in CirclePanel.sleep "+e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

}




