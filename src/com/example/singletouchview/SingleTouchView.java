package com.example.singletouchview;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 单手对图片进行缩放，旋转，平移操作，详情请查看
 * 
 * @blog http://blog.csdn.net/xiaanming/article/details/42833893
 * 
 * @author xiaanming
 *
 */
public class SingleTouchView extends FrameLayout {
	/**
	 * 图片的最大缩放比例
	 */
	public static final float MAX_SCALE = 10.0f;

	/**
	 * 图片的最小缩放比例
	 */
	public static final float MIN_SCALE = 0.3f;

	/**
	 * 控制缩放，旋转图标所在四个点得位置
	 */
	public static final int LEFT_TOP = 0;
	public static final int RIGHT_TOP = 1;
	public static final int RIGHT_BOTTOM = 2;
	public static final int LEFT_BOTTOM = 3;

	/**
	 * 一些默认的常量
	 */
	public static final int DEFAULT_FRAME_PADDING = 8;
	public static final int DEFAULT_FRAME_WIDTH = 2;
	public static final int DEFAULT_FRAME_COLOR = Color.WHITE;
	public static final float DEFAULT_SCALE = 1.0f;
	public static final float DEFAULT_DEGREE = 0;
	public static final int DEFAULT_CONTROL_LOCATION = RIGHT_TOP;
	public static final boolean DEFAULT_EDITABLE = false;

	/**
	 * 图片的旋转角度
	 */
	private float mDegree = DEFAULT_DEGREE;

	/**
	 * 图片的缩放比例
	 */
	private float mScale = DEFAULT_SCALE;

	/**
	 * 图片四个点坐标
	 */
	private Point mLTPoint;
	private Point mRTPoint;
	private Point mRBPoint;
	private Point mLBPoint;
	/**
	 * 编辑区
	 */
	private Rect editAbleRect = new Rect();

	/**
	 * 用于缩放，旋转的控制点的坐标
	 */
	private Point mControlPoint = new Point();

	/**
	 * 用于缩放，旋转的图标
	 */
	private Drawable controlDrawable;

	/**
	 * 缩放，旋转图标的宽和高
	 */
	private int mDrawableWidth, mDrawableHeight;
	/**
	 * 缩放，旋转图标
	 */
	private Rect mDrawableRect;

	/**
	 * 画外围框的Path
	 */
	private Path mPath = new Path();

	/**
	 * 画外围框的画笔
	 */
	private Paint mPaint;

	/**
	 * 初始状态
	 */
	public static final int STATUS_INIT = 0;

	/**
	 * 拖动状态
	 */
	public static final int STATUS_DRAG = 1;

	/**
	 * 旋转或者放大状态
	 */
	public static final int STATUS_ROTATE_ZOOM = 2;

	/**
	 * 当前所处的状态
	 */
	private int mStatus = STATUS_INIT;

	/**
	 * 外边框与图片之间的间距, 单位是dip
	 */
	private int framePadding = DEFAULT_FRAME_PADDING;

	/**
	 * 外边框颜色
	 */
	private int frameColor = DEFAULT_FRAME_COLOR;

	/**
	 * 外边框线条粗细, 单位是 dip
	 */
	private int frameWidth = DEFAULT_FRAME_WIDTH;

	private DisplayMetrics metrics;

	private PointF mPreMovePointF = new PointF();
	private PointF mCurMovePointF = new PointF();

	/**
	 * 控制图标所在的位置（比如左上，右上，左下，右下）
	 */
	private int controlLocation = DEFAULT_CONTROL_LOCATION;

	/**
	 * 是否处于可以缩放，平移，旋转状态
	 */
	private boolean isEditable = DEFAULT_EDITABLE;

	public SingleTouchView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SingleTouchView(Context context) {
		this(context, null);
	}

