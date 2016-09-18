package Chess;

public interface PieceInterface {
	public boolean move(int r, int c) throws PawnPromotion;
	public PieceAbstract undoMove();
	public int[] getCoor();
	public int getRow();
	public int getCol();
	public int getOldRow();
	public int getOldCol();
	public Colour getColour();
	public String printChar();
	public Board getBoard();
	public boolean fillHistory(OldPosition op, PieceAbstract captured);
	public int[][] calcPossibleMoves();
}