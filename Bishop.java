package Chess;

public class Bishop implements PieceInterface {
	
	public Bishop () { }
	
	public char getChar() {
		return 'B';
	}
	
	public boolean move (Board b, byte current, byte newCol, byte newRow) {
		int diffCol = ((current&56)>>3)-newCol;
		int diffRow = (current&7)-newRow;
		
		if (Math.abs(diffCol) != Math.abs(diffRow))
			return false;
		if (b.board[newCol][newRow] != -128 && (-128&b.pieces[b.board[newCol][newRow]]) == (-128&current))
			return false;
		
		byte colSign = (byte) Math.signum(diffCol);
		byte rowSign = (byte) Math.signum(diffRow);
		
		for (byte i = 1; i < Math.abs(diffCol); i++) {
			if (b.board[newCol+(colSign*i)][newRow+(rowSign*i)] != -128)
				return false;
		}
		
		byte[] tempMove = b.tempMove.pieces;
		tempMove[0] = b.board[(current&56)>>3][current&7];
		tempMove[1] = current;
		
		if (b.board[newCol][newRow] != -128)
			tempMove[2] = b.board[newCol][newRow];
		
		b.tempBoard((byte)((current&56)>>3), (byte)(current&7), newCol, newRow);
		
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
		byte[] candidates = new byte[13];
		
		byte col = (byte)((current&56)>>3);
		byte row = (byte)(current&7);
		byte count = 0;
		
		for (byte i = 1; i < 8; i++) {
			if (col+i < 8 && row+i < 8) {
				if (move(b,current,(byte)(col+i),(byte)(row+i))) {
					b.undoTemp();
					candidates[count] = (byte) (-64&current | (col+i)<<3 | row+i);
					count++;
					if (b.board[col+i][row+i] != -128)
						break;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		
		for (byte i = 1; i < 8; i++) {
			if (col+i < 8 && row-i >= 0) {
				if (move(b,current,(byte)(col+i),(byte)(row-i))) {
					b.undoTemp();
					candidates[count] = (byte) (-64&current | (col+i)<<3 | row-i);
					count++;
					if (b.board[col+i][row-i] != -128)
						break;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		
		for (byte i = 1; i < 8; i++) {
			if (col-i >= 0 && row+i < 8) {
				if (move(b,current,(byte)(col-i),(byte)(row+i))) {
					b.undoTemp();
					candidates[count] = (byte) (-64&current | (col-i)<<3 | row+i);
					count++;
					if (b.board[col-i][row+i] != -128)
						break;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		
		for (byte i = 1; i < 8; i++) {
			if (col-i >= 0 && row-i >= 0) {
				if (move(b,current,(byte)(col-i),(byte)(row-i))) {
					b.undoTemp();
					candidates[count] = (byte) (-64&current | (col-i)<<3 | row-i);
					count++;
					if (b.board[col-i][row-i] != -128)
						break;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		
		return candidates;
	}
}