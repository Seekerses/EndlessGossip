package io.seekerses.endlessgossip.gossip.knowledge;

public interface BlackboardListener {

	String getProperty();
	void processChange(Object old, Object updated);
}
