package Chess;

public interface PlayerInterface {
	public void go();
	public char choosePawnPromo();
	public byte getColour();
	public void setColour(byte colour);
}