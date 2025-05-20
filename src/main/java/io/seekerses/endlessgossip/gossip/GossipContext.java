package io.seekerses.endlessgossip.gossip;

import com.mojang.logging.LogUtils;
import io.seekerses.endlessgossip.gossip.agent.Agent;
import io.seekerses.endlessgossip.gossip.communication.gossipSystem.GossipSystemImpl;
import io.seekerses.endlessgossip.gossip.communication.sharedPerception.SharedPerceptionSystemImpl;
import io.seekerses.endlessgossip.gossip.knowledge.Action;
import io.seekerses.endlessgossip.gossip.knowledge.CustomAction;
import io.seekerses.endlessgossip.gossip.knowledge.CustomTag;
import io.seekerses.endlessgossip.gossip.knowledge.Knowledge;
import io.seekerses.endlessgossip.gossip.knowledge.Location;
import io.seekerses.endlessgossip.gossip.knowledge.SimpleLocation;
import io.seekerses.endlessgossip.gossip.knowledge.SystemAction;
import io.seekerses.endlessgossip.gossip.knowledge.Tag;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class GossipContext {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final Map<String, Tag> tagRegistry;
	private final Map<String, Action> actionRegistry;
	private final List<Agent> agentRegistry;
	private final Map<Location, Agent> map;
	private final Map<String, Location> locationRegistry;
	private static final AtomicInteger RELATION_COUNTER = new AtomicInteger(1);
	private static final AtomicInteger KNOWLEDGES_COUNTER = new AtomicInteger(1);
	private static final AtomicInteger CONVERSATION_COUNTER = new AtomicInteger(1);
	private static final Path relationFile = Path.of("C:\\Users\\seeke\\OneDrive\\Desktop\\relations.txt");
	private static final Path knowledgeFile = Path.of("C:\\Users\\seeke\\OneDrive\\Desktop\\knowledges.txt");
	private static final Path conversationFile = Path.of("C:\\Users\\seeke\\OneDrive\\Desktop\\conversation.txt");
	private static final Queue<String> conversationLog = new LinkedList<>();

	private Location defaultLocation;

	private static volatile GossipContext instance;

	public GossipContext() {
		this.tagRegistry = new HashMap<>();
		this.actionRegistry = new HashMap<>();
		this.agentRegistry = new ArrayList<>();
		this.map = new HashMap<>();
		this.locationRegistry = new HashMap<>();
	}

	public static GossipContext getInstance() {
		GossipContext singleton = instance;
		if (singleton == null) {
			synchronized (GossipContext.class) {
				singleton = instance;
				if (singleton == null) {
					instance = singleton = new GossipContext();
				}
			}
		}
		return singleton;
	}

	public static void init() {
		SystemAction.registerSystemActions();
		Location systemDefaultLocation = new SimpleLocation("undefined");
		setDefaultLocation(systemDefaultLocation);
	}

	public static void registerTag(Tag tag) {
		getInstance().tagRegistry.put(tag.getName(), tag);
	}

	public static Tag getTag(String tagName) {
		Tag tag = getInstance().tagRegistry.get(tagName);
		if (tag == null) {
			tag = new CustomTag(tagName);
			registerTag(tag);
		}
		return tag;
	}

	public static void registerAction(Action action) {
		getInstance().actionRegistry.put(action.getName(), action);
	}

	public static Action getAction(String actionName) {
		Action action = getInstance().actionRegistry.get(actionName);
		if (action == null) {
			action = new CustomAction(actionName);
			registerAction(action);
		}
		return action;
	}

	public static void logConversation(Agent from, Agent to, Knowledge knowledge) {
		conversationLog.add(from.getName() + " " + to.getName() + " " + knowledge.toString().replace("\n", " "));
	}


	public static void registerAgent(Agent agent) {
		getInstance().agentRegistry.add(agent);
	}

	public static List<Agent> getAgents() {
		return Collections.unmodifiableList(getInstance().agentRegistry);
	}

	public static void removeAgent(Agent agent) {
		getInstance().agentRegistry.remove(agent);
	}

	public static Location getLocation(String name) {
		return getInstance().locationRegistry.get(name);
	}

	public static void registerLocation(Location location) {
		getInstance().locationRegistry.put(location.getName(), location);
	}

	public static void setDefaultLocation(Location defaultLocation) {
		getInstance().defaultLocation = defaultLocation;
	}

	public static Location getDefaultLocation() {
		return getInstance().defaultLocation;
	}

	public static void systemTick(Agent agent) {
		GossipSystemImpl.tick(agent);
		SharedPerceptionSystemImpl.tick(agent);
	}

	public static void writeConversation() {
		writeToConversation("INTERATION: " + CONVERSATION_COUNTER.getAndIncrement());
		while (!conversationLog.isEmpty()) {
			writeToConversation("\tCONVERSATION: " + conversationLog.poll());
		}
	}

	public static void dumpRelation() {
		writeToRelation("ITERATION: " + RELATION_COUNTER.getAndIncrement());
		getInstance().agentRegistry.forEach((agent -> {
			writeToRelation("\tAGENT: " + agent.getName());
			agent.getMemory().getLongTermMemory().getRelations().forEach((entity, rel) -> {
				writeToRelation("\t\tRELATION: " + entity.getEntity().getName().getString() + " " + rel);
			});
		}));
	}

	public static void dumpKnowledges() {
		writeToKnowledges("ITERATION: " + KNOWLEDGES_COUNTER.getAndIncrement());
		getInstance().agentRegistry.forEach((agent -> {
			writeToKnowledges("\tAGENT: " + agent.getName());
			Set<Knowledge> knowledges = agent.getMemory().getLongTermMemory().getKnowledge();
			knowledges.addAll(agent.getMemory().getShortTermMemory().getKnowledge());
			knowledges.forEach(knowledge -> {
				writeToKnowledges("\t\tKNOWLEDGE: " + knowledge.toString().replace("\n", " "));
			});
		}));

	}

	private static void writeToRelation(String string) {
		try {
			Files.writeString(relationFile, string + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException ex) {
			LOGGER.error("Failed to write relations.", ex);
		}
	}

	private static void writeToKnowledges(String string) {
		try {
			Files.writeString(knowledgeFile, string + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException ex) {
			LOGGER.error("Failed to write knowledges.", ex);
		}
	}

	private static void writeToConversation(String string) {
		try {
			Files.writeString(conversationFile, string + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException ex) {
			LOGGER.error("Failed to write conversation.", ex);
		}
	}
}
