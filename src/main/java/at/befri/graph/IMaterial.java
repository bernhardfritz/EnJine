package at.befri.graph;

public interface IMaterial {
	public default boolean isMultilayered() {
		return false;
	}
}
