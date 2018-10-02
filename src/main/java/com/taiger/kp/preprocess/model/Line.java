package com.taiger.kp.preprocess.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Line implements Comparable<Line>{
	int constant;
	int begin;
	int end;
	
	public boolean tooNear (Line line) {
		if (Math.abs(line.getConstant() - this.constant) <= 7) return true;
		return false;
	}

	@Override
	public int compareTo(Line o) {
		return this.constant - o.constant;
	}
	
	public boolean atLeftOf (Line l) {
		return this.end < l.begin;
	}
}
