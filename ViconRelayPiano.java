
//import java.io.*;
import java.net.*;
//import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jfugue.*;

/**
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
**/
public class ViconRelayPiano
{
	
    
    static LinkedBlockingQueue<String> msgBuf = new LinkedBlockingQueue<String>();
    static volatile boolean keepRunning = true;
    static Thread handlerThread, OnExit;
    static DatagramSocket serverSocket;
    
   public static void main(String args[]) throws Exception
      {
	     serverSocket = new DatagramSocket(CONSTANTS.port);
         System.out.println("Listening on "+CONSTANTS.port);
	     mainLoop();
         
	     
	     CONSTANTS.DebugPrint("Keeprunning = false...", 1);
	     
	     
         
         
       }//end public main

   private static void mainLoop(){
	   
	   byte[] receiveData = new byte[1024];
       //byte[] sendData = new byte[1024];
       MessageHandler handler = new MessageHandler(); 
       handlerThread = new Thread(handler);
       handlerThread.start();
       exitThread exitT = new exitThread();
       OnExit = new Thread(exitT);
       Runtime.getRuntime().addShutdownHook(OnExit);
       
       
	   
	   while(ViconRelayPiano.keepRunning)
       {
          
	      
		   
	      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
          try 
          {
			serverSocket.receive(receivePacket);
          } catch (IOException e) {
			
			e.printStackTrace();
          }
          
          String sentence = new String( receivePacket.getData());
          //System.out.println("RECEIVED+OFFERING: " + sentence);
          
          ViconRelayPiano.msgBuf.offer(sentence);
          

       }//end while keep running
	   
	   
   }//end mainloop function

}//end class

class MessageHandler implements Runnable 
{
	StreamingPlayer music = new StreamingPlayer();
	//We are capturing this information
	
