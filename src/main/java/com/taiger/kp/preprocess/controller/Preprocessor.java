package com.taiger.kp.preprocess.controller;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.springframework.util.Assert;

import lombok.extern.java.Log;

@Log
public class Preprocessor {
	
	public static String fileOutStr = ".jpg";
	public static String dirStr = "/Users/jorge.rios/Work/base/";

	public static Mat preprocess (Mat mat, int i) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		//if (i != 1) return new Mat();
		Tunner tunner = new Tunner();
		
		Mat original = new Mat();
		Mat compose = new Mat();

		if (tunner.isBW(mat)) {
			
			tunner = new Tunner();
			System.gc();

			Mat grayscale = new Mat();
			tunner.grayscale(mat).copyTo(grayscale);
			mat.release();

			Highgui.imwrite("/Users/jorge.rios/Work/gs" + i + fileOutStr, grayscale);
			//mat = Tunner.rotate(mat);

			Mat visited = new Mat(grayscale.rows(), grayscale.cols(), CvType.CV_8UC1, new Scalar(255.0));
			grayscale = tunner.killCouples(grayscale);
			grayscale.copyTo(original);
			Highgui.imwrite("/Users/jorge.rios/Work/org" + i + fileOutStr, original);
			
			//mat = Tunner.rFatter(mat);
			grayscale = tunner.filler(grayscale);

			grayscale = tunner.plusFatter(grayscale);

			//Highgui.imwrite("/Users/jorge.rios/Work/org" + fileOutStr, original);

			grayscale = tunner.imageThresholding(grayscale);

			Highgui.imwrite("/Users/jorge.rios/Work/th" + i + fileOutStr, grayscale);
			//Highgui.imwrite("/Users/jorge.rios/Work/th" + fileOutStr, mat);

			Mat lines = new Mat();
			
			tunner.linesStretch(grayscale, i).copyTo(lines);
			grayscale.release();
			
			Highgui.imwrite("/Users/jorge.rios/Work/lines" + i + fileOutStr, lines);
			//lines = tunner.cleanLines(lines);
			lines = tunner.invert(lines);
			Highgui.imwrite("/Users/jorge.rios/Work/linesc" + i + fileOutStr, lines);
			
			tunner.fusion(original, lines).copyTo(compose);
			original.release();
			lines.release();
			Highgui.imwrite("/Users/jorge.rios/Work/compose" + i + fileOutStr, compose);
			
			//mat = Tunner.filler(mat);

		} /* else {

			// mat = Tunner.invert(mat);
			// mat = Tunner.bright(mat, 0.75, 0);
			// mat = Tunner.denoiseColor(mat);
			// mat = Tunner.invertColor(mat);
			mat = tunner.grayscale(mat);
			// Highgui.imwrite(dirStr + "gs" + fileOutStr, mat);

			// mat = Tunner.invert(mat);
			// mat = Tunner.tuneWhite (mat, 128, 50, 255);
			// mat = Tunner.killHermits(mat);
			// mat = Tunner.rFatter(mat);
			// mat = Tunner.filler(mat);
			// mat = Tunner.plusFatter(mat);
			mat.copyTo(original);
			//Highgui.imwrite("/Users/jorge.rios/Work/org" + fileOutStr, original);
			// mat = Tunner.tuneWhite(mat, 190);
			mat = tunner.imageThresholding(mat);

			//Highgui.imwrite("/Users/jorge.rios/Work/th" + fileOutStr, mat);
			// mat = Tunner.bw128(mat);
			// mat = Tunner.bwMedia(mat);
			// mat = Tunner.invert(mat);
			// mat = Tunner.killCouples(mat);
			// mat = Tunner.rFatter(mat);
			// mat = Tunner.filler(mat); //*

			// mat = Tunner.invert(mat);
			// Tunner.histogram2(mat);
			// Mat h = Tunner.hLines(mat, y);
			// Mat v = Tunner.vLines(mat, x);
			mat = tunner.lines(mat);
			// mat = Tunner.sumLines(v, h);
			mat = tunner.cleanLines(mat);
			mat = tunner.invert(mat);
			mat = tunner.fusion(original, mat);
			//Highgui.imwrite("/Users/jorge.rios/Work/bwx" + i + fileOutStr, mat);
			// Tunner.getHorizontalLines(mat);
			// Tunner.getVerticalLines(mat);
			// Tunner.histogram2(mat);
			// Tunner.histogram3(mat);

			// mat = Tunner.invert(mat);
			// mat = Tunner.fatter(mat);
		} */

		//Highgui.imwrite("/Users/jorge.rios/Work/bw" + i + fileOutStr, mat);
		
		return compose;
	}
	
}
