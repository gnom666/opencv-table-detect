package com.taiger.kp.preprocess.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.formula.eval.IntersectionEval;
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

import com.taiger.kp.preprocess.model.Dither2;
import com.taiger.kp.preprocess.model.Line;

import lombok.extern.java.Log;

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
	public static Mat bright(Mat mat, double alpha, double beta) {
		Mat result = new Mat();
		mat.copyTo(result);

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
	public static Mat invert(Mat mat) {
		Mat result = new Mat();
		mat.copyTo(result);

		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				result.put(i, j, (255.0 - result.get(i, j)[0]) >= 0 ? 255.0 - result.get(i, j)[0] : 0.0);
			}
		}

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
	public static Mat tuneWhite(Mat mat, double limit) {
		Mat result = new Mat();
		mat.copyTo(result);

		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				if (result.get(i, j)[0] > limit)
					result.put(i, j, 255.0);
			}
		}

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
	public static Mat tuneWhite(Mat mat, double center, double radio, double value) {
		Mat result = new Mat();
		mat.copyTo(result);

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
	public static Mat bw128(Mat mat) {
		Mat result = new Mat();
		mat.copyTo(result);

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
	public static Mat bwMedia(Mat mat) {
		Mat result = new Mat();
		mat.copyTo(result);
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
	public static Mat bwAve(Mat mat) {
		Mat result = new Mat();
		mat.copyTo(result);
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
	public static Mat bwDither(Mat mat) {
		// System.out.println(mat.height() + " X " + mat.width());
		Dither2 dither = new Dither2();
		Mat result = new Mat();
		mat.copyTo(result);

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
	public static Mat fatter(Mat mat) {
		Mat result = new Mat();
		mat.copyTo(result);
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
	public static Mat plusFatter(Mat mat) {
		Mat result = new Mat();
		mat.copyTo(result);
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
	public static Mat rFatter(Mat mat) {
		Mat result = new Mat();
		mat.copyTo(result);
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
	public static Mat killHermits(Mat mat) {
		Mat result = new Mat();
		mat.copyTo(result);
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
	 * Eliminates one neighbour pixels and lonely pixels
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public static Mat killCouples(Mat mat) {
		Mat result = new Mat();
		mat.copyTo(result);
		boolean left, up, down, right;

		for (int y = 0; y < mat.height(); y++) {
			up = y > 0;
			down = y < mat.height() - 1;
			for (int x = 0; x < mat.width(); x++) {
				left = x > 0;
				right = x < mat.width() - 1;
				if (mat.get(y, x)[0] == 0.0) {
					int neighbours = 0;
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

					if (neighbours <= 1)
						result.put(y, x, 255.0);
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
	public static Mat filler(Mat mat) {
		Mat result = new Mat();
		mat.copyTo(result);
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

					if (neighbours >= 3)
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
	public static Mat bwFloydSteinberg(Mat mat, double limit) {
		Mat result = new Mat();
		mat.copyTo(result);

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
	public static Mat bwFloydSteinbergFL(Mat mat, double limit) {
		Mat result = new Mat();
		mat.copyTo(result);

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
	public static Mat bwAtkinson(Mat mat, double limit) {
		Mat result = new Mat();
		mat.copyTo(result);

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
	public static Mat denoiseColor(Mat mat) {
		Mat result = new Mat();

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
	public static Mat denoise(Mat mat) {
		Mat result = new Mat();

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
		// Calculate histogram
		java.util.List<Mat> matList = new LinkedList<Mat>();
		matList.add(mat);
		Mat histogram = new Mat();
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
		Mat result = new Mat();
		mat.copyTo(result);

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

		Highgui.imwrite("/Users/jorge.rios/Work/histogram_h.jpg", result);
	}

	/**
	 * Calculates histogram
	 * 
	 * @param image
	 *            Image
	 */
	public static void histogram3(Mat mat) {
		Mat result = new Mat();
		mat.copyTo(result);

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

		Highgui.imwrite("/Users/jorge.rios/Work/histogram_v.jpg", result);
	}

	/**
	 * Gets the grayscale image (1 channel) of a color image (3 channels)
	 * 
	 * @param mat
	 *            Image
	 * @return Modified image
	 */
	public static Mat grayscale(Mat mat) {
		Mat tmp = new Mat();
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC1);

		Imgproc.cvtColor(mat, tmp, CvType.CV_8UC1);
		for (int y = 0; y < mat.height(); y++) {
			for (int x = 0; x < mat.width(); x++) {
				result.put(y, x, (tmp.get(y, x)[0] + tmp.get(y, x)[1] + tmp.get(y, x)[2]) / 3);
			}
		}

		return result;
	}

	/**
	 * Invert color image (3 channels)
	 * 
	 * @param mat
	 *            Image
	 * @return Modified Image
	 */
	public static Mat invertColor(Mat mat) {
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC3);

		for (int y = 0; y < mat.height(); y++) {
			for (int x = 0; x < mat.width(); x++) {
				double[] rgb = { 255.0 - mat.get(y, x)[0], 255.0 - mat.get(y, x)[1], 255.0 - mat.get(y, x)[2] };
				result.put(y, x, rgb);
			}
		}

		return result;
	}

	/**
	 * 
	 * @param bw
	 * @return
	 */
	public static Mat getVerticalLines(Mat bw) {
		Mat result = new Mat();
		Mat vertical = bw.clone();

		try {

			int scale = 25; // play with this variable in order to increase/decrease the amount of lines to
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
			Highgui.imwrite("/Users/jorge.rios/Work/dv.jpg", vertical);
			result = vertical.clone();

			// verticalStructure.release();

		} catch (Exception e) {
			log.severe(e.getMessage());
		}

		return result;
	}

	public static Mat getHorizontalLines(Mat mat) {
		Mat result = new Mat();
		Mat horizontal = mat.clone();

		try {

			int scale = 25; // play with this variable in order to increase/decrease the amount of lines to
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
			Highgui.imwrite("/Users/jorge.rios/Work/dh.jpg", horizontal);
			result = horizontal.clone();

			// horizontalStructure.release();

		} catch (Exception e) {
			log.severe(e.getMessage());
		}

		return result;
	}

	public static Mat intersectLines(Mat mat) {
		Mat result = mat.clone();
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

	public static Mat sumLines(Mat mat) {
		Mat result = mat.clone();
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
	
	public static Mat sumLines(Mat v, Mat h) {
		Mat result = new Mat();
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
	
	public static Mat cleanLines(Mat mat) {
		
		Mat result = new Mat();
		mat.copyTo(result);
		Highgui.imwrite("/Users/jorge.rios/Work/tmp.jpg", result);
		boolean found = false;
		
		for (int y = 0; y < result.height() && !found; y++) {
			found = isWhiteH(mat, y);
			for (int x = 0; x < result.width() && !found; x++) {
				result.put(y, x, 0.0);
			}
		}
		Highgui.imwrite("/Users/jorge.rios/Work/tmp1.jpg", result);
		
		found = false;
		for (int y = result.height()-1; y >= 0 && !found; y--) {
			found = isWhiteH(mat, y);
			for (int x = 0; x < result.width() && !found; x++) {
				result.put(y, x, 0.0);
			}
		}
		Highgui.imwrite("/Users/jorge.rios/Work/tmp2.jpg", result);
		
		found = false;
		for (int x = 0; x < result.width() && !found; x++) {
			found = isWhiteV(mat, x);
			for (int y = 0; y < result.height() && !found; y++) {
				result.put(y, x, 0.0);
			}
		}
		Highgui.imwrite("/Users/jorge.rios/Work/tmp3.jpg", result);
		
		found = false;
		for (int x = result.width()-1; x >= 0  && !found; x--) {
			found = isWhiteV(mat, x);
			for (int y = 0; y < result.height() && !found; y++) {
				result.put(y, x, 0.0);
			}
		}
		Highgui.imwrite("/Users/jorge.rios/Work/tmp4.jpg", result);

		return result;
	}
	
	private static boolean isWhiteH(Mat mat, int y) {
		for (int x = 0; x < mat.width(); x++) {
			if (mat.get(y, x)[0] == 0.0) return false;
		}
		return true;
	}
	
	private static boolean isWhiteV(Mat mat, int x) {
		for (int y = 0; y < mat.height(); y++) {
			if (mat.get(y, x)[0] == 0.0) return false;
		}
		return true;
	}
	
	public static Mat lines(Mat mat) {
		int alpha = 3;
		int beta = 5;
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC1);
		
		Mat v = getVerticalLines(mat); 
		Mat h = getHorizontalLines(mat);
		
		Mat vLong = new Mat(mat.height(), mat.width(), CvType.CV_8UC1);
		Mat hLong = new Mat(mat.height(), mat.width(), CvType.CV_8UC1);
		
		List<MatOfPoint> vcontours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(v, vcontours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		List<MatOfPoint> hcontours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(h, hcontours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		
		List<Line> vLines = new LinkedList<>();
		List<Line> hLines = new LinkedList<>();
		
		for (MatOfPoint c : hcontours) {
			int minx = (int)lineMinX(c);
			int maxx = (int)lineMaxX(c);
			int y = (int)lineY(c);
			hLines.add(new Line(y, minx, maxx));
		}
		
		for (MatOfPoint c : vcontours) {
			int miny = (int)lineMinY(c);
			int maxy = (int)lineMaxY(c);
			int x = (int)lineX(c);
			vLines.add(new Line(x, miny, maxy));
		}
		
		List<Line> vFinalLines = new LinkedList<>();
		List<Line> hFinalLines = new LinkedList<>();
		
		for (Line hl : hLines) {
			boolean foundIntersection = false;
			for (int i = 0; i < vLines.size() && !foundIntersection; i++) {
				if (vLines.get(i).getBegin() - beta <= hl.getConstant() &&
					hl.getConstant() <= vLines.get(i).getEnd() + beta &&
					hl.getBegin() - alpha <= vLines.get(i).getConstant() &&
					vLines.get(i).getConstant() <= hl.getEnd() + alpha) {
					//foundIntersection = true;
					hFinalLines.add(hl);
					
					vFinalLines.add(vLines.get(i));
				}
			}
		}
		/*
		for (Line vl : vLines) {
			boolean foundIntersection = false;
			for (int i = 0; i < hLines.size() && !foundIntersection; i++) {
				if (hLines.get(i).getBegin() - alpha <= vl.getConstant() &&
					vl.getConstant() <= hLines.get(i).getEnd() + alpha &&
					vl.getBegin() - alpha <= hLines.get(i).getConstant() &&
					hLines.get(i).getConstant() <= vl.getEnd() + alpha) {
					foundIntersection = true;
					vFinalLines.add(vl);
				}
			}
		}*/
		
		for (Line hl : hFinalLines) {
			int y = hl.getConstant();
			for (int x = 0; x < result.width(); x++) { 
				if (y != -1) result.put(y, x, 255.0); 
				if (y > 0) result.put(y-1, x, 255.0);
				if (y < result.height()-1) result.put(y+1, x, 255.0);
				
				//if (y > 1) result.put(y-2, x, 255.0);
				if (y < result.height()-2) result.put(y+2, x, 255.0);
			}
		}
		
		for (Line vl : vFinalLines) {
			int x = vl.getConstant();
			for (int y = 0; y < result.height(); y++) { 
				if (x != -1) result.put(y, x, 255.0); 
				if (x > 0) result.put(y, x-1, 255.0);
				if (x < result.width()-1) result.put(y, x+1, 255.0);
				
				//if (x > 1) result.put(y, x-2, 255.0);
				if (x < result.width()-2) result.put(y, x+2, 255.0);
			}
		}
		
		Highgui.imwrite("/Users/jorge.rios/Work/lines.jpg", result);
		return result;
	}

	public static Mat vLines(Mat mat, List<Integer> linesX) {
		if (linesX == null) return null;
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC1);
		Mat v = getVerticalLines(mat);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(v, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		int counter = 0;
		for (MatOfPoint c : contours) {
			/*int x1 = (int) c.get(0, 0)[1];
			int y1 = (int) c.get(0, 0)[0];
			int x2 = (int) c.get(c.height() - 1, 0)[1];
			int y2 = (int) c.get(c.height() - 1, 0)[0];*/
			int x = (int)lineX(c); 
			System.out.println(c.dump());
			for (int y = 0; y < result.height(); y++) { 
				if (x != -1) result.put(y, x, 255.0); 
				if (x > 0) result.put(y, x-1, 255.0);
				if (x < result.width()-1) result.put(y, x+1, 255.0);
				linesX.add(x);
			}
		}
		
		Highgui.imwrite("/Users/jorge.rios/Work/vlines.jpg", result);

		return result;
	}

	public static Mat hLines(Mat mat, List<Integer> linesY) {
		if (linesY == null) return null;
		Mat result = new Mat(mat.height(), mat.width(), CvType.CV_8UC1);
		Mat h = getHorizontalLines(mat);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(h, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		int counter = 0;
		for (MatOfPoint c : contours) {
			int minx = (int)lineMinX(c);
			int maxx = (int)lineMaxX(c);
			int y = (int)lineY(c);
			System.out.println(c.dump());
			for (int x = 0; x < result.width(); x++) { 
				if (y != -1) result.put(y, x, 255.0); 
				if (y > 0) result.put(y-1, x, 255.0);
				if (y < result.height()-1) result.put(y+1, x, 255.0);
				linesY.add(y);
			}
		}
		
		Highgui.imwrite("/Users/jorge.rios/Work/hlines.jpg", result);

		return result;
	}

	private static double lineY(MatOfPoint c) {
		double sum = 0.0;
		for (int i = 0; i < c.height(); i++) {
			sum += c.get(i, 0)[1];
		}
		return sum / c.height();
	}
	
	private static double lineX(MatOfPoint c) {
		double sum = 0.0;
		for (int i = 0; i < c.height(); i++) {
			sum += c.get(i, 0)[0];
		}
		return sum / c.height();
	}
	
	private static double lineMinX(MatOfPoint c) {
		if (c == null || c.height() == 0) return 0.0;
		double min = c.get(0, 0)[0];
		for (int i = 1; i < c.height(); i++) {
			if (c.get(i, 0)[0] < min) min = c.get(i, 0)[0];
		}
		return min;
	}
	
	private static double lineMaxX(MatOfPoint c) {
		if (c == null || c.height() == 0) return 0.0;
		double max = c.get(0, 0)[0];
		for (int i = 1; i < c.height(); i++) {
			if (c.get(i, 0)[0] > max) max = c.get(i, 0)[0];
		}
		return max;
	}
	
	private static double lineMinY(MatOfPoint c) {
		if (c == null || c.height() == 0) return 0.0;
		double min = c.get(0, 0)[1];
		for (int i = 1; i < c.height(); i++) {
			if (c.get(i, 0)[1] < min) min = c.get(i, 0)[1];
		}
		return min;
	}
	
	private static double lineMaxY(MatOfPoint c) {
		if (c == null || c.height() == 0) return 0.0;
		double max = c.get(0, 0)[1];
		for (int i = 1; i < c.height(); i++) {
			if (c.get(i, 0)[1] > max) max = c.get(i, 0)[1];
		}
		return max;
	}

	private static double lineY(double x1, double y1, double x2, double y2, double x) {
		if (x1 == x2)
			return -1;
		return (y2 - y1) * (x - x1) / (x2 - x1) + y1;
	}

	private static double lineX(double x1, double y1, double x2, double y2, double y) {
		if (y1 == y2)
			return -1;
		return (x2 - x1) * (y - y1) / (y2 - y1) + x1;
	}

	public static Mat imageThresholding(Mat gray) {

		Mat bw = new Mat();
		try {
			// Apply adaptiveThreshold at the bitwise_not of gray

			Core.bitwise_not(gray, gray);

			Imgproc.adaptiveThreshold(gray, bw, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, -2);
			// saveMat(File.createTempFile("WB_mean_", FILE_SUFFIX, resultDirFile), bw);

			// Imgproc.adaptiveThreshold(gray, bw, 255, ADAPTIVE_THRESH_GAUSSIAN_C,
			// THRESH_BINARY, 15, -2);

		} catch (Exception e) {
			// if any error occurs
			e.printStackTrace();
		}
		return bw;
	}

	public static Mat fusion(Mat original, Mat mat) {
		Mat result = new Mat();
		original.copyTo(result);
		
		for (int y = 0; y < result.height(); y++) {
			for (int x = 0; x < result.width(); x++) {
				if (mat.get(y, x)[0] == 0.0) 
					result.put(y, x, 0.0);
			}
		}
		
		return result;
	}

}
