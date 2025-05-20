package io.seekerses.endlessgossip.gossip.misc;

import io.seekerses.endlessgossip.gossip.GossipConfig;

import java.util.Random;

public class RandomUtil {

	public static final Random random = new Random(GossipConfig.RANDOM_SEED);
}
