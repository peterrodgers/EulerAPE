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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import uk.ac.kent.elliptic.areaproptool.EllipseDiagram.FitnessMeasure;
import uk.ac.kent.elliptic.areaproptool.EllipseDiagramOps.InitDiagType;


/**
 * 
 * Instantiate, define and handle the main controls of eulerAPE to the left of the Euler diagram display panel
 *
 */



public class SwitchBoardPanel extends JPanel {
	

	// Static fields
	
	public static final int BUTTON_WIDTH = 10;
	public static final int INPUT_TEXT_SIZE = 3;
	public static final int INPUT_LABEL_SIZE = 8;
	
	
	// Constants
	
	public static final int ZONEAREAS_DIGITS_BEFORE_DP = 10;
	public static final int ZONEAREAS_DIGITS_AFTER_DP = 1; 
	public static final int ZONEVARIANCES_DIGITS_BEFORE_DP = 10;
	public static final int ZONEVARIANCES_DIGITS_AFTER_DP = 1; 
	public static final int FITNESS_DIGITS_BEFORE_DP = 10; 
	public static final int FITNESS_DIGITS_AFTER_DP = 2;
	
	public static final int INITDIAG_DEFAULT = 0;
	public static final int INITDIAG_RANDOM_ELLIPSES = 1;
	public static final int INITDIAG_RANDOM_CIRCLES = 2;
	public static final int INITDIAG_LOAD = 3;
	
	public static final boolean LOAD_AS_FROM_FILE__RECOMPUTE_ZONE_AREAS = true;
	public static final boolean RANDOM_DIAG__LOAD_FROM_LIB_IF_AVAILABLE = false;
	public static final boolean RUN_FOR_CIRCLES_AND_ELLIPSES = false; 

	public static final String TESTSUMMARYLOGFILENAME = "testSummaryLog"; 
	public static final String TESTDIAGSSPECSONRUNTERMLOGFILENAME = "testDiagsSpecsOnRunTermLog";
	
	public static enum Curves {CIRCLES, ELLIPSES};

	
	// Instance data fields
	
	protected Random r = new Random();
	
	// ... input and display components
	protected JPanel initDiagPanel;
	protected JPanel paramsHCPanel;
	protected JPanel logfilePanel;
	protected JPanel colorPanel;
	protected JPanel stressPanel;
	protected JPanel aestheticsPanel;
	protected JPanel labelsPanel;
	protected JPanel generateLibPanel;
	protected JPanel viewSearchPanel;
	protected JPanel tableEntryPanel;
		
	protected JButton genRandomAreaSpecsButton;
	protected JButton loadFromFileAreaSpecsButton;
	protected JButton clearAllAreaSpecsButton;
	protected JButton browseInitDiagButton;
	protected JButton refreshInitDiagButton;
	protected JButton resetParamsHCButton;
	protected JButton browseLogfileDirButton;
	protected JButton browseEldFileButton;
	protected JButton generateLibButton;
	protected JButton validateLibButton;
	protected JButton run1aHCButton;
	protected JButton runHCButton;
	protected JButton run2aHCButton;
	protected JButton run2bHCButton;
	protected JButton runAllHCMethodsButton;
	protected JButton evaluateEffectivenessButton;
	
	protected JRadioButton colorRadioButton;
	protected JRadioButton monochromeRadioButton;
	protected JRadioButton defaultInitDiagRadioButton;
	protected JRadioButton randomEllipsesInitDiagRadioButton;
	protected JRadioButton randomCirclesInitDiagRadioButton;
	protected JRadioButton fromFileInitDiagRadioButton;
	protected JRadioButton yesShowLabelsRadioButton;
	protected JRadioButton noShowLabelsRadioButton;
	protected JRadioButton yesColourRadioButton;
	protected JRadioButton noColourRadioButton;
	protected JRadioButton simpleLabelRadioButton;
	protected JRadioButton advancedLabelRadioButton;
	protected JRadioButton hideLabelRadioButton;
	protected JRadioButton ellipsesHCFieldRadioButton;	
	protected JRadioButton circlesHCFieldRadioButton;
	protected JRadioButton yesViewSearchRadioButton;
	protected JRadioButton noViewSearchRadioButton;
	
	protected JTextField diagErrorField;
	protected JTextField filePathInitDiagField;
	protected JTextField dHCField;
	protected JTextField sHCField;
	protected JTextField rHCField;
	protected JTextField logfileDirPathField;
	protected JTextField logfileFileNameField;
	protected JTextField generateLibNoOfDiagPathField;
	protected JTextField generateLibDirPathField;
	protected JTextField message1Field;
	protected JTextField message2Field;
	protected JTextField message3Field;
	
	protected JTextField[] requiredAreaSpecsField = new JTextField[7];
	protected JTextField[] requiredAreaSpecsPCField = new JTextField[7];
	protected JTextField[] currAreaField = new JTextField[7];
	protected JTextField[] currAreaPCField = new JTextField[7];
	protected JTextField[] diffReqActualField = new JTextField[7];
	protected JTextField[] diffReqActualPCField = new JTextField[7];
	
	
	protected JLabel rHCLabel;
	
	// ... switchboard options 
	protected int initDiag = INITDIAG_DEFAULT;
	protected InitDiagType initDiagDefaultType = InitDiagType.BISECTION_ON_SLOPE; 
	protected double initdiagNoEmptyDisconnLikeZones_PolyAreaDiscardThreshold = 1;
	protected boolean loadReqAreaSpecsFromFields = true; 
	protected boolean runUserFriendlyMode = true;
	
	protected FitnessMeasure fitnessMeasure = EllipseDiagram.DEFAULT_FITNESS_MEASURE;
	
	protected boolean dislayRunModeInFileName = false;
	protected boolean saveDiagImgFile = true;
	protected boolean saveDiagSVGFile = true;
	protected boolean saveDiagELDFile = true;
	protected boolean saveFinalDiagOnly = false;
	protected boolean runningATest = false;
	protected boolean currentlyDrawingDiag = false;
	
	
	protected boolean wereReqRegionAreasMult = false;
	protected double reqRegionAreasMultFactor;
	protected boolean checkedIfRequiredRegionAreasShouldBeScaled = false;
	
	protected boolean informedAboutOverwritingOfFiles = false;
	
	protected int fileNameOfSelectedDiagAreaSpecs_sameCount=0;
	protected String prevFileNameOfSelectedDiagAreaSpecs="";
	protected String currentUserDir="";
	
	protected boolean considerLessPreciseIntPnts=false;
	
	protected HillClimber.RunType hcRunType = HillClimber.RunType.RunWithInitParamsOnly; 

	
	// ... diagram and hill climber properties  
	protected int noOfEllipses = 3;
	protected EllipseDiagramPanel ellipseDiagramPanel;
	protected HashMap<String, Double> requiredAreaSpec = null; 
	protected HashMap<String, Double> internalLoadedOrSetRequiredAreaSpecs = null;  
	protected HillClimber hillClimber = null;
	protected String hcRunDetails = "";
	protected String[] hcRunAreaDetails;
	
	// Location of directories and files
	protected String diagLibMainDirPath = "C:\\Users\\Luana\\Desktop\\Tests\\DiagLib\\2011-11-04\\Ellipses\\Venn-3"; 
	protected String ellipsesDiagDirName = "Ellipses";
	protected String circlesDiagDirName = "Circles";
	
	protected String areaSpecsListPath = "C:\\Users\\Luana\\Desktop\\Tests";
	protected String areaSpecsListFileName = "areaSpecsList.eldl";
	protected String fileNameOfSelectedDiagAreaSpecs = "";
	
	protected String defaultInitDiagDirName = "SymmAreaSpecs";
	protected String defaultInitDiagDirPath = areaSpecsListPath + "\\InitDiag\\2.eld"; 
	protected String defaultInitDiagFileFullPath = areaSpecsListPath + "\\InitDiag\\2.eld"; 
	
	protected File savingInitDiagImgFile;
	protected File savingInitDiagSVGFile;
	protected File savingInitDiagFile;
	protected File savingFinalDiagImgFile;
	protected File savingFinalDiagSVGFile;
	protected File savingFinalDiagFile;
	
	protected String logFileName = "hc"; 
	protected String defaultLogfileDir = ""; 
	protected String logfileDir;
	protected File logfileFile;
	protected String testSummaryLogFileName = TESTSUMMARYLOGFILENAME; 
	protected String testInitDiagSummaryLogFileName = "testInitDiagSummaryLog";
	protected String testDiagsSpecsOnRunTermLogFileName = TESTDIAGSSPECSONRUNTERMLOGFILENAME; 
	protected String testDiagListFailedToLoadASFileName = "diagListFailedToLoadAS";
	protected String testDiagListFailedToGenInitDiagFileName = "diagListFailedToGenInitDiag";
	protected String testInvalidDiagsFileName = "invalidDiags";
	protected String testZAsNotComputedSuccFileName = "zasNotComputedSucc";
	protected File initDiagFile;

	public static final int MAX_NO_OF_ITERATIONS = 10000;
	
    protected boolean forceToTermHC = true;
	protected boolean genInitDiagOnly = false;
    
	
	// Constructor
	public SwitchBoardPanel(EllipseDiagramPanel ellipseDiagramPanel, int startingLabelsStatus) {

		super();

		this.ellipseDiagramPanel = ellipseDiagramPanel;
		if ((this.ellipseDiagramPanel.getDiagram() != null) && (this.ellipseDiagramPanel.getDiagram().ellipses.size() > 0)){
			noOfEllipses = this.ellipseDiagramPanel.getDiagram().ellipses.size();
		}
		
		ellipseDiagramPanel.labelsDisplayMode = startingLabelsStatus;

		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);
		addWidgets(this,gridbag);

