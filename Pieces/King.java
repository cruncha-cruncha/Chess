package Chess.Pieces;

import Chess.*;

/**
 * This class implements PieceInterface. It is created once for each Board.
 * It handles piece-specific move rules, and can generate possible moves.
 *
 * @author  Liam Marcassa
 */
public class King implements PieceInterface {
	
	private Board b;
		
	/**
	 * Constructor
	 * 
	 * @param b  the Board that kings are on
	 */
	public King (Board b) {
		this.b = b;
	}
	
	/**
	 * Symbol to print to console. Is not used for identification by Board.
	 * 
	 * @return 'K'
	 */
	public char getChar() {
		return 'K';
	}
	
	/**
	 * Used to ensure a move makes mechanical sense. Kings can only move one
	 * square at a time (unless they are castling, which is mostly handled by Board).
	 * The next square must be open or capturable. Does not check for check.
	 * 
	 * @param current  the current piece
	 * @param next  where the piece wants to be
	 * @return true if move is valid, false if not
	 */
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
	
	/**
	 * Makes sure the king does not run into another piece during it's move.
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
	 * to generate branches.
	 * 
	 * @param current  the current piece
	 * @return an array of valid moves, starting from index zero. Not all values are
	 *         moves: once a zero value has been reached, higher indices can be
	 *         assumed to be zero also. The last index is guaranteed to be zero.
	 */
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
				if (b.board[5][row] == -128)
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
				if (b.board[1][row] == -128 && b.board[3][row] == -128)
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