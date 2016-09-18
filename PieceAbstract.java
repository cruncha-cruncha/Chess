package Chess;

public abstract class PieceAbstract implements PieceInterface {
	protected Colour colour;
	protected int row, col;
	protected OldPosition oldPosition;
	public boolean inPlay;
	protected Board board;
	protected PieceAbstract[][] us, them;
	public PieceAbstract next;
	protected PieceAbstract captured;
	protected int[][] possibleMoves;
	
	public PieceAbstract (Board board, Colour colour, int row, int col) {
		this.board = board;
		this.colour = colour;
		this.row = row;
		this.col = col;
		if (colour == Colour.BLACK) {
			us = board.black;
			them = board.white;
		} else {
			us = board.white;
			them = board.black;
		}
		inPlay = true;
	}
	
	public PieceAbstract (Board board, Colour colour, int row, int col, PieceAbstract next) {
		this(board,colour,row,col);
		this.next = next;
	}
	
	public boolean fillHistory(OldPosition op, PieceAbstract captured) {
		if (oldPosition == null) {
			oldPosition = op;
			this.captured = captured;
			return true;
		} else {
			return false;
		}
	}
	
	public Colour getColour() {
		return colour;
	}
	
	public int[] getCoor() {
		int[] out = {row,col};
		return out;
	}
	
	public int getRow() {
		return row;
	}
	
	public int getCol() {
		return col;
	}
	
	public int getOldRow() {
		if (oldPosition != null) {
			return oldPosition.getRow();
		} else {
			return 0;
		}
	}
	
	public int getOldCol() {
		if (oldPosition != null) {
			return oldPosition.getCol();
		} else {
			return 0;
		}
	}
	
	public Board getBoard() {
		return board;
	}
	
	protected void updateBoard (int r, int c) {
		us[r][c] = us[row][col];
		us[row][col] = null;
		oldPosition = new OldPosition(this,row,col,oldPosition);
		
		row = r;
		col = c;
		
		if (captured != null)
			oldPosition.captured = captured;
		
		if (them[row][col] != null) {
			captured = them[row][col];
			captured.inPlay = false;
			them[row][col] = null;
		} else {
			captured = null;
		}
	}
	
	public PieceAbstract undoMove() {
		if (oldPosition == null)
			return this;
		
		OldPosition op = oldPosition;
		oldPosition = oldPosition.next;
		board.moveList.removeFirst();
		
		if (op.getType() == this.getClass()) {
			us[op.getRow()][op.getCol()] = us[row][col];
			us[row][col] = null;
			
			if (captured != null) {
				them[row][col] = captured;
				captured.inPlay = true;
			}
			
			row = op.getRow();
			col = op.getCol();
			captured = op.captured;
			
			// or maintain a list of prpevious check states?
			board.calcCheck(Colour.BLACK);
			board.calcCheck(Colour.WHITE);
			return this;
		} else {
			// convert back to a pawn
			PieceAbstract l = (colour == Colour.BLACK) ? board.blackKing : board.whiteKing;
			PieceAbstract p = l.next;
			while (p != null) {
				if (p == this) {
					break;
				} else {
					l = p;
					p = p.next;
				}
			}
			
			l.next = new Pawn(board,colour,op.getRow(),op.getCol(),p.next);
			l.next.fillHistory(oldPosition,op.captured);
			
			us[op.getRow()][op.getCol()] = l.next;
			us[row][col] = null;
			
			if (captured != null) {
				them[row][col] = captured;
				captured.inPlay = true;
			}
			
			board.calcCheck(Colour.BLACK);
			board.calcCheck(Colour.WHITE);
			return l.next;
		}
	}
}