package Chess.Players;

import Chess.*;

import java.util.Scanner;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Computer contains the AI components of this project, and implements
 * PlayerInterface.
 *
 * @author Liam Marcassa
 */
public class Computer implements PlayerInterface {
	private Scanner in;
	private Board b;
	private Colour colour, tc;   // colour = our colour, tc = their colour
	private char promoChar = 'N';
	private byte flags, flagsMask;
	private boolean simpleEval,opening,endgame;
	private int maxDepth;

	// These are large values, outside the range produced by the eval() function.
	private static final int WIN = 15000;
	private static final int STALE = -14000;

	// Standing for myLow, myHigh, theirLow, theirHigh. These are indices
	// relating to pieces[]. mLow and tLow will be {0,16}. These calculated
	// once instead of constantly checking colours or writing two nearly
	// identical classes.
	private int mLow, mHi, tLow, tHi;

	// make second evaluation function

	// Weakest pawns = F2, F7
	// 
	// 1 - control center
	// 2 - develop minor pieces (knights & bishops)
	// 3 - castle (king safety)
	// 4 - finish development (including queen & rooks)
	// 5 - attack, usually by pushing center pawns up
	// 
	// encourage good openings (discourage pawn f6)
	
	/**
	 * Constructor, initiallizes piece ranges
	 * 
	 * @param b  the board to play with
	 * @param colour  our piece colour (white or black)
	 */
	public Computer (Board b, Colour colour, Boolean simpleEval, int maxDepth) {
		this.b = b;
		this.colour = colour;
		this.simpleEval = simpleEval;
		this.maxDepth = maxDepth;

		if (colour == Colour.BLACK) {
			tc = Colour.WHITE;
			mLow = 0;
			tLow = 16;
			flagsMask = 13;
			// 00001101
		} else {
			tc = Colour.BLACK;
			mLow = 16;
			tLow = 0;
			flagsMask = 50;
			// 00110010
		}
		mHi = mLow+16;
		tHi = tLow+16;

		in = new Scanner(System.in);
	}

	/**
	 * Helper method
	 * 
	 * @return our colour
	 */
	public Colour getColour() {
		return colour;
	}
	
	/** Dispatch minimax, parse result, move */
	public void makeMove() {
		System.out.println("starting MiniMax");

		if (!simpleEval) {
			detectState();
			if (endgame) { maxDepth += 4; }
		}

		byte[] out = root();

		char currentChar = (char) (((56&out[0])>>3)+65);
		char nextChar = (char) (((56&out[1])>>3)+65);
		System.out.print(currentChar);
		System.out.print((char)((7&out[0])+49));
		System.out.print("-");
		System.out.print(nextChar);
		System.out.println((char)((7&out[1])+49));

		// handle end states so as not to confuse the user
		if (out[0] == -128) {
			b.gameOver = "X";
		} else if (b.boardMove(this,out[0],out[1]) &&
			!b.checkCheck(colour,out[0],out[1])) {
				b.undoMove();
		}
	}

	/**
	 * A Node is created for each vertex in the Minimax tree.
	 * There is no distinction made between a MIN node and a
	 * MAX node, it is all left to the implementation to handle 
	 * nodes correctly.
	 */
	public class Node {
		Node parent;
		int alpha,beta,value;
		boolean blackInCheck, whiteInCheck, firstVal;

		/** Root node constructor */
		public Node () {
			alpha = Integer.MIN_VALUE;
			beta = Integer.MAX_VALUE;
			getChecks();
			firstVal = true;
		}

		/** Child node constructor */
		public Node (Node p) {
			parent = p;
			alpha = p.alpha;
			beta = p.beta;
			getChecks();
			firstVal = true;
		}

		/** Check is used to determine end game states */
		public void getChecks () {
			blackInCheck = b.calcCheck(Colour.BLACK);
			whiteInCheck = b.calcCheck(Colour.WHITE);
		}

