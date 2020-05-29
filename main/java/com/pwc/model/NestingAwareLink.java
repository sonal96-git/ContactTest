package com.pwc.model;
import com.pwc.model.Link;;

public class NestingAwareLink extends Link {

	private boolean nestedEntry;
	private boolean nestedExit;
	private int nestedExitDepthDelta;
	private boolean selected;
	
	public NestingAwareLink() {
		super();
	}

	public NestingAwareLink(Link link, boolean nestedEntry, boolean nestedExit, int nestedExitDepthDelta, boolean selected) {
		super(link.getText(), link.getUrl(), link.isOpenInNewWindow(), link.getLinkAddlCSS(), link.getLevel());
		this.nestedEntry = nestedEntry;
		this.nestedExit = nestedExit;
		this.selected = selected;
	}
	
	/**
	 * Signifies that this {@code Link} contains nested links underneath it.
	 * <p>
	 * Essentially this means that the JSTL will have to add a opening {@code <ul>} tag to this rendered link.
	 * 
	 * @return
	 */
	public boolean isNestedEntry() {
		return nestedEntry;
	}

	public void setNestedEntry(boolean nestedEntry) {
		this.nestedEntry = nestedEntry;
	}

	/**
	 * Signifies that this {@code Link} is the last link in a nested list of links.
	 * <p>
	 * Essentially this means that the JSTL will have to add {@link #getNestedExitDepthDelta()} number of closing {@code <ul>} tags.
	 * 
	 * @return
	 */
	public boolean isNestedExit() {
		return nestedExit;
	}

	public void setNestedExit(boolean nestedExit) {
		this.nestedExit = nestedExit;
	}

	/**
	 * The number of closing {@code <ul>} tags that will have to be added directly after this link item.
	 * 
	 * @return This links level depth compared to the next link following it in the list of links
	 */
	public int getNestedExitDepthDelta() {
		return nestedExitDepthDelta;
	}

	public void setNestedExitDepthDelta(int nestedExitDepthDelta) {
		this.nestedExitDepthDelta = nestedExitDepthDelta;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}