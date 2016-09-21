package Chess;

import java.util.LinkedList;

public class Board {
	
	private static char[] columns;
	private static PlayerInterface p1,p2;
	private static PieceInterface king, queen, rook, bishop, knight, pawn;
	private boolean blackInCheck, whiteInCheck;
	public byte[][] board;
	public byte[] pieces;
	public OldPosition moveList;
	public OldPosition tempMove;
	public byte[] pieceNames;
	private byte turn;
	
	// http://cesarloachamin.github.io/2015/03/31/System-out-print-colors-and-unicode-characters/
	public static final String CYAN = "\u001B[36m";
	public static final String YELLOW = "\u001B[33m";
	public static final String RESET = "\u001B[0m";
	
	public Board () {
		setup();
		printBoard();
	}
	
	public boolean inCheck (byte colour) {
		if ((-128&colour) == -128) {
			return blackInCheck;
		} else {
			return whiteInCheck;
		}
	}
	
	public void noCheck (byte colour) {
		if ((-128&colour) == -128) {
			blackInCheck = false;
		} else {
			whiteInCheck = false;
		}
	}
	
	public OldPosition getHistory () {
		if (moveList == null)
			moveList = new OldPosition();
		return moveList;
	}
	
	private void setMove () {
		moveList = tempMove;
		tempMove = new OldPosition(moveList);
	}
	
	private void undoMove () {
		tempMove = moveList;
		moveList = moveList.next;
	}

	public boolean calcCheck (byte colour) {
		PieceInterface piece;
		setMove();
		if ((-128&colour) == -128) {
			for (byte i = 16; i < 32; i++) {
				try {
					if ((64&pieces[i]) == 64) {
						piece = getPiece(i);
						if (piece.move(this,pieces[i],(byte)((pieces[0]&56)>>3),(byte)(pieces[0]&7))) {
							undoTemp();
							undoMove();
							blackInCheck = true;
							return true;
						}
					}
				} catch (PawnPromotion e) {
					undoTemp();
					undoMove();
					blackInCheck = true;
					return true;
				}
			}
			blackInCheck = false;
		} else {
			for (byte i = 0; i < 16; i++) {
				try {
					if ((64&pieces[i]) == 64) {
						piece = getPiece(i);
						if (piece.move(this,pieces[i],(byte)((pieces[16]&56)>>3),(byte)(pieces[16]&7))) {
							undoTemp();
							undoMove();
							whiteInCheck = true;
							return true;
						}
					}
				} catch (PawnPromotion e) {
					undoTemp();
					undoMove();
					whiteInCheck = true;
					return true;
				}
			}
			whiteInCheck = false;
		}
		undoMove();
		return false;
	}
	
	private void setup () {
		columns = new char[8];
		for (int i = 0; i < 8; i++) {
			columns[i] = (char) (i+65);
		}
		
		pieces = new byte[32];
		// bits:
		// 7 = colour
		// 6 = inPlay
		// 3-5 = x coordinate
		// 0-2 = y coordinate
		pieces[0] = -25; // black king
		pieces[1] = -33; // black queen
		pieces[2] = -57; // black rook
		pieces[3] = -1;  // black rook
		pieces[4] = -41; // black bishop
		pieces[5] = -17; // black bishop
		pieces[6] = -49; // black knight
		pieces[7] = -9;  // black knight
		pieces[8] = -58; // black pawns
		pieces[9] = -50;
		pieces[10] = -42;
		pieces[11] = -34;
		pieces[12] = -26;
		pieces[13] = -18;
		pieces[14] = -10;
		pieces[15] = -2;
		pieces[16] = 96;  // white king
		pieces[17] = 88;  // white queen
		pieces[18] = 64;  // white rook
		pieces[19] = 120; // white rook
		pieces[20] = 80;  // white bishop
		pieces[21] = 104; // white bishop
		pieces[22] = 72;  // white knight
		pieces[23] = 112; // white knight
		pieces[24] = 65;  // white pawns
		pieces[25] = 73;
		pieces[26] = 81;
		pieces[27] = 89;
		pieces[28] = 97;
		pieces[29] = 105;
		pieces[30] = 113;
		pieces[31] = 121;
		
		pieceNames = new byte[32];
		pieceNames[0] = 75;
		pieceNames[1] = 81;
		pieceNames[2] = 82;
		pieceNames[3] = 82;
		pieceNames[4] = 66;
		pieceNames[5] = 66;
		pieceNames[6] = 78;
		pieceNames[7] = 78;
		pieceNames[16] = 75;
		pieceNames[17] = 81;
		pieceNames[18] = 82;
		pieceNames[19] = 82;
		pieceNames[20] = 66;
		pieceNames[21] = 66;
		pieceNames[22] = 78;
		pieceNames[23] = 78;
		for (int i = 0; i < 8; i++) {
			pieceNames[8+i] = 80;
			pieceNames[24+i] = 80;
		}
		
		board = new byte[8][8];
		for (int x = 0; x < 8; x++) {
			for (int y = 2; y < 6; y++) {
				board[x][y] = -128;
			}
		}
		for (int i = 0; i < 32; i++) {
			board[(pieces[i]&56)>>3][pieces[i]&7] = (byte) i;
		}

		whiteInCheck = false;
		blackInCheck = false;
		
		pawn = new Pawn();
		knight = new Knight();
		bishop = new Bishop();
		rook = new Rook();
		queen = new Queen();
		king = new King();
		
		tempMove = new OldPosition();
		
		turn = 0; // white to play
		System.out.println("black = " + CYAN + "CYAN" + RESET);
		System.out.println("white = " + YELLOW + "YELLOW" + RESET);
	}
	
