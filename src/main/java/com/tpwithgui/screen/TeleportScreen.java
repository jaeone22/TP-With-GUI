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
	private String selectedPlayer = null;

	// 화면 분할 설정
	private static final int SPLIT_WIDTH = 200; // 왼쪽 플레이어 목록 너비
	private static final int LIST_TOP = 40;
	private static final int LIST_BOTTOM_OFFSET = 50;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_SPACING = 5;
	private static final int TOTAL_WIDTH = 400; // 전체 창 너비
	private static final int TOTAL_HEIGHT = 250; // 전체 창 높이
	
	// 메뉴 버튼들
	private ButtonWidget teleportToButton;
	private ButtonWidget teleportToMeButton;

    public TeleportScreen() {
		super(Text.translatable("gui.tpwithgui.teleport_menu"));
	}

	@Override
	protected void init() {
		super.init();

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.player == null)
			return;

		playerButtons.clear();
		scrollOffset = 0;
		selectedPlayer = null;

		// 플레이어 버튼 생성 및 추가
		for (var player : client.world.getPlayers()) {
			if (!player.getUuid().equals(client.player.getUuid())) {
				String name = player.getName().getString();
				ButtonWidget button = ButtonWidget
						.builder(Text.literal(name), btn -> selectPlayer(name))
						.dimensions(20, 0, SPLIT_WIDTH - 40, BUTTON_HEIGHT).build();
				// 버튼을 위젯으로 추가하여 자동 클릭 처리되도록 함
				this.addDrawableChild(button);
				playerButtons.add(new PlayerButton(button, name));
			}
		}

		// 최대 스크롤 계산
		int listHeight = TOTAL_HEIGHT - LIST_TOP - LIST_BOTTOM_OFFSET;
		int totalContentHeight = playerButtons.size() * (BUTTON_HEIGHT + BUTTON_SPACING);
		maxScroll = Math.max(0, totalContentHeight - listHeight);

		// 메뉴 버튼들 생성 (오른쪽 영역)
		int menuX = SPLIT_WIDTH + 20;
		int menuY = TOTAL_HEIGHT / 2 - 40;
		
		teleportToButton = ButtonWidget.builder(Text.translatable("gui.tpwithgui.teleport_to_selected"), btn -> {
			if (selectedPlayer != null && client.getNetworkHandler() != null) {
				client.getNetworkHandler().sendChatCommand("tp " + selectedPlayer);
				this.close(); // TP 실행 후 창 닫기
			}
		}).dimensions(menuX, menuY, TOTAL_WIDTH - SPLIT_WIDTH - 40, 20).build();
		
		teleportToMeButton = ButtonWidget.builder(Text.translatable("gui.tpwithgui.teleport_selected_to_me"), btn -> {
			if (selectedPlayer != null && client.getNetworkHandler() != null && client.player != null) {
				client.getNetworkHandler()
						.sendChatCommand("tp " + selectedPlayer + " " + client.player.getName().getString());
				this.close(); // TP 실행 후 창 닫기
			}
		}).dimensions(menuX, menuY + 30, TOTAL_WIDTH - SPLIT_WIDTH - 40, 20).build();
		
		// 닫기 버튼
		ButtonWidget closeButton = ButtonWidget.builder(Text.translatable("gui.tpwithgui.close"), btn -> this.close())
				.dimensions(menuX, menuY + 70, TOTAL_WIDTH - SPLIT_WIDTH - 40, 20).build();

		// 버튼들 추가
		this.addDrawableChild(teleportToButton);
		this.addDrawableChild(teleportToMeButton);
		this.addDrawableChild(closeButton);
		
		// 초기 상태 설정 (플레이어 선택 안됨)
		updateMenuButtonsState();
	}
	
	private void selectPlayer(String playerName) {
		selectedPlayer = playerName;
		updateMenuButtonsState();
	}
	
	private void updateMenuButtonsState() {
		boolean hasSelection = selectedPlayer != null;
		teleportToButton.active = hasSelection;
		teleportToMeButton.active = hasSelection;
		
		// 버튼 텍스트 업데이트
		if (hasSelection) {
			teleportToButton.setMessage(Text.translatable("gui.tpwithgui.teleport_to", selectedPlayer));
			teleportToMeButton.setMessage(Text.translatable("gui.tpwithgui.teleport_to_me", selectedPlayer));
		} else {
			teleportToButton.setMessage(Text.translatable("gui.tpwithgui.teleport_to_selected"));
			teleportToMeButton.setMessage(Text.translatable("gui.tpwithgui.teleport_selected_to_me"));
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		
		int startX = (this.width - TOTAL_WIDTH) / 2;
		int startY = (this.height - TOTAL_HEIGHT) / 2;
		int listHeight = TOTAL_HEIGHT - LIST_TOP - LIST_BOTTOM_OFFSET;
		int y = startY + LIST_TOP - scrollOffset;

		for (PlayerButton playerButton : playerButtons) {
			ButtonWidget button = playerButton.button;
			button.setX(startX + 20);
			button.setY(y);

			boolean isVisible = y + BUTTON_HEIGHT > startY + LIST_TOP && y < startY + LIST_TOP + listHeight;
			button.visible = isVisible;
			button.active = !playerButton.name.equals(selectedPlayer) && isVisible;

			y += BUTTON_HEIGHT + BUTTON_SPACING;
		}

		int menuX = startX + SPLIT_WIDTH + 20;
		int menuY = startY + TOTAL_HEIGHT / 2 - 40;
		
		teleportToButton.setX(menuX);
		teleportToButton.setY(menuY);
		teleportToMeButton.setX(menuX);
		teleportToMeButton.setY(menuY + 30);
		
		for (var widget : this.children()) {
			if (widget instanceof ButtonWidget button && button.getMessage().equals(Text.translatable("gui.tpwithgui.close"))) {
				button.setX(menuX);
				button.setY(menuY + 70);
				break;
			}
		}

		context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("gui.tpwithgui.online_players"), startX + SPLIT_WIDTH / 2, startY + 15, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("gui.tpwithgui.teleport_menu"), startX + SPLIT_WIDTH + (TOTAL_WIDTH - SPLIT_WIDTH) / 2, startY + 15, 0xFFFFFF);

        int selectedTextY = startY + TOTAL_HEIGHT / 2 - 80;
		if (selectedPlayer != null) {
			context.drawTextWithShadow(this.textRenderer, Text.translatable("gui.tpwithgui.selected_player", selectedPlayer), menuX, selectedTextY, 0xFFFFFF);
		} else {
			context.drawTextWithShadow(this.textRenderer, Text.translatable("gui.tpwithgui.no_player_selected"), menuX, selectedTextY, 0x808080);
		}

		if (maxScroll > 0) {
			renderScrollbar(context, startX, startY + LIST_TOP, listHeight);
		}
	}

	private void renderScrollbar(DrawContext context, int startX, int startY, int listHeight) {
		int scrollbarX = startX + SPLIT_WIDTH - 15;
		int scrollbarWidth = 6;

		// 스크롤바 배경
		context.fill(scrollbarX, startY, scrollbarX + scrollbarWidth, startY + listHeight, 0xFF000000);

		// 스크롤바 핸들
		int totalContentHeight = playerButtons.size() * (BUTTON_HEIGHT + BUTTON_SPACING);
		float scrollRatio = (float) scrollOffset / maxScroll;
		int handleHeight = Math.max(20, (int) ((float) listHeight * listHeight / totalContentHeight));
		int handleY = startY + (int) (scrollRatio * (listHeight - handleHeight));

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

    private record PlayerButton(ButtonWidget button, String name) {
    }
}