		setVisible(true);
	}


	protected void addWidgets(JPanel panel, GridBagLayout gridbag) {

		Border etchedBorder = BorderFactory.createEtchedBorder();
		Border spaceBorder1 = BorderFactory.createEmptyBorder(10,5,15,15);
		Border spaceBorder2 = BorderFactory.createEmptyBorder(3,3,5,5);
		Border compoundBorder = BorderFactory.createCompoundBorder(etchedBorder,spaceBorder1);
		Border panelBorder = BorderFactory.createCompoundBorder(spaceBorder2,compoundBorder);
		
		GridBagConstraints c = new GridBagConstraints();

		int yLevel = 0;

		JLabel label;
		
		Font fontButton = new Font("Arial", Font.PLAIN, 12);
		
		// Table with details for every zone
		tableEntryPanel = new JPanel();
		tableEntryPanel.setBorder(panelBorder);
		tableEntryPanel.setOpaque(false);
		
		c.gridx = 0;
		c.gridy = yLevel;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.WEST; 
		gridbag.setConstraints(tableEntryPanel,c);
		panel.add(tableEntryPanel);
		
		GridBagLayout tableEntryPanelGridbag = new GridBagLayout();
		tableEntryPanel.setLayout(tableEntryPanelGridbag);
		GridBagConstraints tableEntryPanelC = new GridBagConstraints();
		tableEntryPanelC.ipadx = 12;
		tableEntryPanelC.ipady = 5; 
			
		// ... column headings 
		
		label = new JLabel("region", SwingConstants.CENTER);
		label.setOpaque(false);
		tableEntryPanelC.gridx = 0;
		tableEntryPanelC.gridy = yLevel;
		tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
		tableEntryPanel.add(label);

		label = new JLabel("required", SwingConstants.CENTER);
		label.setOpaque(false);
		tableEntryPanelC.gridx = 1;
		tableEntryPanelC.gridy = yLevel;
		tableEntryPanelC.gridwidth=2;
		tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
		tableEntryPanel.add(label);

		label = new JLabel("actual", SwingConstants.CENTER);
		label.setOpaque(false);
		tableEntryPanelC.gridx = 4;
		tableEntryPanelC.gridy = yLevel;
		tableEntryPanelC.gridwidth=2;
		tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
		tableEntryPanel.add(label);
		
		label = new JLabel("required - actual", SwingConstants.CENTER);
		label.setOpaque(false);
		tableEntryPanelC.gridx = 7;
		tableEntryPanelC.gridy = yLevel;
		tableEntryPanelC.gridwidth=2;
		tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
		tableEntryPanel.add(label);

		yLevel++;
		
		label = new JLabel("", SwingConstants.CENTER);
		label.setOpaque(false);
		tableEntryPanelC.gridx = 0;
		tableEntryPanelC.gridy = yLevel;
		tableEntryPanelC.gridwidth=1;
		tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
		tableEntryPanel.add(label);

		label = new JLabel("area", SwingConstants.CENTER);
		label.setOpaque(false);
		label.setFont(fontButton);
		tableEntryPanelC.gridx = 1;
		tableEntryPanelC.gridy = yLevel;
		tableEntryPanelC.gridwidth=1;
		tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
		tableEntryPanel.add(label);

		label = new JLabel("% area", SwingConstants.CENTER);
		label.setOpaque(false);
		label.setFont(fontButton);
		tableEntryPanelC.gridx = 2;
		tableEntryPanelC.gridy = yLevel;
		tableEntryPanelC.gridwidth=1;
		tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
		tableEntryPanel.add(label);
		
		label = new JLabel("area", SwingConstants.CENTER);
		label.setOpaque(false);
		label.setFont(fontButton);
		tableEntryPanelC.gridx = 4;
		tableEntryPanelC.gridy = yLevel;
		tableEntryPanelC.gridwidth=1;
		tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
		tableEntryPanel.add(label);
		
		label = new JLabel("% area", SwingConstants.CENTER);
		label.setOpaque(false);
		label.setFont(fontButton);
		tableEntryPanelC.gridx = 5;
		tableEntryPanelC.gridy = yLevel;
		tableEntryPanelC.gridwidth=1;
		tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
		tableEntryPanel.add(label);		
		
		label = new JLabel("area", SwingConstants.CENTER);
		label.setOpaque(false);
		label.setFont(fontButton);
		tableEntryPanelC.gridx = 7;
		tableEntryPanelC.gridy = yLevel;
		tableEntryPanelC.gridwidth=1;
		tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
		tableEntryPanel.add(label);
		
		label = new JLabel("% area", SwingConstants.CENTER);
		label.setOpaque(false);
		label.setFont(fontButton);
		tableEntryPanelC.gridx = 8;
		tableEntryPanelC.gridy = yLevel;
		tableEntryPanelC.gridwidth=1;
		tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
		tableEntryPanel.add(label);				
		
		
		
		// ... table entries 
		
		EllipseDiagram diagram = ellipseDiagramPanel.getDiagram();
		String[] zoneLabels = null;
		HashMap<String, Double> currentZoneAreas = null;
		HashMap<String,  Double> currentZoneVariances = null;
		Double diagErrorCurrentDiagram = null;
		if (diagram == null){
			zoneLabels = EllipseDiagramOps.getZoneLabels(noOfEllipses, false);
		} else {
			zoneLabels = diagram.getZoneLabels();
			currentZoneAreas = diagram.getZoneAreas();
			currentZoneVariances=diagram.computeFitnessOfAllRegions(requiredAreaSpec, fitnessMeasure);
			diagErrorCurrentDiagram = diagram.computeFitnessOfDiagram(requiredAreaSpec, currentZoneVariances, fitnessMeasure);
		} 
		
		double currentDiagAreaTotal = 0;
		double requiredDiagAreaTotal = 0;
		for(String zl : zoneLabels) {
			if ((currentZoneAreas != null) && (currentZoneAreas.get(zl) != null)) {currentDiagAreaTotal += currentZoneAreas.get(zl);}
			if ((requiredAreaSpec != null) && (requiredAreaSpec.get(zl) != null)) {requiredDiagAreaTotal += requiredAreaSpec.get(zl);}
		}
		
		Insets inTextfieldPadding = new Insets(1,5,1,5);
		Insets inButtonfieldPadding = new Insets(0,1,0,1);
		
		int z = 0;
		for(String zl : zoneLabels) {
			yLevel++;
	
			label = new JLabel(zl, SwingConstants.CENTER);
			label.setOpaque(false);
			label.setFont(fontButton);
			tableEntryPanelC.gridx = 0;
			tableEntryPanelC.gridy = yLevel;
			tableEntryPanelC.gridwidth=1;
			tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
			tableEntryPanel.add(label);

			requiredAreaSpecsField[z] = new JTextField(INPUT_TEXT_SIZE+1); //+3
			requiredAreaSpecsField[z].setMargin(inTextfieldPadding);
			requiredAreaSpecsField[z].setHorizontalAlignment(JTextField.LEFT);
			requiredAreaSpecsField[z].setText((requiredAreaSpec == null) ? "" : Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(requiredAreaSpec.get(zl), ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP)))); 
			requiredAreaSpecsField[z].setCaretPosition(0);
			requiredAreaSpecsField[z].setFocusable(true);
			requiredAreaSpecsField[z].setBackground(Color.WHITE);
			requiredAreaSpecsField[z].addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					onKeyReleasedRequiredAreaSpecsField(e);
				}
			});
			tableEntryPanelC.gridx = 1;
			tableEntryPanelC.gridy = yLevel;
			tableEntryPanelC.gridwidth=1;
			tableEntryPanelGridbag.setConstraints(requiredAreaSpecsField[z],tableEntryPanelC);
			tableEntryPanel.add(requiredAreaSpecsField[z]);
			
			
			requiredAreaSpecsPCField[z] = new JTextField(INPUT_TEXT_SIZE);//+3
			requiredAreaSpecsPCField[z].setMargin(inTextfieldPadding);
			requiredAreaSpecsPCField[z].setEditable(false);
			requiredAreaSpecsPCField[z].setHorizontalAlignment(JTextField.LEFT);
			requiredAreaSpecsPCField[z].setText(((requiredAreaSpec == null)||(requiredAreaSpec.get(zl) == null)) ? "" : 
				Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(
                (requiredAreaSpec.get(zl)/requiredDiagAreaTotal)*100, ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP)))+"%");
			requiredAreaSpecsPCField[z].setFocusable(false);
			requiredAreaSpecsPCField[z].setBackground(Color.WHITE);
			tableEntryPanelC.gridx = 2;
			tableEntryPanelC.gridy = yLevel;
			tableEntryPanelC.gridwidth=1;
			tableEntryPanelGridbag.setConstraints(requiredAreaSpecsPCField[z],tableEntryPanelC);
			tableEntryPanel.add(requiredAreaSpecsPCField[z]);
			
			label = new JLabel("", SwingConstants.LEFT);
			label.setOpaque(false);
			tableEntryPanelC.gridx = 3;
			tableEntryPanelC.gridy = yLevel;
			tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
			tableEntryPanel.add(label);

			currAreaField[z] = new JTextField(INPUT_TEXT_SIZE+1);//+3
			currAreaField[z].setMargin(inTextfieldPadding);
			currAreaField[z].setEditable(false);
			currAreaField[z].setHorizontalAlignment(JTextField.LEFT);
			currAreaField[z].setText((currentZoneAreas == null) ? "" : (wereReqRegionAreasMult?Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(currentZoneAreas.get(zl)/reqRegionAreasMultFactor, ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP))):Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(currentZoneAreas.get(zl), ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP)))));
			currAreaField[z].setFocusable(false);
			currAreaField[z].setBackground(Color.WHITE);
			tableEntryPanelC.gridx = 4;
			tableEntryPanelC.gridy = yLevel;
			tableEntryPanelC.gridwidth=1;
			tableEntryPanelGridbag.setConstraints(currAreaField[z],tableEntryPanelC);
			tableEntryPanel.add(currAreaField[z]);

			currAreaPCField[z] = new JTextField(INPUT_TEXT_SIZE);
			currAreaPCField[z].setMargin(inTextfieldPadding);
			currAreaPCField[z].setEditable(false);
			currAreaPCField[z].setHorizontalAlignment(JTextField.LEFT);
			currAreaPCField[z].setText(((currentZoneAreas == null)||(currentZoneAreas.get(zl) == null)) ? "" : 
				   Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(
			       (currentZoneAreas.get(zl)/currentDiagAreaTotal)*100, ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP)))+"%");currAreaPCField[z].setFocusable(false);
			currAreaPCField[z].setBackground(Color.WHITE);
			tableEntryPanelC.gridx = 5;
			tableEntryPanelC.gridy = yLevel;
			tableEntryPanelC.gridwidth=1;
			tableEntryPanelGridbag.setConstraints(currAreaPCField[z],tableEntryPanelC);
			tableEntryPanel.add(currAreaPCField[z]);
	
			
			label = new JLabel("", SwingConstants.LEFT);
			label.setOpaque(false);
			tableEntryPanelC.gridx = 6;
			tableEntryPanelC.gridy = yLevel;
			tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
			tableEntryPanel.add(label);
			
			diffReqActualField[z] = new JTextField(INPUT_TEXT_SIZE+1);
			diffReqActualField[z].setMargin(inTextfieldPadding);
			diffReqActualField[z].setEditable(false);
			diffReqActualField[z].setHorizontalAlignment(JTextField.LEFT);
			diffReqActualField[z].setFocusable(false);
			diffReqActualField[z].setBackground(Color.WHITE);
			diffReqActualField[z].setText(((currentZoneAreas == null)||(currentZoneAreas.get(zl) == null)) ? "" : 
				
				Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps((
						 (Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(requiredAreaSpecsField[z].getText()))-
						  Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(currAreaField[z].getText())))
						  ==-0.0)?
								  0.0:
									  (Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(requiredAreaSpecsField[z].getText()))-
									   Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(currAreaField[z].getText()))), ZONEVARIANCES_DIGITS_BEFORE_DP, ZONEVARIANCES_DIGITS_AFTER_DP))));
			
		
			tableEntryPanelC.gridx = 7;
			tableEntryPanelC.gridy = yLevel;
			tableEntryPanelC.gridwidth=1;
			tableEntryPanelGridbag.setConstraints(diffReqActualField[z],tableEntryPanelC);
			tableEntryPanel.add(diffReqActualField[z]);
			
			diffReqActualPCField[z] = new JTextField(INPUT_TEXT_SIZE); 
			diffReqActualPCField[z].setMargin(inTextfieldPadding);
			diffReqActualPCField[z].setEditable(false);
			diffReqActualPCField[z].setHorizontalAlignment(JTextField.LEFT);
			diffReqActualPCField[z].setFocusable(false);
			diffReqActualPCField[z].setBackground(Color.WHITE);
			diffReqActualPCField[z].setText((diffReqActualField[z].getText().trim().equals(""))?"":
					Double.toString(Utilities.roundToDps(
					((Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(requiredAreaSpecsPCField[z].getText().replace("%",""))))-
					(Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(currAreaPCField[z].getText().replace("%",""))))), 
					ZONEVARIANCES_DIGITS_BEFORE_DP, ZONEVARIANCES_DIGITS_AFTER_DP))+"%");
	
			tableEntryPanelC.gridx = 8;
			tableEntryPanelC.gridy = yLevel;
			tableEntryPanelC.gridwidth=1;
			tableEntryPanelGridbag.setConstraints(diffReqActualPCField[z],tableEntryPanelC);
			tableEntryPanel.add(diffReqActualPCField[z]);
			
			z++;
		}
		
		yLevel++;
		
		
		// Random / Load Area Specification
		
		// ... random 
		genRandomAreaSpecsButton = new JButton("random");
		genRandomAreaSpecsButton.setPreferredSize(new Dimension(68, 18));
		genRandomAreaSpecsButton.setMargin(inButtonfieldPadding);
		genRandomAreaSpecsButton.setFont(fontButton);
		genRandomAreaSpecsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				genRandomAreaSpecsButton();
			}
		});
		genRandomAreaSpecsButton.setFocusable(true);
				
		tableEntryPanelC.gridx = 1;
		tableEntryPanelC.gridy = yLevel;
		tableEntryPanelC.gridwidth = 1;
		tableEntryPanelC.ipadx=0; c.ipady=0;
		tableEntryPanelC.anchor = GridBagConstraints.CENTER;
		tableEntryPanelGridbag.setConstraints(genRandomAreaSpecsButton,tableEntryPanelC);
		tableEntryPanel.add(genRandomAreaSpecsButton);
		
		// ... load 
		loadFromFileAreaSpecsButton = new JButton("load");
		loadFromFileAreaSpecsButton.setPreferredSize(new Dimension(68, 18));
		loadFromFileAreaSpecsButton.setMargin(inButtonfieldPadding);
		loadFromFileAreaSpecsButton.setFont(fontButton);
		loadFromFileAreaSpecsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				loadFromFileAreaSpecButton();
			}
		});
		loadFromFileAreaSpecsButton.setFocusable(true);
				
		tableEntryPanelC.gridx = 1; 
		tableEntryPanelC.gridy = yLevel+1;
		tableEntryPanelC.gridwidth = 1; //6
		tableEntryPanelC.ipadx=0; c.ipady=0;
		//tableEntryPanelC.fill = GridBagConstraints.VERTICAL;
		tableEntryPanelC.anchor = GridBagConstraints.CENTER;
		tableEntryPanelGridbag.setConstraints(loadFromFileAreaSpecsButton,tableEntryPanelC);
		tableEntryPanel.add(loadFromFileAreaSpecsButton);
		
		// ... clear all 
		clearAllAreaSpecsButton = new JButton("clear all");
		clearAllAreaSpecsButton.setPreferredSize(new Dimension(56,18));
		clearAllAreaSpecsButton.setMargin(inButtonfieldPadding);
		clearAllAreaSpecsButton.setFont(fontButton);
		clearAllAreaSpecsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				clearAllAreaSpecsButton();
			}
		});
		clearAllAreaSpecsButton.setFocusable(true);
				
		tableEntryPanelC.gridx = 2; //0;
		tableEntryPanelC.gridy = yLevel+1;
		tableEntryPanelC.gridwidth = 1; //6
		tableEntryPanelC.ipadx=0; c.ipady=0;
		tableEntryPanelC.anchor = GridBagConstraints.CENTER;
		tableEntryPanelGridbag.setConstraints(clearAllAreaSpecsButton,tableEntryPanelC);
		tableEntryPanel.add(clearAllAreaSpecsButton);
				
		
		// diagError
		label = new JLabel("diagError", SwingConstants.LEFT);
		Font newLabelFont=new Font(label.getFont().getName(),Font.ITALIC,label.getFont().getSize());
		label.setFont(newLabelFont);
		label.setHorizontalAlignment(JLabel.RIGHT);
		tableEntryPanelC.gridx = 5;
		tableEntryPanelC.gridy = yLevel+1;
		tableEntryPanelC.gridwidth = 1;	
		tableEntryPanelGridbag.setConstraints(label,tableEntryPanelC);
		tableEntryPanel.add(label);
		
		diagErrorField = new JTextField(INPUT_TEXT_SIZE);
		diagErrorField.setMargin(inTextfieldPadding);
		diagErrorField.setPreferredSize(new Dimension(15,18));
		diagErrorField.setText((diagErrorCurrentDiagram == null) ? "" : Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(diagram.diagError(requiredAreaSpec), FITNESS_DIGITS_BEFORE_DP, FITNESS_DIGITS_AFTER_DP))));
		diagErrorField.setHorizontalAlignment(JTextField.CENTER);
		diagErrorField.setEditable(false);
		diagErrorField.setBackground(Color.WHITE);
		
		tableEntryPanelC.gridx = 7;
		tableEntryPanelC.gridy = yLevel+1;
		tableEntryPanelC.gridwidth = 2;
		tableEntryPanelC.anchor = GridBagConstraints.WEST;
		tableEntryPanelC.fill = GridBagConstraints.BOTH;
		tableEntryPanelGridbag.setConstraints(diagErrorField,tableEntryPanelC);
		tableEntryPanel.add(diagErrorField);
		
		yLevel++;
		
		
		
		// Log File Path Sub-Panel
		
		logfilePanel = new JPanel();
		logfilePanel.setBorder(panelBorder);
		logfilePanel.setOpaque(false);
		
		c.gridx = 0;
		c.gridy = yLevel;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.WEST; 
		gridbag.setConstraints(logfilePanel,c);
		panel.add(logfilePanel);
		
		GridBagLayout logfilePanelGridbag = new GridBagLayout();
		logfilePanel.setLayout(logfilePanelGridbag);
		GridBagConstraints logfilePanelC = new GridBagConstraints();
	
		
		// ... label
		label = new JLabel("SAVE TO FILE", SwingConstants.LEFT);
		logfilePanelC.gridx = 0;
		logfilePanelC.gridy = 0;
		logfilePanelC.anchor = GridBagConstraints.WEST;
		logfilePanelGridbag.setConstraints(label,logfilePanelC);
		logfilePanel.add(label);
		
		// ... label
		label = new JLabel("      Directory   ", SwingConstants.LEFT);
		label.setFont(fontButton);
		logfilePanelC.gridx = 0;
		logfilePanelC.gridy = 1;
		logfilePanelC.anchor = GridBagConstraints.WEST;
		logfilePanelGridbag.setConstraints(label,logfilePanelC);
		logfilePanel.add(label);
		
		// ... text field -> path of diagram file to load
		logfileDirPathField = new JTextField(INPUT_TEXT_SIZE+12);
		logfileDirPathField.setText("");
		logfileDirPathField.setHorizontalAlignment(JTextField.LEFT);
		logfileDirPathField.setCaretPosition(0);
		logfileDirPathField.setFocusable(true);
		
		logfilePanelC.gridwidth = 2;
		logfilePanelC.gridx = 1;
		logfilePanelC.gridy = 1;
		logfilePanelGridbag.setConstraints(logfileDirPathField,logfilePanelC);
		logfilePanel.add(logfileDirPathField);
		
		
		label = new JLabel("", SwingConstants.LEFT);
		label.setOpaque(false);
		logfilePanelC.gridx = 3;
		logfilePanelC.gridy = 1;
		logfilePanelC.gridwidth = 1;
		logfilePanelGridbag.setConstraints(label,logfilePanelC);
		tableEntryPanel.add(label);
		
		
		
		// ... button -> browse for diagram file to load 
		browseLogfileDirButton = new JButton("browse");
		//browseLogfileDirButton.setPreferredSize(new Dimension(60, 20));
		browseLogfileDirButton.setMargin(inButtonfieldPadding);
		browseLogfileDirButton.setFont(fontButton);
		browseLogfileDirButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				browseLogfileDirButton();
			}
		});
		browseLogfileDirButton.setFocusable(true);
		
		logfilePanelC.gridx = 4;
		logfilePanelC.gridy = 1;
		logfilePanelC.gridwidth = 1;
		logfilePanelC.fill = GridBagConstraints.HORIZONTAL;
		logfilePanelC.anchor = GridBagConstraints.CENTER;
		logfilePanelGridbag.setConstraints(browseLogfileDirButton,logfilePanelC);
		logfilePanel.add(browseLogfileDirButton);
		
		// ... label
		label = new JLabel("      File name   ", SwingConstants.LEFT);
		label.setFont(fontButton);
		logfilePanelC.gridx = 0;
		logfilePanelC.gridy = 2;
		logfilePanelC.anchor = GridBagConstraints.WEST;
		logfilePanelGridbag.setConstraints(label,logfilePanelC);
		logfilePanel.add(label);
		
		// ... text field -> path of diagram file to load
		logfileFileNameField = new JTextField(INPUT_TEXT_SIZE+12);
		logfileFileNameField.setText("");
		logfileFileNameField.setHorizontalAlignment(JTextField.LEFT);
		logfileFileNameField.setCaretPosition(0);
		logfileFileNameField.setFocusable(true);
	
		
		logfilePanelC.gridwidth = 2;
		logfilePanelC.gridx = 1;
		logfilePanelC.gridy = 2;
		logfilePanelC.anchor = GridBagConstraints.CENTER;
		logfilePanelGridbag.setConstraints(logfileFileNameField,logfilePanelC);
		logfilePanel.add(logfileFileNameField);
		
		yLevel++;
		
		
		// Show Labels Sub-Panel
		
		aestheticsPanel = new JPanel();
		aestheticsPanel.setBorder(panelBorder);
		aestheticsPanel.setOpaque(false);
		
		c.gridx = 0;
		c.gridy = yLevel;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 6;
		c.anchor = GridBagConstraints.WEST; //GridBagConstraints.CENTER;
		gridbag.setConstraints(aestheticsPanel,c);
		panel.add(aestheticsPanel);
		
		GridBagLayout aestheticsPanelGridbag = new GridBagLayout();
		aestheticsPanel.setLayout(aestheticsPanelGridbag);
	
		GridBagConstraints aestheticsPanelC = new GridBagConstraints();
	
		
		// ... label
		label = new JLabel("       LABELS  ", SwingConstants.LEFT);
		aestheticsPanelC.gridx = 0;
		aestheticsPanelC.gridy = 0;
		aestheticsPanelC.anchor = GridBagConstraints.WEST;
		aestheticsPanelGridbag.setConstraints(label,aestheticsPanelC);
		aestheticsPanel.add(label);
		
		// ... radio button -> yes show label 
		yesShowLabelsRadioButton = new JRadioButton("yes");
		yesShowLabelsRadioButton.setOpaque(false);
		yesShowLabelsRadioButton.setFont(fontButton);
		yesShowLabelsRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				yesShowLabelsRadioButton();
			}
		});

		aestheticsPanelC.gridx = 1;//0;
		aestheticsPanelC.gridy = 0;
		//aestheticsPanelC.gridwidth = 1;
		aestheticsPanelC.anchor = GridBagConstraints.WEST;//CENTER;
		aestheticsPanelGridbag.setConstraints(yesShowLabelsRadioButton,aestheticsPanelC);
		aestheticsPanel.add(yesShowLabelsRadioButton);

		
		// ... radio button -> no don't show label 
		noShowLabelsRadioButton = new JRadioButton("no");
		noShowLabelsRadioButton.setOpaque(false);
		noShowLabelsRadioButton.setFont(fontButton);
		noShowLabelsRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				noShowLabelsRadioButton();
			}
		});
		
		aestheticsPanelC.gridx = 2; //2;
		aestheticsPanelC.gridy = 0; //1;
		//aestheticsPanelC.gridwidth = 1;
		aestheticsPanelC.anchor = GridBagConstraints.EAST; //CENTER;
		aestheticsPanelGridbag.setConstraints(noShowLabelsRadioButton,aestheticsPanelC);
		aestheticsPanel.add(noShowLabelsRadioButton);

		// ... settings for view search radio buttons
				
		ButtonGroup showLabelFieldGroup = new ButtonGroup();
		showLabelFieldGroup.add(yesShowLabelsRadioButton);
		showLabelFieldGroup.add(noShowLabelsRadioButton);
		yesShowLabelsRadioButton.setFocusable(true);
		noShowLabelsRadioButton.setFocusable(true);
		yesShowLabelsRadioButton.setSelected(true);
		
		
		
		// ... label
		label = new JLabel("             COLOUR  ", SwingConstants.LEFT);
		aestheticsPanelC.gridx = 3;
		aestheticsPanelC.gridy = 0;
		//paramsHCPanelC.gridwidth = 3;
		aestheticsPanelC.anchor = GridBagConstraints.WEST;
		aestheticsPanelGridbag.setConstraints(label,aestheticsPanelC);
		aestheticsPanel.add(label);
		
		// ... radio button -> yes show label 
		yesColourRadioButton = new JRadioButton("yes");
		yesColourRadioButton.setOpaque(false);
		yesColourRadioButton.setFont(fontButton);
		yesColourRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				yesColourRadioButton();
			}
		});

		aestheticsPanelC.gridx = 4;//0;
		aestheticsPanelC.gridy = 0;
		//aestheticsPanelC.gridwidth = 1;
		aestheticsPanelC.anchor = GridBagConstraints.WEST;//CENTER;
		aestheticsPanelGridbag.setConstraints(yesColourRadioButton,aestheticsPanelC);
		aestheticsPanel.add(yesColourRadioButton);

		
		// ... radio button -> no don't show label 
		noColourRadioButton = new JRadioButton("no");
		noColourRadioButton.setOpaque(false);
		noColourRadioButton.setFont(fontButton);
		noColourRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				noColourRadioButton();
			}
		});
		
		aestheticsPanelC.gridx = 5; //2;
		aestheticsPanelC.gridy = 0; //1;
		//aestheticsPanelC.gridwidth = 1;
		aestheticsPanelC.anchor = GridBagConstraints.WEST; //CENTER;
		aestheticsPanelGridbag.setConstraints(noColourRadioButton,aestheticsPanelC);
		aestheticsPanel.add(noColourRadioButton);

		// ... settings for view search radio buttons
				
		ButtonGroup colourFieldGroup = new ButtonGroup();
		colourFieldGroup.add(yesColourRadioButton);
		colourFieldGroup.add(noColourRadioButton);
		yesColourRadioButton.setFocusable(true);
		noColourRadioButton.setFocusable(true);
		yesColourRadioButton.setSelected(true);
		
		yLevel++; 		
		
		// Curves for Sets Sub-Panel
		
		paramsHCPanel = new JPanel();
		paramsHCPanel.setBorder(panelBorder);
		paramsHCPanel.setOpaque(false);
		
		c.gridx = 0;
		c.gridy = yLevel;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.WEST; 
		gridbag.setConstraints(paramsHCPanel,c);
		panel.add(paramsHCPanel);
		
		GridBagLayout paramsHCPanelGridbag = new GridBagLayout();
		paramsHCPanel.setLayout(paramsHCPanelGridbag);
		GridBagConstraints paramsHCPanelC = new GridBagConstraints();
	
		
		// ... label
		label = new JLabel("CURVES FOR SETS    ", SwingConstants.LEFT);
		paramsHCPanelC.gridx = 0;
		paramsHCPanelC.gridy = 0;
		//paramsHCPanelC.gridwidth = 3;
		paramsHCPanelC.anchor = GridBagConstraints.WEST;
		paramsHCPanelGridbag.setConstraints(label,paramsHCPanelC);
		paramsHCPanel.add(label);
		
		// ... radio button -> Ellipse 
		ellipsesHCFieldRadioButton = new JRadioButton("ellipses");
		ellipsesHCFieldRadioButton.setOpaque(false);
		ellipsesHCFieldRadioButton.setFont(fontButton);
		ellipsesHCFieldRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ellipsesHCFieldRadioButton();
			}
		});
		ellipsesHCFieldRadioButton.setFocusable(true);

		paramsHCPanelC.gridx = 1;
		paramsHCPanelC.gridy = 0;
		paramsHCPanelC.gridwidth = 1;
		paramsHCPanelC.anchor = GridBagConstraints.WEST;//CENTER;
		paramsHCPanelGridbag.setConstraints(ellipsesHCFieldRadioButton,paramsHCPanelC);
		paramsHCPanel.add(ellipsesHCFieldRadioButton);

		
		// ... radio button -> Circle 
		circlesHCFieldRadioButton = new JRadioButton("circles");
		circlesHCFieldRadioButton.setOpaque(false);
		circlesHCFieldRadioButton.setFont(fontButton);
		circlesHCFieldRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				circlesHCFieldRadioButton();
			}
		});
		circlesHCFieldRadioButton.setFocusable(true);
		
		paramsHCPanelC.gridx = 2;
		paramsHCPanelC.gridy = 0;
		paramsHCPanelC.gridwidth = 1;
		paramsHCPanelC.anchor = GridBagConstraints.WEST; //CENTER;
		paramsHCPanelGridbag.setConstraints(circlesHCFieldRadioButton,paramsHCPanelC);
		paramsHCPanel.add(circlesHCFieldRadioButton);

		// ... settings for Ellipse/Circle HC radio buttons
				
		ButtonGroup ellipseCircleHCFieldGroup = new ButtonGroup();
		ellipseCircleHCFieldGroup.add(ellipsesHCFieldRadioButton);
		ellipseCircleHCFieldGroup.add(circlesHCFieldRadioButton);
		
		if(HillClimber.DEFAULT_RESTRICT_TO_CIRLCES) {
			circlesHCFieldRadioButton.setSelected(true);
			circlesHCFieldRadioButton();
		} else {
			ellipsesHCFieldRadioButton.setSelected(true);
			ellipsesHCFieldRadioButton();
		}	
		
		yLevel++;		
		
			
		// View Search Sub-Panel
		
		viewSearchPanel = new JPanel();
		viewSearchPanel.setBorder(panelBorder);
		viewSearchPanel.setOpaque(false);
		
		c.gridx = 0;
		c.gridy = yLevel;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.WEST; 
		gridbag.setConstraints(viewSearchPanel,c);
		panel.add(viewSearchPanel);
		
		GridBagLayout viewSearchPanelGridbag = new GridBagLayout();
		viewSearchPanel.setLayout(viewSearchPanelGridbag);
		GridBagConstraints viewSearchPanelC = new GridBagConstraints();
	
		
		// ... label
		label = new JLabel("VIEW SEARCH    ", SwingConstants.LEFT);
		viewSearchPanelC.gridx = 0;
		viewSearchPanelC.gridy = 0;
		viewSearchPanelC.anchor = GridBagConstraints.WEST;
		viewSearchPanelGridbag.setConstraints(label,viewSearchPanelC);
		viewSearchPanel.add(label);
		
		// ... radio button -> Ellipse 
		yesViewSearchRadioButton = new JRadioButton("yes");
		yesViewSearchRadioButton.setOpaque(false);
		yesViewSearchRadioButton.setFont(fontButton);

		viewSearchPanelC.gridx = 1;
		viewSearchPanelC.gridy = 0;
		viewSearchPanelC.gridwidth = 1;
		viewSearchPanelC.anchor = GridBagConstraints.WEST;
		viewSearchPanelGridbag.setConstraints(yesViewSearchRadioButton,viewSearchPanelC);
		viewSearchPanel.add(yesViewSearchRadioButton);

		
		// ... radio button -> Circle 
		noViewSearchRadioButton = new JRadioButton("no");
		noViewSearchRadioButton.setOpaque(false);
		noViewSearchRadioButton.setFont(fontButton);
		
		viewSearchPanelC.gridx = 2; 
		viewSearchPanelC.gridy = 0; 
		viewSearchPanelC.gridwidth = 1;
		viewSearchPanelC.anchor = GridBagConstraints.WEST; 
		viewSearchPanelGridbag.setConstraints(noViewSearchRadioButton,viewSearchPanelC);
		viewSearchPanel.add(noViewSearchRadioButton);

		// ... settings for view search radio buttons
				
		ButtonGroup viewSearchFieldGroup = new ButtonGroup();
		viewSearchFieldGroup.add(yesViewSearchRadioButton);
		viewSearchFieldGroup.add(noViewSearchRadioButton);
		yesViewSearchRadioButton.setFocusable(true);
		noViewSearchRadioButton.setFocusable(true);
		
		noViewSearchRadioButton.setSelected(true);

		yLevel++; 

		// ... load the diagram
		refreshInitDiagButton = new JButton("Starting Diagram"); 
		refreshInitDiagButton.setMargin(new Insets(4,5,4,5));
		refreshInitDiagButton.setFont(fontButton);
		refreshInitDiagButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				generateStartingDiagramButton(null);
			}
		});
		refreshInitDiagButton.setFocusable(true);
		
		c.gridx = 0;
		c.gridy = yLevel;
		c.gridwidth = 2;//3
		c.insets=new Insets(2,2,0,0);
		gridbag.setConstraints(refreshInitDiagButton,c);
		panel.add(refreshInitDiagButton);
		
		// ... run - multiple changes per iteration &
		//              optimizer with independent parameter changes 
		runHCButton = new JButton("RUN");
		runHCButton.setMargin(new Insets(4, 0, 4, 0));
		runHCButton.setFont(fontButton);
		runHCButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				runHCButton(HillClimber.MULTIPLE_CHANGES_PER_ITER, false, true);
			}
		});
		runHCButton.setFocusable(true);
		
		c.gridx = 3;
		c.gridy = yLevel;
		c.gridwidth = 1;
		c.insets=new Insets(2,0,0,5);
		gridbag.setConstraints(runHCButton,c);
		panel.add(runHCButton);
	
		yLevel++;
		

		
		
		// Message fields

		message1Field = new JTextField(INPUT_TEXT_SIZE);//+5);
		message1Field.setText("");
		message1Field.setEditable(false);
		message1Field.setBackground(Color.WHITE);
		message1Field.setHorizontalAlignment(JTextField.CENTER);
		message1Field.setCaretPosition(0);
		
		c.gridx = 1;
		c.gridy = yLevel;
		c.gridwidth = 1;
		c.insets=new Insets(5,2,0,0);
		gridbag.setConstraints(message1Field,c);
		panel.add(message1Field);		
	
		message2Field = new JTextField(INPUT_TEXT_SIZE);
		message2Field.setText("");
		message2Field.setEditable(false);
		message2Field.setBackground(Color.WHITE);
		message2Field.setHorizontalAlignment(JTextField.CENTER);
		message2Field.setCaretPosition(0);
		//message2Field.setBorder(null);
		//message2Field.setDoubleBuffered(true);
		
		c.gridx = 3;
		c.gridy = yLevel;
		c.gridwidth = 1;
		c.insets=new Insets(5,0,0,5); 
		gridbag.setConstraints(message2Field,c);
		panel.add(message2Field);
	
		//The following field is invisible and thus not added to the panel
		//...this field will be used to save the time in millisecs (rather than formatted)
		message3Field = new JTextField(INPUT_TEXT_SIZE);
		message3Field.setText("");
		message3Field.setEditable(false);
		message3Field.setHorizontalAlignment(JTextField.CENTER);
		message3Field.setCaretPosition(0);		
		
		// initial other fields that are hidden but used internally
		dHCField = new JTextField();
		sHCField = new JTextField();
		rHCField = new JTextField();
		
	}
	
	
	//  Updating the panel
	
	protected void updateEllipseDiagramPanel(boolean diagLabelsOnly, boolean scaleToFit, boolean updateAreaVarFitnessFields){
		
		updateEllipseDiagramPanel(diagLabelsOnly, scaleToFit, updateAreaVarFitnessFields, null, null);
	}
	protected void updateEllipseDiagramPanel(boolean diagLabelsOnly, boolean scaleToFit, boolean updateAreaVarFitnessFields, HashMap<String, Double> currentZoneVariances, Double currentFitness){
		
		ellipseDiagramPanel.updateDiagramLabelsOnly = new Boolean(diagLabelsOnly);
		ellipseDiagramPanel.updateDisplay(0, scaleToFit);

		if (updateAreaVarFitnessFields){
			if ((currentZoneVariances == null) || (currentFitness == null) || scaleToFit){
				updateAreaVarFitnessFieldsFromDiagram();
			} else {
				updateAreaVarFitnessFieldsFromDiagram(currentZoneVariances, currentFitness.doubleValue());
			}
		}
	}
	
	protected void updateEllipseDiagramPanel_newRequiredAreaSpecs(){
		if (ellipseDiagramPanel.diagram == null) {
		
		} else {
			updateEllipseDiagramPanel (true, false, true);
		}
	}
	
	protected void updateEllipseDiagramPanel_newInitDiagram (){
		updateEllipseDiagramPanel (false, false, true); // switched off initial scaling
	}
	
	protected void updateEllipseDiagramPanel_updateLabelsDisplayMode (){
		updateEllipseDiagramPanel (false, false, false);
	}
	
	protected void updateEllipseDiagramPanel_updateDisplayColour (){
		updateEllipseDiagramPanel (true, false, false);
	}
	
	protected void updateEllipseDiagramPanel_updatedDiagram (boolean updateVars){
		updateEllipseDiagramPanel (false, false, updateVars);
	}
	
	protected void updateEllipseDiagramPanel_updatedDiagram (HashMap<String, Double> currentZoneVariances, double currentFitness){
		updateEllipseDiagramPanel (false, false, true, currentZoneVariances, currentFitness);
	}
	
	public void updateAreaVarFitnessFieldsFromDiagram(){
		if (requiredAreaSpec == null){
			updateRequiredAreaSpecPropFromFields(false);
		    if (requiredAreaSpec == null){
		    	return;
		    } 
		}
		EllipseDiagram diagram = ellipseDiagramPanel.getDiagram();
		
		HashMap<String, Double> currentZoneVariances = diagram.computeFitnessOfAllRegions(requiredAreaSpec, fitnessMeasure);
		if ((!diagram.zoneAreasComputedSuccessfully) || (diagram.zoneAreasComputedSuccessfully == null) || ((fitnessMeasure!=FitnessMeasure.STRESS)&&(currentZoneVariances == null))){
			System.out.println("SwitchBoardPanel.updateFieldsFromDiagram: cannot compute current zone variances because the zone areas were not computed successfully");
			return;
		}
		Double currentFitness = null;
        currentFitness = diagram.computeFitnessOfDiagram(requiredAreaSpec, currentZoneVariances, fitnessMeasure);
		if (currentFitness == null){
			System.out.println("SwitchBoardPanel.updateFieldsFromDiagram: cannot compute current fitness");
			return;
		}		
		
		updateAreaVarFitnessFieldsFromDiagram (currentZoneVariances, currentFitness);
	}	

	
	public void updateAreaVarFitnessFieldsFromDiagram(HashMap<String, Double> currentZoneVariances, double currentFitness){

		EllipseDiagram diagram = ellipseDiagramPanel.getDiagram();
		if (diagram == null){return;}
		String[] zoneLabels = null;
		HashMap<String, Double> currentZoneAreas = null;
		
		zoneLabels = diagram.getZoneLabels();
		currentZoneAreas = diagram.getZoneAreas();
		
		
		double currentDiagAreaTotal = 0;
		double requiredDiagAreaTotal = 0;
		double relerr=0;
		double errorBasedOnAreaProportions=0;
		double abserrOverReqTot = 0;
		for(String zl : zoneLabels) {
			if ((currentZoneAreas != null) && (currentZoneAreas.get(zl) != null)) {currentDiagAreaTotal += currentZoneAreas.get(zl);}
			if ((requiredAreaSpec != null) && (requiredAreaSpec.get(zl) != null)) {requiredDiagAreaTotal += requiredAreaSpec.get(zl);}
		}
		
		
		int z = 0;
		int noAreaZoneCount = 0;
	
		for(String zl : zoneLabels) {
			if (requiredAreaSpecsField[z].getText().trim().equals("")){
				requiredAreaSpecsPCField[z].setText("");
				currAreaField[z].setText("");
				currAreaPCField[z].setText("");
				diffReqActualField[z].setText("");
				diffReqActualPCField[z].setText("");
				noAreaZoneCount++;
			} else {
				
				requiredAreaSpecsPCField[z].setText(((requiredAreaSpec == null)||(requiredAreaSpec.get(zl) == null)) ? "" : 
													Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(
					                                (requiredAreaSpec.get(zl)/requiredDiagAreaTotal)*100, ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP)))+"%");
				
				currAreaField[z].setText((currentZoneAreas == null) ? "" : (wereReqRegionAreasMult?Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(currentZoneAreas.get(zl)/reqRegionAreasMultFactor, ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP))):Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(currentZoneAreas.get(zl), ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP)))));

				currAreaPCField[z].setText(((currentZoneAreas == null)||(currentZoneAreas.get(zl) == null)) ? "" : 
										   Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(
					                       (currentZoneAreas.get(zl)/currentDiagAreaTotal)*100, ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP)))+"%");
				
				diffReqActualField[z].setText(((currentZoneAreas == null)||(currentZoneAreas.get(zl) == null)) ? "" : 
					
					Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(
							( (Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(requiredAreaSpecsField[z].getText()))-
							   Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(currAreaField[z].getText())))
							   ==-0.0) ? 
									   0.0:
									      (Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(requiredAreaSpecsField[z].getText()))-
									      (Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(currAreaField[z].getText())))), ZONEVARIANCES_DIGITS_BEFORE_DP, ZONEVARIANCES_DIGITS_AFTER_DP)))); //4)));
				
				diffReqActualPCField[z].setText((diffReqActualField[z].getText().trim().equals(""))?"":
						 Double.toString(Utilities.roundToDps(
						  ((Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(requiredAreaSpecsPCField[z].getText().replace("%",""))))-
					       (Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(currAreaPCField[z].getText().replace("%",""))))), 
					      ZONEVARIANCES_DIGITS_BEFORE_DP, ZONEVARIANCES_DIGITS_AFTER_DP))+"%");				
			}
			requiredAreaSpecsPCField[z].update(requiredAreaSpecsPCField[z].getGraphics());
			currAreaField[z].update(currAreaField[z].getGraphics());
			currAreaPCField[z].update(currAreaPCField[z].getGraphics());
			diffReqActualField[z].update(diffReqActualField[z].getGraphics());
			diffReqActualPCField[z].update(diffReqActualPCField[z].getGraphics());
			z++;
		}
		if (noAreaZoneCount >0){
			currentFitness=-1;
			diagErrorField.setText("");
		} else {
			double diagError = Utilities.roundToDps(diagram.diagError(requiredAreaSpec), FITNESS_DIGITS_BEFORE_DP, FITNESS_DIGITS_AFTER_DP);
			diagErrorField.setText(Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(diagError)));
		}
		if (currentlyDrawingDiag && yesViewSearchRadioButton.isSelected()){
			diagErrorField.setText("");
		}
		diagErrorField.update(diagErrorField.getGraphics());
	}
	
	
	public void updateRequiredAreaSpecFields(){
		updateRequiredAreaSpecFields(true);
	}
	
	public void updateRequiredAreaSpecFields(boolean doUpdateRequiredAreaSpecPropFromFields){
	
		String[] zoneLabels = EllipseDiagramOps.getZoneLabels(noOfEllipses, false);
			
		double requiredDiagAreaTotal = 0;
		for(String zl : zoneLabels) {
			if ((requiredAreaSpec != null) && (requiredAreaSpec.get(zl) != null)) {requiredDiagAreaTotal += requiredAreaSpec.get(zl);}
		}
		
		for(int i=0; i<zoneLabels.length; i++) {
			requiredAreaSpecsField[i].setText(Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(new Double(requiredAreaSpec.get(zoneLabels[i])), ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP))));
			requiredAreaSpecsPCField[i].setText(((requiredAreaSpec == null)||(requiredAreaSpec.get(zoneLabels[i]) == null)) ? "" : 
				Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(
	            (requiredAreaSpec.get(zoneLabels[i])/requiredDiagAreaTotal)*100, ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP)))+"%");
			
		}
		if (doUpdateRequiredAreaSpecPropFromFields) {updateRequiredAreaSpecPropFromFields(false);}
	}

	protected boolean updateRequiredAreaSpecPropFromFields(boolean typingInRequiredRegionAreas) {
		if (!loadReqAreaSpecsFromFields){return true;}
		
		EllipseDiagram diagram = ellipseDiagramPanel.getDiagram();
		String[] zoneLabels = (diagram == null) ? EllipseDiagramOps.getZoneLabels(noOfEllipses, false) : diagram.getZoneLabels();
		
		int z = 0;
		
		HashMap<String, Double> newRequiredAreaSpecs = new HashMap<String, Double>();
		boolean incompleteAreaSpecs = false;
		double requiredZoneAreaSpec;
		for(String zl : zoneLabels) {
			if ((requiredAreaSpecsField[z]==null)||(requiredAreaSpecsField[z].getText()==null)||(requiredAreaSpecsField[z].getText().trim().equals(""))){
				incompleteAreaSpecs = true;
				break;
			}
		    requiredZoneAreaSpec = Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(requiredAreaSpecsField[z].getText()));
			if (typingInRequiredRegionAreas && (requiredZoneAreaSpec!=0)){ // to remove any trailing 0s
				requiredAreaSpecsField[z].setText(Utilities.removeLeadingZeros(requiredAreaSpecsField[z].getText()));
			}
		    if (!typingInRequiredRegionAreas){
				if (requiredZoneAreaSpec == 0){requiredAreaSpecsField[z].setText("0");} //if get err when parsing the double, 0 is returned //if null then return 0 => change displayed value too 
				
		    }
			if(requiredZoneAreaSpec < 0) {
				message1Field.setText("Invalid required area for zone " + zl);
				return false;
			}
			newRequiredAreaSpecs.put(zl, new Double(requiredZoneAreaSpec));
			z++;
		}
		requiredAreaSpec = incompleteAreaSpecs ? null : newRequiredAreaSpecs;
		// set the actual HashMap to the new one at this point, so that if it fails at any point during the analysis 
		//  of the individual area specs, the original area specs are maintained
		ellipseDiagramPanel.setRequiredAreaSpecs(requiredAreaSpec);

		checkedIfRequiredRegionAreasShouldBeScaled = false;
		
		updateRequiredAreaSpecsPCFields();
		
		return true;
	}
	
	public void updateRequiredAreaSpecsPCFields(){

		double requiredDiagAreaTotal = 0;
		EllipseDiagram diagram = ellipseDiagramPanel.getDiagram();
		String[] zoneLabels = (diagram == null) ? EllipseDiagramOps.getZoneLabels(noOfEllipses, false) : diagram.getZoneLabels();
		for(String zl : zoneLabels) {
			if ((requiredAreaSpec != null) && (requiredAreaSpec.get(zl) != null)) {requiredDiagAreaTotal += requiredAreaSpec.get(zl);}
		}
		for (int i=0; i<zoneLabels.length; i++){
			requiredAreaSpecsPCField[i].setText(((requiredAreaSpec == null)||(requiredAreaSpec.get(zoneLabels[i]) == null)) ? "" : 
						Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(
						(requiredAreaSpec.get(zoneLabels[i])/requiredDiagAreaTotal)*100, ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP)))+"%");
			requiredAreaSpecsPCField[i].update(requiredAreaSpecsPCField[i].getGraphics());
		}
	}
	
	public void updateHCparamsFromHC(){
		dHCField.setText(Double.toString((hillClimber == null) ? HillClimber.DEFAULT_D :Utilities.roundToDps(hillClimber.getD(),FITNESS_DIGITS_BEFORE_DP, FITNESS_DIGITS_AFTER_DP)));
		sHCField.setText(Double.toString((hillClimber == null) ? HillClimber.DEFAULT_S :Utilities.roundToDps(hillClimber.getS(),FITNESS_DIGITS_BEFORE_DP, FITNESS_DIGITS_AFTER_DP)));
		rHCField.setText(Double.toString((hillClimber == null) ? HillClimber.DEFAULT_R :Utilities.roundToDps(hillClimber.getR(),FITNESS_DIGITS_BEFORE_DP, FITNESS_DIGITS_AFTER_DP)));			
		dHCField.update(dHCField.getGraphics());
		sHCField.update(sHCField.getGraphics());
		rHCField.update(rHCField.getGraphics());
	}
	
	public void updateHCStopWatch(boolean updateStopWatchDisplay){
		if ((hillClimber != null) && (hillClimber.stopWatch!=null)){
			message3Field.setText(Double.toString(hillClimber.elapsedTimeMilliSecs));
			message2Field.setText(hillClimber.elapsedTimeString);
			if (updateStopWatchDisplay){
				message2Field.update(message2Field.getGraphics());
			}
		}
	}
	
	public void clearEllipseDiagramPanel(){
		if (ellipseDiagramPanel.getDiagram()!=null){
			ellipseDiagramPanel.setDiagram(null);
			ellipseDiagramPanel.updateDisplay(0, false);
		}
	}
	
	public void deleteTimerDetails(){
		message3Field.setText("             ");
		message2Field.setText("             ");
		message2Field.update(message2Field.getGraphics());
	}
	
	public void resetPanelsForNextRunSameProps(Double[] params){
		resetPanelsForNextRunSameProps(params, false);
	}

	public void resetPanelsForNextRunSameProps(Double[] params, boolean clearDiagramPanel){
		if (params == null){
		} else {
			setParamsHC(params);
		}
		if (clearDiagramPanel){
			clearEllipseDiagramPanel();
		} else {
			refreshInitDiagButton(false, null);
		}
	}
	
	
	protected String getLogFileDirFromField(){
		String logFileDirPath = logfileDirPathField.getText().trim();
		if ((logFileDirPath==null) || (logFileDirPath.equals(""))) {
			logfileFile = null;
			return null;
		}
		
		File logFileDirFile = new File(logFileDirPath); 
		if (!logFileDirFile.exists()){
			JOptionPane.showMessageDialog(this, "Log file directory " + logFileDirPath + " does not exist", "Choosing Log File Destination Directory", JOptionPane.ERROR_MESSAGE);
			logfileDirPathField.setText("");
			logfileFile = null;
			return null;
		}
		
		return logFileDirPath;
	}
	
	protected void getLogfileFileFromField(boolean noDetails, int runMode, boolean lockHCparams, Double[] hcParams){
		String logFileDirPath = getLogFileDirFromField();
		
		if ((logFileDirPath==null) || (logFileDirPath.equals(""))) {return;}
		
		savingInitDiagImgFile = null;
		savingInitDiagSVGFile = null;
		savingInitDiagFile = null;
		savingFinalDiagImgFile = null;
		savingFinalDiagSVGFile = null;
		savingFinalDiagFile = null;
	
		if (!saveFinalDiagOnly){
			if (saveDiagImgFile){ savingInitDiagImgFile = new File (logFileDirPath + File.separator + getFullInitDiagImgName(noDetails, runMode, lockHCparams, hcParams));}
			if (saveDiagSVGFile){ savingInitDiagSVGFile = new File (logFileDirPath + File.separator + getFullInitDiagSVGName(noDetails, runMode, lockHCparams, hcParams));}
			if (saveDiagELDFile){ savingInitDiagFile = new File (logFileDirPath + File.separator + getFullInitDiagName(noDetails, runMode, lockHCparams, hcParams));}
		}		
		if (saveDiagImgFile){ savingFinalDiagImgFile = new File (logFileDirPath + File.separator + getFullFinalDiagImgName(noDetails, runMode, lockHCparams, hcParams, !saveFinalDiagOnly));}
		if (saveDiagSVGFile){ savingFinalDiagSVGFile = new File (logFileDirPath + File.separator + getFullFinalDiagSVGName(noDetails, runMode, lockHCparams, hcParams, !saveFinalDiagOnly));}
		if (saveDiagELDFile){ savingFinalDiagFile = new File (logFileDirPath + File.separator + getFullFinalDiagName(noDetails, runMode, lockHCparams, hcParams, !saveFinalDiagOnly));}
		
		logfileFile = new File (logFileDirPath + File.separator + getFullLogFileName(runMode, lockHCparams, hcParams));
		
	}
	
	
	
	private String getRunModeStr (int runMode, boolean lockHCparams, Double[] hcParams){
		String runModeStr = "";
		if (runMode == HillClimber.MULTIPLE_CHANGES_PER_ITER){
			runModeStr = HillClimber.MULTIPLE_CHANGES_FILENAME_SUFFIX;
		} else if (runMode == HillClimber.SINGLE_CHANGE_PER_ITER){
			runModeStr = HillClimber.SINGLE_CHANGE_FILENAME_SUFFIX;
		}
		runModeStr += "-" + (lockHCparams ? HillClimber.LOCKED_PARAMS_FILENAME_SUFFIX : HillClimber.UNLOCKED_PARAMS_FILENAME_SUFFIX) ;
		if ((hcParams!=null) && (hcParams.length==3)){
			runModeStr += "_(" + ((int)hcParams[0].doubleValue()) + "," + ((int)hcParams[1].doubleValue()) + "," + ((int)hcParams[2].doubleValue()) + ")";
		}
		return runModeStr;
	}
	
	private String getFullLogFileName(int runMode, boolean lockHCparams, Double[] hcParams){
		String suffix = "_" + getRunModeStr(runMode,lockHCparams,hcParams) + "_" + logFileName + ".log";
		return (Utilities.getCurrentDateTime() + suffix);	
	}

	
	
	private String getFullInitDiagImgName(boolean noDetails, int runMode, boolean lockHCparams, Double[] hcParams){
		String suffix = "";
		if (dislayRunModeInFileName){
			suffix = "_" + getRunModeStr(runMode,lockHCparams,hcParams) + "_" + 
			               (circlesHCFieldRadioButton.isSelected() ? "circles":"ellipses");
		}
		if ((fileNameOfSelectedDiagAreaSpecs==null)||(fileNameOfSelectedDiagAreaSpecs.equals(""))){
			return (Utilities.getCurrentDateTime() + "_init.png"); //suffix);
		} else {
			if (!runningATest){ 
				return (fileNameOfSelectedDiagAreaSpecs + "_init.png");
			} else {
				return (fileNameOfSelectedDiagAreaSpecs + "(" + Utilities.getCurrentDateTime() + ")" + suffix + "_init.png");
			}
		}
	}
	private String getFullInitDiagName(boolean noDetails, int runMode, boolean lockHCparams, Double[] hcParams){
		String suffix = "";
		if (dislayRunModeInFileName){
			suffix = "_" + getRunModeStr(runMode,lockHCparams,hcParams) + "_" + 
	                        (circlesHCFieldRadioButton.isSelected() ? "circles":"ellipses");
		}
		if ((fileNameOfSelectedDiagAreaSpecs==null)||(fileNameOfSelectedDiagAreaSpecs.equals(""))){
			return (Utilities.getCurrentDateTime() + "_init.eld");
		} else {
			if (!runningATest){
				return (fileNameOfSelectedDiagAreaSpecs + "_init.eld");
			} else {
				return (fileNameOfSelectedDiagAreaSpecs + "(" + Utilities.getCurrentDateTime() + ")" + suffix + "_init.eld");
			}
		}
	}
	private String getFullInitDiagSVGName(boolean noDetails, int runMode, boolean lockHCparams, Double[] hcParams){
		String suffix = "";
		if (dislayRunModeInFileName){
			suffix = "_" + getRunModeStr(runMode,lockHCparams,hcParams) + "_" + 
	                        (circlesHCFieldRadioButton.isSelected() ? "circles":"ellipses");
		}
		if ((fileNameOfSelectedDiagAreaSpecs==null)||(fileNameOfSelectedDiagAreaSpecs.equals(""))){
			return (Utilities.getCurrentDateTime() + "_init.svg"); 
		} else {
			if (!runningATest){
				return (fileNameOfSelectedDiagAreaSpecs + "_init.svg");
			} else {
				return (fileNameOfSelectedDiagAreaSpecs + "(" + Utilities.getCurrentDateTime() + ")" + suffix + "_init.svg");
			}
		}
	}
	private String getFullIntermediateDiagImgName(boolean noDetails, int runMode, boolean lockHCparams, Double[] hcParams, String addSuffix){
		String suffix = "";
		if (dislayRunModeInFileName){
			suffix = "_" + getRunModeStr(runMode,lockHCparams,hcParams) + "_" + 
		               (circlesHCFieldRadioButton.isSelected() ? "circles":"ellipses");
		}
		if ((fileNameOfSelectedDiagAreaSpecs==null)||(fileNameOfSelectedDiagAreaSpecs.equals(""))){
			return (Utilities.getCurrentDateTime() + "_interm_"+addSuffix+".png"); 
		} else {
			if (!runningATest){ 
				return (fileNameOfSelectedDiagAreaSpecs + "_interm_"+addSuffix+".png");
			} else {	
				return (fileNameOfSelectedDiagAreaSpecs + "(" + Utilities.getCurrentDateTime() + ")" + suffix + "_interm_"+addSuffix+".png");
			}
		}
	}
	private String getFullIntermediateDiagName(boolean noDetails, int runMode, boolean lockHCparams, Double[] hcParams, String addSuffix){
		String suffix = "";
		if (dislayRunModeInFileName){
			suffix = "_" + getRunModeStr(runMode,lockHCparams,hcParams) + "_" + 
                        (circlesHCFieldRadioButton.isSelected() ? "circles":"ellipses");
		}
		if ((fileNameOfSelectedDiagAreaSpecs==null)||(fileNameOfSelectedDiagAreaSpecs.equals(""))){
			return (Utilities.getCurrentDateTime() + "_interm_"+addSuffix+".eld"); 
		} else {
			if (!runningATest){
				return (fileNameOfSelectedDiagAreaSpecs + "_interm_"+addSuffix+".eld");
			} else {
				return (fileNameOfSelectedDiagAreaSpecs + "(" + Utilities.getCurrentDateTime() + ")" + suffix + "_interm_"+addSuffix+".eld");
			}
		}
	}
	private String getFullFinalDiagImgName(boolean noDetails, int runMode, boolean lockHCparams, Double[] hcParams, boolean includeFinalDiagIDSuffix){
		String suffix = "";
		if (dislayRunModeInFileName){
			suffix = "_" + getRunModeStr(runMode,lockHCparams,hcParams) + "_" + 
                       (circlesHCFieldRadioButton.isSelected() ? "circles":"ellipses");
		}
		String finalDiagIDSuffix = "_final"; 
		String ext = ".png";
		if ((fileNameOfSelectedDiagAreaSpecs==null)||(fileNameOfSelectedDiagAreaSpecs.equals(""))){
			return (Utilities.getCurrentDateTime() + (includeFinalDiagIDSuffix ? finalDiagIDSuffix : "") + ext); 
		} else {
			if (!runningATest){ 
				return (fileNameOfSelectedDiagAreaSpecs + (includeFinalDiagIDSuffix ? finalDiagIDSuffix : "") + ext);
			} else {			
				return (fileNameOfSelectedDiagAreaSpecs + "(" + Utilities.getCurrentDateTime() + ")" + suffix + (includeFinalDiagIDSuffix ? finalDiagIDSuffix : "") + ext);
			}
		}
	}
	private String getFullFinalDiagSVGName(boolean noDetails, int runMode, boolean lockHCparams, Double[] hcParams, boolean includeFinalDiagIDSuffix){
		String suffix = "";
		if (dislayRunModeInFileName){
			suffix = "_" + getRunModeStr(runMode,lockHCparams,hcParams) + "_" + 
                        (circlesHCFieldRadioButton.isSelected() ? "circles":"ellipses");
		}
		String finalDiagIDSuffix = "_final"; 
		String ext = ".svg";
		if ((fileNameOfSelectedDiagAreaSpecs==null)||(fileNameOfSelectedDiagAreaSpecs.equals(""))){
			return (Utilities.getCurrentDateTime() + (includeFinalDiagIDSuffix ? finalDiagIDSuffix : "") + ext);
		} else {
			if (!runningATest){
				return (fileNameOfSelectedDiagAreaSpecs + (includeFinalDiagIDSuffix ? finalDiagIDSuffix : "") + ext);
			} else {				
				return (fileNameOfSelectedDiagAreaSpecs + "(" + Utilities.getCurrentDateTime() + ")" + suffix + (includeFinalDiagIDSuffix ? finalDiagIDSuffix : "") + ext);
			}
		}
	}
	private String getFullFinalDiagName(boolean noDetails, int runMode, boolean lockHCparams, Double[] hcParams, boolean includeFinalDiagIDSuffix){
		String suffix = "";
		if (dislayRunModeInFileName){
			suffix = "_" + getRunModeStr(runMode,lockHCparams,hcParams) + "_" + 
                        (circlesHCFieldRadioButton.isSelected() ? "circles":"ellipses");
		}
		String finalDiagIDSuffix = "_final"; 
		String ext = ".eld";
		if ((fileNameOfSelectedDiagAreaSpecs==null)||(fileNameOfSelectedDiagAreaSpecs.equals(""))){
			return (Utilities.getCurrentDateTime() + (includeFinalDiagIDSuffix ? finalDiagIDSuffix : "") + ext);
		} else {
			if (!runningATest){
				return (fileNameOfSelectedDiagAreaSpecs + (includeFinalDiagIDSuffix ? finalDiagIDSuffix : "") + ext);
			} else {				
				return (fileNameOfSelectedDiagAreaSpecs + "(" + Utilities.getCurrentDateTime() + ")" + suffix + (includeFinalDiagIDSuffix ? finalDiagIDSuffix : "") + ext);
			}
		}
	}
	
	
	
	// Action listeners 
	
	protected void onKeyReleasedRequiredAreaSpecsField(KeyEvent e){
		
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {return;}
	
	
		loadReqAreaSpecsFromFields = true;
		wereReqRegionAreasMult = false;
		
		char localeDecimalSep = Utilities.getLocaleDecimalSeparator();
		char defaultDecimalSep = Utilities.defaultDecimalSeparator;

		
		// validate any text input and output error message if chars are entered instead of numbers
		JTextField reqZAtextfield = (JTextField) e.getSource();
		String reqZAStr = reqZAtextfield.getText();
		if (reqZAStr.trim().equals("")){return;}
		int keyLoc = reqZAtextfield.getCaretPosition()-1;
		
		// change locale decimal separator to default decimal separator
	    if(keyLoc<0){return;}
		char lastKey = reqZAStr.toCharArray()[keyLoc];
		if (localeDecimalSep != defaultDecimalSep){
			reqZAStr = Utilities.changeLocaleDecimalSeparatorToDefault(reqZAStr);
		    if ((lastKey==localeDecimalSep)&&(reqZAStr.length()==1)){
		    	reqZAStr+=0;
		    }
		    if (lastKey==defaultDecimalSep){
		    	reqZAStr = reqZAStr.substring(0,keyLoc) + "a";
		    }
		}
		
		if (((lastKey != defaultDecimalSep)&&(lastKey!=localeDecimalSep)&&(!Character.isDigit(lastKey)))||
			(Utilities.safeParseDouble(reqZAStr.trim()) == null)){
			JOptionPane.showMessageDialog(this, "A valid numeric value is expected", "Entering Required Region Areas", JOptionPane.ERROR_MESSAGE);
			String reqZAStr_wCharDeleted = reqZAStr.substring(0,keyLoc) + reqZAStr.substring(keyLoc+1);
			if (reqZAStr_wCharDeleted.equals("")){
				reqZAtextfield.setText("");
			} else {
				while ((reqZAStr_wCharDeleted.length()>0) && Utilities.safeParseDouble(reqZAStr_wCharDeleted.trim()) == null){
					keyLoc = keyLoc-1;
				    reqZAStr_wCharDeleted = reqZAStr_wCharDeleted.substring(0,keyLoc);
				}
				if (reqZAStr_wCharDeleted.equals("")){
					reqZAtextfield.setText("");
				} else {
					reqZAtextfield.setText(Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(Utilities.roundToDps(Utilities.safeParseDouble(reqZAStr_wCharDeleted), ZONEAREAS_DIGITS_BEFORE_DP, ZONEAREAS_DIGITS_AFTER_DP))));
				}
			}
			reqZAtextfield.update(reqZAtextfield.getGraphics());
			return;
		}
		
		reqZAtextfield.update(reqZAtextfield.getGraphics());
		updateRequiredAreaSpecPropFromFields(true);
		
		// delete the current ellipse diagram (if any) and clear the current area and differences columns and fitness (if any) 
		if (ellipseDiagramPanel.getDiagram()!=null){
			for (int i=0; i<7; i++){
				requiredAreaSpecsPCField[i].setText("");
				currAreaField[i].setText("");
				currAreaPCField[i].setText("");
				diffReqActualField[i].setText("");
				diffReqActualPCField[i].setText("");
			}
			diagErrorField.setText("");
			
			//update these messagefields before clearing the diagram or else they will not be cleared
			message1Field.setText("");
			message1Field.update(message1Field.getGraphics());			
			message2Field.setText("");
			message2Field.update(message2Field.getGraphics());
			
			clearEllipseDiagramPanel();
			updateRequiredAreaSpecsPCFields();
		}
		
		reqZAtextfield.setCaretPosition(keyLoc+1);
		reqZAtextfield.moveCaretPosition(keyLoc+1);
	}
	
	
	protected void genRandomAreaSpecsButton(){
		
		for (int i=0; i<7; i++){
			requiredAreaSpecsField[i].setText("");
			requiredAreaSpecsPCField[i].setText("");
			currAreaField[i].setText("");
			currAreaPCField[i].setText("");
			diffReqActualField[i].setText("");
			diffReqActualPCField[i].setText("");
		}
		diagErrorField.setText("");
		clearEllipseDiagramPanel();
		
		//depends on the current scale
		EllipseDiagram diagram = ellipseDiagramPanel.getDiagram();
		String[] zoneLabels = (diagram == null) ? EllipseDiagramOps.getZoneLabels(noOfEllipses, false) : diagram.getZoneLabels();
	
		
		requiredAreaSpec = new HashMap<String, Double>();
		int zc = 0;
		Random random = new Random();
		for (String zl : zoneLabels) {
			double zoneAreaSpec = Utilities.randomNumberInRange(1,10000, random);
			requiredAreaSpec.put(zl, new Double(Math.round(zoneAreaSpec)));
			zc++;
		}
		updateRequiredAreaSpecFields(false); //because the area specs we need are already in this switchboardpanel.requiredAreaSpecs
		
		
		message1Field.setText("");
		message2Field.setText("");
		
		fileNameOfSelectedDiagAreaSpecs = "";
		
		// the following 2 flags should be set in retrieveFileAreaSpecs if successful 
		// but can set them here too as a precaution
		loadReqAreaSpecsFromFields = false;
		wereReqRegionAreasMult = false;
	}
	
	
	protected void clearAllAreaSpecsButton(){
		clearAllAreaSpecsButton(true);
	}
	protected void clearAllAreaSpecsButton(boolean clearReqAreaSpec){
		
		for (int i=0; i<7; i++){
			requiredAreaSpecsField[i].setText("");
			requiredAreaSpecsPCField[i].setText("");
			
			currAreaField[i].setText("");
			currAreaPCField[i].setText("");

			diffReqActualField[i].setText("");
			diffReqActualPCField[i].setText("");

		}
		diagErrorField.setText("");
	
		//update these messagefields before clearing the diagram or else they will not be cleared
		message1Field.setText("");
		message1Field.update(message1Field.getGraphics());
		message2Field.setText("");
		message2Field.update(message2Field.getGraphics());
	
		if (clearReqAreaSpec){
			requiredAreaSpec=null;
			checkedIfRequiredRegionAreasShouldBeScaled = false;
			wereReqRegionAreasMult = false;  //this would be set to true once the required region areas are scaled
			reqRegionAreasMultFactor = 1;
			if (!runningATest){
				loadReqAreaSpecsFromFields=true; //once clear area specs, set loadReqAreaSpecsFromFields to true -> if user decides to load from file, loadReqAreaSpecsFromFields will be set to false
			}
		}

		clearEllipseDiagramPanel();
		updateAreaVarFitnessFieldsFromDiagram();
	}
	
	
	protected void loadFromFileAreaSpecButton(){
		
		File selectedDiagFile = null;
		
		JFileChooser chooser = new JFileChooser(currentUserDir.equals("")?logfileDirPathField.getText().trim():currentUserDir);//diagLibMainDirPath);//areaSpecsListPath);
		FileFilter fileFilterELD = new FileNameExtensionFilter("eulerAPE diagram file", "eld");
		FileFilter fileFilterELS = new FileNameExtensionFilter("eulerAPE region areas file", "els");
		chooser.addChoosableFileFilter(fileFilterELD);
		chooser.addChoosableFileFilter(fileFilterELS);
		chooser.setAcceptAllFileFilterUsed(false);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedDiagFile = chooser.getSelectedFile();
			currentUserDir = chooser.getSelectedFile().getParent().toString();
		} else {
			return;
		}
		
		
		if (!selectedDiagFile.getAbsoluteFile().toString().endsWith(".eld") && !selectedDiagFile.getAbsoluteFile().toString().endsWith(".els")){ //shouldn't need this if have filter with the open dialog
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(this, "Incorrect file type. File must have extension .eld or .els", "Loading Required Region Areas from File", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (!selectedDiagFile.exists()){ //shouldn't need this but just in case
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(this, selectedDiagFile.getAbsolutePath().toString() + " does not exist.", "Loading Required Region Areas from File", JOptionPane.ERROR_MESSAGE);
			return;			
		}

		
		
		
		boolean hasRetrievedFileAreaSpecs = retrieveFileAreaSpec(selectedDiagFile, LOAD_AS_FROM_FILE__RECOMPUTE_ZONE_AREAS, true);
		
		if (!hasRetrievedFileAreaSpecs){
			
			JLabel label = new JLabel();
		    Font font = label.getFont();

		    // create some css from the label's font
		    StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
		    style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		    style.append("font-size:" + font.getSize() + "pt;");

		    
			if (selectedDiagFile.getAbsoluteFile().toString().endsWith(".eld")){
				Toolkit.getDefaultToolkit().beep();
				
			    // html content
			    JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" + 
			             "Failed to load the region areas of the diagram in <br>" + selectedDiagFile.getAbsolutePath().toString()+
						 "<br><br> Check:"+
	                     "<br><br>&nbsp;1) The structure of the file" +
	                         "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;a) the properties of each ellipse are defined as follows" +
	                         "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;label | semi-major axis | semi-minor axis | centre - x | centre - y | rotation |"+
						     "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;b) the properties of 3 ellipses are defined " +
						     "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;c) the ellipses are labelled a, b, c" +
						 "<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;An example of a valid .eld file is available at "+
			                 "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"http://www.eulerdiagrams.org/eulerAPE/diagram_example.php\">http://www.eulerdiagrams.org/eulerAPE/diagram_example.php</a>" +
			             "<br><br>&nbsp;2) The defined diagram has no empty or disconnected regions" +
						 "<br><br>&nbsp;3) The decimal separator corresponds to your locale"+
						     "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Your current locale (LanguageCode_CountryCode):   " + Locale.getDefault() +
						     "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Expected decimal separator:   " + Utilities.getLocaleDecimalSeparator()+"<br><br>"
			            + "</body></html>");

			    // handle link events
			    ep.addHyperlinkListener(new HyperlinkListener()
			    {
					@Override
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)){
			               if (Desktop.isDesktopSupported()) {
						      try {
						        try {
									Desktop.getDesktop().browse(new URI(e.getURL().toString()));
								} catch (URISyntaxException e1) {
									e1.printStackTrace();
								}
						      } catch (IOException ex) {}
						    } else {  }
						}
					}
			    });
			    ep.setEditable(false);
			    ep.setBackground(label.getBackground());

			    // show
			    JOptionPane.showMessageDialog(this, ep, "Loading Required Region Areas from Diagram File", JOptionPane.ERROR_MESSAGE);
			    
			    
			
			} else if (selectedDiagFile.getAbsoluteFile().toString().endsWith(".els")){
				Toolkit.getDefaultToolkit().beep();
				
				String localeDecSep = Character.toString(Utilities.getLocaleDecimalSeparator());
				
				 // html content
			    JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" + 
			            "Failed to load the region areas in <br>" + selectedDiagFile.getAbsolutePath().toString()+
			            "<br><br>&nbsp;Check:"+
                        "<br><br>&nbsp;1) The structure of the file" +
                            "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;a) lines with comments must start with // "+
                            "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;b) the region areas are defined in one line as follows"+
                            "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;a | b | c | ab | ac | bc | abc"+
                            "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;example"+
                            "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;35754"+localeDecSep+"05 | 19659"+localeDecSep+"1 | 25875"+localeDecSep+"2 | 31804"+localeDecSep+"0 | 12767"+localeDecSep+"7 | 6146"+localeDecSep+"55 | 10660"+localeDecSep+"6&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
                        "<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;An example of a valid .els file is available at "+
			                 "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"http://www.eulerdiagrams.org/eulerAPE/areas_example.php\">http://www.eulerdiagrams.org/eulerAPE/areas_example.php</a>" +
                        "<br><br>&nbsp;2) The decimal separator corresponds to your locale"+
                            "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Your current locale (LanguageCode_CountryCode):   " + Locale.getDefault() +
                            "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Expected decimal separator:   " + Utilities.getLocaleDecimalSeparator()+"<br><br>"
			    		);

			    // handle link events
			    ep.addHyperlinkListener(new HyperlinkListener()
			    {
					@Override
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)){
			               if (Desktop.isDesktopSupported()) {
						      try {
						        try {
									Desktop.getDesktop().browse(new URI(e.getURL().toString()));
								} catch (URISyntaxException e1) {
									e1.printStackTrace();
								}
						      } catch (IOException ex) { }
						    } else {  }
						}
					}
			    });
			    ep.setEditable(false);
			    ep.setBackground(label.getBackground());

			    // show
			    JOptionPane.showMessageDialog(this, ep, "Loading Required Region Areas from File", JOptionPane.ERROR_MESSAGE);
			    
				
				
			}
			return;	
		}
		// the following 2 flags should be set in retrieveFileAreaSpecs if successful 
		// but can set them here too as a precaution
		loadReqAreaSpecsFromFields = false;
		wereReqRegionAreasMult = false;
	
	
	}
	
	
	protected boolean retrieveFileAreaSpec (File selectedDiagFile, boolean recomputeDiagZoneAreas, boolean allowMsgDialog){
		String selectedDiagFileName = selectedDiagFile.getName().substring(0,selectedDiagFile.getName().indexOf('.'));
		checkedIfRequiredRegionAreasShouldBeScaled = false;
		
		if (selectedDiagFile.getName().endsWith(".eld")){

			if (recomputeDiagZoneAreas){
				HashMap<String, Double> areaSpecs = EllipseDiagramOps.computeAreaSpecOfDiagInFile(selectedDiagFile, considerLessPreciseIntPnts);
				if (areaSpecs==null){return false;}
				return retrieveFileAreaSpec_update(areaSpecs, selectedDiagFileName, recomputeDiagZoneAreas, allowMsgDialog);
			} else {	
				return retrieveFileAreaSpec(selectedDiagFileName, false, allowMsgDialog);
			}
		
		} else if (selectedDiagFile.getName().endsWith(".els")){
			HashMap<String, Double> areaSpecs = EllipseDiagramOps.loadAreaSpecsFromFile_venn3(selectedDiagFile);
			if (areaSpecs==null){return false;}
			return retrieveFileAreaSpec_update(areaSpecs, selectedDiagFileName, recomputeDiagZoneAreas, allowMsgDialog);
		}
		return true;
	}
	protected boolean retrieveFileAreaSpec (String selectedDiagFileName, boolean recomputeDiagZoneAreas, boolean allowMsgDialog){
		// load area specs saved in a file containing a list of all the diagrams area spec 
		// load area specs scale as is => do not scale
	
		File areaSpecsListFile = new File (areaSpecsListPath + File.separator + areaSpecsListFileName);
		HashMap<String, Double> areaspec = EllipseDiagramOps.loadAreaSpecsFromFile_venn3(areaSpecsListFile, selectedDiagFileName);		
		return retrieveFileAreaSpec_update(areaspec, selectedDiagFileName, recomputeDiagZoneAreas, allowMsgDialog);
	}
	private boolean retrieveFileAreaSpec_update(HashMap<String, Double> areaspec, String selectedDiagFileName, boolean recomputeDiagZoneAreas, boolean allowMsgDialog){
		if ((areaspec != null) || (!allowMsgDialog)){
			message1Field.setText(selectedDiagFileName);
			message1Field.update(message1Field.getGraphics());
			fileNameOfSelectedDiagAreaSpecs = selectedDiagFileName;
		}
		
		if (areaspec == null){
			String msg = "SwitchBoardPanel.loadFromFileAreaSpecsButton: Could not " + (recomputeDiagZoneAreas ? "compute" : "find and retrieve") + " the area specifications of diagram " + selectedDiagFileName + "\nMake sure that the diagram and its file path are valid";
			System.out.println(msg);
			if (allowMsgDialog){
				JOptionPane.showMessageDialog(this, msg, "Loading Required Region Areas from File", JOptionPane.ERROR_MESSAGE);
			}
			return false;
			
		} else {
			
			clearAllAreaSpecsButton();
			
			loadReqAreaSpecsFromFields = false;
			wereReqRegionAreasMult=false;
	
			requiredAreaSpec = areaspec;
			updateRequiredAreaSpecFields();
			updateEllipseDiagramPanel_newRequiredAreaSpecs();
			
			return true;
		}
	}
	
	
	
	protected void selectDefaultInitDiagRadioButton(){
		if(initDiag == INITDIAG_DEFAULT) {
			defaultInitDiagRadioButton.setSelected(true);
		} else if(initDiag == INITDIAG_RANDOM_ELLIPSES) {
			randomEllipsesInitDiagRadioButton.setSelected(true);
		} else if(initDiag == INITDIAG_RANDOM_CIRCLES) {
			randomCirclesInitDiagRadioButton.setSelected(true);
		} else if(initDiag == INITDIAG_LOAD) {
			fromFileInitDiagRadioButton.setSelected(true);
		} 
	}
	
	protected void defaultInitDiagRadioButton(){
		filePathInitDiagField.setText("");
		filePathInitDiagField.setEnabled(false);
		browseInitDiagButton.setEnabled(false);
	}
	protected void randomEllipsesInitDiagRadioButton(){
		filePathInitDiagField.setText("");
		filePathInitDiagField.setEnabled(false);
		browseInitDiagButton.setEnabled(false);
	}
	protected void randomCirclesInitDiagRadioButton(){
		filePathInitDiagField.setText("");
		filePathInitDiagField.setEnabled(false);
		browseInitDiagButton.setEnabled(false);
	}
	
	
	protected void fromFileInitDiagRadioButton(){
		filePathInitDiagField.setEnabled(true);
		browseInitDiagButton.setEnabled(true);
	}
	
	protected void checkInitDiagHCfieldsCoherence(){
		if (!circlesHCFieldRadioButton.isSelected()){return;}
		
		String warningMsg = "To restrict hill climber to use circles as curves, the initial diagram cannot have ellipses as curves.";
		if (!randomCirclesInitDiagRadioButton.isSelected()){
			
			if (randomEllipsesInitDiagRadioButton.isSelected()){
				JOptionPane.showMessageDialog(this, warningMsg + "\nThus it is not possible to have an initial random diagram made of ellipses.", "Drawing Area-Proportional Euler Diagrams", JOptionPane.WARNING_MESSAGE);
				selectDefaultInitDiagRadioButton();
				
			} else { // if default or loaded from file
				refreshInitDiagButton(false,null);
				EllipseDiagram diagram = ellipseDiagramPanel.getDiagram();
				
				boolean diagHasCircles = EllipseDiagramOps.isCircleDiagram(diagram); // since we did a refresh, the diagram shouldn't be null
				
				if (!diagHasCircles){
					
					if (defaultInitDiagRadioButton.isSelected()){  //this should never happen if the default initial diagram is always set to be a circle venn-3  
						JOptionPane.showMessageDialog(this, warningMsg + "\nThe default diagram is made of ellipses and thus, it cannot be used as an initial diagram." , "Drawing Area-Proportional Euler Diagrams", JOptionPane.WARNING_MESSAGE);
						defaultInitDiagRadioButton.setEnabled(false);
						defaultInitDiagRadioButton.setSelected(false);
						randomCirclesInitDiagRadioButton.setSelected(true);
						ellipseDiagramPanel.setDiagram(null);
						updateEllipseDiagramPanel_newInitDiagram();
						
					} else if (fromFileInitDiagRadioButton.isSelected()){ 
						JOptionPane.showMessageDialog(this, warningMsg + "\nThe diagram you selected to load from file has ellipses and thus, it cannot be used as an initial diagram." , "Drawing Area-Proportional Euler Diagrams", JOptionPane.WARNING_MESSAGE);
						filePathInitDiagField.setText("");
						ellipseDiagramPanel.setDiagram(null);
						updateEllipseDiagramPanel_newInitDiagram();
					} 
				}
			}
		}
	}
	
	protected void filePathInitDiagField_ChangeFilePath(){
		checkInitDiagHCfieldsCoherence();
	}
	
	protected void browseInitDiagButton(){
		
		JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			initDiagFile = chooser.getSelectedFile();
		} else {
			return;
		}
		
		if (!initDiagFile.exists()){
			JOptionPane.showMessageDialog(this, "File " + initDiagFile.getAbsolutePath() + " does not exist", "Load Initial Diagram", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!initDiagFile.getAbsoluteFile().toString().endsWith(".eld")){
			JOptionPane.showMessageDialog(this, "Incorrect file type. File " + initDiagFile.getAbsolutePath() + " does not end with .eld", "Load Initial Diagram", JOptionPane.ERROR_MESSAGE);
			return;
		}
		filePathInitDiagField.setText(initDiagFile.getAbsolutePath());
		filePathInitDiagField_ChangeFilePath();
	}
	
	
	protected void loadDefaultInitDiag(BufferedWriter bwRunLogger){
		boolean foundZAwithNoValue = false;
		
		
		for (int i=0; i<7; i++){
			if (requiredAreaSpecsField[i].getText().trim().equals("")){
				foundZAwithNoValue = true;
				break;
			}
		}
		if (foundZAwithNoValue){
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(this, "The 'required area' column is missing some values.\nMake sure that every region is assigned an area.", "Generating the Starting Diagram", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		
		EllipseDiagram ellDiag = EllipseDiagramOps.generateAppropriateInitDiag(new double[]{0.0,120.0,60.0}, requiredAreaSpec, initDiagDefaultType, initdiagNoEmptyDisconnLikeZones_PolyAreaDiscardThreshold, considerLessPreciseIntPnts);
		
		if (ellDiag == null){	
			String errmsg = "**Error finding appropriate initial diagram: null was returned for the diagram most probably because the centre of the 3rd circles could not be found with the bisection method (return the null) because a valid upper and/or lower limit could not be found";
			System.out.println(errmsg);
			if (bwRunLogger != null){
				hcRunDetails = fileNameOfSelectedDiagAreaSpecs + " | " + errmsg;
			}
			return;
		}	
		if (Double.isNaN(ellDiag.getEllipses().get(2).getXc()) || Double.isNaN(ellDiag.getEllipses().get(2).getYc())){
			String errmsg = "**Error finding appropriate initial diagram: a NaN was generated by EllipseDiagramOps.getArcAngleForOverlappingCircles() due to aCos(x) where x>1 when EllipseDiagramOps.areaOverlapSharedZoneWith3rdCircle() was invoked";
			System.out.println(errmsg);
			if (bwRunLogger != null){
				hcRunDetails = fileNameOfSelectedDiagAreaSpecs + " | " + errmsg;
			}
			return;
		} 
	    
		ellipseDiagramPanel.setDiagram(ellDiag);
		return;

	}
	
	protected void loadRandomInitDiag(boolean restrictToCircles){
		filePathInitDiagField.setText("");
		filePathInitDiagField.setEnabled(false);
		browseInitDiagButton.setEnabled(false);
		
		String diagLibPath = "";
		if (!restrictToCircles){
			diagLibPath = diagLibMainDirPath + File.separator + ellipsesDiagDirName + File.separator + noOfEllipses;
		} else {
			diagLibPath = diagLibMainDirPath + File.separator + circlesDiagDirName + File.separator + noOfEllipses;
		}
		File diagLibFile = new File (diagLibPath);
		EllipseDiagram randomDiagram;
		
		if (RANDOM_DIAG__LOAD_FROM_LIB_IF_AVAILABLE && diagLibFile.exists() && (EllipseDiagramOps.getDiagFilesInDir(diagLibFile).length > 0)){
			randomDiagram = EllipseDiagramOps.getARandomDiagFromLib(diagLibFile, considerLessPreciseIntPnts);
		} else {
			Random r = new Random();
			randomDiagram = EllipseDiagramOps.generateAValidRandomDiagram(noOfEllipses, 5.0, ellipseDiagramPanel.getWidth()/2, EllipseDiagramPanel.min_xy, EllipseDiagramPanel.max_xy, restrictToCircles, true, true, true, 0.0, false, r);
		}
		ellipseDiagramPanel.setDiagram(randomDiagram);
	}

	
	protected void generateRandomDiagLib(String diagLibDirPath, int noOfDiags, boolean considerLessPrecisePossibleIntPnts){
		generateRandomDiagLib(diagLibDirPath, noOfDiags, false, false, null, considerLessPrecisePossibleIntPnts);
	}
	protected void generateRandomDiagLib(String diagLibDirPath, int noOfDiags, boolean disallowDiagsWithCloseToEmptyZones, boolean disallowDiagsWithCloseToDisconnectedZones, Double minPolyAreaInZone, boolean considerLessPrecisePossibleIntPnts){
		generateLibDirPathField = new JTextField();
		generateLibDirPathField.setText(diagLibDirPath);
		generateLibNoOfDiagPathField = new JTextField();
		generateLibNoOfDiagPathField.setText(Integer.toString(noOfDiags));
		randomCirclesInitDiagRadioButton = new JRadioButton();
		randomCirclesInitDiagRadioButton.setSelected(false);
		
		int noOfEllipsesInDiag = 3;
		double maxSemiAxis = ellipseDiagramPanel.getWidth()/4;  
		double minPercentOfMax = 0.03;
		double minSemiAxis = maxSemiAxis*minPercentOfMax; 
		
		EllipseDiagramOps.generateAValidRandomDiagramLib(noOfEllipsesInDiag, minSemiAxis, maxSemiAxis, 
										                 EllipseDiagramPanel.min_xy, EllipseDiagramPanel.max_xy, 
										                 randomCirclesInitDiagRadioButton.isSelected(), true,
										                 generateLibDirPathField.getText().trim(), 
										                 (int)(Utilities.safeParseDouble(generateLibNoOfDiagPathField.getText().trim())).doubleValue(),
										                 ellipseDiagramPanel, 
										                 disallowDiagsWithCloseToEmptyZones, disallowDiagsWithCloseToDisconnectedZones, minPolyAreaInZone, considerLessPrecisePossibleIntPnts);
		JOptionPane.showMessageDialog(this, "Done!!", "Generating Random Venn-3 Diagram Library with Ellipses", JOptionPane.INFORMATION_MESSAGE);
	} 
	
	protected void generateRandomAreaSpecsLib(String areaSpecsLibDirPath, int noOfAreaSpecs){
		generateLibDirPathField = new JTextField();
		generateLibDirPathField.setText(areaSpecsLibDirPath);
		generateLibNoOfDiagPathField = new JTextField();
		generateLibNoOfDiagPathField.setText(Integer.toString(noOfAreaSpecs));
		randomCirclesInitDiagRadioButton = new JRadioButton();
		randomCirclesInitDiagRadioButton.setSelected(false);
		
		int noOfEllipsesInDiag = 3;
		double maxZoneArea = 10000;
		double minZoneArea = 1;
		int noOfDPs = 2; 
		
		EllipseDiagramOps.generateRandomAreaSpecsLib (noOfEllipsesInDiag, minZoneArea, maxZoneArea, 
													  generateLibDirPathField.getText().trim(),
													  noOfAreaSpecs, 1, noOfDPs);
		JOptionPane.showMessageDialog(this, "Done!!", "Generating Random Area Specifications Library", JOptionPane.INFORMATION_MESSAGE);
	} 
	
		
	protected void validateLibButton(){
		
		String dirPath = generateLibDirPathField.getText().trim();
		File[] diagFilesInDir = EllipseDiagramOps.getDiagFilesInDir(new File(dirPath));

		try{
		
			BufferedWriter bwInvalidList = new BufferedWriter(new FileWriter(dirPath + "\\ErrorWhenGeneratingInitialDiag_xy_20100815.txt"));
			bwInvalidList.newLine(); 			
			bwInvalidList.append("List of diagrams for which an initial diagram for the hill climber to draw the area-specification could not be generated");
			bwInvalidList.newLine();bwInvalidList.newLine();
			
			for (File diagFile : diagFilesInDir){
				ellipseDiagramPanel.diagram = null;
				retrieveFileAreaSpec (diagFile, true, false);
				refreshInitDiagButton(false, null);
				if (!refreshInitDiagButton(false, null)){
					System.out.println("Error when refreshing and generating the initial diagram for the loaded area-specifications");
					bwInvalidList.append(diagFile.getName().substring(0,diagFile.getName().indexOf(".")));
					bwInvalidList.newLine(); 
				}
			}
			bwInvalidList.close();
		} catch(IOException e) {
			System.out.println("SwitchBoardPanel.validateLibButton: An IO exception occured -> " + e);
		}
		
		JOptionPane.showMessageDialog(this, "Done!!", "Validating Diagram Library", JOptionPane.INFORMATION_MESSAGE);
	}
		
	protected void loadInitDiagFromFile(){
		if (initDiagFile==null){return;}
		
		ellipseDiagramPanel.setDiagram(EllipseDiagramOps.loadDiagFromFile(initDiagFile, considerLessPreciseIntPnts));
	}
	
	protected boolean refreshInitDiagButton(boolean suppressErrorMsg, BufferedWriter bwRunLogger){
		
		message1Field.setText("");
		message1Field.update(message1Field.getGraphics());
		
		boolean defaultInitDiagRadioButton = true;
		
		String[] zoneLabels = EllipseDiagramOps.getZoneLabels(noOfEllipses, false);

		if (defaultInitDiagRadioButton){
			loadDefaultInitDiag(bwRunLogger);
			
		} else if (randomEllipsesInitDiagRadioButton.isSelected()){
			loadRandomInitDiag(false);
		} else if (randomCirclesInitDiagRadioButton.isSelected()){
			loadRandomInitDiag(true);
		} else if (fromFileInitDiagRadioButton.isSelected()){
			loadInitDiagFromFile();
		}
		
		if (ellipseDiagramPanel.getDiagram()==null){
			return false;
		}
		
		if (!ellipseDiagramPanel.getDiagram().isValid()){
			if (!suppressErrorMsg){
			}
			return false;
		}
		
		updateEllipseDiagramPanel_newInitDiagram();
		deleteTimerDetails();
		return true;
	}
	

	protected boolean generateStartingDiagramButton(BufferedWriter bwDiagListFailedToGenInitDiag){
		
		boolean foundZAwithNoValue = false;
		for (int i=0; i<7; i++){
			if (requiredAreaSpecsField[i].getText().trim().equals("")){
				foundZAwithNoValue = true;
				break;
			}
		}
		if (foundZAwithNoValue){
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(this, "The 'required area' column is missing some values.\nMake sure that every region is assigned an area.", "Searching for a Diagram", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		Iterator itrReqAreaSpecs = requiredAreaSpec.entrySet().iterator();
		Entry<String, Double> reqAreaspecEntry;
		String reqAreaspecName;
		double reqAreaspecZoneArea;
		
		// load required region areas from field if requiredAreaSpec=null and loadReqAreaSpecsFromFields=false
		if (requiredAreaSpec == null){
			
			if (!loadReqAreaSpecsFromFields || runningATest){ 
				System.out.println("SwitchBoardPanel.generateStartingDiagramButton: requiredAreaSpecs=null, loadReqAreaSpecsFromFields="+loadReqAreaSpecsFromFields+", runningATest="+runningATest);
				return false; 
			}
			
			String[] zoneLabels = EllipseDiagramOps.getZoneLabels(noOfEllipses, false);
			double actualArea;
			for(int i=0; i<zoneLabels.length; i++) {
				actualArea = Utilities.safeParseDouble(Utilities.changeLocaleDecimalSeparatorToDefault(requiredAreaSpecsField[i].getText().trim()));
				requiredAreaSpec.put(zoneLabels[i], actualArea);
			}
			ellipseDiagramPanel.setRequiredAreaSpecs(requiredAreaSpec);
			checkedIfRequiredRegionAreasShouldBeScaled = false;
		}
		
		// scale required region areas		
		if (!checkedIfRequiredRegionAreasShouldBeScaled){
			if (!scaleRequiredRegionAreas()){
				if (bwDiagListFailedToGenInitDiag!=null){
					
					String enteredReqAreaSpecAsStr = "";
					while (itrReqAreaSpecs.hasNext()){
						reqAreaspecEntry = (Entry<String, Double>)itrReqAreaSpecs.next();
						reqAreaspecName = reqAreaspecEntry.getKey();
						enteredReqAreaSpecAsStr += (reqAreaspecName + "="+ requiredAreaSpec.get(reqAreaspecName) + " | ");
					}
					
					try {
						bwDiagListFailedToGenInitDiag.append(fileNameOfSelectedDiagAreaSpecs + " | Error: Could not scale the required region areas: "+enteredReqAreaSpecAsStr);
						bwDiagListFailedToGenInitDiag.newLine();
						bwDiagListFailedToGenInitDiag.flush();
					} catch (IOException e) {
						System.out.println("SwitchBoardPanel.generateStartingDiagramButton: An IO exception occured -> " + e);
						e.printStackTrace();
					}
				}
				
				if (!runningATest){
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(this, "Error while scaling the required region area", "Scaling the Required Region Area", JOptionPane.ERROR_MESSAGE);
				} 
				
				return false;
			}
		}

		
		// generate initial diagram
		
		if (!refreshInitDiagButton(false, null)){ 
		
			String scaledReqAreaspecAsStr = "";
			if ((bwDiagListFailedToGenInitDiag!=null) || (!runningATest)){
				itrReqAreaSpecs = requiredAreaSpec.entrySet().iterator();
				while (itrReqAreaSpecs.hasNext()){
					reqAreaspecEntry = (Entry<String, Double>)itrReqAreaSpecs.next();
					reqAreaspecName = reqAreaspecEntry.getKey();
					reqAreaspecZoneArea = reqAreaspecEntry.getValue();
					scaledReqAreaspecAsStr += (reqAreaspecName + "="+ requiredAreaSpec.get(reqAreaspecName) + " | ");
				}
			}
			
			if (bwDiagListFailedToGenInitDiag!=null){
				try {
					bwDiagListFailedToGenInitDiag.append(fileNameOfSelectedDiagAreaSpecs + " | Error: Could not generate a valid init diag for internally scaled region areas: "+scaledReqAreaspecAsStr);
					bwDiagListFailedToGenInitDiag.newLine();
					bwDiagListFailedToGenInitDiag.flush();
				} catch (IOException e) {
					System.out.println("SwitchBoardPanel.generateStartingDiagramButton: An IO exception occured -> " + e);
					e.printStackTrace();
				}
			}
			
			if (runningATest){   //to avoid any message dialogs popping up
				return false;
				
			} else {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(this, "Error while generating the starting diagram: Could not compute the area of some regions. Check that the region area are not too small.", "Generating the Starting Diagram", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		return true;
	}

	
	protected boolean generateStartingDiagramButton_noReqAreasScaling(BufferedWriter bwDiagListFailedToGenInitDiag){
		
		if (!refreshInitDiagButton(false, null)){ 
		
			// check if there are small areas if so multiply by 10
			
			boolean foundAreaLessThan10 = false; 
			Iterator itrReqAreaSpecs = requiredAreaSpec.entrySet().iterator();
			Entry<String, Double> reqAreaSpecsEntry;
			String reqAreaSpecName = "";
			Double reqAreaspecZoneArea = null;
			
			int noOfEmptyZones=0;
			String scaledReqAreaSpecAsStr = "";
			reqRegionAreasMultFactor=10;
			while (itrReqAreaSpecs.hasNext()){
				reqAreaSpecsEntry = (Entry<String, Double>)itrReqAreaSpecs.next();
				reqAreaSpecName = reqAreaSpecsEntry.getKey();
				reqAreaspecZoneArea = reqAreaSpecsEntry.getValue();
				if (reqAreaspecZoneArea==0){
					noOfEmptyZones++;
				}
				requiredAreaSpec.put(reqAreaSpecName,reqAreaspecZoneArea*reqRegionAreasMultFactor);
				scaledReqAreaSpecAsStr += (reqAreaSpecName + "="+ requiredAreaSpec.get(reqAreaSpecName) + " | ");
			}	
			
			if (bwDiagListFailedToGenInitDiag!=null){
				try {
					bwDiagListFailedToGenInitDiag.append(fileNameOfSelectedDiagAreaSpecs + " | Error: Could not generate a valid init diag: req region areas x "+reqRegionAreasMultFactor+" and attempted to regenerate the init diag. Scaled region areas: "+scaledReqAreaSpecAsStr);
					bwDiagListFailedToGenInitDiag.newLine();
					bwDiagListFailedToGenInitDiag.flush();
				} catch (IOException e) {
					System.out.println("SwitchBoardPanel.generateStartingDiagramButton: An IO exception occured -> " + e);
					e.printStackTrace();
				}
				
			}
			
			wereReqRegionAreasMult=true;
			 
			 
			if (!refreshInitDiagButton(false, null)){
				
				if (bwDiagListFailedToGenInitDiag!=null){
					try {
						bwDiagListFailedToGenInitDiag.append(fileNameOfSelectedDiagAreaSpecs + " | Error: still could not generate a valid init diag after scaling x "+reqRegionAreasMultFactor+ " (no of empty zones = "+noOfEmptyZones+")");
						bwDiagListFailedToGenInitDiag.newLine();
						bwDiagListFailedToGenInitDiag.flush();
					} catch (IOException e) {
						System.out.println("SwitchBoardPanel.generateStartingDiagramButton: An IO exception occured -> " + e);
						e.printStackTrace();
					}
				}
				
				if (runningATest){   //to avoid any message dialogs popping up
					return false;
				}
	
				if (noOfEmptyZones==7){
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(this, "A diagram does not exist: All your region areas are 0.", "Searching for a Diagram", JOptionPane.WARNING_MESSAGE);
					return false;					
				} else if (noOfEmptyZones > 0){
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(this, "Error while generating the starting diagram: Reduce the number of regions with area 0.", "Generating the Starting Diagram", JOptionPane.ERROR_MESSAGE);
					return false;
				} else {
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(this, "Error while generating the starting diagram: Could not compute the area of some regions. Check that the region area are not too small.", "Generating the Starting Diagram", JOptionPane.ERROR_MESSAGE);
					return false;
				}
		    }
			if (bwDiagListFailedToGenInitDiag!=null){
				try {
					bwDiagListFailedToGenInitDiag.append(fileNameOfSelectedDiagAreaSpecs + " | Valid initial diagram generated successfully after scaling x "+reqRegionAreasMultFactor);
					bwDiagListFailedToGenInitDiag.newLine();
					bwDiagListFailedToGenInitDiag.flush();
				} catch (IOException e) {
					System.out.println("SwitchBoardPanel.generateStartingDiagramButton: An IO exception occured -> " + e);
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	
	protected void ellipsesHCFieldRadioButton (){
		// re-enable and reset parameters which might have been altered for diagrams with circles 
		
		// r parameter for Hill Climber
		if (rHCField == null){return;}
		rHCField.setText(Double.toString(HillClimber.DEFAULT_R));
		rHCField.setEnabled(true);

	}
	protected void circlesHCFieldRadioButton (){
		
		// r parameter for Hill Climber -> not needed -> rotation makes no difference for circles 
		if (rHCField == null){return;}
		rHCField.setText("");
		rHCField.setEnabled(false);

	}
	protected void updateCurveType(Curves curveType){
		if (curveType == Curves.ELLIPSES){
			ellipsesHCFieldRadioButton.setSelected(true);
		} else if (curveType==Curves.CIRCLES){
			circlesHCFieldRadioButton.setSelected(true);
		} 
	}
	protected void yesShowLabelsRadioButton(){
		ellipseDiagramPanel.labelsDisplayMode = EllipseDiagramPanel.LABELS_SIMPLE;
		ellipseDiagramPanel.updateDisplay(0,false);
	}
	protected void noShowLabelsRadioButton(){
		ellipseDiagramPanel.labelsDisplayMode = EllipseDiagramPanel.LABELS_HIDE;
		ellipseDiagramPanel.updateDisplay(0,false);
	}
	protected void updateShowLabels(boolean showLabels){
		if (showLabels){
			yesShowLabelsRadioButton();
		} else {
			noShowLabelsRadioButton();
		}
	}
	protected void yesColourRadioButton(){
		ellipseDiagramPanel.useColor = true;
		ellipseDiagramPanel.updateDisplay(0,false);
	}
	protected void noColourRadioButton(){
		ellipseDiagramPanel.useColor = false;
		ellipseDiagramPanel.updateDisplay(0,false);
	}
	protected void updateShowInColour(boolean showInColour){
		if (showInColour){
			yesColourRadioButton();
		} else {
			noColourRadioButton();
		}
	}


	protected void resetParamsHCButton(){
		dHCField.setText(Double.toString(HillClimber.DEFAULT_D));
		sHCField.setText(Double.toString(HillClimber.DEFAULT_S));
		if (((ellipsesHCFieldRadioButton == null) && (HillClimber.DEFAULT_RESTRICT_TO_CIRLCES)) || (ellipsesHCFieldRadioButton.isSelected())){ 
			rHCField.setText(Double.toString(HillClimber.DEFAULT_R));
		}
		dHCField.update(dHCField.getGraphics());
		sHCField.update(sHCField.getGraphics());
		rHCField.update(rHCField.getGraphics());
	}
	
	public void setParamsHC(Double[] params){
		// Note that params[0]=d, params[1]=s, params[2]=r  
		if ((params==null) || (params.length!=3)){return;}
		dHCField.setText(Double.toString(Utilities.roundToDps(params[0],FITNESS_DIGITS_BEFORE_DP, FITNESS_DIGITS_AFTER_DP)));
		sHCField.setText(Double.toString(Utilities.roundToDps(params[1],FITNESS_DIGITS_BEFORE_DP, FITNESS_DIGITS_AFTER_DP)));
		rHCField.setText(Double.toString(Utilities.roundToDps(params[2],FITNESS_DIGITS_BEFORE_DP, FITNESS_DIGITS_AFTER_DP)));
		dHCField.update(dHCField.getGraphics());
		sHCField.update(sHCField.getGraphics());
		rHCField.update(rHCField.getGraphics());
	}
	
	
	protected void browseLogfileDirButton(){

		File logFileDir;
		
		JFileChooser chooser = new JFileChooser((logfileDirPathField.getText().trim().equals("")?currentUserDir:logfileDirPathField.getText().trim()));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			logFileDir = chooser.getSelectedFile();
		} else {
			return;
		}
		
		if (!logFileDir.exists()){
			JOptionPane.showMessageDialog(this, "Directory " + logFileDir.getAbsolutePath() + " does not exist", "Choosing Log File Destination Directory", JOptionPane.ERROR_MESSAGE);
			return;
		}
		logfileDirPathField.setText(logFileDir.getAbsolutePath());
	}

	
	protected void simpleLabelRadioButton() {
		ellipseDiagramPanel.setLabelsDisplayMode(EllipseDiagramPanel.LABELS_SIMPLE); 
		updateEllipseDiagramPanel_updateLabelsDisplayMode();
	}
	
	protected void advancedLabelRadioButton() {
		ellipseDiagramPanel.setLabelsDisplayMode(EllipseDiagramPanel.LABELS_ADVANCED); 
		updateEllipseDiagramPanel_updateLabelsDisplayMode();
	}

	protected void hideLabelRadioButton() {
		ellipseDiagramPanel.setLabelsDisplayMode(EllipseDiagramPanel.LABELS_HIDE); 
		updateEllipseDiagramPanel_updateLabelsDisplayMode();
	}

	
	protected void colorRadioButton() {
		ellipseDiagramPanel.useColor = true;
		updateEllipseDiagramPanel_updateDisplayColour();
	}	

	protected void monochromeRadioButton() {
		ellipseDiagramPanel.useColor = false;
		updateEllipseDiagramPanel_updateDisplayColour();
	}
	
	
	protected boolean requiredAreaSpecHasEmptyRegionAreas(){

		Iterator itrReqAreaSpecs = requiredAreaSpec.entrySet().iterator();
		Entry<String, Double> reqAreaSpecsEntry;
		double reqAreaspecZoneArea;
		int noOfEmptyZones = 0;
		
		while (itrReqAreaSpecs.hasNext()){
			reqAreaSpecsEntry = (Entry<String, Double>)itrReqAreaSpecs.next();
			reqAreaspecZoneArea = reqAreaSpecsEntry.getValue();
			if (Math.abs(reqAreaspecZoneArea) <= 1e-15){ //ie the region area is zero
				noOfEmptyZones++;
			}
		}
		
		if (noOfEmptyZones>0){
			String errMsg="";
			if (noOfEmptyZones==7){
				errMsg="A diagram does not exist: All your region areas are 0.";
				if (runningATest){
					System.out.println(errMsg);
				} else {
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(this, errMsg, "Validating Required Region Areas", JOptionPane.WARNING_MESSAGE);
				}
				return true;
				
			} else if (noOfEmptyZones > 0){
				errMsg="There are region areas with area 0: Reduce the number of regions with area 0.";
				if (runningATest){
					System.out.println(errMsg);
				} else {
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showMessageDialog(this, errMsg, "Validating Required Region Areas", JOptionPane.WARNING_MESSAGE);
				}
				return true;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	
	protected boolean scaleRequiredRegionAreas (){
		// before calling this method to scale the required region areas, the require region areas should be entered 
		// and checks should have been done to ensure that the region areas are >0
		
		if (checkedIfRequiredRegionAreasShouldBeScaled){return true;}
	    if (requiredAreaSpec == null){return false;}
	    if (requiredAreaSpecHasEmptyRegionAreas()){return false;}
	
	    
		// ... requiredAreaSpecs loaded in generateStartingDiagramButton if loadReqAreaSpecsFromFields=true 
		// ... but need to scale first and have to make sure not to get any dependencies wrong 
		// ... so decided to regenerate do a workaround for now ie 
		// ... load areaspecs now, scale, set loadReqAreaSpecsFromFields to false and then generate starting diagram
	
		String[] zoneLabels = EllipseDiagramOps.getZoneLabels(noOfEllipses, false);
    
		// a fixed smallest region
		double smallestRegion_fixedArea = 100; 
		double smallestRegion_actualArea = -1;
		double actualArea;
	    for(String zl : zoneLabels) {
			actualArea = requiredAreaSpec.get(zl);
			if ((smallestRegion_actualArea == -1) || (smallestRegion_actualArea > actualArea)){
				smallestRegion_actualArea = actualArea;
			}
		}
	    if (smallestRegion_actualArea == 0){return false;} // or else divide by 0 and get NaN -> though region areas should be checked to ensure that greater than 0 before scaling them
	    												   // check if 0 and not if something close to 0 because, if eg smallest region area is 1e-7 then reqRegionAreasMultFactor=100/1e-7=1,000,000,000
	    reqRegionAreasMultFactor = smallestRegion_fixedArea/smallestRegion_actualArea;
	
	    Iterator itrReqAreaSpecs = requiredAreaSpec.entrySet().iterator();
		Entry<String, Double> reqAreaSpecsEntry;
		double scaledArea;
		
		while (itrReqAreaSpecs.hasNext()){
			reqAreaSpecsEntry = (Entry<String, Double>)itrReqAreaSpecs.next();
			actualArea = reqAreaSpecsEntry.getValue();
			scaledArea = actualArea*reqRegionAreasMultFactor;
			scaledArea = Utilities.roundToDps(scaledArea, 15); //set noOfDPs to 15 because if generate random area spec lib without any limits on the noOfDPs, then region areas would have 13dps
			reqAreaSpecsEntry.setValue(scaledArea);
		}	
		
		wereReqRegionAreasMult=true;
		checkedIfRequiredRegionAreasShouldBeScaled=true;
		
		return true;
	}
	

	private void runDrawDiag (boolean viewAnimation, String msgTitle, int runMode, boolean lockHCparams, boolean suppressFinalMsg, BufferedWriter bwRunLogger, BufferedWriter bwDiagListFailedToGenInitDiag, boolean updateStopWatch){
		
		msgTitle = "Searching for a Diagram";
		hcRunAreaDetails = new String[7];
		
		boolean saveDetailsWhenReachMaxRegionAreaThreshold = true;
		double maxRegionAreaAbsErrThreshold = 0.001;
		double maxRegionAreaRelErrThreshold = 1e-6;
		double maxRegionAreaAbsDiffBetweenReqActualProportionsThreshold = 1e-6; 
		double maxRegionAreaAbsErrOverReqTotThreshold = 1e-6;
		
		String errMsg;
		
		boolean foundZAwithNoValue = false;
		for (int i=0; i<7; i++){
			if (requiredAreaSpecsField[i].getText().trim().equals("")){
				foundZAwithNoValue = true;
				break;
			}
		}
		if (foundZAwithNoValue){
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(this, "The 'required area' column is missing some values.\nMake sure that every region is assigned an area.", "Searching for a Diagram", JOptionPane.ERROR_MESSAGE);
			return;
		}
	
		if (!suppressFinalMsg){
			if (!logfileDirPathField.getText().trim().equals("") && logfileFileNameField.getText().trim().equals("")){
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(this, "Missing file name.\n\nYou have selected a directory where the diagram files will be saved, but you did not enter a file name.\nEnter a file name or delete the directory path (if you do not want to save the diagram files), and re-run.", "Saving the Diagram Files", JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		if (!suppressFinalMsg){
			if (!logfileDirPathField.getText().trim().equals("") && !((new File(logfileDirPathField.getText().trim())).exists())){
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(this, "Directory "+logfileDirPathField.getText().trim()+" does not exist.", "Saving the Diagram Files", JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		
		if (!suppressFinalMsg){
			if (logfileDirPathField.getText().trim().equals("") && !logfileFileNameField.getText().trim().equals("")){
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(this, "Missing directory.\n\nYou have entered a file name to save the diagram files, but you have not selected a directory.\nSelect a directory or delete the file name (if you do not want to save the diagram files), and re-run.", "Saving the Diagram Files", JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		if (!suppressFinalMsg){
			if (!logfileFileNameField.getText().trim().equals("") && !(logfileDirPathField.getText().trim().equals("")) && ((new File(logfileDirPathField.getText().trim())).exists())){
				String filePath = logfileDirPathField.getText().trim()+File.separator+logfileFileNameField.getText().trim();
			    if ( ((new File(filePath+"_init.png")).exists()) || ((new File(filePath+"_init.eld")).exists()) || ((new File(filePath+"_final.png")).exists()) || ((new File(filePath+"_final.eld")).exists())) {
			    	if (!(prevFileNameOfSelectedDiagAreaSpecs.equals(logfileFileNameField.getText().trim()))){ //then user already informed 
						Toolkit.getDefaultToolkit().beep();
			    		if(JOptionPane.showConfirmDialog(this, "Diagram files with the name "+logfileFileNameField.getText().trim()+" already exist in "+logfileDirPathField.getText().trim()+" and will be overwritten.\nDo you still want to continue?", "Saving the Diagram Files", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)==JOptionPane.NO_OPTION){
							return;
						}
			    	}
			    }
			}
		}
		
		if (logfileFileNameField != null){
			fileNameOfSelectedDiagAreaSpecs = logfileFileNameField.getText().trim();
		}
		if (!fileNameOfSelectedDiagAreaSpecs.equals("")){
			if (fileNameOfSelectedDiagAreaSpecs.equals(prevFileNameOfSelectedDiagAreaSpecs)){
				fileNameOfSelectedDiagAreaSpecs_sameCount ++;
				fileNameOfSelectedDiagAreaSpecs = fileNameOfSelectedDiagAreaSpecs +"("+fileNameOfSelectedDiagAreaSpecs_sameCount+")";
			} else {
				prevFileNameOfSelectedDiagAreaSpecs = fileNameOfSelectedDiagAreaSpecs;
				fileNameOfSelectedDiagAreaSpecs_sameCount=0;
			}
		}
		
		if (!generateStartingDiagramButton(bwDiagListFailedToGenInitDiag)){
			if (bwRunLogger != null){
				hcRunDetails = fileNameOfSelectedDiagAreaSpecs + " | Error could not generate initial diagram"; 
			}
			return;
		} else {
			if (genInitDiagOnly){
				if (bwRunLogger != null){
					hcRunDetails = fileNameOfSelectedDiagAreaSpecs + " | Generated successfully";	
				}
				getLogfileFileFromField(false, runMode,lockHCparams,null);
				// save init diag
				if (savingInitDiagImgFile != null){
					EllipseDiagramOps.saveDiagImgToFile(ellipseDiagramPanel, savingInitDiagImgFile);
				}
				// save init diag
				if (savingInitDiagSVGFile != null){
					EllipseDiagramOps.saveDiagToSVGFile(ellipseDiagramPanel, savingInitDiagSVGFile);
				}				
				// save init diag
				if (savingInitDiagFile != null){
					EllipseDiagram diag = ellipseDiagramPanel.getDiagram();
					EllipseDiagramOps.saveDiagToFile(diag, savingInitDiagFile);
				}
				return;
			}
		}

		
		message1Field.setText("");
		message1Field.update(message1Field.getGraphics());
		
		Double d = HillClimber.DEFAULT_D;
		Double s = HillClimber.DEFAULT_S;
		Double r = HillClimber.DEFAULT_R;
		Double[] hcParams = new Double[]{d,s,r}; 
		boolean restrictToCircles = circlesHCFieldRadioButton.isSelected();
		
		String runModeStr = getRunModeStr(runMode, lockHCparams, null);
		
		if (runUserFriendlyMode){
			getLogfileFileFromField(true, runMode,lockHCparams,hcParams);
		} else {
			getLogfileFileFromField(false, runMode,lockHCparams,hcParams);			
		}
		
		String intermediateFilesAddSuffix_absErr = "maxAreaAbsDiffLessThan"+maxRegionAreaAbsErrThreshold;
		String intermediateFilesAddSuffix_relErr = "maxAreaRelDiffLessThan"+maxRegionAreaRelErrThreshold;
		String logFileDirPath = getLogFileDirFromField();
		File savingIntermediateDiagImgFile_absErr = null;
		File savingIntermediateDiagFile_absErr = null;
		File savingIntermediateDiagImgFile_relErr = null;
		File savingIntermediateDiagFile_relErr = null;
		if ((logFileDirPath!=null) && (!logFileDirPath.equals(""))) {
			savingIntermediateDiagImgFile_absErr = new File (logFileDirPath + File.separator + getFullIntermediateDiagImgName(false, runMode, lockHCparams, hcParams, intermediateFilesAddSuffix_absErr));
			savingIntermediateDiagFile_absErr = new File (logFileDirPath + File.separator + getFullIntermediateDiagName(false, runMode, lockHCparams, hcParams, intermediateFilesAddSuffix_absErr));
			savingIntermediateDiagImgFile_relErr = new File (logFileDirPath + File.separator + getFullIntermediateDiagImgName(false, runMode, lockHCparams, hcParams, intermediateFilesAddSuffix_relErr));
			savingIntermediateDiagFile_relErr = new File (logFileDirPath + File.separator + getFullIntermediateDiagName(false, runMode, lockHCparams, hcParams, intermediateFilesAddSuffix_relErr));
		}

		
		EllipseDiagram diag = ellipseDiagramPanel.getDiagram();
		
		// save init diag
		if (savingInitDiagImgFile != null){
			EllipseDiagramOps.saveDiagImgToFile(ellipseDiagramPanel, savingInitDiagImgFile);
		}
		// save init diag
		if (savingInitDiagSVGFile != null){
			EllipseDiagramOps.saveDiagToSVGFile(ellipseDiagramPanel, savingInitDiagSVGFile);
		}
		// save init diag
		if (savingInitDiagFile != null){
			EllipseDiagramOps.saveDiagToFile(diag, savingInitDiagFile);
		}
		//updateFieldsFromDiagram(); 
		deleteTimerDetails();
		
		hillClimber = null; //to avoid recreating objects 
		if (restrictToCircles){
			hillClimber = new HillClimber(d, s, diag, requiredAreaSpec, ellipseDiagramPanel, this, logfileFile, fitnessMeasure, forceToTermHC, MAX_NO_OF_ITERATIONS, hcRunType);
		} else {
			hillClimber = new HillClimber(d, s, r, diag, requiredAreaSpec, ellipseDiagramPanel, this, logfileFile, fitnessMeasure, forceToTermHC, MAX_NO_OF_ITERATIONS, hcRunType);
		}
		
		if (!viewAnimation){
			int noOfZones = diag.getZoneLabels().length;
			Color fontColor = Color.GRAY;
			for (int z=0; z<noOfZones; z++){
				currAreaField[z].setForeground(fontColor);
				currAreaPCField[z].setForeground(fontColor);
				diffReqActualField[z].setForeground(fontColor);
				diffReqActualPCField[z].setForeground(fontColor);
				
				currAreaField[z].update(currAreaField[z].getGraphics());
				currAreaPCField[z].update(currAreaPCField[z].getGraphics());
				diffReqActualField[z].update(diffReqActualField[z].getGraphics());
				diffReqActualPCField[z].update(diffReqActualPCField[z].getGraphics());
			}
		}
		diagErrorField.setForeground(Color.GRAY);
		if (viewAnimation){
			diagErrorField.setText("");
		}
		diagErrorField.update(diagErrorField.getGraphics());
		
		
		currentlyDrawingDiag = true;
		String reasonForTerminating="";
		try{
			reasonForTerminating = hillClimber.run_multipleChangesPerIter(lockHCparams, viewAnimation, updateStopWatch, null, null, null, null, maxRegionAreaAbsErrThreshold, maxRegionAreaRelErrThreshold, maxRegionAreaAbsDiffBetweenReqActualProportionsThreshold, maxRegionAreaAbsErrOverReqTotThreshold);
		} catch (Exception e){
			System.out.println("Error during optimization: "+e);
			reasonForTerminating = "Error during optimization";
			
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(this, "Error during the search process: Please report this error and email your region areas to the developers.", "Searching for a Diagram", JOptionPane.ERROR_MESSAGE);
			return;
		}
		currentlyDrawingDiag = false;
	    
		
		// save diag
		if (savingFinalDiagImgFile != null){
			EllipseDiagramOps.saveDiagImgToFile(ellipseDiagramPanel, savingFinalDiagImgFile);
		}
		// save diag
		if (savingFinalDiagSVGFile != null){
			EllipseDiagramOps.saveDiagToSVGFile(ellipseDiagramPanel, savingFinalDiagSVGFile);
		}
		// save diag
		if (savingFinalDiagFile != null){
			EllipseDiagramOps.saveDiagToFile(diag, savingFinalDiagFile);
		}
		
		// getting all the detail about the run and the individual zone areas to save in log file 
		
		if (bwRunLogger != null){
			
			// details about the run
			
			switch (hcRunType){
			
				case RunWithInitParamsOnly:
				{
					hcRunDetails = fileNameOfSelectedDiagAreaSpecs + " | " + runModeStr + " | " + hillClimber.elapsedTimeString + " | " + 
								   hillClimber.elapsedTimeMilliSecs + " | " + hillClimber.elapsedIterations_includingRetries + " | " + hillClimber.fitness + " | " +reasonForTerminating + " | " +
					               //read the time variables directly from the hillclimber rather than the messageboxes because there will some delay and the time would not be the same even though the messagefields just take the value from the hillclimber variables
								   (saveDetailsWhenReachMaxRegionAreaThreshold ? (hillClimber.logStr + " | ") : "") +
					               (restrictToCircles ? (d + ":" + s) : (d + ":" + s + ":" + r)) + " | " + 
					               (restrictToCircles ? "circles" : "ellipses");
					break;
				}
				case RunWithIncParamsOnly: case RunWithInitAndIncParams:
				{
					hcRunDetails = fileNameOfSelectedDiagAreaSpecs + " | " + runModeStr + " | " + 

				               hillClimber.noOfTimesParamsAreInc + " | " + hillClimber.bestretry_index + " | " + 
					           hillClimber.elapsedTimeString_bestretry + " | " + hillClimber.elapsedTimeMilliSecs_bestretry + " | " + hillClimber.elapsedIterations_bestretry + " | " + hillClimber.fitness_bestretry + " | " +hillClimber.reasonForTermination_bestretry + " | " + 
							   (saveDetailsWhenReachMaxRegionAreaThreshold ? ((hillClimber.logStr_bestretry==""?"   |  |  |  |  |  |  |  |  |  |  |  |  | ":hillClimber.logStr_bestretry) + " | ") : "") +
				               (restrictToCircles ? (hillClimber.d_bestretry + ":" + hillClimber.s_bestretry) : (hillClimber.d_bestretry + ":" + hillClimber.s_bestretry + ":" + hillClimber.r_bestretry)) + " | " + 
							   
				               hillClimber.elapsedTimeString + " | " + hillClimber.elapsedTimeMilliSecs + " | " + hillClimber.elapsedIterations_includingRetries + " | " +   
				               
				               (restrictToCircles ? "circles" : "ellipses");
					break;
				}
			}
			
			// details about the individual zone areas of the diagram 
			
		    String[] zoneLabels = diag.getZoneLabels();
			Boolean errOccurred_za = false;
			Boolean errOccurred_zv = false;
			errMsg="";
			HashMap<String, Double> diagZoneAreas = new HashMap<String, Double> ();
			HashMap<String, Double> fitnessOfRegionsInDiag = new HashMap<String, Double> ();
			
			if (diag.zoneAreasComputedSuccessfully){
				try{
					diagZoneAreas = diag.getZoneAreas();
				} catch (Exception e){
					errOccurred_za = true;
					errMsg = "Error when obtaining zone areas (to log HC run details): ";
					System.out.println(errMsg+e);
					if (bwRunLogger != null){
						try{
							bwRunLogger.append("**" + errMsg +e); bwRunLogger.newLine(); bwRunLogger.flush();
						} catch(IOException e2) {
							System.out.println("SwitchBoardPanel.runDrawDiag: An IO exception occured -> " + e2);
						}
					}
				}
			}else {
				errOccurred_za = true;
			}
			
			if (!errOccurred_za){
				try{
					fitnessOfRegionsInDiag = diag.computeFitnessOfAllRegions(requiredAreaSpec, fitnessMeasure);
				} catch (Exception e){
					errOccurred_zv = true;
					errMsg = "Error when computing zone variances (to log HC run details): ";
					System.out.println(errMsg+e);
					if (bwRunLogger != null){
						try{
							bwRunLogger.append("**" + errMsg +e); bwRunLogger.newLine(); bwRunLogger.flush();
						} catch(IOException e2) {
							System.out.println("SwitchBoardPanel.runDrawDiag: An IO exception occured -> " + e2);
						}
					}
				}
			}
			
			Double fitnessOfDiagram = 0.0;
			if (!errOccurred_zv){
				fitnessOfDiagram = diag.computeFitnessOfDiagram(requiredAreaSpec, fitnessOfRegionsInDiag, fitnessMeasure);
			}
			hcRunAreaDetails = new String[zoneLabels.length];
			
			double currentDiagAreaTotal = 0;
			double requiredDiagAreaTotal = 0;
			double errorBasedOnAreaProportions=0;
			for(String zl : zoneLabels) {
				currentDiagAreaTotal += diagZoneAreas.get(zl);
				requiredDiagAreaTotal += requiredAreaSpec.get(zl);
			}
			
			int z=0;
			for(String zl : zoneLabels) {
				errorBasedOnAreaProportions = Math.abs((diagZoneAreas.get(zl)/currentDiagAreaTotal)-(requiredAreaSpec.get(zl)/requiredDiagAreaTotal))*100;
				
				hcRunAreaDetails[z] = fileNameOfSelectedDiagAreaSpecs + " | " + runModeStr + " | " + zl + " | " + 
									  (errOccurred_za ? "err" : diagZoneAreas.get(zl)) + " | " + requiredAreaSpec.get(zl) + " | " +  
								      (errOccurred_za ? "err" : (requiredAreaSpec.get(zl) - diagZoneAreas.get(zl))) + " | " +
								      (errOccurred_zv ? "err" : errorBasedOnAreaProportions + "% | " + 
								      (errOccurred_zv ? "err" : fitnessOfRegionsInDiag.get(zl)) + " | " + 
								      ((errOccurred_za || errOccurred_zv) ? "err" :hillClimber.fitness) + " | " +  //diagFitness) + " | " +  //hillClimber.fitness and diagFitness should give same results
						              (restrictToCircles ? "circles" : "ellipses"));
				z++;
			}
		}
		
		
		
		// put a note whether the diagram was drawn exact (fitness <= our measure for 0 ie 1e-07) or not
		message1Field.setText(reasonForTerminating.contains("Obtained desired area specification")?"Exact":"Inexact");
		message1Field.update(message1Field.getGraphics());
		
		
		// if it was animated then the panels would have been already updated 
		// if not then need to update them at this point
		if (!viewAnimation){
			int noOfZones = diag.getZoneLabels().length;
			Color fontColor = Color.BLACK;
			for (int z=0; z<noOfZones; z++){
				currAreaField[z].setForeground(fontColor);
				currAreaPCField[z].setForeground(fontColor);
				diffReqActualField[z].setForeground(fontColor);
				diffReqActualPCField[z].setForeground(fontColor);
				
				currAreaField[z].update(currAreaField[z].getGraphics());
				currAreaPCField[z].update(currAreaPCField[z].getGraphics());
				diffReqActualField[z].update(diffReqActualField[z].getGraphics());
				diffReqActualPCField[z].update(diffReqActualPCField[z].getGraphics());
			}
			updateEllipseDiagramPanel_updatedDiagram(true);
			updateAreaVarFitnessFieldsFromDiagram();
		}
		diagErrorField.setForeground(Color.BLACK);
		if (viewAnimation){
			updateAreaVarFitnessFieldsFromDiagram(null,0);
		}
	    if ((Utilities.safeParseDouble(diagErrorField.getText().trim()) == 0) && !reasonForTerminating.contains("Obtained desired area specification")){
			diagErrorField.setText(Utilities.changeDefaultDecimalSeparatorToLocale(Double.toString(
					               Utilities.roundToDps(diag.diagError(requiredAreaSpec), FITNESS_DIGITS_BEFORE_DP, 7))));
	    }
		diagErrorField.update(diagErrorField.getGraphics());

		this.ellipseDiagramPanel.updateDisplay(0,true); 
		
		if (runUserFriendlyMode){
			Toolkit.getDefaultToolkit().beep();
		}

		// Clean-Up
		runModeStr = null;		
		d = null; 
		s = null;
		r = null;
		diag = null;
	}
	
	
	
	protected void runHCButton (int mode, boolean lockedHCparams, BufferedWriter bwRunLogger, BufferedWriter bwDiagListFailedToGenInitDiag, boolean updateStopWatch) {
		runDrawDiag (yesViewSearchRadioButton.isSelected(), "Running the Hill Climber", mode, lockedHCparams, false, bwRunLogger,  bwDiagListFailedToGenInitDiag, updateStopWatch);	
	}
	
	protected void runHCButton (int mode, boolean lockedHCparams, boolean suppressFinalRunMsg, BufferedWriter bwRunLogger, BufferedWriter bwDiagListFailedToGenInitDiag, boolean updateStopWatch) {
		runDrawDiag (yesViewSearchRadioButton.isSelected(), "Running the Hill Climber", mode, lockedHCparams, suppressFinalRunMsg, bwRunLogger,  bwDiagListFailedToGenInitDiag, updateStopWatch);	
	}
	
	protected void runHCButton (int mode, boolean lockedHCparams, boolean updateStopWatch) {
		runDrawDiag (yesViewSearchRadioButton.isSelected(), "Running the Hill Climber", mode, lockedHCparams, false, null, null, updateStopWatch);	
	}
	
	protected void runHCButton (int mode, boolean lockedHCparams, boolean suppressFinalRunMsg, boolean updateStopWatch) {
		runDrawDiag (yesViewSearchRadioButton.isSelected(), "Running the Hill Climber", mode, lockedHCparams, suppressFinalRunMsg, null, null, updateStopWatch);	
	}


	
	// run all the methods for the diagram with the same hc properties
	protected void runAllHCMethods (Double[] hcParams, boolean updateStopWatch) {
		runAllHCMethods(null, null, null, hcParams, updateStopWatch);
	}
	protected void runAllHCMethods (BufferedWriter bwRunLogger, BufferedWriter bwDiagsSpecsOnRunTermLogger, BufferedWriter bwDiagListFailedToGenInitDiag, Double[] hcParams, boolean updateStopWatch) {
		
		boolean suppressFinalRunMsg = true;
	
		try{

			runHCButton (HillClimber.MULTIPLE_CHANGES_PER_ITER, false, suppressFinalRunMsg, bwRunLogger, bwDiagListFailedToGenInitDiag, updateStopWatch);
			if (bwRunLogger != null){
				bwRunLogger.append(hcRunDetails);
				bwRunLogger.newLine(); 
				bwRunLogger.flush();
			}
			if (bwDiagsSpecsOnRunTermLogger != null){
				for (String zAreaDetail : hcRunAreaDetails){
					bwDiagsSpecsOnRunTermLogger.append(zAreaDetail);
					bwDiagsSpecsOnRunTermLogger.newLine();
				}
				bwDiagsSpecsOnRunTermLogger.newLine();
				bwDiagsSpecsOnRunTermLogger.flush();
			}
			
		} catch(IOException e) {
			System.out.println("SwitchBoardPanel.runAllHCMethods: An IO exception occured -> " + e);
		}
		
	}


	
	protected void runFromCmd (String elsFilePathWithRegionAreas, String outputDirPath){
		File elsFile = new File (elsFilePathWithRegionAreas);
		ellipseDiagramPanel.diagram = null; //not sure whether this should be included because of memory but should make it run faster because it would avoid unnecessary repainting of the display 
		HashMap<String, Double> areaSpecs = EllipseDiagramOps.loadAreaSpecsFromFile_venn3(elsFile);
		if (areaSpecs==null){
			System.out.println("Error: Could not load region areas from file, "+elsFilePathWithRegionAreas+". Make sure the file format is correct.");
			return;
		}
		retrieveFileAreaSpec_update(areaSpecs, elsFilePathWithRegionAreas, false, false);

        yesViewSearchRadioButton.setSelected(false);
        logfileDirPathField.setText(outputDirPath);
        logfileFileNameField.setText(elsFile.getName().replace(".els", ""));
        
        runAllHCMethods(null, null, null, null, false);
	}
	
	
	
	public static String printAreaSpecs(HashMap<String, Double> areaSpec, String diagname, int noOfEllipses){
		String areaspecStr = ""; 
		String[] zoneLabels = EllipseDiagramOps.getZoneLabels(noOfEllipses, false);
		int z = 0;
		for (String zl : zoneLabels){
			areaspecStr += (areaSpec.get(zl)); 
			z++;
			areaspecStr += (z<zoneLabels.length) ? " | " : (((diagname!=null) && !diagname.equals("")) ? (" : " + diagname) : "");
		}
		return areaspecStr;
	}
	
	
	protected void printToConsoleAreaSpec(HashMap<String, Double> areaSpec, String diagname){
		String areaspecStr="";
		System.out.println("AreaSpecs:");
		String[] zoneLabels = EllipseDiagramOps.getZoneLabels(noOfEllipses, false);
		int z = 0;
		for (String zl : zoneLabels){
			areaspecStr += (zl + "="+ areaSpec.get(zl)); //+ " | ");
			z++;
			areaspecStr += (z<zoneLabels.length) ? " | " : (" : " + diagname);
		}	
		System.out.print(areaspecStr);
		System.out.println();
	}
	
}
