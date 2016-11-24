package Chess;

// KINGSIDE AND QUEENSIDE ARE BACKWARDS

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
	
	/*
	King:
	can castle if Global.canCastle(colour,newCol);
	
	Board:
	if king was at 3,0 or 3,7, moves, and !Global.kingHasMoved(colour), then:
		set flag in OldPosition
		call Global.kingMoved(colour);
	if rook was at 0,0 or 7,0 or 0,7 or 7,7, moves, and !Global.rookHasMoved(colour,oldCol), then:
		set flag in OldPosition
		call Global.rookMoved(colour,oldCol);
	Reseting (in Board):
	if king and flag, then:
		call Global.resetKing(colour);
	if rook and flag, then:
		call Global.resetRook(colour,oldCol);
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