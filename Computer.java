package Chess;

import java.util.Scanner;
import java.util.Arrays;

// just a copy of Human
public class Computer implements PlayerInterface {
	private Scanner in;
	private Board b;
	private Colour colour, tc;   // colour = our colour, tc = their colour
	private char promoChar = 'N';

	private static final int WIN = 15000;
	private static final int STALE = -14000;

	private int mLow, mHi, tLow, tHi;
	
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

	public Colour getColour() {
		return colour;
	}
	
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

	public class Node {
		Node parent;
		int alpha,beta;
		boolean blackInCheck, whiteInCheck;

		public Node () {
			alpha = Integer.MIN_VALUE;
			beta = Integer.MAX_VALUE;
			getChecks();
		}

		public Node (Node p) {
			parent = p;
			alpha = p.alpha;
			beta = p.beta;
			getChecks();
		}

		public void getChecks () {
			blackInCheck = b.calcCheck(Colour.BLACK);
			whiteInCheck = b.calcCheck(Colour.WHITE);
		}

		public void updateAlpha (Node n) {
			if (n.beta > alpha)
				alpha = n.beta;
		}

		public void updateAlpha (int b) {
			if (b > alpha)
				alpha = b;
		}

		public void updateBeta (Node n) {
			if (n.alpha < beta)
				beta = n.alpha;
		}

		public void updateBeta (int a) {
			if (a < beta)
				beta = a;
		}
	}

	public class Piece {
		byte current;
		byte[] nexts;
		int[] evals;
		public Piece (int i) {
			current = b.pieces[i];
			nexts = b.getPiece(i).getMoves(b.pieces[i]);
		}
	}

	// does not recognize check?
	// FIX CASTLING

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

	private int checkMyMoves (Node n, Colour c) {
		if (c == Colour.BLACK) {
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

	private int depthOne (Node parent, byte current, byte next) {
		if (b.boardMove(this,colour,current,next)) {

			Piece[] pieces = new Piece[16];
			Node node = new Node(parent);

			int badMoves = checkMyMoves(node,colour);
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

	private int checkTheirMoves (Node n, Colour c) {
		if (c == Colour.BLACK) {
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

	private int depthTwo (Node parent, byte current, byte next) {
		if (b.boardMove(this,tc,current,next)) {

			Piece[] pieces = new Piece[16];
			Node node = new Node(parent);

			int badMoves = checkTheirMoves(node,tc);
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

	private int depthThree (Node parent, byte current, byte next) {
		if (b.boardMove(this,colour,current,next)) {

			Piece[] pieces = new Piece[16];
			Node node = new Node(parent);

			int badMoves = checkMyMoves(node,colour);
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

	private int depthFour (Node parent, byte current, byte next) {
		if (b.boardMove(this,tc,current,next)) {
			Node node = new Node(parent);

			int badMoves = checkTheirMoves(node,tc);
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

	// 128 64 32 16 8 4 2 1
	//   0  1  1  0 0 0 0 1
	//   0  1  0  1 1 0 1 0

	// must return a value +/- 10,000
	//
	// -15000 = we lost
	// -14000 = stale mate 
	// 15000 = we won

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

	private int pieceValue (int i) {
		switch (b.pieceNames[i]) {
			case 'K':
				return Integer.MAX_VALUE-200;
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
	
	public char choosePawnPromo() {
		promoChar = (promoChar == 'N') ? 'Q' : 'N';
		return promoChar;
	}
}