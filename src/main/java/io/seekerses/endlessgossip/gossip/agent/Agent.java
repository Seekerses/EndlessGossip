package io.seekerses.endlessgossip.gossip.agent;

import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.Location;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;
import io.seekerses.endlessgossip.gossip.knowledge.handler.KnowledgeHandler;
import io.seekerses.endlessgossip.gossip.memory.AgentMemory;
import io.seekerses.endlessgossip.gossip.misc.Named;
import net.minecraft.world.entity.Mob;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Agent extends Named {
	AgentMemory getMemory();
	Mob getRelatedMob();
	Map<Tag, Float> getPersonTags();
	void setPersonTags(Map<Tag, Float> personTags);
	Float getInterest(Tag tag);
	Map<Tag, Float> getInterests();
	void setInterests(Map<Tag, Float> interests);
	ActionRelation getActionRelation(Action action);
	void setActionRelations(Map<Action, ActionRelation> actionRelations);
	Map<Action, ActionRelation> getActionRelations();
	Float getAttentiveness();
	void setAttentiveness(Float attentiveness);
	Float getExtravert();
	void setExtravert(Float extravert);
	Float getRandomness();
	void setRandomness(Float randomness);

	boolean sharedPerceptionActive();
	boolean gossipSystemActive();
	void addToSquad(Agent agent);
	boolean isInSquad();
	void leaveSquad();

	void say(Agent agent);
	void talk(Agent agent);
	void shout(Location location);
	void receiveKnowledge(Knowledge knowledge);
	void receiveEvent(Knowledge knowledge, Agent agent);
	boolean preHandleKnowledge(Knowledge knowledge);
	void postHandleKnowledge(Knowledge knowledge, boolean isAdded);

	void signalKnowledgeSensor(Knowledge knowledge);
	void signalSay(Knowledge knowledge, Agent agent);
	void signalShout(Knowledge knowledge);
	void signalHear(Knowledge knowledge, Agent agent);

	void setInFight(boolean inFight);
	boolean inFight();
	void setInConversation(boolean inConversation);
	boolean inConversation();
	List<Knowledge> getMostStrengthKnowledge(Set<Knowledge> otherKnowledges, long size);

	void delegateTick();

	void registerKnowledgeHandler(KnowledgeHandler handler);
	KnowledgeHandler getKnowledgeHandler(Action action);

	void proposeLeaderElection();

	void proposePlan(SimpleAgent.RunnableSerializable plan);
}
