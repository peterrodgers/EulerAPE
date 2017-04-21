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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import uk.ac.kent.elliptic.areaproptool.SwitchBoardPanel.Curves;

import jargs.gnu.CmdLineParser;  // this command-line parser by Steve Purcell (2005) is available at http://jargs.sourceforge.net


/**
 * Starts eulerAPE
 * 
 */


public class APEllipseAppletForJar  extends JFrame implements ActionListener {
	
	public static Dimension FRAMEDIMENSION = new Dimension(1250, 780);
	
	protected int width;
	protected int height;
	
	protected EllipseDiagramPanel ellipseDiagramPanel;
	protected SwitchBoardPanel switchBoardPanel;
	
	
	public APEllipseAppletForJar(EllipseDiagramPanel ellipseDiagramPanel, SwitchBoardPanel switchBoardPanel, boolean isVisible){
		
		super("eulerAPE: Drawing Area-Proportional Euler and Venn Diagrams using Ellipses");
		
		try {
	        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	    } catch(Exception e) {
	    	System.out.println("APEllipseAppletForJar.APEllipseAppletForJar: Error setting CrossPlatform LookAndFeel: " + e);
	    }
		
		this.ellipseDiagramPanel = ellipseDiagramPanel;
		this.switchBoardPanel = switchBoardPanel;
		
		BufferedImage icon=null;
		try {
			icon = ImageIO.read(getClass().getResourceAsStream("/uk/ac/kent/elliptic/areaproptool/eulerAPE.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		setIconImage(icon);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setSize(FRAMEDIMENSION);
		
		setResizable(false);
		
		setLocationRelativeTo(getRootPane()); //centre the window to screen
		
		setVisible(isVisible);
		
		
		// Check screen resolution first 
		
		// Get the default toolkit
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		// Get the current screen size
		Dimension scrnsize = toolkit.getScreenSize();	
	}
	
	
	public static APEllipseAppletForJar startEulerAPEWin(boolean isVisible){
		
		// Main frame panel
		// ... all the components in the frame are placed in this panel
		// ... this will ensure proper border padding from the edge of the window, when frame.pack() is invoked
		// ... frame.pack() is required for inner panels to be visible on Mac 
		JPanel mainFramePanel = new JPanel();
		mainFramePanel.setSize(FRAMEDIMENSION);
		mainFramePanel.setBorder(BorderFactory.createEmptyBorder(10,15,10,5));
		
		
		// Inner panels -> EllipseDiagramPanel and SwitchBoardPanel
		EllipseDiagramPanel ellipseDiagramPanel = new EllipseDiagramPanel(null,null);
		Border emptyBorder = BorderFactory.createEmptyBorder(1,16,0,0);
		SwitchBoardPanel switchBoardPanel = new SwitchBoardPanel(ellipseDiagramPanel, EllipseDiagramPanel.LABELS_SIMPLE);
		switchBoardPanel.setOpaque(false);
		switchBoardPanel.setBorder(emptyBorder);
	
		
		
		// Load actual application
		
		APEllipseAppletForJar apEllipsesFrame= new APEllipseAppletForJar(ellipseDiagramPanel, switchBoardPanel, isVisible);
		
		GridBagLayout gridbag = new GridBagLayout();
		apEllipsesFrame.setLayout(gridbag);
		
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTH;
		
		c.gridx = 0;
		c.gridy = 0;
		gridbag.setConstraints(ellipseDiagramPanel,c);
		mainFramePanel.add(ellipseDiagramPanel);
		
		c.gridx = 1;
		c.gridy = 0;
		gridbag.setConstraints(switchBoardPanel,c);
		mainFramePanel.add(switchBoardPanel);

		apEllipsesFrame.add(mainFramePanel);
		apEllipsesFrame.pack();
		mainFramePanel.setBackground(Color.white);
		
		switchBoardPanel.requestFocus();
		
		return apEllipsesFrame;
	}

	public static final String cmdlineOptions = "[{-i,--input} an_els_file_path] [{-o,--output} a_dir_path] [{-l, --showlabels} yes_or_no] [{-c, --showincolour} yes_or_no] [{--curves} ellipses_or_circles] [{-s, --silent}]";	
	private static void printUsage() {
        System.err.println("Usage: Options "+cmdlineOptions);    
    }

	public static void main(String[] args) {

		boolean runningInCmd = false;
		String elsFilePathWithRegionAreas = "";
		String outputDiagDirPath = "";
		boolean showLabels = true;
		boolean showInColour = true;
		Curves curveType = Curves.ELLIPSES; 
		
		// the command-line parser, jargs.gnu.CmdLineParser, by Steve Purcell is available at http://jargs.sourceforge.net
		
		// configure optional command-line arguments
		// ... if no arguments, load window and allow user to run eulerAPE from GUI
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option parserInputElsFilePath = parser.addStringOption('i', "input");
		CmdLineParser.Option parserOutputEldDirPath = parser.addStringOption('o', "output");
		CmdLineParser.Option parserShowLabels = parser.addStringOption('l', "showlabels");
		CmdLineParser.Option parserShowInColour = parser.addStringOption('c', "showincolour");
		CmdLineParser.Option parserCurves = parser.addStringOption("curves");
		CmdLineParser.Option parserSilent = parser.addBooleanOption('s', "silent");
		
		// retrieve args
		try {
			for (int a=0; a<args.length; a++){
				if (args[a].trim().equals("-curves")||args[a].trim().equals("-curve")||args[a].trim().equals("--curve")){
					System.out.println();
					System.err.println("Error in command-line arguments: illegal option '"+args[a]+"'; the option which you might need is '--curves' followed by 'circles' or 'ellipses' (without the quotes)");
	        		System.out.println();
	        		printUsage();
	        		System.exit(2);	
				}
			}
			parser.parse(args);
        }
        catch (CmdLineParser.OptionException e ) {
        	System.out.println();
            System.err.println("Error in command-line arguments: "+e.getMessage());
            System.out.println();
            printUsage();
            System.exit(2);
        }
        
        // ... retrieve individual args
        String parserInputElseFilePathValue = (String)parser.getOptionValue(parserInputElsFilePath);
        String parserOutputEldDirPathValue = (String)parser.getOptionValue(parserOutputEldDirPath);
        
        String parserShowLabelsValue = (String)parser.getOptionValue(parserShowLabels, "yes");
        String parserShowInColourValue = (String)parser.getOptionValue(parserShowInColour, "yes");
        String parserCurvesValue = (String)parser.getOptionValue(parserCurves, "ellipses");
       Boolean parserSilentValue = (Boolean)parser.getOptionValue(parserSilent, Boolean.FALSE);
        String[] parserOtherArgs = parser.getRemainingArgs();
        
        // check input 
    	File inputElsFile = null;
    	File outputDiagDir = null;
        if ((parserInputElseFilePathValue==null)&&((parserOtherArgs==null)||(parserOtherArgs.length==0))){
        	runningInCmd=false;
        } else {
        	runningInCmd=true;
        	
        	System.out.println();
        	
        	if ((parserOtherArgs!=null)&&(parserOtherArgs.length>0)){
        		System.err.println("Error in command-line arguments: invalid usage of command-line arguments.");
        		System.out.println();
        		printUsage();
        		System.exit(2);	
        	}
        	
        	if (parserInputElseFilePathValue==null){
        		System.err.println("Error in command-line arguments: to run eulerAPE from the command-line, use -i (or --input) followed by the .els file with the region areas for which a diagram should be drawn. Other options can be omitted.");
        		System.out.println();
        		printUsage();
        		System.exit(2);	
        	}
    		
    	
        	// same checks as those in SwitchBoardPanel.loadFromFileAreaSpecsButton except that the error message is displayed in the command-line rather than having an error message dialog
    	    inputElsFile = new File(parserInputElseFilePathValue);
    	    if (inputElsFile.exists() && !inputElsFile.isFile()){
    			System.err.println("Error in command-line arguments: " + parserInputElseFilePathValue + " is a directory (not a file); -i (or --input) must be followed by the path to a .els file.");
        		System.exit(2);   	    	
    	    }
    	    if (!inputElsFile.getAbsoluteFile().toString().endsWith(".els")){
    			System.err.println("Error in command-line arguments: incorrect value for -i (or --input). The path to a .els file is expected.");
        		System.exit(2);
    		}
    		if (!inputElsFile.exists()){ 
    			System.err.println("Error in command-line arguments: "+ parserInputElseFilePathValue + " for -i (or --input) does not exist.");
    			System.exit(2);
    		}
    		elsFilePathWithRegionAreas = parserInputElseFilePathValue;
    		
    		
    	    // checks for output dir
        	if (parserOutputEldDirPathValue == null){
        		outputDiagDirPath = Utilities.getCurrentWorkingDirPath();
        		outputDiagDir = new File(outputDiagDirPath); 
       
        	} else {
        		outputDiagDir = new File(parserOutputEldDirPathValue);
        	    if (outputDiagDir.exists() && !outputDiagDir.isDirectory()){
        			System.err.println("Error in command-line arguments: " + parserOutputEldDirPathValue + " is not a directory; -o (or --output) must be followed by the path to a directory.");
            		System.exit(2);   	    	
        	    }
        		if (!outputDiagDir.exists()){
        			System.err.println("Error in command-line arguments: "+ parserOutputEldDirPathValue + " for -o (or --output) does not exist.");
        			System.exit(2);			
        		}
        	    outputDiagDirPath = parserOutputEldDirPathValue;
        	}
    	    
    	    
        	// parse showlabel option 
    	    String parserShowLabelsValueFormatted = parserShowLabelsValue.trim().toLowerCase();
    	    if (parserShowLabelsValueFormatted.equals("yes")||parserShowLabelsValueFormatted.equals("true")){
    	    	showLabels = true;
    	    } else if (parserShowLabelsValueFormatted.equals("no")||parserShowLabelsValueFormatted.equals("false")){ 
    	    	showLabels = false;
    	    } else{
    			System.err.println("Error in command-line arguments: incorrect value for option -l (or --showlabels); yes or no is expected (e.g., -l yes). When -l (or --showlabels) is missing, the generated diagram is labelled.");
    			System.exit(2);	
    	    }
        	
        	// parse showincolour option 
    	    String parserShowInColourValueFormatted = parserShowInColourValue.trim().toLowerCase();
    	    if (parserShowInColourValueFormatted.equals("yes")||parserShowInColourValueFormatted.equals("true")){
    	    	showInColour = true;
    	    } else if (parserShowInColourValueFormatted.equals("no")||parserShowInColourValueFormatted.equals("false")){ 
    	    	showInColour = false;
    	    } else{
    			System.err.println("Error in command-line arguments: incorrect value for option -c (or --showincolour); yes or no is expected (e.g., -c yes). When -c (or --showincolour) is missing, the generated diagram is coloured.");
    			System.exit(2);	
    	    }
        	
        	// parse curve type 
    	    String parserCurveValueFormatted = parserCurvesValue.trim().toLowerCase();
    	    if (parserCurveValueFormatted.equals("ellipse")||parserCurveValueFormatted.equals("ellipses")){
    	    	curveType = Curves.ELLIPSES;
    	    } else if (parserCurveValueFormatted.equals("circle")||parserCurveValueFormatted.equals("circles")){ 
    	    	curveType = Curves.CIRCLES;
    	    } else{
    			System.err.println("Error in command-line arguments: incorrect value for option --curves; ellipses or circles is expected (e.g., --curves ellipses). When --curves is missing, the curves of the generated diagram will be ellipses.");
    			System.exit(2);	
    	    }
    	    
        	
    	    // if !silent show that eulerAPE is running
            if (!parserSilentValue){
            	System.out.println("eulerAPE is generating the diagram for region areas " + parserInputElseFilePathValue + " ...");
            }
        }
        
		
        
        // start eulerAPE
        // ... this is the only line of code which is required when not using the command-line
		APEllipseAppletForJar eulerAPEwin = startEulerAPEWin(!runningInCmd);
		
		
		// if running in cmd
		if (runningInCmd){

        	// same check as in SwitchBoardPanel.loadFromFileAreaSpecsButton except that the error message is displayed in the command-line rather than having an error message dialog
    		// ... try to load the area specs here to ensure no errors when they are loaded during the actual drawing process
    		boolean areaSpecsLoadedFromFileSuccessfully = eulerAPEwin.switchBoardPanel.retrieveFileAreaSpec(inputElsFile, false, false);

    		if (!areaSpecsLoadedFromFileSuccessfully){
    			if (inputElsFile.getAbsoluteFile().toString().endsWith(".els")){
    				String localeDecSep = Character.toString(Utilities.getLocaleDecimalSeparator());
    				
    				
    				System.err.println("\nError when loading required region areas from file: Failed to load the region areas in " + parserInputElseFilePathValue.toString()+
                            "\n\n Check:"+
                            "\n\n 1) The structure of the file" +
                              "\n    a) lines with comments must start with // "+
                              "\n    b) the region areas are defined in one line as follows"+
                              "\n         a | b | c | ab | ac | bc | abc"+
                              "\n       example"+
                              "\n         35754"+localeDecSep+"05 | 19659"+localeDecSep+"1 | 25875"+localeDecSep+"2 | 31804"+localeDecSep+"0 | 12767"+localeDecSep+"7 | 6146"+localeDecSep+"55 | 10660"+localeDecSep+"6          "+
                            "\n\n    An example of a valid .els file is available at http://www.eulerdiagrams.org/eulerAPE/areas_example.php"+
                            "\n\n 2) The decimal separator corresponds to your locale"+
                            "\n\n    Your current locale (LanguageCode_CountryCode):   " + Locale.getDefault() +
                              "\n    Expected decimal separator:   " + Utilities.getLocaleDecimalSeparator() +
                            (runningInCmd?(
                            "\n\n    If you would like to specify a Locale with " +
                              "\n     - xx as the ISO 639 alpha-2 (or ISO 639-1) language code " +
                              "\n          (e.g., it for Italian; codes at " +
                              "\n           http://www.loc.gov/standards/iso639-2/php/code_list.php)," +
                              "\n     - XX as the ISO 3166 alpha-2 (or ISO 3166-1) country code  " +
                              "\n          (e.g., IT for Italy; codes at " +
                              "\n           http://www.iso.org/iso/home/standards/country_codes/iso-3166-1_decoding_table.htm)," +
                            "\n\n    run eulerAPE from the command-line by typing "+
                            "\n\n       java -Duser.language=xx -Duser.region=XX -jar eulerAPE_2.0.3.jar"+
                            "\n\n    followed by the required options" +
                            "\n\n       "+ cmdlineOptions + "\n"
                             ):"")
                     

                             // set Locale from command-line (see http://download.java.net/jdk7/archive/b123/docs/api/java/util/Locale.html on how to set the Locale from the command-line)
                             // language - An ISO 639 alpha-2 or alpha-3 language code, or a language subtag up to 8 characters in length. See the Locale class description about valid language values.
                             // country - An ISO 3166 alpha-2 country code or a UN M.49 numeric-3 area code. See the Locale class description about valid country values.
    				);
    			}
    			System.exit(2);
    		}

			
    		// run the hill climber without gui and user intervention 
			eulerAPEwin.switchBoardPanel.saveFinalDiagOnly = true;
			
			eulerAPEwin.switchBoardPanel.updateShowLabels(showLabels);
			eulerAPEwin.switchBoardPanel.updateShowInColour(showInColour);
			eulerAPEwin.switchBoardPanel.updateCurveType(curveType);
			
			eulerAPEwin.switchBoardPanel.runFromCmd(elsFilePathWithRegionAreas, outputDiagDirPath);
			eulerAPEwin.dispose();
			
		
			if (!parserSilentValue){
        		try {
        			
        			double diagError = eulerAPEwin.switchBoardPanel.ellipseDiagramPanel.diagram.diagError(eulerAPEwin.switchBoardPanel.requiredAreaSpec);
        			
					System.out.println( "\n" +
										"eulerAPE generated an " +
							   			(eulerAPEwin.switchBoardPanel.message1Field.getText().trim().equals("Exact") ? "exact" : "inexact") + 
							   			 " diagram (diagError=" + diagError + "): files " +
									     inputElsFile.getName().replace(".els", ".eld") + " and " + inputElsFile.getName().replace(".els", ".png") + " are in " + ((outputDiagDir==null)?"":outputDiagDir.getCanonicalPath().toString())
									     );
									  
				} catch (IOException e) {
					e.printStackTrace();
				}
        	} 
		}	
	}

	@Override
	public void actionPerformed(ActionEvent e) {		
	}
}
