package Chess;

import java.util.LinkedList;

public class Board {
	
	public PieceAbstract[][] white, black;
	private char[] columns;
	private boolean blackInCheck, whiteInCheck;
	public PieceAbstract whiteKing, blackKing;
	public LinkedList<OldPosition> moveList;
	private Colour turn;
	private PlayerInterface p1,p2;

	// http://cesarloachamin.github.io/2015/03/31/System-out-print-colors-and-unicode-characters/
	public static final String CYAN = "\u001B[36m";
	public static final String YELLOW = "\u001B[33m";
	//Reset code
	public static final String RESET = "\u001B[0m";
	
	public Board () {
		setup();
		printBoard();
	}
	
	public PieceAbstract getKing (Colour colour) {
		if (colour == Colour.BLACK) {
			return blackKing;
		} else {
			return whiteKing;
		}
	}
	
	public boolean inCheck (Colour colour) {
		if (colour == Colour.BLACK) {
			return blackInCheck;
		} else {
			return whiteInCheck;
		}
	}
	
	public void noCheck (Colour colour) {
		if (colour == Colour.BLACK) {
			blackInCheck = false;
		} else {
			whiteInCheck = false;
		}
	}

	public boolean calcCheck (Colour colour) {
		PieceAbstract piece;
		if (colour == Colour.BLACK) {
			piece = whiteKing;
			while (piece != null) {
				try {
					if (piece.move(blackKing.getRow(),blackKing.getCol())) {
						piece.undoMove();
						return true;
					}
				} catch (PawnPromotion p) {
					piece.undoMove();
					return true;
				}
				piece = piece.next;
			}
			
		} else {
			piece = blackKing;
			while (piece != null) {
				try {
					if (piece.move(whiteKing.getRow(),whiteKing.getCol())) {
						piece.undoMove();
						return true;
					}
				} catch (PawnPromotion p) {
					piece.undoMove();
					return true;
				}
				piece = piece.next;
			}
		}
		return false;
	}
	
	private void setup () {
		columns = new char[8];
		for (int i = 0; i < 8; i++) {
			columns[i] = (char) (i+65);
		}
		white = new PieceAbstract[8][8];
		black = new PieceAbstract[8][8];
		white[1][0]=new Pawn(this,Colour.WHITE,1,0);
		black[6][0]=new Pawn(this,Colour.BLACK,6,0);
		for (int i = 1; i < 8; i++) {
			white[1][i]=new Pawn(this,Colour.WHITE,1,i,white[1][i-1]);
			black[6][i]=new Pawn(this,Colour.BLACK,6,i,black[6][i-1]);
		}
		white[0][0] = new Rook(this,Colour.WHITE,0,0,white[1][7]);
		white[0][7] = new Rook(this,Colour.WHITE,0,7,white[0][0]);
		white[0][1] = new Knight(this,Colour.WHITE,0,1,white[0][7]);
		white[0][6] = new Knight(this,Colour.WHITE,0,6,white[0][1]);
		white[0][5] = new Bishop(this,Colour.WHITE,0,5,white[0][6]);
		white[0][2] = new Bishop(this,Colour.WHITE,0,2,white[0][5]);
		white[0][3] = new Queen(this,Colour.WHITE,0,3,white[0][2]);
		white[0][4] = new King(this,Colour.WHITE,0,4,white[0][3]);
		whiteKing = white[0][4];
		whiteInCheck = false;
		black[7][0] = new Rook(this,Colour.BLACK,7,0,black[6][7]);
		black[7][7] = new Rook(this,Colour.BLACK,7,7,black[7][0]);
		black[7][1] = new Knight(this,Colour.BLACK,7,1,black[7][7]);
		black[7][6] = new Knight(this,Colour.BLACK,7,6,black[7][1]);
		black[7][2] = new Bishop(this,Colour.BLACK,7,2,black[7][6]);
		black[7][5] = new Bishop(this,Colour.BLACK,7,5,black[7][2]);
		black[7][3] = new Queen(this,Colour.BLACK,7,3,black[7][5]);
		black[7][4] = new King(this,Colour.BLACK,7,4,black[7][3]);
		blackKing = black[7][4];
		blackInCheck = false;
		moveList = new LinkedList<OldPosition>();
		turn = Colour.WHITE;
		System.out.println("black = " + CYAN + "CYAN" + RESET);
		System.out.println("white = " + YELLOW + "YELLOW" + RESET);
		System.out.println(" ");
	}
	
