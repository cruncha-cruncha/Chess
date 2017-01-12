package Chess.Pieces;

import Chess.*;

/**
 * This class implements PieceInterface. It is created once for each Board.
 * It handles piece-specific move rules, and can generate possible moves.
 * Knights are unique in that they can hop over other pieces.
 *
 * @author  Liam Marcassa
 */
public class Knight implements PieceInterface {
	
	private Board b;
	
	/**
	 * Constructor
	 * 
	 * @param b  the Board that knights are on
	 */
	public Knight (Board b) {
		this.b = b;
	}
	
	/**
	 * Symbol to print to console. Is not used for identification by Board.
	 * 'K' was already taken by King, and since king is more important,
	 * N was chosen for Knight.
	 * 
	 * @return 'N'
	 */
	public char getChar() {
		return 'N';
	}
	
	/**
	 * Used to ensure a move makes mechanical sense. Knights can only move
	 * two squares one way and one the other. Does not check for check.
	 * 
	 * @param current  the current piece
	 * @param next  where the piece wants to be
	 * @return true if move is valid, false if not
	 */
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
	
	/**
	 * Makes sure knight does not run into another piece during it's move.
	 * 
	 * @param current  the current piece
	 * @param candidates  possible next pieces
	 * @return a byte array of unkown length containing valid next positions. 
	 * 		   Not every element is a valid position; other code is expecting
	 * 		   this array to be filled starting from index zero and increasing.
	 * 		   Once an element of value zero is encountered, there are no more
	 * 		   valid moves; the rest of the array can be skipped.
	 */
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
	
	/**
	 * Returns every valid mechanical move possible from the current position.
	 * Does not check check. Used by Board to calculate check and by Computer
	 * to generate branches. This method generates the set of all possible
	 * moves which stay on the board, and then checks for collisions.
	 * 
	 * @param current  the current piece
	 * @return an array of valid moves, starting from index zero. Not all values are
	 *         moves: once a zero value has been reached, higher indices can be
	 *         assumed to be zero also. The last index is guaranteed to be zero.
	 */
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
			case 82:
				// cannot move way up or right
				candidates[0] = (byte) (-64&current | (col-1)<<3 | row-2);
				candidates[1] = (byte) (-64&current | (col-2)<<3 | row-1);
				candidates[2] = (byte) (-64&current | (col-2)<<3 | row+1);
				break;
			case 88:
				// cannote move up or way right
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row-2);
				candidates[1] = (byte) (-64&current | (col-1)<<3 | row-2);
				candidates[2] = (byte) (-64&current | (col-2)<<3 | row-1);
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
			case 73:
				// cannot move way right or down
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row+2);
				candidates[1] = (byte) (-64&current | (col-2)<<3 | row+1);
				candidates[2] = (byte) (-64&current | (col-1)<<3 | row+2);
				break;
			case 97:
				// cannot move right or way down
				candidates[0] = (byte) (-64&current | (col-2)<<3 | row-1);
				candidates[1] = (byte) (-64&current | (col-2)<<3 | row+1);
				candidates[2] = (byte) (-64&current | (col-1)<<3 | row+2);
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
			case 37:
				// cannot move way down or left
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row+2);
				candidates[1] = (byte) (-64&current | (col+2)<<3 | row+1);
				candidates[2] = (byte) (-64&current | (col+2)<<3 | row-1);
				break;
			case -123:
				// cannot move down or way left
				candidates[0] = (byte) (-64&current | (col+1)<<3 | row+2);
				candidates[1] = (byte) (-64&current | (col+2)<<3 | row+1);
				candidates[2] = (byte) (-64&current | (col-1)<<3 | row+2);
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
			case -108:
				// cannot move way left or up
				candidates[0] = (byte) (-64&current | (col+2)<<3 | row-1);
				candidates[1] = (byte) (-64&current | (col+1)<<3 | row-2);
				candidates[2] = (byte) (-64&current | (col-1)<<3 | row-2);
				break;
			case 22:
				// cannot move left or way up
				candidates[0] = (byte) (-64&current | (col+2)<<3 | row+1);
				candidates[1] = (byte) (-64&current | (col+2)<<3 | row-1);
				candidates[2] = (byte) (-64&current | (col+1)<<3 | row-2);
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