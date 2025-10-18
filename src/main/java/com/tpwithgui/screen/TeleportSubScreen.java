package com.tpwithgui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TeleportSubScreen extends Screen {
	private final String playerName;

	public TeleportSubScreen(String playerName, Screen previousScreen) {
		super(Text.translatable("gui.tpwithgui.teleport_title", playerName));
		this.playerName = playerName;
	}

	@Override
	protected void init() {
		super.init();
		MinecraftClient client = MinecraftClient.getInstance();

		int midY = this.height / 2;
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.tpwithgui.teleport_to", playerName), btn -> {
			if (client.getNetworkHandler() != null) {
				client.getNetworkHandler().sendChatCommand("/tp " + playerName);
			}
			this.close();
		}).dimensions(this.width / 2 - 100, midY - 30, 200, 20).build());

		this.addDrawableChild(
				ButtonWidget.builder(Text.translatable("gui.tpwithgui.teleport_to_me", playerName), btn -> {
					if (client.getNetworkHandler() != null && client.player != null) {
						client.getNetworkHandler()
								.sendChatCommand("/tp " + playerName + " " + client.player.getName().getString());
					}
					this.close();
				}).dimensions(this.width / 2 - 100, midY, 200, 20).build());

		this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.tpwithgui.cancel"), btn -> this.close())
				.dimensions(this.width / 2 - 100, midY + 30, 200, 20).build());
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
