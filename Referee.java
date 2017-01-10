package Chess;

import Chess.Players.*;

import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;


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
	private BufferedWriter bw;
	public int moveCount;

	// TODO:
	// - play with evaluation functions (penalty for not castling, piece-position rewards, opening/mid/endgame detection?)
	// - make depths recursive?
	// 
	// "Onlya6th
	
	/** Constructor, main entry point for entire program. */
	public Referee () {
		moveCount = 0;
		in = new Scanner(System.in);
		board = new Board();
		getSides();
		System.out.println("Move to x to quit");
		makeFile();
		go();
		closeFile();
	}

	/** Make the file "output.txt", quit if fail */
	private void makeFile () {
		try {
			bw = new BufferedWriter(new FileWriter("output.txt"));
		} catch (java.io.IOException e) {
			System.out.println("Fatal Error - could not create output file");
			e.printStackTrace();
			System.exit(1);
		}
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

			if (!board.gameOver.equals("X")) { saveToFile(moveCount+2); }
			moveCount++;
		}
		return true;
	}

	/**
	 * Writes moves out to file "output.txt" in coordinate notation,
	 * quit if fail. Only looks nice if white plays first.
	 * 
	 * @param moves  the number of individual moves made plus 1
	 */
	private void saveToFile (int moves) {
		try {
			if (turn == Colour.BLACK) {
				int num = moves/2;
				bw.write(Integer.toString(num) + ". ");
			} else {
				bw.write(" ");
			}

			try {
				int index = board.moveHistory.pieces[1];
				char currentChar = (char) (((56&board.moveHistory.pieces[2])>>3)+65);
				char nextChar = (char) (((56&board.pieces[index])>>3)+65);
				bw.write(currentChar);
				bw.write((char)((7&board.moveHistory.pieces[2])+49));
				bw.write("-");
				bw.write(nextChar);
				bw.write((char)((7&board.pieces[index])+49));
			} catch (java.lang.NullPointerException e) {
				// do nothing
			}

			if (turn == Colour.WHITE) { bw.newLine(); }
		} catch (java.io.IOException e) {
			System.out.println("Fatal Error - could not write moves to file");
			e.printStackTrace();
			System.exit(0);
		}
	}

	/** Close "output.txt" */
	private void closeFile () {
		try {
			bw.close();
		} catch (java.io.IOException e) {
			// do nothing
		}

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