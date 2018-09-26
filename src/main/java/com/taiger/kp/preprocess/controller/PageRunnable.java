package com.taiger.kp.preprocess.controller;

import org.opencv.core.Mat;

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
public class PageRunnable implements Runnable {
	
	private int index;
	private Mat page;
	private Mat modPage;
	

	@Override
	public void run() {
		modPage = Preprocessor.preprocess(page, index);
	}

}
