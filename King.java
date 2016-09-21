package Chess;

public class King implements PieceInterface {
		
	public King () { }
	
	public char getChar() {
		return 'K';
	}
	
	public boolean move (Board b, byte current, byte newCol, byte newRow) {
		boolean canMove = false;
		
		int diffCol = ((current&56)>>3)-newCol;
		int diffRow = (current&7)-newRow;
		
		if (diffRow > 1 || diffRow < -1)
			return false;
		if (b.board[newCol][newRow] != -128 && (-128&b.pieces[b.board[newCol][newRow]]) == (-128&current))
			return false;
		
		byte col = (byte) ((current&56)>>3);
		byte row = (byte) (current&7);
		byte[] tempMove = b.tempMove.pieces;
		
		if ((-128&current) == -128) {
			if (diffCol < 2 || diffCol > -2) {
				if (b.board[newCol][newRow] == -128 || b.board[newCol][newRow] > 15)
					canMove = true;
			} else if (col == 4 && row == 7) {
				if (diffCol == 2 && b.board[0][7] == 2 && b.board[3][7] == -128 && b.board[2][7] == -128 && b.board[1][7] == -128) { // queenside
					if (!b.inCheck(current)) {
						OldPosition op1 = b.moveList;
						while (op1 != null) {
							if (op1.pieces[0] == 2)
								return false;
							op1 = op1.next;
						}
						tempMove[2] = -128;
						canMove = true;
					}
				} else if (diffCol == -2 && b.board[7][7] == 3 && b.board[5][7] == -128 && b.board[6][7] == -128) { // kingside
					if (!b.inCheck(current)) {
						OldPosition op2 = b.moveList;
						while (op2 != null) {
							if (op2.pieces[0] == 3)
								return false;
							op2 = op2.next;
						}
						tempMove[2] = -64;
						canMove = true;
					}
				}
			}
		} else {
			if (diffCol < 2 || diffCol > -2) {
				if (b.board[newCol][newRow] < 16)
					canMove = true;
			} else if (col == 4 && row == 0) {
				if (diffCol == 2 && b.board[0][0] == 18 && b.board[3][0] == -128 && b.board[2][0] == -128 && b.board[1][0] == -128) { // queenside
					if (!b.inCheck(current)) {
						OldPosition op3 = b.moveList;
						while (op3 != null) {
							if (op3.pieces[0] == 18)
								return false;
							op3 = op3.next;
						}
						tempMove[2] = -128;
						canMove = true;
					}
				} else if (diffCol == -2 && b.board[7][0] == 19 && b.board[5][0] == -128 && b.board[6][0] == -128) { // kingside
					if (!b.inCheck(current)) {
						OldPosition op4 = b.moveList;
						while (op4 != null) {
							if (op4.pieces[0] == 19)
								return false;
							op4 = op4.next;
						}
						tempMove[2] = -64;
						canMove = true;
					}
				}
			}
		}
		
		if (canMove) {
			tempMove[0] = b.board[(current&56)>>3][current&7];
			//if (tempMove[0] == -128)
				//System.out.println(current);
			//	return false;
			tempMove[1] = current;
			
			b.tempBoard((byte)((current&56)>>3), (byte)(current&7), newCol, newRow);
			
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

		return canMove;
	}
	
	public byte[] calcPossibleMoves (Board b, byte current) {
		byte[] candidates = new byte[8];
		
		byte col = (byte)((current&56)>>3);
		byte row = (byte)(current&7);
		byte count = 0;
		
		if (row+1 < 8) {
			if (move(b,current,col,(byte)(row+1))) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | col<<3 | row+1);
				count++;
			}
			if (col+1 < 8 && move(b,current,(byte)(col+1),(byte)(row+1))) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | (col+1)<<3 | row+1);
				count++;
			}
			if (col-1 >= 0  && move(b,current,(byte)(col-1),(byte)(row+1))) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | (col-1)<<3 | row+1);
				count++;
			}
		}
		
		if (row-1 >= 0) {
			if (move(b,current,col,(byte)(row-1))) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | col<<3 | row-1);
				count++;
			}
			if (col+1 < 8 && move(b,current,(byte)(col+1),(byte)(row-1))) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | (col+1)<<3 | row-1);
				count++;
			}
			if (col-1 >= 0  && move(b,current,(byte)(col-1),(byte)(row-1))) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | (col-1)<<3 | row-1);
				count++;
			}
		}
		
		if (col+1 < 8) {
			if (move(b,current,(byte)(col+1),row)) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | (col+1)<<3 | row);
				count++;
			}
			if (col+2 < 8 && move(b,current,(byte)(col+2),row)) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | (col+2)<<3 | row);
				count++;
			}
		}
		
		if (col-1 >= 0) {
			if (move(b,current,(byte)(col-1),row)) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | (col-1)<<3 | row);
				count++;
			}
			if (col-2 >= 0 && move(b,current,(byte)(col-2),row)) {
				b.undoTemp();
				candidates[count] = (byte) (-64&current | (col-2)<<3 | row);
				count++;
			}
		}

		return candidates;
	}
}