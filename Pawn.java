package Chess;

public final class Pawn implements PieceInterface {
	private int startRow, enPassant;
	private boolean capturePassant;
	
	public Pawn () { }
	
	public char getChar() { 
		return 'P';
	}
	
	public boolean move (Board b, byte current, byte newCol, byte newRow) throws PawnPromotion {
		
		boolean canMove = false;
		
		byte oldCol = (byte) ((current&56)>>3);
		byte oldRow = (byte) (current&7);
		
		byte[] tempMove = b.tempMove.pieces;
		
		if ((-128&current) == -128) {
			// black pawn
			if (oldCol == newCol) {
				if (oldRow-newRow == 1 && b.board[oldCol][newRow] == -128) {
					tempMove[2] = 0;
					canMove = true;
				} else if (oldRow-newRow == 2 && oldRow == 6 && b.board[oldCol][newRow+1] == -128 && b.board[oldCol][newRow] == -128) {
					tempMove[2] = 0;
					canMove = true;
				}
			} else {
				if ((oldCol+1 == newCol || oldCol-1 == newCol) && oldRow-newRow == 1) {
					if (b.board[newCol][newRow] > 15) {
						tempMove[2] = b.board[newCol][newRow];
						canMove = true;
					} else if (oldRow == 3 && b.board[newCol][oldRow] != -128 && b.pieceNames[b.board[newCol][oldRow]] == 'P') {
						byte[] enPass = b.moveList.pieces;
						if (enPass != null && enPass[0] == b.board[newCol][oldRow]) {
							tempMove[2] = b.board[newCol][oldRow];
							canMove = true;
						}
					}
				}
			}
		} else {
			// white pawn
			if (oldCol == newCol) {
				if (newRow-oldRow == 1 && b.board[oldCol][newRow] == -128) {
					tempMove[2] = 0;
					canMove = true;
				} else if (newRow-oldRow == 2 && oldRow == 1 && b.board[oldCol][oldRow+1] == -128 && b.board[oldCol][newRow] == -128) {
					tempMove[2] = 0;
					canMove = true;
				}
			} else {
				if ((oldCol+1 == newCol || oldCol-1 == newCol) && newRow-oldRow == 1) {
					if (b.board[newCol][newRow] >= 0 && b.board[newCol][newRow] < 16) {
						tempMove[2] = b.board[newCol][newRow];
						canMove = true;
					} else if (oldRow == 4 && b.board[newCol][oldRow] != -128 && b.pieceNames[b.board[newCol][oldRow]] == 'P') {
						byte[] enPass = b.moveList.pieces;
						if (enPass != null && enPass[0] == b.board[newCol][oldRow]) {
							tempMove[2] = b.board[newCol][oldRow];
							canMove = true;
						}
					}
				}
			}
		}
		
		if (canMove) {
			tempMove[0] = b.board[oldCol][oldRow];
			tempMove[1] = current;
			
			b.tempBoard(oldCol, oldRow, newCol, newRow);
			
			if (b.inCheck(current)) {
				if (b.calcCheck(current)) {
					b.undoTemp();
					canMove = false;
				}
			} else {
				if (b.calcCheck(current)) {
					b.undoTemp();
					b.noCheck(current);
					canMove = false;
				}
			}
		}
		
		if (canMove)
			if (newRow == 0 || newRow == 7)
				throw new PawnPromotion();
		
		return canMove;
	}
	
	public byte[] calcPossibleMoves (Board b, byte current) throws PawnPromotion {
		byte[] candidates = new byte[4];
		
		byte col = (byte)((current&56)>>3);
		byte row = (byte)(current&7);
		byte count = 0;
		
		if ((-128&current) == -128) {
			if (move(b,current,col,(byte)(row-1))) {
				b.undoTemp();
				candidates[count] = (byte) (-64 | col<<3 | row-1);
				count++;
			}
			if (row == 6 && move(b,current,col,(byte)(row-2))) {
				b.undoTemp();
				candidates[count] = (byte) (-64 | col<<3 | row-2);
				count++;
			}
			if (col < 7 && move(b,current,(byte)(col+1),(byte)(row-1))) {
				b.undoTemp();
				candidates[count] = (byte) (-64 | (col+1)<<3 | row-1); 
				count++;
			}
			if (col > 0 && move(b,current,(byte)(col-1),(byte)(row-1))) {
				b.undoTemp();
				candidates[count] = (byte) (-64 | (col-1)<<3 | row-1);
			}
		} else {
			if (move(b,current,col,(byte)(row+1))) {
				b.undoTemp();
				candidates[count] = (byte) (64 | col<<3 | row+1);
				count++;
			}
			if (row == 1 && move(b,current,col,(byte)(row+2))) {
				b.undoTemp();
				candidates[count] = (byte) (64 | col<<3 | row+2);
				count++;
			}
			if (col < 7 && move(b,current,(byte)(col+1),(byte)(row-1))) {
				b.undoTemp();
				candidates[count] = (byte) (64 | (col+1)<<3 | row-1);
				count++;
			}
			if (col > 0 && move(b,current,(byte)(col-1),(byte)(row-1))) {
				b.undoTemp();
				candidates[count] = (byte) (64 | (col-1)<<3 | row-1);
			}
		}
		
		return candidates;
	}

}