	public void printBoard () {
		System.out.println();
		System.out.print(" ");
		for (int x=0; x < 8; x++) {
			System.out.print(columns[x]);
		}
		System.out.println();
		for (byte y=7; y >= 0; y--) {
			System.out.print(y+1);
			for (byte x=0; x < 8; x++) {
				if (board[x][y] == -128) {
					System.out.print(".");
				} else {
					if (board[x][y] < 16) {
						System.out.print(CYAN);
					} else {
						System.out.print(YELLOW);
					}
					System.out.print(getPiece(board[x][y]).getChar());
					System.out.print(RESET);
				}
			}
			System.out.println();
		}
		System.out.print(" ");
		for (int x=0; x < 8; x++) {
			System.out.print(columns[x]);
		}
		System.out.println("\n");
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
	
	public void setPlayers(PlayerInterface p1, PlayerInterface p2) {
		// p1 is white
		if (p1.getColour() == 0) {
			this.p1 = p1;
			this.p2 = p2;
		} else {
			this.p1 = p2;
			this.p2 = p1;
		}
	}
	
	public void go() {
		for ( ; ; ) {
			if (turn == 0) {
				p1.go();
			} else {
				p2.go();
			}
			
			turn = (turn == -128) ? (byte) 0 : (byte) -128;
			setMove();

			if (staleMate())
				break;
			if (checkMate(turn))
				break;
		}
	}
	
	public boolean staleMate() {
		if (blackInCheck || whiteInCheck)
			return false;
		PieceInterface pi;
		try {
			for (byte i = 0; i < 32; i++) {
				if ((64&pieces[i]) == 64) {
					pi = getPiece(i);
					if (pi.calcPossibleMoves(this,pieces[i])[0] != 0)
						return false;
				}
			}
		} catch (PawnPromotion e) {
			System.out.println("VERY BAD");
			System.exit(1);
		}
		System.out.println("stalemate");
		return true;
	}
	
	public boolean checkMate (byte colour) {
		PieceInterface pi;
		if ((-128&colour) == -128) {
			if (calcCheck(colour)) {
				try {
					for (byte i = 0; i < 16; i++) {
						if ((64&pieces[i]) == 64) {
							pi = getPiece(i);
							if (pi.calcPossibleMoves(this,pieces[i])[0] != 0)
								return false;
						}
					}
				} catch (PawnPromotion e) {
					System.out.println("BAD BAD NOT GOOD");
					System.exit(1);
				}
				System.out.println("white wins");
			} else {
				return false;
			}
		} else {
			if (calcCheck(colour)) {
				try {
					for (byte i = 16; i < 32; i++) {
						if ((64&pieces[i]) == 64) {
							pi = getPiece(i);
							if (pi.calcPossibleMoves(this,pieces[i])[0] != 0)
								return false;
						}
					}
				} catch (PawnPromotion e) {
					System.out.println("BAD BAD NOT GOOD");
					System.exit(1);
				}
				System.out.println("black wins");
			} else {
				return false;
			}
		}
		System.out.println("game over");
		return true;
	}
	
	public void undoTemp () {
		byte[] tmp = tempMove.pieces;
		
		board[(pieces[tmp[0]]&56)>>3][pieces[tmp[0]]&7] = -128;
		
		if (tmp[2] != -0) {
			if ((-64&tmp[2]) == -64) { // reverse castle kingside
				if ((-128&tmp[1]) == -128) {
					pieces[0] = -25;
					pieces[3] = -1;
					board[6][7] = -128;
					board[4][7] = 0;
					board[5][7] = -128;
					board[7][7] = 3;
				} else {
					pieces[16] = 96;
					pieces[18] = 120;
					board[6][0] = -128;
					board[4][0] = 16;
					board[5][0] = -128;
					board[7][0] = 19;
				}
			} else if ((-128&tmp[2]) == -128) { // reverse castle queenside
				if ((-128&tmp[1]) == -128) {
					pieces[0] = -25;
					pieces[2] = -57;
					board[2][7] = -128;
					board[4][7] = 0;
					board[3][7] = -128;
					board[0][7] = 2;
				} else {
					pieces[16] = 96;
					pieces[18] = 64;
					board[2][0] = -128;
					board[4][0] = 16;
					board[3][0] = -128;
					board[0][0] = 18;
				}
			} else if ((32&tmp[2]) == 32) {
				pieceNames[tmp[0]] = (byte) (0xFF&'P');
			} else {
				pieces[tmp[2]] = (byte) (64 | pieces[tmp[2]]);
				board[(pieces[tmp[2]]&56)>>3][pieces[tmp[2]]&7] = tmp[2];
			}
		}
		
		board[(tmp[1]&56)>>3][tmp[1]&7] = tmp[0];
		
		pieces[tmp[0]] = tmp[1];
	}
	
	public void tempBoard (byte oldCol, byte oldRow, byte newCol, byte newRow) {
		byte[] tmp = tempMove.pieces;
		
		if (tmp[2] != 0) {
			if ((-64&tmp[2]) == -64) { // kingside castle
				if ((-128&tmp[1]) == -128) {
					pieces[0] = -9;
					pieces[3] = -17;
					board[4][7] = -128;
					board[6][7] = 0;
					board[7][7] = -128;
					board[5][7] = 3;
				} else {
					pieces[16] = 112;
					pieces[18] = 104;
					board[4][0] = -128;
					board[6][0] = 16;
					board[7][0] = -128;
					board[5][0] = 19;
				}
			} else if ((-128&tmp[2]) == -128) { // queenside castle
				if ((-128&tmp[1]) == -128) {
					pieces[0] = -41;
					pieces[2] = -33;
					board[4][7] = -128;
					board[2][7] = 0;
					board[0][7] = -128;
					board[3][7] = 2;
				} else {
					pieces[16] = 80;
					pieces[18] = 88;
					board[4][0] = -128;
					board[2][0] = 16;
					board[0][0] = -128;
					board[3][0] = 18;
				}
			} else {
				pieces[tmp[2]] = (byte) (64 ^ pieces[tmp[2]]);
			}
		}
		
		if ((-128&pieces[tmp[0]]) == -128) {
			pieces[tmp[0]] = (byte) (-64 | (newCol << 3) | newRow);
		}  else {
			pieces[tmp[0]] = (byte) (64 | (newCol << 3) | newRow);
		}
		
		board[newCol][newRow] = tmp[0];
		board[oldCol][oldRow] = -128;
	}
	
	public boolean move(PlayerInterface player, byte[] aMove) {
		return move(player,pieces[board[aMove[0]][aMove[1]]],aMove[2],aMove[3]);
	}
	
	public boolean move(PlayerInterface player, byte current, byte newCol, byte newRow) {
		boolean moved = false;
		PieceInterface pi = getPiece(board[(current&56)>>3][current&7]);
		try {
			moved = pi.move(this,current,newCol,newRow);
		} catch (PawnPromotion e) {
			char promo = player.choosePawnPromo();
			pieceNames[board[(current&56)>>3][current&7]] = (byte) (0xFF&promo);
			moved = true;
		}
		return moved;
	}
	
}