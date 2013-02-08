package util.board4;


public final class Masks {
	public final static long[] knightMoves;
	public final static long[] kingMoves;
	public final static long[] rookMoves;
	public final static long[] queenMoves;
	public final static long[] bishopMoves;
	
	public final static long[][] castleMask;
	public final static long[][] castleBlockedMask;
	/** masks the squares for checking if castling through check or not*/
	public final static long[][] castleThroughCheck;
	
	public final static long[] colMask;
	/** exclude specified col (colMaskExc[i] = ~colMask[i]*/
	public final static long[] colMaskExc;
	
	/** passed pawns have no pawns opposing them, and no pawns in either side col*/
	public final static long[][] passedPawnMasks;
	public final static long[] pawnPromotionMask;
	/** masks for pawns that are not passed, but have no pawns in their col*/
	public final static long[][] unopposePawnMasks;
	
	
	static{
		knightMoves = genKnightMoves();
		kingMoves = genKingMoves();
		rookMoves = genRookMoves();
		bishopMoves = genBishopMoves();
		queenMoves = genQueenMoves();
		
		castleMask = new long[2][2];
		castleMask[0][0] = 1L << 2; //white, castle left
		castleMask[0][1] = 1L << 6;
		castleMask[1][0] = 1L << 58;
		castleMask[1][1] = 1L << 62;
		castleBlockedMask = new long[2][2];
		castleBlockedMask[0][0] = 0xEL; //blocked on the left
		castleBlockedMask[0][1] = 0x60L; //right
		castleBlockedMask[1][0] = 0xEL << 56;
		castleBlockedMask[1][1] = 0x60L << 56;
		castleThroughCheck = new long[2][2];
		castleThroughCheck[0][0] = 0x1CL;
		castleThroughCheck[0][1] = 0x70L;
		castleThroughCheck[1][0] = 0x1CL << 56;
		castleThroughCheck[1][1] = 0x70L << 56;
		
		colMask = new long[8];
		long col = 0x0101010101010101L;
		colMask[0] = col;
		for(int i = 1; i < 8; i++){
			colMask[i] = col << i;
		}
		colMaskExc = new long[8];
		for(int i = 0; i < 8; i++){
			colMaskExc[i] = ~colMask[i];
		}

		
		passedPawnMasks = genPassedPawnMasks();
		pawnPromotionMask = new long[]{0xFFL<<56, 0xFFL};
		unopposePawnMasks = genUnopposedPawnMasks();
		
		//System.out.println(getString(pawnPromotionMask[0]));
		/*for(int i = 0; i < 64; i++){
			System.out.println(i);
			System.out.println(getString(unopposePawnMasks[1][i]));
		}*/
	}
	
	private static long[][] genUnopposedPawnMasks(){
		long[][] l = new long[2][64];
		long col = 0x0101010101010101L;
		for(int a = 0; a < 64; a++){
			for(int p = 0; p < 2; p++){
				l[p][a] = p==0? col<<(a+8): col>>>64-(a);
			}
		}
		return l;
	}
	
	private static long[][] genPassedPawnMasks(){
		long[][] l = new long[2][64];
		long col = 0x0101010101010101L;
		for(int a = 0; a < 64; a++){
			for(int p = 0; p < 2; p++){
				l[p][a] = p==0? col<<(a+8): col>>>64-(a);
				if(a%8 != 0){
					l[p][a] |= p==0? col<<(a-1+8): col>>>64-(a-1);
				}
				if(a%8 != 7){
					l[p][a] |= p==0? col<<(a+1+8): col>>>64-(a+1);
				}
			}
		}
		return l;
	}
	
	
	/** generates knight moves from each board position*/
	private static long[] genKnightMoves(){
		final int[][] offsets = new int[][]{
				{-1,2},{1,2},{2,1},{2,-1},
				{1,-2},{-1,-2},{-2,-1},{-2,1}
		};
		return genMoves(offsets, false);
	}
	
