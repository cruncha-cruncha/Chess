package Chess;

/**
 * Used to track move history, this class can form a basic
 * linked list. 
 */
public class OldPosition {

	// pieces[0] (the flags byte) bits:
	// 7 = en passant
	// 6 = captured a piece non passant
	// 5 = black was in check
	// 4 = white was in check
	// 3 = kingside castle
	// 2 = queenside castle
	// 1 = sync not castle
	// 0 = used to be a pawn
	// pieces[1] bits:
	// 7-5 = unused
	// 4-0 = index
	// pieces[2] bits:
	// 7-0 = old piece
	// pieces[3] bits:
	// 7-5 = unused
	// 4-0 = index of captured piece
	public byte[] pieces;
	public OldPosition prev;
	
	/**
	 * Constructor, automatically links list
	 * 
	 * @param prev  the previous OldPosition
	 */
	public OldPosition (OldPosition prev) {
		this.prev = prev;
		pieces = new byte[4];
	}
	
	/** Empty constructor */
	public OldPosition () { }
}