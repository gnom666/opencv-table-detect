package com.taiger.kp.preprocess.model;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Getter
@Setter
@Log
public class Dither2 {

	int number = 10;
	List<Mat> mats;

	public Dither2() {
mats = new LinkedList<Mat>();
		
		Mat mat = new Mat(3, 3, CvType.CV_8U);
		mat.put(0, 0, 255.0);
		mat.put(0, 1, 255.0);
		mat.put(0, 2, 255.0);
		mat.put(1, 0, 255.0);
		mat.put(1, 1, 255.0);
		mat.put(1, 2, 255.0);
		mat.put(2, 0, 255.0);
		mat.put(2, 1, 255.0);
		mat.put(2, 2, 255.0);
		mats.add(mat);
		
		mat = new Mat(3, 3, CvType.CV_8U);
		mat.put(0, 0, 255.0);
		mat.put(0, 1, 255.0);
		mat.put(0, 2, 255.0);
		mat.put(1, 0, 255.0);
		mat.put(1, 1, 255.0);
		mat.put(1, 2, 255.0);
		mat.put(2, 0, 255.0);
		mat.put(2, 1, 255.0);
		mat.put(2, 2, 255.0);
		mats.add(mat);

		mat = new Mat(3, 3, CvType.CV_8U);
		mat.put(0, 0, 255.0);
		mat.put(0, 1, 0.0);
		mat.put(0, 2, 255.0);
		mat.put(1, 0, 255.0);
		mat.put(1, 1, 0.0);
		mat.put(1, 2, 255.0);
		mat.put(2, 0, 255.0);
		mat.put(2, 1, 255.0);
		mat.put(2, 2, 255.0);
		mats.add(mat);

		mat = new Mat(3, 3, CvType.CV_8U);
		mat.put(0, 0, 255.0);
		mat.put(0, 1, 0.0);
		mat.put(0, 2, 255.0);
		mat.put(1, 0, 255.0);
		mat.put(1, 1, 0.0);
		mat.put(1, 2, 0.0);
		mat.put(2, 0, 255.0);
		mat.put(2, 1, 255.0);
		mat.put(2, 2, 255.0);
		mats.add(mat);

		mat = new Mat(3, 3, CvType.CV_8U);
		mat.put(0, 0, 255.0);
		mat.put(0, 1, 0.0);
		mat.put(0, 2, 255.0);
		mat.put(1, 0, 0.0);
		mat.put(1, 1, 0.0);
		mat.put(1, 2, 0.0);
		mat.put(2, 0, 255.0);
		mat.put(2, 1, 0.0);
		mat.put(2, 2, 255.0);
		mats.add(mat);

		mat = new Mat(3, 3, CvType.CV_8U);
		mat.put(0, 0, 255.0);
		mat.put(0, 1, 0.0);
		mat.put(0, 2, 255.0);
		mat.put(1, 0, 0.0);
		mat.put(1, 1, 0.0);
		mat.put(1, 2, 0.0);
		mat.put(2, 0, 255.0);
		mat.put(2, 1, 0.0);
		mat.put(2, 2, 255.0);
		mats.add(mat);

		mat = new Mat(3, 3, CvType.CV_8U);
		mat.put(0, 0, 0.0);
		mat.put(0, 1, 0.0);
		mat.put(0, 2, 255.0);
		mat.put(1, 0, 0.0);
		mat.put(1, 1, 255.0);
		mat.put(1, 2, 0.0);
		mat.put(2, 0, 255.0);
		mat.put(2, 1, 0.0);
		mat.put(2, 2, 0.0);
		mats.add(mat);

		mat = new Mat(3, 3, CvType.CV_8U);
		mat.put(0, 0, 0.0);
		mat.put(0, 1, 0.0);
		mat.put(0, 2, 255.0);
		mat.put(1, 0, 0.0);
		mat.put(1, 1, 0.0);
		mat.put(1, 2, 0.0);
		mat.put(2, 0, 255.0);
		mat.put(2, 1, 0.0);
		mat.put(2, 2, 0.0);
		mats.add(mat);

		mat = new Mat(3, 3, CvType.CV_8U);
		mat.put(0, 0, 0.0);
		mat.put(0, 1, 0.0);
		mat.put(0, 2, 0.0);
		mat.put(1, 0, 0.0);
		mat.put(1, 1, 255.0);
		mat.put(1, 2, 0.0);
		mat.put(2, 0, 0.0);
		mat.put(2, 1, 0.0);
		mat.put(2, 2, 0.0);
		mats.add(mat);

		mat = new Mat(3, 3, CvType.CV_8U);
		mat.put(0, 0, 0.0);
		mat.put(0, 1, 0.0);
		mat.put(0, 2, 0.0);
		mat.put(1, 0, 0.0);
		mat.put(1, 1, 0.0);
		mat.put(1, 2, 0.0);
		mat.put(2, 0, 0.0);
		mat.put(2, 1, 0.0);
		mat.put(2, 2, 0.0);
		mats.add(mat);

	}

	public void apply(Mat result, int row, int col) {
		double sum = 0.0;
		int count = 0;
		
		for (int i = row; i < row+3 && i < result.height(); i++) {
			for (int j = col; j < col+3 && j < result.width(); j++) {
				count++;
				sum += result.get(i, j)[0];
			}
		}
		
		//System.out.println("sum: " + sum + " count: " + count);
		//System.out.println("ave: " + ((int)sum / count) + " rest: " + (((int)sum / count) / 26));
		Mat replacement = mats.get(9 - (int)(((int)sum / count) / 26));
		//System.out.println("replacement: " + replacement.dump());
		for (int i = 0; i < 3 && row+i < result.height(); i++) {
			for (int j = 0; j < 3 && col+j < result.width(); j++) {
				result.put(row+i, col+j, replacement.get(i, j));
			}
		}
	}
}
