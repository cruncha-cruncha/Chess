package Chess;

public class King extends PieceAbstract {
		
	public King (Board board, Colour colour, int row, int col) {
		super(board,colour,row,col);
		possibleMoves = new int[8][2];
	}
	
	public King (Board board, Colour colour, int row, int col, PieceAbstract next) {
		this(board,colour,row,col);
		this.next = next;
	}
	
	public String printChar() {
		return "K";
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
		
		boolean canMove = false;
		Rook canCastle = null;
		if (us[r][c] == null) {
			if (diffCol == 2 && !board.inCheck(colour) && oldPosition == null) {
				if (row == 0) {
					if (board.white[0][7] != null && board.white[0][7].printChar().equals("R")) {
						Rook rook = (Rook) board.white[0][7];
						if (!rook.hasMoved() && them[0][6] == null && us[0][5] == null && them[0][5] == null)
							canMove = true;
							canCastle = rook;
					}
				} else {
					if (board.black[7][7] != null && board.black[7][7].printChar().equals("R")) {
						Rook rook = (Rook) board.black[7][7];
						if (!rook.hasMoved() && them[7][6] == null && us[7][5] == null && them[7][5] == null)
							canMove = true;
							canCastle = rook;
					}
				}
			} else if (diffCol == -2 && !board.inCheck(colour) && oldPosition == null) {
				if (row == 0) {
					if (board.white[0][0] != null && board.white[0][0].printChar().equals("R")) {
						Rook rook = (Rook) board.white[0][0];
						if (!rook.hasMoved() && them[0][2] == null && us[0][1] == null && them[0][1] == null && us[0][3] == null && them[0][3] == null)
							canMove = true;
							canCastle = rook;
					}
				} else {
					if (board.black[7][0] != null && board.black[7][0].printChar().equals("R")) {
						Rook rook = (Rook) board.black[7][0];
						if (!rook.hasMoved() && them[7][2] == null && us[7][1] == null && them[7][1] == null && us[7][3] == null && them[7][3] == null)
							canMove = true;
							canCastle = rook;
					}
				}
			} else if (diffRow > -2 && diffRow < 2 && diffCol > -2 && diffCol < 2) {
				canMove = true;
			}
		}
		
		if (canMove) {
			if (canCastle != null)
				canCastle.castle();
			updateBoard(r,c);
			if (board.calcCheck(colour)) {
				undoMove();
				board.noCheck(colour);
				canMove = false;
			}
		}
		
		return canMove;
	}
	
	public PieceAbstract undoMove() {
		if (oldPosition == null)
			return this;
		
		OldPosition op = oldPosition;
		oldPosition = oldPosition.next;
		board.moveList.removeFirst();
		
		if (oldPosition == null && row == 0 && col == 2) {
			((Rook) board.white[0][3]).undoCastle();
		} else if (oldPosition == null && row == 0 && col == 6) {
			((Rook) board.white[0][5]).undoCastle();
		} else if (oldPosition == null && row == 7 && col == 2) {
			((Rook) board.white[7][3]).undoCastle();
		} else if (oldPosition == null && row == 7 && col == 6) {
			((Rook) board.white[7][5]).undoCastle();
		}
		
		us[op.getRow()][op.getCol()] = us[row][col];
		us[row][col] = null;
		
		if (captured != null) {
			them[row][col] = captured;
			captured.inPlay = true;
		}
		
		row = op.getRow();
		col = op.getCol();
		captured = op.captured;
		
		board.calcCheck(Colour.BLACK);
		board.calcCheck(Colour.WHITE);
		return this;
	}
	
	public int[][] calcPossibleMoves() {
		int[][] candidates = {{row,col+1},{row,col-1},{row+1,col},{row+1,col+1},{row+1,col-1},{row-1,col},{row-1,col+1},{row-1,col-1},{row+2,col},{row-2,col}};
		
		int count = 0;
		for (int i = 0; i < 10; i++) {
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