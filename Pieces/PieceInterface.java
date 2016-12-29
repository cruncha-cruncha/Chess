package Chess.Pieces;

/**
 * This is an interface which is implemented by all pieces. It ensures each child
 * class provides a character to display to console, piece-specific movement rules,
 * and generation of possible moves.
 *
 * @author  Liam Marcassa
 */
public interface PieceInterface {
	/**
	 * @return a character which can be used to identify the class.
	 * 		   Should be unique to avoid confusion.
	 */
	public char getChar ();

	/**
	 * @param current  the current piece (colour, in play status, and position).
	 * @param next  desired piece move.
	 * @return true if move makes mechanical sense (function should not check check).
	 */
	public boolean validateMove (byte current, byte next);

	/**
	 * @param current  the current piece (colour, in play status, and position).
	 * @return byte array of valid moves, starting from index zero. Not all values
	 *         are moves: once a zero value has been reached when iterating, higher
	 *         indices can be assumed to be zero also. The last index is guaranteed
	 *         to be zero.
	 */
	public byte[] getMoves (byte current);
}