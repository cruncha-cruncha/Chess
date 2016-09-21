package Chess;

import java.util.Scanner;

// copy of IO, used for testing
public class Human implements PlayerInterface {
	
	private Scanner in;
	private Board b;
	private byte colour;
	
	public Human (Board b, byte colour) {
		this.b = b;
		in = new Scanner(System.in);
		this.colour = (colour == 0) ? (byte) -128 : (byte) 0;
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
}

