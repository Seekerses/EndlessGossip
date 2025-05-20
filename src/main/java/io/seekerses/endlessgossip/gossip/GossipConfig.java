package io.seekerses.endlessgossip.gossip;

public class GossipConfig {
	public static final float OBLIVION_FACTOR = 2f;
	public static final float GOSSIP_FACTOR = 1.2f;
	public static final long UPDATE_STRENGTH_MILLIS = 10_000L;
	public static final long SHORT_TERM_KNOWLEDGE_SIZE = 10L;
	public static final long LONG_TERM_KNOWLEDGE_SIZE = 1000L;
	public static final float MINIMAL_KNOWLEDGE_STRENGTH = 1f;
	public static final float DEFAULT_RELATION = 0f;
	public static final long SENSORS_CLEAR_TIME = 10_000L;
	public static final long RANDOM_SEED = 12341234;
	public static final float DISCOVER_AGENT_STRENGTH = 100f;
	public static final float FRIEND_RELATION = 200f;
	public static final float ENEMY_RELATION = -100f;
	public static final int MAX_CONVERSATION_LENGTH = 1;
	public static final float DISTORTION_FACTOR = 1.25f;
	public static final float MINIMAL_RELATION_FOR_CONVERSATION = 100;
	public static final long TOP_KNOWLEDGE_FOR_CONVERSATION_SIZE = 1;
}
