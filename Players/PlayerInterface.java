package Chess.Players;

import Chess.*;

/**
 * Interface from which all players must inherit. This class
 * makes it easier for the Board and Referee to handle turns 
 * and play.
 * 
 * @author  Liam Marcassa
 */
public interface PlayerInterface {
	/**
	 * Called by the Referee, it must have knowledge of
	 * the Board in order to work.
	 */
	public void makeMove();

	/**
	 * The Board will recognize when a pawn has reached a back rank,
	 * and call this method. 
	 * 
	 * @return a character corresponding to the desired piece promotion
	 *         promotion code (Q,B,R,N).
	 */
	public char choosePawnPromo();

	/**
	 * The player's piece colour, essential when determining if
	 * a move is valid or not.
	 * 
	 * @return the colour.
	 */
	public Colour getColour();
}