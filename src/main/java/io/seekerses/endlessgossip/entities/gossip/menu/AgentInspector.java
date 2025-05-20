package io.seekerses.endlessgossip.entities.gossip.menu;

import com.mojang.datafixers.util.Pair;
import io.seekerses.endlessgossip.gossip.GossipContext;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.SimpleActionRelation;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class AgentInspector extends Screen {

	private final Agent agent;
	private EditBox nameInput;
	private EditBox extravertInput;
	private EditBox attentivenessInput;
	private EditBox randomnessInput;
	private EditBox tagsInput;
	private EditBox interestsInput;
	private EditBox actionRelationInput;
	private KnowledgeTable knowledgeTable;

	public AgentInspector(Agent agent) {
		super(Component.literal("Agent Inspector"));
		this.agent = agent;

	}

	@Override
	protected void init() {
		this.nameInput = new EditBox(this.font, this.width / 2 - 100, 20, 200, 20, Component.literal("Name"));
		nameInput.setBordered(true);
		nameInput.setValue(agent.getName());
		this.addRenderableWidget(this.nameInput);

		this.extravertInput = new EditBox(this.font, this.width / 2 - 100, 40, 200, 20, Component.literal("Extravert"));
		extravertInput.setBordered(true);
		extravertInput.setValue(agent.getExtravert().toString());
		this.addRenderableWidget(this.extravertInput);

		this.attentivenessInput = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, Component.literal("Attentiveness"));
		attentivenessInput.setBordered(true);
		attentivenessInput.setValue(agent.getAttentiveness().toString());
		this.addRenderableWidget(this.attentivenessInput);

		this.randomnessInput = new EditBox(this.font, this.width / 2 - 100, 80, 200, 20, Component.literal("Randomness"));
		randomnessInput.setBordered(true);

		randomnessInput.setValue(agent.getRandomness().toString());
		this.addRenderableWidget(this.randomnessInput);

		this.tagsInput = new EditBox(this.font, this.width / 2 - 100, 100, 200, 20, Component.literal("Tags"));
		tagsInput.setBordered(true);
		tagsInput.setValue(agent.getPersonTags()
				.entrySet().stream()
				.map(entry -> entry.getKey().getName() + "=" + entry.getValue().toString())
				.collect(Collectors.joining(";")));
		this.addRenderableWidget(this.tagsInput);

		this.interestsInput = new EditBox(this.font, this.width / 2 - 100, 120, 200, 20, Component.literal("Interests"));
		interestsInput.setBordered(true);
		interestsInput.setValue(agent.getInterests()
				.entrySet().stream()
				.map(entry -> entry.getKey().getName() + "=" + entry.getValue().toString())
				.collect(Collectors.joining(";")));
		this.addRenderableWidget(this.interestsInput);

		this.actionRelationInput = new EditBox(this.font, this.width / 2 - 100, 140, 200, 20, Component.literal("Action relations"));
		actionRelationInput.setBordered(true);
		actionRelationInput.setValue(agent.getActionRelations()
				.entrySet().stream()
				.map(entry -> entry.getKey().getName() + "=" + entry.getValue().toSubject() + ":" + entry.getValue().toObject())
				.collect(Collectors.joining(";")));
		this.addRenderableWidget(this.actionRelationInput);
		// this.width / 2 - 50, 110, 100, 20
		this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> {

			agent.setName(this.nameInput.getValue());
			agent.setExtravert(Float.valueOf(this.extravertInput.getValue()));
			agent.setRandomness(Float.valueOf(this.randomnessInput.getValue()));
			agent.setAttentiveness(Float.valueOf(this.attentivenessInput.getValue()));
			if (StringUtil.isNullOrEmpty(tagsInput.getValue())) {
				agent.setPersonTags(Collections.emptyMap());
			} else {
				agent.setPersonTags(Arrays.stream(this.tagsInput.getValue().split(";")).map(tagAndObscurity -> {
					String[] tagObs = tagAndObscurity.split("=");
					return new Pair<>(
							GossipContext.getTag(tagObs[0]),
							Float.valueOf(tagObs[1])
					);
				}).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
			}
			if (StringUtil.isNullOrEmpty(interestsInput.getValue())) {
				agent.setInterests(Collections.emptyMap());
			} else {
				agent.setInterests(Arrays.stream(this.interestsInput.getValue().split(";")).map(tagAndObscurity -> {
					String[] tagObs = tagAndObscurity.split("=");
					return new Pair<>(
							GossipContext.getTag(tagObs[0]),
							Float.valueOf(tagObs[1])
					);
				}).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
			}
			if (StringUtil.isNullOrEmpty(actionRelationInput.getValue())) {
				agent.setActionRelations(Collections.emptyMap());
			} else {
				agent.setActionRelations(Arrays.stream(this.actionRelationInput.getValue().split(";")).map(tagAndObscurity -> {
					String[] tagObs = tagAndObscurity.split("=");
					String[] relations = tagObs[1].split(":");
					return new Pair<>(
							GossipContext.getAction(tagObs[0]),
							new SimpleActionRelation(Float.parseFloat(relations[0]), Float.parseFloat(relations[1]))
					);
				}).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
			}
			this.onClose();
		}).build());

		this.knowledgeTable = new KnowledgeTable(this.minecraft, this.width, this.height - 160, 160, this.height - 20);
		this.knowledgeTable.setKnowledge(agent.getMemory().getKnowledge());
		this.addWidget(this.knowledgeTable);
	}

	@Override
	public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawString(Minecraft.getInstance().font, "Name:",this.width / 2 - 200, 25, 0xFFFFFF);
		guiGraphics.drawString(Minecraft.getInstance().font, "Extravert:",this.width / 2 - 200, 45,0xFFFFFF );
		guiGraphics.drawString(Minecraft.getInstance().font, "Attentiveness:",this.width / 2 - 200, 65, 0xFFFFFF);
		guiGraphics.drawString(Minecraft.getInstance().font, "Randomness:",this.width / 2 - 200, 85, 0xFFFFFF);
		guiGraphics.drawString(Minecraft.getInstance().font, "Tags:",this.width / 2 - 200, 105, 0xFFFFFF);
		guiGraphics.drawString(Minecraft.getInstance().font, "Interests:",this.width / 2 - 200, 125, 0xFFFFFF);
		guiGraphics.drawString(Minecraft.getInstance().font, "Action relations:",this.width / 2 - 200, 145, 0xFFFFFF);
		guiGraphics.drawString(Minecraft.getInstance().font, "Knowledges:",this.width / 2 - 200, 165, 0xFFFFFF);
		if (knowledgeTable == null) {
			this.knowledgeTable = new KnowledgeTable(this.minecraft, this.width, this.height - 140, 160, this.height - 20);
		}
		this.knowledgeTable.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static class KnowledgeTable extends AbstractSelectionList<KnowledgeTable.Entry> {
		public KnowledgeTable(Minecraft minecraft, int width, int height, int top, int bottom) {
			super(minecraft, width, height, top, bottom, 130);
		}

		public void setKnowledge(Set<Knowledge> knowledge) {
			this.clearEntries();
			for (Knowledge entry : knowledge) {
				this.addEntry(new Entry(entry));
			}
		}

		@Override
		public void updateNarration(NarrationElementOutput elementOutput) {
			elementOutput.add(NarratedElementType.TITLE);
		}

		private static class Entry extends AbstractSelectionList.Entry<Entry> {
			private final Knowledge knowledge;

			public Entry(Knowledge knowledge) {
				this.knowledge = knowledge;
			}

			@Override
			public void render(@Nonnull GuiGraphics gui, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
				String knowledgeRepresentation = knowledge.toString();
				int i = 0;
				for (String text : knowledgeRepresentation.split("\n")) {
					gui.drawString(Minecraft.getInstance().font, text, left + 5, top + 5 + 10 * i, 0xFFFFFF);
					i++;
				}
				gui.drawString(Minecraft.getInstance().font, "", left + 5, top + 5 + 10 * i, 0xFFFFFF);
				i++;
				gui.drawString(Minecraft.getInstance().font, "-----------------", left + 5, top + 5 + 10 * i, 0xFFFFFF);
				i++;
				gui.drawString(Minecraft.getInstance().font, "", left + 5, top + 5 + 10 * i, 0xFFFFFF);
			}
		}
	}
}
