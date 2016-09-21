package com.example.singletouchview;

import java.util.ArrayList;
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
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import wu.a.utils.MathUtils;

/**
 * 单手对图片进行缩放，旋转，平移操作，详情请查看
 * 
 * @blog http://blog.csdn.net/xiaanming/article/details/42833893
 * 
 * @author junxu.wang
 *
 */
public class SingleTouchView extends FrameLayout {
	private boolean isDebug = true;
	/**
	 * 图片的最大缩放比例
	 */
	public static final float MAX_SCALE = 4.0f;

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
	public static final int DEFAULT_CONTROL_LOCATION = RIGHT_BOTTOM;
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
	 * 用于缩放，旋转的控制点的坐标
	 */
	private Point mControlPoint = new Point();

	/**
	 * 用于关闭的控制点的坐标
	 */
	private Point mClosePoint = new Point();

	/**
	 * 用于设置的控制点的坐标
	 */
	private Point mSettingPoint = new Point();

	/**
	 * 用于缩放，旋转的图标
	 */
	private Drawable controlDrawable;
	private Drawable settingDrawable;
	private Drawable closeDrawable;

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

	public static final int STATUS_CLOSE_PRESSED = 3;

	/**
	 * 设置状态
	 */
	public static final int STATUS_SETTING = 4;

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

	private PointF mDownPointF = new PointF();
	private PointF mPreMovePointF = new PointF();
	private PointF mCurMovePointF = new PointF();
	private float offScaleDis, originScaleDis;

	/**
	 * 控制图标所在的位置（比如左上，右上，左下，右下）
	 */
	private int controlLocation = DEFAULT_CONTROL_LOCATION;

	/**
	 * 关闭图标所在的位置（比如左上，右上，左下，右下）
	 */
	private int closeLocation = LEFT_TOP;

	/**
	 * 设置图标所在的位置（比如左上，右上，左下，右下）
	 */
	private int settingLocation = RIGHT_TOP;

	/**
	 * 是否处于可以缩放，平移，旋转状态
	 */
	private boolean isEditable = DEFAULT_EDITABLE;

