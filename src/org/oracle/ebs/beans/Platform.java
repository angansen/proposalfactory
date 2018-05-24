package org.oracle.ebs.beans;

public class Platform {

	private String name;
	private int count;
	private String displayName;

	public Platform(String name, int count) {
		this.name = name;
		this.count = count;
	}

	public Platform(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}

	public Platform(String name, String displayName, int count) {
		this.name = name;
		this.count = count;
		this.displayName = displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public boolean equals(Object obj) {
		Platform paramPlatform = (Platform) obj;
		return this.name.equals(paramPlatform.getName());
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.name.hashCode();
	}

	@Override
	public String toString() {
		return "Platform [name=" + name + ", count=" + count + ", displayName=" + displayName + "]";
	}
	
	
}
