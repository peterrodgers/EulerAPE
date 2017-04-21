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


/**
 * 
 * StopWatch 
 * 
 * 
 * This is a modified version of Goldberg's 2005 StopWatch.java licensed as
 *  
 * 		Copyright (c) 2005, Corey Goldberg
	
 *	    StopWatch.java is free software; you can redistribute it and/or modify
 *	    it under the terms of the GNU General Public License as published by
 *	    the Free Software Foundation; either version 2 of the License, or
 *	    (at your option) any later version.
 *   
 * available at http://www.goldb.org/stopwatchjava.html
 *
 *
 */


public class StopWatch {
    
	public static final int MILLISECS_IN_1SEC = 1000;
	public static final int SECS_IN_1MIN = 60;
	public static final int MINS_IN_1HR = 60;
	
	public static final int MILLISECS_IN_1MIN = MILLISECS_IN_1SEC * SECS_IN_1MIN;
	public static final int SECS_IN_1HR = SECS_IN_1MIN * MINS_IN_1HR;
	
	private long startTime = 0;   // all calculations (eg elapsed time) are carried out using the resumeTime (NOT the startTime)
    private long resumeTime = 0;  // if the watch was not paused, then resumeTime = startTime, else resumeTime = time when the watch was resumed  
    private long stopTime = 0;
    private double elapsedTimeInMilliSecsBeforeLatterPause = 0; 
    private int pauseCount = 0;
    private boolean running = false;  // if the watch is paused, running=false
    
   
    
    public void start() {
    	this.reset();
        this.startTime = System.currentTimeMillis();
        this.resumeTime = this.startTime;
        this.running = true;
    }
    
    public void pause(){
    	stop();
    	elapsedTimeInMilliSecsBeforeLatterPause = getElapsedTimeMilliSecs();
    	pauseCount++;
    }
    
    public void resume(){
        this.resumeTime = System.currentTimeMillis();
        this.running = true;
    }

    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
    }
    
    public void reset() {
    	this.startTime = 0;
    	this.resumeTime = 0;
        this.stopTime = 0;
        this.elapsedTimeInMilliSecsBeforeLatterPause = 0;
        this.pauseCount = 0;
        this.running = false;
    }

    
    //elapsed time in mins
    public double getElapsedTimeMins() {
        return (getElapsedTimeMilliSecs()/MILLISECS_IN_1MIN);
    }
    
    //elapsed time in seconds
    public double getElapsedTimeSecs() {
    	return (getElapsedTimeMilliSecs()/MILLISECS_IN_1SEC);
    }

    
    //elapsed time in milliseconds
    //public long getElapsedTimeMilliSecs() {
    //... must return a double or else when divide by a factor to convert to eg. mins or secs, 
    //... the value would be rounded up to an integer, even though the final method returns a double
    public double getElapsedTimeMilliSecs() {	
        long elapsed;
        if (running) {
            elapsed = (System.currentTimeMillis() - resumeTime);
        } else {
            elapsed = (stopTime - resumeTime);
        }
   		elapsed += elapsedTimeInMilliSecsBeforeLatterPause;

        return elapsed;
    }
    

    
    
    //elapsed time as string illustrating hours, mins, secs 
    public String getElapsedTimeString() {   
    	return convertMilliSecsToTimeString(getElapsedTimeMilliSecs());
    }
   
    
    //convert from millisecs (type long) to string time format illustrating hours:mins:secs 
    public static String convertMilliSecsToTimeString (double msecsTime){ 
    	// based on 
    	//     http://www.coderanch.com/t/378404/Java-General/java/Convert-milliseconds-time
    	
    	String milliseconds = Integer.toString((int)(msecsTime % 1000));   
    	double time = msecsTime / 1000; 
    	String seconds = Integer.toString((int)(time % 60));  
    	String minutes = Integer.toString((int)((time % 3600) / 60));  
    	String hours = Integer.toString((int)(time / 3600));
    	
    	for (int i = 0; i < 2; i++) {
    		if (milliseconds.length() < 2) {  
    			milliseconds = "0" + milliseconds;  
    		} 
    		if (seconds.length() < 2) {  
    			seconds = "0" + seconds;  
    		}  
    		if (minutes.length() < 2) {  
    			minutes = "0" + minutes;  
    		}  
    		if (hours.length() < 2) {  
    			hours = "0" + hours;  
    		}  
    	}
    	return (hours + ":" + minutes + ":" + seconds + ":" + milliseconds); 
    }
    
}