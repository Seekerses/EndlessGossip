package io.seekerses.endlessgossip.gossip.agent;

import io.seekerses.endlessgossip.gossip.GossipContext;
import io.seekerses.endlessgossip.gossip.communication.sharedPerception.SharedPerceptionSystemImpl;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.Location;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;
import io.seekerses.endlessgossip.gossip.knowledge.handler.KnowledgeHandler;
import io.seekerses.endlessgossip.gossip.memory.AgentMemory;
import io.seekerses.endlessgossip.gossip.memory.ComplexAgentMemory;
import io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys;
import io.seekerses.endlessgossip.gossip.misc.KnowledgeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.seekerses.endlessgossip.gossip.misc.KnowledgeUtil.createEmptySystemKnowledge;
import static io.seekerses.endlessgossip.gossip.misc.KnowledgeUtil.createEventAtAgentLocation;

public class SimpleAgent implements Agent {

	private String name;
	private final Mob relatedMob;
	private final AgentMemory memory;
	private final Map<Tag, Float> interests = new HashMap<>(Map.of(GossipContext.getTag("VILLAGER"), 100f));
	private final Map<Action, ActionRelation> actionRelations = new HashMap<>(Map.of(GossipContext.getAction("LOCATE"), new SimpleActionRelation(10, 0)));
	private final Map<Tag, Float> tags = new HashMap<>(Map.of(GossipContext.getTag("VILLAGER"), 30f));
	private float attentiveness = 50;
	private float extravert = 50;
	private float randomness = 10;
	private boolean inFight;
	private boolean inConversation;
	private int tickBeforeNonFight;
	private final Map<Action, KnowledgeHandler> knowledgeHandlers;

	public SimpleAgent(String name, Mob mob) {
		this.name = name;
		this.relatedMob = mob;
		this.memory = new ComplexAgentMemory(this);
//		this.interests = new HashMap<>();
//		this.actionRelations = new HashMap<>();
//		this.tags = new HashMap<>();
		this.knowledgeHandlers = new HashMap<>();
		KnowledgeUtil.registerSystemHandlers(this);
	}

	@Override
	public AgentMemory getMemory() {
		return memory;
	}

	@Override
	public Mob getRelatedMob() {
		return relatedMob;
	}

	@Override
	public Map<Tag, Float> getPersonTags() {
		return tags;
	}

	@Override
	public void setPersonTags(Map<Tag, Float> personTags) {
		this.tags.clear();
		this.tags.putAll(personTags);
	}

	@Override
	public Float getInterest(Tag tag) {
		return interests.get(tag);
	}

	@Override
	public Map<Tag, Float> getInterests() {
		return interests;
	}

	@Override
	public void setInterests(Map<Tag, Float> interests) {
		this.interests.clear();
		this.interests.putAll(interests);
	}

	@Override
	public ActionRelation getActionRelation(Action action) {
		return actionRelations.get(action);
	}

	@Override
	public void setActionRelations(Map<Action, ActionRelation> actionRelations) {
		this.actionRelations.clear();
		this.actionRelations.putAll(actionRelations);
	}

	@Override
	public Map<Action, ActionRelation> getActionRelations() {
		return actionRelations;
	}

	@Override
	public Float getAttentiveness() {
		return attentiveness;
	}

	@Override
	public void setAttentiveness(Float attentiveness) {
		this.attentiveness = attentiveness;
	}

	@Override
	public Float getExtravert() {
		return extravert;
	}

	@Override
	public void setExtravert(Float extravert) {
		this.extravert = extravert;
	}

	@Override
	public Float getRandomness() {
		return randomness;
	}

	@Override
	public void setRandomness(Float randomness) {
		this.randomness = randomness;
	}

	@Override
	public boolean sharedPerceptionActive() {
		return isInSquad();
	}

	@Override
	public boolean gossipSystemActive() {
		return !isInSquad();
	}

	@Override
	public void addToSquad(Agent agent) {
		memory.getShortTermMemory().addToSquad(agent);
	}

	@Override
	public boolean isInSquad() {
		return memory.getShortTermMemory().isInSquad();
	}

