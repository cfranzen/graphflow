package models.pointBased;

import java.awt.geom.Path2D;
import java.util.HashSet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * For painting
 * 
 * @author n.frantzen <nils.frantzen@rwth-aachen.de>
 * 
 */
public class SeaEdge extends Edge {

	private Path2D path;
	public HashSet<Integer> edgeIds = new HashSet<>();

	/**
	 * 
	 */
	public SeaEdge(Path2D path) {
		this.path = path;
	}

	/**
	 * @param start
	 * @param dest
	 */
	public SeaEdge(GeoPosition start, GeoPosition dest) {
		super(start, dest);
	}

	/**
	 * @param edge
	 */
	public SeaEdge(Edge edge) {
		super(edge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return the path
	 */
	public Path2D getPath() {
		return path;
	}

	/**
	 * @param path
	 */
	public void setPath(Path2D path) {
		this.path = path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((dest == null) ? 0 : dest.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SeaEdge other = (SeaEdge) obj;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start)) {
			if (!start.equals(other.dest)) {
				return false;
			}
		}
		if (dest == null) {
			if (other.dest != null)
				return false;
		} else if (!dest.equals(other.dest)) {
			if (!dest.equals(other.start)) {
				return false;
			}
		}
		return true;
	}

}
