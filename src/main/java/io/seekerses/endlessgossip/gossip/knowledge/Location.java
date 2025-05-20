package io.seekerses.endlessgossip.gossip.knowledge;

import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.misc.Named;

import java.util.Set;

public interface Location extends Named {

	Set<Agent> getAgents();
}
