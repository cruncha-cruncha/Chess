package Chess;

public final class Pawn implements PieceInterface {
	private int startRow, enPassant;
	private boolean capturePassant;
	private Board b;
	
	public Pawn (Board b) { 
		this.b = b;
	}
	
	public char getChar() { 
		return 'P';
	}
	
	public boolean validateMove (byte current, byte next) {
		byte oldCol = (byte) ((56&current)>>3);
		byte oldRow = (byte) (7&current);
		byte newCol = (byte) ((56&next)>>3);
		byte newRow = (byte) (7&next);
		
		int diffCol = newCol-oldCol;
		int diffRow = newRow-oldRow;
		byte oldPawn;
		
		if (Math.abs(diffCol) == 1 && diffRow == -1 && (-128&current) == -128) {
			if (b.board[newCol][newRow] > 15) {
				return true;
			} else if (newRow == 2 && b.board[newCol][oldRow] > 15) {
				// check en passant
				oldPawn = (byte) (65 | newCol<<3);
				if (b.pieceNames[b.board[newCol][oldRow]] == 'P' && b.board[newCol][oldRow] == b.moveHistory.pieces[1] && b.moveHistory.pieces[2] == oldPawn) 
					return true;
			}
		} else if (Math.abs(diffCol) == 1 && diffRow == 1 && (-128&current) == 0) {
			if (b.board[newCol][newRow] != -128 && b.board[newCol][newRow] < 16) {
				return true;
			} else if (newRow == 5 && b.board[newCol][oldRow] != -128 && b.board[newCol][oldRow] < 16) {
				// check en passant
				oldPawn = (byte) (-58 | newCol<<3);
				if (b.pieceNames[b.board[newCol][oldRow]] == 'P' && b.board[newCol][oldRow] == b.moveHistory.pieces[1] && b.moveHistory.pieces[2] == oldPawn)
					return true;
			}
		} else if (diffCol == 0) {
			if (diffRow == -2) {
				if ((-128&current) == -128 && oldRow == 6 && b.board[oldCol][5] == -128 && b.board[newCol][4] == -128)
					return true;
			} else if (diffRow == -1) {
				if ((-128&current) == -128 && b.board[newCol][newRow] == -128)
					return true;
			} else if (diffRow == 1) {
				if ((-128&current) == 0 && b.board[newCol][newRow] == -128)
					return true;
			} else if (diffRow == 2) {
				if ((-128&current) == 0 && oldRow == 1 && b.board[oldCol][2] == -128 && b.board[newCol][3] == -128)
					return true;
			}
		}

		return false;
	}
	
	public byte[] getMoves (byte current) {
		byte[] candidates = new byte[5];
		byte col = (byte)((current&56)>>3);
		byte row = (byte)(current&7);
		int size = 0;
		
		// -57 = keep row
		// -8 = keep col
		
		if ((-128&current) == -128) {
			if (b.board[col][row-1] == -128) {
				candidates[size++] = (byte) (-8&current | row-1);
				if (row == 6 && b.board[col][4] == -128) {
					candidates[size++] = (byte) (-8&current | 4);
				} else if (row == 1) {
					// pawn gets promoted to either a queen or a knight
					candidates[size++] = (byte) (-8&current | row-1);
				}
			}
			if (col-1 >= 0) {
				if (b.board[col-1][row-1] > 15) {
					candidates[size++] = (byte) (-64&current | (col-1)<<3 | row-1);
				} else if (row == 3 && b.board[col-1][3] > 23 && b.board[col-1][3] == b.moveHistory.pieces[1]) {
					byte oldPawn = (byte) (65 | (col-1)<<3);
					if (b.moveHistory.pieces[2] == oldPawn)
						candidates[size++] = (byte) (-62 | (col-1)<<3);
				}
			}
			if (col+1 < 8) {
				if (b.board[col+1][row-1] > 15) {
					candidates[size++] = (byte) (-64&current | (col+1)<<3 | row-1);
				} else if (row == 3 && b.board[col+1][3] > 23 && b.board[col+1][3] == b.moveHistory.pieces[1]) {
					byte oldPawn = (byte) (65 | (col+1)<<3);
					if (b.moveHistory.pieces[2] == oldPawn)
						candidates[size++] = (byte) (-62 | (col+1)<<3);
				}
			}
		} else {
			if (b.board[col][row+1] == -128) {
				candidates[size++] = (byte) (-8&current | row+1);
				if (row == 1 && b.board[col][3] == -128) {
					candidates[size++] = (byte) (-8&current | 3);
				} else if (row == 6) {
					// pawn gets promoted to either a queen or a knight
					candidates[size++] = (byte) (-8&current | row+1);
				}
			}
			if (col-1 >= 0) {
				if (b.board[col-1][row+1] != -128 && b.board[col-1][row+1] < 16) {
					candidates[size++] = (byte) (-64&current | (col-1)<<3 | row+1);
				} else if (row == 4 && b.board[col-1][4] > 7 && b.board[col-1][4] < 16 && b.board[col-1][4] == b.moveHistory.pieces[1]) {
					byte oldPawn = (byte) (-58 | (col-1)<<3);
					if (b.moveHistory.pieces[2] == oldPawn)
						candidates[size++] = (byte) (69 | (col-1)<<3);
				}
			}
			if (col+1 < 8) {
				if (b.board[col+1][row+1] != -128 && b.board[col+1][row+1] < 16) {
					candidates[size++] = (byte) (-64&current | (col+1)<<3 | row+1);
				} else if (row == 4 && b.board[col+1][4] > 7 && b.board[col+1][4] < 16 && b.board[col+1][4] == b.moveHistory.pieces[1]) {
					byte oldPawn = (byte) (-58 | (col+1)<<3);
					if (b.moveHistory.pieces[2] == oldPawn)
						candidates[size++] = (byte) (69 | (col+1)<<3);
				}
			}
		}

		return candidates;
	}

}