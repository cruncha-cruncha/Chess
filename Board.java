package Chess;

import Chess.GUI.*;
import Chess.Pieces.*;
import Chess.Players.*;

import java.util.LinkedList;
import java.util.Scanner;

/**
 * Represents a chess board, and is responsible for ensuring each move is valid.
 * Throughout the code, effort has been taken to seperate "check" detection and
 * action from everything else, since it is a fairly intensive process (more on
 * this decision in the report).
 *
 * @author  Liam Marcassa
 */
public class Board {
	
	private static char[] columns; // [A,B,C,D,E,F,G,H]
	private static PieceInterface king, queen, rook, bishop, knight, pawn;
	private boolean blackInCheck, whiteInCheck;

	// Used to indicate end game states: either a mate or stale. If the game is not
	// over, is kept blank.
	public String gameOver;

	// A linked list detailing the exact board changes between one state and the next.
	public OldPosition moveHistory;

	// pieces and pieceNames together provide a complete board specification. Each element of 
	// pieces contains the following bits; [7]: colour (0=white, 1=black); [6] in play (1=in play,
	// 0=captured); [5-3]: column/file of piece (x coordinate); [2-0]: row/rank of piece (y coordiante).
	// Location A1 is at (0,0). The black king is always at pieces[0], and the white king is 
	// always at pieces[16] (see #fillPieces() for others). Since pawns can be promoted into
	// other pieces, pieceNames is required to indicate which type of piece each pieces[x] is. 
	public byte[] pieces;
	public byte[] pieceNames;

	// board is a direct representation of a chess board: an 8x8 matrix. If no piece is on
	// square (x,y), board[x][y] == -128, else: board[ (56 & pieces[i]) >> 3 ][ (7 & pieces[i]) ] == i
	public byte[][] board;
	
	// ASCII output is terrible for chess, but I have no experience with Swing. In an attempt to 
	// make the output more clear, colour can be used in *nix systems to differentiate between
	// players (this has only been tested in OS X however). The colour codes were taken from:
	// http://cesarloachamin.github.io/2015/03/31/System-out-print-colors-and-unicode-characters/
	private boolean isUnix;
	public static final String CYAN = "\u001B[36m";
	public static final String YELLOW = "\u001B[33m";
	public static final String RESET = "\u001B[0m";

	// used as a dummy player to catch pawn promotions when detecting check
	private Shell shell = new Shell();
	
	/** 
	 * Initializes gameOver string and detects *nix systems.
	 */
	public Board () {
		gameOver = "";
		isUnix = detectUnix();
	}
	
	/**
	 * Detect *nix systems in an attempt to render output slightly better (tested on an OS X bash shell).
	 * Based on a technique from https://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
	 * 
	 * @return true if *nix system, false otherwise
	 */
	private boolean detectUnix () {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Get user-defined parameters, including board configuration and depth. Initialize
	 * variables appropriately.
	 * 
	 * @return true, program exits on a bad board.
	 */
	public BoardOptions setupBoard () {
		System.out.println("<loading swing>");

		initVars(); // piece interfaces and moveHistory linked list

		BoardGUI gui = new BoardGUI();
		BoardOptions options = gui.askBoard();

		if (options.board[0][0] == 'x') { // BoardGUI will set this if bad board
			System.out.println("Fatal Error: could not parse board");
			System.exit(0);
		}

		fillPieces(options.board); // populate pieces and pieceNames
		fillBoard(); // populate board, set checks and castling variables

		return options;
	}
	
	/**
	 * Gain access to each piece type's movement generation and validation methods,
	 * and initilize the linked list of previous moves (moveHistory).
	 */
	private void initVars () {
		pawn = new Pawn(this);
		knight = new Knight(this);
		bishop = new Bishop(this);
		rook = new Rook(this);
		queen = new Queen(this);
		king = new King(this);
		
		moveHistory = new OldPosition();
	}
	
	/**
	 * Create and populate the board (must be called after #fillPieces()).
	 * Calculate check states, and whether or not castling can occur.
	 * Inform user on how to interpret the board.
	 */
	private void fillBoard () {
		// column labels (A-H)
		columns = new char[8];
		for (int i = 0; i < 8; i++) {
			columns[i] = (char) (i+65);
		}
		
		// fill board
		board = new byte[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board[i][j] = -128;
			}
		}
		for (int i = 0; i < 32; i++) {
			if (pieces[i] != 0)
				board[(pieces[i]&56)>>3][pieces[i]&7] = (byte) i;
		}
		
		// update CastleSync
		if (board[4][7] != 0)
			CastleSync.set((byte)-25);
		if (board[4][0] != 16)
			CastleSync.set((byte)96);
		if (board[0][7] == -128 || board[0][7] > 15 || pieceNames[board[0][7]] != 'R')
			CastleSync.set((byte)-57);
		if (board[7][7] == -128 || board[7][7] > 15 || pieceNames[board[7][7]] != 'R')
			CastleSync.set((byte)-1);
		if (board[0][0] < 16 || pieceNames[board[0][0]] != 'R')
			CastleSync.set((byte)64);
		if (board[7][0] < 16 || pieceNames[board[7][0]] != 'R')
			CastleSync.set((byte)120);

		// update check information
		calcCheck(Colour.BLACK);
		calcCheck(Colour.WHITE);
		
		// tell the user whats going on
		if (isUnix) {
			System.out.println("black pieces are " + CYAN + "CYAN" + RESET);
			System.out.println("white pieces are " + YELLOW + "YELLOW" + RESET);
		} else {
			System.out.println("black pieces are UPPERCASE");
			System.out.println("white pieces are lowercase");
		}
	}
	
