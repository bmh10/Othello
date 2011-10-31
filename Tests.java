import junit.framework.TestCase;

/*
 * JUnit tests for Othello game
 */

public class Tests extends TestCase {

    private Game game;

    /**
     * Sets up the test fixture.
     * Called before every test case method.
     */
    protected void setUp() {
    	game = new Game();
    	game.init();
    }

    /**
     * Tears down the test fixture.
     * Called after every test case method.
     */
    protected void tearDown() {
    	// Unimplemented as the tests follow on from each other
    }
    
    /*
     * Tests that game is setup correctly
     */
    public void testGameStart() {
    	// Test board size
    	assertEquals(game.board.dimensions.x, 8);
    	assertEquals(game.board.dimensions.y, 8);
    	
    	// Test that there are 2 black counters and 2 red counters
    	assertEquals(game.board.getCount(true), 2);
    	assertEquals(game.board.getCount(false), 2);
    	
    	// Test that black plays first
    	assertEquals(Game.blackToPlay, true);
    	
    	// Test that number of possible moves is 4
    	assertEquals(game.board.getNumberOfPossibleMoves(), 4);
    }
    
    /*
     * Check the everything is as it should be after one move
     */
    public void testFirstMove() {
    	game.makeComputerMove();
    	// Test that there are 4 black counters and 1 red counter
    	assertEquals(game.board.getCount(true), 4);
    	assertEquals(game.board.getCount(false), 1);
    	
    	// Test that opponent now has 3 possible moves
    	assertEquals(game.board.getNumberOfPossibleMoves(), 3);
    }
    
    /*
     * Test that happens when an invalid move is attempted
     */
    public void testInvalidMove() {
    	// Try placing a red piece in an invalid square.
    	// Should not be allowed so square should remain empty
    	Board.setHighlightedSquare(0, 0);
    	Board.placePieceOnHighlightedSquare(Square.SquareType.Red);
    	assertTrue(game.board.getSquare(0, 0).isEmpty());
    }
    
    /*
     * Tests that flips are correct in a more complex scenario
     */
    public void testFlips() {
    	// 0=empty, 1=black, 2=red
    	int[][] customBoard =
    		   {{2,0,0,0,0,0,0,0},
    			{0,1,0,0,0,0,0,2},
    			{0,0,1,0,0,0,1,0},
    			{0,0,0,1,0,1,0,0},
    			{2,1,1,1,0,1,1,2},
    			{0,0,0,1,0,1,0,0},
    			{0,0,1,0,0,0,1,0},
    			{0,2,0,0,0,0,0,2}};
    	
    	// Setup test board with custom board (red to move)
    	game.board.setupTestBoard(customBoard, false);
    	
    	// Check board was set up correctly
    	assertEquals(game.board.getCount(true), 14);
    	assertEquals(game.board.getCount(false), 6);
    	
    	// Play a red counter in centre which should cause all black counters to flip
    	Board.setHighlightedSquare(4, 4);
    	Board.placePieceOnHighlightedSquare(Square.SquareType.Red);

    	// Check move was made successfully
    	assertTrue(game.board.getSquare(4, 4).isRed());
    	
    	// Check flips were carried out correctly
    	assertEquals(game.board.getCount(true), 0);
    	assertEquals(game.board.getCount(false), 21);
    	
    	// Taking into account that the custom board array initialised above has not changed,
    	// all the positions with counters in (i.e. greater than 0) should now be red counters 
    	// in the actual board. All the other positions, except for where we placed the red counter 
    	// (at 4,4), should still be empty.
    	for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 8; ++j) {
				if (customBoard[i][j] > 0 || (i==4 && j==4))
					assertTrue(game.board.getSquare(i, j).isRed());
				else
					assertTrue(game.board.getSquare(i, j).isEmpty());
			}
    	}
    }
    
    /*
     * Test when happens when a player cannot move
     */
    public void testPlayerStuck() {
    	// 0=empty, 1=black, 2=red
    	// In this scenario red can move but black cannot
    	int[][] customBoard =
    		   {{0,0,0,0,0,0,0,0},
    			{0,0,0,0,0,0,0,0},
    			{0,0,0,0,0,0,0,0},
    			{0,0,0,0,0,0,0,0},
    			{2,1,1,1,1,0,0,0},
    			{0,0,0,0,0,0,0,0},
    			{0,0,0,0,0,0,0,0},
    			{0,0,0,0,0,0,0,0}};
    	
    	// Setup test board with custom board (black to move)
    	game.board.setupTestBoard(customBoard, true);
    	
    	// Check board was set up correctly
    	assertEquals(game.board.getCount(true), 4);
    	assertEquals(game.board.getCount(false), 1);
    	
    	// Test that black cannot move
    	assertEquals(game.board.getNumberOfPossibleMoves(), 0);
    	
    	// Check that end-game has not been reached
    	assertFalse(game.board.checkForEndgame());
    }
    
    /*
     * Test end-game conditions (test 1)
     */
    public void testEndGame1() {
    	// Set computers move delay to zero to speed things up
    	// and make computer play both sides
    	Game.MOVEDELAY = 0;
    	while(!game.board.checkForEndgame()) {
    		game.makeComputerMove();
    		game.playerAsBlack = !game.playerAsBlack;
    	}
    	// Check that neither player has a possible move
    	game.board.CalculatePossibleMoves(true);
    	assertEquals(game.board.getNumberOfPossibleMoves(), 0);
    	game.board.CalculatePossibleMoves(false);
    	assertEquals(game.board.getNumberOfPossibleMoves(), 0);
    }

    /*
     * Test end-game conditions (test 2)
     */
    public void testEndGame2() {

    	// 0=empty, 1=black, 2=red
    	// In this scenario there is one move until the end of the game
    	int[][] customBoard =
    		   {{0,2,1,2,1,2,1,2},
    			{2,1,2,1,2,1,2,1},
    			{1,2,1,2,1,2,1,2},
    			{2,1,2,1,2,1,2,1},
    			{1,2,1,2,1,2,1,2},
    			{2,1,2,1,2,1,2,1},
    			{1,2,1,2,1,2,1,2},
    			{2,1,2,1,2,1,2,1}};

    	// Setup test board with custom board (black to move)
    	game.board.setupTestBoard(customBoard, true);

    	// Check board was set up correctly
    	assertEquals(game.board.getCount(true)+game.board.getCount(false), 63);
    	
    	// Play a black counter in top-left corner
    	Board.setHighlightedSquare(0, 0);
    	Board.placePieceOnHighlightedSquare(Square.SquareType.Black);

    	// Check move was made successfully
    	assertTrue(game.board.getSquare(0, 0).isBlack());
    	
    	// Check flips were carried out correctly
    	assertTrue(game.board.getSquare(0, 1).isBlack());
    	assertTrue(game.board.getSquare(1, 0).isBlack());
    	
    	// Check that end-game has been reached
    	assertTrue(game.board.checkForEndgame());

    	// Check that winner is correct
    	int blackCount = game.board.getCount(true);
    	int redCount = game.board.getCount(false);
    	assert(blackCount > redCount);
    }
    
} 

	
	