	public void printBoard () {
		System.out.println();
		System.out.print(" ");
		for (int x=0; x < 8; x++) {
			System.out.print(columns[x]);
		}
		System.out.println();
		for (int x=7; x >= 0; x--) {
			System.out.print(x+1);
			for (int y=0; y < 8; y++) {
				if (white[x][y] != null) {
					System.out.print(YELLOW);
					System.out.print(white[x][y].printChar());
					System.out.print(RESET);
				} else if (black[x][y] != null) {
					System.out.print(CYAN);
					System.out.print(black[x][y].printChar());
					System.out.print(RESET);
				} else {
					System.out.print(".");
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
	
	public boolean pieceAt(Colour colour, int row, int col) {
		if (colour == Colour.BLACK) {
			if (black[row][col] != null)
				return true;
		} else {
			if (white[row][col] != null)
				return true;
		}
		return false;
	}
	
	public void undo() {
		try {
			moveList.getFirst().getPiece().undoMove();
			turn = (turn == Colour.WHITE) ? Colour.BLACK : Colour.WHITE;
		} catch (java.util.NoSuchElementException e) {
			// do nothing
		}
	}
	
	public void setPlayers(PlayerInterface p1, PlayerInterface p2) {
		if (p1.getColour() == Colour.WHITE) {
			this.p1 = p1;
			this.p2 = p2;
		} else {
			this.p1 = p2;
			this.p2 = p1;
		}
	}
	
	public void go() {
		for ( ; ; ) {
			if (turn == Colour.WHITE) {
				p1.go();
			} else {
				p2.go();
			}
			// check stalemate and checkmate conditions
			if (staleMate())
				break;
			if (checkMate(turn))
				break;
		}
	}
	
	public boolean staleMate() {
		if (blackInCheck || whiteInCheck)
			return false;
		int[][] moves;
		PieceAbstract piece = blackKing;
		while (piece != null) {
			moves = piece.calcPossibleMoves();
			if (moves.length > 0)
				return false;
			piece = piece.next;
		}
		piece = whiteKing;
		while (piece != null) {
			moves = piece.calcPossibleMoves();
			if (moves.length > 0)
				return false;
			piece = piece.next;
		}
		System.out.println("Stalemate!");
		return true;
	}
	
	public boolean checkMate (Colour colour) {
		if (colour == Colour.BLACK) {
			if (blackInCheck) {
				PieceAbstract piece = blackKing;
				int[][] moves;
				while (piece != null) {
					moves = piece.calcPossibleMoves();
					if (moves.length > 0)
						return false;
					piece = piece.next;
				}
			} else {
				return false;
			}
		} else {
			if (whiteInCheck) {
				PieceAbstract piece = whiteKing;
				int[][] moves;
				while (piece != null) {
					moves = piece.calcPossibleMoves();
					if (moves.length > 0)
						return false;
					piece = piece.next;
				}
			} else {
				return false;
			}
		}
		System.out.println(colour);
		System.out.println("Game over");
		return true;
	}
	
	public boolean move(PlayerInterface player, int[] aMove) {
		return move(player,aMove[0],aMove[1],aMove[2],aMove[3]);
	}
	
	public boolean move(PlayerInterface player, int oldRow, int oldCol, int newRow, int newCol) {
		boolean moved = false;
		if (player.getColour() == turn) {
			PieceAbstract piece = (player.getColour() == Colour.WHITE) ? white[oldRow][oldCol] : black[oldRow][oldCol];
			if (piece != null) {
				try {
					moved = piece.move(newRow,newCol);
				} catch (PawnPromotion e) {
					String promo = player.choosePawnPromo();
					Pawn pawn = (Pawn) e.pawn;
					PieceAbstract[][] board = (pawn.getColour() == Colour.WHITE) ? white : black;
					
					PieceAbstract l = (pawn.getColour() == Colour.BLACK) ? blackKing : whiteKing;
					PieceAbstract p = l.next;
					while (p != null) {
						if (p == pawn) {
							break;
						} else {
							l = p;
							p = p.next;
						}
					}
									
					if (promo.equals("Q")) {
						l.next = new Queen(this,pawn.getColour(),pawn.getRow(),pawn.getCol(),p.next);
						l.next.fillHistory(pawn.getHistory(),pawn.getCaptured());
						board[pawn.getRow()][pawn.getCol()] = l.next;
					} else if (promo.equals("N")) {
						l.next = new Knight(this,pawn.getColour(),pawn.getRow(),pawn.getCol(),p.next);
						l.next.fillHistory(pawn.getHistory(),pawn.getCaptured());
						board[pawn.getRow()][pawn.getCol()] = l.next;
					} else if (promo.equals("B")) {
						l.next = new Bishop(this,pawn.getColour(),pawn.getRow(),pawn.getCol(),p.next);
						l.next.fillHistory(pawn.getHistory(),pawn.getCaptured());
						board[pawn.getRow()][pawn.getCol()] = l.next;
					} else if (promo.equals("R")) {
						l.next = new Rook(this,pawn.getColour(),pawn.getRow(),pawn.getCol(),p.next);
						l.next.fillHistory(pawn.getHistory(),pawn.getCaptured());
						board[pawn.getRow()][pawn.getCol()] = l.next;
					}
					
					moved = true;
				}
			}
		}
		if (moved)
			turn = (turn == Colour.WHITE) ? Colour.BLACK : Colour.WHITE;
		return moved;
	}
	
}