import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

/**
 * 
 * 
 * @author Caleb Piekstra
 *
 */

public class AnimatePlayer {
	
	// ANIMATION 
	
	// in milliseconds, represents the total theoretical time of the moving animation between two squares
	public static final int ANIMATION_DELAY = 250;										
	// in milliseconds, how much time each phase of the moving animation should take
	public static final int ANIMATION_DELAY_STEP = ANIMATION_DELAY / Player.NUM_PHASES; 
	
	// Pair each animation executor with a playerId to make sure that each player gets their own executor
	public static Hashtable<Integer, ScheduledFuture<?>> animationHandlers = new Hashtable<Integer, ScheduledFuture<?>>();
	
	// This sequence will have to be modified so that both the source and destination tiles
	// are considered to be occupied during the animation in order to reserve the destination
	// so that no other player can try to occupy it during the animation sequence
	public static void animatePlayer(Player p, Callable<?> updateDisplayCallable) {	
		if (p == null) {
			return;
		}
		boolean endPhases = false;
		if (p.animatePhase >= Player.NUM_PHASES) {
			endPhases = true;
		}		
		if (!p.inAnimationPhase) {
			p.inAnimationPhase = true;
			p.animatePhase++;
			
			// Update the display
			try {
				updateDisplayCallable.call();
			} catch (Exception e) {}
		}	
		if (endPhases) {		
			// Reset to default of no animation		
			p.animatePhase = 0;			
			
			// Let the player move again now that the animation is complete
			p.allowedToMove = true; 
			
			// Kill the animation timer by canceling the handler
			animationHandlers.get(p.idx).cancel(true);
			
			// Update the display			
			try {
				updateDisplayCallable.call();
			} catch (Exception e) {}
		}
	}
}
