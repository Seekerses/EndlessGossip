package io.seekerses.endlessgossip.gossip.knowledge.handler;

import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.communication.sharedPerception.SharedPerceptionSystemImpl;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.misc.RelationUtil;
import net.minecraft.world.entity.Entity;

import java.util.Objects;

public class InviteGroupHandler implements KnowledgeHandler {

	public static final InviteGroupHandler INSTANCE = new InviteGroupHandler();

	@Override
	public boolean processKnowledge(Agent agent, Knowledge knowledge) {
		Entity subject = knowledge.getSubject().getEntity();
		Entity object = knowledge.getObject().getEntity();
		if (subject instanceof GossipDrivenMob mob && object instanceof GossipDrivenMob invited) {
			if (Objects.equals(invited.getAgent(), agent) && RelationUtil.relationCheck(agent, mob.getAgent())) {
				mob.getAgent().addToSquad(agent);
			}
		}
		return false;
	}

	@Override
	public Action getSupportedAction() {
		return SystemAction.INVITE_GROUP;
	}
}
