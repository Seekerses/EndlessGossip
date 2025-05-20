package io.seekerses.endlessgossip.gossip.knowledge;

public class CustomTag implements Tag {

	private String name;

	public CustomTag(String name) {
		this.name = name;
	}
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
