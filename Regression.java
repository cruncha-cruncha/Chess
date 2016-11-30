package Chess;

public class Regression {

	private Board b;
	private Shell shell;

	public Regression () {
		shell = new Shell();
		b = new Board();
		runTest();
		//pawnTest();
	}

	private void pawnTest () {
		byte current = b.pieces[24];
		byte[] nexts = b.getPiece(24).getMoves(current);
		int a = 0;
		while (nexts[a] != 0) {
			System.out.println(nexts[a]);
			if (b.boardMove(shell,Colour.WHITE,current,nexts[a])) {
				b.printBoard();
				b.undoMove();
			}
			a++;
		}
		System.out.println(a);
	}

	private void runTest () {
		byte current;
		for (int i = 0; i < 16; i++) {
			current = b.pieces[i];
			try {
				byte[] nexts = b.getPiece(i).getMoves(current);
				int a = 0;
				while (nexts[a] != 0) {
					if (b.boardMove(shell,Colour.BLACK,current,nexts[a])) {
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
					if (b.boardMove(shell,Colour.WHITE,current,nexts[a])) {
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