		/**
		 * Update alpha using a child node
		 * 
		 * @param n  a child node
		 */
		public void updateAlpha (Node n) {
			updateAlpha(n.value);
		}

		/**
		 * Update alpha using a beta value
		 * 
		 * @param b  a beta value
		 */
		public void updateAlpha (int v) {
			if (v > alpha) {
				alpha = v;
				value = v;
			} else if (firstVal) {
				value = v;
			}
			firstVal = false;
		}

		/**
		 * Update beta using a child node
		 * 
		 * @param n  a child node
		 */
		public void updateBeta (Node n) {
			updateBeta(n.value);
		}

		/**
		 * Update beta using an alpha value
		 * 
		 * @param a  an alpha value
		 */
		public void updateBeta (int v) {
			if (v < beta) {
				beta = v;
				value = v;
			} else if (firstVal) {
				value = v;
			}
			firstVal = false;
		}
	}

	/**
	 * Helper class: organized info and reduces code duplication
	 */
	public class Piece {
		byte current;
		byte[] nexts;
		int[] evals;
		public Piece (int i) {
			current = b.pieces[i];
			nexts = b.getPiece(i).getMoves(b.pieces[i]);
		}
	}

	/**
	 *
	 * Probably lags behind the states a little, but still decent
	 */
	private void detectState () {
		opening = true;
		endgame = false;
		int pieceCount = 0;
		int pawnCount = 0;

		if (opening) {
			// are we still in opening?
			for (int i = 0; i < 8; i++) {
				if ((64&b.pieces[i]) == 64) {
					// check black back rank
					if ((7&b.pieces[i]) == 7) {
						pieceCount += 1;
					}
				}
				if ((64&b.pieces[i+16]) == 64) {
					// check white back rank
					if ((7&b.pieces[i+16]) == 0) {
						pieceCount += 1;
					}
				}
				if ((64&b.pieces[i+8]) == 64) {
					// check black pawns
					if ((7&b.pieces[i+8]) == 6) {
						pawnCount += 1;
					}
				}
				if ((64&b.pieces[i+24]) == 64) {
					// check white pawns
					if ((7&b.pieces[i+24]) == 1) {
						pawnCount += 1;
					}
				}
			}

			if (pawnCount < 5 || pieceCount < 4) {
				// at least 4 pawns have moved, or only
				// the king and two rooks are on the back rank
				opening = false;
				pieceCount = 0;
			}
		}


		if (!opening) {
			// are we still in mid?
			for (int i = 0; i < 32; i++) {
				if ((64&b.pieces[i]) == 64) {
					pieceCount += 1;
				}
			}

			if (pieceCount < 7) { endgame = true; }
		}
	}

	/**
	 * The root node
	 * 
	 * @return byte[] of length two, corresponding to the current and desired location of 
	 * 		   a piece. Format is; [7-6]: unused, [5-3]: column, [2-0] row.
	 */
	private byte[] root () {
		Piece[] pieces = new Piece[16];
		Node node = new Node();

		flags = (byte) (flagsMask&CastleSync.getFlags());

		Dispatch d = new Dispatch();

		for (int i = 0; i < 16; i++) {
			if ((64&b.pieces[i+mLow]) == 64) {
				pieces[i] = new Piece(i+mLow);
				pieces[i].evals = new int[pieces[i].nexts.length];
				int a = 0;
				while (pieces[i].nexts[a] != 0) {
					pieces[i].evals[a] = oddDepth(node, pieces[i].current, pieces[i].nexts[a], d);
					if (pieces[i].evals[a] > (-100-STALE) && pieces[i].evals[a] <= (0-STALE)) // LIMIT DEPTH TO 100
						pieces[i].evals[a] = pieces[i].evals[a] * -1;						
					node.updateAlpha(pieces[i].evals[a]);
					a++;
				}
			}
		}

		// find the highest eval
		Random rand = new Random(ThreadLocalRandom.current().nextInt());
		byte[] out = new byte[2];
		int count = 0;
		outer:
		for (int i = 0; i < 16; i++) {
			int a = 0;
			while (pieces[i] != null && pieces[i].nexts[a] != 0) {
				if (pieces[i].evals[a] == node.alpha) {
					//out[0] = pieces[i].current;
					//out[1] = pieces[i].nexts[a];
					//break outer;
					count++;
					if (rand.nextDouble() < (1.0/count)) {
						out[0] = pieces[i].current;
						out[1] = pieces[i].nexts[a];
					}
				}
				a++;
			}
		}

		System.out.println(count);
		System.out.println(node.alpha);

		// catch "ignore" states? would they ever be the only option?
		if (node.alpha == WIN) {
			b.gameOver = "Computer wins!";
		} else if (node.alpha == (0-WIN)) {
			b.gameOver = "Human wins!";
		} else if (node.alpha == STALE) {
			b.gameOver = "Stalemate";
		}
		
		return out;
	}

