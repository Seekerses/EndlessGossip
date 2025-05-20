package io.seekerses.endlessgossip.gossip.communication.gossipSystem;

import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;

import java.io.Serializable;

public record ConversationMessage(Knowledge knowledge, int iteration) implements Serializable {}
