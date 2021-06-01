package nl.pascalroeleven.minecraft.mineshotrevived.client.util;

import static net.minecraft.util.text.event.ClickEvent.Action.OPEN_FILE;

import java.io.File;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;

public class ChatUtils {
	private static final Minecraft MC = Minecraft.getInstance();

	public static void print(String msg, Color format, Object... args) {
		if (MC.ingameGUI == null) {
			return;
		}

		NewChatGui chat = MC.ingameGUI.getChatGUI();
		TranslationTextComponent ret = new TranslationTextComponent(msg, args);
		ret.getStyle().setColor(format);

		chat.printChatMessage(ret);
	}

	public static void print(String msg, Object... args) {
		print(msg, null, args);
	}

	public static void printFileLink(String msg, File file) {
		TranslationTextComponent text = new TranslationTextComponent(file.getName());
		String path;

		try {
			path = file.getAbsoluteFile().getCanonicalPath();
		} catch (IOException ex) {
			path = file.getAbsolutePath();
		}

		text.getStyle().setClickEvent(new ClickEvent(OPEN_FILE, path));
		text.getStyle().setUnderlined(true);

		print(msg, text);
	}
}
