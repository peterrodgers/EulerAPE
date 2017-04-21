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
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;


/**
 * 
 * General Utilities
 *
 */


public class Utilities {

	
	// Conversions and Parsing
	
	public static Double safeParseDouble(String doubleString) {
		// decimal mark (based on the system locale) independent
		double ret = 0.0;
		try {
			ret = Double.parseDouble(doubleString);
		} catch(Exception e) {
			return null;
		}
		return ret;
		
	}

	public static Point2D.Double convertToPoint2D_Double (Point pnt){ 
		return (new Point2D.Double (new Double(pnt.x), new Double(pnt.y)));
	}
	
	public static double[] convertDblArrayListToArray(ArrayList<Double> ds_arraylist){
		double[] ds_array = new double[ds_arraylist.size()];
		int i = 0;
		for (Double d : ds_arraylist){
			ds_array[i] = d;
			i++;
		}
		return ds_array;
	}
	
	// ... converting a char/string(first char in the string) to ASCII int and vice-versa 
	public static int convertCharToASCIIint (char c){
		return (c);
	}	
	public static int convertCharToASCIIint (String s){
		// assuming that the string is a one-character string or else the ASCII value of the first character of the string is required
		return (convertCharToASCIIint(s.charAt(0)));
	}
	public static char convertIntASCIItoChar (int i){
		return ((char)i);
	}
	public static String convertIntASCIItoString (int i){
		return (Character.toString(convertIntASCIItoChar(i)));
	}
	
	
	// ... converting between coordinate systems
	public static Point2D.Double changeCoorSys_originMid_to_originTopLeft(Point2D.Double pnt, Point2D.Double centreOfSystem){
		return new Point2D.Double( new Double(pnt.x + centreOfSystem.x), new Double( -pnt.y + centreOfSystem.y ));
	}
	
	public static Point2D.Double changeCoorSys_originTopLeft_to_originMid(Point2D.Double pnt, Point2D.Double centreOfSystem){
		return new Point2D.Double( new Double(pnt.x - centreOfSystem.x), new Double( -pnt.y + centreOfSystem.y ));
	}
	
	public static Point2D.Double changeCoorSys_originTopLeft_to_originBottomLeft(Point2D.Double pnt, double boxHeight){
		return new Point2D.Double( new Double(pnt.x), new Double(boxHeight - pnt.y));
	}
	
	public static Point2D.Double changeCoorSys_originBottomLeft_to_originTopLeft(Point2D.Double pnt, double boxHeight){
		return new Point2D.Double( new Double(pnt.x), new Double(boxHeight - pnt.y));
	}
	
	
	
	// ... removing leading and trailing zeros in numeric string 
	
	public static String removeLeadingZeros(String numericStr){
		boolean isDecimalNumber = false;
		boolean isZero = false;
		
		// check if the string is a decimal number numeric and if so, leave one trailing zero just before decimal separator
		char localeDecimalSep = Utilities.getLocaleDecimalSeparator();
		Double parsedDbl = safeParseDouble(changeLocaleDecimalSeparatorToDefault(numericStr));
		if (parsedDbl != null){ //it is a numeric string
			if (parsedDbl==0){ //if it is a 0 then turn
				isZero=true;
			}
			if (numericStr.indexOf(localeDecimalSep)>-1){ //it is a decimal no
				isDecimalNumber=true;
			} 
		}
		
		// remove leading zeros
		if (numericStr == null){return null;}
		char[] chars = numericStr.toCharArray();
		int index=0;
		for (; index < numericStr.length(); index++){
			if (chars[index] != '0'){break;}
		}
		if (index==0){
			return numericStr;
		} else if ((index == numericStr.length()-1)&& isZero){
			return "0";
		} else if (isDecimalNumber){
			return "0" + numericStr.substring(index);
		} else {
			return numericStr.substring(index);
		}
	}
	
	public static String removeTrailingZeros(String numericStr){
		// check that, if the string is really numeric, then it is a decimal number before remove trailing zeros
		// ... if it is not a decimal number then trailing 0s are significant and shouldn't be removed
		char localeDecimalSep = Utilities.getLocaleDecimalSeparator();
		if (safeParseDouble(changeLocaleDecimalSeparatorToDefault(numericStr))!=null){ //then it is really a numeric string and we must ensure that it is a decimal number before removing trailing 0s 
			if (numericStr.indexOf(localeDecimalSep)==-1){return numericStr;} //return if it is not a decimal no (ie it does not have a locale decimal separator)
		}
		// remove trailing zeros
		if (numericStr == null){return null;}
		char[] chars = numericStr.toCharArray();
		int length = numericStr.length();
		int index = length-1;
		for (; index >=0; index--){
			if (chars[index] != '0'){break;}
		}
		return (index == length-1) ? numericStr :numericStr.substring(0,index+1);
	}
	
