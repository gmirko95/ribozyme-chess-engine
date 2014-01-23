package chess.search.search34.pipeline;

import chess.search.search34.Hash;
import chess.search.search34.Search34;
import chess.search.search34.StackFrame;
import chess.search.search34.TTEntry;
import chess.state4.State4;

/** null move pruning */
public class NullMoveStage implements MidStage {

	private final StackFrame[] stack;
	private final Hash m;
	private final Search34 searcher;
	private final MidStage next;

	public NullMoveStage(StackFrame[] stack, Hash m, Search34 searcher, MidStage next){
		this.stack = stack;
		this.m = m;
		this.searcher = searcher;
		this.next = next;
	}

	@Override
	public int eval(SearchContext c, NodeProps props, State4 s) {
		final boolean threatMove; //true if opponent can make a move that causes null-move fail low

		if(c.nt != NodeType.pv &&
				!c.skipNullMove &&
				c.depth > 3 * Search34.ONE_PLY &&
				!props.alliedKingAttacked &&
				props.hasNonPawnMaterial &&
				props.nonMateScore){

			final int r = 3*Search34.ONE_PLY + c.depth/4;

			//note, non-pv nodes are null window searched - no need to do it here explicitly
			//stack[c.stackIndex+1].futilityPrune = true;
			s.nullMove();
			final long nullzkey = s.zkey();
			int n = -searcher.recurse(new SearchContext(1 - c.player, -c.beta, -c.alpha, c.depth - r, c.nt, c.stackIndex + 1, true), s);
			s.undoNullMove();

			threatMove = n < c.alpha;

			if(n >= c.beta){
				if(n >= 70000){
					n = c.beta;
				}
				if(c.depth < 12*Search34.ONE_PLY){
					return n;
				}

				//verification chess.search
				//stack[stackIndex+1].futilityPrune = false;
				double v = searcher.recurse(new SearchContext(c.player, c.alpha, c.beta, c.depth - r, c.nt, c.stackIndex + 1, true), s);
				if(v >= c.beta){
					return n;
				}
			} else if(n < c.alpha){
				final TTEntry nullTTEntry = m.get(nullzkey);
				if(nullTTEntry != null && nullTTEntry.move != 0){
					//doesnt matter which we store to, no killers stored at this point in execution
					stack[c.stackIndex].killer[0] = nullTTEntry.move & 0xFFFL;
				}
			} //case alpha < n < beta can only happen in pv nodes, at which we dont null move prune
		} else{
			threatMove = false;
		}

		return next.eval(c, props, s);
	}
}
