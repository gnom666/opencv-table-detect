package com.taiger.kp.preprocess;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import com.taiger.kp.preprocess.controller.ExtractImages;
import com.taiger.kp.preprocess.controller.Preprocessor;

import lombok.extern.java.Log;

/**
 * Hello world!
 *
 */
@Log
public class App {

	public static String fileInStr = "3.png";
	public static String fileOutStr = "_b2.jpg";
	public static String dirStr = "/Users/jorge.rios/Work/base/";

	public static void main(String[] argss) {
		nu.pattern.OpenCV.loadLocally();
		
		List <Mat> pages = null;
		List <Mat> modPages = new ArrayList<>();
		
		for (int index = 4; index <= 4; index++) {
			
			//if (index == 2) continue;
			
			try {
				
				pages = ExtractImages.noMultipleImages("/Users/jorge.rios/Work/base/", "/Users/jorge.rios/Work/base/", "PF NORESTE " + index + ".pdf");
				if (pages.isEmpty()) {
					pages = ExtractImages.pdf2image("/Users/jorge.rios/Work/base/PF NORESTE " + index + ".pdf", "/Users/jorge.rios/Work/base/");
					log.info("has multiple images");
				}	else {
					log.info("has NOT multiple images");
				}
					
				
				//pages =  ExtractImages.extract("/Users/jorge.rios/Work/base/", "/Users/jorge.rios/Work/base/", "PF METRO " + index + ".pdf");
				
			} 	catch (Exception e) {
				e.printStackTrace();
			}

			// Mat mat = Highgui.imread (dirStr + fileInStr,
			// Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			//Mat mat = Highgui.imread(dirStr + fileInStr, Highgui.CV_LOAD_IMAGE_COLOR);
			if (pages == null ) return;
			//Mat mat = pages.get(3).get(0);
			int i = 0;
			for (Mat page : pages) {
				i++;
					/**
					try {
						System.gc();
						Thread.sleep(5000);
					} 	catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}**/
				modPages.add(Preprocessor.preprocess(page, i));
			}
			i = 0;
			for (Mat page : modPages) {
				
				i++;
				Highgui.imwrite("/Users/jorge.rios/Work/bw" + i + ".png", page);
				/*try {
					System.gc();
					Thread.sleep(5000);
				} 	catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}

			ExtractImages.savePdf(modPages, "/Users/jorge.rios/Work/base/new PF NORESTE " + index + ".pdf");
			
			
		}

		
		
		/*
		boolean black = true;

		if (Tunner.isBW(mat)) {

			mat = Tunner.grayscale(mat);

			//Highgui.imwrite("/Users/jorge.rios/Work/rot0" + fileOutStr, mat);
			//mat = Tunner.rotate(mat);
			//Highgui.imwrite("/Users/jorge.rios/Work/rot" + fileOutStr, mat);

			mat = Tunner.killHermits(mat);
			mat.copyTo(original);
			mat = Tunner.rFatter(mat);
			mat = Tunner.filler(mat);

			mat = Tunner.plusFatter(mat);

			Highgui.imwrite("/Users/jorge.rios/Work/org" + fileOutStr, original);

			mat = Tunner.imageThresholding(mat);

			Highgui.imwrite("/Users/jorge.rios/Work/th" + fileOutStr, mat);

			List<Integer> y = new LinkedList<>();
			List<Integer> x = new LinkedList<>();

			mat = Tunner.lines(mat);

			mat = Tunner.cleanLines(mat);
			mat = Tunner.invert(mat);
			mat = Tunner.fusion(original, mat);
			mat = Tunner.filler(mat);

		} else {

			// mat = Tunner.invert(mat);
			// mat = Tunner.bright(mat, 0.75, 0);
			// mat = Tunner.denoiseColor(mat);
			// mat = Tunner.invertColor(mat);
			mat = Tunner.grayscale(mat);
			// Highgui.imwrite(dirStr + "gs" + fileOutStr, mat);

			// mat = Tunner.invert(mat);
			// mat = Tunner.tuneWhite (mat, 128, 50, 255);
			// mat = Tunner.killHermits(mat);
			// mat = Tunner.rFatter(mat);
			// mat = Tunner.filler(mat);
			// mat = Tunner.plusFatter(mat);
			mat.copyTo(original);
			Highgui.imwrite("/Users/jorge.rios/Work/org" + fileOutStr, original);
			// mat = Tunner.tuneWhite(mat, 190);
			mat = Tunner.imageThresholding(mat);

			Highgui.imwrite("/Users/jorge.rios/Work/th" + fileOutStr, mat);
			// mat = Tunner.bw128(mat);
			// mat = Tunner.bwMedia(mat);
			// mat = Tunner.invert(mat);
			// mat = Tunner.killCouples(mat);
			// mat = Tunner.rFatter(mat);
			// mat = Tunner.filler(mat); 

			// mat = Tunner.invert(mat);
			// Tunner.histogram2(mat);
			// Mat h = Tunner.hLines(mat, y);
			// Mat v = Tunner.vLines(mat, x);
			mat = Tunner.lines(mat);
			// mat = Tunner.sumLines(v, h);
			mat = Tunner.cleanLines(mat);
			mat = Tunner.invert(mat);
			mat = Tunner.fusion(original, mat);
			// Tunner.getHorizontalLines(mat);
			// Tunner.getVerticalLines(mat);
			// Tunner.histogram2(mat);
			// Tunner.histogram3(mat);

			// mat = Tunner.invert(mat);
			// mat = Tunner.fatter(mat);
		}

		Highgui.imwrite("/Users/jorge.rios/Work/bw" + fileOutStr, mat); */
	}

	public static List<RenderedImage> getImagesFromPDF(PDDocument document) throws IOException {
		List<RenderedImage> images = new ArrayList<>();
		for (PDPage page : document.getPages()) {
			images.addAll(getImagesFromResources(page.getResources()));
		}

		return images;
	}

	private static List<RenderedImage> getImagesFromResources(PDResources resources) throws IOException {
		List<RenderedImage> images = new ArrayList<>();

		for (COSName xObjectName : resources.getXObjectNames()) {
			PDXObject xObject = resources.getXObject(xObjectName);

			if (xObject instanceof PDFormXObject) {
				images.addAll(getImagesFromResources(((PDFormXObject) xObject).getResources()));
			} else if (xObject instanceof PDImageXObject) {
				images.add(((PDImageXObject) xObject).getImage());
			}
		}

		return images;
	}

}
