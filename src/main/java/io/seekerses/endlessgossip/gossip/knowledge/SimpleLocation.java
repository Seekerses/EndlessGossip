package io.seekerses.endlessgossip.gossip.knowledge;

import io.seekerses.endlessgossip.gossip.agent.Agent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SimpleLocation implements Location {

	private String name;
	private final Set<Agent> agents;

	public SimpleLocation(String name) {
		this.name = name;
		this.agents = new HashSet<>();
	}

	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public void addAgent(Agent agent) {
		this.agents.add(agent);
	}

	@Override
	public Set<Agent> getAgents() {
		return agents;
	}

	public void removeAgent(Agent agent) {
		this.agents.remove(agent);
	}

	public void clear() {
		this.agents.clear();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SimpleLocation that = (SimpleLocation) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}
}
