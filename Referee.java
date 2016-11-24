package Chess;

import java.util.Scanner;
import javax.swing.*;
import java.awt.*;

public class Referee {
	
	private Colour turn;
	private Scanner in;
	private Board board;
	private PlayerInterface wPlayer, bPlayer;
	
	public Referee () {
		in = new Scanner(System.in);
		board = new Board();
		getSides();
		go();
	}

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
	
	private boolean go () {
		while (true) {
			//if (board.gameOver())
			//	break;
			
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
		return false;
	}
	
	public static void main (String[] args) {
		Referee ref = new Referee();
	}
}