package com.example.singletouchview;

import com.example.singletouchview.OnDoubleClick.OnDoubleClicked;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends FragmentActivity implements OnClickListener {
	View textview;
	SingleTouchView mSingleTouchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textview=findViewById(R.id.textview);
		mSingleTouchView=(SingleTouchView) findViewById(R.id.SingleTouchView);
		mSingleTouchView.setOnClickListener(this);
		textview.setOnClickListener(this);
//		OnDoubleClick mOnDoubleClick=new OnDoubleClick(new OnDoubleClicked() {
//			
//			@Override
//			public void onDoubleClicked(View v) {
//				Logger.l("onDoubleClicked textview");
//				
//			}
//		});
//		textview.setOnTouchListener(mOnDoubleClick);
		
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
		if(v==mSingleTouchView){
			Logger.l("onClick mSingleTouchView");
			mSingleTouchView.setItem(null);
		}else if(v==textview){
			Logger.l("onClick textview");
			SigleTouchItem item=new SigleTouchItem(textview);
			mSingleTouchView.setItem(item);
		}
	}
	

}
