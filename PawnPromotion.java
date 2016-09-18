package Chess;

public class PawnPromotion extends Exception {
	public PieceAbstract pawn;
	public PawnPromotion (PieceAbstract pawn) {
		this.pawn = pawn;
	}
}