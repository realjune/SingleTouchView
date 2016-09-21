package wu.a.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.graphics.Point;

/**
 * @author junxu.wang
 *
 */
public class MathUtils { 

	/**
	 * 弧度换算成角度
	 * 
	 * @return
	 */
	public static double radianToDegree(double radian) {
		return radian * 180 / Math.PI;
	}

	/**
	 * 角度换算成弧度
	 * 
	 * @param degree
	 * @return
	 */
	public static double degreeToRadian(double degree) {
		return degree * Math.PI / 180;
	}

	/**
	 * 坐标选装
	 * 
	 * @param viewCenter
	 *            旋转轴中心点
	 * @param source
	 *            旋转的坐标
	 * @param degree
	 *            旋转角度
	 * @return 旋转后的坐标
	 */
	public static Point obtainRoationPoint(Point center, Point source, float degree) {
		// 两者之间的距离
		Point disPoint = new Point();
		disPoint.x = source.x - center.x;
		disPoint.y = source.y - center.y;

		// 没旋转之前的弧度
		double originRadian = 0;

		// 没旋转之前的角度
		double originDegree = 0;

		// 旋转之后的角度
		double resultDegree = 0;

		// 旋转之后的弧度
		double resultRadian = 0;

		// 经过旋转之后点的坐标
		Point resultPoint = new Point();

		double distance = Math.sqrt(disPoint.x * disPoint.x + disPoint.y * disPoint.y);
		if (disPoint.x == 0 && disPoint.y == 0) {
			return center;
			// 第一象限
		} else if (disPoint.x >= 0 && disPoint.y >= 0) {
			// 计算与x正方向的夹角
			originRadian = Math.asin(disPoint.y / distance);

			// 第二象限
		} else if (disPoint.x < 0 && disPoint.y >= 0) {
			// 计算与x正方向的夹角
			originRadian = Math.asin(Math.abs(disPoint.x) / distance);
			originRadian = originRadian + Math.PI / 2;

			// 第三象限
		} else if (disPoint.x < 0 && disPoint.y < 0) {
			// 计算与x正方向的夹角
			originRadian = Math.asin(Math.abs(disPoint.y) / distance);
			originRadian = originRadian + Math.PI;
		} else if (disPoint.x >= 0 && disPoint.y < 0) {
			// 计算与x正方向的夹角
			originRadian = Math.asin(disPoint.x / distance);
			originRadian = originRadian + Math.PI * 3 / 2;
		}

		// 弧度换算成角度
		originDegree = radianToDegree(originRadian);
		resultDegree = originDegree + degree;

		// 角度转弧度
		resultRadian = degreeToRadian(resultDegree);

		resultPoint.x = (int) Math.round(distance * Math.cos(resultRadian));
		resultPoint.y = (int) Math.round(distance * Math.sin(resultRadian));
		resultPoint.x += center.x;
		resultPoint.y += center.y;

		return resultPoint;
	}
	


	/**
	 * 获取变长参数最大的值
	 * 
	 * @param array
	 * @return
	 */
	public static int getMaxValue(Integer... array) {
		List<Integer> list = Arrays.asList(array);
		Collections.sort(list);
		return list.get(list.size() - 1);
	}

	/**
	 * 获取变长参数最大的值
	 * 
	 * @param array
	 * @return
	 */
	public static int getMinValue(Integer... array) {
		List<Integer> list = Arrays.asList(array);
		Collections.sort(list);
		return list.get(0);
	}
}
