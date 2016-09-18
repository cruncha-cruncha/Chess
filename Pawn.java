package Chess;

public class Pawn extends PieceAbstract {
	private int startRow, enPassant;
	private boolean capturePassant;
	
	public Pawn (Board board, Colour colour, int row, int col) {
		super(board,colour,row,col);
		if (colour == Colour.BLACK) {
			startRow = 6;
			enPassant = 3;
		} else {
			startRow = 1;
			enPassant = 4;
		}
		capturePassant = false;
		possibleMoves = new int[4][2];
	}
	
	public Pawn (Board board, Colour colour, int row, int col, PieceAbstract next) {
		this(board,colour,row,col);
		this.next = next;
	}
	
	public String printChar() {
		return "P";
	}
	
	public boolean move(int r, int c) throws PawnPromotion {
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
		
		capturePassant = false;
		boolean canMove = false;
		if (c == col) {
			if (us[r][c] != null)
				return false;
			if (them[r][c] != null)
				return false;
			if (Math.abs(r-row) == 1) {
				canMove = true;
			} else if (Math.abs(r-row) == 2 && row == startRow) {
				if (colour == Colour.BLACK) {
					if (us[r+1][c] == null && them[r+1][c] == null)
						canMove = true;
				} else {
					if (us[r-1][c] == null && them[r-1][c] == null)
						canMove = true;
				}
			}
		} else if (c+1 == col || c-1 == col) {
			if (Math.abs(r-row) == 1) {
				if (them[r][c] != null) {
					canMove = true;
				} else {
					// en Passant is only legal if it is the subsequent move
					if (row == enPassant && board.moveList != null) {
						OldPosition p = board.moveList.getFirst();
						if (p.getType() == Pawn.class) {
							if (p.getPiece().getRow() == row && p.getPiece().getCol() == c) {
								if (colour == Colour.BLACK) {
									if (p.getRow() == r-1 && p.getCol() == c)
										capturePassant = true;
										canMove = true;
								} else {
									if (p.getRow() == r+1 && p.getCol() == c)
										capturePassant = true;
										canMove = true;
								}
								
							}
						}
					}
				}
			}
		} else {
			return false;
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
		
		if (canMove) 
			if (row == 0 || row == 7) 
				throw new PawnPromotion(this);
		
		return canMove;
	}
	
	public OldPosition getHistory() {
		return oldPosition;
	}
	
	public PieceAbstract getCaptured() {
		return captured;
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
		} else if (capturePassant) {
			if (colour == Colour.BLACK) {
				captured = them[row+1][col];
				captured.inPlay = false;
				them[row+1][col] = null;
			} else {
				captured = them[row-1][col];
				captured.inPlay = false;
				them[row+1][col] = null;
			}
			capturePassant = false;
		} else {
			captured = null;
		}
	}
	
	public int[][] calcPossibleMoves() {
		int[][] candidates = new int[4][2];
		if (colour == Colour.BLACK) {
			candidates[0][0] = row-1;
			candidates[0][1] = col;
			candidates[1][0] = row-1;
			candidates[1][1] = col+1;
			candidates[2][0] = row-1;
			candidates[2][1] = col-1;
			candidates[3][0] = row-2;
			candidates[3][1] = col;
		} else {
			candidates[0][0] = row+1;
			candidates[0][1] = col;
			candidates[1][0] = row+1;
			candidates[1][1] = col+1;
			candidates[2][0] = row+1;
			candidates[2][1] = col-1;
			candidates[3][0] = row+2;
			candidates[3][1] = col;
		}
		
		int count = 0;
		for (int i = 0; i < 4; i++) {
			try {
				if (move(candidates[i][0],candidates[i][1])) {
					this.undoMove();
					possibleMoves[count] = candidates[i];
					count++;
				}
			} catch (PawnPromotion e) {
				this.undoMove();
				possibleMoves[count] = candidates[i];
				count++;
			}
		}
		
		if (count != 4)
			possibleMoves[count][0] = -1;
		
		return possibleMoves;
	}

}