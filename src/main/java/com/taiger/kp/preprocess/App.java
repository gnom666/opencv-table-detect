package com.taiger.kp.preprocess;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import com.taiger.kp.preprocess.controller.Tunner;

import lombok.extern.java.Log;

/**
 * Hello world!
 *
 */
@Log
public class App {
	
	public static String fileInStr = "bajio4.jpg"; 
	public static String fileOutStr = "_b4.jpg";
	public static String dirStr = "/Users/jorge.rios/Work/base/";
	
    public static void main (String[] argss) {
    	nu.pattern.OpenCV.loadLocally();
        
        //Mat mat = Highgui.imread (dirStr + fileInStr, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
    	Mat mat = Highgui.imread (dirStr + fileInStr, Highgui.CV_LOAD_IMAGE_COLOR);
    	Mat original = new Mat();
    	
    	boolean black = false;
    	
    	if (black) {
    		
    		mat = Tunner.grayscale(mat);
            
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
            
    	}	else {
            
            //mat = Tunner.invert(mat);
            //mat = Tunner.bright(mat, 0.75, 0);
            //mat = Tunner.denoiseColor(mat);
            //mat = Tunner.invertColor(mat);
            mat = Tunner.grayscale(mat);
            //Highgui.imwrite(dirStr + "gs" + fileOutStr, mat);
            
            //mat = Tunner.invert(mat);
            //mat = Tunner.tuneWhite (mat, 128, 50, 255);
            //mat = Tunner.killHermits(mat);
            //mat = Tunner.rFatter(mat);
            //mat = Tunner.filler(mat);
            //mat = Tunner.plusFatter(mat);
            mat.copyTo(original);
            Highgui.imwrite("/Users/jorge.rios/Work/org" + fileOutStr, original);
            //mat = Tunner.tuneWhite(mat, 190);
            mat = Tunner.imageThresholding(mat);
            
            Highgui.imwrite("/Users/jorge.rios/Work/th" + fileOutStr, mat);
            //mat = Tunner.bw128(mat);
            //mat = Tunner.bwMedia(mat);
            //mat = Tunner.invert(mat);
            //mat = Tunner.killCouples(mat);
            //mat = Tunner.rFatter(mat);
            //mat = Tunner.filler(mat); //*/
            
            //mat = Tunner.invert(mat);
            //Tunner.histogram2(mat);
            //Mat h = Tunner.hLines(mat, y);
            //Mat v = Tunner.vLines(mat, x);
            mat = Tunner.lines(mat);
            //mat = Tunner.sumLines(v, h);
            mat = Tunner.cleanLines(mat);
            mat = Tunner.invert(mat);
            mat = Tunner.fusion(original, mat);
            //Tunner.getHorizontalLines(mat);
            //Tunner.getVerticalLines(mat);
            //Tunner.histogram2(mat);
            //Tunner.histogram3(mat);
            
            //mat = Tunner.invert(mat);
            //mat = Tunner.fatter(mat);
    	}
        
        Highgui.imwrite("/Users/jorge.rios/Work/bw" + fileOutStr, mat);
    }
    
    
}
