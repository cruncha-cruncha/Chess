package Chess;

public interface PlayerInterface {
	public void go();
	public String choosePawnPromo();
	public Colour getColour();
	public void setColour(Colour colour);
}