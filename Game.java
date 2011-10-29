import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Iterator;


/*
 * Main class which manages the game
 */
@SuppressWarnings("serial")
public class Game extends Applet implements Runnable {

	Thread engine = null;
	private static Dimension winSize;
	static Font scorefont, tinyfont, smallfont, largefont;
	Image dbimage, logo;
	long initialTime;
	
	// Player options
	int numPlayers;
	boolean playerAsBlack;
	static boolean sound;
	static boolean moveVisualize;
	
	static Board.BoardType BoardSize;
	
	// Game state and Components
	Board board;
	boolean blackToPlay;
	boolean cantMove;
	int delayCounter;
	boolean computerMoving;
	boolean inDemoMode;
	
	boolean showStats;
	
	enum State{Playing, Endgame, Main, Options, Info, Check, PlayerSelect};
	State state;
	State prevState;
	private static HashSet<MenuOption> menuOptions;
	private static int selectedOption;

	public static final int MOVEDELAY = 100;
	public static final int gamePause = 10;
	int pause;
	
	public String getAppletInfo() {
		return "Othello by Ben Homer";
	}
	
	/*
	 * Initialises Othello game
	 */
	public void init() {
		initialTime = System.currentTimeMillis();
		state = State.Main;
		prevState = State.Main;
		menuOptions = new HashSet<MenuOption>();

		// Default settings
		blackToPlay = true;
		sound = true;
		moveVisualize = true;
		showStats = false;		
		pause = gamePause;
		delayCounter = 0;
		
		BoardSize = Board.BoardType.Standard;
		board = new Board(BoardSize);
		
		
		setBackground(Color.white);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(dim.width - 10, dim.height - 110);
		Dimension d = winSize = this.getSize();
		logo = this.getImage(getDocumentBase(), "othello_logo.jpg");

		scorefont = new Font("Digital Dream", Font.BOLD, 30);
		smallfont = new Font("Atomic Clock Radio", Font.BOLD, 16);
		tinyfont = new Font("Arial", Font.BOLD, 10);
		largefont = new Font("Digital Dream", Font.BOLD, 48);
		
		dbimage = createImage(d.width, d.height);
		
		SoundManager.init();
		
		// Add key/mouse listeners
		this.addKeyListener(keyListener);
		this.addMouseMotionListener(mouseMoveListener);
		this.addMouseListener(mouseClickListener);
	}
	

	/*
	 * Returns index of currently selected menu option
	 */
	public static int getSelectedOption() {
		return selectedOption;
	}
	
	/*
	 * Adds a menu option to the set of all menu options
	 */
	public static void addToMenuOptions(MenuOption option) {
		menuOptions.add(option);
	}
	
	/*
	 * Returns size of game window
	 */
	public static Dimension getWinSize() {
		return winSize;
	}
	
	/*
	 *  Main game loop 
	 */
	@Override
	public void run() {
		while(true) {
			try {
				repaint();
				Thread.currentThread();
				Thread.sleep(pause);  // Change pause to alter game speed
			}
			catch (Exception e) {}
		}
	}
	
	
	/*
	 * Displays game statistics
	 */
	public void displayStats(Graphics g, int s) {
		Point p = Board.getHighlightedSquare().getSquarePos();
		String player = (blackToPlay) ? "BLACK" : "RED";
		String time = Long.toString(System.currentTimeMillis() - initialTime);
		
		g.setFont(smallfont);
		g.setColor(Color.BLACK);
		FontMetrics fm = g.getFontMetrics();
		rightString(g, fm, "GAME STATE" , s);
		rightString(g, fm, "Running time: " + time, s=space(s));
		rightString(g, fm, "State: " + state.toString(), s=space(s));
		rightString(g, fm, "Selected Option: " + selectedOption, s=space(s));
		rightString(g, fm, "Highlighted Square: " + p.x + ", " + p.y, s=space(s));
		rightString(g, fm, "Associated Flip Squares: " + Board.getHighlightedSquare().getFlipSquaresAsString(), s=space(s));
		rightString(g, fm, "Player to move: " + player,s=space(s));
		rightString(g, fm, "Possible Moves: " + board.getPossibleMovesAsString() , s=space(s));
	}
	
	/*
	 * Draws and centers a string on the game screen at the specified y-position
	 */
	private void centerString(Graphics g, FontMetrics fm, String str, int ypos) {
		g.drawString(str, (winSize.width - fm.stringWidth(str))/2, ypos);
	}
	
