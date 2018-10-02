package com.taiger.kp.preprocess.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.springframework.util.Assert;

import com.taiger.kp.preprocess.model.PageRunnable;

import lombok.extern.java.Log;

@Log
public class Preprocessor {
	
	public static String fileOutStr = ".jpg";
	public static String dirStr = "/Users/jorge.rios/Work/base/";
	
	public static List<Mat> preprocessDoc (List<Mat> pages) {
		List<Mat> modPages = new ArrayList<>();
		
		int i = 0;
		modPages = new ArrayList<>();
		List<PageRunnable> runnables = new ArrayList<>();
		while (i < pages.size()) {
			runnables.add(new PageRunnable(i, pages.get(i), null));
			i++;
		}
		
		ExecutorService es = Executors.newCachedThreadPool();
		for (i = 0; i < pages.size(); i++) {
		    es.execute(runnables.get(i));
		}
		es.shutdown();
		boolean finished = false;
		try {
			finished = es.awaitTermination(2, TimeUnit.MINUTES);
		} 	catch (InterruptedException e) {
			log.severe(e.toString());
		}
		
		for (PageRunnable pageR : runnables) {
			modPages.add(pageR.getModPage());
		}
		
		return modPages;
		
	}

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

			//Mat visited = new Mat(grayscale.rows(), grayscale.cols(), CvType.CV_8UC1, new Scalar(255.0));
			//grayscale = tunner.killHermitsFast(grayscale);
			
			grayscale = tunner.killHermits(grayscale);
			
			grayscale.copyTo(original);
			
			Highgui.imwrite("/Users/jorge.rios/Work/org" + i + fileOutStr, original);
			
			//mat = Tunner.rFatter(mat);
			grayscale = tunner.filler(grayscale);

			grayscale = tunner.plusFatter(grayscale);
			
			grayscale = tunner.filler(grayscale);
			
			Highgui.imwrite("/Users/jorge.rios/Work/plus" + i + fileOutStr, grayscale);

			//Highgui.imwrite("/Users/jorge.rios/Work/org" + fileOutStr, original);

			//grayscale = tunner.imageThresholding(grayscale);
			grayscale = tunner.invert(grayscale);

			Highgui.imwrite("/Users/jorge.rios/Work/th" + i + fileOutStr, grayscale);
			//Highgui.imwrite("/Users/jorge.rios/Work/th" + fileOutStr, mat);

			Mat lines = new Mat();
			
			tunner.linesStretchParall(grayscale, i).copyTo(lines);
			grayscale.release();
			
			Highgui.imwrite("/Users/jorge.rios/Work/lines" + i + fileOutStr, lines);
			//lines = tunner.cleanLines(lines);
			lines = tunner.invert(lines);
			Highgui.imwrite("/Users/jorge.rios/Work/linesc" + i + fileOutStr, lines);
			
			tunner.fusion(original, lines).copyTo(compose);
			//compose = tunner.filler(compose);
			original.release();
			lines.release();
			Highgui.imwrite("/Users/jorge.rios/Work/compose" + i + fileOutStr, compose);
			
			//mat = Tunner.filler(mat);

		}  else {
			
			tunner = new Tunner();
			System.gc();

			Mat grayscale = new Mat();
			tunner.grayscale(mat).copyTo(grayscale);
			mat.release();

			Highgui.imwrite("/Users/jorge.rios/Work/gs" + i + fileOutStr, grayscale);
			//mat = Tunner.rotate(mat);
			
			grayscale = tunner.imageThresholding(grayscale);
			
			grayscale = tunner.invert(grayscale);

			//Mat visited = new Mat(grayscale.rows(), grayscale.cols(), CvType.CV_8UC1, new Scalar(255.0));
			original = grayscale.clone();
			Highgui.imwrite("/Users/jorge.rios/Work/org" + i + fileOutStr, original);
			grayscale = tunner.killHermits(grayscale);
			//grayscale.copyTo(original);
			Highgui.imwrite("/Users/jorge.rios/Work/orgx" + i + fileOutStr, grayscale);
			
			//mat = Tunner.rFatter(mat);
			grayscale = tunner.filler(grayscale);

			grayscale = tunner.plusFatter(grayscale);
			
			grayscale = tunner.filler(grayscale);

			//Highgui.imwrite("/Users/jorge.rios/Work/org" + fileOutStr, original);

			grayscale = tunner.invert(grayscale);

			Highgui.imwrite("/Users/jorge.rios/Work/th" + i + fileOutStr, grayscale);
			//Highgui.imwrite("/Users/jorge.rios/Work/th" + fileOutStr, mat);

			Mat lines = new Mat();
			
			tunner.linesStretchParall (grayscale, i).copyTo(lines);
			grayscale.release();
			
			Highgui.imwrite("/Users/jorge.rios/Work/lines" + i + fileOutStr, lines);
			//lines = tunner.cleanLines(lines);
			lines = tunner.invert(lines);
			Highgui.imwrite("/Users/jorge.rios/Work/linesc" + i + fileOutStr, lines);
			
			tunner.fusion(original, lines).copyTo(compose);
			//compose = tunner.filler(compose);
			original.release();
			lines.release();
			Highgui.imwrite("/Users/jorge.rios/Work/compose" + i + fileOutStr, compose);
			
		} 

		//Highgui.imwrite("/Users/jorge.rios/Work/bw" + i + fileOutStr, mat);
		
		return compose;
	}
	
}
