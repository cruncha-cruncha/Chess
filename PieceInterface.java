package Chess;

public interface PieceInterface {
	public char getChar ();
	public boolean validateMove (byte current, byte next);
	public byte[] getMoves (byte current);
}