package nl.pascalroeleven.minecraft.mineshotrevived.client;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F9;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import nl.pascalroeleven.minecraft.mineshotrevived.client.capture.task.CaptureTask;
import nl.pascalroeleven.minecraft.mineshotrevived.client.capture.task.RenderTickTask;
import nl.pascalroeleven.minecraft.mineshotrevived.client.util.ChatUtils;

public class ScreenshotHandler {
	private static final Minecraft MC = Minecraft.getInstance();
	private static final Logger L = LogManager.getLogger();
	private static final String KEY_CATEGORY = "key.categories.mineshotrevived";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

	private final KeyBinding keyCapture = new KeyBinding("key.mineshotrevived.capture", GLFW_KEY_F9, KEY_CATEGORY);

	private Path taskFile;
	private RenderTickTask task;

	public ScreenshotHandler() {
		ClientRegistry.registerKeyBinding(keyCapture);
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		// don't poll keys when there's an active task
		if (task != null) {
			return;
		}

		if (keyCapture.isPressed()) {
			taskFile = getScreenshotFile();

			task = new CaptureTask(taskFile);
		}
	}

	@SubscribeEvent
	public void onRenderTick(RenderTickEvent evt) {
		if (task == null) {
			return;
		}

		try {
			if (task.onRenderTick(evt)) {
				task = null;
				ChatUtils.printFileLink("screenshot.success", taskFile.toFile());
			}
		} catch (Exception ex) {
			L.error("Screenshot capture failed", ex);
			ChatUtils.print("screenshot.failure", ex.getMessage());
			task = null;
		}
	}

	private Path getScreenshotFile() {
		Path dir = MC.gameDir.toPath().resolve("screenshots");

		try {
			if (!Files.exists(dir)) {
				Files.createDirectories(dir);
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		// loop though suffixes while the file exists
		int i = 0;
		Path file;
		do {
			file = dir.resolve(String.format("huge_%s_%04d.tga", DATE_FORMAT.format(new Date()), i++));
		} while (Files.exists(file));

		return file;
	}
}
