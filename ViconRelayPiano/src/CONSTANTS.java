
public final class CONSTANTS {
	public static final boolean writeOutputFile = false;					//Are we going to write to an output file?
	public static final String outputFileName = "Vicon_output_5.txt";		//What is the name of the output file
	public static final int verbose = 150;				//Amount of internal data to print (lower is less)
	public static final int port = 9875;				//port to connect on
	public static final float scale = (float) 0.1;		
	public static final float panScale = 4.0f;				
	public static final float zPlay = .2f;	//.2f		//Location of play barrier
	public static final int objects = 4;			//Number of objects we are tracking
	public static final int minVolume = 7;			//Minimum volume of a strike
	public static final int instrument = 0;			//Grand piano
	/*  Instrument Types
	 * 0-7 Piano
	 * 8-15 chromatic percussion
	 * 16-23 organ
	 * 24-31 Guitar
	 * 32-39 bass
	 * 40-47 strings
	 * 48-55 ensemble
	 * 56-63 brass
	 * 64-71 reed
	 * 72-79 pipe
	 * 80-87 synth lead
	 * 88-95 synth pad
	 * 96-103 synth effects
	 * 104-111 ethnic
	 * 112-119 Percussive
	 * 120-127 sound effects
	 */

	/* TODO: Change this to two octaves, see if it is easier to play
	 * 
	 * 
	 */
	/* maximum volume calcs...
	 * Free fall from 1m... v=4.43m/s
	 * free fall from 1/2m..v=3.126m/s
	 * free fall from 1/3m..v=2.558m/s */ 
	public static final float velMaxVol = 1.50f;		//Velocity of maximum loudness (m/s)
	
	public static final float firstKeyX = -3.17f;		//location of first key in X
	public static final float lastKeyX = 2.3f;			//location of last key in X
	public static final int whiteKeys = 21;				//How many white keys on keyboard
	public static final float range = lastKeyX - firstKeyX; 	//Range in m of keyboard
	public static final float keySize = range/whiteKeys;//Size of one key
	public static final int firstKey = 60;				//Middle C
		
	/* Might need to do this in half strike zones... */
	/*
	 *   1  3     6  8  10
	 * x|# |# |xx|# |# |# |x
	 * |c |d |e |f |g |a |b |
	 *  0  2  4  5  7  9  11
	 */
	public static final int[] wKeyIndex = {0, 2, 4, 5, 7, 9, 11};	//From middle c
	public static final int[] bKeyIndex = {1, 3, 6, 8, 10};			//from middle c
	public static final int[] bKeyIndexHalf = {-1,1,1,3,3,-1,-1,6,6,8,8,10,10,-1};
	//Had conflicting data on what first key was... what is first key?  Have as C right now
	
	public static final String[] wKeyName = {"c", "d", "e", "f", "g", "a", "b"};
	public static final String[] bKeyName = {"X", "c#","c#", "d#","d#","X","X", "f#","f#", "g#","g#","a#","a#","X"};
	
	public static final float yBlackKeys = 0.0f;//y> this, black keys y<this, white keys
	/**y=2.4 at display wall		      
	 * |			    4
	 * |			  ^
	 * |______________x,y = 0,0 at center
	 * |			  v
	 * |      <-y->
	 * |			   -2
	 * 
	 * ^Display Wall
	 */
	//
	public static void DebugPrint(String str, int v){
		if (verbose >= v){
			
			System.out.println(str);
			
		}//end if verbose
		
	}//end DebugPrint
	
}//end CONSTANTS
