package com.taiger.kp.preprocess.model;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

import com.taiger.kp.preprocess.controller.Tunner;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;

@Getter
@Setter
@Log
@AllArgsConstructor
@NoArgsConstructor
public class LineDetectionRunnable implements Runnable {
	
	private int index;
	private Mat page;
	private List<Line> lines;
	private int side; // 0:H 1:V

	@Override
	public void run() {
		switch (side) {
			case Constants.HORIZONTAL:
				lines = new Tunner().linesStretchH (page, index);
				break;
			case Constants.VERTICAL:
				lines = new Tunner().linesStretchV (page, index);
				break;
			default:
				lines = new ArrayList<>();
				break;
		}
	}
}
