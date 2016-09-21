package Chess;

import java.util.Scanner;
import java.util.HashSet;

public class IO implements PlayerInterface {
	
	private Scanner in;
	private Board b;
	private byte colour;

	public IO () {
		in = new Scanner(System.in);
		master();
	}
	
	private void master () {
		String input;
		for ( ; ; ) {
			setup();
			b.go();
			for ( ; ; ) {
				System.out.print("play again (y/n): ");
				input = in.next().toLowerCase();
				if (input.equals("q") || input.equals("n"))
					System.exit(0);
				if (input.equals("y"))
					break;
			}
		}
	}
	
	private void setup() {
		String input;
		
		System.out.println("press q at any time to quit");
		
		for ( ; ; ) {
			System.out.println("load from file (y/n): ");
			b = new Board();
			break;
		}
		
		for ( ; ; ) {
			System.out.print("b or w? ");
			input = in.next();
			if (input.equals("b")) {
				colour = -128;
				break;
			} else if (input.equals("w")) {
				colour = 0;
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
				rival = new Human(b,colour);
				break;
			} else if (input.equals("2")) {
				rival = new Computer(b,colour);
				break;
			} else if (input.equals("Q")) {
				System.exit(0);
			}
		}
		
		b.setPlayers(this,rival);
		
		System.out.println("please use coordinate notation");
		System.out.println("ex: E2 E4\n");
	}
	
	public byte getColour() {
		return colour;
	}

	
	public void setColour(byte colour) {
		this.colour = colour;
	}
	
	public void go() {
		String input;
		for ( ; ; ) {
			byte[] aMove = getMove();
			if (!b.move(this,aMove)) {
				System.out.println(" invalid move");
			} else {
				b.printBoard();
				break;
			}
		}
	}
	
	private byte[] getMove() {
		char[] parsed;
		byte[] out = new byte[4];
		for ( ; ; ) {
			System.out.print("move: ");
			parsed = in.next().toUpperCase().toCharArray();
			if (parsed.length == 2 && parsed[0] >= 65 && parsed[0] <= 72 && parsed[1] >= 49 && parsed[1] <= 56) {
				byte oldCol = (byte) (parsed[0]-65); 
				byte oldRow = (byte) (parsed[1]-49); 
				if (b.board[oldCol][oldRow] != -128 && (-128&b.pieces[b.board[oldCol][oldRow]]) == colour) {
					parsed = in.next().toUpperCase().toCharArray();
					if (parsed.length == 2 && parsed[0] >= 65 && parsed[0] <= 72 && parsed[1] >= 49 && parsed[1] <= 56) {
						out[0] = oldCol;
						out[1] = oldRow;
						out[2] = (byte) (parsed[0]-65);
						out[3] = (byte) (parsed[1]-49); 
						break;
					}
				}
			}
			if (parsed[0] == 'Q') {
				System.exit(0);
			} else {
				System.out.println(" invalid coordinate");
				in = new Scanner(System.in);
			}
		}
		return out;
	}
	
	public char choosePawnPromo() {
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
		return promo.charAt(0);
	}
	
	public static void main (String[] args) {
		IO io = new IO();
	}
}