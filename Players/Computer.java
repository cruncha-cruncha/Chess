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
	public Computer (Board b, Colour colour) {
		this.b = b;
		this.colour = colour;

		if (colour == Colour.BLACK) {
			tc = Colour.WHITE;
			mLow = 0;
			tLow = 16;
			flagsMask = 13;
		} else {
			tc = Colour.BLACK;
			mLow = 16;
			tLow = 0;
			flagsMask = 50;
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

		// gun shy honey, collect it all
		// the dust inside, the rusted souls
		// you should get a ride
		// cause you can't control
		// the heart that beats
		// under the bone 
		// come on my comeback chameleon, give it up
		// you've got your life to attend to, buttercup
		// you're entertaining the talk that is told through the teeth of the mouths of millions
		// dying to meet ya

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
		private boolean simpleEval;
		// PRIVATE
		public int curDepth, maxDepth;

		public Dispatch () {
			simpleEval = true;
			curDepth = 0;
			maxDepth = 2;
		}

		public Dispatch (boolean eval, int depth) {
			simpleEval = eval;
			curDepth = 0;
			maxDepth = depth;
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
			// 0 0
			// 1 0
			// 2 1
			// 3 1
			// 4 2
			// 5 2
		}

		public int eval () {
			/*
			if (simpleEval) {
				return simpleEval();
			} else {
				return betterEval();
			}*/
			return simpleEval();
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

		// HOW THE FUCK DOES THIS WORK??
		if (flags == 0 && (flagsMask&CastleSync.getFlags()) != 0) {
			if ((56&b.pieces[mLow]) == 16 || (56&b.pieces[mLow]) == 48) {
				sum += 2;
			} else {
				sum -= 2;
			}
		}

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