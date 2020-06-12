package nl.pascalroeleven.minecraft.mineshotrevived.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public interface PrivateAccessor {
	static final Logger L = LogManager.getLogger();

	final Method framebufferSizeUpdate = ObfuscationReflectionHelper.findMethod(MainWindow.class, "func_198102_b",
			long.class, int.class, int.class);
	final Method setDirection = ObfuscationReflectionHelper.findMethod(ActiveRenderInfo.class, "func_216776_a",
			float.class, float.class);
	final Method movePosition = ObfuscationReflectionHelper.findMethod(ActiveRenderInfo.class, "func_216782_a",
			double.class, double.class, double.class);
	final Method calcCameraDistance = ObfuscationReflectionHelper.findMethod(ActiveRenderInfo.class, "func_216779_a",
			double.class);
	static final Field thirdPerson = ObfuscationReflectionHelper.findField(ActiveRenderInfo.class, "field_216799_k");

	default void framebufferSizeUpdate(Minecraft mc, int framebufferWidth, int framebufferHeight) {
		try {
			framebufferSizeUpdate.invoke(mc.getMainWindow(), mc.getMainWindow().getHandle(), framebufferWidth,
					framebufferHeight);
		} catch (Exception ex) {
			L.error("onFramebufferSizeUpdate() failed", ex);
		}
	}

	default void setDirection(Minecraft mc, float pitchIn, float yawIn) {
		try {
			setDirection.invoke(mc.gameRenderer.getActiveRenderInfo(), pitchIn, yawIn);
		} catch (Exception ex) {
			L.error("setDirection() failed", ex);
		}
	}

	default void movePosition(Minecraft mc, double distanceOffset, double verticalOffset, double horizontalOffset) {
		try {
			movePosition.invoke(mc.gameRenderer.getActiveRenderInfo(), distanceOffset, verticalOffset,
					horizontalOffset);
		} catch (Exception ex) {
			L.error("movePosition() failed", ex);
		}
	}

	default double calcCameraDistance(Minecraft mc, double startingDistance) {
		try {
			return (double) calcCameraDistance.invoke(mc.gameRenderer.getActiveRenderInfo(), startingDistance);
		} catch (Exception ex) {
			L.error("calcCameraDistance() failed", ex);
		}
		return -1;
	}

	default void setThirdPerson(Minecraft mc, boolean thirdPersonIn) {
		try {
			thirdPerson.set(mc.gameRenderer.getActiveRenderInfo(), thirdPersonIn);
		} catch (Exception ex) {
			L.error("setLook() failed", ex);
		}
	}
}
