package Chess;

public class Knight implements PieceInterface {
	
	public Knight () { }
	
	public char getChar() {
		return 'N';
	}
	
	public boolean move (Board b, byte current, byte newCol, byte newRow) {
		int diffCol = Math.abs(((current&56)>>3)-newCol);
		int diffRow = Math.abs((current&7)-newRow);
		
		if (diffRow == 1 && diffCol == 2) {
			return canMove(b,current,newRow,newCol);
		} else if (diffRow == 2 && diffCol == 1) {
			return canMove(b,current,newCol,newRow);
		}
		
		return false;
	}
	
	private boolean canMove (Board b, byte current, byte newCol, byte newRow) {
		byte[] tempMove = b.tempMove.pieces;
		
		if (b.board[newCol][newRow] != -128) {
				//if (b.board[newCol][newRow] != -128 && ((b.board[newCol][newRow] < 16 && (-128&current) == 1) || (b.board[newCol][newRow] > 15 && (-128&current) == 0))) {
				if (b.board[newCol][newRow] != -128 && (-128&b.pieces[b.board[newCol][newRow]]) == (-128&current)) {
					return false;
				} else {
					tempMove[2] = b.board[newCol][newRow];
				}
		} else {
			tempMove[2] = 0;
		}
		
		byte col = (byte) ((current&56)>>3);
		byte row = (byte) (current&7);
		
		tempMove[0] = b.board[col][row];
		tempMove[1] = current;
		
		b.tempBoard(col, row, newCol, newRow);
		
		if (b.inCheck(current)) {
			if (b.calcCheck(current)) {
				b.undoTemp();
				return false;
			}
		} else {
			if (b.calcCheck(current)) {
				b.undoTemp();
				b.noCheck(current);
				return false;
			}
		}
		
		return true;
	}
	
	public byte[] calcPossibleMoves (Board b, byte current) {		
		byte[] candidates = new byte[8];
		
		byte col = (byte)((current&56)>>3);
		byte row = (byte)(current&7);
		byte count = 0;
		
		if (col+1 < 8) {
			if (row+2 < 8 && canMove(b,current,(byte)(col+1),(byte)(row+2))) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | (col+1)<<3 | row+2);
				count++;
			}
			if (row-2 >= 0 && canMove(b,current,(byte)(col+1),(byte)(row-2))) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | (col+1)<<3 | row-2);
				count++;
			}
			if (col+2 < 8) {
				if (row+1 < 8 && canMove(b,current,(byte)(col+2),(byte)(row+1))) {
					b.undoTemp();
					candidates[count] = (byte) (-64&current | (col+2)<<3 | row+1);
					count++;
				}
				if (row-1 >= 0 && canMove(b,current,(byte)(col+2),(byte)(row-1))) {
					b.undoTemp();
					candidates[count] = (byte) (-64&current | (col+2)<<3 | row-1);
					count++;
				}
			}
		}
		
		if (col-1 >= 0) {
			if (row+2 < 8 && canMove(b,current,(byte)(col-1),(byte)(row+2))) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | (col-1)<<3 | row+2);
				count++;
			}
			if (row-2 >= 0 && canMove(b,current,(byte)(col-1),(byte)(row-2))) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | (col-1)<<3 | row-2);
				count++;
			}
			if (col-2 >= 0) {
				if (row+1 < 8 && canMove(b,current,(byte)(col-2),(byte)(row+1))) {
					b.undoTemp();
					candidates[count] = (byte) (-64&current | (col-2)<<3 | row+1);
					count++;
				}
				if (row-1 >= 0 && canMove(b,current,(byte)(col-2),(byte)(row-1))) {
					b.undoTemp();
					candidates[count] = (byte) (-64&current | (col-2)<<3 | row-1);
					count++;
				}
			}
		}
		
		return candidates;
	}
}