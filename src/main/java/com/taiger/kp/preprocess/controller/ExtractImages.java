package com.taiger.kp.preprocess.controller;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.springframework.util.Assert;

import com.taiger.kp.preprocess.model.RGBA;

import lombok.extern.java.Log;

@Log
public class ExtractImages {
	
	/**
	 * Save processed pages in a pdf file
	 * @param pages images
	 * @param out output file
	 */
	public static void save (List<List<Mat>> pages, String out) {
		Assert.notNull(pages, "pages null");
		
		PDDocument document = new PDDocument();
		int i = 0;
		
		try {
			for (List<Mat> p : pages) {
				for (Mat mat : p) {
					BufferedImage image = mat2bufferedi(mat);
					
					float width = mat.width();
					float height = mat.height();
					PDPage page = new PDPage(new PDRectangle(width, height));
					document.addPage(page); 
					PDImageXObject img = LosslessFactory.createFromImage(document, image);
					PDPageContentStream contentStream = new PDPageContentStream(document, page);
					contentStream.drawImage(img, 0, 0);
					contentStream.close();
				}
			}
			
			document.save(out);
			document.close();
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
	}
	
	/**
	 * Save processed pages in a pdf file
	 * @param pages images
	 * @param out output file
	 */
	public static void savePdf (List<Mat> pages, String out) {
		Assert.notNull(pages, "pages null");
		
		PDDocument document = new PDDocument();
		int i = 0;
		
		try {
			for (Mat p : pages) {
				BufferedImage image = mat2bufferedi(p);
				
				float width = p.width();
				float height = p.height();
				PDPage page = new PDPage(new PDRectangle(width, height));
				document.addPage(page); 
				PDImageXObject img = LosslessFactory.createFromImage(document, image);
				PDPageContentStream contentStream = new PDPageContentStream(document, page);
				contentStream.drawImage(img, 0, 0);
				contentStream.close();
			}
			
			document.save(out);
			document.close();
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
	}
	 
	/**
	 * Convert every page of a pdf into a list of images
	 * @param pdf input pdf
	 * @param out output dir
	 * @return List of images
	 */
	public static List<Mat> pdf2image (String pdf, String out) {
		List<Mat> pageImgs = new ArrayList<>();
		
		try (final PDDocument document = PDDocument.load(new File(pdf))){
			
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            
            for (int page = 0; page < document.getNumberOfPages(); ++page)
            {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.GRAY);
                
                int rows = bim.getWidth();
		        int cols = bim.getHeight();
		        Mat newMat = new Mat(cols, rows, CvType.CV_8UC3);

		        for (int c = 0; c < cols; c++){
		            for (int r = 0; r < rows; r++) {
		                newMat.put(c, r, iToGrayscale(bim.getRGB(r, c)).toArray());
		            }
		        }
		        
		        Highgui.imwrite("/Users/jorge.rios/Work/img_" + page + ".png", newMat);
		        pageImgs.add(newMat);
                
                String fileName = out + "image-" + page + ".png";
                ImageIOUtil.writeImage(bim, fileName, 300);
            }
            
            document.close();
            
        } 	catch (IOException e){
            log.severe("Exception while trying to create pdf document - " + e);
        }
		
		return pageImgs;
	}
	
	/**
	 * Determines whether a pdf pages contain more than one image and return the list of images if the file
	 * contains only one image per page. In case of finding more than one image in one page an empty list is returned
	 * @param inDir input directory
	 * @param outDir output directory
	 * @param fileName file name
	 * @return List of images
	 * @throws Exception
	 */
	public static List<Mat> noMultipleImages (String inDir, String outDir, String fileName) throws Exception {
		Assert.hasText(inDir, "inDir should have text");
		Assert.hasText(outDir, "outDir should have text");
		Assert.hasText(fileName, "fileName should have text");
		
		List<List<Mat>> imgs = new ArrayList<>();

        try (final PDDocument document = PDDocument.load(new File(inDir + fileName))){

            PDPageTree list = document.getPages();
            int i = 0;
            for (PDPage page : list) {
            	List<Mat> pageImgs = new ArrayList<>();
                PDResources pdResources = page.getResources();
                int imgNum = 0;
                for (COSName name : pdResources.getXObjectNames()) {
                    PDXObject o = pdResources.getXObject(name);
                    if (o instanceof PDImageXObject) {
                        PDImageXObject image = (PDImageXObject)o;
                        //String filename = outDir + "extracted-image-" + i + ".png";
                        //ImageIO.write(image.getImage(), "png", new File(filename));
                        //System.out.println(i);
                        i++;
                        if (++imgNum >= 2) return new ArrayList<>();
                        
                        BufferedImage bi = image.getImage();
						int rows = image.getWidth();
				        int cols = image.getHeight();
				        Mat newMat = new Mat(cols, rows, CvType.CV_8UC3);
	
				        for (int c = 0; c < cols; c++){
				            for (int r = 0; r < rows; r++) {
				                newMat.put(c, r, iToRGBA(bi.getRGB(r, c)).toArray());
				            }
				        }
				        
				        Highgui.imwrite("/Users/jorge.rios/Work/img_" + i + ".png", newMat);
				        pageImgs.add(newMat);
                    }
                }
                imgs.add(pageImgs);
            }

        } 	catch (IOException e) {
            log.severe(e.getMessage());
        }
        
        List<Mat> result = new ArrayList<>();
        for (List<Mat> page : imgs) {
        	if (!page.isEmpty())
        		result.add(page.get(0));
        }
		
		return result;
	}
	
	/**
	 * Extract all the images in a pdf
	 * @param inDir input dir
	 * @param outDir output dir
	 * @param fileName file name
	 * @return List of pages that are a list of images
	 * @throws Exception
	 */
	public static List<List<Mat>> extract (String inDir, String outDir, String fileName) throws Exception {
		Assert.hasText(inDir, "inDir should have text");
		Assert.hasText(outDir, "outDir should have text");
		Assert.hasText(fileName, "fileName should have text");
		
		List<List<Mat>> imgs = new ArrayList<>();

        try (final PDDocument document = PDDocument.load(new File(inDir + fileName))){

            PDPageTree list = document.getPages();
            int i = 0;
            for (PDPage page : list) {
            	List<Mat> pageImgs = new ArrayList<>();
                PDResources pdResources = page.getResources();
                for (COSName name : pdResources.getXObjectNames()) {
                    PDXObject o = pdResources.getXObject(name);
                    if (o instanceof PDImageXObject) {
                        PDImageXObject image = (PDImageXObject)o;
                        //String filename = outDir + "extracted-image-" + i + ".png";
                        //ImageIO.write(image.getImage(), "png", new File(filename));
                        //System.out.println(i);
                        i++;
                        
                        BufferedImage bi = image.getImage();
						int rows = image.getWidth();
				        int cols = image.getHeight();
				        Mat newMat = new Mat(cols, rows, CvType.CV_8UC3);
	
				        for (int c = 0; c < cols; c++){
				            for (int r = 0; r < rows; r++) {
				                newMat.put(c, r, iToRGBA(bi.getRGB(r, c)).toArray());
				            }
				        }
				        
				        Highgui.imwrite("/Users/jorge.rios/Work/img_" + i + ".png", newMat);
				        pageImgs.add(newMat);
                    }
                }
                imgs.add(pageImgs);
            }

        } 	catch (IOException e) {
            log.severe(e.getMessage());
        }
        
        return imgs;
    }
	
	/**
	 * Convert a color integer to RGB
	 * @param value integer representing a color
	 * @return RGB
	 */
	public static RGBA iToRGBA (int value) {
		RGBA result = new RGBA();
		
		result.setRed  (value & 0x00ff0000);
		result.setGreen(value & 0x0000ff00);
		result.setBlue (value & 0x000000ff);
		result.setAlpha(value & 0xff000000);
		
		return result;
	}
	
	/**
	 * Convert a grayscale integer to RGB
	 * @param value integer representing a gray
	 * @return RGB
	 */
	public static RGBA iToGrayscale (int value) {
		RGBA result = new RGBA();
		
		result.setRed  (value & 0x000000ff);
		result.setGreen(value & 0x000000ff);
		result.setBlue (value & 0x000000ff);
		result.setAlpha(value & 0x000000ff);
		
		//result.toGray();
		
		return result;
	}
	
	/**
	 * Convert an OpenCV Mat to a byte array
	 * @param mat OpenCV image
	 * @return byte aray representation of an image
	 */
	public static byte[] mat2bytearray (Mat mat) {
		Assert.notNull(mat, "mat null");
		
		//Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY, 0);

		// Create an empty image in matching format
		BufferedImage gray = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_BYTE_GRAY);
		
		Assert.notNull(gray, "gray null");

		// Get the BufferedImage's backing array and copy the pixels directly into it
		byte[] data = ((DataBufferByte) gray.getRaster().getDataBuffer()).getData();
		mat.get(0, 0, data);
		
		return data;
	}
	
	/**
	 * Convert an OpenCV Mat to a buffered image
	 * @param mat OpenCV image
	 * @return buffered image
	 */
	public static BufferedImage mat2bufferedi (Mat mat) {
		Assert.notNull(mat, "mat null");
		
		//Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY, 0);

		// Create an empty image in matching format
		BufferedImage gray = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_BYTE_GRAY);
		
		Assert.notNull(gray, "gray null");

		// Get the BufferedImage's backing array and copy the pixels directly into it
		byte[] data = ((DataBufferByte) gray.getRaster().getDataBuffer()).getData();
		mat.get(0, 0, data);
		
		return gray;
	}
}
