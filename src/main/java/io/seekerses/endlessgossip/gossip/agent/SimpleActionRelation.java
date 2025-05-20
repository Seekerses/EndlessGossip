package io.seekerses.endlessgossip.gossip.agent;

public class SimpleActionRelation implements ActionRelation {

	private final float relationToSubject;
	private final float relationToObject;

	public SimpleActionRelation(float toSubject, float toObject) {
		this.relationToSubject = toSubject;
		this.relationToObject = toObject;
	}

	@Override
	public float toSubject() {
		return this.relationToSubject;
	}

	@Override
	public float toObject() {
		return this.relationToObject;
	}
}
