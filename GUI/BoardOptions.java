package Chess.GUI;

import Chess.*;

/**
 * Container class for communication between the GUI (where
 * the user specifies several options) and other classes that
 * need to know those specifications (namely Board, Referee, and
 * Computer).
 * 
 * @author  Liam Marcassa
 */
public class BoardOptions {
	public Colour firstColour; // which colour plays first
	public boolean humanFirst; // if true, Human makes the first move, otherwise Computer does
	public boolean simpleEval; // if true, Computer uses simple (faster) evaluation function
	public int depth; 		   // maximum depth that the Computer will search to (except in the endgame)
	public char[][] board;     // the intial board configuration

	public BoardOptions () {
		board = new char[8][8];
	}
}