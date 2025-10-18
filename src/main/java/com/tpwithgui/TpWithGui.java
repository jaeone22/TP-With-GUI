package com.tpwithgui;

import com.tpwithgui.screen.TeleportScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TpWithGui implements ClientModInitializer {
	public static final String MOD_ID = "tpwithgui";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// 키바인드
	private static KeyBinding openGuiKey;

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing TP With GUI...");

		// 키바인드 등록 (기본값 K)
		openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.tpwithgui.open_gui",
				GLFW.GLFW_KEY_K,
				KeyBinding.Category.MISC));

		// 클라이언트 명령어 등록: /tpgui
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("tpgui")
					.executes(context -> {
						// GUI 열기
						context.getSource().getClient().setScreen(new TeleportScreen());
						return 1;
					}));
		});

		// 클라이언트 명령어 등록: /.
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal(".")
					.executes(context -> {
						// GUI 열기
						context.getSource().getClient().setScreen(new TeleportScreen());
						return 1;
					}));
		});

		// 키 입력 감지
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openGuiKey.wasPressed()) {
				// 화면이 없을 때 (게임 플레이 중일 때)
				if (client.currentScreen == null) {
					client.setScreen(new TeleportScreen());
				}
			}
		});

		LOGGER.info("TP With GUI initialized successfully");
	}
}
