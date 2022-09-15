import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.metal.MetalButtonUI;

/*
 * Main class for the program that draws every entity and simulates
 * the game of life starting with the provided pattern
 */
public class Life extends JFrame{
	private JPanel gameGridPanel, mainPanel, bottomPanel, topPanel;
	private PlaceholderTextField textField;
	private JLabel iterationsLabel;
	private int iterationsCompleted, gridSize, iterations;
	private Square[][] squares;
	private JButton start, stop, reset;
	private JComboBox patternsCB;
	private boolean stopped, pressedLeft, pressedRight, simulating;
	private Color squareColor;
	String pattern;
	
	public Life() throws IOException {
		iterationsCompleted = 0;
		gridSize = 45;
		iterations = 0;
		stopped = false;
		squareColor = new Color(40, 40, 40);
		pressedLeft = false;
		pressedRight = false;
		simulating = false;
		
		// Picutures for the buttons
		BufferedImage playPic = ImageIO.read(this.getClass().getResourceAsStream("resources/play.png"));
		BufferedImage pausePic = ImageIO.read(this.getClass().getResourceAsStream("resources/pause.png"));
		BufferedImage resetPic = ImageIO.read(this.getClass().getResourceAsStream("resources/reset.png"));
		
		// User can select patterns from the combobox
		String[] patterns = {"Custom", "Random", "Pentadecathlon", "Simkin Glider"};
		patternsCB = new JComboBox<String>(patterns);
		patternsCB.setBackground(new Color(25, 25, 25));
		patternsCB.setForeground(Color.white);
		
		// Adding an action listener to the combobox so that the grid updates automatically when user selects a specific pattern
		ActionListener cbActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = (String) patternsCB.getSelectedItem();
                if (!simulating) {
                	switch (s) {
                    case "Custom":
                		resetGrid();
                    	enableSquares();
                        break;
                    case "Random":
                		resetGrid();
                    	disableSquares();
                    	randomPattern();
                        break;
                    case "Pentadecathlon":
                		resetGrid();
                    	disableSquares();
                        pentaDec();
                        break;
                    case "Simkin Glider":
                		resetGrid();
                    	disableSquares();
                        simkinGlider();
                        break;
                	}
                }
            }
        };
        patternsCB.addActionListener(cbActionListener);
        
        // Customized text field
		textField = new PlaceholderTextField(8);
		textField.setPreferredSize(new Dimension(50, 25));
		textField.setPlaceholder("No. Iterations");
		textField.setFont(new Font("Arial", Font.BOLD, 14));
		textField.setBorder(null);
		textField.setBackground(new Color(25, 25, 25));
		textField.setForeground(Color.white);
		textField.setCaretColor(Color.WHITE);
		
		// Panel to hold combo box and text field
		JPanel patternsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
		patternsBox.setBackground(Color.decode("#3d3d3d"));
		patternsBox.add(patternsCB);
		patternsBox.add(Box.createRigidArea(new Dimension(20, 0)));
		patternsBox.add(textField);
		
		// start button will be used to start simulation
		start = new JButton("");
    	start.setBackground(Color.decode("#02A122"));
    	start.setBorderPainted(false);
    	start.setFont(new Font("Arial", Font.BOLD, 14));
    	start.setIcon(new ImageIcon(playPic));
    	start.setOpaque(true);
    	start.setCursor(new Cursor(Cursor.HAND_CURSOR));
    	start.addMouseListener(new java.awt.event.MouseAdapter() {
    	    public void mouseEntered(java.awt.event.MouseEvent evt) {
    	    	if (start.isEnabled()) {
    	    		start.setBackground(Color.decode("#01B826"));
    	    	}
    	    }

    	    public void mouseExited(java.awt.event.MouseEvent evt) {
    	    	start.setBackground(Color.decode("#02A122"));
    	    }
    	});
		start.addActionListener(ev -> {
			try {
				start();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
		
		// stop button will pause the simulation
		stop = new JButton("");
		stop.setBackground(Color.red);
		stop.setBorderPainted(false);
		stop.setFont(new Font("Arial", Font.BOLD, 14));
		stop.setIcon(new ImageIcon(pausePic));
		stop.setOpaque(true);
		stop.setCursor(new Cursor(Cursor.HAND_CURSOR));
		stop.addMouseListener(new java.awt.event.MouseAdapter() {
    	    public void mouseEntered(java.awt.event.MouseEvent evt) {
    	    	if (stop.isEnabled()) {
    	    		stop.setBackground(Color.decode("#FC8686"));
    	    	}
    	    }

    	    public void mouseExited(java.awt.event.MouseEvent evt) {
    	    	stop.setBackground(Color.red);
    	    }
    	});
		stop.setEnabled(false);
		stop.addActionListener(ev -> stop());
		
		// reset button will only be enabled when simulation is paused and will reset
		// everything back to default
		reset = new JButton("");
		reset.setBackground(Color.decode("#1e81b0"));
		reset.setBorderPainted(false);
		reset.setFont(new Font("Arial", Font.BOLD, 14));
		reset.setIcon(new ImageIcon(resetPic));
		reset.setOpaque(true);
		reset.setCursor(new Cursor(Cursor.HAND_CURSOR));
		reset.addMouseListener(new java.awt.event.MouseAdapter() {
    	    public void mouseEntered(java.awt.event.MouseEvent evt) {
    	    	if (reset.isEnabled()) {
    	    		reset.setBackground(Color.decode("#58add5"));
    	    	}
    	    }

    	    public void mouseExited(java.awt.event.MouseEvent evt) {
    	    	reset.setBackground(Color.decode("#1e81b0"));
    	    }
    	});
		reset.setEnabled(false);
		reset.addActionListener(ev -> reset());
		
		// flow panel holds the buttons
		JPanel buttonFlowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
		buttonFlowPanel.setBackground(Color.decode("#3d3d3d"));
		buttonFlowPanel.add(start);
		buttonFlowPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonFlowPanel.add(stop);
		buttonFlowPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonFlowPanel.add(reset);
		
		// top panel to hold the buttons, combo box and text field
		topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.setBackground(Color.decode("#3d3d3d"));
		topPanel.add(buttonFlowPanel);
		topPanel.add(patternsBox);
		
		// main grid panel
		gameGridPanel = new JPanel();
		gameGridPanel.setLayout(new GridLayout(gridSize, gridSize));
		createGrid(gameGridPanel, gridSize, gridSize);
		
		// keep track of iterations completed and max iterations
		iterationsLabel = new JLabel("Iteration: " + iterationsCompleted + "/" + iterations);
		iterationsLabel.setFont(new Font("Arial", Font.BOLD, 14));
		iterationsLabel.setForeground(Color.white);
		
		// bottom panel to hold the iterations label
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.setBackground(Color.decode("#3d3d3d"));
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(iterationsLabel);
		bottomPanel.add(Box.createHorizontalGlue());
		
		// main panel to hold everything
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		mainPanel.setBackground(Color.decode("#3d3d3d"));
		mainPanel.add(topPanel);
		mainPanel.add(gameGridPanel);
		mainPanel.add(bottomPanel);
		
		 // adding the main panel to the frame
        getContentPane().add(mainPanel);
        pack();

        // setting the title, making the app closeable, disabling resizing, and setting it visible
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        setTitle("Game Of Life");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
	}
	
	/*
	 * stop the simulation, update flags, and enable/disable buttons appropriately
	 */
	private void stop() {
		simulating = false;
		stopped = true;
		stop.setEnabled(false);
		reset.setEnabled(true);
	}
	
	/*
	 * reset everything
	 */
	private void reset() {
		enableSquares();
		patternsCB.setSelectedItem("Custom");
		reset.setEnabled(false);
		textField.setText("");
		textField.setEditable(true);
		stopped = false;
		start.setEnabled(true);
		resetGrid();
		iterations = 0;
		iterationsCompleted = 0;
		iterationsLabel.setText("Iteration: " + iterationsCompleted + "/" + iterations);
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		 new Life();
	}
	
	/*
	 * start the simulation if max number of iterations has been provided
	 */
	public void start() throws InterruptedException {
		try {
			Integer.parseInt(textField.getText());
			simulating = true;
			disableSquares();
			reset.setEnabled(false);
			stop.setEnabled(true);
			start.setEnabled(false);
			textField.setEditable(false);
			stopped = false;
			if (iterations == iterationsCompleted) {
				iterationsCompleted = 0;
			}
			iterations = Integer.parseInt(textField.getText());
			simulate();
		}
		catch (NumberFormatException e){
			System.out.println("Not an integer");
		}
	}
	
	/*
	 * creating the main grid
	 */
	public void createGrid(JPanel panel, int x, int y){
		// creating and adding the squares to the grid
		squares = new Square[x][y];
		for (int i = 0; i < x; i++){
			for (int j = 0; j < y; j++){
				squares[i][j] = new Square(i, j);
				Square button = squares[i][j];
				squares[i][j].addMouseListener(new MouseAdapter(){
					@Override
					public void mousePressed(MouseEvent me) {
						// if left mouse button is pressed and the user is entering a custom panel and selecting
						// a square that is "dead" make that square alive (i.e. set it to white)
						if (me.getButton() == MouseEvent.BUTTON1) {
							pressedLeft = true;
							if (button.isEnabled() && button.getBackground() == squareColor) {
								button.setBackground(Color.white);
								button.setAlive(true);
							}
						}
						// right mouse button makes square dead (i.e. reverses what left mouse button does)
						else if (me.getButton() == MouseEvent.BUTTON3) {
							pressedRight = true;
							if (button.isEnabled() && button.getBackground() == Color.white) {
								button.setBackground(squareColor);
								button.setAlive(false);
							}
						}
					}
					@Override
					public void mouseEntered(MouseEvent me) {
						// add a simple border to grid squares
						if (button.getBackground() == squareColor) {
							button.setBorder(BorderFactory.createLineBorder(Color.white, 2));
							button.setBorderPainted(true);
						}
						// following two conditional statements allow the user to hold the mouse button and draw many squares
						// very quickly instead of individually clicking on each one
						if (button.getBackground() == squareColor && pressedLeft && button.isEnabled()) {
							button.setBackground(Color.white);
							button.setAlive(true);
						}
						else if (button.getBackground() == Color.white && pressedRight && button.isEnabled()) {
							button.setBackground(squareColor);
							button.setAlive(false);
						}
					}
					@Override
					public void mouseExited(MouseEvent me) {
						button.setBorderPainted(false);
					}
					@Override
					public void mouseReleased(MouseEvent me) {
						pressedLeft = false;
						pressedRight = false;
					}
				});
				squares[i][j].setBackground(squareColor);
				squares[i][j].setBorderPainted(false);
				// keeping track of the coordinates of each square
				squares[i][j].setPreferredSize(new Dimension(15, 15));
				// adding each square to the given panel
				panel.add(squares[i][j]);
			}
		}
	}
	
	/*
	 * enable all the grid squares
	 */
	public void enableSquares() {
		for (int i = 0; i < gridSize; i++){
			for (int j = 0; j < gridSize; j++){
				squares[i][j].setEnabled(true);
			}
		}
	}
	
	/*
	 * disable all the grid squares
	 */
	public void disableSquares() {
		for (int i = 0; i < gridSize; i++){
			for (int j = 0; j < gridSize; j++){
				squares[i][j].setEnabled(false);
			}
		}
	}
	
	/*
	 * method to add the pentadecathlon pattern to the grid
	 */
	public void pentaDec() {
		// following values are needed to dynamically center pattern on any grid size
		int PENTA_DEC_WIDTH = 10;
		int PENTA_DEC_HEIGHT = 3;
		int x_offset = gridSize/2 - PENTA_DEC_WIDTH/2;
		int y_offset = gridSize/2 - PENTA_DEC_HEIGHT/2;
		
		squares[y_offset+1][x_offset].setBackground(Color.white);
		squares[y_offset+1][x_offset].setAlive(true);
		squares[y_offset+1][x_offset+1].setBackground(Color.white);
		squares[y_offset+1][x_offset+1].setAlive(true);
		squares[y_offset+2][x_offset+2].setBackground(Color.white);
		squares[y_offset+2][x_offset+2].setAlive(true);
		squares[y_offset][x_offset+2].setBackground(Color.white);
		squares[y_offset][x_offset+2].setAlive(true);
		squares[y_offset+1][x_offset+3].setBackground(Color.white);
		squares[y_offset+1][x_offset+3].setAlive(true);
		squares[y_offset+1][x_offset+4].setBackground(Color.white);
		squares[y_offset+1][x_offset+4].setAlive(true);
		squares[y_offset+1][x_offset+5].setBackground(Color.white);
		squares[y_offset+1][x_offset+5].setAlive(true);
		squares[y_offset+1][x_offset+6].setBackground(Color.white);
		squares[y_offset+1][x_offset+6].setAlive(true);
		squares[y_offset+2][x_offset+7].setBackground(Color.white);
		squares[y_offset+2][x_offset+7].setAlive(true);
		squares[y_offset][x_offset+7].setBackground(Color.white);
		squares[y_offset][x_offset+7].setAlive(true);
		squares[y_offset+1][x_offset+8].setBackground(Color.white);
		squares[y_offset+1][x_offset+8].setAlive(true);
		squares[y_offset+1][x_offset+9].setBackground(Color.white);
		squares[y_offset+1][x_offset+9].setAlive(true);
	}
	
	/*
	 * add a random pattern to the grid
	 */
	public void randomPattern() {
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				int random = new Random().nextBoolean() ? 0 : 1;
				if (random == 1) {
					squares[i][j].setBackground(Color.white);
					squares[i][j].setAlive(true);
				}
			}
		}
	}
	
	/*
	 * reset grid back to original state
	 */
	public void resetGrid() {
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				squares[i][j].setBackground(squareColor);
				squares[i][j].setAlive(false);
			}
		}
	}
	
	/*
	 * add simkin-glider gun pattern to grid
	 */
	public void simkinGlider() {
		// first set of 3 squares
		squares[1][1].setBackground(Color.white);
		squares[1][1].setAlive(true);
		squares[1][2].setBackground(Color.white);
		squares[1][2].setAlive(true);
		squares[2][1].setBackground(Color.white);
		squares[2][1].setAlive(true);
		squares[2][2].setBackground(Color.white);
		squares[2][2].setAlive(true);
		squares[1][8].setBackground(Color.white);
		squares[1][8].setAlive(true);
		squares[1][9].setBackground(Color.white);
		squares[1][9].setAlive(true);
		squares[2][8].setBackground(Color.white);
		squares[2][8].setAlive(true);
		squares[2][9].setBackground(Color.white);
		squares[2][9].setAlive(true);
		squares[4][5].setBackground(Color.white);
		squares[4][5].setAlive(true);
		squares[4][6].setBackground(Color.white);
		squares[4][6].setAlive(true);
		squares[5][5].setBackground(Color.white);
		squares[5][5].setAlive(true);
		squares[5][6].setBackground(Color.white);
		squares[5][6].setAlive(true);
		
		// second set of 3 squares
		squares[9][28].setBackground(Color.white);
		squares[9][28].setAlive(true);
		squares[9][29].setBackground(Color.white);
		squares[9][29].setAlive(true);
		squares[10][28].setBackground(Color.white);
		squares[10][28].setAlive(true);
		squares[10][29].setBackground(Color.white);
		squares[10][29].setAlive(true);
		squares[12][25].setBackground(Color.white);
		squares[12][25].setAlive(true);
		squares[12][26].setBackground(Color.white);
		squares[12][26].setAlive(true);
		squares[13][25].setBackground(Color.white);
		squares[13][25].setAlive(true);
		squares[13][26].setBackground(Color.white);
		squares[13][26].setAlive(true);
		squares[12][32].setBackground(Color.white);
		squares[12][32].setAlive(true);
		squares[12][33].setBackground(Color.white);
		squares[12][33].setAlive(true);
		squares[13][32].setBackground(Color.white);
		squares[13][32].setAlive(true);
		squares[13][33].setBackground(Color.white);
		squares[13][33].setAlive(true);
		
		// glider
		squares[1][21].setBackground(Color.white);
		squares[1][21].setAlive(true);
		squares[2][19].setBackground(Color.white);
		squares[2][19].setAlive(true);
		squares[2][20].setBackground(Color.white);
		squares[2][20].setAlive(true);
		squares[2][21].setBackground(Color.white);
		squares[2][21].setAlive(true);
		squares[3][19].setBackground(Color.white);
		squares[3][19].setAlive(true);
		squares[3][21].setBackground(Color.white);
		squares[3][21].setAlive(true);
		squares[4][19].setBackground(Color.white);
		squares[4][19].setAlive(true);
	}
	
	/*
	 * method used to determine if a wrap around is happening
	 */
	public boolean wrap(int xcoord, int ycoord) {
		if (xcoord == gridSize-1 || xcoord == 0 || ycoord == gridSize-1 || ycoord == 0) {
			return true;
		}
		return false;
	}
	
	/*
	 * method used to execute the wrap around correctly
	 * i.e. the right most squares of the grid are adjacent to the left most squares
	 * and the top most squares of the grid are adjacent to the bottom most
	 */
	public int checkWrap(int xcoord, int ycoord, int i, int j) {
		int numNeighbours = 0;
		// if we are in the right most column, we need to check all adjacent
		// squares in the left most column, which will be three squares 
		// 1 square being at the 0th column with the same y position
		// 1 square being at the 0th column with y position +1
		// 1 square beiung at the 0th column with y position -1
		// same methodology applies to the other far most columns
		if (xcoord == gridSize-1) {
			if (squares[0][ycoord].getBackground() == Color.white) {
				numNeighbours++;
	   		}
		   	if (ycoord-1 >= 0) {
		   		if (squares[0][ycoord-1].getBackground() == Color.white) {
		   			numNeighbours++;
		   		}
		   	}
	   		if (ycoord+1 <= gridSize-1) {
	   			if (squares[0][ycoord+1].getBackground() == Color.white) {
	   				numNeighbours++;
	   			}
	   		}
		}
   		if (xcoord == 0) {
			if (squares[gridSize-1][ycoord].getBackground() == Color.white) {
				numNeighbours++;
	   		}
		   	if (ycoord-1 >= 0) {
		   		if (squares[gridSize-1][ycoord-1].getBackground() == Color.white) {
		   			numNeighbours++;
		   		}
		   	}
	   		if (ycoord+1 <= gridSize-1) {
	   			if (squares[gridSize-1][ycoord+1].getBackground() == Color.white) {
	   				numNeighbours++;
	   			}
	   		}
   		}
   		if (ycoord == gridSize-1) {
			if (squares[xcoord][0].getBackground() == Color.white) {
				numNeighbours++;
	   		}
		   	if (xcoord-1 >= 0) {
		   		if (squares[xcoord-1][0].getBackground() == Color.white) {
		   			numNeighbours++;
		   		}
		   	}
	   		if (xcoord+1 <= gridSize-1) {
	   			if (squares[xcoord+1][0].getBackground() == Color.white) {
	   				numNeighbours++;
	   			}
	   		}
   		}
   		if (ycoord == 0) {
			if (squares[xcoord][gridSize-1].getBackground() == Color.white) {
				numNeighbours++;
	   		}
		   	if (xcoord-1 >= 0) {
		   		if (squares[xcoord-1][gridSize-1].getBackground() == Color.white) {
		   			numNeighbours++;
		   		}
		   	}
	   		if (xcoord+1 <= gridSize-1) {
	   			if (squares[xcoord+1][gridSize-1].getBackground() == Color.white) {
	   				numNeighbours++;
	   			}
	   		}
   		}
   		return numNeighbours;
	}
	
	/*
	 * simulate in a different thread to prevent blocking for the main Swing thread
	 */
	private void simulate() {
    	new Thread(new Runnable() {
    		@Override
    		public void run() {
    			int numNeighbours;
    			while (iterationsCompleted < iterations && !stopped) {
    				for (int i = 0; i < gridSize; i++) {
    					for (int j = 0; j < gridSize; j++) {
    						 numNeighbours = 0;
    						 int xcoord = squares[i][j].getXcoord();
    						 int ycoord = squares[i][j].getYcoord();
    						 if (wrap(xcoord, ycoord)) {
    							 numNeighbours = checkWrap(xcoord, ycoord, i, j);
    						 }
    						 for (int x = xcoord - 1; x < xcoord + 2; x++) {
    							 for (int y = ycoord - 1; y < ycoord + 2; y++) {
    								 // skip checking own square
    								 if (x == xcoord && y == ycoord) {
    									 continue; 
    								 }
    								 // checking if we are targeting a square outside of the grid
    		                         // and if we are we skip it
    		                         if (x > (gridSize - 1) || x < 0 || y > (gridSize - 1) || y < 0){
    		                        	 continue;
    		                         }
    		                         if (squares[x][y].getBackground() == Color.white) {
    									 numNeighbours++;
    								 }
    							 }
    						 }
    						 // update squares based on game rules
    						 if (squares[i][j].getBackground() == squareColor) {
    							 if (numNeighbours == 3) {
    								 squares[i][j].setAlive(true);
    							 }
    						 }
    						 else {
    							 if (numNeighbours < 2) {
    								 squares[i][j].setAlive(false);
    							 }
    							 else if (numNeighbours > 3) {
    								 squares[i][j].setAlive(false);
    							 }
    						 }
    					}
    				}
    				// add a slight delay so user can see what's happening
    				try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    				iterationsCompleted++;
    				iterationsLabel.setText("Iteration: " + iterationsCompleted + "/" + iterations);
    				// visually change the updated squares in the grid
    				for (int i = 0; i < gridSize; i++) {
    					for (int j = 0; j < gridSize; j++) {
    						if (squares[i][j].getAlive()) {
    							squares[i][j].setBackground(Color.white);
    						}
    						else {
    							squares[i][j].setBackground(squareColor);
    						}
    					}
    				}
    			} 
    			if (iterations == iterationsCompleted) {
    				String s = (String) patternsCB.getSelectedItem();
    				if (s.equals("Custom")) {
    					enableSquares();
    				}
    				reset.setEnabled(true);
    				textField.setEditable(true);
    				stop.setEnabled(false);
    				simulating = false;
    			}
    			start.setEnabled(true);
    		}
		}).start();
    }
}