	public class Dispatch {
		public int curDepth;

		public Dispatch () {
			curDepth = 0;
		}

		public boolean downOne () {
			curDepth += 1;
			return (curDepth >= maxDepth) ? true : false;
		}

		public void upOne () {
			curDepth -= 1;
		}

		public int lookAhead () {
			if ((curDepth%2) == 0) {
				return curDepth/2;
			} else {
				return (curDepth-1)/2;
			}
		}

		public int eval () {
			if (simpleEval) {
				return simpleEval();
			} else {
				return bigEval();
			}
		}
	}

	/**
	 * Catch end game states (win, loss, stalemate)
	 * 
	 * @param n  the current node 
	 * @return zero if non-end game state, WIN if win, 0-WIN if loss,
	 * 		   and STALE if stalemate
	 */
	private int checkMyMoves (Node n) {
		if (colour == Colour.BLACK) {
			if (n.blackInCheck) {
				// if all moves are here, is checkmate
				if (n.parent.blackInCheck) { return (0-WIN); }
				// illegal move, cannot put self in check
				return STALE;
			}
		} else {
			if (n.whiteInCheck) {
				// if all moves are here, is checkmate
				if (n.parent.whiteInCheck) { return (0-WIN); }
				// illegal move, cannot put self in check
				return STALE;
			}
		}
		return 0;
	}

	private int oddDepth (Node parent, byte current, byte next, Dispatch d) {
		if (b.boardMove(this,current,next)) {

			Piece[] pieces = new Piece[16];
			Node node = new Node(parent);

			int badMove = checkMyMoves(node);
			if (badMove != 0) {
				b.undoMove();
				return badMove + d.lookAhead();
			}

			if (d.downOne()) {
				int out = d.eval();
				b.undoMove();
				d.upOne();
				return out;
			}

			outer:
			for (int i = 0; i < 16; i++) {
				if ((64&b.pieces[i+tLow]) == 64) {
					pieces[i] = new Piece(i+tLow);
					int a = 0;
					while (pieces[i].nexts[a] != 0) {
						node.updateBeta(evenDepth(node, pieces[i].current, pieces[i].nexts[a], d));
						if (node.beta < node.alpha)
							break outer;
						a++;
					}
				}
			}

			b.undoMove();
			d.upOne();

			return node.value;
		} else {
			return Integer.MIN_VALUE; // ignore, tried to castle
		}
	}

	/**
	 * This method filters the opponents moves for end game states.
	 * It is the mirror of {@link #checkMyMoves(Node) checkMyMoves}.
	 * 
	 * @param n  the current node
	 * @return zero if non-end game state, WIN if opponent wins, 0-WIN if opponent loses,
	 * 		   and 0-STALE if stalemate.
	 */
	private int checkTheirMoves (Node n) {
		if (tc == Colour.BLACK) {
			if (n.blackInCheck) {
				// if all moves are here, we win
			 	if (n.parent.blackInCheck) { return WIN; }
			 	// illegal move, cannot put self in check
			 	return (0-STALE);
			}
		} else {
			if (n.whiteInCheck) {
				// if all moves are here, we win
				if (n.parent.whiteInCheck) { return WIN; }
				// illegal move, cannot put self in check
				return (0-STALE);
			}
		}
		return 0;
	}

