package chess.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chess.state4.State4;
import chess.uci.Position;

public class FenParser {
	/**
	 * parses fen and stores result in passed position object
	 * @param fen fen to read position from
	 * @param p position in which to store the loaded fen
	 */
	public static void parse(String fen, Position p){
		String[] s = fen.split("\\s+");
		
		parsePieces(s[0], p.s);
		p.sideToMove = s[1].toLowerCase().charAt(0) == 'w'? 0: 1;
		parseCastling(s[2], p.s);
		parseEnPassant(s[3], p.s);
		p.halfMoves = s[4].equals("-")? 0: Integer.parseInt(s[4]);
		p.s.drawCount = p.halfMoves;
		p.fullMoves = s[5].equals("-")? 0: Integer.parseInt(s[5]);

		p.s.update(p.sideToMove);
	}
	
	private static void parseEnPassant(String p, State4 state){
		if(p.length() == 1 && p.equals("-")){
			return;
		}
		
		p = p.toLowerCase();
		int index = p.charAt(0)-'a'+(p.charAt(1)-'1')*8;
		state.enPassante = 1L << index;
	}
	
	private static void parseCastling(String p, State4 state){
		state.kingMoved[0] = true;
		state.kingMoved[1] = true;
		state.rookMoved[0][0] = true;
		state.rookMoved[0][1] = true;
		state.rookMoved[1][0] = true;
		state.rookMoved[1][1] = true;
		if(p.length() == 1 && p.equals("-")){
			return;
		}
		
		for(int a = 0; a < p.length(); a++){
			char c = p.charAt(a);
			int player = Character.isUpperCase(c)? 0: 1;
			state.kingMoved[player] = false;
			c = Character.toLowerCase(c);
			
			if(c == 'k'){
				state.rookMoved[player][1] = false;
			} else if(c == 'q'){
				state.rookMoved[player][0] = false;
			}
		}
	}
	
	private static void parsePieces(String p, State4 state){
		String[] s = p.split("/");
		for(int a = 0; a < 8; a++){
			
			int off = 0;
			for(int i = 0; i < s[a].length(); i++){
				
				char c = s[a].charAt(i);
				if(Character.isDigit(c)){
					int offset = Integer.parseInt(""+c);
					off += offset-1;
				} else{
					int player = Character.isUpperCase(c)? State4.WHITE: State4.BLACK;
					c = Character.toLowerCase(c);
					long[] pieces = null;
					int type = 0;
					
					if(c == 'q'){
						pieces = state.queens;
						type = State4.PIECE_TYPE_QUEEN;
					} else if(c == 'b'){
						pieces = state.bishops;
						type = State4.PIECE_TYPE_BISHOP;
					} else if(c == 'r'){
						pieces = state.rooks;
						type = State4.PIECE_TYPE_ROOK;
					} else if(c == 'k'){
						pieces = state.kings;
						type = State4.PIECE_TYPE_KING;
					} else if(c == 'n'){
						pieces = state.knights;
						type = State4.PIECE_TYPE_KNIGHT;
					} else if(c == 'p'){
						pieces = state.pawns;
						type = State4.PIECE_TYPE_PAWN;
					}
					
					final int index = (7-a)*8+off+i;
					pieces[player] |= 1L << index;
					//state.mailbox[index] = type; //handled by update below
					state.pieceCounts[player][type]++;
					state.pieceCounts[player][State4.PIECE_TYPE_EMPTY]++;
				}
			}
		}
	}
}