	/*
	 * Draws a string on the left side of the game screen at the specified y-position
	 */
	private void rightString(Graphics g, FontMetrics fm, String str, int ypos) {
		g.drawString(str, (int)(winSize.width*0.6f), ypos);
	}
	
	/*
	 * Allows for easy formatting of string in a vertical column
	 */
	private int space(int s) {
		s += 20;
		return s;
	}
	
	private int lspace(int s) {
		s += 50;
		return s;
	}
	
	/*
	 * Draws game banner at the start of a new game
	 */
	public void drawBanner(Graphics g) {
		g.setFont(largefont);
		FontMetrics fm = g.getFontMetrics();
		g.setColor(Color.BLACK);
		g.drawImage(logo, (winSize.width-logo.getWidth(null))/2, 50, null);
		g.setFont(scorefont);
		fm = g.getFontMetrics();
		centerString(g, fm, "by Ben Homer", 290);
		g.setFont(smallfont);
		fm = g.getFontMetrics();

		if(state == State.Info)
			openGameInfo(g, fm, 350);
		else if(state == State.Check)
			openCheck(g, fm, 350);
		else if(state == State.PlayerSelect)
			openPlayerSelect(g, fm, 350);
		else {
			centerString(g, fm, "Click the menu option you want:", 350);

			if (state == State.Main)
				openMainMenu(g, fm, 400);

			else if (state == State.Options)
				openOptionsMenu(g, fm, 400);
		}
	}
	
	/* 
	 * Draws game main menu 
	 */
	public void openMainMenu(Graphics g, FontMetrics fm, int s) {
		MenuOption singlePlayer = new MenuOption("Single Player", s, g, State.Main, 0);
		singlePlayer.draw(g);
		MenuOption twoPlayer = new MenuOption("Two Player", s=lspace(s), g, State.Main, 1);
		twoPlayer.draw(g);
		MenuOption option = new MenuOption("Options", s=lspace(s), g, State.Main, 2);
		option.draw(g);
		MenuOption quit = new MenuOption("Quit", s=lspace(s), g, State.Main, 3);
		quit.draw(g);
	}
	
	/*
	 * Draws game options menu
	 */
	public void openOptionsMenu(Graphics g, FontMetrics fm, int l) {
		String s, v, size = "";
		s = (sound) ? "ON" : "OFF";
		v = (moveVisualize) ? "ON" : "OFF"; 
		
		if (Board.isBoardTiny()) size = "Tiny Board";
		else if (Board.isBoardStandard()) size = "Standard Board";
		else if (Board.isBoardGiant()) size = "Giant Board";
		
		MenuOption gameInfo = new MenuOption("Game Info", l, g, State.Options, 0);
		gameInfo.draw(g);
		MenuOption sound = new MenuOption("Sound: " + s, l=lspace(l), g, State.Options, 1);
		sound.draw(g);
		MenuOption boardSize = new MenuOption("Board Size: " + size, l=lspace(l), g, State.Options, 2);
		boardSize.draw(g);
		MenuOption moveVisual = new MenuOption("Move Highlighting: " + v, l=lspace(l), g, State.Options, 3);
		moveVisual.draw(g);
		MenuOption back = new MenuOption("Back", l=lspace(l), g, State.Options, 4);
		back.draw(g);
	}
	
	/*
	 * Draws game info
	 */
	public void openGameInfo(Graphics g, FontMetrics fm, int s) {
		selectedOption = 0;
		
		// Paragraph about game rules and controls
		centerString(g, fm, "An emulation of Othello written in Java by Ben Homer", s);
		
		centerString(g, fm, "Rules", s=space(s)+20);
		centerString(g, fm, "The game of Othello (also known as Reversi), is a strategy game for two players involving a square", s=space(s));
		centerString(g, fm, "board and counters which are red for one player and black for the other.  Players take turns to", s=space(s));
		centerString(g, fm, "place counters on the board, attempting to surround their opponent’s counters. Any opposing counters", s=space(s));
		centerString(g, fm, "that are surrounded are flipped over and converted from black to red, or vice versa.  At the end", s=space(s));
		centerString(g, fm, "of the game, the winner is the side with more counters in their colour on the board.", s=space(s));
		
		centerString(g, fm, "Controls", s=space(s)+20);
		centerString(g, fm, "Simply click on the square you wish to play your counter", s=space(s));
		centerString(g, fm, "Press ESC during a game to quit", s=space(s));
		centerString(g, fm, "Press BACKSPACE at any time for game stats", s=space(s));
		centerString(g, fm, "Go to the options menu to adjust game settings", s=space(s)+20);
		
		MenuOption back = new MenuOption("Back", s=lspace(s)+20, g, State.Info, 0);
		back.draw(g);
	}