	float x, y, z;
	File logFile;
	BufferedWriter writer;
	
	
	//Message Handler Run
	public void run() {
		
		//If we are logging, open the file to log, and a writer to write
		if (CONSTANTS.writeOutputFile){
			logFile = new File(CONSTANTS.outputFileName);
			try {
				writer = new BufferedWriter(new FileWriter(logFile));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//end if we are logging, make a file
		
		double messages_r = 0;
		double messages_bad = 0;
		long lastTime, thisTime;					//For figuring out timing.  (miliseconds)
		double deltaTime;							//the change in time between samples in seconds.
		float[] lastX = new float[CONSTANTS.objects],	//last x, y, z for each object
				lastY = new float[CONSTANTS.objects],
				lastZ = new float[CONSTANTS.objects];
		//float[] maxZ = new float[CONSTANT;		//Maximum value of Z recorded
		lastTime = System.nanoTime();
		
		System.out.println("Message Handler running...");
		
		/*
		 * Main loop
		 */
		while (ViconRelayPiano.keepRunning) 
		{
            try 
            {
            	/*
            	 * Take a message, split it into sections, mark the time difference...
            	 */
                String msg = ViconRelayPiano.msgBuf.take();			//Should be blocking
                String[] data = msg.split("%");						//Split good from bad
                String[] objects = data[0].split("\n");				//Split objects
                CONSTANTS.DebugPrint("Message Recieved: "+msg, 200);//Debug printing
                thisTime = System.nanoTime();
                deltaTime = (double)(thisTime - lastTime)/1000000000.0;	//get delta time in seconds
                lastTime = thisTime;								//set last time for next iter
                if (deltaTime <= 0) {deltaTime = .001;}				//ensure at least a milisecond
                
                //If we are logging, mark the time delta
                if (CONSTANTS.writeOutputFile){
                	writer.write(Double.toString(deltaTime));
                	writer.newLine();
                }
                
                /* For each part of the message */
                for (int i = 0; i < CONSTANTS.objects; i++){
                	
                	/*
                	 * Each part will be in the format of...
                	 * "Object Name", x, y, z
                	 */
	                String[] parts = objects[i].split("~");
	                messages_r+=1;	//mark that we received another message
	               
	                //Make sure we have enough parts
		            if (parts.length >= 4){
		                
		            	//If we are logging, write the object name, x, y, z
		            	if (CONSTANTS.writeOutputFile){
		            		writer.write(objects[i]);
		            		writer.newLine();
		            	}//end if we are writing a log file
		            	
		            	/* Set up the old values and parse in the new values */

		            	x = Float.parseFloat(parts[1]);
		                y = Float.parseFloat(parts[2]);
		                z = Float.parseFloat(parts[3]);
		                //if bad data, throw it out.
		                if(x == 0.0f && y == 0.0f && z == 0.0f){
		                	continue;
		                }
		                /*
		                 * Left most white key at -3.26
		                 * + one octave at        -1.36
		                 *  90 cm between octaves. (8 white keys)
		                 * 
		                 */
		            	//If we have crossed the play barrier
		                if (z < CONSTANTS.zPlay && lastZ[i] > CONSTANTS.zPlay)
		                {
		                	/*
		                	 * Calculate the velocity of the object in m/s using
		                	 * 	pathagrian's Theorem, then relate to volume
		                	 */
		                	/*Are we within bounds?
		                	 */
		                	if (x < CONSTANTS.firstKeyX || x > CONSTANTS.lastKeyX){
		                		continue;
		                	}
		                	//Velocity on only z.
		                	double velocity = (Math.sqrt(Math.pow(z - lastZ[i], 2)))/deltaTime;
		                	
		                	//Calc volume vel/maxvel
		                	int vol = CONSTANTS.minVolume 
		                			+ (int)(((127-CONSTANTS.minVolume)*velocity/CONSTANTS.velMaxVol));	
		                	
		                	if (vol > 127)
		                	{
		                		vol = 127;
		                	}
		                	
		                	//Find note
		                	float xScaled = (x - CONSTANTS.firstKeyX);					//x value scaled to start
		                	int fullNoteOffset = (int)((xScaled)/CONSTANTS.keySize + 1);	//x value scaled to key size
		                	int halfNoteOffset = (int)((xScaled)/(CONSTANTS.keySize/2.0));//half note offset (half key size)
		                	int octaveOffset = (fullNoteOffset)/7;						//octave
		                	int noteOffset = 0;					//midi offset of the note
		                	String noteName = "X";
		                	
		                	//Playing in white keys
		                	if (y < CONSTANTS.yBlackKeys) 
		                	{
		                		noteOffset = CONSTANTS.wKeyIndex[fullNoteOffset%7];
		                		noteName = CONSTANTS.wKeyName[fullNoteOffset%7];
		                	}//end if white key range
		                	else//Playing in black keys..... if -1 is returned for note offset, dead zone
		                	{
		                		noteOffset = CONSTANTS.bKeyIndexHalf[(halfNoteOffset)%14];
		                		noteName = CONSTANTS.bKeyName[(halfNoteOffset)%14];
		                	}//end if black key range
		                	int note = octaveOffset*12+noteOffset+CONSTANTS.firstKey;	
		                	
		                	CONSTANTS.DebugPrint("X: "+x+" Y: "+y+" Z: "+z+
		                			"\nXscaled: "+ xScaled + "  fullNoteOffset: "+fullNoteOffset+"  halfNoteOffset: "+halfNoteOffset+
		                			"\noctaveOffset: "+octaveOffset+" noteOffset: "+noteOffset+ 
		                			"\nTimeDelta: "+deltaTime + "   Velocity: "+velocity+
		                			"\nVol: "+vol+" Note: "+note+" NoteName: "+ noteName +"\n", 50);
		                	
		                	//If we received a valid note offset, play the note.
		                	if (noteOffset != -1){
			                	//String noteString ="["+note+"]wa"+vol; 
			                	String noteString = "I"+CONSTANTS.instrument+" ["+note+"]wa"+vol;
			                	music.stream(noteString);
		                	}//end if received valid data
		                	
		                }//end if note is to be played
		            	lastX[i] = x;
		                lastY[i] = y;
		                lastZ[i] = z;
		            }//end if good message
		            
		            else//bad message
		            {
		            	CONSTANTS.DebugPrint("Bad Message! Parts:"+parts.toString()+" Expecting >=4", 150);
		            	messages_bad+=1;
		            	
		            	CONSTANTS.DebugPrint(msg, 150);
		            	CONSTANTS.DebugPrint("BAD RATIO: " + (messages_bad/messages_r), 150);
		            
		            }//end else bad message
	            
                }//end for each object
                
            } catch (InterruptedException | IOException ie) {
            	
            	ie.printStackTrace();
            	
            }//end catch
            
        }//end while the program is still running
		
		//flush and close the writer
		try {
			if(CONSTANTS.writeOutputFile){
				writer.flush();
				writer.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		music.close();
	}//end run method
	
	
	
}//end MessageHandler class
	


class exitThread implements Runnable {
	
	public void run() {
		System.out.println("Exiting");
		ViconRelayPiano.serverSocket.close();
		ViconRelayPiano.keepRunning = false;
		
	}//end run exitThread
}//end class exitThread

