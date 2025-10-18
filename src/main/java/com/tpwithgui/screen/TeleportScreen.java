package com.tpwithgui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TeleportScreen extends Screen {
	public TeleportScreen() {
		super(Text.literal("Online Players"));
	}

	@Override
	protected void init() {
		super.init();

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.player == null)
			return;

		int y = 60;
		for (var player : client.world.getPlayers()) {
			if (!player.getUuid().equals(client.player.getUuid())) {
				String name = player.getName().getString();
				this.addDrawableChild(ButtonWidget
						.builder(Text.literal(name), btn -> client.setScreen(new TeleportMenuScreen(name, this)))
						.dimensions(this.width / 2 - 100, y, 200, 20).build());
				y += 25;
			}
		}

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), btn -> this.close())
				.dimensions(this.width / 2 - 100, this.height - 40, 200, 20).build());
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
