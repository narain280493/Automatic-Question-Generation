// Question Generation via Overgenerating Transformations and Ranking
// Copyright (c) 2010 Carnegie Mellon University.  All Rights Reserved.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// For more information, bug reports, fixes, contact:
//    Michael Heilman
//	  Carnegie Mellon University
//	  mheilman@cmu.edu
//	  http://www.cs.cmu.edu/~mheilman

package edu.cmu.ark;

import edu.stanford.nlp.trees.tregex.*;
import java.util.*;

public class TregexPatternFactory {
	protected TregexPatternFactory(){
		map = new HashMap<String, TregexPattern>();
	}
	
	public static TregexPattern getPattern(String tregex){
		if(instance == null){
			instance = new TregexPatternFactory();
		}
		Map<String, TregexPattern> myMap = instance.getMap();
		TregexPattern pattern = myMap.get(tregex);
		if(pattern == null){
			try{
				pattern = TregexPattern.compile(tregex);
				myMap.put(tregex, pattern);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return pattern;
	}
	
	private Map<String, TregexPattern> getMap(){
		return map;
	}
	
	private static TregexPatternFactory instance;
	private Map<String, TregexPattern> map;
}
