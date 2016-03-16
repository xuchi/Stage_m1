package com.example.eyeeye;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

	private Button button1,button2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		button1=(Button)findViewById(R.id.button1);
		button1.setOnClickListener(this);
		button2=(Button)findViewById(R.id.button2);
		button2.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if(v==button1){
			Intent intent = new Intent(this, Collection.class);
			startActivity(intent);
		} if(v==button2){
			Intent intent = new Intent(this, Gamera.class);
			startActivity(intent);
		}
	}
}
