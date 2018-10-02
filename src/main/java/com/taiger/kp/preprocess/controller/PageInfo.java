package com.taiger.kp.preprocess.controller;

import org.opencv.core.Mat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PageInfo {
	int width;
	int height;
	int number;
	Mat page;
}
