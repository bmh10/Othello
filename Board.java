import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;


/*
 * Manages the game board
 */

public class Board {

	enum BoardType{Tiny, Standard, Giant};
	private static BoardType boardType;
	Point dimensions;
	private static Square[][] board;
	private static Square highlightedSquare;
	private int counter;
	private HashSet<Square> possibleFlips;
	
	
	private static final Point TinyBoard = new Point(6,6);
	private static final Point StandardBoard = new Point(8,8);
	private static final Point GiantBoard = new Point(12,12);
	
	/*
	 * Multiple constructors - one takes a board size, the other
	 * just uses whatever the board size is already set to
	 */
	Board() {
		switch (Board.boardType) {
		case Tiny:
			this.dimensions = TinyBoard; break;
		case Giant:
			this.dimensions = GiantBoard; break;
		default:
			this.dimensions = StandardBoard; break;
		}
		board = new Square[dimensions.x][dimensions.y];
		init();
	}
	
	Board(BoardType boardType) {
		Board.boardType = boardType;
		switch (Board.boardType) {
		case Tiny:
			this.dimensions = TinyBoard; break;
		case Giant:
			this.dimensions = GiantBoard; break;
		default:
			this.dimensions = StandardBoard; break;
		}
		board = new Square[dimensions.x][dimensions.y];
		init();
	}
	
	/*
	 * Sets up board at start of game
	 */
	private void init() {
		possibleFlips = new HashSet<Square>();
		
		for (int i = 0; i < dimensions.x; ++i) {
			for (int j = 0; j < dimensions.y; ++j) {
				board[i][j] = new Square(Square.SquareType.Empty, i, j, this);
			}
		}
		switch (boardType) {
		case Tiny:
			placePiece(new Square(Square.SquareType.Red, 2, 2, this));
			placePiece(new Square(Square.SquareType.Black, 3, 2, this));
			placePiece(new Square(Square.SquareType.Red, 3, 3, this));
			placePiece(new Square(Square.SquareType.Black, 2, 3, this));
			break;
		case Standard:
			placePiece(new Square(Square.SquareType.Red, 3, 3, this));
			placePiece(new Square(Square.SquareType.Black, 4, 3, this));
			placePiece(new Square(Square.SquareType.Red, 4, 4, this));
			placePiece(new Square(Square.SquareType.Black, 3, 4, this));
			break;
		case Giant:
			placePiece(new Square(Square.SquareType.Red, 5, 5, this));
			placePiece(new Square(Square.SquareType.Black, 6, 5, this));
			placePiece(new Square(Square.SquareType.Red, 6, 6, this));
			placePiece(new Square(Square.SquareType.Black, 5, 6, this));
			break;
		}
		
		setHighlightedSquare(0, 0);
		// Calculate possible moves with black to play
		CalculatePossibleMoves(true);
	}
	
	public Point getDimensions() {
		return dimensions;
	}
	
	public static void setType(BoardType boardType) {
		Board.boardType = boardType;
	}
	
	public static boolean isBoardTiny() {
		return Board.boardType == BoardType.Tiny; 
	}
	
	public static boolean isBoardStandard() {
		return Board.boardType == BoardType.Standard; 
	}
	
	public static boolean isBoardGiant() {
		return Board.boardType == BoardType.Giant; 
	}
	
	public Square getSquare(int x, int y) {
		return board[x][y];
	}
	
	public static void setHighlightedSquare(int x, int y) {
		Board.highlightedSquare = board[x][y];
	}
	
	public static Square getHighlightedSquare() {
		return highlightedSquare;
	}
	
	public static void placePieceOnHighlightedSquare(Square.SquareType sType) {
		SoundManager.PLACEPIECE.play();
		highlightedSquare.setType(sType);
		placePiece(highlightedSquare);
	}
	
	public static void placePiece(Square s) {
		board[s.getSquarePos().x][s.getSquarePos().y] = s;
	}
	
	public String getPossibleMovesAsString() {
		String s = "";
		for (int i = 0; i < dimensions.x; ++i) {
			for (int j = 0; j < dimensions.y; ++j) {
				if (board[i][j].isPossMove())
					s += "(" + i +", " + j + "), ";
			}
		}
		//if (s.endsWith(",")) remove final comma
		if (s.isEmpty()) s = "No possible moves detected.";
		return s;
	}
	
	public int getNumberOfPossibleMoves() {
		int count = 0;
		// Go through all squares in board
		for (int i = 0; i < dimensions.x; ++i) {
			for (int j = 0; j < dimensions.y; ++j) {
				// If square is a possible move increment counter
				if (board[i][j].isPossMove())
					count++;
			}
		}
		return count;
	}
	
	/*
	 * Checks if end of game has been reached (i.e when neither player has any legal moves they can make)
	 */
	public boolean checkForEndgame() {
		CalculatePossibleMoves(true);
		int blackMoves = getNumberOfPossibleMoves();
		CalculatePossibleMoves(false);
		int redMoves = getNumberOfPossibleMoves();
		
		if (blackMoves == 0 && redMoves == 0)
			return true;
		return false;
	}
	
