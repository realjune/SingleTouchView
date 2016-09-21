package com.example.singletouchview;

import com.example.singletouchview.SingleTouchView.OnStateChangedListener;
import com.example.singletouchview.SingleTouchView.SigleTouchItem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements OnClickListener {
	SingleTouchView mSingleTouchView;
	private GestureDetector gestureScanner;
	private EditText textview_foucs;
	private ImageView cutview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textview_foucs = (EditText) findViewById(R.id.textview_foucs);
		cutview = (ImageView) findViewById(R.id.cutview);
		findViewById(R.id.add).setOnClickListener(this);
		findViewById(R.id.shot).setOnClickListener(this);

		mSingleTouchView = (SingleTouchView) findViewById(R.id.SingleTouchView);
		textview_foucs.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				current.setText(s);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://blog.csdn.net/xiaanming"));
			startActivity(intent);
			return true;
		default:
			return false;
		}
	}

	@SuppressLint("NewApi")
	OnLayoutChangeListener mOnLayoutChangeListener = new OnLayoutChangeListener() {

		@Override
		public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
				int oldRight, int oldBottom) {
			if (/* !isInputMethodShow() */bottom > oldBottom) {
				textview_foucs.removeOnLayoutChangeListener(mOnLayoutChangeListener);
				textview_foucs.setVisibility(View.INVISIBLE);
			}

		}
	};

	TextView current;

	@SuppressLint("NewApi")
	OnStateChangedListener mOnStateChangedListener = new OnStateChangedListener() {

		@Override
		public void onDoubleClick(View item) {
			current = (TextView) item;
			textview_foucs.setText(current.getText());
			textview_foucs.setVisibility(View.VISIBLE);
			showInputMethod(textview_foucs);
			textview_foucs.addOnLayoutChangeListener(mOnLayoutChangeListener);
		}

		@Override
		public void onClicked(View item) {

		}

		@Override
		public void onUnbind(View item) {
			hideInputMethod(textview_foucs);
			textview_foucs.removeOnLayoutChangeListener(mOnLayoutChangeListener);
			textview_foucs.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onSetting(View view) {
			// mSingleTouchView.move(view, 0);
			ViewGroup vg = (ViewGroup) (view.getParent());
			vg.removeView(view);
			vg.addView(view, 0);
		}

		@Override
		public void onClosed(View view) {
			mSingleTouchView.remove(view);
			ViewGroup vg = (ViewGroup) (view.getParent());
			vg.removeView(view);

		}
	};

	private void showInputMethod(View v) {
		v.requestFocus();
		InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
	}

	private void hideInputMethod(View v) {
		InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0); // 强制隐藏键盘
	}

	private boolean isInputMethodShow() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen = imm.isActive();//
		return isOpen;
	}

	private Bitmap shotView(View view) {
		// View是你需要截图的View
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b = view.getDrawingCache();
		// view.destroyDrawingCache();
		return b;
	}

	private Bitmap drawView(SigleTouchItem item) {
		// view to bitmap without matrix
		View view = item.getView();
		Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Config.ARGB_8888);
		Canvas cvs = new Canvas(b);
		view.draw(cvs);

		// view offset

		int screenWidth = mSingleTouchView.getWidth();
		int screenHeight = mSingleTouchView.getHeight();
		RectF viewRect = item.getPostArea();
		if (viewRect.left >= screenWidth || viewRect.top >= screenHeight || viewRect.bottom <= 0
				|| viewRect.right <= 0) {
			// view不在屏幕内
			return null;
		}

		float offx = 0, offy = 0, offW = 0, offH = 0;
		if (viewRect.left < 0) {
			offx = 0 - viewRect.left;
			offW -= offx;
		}
		if (viewRect.top < 0) {
			offy = 0 - viewRect.top;
			offH -= offy;
		}
		if (viewRect.right > screenWidth) {
			offW += (screenWidth - viewRect.right);
		}
		if (viewRect.bottom > screenHeight) {
			offH += (screenHeight - viewRect.bottom);
		}
		if (offx != 0 || offy != 0 || offW != 0 || offH != 0) {
			// offx *= scale;
			// offy *= scale;
			// offW *= scale;
			// offH *= scale;
			// bmp = cropBitmap(bmp, (int) offx, (int) offy, (int)
			// (bmp.getWidth() + offW), (int) (bmp.getHeight() + offH));
		}

		// Bitmap bitmap = b;
		// Matrix matrix = new Matrix();
		// matrix.postScale(view.getScaleX(),view.getScaleY()); //长和宽放大缩小的比例
		// matrix.postRotate(view.getRotation());
		// Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0,
		// view.getWidth(),view.getHeight(), matrix, true);
		// if (resizeBmp != bitmap) {
		// bitmap.recycle();
		// }

		Bitmap bitmap = b;
		int newWidth = view.getWidth();
		int newHeight = view.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(view.getScaleX(), view.getScaleY()); // 长和宽放大缩小的比例
		matrix.postRotate(view.getRotation());
		Bitmap newBmp = createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), (int) -offx, (int) -offy,
				(int) offW, (int) offH, matrix, true);

		if (newBmp != bitmap) {
			bitmap.recycle();
		}

		return newBmp;
	}

	private Bitmap createBitmap(Bitmap source, int x, int y, int width, int height, int offx, int offy, int offW,
			int offH, Matrix m, boolean filter) {
		int neww = width;
		int newh = height;
		Canvas canvas = new Canvas();
		Bitmap bitmap;
		Paint paint;

		Rect srcR = new Rect(x, y, x + width, y + height);
		RectF dstR = new RectF(0, 0, width, height);

		Config newConfig = Config.ARGB_8888;
		final Config config = source.getConfig();
		// GIF files generate null configs, assume ARGB_8888
		if (config != null) {
			switch (config) {
			case RGB_565:
				newConfig = Config.RGB_565;
				break;
			case ALPHA_8:
				newConfig = Config.ALPHA_8;
				break;
			// noinspection deprecation
			case ARGB_4444:
			case ARGB_8888:
			default:
				newConfig = Config.ARGB_8888;
				break;
			}
		}

		if (m == null || m.isIdentity()) {
			neww = neww + offW;
			newh = newh + offH;
			canvas.translate(offx, offy);
			bitmap = Bitmap.createBitmap(neww, newh, newConfig);
			paint = null; // not needed
		} else {
			final boolean transformed = !m.rectStaysRect();

			RectF deviceR = new RectF();
			m.mapRect(deviceR, dstR);

			neww = Math.round(deviceR.width()) + offW;
			newh = Math.round(deviceR.height()) + offH;

			bitmap = Bitmap.createBitmap(neww, newh, transformed ? Config.ARGB_8888 : newConfig);

			canvas.translate(-deviceR.left + offx, -deviceR.top + offy);
			canvas.concat(m);

			paint = new Paint();
			paint.setFilterBitmap(filter);
			if (transformed) {
				paint.setAntiAlias(true);
			}
		}

		// The new bitmap was created from a known bitmap source so assume that
		// they use the same density
		bitmap.setDensity(source.getDensity());
		bitmap.setHasAlpha(source.hasAlpha());
		// bitmap.setPremultiplied(source.ismRequestPremultiplied);

		canvas.setBitmap(bitmap);
		canvas.drawBitmap(source, srcR, dstR, paint);
		canvas.setBitmap(null);

		return bitmap;
	}

	private Bitmap drawView(View view, RectF size, SigleTouchItem item) {
		Bitmap b = null;
		int viewWidth = view.getMeasuredWidth();
		int viewHeight = view.getMeasuredHeight();
		if (viewWidth > 0 && viewHeight > 0) {
			b = Bitmap.createBitmap((int) size.width(), (int) size.height(), Config.ARGB_8888);
			Canvas cvs = new Canvas(b);
			cvs.save();
			// Matrix matrix=new Matrix();
			// matrix.setScale(view.getScaleX(), view.getScaleY());
			// matrix.postRotate(view.getRotation()/*,view.getWidth()*view.getScaleX()/2,view.getHeight()*view.getScaleY()/2*/);
			// matrix.postTranslate((size.width()-view.getWidth())/2,
			// (size.height()-view.getHeight())/2);
			// cvs.setMatrix(matrix);

			cvs.translate(size.width() / 2, 0);
			cvs.scale(view.getScaleX(), view.getScaleY());
			// cvs.translate(x, y);
			// cvs.translate(-(item.getCenter().x-item.mLTPoint.x)-(item.getPostArea().left-item.getCenter().x),
			// 0);
			cvs.rotate(view.getRotation());
			// cvs.translate(x, y);
			// Logger.l("xxxxx"+x+" yyyyy "+y);
			view.draw(cvs);

			cvs.restore();
		}
		return b;
	}

	private void addFragItem(int x, int y, int w, int h) {
		TextView tv = new TextView(this);
		tv.setMinLines(2);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
		lp.leftMargin = x;
		lp.topMargin = y;
		tv.setLayoutParams(lp);
		tv.setText("dddddddddd");
		tv.setGravity(Gravity.CENTER);
		tv.setBackgroundColor((int) (0xFF000000 + Math.random() * 0xFFFFFF));
		mSingleTouchView.addView(tv, lp);
		mSingleTouchView.add(tv, mOnStateChangedListener);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add:
			addFragItem(mSingleTouchView.getWidth() / 2, mSingleTouchView.getHeight() / 2, 400, 200);
			break;
		case R.id.shot:
			SigleTouchItem item = mSingleTouchView.getItems().get(mSingleTouchView.getItems().size() - 1);
			// for (SigleTouchItem item : mSingleTouchView.getItems()) {
			if (!item.isPrepared()) {
				item.prepare();
			}
			RectF area = new RectF(item.getPostArea());
			area.inset(-8, -8);
			// Bitmap b = drawView(item.getView(), item.getPostArea(),
			// item);
			cutview.setImageBitmap(drawView(item));
			// }
			break;
		}
	}
}
