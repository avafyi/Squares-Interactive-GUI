import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 
 * @author Caleb Piekstra
 *
 */

public class MovePlayer extends AnimatePlayer {
	
	public static ScheduledFuture<?> movePlayer(MapSquare[][] map, int direction, Player p, Callable<?> updateDisplayCallable) {
		if (p == null || !p.allowedToMove) {
			return null;		// Make sure we have a player to move and that they are allowed to move
		}
		boolean animate = false;
		// If the player is not current facing the direction specified via
		// keyboard input, then turn them to face that direction
		if ( p.direction != direction ) {
			p.direction = direction;
		} else {
			animate = true;	
		}
		if (animate) {
			p.allowedToMove = false;
			p.animatePhase = 1;	// Start the animation
			ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
			ScheduledFuture<?> taskHandler = scheduledExecutor.scheduleAtFixedRate(new Animation(p, updateDisplayCallable), 0, ANIMATION_DELAY_STEP, TimeUnit.MILLISECONDS);
		    animationHandlers.put(p.idx, taskHandler);
		} else {
			p.allowedToMove = true;
		}
		return animationHandlers.get(p.idx);
	}
}
