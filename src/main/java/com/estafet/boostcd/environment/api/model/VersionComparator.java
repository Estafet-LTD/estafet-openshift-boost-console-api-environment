package com.estafet.boostcd.environment.api.model;

import java.util.Comparator;

public class VersionComparator implements Comparator<String> {

	@Override
	public int compare(String version1, String version2) {
		String[] v1 = version1.replaceAll("\\-SNAPSHOT", "").split("\\.");
		String[] v2 = version2.replaceAll("\\-SNAPSHOT", "").split("\\.");
		if (version1.equals("master")) {
			return 1;
		} else if (version2.equals("master")) {
			return -1;
		} else if (version1.equals(version2)) {
			return 0;
		} else if (major(v1) == major(v2)) {
			if (minor(v1) ==  minor(v2)) {
				return revision(v1).compareTo(revision(v2));
			} else {
				return minor(v1).compareTo(minor(v2));
			}
					
		} else {
			return major(v1).compareTo(major(v2));
		}
	}

	private Integer major(String[] version) {
		return Integer.parseInt(version[0]);
	}

	private Integer minor(String[] version) {
		return version.length > 1 ? Integer.parseInt(version[1]) : 0;
	}
		
	private Integer revision(String[] version) {
		return version.length > 2 ? Integer.parseInt(version[2]) : 0;
	}
	
}
