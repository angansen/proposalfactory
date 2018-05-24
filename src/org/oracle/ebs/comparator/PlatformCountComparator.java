package org.oracle.ebs.comparator;

import java.util.Comparator;

import org.oracle.ebs.beans.Platform;

public class PlatformCountComparator implements Comparator<Platform> {

	@Override
	public int compare(Platform o1, Platform o2) {

		int flag = 0;

		if (o1.getCount() > o2.getCount()) {
			return -1;
		} else if (o1.getCount() < o2.getCount()) {
			return 1;
		}
		return 0;
	}

}
