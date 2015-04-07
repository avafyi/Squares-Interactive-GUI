import java.util.TimerTask;
import java.util.concurrent.Callable;

/**
 * 
 * 
 * @author Caleb Piekstra
 *
 */
public class Animators {

	public static class MoveAnimation extends TimerTask {
		private Player player = null;
		private int speedToggle;
		private Callable<?> callable;
		
		public MoveAnimation(Player player, Callable<?> callme) {
			this.player = player;
			speedToggle = player.speed;
			callable = callme;
		}
		public void run() {
			// If the player is walking, the animation will occur every other run()
			if (--speedToggle <= 0) { 
				speedToggle = player.speed;	// Update the speed of the player in case they started sprinting during the animation
				AnimatePlayer.animatePlayer(player, callable);
			}
		}
	}

//	public static class JumpAnimation extends TimerTask {
//		private Player player = null;
//		private int speedToggle;
//		private Callable<?> callable;
//		
//		public JumpAnimation(Player player, Callable<?> callme) {
//			this.player = player;
//			speedToggle = player.speed;
//			callable = callme;
//		}
//		public void run() {
//			// If the player is walking, the animation will occur every other run()
//			if (--speedToggle <= 0) { 
//				speedToggle = player.speed;	// Update the speed of the player in case they started sprinting during the animation
//				AnimatePlayer.animatePlayer(player, callable);
//			}
//		}
//	}
}
