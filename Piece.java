
public class Piece {

	private PieceColor color;
	private boolean isKing;
	
	
	public Piece(PieceColor color, boolean isKing) {
		this.color = color;
		this.isKing = isKing;
	}
	
	public PieceColor getColor() {
		return color;
	}
	
	public boolean isKing() {
		return isKing;
	}
	
	public void makeKing() {
		this.isKing = true;
	}
}
