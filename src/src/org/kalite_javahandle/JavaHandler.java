package org.kalite_javahandle;

import android.webkit.WebChromeClient;
import org.renpy.android.PythonActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.Window;
import android.webkit.WebSettings;

import android.widget.RelativeLayout;
import android.widget.ProgressBar;
import android.view.View;
import android.os.Build;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

//for unzipping
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import android.os.AsyncTask;


import android.content.Context;
import android.view.MotionEvent;
import android.os.Environment;

//for RSA
import android.util.Base64;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.OutputStreamWriter;

import android.widget.Toast;
import java.lang.Thread;

//webview
import android.widget.FrameLayout;
import android.widget.VideoView;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.graphics.Canvas;
import android.net.Uri;
import android.content.Intent;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.FragmentManager;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;


public class JavaHandler {
	static Activity myActivity = (Activity)PythonActivity.mActivity;
	ProgressBar progressBar;
	WebView wv;
	static boolean asus_switch = false;   //for faster development, skip moving file.
	static String content_data_path;
	SharedPreferences sharedpreferences;
	Editor editor;

	public static boolean unzipKaLite(){
		String _fileLocation = Environment.getExternalStorageDirectory().getPath() + "/org.kalite.test/ka-lite.zip";
	    String _targetLocation = Environment.getExternalStorageDirectory().getPath() + "/org.kalite.test/.";
	    return unzipThreadUI(_fileLocation, _targetLocation);
	}

	public static void recursive_search(File root){
		if(root.listFiles()!=null && root.listFiles().length>0){
			for (File f: root.listFiles()) {
				if(f.isDirectory()){
					if(!f.getPath().contains("org.kalite.test")){
						if(f.getName().equals("ka-lite")){
							content_data_path = f.getPath();
							break;
						}
						recursive_search(f);
					}
				}
	        }
	    }
	}

	public static boolean isContentExist(){
		String content_folder_path = Environment.getExternalStorageDirectory().getPath() + "/org.kalite.test/copied_kalite_content";
		File contentFolder = new File(content_folder_path);
		if(contentFolder.exists()){
			return true;
		}else{
			return false;
		}
	}

	public static void movingDataSqlite(){
		String data_sqlite_path = Environment.getExternalStorageDirectory().getPath() + "/org.kalite.test/ka-lite/kalite/database";
		String data_sqlite_destination = Environment.getExternalStorageDirectory().getPath() + "/kalite_essential";

		File data_sqlite_source = new File(data_sqlite_path);
		File data_sqlite_destination_folder = new File(data_sqlite_destination);
		if(!data_sqlite_destination_folder.exists()){
			//data_sqlite_destination_folder.mkdir();
			fileMovingThreadUI file_mover_1 = new fileMovingThreadUI(data_sqlite_source, data_sqlite_destination_folder);
			file_mover_1.start_moving();
			file_mover_1 = null;
		}else{
			SQLiteDatabase db = SQLiteDatabase.openDatabase(data_sqlite_destination+"/data.sqlite", null, 0);
			String myQuery = "SELECT id FROM securesync_device WHERE id='49c04a1ff93b5de0b5f3d99340346210';";
			Cursor cursor = db.rawQuery(myQuery, null);
			if(cursor.getCount() > 0){
				File bad_datasqlite = new File(data_sqlite_destination+"/data.sqlite");
				bad_datasqlite.delete();

				fileMovingThreadUI file_mover_1 = new fileMovingThreadUI(data_sqlite_source, data_sqlite_destination_folder);
				file_mover_1.start_moving();
				file_mover_1 = null;
			}
		}
	}

	public static boolean movingFile(){
		String copied_content = Environment.getExternalStorageDirectory().getPath() + "/org.kalite.test/copied_kalite_content";
	    String moving = "null";
	    File dir_ainol = new File("/mnt/sd-ext/ka-lite");//this folder has to have unique name
        File dir_nexus7 = new File("/storage/emulated/0/UNICEF");//this folder has to have unique name
        File dir_asus_memo = new File("/Removable/MicroSD/ka-lite");//this folder has to have unique name
		if(dir_ainol.exists()) {
			System.out.println("movingFile from SD Ainol");
			moving = "/mnt/sd-ext/ka-lite";
		}else if(dir_asus_memo.exists()){
			System.out.println("movingFile from SD Asus");
			moving = "/Removable/MicroSD/ka-lite";
		}else if(dir_nexus7.exists()){
			moving = "/storage/emulated/0/UNICEF";
		}else{
			File _root = Environment.getExternalStorageDirectory();
			recursive_search(_root);
			moving = content_data_path;  //content_data_path has been processed by recursive_search
			if(content_data_path == null){

				return false;
			}
		}

		File sourceFile = new File(moving);

		File destinyFolder = new File(copied_content);

		if(destinyFolder.exists()){
			deleteDirectory(destinyFolder);
		}

		fileMovingThreadUI file_mover_2 = new fileMovingThreadUI(sourceFile, destinyFolder);
		file_mover_2.start_moving();
		file_mover_2 = null;

		return true;
	}

