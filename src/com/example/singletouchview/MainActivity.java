package com.example.singletouchview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends FragmentActivity {
	View textview;
	SingleTouchView mSingleTouchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textview=findViewById(R.id.textview);
		SigleItem item=new SigleItem(textview);
		mSingleTouchView=(SingleTouchView) findViewById(R.id.SingleTouchView);
		mSingleTouchView.setItem(item);
		
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
	

}
