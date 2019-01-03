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
import com.taiger.kp.preprocess.controller.PageInfo;
import com.taiger.kp.preprocess.controller.Preprocessor;
import com.taiger.kp.preprocess.model.PageRunnable;

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
	public static List <PageInfo> pagesInfo = null;
	public static List <PageInfo> infoPages = null;
	
	//static Integer i = 0;

	public static void main(String[] argss) {
		nu.pattern.OpenCV.loadLocally();
		
		long startTime = System.nanoTime();
		
		List<String> filenames = new ArrayList<>();
//		filenames.add("PF BAJIO 2.pdf");
//		filenames.add("PF BAJIO 3.pdf");
//		filenames.add("PF BAJIO 4.pdf");
//		filenames.add("PF METRO 1.pdf");
//		filenames.add("PF METRO 2.pdf");
//		filenames.add("PF METRO 3.pdf");
//		filenames.add("PF METRO 4.pdf");
//		filenames.add("PF NORESTE 1.pdf");
//		filenames.add("PF NORESTE 3.pdf");
//		filenames.add("PF NORESTE 4.pdf");
//		filenames.add("PF NOROESTE 1.pdf");
//		filenames.add("PF NOROESTE 2.pdf");
//		filenames.add("PF OCCIDENTE 1.pdf");
//		filenames.add("PF SUR 1.pdf");
//		filenames.add("PF SUR 2.pdf");
//		filenames.add("PF SURESTE 2.pdf");
//		filenames.add("PF SURESTE 3.pdf");
		
//		filenames.add("Doc7-2.pdf");
		filenames.add("Doc63-2.pdf");
		filenames.add("Doc20-2.pdf");
//		filenames.add("Doc7.pdf");
//		filenames.add("Doc63.pdf");
//		filenames.add("Doc20.pdf");
		
		
		//*
		for (String filename : filenames) {
			
			try {
				
				pagesInfo = ExtractImages.noMultipleImagesInfo ("/Users/jorge.rios/Work/base/", "/Users/jorge.rios/Work/base/", filename);
				if (pagesInfo.isEmpty()) {
					pagesInfo = ExtractImages.pdf2imageInfo ("/Users/jorge.rios/Work/base/" + filename, "/Users/jorge.rios/Work/base/");
					log.info("has multiple images");
				}	else {
					log.info("has NOT multiple images");
				}
				
			} 	catch (Exception e) {
				e.printStackTrace();
			}

			if (pagesInfo == null ) return;
			
			pages = new ArrayList<>();
			pagesInfo.forEach (pi -> pages.add(pi.getPage()));

			modPages = Preprocessor.preprocessDoc(pages);
			
			int i = 0;
			for (Mat page : modPages) {
				pagesInfo.get(i).setPage (page);
				i++;
				//Highgui.imwrite ("/Users/jorge.rios/Work/bw" + i + ".png", page);
			}

			ExtractImages.savePdfInfo (pagesInfo, "/Users/jorge.rios/Work/base/new " + filename);
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
