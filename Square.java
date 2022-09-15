import javax.swing.*;
/*
 *  class used to simplify setting up the buttons/squares in the gui grid
 */
public class Square extends JButton
{
    private int xcoord, ycoord;
    private boolean alive;

    public Square(int xcoord, int ycoord)
    {
        super();
        this.xcoord = xcoord;
        this.ycoord = ycoord;
        alive = false;
    }

    // getters and setters
    public int getXcoord()
    {
        return xcoord;
    }

    public int getYcoord()
    {
        return ycoord;
    }
    
    public void setAlive(boolean aliveOrNot) {
    	alive = aliveOrNot;
    }
    
    public boolean getAlive() {
    	return alive;
    }
}