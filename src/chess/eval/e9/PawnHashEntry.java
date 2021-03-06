package chess.eval.e9;

public final class PawnHashEntry {
	/** score for white*/
	public int score1;
	/** number of isolated pawns for white*/
	public int isolatedPawns1;
	/** number of backward pawns for white*/
	public int backwardPawns1;
	/** number of doubled pawns for white*/
	public int doubledPawns1;
	
	/** score for black*/
	public int score2;
	/** number of isolated pawns for white*/
	public int isolatedPawns2;
	/** number of backward pawns for white*/
	public int backwardPawns2;
	/** number of doubled pawns for white*/
	public int doubledPawns2;
	
	/** all passed pawns in the game (ie, both players passed pawns aggregate)*/
	public long passedPawns;
	/** all weak pawns in the game*/
	public long weakPawns;
	public long zkey;
	int seq;
	
	public void clear(){
		score1 = 0;
		isolatedPawns1 = 0;
		backwardPawns1 = 0;
		doubledPawns1 = 0;
		
		score2 = 0;
		isolatedPawns2 = 0;
		backwardPawns2 = 0;
		doubledPawns2 = 0;
		
		passedPawns = 0;
		weakPawns = 0;
		zkey = 0;
	}
	
	public static void fill(final PawnHashEntry src, final PawnHashEntry target){
		target.score1 = src.score1;
		target.isolatedPawns1 = src.isolatedPawns1;
		target.backwardPawns1 = src.backwardPawns1;
		target.doubledPawns1 = src.doubledPawns1;
		
		target.score2 = src.score2;
		target.isolatedPawns2 = src.isolatedPawns2;
		target.backwardPawns2 = src.backwardPawns2;
		target.doubledPawns2 = src.doubledPawns2;
		
		target.passedPawns = src.passedPawns;
		target.weakPawns = src.weakPawns;
		target.zkey = src.zkey;
	}
}
