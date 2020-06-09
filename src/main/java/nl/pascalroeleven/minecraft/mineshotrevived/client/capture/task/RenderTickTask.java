package nl.pascalroeleven.minecraft.mineshotrevived.client.capture.task;

import net.minecraftforge.event.TickEvent.RenderTickEvent;

public interface RenderTickTask {

	/**
	 * Called on every frame to update the task.
	 * 
	 * @param evt current render tick event
	 * @return true if the task is done and can be disposed or false if it should
	 *         continue to be updated.
	 * @throws Exception
	 */
	boolean onRenderTick(RenderTickEvent evt) throws Exception;
}
