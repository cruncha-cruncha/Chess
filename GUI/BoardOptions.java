package Chess.GUI;

import Chess.*;

public class BoardOptions {
	public Colour firstColour;
	public boolean humanFirst, simpleEval;
	public int depth;
	public char[][] board;

	public BoardOptions () {
		board = new char[8][8];
	}
}