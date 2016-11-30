package Chess;

public class Bishop implements PieceInterface {
	
	private Board b;
	
	public Bishop (Board b) {
		this.b = b;
	}
	
	public char getChar() {
		return 'B';
	}
	
	public boolean validateMove (byte current, byte next) {
		int diffCol = ((56&next)>>3)-((56&current)>>3);
		int diffRow = (7&next)-(7&current);
		
		if (Math.abs(diffCol) != Math.abs(diffRow))
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
		
		byte[] candidates = new byte[14];
		int size = 0;
		
		// -57 = keep row
		// -8 = keep col
		int x = col+1;
		int y = row+1;
		
		if ((-128&current) == -128) {
			while (x < 8 && y < 8 && b.board[x][y] == -128)
				candidates[size++] = (byte) (-64&current | (x++)<<3 | y++);
			if (x < 8 && y < 8 && b.board[x][y] > 15)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
			
			x = col+1;
			y = row-1;
			while (x < 8 && y >= 0 && b.board[x][y] == -128) 
				candidates[size++] = (byte) (-64&current | (x++)<<3 | y--);
			if (x < 8 && y >= 0 && b.board[x][y] > 15)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
				
			x = col-1;
			y = row+1;
			while (x >= 0 && y < 8 && b.board[x][y] == -128)
				candidates[size++] = (byte) (-64&current | (x--)<<3 | y++);
			if (x >= 0 && y < 8 && b.board[x][y] > 15)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
				
			x = col-1;
			y = row-1;
			while (x >= 0 && y >= 0 && b.board[x][y] == -128)
				candidates[size++] = (byte) (-64&current | (x--)<<3 | y--);
			if (x >= 0 && y >= 0 && b.board[x][y] > 15)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
		} else {
			while (x < 8 && y < 8 && b.board[x][y] == -128)
				candidates[size++] = (byte) (-64&current | (x++)<<3 | y++);
			if (x < 8 && y < 8 && b.board[x][y] < 16)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
			
			x = col+1;
			y = row-1;
			while (x < 8 && y >= 0 && b.board[x][y] == -128) 
				candidates[size++] = (byte) (-64&current | (x++)<<3 | y--);
			if (x < 8 && y >= 0 && b.board[x][y] < 16)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
				
			x = col-1;
			y = row+1;
			while (x >= 0 && y < 8 && b.board[x][y] == -128)
				candidates[size++] = (byte) (-64&current | (x--)<<3 | y++);
			if (x >= 0 && y < 8 && b.board[x][y] < 16)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
				
			x = col-1;
			y = row-1;
			while (x >= 0 && y >= 0 && b.board[x][y] == -128)
				candidates[size++] = (byte) (-64&current | (x--)<<3 | y--);
			if (x >= 0 && y >= 0 && b.board[x][y] < 16)
				candidates[size++] = (byte) (-64&current | x<<3 | y);
		}
		
		return candidates;
	}
}