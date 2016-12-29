package Chess.Players;

import Chess.*;

import java.util.Scanner;
import java.util.Arrays;

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

	// These are large values, outside the range produced by the eval() function.
	private static final int WIN = 15000;
	private static final int STALE = -14000;

	// Standing for myLow, myHigh, theirLow, theirHigh. These are indices
	// relating to pieces[]. mLow and tLow will be {0,16}. These calculated
	// once instead of constantly checking colours or writing two nearly
	// identical classes.
	private int mLow, mHi, tLow, tHi;
	
	/**
	 * Constructor, initiallizes piece ranges
	 * 
	 * @param b  the board to play with
	 * @param colour  our piece colour (white or black)
	 */
	public Computer (Board b, Colour colour) {
		this.b = b;
		this.colour = colour;
		tc = (colour == Colour.BLACK) ? Colour.WHITE : Colour.BLACK;
		in = new Scanner(System.in);
		mLow = (colour == Colour.BLACK) ? 0 : 16;
		mHi = mLow+16;
		tLow = (colour == Colour.BLACK) ? 16 : 0;
		tHi = tLow+16;
	}

	/**
	 * Helper method
	 * 
	 * @return our colour
	 */
	public Colour getColour() {
		return colour;
	}
	
	/** Calls getMove(), then ensures move does not put Human in check. */
	public void makeMove() {
		String input;
		for ( ; ; ) {
			byte[] aMove = getMove();
			if (b.boardMove(this,colour,aMove[0],aMove[1]) &&
				b.checkCheck(colour,aMove[0],aMove[1])) {
				break;
			}
			System.out.println(" invalid move");
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
		int alpha,beta;
		boolean blackInCheck, whiteInCheck;

		/** Root node constructor */
		public Node () {
			alpha = Integer.MIN_VALUE;
			beta = Integer.MAX_VALUE;
			getChecks();
		}

		/** Child node constructor */
		public Node (Node p) {
			parent = p;
			alpha = p.alpha;
			beta = p.beta;
			getChecks();
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
			if (n.beta > alpha)
				alpha = n.beta;
		}

		/**
		 * Update alpha using a beta value
		 * 
		 * @param b  a beta value
		 */
		public void updateAlpha (int b) {
			if (b > alpha)
				alpha = b;
		}

		/**
		 * Update beta using a child node
		 * 
		 * @param n  a child node
		 */
		public void updateBeta (Node n) {
			if (n.alpha < beta)
				beta = n.alpha;
		}

		/**
		 * Update beta using an alpha value
		 * 
		 * @param a  an alpha value
		 */
		public void updateBeta (int a) {
			if (a < beta)
				beta = a;
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
	 * The root node
	 * 
	 * @return byte[] of length two, corresponding to the current and desired location of 
	 * 		   a piece. Format is; [7-6]: unused, [5-3]: column, [2-0] row.
	 */
	private byte[] root () {
		Piece[] pieces = new Piece[16];
		Node node = new Node();

		for (int i = 0; i < 16; i++) {
			if ((64&b.pieces[i+mLow]) == 64) {
				pieces[i] = new Piece(i+mLow);
				pieces[i].evals = new int[pieces[i].nexts.length];
				int a = 0;
				while (pieces[i].nexts[a] != 0) {
					pieces[i].evals[a] = depthOne(node, pieces[i].current, pieces[i].nexts[a]);
					node.updateAlpha(pieces[i].evals[a]);
					a++;
				}
			}
		}

		// find the highest eval
		byte[] out = new byte[2];
		outer:
		for (int i = 0; i < 16; i++) {
			int a = 0;
			while (pieces[i] != null && pieces[i].nexts[a] != 0) {
				if (pieces[i].evals[a] == node.alpha) {
					out[0] = pieces[i].current;
					out[1] = pieces[i].nexts[a];
					break outer;
				}
				a++;
			}
		}

		// IF BEST MOVE = LOSE, WE HAVE LOST
		// IF BEST MOVE = STALE, WE HAVE STALEMATED
		// IF BEST MOVE = WIN, WE HAVE WON
		// SET b.gameOver (a String) to corresponding text

		return out;
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
			if (n.blackInCheck && n.parent.blackInCheck) {
				// if all moves are here, is checkmate
				return (0-WIN);
			} else if (n.whiteInCheck && n.parent.whiteInCheck) {
				// we win!!
				return WIN;
			} else if (n.parent.blackInCheck) {
				// if all moves are here, is stale
				return STALE;
			}
		} else {
			if (n.whiteInCheck && n.parent.whiteInCheck) {
				// if all moves are here, is checkmate
				return (0-WIN);
			} else if (n.blackInCheck && n.parent.blackInCheck) {
				// we win!!
				return WIN;
			} else if (n.parent.whiteInCheck) {
				// if all moves are here, is stale
				return STALE;
			}
		}
		return 0;
	}

	/**
	 * Attempts to move a piece from current to next, then dispatches
	 * and evaluates all child moves. The actual piece moved is unimportant
	 * to the method. Corresponds to depth one of the search tree.
	 * 
	 * @param parent  the parent node (root).
	 * @param current  the current position of a piece.
	 * @param next  the next desired position of a piece.
	 * @return the utility of the move (from current to next).
	 */
	private int depthOne (Node parent, byte current, byte next) {
		if (b.boardMove(this,colour,current,next)) {

			Piece[] pieces = new Piece[16];
			Node node = new Node(parent);

			int badMoves = checkMyMoves(node);
			if (badMoves != 0) {
				b.undoMove();
				return badMoves;
			}
			
			outer:
			for (int i = 0; i < 16; i++) {
				if ((64&b.pieces[i+tLow]) == 64) {
					pieces[i] = new Piece(i+tLow);
					int a = 0;
					while (pieces[i].nexts[a] != 0) {
						node.updateBeta(depthTwo(node, pieces[i].current, pieces[i].nexts[a]));
						if (node.beta <= node.alpha)
							break outer;
						a++;
					}
				}
			}

			b.undoMove();

			return node.beta;
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
			if (n.blackInCheck && n.parent.blackInCheck) {
				// if all moves are here, is checkmate
				return WIN;
			} else if (n.whiteInCheck && n.parent.whiteInCheck) {
				// we win!!
				return (0-WIN);
			} else if (n.parent.blackInCheck) {
				// if all moves are here, is stale
				return (0-STALE);
			}
		} else {
			if (n.whiteInCheck && n.parent.whiteInCheck) {
				// if all moves are here, is checkmate
				return WIN;
			} else if (n.blackInCheck && n.parent.blackInCheck) {
				// we win!!
				return (0-WIN);
			} else if (n.parent.whiteInCheck) {
				// if all moves are here, is stale
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
	private int depthTwo (Node parent, byte current, byte next) {
		if (b.boardMove(this,tc,current,next)) {

			Piece[] pieces = new Piece[16];
			Node node = new Node(parent);

			int badMoves = checkTheirMoves(node);
			if (badMoves != 0) {
				b.undoMove();
				return badMoves;
			}

			outer:
			for (int i = 0; i < 16; i++) {
				if ((64&b.pieces[i+mLow]) == 64) {
					pieces[i] = new Piece(i+mLow);

					int a = 0;
					while (pieces[i].nexts[a] != 0) {
						node.updateAlpha(depthThree(node, pieces[i].current, pieces[i].nexts[a]));
						if (node.beta <= node.alpha)
							break outer;
						a++;
					}
				}
			}

			b.undoMove();

			return node.alpha;
		} else {
			return Integer.MAX_VALUE; // ignore, tried to castle
		}
	}

	/**
	 * Attempts to move a piece from current to next, then dispatches
	 * and evaluates all child moves. The actual piece moved is unimportant
	 * to the method. Corresponds to depth three of the searh tree.
	 * 
	 * @param parent  the parent node (root).
	 * @param current  the current position of a piece.
	 * @param next  the next desired position of a piece.
	 * @return the utility of the move (from current to next).
	 */
	private int depthThree (Node parent, byte current, byte next) {
		if (b.boardMove(this,colour,current,next)) {

			Piece[] pieces = new Piece[16];
			Node node = new Node(parent);

			int badMoves = checkMyMoves(node);
			if (badMoves != 0) {
				b.undoMove();
				return badMoves;
			}

			outer:
			for (int i = 0; i < 16; i++) {
				if ((64&b.pieces[i+tLow]) == 64) {
					pieces[i] = new Piece(i+tLow);
					int a = 0;
					while (pieces[i].nexts[a] != 0) {
						node.updateBeta(depthFour(node, pieces[i].current, pieces[i].nexts[a]));
						if (node.beta <= node.alpha)
							break outer;
						a++;
					}
				}
			}

			b.undoMove();

			return node.beta;
		} else {
			return Integer.MIN_VALUE; // ignore, tried to castle
		}
	}

	/**
	 * Attempts to move a piece from current to next, then evaluates the
	 * utility of the resulting board configuration. Corresponds to depth
	 * four of the search tree.
	 * 
	 * @param parent  the parent node (root).
	 * @param current  the current position of a piece.
	 * @param next  the next desired position of a piece.
	 * @return the utility of the move (from current to next).
	 */
	private int depthFour (Node parent, byte current, byte next) {
		if (b.boardMove(this,tc,current,next)) {
			Node node = new Node(parent);

			int badMoves = checkTheirMoves(node);
			if (badMoves != 0) {
				b.undoMove();
				return badMoves;
			}

			int out = eval();

			b.undoMove();

			return out;
		} else {
			return Integer.MAX_VALUE; // ignore, tried to castle
		}
	}

	/**
	 * The evaluation function for a board state. Emphasis on speed.
	 * @return int  the expected utility
	 */
	private int eval () {
		int sum = 0;
		for (int i = mLow; i < mHi; i++)
			if ((64&b.pieces[i]) == 64)
				sum += pieceValue(i);
		for (int i = tLow; i < tHi; i++)
			if ((64&b.pieces[i]) == 64)
				sum -= pieceValue(i);
		return sum;
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
	 * Standard method required by inheritance. Similar to Human counterpart.
	 * 
	 * @return byte[] of length two, corresponding to the current and desired location of 
	 * 		   a piece. Format is; [7-6]: unused, [5-3]: column, [2-0] row.
	 */
	private byte[] getMove() {
		char[] parsed;
		byte[] out = new byte[2];
		//
		System.out.println("starting MiniMax");
		byte[] move = root();
		char currentChar = (char) (((56&move[0])>>3)+65);
		char nextChar = (char) (((56&move[1])>>3)+65);
		System.out.print(currentChar);
		System.out.println((char)((7&move[0])+49));
		System.out.print(nextChar);
		System.out.println((char)((7&move[1])+49));
		//
		for ( ; ; ) {
			System.out.print("move: ");
			parsed = in.next().toUpperCase().toCharArray();
			if (parsed.length == 2 && parsed[0] >= 65 && parsed[0] <= 72 && parsed[1] >= 49 && parsed[1] <= 56) {
				byte oldCol = (byte) (parsed[0]-65); 
				byte oldRow = (byte) (parsed[1]-49); 
				if (b.board[oldCol][oldRow] != -128) {
					parsed = in.next().toUpperCase().toCharArray();
					if (parsed.length == 2 && parsed[0] >= 65 && parsed[0] <= 72 && parsed[1] >= 49 && parsed[1] <= 56) {
						out[0] = b.pieces[b.board[oldCol][oldRow]];
						out[1] = (byte) ((-64&out[0]) | ((parsed[0]-65)<<3) | (parsed[1]-49));
						if (b.validateMove(colour,out[0],out[1])) {
							break;
						}
					}
				}
			}
			if (parsed[0] == 'X') {
				System.exit(0);
			} else {
				System.out.println(" invalid coordinates");
				in = new Scanner(System.in);
			}
		}
		return out;
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