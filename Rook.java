package Chess;

public class Rook implements PieceInterface {

	private Board b;

	public Rook (Board b) {
		this.b = b;
	}
	
	public char getChar() {
		return 'R';
	}
	
	public boolean validateMove (byte current, byte next) {
		int diffCol = ((56&next)>>3)-((56&current)>>3);
		int diffRow = (7&next)-(7&current);
		
		if (diffCol != 0 && diffRow != 0)
			return false;
		
		int vCol = (diffCol == 0) ? 0 : diffCol / Math.abs(diffCol);
		int vRow = (diffRow == 0) ? 0 : diffRow / Math.abs(diffRow);
		int x = ((56&current)>>3) + vCol;
		int y = (7&current) + vRow;
		while (x != ((56&next)>>3) || y != (7&next)) {
			if (b.board[x][y] != -128)
				return false;
			x += vCol;
			y += vRow;
		}
		
		// can capture, but not same colour
		if ((-128&current) == -128) {
			if (b.board[x][y] != -128 && b.board[x][y] < 16)
				return false;
		} else {
			if (b.board[x][y] > 15)
				return false;
		}
		
		return true;
	}
	
	public byte[] getMoves (byte current) {
		byte col = (byte)((current&56)>>3);
		byte row = (byte)(current&7);
		
		byte[] candidates = new byte[15]; // max plus one
		byte size = 0;
		
		// -57 = keep row
		// -8 = keep col
		int x = col+1;
		int y = row+1;
		
		if ((-128&current) == -128) {
			while (y < 8 && b.board[col][y] == -128)
				candidates[size++] = (byte) (-8&current | y++);
			if (y < 8 && b.board[col][y] > 15)
				candidates[size++] = (byte) (-8&current | y);
			while (x < 8 && b.board[x][row] == -128)
				candidates[size++] = (byte) (-57&current | (x++)<<3);
			if (x < 8 && b.board[x][row] > 15)
				candidates[size++] = (byte) (-8&current | x<<3);
			
			x = col-1;
			y = row-1;
			while (y >= 0 && b.board[col][y] == -128)
				candidates[size++] = (byte) (-8&current | y--);
			if (y >= 0 && b.board[col][y] > 15)
				candidates[size++] = (byte) (-8&current | y);
			while (x >= 0 && b.board[x][row] == -128)
				candidates[size++] = (byte) (-57&current | (x--)<<3);
			if (x >= 0 && b.board[x][row] > 15)
				candidates[size++] = (byte) (-57&current | x<<3);
		} else {
			while (y < 8 && b.board[col][y] == -128)
				candidates[size++] = (byte) (-8&current | y++);
			if (y < 8 && b.board[col][y] < 16)
				candidates[size++] = (byte) (-8&current | y);
			while (x < 8 && b.board[x][row] == -128)
				candidates[size++] = (byte) (-57&current | (x++)<<3);
			if (x < 8 && b.board[x][row] < 16)
				candidates[size++] = (byte) (-8&current | x<<3);
			
			x = col-1;
			y = row-1;
			while (y >= 0 && b.board[col][y] == -128)
				candidates[size++] = (byte) (-8&current | y--);
			if (y >= 0 && b.board[col][y] < 16)
				candidates[size++] = (byte) (-8&current | y);
			while (x >= 0 && b.board[x][row] == -128)
				candidates[size++] = (byte) (-57&current | (x--)<<3);
			if (x >= 0 && b.board[x][row] < 16)
				candidates[size++] = (byte) (-57&current | x<<3);
		}

		return candidates;
	}
}