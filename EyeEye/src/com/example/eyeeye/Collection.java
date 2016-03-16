package com.example.eyeeye;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.AdapterView.OnItemClickListener;

public class Collection extends ListActivity {

	private List<Map<String,Object>> list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final SimpleAdapter simpleAdapter = new SimpleAdapter(this,getData(),R.layout.collection2,new String[]{"img","text"},new int[]{R.id.img_pre,R.id.text});
		simpleAdapter.setViewBinder(new ListViewBinder());
		setListAdapter(simpleAdapter);
		final ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				Map<String, Object> item = (Map<String, Object>) parent.getItemAtPosition(pos);
				Intent intent = new Intent(Collection.this,Presentation.class);
				intent.putExtra("bitmap", (Bitmap)item.get("img"));
				intent.putExtra("name", (String)item.get("text"));								
				startActivity(intent);
			}
		});
		lv.setOnTouchListener(new OnTouchListener(){
			float oldx=0;
			float oldy=0;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					oldx=event.getX();
					oldy=event.getY();
					break;
				case MotionEvent.ACTION_UP:
					float newx=event.getX();
					float newy=event.getY();

					final int oldPosition=lv.pointToPosition((int)oldx, (int)oldy);
					int newPosition = lv.pointToPosition((int)newx,(int) newy);
					if(oldPosition==newPosition && Math.abs(newx-oldx)>200){
						String name=list.get(oldPosition).get("text").toString();
						File collection=new File(getFilesDir().getAbsoluteFile(), "/Collection/"+name);
						File feature=new File(getFilesDir().getAbsolutePath(),"/Features/"+name);
						collection.delete();
						feature.delete();
						list.remove(oldPosition);					
						simpleAdapter.notifyDataSetChanged();
						return true;
					}
					break;
				}
				return false;	
			}

		});

	}

	class ListViewBinder implements ViewBinder {

		@Override
		public boolean setViewValue(View view, Object data,
				String textRepresentation) {
			if ((view instanceof ImageView) && (data instanceof Bitmap)) {
				ImageView imageView = (ImageView) view;
				Bitmap bmp = (Bitmap) data;
				imageView.setImageBitmap(bmp);
				return true;
			}
			return false;
		}
	}
	
	private List<Map<String,Object>> getData(){

		File dir=new File(getFilesDir().getAbsoluteFile(), "/Collection/");
		File file[]=dir.listFiles();
		list = new ArrayList<Map<String, Object>>();
		try {

			for (int i = 0; i < file.length; i++) {
				FileInputStream in = new FileInputStream(file[i].getAbsolutePath());
				BufferedInputStream bs = new BufferedInputStream(in);
				Bitmap bitmap = BitmapFactory.decodeStream(bs);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("img", bitmap);
				map.put("text", file[i].getName());
				list.add(map);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return list;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
