package Chess;

public class OldPosition {
	private int row, col;
	public PieceAbstract captured;
	public OldPosition next;
	private PieceAbstract piece;
	
	public OldPosition (PieceAbstract piece, int row, int col, PieceAbstract captured, OldPosition next) {
		this(piece,row,col,captured);
		this.next = next;
	}
	
	public OldPosition (PieceAbstract piece, int row, int col, PieceAbstract captured) {
		this(piece,row,col);
		this.captured = captured;
	}
	
	public OldPosition (PieceAbstract piece, int row, int col, OldPosition next) {
		this(piece,row,col);
		this.next = next;
	}
	
	public OldPosition (PieceAbstract piece, int row, int col) {
		// notify Board to add ourselves to movesList?
		this.piece = piece;
		this.row = row;
		this.col = col;
		piece.getBoard().moveList.addFirst(this);
	}
	
	public int getRow() {
		return row;
	}
	
	public int getCol() {
		return col;
	}
	
	public Class getType() {
		return piece.getClass();
	}
	
	public Colour getColour() {
		return piece.getColour();
	}
	
	public PieceAbstract getPiece() {
		return piece;
	}
}