	public static String removeLeadingAndTrailingZeros(String numericStr){
		return removeTrailingZeros(removeLeadingAndTrailingZeros(numericStr));
	}

	
	// Locale Number Formatting 
	
	public static char getLocaleDecimalSeparator(){
		Locale systemLocale = Locale.getDefault();
		NumberFormat nf = NumberFormat.getInstance(systemLocale);
        DecimalFormat df = (DecimalFormat)nf;
        return df.getDecimalFormatSymbols().getDecimalSeparator();
	}
	
	public static char defaultDecimalSeparator = '.';
	
	public static String changeLocaleDecimalSeparatorToDefault(String valueAsStr){
		return changeLocaleDecimalSeparatorTo(valueAsStr, defaultDecimalSeparator);
	}
	public static String changeLocaleDecimalSeparatorTo(String valueAsStr, char decSepToChangeTo){
		// no checks are carried out to ensure that the string is a number -> must be done elsewhere
        char localeDecimalSep = getLocaleDecimalSeparator();
        return (valueAsStr.replace(localeDecimalSep, decSepToChangeTo));
	}
	public static String changeDefaultDecimalSeparatorToLocale(String valueAsStr){
		// no checks are carried out to ensure that the string is a number -> must be done elsewhere
		char localeDecimalSep = getLocaleDecimalSeparator();
        return (valueAsStr.replace(defaultDecimalSeparator, localeDecimalSep));
	}
	
	
	// Rounding
	
	public static int computeNumberOfDigitsBeforeDP (double value){
		String valueAsStr = Double.toString(value);
		int indexOfDP = valueAsStr.indexOf(".");
		String digitsBeforeDPAsStr;
		if (indexOfDP > -1){
			digitsBeforeDPAsStr = valueAsStr.substring(0,indexOfDP);
		} else {
			digitsBeforeDPAsStr = valueAsStr;
		}
		return digitsBeforeDPAsStr.length();
	}
	
	public static int computeNumberOfDigitsAfterDP (double value){
		String valueAsStr = Double.toString(value);
		int indexOfDP = valueAsStr.indexOf(".");
		String digitsAfterDPAsStr;
		if (indexOfDP > -1){
			digitsAfterDPAsStr = valueAsStr.substring(indexOfDP+1);
		} else {
			digitsAfterDPAsStr = "";
		}
		return digitsAfterDPAsStr.length();
	}
	
	
	public static Double roundToDps (double value, double valueFromWhichToDetermineNoOfDigitsAfterDP){
		
		int noOfDigitsAfterDP = computeNumberOfDigitsAfterDP(valueFromWhichToDetermineNoOfDigitsAfterDP);  
		
		return roundToDps(value, noOfDigitsAfterDP);
	}
	
	public static Double roundToDps (double value, int noOfDigitsAfterDP){
		
		int noOfDigitsBeforeDP = computeNumberOfDigitsBeforeDP(value)+2;   //+2 to ensure that no rounding occurs for digits before dp
		
		return roundToDps(value, noOfDigitsBeforeDP, noOfDigitsAfterDP);
	}
	
	public static Double roundToDps (double value, int noOfDigitsBeforeDP, int noOfDigitsAfterDP){
		
		Locale systemLocale = Locale.getDefault();
		NumberFormat nf = NumberFormat.getNumberInstance(systemLocale);
		DecimalFormat df = (DecimalFormat)nf;
		
        String formatStrPattern = "";
		for (int i=0; i < noOfDigitsBeforeDP; i++){ formatStrPattern += "#";}
		formatStrPattern += ".";
		for (int i=0; i < noOfDigitsAfterDP; i++){  formatStrPattern += "#";}
		
		df.applyPattern(formatStrPattern);
		String formattedValueAsStr = df.format(value);
		
		NumberFormat format = NumberFormat.getInstance(systemLocale);
	    try {
			return format.parse(formattedValueAsStr).doubleValue();
		} catch (ParseException e) {
			System.out.println("Parsing error while converting formatted decimal value "+ formattedValueAsStr +" to number (Utilities.roundToDps): "+e);
			return null;
		}
	}
	
	
	// Combinatorial Functions
	public static Double[][] generateNumPermSeq (Double[] numL){
		if ((numL == null) || (numL.length == 0)){
			return null;
		}
		
		int noOfNumPermSeq = (int)Math.pow(numL.length, numL.length);
		Double[][] numPermSeqL = new Double[noOfNumPermSeq][numL.length];
		int seqCount = 0;
		for (Double num2 : numL){
			for (Double num1 : numL){
				for (Double num0 : numL){
					numPermSeqL[seqCount][0]=num0;
					numPermSeqL[seqCount][1]=num1;
					numPermSeqL[seqCount][2]=num2;
					seqCount++;
				}
			}
		}
		return numPermSeqL;
	}
	
