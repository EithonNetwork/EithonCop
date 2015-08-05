package net.eithon.plugin.cop.spam;

import java.util.ArrayList;
import java.util.Iterator;

import net.eithon.library.core.PlayerCollection;
import net.eithon.plugin.cop.Config;

import org.bukkit.entity.Player;

class RepeatedLines {
	PlayerCollection<ArrayList<OldLine>> _oldLines;
	
	RepeatedLines() {
		this._oldLines =  new PlayerCollection<ArrayList<OldLine>>();
	}
	
	int numberOfDuplicates(Player player, String line) {
		line = line.toLowerCase();
		ArrayList<OldLine> oldLines = this._oldLines.get(player);
		if (oldLines == null) {
			oldLines = new ArrayList<OldLine>();
			oldLines.add(new OldLine(line));
			this._oldLines.put(player, oldLines);
			return 1;
		}
		
		Iterator<OldLine> iterator = oldLines.iterator();
		int maxNumberOfDuplicates = 0;
		while (iterator.hasNext()) {
		    OldLine oldLine = iterator.next();
		    if (oldLine.isTooOld()) {
		        iterator.remove();
		    } else {
		    	if (isDuplicateLine(line, oldLine.getLine())) {
		    		oldLine.addDuplicate();
		    		int numberOfDuplicates = oldLine.numberOfDuplicates();
		    		if (numberOfDuplicates > maxNumberOfDuplicates) maxNumberOfDuplicates = numberOfDuplicates;
		    	}
		    }
		}
		oldLines.add(new OldLine(line));
		return maxNumberOfDuplicates;
	}
	
	private boolean isDuplicateLine(String line, String oldLine) {
		if (oldLine.length() == 0) return false;
		int distance = editDistance(line, oldLine);
		double similarity = 1.0 - ((double) distance) / oldLine.length();
		return similarity > Config.V.lineIsProbablyDuplicate;
	}
	
	// https://gist.github.com/wickedshimmy/449595/cb33c2d0369551d1aa5b6ff5e6a802e21ba4ad5c
	private static int editDistance (String original, String modified) {
		int len_orig = original.length();
		int len_diff = modified.length();

		int[][] matrix = new int[len_orig + 1][len_diff + 1];
		for (int i = 0; i <= len_orig; i++)
			matrix[i][0] = i;
		for (int j = 0; j <= len_diff; j++)
			matrix[0][j] = j;
		
		for (int i = 1; i <= len_orig; i++) {
			for (int j = 1; j <= len_diff; j++) {
				int cost = modified.charAt(j - 1) == original.charAt(i - 1) ? 0 : 1;
				int[] vals = new int[] {
					matrix[i - 1][j] + 1,
					matrix[i][j - 1] + 1,
					matrix[i - 1][j - 1] + cost
				};
				matrix[i][j] = minimalValue(vals);
				if (i > 1 && j > 1 && original.charAt(i - 1) == modified.charAt(j - 2) && original.charAt(i - 2) == modified.charAt(j - 1))
					matrix[i][j] = Math.min(matrix[i][j], matrix[i - 2][j - 2] + cost);
			}
		}
		return matrix[len_orig][len_diff];
	}

	private static int minimalValue(int[] vals) {
		int min = Integer.MAX_VALUE;
		for (int value : vals) {
			if (value < min) min = value;
		}
		return min;
	}

}
