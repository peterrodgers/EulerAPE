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

/**
 * 
 * Operations on matrices
 *
 */



public class MatricesOperations {
	
	
	
	// Multiply two rxc matrices
	public static double[][] mult2Mat (double[][] matrix1, double[][] matrix2){
		
		// no check are carried out to ensure that the matrix can be multiplied (in terms of their size 
		// eg: can x 3x2 with a 2x5 but cannot x 3x2 3x2
		// also no checks are not carried out to ensure that the matrices are not empty
		
		if (matrix1[0].length != matrix2.length){ return null;} //error: columns of 1 != rows of 2 - cannot multiply such matrices
		
		int x= matrix1.length; // rows of 1
		int xy= matrix2.length; // rows of 2 -> could have had matrix1[0].length (columns of 1)
		int y = matrix2[0].length; //columns of 2
		
		double[][] result = new double[x][matrix2[0].length];
		
		// based on http://www.roseindia.net/java/beginners/MatrixMultiply.shtml
		// and http://www.ee.ucl.ac.uk/~mflanaga/java/MatrixExample2.java
		
		int i; int j; int k;
		for(i = 0; i < x; i++) {
			for(j = 0; j < y; j++) {
				for(k = 0; k < xy; k++){
					result[i][j] += (matrix1[i][k] * matrix2[k][j]);
		        }
		    }  
		}
		
		return result;
	}

}