	/**
	 * 用于旋转缩放的Bitmap
	 */
	private SigleTouchItem item;

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
			controlDrawable = getContext().getResources().getDrawable(R.drawable.frag_pic_rotate_icon);
			settingDrawable = getContext().getResources().getDrawable(R.drawable.st_rotate_icon);
			closeDrawable = getContext().getResources().getDrawable(R.drawable.frag_pic_remove_icon);
		}

		mDrawableWidth = controlDrawable.getIntrinsicWidth();
		mDrawableHeight = controlDrawable.getIntrinsicHeight();
		mDrawableRect = new Rect(0, 0, mDrawableWidth, mDrawableHeight);

		gestureScanner = new GestureDetector(mOnGestureListener);
		gestureScanner.setOnDoubleTapListener(mOnDoubleTapListener);
	}

	public void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (isEditable) {

			// 处于可编辑状态才画边框和控制图标
			mPaint.setColor(0xFF009988);
			canvas.drawPath(mPath, mPaint);
			
			// 画旋转, 缩放图标
			controlDrawable.setBounds(mControlPoint.x - mDrawableWidth / 2, mControlPoint.y - mDrawableHeight / 2,
					mControlPoint.x + mDrawableWidth / 2, mControlPoint.y + mDrawableHeight / 2);
			controlDrawable.draw(canvas);

			// 画关闭图标
			closeDrawable.setBounds(mClosePoint.x - mDrawableWidth / 2, mClosePoint.y - mDrawableHeight / 2,
					mClosePoint.x + mDrawableWidth / 2, mClosePoint.y + mDrawableHeight / 2);
			closeDrawable.draw(canvas);

			// 画设置图标
			// settingDrawable.setBounds(mSettingPoint.x - mDrawableWidth / 2,
			// mSettingPoint.y - mDrawableHeight / 2,
			// mSettingPoint.x + mDrawableWidth / 2, mSettingPoint.y +
			// mDrawableHeight / 2);
			// settingDrawable.draw(canvas);

			if (isDebug) {

				// item 面积
				RectF postRectF = new RectF(item.getPostRect());
				mPaint.setColor(0xFF00FF00);
				canvas.drawRect(postRectF, mPaint);

				// 操作柄区域
				mPaint.setColor(0xFF00ffFF);
				canvas.drawRect(mDrawableRect, mPaint);

				// 图形矩形区域
				mPaint.setColor(0xFF888888);
				canvas.drawRect(item.getPostArea(), mPaint);

				// 图形矩形区域
				mPaint.setColor(0xFF000000);
				canvas.drawCircle(item.getCenter().x, item.getCenter().y, 10, mPaint);
			}
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
			mDrawableRect.offsetTo(mControlPoint.x - mDrawableRect.width() / 2,
					mControlPoint.y - mDrawableRect.height() / 2);
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
		boolean result = false;
		if (item != null) {
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				int mStatus = JudgeStatus(ev.getX(), ev.getY());
				if (mStatus == STATUS_ROTATE_ZOOM && mStatus == STATUS_CLOSE_PRESSED) {
					result = true;
				}
			}
		}
		Logger.l("onInterceptTouchEvent   " + result);
		if (result) {
			return result;
		} else {
			setFoucs(false);
			return super.onInterceptTouchEvent(ev);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent(ev);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	public boolean onTouchEvent(MotionEvent event) {
		if (item == null) {
			return super.onTouchEvent(event);
		}

		float x = event.getX();
		float y = event.getY();
		mCurMovePointF.set(x, y);

		gestureScanner.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mStatus = JudgeStatus(event.getX(), event.getY());
			if (mStatus == STATUS_INIT) {
				if (item != null) {
					setItem(null);
				}
				return super.onTouchEvent(event);
			}

			mDownPointF.set(x, y);
			mPreMovePointF.set(x, y);

			if (mStatus == STATUS_ROTATE_ZOOM) {
				int halfBitmapWidth = item.getWidth() / 2;
				int halfBitmapHeight = item.getHeight() / 2;

				// 图片某个点到图片中心的距离
				originScaleDis = (float) Math
						.sqrt(halfBitmapWidth * halfBitmapWidth + halfBitmapHeight * halfBitmapHeight);

				offScaleDis = distance4PointF(item.getCenter(), mPreMovePointF) - distance4PointF(item.getCenter(),
						new PointF(item.getPostRect().right, item.getPostRect().bottom));
			}
			break;
		case MotionEvent.ACTION_UP:
			float moveDis = distance4PointF(mDownPointF, mCurMovePointF);
			if (moveDis < 10) {
				switch (mStatus) {
				case STATUS_SETTING:
					if (item != null) {
						item.onSetting();
					}
					break;
				case STATUS_CLOSE_PRESSED:
					if (item != null) {
						item.onClosed();
					}
					break;
				}
			}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			mStatus = STATUS_INIT;

			break;
		case MotionEvent.ACTION_MOVE:
			if (mStatus == STATUS_ROTATE_ZOOM) {
				float scale = 1f;
				// 移动的点到图片中心的距离
				float moveToCenterDistance = distance4PointF(item.getCenter(), mCurMovePointF) - offScaleDis;
				// 计算缩放比例
				scale = moveToCenterDistance / originScaleDis;
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
				float newDegree = (float) MathUtils.radianToDegree(radian);

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
					item.onScaleRotation(scale, scale, mDegree);
				}
			} else if (mStatus == STATUS_DRAG) {
				onMove(mCurMovePointF.x - mPreMovePointF.x, mCurMovePointF.y - mPreMovePointF.y);
			}

			mPreMovePointF.set(mCurMovePointF);
			break;
		}
		return true;
	}

	public void onMove(float dx, float dy) {
		transformDraw();
		if (item != null) {
			item.onMove(dx, dy);
		}
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
		mLTPoint = MathUtils.obtainRoationPoint(cp, lt, degree);
		mRTPoint = MathUtils.obtainRoationPoint(cp, rt, degree);
		mRBPoint = MathUtils.obtainRoationPoint(cp, rb, degree);
		mLBPoint = MathUtils.obtainRoationPoint(cp, lb, degree);
		mControlPoint = LocationToPoint(controlLocation);
		mClosePoint = LocationToPoint(closeLocation);
		mSettingPoint = LocationToPoint(settingLocation);
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
		if (distanceToControl < Math.min(mDrawableWidth, mDrawableHeight) / 2) {
			return STATUS_ROTATE_ZOOM;
		} else if (isInSircleAear(mClosePoint, Math.min(mDrawableWidth, mDrawableHeight) / 2, x, y)) {
			return STATUS_CLOSE_PRESSED;
		} /*
			 * else if (isInSircleAear(mSettingPoint, Math.min(mDrawableWidth,
			 * mDrawableHeight) / 2, x, y)) { return STATUS_SETTING; }
			 */
		else if (isFoucs() && item.getPostArea().contains((int) x, (int) y)) {
			return STATUS_DRAG;
		}
		return STATUS_INIT;

	}

	/**
	 * 圆心center半径r是否包含x,y
	 *
	 * @param center
	 * @param r
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isInSircleAear(Point center, float r, float x, float y) {
		float distanceToControl = distance4PointF(center.x, center.y, x, y);
		return distanceToControl < r;
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
	 * @return
	 */
	private float distance4PointF(PointF pf1, PointF pf2) {
		float disX = pf2.x - pf1.x;
		float disY = pf2.y - pf1.y;
		return (float) Math.sqrt(disX * disX + disY * disY);
	}

	/**
	 * 两个点之间的距离
	 *
	 * @param x1
	 * @param y1
	 * @return
	 */
	private float distance4PointF(float x, float y, float x1, float y1) {
		float disX = x1 - x;
		float disY = y1 - y;
		return (float) Math.sqrt(disX * disX + disY * disY);
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

	private boolean isFoucs;

	public boolean isFoucs() {
		return isFoucs;
	}

	public void setFoucs(boolean f) {
		this.isFoucs = f;
	}

	public void setItem(SigleTouchItem item) {
		if (this.item == item) {
			return;
		}
		if (this.item != null) {
			this.item.onUnbind();
		}
		this.item = item;
		isEditable = item != null;
		if (item != null) {
			if (!item.isPrepared()) {
				item.prepare();
			}
			RectF postRect = item.getPostRect();
			mControlPoint.x = (int) postRect.centerX();
			mControlPoint.y = (int) postRect.centerY();
			mScale = item.getScale();
			mDegree = item.getRotation();
		}
		transformDraw();
	}

	public SigleTouchItem getItem() {
		return item;
	}

	private List<SigleTouchItem> views = new ArrayList<SigleTouchItem>();

	public List<SigleTouchItem> getItems() {
		return views;
	}

	public boolean add(View v, OnStateChangedListener onStateChangedListener) {
		SigleTouchItem item = new SigleTouchItem(this, v, onStateChangedListener);
		return views.add(item);
	}

	public boolean remove(View v) {
		for (int i = 0, end = views.size(); i < end; i++) {
			SigleTouchItem item = views.get(i);
			if (item.getView() == v) {
				item.destroy();
				views.remove(i);
				if (getItem() == item) {
					setItem(null);
				}
				return true;
			}
		}
		return false;
	}

	public boolean removeAll() {
		setItem(null);
		views.clear();
		return true;
	}

	public boolean move(View v, int index) {
		if (views.get(index).getView() != v) {
			for (int i = 0, end = views.size(); i < end; i++) {
				SigleTouchItem item = views.get(i);
				if (item.getView() == v) {
					views.remove(i);
					views.add(index, item);
					return true;
				}
			}
		}
		return false;
	}

	public class SigleTouchItem {
		private SingleTouchView mSingleTouchView;
		private View view;
		private float translationX, translationY;
		private OnStateChangedListener mOnStateChangedListener;

		private boolean isPrepared;

		/**
		 * 缩放后位置大小
		 */
		private RectF postRect = new RectF();
		/**
		 * 旋转后所占区域
		 */
		private RectF rectArea = new RectF();

		/**
		 * 图片四个点坐标
		 */
		private Point mLTPoint;
		private Point mRTPoint;
		private Point mRBPoint;
		private Point mLBPoint;

		public SigleTouchItem(SingleTouchView singleTouchView, View view,
				OnStateChangedListener onStateChangedListener) {
			this.view = view;
			this.mOnStateChangedListener = onStateChangedListener;
			this.mSingleTouchView = singleTouchView;

			view.setOnTouchListener(itemOnTouchListener);
		}

		public void prepare() {
			isPrepared = true;
			postRect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
			postRect.offset(view.getTranslationX(), view.getTranslationY());
			postRect.inset(-(view.getScaleX() - 1) * view.getWidth() / 2,
					-(view.getScaleY() - 1) * view.getHeight() / 2);
			computeRect(postRect, getRotation());
		}

		public boolean isPrepared() {
			return isPrepared;
		}

		public void destroy() {
			view.setOnTouchListener(null);
			view = null;
			mOnStateChangedListener = null;
		}

		public View getView() {
			return view;
		}

		public void onMove(float dx, float dy) {
			translationX += dx;
			translationY += dy;
			view.setTranslationX(translationX);
			view.setTranslationY(translationY);

			postRect.offset(dx, dy);

			rectArea.offset(dx, dy);

		}

		public void onScaleRotation(float scaleX, float scaleY, float mDegree) {
			view.setScaleX(scaleX);
			view.setScaleY(scaleY);
			view.setRotation(mDegree);
			postRect.inset((postRect.width() - view.getScaleX() * view.getWidth()) / 2,
					(postRect.height() - view.getScaleY() * view.getHeight()) / 2);

			computeRect(postRect, mDegree);
		}

		public float getScale() {
			return view.getScaleX();
		}

		public float getRotation() {
			return view.getRotation();
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

		public PointF getCenter() {
			return new PointF(postRect.centerX(), postRect.centerY());
		}

		public float getTranlateX() {
			return view.getTranslationX();
		}

		public float getTranlateY() {
			return view.getTranslationY();
		}

		public RectF getPostRect() {
			return postRect;
		}

		public RectF getPostArea() {
			return rectArea;
		}

		/**
		 * 获取四个点和View的大小
		 *
		 * @param degree
		 */
		private void computeRect(RectF rect, float degree) {
			Point lt = new Point((int) (rect.left), (int) rect.top);
			Point rt = new Point((int) rect.right, (int) rect.top);
			Point rb = new Point((int) rect.right, (int) rect.bottom);
			Point lb = new Point((int) rect.left, (int) rect.bottom);
			Point cp = new Point((int) rect.centerX(), (int) rect.centerY());
			mLTPoint = MathUtils.obtainRoationPoint(cp, lt, degree);
			mRTPoint = MathUtils.obtainRoationPoint(cp, rt, degree);
			mRBPoint = MathUtils.obtainRoationPoint(cp, rb, degree);
			mLBPoint = MathUtils.obtainRoationPoint(cp, lb, degree);
			rectArea.set(MathUtils.getMinValue(mLTPoint.x, mRTPoint.x, mRBPoint.x, mLBPoint.x),
					MathUtils.getMinValue(mLTPoint.y, mRTPoint.y, mRBPoint.y, mLBPoint.y),
					MathUtils.getMaxValue(mLTPoint.x, mRTPoint.x, mRBPoint.x, mLBPoint.x),
					MathUtils.getMaxValue(mLTPoint.y, mRTPoint.y, mRBPoint.y, mLBPoint.y));
			rectArea.offsetTo((rect.centerX() - rectArea.width() / 2), (rect.centerY() - rectArea.height() / 2));
		}

		protected void onDoubleClicked() {
			Logger.l("onDoubleClicked item");
			mSingleTouchView.setItem(this);
			if (mOnStateChangedListener != null) {
				mOnStateChangedListener.onDoubleClick(view);
			}
		}

		protected void onClicked() {
			if (view == null) {
				return;
			}
			prepare();
			mSingleTouchView.setItem(this);
			if (mOnStateChangedListener != null) {
				mOnStateChangedListener.onClicked(view);
			}
		}

		protected void onUnbind() {
			if (mOnStateChangedListener != null) {
				mOnStateChangedListener.onUnbind(view);
			}
		}

		protected void onSetting() {
			if (mOnStateChangedListener != null) {
				mOnStateChangedListener.onSetting(view);
			}
		}

		protected void onClosed() {
			if (mOnStateChangedListener != null) {
				mOnStateChangedListener.onClosed(view);
			}
		}

		private OnTouchListener itemOnTouchListener = new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				view = v;
				// gestureScanner.onTouchEvent(event);
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (!mSingleTouchView.isFoucs()) {
						mSingleTouchView.setFoucs(true);
						mSingleTouchView.setItem(SigleTouchItem.this);
					}
				}
				return false;
			}
		};
	}

	public GestureDetector gestureScanner;
	private OnGestureListener mOnGestureListener = new OnGestureListener() {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}
	};
	private GestureDetector.OnDoubleTapListener mOnDoubleTapListener = new GestureDetector.OnDoubleTapListener() {
		public boolean onDoubleTap(MotionEvent e) {
			Logger.l("onDoubleTap");
			if (isFoucs()) {
				item.onDoubleClicked();
				return true;
			}
			return false;
		}

		public boolean onDoubleTapEvent(MotionEvent e) {
			return false;
		}

		public boolean onSingleTapConfirmed(MotionEvent e) {
			return false;
		}
	};

	public interface OnStateChangedListener {
		void onDoubleClick(View view);

		void onClicked(View view);

		void onUnbind(View view);

		void onSetting(View view);

		void onClosed(View view);
	}
}