package com.example.singletouchview;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class MainActivity extends FragmentActivity implements OnClickListener {
	EditText textview;
	SingleTouchView mSingleTouchView;
	private GestureDetector gestureScanner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textview = (EditText) findViewById(R.id.textview);
		mSingleTouchView = (SingleTouchView) findViewById(R.id.SingleTouchView);
		mSingleTouchView.setOnClickListener(this);
		textview.setOnClickListener(this);
		// OnDoubleClick mOnDoubleClick=new OnDoubleClick(new OnDoubleClicked()
		// {
		//
		// @Override
		// public void onDoubleClicked(View v) {
		// Logger.l("onDoubleClicked textview");
		//
		// }
		// });
		// textview.setOnTouchListener(mOnDoubleClick);
		gestureScanner = new GestureDetector(new OnGestureListener() {

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onDown(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		gestureScanner.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
			public boolean onDoubleTap(MotionEvent e) {
				// 双击时产生一次
				Logger.l("onDoubleTap");
				textview.setText("doubletap");
				textview.requestFocus();
				InputMethodManager imm=(InputMethodManager) textview.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
				return false;
			}

			public boolean onDoubleTapEvent(MotionEvent e) {
				// 双击时产生两次
				Logger.l("onDoubleTapEvent");
				return false;
			}

			public boolean onSingleTapConfirmed(MotionEvent e) {
				// 短快的点击算一次单击
				Logger.l("onSingleTapConfirmed");
				SigleTouchItem item = new SigleTouchItem(textview);
				mSingleTouchView.setItem(item);
				return false;
			}
		});

		textview.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gestureScanner.onTouchEvent(event);
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
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

	@Override
	public void onClick(View v) {
		if (v == mSingleTouchView) {
			Logger.l("onClick mSingleTouchView");
			mSingleTouchView.setItem(null);
		} else if (v == textview) {
			Logger.l("onClick textview");
		}
	}

}
