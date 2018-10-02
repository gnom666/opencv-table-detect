package com.taiger.kp.preprocess.model;

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
public class ROIRunnable implements Runnable {

	private Mat roi;
	private int order = -1;
	private int direction = -1;

	@Override
	public void run() {
		switch (order) {
			case Constants.KILL_HERMITS:
				new Tunner().killHermitsMod(roi, this.direction);
				break;
			case Constants.FILLER:
				break;
			default:
				break;
		}
	}

}
