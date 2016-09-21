package Chess;

public interface PieceInterface {
	public boolean move (Board b, byte current, byte newCol, byte newRow) throws PawnPromotion;
	public char getChar ();
	public byte[] calcPossibleMoves (Board b, byte current) throws PawnPromotion;
}