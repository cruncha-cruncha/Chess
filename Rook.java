package Chess;

public class Rook extends PieceAbstract {

	public Rook (Board board, Colour colour, int row, int col) {
		super(board,colour,row,col);
		possibleMoves = new int[14][2];
	}
	
	public Rook (Board board, Colour colour, int row, int col, PieceAbstract next) {
		this(board,colour,row,col);
		this.next = next;
	}
	
	public String printChar() {
		return "R";
	}
	
	public boolean hasMoved () {
		if (oldPosition == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public void castle() {
		oldPosition = new OldPosition(this,row,col,oldPosition);
		if (row == 0) {
			if (col == 0) {
				us[0][0] = null;
				col = 3;
			} else {
				us[0][7] = null;
				col = 5;
			}
		} else {
			if (col == 0) {
				us[7][0] = null;
				col = 3;
			} else {
				us[7][7] = null;
				col = 5;
			}
		}
		us[row][col] = this;
	}
	
	public void undoCastle() {
		oldPosition = oldPosition.next;
		board.moveList.removeFirst();
		if (row == 0) {
			if (col == 3) {
				us[0][3] = null;
				col = 0;
			} else {
				us[0][5] = null;
				col = 7;
			}
		} else {
			if (col == 3) {
				us[7][3] = null;
				col = 0;
			} else {
				us[7][5] = null;
				col = 7;
			}
		}
		us[row][col] = this;
	}
	
	public boolean move(int r, int c) {
		if (!inPlay)
			return false;
		if (r < 0 || r > 7)
			return false;
		if (c < 0 || c > 7)
			return false;
		if (colour == Colour.BLACK && r > row)
			return false;
		if (colour == Colour.WHITE && r < row)
			return false;
		
		int diffRow = r-row;
		int diffCol = c-col;
		
		// http://stackoverflow.com/questions/13988805/fastest-way-to-get-sign-in-java
		int rowSign = (int) Math.signum(diffRow);
		int colSign = (int) Math.signum(diffCol);
		
		if (us[r][c] == null) {
			if (diffRow == 0) {
				for (int x=1; x < Math.abs(diffCol); x++) {
					if (us[row][col+x*colSign] != null)
						return false;
					if (them[row][col+x*colSign] != null)
						return false;
				}
			} else if (diffCol == 0) {
				for (int x=1; x < Math.abs(diffRow); x++) {
					if (us[row+x*rowSign][col] != null)
						return false;
					if (them[row+x*rowSign][col] != null)
						return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}

		updateBoard(r,c);
		if (board.inCheck(colour)) {
			if (board.calcCheck(colour)) {
				super.undoMove();
				return false;
			}
		} else {
			if (board.calcCheck(colour)) {
				super.undoMove();
				board.noCheck(colour);
				return false;
			}
		}
		
		return true;
	}
	
	public int[][] calcPossibleMoves() {
		int count = 0;
		for (int x=1; x<8; x++) {
			if (move(row,col+x)) {
				this.undoMove();
				possibleMoves[count][0] = row;
				possibleMoves[count][1] = col+x;
				count++;
			} else if (move(row,col-x)) {
				this.undoMove();
				possibleMoves[count][0] = row;
				possibleMoves[count][1] = col-x;
				count++;
			} else if (move(row+x,col)) {
				this.undoMove();
				possibleMoves[count][0] = row+x;
				possibleMoves[count][1] = col;
				count++;
			} else if (move(row-x,col)) {
				this.undoMove();
				possibleMoves[count][0] = row-x;
				possibleMoves[count][1] = col;
				count++;
			}
		}
		
		if (count != 14)
			possibleMoves[count][0] = -1;
		
		return possibleMoves;
	}
}