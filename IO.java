package Chess;

import java.util.Scanner;
import java.util.HashSet;

public class IO implements PlayerInterface {
	
	private Scanner in;
	private Board board;
	private Colour colour;

	public IO () {
		in = new Scanner(System.in);
		board = new Board();
		setup();
		board.go();
		System.out.println("play again?");
		// do something
	}
	
	private void setup() {
		String input;
		
		System.out.println("press q at any time to quit");
		
		System.out.println("load from file (y/n): ");
		// do something
		
		for ( ; ; ) {
			System.out.print("b or w? ");
			input = in.next();
			if (input.equals("b")) {
				colour = Colour.BLACK;
				break;
			} else if (input.equals("w")) {
				colour = Colour.WHITE;
				break;
			} else if (input.equals("q")) {
				System.exit(0);
			}
		}
		
		PlayerInterface rival;
		for ( ; ; ) {
			System.out.print("human vs (1) human or (2) computer? ");
			input = in.next();
			if (input.equals("1")) {
				rival = new Human(board,colour);
				break;
			} else if (input.equals("2")) {
				rival = new Computer(board,colour);
				break;
			} else if (input.equals("Q")) {
				System.exit(0);
			}
		}
		
		board.setPlayers(this,rival);
		
		System.out.println("please use coordinate notation");
		System.out.println("ex: E2 E4\n");
	}
	
	public Colour getColour() {
		return colour;
	}
	
	public void setColour(Colour colour) {
		this.colour = colour;
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
		String promo;
		for ( ; ; ) {
			System.out.print("promote pawn to (Q,R,B,N): ");
			promo = in.next();
			if (promo.equals("q"))
				System.exit(0);
			promo = promo.toUpperCase();
			if (promo.equals("Q") || promo.equals("R") || promo.equals("B") || promo.equals("N"))
				break;
		}

		return promo;
	}
	
	public static void main (String[] args) {
		IO io = new IO();
	}
}