	public SingleTouchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		obtainStyledAttributes(attrs);
		init();
	}

	/**
	 * 获取自定义属性
	 * 
	 * @param attrs
	 */
	private void obtainStyledAttributes(AttributeSet attrs) {
		metrics = getContext().getResources().getDisplayMetrics();
		framePadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_FRAME_PADDING, metrics);
		frameWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_FRAME_WIDTH, metrics);

		TypedArray mTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SingleTouchView);
		framePadding = mTypedArray.getDimensionPixelSize(R.styleable.SingleTouchView_framePadding, framePadding);
		frameWidth = mTypedArray.getDimensionPixelSize(R.styleable.SingleTouchView_frameWidth, frameWidth);
		frameColor = mTypedArray.getColor(R.styleable.SingleTouchView_frameColor, DEFAULT_FRAME_COLOR);
		controlDrawable = mTypedArray.getDrawable(R.styleable.SingleTouchView_controlDrawable);
		controlLocation = mTypedArray.getInt(R.styleable.SingleTouchView_controlLocation, DEFAULT_CONTROL_LOCATION);

		mScale = mTypedArray.getFloat(R.styleable.SingleTouchView_scale, DEFAULT_SCALE);
		mDegree = mTypedArray.getFloat(R.styleable.SingleTouchView_degree, DEFAULT_DEGREE);
		isEditable = mTypedArray.getBoolean(R.styleable.SingleTouchView_editable, DEFAULT_EDITABLE);

		mTypedArray.recycle();

	}

	private void init() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(frameColor);
		mPaint.setStrokeWidth(frameWidth);
		mPaint.setStyle(Style.STROKE);

		if (controlDrawable == null) {
			controlDrawable = getContext().getResources().getDrawable(R.drawable.st_rotate_icon);
		}

		mDrawableWidth = controlDrawable.getIntrinsicWidth();
		mDrawableHeight = controlDrawable.getIntrinsicHeight();
		mDrawableRect = new Rect(0, 0, mDrawableWidth, mDrawableHeight);
	}

	public void draw(Canvas canvas) {
		super.draw(canvas);
		// 处于可编辑状态才画边框和控制图标
		mPaint.setColor(0xFF009988);
		if (isEditable) {
			canvas.drawPath(mPath, mPaint);
			// 画旋转, 缩放图标
			controlDrawable.setBounds(mControlPoint.x - mDrawableWidth / 2, mControlPoint.y - mDrawableHeight / 2,
					mControlPoint.x + mDrawableWidth / 2, mControlPoint.y + mDrawableHeight / 2);
			controlDrawable.draw(canvas);

			RectF postRectF = new RectF(item.getPostRect());
			postRectF.inset(-framePadding, -framePadding);
			mPaint.setColor(0xFF00FF00);
			canvas.drawRect(postRectF, mPaint);

			mPaint.setColor(0xFF00ff00);
			canvas.drawRect(editAbleRect, mPaint);

			mPaint.setColor(0xFF00ffFF);
			canvas.drawRect(mDrawableRect, mPaint);

		}
	}

	/**
	 * 设置Matrix, 强制刷新
	 */
	private void transformDraw() {
		if (item != null) {
			RectF postRectF = new RectF(item.getPostRect());
			postRectF.inset(-framePadding, -framePadding);
			computeRect((int) postRectF.left, (int) postRectF.top, (int) postRectF.right, (int) postRectF.bottom,
					mDegree);
			editAbleRect.set(getMinValue(mLTPoint.x, mRTPoint.x, mRBPoint.x, mLBPoint.x),
					getMinValue(mLTPoint.y, mRTPoint.y, mRBPoint.y, mLBPoint.y),
					getMaxValue(mLTPoint.x, mRTPoint.x, mRBPoint.x, mLBPoint.x),
					getMaxValue(mLTPoint.y, mRTPoint.y, mRBPoint.y, mLBPoint.y));
			mDrawableRect.offsetTo(mControlPoint.x - mDrawableRect.width() / 2,
					mControlPoint.y - mDrawableRect.height() / 2);
			editAbleRect.union(mDrawableRect);
			mPath.reset();
			mPath.moveTo(mLTPoint.x, mLTPoint.y);
			mPath.lineTo(mRTPoint.x, mRTPoint.y);
			mPath.lineTo(mRBPoint.x, mRBPoint.y);
			mPath.lineTo(mLBPoint.x, mLBPoint.y);
			mPath.close();
		}

		invalidate();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (item != null) {
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				if (editAbleRect.contains((int) ev.getX(), (int) ev.getY())) {
					return true;
				}
			}
		}

		return super.onInterceptTouchEvent(ev);
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (!isEditable) {
			return super.onTouchEvent(event);
		}
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mPreMovePointF.set(x + item.getLeft(), y + item.getTop());
			mStatus = JudgeStatus(x, y);

			if (!editAbleRect.contains((int) x, (int) y)) {
				isEditable = false;
				return super.onTouchEvent(event);
			}
			break;
		case MotionEvent.ACTION_UP:
			mStatus = STATUS_INIT;

			break;
		case MotionEvent.ACTION_MOVE:
			mCurMovePointF.set(x + item.getLeft(), y + item.getTop());
			if (mStatus == STATUS_ROTATE_ZOOM) {
				float scale = 1f;

				int halfBitmapWidth = item.getWidth() / 2;
				int halfBitmapHeight = item.getHeight() / 2;

				// 图片某个点到图片中心的距离
				float bitmapToCenterDistance = FloatMath
						.sqrt(halfBitmapWidth * halfBitmapWidth + halfBitmapHeight * halfBitmapHeight);

				// 移动的点到图片中心的距离
				float moveToCenterDistance = distance4PointF(item.getCenter(), mCurMovePointF);

				// 计算缩放比例
				scale = moveToCenterDistance / bitmapToCenterDistance;

				// 缩放比例的界限判断
				if (scale <= MIN_SCALE) {
					scale = MIN_SCALE;
				} else if (scale >= MAX_SCALE) {
					scale = MAX_SCALE;
				}

				// 角度
				double a = distance4PointF(item.getCenter(), mPreMovePointF);
				double b = distance4PointF(mPreMovePointF, mCurMovePointF);
				double c = distance4PointF(item.getCenter(), mCurMovePointF);

				double cosb = (a * a + c * c - b * b) / (2 * a * c);

				if (cosb >= 1) {
					cosb = 1f;
				}

				double radian = Math.acos(cosb);
				float newDegree = (float) radianToDegree(radian);

				// center -> proMove的向量， 我们使用PointF来实现
				PointF centerToProMove = new PointF((mPreMovePointF.x - item.getCenter().x),
						(mPreMovePointF.y - item.getCenter().y));

				// center -> curMove 的向量
				PointF centerToCurMove = new PointF((mCurMovePointF.x - item.getCenter().x),
						(mCurMovePointF.y - item.getCenter().y));

				// 向量叉乘结果, 如果结果为负数， 表示为逆时针， 结果为正数表示顺时针
				float result = centerToProMove.x * centerToCurMove.y - centerToProMove.y * centerToCurMove.x;

				if (result < 0) {
					newDegree = -newDegree;
				}

				mDegree = mDegree + newDegree;
				mScale = scale;

				transformDraw();

				if (item != null) {
					item.onScaleRatasion(scale, scale, mDegree);
				}
			} else if (mStatus == STATUS_DRAG) {
				transformDraw();
				if (item != null) {
					item.onMove(mCurMovePointF.x - mPreMovePointF.x, mCurMovePointF.y - mPreMovePointF.y);
				}
			}

			mPreMovePointF.set(mCurMovePointF);
			break;
		}
		return true;
	}

	/**
	 * 获取四个点和View的大小
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param degree
	 */
	private void computeRect(int left, int top, int right, int bottom, float degree) {
		Point lt = new Point(left, top);
		Point rt = new Point(right, top);
		Point rb = new Point(right, bottom);
		Point lb = new Point(left, bottom);
		Point cp = new Point((left + right) / 2, (top + bottom) / 2);
		mLTPoint = obtainRoationPoint(cp, lt, degree);
		mRTPoint = obtainRoationPoint(cp, rt, degree);
		mRBPoint = obtainRoationPoint(cp, rb, degree);
		mLBPoint = obtainRoationPoint(cp, lb, degree);
		mControlPoint = LocationToPoint(controlLocation);
	}

	/**
	 * 根据位置判断控制图标处于那个点
	 * 
	 * @return
	 */
	private Point LocationToPoint(int location) {
		switch (location) {
		case LEFT_TOP:
			return mLTPoint;
		case RIGHT_TOP:
			return mRTPoint;
		case RIGHT_BOTTOM:
			return mRBPoint;
		case LEFT_BOTTOM:
			return mLBPoint;
		}
		return mLTPoint;
	}

	/**
	 * 获取变长参数最大的值
	 * 
	 * @param array
	 * @return
	 */
	public int getMaxValue(Integer... array) {
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
	public int getMinValue(Integer... array) {
		List<Integer> list = Arrays.asList(array);
		Collections.sort(list);
		return list.get(0);
	}

	/**
	 * 获取旋转某个角度之后的点
	 * 
	 * @param viewCenter
	 * @param source
	 * @param degree
	 * @return
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
	 * 根据点击的位置判断是否点中控制旋转，缩放的图片， 初略的计算
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int JudgeStatus(float x, float y) {
		PointF touchPoint = new PointF(x, y);
		PointF controlPointF = new PointF(mControlPoint);

		// 点击的点到控制旋转，缩放点的距离
		float distanceToControl = distance4PointF(touchPoint, controlPointF);

		// 如果两者之间的距离小于 控制图标的宽度，高度的最小值，则认为点中了控制图标
		if (distanceToControl < Math.min(mDrawableWidth / 2, mDrawableHeight / 2)) {
			return STATUS_ROTATE_ZOOM;
		}

		return STATUS_DRAG;

	}

	public float getImageDegree() {
		return mDegree;
	}

	/**
	 * 设置图片旋转角度
	 * 
	 * @param degree
	 */
	public void setImageDegree(float degree) {
		if (this.mDegree != degree) {
			this.mDegree = degree;
			transformDraw();
		}
	}

	public float getImageScale() {
		return mScale;
	}

	/**
	 * 设置图片缩放比例
	 * 
	 * @param scale
	 */
	public void setImageScale(float scale) {
		if (this.mScale != scale) {
			this.mScale = scale;
			transformDraw();
		}
		;
	}

	public Drawable getControlDrawable() {
		return controlDrawable;
	}

	/**
	 * 设置控制图标
	 * 
	 * @param drawable
	 */
	public void setControlDrawable(Drawable drawable) {
		this.controlDrawable = drawable;
		mDrawableWidth = drawable.getIntrinsicWidth();
		mDrawableHeight = drawable.getIntrinsicHeight();
		mDrawableRect.set(0, 0, mDrawableWidth, mDrawableHeight);
		transformDraw();
	}

	public int getFramePadding() {
		return framePadding;
	}

	public void setFramePadding(int framePadding) {
		if (this.framePadding == framePadding)
			return;
		this.framePadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, framePadding, metrics);
		transformDraw();
	}

	public int getFrameColor() {
		return frameColor;
	}

	public void setFrameColor(int frameColor) {
		if (this.frameColor == frameColor)
			return;
		this.frameColor = frameColor;
		mPaint.setColor(frameColor);
		invalidate();
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public void setFrameWidth(int frameWidth) {
		if (this.frameWidth == frameWidth)
			return;
		this.frameWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, frameWidth, metrics);
		mPaint.setStrokeWidth(frameWidth);
		invalidate();
	}

	/**
	 * 设置控制图标的位置, 设置的值只能选择LEFT_TOP ，RIGHT_TOP， RIGHT_BOTTOM，LEFT_BOTTOM
	 * 
	 * @param controlLocation
	 */
	public void setControlLocation(int location) {
		if (this.controlLocation == location)
			return;
		this.controlLocation = location;
		transformDraw();
	}

	public int getControlLocation() {
		return controlLocation;
	}

	public boolean isEditable() {
		return isEditable;
	}

	/**
	 * 设置是否处于可缩放，平移，旋转状态
	 * 
	 * @param isEditable
	 */
	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
		invalidate();
	}

	/**
	 * 两个点之间的距离
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private float distance4PointF(PointF pf1, PointF pf2) {
		float disX = pf2.x - pf1.x;
		float disY = pf2.y - pf1.y;
		return FloatMath.sqrt(disX * disX + disY * disY);
	}

	@SuppressWarnings("serial")
	public static class NotSupportedException extends RuntimeException {
		private static final long serialVersionUID = 1674773263868453754L;

		public NotSupportedException() {
			super();
		}

		public NotSupportedException(String detailMessage) {
			super(detailMessage);
		}

	}

	public void setItem(SigleTouchItem item) {
		this.item = item;
		isEditable = item != null;
		if (item != null) {
			RectF postRect = item.getPostRect();
			mControlPoint.x = (int) postRect.centerX();
			mControlPoint.y = (int) postRect.centerY();
		}
		transformDraw();
	}

	/**
	 * 用于旋转缩放的Bitmap
	 */
	private SigleTouchItem item;

}

