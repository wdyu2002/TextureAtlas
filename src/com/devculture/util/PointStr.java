package com.devculture.util;

import java.awt.Point;

public class PointStr extends Point {
	private static final long serialVersionUID = 1L;

	public PointStr(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public PointStr(Point point) {
		this.x = point.x;
		this.y = point.y;
	}

	public String toString() {
		return "(" + this.x + ", " + this.y + ")";
	}
}