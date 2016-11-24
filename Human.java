package Chess;

import java.util.Scanner;

public class Human implements PlayerInterface {
	
	private Scanner in;
	private Board b;
	private Colour colour;
	
	public Human (Board b, Colour colour) {
		this.b = b;
		this.colour = colour;
		in = new Scanner(System.in);
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
	
	private byte[] getMove() {
		char[] parsed;
		byte[] out = new byte[2];
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
		String promo;
		for ( ; ; ) {
			System.out.print("promote pawn to (Q,R,B,N): ");
			promo = in.next();
			if (promo.equals("x"))
				System.exit(0);
			promo = promo.toUpperCase();
			if (promo.equals("Q") || promo.equals("R") || promo.equals("B") || promo.equals("N"))
				break;
		}
		return promo.charAt(0);
	}
}

