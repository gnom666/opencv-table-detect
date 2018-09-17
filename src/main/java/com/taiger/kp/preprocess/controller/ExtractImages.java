package com.taiger.kp.preprocess.controller;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.springframework.util.Assert;

import com.taiger.kp.preprocess.model.RGBA;

import lombok.extern.java.Log;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Log
public class ExtractImages {
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
				        
				        //Highgui.imwrite("/Users/jorge.rios/Work/base/img_" + i + ".jpg", newMat);
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
	
	public static RGBA iToRGBA (int value) {
		RGBA result = new RGBA();
		
		result.setRed  (value & 0x00ff0000);
		result.setGreen(value & 0x0000ff00);
		result.setBlue (value & 0x000000ff);
		result.setAlpha(value & 0xff000000);
		
		return result;
	}
}
