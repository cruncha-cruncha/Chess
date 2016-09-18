package Chess;

public class Knight extends PieceAbstract {
	
	public Knight (Board board, Colour colour, int row, int col) {
		super(board,colour,row,col);
		possibleMoves = new int[8][2];
	}
	
	public Knight (Board board, Colour colour, int row, int col, PieceAbstract next) {
		this(board,colour,row,col);
		this.next = next;
	}
	
	public String printChar() {
		return "N";
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
		
		boolean canMove = false;
		int diffRow = Math.abs(r-row);
		int diffCol = Math.abs(c-col);
		
		if (us[r][c] == null) {
			if (diffRow == 1 && diffCol == 2) {
				canMove = true;
			} else if (diffRow == 2 && diffCol == 1) {
				canMove = true;
			}
		}
		
		if (canMove) {
			updateBoard(r,c);
			if (board.inCheck(colour)) {
				if (board.calcCheck(colour)) {
					super.undoMove();
					canMove = false;
				}
			} else {
				if (board.calcCheck(colour)) {
					super.undoMove();
					board.noCheck(colour);
					canMove = false;
				}
			}
		}
		
		return canMove;
	}
	
	public int[][] calcPossibleMoves() {
		int[][] candidates = {{row+1,col+2},{row+1,col-2},{row+2,col+1},{row+2,col-1},{row-1,col+2},{row-1,col-2},{row-2,col+1},{row-2,col-1}};
		
		int count = 0;
		for (int i = 0; i < 8; i++) {
			if (move(candidates[i][0],candidates[i][1])) {
				this.undoMove();
				possibleMoves[count] = candidates[i];
				count++;
			}
		}
		
		if (count != 8)
			possibleMoves[count][0] = -1;
		
		return possibleMoves;
	}
}