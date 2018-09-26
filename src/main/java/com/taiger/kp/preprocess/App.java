package com.taiger.kp.preprocess;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
import com.taiger.kp.preprocess.controller.PageRunnable;
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
	
	public static List <Mat> modPages = null;
	public static List <Mat> pages = null;
	
	//static Integer i = 0;

	public static void main(String[] argss) {
		nu.pattern.OpenCV.loadLocally();
		
		long startTime = System.nanoTime();
		
		/*
		for (int index = 4; index <= 4; index++) {
			
			//if (index == 2) continue;
			
			try {
				
				pages = ExtractImages.noMultipleImages("/Users/jorge.rios/Work/base/", "/Users/jorge.rios/Work/base/", "PF BAJIO " + index + ".pdf");
				if (pages.isEmpty()) {
					pages = ExtractImages.pdf2image("/Users/jorge.rios/Work/base/PF BAJIO " + index + ".pdf", "/Users/jorge.rios/Work/base/");
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
			modPages = new ArrayList<>();
			for (Mat page : pages) {
				i++;
					/*
					try {
						System.gc();
						Thread.sleep(5000);
					} 	catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//*
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
				}
				//*
			}

			ExtractImages.savePdf(modPages, "/Users/jorge.rios/Work/base/new PF BAJIO " + index + ".pdf");
			
			
		} //*/
		
		
		
		
		//*
		for (int index = 4; index <= 4; index++) {
			
			//if (index == 2) continue;
			
			try {
				
				pages = ExtractImages.noMultipleImages("/Users/jorge.rios/Work/base/", "/Users/jorge.rios/Work/base/", "PF BAJIO " + index + ".pdf");
				if (pages.isEmpty()) {
					pages = ExtractImages.pdf2image("/Users/jorge.rios/Work/base/PF BAJIO " + index + ".pdf", "/Users/jorge.rios/Work/base/");
					log.info("has multiple images");
				}	else {
					log.info("has NOT multiple images");
				}
				
			} 	catch (Exception e) {
				e.printStackTrace();
			}

			if (pages == null ) return;

			modPages = Preprocessor.preprocessDoc(pages);
			
			int i = 0;
			for (Mat page : modPages) {
				i++;
				Highgui.imwrite("/Users/jorge.rios/Work/bw" + i + ".png", page);
			}

			ExtractImages.savePdf(modPages, "/Users/jorge.rios/Work/base/new PF BAJIO " + index + ".pdf");
		}
		//*/
		
		
		
		
		long endTime = System.nanoTime();

		long timeElapsed = endTime - startTime;

		System.out.println("Execution time in nanoseconds: " + timeElapsed);
		System.out.println("Execution time in milliseconds: " + timeElapsed / 1000000);
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
