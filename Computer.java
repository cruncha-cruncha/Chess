package Chess;

import java.util.Scanner;

// just a copy of Human
public class Computer implements PlayerInterface {
private Scanner in;
	private Board board;
	private Colour colour;
	
	public Computer (Board board, Colour colour) {
		this.board = board;
		in = new Scanner(System.in);
		this.colour = (colour == Colour.WHITE) ? Colour.BLACK : Colour.WHITE;
	}

	public void go() {
		String input;
		for ( ; ; ) {
			int[] aMove = getMove();
			if (aMove[0] == -1) {
				board.undo();
				board.printBoard();
				break;
			} else {
				if (!board.move(this,aMove)) {
					System.out.println(" invalid move");
				} else {
					board.printBoard();
					break;
				}
			}
		}
	}
	
	private int[] getMove() {
		char[] parsed;
		int[] out = new int[4];
		for ( ; ; ) {
			System.out.print("move: ");
			parsed = in.next().toUpperCase().toCharArray();
			if (parsed.length == 2 && parsed[0] >= 65 && parsed[0] <= 72 && parsed[1] >= 49 && parsed[1] <= 56) {
				int oldRow = parsed[1]-49;
				int oldCol = parsed[0]-65;
				if (board.pieceAt(colour,oldRow,oldCol)) {
					parsed = in.next().toUpperCase().toCharArray();
					if (parsed.length == 2 && parsed[0] >= 65 && parsed[0] <= 72 && parsed[1] >= 49 && parsed[1] <= 56) {
						int newRow = parsed[1]-49;
						int newCol = parsed[0]-65;
						out[0] = oldRow;
						out[1] = oldCol;
						out[2] = newRow;
						out[3] = newCol;
						break;
					}
				}
			}
			if (parsed[0] == 'Q') {
				System.exit(0);
			} else if (parsed.length == 4 && parsed[0] == 'U' && parsed[1] == 'N' && parsed[2] == 'D' && parsed[3] == 'O') {
				out[0] = -1;
			} else {
				System.out.println(" invalid coordinate");
				in = new Scanner(System.in);
			}
		}
		return out;
	}
	
	public String choosePawnPromo() {
		String promo = "Q";
		return promo;
	}
	
	public Colour getColour() {
		return colour;
	}
	
	public void setColour(Colour colour) {
		this.colour = colour;
	}
}