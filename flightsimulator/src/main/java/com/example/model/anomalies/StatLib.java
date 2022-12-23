package com.example.model.anomalies;

public class StatLib {

	public static Point[] createPointsArray(float[] x, float[] y) {
		Point ps[] = new Point[x.length];
		for (int i = 0; i < x.length; i++)
			ps[i] = new Point(x[i], y[i]);
		return ps;
	}

	public static float calculateMue(float[] x) {
		float mue = 0;
		float sum = 0;
		for (float f : x) {
			sum += f;
		}
		int count = x.length;
		mue = (1 / (float) count) * sum;
		return mue;
	}

	public static float avg(float[] x) {
		float sum = 0f;
		int count = 0;
		for (float f : x) {
			sum += f;
			count += 1;
		}
		return sum / count;
	}

	// returns the variance of X and Y
	public static float var(float[] x) {
		float square_sum = 0f;
		for (float f : x) {
			square_sum += f * f;
		}

		float mue = calculateMue(x);
		float var = ((1 / (float) x.length) * square_sum) - (mue * mue);
		return var;
	}

	// returns the covariance of X and Y
	public static float cov(float[] x, float[] y) {
		float avg_x, avg_y, diff_x, diff_y;
		avg_x = avg(x);
		avg_y = avg(y);

		float multiplied_diff = 0f;
		for (int i = 0; i < x.length; i++) {
			diff_x = x[i] - avg_x;
			diff_y = y[i] - avg_y;
			multiplied_diff += diff_x * diff_y;
		}
		return multiplied_diff / ((float) x.length);
	}

	// returns the Pearson correlation coefficient of X and Y
	public static float pearson(float[] x, float[] y) {
		float deviation_x, deviation_y, pearson;
		deviation_x = (float) Math.sqrt(var(x));
		deviation_y = (float) Math.sqrt(var(y));

		pearson = cov(x, y) / (deviation_x * deviation_y);
		return pearson;
	}

	// performs a linear regression and returns the line equation
	public static Line linear_reg(Point[] points) {

		// y = ax+b
		float a, b;
		int len = points.length;
		float x[] = new float[len];
		float y[] = new float[len];

		for (int i = 0; i < len; i++) {
			x[i] = points[i].x;
			y[i] = points[i].y;
		}

		a = cov(x, y) / var(x);
		b = avg(y) - (a * avg(x));

		Line line = new Line(a, b);
		return line;
	}

	// returns the deviation between point p and the line equation of the points
	public static float dev(Point p, Point[] points) {
		Line l1 = linear_reg(points);
		return dev(p, l1);
	}

	// returns the deviation between point p and the line
	public static float dev(Point point, Line line) {
		float func_at_p = line.f(point.x);
		return Math.abs(point.y - func_at_p);
	}

}
