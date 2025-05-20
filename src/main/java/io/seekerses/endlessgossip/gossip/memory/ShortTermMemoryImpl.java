package io.seekerses.endlessgossip.gossip.memory;

import io.seekerses.endlessgossip.gossip.GossipConfig;
import io.seekerses.endlessgossip.gossip.GossipContext;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.agent.Squad;
import io.seekerses.endlessgossip.gossip.communication.sharedPerception.SharedPerceptionSystemImpl;
import io.seekerses.endlessgossip.gossip.knowledge.BlackboardListener;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.Location;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys.LOCATION;
import static io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys.SQUAD;
import static io.seekerses.endlessgossip.gossip.memory.DefaultMemoryKeys.TALKED_RECENTLY;

public class ShortTermMemoryImpl implements ShortTermMemory {

	private final Agent agent;
	private final KnowledgeStorage knowledgeStorage;
	private final Map<String, Object> blackboard;
	private final Map<String, List<BlackboardListener>> listeners;

	public ShortTermMemoryImpl(Agent agent) {
		this(
				agent,
				new KnowledgeStorageImpl(agent, GossipConfig.SHORT_TERM_KNOWLEDGE_SIZE),
				new HashMap<>(),
				new HashMap<>()
		);
	}

	public ShortTermMemoryImpl(Agent agent, KnowledgeStorage knowledgeStorage, Map<String, Object> blackboard, Map<String, List<BlackboardListener>> listeners) {
		this.agent = agent;
		this.knowledgeStorage = knowledgeStorage;
		this.blackboard = blackboard;
		this.listeners = listeners;
		write(LOCATION, GossipContext.getDefaultLocation());
		write(SQUAD, new Squad(agent));
		write(TALKED_RECENTLY, new ArrayList<>());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(@NotNull String key) {
		return (T) blackboard.get(key);
	}

	@Override
	public void write(@NotNull String key, @NotNull Object value) {
		Object old = blackboard.get(key);
		blackboard.put(key, value);
		List<BlackboardListener> listenersForProperty = listeners.get(key);
		if (listenersForProperty != null) {
			listenersForProperty.forEach(listener -> listener.processChange(old, value));
		}
	}

	@Override
	public Map<String, Object> getBlackboard() {
		return blackboard;
	}

	@Override
	public void registerListener(@NotNull BlackboardListener listener) {
		this.listeners.computeIfAbsent(listener.getProperty(), k -> new ArrayList<>()).add(listener);
	}

	@Override
	public boolean update(Knowledge knowledge) {
		Knowledge actualKnowledge = knowledge;
		if (SystemAction.REPORT_MESSAGE.equals(actualKnowledge.getAction())) {
			actualKnowledge = knowledge.getValue();
		}
		if (isInSquad() && !SystemAction.REPORT_MESSAGE.equals(knowledge.getAction())) {
			SharedPerceptionSystemImpl.reportEvent(agent, actualKnowledge);
		}
		if (agent.getMemory().getLongTermMemory()
					.getKnowledgeHolder().containsKnowledge(knowledge)
			|| knowledgeStorage.containsKnowledge(knowledge)) {
			return false;
		}
		if (!knowledgeStorage.addKnowledge(actualKnowledge)) {
			transmit();
			return knowledgeStorage.addKnowledge(actualKnowledge);
		}
		return true;
	}


	@Override
	public void disposeGroup() {
		Squad squad = read(SQUAD);
		Knowledge disposeKnowledge = SharedPerceptionSystemImpl.createGroupDisposalKnowledge(agent);
		squad.getAgents().forEach(member -> member.receiveKnowledge(disposeKnowledge));
	}

	@Override
	public Location getLocation() {
		return read("Location");
	}

	@Override
	public void setLocation(Location location) {
		write(LOCATION, location);
	}

	@Override
	public void addToSquad(Agent agent) {
		Squad currentSquad = read(SQUAD);
		Squad allySquad = agent.getMemory().getShortTermMemory().read(SQUAD);
		currentSquad.addAgents(allySquad.getAgents());
		allySquad.setSquad(currentSquad.getAgents());
	}

	@Override
	public boolean isInSquad() {
		return !((Squad) read(SQUAD)).isSolo();
	}

	@Override
	public void leaveSquad() {
		Squad squad = read(SQUAD);
		squad.removeAgent(agent);
		write(SQUAD, new Squad(agent));
	}

	@Override
	public void transmit() {
		Set<Knowledge> toRemove = knowledgeStorage.getKnowledge().stream()
				.filter(agent.getMemory().getLongTermMemory()::addKnowledge).collect(Collectors.toSet());
		toRemove.forEach(knowledgeStorage::removeKnowledge);
	}

	@Override
	public Set<Knowledge> getKnowledge(Object target) {
		return knowledgeStorage.getKnowledge(target);
	}

	@Override
	public Set<Knowledge> getKnowledge() {
		return knowledgeStorage.getKnowledge();
	}

	@Override
	public Knowledge getMostStrengthKnowledgeOfTag(Tag tag) {
		return knowledgeStorage.getMostStrengthKnowledgeOfTag(tag);
	}

	@Override
	public String getDump() {
		return knowledgeStorage.getDump();
	}

	@Override
	public void restoreFromDump(String encodedDump, Level level) {
		knowledgeStorage.restoreFromDump(encodedDump, level);
	}
}