	/** generates possible king moves from each board position*/
	private static long[] genKingMoves(){
		int[][] offsets = new int[8][];
		int index = 0;
		for(int x = -1; x <= 1; x++){
			for(int y = -1; y <= 1; y++){
				if(!(x==0 && y==0)){
					offsets[index++] = new int[]{x,y};
				}
			}
		}
		return genMoves(offsets, false);
	}
	
	/** generates possible rook moves from each board position*/
	private static long[] genRookMoves(){
		final long[] moves = new long[64];
		final long hoirzMask = 0x7E;
		final long vertMask = 0x1010101010100L;
		/*final long hoirzMask = 0xFF;
		final long vertMask = 0x101010101010101L;*/
		//System.out.println(getString(vertMask));
		for(int i = 0; i < 64; i++){
			moves[i] = (hoirzMask << 8*(i/8)) | (vertMask << i%8);
			moves[i] |= 1L << i;
			//System.out.println(getString(moves[i]));
		}
		return moves;
	}
	
	private static long[] genQueenMoves(){
		final long[] moves = new long[64];
		for(int i = 0; i < 64; i++){
			moves[i] = rookMoves[i] | bishopMoves[i];
		}
		return moves;
	}
	
	private static long[] genBishopMoves(){
		int[][] offsets = new int[8*4][];
		int index = 0;
		for(int i = 0; i < 8; i++){
			offsets[index] = new int[]{i, i};
			offsets[index+1] = new int[]{i, -i};
			offsets[index+2] = new int[]{-i, -i};
			offsets[index+3] = new int[]{-i, i};
			index += 4;
		}
		long[] moves = genMoves(offsets, true);
		for(int i = 0; i < 64; i++){
			moves[i] |= 1L << i;
		}
		return moves;
	}
	
	/**
	 * Generates all moves at each position for the given offsets
	 * @param offsets
	 * @param avoidEdges if set, avoids placing pieces on board edges
	 * (ie, for queen, rook, bishop)
	 * @return returns moves indexed by position
	 */
	public static long[] genMoves(int[][] offsets, boolean avoidEdges){
		final long[] moves = new long[64];
		final int leftEdge = !avoidEdges? 0: 1;
		final int rightEdge = !avoidEdges? 8: 7;
		for(int i = 0; i < 64; i++){
			final boolean[][] b = new boolean[8][8];
			for(int a = 0; a < offsets.length; a++){
				int x = i%8+offsets[a][0];
				int y = i/8+offsets[a][1];
				if(x >= leftEdge && x < rightEdge && y >= leftEdge && y < rightEdge){
					b[y][x] = true;
				}
			}
			moves[i] = convert(b);
		}
		return moves;
	}
	
	/** converts passed boolean matrix into a bitboarb*/
	private static long convert(boolean[][] b){
		long l = 0;
		long q = 1;
		for(int i = 0; i < 64; i++){
			if(b[i/8][i%8]){
				l |= q << i;
			}
		}
		return l;
	}
	
	/** gets a sting representation of the board specified by passed long
	 * with 0 index in bottom left, 63 index in upper right*/
	public static String getString(long l){
		final long q = 1;
		String s = "";
		String temp = "";
		for(int i = 63; i >= 0; i--){
			long mask = q << i;
			if((l & mask) == mask){
				temp = "X "+temp;
			} else{
				temp = "- "+temp;
			}
			
			if(i % 8 == 0){
				s += temp+"\n";
				temp = "";
			}
		}
		return s;
	}
	
	public static void main(String[] args){
		//System.out.println("here");
		/*long l = 982312;
		System.out.println(getString(l));
		System.out.println(BitUtil.lsbIndex(l));*/
		
		/*Random r = new Random();
		for(int i = 0; i < 99999; i++){
			long templ = r.nextLong();
			//templ = templ & -templ;
			try{
				BitUtil.lsbIndex(templ);
			} catch(Exception e){
				System.out.println(templ+"\n"+getString(templ));
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		final int tests = 999999;
		long time = System.currentTimeMillis();
		for(int i = 0; i < tests; i++){
			BitUtil.lsbIndex(r.nextLong());
		}
		time = System.currentTimeMillis()-time;
		System.out.println("time (msec) = "+time);
		System.out.println("msec/test = "+(time*1./tests));*/
	}
}