	public static int noOfCombinations (int n, int r){
		return (factorial(n) / (factorial(n-r)*factorial(r)));
	}
	
	public static Integer factorial(int value) {
		// iterative version 
		if (value < 0){ return null;}
	
		if ((value == 0) || (value == 1)){
			return 1;
		} else {
			int result = value;
			for (int i = (value-1); i > 1; i--){
				result *= i;
			}
			return result;
		}
	}	
	
	
	
	// Time and Date
	
	public static String getCurrentDateTime(){
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
	    Date date = new Date();
	    return(dateFormat.format(date));
	}


	
	// Colours 
	
	public static Color getColorFromChar(char c, Color colors[]) {
		int i = (int)c - (int)'a';
		if(i < 0) {i = 0-i;} // capitals give a negative i
		
		int colorInt = i % colors.length;
	    Color color = colors[colorInt];
		while(i >= colors.length) {
			color.brighter();
			i = i - colors.length;
		}
		return color;
	}
		
	public static Color mixColors(Color c1, Color c2) {
		int r1 = c1.getRed();
		int g1 = c1.getGreen();
		int b1 = c1.getBlue();

		int r2 = c2.getRed();
		int g2 = c2.getGreen();
		int b2 = c2.getBlue();

		Color ret = new Color(r1^r2, g1^g2, b1^b2);
		return ret;
	}	
	
	public static Color getRandomColor() {
		Random random = new Random();
		return new Color(random.nextInt(255),random.nextInt(255),random.nextInt(255));
	}
	
	
	
	// Random Operators

	/** Generate random integers in a certain range. 
	 *    the idea and most of the code was obtained from http://www.javapractices.com/topic/TopicAction.do?Id=62*/
	public static Integer randomIntegerInRange (int range_firstInt, int range_lastInt){
		
		// check the given range
	    if (range_firstInt > range_lastInt) {
		      System.out.println("Utilities.randomIntegerInRange:  inappropriate range - the first integer in the range is greater than the last one");
		      return null;
	    }
	    
	    Random random = new Random();
	    
	    //get the range, casting to long to avoid overflow problems
	    long range = (long)range_lastInt - (long)range_firstInt + 1;
	    
	    // compute a fraction of the range, 0 <= frac < range
	    long fraction = (long)(range * random.nextDouble());
	    int randomInt =  (int)(fraction + range_firstInt);  
	    
	    return (new Integer (randomInt));
	}
	
	public static Double randomNumberInRange (double range_first, double range_last, Random random){
		
		// check the given range
	    if (range_first > range_last) {
		      System.out.println("Utilities.randomNoInRange:  inappropriate range - the first number in the range is greater than the last one");
		      return null;
	    }
	    
	    double range = range_last - range_first + 1;
	    
	    // compute a fraction of the range, 0 <= frac < range
	    double fraction = (range * random.nextDouble());
	    double randomNo =  (fraction + range_first);  
	    
	    return (new Double (randomNo));
	}
	
	public static Double randomNumberInRange_forValuesBetween0and1 (double range_first, double range_last){
		
		// same as above but cannot plus 1 for range
		
		// check the given range
	    if (range_first > range_last) {
		      System.out.println("Utilities.randomNoInRange:  inappropriate range - the first number in the range is greater than the last one");
		      return null;
	    }
	    
	    Random random = new Random();
	    double range = range_last - range_first;
	    
	    // compute a fraction of the range, 0 <= frac < range
	    double fraction = (range * random.nextDouble());
	    double randomNo =  (fraction + range_first);  
	    
	    return (new Double (randomNo));
	}
	
	
	
	public static boolean createDir (String dirPath){
		
		boolean success;
		int index=-1;
		ArrayList<String> dirNames = new ArrayList<String>();
		do{
			index = dirPath.indexOf(File.separator);
			if (index > -1){
				dirNames.add(dirPath.substring(0,index));
				dirPath = dirPath.substring(index+1);
			} else {
				if (dirPath.trim().length() > 0){
					dirNames.add(dirPath);
				}
			}
		} while (index > -1);
		
		File rootDir = File.listRoots()[0];
        if (rootDir.toString().startsWith(dirNames.get(0))){  
        	dirNames.remove(0);
        }
		
		File dirsPathFile = rootDir; 
		for (int i=0; i<dirNames.size(); i++){
			dirsPathFile = new File(dirsPathFile, dirNames.get(i));
		}

		try{
			// Create all directories (if multiple directories are required according to dirPath)
			success = dirsPathFile.mkdirs();
		}catch (Exception e){ //Catch exception if any
			System.err.println("createDir: " + e.getMessage());
			return false;
		}
		return success;
	}
	
