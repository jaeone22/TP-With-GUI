package com.tpwithgui.screen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TeleportScreen extends Screen {
	private final List<PlayerButton> playerButtons = new ArrayList<>();
	private int scrollOffset = 0;
	private int maxScroll = 0;

	// 스크롤 영역 설정
	private static final int LIST_TOP = 40;
	private static final int LIST_BOTTOM_OFFSET = 50;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_SPACING = 5;

	public TeleportScreen() {
		super(Text.translatable("gui.tpwithgui.online_players"));
	}

	@Override
	protected void init() {
		super.init();

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.player == null)
			return;

		playerButtons.clear();
		scrollOffset = 0;

		// 플레이어 버튼 생성 및 추가
		for (var player : client.world.getPlayers()) {
			if (!player.getUuid().equals(client.player.getUuid())) {
				String name = player.getName().getString();
				ButtonWidget button = ButtonWidget
						.builder(Text.literal(name), btn -> client.setScreen(new TeleportMenuScreen(name, this)))
						.dimensions(this.width / 2 - 100, 0, 200, BUTTON_HEIGHT).build();
				// 버튼을 위젯으로 추가하여 자동 클릭 처리되도록 함
				this.addDrawableChild(button);
				playerButtons.add(new PlayerButton(button, name));
			}
		}

		// 최대 스크롤 계산
		int listHeight = this.height - LIST_TOP - LIST_BOTTOM_OFFSET;
		int totalContentHeight = playerButtons.size() * (BUTTON_HEIGHT + BUTTON_SPACING);
		maxScroll = Math.max(0, totalContentHeight - listHeight);

		// 닫기 버튼
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.tpwithgui.close"), btn -> this.close())
				.dimensions(this.width / 2 - 100, this.height - 40, 200, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		// 버튼 위치 및 가시성 업데이트
		int listHeight = this.height - LIST_TOP - LIST_BOTTOM_OFFSET;
		int y = LIST_TOP - scrollOffset;

		for (PlayerButton playerButton : playerButtons) {
			ButtonWidget button = playerButton.button;
			button.setY(y);

			// 보이는 영역에 있는 버튼만 활성화
			boolean isVisible = y + BUTTON_HEIGHT > LIST_TOP && y < LIST_TOP + listHeight;
			button.visible = isVisible;
			button.active = isVisible;

			y += BUTTON_HEIGHT + BUTTON_SPACING;
		}

		super.render(context, mouseX, mouseY, delta);

		// 제목
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

		// 스크롤바 렌더링
		if (maxScroll > 0) {
			renderScrollbar(context, listHeight);
		}
	}

	private void renderScrollbar(DrawContext context, int listHeight) {
		int scrollbarX = this.width / 2 + 110;
		int scrollbarWidth = 6;

		// 스크롤바 배경
		context.fill(scrollbarX, LIST_TOP, scrollbarX + scrollbarWidth, LIST_TOP + listHeight, 0xFF000000);

		// 스크롤바 핸들
		int totalContentHeight = playerButtons.size() * (BUTTON_HEIGHT + BUTTON_SPACING);
		float scrollRatio = (float) scrollOffset / maxScroll;
		int handleHeight = Math.max(20, (int) ((float) listHeight * listHeight / totalContentHeight));
		int handleY = LIST_TOP + (int) (scrollRatio * (listHeight - handleHeight));

		context.fill(scrollbarX, handleY, scrollbarX + scrollbarWidth, handleY + handleHeight, 0xFF808080);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (maxScroll > 0) {
			int scrollAmount = (int) (verticalAmount * 20);
			scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollAmount));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private static class PlayerButton {
		final ButtonWidget button;

		PlayerButton(ButtonWidget button, String name) {
			this.button = button;
		}
	}
}