	/*
	 * Draws checker screen (checks if user want to exit)
	 */
	public void openCheck(Graphics g, FontMetrics fm, int s) {
		centerString(g, fm, "Are you sure you want to quit?", s);
		
		MenuOption yes = new MenuOption("Yes", s=lspace(s)+20, g, State.Check, 0);
		yes.draw(g);
		MenuOption no = new MenuOption("No", s=lspace(s), g, State.Check, 1);
		no.draw(g);
	}
	
	/*
	 * Draws checker screen (checks if user want to exit)
	 */
	public void openPlayerSelect(Graphics g, FontMetrics fm, int s) {
		centerString(g, fm, "Which colour do you want to play as?", s);
		
		MenuOption black = new MenuOption("Black", s=lspace(s)+20, g, State.PlayerSelect, 0);
		black.draw(g);
		MenuOption red = new MenuOption("Red", s=lspace(s), g, State.PlayerSelect, 1);
		red.draw(g);
	}
	
	
	public void drawIngameStats(Graphics g) {
		g.setFont(scorefont);
		g.setColor(Color.BLACK);
		FontMetrics fm = g.getFontMetrics();
		
		// Draw next move
		String nm = "Next Move:";
		int nmwidth = fm.stringWidth(nm);
		g.drawString(nm, board.dimensions.x*Square.Size + 100, board.dimensions.y*Square.Size/2 - 100);
		String player = (blackToPlay) ? "Black" : "Red";
		Color sColor = (blackToPlay) ? Color.BLACK : Color.RED;
		g.setColor(sColor);
		int offset = (nmwidth-fm.stringWidth(player))/2;
		g.drawString(player, board.dimensions.x*Square.Size + 100 + offset, board.dimensions.y*Square.Size/2 - 50 );
		
		// Let player know if it's their move or computers move
		if (numPlayers == 1) {
			String s = (computerMoving) ? "Computer is moving..." : "Your move...";
			g.setFont(smallfont);
			fm = g.getFontMetrics();
			g.setColor(Color.BLACK);
			offset = (nmwidth-fm.stringWidth(s))/2;
			g.drawString(s, board.dimensions.x*Square.Size + 100 + offset, board.dimensions.y*Square.Size/2);
		}
		
		// Draw counters
		int blackCount = board.getCount(true);
		int redCount = board.getCount(false);
		
		g.setFont(scorefont);
		fm = g.getFontMetrics();
		g.setColor(Color.RED);
		String reds = "Reds: " + redCount;
		offset = (nmwidth-fm.stringWidth(reds))/2;
		g.drawString(reds, board.dimensions.x*Square.Size + 100 + offset, board.dimensions.y*Square.Size/2 + 50);
		
		g.setColor(Color.BLACK);
		String blacks = "Blacks: " + blackCount;
		offset = (nmwidth-fm.stringWidth(blacks))/2;
		g.drawString(blacks, board.dimensions.x*Square.Size + 100 + offset, board.dimensions.y*Square.Size/2 + 100);
		
		if (state == State.Endgame) {
			g.setColor(Color.BLACK);
			String winner;
			if (blackCount == redCount)
				winner = "The game was drawn.";
			else if (blackCount > redCount)
				winner = "Black is the winner!";
			else
				winner = "Red is the winner!";
			offset = (nmwidth-fm.stringWidth(winner))/2;
			g.drawString(winner, board.dimensions.x*Square.Size + 100 + offset, board.dimensions.y*Square.Size/2 + 160);
		}
		else if (cantMove) {
			g.setColor(Color.BLACK);
			g.setFont(smallfont);
			fm = g.getFontMetrics();
			String ply = (blackToPlay) ? "Black" : "Red";
			ply += " cannot move, click to continue...";
			offset = (nmwidth-fm.stringWidth(ply))/2;
			g.drawString(ply, board.dimensions.x*Square.Size + 100 + offset, board.dimensions.y*Square.Size/2 + 160);
		}
		
		String quit = "Press ESC to quit";
		g.setFont(smallfont);
		fm = g.getFontMetrics();
		offset = (nmwidth-fm.stringWidth(quit))/2;
		g.drawString(quit, board.dimensions.x*Square.Size + 100 + offset, board.dimensions.y*Square.Size/2 + 200);
	}
	
