
public class Board {

	private Piece [][] board;
	
	public Board() {
		board = new Piece [8][8]; 
	}
	
	public void initializeBoard() {
		for(int row =0; row<8; row++) {
			for(int col=0; col<8; col++) {
				if((col+row)%2 != 0) {
					if(row<3) {
						//place black piece
						board [row][col] = new Piece(PieceColor.BLACK, false);
					}
					else if(row>4) {
						//place red piece
						board [row][col] = new Piece(PieceColor.RED, false);
					}
					else {
						//place no piece
						board [row][col] = null;
					}
				} else {
						board[row][col] = null;
					}
			}
		}
	}
	
	
	
	
	public Piece getPiece(int row, int col) {
		return board[row][col];
		
	}
	
	public MoveResult movePiece(int fromRow, int fromCol, int toRow, int toCol) {
		
		//checking if piece exists & check if destination is empty
		Piece piece = board[fromRow][fromCol];
		if(piece == null || board[toRow][toCol] != null) {
			return new MoveResult(false, false);
		}
		
		int rowDiff = toRow - fromRow;
		int colDiff = toCol - fromCol;
		
		//Normal forward move (1 step diagonally)
		if(Math.abs(rowDiff) == 1 && Math.abs(colDiff) == 1) {
			if(isForwardMove(piece.getColor(), rowDiff) || piece.isKing()) {
				board[toRow][toCol] = piece;
				board[fromRow][fromCol] = null;
				
				//Promoting to king
				if((piece.getColor() == PieceColor.RED && toRow == 0) || 
						(piece.getColor() == PieceColor.BLACK && toRow == 7) ) {
					piece.makeKing();
				}
				
				return new MoveResult(true, false); // no capture
			}
		} 
		
		//Move to eat
		if(Math.abs(rowDiff) == 2 && Math.abs(colDiff) == 2) {
			
			int jumpedRow = fromRow + rowDiff / 2;
			int jumpedCol = fromCol + colDiff / 2;
			
			
			Piece jumpedPiece = board[jumpedRow][jumpedCol];
			
			if(jumpedPiece != null && jumpedPiece.getColor() != piece.getColor()) {
				//eating is valid
				board[toRow][toCol] = piece;
				board[fromRow][fromCol] = null;
				board[jumpedRow][jumpedCol] = null;
				
				
				//Promote to king
				if((piece.getColor() == PieceColor.RED && toRow == 0) || 
						(piece.getColor() == PieceColor.BLACK && toRow == 7) ) {
					piece.makeKing();
				}
				
				return new MoveResult(true, true); // yes capture
			}
			
		}
		
			return new MoveResult(false, false);
		
		
}	
	private boolean isForwardMove(PieceColor color, int rowDiff) {
		return (color == PieceColor.RED && rowDiff < 0) || (color == PieceColor.BLACK && rowDiff > 0);
	}
	
	
	public int countPieces(PieceColor color) {
		int count = 0;
		for(int row=0; row<8; row++) {
			for(int col = 0; col < 8; col++) {
				Piece p = board[row][col];
				if(p != null && p.getColor() == color) {
					count++;
				}
			}
		}
		
		return count;
	}
	
	
	public boolean hasCaptureMove(int row, int col) {
		
		Piece piece = board[row][col];
		if(piece == null) {
			return false;
		}
		
		
		int[] rowOffsets = {-2,-2,2,2};
		int[] colOffsets = {-2,2,-2,2};
		
		for(int i=0; i<4; i++) {
			int newRow = row + rowOffsets[i];
			int newCol = col + colOffsets[i];
			
			if(isValidIndex(newRow, newCol)) {
				int jumpedRow = row + rowOffsets[i] / 2;
				int jumpedCol = col + colOffsets[i] / 2;
				
				if(board[newRow][newCol]==null) {
					Piece jumped = board[jumpedRow][jumpedCol];
					if(jumped != null && jumped.getColor() != piece.getColor()) {
						//check movement for non-kings
						int rowDiff = newRow - row;
						if(piece.isKing() || isForwardMove(piece.getColor(), rowDiff)) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
		
	}
	
	private boolean isValidIndex(int row, int col) {
		return row >= 0 && row < 8 && col >= 0 && col < 8;
	}
	
	
}
