package com.example.eyeeye;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class Presentation extends Activity{
	private TextView 	text;
	private ImageView 	image;
	private Bitmap 		bitmap;
	private String 		name;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.presentation);
		StringBuilder sb = new StringBuilder("");
		Resources res = getResources();
		XmlResourceParser xrp = res.getXml(R.xml.test);
		text  =	(TextView)this.findViewById(R.id.textview);
		image = (ImageView) this.findViewById(R.id.imageview);
		Intent intent = getIntent();
		if(intent !=null){
			bitmap = intent.getParcelableExtra("bitmap");
			name   = intent.getStringExtra("name");
			image.setImageBitmap(bitmap);
			
		}
		//int counter = 0;
		try{
			while(xrp.getEventType() !=XmlResourceParser.END_DOCUMENT){
				if(xrp.getEventType() == XmlResourceParser.START_TAG){
					String tagname = xrp.getName();
					if(tagname.endsWith(name)){
						//counter++;
						sb.append("nom: "+xrp.getAttributeValue(0)+"\n");
						sb.append("auther: "+xrp.getAttributeValue(1)+"\n");
						sb.append("annee: "+xrp.getAttributeValue(2)+"\n");
						sb.append("presentation: "+xrp.getAttributeValue(3)+"\n");
					}
				}
				try {
					xrp.next();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			text.setText(sb.toString());
		}catch(XmlPullParserException e){
			//rien
		}
	}

	public void onBackPressed() {
		super.onBackPressed();
	}
}
