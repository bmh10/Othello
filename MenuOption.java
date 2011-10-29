import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;


/*
 * Represents a single option in a menu
 */

public class MenuOption {

	private String str;
	private int y;
	private int optionNum;
	private Rectangle rect;
	private Game.State menu;
	
	/*
	 * Note that the constructor adds a created menu option  
	 * to the set of all menu options in the Game class
	 */
	MenuOption(String str, int y, Graphics g, Game.State menu, int optionNum) {
		this.str = str;
		this.y = y;
		this.menu = menu;
		this.optionNum = optionNum;
		g.setFont(Game.largefont);
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(str);
		int h = 30;
		this.rect = new Rectangle((Game.getWinSize().width - w)/2, y-30, w, h);
		Game.addToMenuOptions(this);
	}
	
	public Rectangle getBoundingRect() {
		if (rect != null)
			return rect;
		return null;
	}
	
	/*
	 * Gets this options index position in a menu
	 */
	public int getOptionNum() {
		return optionNum;
	}
	
	/*
	 * Returns the game menu this option is associated with
	 */
	public Game.State getAssociatedMenu() {
		return menu;
	}
	
	/*
	 * Draws this option and highlights it if this option is currently selected
	 */
	public void draw(Graphics g) {
		g.setFont(Game.largefont);
		FontMetrics fm = g.getFontMetrics();
		// If this options menu is currently selected change its display colour
		Color col = (Game.getSelectedOption() == this.getOptionNum()) ? Color.RED : Color.BLACK;
		g.setColor(col);
		g.drawString(str, (Game.getWinSize().width - fm.stringWidth(str))/2, y);
	}
}