	/**
	 * Parse the user-supplied board, and use that data to create and populate
	 * the pieces and pieceNames arrays. User supplied board has already been 
	 * sanitized.
	 * 
	 * @param config  the user-supplied board, containing uppercase and
	 * 				  lowercase chars corresponding to piece codes.
	 */
	private void fillPieces (char[][] config) {
		// pieces[x] bits;
		// 7 = colour (0 = white, 1 = black)
		// 6 = inPlay (0 = not in play (captured), 1 = in play)
		// 5-3 = x coordinate
		// 2-0 = y coordinate
		pieces = new byte[32];
		pieceNames = new byte[32]; // these are really just chars
		
		// Order of the pieces array, never changes. Only pawns may get promoted to other pieces.
		String key = "KQRRBBNNPPPPPPPPkqrrbbnnpppppppp";

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (config[i][j] != 0) {
					// fill two spaces, if they're already full, go to pawn section
					int index = key.indexOf(config[i][j]);
					char pieceName = key.charAt(index);
					if (pieceName > 96)
						pieceName = (char) (pieceName-32);

					if (pieces[index] != 0) {
						if (index != 1 && index != 17 && pieces[index+1] == 0) {
							index += 1;
						} else {
							index = (index < 16) ? 8 : 24;
							while (pieces[index] != 0)
								index += 1;
						}
					}

					pieces[index] = (index < 16) ? (byte) (1<<7) : (byte) (0);
					pieces[index] = (byte) (pieces[index] | (1<<6));
					pieces[index] = (byte) (pieces[index] | (i<<3));
					pieces[index] = (byte) (pieces[index] | (j));
					pieceNames[index] = (byte) pieceName;
				}
			}
		}
	}
	
	/** Print a textual representation of the board to console. */
	public void printBoard () {
		// print column labels (chars A-H)
		System.out.println();
		System.out.print(" ");
		for (int x=0; x < 8; x++) {
			System.out.print(" ");
			System.out.print(columns[x]);
		}

		// print each row of the board
		System.out.println();
		for (byte y=7; y >= 0; y--) {
			System.out.print(y+1);
			for (byte x=0; x < 8; x++) {
				System.out.print(" ");
				if (board[x][y] == -128) {
					System.out.print((char)183); // a period floating in space
				} else {
					if (isUnix) {
						if (board[x][y] < 16) {
							System.out.print(CYAN);
						} else {
							System.out.print(YELLOW);
						}
						System.out.print(getPiece(board[x][y]).getChar());
						System.out.print(RESET);
					} else {
						if (board[x][y] < 16) {
							System.out.print(getPiece(board[x][y]).getChar());
						} else {
							System.out.print((char) (getPiece(board[x][y]).getChar()+32));
						}
					}
				}
			}
			System.out.println();
		}

		// reprint column labels (chars A-H)
		System.out.print(" ");
		for (int x=0; x < 8; x++) {
			System.out.print(" ");
			System.out.print(columns[x]);
		}

		System.out.println("\n");
	}

	/**
	 * Return the appropriate class by looking up the char code
	 * in pieceNames.
	 * 
	 * @param index of pieces array (and also pieceNames)
	 * @return the corresponding piece class
	 */
	public PieceInterface getPiece (int index) {
		switch(pieceNames[index]) {
			case 75:
				return king;
			case 81:
				return queen;
			case 82:
				return rook;
			case 66:
				return bishop;
			case 78:
				return knight;
			case 80:
				return pawn;
		}
		return null;
	}
	
	/**
	 * Undo the previous move based on moveHistory. Handles all relevant checking and
	 * castling states.
	 * 
	 * @return true unless moveHistory has no more links
	 */
	public boolean undoMove () {
		if (moveHistory.prev == null)
			return false;
		
		byte p0 = moveHistory.pieces[0];
		byte p1 = moveHistory.pieces[1];
		byte p2 = moveHistory.pieces[2];
		byte p3 = moveHistory.pieces[3];
		
		// check for and handle castling
		if ((8&p0) == 8) {
			// kingside castle
			if (p1 == 0) {
				// black rook
				board[7][7] = board[5][7];
				board[5][7] = -128;
				pieces[board[7][7]] = -1;
				CastleSync.reset((byte)-25);
				CastleSync.reset((byte)-1);
			} else {
				// white rook
				board[7][0] = board[5][0];
				board[5][0] = -128;
				pieces[board[7][0]] = 120;
				CastleSync.reset((byte)96);
				CastleSync.reset((byte)120);
			}
		} else if ((4&p0) == 4) {
			// queenside castle
			if (p1 == 0) {
				// black rook
				board[0][7] = board[3][7];
				board[3][7] = -128;
				pieces[board[0][7]] = -57;
				CastleSync.reset((byte)-25);
				CastleSync.reset((byte)-57);
			} else {
				// white rook
				board[0][0] = board[3][0];
				board[3][0] = -128;
				pieces[board[0][0]] = 64;
				CastleSync.reset((byte)96);
				CastleSync.reset((byte)64);
			}
		} else if ((2&p0) == 2) {
			if (p1 < 16) {
				// black piece
				CastleSync.reset(p2);
			} else {
				// white piece
				CastleSync.reset(p2);
			}
		} else if ((1&p0) == 1) {
			// change back to a pawn
			pieceNames[p1] = 'P';
		}
		
		if ((-64&p0) != 0) {
			// captured a piece
			pieces[p3] = (byte) (64 | pieces[p3]);
			board[(56&pieces[p3])>>3][7&pieces[p3]] = p3;
			if ((-128&p0) == -128) {
				// en passant
				board[(56&pieces[p3])>>3][7&pieces[p1]] = -128;
			}
		} else {
			board[(56&pieces[p1])>>3][7&pieces[p1]] = -128;
		}
		
		board[(56&p2)>>3][7&p2] = p1;
		pieces[p1] = p2;
		
		// black was in check
		if ((32&p0) == 32) {
			blackInCheck = true;
		} else {
			blackInCheck = false;
		}
		
		// white was in check
		if ((16&p0) == 16) {
			whiteInCheck = true;
		} else {
			whiteInCheck = false;
		}
		
		moveHistory = moveHistory.prev;
		return true;
	}
	
	/**
	 * Before any values are actually changed, ensure a move makes mechanical sense.
	 * Checks that the piece is in play, it is of the correct colour, and can move 
	 * to next position without colliding with another piece. Does not check check.
	 * 
	 * @param c, the colour of the piece being moved
	 * @param current, a piece (from the pieces array)
	 * @param next, the complete status of its desired next location
	 * @return true, if the move is valid, otherwise false
	 */
	public boolean validateMove (Colour c, byte current, byte next) {
		if ((64&current) != 64 || current == next)
			return false;
		if (c == Colour.BLACK) {
			if ((-128&current) == 0)
				return false;
		} else {
			if ((-128&current) == -128)
				return false;
		}
		PieceInterface pi = getPiece(board[(56&current)>>3][7&current]);
		return pi.validateMove(current,next);
	}

	/**
	 * Verify castling king is not escaping check or moving through check. 
	 * 
	 * @param  current, the current location of the king (must be on the E file, and a back rank)
	 * @param  newKing, the king's next location
	 * @return true if castling is allowed, false otherwise
	 */
	private boolean canCastle (byte current, byte newKing) {
		boolean out = true;
		switch (newKing) {
			case -9:
				// black kingside
				if (blackInCheck) { return false; }
				// (5,7) cannot be in check
				boardMove(shell,current,(byte)-17);
				calcCheck(Colour.BLACK);
				if (blackInCheck) { out = false; }
				undoMove();
				break;
			case -41:
				// black queenside
				if (blackInCheck) { return false; }
				// (2,7) cannot be in check
				boardMove(shell,current,(byte)-33);
				calcCheck(Colour.BLACK);
				if (blackInCheck) { out = false; }
				undoMove();
				break;
			case 112:
				// white kingside
				if (whiteInCheck) { return false; }
				// (5,0) cannot be in check
				boardMove(shell,current,(byte)104);
				calcCheck(Colour.WHITE);
				if (whiteInCheck) { out = false; }
				undoMove();
				break;
			case 80:
				// white queenside
				if (whiteInCheck) { return false; }
				// (2,0) cannot be in check
				boardMove(shell,current,(byte)88);
				calcCheck(Colour.WHITE);
				if (whiteInCheck) { out = false; }
				undoMove();
				break;
		}

		return out;
	}
	
	/**
	 * Make a move. Recognizes captures, pawn promotions, and castling. Updates pieces,
	 * pieceNames, board, and moveHistory. Does not check check.
	 * 
	 * @param player  the player making the move (required in case a pawn gets promoted).
	 * @param c  the player's colour (required for castling)
	 * @param current  an item from the pieces array
	 * @param next  a new entry for the pieces array
	 * @return true unless castling failed
	 */
	public boolean boardMove (PlayerInterface player, byte current, byte next) {
		byte oldCol = (byte) ((56&current)>>3);
		byte oldRow = (byte) (7&current);
		byte newCol = (byte) ((56&next)>>3);
		byte newRow = (byte) (7&next);
		PieceInterface pi = getPiece(board[oldCol][oldRow]);
		moveHistory = new OldPosition(moveHistory);
		
		// check for and handle castling
		if (pieceNames[board[oldCol][oldRow]] == 'K') {
			if (Math.abs(newCol-oldCol) == 2) {
				if (!canCastle(current,next)) {
					moveHistory = moveHistory.prev;
					return false; // only time this method does not return true
				} else {
					if (newCol == 6) {
						// castle kingside
						if (oldRow == 7) {
							// black rook
							pieces[board[7][7]] = -17;
							board[5][7] = board[7][7];
							board[7][7] = -128;
							CastleSync.set((byte)-25);
							CastleSync.set((byte)-1);
						} else {
							// white rook
							pieces[board[7][0]] = 104;
							board[5][0] = board[7][0];
							board[7][0] = -128;
							CastleSync.set((byte)96);
							CastleSync.set((byte)120);
						}
						moveHistory.pieces[0] = 8;
					} else {
						// castle queenside
						if (oldRow == 7) {
							// black rook
							pieces[board[0][7]] = -33;
							board[3][7] = board[0][7];
							board[0][7] = -128;
							CastleSync.set((byte)-25);
							CastleSync.set((byte)-57);
						} else {
							// white rook
							pieces[board[0][0]] = 88;
							board[3][0] = board[0][0];
							board[0][0] = -128;
							CastleSync.set((byte)96);
							CastleSync.set((byte)64);
						}
						moveHistory.pieces[0] = 4;
					}
				}
			} else {
				if (current == -25 || current == 96) {
					// sync not castle (rooks/king moved from default square)
					CastleSync.set(current);
					moveHistory.pieces[0] = 2;
				}
			}
		} else if (pieceNames[board[oldCol][oldRow]] == 'R') {
			if (current == -57 || current == -1 || current == 64 || current == 120) {
				// sync not castle (rooks/king moved from default square)
				CastleSync.set(current);
				moveHistory.pieces[0] = 2;
			}
		} else if (pieceNames[board[oldCol][oldRow]] == 'P' && oldCol != newCol && board[newCol][newRow] == -128) {
			// handle en passant
			moveHistory.pieces[0] = -128;
			moveHistory.pieces[3] = board[newCol][oldRow];
			pieces[board[newCol][oldRow]] = (byte) (64^pieces[board[newCol][oldRow]]);
			board[newCol][oldRow] = -128;
		}
		
		// update pieces array
		pieces[board[oldCol][oldRow]] = next;
		
		// update linked list of past moves
		moveHistory.pieces[1] = board[oldCol][oldRow];
		moveHistory.pieces[2] = current;
		
		// check and handle if capture
		if (board[newCol][newRow] != -128) {
			moveHistory.pieces[0] = (byte) (64 | moveHistory.pieces[0]);
			moveHistory.pieces[3] = board[newCol][newRow];
			pieces[board[newCol][newRow]] = (byte) (64^pieces[board[newCol][newRow]]);
		}
			
		// update board array
		board[newCol][newRow] = board[oldCol][oldRow];
		board[oldCol][oldRow] = -128;
		
		// check for and handle pawn promotion
		if (pieceNames[board[newCol][newRow]] == 'P' && (newRow == 7 || newRow == 0)) {
			char promo = player.choosePawnPromo();
			pieceNames[board[newCol][newRow]] = (byte) (0xFF&promo);
			moveHistory.pieces[0] = (byte) (1 | moveHistory.pieces[0]);
		}

		return true;
	}
	
	/**
	 * Used by Human to ensure the move does not put them in check. Automatically
	 * reverts the move if it does. 
	 * 
	 * @param c  player's colour
	 * @param current  the current (now old) position
	 * @param next  the new (current) position
	 * @return true if move is valid, false if move was reverted
	 */
	public boolean checkCheck (Colour c, byte current, byte next) {
		Colour o = (c == Colour.BLACK) ? Colour.WHITE : Colour.BLACK;
		if (calcCheck(o)) {
			if (o == Colour.BLACK) {
				moveHistory.pieces[0] = (byte) (32 | moveHistory.pieces[0]);
			} else {
				moveHistory.pieces[0] = (byte) (16 | moveHistory.pieces[0]);
			}
		}

		// ensure move does not put player in or keep in check
		if (inCheck(c)) {
			if (calcCheck(c)) {
				undoMove();
				return false;
			}
			// flag was in check
			if (c == Colour.BLACK) {
				moveHistory.pieces[0] = (byte) (32 | moveHistory.pieces[0]);
			} else {
				moveHistory.pieces[0] = (byte) (16 | moveHistory.pieces[0]);
			}
		} else if (calcCheck(c)) {
			undoMove();
			reCheck(c);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Helper function
	 * 
	 * @param c  the player's colour
	 * @return true if player is in check, false if not in check
	 */
	private boolean inCheck (Colour c) {
		return (c == Colour.BLACK) ? blackInCheck : whiteInCheck;
	}
	
	/**
	 * Helper function; flip-flops check flag
	 * 
	 * @param c  player's colour
	 */
	private void reCheck (Colour c) {
		if (c == Colour.BLACK) {
			blackInCheck = !blackInCheck;
		} else {
			whiteInCheck = !whiteInCheck;
		}
	}
	
	/**
	 * Calculates either blackInCheck or whiteInCheck. Must be called for each colour
	 * at least once a turn.
	 * 
	 * @param c  the player's colour.
	 * @return true if player is in check, false if they are not.
	 */
	public boolean calcCheck (Colour c) {
		boolean valid = false;
		Colour o = (c == Colour.BLACK) ? Colour.WHITE : Colour.BLACK;
		
		if (c == Colour.BLACK) {
			if ((64&pieces[0]) == 0) { 
				valid = true;
			} else {
				for (int i = 16; i < 32; i++) {
					if (validateMove(o,pieces[i],pieces[0]) &&
						boardMove(shell,pieces[i],pieces[0])) {
						valid = true;
						undoMove();
						break;
					}
				}
			}
			blackInCheck = valid;
		} else {
			if ((64&pieces[16]) == 0) {
				valid = true;
			} else {
				for (int i = 0; i < 16; i++) {
					if (validateMove(o,pieces[i],pieces[16]) &&
						boardMove(shell,pieces[i],pieces[16])) {
						valid = true;
						undoMove();
						break;
					}
				}
			}
			whiteInCheck = valid;
		}

		return valid;
	}
	
	/**
	 * Helper class; used to handle pawn promotions when calculating check.
	 */
	private class Shell implements PlayerInterface {
		char promoChar = 'N';
		public Colour getColour() { return Colour.BLACK; }
		public void makeMove () { }
		public char choosePawnPromo () {
			promoChar = (promoChar == 'N') ? 'Q' : 'N';
			return promoChar;
		}
	}
}