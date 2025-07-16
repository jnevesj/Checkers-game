import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



public class CheckersGUI extends JFrame {

	private int selectedRow = -1;
	private int selectedCol = -1;
	private PieceColor currentTurn = PieceColor.RED;
	
	private JButton restartButton;
	
	private JLabel turnLabel;
	private JPanel boardPanel;
	private JButton [][] buttons = new JButton [8][8];
	
	private int redCaptured = 0;
	private int blackCaptured = 0;
	private JLabel capturedLabel;
	
	private Board board = new Board();
	
	public CheckersGUI() {
		setTitle("Checkers Game");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600,600);
		setLayout(new BorderLayout());
		
		turnLabel = new JLabel("Red's turn to play");
		turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(turnLabel, BorderLayout.NORTH);
		
		capturedLabel = new JLabel("Red ate: 0 pieces | Black ate: 0 pieces");
		capturedLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		restartButton = new JButton("New Game");
		restartButton.addActionListener(e -> restartGame());
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(capturedLabel, BorderLayout.NORTH);
		bottomPanel.add(restartButton, BorderLayout.SOUTH);
		
		add(bottomPanel, BorderLayout.SOUTH);
		
		board.initializeBoard();
		initBoardUI();
		
		setVisible(true);
	}
	
	
	private void initBoardUI() {
		boardPanel = new JPanel();
		boardPanel.setLayout(new GridLayout(8,8));
		add(boardPanel, BorderLayout.CENTER);
		
		for(int row=0; row<8; row++) {
			for(int col=0; col<8; col++) {
				JButton button = new JButton();
				button.setMargin(new Insets(0,0,0,0));
				button.setFocusPainted(false);
				button.setOpaque(true);
				
				Color squareColor = (row + col) % 2 == 0 ? Color.LIGHT_GRAY : Color.DARK_GRAY;
				button.setBackground(squareColor);
				
				int r = row;
				int c = col;
				
				button.addActionListener(e -> handleClick(r, c));
				
				buttons[row][col] = button;
				boardPanel.add(button);
			}
		}
		
		updateBoardUI();
	}
	
	
	
	
	private void updateBoardUI() {
		
		for(int row=0; row<8; row++) {
			for(int col=0; col<8; col++) {
				Piece piece = board.getPiece(row, col);
				JButton button = buttons[row][col];
		
				if(piece == null) {
					button.setText("");
				} else {
					String label; 
						if( piece.getColor() == PieceColor.RED) {
							 label = piece.isKing() ? "👑🔴" : "🔴";
						}else {
							label = piece.isKing() ? "👑⚫️" : "⚫";
						}
					button.setText(label);
					button.setFont(new Font("Arial", Font.PLAIN, 24));
				}
			
			
			}
		}
	}
	
	
	private void handleClick(int row, int col) {
		
		if(currentTurn != PieceColor.RED)return;
		
		Piece clickedPiece = board.getPiece(row, col);
		
		if(selectedRow == -1) {
			if(clickedPiece != null && clickedPiece.getColor()== currentTurn) {
		
			selectedRow = row;
			selectedCol = col;
			highlightSelected(true);
			}
		}else {	
			int fromRow = selectedRow;
			int toRow = row;
			int fromCol = selectedCol;
			int toCol = col;
			
			if(fromRow == toRow && fromCol == toCol) {
				highlightSelected(false);
				selectedRow = -1;
				selectedCol = -1;
				return;
			}
			highlightSelected(false);
			selectedRow = -1;
			selectedCol = -1;
			
			MoveResult result = board.movePiece(fromRow, fromCol, toRow, toCol);
			
				if(!result.moved)  {
					JOptionPane.showMessageDialog(this, "Invalid move.");
					return;
				}

					if(result.capture) {
					//checking captured
						Piece movedPiece = board.getPiece(toRow, toCol);
						if(movedPiece.getColor()==PieceColor.RED) {
							blackCaptured++;
						}else {
							redCaptured++;
						}
						updateCapturedLabel();
						
						//Check for other food
						if(board.hasCaptureMove(toRow, toCol)) {
							selectedRow = toRow;
							selectedCol = toCol;
							highlightSelected(true);
							updateBoardUI();
							return; // no switching turns
						}
					}
					
					
					checkWinCondition();
					
					//AI's turn
						currentTurn = PieceColor.BLACK;
						turnLabel.setText("Black's Turn");
						updateBoardUI();
					// AI playing	
						SwingUtilities.invokeLater(() -> {
						boolean aiMoved = performAIMove();
						updateBoardUI();
						checkWinCondition();
						
						if(aiMoved) {
							currentTurn = PieceColor.RED;
							turnLabel.setText("Red's Turn");
						}
						});
			
		}
		
	}
	
	
	
	private void highlightSelected(boolean on) {
		for(int row=0; row<8; row++) {
			for(int col=0; col<8; col++) {
				
				JButton button = buttons[row][col];
				
				if(on && row == selectedRow && col == selectedCol) {
					buttons[row][col].setBackground(Color.YELLOW);
				}else {
					Color baseColor = (row + col) % 2 == 0 ? Color.LIGHT_GRAY : Color.DARK_GRAY;
					buttons[row][col].setBackground(baseColor);
				}
				
				button.repaint();
			}
	}
}		
	
	
	private void updateCapturedLabel() {
		capturedLabel.setText("Red ate: " + redCaptured + " pieces | Black ate: " + blackCaptured + " pieces");
	}
	
	
	private void restartGame() {
		board.initializeBoard();
		redCaptured = 0;
		blackCaptured = 0;
		currentTurn = PieceColor.RED;
		turnLabel.setText("Red's Turn");
		updateCapturedLabel();
		updateBoardUI();
	}
	
	private boolean performAIMove() {
		//defining movement offsets
		int [] movedOffsetRow = {1,1}; //normal move
		int [] movedOffsetCol = {-1,1}; 
		
		int [] jumpRowOffset = {2,2}; //eat move
		int [] jumpColOffset = {-2,2};
		
		//1.scan all black pieces
		for(int row=0; row<8; row++) {
			for(int col=0; col<8; col++) {
				Piece piece = board.getPiece(row, col);
				if(piece != null && piece.getColor()==PieceColor.BLACK) {
					//2. For each piece try all diagonal moves including eating
					for(int d=0; d<2; d++) {
					int newRow  = row + jumpRowOffset[d];
					int newCol = col + jumpColOffset[d];
					
						if(isInsideBoard(newRow, newCol)) {
							MoveResult result = board.movePiece(row, col, newRow, newCol);
							if(result.moved) {
								if(result.capture) {
								redCaptured++;
								updateCapturedLabel();
								}
								updateBoardUI();
								return true;
								
							}
						}
					}
				}
			}
		}
		
		//if no captures tries normal move
		for(int row=0; row<8; row++) {
			for(int col=0; col<8; col++) {
				Piece piece = board.getPiece(row, col);
				if(piece != null && piece.getColor()==PieceColor.BLACK) {
					for(int d = 0; d<2; d++) {
						int newRow = row + movedOffsetRow[d];
						int newCol = col + movedOffsetCol[d];
						
						if(isInsideBoard(newRow, newCol)) {
							MoveResult result = board.movePiece(row, col, newRow, newCol);
							if(result.moved) {
								updateCapturedLabel();
								updateBoardUI();
								return true;
							}
						}
					}
		
				}
			}
		}
		return false;
	}
	
	private boolean isInsideBoard(int row, int col) {
		return row >= 0 && row < 8 && col >= 0 && col < 8;
	}
	
	private void checkWinCondition() {
		int redRemaining = board.countPieces(PieceColor.RED);
		int blackRemaining = board.countPieces(PieceColor.BLACK);
		
		if(redRemaining == 0) {
			JOptionPane.showMessageDialog(this, "🎉 Black Wins!!");
		}else if(blackRemaining == 0){
			JOptionPane.showMessageDialog(this,  "🎉 Red Wins!!");
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new CheckersGUI();
	}

}
