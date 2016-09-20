package com.example.singletouchview;

import com.example.singletouchview.SigleTouchItem.OnStateChangedListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity {
	EditText textview, textview1, textview2;
	SingleTouchView mSingleTouchView;
	private GestureDetector gestureScanner;
	private EditText textview_foucs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textview = (EditText) findViewById(R.id.textview);
		textview1 = (EditText) findViewById(R.id.textview1);
		textview2 = (EditText) findViewById(R.id.textview2);
		textview_foucs = (EditText) findViewById(R.id.textview_foucs);
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
		unbind();
	}

	private void unbind() {
		mSingleTouchView.add(textview, mOnStateChangedListener);
		mSingleTouchView.add(textview1, mOnStateChangedListener);
		mSingleTouchView.add(textview2, mOnStateChangedListener);
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
			if (/*!isInputMethodShow()*/bottom>oldBottom) {
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
			current=(TextView) item;
			textview_foucs.setText(((EditText) item).getText());
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

}
