/**
 * 
 */
package models.nodeBased;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeaNode {

	private static final Pattern PATTERN = Pattern
			.compile("\\[id=(\\w+),pos=\\[([\\w+,.,-]+), ([\\w+,.,-]+)\\],edges=\\[(\\w+(, \\w+)*)\\]\\]");
	private Logger logger = LoggerFactory.getLogger(getClass());
	public long id;
	public GeoPosition pos;
	public List<Long> edges = new ArrayList<>();

	/**
	 * @param iD
	 * @param pos
	 */
	public SeaNode(long id, GeoPosition pos, List<Long> edges) {
		super();
		this.id = id;
		this.pos = pos;
		this.edges = edges;
	}

	public SeaNode(String line) {
		final Matcher matcher = PATTERN.matcher(line);

		logger.debug("Parsing line: " + line);

		if (matcher.find()) {
			this.id = Long.parseLong(matcher.group(1));
			this.pos = getValidGeoPos(Double.parseDouble(matcher.group(2)), Double.parseDouble(matcher.group(3)));
			this.edges = Arrays.stream(matcher.group(4).split(",")).map(String::trim).map((x) -> Long.parseLong(x))
					.collect(Collectors.toList());
		} else {
			logger.warn("No match found for line: " + line);
		}
	}

	private GeoPosition getValidGeoPos(double lat, double lon) {
		double vLon = lon;
		if (lon > 180) {
			vLon = -180 + (lon - 180);
		} else if (lon < -180) {
			vLon = 180 - (lon + 180);
		}
		return new GeoPosition(lat, vLon);

	}

	public void addEdge(long nodeId) {
		edges.add(nodeId);
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

}