package com.taiger.kp.preprocess.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RGBA {
	double red = 255; 
	double green = 255;
	double blue = 255;
	double alpha = 255;
	
	public double[] toArray () {
		double[] result = {red, green, blue};
		return result;
	}
}
