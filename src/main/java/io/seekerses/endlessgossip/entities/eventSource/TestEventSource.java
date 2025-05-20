package io.seekerses.endlessgossip.entities.eventSource;

import io.seekerses.endlessgossip.gossip.knowledge.EventSource;
import io.seekerses.endlessgossip.gossip.knowledge.EventSourceType;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.KnowledgeType;
import io.seekerses.endlessgossip.gossip.knowledge.SimpleAction;
import io.seekerses.endlessgossip.gossip.knowledge.SimpleTag;
import net.minecraft.world.level.Level;

import java.time.LocalDateTime;

public class TestEventSource extends EventSourceEntity {
	public TestEventSource(Level world) {
		super(world);
		setLifetime(1000);
		setKnowledge(Knowledge.builder()
				.source(new EventSource(this, EventSourceType.VISION))
				.action(SimpleAction.LOCATE)
				.knowledgeTag(SimpleTag.TEST)
				.object(this)
				.subject(this)
				.strength(1000)
				.type(KnowledgeType.DISPLACEABLE)
				.timestamp(LocalDateTime.now())
				.value(null)
				.build());
	}
}
