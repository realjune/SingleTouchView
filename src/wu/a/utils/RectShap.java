package wu.a.utils;

import android.graphics.Point;

public class RectShap {
	private float[] points;

	public RectShap() {
	}

	public RectShap(float... points) {
		this.points = points;
	}

	public void set(float... xyPoints) {
		this.points = xyPoints;
	}
	

}
