package bots;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.StringTokenizer;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

/**
 * 
 *
 * 
 */
public class PabloEscobarBot extends Bot {

	/**
	 * Image for drawing
	 */
	private Image image = null;
	/**
	 * Next message to send
	 */
	private String nextMessage;
	/**
	 * My name
	 */
	private String name;

	/**
	 * Name of my image
	 */
	public String[] imageNames()
	{
		String[] images = {"Twitter-logi.svg.png"};
		return images;
	}

	/**
	 * Save my image
	 */
	public void loadedImages(Image[] images)
	{
		if (images != null && images.length > 0)
			image = images[0];
	}

	/**
	 * This is your main method.  Decide on what your bot will do here. Please use the constants
	 * in the Arena class.
	 */
	public int getMove(BotInfo me, boolean shotOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets)
	{
		//if there is a dangerous bullet
			//move in best direction
		//else if there is a close bot
			//fire in their direction
		//else
			//move towards closest victim
		return BattleBotArena.FIREUP;
	}
	//POSSIBLE METHODS - to start
	//1. getDistance(int x1, int y1, int x2, int y2)
	//2. getDangerLevel(BotInfo[] me, Bullet bullet) -returns distance if dangerous, -1 otherwise
	//3. mostDangerous(BotInfo[] me, Bullet[] bullets)
	//4. dodge(BotInfo[] me, Bullet[] bullets)
	//
	private double getDistance(double x1, double y1, double x2, double y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	private double getAngle(double x1, double y1, double x2, double y2) {
	    double dx = x2 - x1;
	    double dy = y2 - y1;
	    return Math.atan2(dy, dx);
	}

	private double[] getBulletTrajectory(Bullet bullet, double time) {
	    double newX = bullet.getX() + bullet.getXSpeed() * time;
	    double newY = bullet.getY() + bullet.getYSpeed() * time;
	    return new double[]{newX, newY};
	}


	/**
	 * Construct and return my name
	 */
	public String getName()
	{
		return "JosePablo";
	}

	/**
	 * What do you want to do before the round starts?
	 */
	public void newRound()
	{

	}

	/**
	 * Draw the bot
	 */
	public void draw (Graphics g, int x, int y)
	{
		g.drawImage(image, x,y,Bot.RADIUS*2, Bot.RADIUS*2, null);
	}

	/**
	 * Required method
	 */
	public void incomingMessage(int id, String msg)
	{
	}

	/**
	 * Team name
	 */
	public String getTeamName()
	{
		return "Team";
	}

	/**
	 * Send and clear my message buffer
	 */
	public String outgoingMessage()
	{
		String msg = nextMessage;
		nextMessage = null;
		return msg;
	}
}
