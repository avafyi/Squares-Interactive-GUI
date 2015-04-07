import java.util.TimerTask;
import java.util.concurrent.Callable;

/**
 * 
 * 
 * @author Caleb Piekstra
 *
 */

public class Animation extends TimerTask {
	private Player p = null;
	private int speedToggle;
	private Callable<?> callable;
	
	public Animation(Player p, Callable<?> callme) {
		this.p = p;
		speedToggle = p.speed;
		callable = callme;
	}
	public void run() {
		// If the player is walking, the animation will occur every other run()
		if (--speedToggle <= 0) { 
			speedToggle = p.speed;	// Update the speed of the player in case they started sprinting during the animation
			AnimatePlayer.animatePlayer(p, callable);
		}
	}
}