	public static boolean copyFileToAnotherDir (String fileName, String srcDir, String destDir){
		return copyFileToAnotherDir(fileName, fileName, srcDir, destDir);
	}
	
	public static boolean copyFileToAnotherDir (String srcFileName, String destFileName, String srcDir, String destDir){
		// Copy the source file to target file.
	    // In case the dst file does not exist, it is created
		
		try{
			InputStream in = new FileInputStream(new File(srcDir+File.separator+srcFileName));
			OutputStream out = new FileOutputStream(new File(destDir+File.separator+destFileName));
	
			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
	
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
	
			in.close();
			out.close();
			
		} catch (IOException e){
			System.err.println("copyFileToAnotherDir: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	
	public static String getCurrentWorkingDirPath (){
	    File cwd = new File(".");
		try {
			return cwd.getCanonicalPath();
		} catch (IOException e) {
			System.out.println("Utilities.getCurrentWorkingDirPath: "+e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getParentDirPathOfCurrentWorkingDir (){
	    File pd = new File("..");
		try {
			return pd.getCanonicalPath();
		} catch (IOException e) {
			System.out.println("Utilities.getParentDirPathOfCurrentWorkingDir: "+e.getMessage());
			e.printStackTrace();
		}
		return null;
	}  
	

	public static File[] getLogFilesInDir (String dirPath){
		File dir = new File(dirPath);
		if (!dir.exists()){return null;}
		
		FilenameFilter filesFound = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".log");
		}};
			
		return dir.listFiles(filesFound);
	}
	
	public static File[] getTextFilesInDir (String dirPath){
		File dir = new File(dirPath);
		if (!dir.exists()){return null;}
		
		FilenameFilter filesFound = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".txt");
		}};
			
		return dir.listFiles(filesFound);
	}
	
	
	
	
	// Array Operators
	
	public static <T> T[] concatArrays (T[] array1, T[] array2) {
		
		if ((array1 == null) || (array1.length <= 0)){return array2;}
		if ((array2 == null) || (array2.length <= 0)){return array1;}
		
		final T[] array3 = (T[]) java.lang.reflect.Array.newInstance(array1.getClass().getComponentType(), array1.length + array2.length);
		
		System.arraycopy(array1, 0, array3, 0, array1.length);
		System.arraycopy(array2, 0, array3, array1.length, array2.length);

		return array3;
	}
	
	public static boolean arrayContainsStringValue (ArrayList<String> array, String valueToCheck){
		
		for (String stringInArray : array){
			if (stringInArray.equals(valueToCheck)){
				return true;
			}
		}
		return false;
	}
	
	
	// HashMaps Operators
	
	//public static HashMap<Object, Object> sortHashMapByKeys (HashMap<Object, Object> unsortedHashMap){
	//   could not use the above generic types because of problems with HashMap type casts when invoking this method
	public static HashMap<String, Double> sortHashMapByKeys(HashMap<String, Double> unsortedHashMap){
		HashMap<String, Double> sortedHashMap = new HashMap<String, Double>();
		
		Object[] keys = unsortedHashMap.keySet().toArray();
		Arrays.sort(keys);
		
		Double value;
		for (Object key : keys){
			value = unsortedHashMap.get(key);
			sortedHashMap.put((String)key,value);
		}
		return sortedHashMap;
	}
	
	public static HashMap sortHashMapByValue(HashMap map) {
	// Obtained this from http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
	     List list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	              .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });

	     HashMap result = new LinkedHashMap();
	     for (Iterator it = list.iterator(); it.hasNext();) {
	    	 Map.Entry entry = (Map.Entry)it.next();
	    	 result.put(entry.getKey(), entry.getValue());
	     }
	     return result;
	}
	
	
	// Clone and copy
	public static HashMap<String, Double> cloneHashMap(HashMap<String, Double> hashmapOrig){
		
		if (hashmapOrig==null){return null;}

		HashMap<String, Double> hashmapClone = new HashMap<String, Double>();
			
		Iterator itr = hashmapOrig.entrySet().iterator();
		Entry<String, Double> za;
		while (itr.hasNext()){
			za = (Entry<String, Double>)itr.next();
			hashmapClone.put(new String((String)za.getKey()), new Double(((Double)za.getValue()).doubleValue()));
		}
			
		return hashmapClone;
	}
	
	public static HashMap<String, Double> copyHashMap(HashMap<String, Double> hashmapOrig, HashMap<String, Double> hashmapCopy){
		
		Iterator itr = hashmapOrig.entrySet().iterator();
		Entry<String, Double> zv;
		while (itr.hasNext()){
			zv = (Entry<String, Double>)itr.next();
			hashmapCopy.put((String)zv.getKey(), ((Double)zv.getValue()).doubleValue());
		}
		
		return hashmapCopy;
	}
 
}