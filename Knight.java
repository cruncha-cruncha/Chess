package Chess;

public class Knight implements PieceInterface {
	
	private Board b;
	
	public Knight (Board b) {
		this.b = b;
	}
	
	public char getChar() {
		return 'N';
	}
	
	public boolean validateMove (byte current, byte next) {
		int diffCol = ((56&current)>>3)-((56&next)>>3);
		int diffRow = (7&current)-(7&next);
		
		if ((Math.abs(diffCol) != 1 || Math.abs(diffRow) != 2) &&
			(Math.abs(diffCol) != 2 || Math.abs(diffRow) != 1)) {
			return false;
		}
		
		byte[] n = {next,0};
		
		if (diffRow == 1 && diffCol == 2) {
			if (checkCollisions(current,n)[0] == 0)
				return false;
		} else if (diffRow == 2 && diffCol == 1) {
			if (checkCollisions(current,n)[0] == 0)
				return false;
		}
		
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
		byte[] candidates = new byte[9];
		byte col = (byte)((current&56)>>3);
		byte row = (byte)(current&7);
				
		byte state = 0;
		if (row+2 < 8) {
			state = (byte) (state | 1);
		} else if (row+1 < 8) {
			state = (byte) (state | 2);
		}
		if (col+2 < 8) {
			state = (byte) (state | 4);
		} else if (col+1 < 8) {
			state = (byte) (state | 8);
		}
		if (row-2 >= 0) {
			state = (byte) (state | 16);
		} else if (row-1 >= 0) {
			state = (byte) (state | 32);
		}
		if (col-2 >= 0) {
			state = (byte) (state | 64);
		} else if (col-1 >= 0) {
			state = (byte) (state | -128);
		}
		
		switch (state) {
			case 85:
				// all sides available
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row+2);
				candidates[1] = (byte) (-64&current | (col+2)<<3 | row+1);
				candidates[2] = (byte) (-64&current | (col+2)<<3 | row-1);
				candidates[3] = (byte) (-64&current | (col+1)<<3 | row-2);
				candidates[4] = (byte) (-64&current | (col-1)<<3 | row-2);
				candidates[5] = (byte) (-64&current | (col-2)<<3 | row-1);
				candidates[6] = (byte) (-64&current | (col-2)<<3 | row+1);
				candidates[7] = (byte) (-64&current | (col-1)<<3 | row+2);
				break;
			case 86:
				// cannot move way up
				candidates[0] = (byte) (-64&current | (col+2)<<3 | row+1);
				candidates[1] = (byte) (-64&current | (col+2)<<3 | row-1);
				candidates[2] = (byte) (-64&current | (col+1)<<3 | row-2);
				candidates[3] = (byte) (-64&current | (col-1)<<3 | row-2);
				candidates[4] = (byte) (-64&current | (col-2)<<3 | row-1);
				candidates[5] = (byte) (-64&current | (col-2)<<3 | row+1);
				break;
			case 84:
				// cannot move up
				candidates[0] = (byte) (-64&current | (col+2)<<3 | row-1);
				candidates[1] = (byte) (-64&current | (col+1)<<3 | row-2);
				candidates[2] = (byte) (-64&current | (col-1)<<3 | row-2);
				candidates[3] = (byte) (-64&current | (col-2)<<3 | row-1);
				break;
			case 90:
				// cannot move way up or way right
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row-2);
				candidates[1] = (byte) (-64&current | (col-1)<<3 | row-2);
				candidates[2] = (byte) (-64&current | (col-2)<<3 | row-1);
				candidates[3] = (byte) (-64&current | (col-2)<<3 | row+1);
				break;
			case 80:
				// cannot move up or right
				candidates[0] = (byte) (-64&current | (col-1)<<3 | row-2);
				candidates[1] = (byte) (-64&current | (col-2)<<3 | row-1);
				break;
			case 89:
				// cannot move way right
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row+2);
				candidates[1] = (byte) (-64&current | (col+1)<<3 | row-2);
				candidates[2] = (byte) (-64&current | (col-1)<<3 | row-2);
				candidates[3] = (byte) (-64&current | (col-2)<<3 | row-1);
				candidates[4] = (byte) (-64&current | (col-2)<<3 | row+1);
				candidates[5] = (byte) (-64&current | (col-1)<<3 | row+2);
				break;
			case 81:
				// cannot move right
				candidates[0] = (byte) (-64&current | (col-1)<<3 | row-2);
				candidates[1] = (byte) (-64&current | (col-2)<<3 | row-1);
				candidates[2] = (byte) (-64&current | (col-2)<<3 | row+1);
				candidates[3] = (byte) (-64&current | (col-1)<<3 | row+2);
				break;
			case 105:
				// cannot move way right or way down
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row+2);
				candidates[1] = (byte) (-64&current | (col-2)<<3 | row-1);
				candidates[2] = (byte) (-64&current | (col-2)<<3 | row+1);
				candidates[3] = (byte) (-64&current | (col-1)<<3 | row+2);
				break;
			case 65:
				// cannot move right or down
				candidates[0] = (byte) (-64&current | (col-2)<<3 | row+1);
				candidates[1] = (byte) (-64&current | (col-1)<<3 | row+2);
				break;
			case 101:
				// cannot move way down
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row+2);
				candidates[1] = (byte) (-64&current | (col+2)<<3 | row+1);
				candidates[2] = (byte) (-64&current | (col+2)<<3 | row-1);
				candidates[3] = (byte) (-64&current | (col-2)<<3 | row-1);
				candidates[4] = (byte) (-64&current | (col-2)<<3 | row+1);
				candidates[5] = (byte) (-64&current | (col-1)<<3 | row+2);
				break;
			case 69:
				// cannot move down
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row+2);
				candidates[1] = (byte) (-64&current | (col+2)<<3 | row+1);
				candidates[2] = (byte) (-64&current | (col-2)<<3 | row+1);
				candidates[3] = (byte) (-64&current | (col-1)<<3 | row+2);
				break;
			case -91:
				// cannot move way down or way left
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row+2);
				candidates[1] = (byte) (-64&current | (col+2)<<3 | row+1);
				candidates[2] = (byte) (-64&current | (col+2)<<3 | row-1);
				candidates[3] = (byte) (-64&current | (col-1)<<3 | row+2);
				break;
			case 5:
				// cannot move down or left
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row+2);
				candidates[1] = (byte) (-64&current | (col+2)<<3 | row+1);
				break;
			case -107:
				// cannot move way left
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row+2);
				candidates[1] = (byte) (-64&current | (col+2)<<3 | row+1);
				candidates[2] = (byte) (-64&current | (col+2)<<3 | row-1);
				candidates[3] = (byte) (-64&current | (col+1)<<3 | row-2);
				candidates[4] = (byte) (-64&current | (col-1)<<3 | row-2);
				candidates[5] = (byte) (-64&current | (col-1)<<3 | row+2);
				break;
			case 21:
				// cannot move left
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row+2);
				candidates[1] = (byte) (-64&current | (col+2)<<3 | row+1);
				candidates[2] = (byte) (-64&current | (col+2)<<3 | row-1);
				candidates[3] = (byte) (-64&current | (col+1)<<3 | row-2);
				break;
			case -106:
				// cannot move way up or way left
				candidates[0] = (byte) (-64&current | (col+2)<<3 | row+1);
				candidates[1] = (byte) (-64&current | (col+2)<<3 | row-1);
				candidates[2] = (byte) (-64&current | (col+1)<<3 | row-2);
				candidates[3] = (byte) (-64&current | (col-1)<<3 | row-2);
				break;
			case 20:
				// cannot move up or left
				candidates[0] = (byte) (-64&current | (col+2)<<3 | row-1);
				candidates[1] = (byte) (-64&current | (col+1)<<3 | row-2);
				break;
		}
		
		return checkCollisions(current,candidates);
	}
}