package tests.searchTests;

import java.util.ArrayList;
import java.util.List;

import search.Search3;
import search.SearchStat;
import search.legacy.SearchS4V29;
import state4.State4;
import util.OldPositions;
import debug.Debug;
import eval.Evaluator2;
import eval.evalV8.SuperEvalS4V8;


/** tests searchs on several sample boards and aggregates results*/
public class Test29 {
	public static void main(String[] args){
		
		List<char[][]> boards = new ArrayList<char[][]>();
		boards.add(OldPositions.puzzleSetup1);
		boards.add(OldPositions.puzzleSetup2);
		boards.add(OldPositions.puzzleSetup3);
		boards.add(OldPositions.enPassanteMistake);
		boards.add(OldPositions.queenSac1);
		boards.add(OldPositions.queenSac2);
		boards.add(OldPositions.bishopSack);
		boards.add(OldPositions.queenKingFork);
		
		final int maxPly = 8;
		SearchStat agg = new SearchStat();
		
		for(char[][] c: boards){
			State4 s = Debug.loadConfig(c);
			
			Evaluator2<State4> e1 = 
					new SuperEvalS4V8();
			
			Search3 search = new SearchS4V29(maxPly, s, e1, 20, false);
			
			search.search(State4.WHITE, new int[4], 8);
			SearchStat stats = search.getStats();
			agg(stats, agg);
			print(stats);
			
			/*stats = search.search(State3.BLACK, maxPly, new int[4]);
			agg(stats, agg);
			print(stats);*/
		}
		
		System.out.println("\n=======================\n");
		print(agg);

	}
	
	/** aggregate search stats together*/
	private static void agg(SearchStat s, SearchStat agg){
		//agg.forcedQuietCutoffs += s.forcedQuietCutoffs;
		//agg.hashHits += s.hashHits;
		agg.nodesSearched += s.nodesSearched;
		agg.searchTime += s.searchTime;
	}
	
	private static void print(SearchStat s){
		//String t = ""+s.nodesSearched+" in "+s.searchTime+"ms (hash="+s.hashHits+", q-cuts="+s.forcedQuietCutoffs+")";
		String t = ""+s.nodesSearched+" in "+s.searchTime+"ms";
		System.out.println(t);
	}
}
