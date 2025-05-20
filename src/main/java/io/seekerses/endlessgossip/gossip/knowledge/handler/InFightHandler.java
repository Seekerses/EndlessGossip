package io.seekerses.endlessgossip.gossip.knowledge.handler;

import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.GossipDrivenMob;
import io.seekerses.endlessgossip.gossip.communication.sharedPerception.SharedPerceptionSystemImpl;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.misc.RelationUtil;
import net.minecraft.world.entity.Entity;


public class InFightHandler implements KnowledgeHandler {

	public static final InFightHandler INSTANCE = new InFightHandler();

	@Override
	public boolean processKnowledge(Agent agent, Knowledge knowledge) {
		Entity entity = knowledge.getSubject().getEntity();
		if (entity instanceof GossipDrivenMob mob) {
			if (RelationUtil.relationCheck(agent, mob.getAgent())) {
				SharedPerceptionSystemImpl.createGroupInvitation(agent.getRelatedMob(), mob.getAgent().getRelatedMob());
			}
		}
		return false;
	}

	@Override
	public Action getSupportedAction() {
		return SystemAction.IN_FIGHT;
	}
}
