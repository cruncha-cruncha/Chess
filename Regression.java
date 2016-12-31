package Chess;

import Chess.Players.*;

public class Regression {

	private Board b;
	private Shell shell;

	public Regression () {
		shell = new Shell();
		b = new Board();
		//runTest();
		castleTest();
	}

	private void castleTest () {
		byte current = b.pieces[0];
		byte[] nexts = b.getPiece(0).getMoves(current);
		int a = 0;
		while (nexts[a] != 0) {
			//System.out.println(nexts[a]);
			if (b.boardMove(shell,current,nexts[a])) {
				b.printBoard();
				b.undoMove();
				System.out.println(CastleSync.canCastle((byte)-9));
				System.out.println(CastleSync.getFlags());
			} else {
				System.out.println("bad move");
				System.out.println(nexts[a]);
				System.out.println("-done-");
			}

			a++;
		}
		//System.out.println(a);
	}

	private void runTest () {
		byte current;
		for (int i = 0; i < 16; i++) {
			current = b.pieces[i];
			try {
				byte[] nexts = b.getPiece(i).getMoves(current);
				int a = 0;
				while (nexts[a] != 0) {
					if (b.boardMove(shell,current,nexts[a])) {
						b.printBoard();
						b.undoMove();
					}
					a++;
				}
			} catch (java.lang.NullPointerException e) { }
		}
		for (int i = 16; i < 32; i++) {
			current = b.pieces[i];
			try {
				byte[] nexts = b.getPiece(i).getMoves(current);
				int a = 0;
				while (nexts[a] != 0) {
					if (b.boardMove(shell,current,nexts[a])) {
						b.printBoard();
						b.undoMove();
					}
					a++;
				}
			} catch (java.lang.NullPointerException e) { }
		}
	}

	private class Shell implements PlayerInterface {
		char promoChar = 'N';
		public Colour getColour() { return Colour.BLACK; }
		public void makeMove () { }
		public char choosePawnPromo () {
			promoChar = (promoChar == 'N') ? 'Q' : 'N';
			return promoChar;
		}
	}

	public static void main (String[] args) {
		Regression r = new Regression();
	}
}