	public static boolean deleteDirectory(File tobe_delete) {
	    if( tobe_delete.exists() ) {
		   	File[] files = tobe_delete.listFiles();
		  	if (files == null) {
		       	return true;
		  	}
		  	for(File f: files){
		  		if(f.isDirectory()) {
		       		deleteDirectory(f);
		      	}
		      	else {
		           f.delete();
		       	}
		  	}
		}
		return( tobe_delete.delete() );
	}

	public static class fileMovingThreadUI {
		private File _targetFile;   
		private File _destination;
		public fileMovingThreadUI(File targetFile, File destination) {
			_targetFile = targetFile;     
			_destination = destination;      
      	} 

      	public void copyDir(File sourceLocation, File targetLocation){
      		try{
				if (sourceLocation.isDirectory()) {
				    if (!targetLocation.exists()) {
				        targetLocation.mkdir();
				    }
				    String[] children = sourceLocation.list();
				    for (int i = 0; i < sourceLocation.listFiles().length; i++) {
				        copyDir(new File(sourceLocation, children[i]),
				                new File(targetLocation, children[i]));
				    }
				} else {
				    InputStream in = new FileInputStream(sourceLocation);
				    OutputStream out = new FileOutputStream(targetLocation);
				    // Copy the bits from instream to outstream
				    byte[] buf = new byte[1024];
				    int len;
				    while ((len = in.read(buf)) > 0) {
				        out.write(buf, 0, len);
				    }
				    in.close();
				    out.close();
				}
		    }catch(IOException ex){
		        ex.printStackTrace(); 
		    }
      	}
		
		public void start_moving() {
		   	copyDir(_targetFile, _destination);
		}
	}

	public static void dirChecker(String dir) {
		File f = new File(dir);
		if (!f.isDirectory()) {
			f.mkdirs();
		}
	}

	public static boolean unzipThreadUI(String _zipFile, String _targetLocation){
		//create target location folder if not exist
		dirChecker(_targetLocation);
		byte[] buffer = new byte[1024];
		try {
			FileInputStream fin = new FileInputStream(_zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = zin.getNextEntry();
			while (ze != null) {
				//create dir if required while unzipping
				if (ze.isDirectory()) {
					dirChecker(_targetLocation + File.separator + ze.getName());
				} else {
					File newfile = new File(_targetLocation + File.separator + ze.getName());						
					File parentDir = new File(newfile.getParent());
					if(!parentDir.exists()){
						parentDir.mkdirs();
					}
					
					FileOutputStream fout = new FileOutputStream(newfile);

					int len_ui;
		            while ((len_ui = zin.read(buffer)) > 0) {
		            	fout.write(buffer, 0, len_ui);
		            }
					zin.closeEntry();
					fout.close();
				}
				ze = zin.getNextEntry();
			}
			zin.close();
			return new File(_zipFile).delete(); 
		} catch (Exception e) {
			System.out.println(e);
		}
		return false;
	}

	public static void generateRSA(){
		try { 
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); 
            keyGen.initialize(2048); 
            KeyPair RSA_key = keyGen.generateKeyPair(); 
            Key priavte_key = RSA_key.getPrivate();
            Key public_key = RSA_key.getPublic();
            
            byte[] publicKeyBytes = public_key.getEncoded();
            byte[] privateKeyBytes = priavte_key.getEncoded();
            
            String content_root = null;
            String content_data = null;

            String copied_content = Environment.getExternalStorageDirectory().getPath() + "/org.kalite.test/copied_kalite_content";
            String database_path = "\nDATABASE_PATH = \"" + Environment.getExternalStorageDirectory().getPath() + "/kalite_essential/data.sqlite\"";

            if(!asus_switch){
	            content_root = "\nCONTENT_ROOT = \"" + copied_content +"/content/\"";
	            content_data = "\nCONTENT_DATA_PATH = \"" + copied_content +"/data/\"";
	        }else{
	            content_root = "\nCONTENT_ROOT = \""  +"/Removable/MicroSD/ka-lite/content/\"";
	            content_data = "\nCONTENT_DATA_PATH = \"" +"/Removable/MicroSD/ka-lite/data/\"";
	        }


            String gut ="CHANNEL = \"connectteaching\"" +
            		"\nLOAD_KHAN_RESOURCES = False" +
            		"\nLOCKDOWN = True" +   //jamie ask to add it, need to test
            		"\nSESSION_IDLE_TIMEOUT = 0" + //jamie ask to add it, need to test
            		"\nPDFJS = False" +
            		database_path + 
            		content_root +
            		content_data +
            		"\nUSE_I18N = False" +
            		"\nUSE_L10N = False" +
            		"\nEBUG = False" +
            		"\nOWN_DEVICE_PUBLIC_KEY=" + "\"" + Base64.encodeToString(publicKeyBytes, 24, publicKeyBytes.length-24, Base64.DEFAULT).replace("\n", "\\n") + "\""
            		+ "\nOWN_DEVICE_PRIVATE_KEY=" +  "\"" + "-----BEGIN RSA PRIVATE KEY-----" + "\\n" 
            		+ Base64.encodeToString(privateKeyBytes, 26, privateKeyBytes.length-26, Base64.DEFAULT).replace("\n", "\\n")
            		+ "-----END RSA PRIVATE KEY-----" + "\"";
            
            String fileLocation2 = Environment.getExternalStorageDirectory().getPath() + "/org.kalite.test/ka-lite/kalite/";
            File myFile = new File(fileLocation2 , "local_settings.py");
            if(myFile.exists())
            {
               try
               {
                    FileOutputStream fOut = new FileOutputStream(myFile);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(gut);
                    myOutWriter.close();
                    fOut.close();
                } catch(Exception e)
                {

                }
            }
            else
            {
                myFile.createNewFile();
            }
            
        } catch(Exception e) { 
            System.out.println("RSA generating error"); 
        }
	}

