package Chess;

import java.util.Scanner;
import java.util.Arrays;

// just a copy of Human
public class Computer implements PlayerInterface {
	private Scanner in;
	private Board b;
	private Colour colour, tc;
	private char promoChar = 'N';

	private final int MAX_DEPTH = 4;

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
		boolean inCheck;

		public Node () {
			alpha = Integer.MIN_VALUE;
			beta = Integer.MAX_VALUE;
		}

		public Node (Node p) {
			parent = p;
			alpha = p.alpha;
			beta = p.beta;
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
	}

	// does not recognize check
	// changes king to a pawn?

	private byte[] root () {
		Piece[] pieces = new Piece[16];
		Node node = new Node();

		for (int i = 0; i < 16; i++) {
			if ((64&b.pieces[i+mLow]) == 64) {
				pieces[i] = new Piece();
				pieces[i].current = b.pieces[i+mLow];
				pieces[i].nexts = b.getPiece(i+mLow).getMoves(b.pieces[i+mLow]);
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
			while (pieces[i].nexts[a] != 0) {
				if (pieces[i].evals[a] == node.alpha) {
					out[0] = pieces[i].current;
					out[1] = pieces[i].nexts[a];
					break outer;
				}
				a++;
			}
		}

		return out;
	}

	private int depthOne (Node parent, byte current, byte next) {
		if (b.boardMove(this,colour,current,next)) {
			if (kingCapture()) {
				b.undoMove();
				return Integer.MAX_VALUE;
			}

			Piece[] pieces = new Piece[16];
			Node node = new Node(parent);
			
			outer:
			for (int i = 0; i < 16; i++) {
				if ((64&b.pieces[i+tLow]) == 64) {
					pieces[i] = new Piece();
					pieces[i].current = b.pieces[i+tLow];
					pieces[i].nexts = b.getPiece(i+tLow).getMoves(b.pieces[i+tLow]);
					int a = 0;
					int tmp;
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

	private int depthTwo (Node parent, byte current, byte next) {
		if (b.boardMove(this,tc,current,next)) {
			if (kingCapture()) {
				b.undoMove();
				return Integer.MIN_VALUE;
			}

			Piece[] pieces = new Piece[16];
			Node node = new Node(parent);

			outer:
			for (int i = 0; i < 16; i++) {
				if ((64&b.pieces[i+mLow]) == 64) {
					pieces[i] = new Piece();
					pieces[i].current = b.pieces[i+mLow];
					pieces[i].nexts = b.getPiece(i+mLow).getMoves(b.pieces[i+mLow]);
					int a = 0;
					int tmp;
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
			if (kingCapture()) {
				b.undoMove();
				// figure out if stalemate ????
				return Integer.MAX_VALUE;
			}

			Piece[] pieces = new Piece[16];
			Node node = new Node(parent);

			outer:
			for (int i = 0; i < 16; i++) {
				if ((64&b.pieces[i+tLow]) == 64) {
					pieces[i] = new Piece();
					pieces[i].current = b.pieces[i+tLow];
					pieces[i].nexts = b.getPiece(i+tLow).getMoves(b.pieces[i+tLow]);
					int a = 0;
					int tmp;
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
		try {
			if (b.boardMove(this,tc,current,next)) {
				if (kingCapture()) {
					b.undoMove();
					// figure out what it means???
					return Integer.MIN_VALUE;
				}

				int out = eval();

				b.undoMove();

				return out;
			} else {
				return Integer.MAX_VALUE; // ignore, tried to castle
			}
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			System.out.println("ERROR");
			System.out.println(b.pieces[17]);
			b.printBoard();
			/*
			System.out.println(b.undoMove());
			b.printBoard();
			System.out.println(b.undoMove());
			b.printBoard();
			System.out.println(b.undoMove());
			b.printBoard();
			System.out.println(current);*/
			System.exit(0);
		}
		return 0;
	}

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

	private boolean kingCapture () {
		if ((64&b.pieces[0]) == 0 || (64&b.pieces[16]) == 0)
			return true;
		return false;
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