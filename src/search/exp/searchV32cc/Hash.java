package search.exp.searchV32cc;

public interface Hash {
	public void put(final long zkey, final TTEntry t);
	public TTEntry get(final long zkey);
}
