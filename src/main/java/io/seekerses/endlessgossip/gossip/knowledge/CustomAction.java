package io.seekerses.endlessgossip.gossip.knowledge;

public class CustomAction implements Action {

	private String name;

	public CustomAction(String name) {
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
