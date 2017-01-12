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
	// 1 = sync not castle (rook/king moved from default square)
	// 0 = piece used to be a pawn
	// 
	// pieces[1] bits:
	// 7-5 = unused
	// 4-0 = index (of Board.pieces)
	// 
	// pieces[2] bits:
	// 7-0 = old piece (what Board.pieces[x] used to be)
	// 
	// pieces[3] bits:
	// 7-5 = unused
	// 4-0 = index of captured piece (of Board.pieces)
	public byte[] pieces;
	public OldPosition prev; // linked list
	
	/**
	 * Constructor, automatically links list
	 * 
	 * @param prev  the previous OldPosition
	 */
	public OldPosition (OldPosition prev) {
		this.prev = prev;
		pieces = new byte[4];
	}
	
	/** Empty constructor, used for sentinel node */
	public OldPosition () { }
}