	/**
	 * Attempts to move a piece from current to next, then dispatches
	 * and evaluates all child moves. The actual piece moved is unimportant
	 * to the method. Corresponds to depth two of the searh tree.
	 * 
	 * @param parent  the parent node (root).
	 * @param current  the current position of a piece.
	 * @param next  the next desired position of a piece.
	 * @return the utility of the move (from current to next).
	 */
	private int evenDepth (Node parent, byte current, byte next, Dispatch d) {
		if (b.boardMove(this,current,next)) {

			Piece[] pieces = new Piece[16];
			Node node = new Node(parent);

			int badMove = checkTheirMoves(node);
			if (badMove != 0) {
				b.undoMove();
				return badMove - d.lookAhead();
			}

			if (d.downOne()) {
				int out = d.eval();
				b.undoMove();
				d.upOne();
				return out;
			}

			outer:
			for (int i = 0; i < 16; i++) {
				if ((64&b.pieces[i+mLow]) == 64) {
					pieces[i] = new Piece(i+mLow);
					int a = 0;
					while (pieces[i].nexts[a] != 0) {
						node.updateAlpha(oddDepth(node, pieces[i].current, pieces[i].nexts[a], d));
						// should it be less than or equal to???
						if (node.beta < node.alpha)
							break outer;
						a++;
					}
				}
			}

			b.undoMove();
			d.upOne();

			return node.value;
		} else {
			return Integer.MAX_VALUE; // ignore, tried to castle
		}
	}
	
	/**
	 * The evaluation function for a board state. Emphasis on speed.
	 * @return int  the expected utility
	 */
	private int simpleEval () {
		int sum = 0;

		for (int i = mLow; i < mHi; i++)
			if ((64&b.pieces[i]) == 64)
				sum += pieceValue(i);
		for (int i = tLow; i < tHi; i++)
			if ((64&b.pieces[i]) == 64)
				sum -= pieceValue(i);

		if (flags == 0 && (flagsMask&CastleSync.getFlags()) != 0) {
			if ((56&b.pieces[mLow]) == 16 || (56&b.pieces[mLow]) == 48) {
				// we castled
				sum += 2;
			} else {
				// we moved rook/king but did not castle
				sum -= 2;
			}
		}

		return sum;
	}

