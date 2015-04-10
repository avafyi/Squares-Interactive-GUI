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
	
	public static void movePlayer(int direction, Player player, Callable<?> updateDisplayCallable) {
		if (player == null || !player.allowedToMove) {
			return;		// Make sure we have a player to move and that they are allowed to move
		}
		boolean animate = false;
		// If the player is not current facing the direction specified via
		// keyboard input, then turn them to face that direction
		if (player.direction != direction) {
			player.direction = direction;
		} else {
			animate = true;	
		}
		if (animate) {
			player.allowedToMove = false;
			player.animatePhase = 1;	// Start the animation
			ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
			int delay = player.isJumping ? ANIMATION_DELAY_JUMP : ANIMATION_DELAY_STEP;
			ScheduledFuture<?> taskHandler = scheduledExecutor.scheduleAtFixedRate(new Animators.MoveAnimation(player, updateDisplayCallable), 0, delay, TimeUnit.MILLISECONDS);
		    animationHandlers.put(player.idx, taskHandler);
		} else {
			player.allowedToMove = true;
		}
	}
}
