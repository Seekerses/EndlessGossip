package io.seekerses.endlessgossip.gossip.misc;

import io.seekerses.endlessgossip.gossip.agent.Agent;

public class RelationUtil {

	public static boolean relationCheck(Agent agent, Agent about) {
		return agent.getExtravert() + agent.getMemory().getLongTermMemory().getRelation(about.getRelatedMob()) > 0;
	}

	public static boolean informationTrustCheck(Agent receiver, Agent sender) {
		return receiver.getExtravert()
			   + receiver.getMemory().getLongTermMemory().getRelation(sender.getRelatedMob())
			   + RandomUtil.random.nextFloat(receiver.getRandomness() != null && receiver.getRandomness() > 0
											 ? receiver.getRandomness()
											 : 1)
			   > 0;
	}
}
