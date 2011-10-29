import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


/*
 * Represents a single square within the game board
 */

public class Square {

	public enum SquareType{Black, Red, Empty};
	SquareType sType;
	Point pos;
	public static int Size = 60;
	boolean possibleMove;
	private HashSet<Square> toFlip;
	
	Square(SquareType sType, int x, int y, Board board) {
		this.sType = sType;
		this.pos = new Point(x, y);
		this.possibleMove = false;
		this.toFlip = new HashSet<Square>();
	}
	
	public SquareType getSquareType() {
		return sType;
	}
	
	public boolean isEmpty() {
		return sType == SquareType.Empty;
	}
	
	public boolean isBlack() {
		return sType == SquareType.Black;
	}
	
	public boolean isRed() {
		return sType == SquareType.Red;
	}
	
	public Point getSquarePos() {
		return pos;
	}
	
	public boolean isPossMove() {
		return possibleMove;
	}
	
	public void setPossMove(boolean possibleMove) {
		this.possibleMove = possibleMove;
	}
	
	/*
	 * Resets square before a new board calculation
	 */
	public void reset() {
		this.setPossMove(false);
		this.toFlip.clear();
	}
	
	/*
	 * Add another square to the set of squares that will be flipped
	 * if this square is chosen as a move
	 */
	public void addToFlipSquares(Collection<? extends Square> c) {
		this.toFlip.addAll(c);
	}
	
	/*
	 * Display associated flip squares as string (for ingame stats)
	 */
	public String getFlipSquaresAsString() {
		String s = "";
		Iterator<Square> it = toFlip.iterator();
		while (it.hasNext()) {
			Square sq = it.next();
			s += "(" + sq.pos.x + ", " + sq.pos.y + "), ";
		}
		if (s.isEmpty()) s = "No associated flip squares.";
		return s;
	}
	
	/*
	 * Flips all the squares associated with this square in the current game
	 * context when this square is chosen as a move
	 */
	public void flipAssociatedSquares() {
		Iterator<Square> it = toFlip.iterator();
		while (it.hasNext()) {
			it.next().flip();
		}
	}
	
	/*
	 * Returns the number of associated squares which would be flipped if this
	 * square was chosen as a move in current game context
	 */
	public int getNumOfFlipSquares() {
		return toFlip.size();
	}
	
	/*
	 * Flips black to red and red to black
	 */
	public void flip() {
		switch (sType) {
		case Black:
			sType = SquareType.Red;
			break;
		case Red:
			sType = SquareType.Black;
			break;
		default:
			break;
		}
	}
	
	/*
	 * Returns the bounding rectangle of the square
	 */
	public Rectangle getBoundingRect() {
		return new Rectangle(pos.x*Size, pos.y*Size, Size, Size);
	}
	
	public void setType(SquareType sType) {
		this.sType = sType;
	}
	
	/*
	 * Checks if square has a piece in it
	 */
	public boolean isInhabited() {
		return !(sType.equals(SquareType.Empty));
	}
	
	/*
	 * Draw this square, highlights if a possible move and also
	 * draws counters in squares
	 */
	public void draw(Graphics g) {
		
		if (this.isEmpty()) {
			if (this.isPossMove() && Game.moveVisualize) {
				g.setColor(Color.ORANGE);
				Rectangle r = this.getBoundingRect();
				g.fill3DRect(r.x, r.y, Size, Size, true);
			}
		}
		else {
			if (sType.equals(SquareType.Black))
				g.setColor(Color.BLACK);
			else
				g.setColor(Color.RED);
			g.fillOval(pos.x*Size, pos.y*Size, Size, Size);
		}
	}
}
