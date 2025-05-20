package io.seekerses.endlessgossip.gossip.knowledge.handler;

import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.agent.Squad;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys;
import net.minecraft.world.entity.Entity;

import java.util.Objects;

public class DisposeGroupHandler implements KnowledgeHandler {

	public static final DisposeGroupHandler INSTANCE = new DisposeGroupHandler();

	@Override
	public boolean processKnowledge(Agent agent, Knowledge knowledge) {
		Entity subject = knowledge.getSubject().getEntity();
		if (subject instanceof GossipDrivenMob gdm) {
			if (Objects.equals(
					gdm.getAgent(),
					agent.getMemory().getShortTermMemory().<Squad>read(DefaultMemoryKeys.SQUAD).getLeader())) {
				agent.leaveSquad();
			}
		}
		return false;
	}

	@Override
	public Action getSupportedAction() {
		return SystemAction.DISPOSE_GROUP;
	}
}