	public void initWebView(){

	}

	public void show_toast(String str){
		Toast.makeText(myActivity, str, Toast.LENGTH_LONG).show();
	}


	public void showWebView(){ 
		sharedpreferences = myActivity.getSharedPreferences("MyPref", myActivity.MODE_MULTI_PROCESS);
		editor = sharedpreferences.edit();
		editor.putInt("live", 1);
		editor.commit(); 

		progressBar = new ProgressBar(myActivity, null, android.R.attr.progressBarStyleHorizontal);
		progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 54));
		// retrieve the top view of our application
		final FrameLayout decorView = (FrameLayout) myActivity.getWindow().getDecorView();
		decorView.addView(progressBar);

		wv = new MyWebView(myActivity);
		
		WebSettings ws = wv.getSettings();
		ws.setJavaScriptEnabled(true);


		wv.setWebChromeClient(new MyWebChromeClient());
		wv.setWebViewClient(new MyWebViewClient());
		wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		ws.setPluginState(WebSettings.PluginState.ON);

		wv.loadUrl("http://0.0.0.0:8008/");
		ws.setRenderPriority(WebSettings.RenderPriority.HIGH);
		ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
		
        myActivity.setContentView(wv);
	}

	public void quitDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
        builder.setMessage("Do you want to exit this app ?")
              	.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                   	public void onClick(DialogInterface dialog, int id) {
                   		// System.exit(0);
                   		editor.putInt("live", 0);
						editor.commit(); 
                   	}
               	})
               	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   	public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   	}
               	});
        // Create the AlertDialog object and return it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
	}

	public static void killApp(){
		System.exit(0);
	}

	public static void displayInLogCat(String s){
		System.out.println(s);
	}

	public boolean whetherHomePage(){
		if(wv.getUrl().equals("http://0.0.0.0:8008/")){
			return true;
		}else{
			return false;
		}
	}

	public boolean backPressed(){
		if(wv.canGoBack()){
			return true;
		}else{
			return false;
		}
	}

	public void goBack(){
		wv.goBack();
	}

	public void reloadFirstPage(){
		wv.loadUrl("http://0.0.0.0:8008/");
	}

	private class MyWebChromeClient extends WebChromeClient{
		@Override
		public void onProgressChanged(WebView view, int progress) {

			progressBar.setVisibility(View.VISIBLE);
        	progressBar.setProgress(progress);

			if(progress == 100){
				progressBar.setVisibility(View.GONE);
            }
		}
	}

	private class MyWebViewClient extends WebViewClient {
		@Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
           //Error happens here and returns an empty page.
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.endsWith(".pdf")){
            	String content_path = Environment.getExternalStorageDirectory().getPath() + "/org.kalite.test/copied_kalite_content/content/";
            	String[] parts = url.split("/");
            	String pdf_path = content_path + parts[parts.length - 1];
            	File pdf_file = new File(pdf_path);
            	Uri pdf_uri = Uri.fromFile(pdf_file);
            	Intent intent = new Intent(Intent.ACTION_VIEW);
            	intent.setDataAndType(pdf_uri, "application/pdf");
           // 	intent.setPackage("com.adobe.reader");
            //	intent.setType("application/pdf");
        		myActivity.startActivity(intent);
            	return true;
            }
            else{
            	view.loadUrl(url);
            	return false;
            }
        }
    }

	private class MyWebView extends WebView{

	    public MyWebView(Context context) {
	        super(context);
	    }

	    private long lastMoveEventTime = -1;
	    private int eventTimeInterval = 60;

	    @Override
	    public boolean onTouchEvent(MotionEvent ev) {

	        long eventTime = ev.getEventTime();
	        int action = ev.getAction();

	        switch (action){
	            case MotionEvent.ACTION_MOVE: {
	                if ((eventTime - lastMoveEventTime) > eventTimeInterval){
	                    lastMoveEventTime = eventTime;
	                    return super.onTouchEvent(ev);
	                }
	                break;
	            }
	            case MotionEvent.ACTION_DOWN:
	            case MotionEvent.ACTION_UP: {
	                return super.onTouchEvent(ev);
	            }
	        }
	        return true;
	    }
	}


}