class SigleTouchItem {

	View view;
	float translationX, translationY;

	public SigleTouchItem(View view) {
		this.view = view;
	}

	public void onMove(float dx, float dy) {
		translationX += dx;
		translationY += dy;
		getCenter();
		centerPoint.x += dx;
		centerPoint.y += dy;
		onTranlate(translationX, translationY);
	}

	private void onTranlate(float translationX, float translationY) {
		view.setTranslationX(translationX);
		view.setTranslationY(translationY);
	}

	public void onScaleRatasion(float scaleX, float scaleY, float mDegree) {
		view.setScaleX(scaleX);
		view.setScaleY(scaleY);
		view.setRotation(mDegree);
	}

	public int getWidth() {
		return view.getWidth();
	}

	public int getHeight() {
		return view.getHeight();
	}

	public int getLeft() {
		return view.getLeft();
	}

	public int getTop() {
		return view.getTop();
	}

	public int getRight() {
		return view.getRight();
	}

	public int getBottom() {
		return view.getBottom();
	}

	/**
	 * SingleTouchView的中心点坐标，相对于其父类布局而言的
	 */
	PointF centerPoint;

	public PointF getCenter() {
		if (centerPoint == null) {
			centerPoint = new PointF((view.getLeft() + view.getRight()) >> 1, (view.getTop() + view.getBottom()) >> 1);
		}
		return centerPoint;
	}

	public float getTranlateX() {
		return view.getTranslationX();
	}

	public float getTranlateY() {
		return view.getTranslationY();
	}

	private RectF postRect = new RectF();

	public RectF getPostRect() {
		postRect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
		postRect.offset(view.getTranslationX(), view.getTranslationY());
		postRect.inset(-(view.getScaleX() - 1) * view.getWidth() / 2, -(view.getScaleY() - 1) * view.getHeight() / 2);
		return postRect;
	}

}