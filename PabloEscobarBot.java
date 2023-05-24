package bots;

import java.awt.Color;

import java.awt.geom.Point2D;
import java.util.HashMap;

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

	private HashMap<String, Point2D.Double> previousBotPositions = new HashMap<>();
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
	@Override
	public int getMove(BotInfo me, boolean shotOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {
	    // Dodge bullets if necessary
	    int dodgeMove = dodge(me, bullets, liveBots, deadBots);
	    if (dodgeMove != BattleBotArena.STAY) {
	        return dodgeMove;
	    }

	    // Determine nearest dead bot
	    BotInfo nearestDeadBot = findNearestDeadBot(me, deadBots);

	    // If there is a dead bot nearby, move towards it
	    if (nearestDeadBot != null) {
	        return moveToTarget(me, nearestDeadBot.getX(), nearestDeadBot.getY());
	    }

	    // Determine nearest live bot
	    BotInfo nearestLiveBot = null;
	    double minDistance = Double.MAX_VALUE;
	    for (BotInfo bot : liveBots) {
	        if (!bot.getName().equals(me.getName())) {
	            double distance = getDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
	            if (distance < minDistance) {
	                minDistance = distance;
	                nearestLiveBot = bot;
	            }
	        }
	    }

	    // If there is a live bot nearby, move towards it and fire
	    if (nearestLiveBot != null) {
	        // Determine angle to nearest live bot
	        double angleToBot = getAngle(me.getX(), me.getY(), nearestLiveBot.getX(), nearestLiveBot.getY());

	        // Determine direction to move towards nearest live bot
	        int moveDirection = setMoveHeading(angleToBot);

	        // Determine direction to fire towards nearest live bot
	        int fireDirection = setFireHeading(angleToBot);

	        // Fire if able to and on target
	        if (shotOK && fireDirection == moveDirection) {
	            return fireDirection;
	        }

	        // Move towards nearest live bot
	        return moveDirection;
	    }

	    // Move randomly
	    return (int) (Math.random() * 4) + 1;
	}


	//POSSIBLE METHODS - to start
	//1. getDistance(int x1, int y1, int x2, int y2)
	//2. getDangerLevel(BotInfo[] me, Bullet bullet) -returns distance if dangerous, -1 otherwise
	//3. mostDangerous(BotInfo[] me, Bullet[] bullets)
	//4. dodge(BotInfo[] me, Bullet[] bullets)
	//
	private int setFireHeading(double angle) {
		if (angle >= 45 && angle < 135) {
			return BattleBotArena.FIREUP;
		} else if (angle >= 135 && angle < 225) {
			return BattleBotArena.FIRELEFT;
		} else if (angle >= 225 && angle < 315) {
			return BattleBotArena.FIREDOWN;
		} else {
			return BattleBotArena.FIRERIGHT;
		}
	}
	

	private int setMoveHeading(double angle) {
		if (angle >= 45 && angle < 135) {
			return BattleBotArena.UP;
		} else if (angle >= 135 && angle < 225) {
			return BattleBotArena.LEFT;
		} else if (angle >= 225 && angle < 315) {
			return BattleBotArena.DOWN;
		} else {
			return BattleBotArena.RIGHT;
		}
	}

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

	private BotInfo findNearestDeadBot(BotInfo me, BotInfo[] deadBots) {
		BotInfo nearestDeadBot = null;
		double minDistance = Double.MAX_VALUE;
		for (BotInfo deadBot : deadBots) {
			double distance = getDistance(me.getX(), me.getY(), deadBot.getX(), deadBot.getY());
			if (distance < minDistance) {
				minDistance = distance;
				nearestDeadBot = deadBot;
			}
		}
		return nearestDeadBot;
	}

	private boolean isBulletDangerous(BotInfo me, Bullet bullet) {
		double bulletX = bullet.getX();
		double bulletY = bullet.getY();
		double botX = me.getX();
		double botY = me.getY();

		// Calculate the distance between the bullet and the bot
		double distance = getDistance(botX, botY, bulletX, bulletY);

		// If the bullet is too far away, it's not dangerous
		if (distance > 150) {
			return false;
		}

		// Use the bullet's speed as a proxy for its trajectory
		double bulletTrajectoryX = bullet.getXSpeed();
		double bulletTrajectoryY = bullet.getYSpeed();

		// Calculate the vector from the bullet to the bot
		double bulletToBotX = botX - bulletX;
		double bulletToBotY = botY - bulletY;

		// Calculate the dot product of the bullet trajectory and the vector from the bullet to the bot
		double dotProduct = bulletTrajectoryX * bulletToBotX + bulletTrajectoryY * bulletToBotY;

		// If the dot product is negative, the bullet is moving away from the bot
		if (dotProduct < 0) {
			return false;
		}

		// Calculate the squared length of the bullet trajectory and the bullet-to-bot vector
		double bulletTrajectoryLengthSquared = bulletTrajectoryX * bulletTrajectoryX + bulletTrajectoryY * bulletTrajectoryY;
		double bulletToBotLengthSquared = bulletToBotX * bulletToBotX + bulletToBotY * bulletToBotY;

		// Calculate the squared distance from the bot to the line of the bullet trajectory
		double squaredDistanceFromBotToLine = bulletToBotLengthSquared - (dotProduct * dotProduct) / bulletTrajectoryLengthSquared;

		// If the squared distance is less than the squared sum of the bot radius and an arbitrary bullet radius, it's dangerous
		double dangerThresholdSquared = Math.pow(Bot.RADIUS + 1, 2); // Assuming an arbitrary bullet radius of 1
		return squaredDistanceFromBotToLine < dangerThresholdSquared;
	}

	private int dodge(BotInfo me, Bullet[] bullets, BotInfo[] liveBots, BotInfo[] deadBots) {
	    // Find the most dangerous bullet
	    Bullet mostDangerousBullet = mostDangerousBullet(me, bullets);
	       int move = 0;

	    if (mostDangerousBullet != null) {
	        // Check if there's a tombstone close enough to use as a shield
	        double minDistanceToTombstone = Double.MAX_VALUE;
	        BotInfo nearestTombstone = null;

	        for (BotInfo deadBot : deadBots) {       	
	                double distance = getDistance(me.getX(), me.getY(), deadBot.getX(), deadBot.getY());
	                if (distance < minDistanceToTombstone) {
	                    minDistanceToTombstone = distance;
	                    nearestTombstone = deadBot;
	            }
	        }

	        // If a tombstone is close enough, move towards it
	        if (nearestTombstone != null && minDistanceToTombstone < 100) {
	            return moveToTarget(me, nearestTombstone.getX(), nearestTombstone.getY());
	        } else {
	            // Calculate potential dodging positions
	            double dodgeUpX = me.getX();
	            double dodgeUpY = me.getY() - Bot.RADIUS * 2;
	            double dodgeDownX = me.getX();
	            double dodgeDownY = me.getY() + Bot.RADIUS * 2;
	            double dodgeLeftX = me.getX() - Bot.RADIUS * 2;
	            double dodgeLeftY = me.getY();
	            double dodgeRightX = me.getX() + Bot.RADIUS * 2;
	            double dodgeRightY = me.getY();

	            boolean nearTop = me.getY() < 200;
	            boolean nearBottom = me.getY() > BattleBotArena.HEIGHT - 200;
	            boolean nearLeft = me.getX() < 200;
	            boolean nearRight = me.getX() > BattleBotArena.WIDTH - 200;

	            // Prioritize moving towards the center if near edge of screen
	            if (nearTop || nearBottom) {
	                dodgeUpY = BattleBotArena.HEIGHT / 2;
	                dodgeDownY = BattleBotArena.HEIGHT / 2;
	            }
	            if (nearLeft || nearRight) {
	                dodgeLeftX = BattleBotArena.WIDTH / 2;
	                dodgeRightX = BattleBotArena.WIDTH / 2;
	            }
	            
	         // Calculate the danger scores for each dodging position
	            double upDangerScore = calculateDangerScore(dodgeUpX, dodgeUpY, bullets, liveBots);
	            double downDangerScore = calculateDangerScore(dodgeDownX, dodgeDownY, bullets, liveBots);
	            double leftDangerScore = calculateDangerScore(dodgeLeftX, dodgeLeftY, bullets, liveBots);
	            double rightDangerScore = calculateDangerScore(dodgeRightX, dodgeRightY, bullets, liveBots);

	            // Calculate danger score for dodging up or down based on the nearest bot's position and heading
	            double distanceToClosestBot = Double.MAX_VALUE;
	            BotInfo closestBot = null;
	            for (BotInfo bot : liveBots) {
	                double distanceToBot = getDistance(me.getX(), me.getY(), bot.getX(), bot.getY());
	                if (distanceToBot < distanceToClosestBot) {
	                    distanceToClosestBot = distanceToBot;
	                    closestBot = bot;
	                }
	            }
	            double angleToBot = 0;
	            if (closestBot != null) {
	                angleToBot = angleBetween(me.getX(), me.getY(), closestBot.getX(), closestBot.getY());
	            }
	            double dodgeUpDangerScore = calculateDangerScore(dodgeUpX, dodgeUpY, bullets, liveBots);
	            double dodgeDownDangerScore = calculateDangerScore(dodgeDownX, dodgeDownY, bullets, liveBots);
	            double dodgeLeftDangerScore = calculateDangerScore(dodgeLeftX, dodgeLeftY, bullets, liveBots);
	            double dodgeRightDangerScore = calculateDangerScore(dodgeRightX, dodgeRightY, bullets, liveBots);



	            // Find the direction with the lowest danger score
	            double minDangerScore = Math.min(Math.min(upDangerScore, downDangerScore), Math.min(leftDangerScore, rightDangerScore));
	            minDangerScore = Math.min(minDangerScore, Math.min(dodgeUpDangerScore, dodgeDownDangerScore));

	            // Move the bot in the direction of the least dangerous dodging position
	            if (minDangerScore == upDangerScore) {
	            	move = BattleBotArena.UP;
	            } else if (minDangerScore == downDangerScore) {
	            	move = BattleBotArena.DOWN;

	            } else if (minDangerScore == leftDangerScore) {
	            	move = BattleBotArena.LEFT;
	            } else if (minDangerScore == rightDangerScore) {
	            	move = BattleBotArena.RIGHT;
	            } else if (minDangerScore == dodgeUpDangerScore) {
	            	move = BattleBotArena.UP;
	            } else {
	            	move = BattleBotArena.DOWN;
	            }
	        }
	    }
		return move;
	}

			private int moveToTarget(BotInfo me, double targetX, double targetY) {
				double dx = targetX - me.getX();
				double dy = targetY - me.getY();

				if (Math.abs(dx) > Math.abs(dy)) {
					if (dx > 0) {
						return BattleBotArena.RIGHT;
					} else {
						return BattleBotArena.LEFT;
					}
				} else {
					if (dy > 0) {
						return BattleBotArena.DOWN;
					} else {
						return BattleBotArena.UP;
					}
				}
			}

			private double calculateDangerScore(double x, double y, Bullet[] bullets, BotInfo[] liveBots) {
				double dangerScore = 0;

				for (Bullet bullet : bullets) {
					double distance = getDistance(x, y, bullet.getX(), bullet.getY());
					dangerScore += 1 / (distance * distance); // The closer the bullet, the higher the danger score
				}

				for (BotInfo bot : liveBots) {
					Point2D.Double previousPosition = previousBotPositions.get(bot.getName());

					if (previousPosition != null) {
						double botHeadingX = bot.getX() - previousPosition.x;
						double botHeadingY = bot.getY() - previousPosition.y;

						double botToX = x - bot.getX();
						double botToY = y - bot.getY();
						double distance = Math.sqrt(botToX * botToX + botToY * botToY);
						double dotProduct = botToX * botHeadingX + botToY * botHeadingY;

						if (dotProduct > 0) { // The bot is facing the point (x, y)
							dangerScore += 1 / (distance * distance); // The closer the bot, the higher the danger score
						}
					}
				}

				return dangerScore;
			}

			private Bullet mostDangerousBullet(BotInfo me, Bullet[] bullets) {
				Bullet mostDangerousBullet = null;
				double maxDangerScore = Double.MIN_VALUE;

				for (Bullet bullet : bullets) {
					if (isBulletDangerous(me, bullet)) {
						double distanceFromBullet = getDistance(me.getX(), me.getY(), bullet.getX(), bullet.getY());
						double dangerScore = 1 / (distanceFromBullet * distanceFromBullet); // Inverse square of distance

						if (dangerScore > maxDangerScore) {
							maxDangerScore = dangerScore;
							mostDangerousBullet = bullet;
						}
					}
				}

				return mostDangerousBullet;
			}

			private double angleBetween(double x1, double y1, double x2, double y2) {
				double angle = Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
				if(angle < 0){
					angle += 360;
				}
				return angle;
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
