package Chess;

public class OldPosition {
	public byte[] pieces;
	public OldPosition prev;
	
	// CHANGE TO FOUR BITS, ONE FOR FLAGS
	
	// pieces[0] bits:
	// 7 = en passant
	// 6 = captured a piece non passant
	// 5 = was in check
	// 4 = put other in check
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
	
	public OldPosition (byte[] pieces, OldPosition prev) {
		this.pieces = pieces;
		this.prev = prev;
	}
	
	public OldPosition (OldPosition prev) {
		this.prev = prev;
		pieces = new byte[4];
	}
	
	public OldPosition (byte[] pieces) {
		this.pieces = pieces;
	}
	
	public OldPosition () { }
}