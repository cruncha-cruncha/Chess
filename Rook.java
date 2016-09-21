package Chess;

public class Rook implements PieceInterface {

	public Rook () { }
	
	public char getChar() {
		return 'R';
	}
	
	public boolean move (Board b, byte current, byte newCol, byte newRow) {
		int diffCol = ((current&56)>>3)-newCol;
		int diffRow = (current&7)-newRow;
		
		if (diffCol != 0 && diffRow != 0)
			return false;
		if (b.board[newCol][newRow] != -128 && (-128&b.pieces[b.board[newCol][newRow]]) == (-128&current))
			return false;

		byte colSign = (byte) Math.signum(diffCol);
		byte rowSign = (byte) Math.signum(diffRow);
		
		for (int i = 1; i < Math.abs(diffCol); i++) {
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
		byte[] candidates = new byte[14];
		
		byte col = (byte)((current&56)>>3);
		byte row = (byte)(current&7);
		byte count = 0;
		
		for (byte i = 1; i < 8; i++) {
			if (col+i < 8) {
				if (move(b,current,(byte)(col+i),row)) {
					b.undoTemp();
					candidates[count] = (byte) (-64&current | (col+i)<<3 | row);
					count++;
					if (b.board[col+i][row] != -128)
						break;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		
		for (byte i = 1; i < 8; i++) {
			if (row+i < 8) {
				if (move(b,current,col,(byte)(row+i))) {
					b.undoTemp();
					candidates[count] = (byte) (-64&current | col<<3 | row+i);
					count++;
					if (b.board[col][row+i] != -128)
						break;
				} else {
					break;
				}	
			} else {
				break;
			}
		}
		
		for (byte i = 1; i < 8; i++) {
			if (col-i >= 0) {
				if (move(b,current,(byte)(col-i),row)) {
					b.undoTemp();
					candidates[count] = (byte) (-64&current | (col-i)<<3 | row);
					count++;
					if (b.board[col-i][row] != -128)
						break;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		
		for (byte i = 1; i < 8; i++) {
			if (row-i >= 0) {
				if (move(b,current,col,(byte)(row-i))) {
					b.undoTemp();
					candidates[count] = (byte) (-64&current | col<<3 | row-1);
					count++;
					if (b.board[col][row-i] != -128)
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