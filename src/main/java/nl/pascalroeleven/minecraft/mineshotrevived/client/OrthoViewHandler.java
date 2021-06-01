package nl.pascalroeleven.minecraft.mineshotrevived.client;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_3;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_4;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_5;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_6;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_7;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_8;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_MULTIPLY;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.settings.PointOfView;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import nl.pascalroeleven.minecraft.mineshotrevived.client.util.ChatUtils;
import nl.pascalroeleven.minecraft.mineshotrevived.util.reflection.PrivateAccessor;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.FORGE)
public class OrthoViewHandler implements PrivateAccessor {
	private static final Minecraft MC = Minecraft.getInstance();
	private static final String KEY_CATEGORY = "key.categories.mineshotrevived";
	private static final float ZOOM_STEP = 0.5f;
	private static final float ROTATE_STEP = 15;
	private static final float ROTATE_SPEED = 4;
	private static final float SECONDS_PER_TICK = 1f / 20f;

	private final KeyBinding keyToggle = new KeyBinding("key.mineshotrevived.ortho.toggle", GLFW_KEY_KP_5,
			KEY_CATEGORY);
	private final KeyBinding keyZoomIn = new KeyBinding("key.mineshotrevived.ortho.zoom_in", GLFW_KEY_KP_ADD,
			KEY_CATEGORY);
	private final KeyBinding keyZoomOut = new KeyBinding("key.mineshotrevived.ortho.zoom_out", GLFW_KEY_KP_SUBTRACT,
			KEY_CATEGORY);
	private final KeyBinding keyRotateL = new KeyBinding("key.mineshotrevived.ortho.rotate_l", GLFW_KEY_KP_4,
			KEY_CATEGORY);
	private final KeyBinding keyRotateR = new KeyBinding("key.mineshotrevived.ortho.rotate_r", GLFW_KEY_KP_6,
			KEY_CATEGORY);
	private final KeyBinding keyRotateU = new KeyBinding("key.mineshotrevived.ortho.rotate_u", GLFW_KEY_KP_8,
			KEY_CATEGORY);
	private final KeyBinding keyRotateD = new KeyBinding("key.mineshotrevived.ortho.rotate_d", GLFW_KEY_KP_2,
			KEY_CATEGORY);
	private final KeyBinding keyRotateT = new KeyBinding("key.mineshotrevived.ortho.rotate_t", GLFW_KEY_KP_7,
			KEY_CATEGORY);
	private final KeyBinding keyRotateF = new KeyBinding("key.mineshotrevived.ortho.rotate_f", GLFW_KEY_KP_1,
			KEY_CATEGORY);
	private final KeyBinding keyRotateS = new KeyBinding("key.mineshotrevived.ortho.rotate_s", GLFW_KEY_KP_3,
			KEY_CATEGORY);
	private final KeyBinding keyClip = new KeyBinding("key.mineshotrevived.ortho.clip", GLFW_KEY_KP_MULTIPLY,
			KEY_CATEGORY);
	private final KeyBinding keyMod = new KeyBinding("key.mineshotrevived.ortho.mod", GLFW_KEY_LEFT_CONTROL,
			KEY_CATEGORY);

	private boolean enabled;
	private boolean freeCam;
	private boolean clip;

	private float zoom;
	private float xRot;
	private float yRot;

	private int tick;
	private int tickPrevious;
	private double partialPrevious;

	private PointOfView thirdPersonView;

	public OrthoViewHandler() {
		ClientRegistry.registerKeyBinding(keyToggle);
		ClientRegistry.registerKeyBinding(keyZoomIn);
		ClientRegistry.registerKeyBinding(keyZoomOut);
		ClientRegistry.registerKeyBinding(keyRotateL);
		ClientRegistry.registerKeyBinding(keyRotateR);
		ClientRegistry.registerKeyBinding(keyRotateU);
		ClientRegistry.registerKeyBinding(keyRotateD);
		ClientRegistry.registerKeyBinding(keyRotateT);
		ClientRegistry.registerKeyBinding(keyRotateF);
		ClientRegistry.registerKeyBinding(keyRotateS);
		ClientRegistry.registerKeyBinding(keyClip);
		ClientRegistry.registerKeyBinding(keyMod);

		reset();
	}