	// NEED TO EXPERIMENT WITH WEIGHTS
	private int bigEval () {

		// keep recursing a little bit further in some cases?
		int out = 0;
		byte next;

		// piece totals
		int sum = 0;
		for (int i = mLow; i < mHi; i++)
			if ((64&b.pieces[i]) == 64)
				sum += pieceValue(i);
		for (int i = tLow; i < tHi; i++)
			if ((64&b.pieces[i]) == 64)
				sum -= pieceValue(i);

		if (opening) {
			// "attack" centre four squares
			int centreAttack = 0;
			for (int i = mLow; i < mHi; i++) {
				next = (byte) (-64&b.pieces[i] | 36); // E5
				if (b.validateMove(colour,b.pieces[i],next))
					centreAttack += 3;
				next = (byte) (-64&b.pieces[i] | 35); // E4
				if (b.validateMove(colour,b.pieces[i],next))
					centreAttack += 3;
				next = (byte) (-64&b.pieces[i] | 27); // D4
				if (b.validateMove(colour,b.pieces[i],next))
					centreAttack += 3;
				next = (byte) (-64&b.pieces[i] | 28); // D5
				if (b.validateMove(colour,b.pieces[i],next))
					centreAttack += 3;
			}

			// develop minor pieces
			int development = 0;
			for (int i = mLow+2; i < mHi-8; i++) {
				if ((64&b.pieces[i]) == 64) {
					next = (byte) (7&b.pieces[i]);
					if (next != 0 && next != 7) {
						development += 5;
					}
				}
			}

			// get king to safety
			int castled = 0;
			if (flags == 0 && (flagsMask&CastleSync.getFlags()) != 0) {
				if ((56&b.pieces[mLow]) == 16 || (56&b.pieces[mLow]) == 48) {
					// we castled
					castled += 3;
				} else {
					// we moved rook/king but did not castle
					castled -= 3;
				}
			}

			// encourage pawns to move off first row?
			// pawn structure ?
			// be careful with f2 / f7 ?
			out = sum + centreAttack + development + castled;
		}

		if (opening || !endgame) {
			// maintain kings safety (try not to get back-rank checked or present weak squares),
			// encourages the following pawn structures (shown if white king castled kingside):
			// 
			// 0 0 0
			// 0 0 1
			// 1 1 0
			// 
			// 0 0 1
			// 0 1 0
			// 1 0 0
			// 
			int protectKing = 0;
			if (colour == Colour.BLACK) {
				if (((56&b.pieces[mLow])>>3) > 4) {
					// kingside
					if (b.board[5][6] < mHi && b.board[5][6] > mLow+7 &&
						b.board[6][6] < mHi && b.board[6][6] > mLow+7 &&
						b.board[7][5] < mHi && b.board[7][5] > mLow+7) {
						protectKing += 4;
					} else if (b.board[5][6] < mHi && b.board[5][6] > mLow+7 &&
						b.board[6][5] < mHi && b.board[6][5] > mLow+7 &&
						b.board[7][4] < mHi && b.board[7][4] > mLow+7) {
						protectKing += 4;
					}
				} else if (((56&b.pieces[mLow])>>3) < 3) {
					// queenside
					if (b.board[0][5] < mHi && b.board[0][5] > mLow+7 &&
						b.board[1][6] < mHi && b.board[1][6] > mLow+7 &&
						b.board[2][6] < mHi && b.board[2][6] > mLow+7) {
						protectKing += 4;
					} else if (b.board[0][4] < mHi && b.board[0][4] > mLow+7 &&
						b.board[1][5] < mHi && b.board[1][5] > mLow+7 &&
						b.board[2][6] < mHi && b.board[2][6] > mLow+7) {
						protectKing += 4;
					}
				}
			} else {
				if (((56&b.pieces[mLow])>>3) > 4) {
					// kingside
					if (b.board[5][1] < mHi && b.board[5][1] > mLow+7 &&
						b.board[6][1] < mHi && b.board[6][1] > mLow+7 &&
						b.board[7][2] < mHi && b.board[7][2] > mLow+7) {
						protectKing += 4;
					} else if (b.board[5][1] < mHi && b.board[5][1] > mLow+7 &&
						b.board[6][2] < mHi && b.board[6][2] > mLow+7 &&
						b.board[7][3] < mHi && b.board[7][3] > mLow+7) {
						protectKing += 4;
					}
				} else if (((56&b.pieces[mLow])>>3) < 3) {
					// queenside
					if (b.board[0][2] < mHi && b.board[0][2] > mLow+7 &&
						b.board[1][1] < mHi && b.board[1][1] > mLow+7 &&
						b.board[2][1] < mHi && b.board[2][1] > mLow+7) {
						protectKing += 4;
					} else if (b.board[0][3] < mHi && b.board[0][3] > mLow+7 &&
						b.board[1][2] < mHi && b.board[1][2] > mLow+7 &&
						b.board[2][1] < mHi && b.board[2][1] > mLow+7) {
						protectKing += 4;
					}
				}
			}
			out += protectKing;
		}
	
		if (!endgame) {			
			// push pawns up (count number of pawns past center line)
			int pawnAggression = 0;
			if (colour == Colour.BLACK) {
				for (int i = mLow+8; i < mHi; i++) {
					if ((64&b.pieces[i]) == 64 && (7&b.pieces[i]) < 4) {
						pawnAggression += 1;
					}
				}
			} else {
				for (int i = mLow+8; i < mHi; i++) {
					if ((64&b.pieces[i]) == 64 && (7&b.pieces[i]) > 3) {
						pawnAggression += 1;
					}
				}
			}

			// find open / half-open files for rooks
			int open = 0;
			boolean openFile = true;
			next = b.pieces[mLow+2]; // rook #1
			if ((64&next) == 64) {
				int file = (56&next)>>3;
				for (int i = 0; i < 8; i++) {
					if (b.board[file][i] != -128 && (b.board[file][i] < mLow || b.board[file][i] >= mHi)) {
						openFile = false;
						break;
					}
				}
				if (openFile)
					open += 3;
			}
			openFile = true;
			next = b.pieces[mLow+3]; // rook #2
			if ((64&next) == 64) {
				int file = (56&next)>>3;
				for (int i = 0; i < 8; i++) {
					if (b.board[file][i] != -128 && (b.board[file][i] < mLow || b.board[file][i] >= mHi)) {
						openFile = false;
						break;
					}
				}
				if (openFile)
					open += 3;
			}

			// reward passed pawn (no enemy pawns in front on adjacent files)
			int passed = 0;
			int plusPawn = (colour == Colour.BLACK) ? -1 : 1;
			for (int i = mLow+8; i < mHi; i++) {
				openFile = true;
				next = b.pieces[i];
				if ((64&next) == 64) {
					// ignore en passant
					int file = ((56&next)>>3)-1;
					int rank = (7&next) + plusPawn;
					if (file >= 0) {
						while (rank >= 0 && rank < 8) {
							if (b.board[file][rank] >= tLow+8 && b.board[file][rank] < tHi) {
								openFile = false;
								break;
							}
							rank += plusPawn;
						}
					}
					if (openFile) {
						file += 2;
						rank = (7&next) + plusPawn;
						if (file < 8) {
							while (rank >= 0 && rank < 8) {
								if (b.board[file][rank] >= tLow+8 && b.board[file][rank] < tHi) {
									openFile = false;
									break;
								}
								rank += plusPawn;
							}
							if (openFile)
								passed += 2; // is this high enough??
						}
					}
				}
			}

			// identify opponents weaknesses and focus attack on them (count number of protectors vs attackers?)
			// attack f-file pawn?
			out += (sum + open + passed + pawnAggression);
		}

		if (endgame) {
			// looks farther ahead (in makeMove method)
			
			// get king to center
			int centerKing = 0;
			if ((56&b.pieces[mLow]) > 8 && (56&b.pieces[mLow]) < 48 && (7&b.pieces[mLow]) > 1 && (7&b.pieces[mLow]) < 6) {
				centerKing += 1;
			}
			
			// queen promotions (promote mine, prevent theirs) ? 
			out = sum + centerKing;
		}

		return out;
	}

	/**
	 * @param i  the piece index (@see Chess.Board)
	 * @return standard relative weight
	 */
	private int pieceValue (int i) {
		switch (b.pieceNames[i]) {
			case 'K':
				return 200;
			case 'Q':
				return 9;
			case 'R':
				return 5;
			case 'B':
				return 3;
			case 'N':
				return 3;
			case 'P':
				return 1;
		}
		return 0;
	}
	
	/**
	 * "Catch" pawn promotion, promote to queen or knight (these will cover all movement options possible).
	 * Both options will be explored (@see Chess.Pawn#getMoves(byte)).
	 * 
	 * @return a character code for the promoted pawn (either 'N' or 'Q').
	 */
	public char choosePawnPromo() {
		promoChar = (promoChar == 'N') ? 'Q' : 'N';
		return promoChar;
	}
}