	/*
	 * Calculates possible moves a given player can make given the current state of the board and returns list
	 */
	public void CalculatePossibleMoves(boolean blackToPlay) {
		// Go through all squares in board
		for (int i = 0; i < dimensions.x; ++i) {
			for (int j = 0; j < dimensions.y; ++j) {
				// Reset all squares (assume not a possible move)
				board[i][j].reset();
			}
		}
		
		// Reset all possible flips
		possibleFlips.clear();
		
		// Go through all squares in board and calculate possible moves
		for (int i = 0; i < dimensions.x; ++i) {
			for (int j = 0; j < dimensions.y; ++j) {
				// Black to play and found a black square
				if (blackToPlay && board[i][j].isBlack()) {
					checkForPlayer(false, i, j);
				}
				
				// Red to play and found a red square
				if (!blackToPlay && board[i][j].isRed()) {
					checkForPlayer(true, i, j);
				}
			}
		}
		
	}
	
	/*
	 * Checks  vertically, horizontally and diagonally for possible moves squares for either black or red player
	 */
	void checkForPlayer(boolean checkForBlacks, int i, int j) {
		counter = 0;
		possibleFlips.clear();
		//**Check vertical**\\
		// Check upwards
		for (int n = j-1; n >= 0; --n) {
			if (!check(i, n, checkForBlacks))
				break;
		}
		
		counter = 0;
		possibleFlips.clear();
		
		// Check downwards
		for (int n = j+1; n < dimensions.y; ++n) {
			if (!check (i, n, checkForBlacks))
				break;
		}
		
		counter = 0;
		possibleFlips.clear();
		
		//**Check horizontal**\\
		// Check to left
		for (int n = i-1; n >=0; --n) {
			if (!check(n, j, checkForBlacks))
			break;
		}

		counter = 0;
		possibleFlips.clear();
		
		// Check to right
		for (int n = i+1; n < dimensions.x; ++n) {
			if (!check(n, j, checkForBlacks))
			break;
		}
		
		counter = 0;
		possibleFlips.clear();
		
		//**Check diagonals**\\
		// Check North-East
		for (int n = 1; i+n < dimensions.x && j-n >= 0; ++n) {
			if (!check(i+n, j-n, checkForBlacks))
			break;
		}
		
		counter = 0;
		possibleFlips.clear();
		
		// Check North-West
		for (int n = 1; i-n >= 0 && j-n >= 0; ++n) {
			if (!check(i-n, j-n, checkForBlacks))
			break;
		}
		
		counter = 0;
		possibleFlips.clear();
		
		// Check South-East
		for (int n = 1; i+n < dimensions.x && j+n < dimensions.y; ++n) {
			if (!check(i+n, j+n, checkForBlacks))
			break;
		}
		
		counter = 0;
		possibleFlips.clear();
		
		// Check South-West
		for (int n = 1; i-n >= 0 && j+n < dimensions.y; ++n) {
			if (!check(i-n, j+n, checkForBlacks))
			break;
		}
	}
	
	/*
	 * Helper function for check for player (to avoid repeating code many times)
	 */
	private boolean check(int l, int r, boolean checkForBlacks) {
		if (checkForBlacks) {
			// If black increment counter and continue
			if (board[l][r].isBlack()) {
				counter++;
				possibleFlips.add(board[l][r]);
			}

			// If red break out of loop
			else if (board[l][r].isRed())
				return false;

			// If empty and count > 0 set square as possible move else return from function
			else if (board[l][r].isEmpty()) {
				if (counter > 0) {
					board[l][r].setPossMove(true);
					board[l][r].addToFlipSquares(possibleFlips);
				}
				return false;
			}
		}

		else {
			// If red increment counter and continue
			if (board[l][r].isRed()) {
				counter++;
				possibleFlips.add(board[l][r]);
			}

			// If black break out of loop
			else if (board[l][r].isBlack()) 
				return false;

			// If empty and count > 0 set square as possible move else return from function
			else if (board[l][r].isEmpty()) {
				if (counter > 0) {
					board[l][r].setPossMove(true);
					board[l][r].addToFlipSquares(possibleFlips);
				}
				return false;
			}

		}
		return true;
	}
	
	/*
	 * Returns number of specified coloured pieces on board
	 */
	public int getCount(boolean getBlackCount) {
		int count = 0;
		if (getBlackCount) {
			for (int i = 0; i < dimensions.x; ++i) {
				for (int j = 0; j < dimensions.y; ++j) {
					if (board[i][j].isBlack())
						count++;
				}
			}
		}
		else {
			for (int i = 0; i < dimensions.x; ++i) {
				for (int j = 0; j < dimensions.y; ++j) {
					if (board[i][j].isRed())
						count++;
				}
			}
		}
		
		return count;
	}
	
	/*
	 * Draws the board, the pieces and the highlights the selected square
	 */
	public void draw(Graphics g, Dimension winSize) {
		
		for (int i = 0; i < dimensions.x; ++i) {
			for (int j = 0; j < dimensions.y; ++j) {
				g.setColor(Color.getHSBColor(.5f, .5f, .5f));
				g.fillRect(i*Square.Size, j*Square.Size, Square.Size, Square.Size);
				g.setColor(Color.BLACK);
				g.drawRect(i*Square.Size, j*Square.Size, Square.Size, Square.Size);
			}
		}
		
		// Fill selected square
		g.setColor(Color.CYAN);
		Rectangle r = highlightedSquare.getBoundingRect(); 
		g.fill3DRect(r.x, r.y, r.width, r.height, true);
		
		// Draw pieces - delegated to square class
		for (int i = 0; i < dimensions.x; ++i) {
			for (int j = 0; j < dimensions.y; ++j) {
				board[i][j].draw(g);
				}
			}
		}
	
}
