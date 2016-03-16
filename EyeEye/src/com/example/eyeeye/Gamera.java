package com.example.eyeeye;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import com.exemple.fonctionslib.Fonction;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class Gamera extends Activity implements SurfaceHolder.Callback, OnClickListener, PictureCallback {

	private static final String   TAG = "SCAN";
	private boolean 		      traitement = true;	
	private Camera 				  camera;
	private SurfaceView 		  preview;
	private static RelativeLayout splash;	
	private boolean 			  ready;
    private String[] 			  images;
    private ProgressDialog 		  progress;
    private Bitmap 				  bitmap;
    private String 				  reponse;
	
	private static Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				splash.setVisibility(View.GONE);
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.scan_surface_view);
		SurfaceHolder holder 	= surfaceView.getHolder();
		holder.addCallback(this);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    splash = (RelativeLayout) findViewById(R.id.splash);
		Thread thread = new Thread() {
			@Override
			public void run() {
				prepare();
			}
		};
		thread.start();
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
		int maxWidth = 320;
		int mWidth 	 = 0, 	mHeight = 0;
		int defaultWidth = 0, 	defaultHeight = 0;
		traitement 	= true;
		preview 	= (SurfaceView) findViewById(R.id.scan_surface_view);		
		camera 		= Camera.open();
		camera.setDisplayOrientation(90);	
		Camera.Parameters parameters 	= camera.getParameters();		
		List<Camera.Size> pictureSizes 	= parameters.getSupportedPictureSizes();
		Iterator<Camera.Size> iterator 	= pictureSizes.iterator();
		Camera.Size size;
		preview.setOnClickListener(this);	
		while (iterator.hasNext()) {
			size = iterator.next();
			if (mWidth < size.width && size.width <= maxWidth) {
				mWidth = size.width;
				mHeight = size.height;
			}
			defaultWidth = size.width;
			defaultHeight = size.height;
		}
		if (mWidth != 0 && mHeight != 0) {
			parameters.setPictureSize(mWidth, mHeight);
		} else {
			parameters.setPictureSize(defaultWidth, defaultHeight);
		}
		//autofocus
        List<String> focusMode = parameters.getSupportedFocusModes();
        for (int i=0; i<focusMode.size(); i++) {
        	Log.i("ShotActivity", "Focus mode supported : " + focusMode.get(i));
        }
        if (focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
        	parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        	camera.setParameters(parameters);
        } else {
        	Log.w("ShotActivity", "AutoFocus non disponible");
        }
		camera.setParameters(parameters);		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		camera.setPreviewCallback(null);
		camera.release();
		camera = null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.shot, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent(this, Collection.class);
		startActivity(intent);
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (camera != null) {
			camera.stopPreview();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.scan_surface_view:
			if (!ready) {
				Toast.makeText(this, "Le matcher est en cours de chargement.", Toast.LENGTH_SHORT).show();
			} else {
				if (traitement) {
					traitement = false;
					preview.setOnClickListener(null);
					camera.takePicture(null, null, this);
				}
			}
		}
	}

	@Override
	public void onPictureTaken(final byte[] data, Camera camera) {
				
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.scan_radio_group);
		if (radioGroup.getCheckedRadioButtonId() == R.id.scan_match) {
			Thread argos = new Thread() {
				@Override
				public void run() {					
					Bitmap storedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, null);
					Matrix mat = new Matrix();
					mat.postRotate(90); 
					storedBitmap = Bitmap.createBitmap(storedBitmap, 0, 0, storedBitmap.getWidth(), storedBitmap.getHeight(), mat, true);
					File files = getFilesDir();
					File image = new File(files, "image.jpg");
					if (image.exists()) {
						image.delete();
					}
					try {
						image.createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					image.setReadable(true, false);
					try {
						FileOutputStream fos = new FileOutputStream(image);
						boolean ecriture = storedBitmap.compress(CompressFormat.JPEG, 100, fos);		
						//Log.i(TAG, "taille = " + storedBitmap.getWidth() + "*" + storedBitmap.getHeight() + "; ecriture = " + ecriture);	
						fos.flush();
						fos.close();
						storedBitmap.recycle();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					//Log.i(TAG, "path = " + image.getAbsolutePath());
					String path=image.getAbsolutePath();
					reponse=matcher(path);				
					AlertDialog.Builder adb = new AlertDialog.Builder(Gamera.this);
					adb.setTitle(R.string.reponse);
					if (reponse == null) {
						adb.setMessage(R.string.no_match);
					} 
					else {
						ImageView view = new ImageView(Gamera.this);
						FileInputStream in = null;
						try {
							in = new FileInputStream(getFilesDir().getAbsolutePath()+"/Collection/"+reponse);
							
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						BufferedInputStream bs = new BufferedInputStream(in);
						bitmap = BitmapFactory.decodeStream(bs);
						view.setImageBitmap(bitmap);
						
						adb.setView(view);
						adb.setMessage("Correspondance avec " + reponse + ".");
					}
					adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Gamera.this,Presentation.class);
							intent.putExtra("bitmap", bitmap);
							intent.putExtra("name", reponse);								
							startActivity(intent);
						}
					});
					adb.setNegativeButton("Return", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							traitement = true;
							preview.setOnClickListener(Gamera.this);
							Gamera.this.camera.startPreview();
						}
					});
					adb.show();
				}
			};
			argos.run();
		} else if (radioGroup.getCheckedRadioButtonId() == R.id.scan_add) {
			AlertDialog.Builder demandeNom = new AlertDialog.Builder(this);
			demandeNom.setTitle("Nom de l'image");
			final EditText input = new EditText(this);
			input.setHint("nom de l'image");
			demandeNom.setView(input);
			demandeNom.setCancelable(false);
			demandeNom.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					final String nom = input.getText().toString()+".jpg";
					final Handler handler = new Handler() {
						@Override
						public void handleMessage(Message msg) {
							if (progress != null) {
								progress.dismiss();
							}
							AlertDialog.Builder adb = new AlertDialog.Builder(Gamera.this);
							adb.setTitle("Reponse");
							adb.setMessage("Votre image a bien ete ajoute !");
							adb.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									traitement = true;
									preview.setOnClickListener(Gamera.this);
									Gamera.this.camera.startPreview();
								}
							});
							adb.show();
						}
					};
					Thread thread = new Thread() {
						@Override
						public void run() {
							Bitmap storedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, null);
							Matrix mat = new Matrix();
							mat.postRotate(90); 
							storedBitmap = Bitmap.createBitmap(storedBitmap, 0, 0, storedBitmap.getWidth(), storedBitmap.getHeight(), mat, true);
							File files = getFilesDir().getAbsoluteFile();
							File image = new File(files, "/Collection/"+nom);
							if (image.exists()) {
								image.delete();
							}
							try {
								image.createNewFile();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							image.setReadable(true, false);
							try {
								FileOutputStream fos = new FileOutputStream(image);
								storedBitmap.compress(CompressFormat.JPEG, 100, fos);
								fos.flush();
								fos.close();
								storedBitmap.recycle();
							} catch (IOException e) {
								e.printStackTrace();
							}
							addImage(nom);
							handler.sendEmptyMessage(0);
						}

					};
					thread.start();
					progress = ProgressDialog.show(Gamera.this, "EyeSnap", "Ajout de l'image en cours ...");
				}
			});
			demandeNom.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					traitement = true;
					preview.setOnClickListener(Gamera.this);
					Gamera.this.camera.startPreview();
				}
			});
			demandeNom.show();
		}
	}
	
	private void prepare() {
		Fonction ff= new Fonction();
    	AssetManager assets = getAssets();
		
		try{
			images=assets.list("Collection");
		}catch(IOException e){
			e.printStackTrace();
		}

		for(int i=0;i<images.length;i++){
			if(images[i].endsWith(".jpg")){
				copyImageToData(images[i]);
				String pathIn = null;
				
				String folderPath=getFilesDir().getAbsolutePath()+"/Features"; // the path of the data/data/file/Collection
		    	File folder = new File(folderPath);
		    	if(!folder.exists()){
		    		folder.mkdirs();
		    	}
				String pathOut = getFilesDir().getAbsolutePath() + "/Features/"+images[i].replace(".jpg", ".txt");
				pathIn = getFilesDir().getAbsolutePath() + "/Collection/"+images[i];				
				ff.genereFeature(pathIn, pathOut);	//pour appelle la fonction native
				
			}

		}
		
		handler.sendEmptyMessage(0);
		ready = true;
		Log.e(TAG, "MATCHER LOADED");
	}

	public String matcher(String mat1){
		Fonction fonction = new Fonction();
		int dist,max_dist = 0,index = 0;
		File dir=new File(getFilesDir().getAbsoluteFile(), "/Collection/");
		File file[]=dir.listFiles();
		for(int i =0;i<file.length;i++){
			//Log.e(TAG, "+++++++++");
			String mat2 = getFilesDir().getAbsolutePath()+"/Features/"+file[i].getName().replace(".jpg", ".txt");
			dist=fonction.matcher(mat1, mat2);
			//Log.e(TAG, "file "+mat2);
			if(dist>max_dist){
				max_dist=dist;
				index=i;
			}
		}
		
		return file[index].getName();
    }
	
	public void copyImageToData(String nameImage){
    	String folderPath	=	getFilesDir().getAbsolutePath()+"/Collection"; // the path of the data/data/file/Collection
    	File folder 		= 	new File(folderPath);
    	if(!folder.exists()){
    		folder.mkdirs();
    	}
    	String filePath	=	folderPath+"/"+nameImage; //to create the file of the data folder 
      	try{
      	File file = new File(filePath);
      	InputStream in =getAssets().open("Collection/"+nameImage);  	
      	if(file.exists() && file.length()>0){
      		
      	}else{
          	FileOutputStream out = new FileOutputStream(file);
          	byte[] buffer = new byte[1024];
          	int len	= 0;
          	while((len=in.read(buffer))!=-1){
          		out.write(buffer,0,len);
          	}
          	in.close();
          	out.close();
      	}
      	
      	}catch(IOException e){
      		e.printStackTrace();
      	}
    }

	public void addImage(String nameImage){
		Fonction fonction= new Fonction();
		String folderPath=getFilesDir().getAbsolutePath()+"/Features"; // the path of the data/data/file/Collection
		File folder = new File(folderPath);
		if(!folder.exists()){
		    folder.mkdirs();
		}
		String pathOut = getFilesDir().getAbsolutePath() + "/Features/"+nameImage.replace(".jpg", ".txt");
		String pathIn = getFilesDir().getAbsolutePath() + "/Collection/"+nameImage;				
		fonction.genereFeature(pathIn, pathOut);	//pour appelle la fonction native
		Log.e(TAG, "add a image");
	}

	/*	
	private static void saveImage(byte[] data, String path) {
		// d'abord rotation du bitmap
		Bitmap storedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, null);

		//Matrix mat = new Matrix();
		//mat.postRotate(90); // angle is the desired angle you wish to rotate
		//storedBitmap = Bitmap.createBitmap(storedBitmap, 0, 0, storedBitmap.getWidth(), storedBitmap.getHeight(), mat, true);

		File image = new File(path);
		if (image.exists()) {
			image.delete();
		}
		try {
			image.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		image.setReadable(true, false);
		try {
			FileOutputStream fos = new FileOutputStream(image);
			boolean ecriture = storedBitmap.compress(CompressFormat.JPEG, 100, fos);

			Log.i(TAG, "taille = " + storedBitmap.getWidth() + "*" + storedBitmap.getHeight() + "; ecriture = " + ecriture);

			fos.flush();
			fos.close();

			storedBitmap.recycle();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
	
	@Override
	public void onBackPressed() {	
		super.onBackPressed();	
	}

}
