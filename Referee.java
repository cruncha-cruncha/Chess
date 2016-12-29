package Chess;

import Chess.Players.*;

import java.util.Scanner;
import javax.swing.*;
import java.awt.*;

/**
 * This class is the main entry to the program. It initilializes all required variables,
 * governs turn-based play, and handles game endings.
 *
 * @author  Liam Marcassa
 */
public class Referee {
	
	private Colour turn;
	private Scanner in;
	private Board board;
	private PlayerInterface wPlayer, bPlayer;

	// TODO:
	// - fix castling rules (inbetween squares cannot be in check!)
	// - output chess moves to file
	// - play with evaluation functions
	// - add more depths
	
	/**
	 * Constructor, main entry point for entire program.
	 */
	public Referee () {
		in = new Scanner(System.in);
		board = new Board();
		getSides();
		go();
	}

	/**
	 * Prompts the user for their choice of colour and turn order,
	 * initiallizes appropriate variables.
	 * 
	 * @return for convenience
	 */
	private boolean getSides () {
		System.out.print("white plays first? (y/n): ");
		String choice = in.next().toLowerCase();
		if (choice.equals("n")) {
			turn = Colour.BLACK;
		} else {
			turn = Colour.WHITE;
		}
		System.out.print("human makes the first move? (y/n): ");
		choice = in.next().toLowerCase();
		if (choice.equals("n")) {
			if (turn == Colour.WHITE) {
				wPlayer = new Computer(board,Colour.WHITE);
				bPlayer = new Human(board,Colour.BLACK);
			} else {
				wPlayer = new Human(board,Colour.WHITE);
				bPlayer = new Computer(board,Colour.BLACK);
			}
		} else {
			if (turn == Colour.WHITE) {
				wPlayer = new Human(board,Colour.WHITE);
				bPlayer = new Computer(board,Colour.BLACK);
			} else {
				wPlayer = new Computer(board,Colour.WHITE);
				bPlayer = new Human(board,Colour.BLACK);
			}
		}
		return true;
	}
	
	/**
	 * Regulates turns, checks for end condition.
	 * 
	 * @return true if no error
	 */
	private boolean go () {
		String gameOver;
		while (true) {

			if (!board.gameOver.equals("")) {
				System.out.println(board.gameOver);
				break;
			}
			
			if (turn == Colour.WHITE) {
				wPlayer.makeMove();
			} else {
				bPlayer.makeMove();
			}
			turn = (turn == Colour.BLACK) ? Colour.WHITE : Colour.BLACK;
			board.printBoard();
			if (in.next().equals("x"))
				break;
		}
		return true;
	}
	
	/**
	 * Entry point of program
	 * 
	 * @param args  unused
	 */
	public static void main (String[] args) {
		Referee ref = new Referee();
	}
}