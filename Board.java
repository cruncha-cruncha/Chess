package Chess;

import java.util.LinkedList;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Board {
	
	private static char[] columns;
	private static PieceInterface king, queen, rook, bishop, knight, pawn;
	private boolean blackInCheck, whiteInCheck;
	public byte[][] board;
	public byte[] pieces;
	public byte[] pieceNames;
	public OldPosition moveHistory;
	private boolean isUnix;
	
	// only works on unix systems
	// http://cesarloachamin.github.io/2015/03/31/System-out-print-colors-and-unicode-characters/
	public static final String CYAN = "\u001B[36m";
	public static final String YELLOW = "\u001B[33m";
	public static final String RESET = "\u001B[0m";
	
	public Board () {
		isUnix = detectUnix();
		setupBoard(); 
		printBoard();
	}
	
	private boolean detectUnix () {
		// https://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean setupBoard () {
		Scanner in = new Scanner(System.in);
		System.out.print("use default configuration? (y/n): ");
		String choice = in.next().toLowerCase();
		initVars();
		if (choice.equals("n")) {
			char[][] out = askBoard();
			if (out[0][0] == 'x') {
				System.out.println("Fatal Error: could not parse board");
				System.exit(0);
			}
			fillPieces(out);
		} else {
			fillPieces();
		}
		fillBoard();
		return true;
	}
	
	private void initVars () {
		pawn = new Pawn(this);
		knight = new Knight(this);
		bishop = new Bishop(this);
		rook = new Rook(this);
		queen = new Queen(this);
		king = new King(this);
		
		moveHistory = new OldPosition();
	}
	
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
		if (board[3][7] != 0)
			CastleSync.set((byte)-33);
		if (board[3][0] != 16)
			CastleSync.set((byte)88);
		if (board[0][7] == -128 || board[0][7] > 15 || pieceNames[board[0][7]] != 'R')
			CastleSync.set((byte)-57);
		if (board[7][7] == -128 || board[7][7] > 15 || pieceNames[board[7][7]] != 'R')
			CastleSync.set((byte)-1);
		if (board[0][0] < 16 || pieceNames[board[0][0]] != 'R')
			CastleSync.set((byte)64);
		if (board[7][0] < 16 || pieceNames[board[7][0]] != 'R')
			CastleSync.set((byte)120);

		calcCheck(Colour.BLACK);
		calcCheck(Colour.WHITE);
		
		if (isUnix) {
			System.out.println("black pieces are " + CYAN + "CYAN" + RESET);
			System.out.println("white pieces are " + YELLOW + "YELLOW" + RESET);
		} else {
			System.out.println("black pieces are UPPERCASE");
			System.out.println("white pieces are lowercase");
		}
	}
	
	private void fillPieces () {
		// black then white
		byte[] filler = {-25,-33,-57,-1,-41,-17,-49,-9,-58,-50,-42,-34,-26,-18,-10,-2,
		               96,88,64,120,80,104,72,112,65,73,81,89,97,105,113,121};
		pieces = new byte[32];
		for (int i = 0; i < 32; i++)
			pieces[i] = filler[i];
		
		byte[] names = {'K','Q','R','R','B','B','N','N','P','P','P','P','P','P','P','P'};
		pieceNames = new byte[32];
		for (int i = 0; i < 16; i++) {
			pieceNames[i] = names[i];
			pieceNames[i+16] = names[i];
		}
	}
	
	private void fillPieces (char[][] config) {
		pieces = new byte[32];
		pieceNames = new byte[32];
		// bits:
		// 7 = colour (0 = white, 1 = black)
		// 6 = inPlay (0 = not in play (captured), 1 = in play)
		// 5-3 = x coordinate
		// 2-0 = y coordinate
		
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
					pieces[index] = (byte) (pieces[index] | (1<<6)); // IS THIS WORKING?? CRASHES WHEN 64 USED INSTEAD
					pieces[index] = (byte) (pieces[index] | (i<<3));
					pieces[index] = (byte) (pieces[index] | (j));
					pieceNames[index] = (byte) pieceName;
				}
			}
		}
	}
	
	private char[][] askBoard () {
		System.out.println("<loading swing>");
		char[][] out = new char[8][8];
		AtomicBoolean paused = new AtomicBoolean(true); // is this skookum?
		
		JFrame mainFrame = new JFrame("enter board configuration");
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainFrame.setSize(340,205);
		mainFrame.setLayout(new FlowLayout());
		
		JPanel input = new JPanel();
		String[][] data = {{"8","","","","","","","",""},
						   {"7","","","","","","","",""},
						   {"6","","","","","","","",""},
						   {"5","","","","","","","",""},
						   {"4","","","","","","","",""},
						   {"3","","","","","","","",""},
						   {"2","","","","","","","",""},
						   {"1","","","","","","","",""},
						   {"","A","B","C","D","E","F","G","H"}};
		String[] pos = {"","A","B","C","D","E","F","G","H"};
		DefaultTableModel model = new DefaultTableModel(data,pos);
		JTable table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int i = 0; i < 9; i++)
			table.getColumnModel().getColumn(i).setPreferredWidth(10);
		table.putClientProperty("terminateEditOnFocusLost", true);
		table.setGridColor(Color.black);
		input.add(table);
		mainFrame.add(input);
		
		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
		JTextArea instructions = new JTextArea(1,8);
		instructions.setEditable(false);
		instructions.append("white = lowercase \n");
		instructions.append("black = UPPERCASE \n");
		instructions.append("pawn = P/p \n");
		instructions.append("knight = N/n \n");
		instructions.append("bishop = B/b \n");
		instructions.append("rook = R/r \n");
		instructions.append("queen = Q/q \n");
		instructions.append("king = K/k");
		options.add(instructions);
		JButton done = new JButton("done");
		options.add(done);
		mainFrame.add(options);
		
		done.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				for (int i = 0; i < 8; i++) {
					for (int j = 0; j < 8; j++) {
						String s = (String) table.getValueAt(7-j,i+1);
						if (!s.equals("")) {
							out[i][j] = s.charAt(0);
						}
					}
				}
				
				int[] count = new int[12];
				String key = "kqrbnpKQRBNP";
				outer:
				for (int i = 0; i < 8; i++) {
					for (int j = 0; j < 8; j++) {
						char c = out[i][j];
						if (c != 0) {
							int index = key.indexOf(c);
						if ((index == -1 ) || ((c == 'P' || c == 'p') && (j == 0 || j == 7))) {
								// if an unkown character is present, discard the board
								// also, cannot have pawn on the back ranks
								out[0][0] = 'x';
								paused.set(false);
								return;
							} else {
								count[index] += 1;
							}
						}
					}
				}
				
				mainFrame.dispose();
				
				// must have a king of each color, and no more pieces than allowed
				if (count[0] != 1 || count[6] != 1 || count[1] > 9 || count[7] > 9 || count[2] > 10 || count[8] > 10 ||
					count[3] > 10 || count[9] > 10 || count[4] > 10 || count[10] > 10 || count[5] > 8 || count[11] > 8 ||
				    count[1]+count[2]+count[3]+count[4]+count[5] > 15 || count[7]+count[8]+count[9]+count[10]+count[11] > 15) {
					out[0][0] = 'x';
					paused.set(false);
					return;
				}
				
				paused.set(false);
			}
		});
		
		mainFrame.setVisible(true);
		
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) { }
			if (!paused.get())
				break;
		}
		return out;
	}
	
	public void printBoard () {
		System.out.println();
		System.out.print(" ");
		for (int x=0; x < 8; x++) {
			System.out.print(" ");
			System.out.print(columns[x]);
		}
		System.out.println();
		for (byte y=7; y >= 0; y--) {
			System.out.print(y+1);
			for (byte x=0; x < 8; x++) {
				System.out.print(" ");
				if (board[x][y] == -128) {
					System.out.print((char)183);
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
		System.out.print(" ");
		for (int x=0; x < 8; x++) {
			System.out.print(" ");
			System.out.print(columns[x]);
		}
		System.out.println("\n");
	}

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
	
	public PieceInterface getPiece (byte index) {
		switch (pieceNames[index]) {
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
		} else {
			board[(56&pieces[p1])>>3][7&pieces[p1]] = -128;
		}
		
		board[(56&p2)>>3][7&p2] = p1;
		pieces[p1] = p2;
		
		// we were in check
		if ((32&p0) == 32) {
			if (p1 < 16) {
				reCheck(Colour.BLACK);
			} else {
				reCheck(Colour.WHITE);
			}
		}
		
		// we put them in check
		if ((16&p0) == 16) {
			if (p1 < 16) {
				reCheck(Colour.WHITE);
			} else {
				reCheck(Colour.BLACK);
			}
		}
		
		moveHistory = moveHistory.prev;
		return true;
	}
	
	public boolean validateMove (Colour c, byte current, byte next) {
		if ((64&current) != 64 || current == next)
			return false;
		PieceInterface pi = getPiece(board[(56&current)>>3][7&current]);
		if (c == Colour.BLACK) {
			if ((-128&current) == 0)
				return false;
		} else {
			if ((-128&current) == -128)
				return false;
		}
		return pi.validateMove(current,next);
	}
	
	public boolean boardMove (PlayerInterface player, Colour c, byte current, byte next) {
		byte oldCol = (byte) ((56&current)>>3);
		byte oldRow = (byte) (7&current);
		byte newCol = (byte) ((56&next)>>3);
		byte newRow = (byte) (7&next);
		PieceInterface pi = getPiece(board[oldCol][oldRow]);
		moveHistory = new OldPosition(moveHistory);
		
		// check for and handle castling
		if (pieceNames[board[oldCol][oldRow]] == 'K') {
			if (Math.abs(newCol-oldCol) == 2) {
				if (inCheck(c)) {
					return false;
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
							CastleSync.set((byte)-1);
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
					// sync not castle
					CastleSync.set(current);
					moveHistory.pieces[0] = 2;
				}
			}
		} else if (pieceNames[board[oldCol][oldRow]] == 'R') {
			if (current == -57 || current == -1 || current == 64 || current == 120) {
				// sync not castle
				CastleSync.set(current);
				moveHistory.pieces[0] = 2;
			}
		} else if (pieceNames[board[oldCol][oldRow]] == 'P' && oldCol != newCol && board[newCol][newRow] == -128) {
			// catch and handle en passant
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
	
	public boolean checkCheck (Colour c, byte current, byte next) {
		// ensure move does not put player in or keep in check
		if (inCheck(c)) {
			if (calcCheck(c)) {
				undoMove();
				return false;
			}
			// flag was in check
			moveHistory.pieces[0] = (byte) (32 | moveHistory.pieces[0]);
		} else if (calcCheck(c)) {
			undoMove();
			reCheck(c);
			return false;
		}
		
		// flag if move put other player into check
		Colour o = (c == Colour.BLACK) ? Colour.WHITE : Colour.BLACK;
		if (calcCheck(o))
			moveHistory.pieces[0] = (byte) (16 | moveHistory.pieces[0]);
		
		return true;
	}
	
	public boolean gameOver () {
		// something something Computer
		return true;
	}
	
	// return if player is currently in check
	private boolean inCheck (Colour c) {
		return (c == Colour.BLACK) ? blackInCheck : whiteInCheck;
	}
	
	// flip flop check status
	private void reCheck (Colour c) {
		if (c == Colour.BLACK) {
			blackInCheck = !blackInCheck;
		} else {
			whiteInCheck = !whiteInCheck;
		}
	}
	
	// calculate if player is in check
	private boolean calcCheck (Colour c) {
		Shell shell = new Shell();
		boolean valid = false;
		Colour o = (c == Colour.BLACK) ? Colour.WHITE : Colour.BLACK;
		
		if (c == Colour.BLACK) {
			for (int i = 16; i < 32; i++) {
				if (validateMove(o,pieces[i],pieces[0]) &&
					boardMove(shell,o,pieces[i],pieces[0])) {
					valid = true;
					for (int b = 0; b < 16; b++) {
						if (validateMove(c,pieces[b],pieces[16])) {
							valid = false;
							break;
						}
					}
					undoMove();
					if (valid) {break;}
				}
			}
			blackInCheck = valid;
		} else {
			for (int i = 0; i < 16; i++) {
				if (validateMove(o,pieces[i],pieces[16]) &&
					boardMove(shell,o,pieces[i],pieces[16])) {
					valid = true;
					for (int b = 16; b < 32; b++) {
						if (validateMove(c,pieces[b],pieces[0])) {
							valid = false;
							break;
						}
					}
					undoMove();
					if (valid) {break;}
				}
			}
			whiteInCheck = valid;
		}

		return valid;
	}
	
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