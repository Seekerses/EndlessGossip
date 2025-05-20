package io.seekerses.endlessgossip.gossip.agent;

import com.mojang.datafixers.util.Pair;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Squad {

	private Set<Agent> squad;
	private Agent leader;

	public Squad() {
		this.squad = new HashSet<>();
	}

	public Squad(Set<Agent> squad) {
		this.squad = squad;
	}

	public Squad(Agent agent) {
		this.squad = new HashSet<>();
		this.squad.add(agent);
		this.leader = agent;
	}

	public void addAgent(Agent agent) {
		this.squad.add(agent);
	}

	public void addAgents(Set<Agent> agents) {
		this.squad.addAll(agents);
	}

	public void removeAgent(Agent agent) {
		this.squad.remove(agent);
	}

	public Set<Agent> getAgents() {
		return this.squad;
	}

	public void setSquad(Set<Agent> squad) {
		this.squad = squad;
	}

	public boolean isSolo() {
		return squad.size() == 1;
	}

	public Agent getLeader() {
		return leader;
	}

	public void setLeader(Agent agent) {
		this.leader = agent;
	}

	public void chooseLeader() {
		Optional<Pair<Agent, Float>> optAgent = getAgents().stream()
				.map(agent -> Pair.of(agent, getAgents().stream().filter(ag -> ag != agent)
						.map(ag -> ag.getMemory().getLongTermMemory().getRelation(agent.getRelatedMob()))
						.reduce(0f, Float::sum)))
				.max(Comparator.comparingDouble(Pair::getSecond));
		optAgent.ifPresent(agentFloatPair -> this.leader = agentFloatPair.getFirst());
	}
}
