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

package edu.cmu.ark.ranking;

import java.text.NumberFormat;

public class Rankable implements Comparable<Object>{
	public String id;
	public double label;
	public double [] features;
	public double score;
	public Object pointer1; //e.g., to a question
	public Object pointer2;
	public Object pointer3; 
	
	public int compareTo(Object o) {
		return Double.compare(this.score, ((Rankable)o).score);
	}
	
	public String toString(){
		String res = "id="+id
			+"\tlabel="+NumberFormat.getInstance().format(label)
			+"\tscore="+NumberFormat.getInstance().format(score);
		if(features != null){
			for(int i=0;i<features.length;i++){
				res+="\t"+features[i];
			}
		}
		return res;
	}
}
