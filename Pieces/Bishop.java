package Chess.Pieces;

import Chess.*;

/**
 * This class implements PieceInterface. It is created once for each Board.
 * It handles piece-specific move rules, and can generate possible moves.
 *
 * @author  Liam Marcassa
 */
public class Bishop implements PieceInterface {
	
	private Board b;
	
	/**
	 * Constructor
	 * 
	 * @param b  the Board that bishops are on
	 */
	public Bishop (Board b) {
		this.b = b;
	}
	
	/**
	 * Symbol to print to console. Is not used for identification by Board.
	 * 
	 * @return 'B'
	 */
	public char getChar() {
		return 'B';
	}
	
	/**
	 * Used to ensure a move makes mechanical sense. Bishops can only
	 * move diagonally, and not through any other pieces. Does not check
	 * for check.
	 * 
	 * @param current  the current piece
	 * @param next  where the piece wants to be
	 * @return true if move is valid, false if not
	 */
	public boolean validateMove (byte current, byte next) {
		int diffCol = ((56&next)>>3)-((56&current)>>3);
		int diffRow = (7&next)-(7&current);
		
		if (Math.abs(diffCol) != Math.abs(diffRow))
			return false;
		
		int vCol = (diffCol == 0) ? 0 : diffCol / Math.abs(diffCol);
		int vRow = (diffRow == 0) ? 0 : diffRow / Math.abs(diffRow);
		int x = ((56&current)>>3) + vCol;
		int y = (7&current) + vRow;
		while (x != ((56&next)>>3) || y != (7&next)) {
			if (b.board[x][y] != -128)
				return false;
			x += vCol;
			y += vRow;
		}
		
		// can capture, but not same colour
		if ((-128&current) == -128) {
			if (b.board[x][y] != -128 && b.board[x][y] < 16)
				return false;
		} else {
			if (b.board[x][y] > 15)
				return false;
		}
		
		return true;
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
		byte col = (byte)((current&56)>>3);
		byte row = (byte)(current&7);
		
		// maximum possible number of moves is 7+6=13
		byte[] candidates = new byte[14];
		int size = 0;
		
		int x = col+1;
		int y = row+1;
		
		if ((-128&current) == -128) { // piece is black
			// move up and to the right
			while (x < 8 && y < 8 && b.board[x][y] == -128)
				candidates[size++] = (byte) (-64&current | (x++)<<3 | y++);
			if (x < 8 && y < 8 && b.board[x][y] > 15)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
			
			// move down and to the right
			x = col+1;
			y = row-1;
			while (x < 8 && y >= 0 && b.board[x][y] == -128) 
				candidates[size++] = (byte) (-64&current | (x++)<<3 | y--);
			if (x < 8 && y >= 0 && b.board[x][y] > 15)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
			
			// move up and to the left	
			x = col-1;
			y = row+1;
			while (x >= 0 && y < 8 && b.board[x][y] == -128)
				candidates[size++] = (byte) (-64&current | (x--)<<3 | y++);
			if (x >= 0 && y < 8 && b.board[x][y] > 15)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
			
			// move down and to the left	
			x = col-1;
			y = row-1;
			while (x >= 0 && y >= 0 && b.board[x][y] == -128)
				candidates[size++] = (byte) (-64&current | (x--)<<3 | y--);
			if (x >= 0 && y >= 0 && b.board[x][y] > 15)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
		} else { // piece is white
			// move up and to the right
			while (x < 8 && y < 8 && b.board[x][y] == -128)
				candidates[size++] = (byte) (-64&current | (x++)<<3 | y++);
			if (x < 8 && y < 8 && b.board[x][y] < 16)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
			
			// move down and to the right
			x = col+1;
			y = row-1;
			while (x < 8 && y >= 0 && b.board[x][y] == -128) 
				candidates[size++] = (byte) (-64&current | (x++)<<3 | y--);
			if (x < 8 && y >= 0 && b.board[x][y] < 16)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
				
			// move up and to the left
			x = col-1;
			y = row+1;
			while (x >= 0 && y < 8 && b.board[x][y] == -128)
				candidates[size++] = (byte) (-64&current | (x--)<<3 | y++);
			if (x >= 0 && y < 8 && b.board[x][y] < 16)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
			
			// move down and to the left
			x = col-1;
			y = row-1;
			while (x >= 0 && y >= 0 && b.board[x][y] == -128)
				candidates[size++] = (byte) (-64&current | (x--)<<3 | y--);
			if (x >= 0 && y >= 0 && b.board[x][y] < 16)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
		}
		
		return candidates;
	}
}