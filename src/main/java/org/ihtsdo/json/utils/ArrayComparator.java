package org.ihtsdo.json.utils;

import java.util.Comparator;

public class ArrayComparator implements Comparator<String[]> {

	private int[] indexes;
	private boolean reverse;

	public ArrayComparator(int[] indexes, boolean reverse) {
		this.indexes = indexes;
		this.reverse = reverse;
	}

	public int compare(String[] o1, String[] o2) {
		for (int i = 0; i < indexes.length; i++) {
			if (o1[indexes[i]].compareTo(o2[indexes[i]]) < 0) {
				return reverse ? 1 : -1;
			} else if (o1[indexes[i]].compareTo(o2[indexes[i]]) > 0) {
				return reverse ? -1 : 1;
			}
		}
		return 0;
	}
}