	/*
	 * Updates graphics and computers move (if 1 player game) every step
	 */
	public void update(Graphics realg) {
		Graphics g = dbimage.getGraphics();
		g.setColor(getBackground());
		g.fillRect(0, 0, winSize.width, winSize.height);
		g.setColor(getForeground());
		
		if (state == State.Playing || state == State.Endgame) {
			board.draw(g, winSize);
			drawIngameStats(g);
			
			// Check if computer's turn to move (add a delay so it takes longer)
			if (numPlayers == 1 && ((blackToPlay && !playerAsBlack) || (!blackToPlay && playerAsBlack))) {
				computerMoving = true;
				++delayCounter;
				if (delayCounter == MOVEDELAY) {
					makeComputerMove();
					delayCounter = 0;
					computerMoving = false;
				}
			}
		}
		else
			drawBanner(g);

		if (showStats)
			displayStats(g, 600);

		realg.drawImage(dbimage, 0, 0, this);
	}
	
	/*
	 * Makes the computer player the current move
	 */
	private void makeComputerMove() {
		int numFlips = 0;
		// Look through all possible move squares and choose the one which will cause most flips (greedy approach)
		for (int i = 0; i < board.getDimensions().x; ++i) {
			for (int j = 0; j < board.getDimensions().y; ++j) {
				Square curr = board.getSquare(i, j);
				if (curr.isPossMove())
				{
					// If current square would mean more flips, set max flips to new value and highlight this square
					if (curr.getNumOfFlipSquares() > numFlips) {
						numFlips = curr.getNumOfFlipSquares();
						Board.setHighlightedSquare(i, j);
					}
				}
			}
		}
		// If player is black computer must play a red
		if (playerAsBlack)
			Board.placePieceOnHighlightedSquare(Square.SquareType.Red);
		else
			Board.placePieceOnHighlightedSquare(Square.SquareType.Black);
		blackToPlay = !blackToPlay;
		
		postMoveActions();
		
	}
	
	/*
	 * Flips squares after a move is made and checks for endgame
	 */
	private void postMoveActions() {
		Board.getHighlightedSquare().flipAssociatedSquares();

		if (board.checkForEndgame()) {
			state = State.Endgame;
		}
		else {
			// Calculate possible moves for other player in next turn
			board.CalculatePossibleMoves(blackToPlay);
			if (board.getNumberOfPossibleMoves() == 0) {
				cantMove = true;
			}
		}
	}

	/*
	 * Starts a game thread
	 */
	public void start() {
		if(engine == null) {
			engine = new Thread(this);
			engine.start();
		}
	}
	
	/*
	 * Stops the current game thread
	 */
	@SuppressWarnings("deprecation")
	public void stop() {
		if (engine != null && engine.isAlive())
			engine.stop();
		engine = null;
	}
	
