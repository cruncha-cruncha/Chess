package Chess;

// ALL OF THESE CLASSES NEED TO BE REVIEWED, PROBABLY COULD BE FASTER

public class King implements PieceInterface {
	
	private Board b;
		
	public King (Board b) {
		this.b = b;
	}
	
	public char getChar() {
		return 'K';
	}
	
	public boolean validateMove (byte current, byte next) {
		byte oldCol = (byte) ((56&current)>>3);
		byte oldRow = (byte) (7&current);
		byte newCol = (byte) ((56&next)>>3);
		byte newRow = (byte) (7&next);
		
		int diffCol = newCol-oldCol;
		int diffRow = newRow-oldRow;
		
		if (Math.abs(diffCol) > 2 || Math.abs(diffRow) > 1)
			return false;
		
		// ensure castle is valid
		if (diffCol == 2) {
			// kingside
			if (CastleSync.canCastle(next) && b.board[5][oldRow] == -128 && b.board[6][oldRow] == -128) {
				return true;
			} else {
				return false;
			}
		} else if (diffCol == -2) { 
			// queenside
			if (CastleSync.canCastle(next) && b.board[1][oldRow] == -128 && b.board[2][oldRow] == -128 && b.board[3][oldRow] == -128) {
				return true;
			} else {
				return false;
			}
		}
		
		byte[] n = {next,0};
		if (checkCollisions(current,n)[0] == 0)
			return false;
		return true;
	}
	
	private byte[] checkCollisions (byte current, byte[] candidates) {
		byte count = 0;
		byte i = 0;
		if ((-128&current) == -128) {
			while (candidates[i] != 0) {
				if (b.board[(56&candidates[i])>>3][7&candidates[i]] == -128 || b.board[(56&candidates[i])>>3][7&candidates[i]] > 15) {
					candidates[count++] = candidates[i];
				}
				i++;
			}
		} else {
			while (candidates[i] != 0) {
				if (b.board[(56&candidates[i])>>3][7&candidates[i]] < 16) {
					candidates[count++] = candidates[i];
				}
				i++;
			}
		}
		candidates[count] = 0;
		return candidates;
	}
	
	public byte[] getMoves (byte current) {
		byte[] candidates = new byte[11]; // one more than needed
		byte col = (byte)((current&56)>>3);
		byte row = (byte)(current&7);
		byte count = 0;
		
		// -57 = keep row
		// -8 = keep col
		
		if (col+1 < 8) {
			candidates[count++] = (byte) (-57&current | (col+1)<<3);
			if (row+1 < 8)
				candidates[count++] = (byte) (-64&current | (col+1)<<3 | row+1);
			if (row-1 >= 0)
				candidates[count++] = (byte) (-64&current | (col+1)<<3 | row-1);
			if (col+2 < 8 && CastleSync.canCastle((byte)(16+current))) {
				// kingside
				if (b.board[5][row] == -128 && b.board[6][row] == -128)
					candidates[count++] = (byte) (16+current);
			}
		}
		
		if (col-1 >= 0) {
			candidates[count++] = (byte) (-57&current | (col-1)<<3);
			if (row+1 < 8)
				candidates[count++] = (byte) (-64&current | (col-1)<<3 | row+1);
			if (row-1 >= 0)
				candidates[count++] = (byte) (-64&current | (col-1)<<3 | row-1);
			if (col-2 < 8 && CastleSync.canCastle((byte)(current-16))) {
				// queenside
				if (b.board[1][row] == -128 && b.board[2][row] == -128)
					candidates[count++] = (byte) (current-16);
			}
		}
		
		if (row+1 < 8)
			candidates[count++] = (byte) (-8&current | row+1);
		if (row-1 >= 0)
			candidates[count++] = (byte) (-8&current | row-1);

		return checkCollisions(current, candidates);
	}
}