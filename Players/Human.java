package Chess.Players;

import Chess.*;

import java.util.Scanner;

/**
 * This class implements PlayerInterface. It prompts the user for a move and ensures that
 * the move is valid. 
 *
 * @author  Liam Marcassa
 */
public class Human implements PlayerInterface {
	
	private Scanner in;
	private Board b;
	private Colour colour;
	
	/**
	 * Constructor
	 * 
	 * @param b  the Board to play on
	 * @param colour  Human's piece colour
	 */
	public Human (Board b, Colour colour) {
		this.b = b;
		this.colour = colour;
		in = new Scanner(System.in);
	}

	/**
	 * Helper function. 
	 * 
	 * @return the player's piece colour
	 */
	public Colour getColour() {
		return colour;
	}
	
	/** Calls getMove(), then ensures move does not put Human in check. */
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
	
	/**
	 * Ask the user for a move, sanitize input. Exit if input is 'x'.
	 * 
	 * @return byte[] of size two, representing a current piece and it's desired replacement
	 */
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
	
	/**
	 * Prompt user for pawn promotion, sanitize input. Exit if input is 'x'.
	 * 
	 * @return the char standing for a piece code (Q,R,B,N).
	 */
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