	@Override
	public void leaveSquad() {
		memory.getShortTermMemory().leaveSquad();
	}

	@Override
	public void say(Agent agent) {

	}

	@Override
	public void talk(Agent agent) {

	}

	@Override
	public void shout(Location location) {

	}

	@Override
	public void receiveKnowledge(Knowledge knowledge) {
		if (knowledge != null && !preHandleKnowledge(knowledge)) {
			postHandleKnowledge(knowledge, memory.getShortTermMemory().update(knowledge));
		}
	}


	@Override
	public void receiveEvent(Knowledge knowledge, Agent agent) {
		if (extravert + memory.getLongTermMemory().getRelation(agent.getRelatedMob()) > 0) {
			receiveKnowledge(knowledge);
		}
	}

	@Override
	public boolean preHandleKnowledge(Knowledge knowledge) {
		 return !SharedPerceptionSystemImpl.processHandlers(this, knowledge);
	}

	@Override
	public void postHandleKnowledge(Knowledge knowledge, boolean isAdded) {
		if (isAdded) {
			Player player = relatedMob.level().getNearestPlayer(relatedMob, 1000);
			if (player != null) {
				player.sendSystemMessage(Component.literal("I obtained knowledge: " + knowledge.toString()));
			}
		}
	}

	@Override
	public void signalKnowledgeSensor(Knowledge knowledge) {

	}

	@Override
	public void signalSay(Knowledge knowledge, Agent agent) {

	}

	@Override
	public void signalShout(Knowledge knowledge) {

	}

	@Override
	public void signalHear(Knowledge knowledge, Agent agent) {

	}

	@Override
	public void setInFight(boolean inFight) {
		if (inFight) {
			this.inFight = true;
			tickBeforeNonFight = 50;
		} else {
			this.inFight = false;
			tickBeforeNonFight = 0;
		}

	}

	@Override
	public boolean inFight() {
		return inFight;
	}

	@Override
	public void setInConversation(boolean inConversation) {
		this.inConversation = inConversation;
	}

	@Override
	public boolean inConversation() {
		return inConversation;
	}

	@Override
	public List<Knowledge> getMostStrengthKnowledge(Set<Knowledge> otherKnowledges, long size) {
		return memory.getLongTermMemory().getMostStrengthKnowledgeExcept(otherKnowledges, size);
	}

	@Override
	public void delegateTick() {
		GossipContext.systemTick(this);
		if (inFight) {
			Knowledge knowledge = createEmptySystemKnowledge(this);
			knowledge.setAction(SystemAction.IN_FIGHT);
			createEventAtAgentLocation(this, knowledge, 3);
			tickBeforeNonFight--;
			if (tickBeforeNonFight <= 0) {
				setInFight(false);
			}
		}
	}


	@Override
	public void registerKnowledgeHandler(KnowledgeHandler handler) {
		this.knowledgeHandlers.put(handler.getSupportedAction(), handler);
	}

	@Override
	public KnowledgeHandler getKnowledgeHandler(Action action) {
		return this.knowledgeHandlers.get(action);
	}

	@Override
	public void proposeLeaderElection() {
		Squad squad = (Squad) memory.getShortTermMemory().getBlackboard().get(DefaultMemoryKeys.SQUAD);
		if (squad != null && squad.getAgents().size() > 1) {
			squad.getAgents().forEach(agent -> {
				agent.receiveKnowledge(SharedPerceptionSystemImpl.nominateLeader(this));
			});
		}
	}

	@Override
	public void proposePlan(RunnableSerializable plan) {
		Squad squad = getMemory().getShortTermMemory().read(DefaultMemoryKeys.SQUAD);
		squad.getAgents().forEach(agent -> agent.receiveKnowledge(SharedPerceptionSystemImpl.proposePlan(this, plan)));
	}

	public static class RunnableSerializable implements Serializable {
		public Runnable runnable;

		public RunnableSerializable(Runnable runnable) {
			this.runnable = runnable;
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		relatedMob.setCustomNameVisible(true);
		relatedMob.setCustomName(Component.literal(name));
		this.name = name;
	}
}
