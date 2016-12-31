package Chess;

/**
 * This class keeps track of King and Rook movements. It does not ultimately decide
 * whether a king can castle, but provides a quick, early decision if pieces have
 * moved.
 *
 * @author  Liam Marcassa
 */
public class CastleSync {

	// bits:
	// 0 = black king has moved
	// 1 = white king has moved
	// 2 = black kingside rook has moved
	// 3 = black queenside rook has moved
	// 4 = white kingside rook has moved
	// 5 = white queenside rook has moved
	private static byte canCastle = 0;

	// timesMoved is not actually the number of moves a piece has made,
	// but the number of times it has moved away from its default location
	private static int[] timesMoved = new int[6]; 
	
	/**
	 * Increments an element of timesMoved and ensures canCastle bit is set.
	 * Called every time a rook or king moves away from their default starting
	 * square.
	 * 
	 * @param oldPos  is one of six default starting pieces
	 */
	public static void set (byte oldPos) {
		switch (oldPos) {
			case -25:
				// black king
				timesMoved[0] = timesMoved[0] + 1;
				canCastle = (byte) (1|canCastle);
				break;
			case 96:
				// white king
				timesMoved[1] = timesMoved[1] + 1;
				canCastle = (byte) (2|canCastle);
				break;
			case -1:
				// black kingside rook
				timesMoved[2] = timesMoved[2] + 1;
				canCastle = (byte) (4|canCastle);
				break;
			case -57:
				// black queenside rook
				timesMoved[3] = timesMoved[3] + 1;
				canCastle = (byte) (8|canCastle);
				break;
			case 120:
				// white kingside rook
				timesMoved[4] = timesMoved[4] + 1;
				canCastle = (byte) (16|canCastle);
				break;
			case 64:
				// white queenside rook
				timesMoved[5] = timesMoved[5] + 1;
				canCastle = (byte) (32|canCastle);
				break;
		}
	}
	
	/**
	 * Called by Board#undoMove(); decrements timesMoved and resets
	 * bit if timesMoves[x] == 0.
	 * 
	 * @param oldPos  one of six default starting pieces
	 */
	public static void reset (byte oldPos) {
		switch (oldPos) {
			case -25:
				// black king
				timesMoved[0] = timesMoved[0] - 1;
				if (timesMoved[0] == 0)
					canCastle = (byte) (1^canCastle);
				break;
			case 96:
				// white king
				timesMoved[1] = timesMoved[1] - 1;
				if (timesMoved[1] == 0)
					canCastle = (byte) (2^canCastle);
				break;
			case -1:
				// black kingside rook
				timesMoved[2] = timesMoved[2] - 1;
				if (timesMoved[2] == 0)
					canCastle = (byte) (4^canCastle);
				break;
			case -57:
				// black queenside rook
				timesMoved[3] = timesMoved[3] - 1;
				if (timesMoved[3] == 0)
					canCastle = (byte) (8^canCastle);
				break;
			case 120:
				// white kingside rook
				timesMoved[4] = timesMoved[4] - 1;
				if (timesMoved[4] == 0)
					canCastle = (byte) (16^canCastle);
				break;
			case 64:
				// white queenside rook
				timesMoved[5] = timesMoved[5] - 1;
				if (timesMoved[5] == 0)
					canCastle = (byte) (32^canCastle);
				break;
		}
	}

	/**
	 * Useful for debugging.
	 * 
	 * @return canCastle
	 */
	public static byte getFlags () {
		return canCastle;
	}
	
	/**
	 * Allows the program to quickly check if a king cannot
	 * castle in a certain direction (kingside or queenside).
	 * 
	 * @param
	 * @return false if either the king or rook has moved, true if not. True
	 * 		   does not imply the castling is valid, check still has to be checked.
	 */
	public static boolean canCastle (byte newKing) {
		switch (newKing) {
			case -9:
				// black kingside
				if ((5&canCastle) == 0)
					return true;
				break;
			case -41:
				// black queenside
				if ((9&canCastle) == 0)
					return true;
				break;
			case 112:
				// white kingside
				if ((18&canCastle) == 0)
					return true;
				break;
			case 80:
				// white queenside
				if ((34&canCastle) == 0)
					return true;
				break;
			default:
				return false;
		}
		return false;
	}
}