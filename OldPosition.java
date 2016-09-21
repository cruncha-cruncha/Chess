package Chess;

public class OldPosition {
	public byte[] pieces;
	public OldPosition next;
	
	// pieces[0] = index of pieces[1]
	// pieces[1] = a piece
	// pieces[2] bits:
	// 7: castle
	// 6: castle side (0 = queenside, 1 = kingside)
	// 5: pawn promotion
	// 0-4: index
	
	public OldPosition (byte[] pieces, OldPosition next) {
		this.pieces = pieces;
		this.next = next;
	}
	
	public OldPosition (OldPosition next) {
		this.next = next;
		pieces = new byte[3];
	}
	
	public OldPosition (byte[] pieces) {
		this.pieces = pieces;
	}
	
	public OldPosition () {
		pieces = new byte[3];
	}
}