	/*
	 * Allows user to use mouse as game controller
	 */
	MouseMotionListener mouseMoveListener = new MouseMotionListener() {
		@Override
		public void mouseMoved(MouseEvent e) {
			switch(state) {
			case Playing:
				// Set highlighted square when mouse moves over it
				for (int i = 0; i < board.getDimensions().x; ++i) {
					for (int j = 0; j < board.getDimensions().y; ++j) {
						Square s = board.getSquare(i, j);
						if (s.getBoundingRect().contains(e.getPoint()))
							Board.setHighlightedSquare(i, j);
					}
				}
				break;
				
			case Main: case Options: case Info: case Check: case PlayerSelect:
				// Go through options and set the one mouse is over to selected (works for all menus)
				Iterator<MenuOption> it = menuOptions.iterator();
				while (it.hasNext()) {
					MenuOption curr = it.next();
					if (curr.getBoundingRect() != null && curr.getBoundingRect().contains(e.getPoint()))
						selectedOption = curr.getOptionNum();
				}
			break;
			
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// Unimplemented
		}
	};

	MouseListener mouseClickListener = new MouseAdapter() {

		@Override
		public void mousePressed(MouseEvent e) {
			switch(state) {
			case Playing:
				if ((numPlayers == 1 && (blackToPlay && playerAsBlack) || (!blackToPlay && !playerAsBlack)) || (numPlayers == 2))
				{
					// If player could not move but clicked mouse, switch player
					if (cantMove) {
						cantMove = false;
						blackToPlay = !blackToPlay;
						board.CalculatePossibleMoves(blackToPlay);
					}
					else {
						// If black's turn place black piece and switch to red's turn
						if (blackToPlay) {
							if (!Board.getHighlightedSquare().isInhabited() && Board.getHighlightedSquare().isPossMove()) {
								Board.placePieceOnHighlightedSquare(Square.SquareType.Black);
								blackToPlay = false;
							}
						}
						// If red's turn place red piece and switch to black's turn
						else {
							if (!Board.getHighlightedSquare().isInhabited() && Board.getHighlightedSquare().isPossMove()) {
								Board.placePieceOnHighlightedSquare(Square.SquareType.Red);
								blackToPlay = true;
							}
						}
						// Flip appropriate squares after move made and check for endgame
						postMoveActions();
					}
				}
				break;
					
			case Main:
				SoundManager.MENUCLICK.play();
				switch (selectedOption) {
				case 0: 
					numPlayers = 1;
					board = new Board();
					blackToPlay = true;
					computerMoving = false;
					state = State.PlayerSelect;
					break;
				case 1:
					numPlayers = 2;
					board = new Board();
					blackToPlay = true;
					state = State.Playing;
					break;
				case 2:
					state = State.Options;
					break;
				case 3:
					prevState = State.Main;
					state = State.Check;
					break;	
				}
				break;
					
			case PlayerSelect:
				SoundManager.MENUCLICK.play();
				switch(selectedOption) {
				case 0:
					// Chosen Black
					playerAsBlack = true;
					state = State.Playing;
					break;
				case 1:
					// Chosen Red
					playerAsBlack = false;
					state = State.Playing;
					break;
				}
				break;
					
			case Options:
				SoundManager.MENUCLICK.play();
				switch(selectedOption) {
				case 0:
					// Show game info text
					state = State.Info;
					break;
				case 1:
					SoundManager.unmute();
					// Toggle sound ON/OFF
					if (sound) {
						sound = false;
						SoundManager.mute();
					}
					else {
						sound = true;
						SoundManager.unmute();
					}
					break;
				case 2:
					if (Board.isBoardTiny())
						Board.setType(Board.BoardType.Standard);
					else if (Board.isBoardStandard())
						Board.setType(Board.BoardType.Giant);
					else if (Board.isBoardGiant())
						Board.setType(Board.BoardType.Tiny);
					break;
				case 3:
					// Toggle move visuals ON/OFF
					moveVisualize = !moveVisualize;
					break;
				case 4:
					state = State.Main;
					break;
				}
			break;

			case Info:
				SoundManager.MENUCLICK.play();
				switch(selectedOption) {
				case 0:
					state = State.Options;
					break;
				}
				break;

			case Check:
				SoundManager.MENUCLICK.play();
				switch(selectedOption) {
				case 0:
					if(prevState.equals(State.Playing) || prevState.equals(State.Endgame))
						state = State.Main;
					else
						System.exit(0);
					break;
				case 1:
					state = prevState;
					break;
				}
				break;
			}
		}
	};

	 
	/*
	 * Allows user to use keyboard as game controller
	*/
	KeyListener keyListener = new KeyAdapter() {
		
	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		if (key == KeyEvent.VK_BACK_SPACE) {
			if (showStats) showStats = false;
			else showStats = true;
		}
		
		switch(state) {
			case Playing: case Endgame: case Main:
				
				switch(key) {
				case Event.ESCAPE:
					SoundManager.MENUCLICK.play();
					prevState = state;
					state = State.Check;
					break;
				}
				
			break;
			
			case Options:
				switch(key) {
					
					case Event.ESCAPE:
						SoundManager.MENUCLICK.play();
						state = State.Main;
						break;
				}
			break;
			
			case Info:
				switch(key) {
					case Event.ESCAPE:
						SoundManager.MENUCLICK.play();
						state = State.Options;
						break;
				}
			break;
			
			case Check:
				SoundManager.MENUCLICK.play();
				switch(key) {
					case 'Y': case 'y':
						if(prevState.equals(State.Playing) || prevState.equals(State.Endgame))
							state = State.Main;
						else
							System.exit(0);
						break;
					case 'N': case 'n':
						state = prevState;
						break;
				}
			break;
			
			case PlayerSelect:
				switch(key) {
				case Event.ESCAPE:
					SoundManager.MENUCLICK.play();
					state = State.Main;
				break;
				}
			break;
			}
		}
	};
	
}
	

