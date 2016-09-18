package Chess;

public class Bishop extends PieceAbstract {
	
	public Bishop (Board board, Colour colour, int row, int col) {
		super(board,colour,row,col);
		possibleMoves = new int[13][2];
	}
	
	public Bishop (Board board, Colour colour, int row, int col, PieceAbstract next) {
		this(board,colour,row,col);
		this.next = next;
	}
	
	public String printChar() {
		return "B";
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
		
		if (us[r][c] == null && Math.abs(diffRow) == Math.abs(diffCol)) {
			for (int x=1; x < Math.abs(diffRow); x++) {
				if (us[row+x*rowSign][col+x*colSign] != null)
					return false;
				if (them[row+x*rowSign][col+x*colSign] != null)
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
			if (move(row+x,col+x)) {
				this.undoMove();
				possibleMoves[count][0] = row+x;
				possibleMoves[count][1] = col+x;
				count++;
			} else if (move(row+x,col-x)) {
				this.undoMove();
				possibleMoves[count][0] = row+x;
				possibleMoves[count][1] = col-x;
				count++;
			} else if (move(row-x,col+x)) {
				this.undoMove();
				possibleMoves[count][0] = row-x;
				possibleMoves[count][1] = col+x;
				count++;
			} else if (move(row-x,col-x)) {
				this.undoMove();
				possibleMoves[count][0] = row-x;
				possibleMoves[count][1] = col-x;
				count++;
			}
		}
		
		if (count != 13)
			possibleMoves[count][0] = -1;
		
		return possibleMoves;
	}
}