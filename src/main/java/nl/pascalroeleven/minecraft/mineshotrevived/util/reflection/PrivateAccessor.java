package nl.pascalroeleven.minecraft.mineshotrevived.util.reflection;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public interface PrivateAccessor {
	static final Logger L = LogManager.getLogger();

	final Method framebufferSizeUpdate = ObfuscationReflectionHelper.findMethod(MainWindow.class, "func_198102_b",
			long.class, int.class, int.class);

	default void framebufferSizeUpdate(Minecraft mc, int framebufferWidth, int framebufferHeight) {
		try {
			framebufferSizeUpdate.invoke(mc.getMainWindow(), mc.getMainWindow().getHandle(), framebufferWidth,
					framebufferHeight);
		} catch (Exception ex) {
			L.error("onFramebufferSizeUpdate() failed", ex);
		}
	}
}