	private void reset() {
		freeCam = false;
		clip = false;

		zoom = 8;
		xRot = 30;
		yRot = -45;
		tick = 0;
		tickPrevious = 0;
		partialPrevious = 0;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void enable() {
		// disable in multiplayer
		// Of course, programmers could just delete this check and abuse the
		// orthographic camera, but at least the official build won't support it
		if (!MC.isSingleplayer()) {
			ChatUtils.print("mineshotrevived.orthomp");
			return;
		}

		if (!enabled) {
			/*
			 * clippingEnabled = clippingHelper.isEnabled();
			 * clippingHelper.setEnabled(false);
			 */
			reset();
		}

		enabled = true;
	}

	public void disable() {
		if (enabled) {
			/* clippingHelper.setEnabled(clippingEnabled); */
		}

		enabled = false;
	}

	public void toggle() {
		if (isEnabled()) {
			disable();
		} else {
			enable();
		}
	}

	private boolean modifierKeyPressed() {
		return keyMod.isKeyDown();
	}

	private void updateZoomAndRotation(double multi) {
		if (keyZoomIn.isKeyDown()) {
			zoom *= 1 - ZOOM_STEP * multi;
		}
		if (keyZoomOut.isKeyDown()) {
			zoom *= 1 + ZOOM_STEP * multi;
		}

		if (keyRotateL.isKeyDown()) {
			yRot += ROTATE_STEP * multi;
		}
		if (keyRotateR.isKeyDown()) {
			yRot -= ROTATE_STEP * multi;
		}

		if (keyRotateU.isKeyDown()) {
			xRot += ROTATE_STEP * multi;
		}
		if (keyRotateD.isKeyDown()) {
			xRot -= ROTATE_STEP * multi;
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent evt) {
		boolean mod = modifierKeyPressed();

		// change perspectives, using modifier key for opposite sides
		if (keyToggle.isKeyDown()) {
			if (mod) {
				freeCam = !freeCam;
			} else {
				toggle();
			}
		} else if (keyClip.isKeyDown()) {
			clip = !clip;
		} else if (keyRotateT.isKeyDown()) {
			xRot = mod ? -90 : 90;
			yRot = 0;
		} else if (keyRotateF.isKeyDown()) {
			xRot = 0;
			yRot = mod ? -90 : 90;
		} else if (keyRotateS.isKeyDown()) {
			xRot = 0;
			yRot = mod ? 180 : 0;
		}

		// update stepped rotation/zoom controls
		// note: the smooth controls are handled in onFogDensity, since they need to be
		// executed on every frame
		if (mod) {
			updateZoomAndRotation(1);
			// snap values to step units
			xRot = Math.round(xRot / ROTATE_STEP) * ROTATE_STEP;
			yRot = Math.round(yRot / ROTATE_STEP) * ROTATE_STEP;
			zoom = Math.round(zoom / ZOOM_STEP) * ZOOM_STEP;
		}
	}

	@SubscribeEvent
	public void onClientTickEvent(final ClientTickEvent event) {
		if (!enabled || event.phase != Phase.START) {
			return;
		}

		tick++;
	}

	@SubscribeEvent
	public void onRenderTickStart(RenderTickEvent evt) {
		if (!enabled || evt.phase != Phase.START) {
			return;
		}

		if (!freeCam) {
			// Turn off thirdPersonView off temporary
			thirdPersonView = MC.gameSettings.getPointOfView();
			MC.gameSettings.setPointOfView(PointOfView.FIRST_PERSON);
		}

	}

	@SubscribeEvent
	public void cameraSetup(CameraSetup event) {
		if (!enabled) {
			return;
		}

		if (!freeCam) {
			// Execute the last part of ActiveRenderInfo->update (but don't care about the
			// renderViewEntity.isSleeping part) because we have overridden it by turning
			// thirdPersonView off temporarily.
			// However this time with our camera angles instead of the entity's.
			// We also don't need to distinguish between thirdPerson and thirdPersonReverse
			// cameras
			setDirection(MC, yRot + 180.0F, xRot);

			if (thirdPersonView != PointOfView.FIRST_PERSON) {
				movePosition(MC, -calcCameraDistance(MC, 4.0D), 0.0D, 0.0D);

				// Make sure the player is rendered for this frame (side effect of temporarily
				// disabling thirdPersonView)
				setThirdPerson(MC, true);
			}

			// Set thirdPersonView back to what it was
			MC.gameSettings.setPointOfView(thirdPersonView);

			event.setPitch(xRot);
			event.setYaw(yRot + 180);
		}
	}

	@SubscribeEvent
	public void onFogDensity(EntityViewRenderEvent.FogDensity evt) {
		if (!enabled) {
			return;
		}

		// update zoom and rotation
		if (!modifierKeyPressed()) {
			int ticksElapsed = tick - tickPrevious;
			double partial = evt.getRenderPartialTicks();
			double elapsed = ticksElapsed + (partial - partialPrevious);
			elapsed *= SECONDS_PER_TICK * ROTATE_SPEED;
			updateZoomAndRotation(elapsed);

			tickPrevious = tick;
			partialPrevious = partial;
		}

		float width = zoom
				* (MC.getMainWindow().getFramebufferWidth() / (float) MC.getMainWindow().getFramebufferHeight());
		float height = zoom;

		// override projection matrix
		RenderSystem.matrixMode(GL_PROJECTION);
		RenderSystem.loadIdentity();
		RenderSystem.ortho(-width, width, -height, height, clip ? 0 : -9999, 9999);
	}
}
