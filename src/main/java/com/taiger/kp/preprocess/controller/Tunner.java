package com.taiger.kp.preprocess.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import com.taiger.kp.preprocess.model.Constants;
import com.taiger.kp.preprocess.model.Dither2;
import com.taiger.kp.preprocess.model.Line;
import com.taiger.kp.preprocess.model.LineDetectionRunnable;

import lombok.extern.java.Log;

import org.springframework.util.Assert;

@Log
public class Tunner {

	/**
	 * Changes contrast and brightness
	 * 
	 * @param mat
	 *            Image
	 * @param alpha
	 *            Contrast
	 * @param beta
	 *            Brightness
	 * @return Modified image
	 */
	public Mat bright(Mat mat, double alpha, double beta) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				result.put(i, j, alpha * (result.get(i, j)[0]) + beta);
			}
		}

		return result;
	}

	/**
	 * Negative
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat invert(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC1, new Scalar(255));
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		
		Core.bitwise_not(mat, result);
//		for (int i = 0; i < mat.height(); i++) {
//			for (int j = 0; j < mat.width(); j++) {
//				result.put(i, j, (255.0 - result.get(i, j)[0]) >= 0 ? 255.0 - result.get(i, j)[0] : 0.0);
//			}
//		}

		return result;
	}

	/**
	 * Changes to white pixels over specified limit
	 * 
	 * @param mat
	 *            Image
	 * @param limit
	 *            Limit
	 * @return Modified image
	 */
	public Mat tuneWhite(Mat mat, double limit) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Assert.isTrue(limit >= 0 && limit < 256, "limit should be between [0;255]");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				if (result.get(i, j)[0] > limit)
					result.put(i, j, 255.0);
			}
		}

		return result;
	}

	/**
	 * Changes pixels around specified center
	 * 
	 * @param mat
	 *            Image
	 * @param center
	 *            Centroid
	 * @param radio
	 *            Radius
	 * @param value
	 *            New value
	 * @return Image
	 */
	public Mat tuneWhite(Mat mat, double center, double radio, double value) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Assert.isTrue(center >= 0 && center < 256, "center should be between [0;255]");
		Assert.isTrue(radio >= 0 && radio < 128 && center - radio >= 0 && center + radio < 256,
				"radio should be between [0;127] and 'center +- radio' inside [0;255]");
		Assert.isTrue(value >= 0 && value < 256, "value should be between [0;255]");

		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				if (result.get(i, j)[0] > center - radio && result.get(i, j)[0] < center + radio)
					result.put(i, j, value);
			}
		}

		return result;
	}

	/**
	 * Black and white using 256/2
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat bw128(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				if (result.get(i, j)[0] / 2 > 127)
					result.put(i, j, 255.0);
				else
					result.put(i, j, 0.0);
			}
		}

		return result;
	}

	/**
	 * Black and white using media
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat bwMedia(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		double higher = 0, lower = 255;

		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				if (mat.get(i, j)[0] < lower)
					lower = mat.get(i, j)[0];
				if (mat.get(i, j)[0] > higher)
					higher = mat.get(i, j)[0];
			}
		}

		double media = (higher - lower) / 2;

		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				if (result.get(i, j)[0] / 2 > 255 - media)
					result.put(i, j, 255.0);
				else
					result.put(i, j, 0.0);
			}
		}

		return result;
	}

	/**
	 * Black and white using average
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat bwAve(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		double sum = 0;
		int qty = mat.height() * mat.width();

		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				sum += result.get(i, j)[0];
			}
		}

		double ave = sum / qty;
		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				if (result.get(i, j)[0] / 2 > 255 - ave)
					result.put(i, j, 255.0);
				else
					result.put(i, j, 0.0);
			}
		}

		return result;
	}

	/**
	 * Black and white using dither matrix
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat bwDither(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Dither2 dither = new Dither2();
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		for (int i = 0; i < mat.height(); i += 3) {
			for (int j = 0; j < mat.width(); j += 3) {
				// System.out.println(i + " X " + j);
				dither.apply(result, i, j);
			}
		}

		return result;
	}

	/**
	 * Puts black around black pixels
	 * 
	 * @param mat
	 *            Image
	 * @return Modified Image
	 */
	public Mat fatter(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		boolean left, up, down, right;

		for (int y = 0; y < result.height(); y++) {
			up = y > 0;
			down = y < result.height() - 1;
			for (int x = 0; x < result.width(); x++) {
				left = x > 0;
				right = x < result.width() - 1;
				if (mat.get(y, x)[0] == 0.0) {
					if (left) {
						result.put(y, x - 1, 0.0);
						if (up)
							result.put(y - 1, x - 1, 0.0);
						if (down)
							result.put(y + 1, x - 1, 0.0);
					}
					if (right) {
						result.put(y, x + 1, 0.0);
						if (up)
							result.put(y - 1, x + 1, 0.0);
						if (down)
							result.put(y + 1, x + 1, 0.0);
					}
					if (up)
						result.put(y - 1, x, 0.0);
					if (down)
						result.put(y + 1, x, 0.0);
				}
			}
		}

		return result;
	}

	/**
	 * Puts black the the up, down, left and right pixels around a black pixel
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat plusFatter(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		boolean left, up, down, right;

		for (int y = 0; y < result.height(); y++) {
			
			up = y > 0;
			down = y < result.height() - 1;
			
			for (int x = 0; x < result.width(); x++) {
				
				left = x > 0;
				right = x < result.width() - 1;
				
				if (mat.get(y, x)[0] == 0.0) {
					if (left)
						result.put(y, x - 1, 0.0);
					if (right)
						result.put(y, x + 1, 0.0);
					if (up)
						result.put(y - 1, x, 0.0);
					if (down)
						result.put(y + 1, x, 0.0);
				}
			}
		}

		return result;
	}

	/**
	 * Puts black the the up and right pixels around a black pixel
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat rFatter(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		boolean down, right;

		for (int y = 0; y < result.height(); y++) {
			down = y < result.height() - 1;
			for (int x = 0; x < result.width(); x++) {
				right = x < result.width() - 1;
				if (mat.get(y, x)[0] == 0.0) {
					if (right)
						result.put(y, x + 1, 0.0);
					if (down)
						result.put(y + 1, x, 0.0);
				}
			}
		}

		return result;
	}

	/**
	 * Eliminates lonely black pixels
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat killHermits(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		boolean left, up, down, right;

		for (int y = 0; y < mat.height(); y++) {
			up = y > 0;
			down = y < result.height() - 1;
			for (int x = 0; x < mat.width(); x++) {
				left = x > 0;
				right = x < result.width() - 1;
				if (result.get(y, x)[0] == 0.0) {
					boolean hasNeighbour = false;
					if (up && result.get(y - 1, x)[0] == 0.0)
						hasNeighbour = true;
					if (down && result.get(y + 1, x)[0] == 0.0 && !hasNeighbour)
						hasNeighbour = true;
					if (left && result.get(y, x - 1)[0] == 0.0 && !hasNeighbour)
						hasNeighbour = true;
					if (right && result.get(y, x + 1)[0] == 0.0 && !hasNeighbour)
						hasNeighbour = true;
					if (up && left && result.get(y - 1, x - 1)[0] == 0.0 && !hasNeighbour)
						hasNeighbour = true;
					if (up && right && result.get(y - 1, x + 1)[0] == 0.0 && !hasNeighbour)
						hasNeighbour = true;
					if (down && left && result.get(y + 1, x - 1)[0] == 0.0 && !hasNeighbour)
						hasNeighbour = true;
					if (down && right && result.get(y + 1, x + 1)[0] == 0.0 && !hasNeighbour)
						hasNeighbour = true;

					if (!hasNeighbour)
						result.put(y, x, 255.0);
				}
			}
		}

		return result;
	}
	
	/**
	 * Eliminates lonely black pixels. It modifies original imgae
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat killHermitsMod(Mat mat, int direction) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Assert.isTrue(direction >= 0, "wrong direction");
		Mat tmp = new Mat();
		mat.copyTo(tmp);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		boolean left, up, down, right;

		if (direction == Constants.RIGHT) {
			for (int y = 0; y < tmp.height(); y++) {
				up = y > 0;
				down = y < tmp.height() - 1;
				for (int x = 0; x < tmp.width(); x++) {
					left = x > 0;
					right = x < tmp.width() - 1;
					if (tmp.get(y, x)[0] == 0.0) {
						boolean hasNeighbour = false;
						if (up && tmp.get(y - 1, x)[0] == 0.0)
							hasNeighbour = true;
						if (down && tmp.get(y + 1, x)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;
						if (left && tmp.get(y, x - 1)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;
						if (right && tmp.get(y, x + 1)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;
						if (up && left && tmp.get(y - 1, x - 1)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;
						if (up && right && tmp.get(y - 1, x + 1)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;
						if (down && left && tmp.get(y + 1, x - 1)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;
						if (down && right && tmp.get(y + 1, x + 1)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;

						if (!hasNeighbour)
							mat.put(y, x, 255.0);
					}
				}
			}
		}	else {
			for (int y = 0; y < tmp.height(); y++) {
				up = y > 0;
				down = y < tmp.height() - 1;
				for (int x = 0; x < tmp.width(); x++) {
					left = x > 0;
					right = x < tmp.width() - 1;
					if (tmp.get(y, x)[0] == 0.0) {
						boolean hasNeighbour = false;
						if (up && tmp.get(y - 1, x)[0] == 0.0)
							hasNeighbour = true;
						if (down && tmp.get(y + 1, x)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;
						if (left && tmp.get(y, x - 1)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;
						if (right && tmp.get(y, x + 1)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;
						if (up && left && tmp.get(y - 1, x - 1)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;
						if (up && right && tmp.get(y - 1, x + 1)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;
						if (down && left && tmp.get(y + 1, x - 1)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;
						if (down && right && tmp.get(y + 1, x + 1)[0] == 0.0 && !hasNeighbour)
							hasNeighbour = true;

						if (!hasNeighbour)
							mat.put(y, x, 255.0);
					}
				}
			}
		}

		return mat;
	}
	
	public Mat open (Mat mat, int rounds) {
		Assert.isTrue(rounds > 0, "rounds should be positive");
		Mat result = mat.clone();
		
		Imgproc.morphologyEx(mat, result, Imgproc.MORPH_OPEN, new Mat(3, 3, CvType.CV_8UC1, new Scalar(255)), new Point(-1, -1), rounds);
		
		return result;
	}
	
	public Mat close (Mat mat, int rounds) {
		Assert.isTrue(rounds > 0, "rounds should be positive");
		Mat result = mat.clone();
		
		Imgproc.morphologyEx(mat, result, Imgproc.MORPH_CLOSE, new Mat(3, 3, CvType.CV_8UC1, new Scalar(255)), new Point(-1, -1), rounds);
		
		return result;
	}

	/**
	 * Eliminates one neighbour pixels and lonely pixels
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat killCouples(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		boolean left, up, down, right;

		for (int y = 0; y < mat.height(); y++) {
			up = y > 0;
			down = y < mat.height() - 1;
			for (int x = 0; x < mat.width(); x++) {
				left = x > 0;
				right = x < mat.width() - 1;
				if (result.get(y, x)[0] == 0.0) {
					int neighbours = 0;
					if (up && result.get(y - 1, x)[0] == 0.0)
						neighbours++;
					if (down && result.get(y + 1, x)[0] == 0.0)
						neighbours++;
					if (left && result.get(y, x - 1)[0] == 0.0)
						neighbours++;
					if (right && result.get(y, x + 1)[0] == 0.0)
						neighbours++;
					if (up && left && result.get(y - 1, x - 1)[0] == 0.0)
						neighbours++;
					if (up && right && result.get(y - 1, x + 1)[0] == 0.0)
						neighbours++;
					if (down && left && result.get(y + 1, x - 1)[0] == 0.0)
						neighbours++;
					if (down && right && result.get(y + 1, x + 1)[0] == 0.0)
						neighbours++;

					if (neighbours <= 1) {
						result.put(y, x, 255.0);
					}
				}
			}
		}
		
		for (int y = mat.height() - 1; y >= 0; y--) {
			up = y > 0;
			down = y < mat.height() - 1;
			for (int x = mat.width() - 1; x >= 0; x--) {
				left = x > 0;
				right = x < mat.width() - 1;
				if (result.get(y, x)[0] == 0.0) {
					int neighbours = 0;
					if (up && result.get(y - 1, x)[0] == 0.0)
						neighbours++;
					if (down && result.get(y + 1, x)[0] == 0.0)
						neighbours++;
					if (left && result.get(y, x - 1)[0] == 0.0)
						neighbours++;
					if (right && result.get(y, x + 1)[0] == 0.0)
						neighbours++;
					if (up && left && result.get(y - 1, x - 1)[0] == 0.0)
						neighbours++;
					if (up && right && result.get(y - 1, x + 1)[0] == 0.0)
						neighbours++;
					if (down && left && result.get(y + 1, x - 1)[0] == 0.0)
						neighbours++;
					if (down && right && result.get(y + 1, x + 1)[0] == 0.0)
						neighbours++;

					if (neighbours <= 2) {
						result.put(y, x, 255.0);
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * Changes to black white pixels with three or more black pixels
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat filler(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		boolean left, up, down, right;

		for (int y = 0; y < mat.height(); y++) {
			up = y > 0;
			down = y < mat.height() - 1;
			for (int x = 0; x < mat.width(); x++) {
				left = x > 0;
				right = x < mat.width() - 1;
				int neighbours = 0;

				if (mat.get(y, x)[0] == 255.0) {
					if (up && mat.get(y - 1, x)[0] == 0.0)
						neighbours++;
					if (down && mat.get(y + 1, x)[0] == 0.0)
						neighbours++;
					if (left && mat.get(y, x - 1)[0] == 0.0)
						neighbours++;
					if (right && mat.get(y, x + 1)[0] == 0.0)
						neighbours++;
					if (up && left && mat.get(y - 1, x - 1)[0] == 0.0)
						neighbours++;
					if (up && right && mat.get(y - 1, x + 1)[0] == 0.0)
						neighbours++;
					if (down && left && mat.get(y + 1, x - 1)[0] == 0.0)
						neighbours++;
					if (down && right && mat.get(y + 1, x + 1)[0] == 0.0)
						neighbours++;

					if (neighbours >= 5)
						result.put(y, x, 0.0);
				}
			}
		}
		return result;
	}

	/**
	 * Black and white using Floyd-Steinberg error difusion
	 * 
	 * @param mat
	 *            Image
	 * @param limit
	 *            Limit
	 * @return Modified image
	 */
	public Mat bwFloydSteinberg(Mat mat, double limit) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Assert.isTrue(limit >= 0 && limit < 256, "center should be between [0;255]");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		int width = result.width();
		int height = result.height();
		double oldpixel, newpixel, error;
		boolean nbottom, nleft, nright;
		for (int y = 0; y < height; y++) {
			nbottom = y < height - 1;
			for (int x = 0; x < width; x++) {
				nleft = x > 0;
				nright = x < width - 1;
				oldpixel = result.get(y, x)[0];
				newpixel = oldpixel < limit ? 0.0 : 255.0;
				result.put(y, x, newpixel);
				error = oldpixel - newpixel;

				if (nright)
					result.put(y, x + 1, result.get(y, x + 1)[0] + 7 * error / 16);
				if (nleft && nbottom)
					result.put(y + 1, x - 1, result.get(y + 1, x - 1)[0] + 3 * error / 16);
				if (nbottom)
					result.put(y + 1, x, result.get(y + 1, x)[0] + 5 * error / 16);
				if (nright && nbottom)
					result.put(y + 1, x + 1, result.get(y + 1, x + 1)[0] + error / 16);
			}
		}

		return result;
	}

	/**
	 * Black and white using Floyd-Steinberg FL error difusion
	 * 
	 * @param mat
	 *            Image
	 * @param limit
	 *            Limit
	 * @return Modified image
	 */
	public Mat bwFloydSteinbergFL(Mat mat, double limit) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Assert.isTrue(limit >= 0 && limit < 256, "center should be between [0;255]");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		int width = result.width();
		int height = result.height();
		double oldpixel, newpixel, error;
		boolean nbottom, nleft, nright;
		for (int y = 0; y < height; y++) {
			nbottom = y < height - 1;
			for (int x = 0; x < width; x++) {
				nleft = x > 0;
				nright = x < width - 1;
				oldpixel = result.get(y, x)[0];
				newpixel = oldpixel < limit ? 0.0 : 255.0;
				result.put(y, x, newpixel);
				error = oldpixel - newpixel;

				if (nright)
					result.put(y, x + 1, result.get(y, x + 1)[0] + error / 2);
				if (nleft && nbottom)
					result.put(y + 1, x - 1, result.get(y + 1, x - 1)[0] + error / 4);
				if (nbottom)
					result.put(y + 1, x, result.get(y + 1, x)[0] + error / 4);
			}
		}

		return result;
	}

	/**
	 * Black and white using Atkinson with Floyd-Steinberg error difusion
	 * 
	 * @param mat
	 *            Image
	 * @param limit
	 *            Limit
	 * @return Modified image
	 */
	public Mat bwAtkinson(Mat mat, double limit) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Assert.isTrue(limit >= 0 && limit < 256, "center should be between [0;255]");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		int width = result.width();
		int height = result.height();
		double oldpixel, newpixel, error;
		boolean nbottom, nleft, nright, nrebottom, nreright;
		for (int y = 0; y < height; y++) {
			nbottom = y < height - 1;
			nrebottom = y < height - 2;
			for (int x = 0; x < width; x++) {
				nleft = x > 0;
				nright = x < width - 1;
				nreright = x < width - 2;
				oldpixel = result.get(y, x)[0];
				newpixel = oldpixel < limit ? 0.0 : 255.0;
				result.put(y, x, newpixel);
				error = oldpixel - newpixel;

				if (nright)
					result.put(y, x + 1, result.get(y, x + 1)[0] + error / 8);
				if (nreright)
					result.put(y, x + 2, result.get(y, x + 2)[0] + error / 8);
				if (nleft && nbottom)
					result.put(y + 1, x - 1, result.get(y + 1, x - 1)[0] + error / 8);
				if (nbottom)
					result.put(y + 1, x, result.get(y + 1, x)[0] + error / 8);
				if (nrebottom)
					result.put(y + 2, x, result.get(y + 2, x)[0] + error / 8);
				if (nright && nbottom)
					result.put(y + 1, x + 1, result.get(y + 1, x + 1)[0] + error / 8);
			}
		}

		return result;
	}

	/**
	 * Denoises color image
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat denoiseColor(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		Photo.fastNlMeansDenoisingColored(mat, result);

		return result;
	}

	/**
	 * Denoises image
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat denoise(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		Photo.fastNlMeansDenoising(mat, result);

		return result;
	}

	/**
	 * Calculates histogram
	 * 
	 * @param image
	 *            Image
	 */
	public static void histogram(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		// Calculate histogram
		java.util.List<Mat> matList = new LinkedList<Mat>();
		matList.add(mat);
		Mat histogram = new Mat();
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		MatOfFloat ranges = new MatOfFloat(0, 256);
		MatOfInt histSize = new MatOfInt(255);
		Imgproc.calcHist(matList, new MatOfInt(0), new Mat(), histogram, histSize, ranges);

		// Create space for histogram image
		Mat histImage = Mat.zeros(100, (int) histSize.get(0, 0)[0], CvType.CV_8UC1);
		// Normalize histogram
		Core.normalize(histogram, histogram, 1, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
		// Draw lines for histogram points
		for (int i = 0; i < (int) histSize.get(0, 0)[0]; i++) {
			Core.line(histImage, new org.opencv.core.Point(i, histImage.rows()),
					new org.opencv.core.Point(i, histImage.rows() - Math.round(histogram.get(i, 0)[0])),
					new Scalar(255, 255, 255), 1, 8, 0);
		}

		// Highgui.imwrite("/Users/jorge.rios/Work/histogram.jpg", histImage);
	}

	/**
	 * Calculates histogram
	 * 
	 * @param image
	 *            Image
	 */
	public static void histogram2(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		for (int y = 0; y < mat.height(); y++) {
			for (int x = 0; x < mat.width(); x++) {
				if (result.get(y, x)[0] == 0.0) {
					boolean blackFound = false;
					for (int i = x; i > 0 && !blackFound; i--) {
						if (result.get(y, i - 1)[0] == 0.0) {
							blackFound = true;
						} else {
							result.put(y, i - 1, 0.0);
							result.put(y, i, 255.0);
						}
					}
				}
			}
		}

		// Highgui.imwrite("/Users/jorge.rios/Work/histogram_h.jpg", result);
	}

	/**
	 * Calculates histogram
	 * 
	 * @param image
	 *            Image
	 */
	public static void histogram3(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		for (int x = 0; x < mat.width(); x++) {
			for (int y = mat.height() - 1; y > 0; y--) {
				if (result.get(y, x)[0] == 0.0) {
					boolean blackFound = false;
					for (int i = y; i < mat.height() - 1 && !blackFound; i++) {
						if (result.get(i + 1, x)[0] == 0.0) {
							blackFound = true;
						} else {
							result.put(i + 1, x, 0.0);
							result.put(i, x, 255.0);
						}
					}
				}
			}
		}

		// Highgui.imwrite("/Users/jorge.rios/Work/histogram_v.jpg", result);
	}

	/**
	 * Gets the grayscale image (1 channel) of a color image (3 channels)
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public Mat grayscale(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC1);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		/*Imgproc.cvtColor(mat, tmp, CvType.CV_8UC1);
		for (int y = 0; y < mat.height(); y++) {
			for (int x = 0; x < mat.width(); x++) {
				result.put(y, x, (tmp.get(y, x)[0] + tmp.get(y, x)[1] + tmp.get(y, x)[2]) / 3);
			}
		}*/
		Imgproc.cvtColor(mat, result, Imgproc.COLOR_BGR2GRAY);

		return result;
	}

	/**
	 * Invert color image (3 channels)
	 * 
	 * @param mat
	 *            Image
	 * @return Modified Image
	 */
	public Mat invertColor(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC3);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		for (int y = 0; y < mat.height(); y++) {
			for (int x = 0; x < mat.width(); x++) {
				double[] rgb = { 255.0 - mat.get(y, x)[0], 255.0 - mat.get(y, x)[1], 255.0 - mat.get(y, x)[2] };
				result.put(y, x, rgb);
			}
		}

		return result;
	}

	/**
	 * Finds the vertical lines on the image
	 * 
	 * @param mat
	 *            Image
	 * @return Image with only vertical lines
	 */
	public Mat getVerticalLines(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		Mat vertical = mat.clone();
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		try {

			int scale = 26; // play with this variable in order to increase/decrease the amount of lines to
							// be detected

			// Specify size on vertical axis
			int verticalsize = vertical.rows() / scale;

			// Create structure element for extracting vertical lines through morphology
			// operations
			Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, verticalsize));

			// Apply morphology operations
			Imgproc.erode(vertical, vertical, verticalStructure, new Point(-1, -1), 3);
			// Highgui.imwrite("/Users/jorge.rios/Work/ev.jpg", vertical);

			Imgproc.dilate(vertical, vertical, verticalStructure, new Point(-1, -1), 3);
			// Highgui.imwrite("/Users/jorge.rios/Work/dv.jpg", vertical);
			result = vertical.clone();

			verticalStructure.release();

		} catch (Exception e) {
			log.severe(e.getMessage());
		}

		return result;
	}

	/**
	 * Finds the horizontal lines on the image
	 * 
	 * @param mat
	 *            Image
	 * @return Image with only horizontal lines
	 */
	public Mat getHorizontalLines(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		Mat horizontal = mat.clone();

		try {

			int scale = 26; // play with this variable in order to increase/decrease the amount of lines to
							// be detected

			// Specify size on horizontal axis
			int horizontalsize = horizontal.cols() / scale;

			// Create structure element for extracting horizontal lines through morphology
			// operations
			Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontalsize, 1));

			// Apply morphology operations
			Imgproc.erode(horizontal, horizontal, horizontalStructure, new Point(-1, -1), 3);
			// Highgui.imwrite("/Users/jorge.rios/Work/eh.jpg", horizontal);

			Imgproc.dilate(horizontal, horizontal, horizontalStructure, new Point(-1, -1), 3);
			// Highgui.imwrite("/Users/jorge.rios/Work/dh.jpg", horizontal);
			result = horizontal.clone();

			horizontalStructure.release();

		} catch (Exception e) {
			log.severe(e.getMessage());
		}

		return result;
	}

	/**
	 * Intersect lines
	 * @param mat Image not inverted
	 * @return Image 0with horizontal and vertical lines
	 */
	public Mat intersectLines(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = mat.clone();
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		Mat v = getVerticalLines(mat);
		Mat h = getHorizontalLines(mat);

		for (int y = 0; y < mat.height(); y++) {
			for (int x = 0; x < mat.width(); x++) {
				if (v.get(y, x)[0] == h.get(y, x)[0] && v.get(y, x)[0] == 0.0) {
					result.put(y, x, 0.0);
				} else {
					result.put(y, x, 255.0);
				}

			}
		}

		return result;
	}

	/**
	 * 
	 * @param mat Image inverted
	 * @return
	 */
	public Mat sumLines(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = mat.clone();
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		Mat v = getVerticalLines(mat);
		Mat h = getHorizontalLines(mat);

		for (int y = 0; y < mat.height(); y++) {
			for (int x = 0; x < mat.width(); x++) {

				if (v.get(y, x)[0] == 255.0 || h.get(y, x)[0] == 255.0) {
					result.put(y, x, 255.0);
				} else {
					result.put(y, x, 0);
				}
			}
		}

		return result;
	}

	/**
	 * 
	 * @param v
	 * @param h
	 * @return
	 */
	public Mat sumLines(Mat v, Mat h) {
		Assert.notNull(v, "'v' shouldn't be null");
		Assert.notNull(h, "'h' shouldn't be null");
		Mat result = new Mat();
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		v.copyTo(result);

		for (int y = 0; y < result.height(); y++) {
			for (int x = 0; x < result.width(); x++) {

				if (v.get(y, x)[0] == 255.0 || h.get(y, x)[0] == 255.0) {
					result.put(y, x, 255.0);
				} else {
					result.put(y, x, 0);
				}
			}
		}

		return result;
	}

	public Mat cleanLines(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat();
		mat.copyTo(result);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		// Highgui.imwrite("/Users/jorge.rios/Work/tmp.jpg", result);
		boolean found = false;

		for (int y = 0; y < result.height() && !found; y++) {
			found = isWhiteH(mat, y);
			for (int x = 0; x < result.width() && !found; x++) {
				result.put(y, x, 0.0);
			}
		}
		// Highgui.imwrite("/Users/jorge.rios/Work/tmp1.jpg", result);

		found = false;
		for (int y = result.height() - 1; y >= 0 && !found; y--) {
			found = isWhiteH(mat, y);
			for (int x = 0; x < result.width() && !found; x++) {
				result.put(y, x, 0.0);
			}
		}
		// Highgui.imwrite("/Users/jorge.rios/Work/tmp2.jpg", result);

		found = false;
		for (int x = 0; x < result.width() && !found; x++) {
			found = isWhiteV(mat, x);
			for (int y = 0; y < result.height() && !found; y++) {
				result.put(y, x, 0.0);
			}
		}
		// Highgui.imwrite("/Users/jorge.rios/Work/tmp3.jpg", result);

		found = false;
		for (int x = result.width() - 1; x >= 0 && !found; x--) {
			found = isWhiteV(mat, x);
			for (int y = 0; y < result.height() && !found; y++) {
				result.put(y, x, 0.0);
			}
		}
		// Highgui.imwrite("/Users/jorge.rios/Work/tmp4.jpg", result);

		return result;
	}

	private static boolean isWhiteH(Mat mat, int y) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		for (int x = 0; x < mat.width(); x++) {
			if (mat.get(y, x)[0] == 0.0)
				return false;
		}
		return true;
	}

	private static boolean isWhiteV(Mat mat, int x) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		for (int y = 0; y < mat.height(); y++) {
			if (mat.get(y, x)[0] == 0.0)
				return false;
		}
		return true;
	}

	public Mat lines(Mat mat, int index) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		int alpha = 20;
		int beta = 20;
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC1, new Scalar(0.0));
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		Mat v = getVerticalLines(mat);
		Highgui.imwrite("/Users/jorge.rios/Work/v" + index + ".png", v);
		Mat h = getHorizontalLines(mat);
		Highgui.imwrite("/Users/jorge.rios/Work/h" + index + ".png", h);

		List<MatOfPoint> vcontours = new ArrayList<MatOfPoint>();
		Mat tmp = new Mat();
		Imgproc.findContours(v, vcontours, tmp, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		Highgui.imwrite("/Users/jorge.rios/Work/tmpv" + index + ".png", tmp);
		tmp.release();

		List<MatOfPoint> hcontours = new ArrayList<MatOfPoint>();
		tmp = new Mat();
		Imgproc.findContours(h, hcontours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		Highgui.imwrite("/Users/jorge.rios/Work/tmph" + index + ".png", tmp);
		tmp.release();

		v.release();
		h.release();

		List<Line> vLines = new LinkedList<>();
		List<Line> hLines = new LinkedList<>();

		for (MatOfPoint c : hcontours) {
			// System.out.println(c.dump());
			int minx = (int) lineMinX(c);
			int maxx = (int) lineMaxX(c);
			int y = (int) lineY(c);
			hLines.add(new Line(y, minx, maxx));
		}

		for (MatOfPoint c : vcontours) {
			// System.out.println(c.dump());
			int miny = (int) lineMinY(c);
			int maxy = (int) lineMaxY(c);
			int x = (int) lineX(c);
			vLines.add(new Line(x, miny, maxy));
		}

		List<Line> vFinalLines = new LinkedList<>();
		List<Line> hFinalLines = new LinkedList<>();

		for (Line hl : hLines) {
			int foundIntersection = 0;
			for (int i = 0; i < vLines.size() && foundIntersection <= 1; i++) {
				if (vLines.get(i).getBegin() - beta <= hl.getConstant()
						&& hl.getConstant() <= vLines.get(i).getEnd() + beta
						&& hl.getBegin() - alpha <= vLines.get(i).getConstant()
						&& vLines.get(i).getConstant() <= hl.getEnd() + alpha) {
					foundIntersection++;
				}
				if (foundIntersection > 1) {
					boolean tooNear = false;
					for (int j = 0; j < hFinalLines.size() && !tooNear; j++) {
						tooNear = hFinalLines.get(j).tooNear(hl);
					}
					if (!tooNear)
						hFinalLines.add(hl);

					tooNear = false;
					for (int j = 0; j < vFinalLines.size() && !tooNear; j++) {
						tooNear = vFinalLines.get(j).tooNear(vLines.get(i));
					}
					if (!tooNear)
						vFinalLines.add(vLines.get(i));
				}
			}
		}
		/*
		 * for (Line vl : vLines) { boolean foundIntersection = false; for (int i = 0; i
		 * < hLines.size() && !foundIntersection; i++) { if (hLines.get(i).getBegin() -
		 * alpha <= vl.getConstant() && vl.getConstant() <= hLines.get(i).getEnd() +
		 * alpha && vl.getBegin() - alpha <= hLines.get(i).getConstant() &&
		 * hLines.get(i).getConstant() <= vl.getEnd() + alpha) { foundIntersection =
		 * true; vFinalLines.add(vl); } } }
		 */

		for (Line hl : hFinalLines) {
			int y = hl.getConstant();
			for (int x = 0; x < result.width(); x++) {
				if (y != -1)
					result.put(y, x, 255.0);
				if (y > 0)
					result.put(y - 1, x, 255.0);
				if (y < result.height() - 1)
					result.put(y + 1, x, 255.0);

				// if (y > 1) result.put(y-2, x, 255.0);
				// if (y < result.height()-2) result.put(y+2, x, 255.0);
			}
		}

		for (Line vl : vFinalLines) {
			int x = vl.getConstant();
			for (int y = 0; y < result.height(); y++) {
				if (x != -1)
					result.put(y, x, 255.0);
				if (x > 0)
					result.put(y, x - 1, 255.0);
				if (x < result.width() - 1)
					result.put(y, x + 1, 255.0);

				// if (x > 1) result.put(y, x-2, 255.0);
				// if (x < result.width()-2) result.put(y, x+2, 255.0);
			}
		}

		// Highgui.imwrite("/Users/jorge.rios/Work/lines" + iCount++ + ".jpg", result);
		return result;
	}
	
	public List<Line> linesStretchH (Mat mat, int index) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		
		Mat h = getHorizontalLines(mat);
		Highgui.imwrite("/Users/jorge.rios/Work/h" + index + ".png", h);
		
		List<MatOfPoint> hcontours = new ArrayList<MatOfPoint>();
		Mat tmp = new Mat();
		Imgproc.findContours(h, hcontours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		tmp.release();
		h.release();
		
		List<Line> hLines = new LinkedList<>();
		for (MatOfPoint c : hcontours) {
			// System.out.println(c.dump());
			int minx = (int) lineMinX(c);
			int maxx = (int) lineMaxX(c);
			int y = (int) lineY(c);
			hLines.add(new Line(y, minx, maxx));
		}
		
		List<Line> hFinalLines = new LinkedList<>();
		Collections.sort(hLines);
		if (!hLines.isEmpty())
			hFinalLines.add(hLines.get(0));
		for (int i = 1; i < hLines.size(); i++) {
			Line li = hLines.get(i);
			boolean added = false;
			for (int j = 0; j < hFinalLines.size() && !added; j++) {
				Line lj = hFinalLines.get(j);

				if (li.tooNear(lj)) {
					Line composed = composeLines(li, lj);
					lj.setConstant(composed.getConstant());
					lj.setBegin(composed.getBegin());
					lj.setEnd(composed.getEnd());
					added = true;
				}
			}
			if (!added)
				hFinalLines.add(li);
		}
		Collections.sort(hFinalLines);
		
		return hFinalLines;
	}
	
	public List<Line> linesStretchV (Mat mat, int index) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		
		Mat v = getVerticalLines(mat);
		Highgui.imwrite("/Users/jorge.rios/Work/v" + index + ".png", v);
		
		List<MatOfPoint> vcontours = new ArrayList<MatOfPoint>();
		Mat tmp = new Mat();
		Imgproc.findContours(v, vcontours, tmp, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		tmp.release();
		v.release();
		
		List<Line> vLines = new LinkedList<>();
		for (MatOfPoint c : vcontours) {
			// System.out.println(c.dump());
			int miny = (int) lineMinY(c);
			int maxy = (int) lineMaxY(c);
			int x = (int) lineX(c);
			vLines.add(new Line(x, miny, maxy));
		}
		
		List<Line> vFinalLines = new LinkedList<>();
		Collections.sort(vLines);
		if (!vLines.isEmpty())
			vFinalLines.add(vLines.get(0));
		for (int i = 1; i < vLines.size(); i++) {
			Line li = vLines.get(i);
			boolean added = false;
			for (int j = 0; j < vFinalLines.size() && !added; j++) {
				Line lj = vFinalLines.get(j);

				if (li.tooNear(lj)) {
					Line composed = composeLines(li, lj);
					lj.setConstant(composed.getConstant());
					lj.setBegin(composed.getBegin());
					lj.setEnd(composed.getEnd());
					added = true;
				}
			}
			if (!added)
				vFinalLines.add(li);
		}
		Collections.sort(vFinalLines);
		
		return vFinalLines;
	}
	
	public Mat linesStretchParall (Mat mat, int index) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC1, new Scalar(0.0));
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		
		LineDetectionRunnable ldrH = new LineDetectionRunnable(index, mat, null, 0);
		LineDetectionRunnable ldrV = new LineDetectionRunnable(index, mat, null, 1);

		ExecutorService es = Executors.newCachedThreadPool();
		es.execute(ldrH);
		es.execute(ldrV);
		es.shutdown();
		boolean finished = false;
		try {
			finished = es.awaitTermination(2, TimeUnit.MINUTES);
		} 	catch (InterruptedException e) {
			log.severe(e.toString());
		}
		
		List<Line> hFinalLines = ldrH.getLines();
		List<Line> vFinalLines = ldrV.getLines();

		for (Line hl : hFinalLines) {
			int y = hl.getConstant();
			for (int x = hl.getBegin(); x < hl.getEnd(); x++) {
				if (y != -1)
					result.put(y, x, 255.0);
				if (y > 0)
					result.put(y - 1, x, 255.0);
				if (y < result.height() - 1)
					result.put(y + 1, x, 255.0);
				
				if (y > 1)
					result.put(y - 2, x, 255.0);
				if (y < result.height() - 2)
					result.put(y + 2, x, 255.0);
			}
		}

		for (Line vl : vFinalLines) {
			int x = vl.getConstant();
			for (int y = vl.getBegin(); y < vl.getEnd(); y++) {
				if (x != -1)
					result.put(y, x, 255.0);
				if (x > 0)
					result.put(y, x - 1, 255.0);
				if (x < result.width() - 1)
					result.put(y, x + 1, 255.0);
				
				if (x > 1)
					result.put(y, x - 2, 255.0);
				if (x < result.width() - 2)
					result.put(y, x + 2, 255.0);
			}
		}

		// Highgui.imwrite("/Users/jorge.rios/Work/lines" + iCount++ + ".jpg", result);
		return result;
	}

	public Mat linesStretch(Mat mat, int index) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC1, new Scalar(0.0));
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());

		Mat v = getVerticalLines(mat);
		Highgui.imwrite("/Users/jorge.rios/Work/v" + index + ".png", v);
		Mat h = getHorizontalLines(mat);
		Highgui.imwrite("/Users/jorge.rios/Work/h" + index + ".png", h);

		List<MatOfPoint> vcontours = new ArrayList<MatOfPoint>();
		Mat tmp = new Mat();
		Imgproc.findContours(v, vcontours, tmp, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		tmp.release();

		List<MatOfPoint> hcontours = new ArrayList<MatOfPoint>();
		tmp = new Mat();
		Imgproc.findContours(h, hcontours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		tmp.release();

		v.release();
		h.release();

		List<Line> vLines = new LinkedList<>();
		List<Line> hLines = new LinkedList<>();

		for (MatOfPoint c : hcontours) {
			// System.out.println(c.dump());
			int minx = (int) lineMinX(c);
			int maxx = (int) lineMaxX(c);
			int y = (int) lineY(c);
			hLines.add(new Line(y, minx, maxx));
		}

		for (MatOfPoint c : vcontours) {
			// System.out.println(c.dump());
			int miny = (int) lineMinY(c);
			int maxy = (int) lineMaxY(c);
			int x = (int) lineX(c);
			vLines.add(new Line(x, miny, maxy));
		}

		List<Line> vFinalLines = new LinkedList<>();
		List<Line> hFinalLines = new LinkedList<>();

		Collections.sort(hLines);
		Collections.sort(vLines);

		if (!hLines.isEmpty())
			hFinalLines.add(hLines.get(0));
		for (int i = 1; i < hLines.size(); i++) {
			Line li = hLines.get(i);
			boolean added = false;
			for (int j = 0; j < hFinalLines.size() && !added; j++) {
				Line lj = hFinalLines.get(j);

				if (li.tooNear(lj)) {
					Line composed = composeLines(li, lj);
					lj.setConstant(composed.getConstant());
					lj.setBegin(composed.getBegin());
					lj.setEnd(composed.getEnd());
					added = true;
				}
			}
			if (!added)
				hFinalLines.add(li);
		}

		if (!vLines.isEmpty())
			vFinalLines.add(vLines.get(0));
		for (int i = 1; i < vLines.size(); i++) {
			Line li = vLines.get(i);
			boolean added = false;
			for (int j = 0; j < vFinalLines.size() && !added; j++) {
				Line lj = vFinalLines.get(j);

				if (li.tooNear(lj)) {
					Line composed = composeLines(li, lj);
					lj.setConstant(composed.getConstant());
					lj.setBegin(composed.getBegin());
					lj.setEnd(composed.getEnd());
					added = true;
				}
			}
			if (!added)
				vFinalLines.add(li);
		}

		Collections.sort(hFinalLines);
		Collections.sort(vFinalLines);

		for (Line hl : hFinalLines) {
			int y = hl.getConstant();
			for (int x = hl.getBegin(); x < hl.getEnd(); x++) {
				if (y != -1)
					result.put(y, x, 255.0);
				if (y > 0)
					result.put(y - 1, x, 255.0);
				if (y < result.height() - 1)
					result.put(y + 1, x, 255.0);

				/*if (y > 1)
					result.put(y - 2, x, 255.0);
				if (y < result.height() - 2)
					result.put(y + 2, x, 255.0);*/
			}
		}

		for (Line vl : vFinalLines) {
			int x = vl.getConstant();
			for (int y = vl.getBegin(); y < vl.getEnd(); y++) {
				if (x != -1)
					result.put(y, x, 255.0);
				if (x > 0)
					result.put(y, x - 1, 255.0);
				if (x < result.width() - 1)
					result.put(y, x + 1, 255.0);

				/*if (x > 1)
					result.put(y, x - 2, 255.0);
				if (x < result.width() - 2)
					result.put(y, x + 2, 255.0);*/
			}
		}

		// Highgui.imwrite("/Users/jorge.rios/Work/lines" + iCount++ + ".jpg", result);
		return result;
	}

	private Line composeLines(Line l1, Line l2) {
		Assert.notNull(l1, "'l1' shouldn't be null");
		Assert.notNull(l2, "'l2' shouldn't be null");
		int constant = (l1.getConstant() + l2.getConstant()) / 2;
		int begin = l1.getBegin() < l2.getBegin() ? l1.getBegin() : l2.getBegin();
		int end = l1.getEnd() > l2.getEnd() ? l1.getEnd() : l2.getEnd();

		return new Line(constant, begin, end);
	}

	public Mat vLines(Mat mat, List<Integer> linesX) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Assert.notNull(linesX, "'linesX' shouldn't be null");
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC1);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		Mat v = getVerticalLines(mat);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(v, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		int counter = 0;
		for (MatOfPoint c : contours) {
			/*
			 * int x1 = (int) c.get(0, 0)[1]; int y1 = (int) c.get(0, 0)[0]; int x2 = (int)
			 * c.get(c.height() - 1, 0)[1]; int y2 = (int) c.get(c.height() - 1, 0)[0];
			 */
			int x = (int) lineX(c);
			for (int y = 0; y < result.height(); y++) {
				if (x != -1)
					result.put(y, x, 255.0);
				if (x > 0)
					result.put(y, x - 1, 255.0);
				if (x < result.width() - 1)
					result.put(y, x + 1, 255.0);
				linesX.add(x);
			}
		}

		// Highgui.imwrite("/Users/jorge.rios/Work/vlines.jpg", result);

		return result;
	}

	public Mat hLines(Mat mat, List<Integer> linesY) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Assert.notNull(linesY, "'linesY' shouldn't be null");
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC1);
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		Mat h = getHorizontalLines(mat);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(h, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		int counter = 0;
		for (MatOfPoint c : contours) {
			int minx = (int) lineMinX(c);
			int maxx = (int) lineMaxX(c);
			int y = (int) lineY(c);
			System.out.println(c.dump());
			for (int x = 0; x < result.width(); x++) {
				if (y != -1)
					result.put(y, x, 255.0);
				if (y > 0)
					result.put(y - 1, x, 255.0);
				if (y < result.height() - 1)
					result.put(y + 1, x, 255.0);
				linesY.add(y);
			}
		}

		// Highgui.imwrite("/Users/jorge.rios/Work/hlines.jpg", result);

		return result;
	}

	private double lineY(MatOfPoint c) {
		double sum = 0.0;
		for (int i = 0; i < c.height(); i++) {
			sum += c.get(i, 0)[1];
		}
		return sum / c.height();
	}

	private double lineX(MatOfPoint c) {
		double sum = 0.0;
		for (int i = 0; i < c.height(); i++) {
			sum += c.get(i, 0)[0];
		}
		return sum / c.height();
	}

	private double lineMinX(MatOfPoint c) {
		if (c == null || c.height() == 0)
			return 0.0;
		double min = c.get(0, 0)[0];
		for (int i = 1; i < c.height(); i++) {
			if (c.get(i, 0)[0] < min)
				min = c.get(i, 0)[0];
		}
		return min;
	}

	private double lineMaxX(MatOfPoint c) {
		if (c == null || c.height() == 0)
			return 0.0;
		double max = c.get(0, 0)[0];
		for (int i = 1; i < c.height(); i++) {
			if (c.get(i, 0)[0] > max)
				max = c.get(i, 0)[0];
		}
		return max;
	}

	private double lineMinY(MatOfPoint c) {
		if (c == null || c.height() == 0)
			return 0.0;
		double min = c.get(0, 0)[1];
		for (int i = 1; i < c.height(); i++) {
			if (c.get(i, 0)[1] < min)
				min = c.get(i, 0)[1];
		}
		return min;
	}

	private double lineMaxY(MatOfPoint c) {
		if (c == null || c.height() == 0)
			return 0.0;
		double max = c.get(0, 0)[1];
		for (int i = 1; i < c.height(); i++) {
			if (c.get(i, 0)[1] > max)
				max = c.get(i, 0)[1];
		}
		return max;
	}

	private double lineY(double x1, double y1, double x2, double y2, double x) {
		if (x1 == x2)
			return -1;
		return (y2 - y1) * (x - x1) / (x2 - x1) + y1;
	}

	private double lineX(double x1, double y1, double x2, double y2, double y) {
		if (y1 == y2)
			return -1;
		return (x2 - x1) * (y - y1) / (y2 - y1) + x1;
	}

	public Mat imageThresholding(Mat gray) {
		Assert.notNull(gray, "'gray' shouldn't be null");

		Mat bw = new Mat();
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		try {
			// Apply adaptiveThreshold at the bitwise_not of gray

			Core.bitwise_not(gray, gray);

			Imgproc.adaptiveThreshold(gray, bw, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 2);
			// saveMat(File.createTempFile("WB_mean_", FILE_SUFFIX, resultDirFile), bw);

			// Imgproc.adaptiveThreshold(gray, bw, 255, ADAPTIVE_THRESH_GAUSSIAN_C,
			// THRESH_BINARY, 15, -2);

		} catch (Exception e) {
			// if any error occurs
			e.printStackTrace();
		}
		return bw;
	}

	public Mat fusion(Mat original, Mat mat) {
		Assert.notNull(original, "'original' shouldn't be null");
		Assert.notNull(mat, "'mat' shouldn't be null");
		
		Mat result = new Mat();
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		original.copyTo(result);

		Core.bitwise_and(original, mat, result);
		
//		for (int y = 0; y < result.height(); y++) {
//			for (int x = 0; x < result.width(); x++) {
//				if (mat.get(y, x)[0] == 0.0)
//					result.put(y, x, 0.0);
//			}
//		}

		return result;
	}

	public boolean isBW (Mat mat) {
		Assert.notNull (mat, "'mat' shouldn't be null");
		Mat gray = new Mat ();
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
		Mat bw = imageThresholding(gray);
		
		//double comp = compareImages(gray, bw);
		
		if (compareImages(gray, bw)) {
			//System.out.println("BW");
			return true;
		}	else {
			//System.out.println("not BW");
			return false;
		}
		
		/*
		for (int y = 0; y < mat.height(); y++) {
			for (int x = 0; x < mat.width(); x++) {
				if (gray.get(y, x)[0] != 0.0 && gray.get(y, x)[0] != 255.0) {
					return false;
				}
			}
		}

		return true; 
		*/
	}
	
	
	static boolean compareImages(Mat mat1, Mat mat2) {
		Mat sub = new Mat();
		Core.subtract(mat1, mat2, sub);
		Scalar ssub= Core.sumElems(sub);
	    return Core.sumElems(sub).val[0] == 0;
	}

	public boolean isEmptyBox(Mat mat, int frame) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		Assert.isTrue(frame >= 0 && frame < 51, "frame should be between [0;50]");
		Mat tmp = new Mat();
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		Imgproc.cvtColor(mat, tmp, CvType.CV_8UC1);
		tmp = killHermits(tmp);

		for (int y = frame; y < mat.height() - frame; y++) {
			for (int x = frame; x < mat.width() - frame; x++) {
				if (tmp.get(y, x)[0] == 0.0) {
					return false;
				}
			}
		}

		return true;
	}

	public Mat rotate(Mat mat) {
		Assert.notNull(mat, "'mat' shouldn't be null");
		int cx = mat.width() / 2;
		int cy = mat.height() / 2;
		double theta = Math.toRadians(90);
		Mat result = new Mat(mat.width(), mat.height(), CvType.CV_8UC1, new Scalar(255.0));
		//log.info(new Object(){}.getClass().getEnclosingMethod().getName());
		// x' = cx + (x-cx) * Cos(theta) - (y-cy) * Sin(theta)
		// y' = cy + (x-cx) * Sin(theta) + (y-cy) * Cos(theta)

		for (int y = 0; y < mat.height(); y++) {
			for (int x = 0; x < mat.width(); x++) {
				int xp = (int) Math.round(cy + (x - cx) * Math.cos(theta) - (y - cy) * Math.sin(theta));
				int yp = (int) Math.round(cx + (x - cx) * Math.sin(theta) + (y - cy) * Math.cos(theta));

				result.put(yp, xp, mat.get(y, x)[0]);
			}
		}

		return result;
	}

}
