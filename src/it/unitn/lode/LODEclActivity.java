package it.unitn.lode;

import it.unitn.lode.data.Courses;
import it.unitn.lode.data.Lectures;
import it.unitn.lode.data.LodeSaxDataParser;
import it.unitn.lode.data.TimedSlides;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LODEclActivity extends Activity implements OnItemClickListener, OnClickListener{
	private LodeSaxDataParser lecturesParser = null;
	private ListView lvCourses = null;
	private ListView lvLectures = null;
	private ArrayList<TextView> courses = null;
	private ArrayList<TextView> lectures = null;
	private RelativeLayout rlCL = null;
	private RelativeLayout.LayoutParams rlCLParams = null;
	private LodeSaxDataParser coursesParser = null;
	private Runnable coursesPopulator = null;
	private Runnable lecturesPopulator = null;
	private List<Courses> cs = null;
	private List<Lectures> ls = null;
	private Map<Integer, List<Lectures>> cl = null;
	private Iterator<Courses> csIterator = null;
	private Iterator<Lectures> lsIterator = null;
	private Handler handler = null;
	private final Context coursesContext = this;
	private TextView tvCourse = null;
	private Courses selectedCourse = null;
	private TextView tvItem = null;
	private final int LV_COURSES = 100;
	private final int LV_LECTURES = 101;
	private ImageButton btnWatch = null;
	private ImageButton btnDownload = null;
	private final String BASE_URL = "http://latemar.science.unitn.it/itunes/feeds/";
	public static Context CL_CONTEXT;
	private AlertDialog alertNetwork = null, alertExit = null, alertWrongData = null;
	private boolean comingFromSettings = false;
	private ProgressBar pbCL = null;
	private DisplayMetrics metrics = null;
	public static int scrWidth, scrHeight;
	private RelativeLayout rlLectureInfo = null;
	private RelativeLayout rlLIContainer = null;
	private RelativeLayout rlButtons = null;
	private TextView tvLectureInfo = null;
	private RelativeLayout.LayoutParams rlLIParams = null;
	private Typeface tfApplegaramound = null;
	private final int WATCH = 0, DOWNLOAD = 1, INFO = 2;
	private LectureDownloader lectureDownloader = null;
	private ProgressDialog dProgressDialog = null;
	private String lectureDir = "";
	private LodeSaxDataParser tsParser = null;
	private List<TimedSlides> ts = null;
	private boolean downloadCancelled = false;
	private final String SD_CARD = Environment.getExternalStorageDirectory().toString();
	private boolean urlGoAhead = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.courses_lectures);
	    metrics = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metrics);
        scrWidth = metrics.widthPixels;
        scrHeight = metrics.heightPixels;
       
/******* LECTURE INFO AND OPTIONS LAYOUT ******/
        // instantiate it within the onCreate method
        dProgressDialog = new ProgressDialog(LODEclActivity.this);
        dProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Pause", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadCancelled = false;
				lectureDownloader.cancel(true);
		        makeToast("Download has been paused.");
			}
		});
        dProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadCancelled = true;
				lectureDownloader.cancel(true);
		        makeToast("Download has been cancelled.");
			}
		});
        dProgressDialog.setCancelable(false);
        dProgressDialog.setCanceledOnTouchOutside(false);
        dProgressDialog.setMessage("Downloading lecture");
        dProgressDialog.setIndeterminate(false);
        dProgressDialog.setMax(100);
        dProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        
        rlLIContainer = new RelativeLayout(this);
        rlLIContainer.setBackgroundColor(Color.TRANSPARENT);
        rlLIContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        rlLIContainer.setVisibility(View.GONE);

        rlButtons = new RelativeLayout(this);
        rlLectureInfo = new RelativeLayout(this);
        rlLectureInfo.setBackgroundResource(R.layout.lecture_info);
        btnWatch = new ImageButton(this);
        btnDownload = new ImageButton(this);
        tvLectureInfo = new TextView(this);
        
        btnWatch.setImageResource(android.R.drawable.ic_media_play);
        btnWatch.setOnClickListener(this);
        btnWatch.setId(WATCH);
        btnDownload.setImageResource(android.R.drawable.stat_sys_download);
        btnDownload.setOnClickListener(this);
        btnDownload.setId(DOWNLOAD);
        tvLectureInfo.setId(INFO);
        tvLectureInfo.setTypeface(tfApplegaramound, Typeface.NORMAL);
        tvLectureInfo.setTextSize(13);
        tvLectureInfo.setTextColor(Color.BLACK);
        tvLectureInfo.setGravity(Gravity.CENTER_HORIZONTAL);

        rlLectureInfo.setGravity(Gravity.CENTER_HORIZONTAL);
        rlLectureInfo.setPadding(10, 10, 10, 10);
        
        rlLIParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 150);
        rlLectureInfo.addView(tvLectureInfo, rlLIParams);
        
        rlButtons.setPadding(0, 15, 10, 0);
        
        rlLIParams = new RelativeLayout.LayoutParams(50, 50);
        rlLIParams.leftMargin = scrWidth / 4 - 55;
        rlButtons.addView(btnWatch, rlLIParams);

        rlLIParams = new RelativeLayout.LayoutParams(50, 50);
        rlLIParams.leftMargin = scrWidth / 4 + 5;
        rlButtons.addView(btnDownload, rlLIParams);
        
        rlLIParams = new RelativeLayout.LayoutParams(scrWidth / 2, scrHeight / 2);
        rlLIContainer.addView(rlLectureInfo, rlLIParams);
        rlLIParams = new RelativeLayout.LayoutParams(scrWidth / 2, 80);
        rlLIParams.topMargin = scrHeight / 2 - 80;
        rlLIContainer.addView(rlButtons, rlLIParams);
/********** END ***********/        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please make sure you have an active data connection.")
		       .setCancelable(false)
		       .setTitle("Lode4Android: No Internet Access")
		       .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                LODEclActivity.this.finish();
		           }
		       })
		       .setNegativeButton("Settings", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
		           }
		       });
		alertNetwork = builder.create();
		alertNetwork.setOwnerActivity(this);
		
		builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you want to leave LODE4Android?")
		       .setCancelable(true)
		       .setTitle("LODE4Android: Exit")
		       .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                LODEclActivity.this.finish();
		           }
		       })
		       .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.dismiss();
		           }
		       });
		alertExit = builder.create();
		alertExit.setOwnerActivity(this);

		builder = new AlertDialog.Builder(this);
		builder.setMessage("The course or lecture data is incorrect.")
		       .setCancelable(true)
		       .setTitle("LODE4Android: Error")
		       .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.dismiss();
		           }
		       });
		alertWrongData = builder.create();
		alertWrongData.setOwnerActivity(this);

		pbCL = (ProgressBar) findViewById(R.id.pbCL);
		pbCL.setVisibility(View.GONE);
		
		CL_CONTEXT = this;
        handler = new Handler();
        lvCourses = new ListView(this);
        lvCourses.setId(LV_COURSES);
        lvLectures = new ListView(this);
        lvLectures.setId(LV_LECTURES);
        
        rlCL = (RelativeLayout) findViewById(R.id.rlCL);

/*** ADD LECTURE INFO AND OPTIONS LAYOUT TO MAIN LAYOUT *****/
        rlLIParams = new RelativeLayout.LayoutParams(scrWidth / 2, scrHeight / 2);
        rlLIParams.topMargin = scrHeight/ 6 + 10;
        rlLIParams.leftMargin = scrWidth / 4 - 15;
        rlCL.addView(rlLIContainer, rlLIParams);
/****** END ******/
        courses = new ArrayList<TextView>();
        lectures = new ArrayList<TextView>();
        cl = new TreeMap<Integer, List<Lectures>>();
        tvCourse = new TextView(this);
        
        coursesPopulator = new Runnable() {
			@Override
			public void run() {
		    	File coursesMetaDataDir = new File(SD_CARD + "/lode");
		    	File coursesMetaData = new File(SD_CARD + "/lode/COURSES.XML");
		    	if(!coursesMetaData.exists()){
		    		try {
		    			coursesMetaDataDir.mkdirs();
						URL courseMetaDataUrl = new URL(BASE_URL + "COURSES.XML");
			            FileOutputStream output = null;
			            URLConnection connection = courseMetaDataUrl.openConnection();
			            connection.connect();
			            InputStream input = new BufferedInputStream(courseMetaDataUrl.openStream());
			            output = new FileOutputStream(coursesMetaData);
			            byte data[] = new byte[1024];
			            int count = 0;
			            while ((count = input.read(data)) != -1){
			            	output.write(data, 0, count);
			            }
			            output.flush();
			            output.close();
			            input.close();
						urlGoAhead = true;
					} catch (Exception e) {
			    		handler.post(new Runnable() {
							@Override
							public void run() {
					    		makeToast("Error downloading course data.");
							}
						});
						e.printStackTrace();
						urlGoAhead = false;
					}
		    	}

		    	try{
					coursesParser = new LodeSaxDataParser(new URL(BASE_URL + "COURSES.XML"));
				}catch(Exception e){
		            handler.post(new Runnable(){
						@Override
						public void run() {
							comingFromSettings = true;
							alertNetwork.show();
						}
					});
				}
				try{
			        cs = coursesParser.parseCourses();
		            csIterator = cs.iterator();
		            handler.post(new Runnable(){
						@Override
						public void run() {
							pbCL.bringToFront();
							String title;
				            tvCourse = new TextView(coursesContext);
				        	tvCourse.setText("\nAvailable Courses");
				        	courses.add(tvCourse);
					        while(csIterator.hasNext()){
					        	title = csIterator.next().getTitoloc().trim().replaceAll(" +", " ").replace("\n", "");
					        	title = title.trim();
					            tvCourse = new TextView(coursesContext);
					        	tvCourse.setText(title);
					        	courses.add(tvCourse);
					        }
							pbCL.setVisibility(View.GONE);
					        lvCourses.invalidateViews();
						}
					});
				}catch(RuntimeException e1){
		            handler.post(new Runnable(){
						@Override
						public void run() {
							comingFromSettings = true;
							if(isConnected()){
								alertWrongData.show();
							}
							else{
								alertNetwork.show();
							}
						}
					});
				}
			}
		};
		pbCL.setVisibility(View.VISIBLE);
		new Thread(coursesPopulator).start();
        lvCourses.setAdapter(new CoursesAdapter(this, R.layout.courses, courses, metrics){
			@Override
			public boolean isEnabled(int position) {
				if(position == 0)
					return false;
				return true;
			}
		});
        lvLectures.setAdapter(new LecturesAdapter(this, R.layout.lectures, lectures, metrics){
			@Override
			public boolean isEnabled(int position) {
				if(position == 0)
					return false;
				return true;
			}
		});
        lvCourses.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvCourses.setCacheColorHint(Color.parseColor("#00000000"));
        lvCourses.setBackgroundResource(R.layout.courses_corners);
        lvCourses.setSelector(R.drawable.list_item);
        lvCourses.setDividerHeight(2);
        lvCourses.setOnItemClickListener(this);

        
        lvLectures.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvLectures.setCacheColorHint(Color.parseColor("#00000000"));
        lvLectures.setBackgroundResource(R.layout.courses_corners);
        lvLectures.setSelector(R.layout.courses_corners_clicked);
        lvLectures.setDividerHeight(2);
        lvLectures.setOnItemClickListener(this);

/***** ADD VIEWS TO LAYOUTS ******/
        if(metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM){
            rlCLParams = new RelativeLayout.LayoutParams(dp(scrWidth / 3), dp(scrHeight));
            rlCL.addView(lvCourses, rlCLParams);
            
            rlCLParams = new RelativeLayout.LayoutParams(dp(scrWidth * 2 / 3 - 10), dp(scrHeight));
            rlCLParams.leftMargin = dp(scrWidth / 3 + 5);
            rlCL.addView(lvLectures, rlCLParams);
        }
        else{
            rlCLParams = new RelativeLayout.LayoutParams(scrWidth / 3, scrHeight);
            rlCL.addView(lvCourses, rlCLParams);
            
            rlCLParams = new RelativeLayout.LayoutParams(scrWidth * 2 / 3 - 10, scrHeight);
            rlCLParams.leftMargin = scrWidth / 3 + 5;
            rlCL.addView(lvLectures, rlCLParams);
        }
/************* END ******************/        
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Integer POSITION = position;
		if(parent.getId() == LV_COURSES){
		     lvLectures.setEnabled(false);
			if(rlLIContainer.getVisibility() == View.VISIBLE){
				rlLIContainer.setVisibility(View.INVISIBLE);
			}
			tvItem = (TextView) parent.getItemAtPosition(position);
			
			Iterator<Courses> selectedIterator = cs.iterator();
			String title;
			while(selectedIterator.hasNext()){
				selectedCourse = selectedIterator.next();
				title = selectedCourse.getTitoloc().trim().replaceAll(" +", " ").replace("\n", "");
				title = title.trim();
				if(title.equals(tvItem.getText())){
					lecturesPopulator = new Runnable() {
						@Override
						public void run() {
							try{
								if(cl.get(POSITION) == null){
									String url = BASE_URL + selectedCourse.getFolderc() + "/LECTURES.XML";
									lecturesParser = new LodeSaxDataParser(new URL(url));
								}
							}catch(Exception e){
					            handler.post(new Runnable(){
									@Override
									public void run() {
										comingFromSettings = true;
										alertNetwork.show();
									}
								});
							}
							try{
								if(cl.get(POSITION) == null){
									ls = lecturesParser.parseLectures();
									cl.put(POSITION, ls);
								}
								lsIterator = cl.get(POSITION).iterator();
								handler.post(new Runnable() {
									@Override
									public void run() {
										pbCL.setVisibility(View.VISIBLE);
										pbCL.bringToFront();
										lectures.removeAll(lectures);
							            tvItem = new TextView(coursesContext);
							            String courseDetails = "Professor in charge: " + selectedCourse.getDocentec() + "\n";
							            courseDetails+= "Academic Year: " + selectedCourse.getYear();
							        	tvItem.setText(courseDetails);
							        	lectures.add(tvItem);
										
								        while(lsIterator.hasNext()){
								        	Lectures nextLs = lsIterator.next();
								        	String lTitle = nextLs.getTitolol().trim().replaceAll(" +", " ").replace("\n", "");
								        	lTitle = lTitle.trim();
								            tvItem = new TextView(coursesContext);
								        	tvItem.setText(lTitle);
								        	tvItem.setTag(R.id.tvLectureDataUrl, BASE_URL + nextLs.getFolderl());
								        	tvItem.setTag(R.id.tvVideoUrl, nextLs.getUrllez());
								        	tvItem.setTag(R.id.tvLectureDir, nextLs.getFolderl());
								        	tvItem.setTag(R.id.tvLectureInfoConcat,
								        			"\nTopic: "	+ nextLs.getTitolol()
								        			+ "\nDate: " + nextLs.getDatel()
								        			+ "\nLecturer: " + nextLs.getDocentel());
								        	tvItem.setTag(R.id.tvIdForBookmark, nextLs.getFolderl());
								        	lectures.add(tvItem);
								        }
										pbCL.setVisibility(View.GONE);
										lvLectures.setEnabled(true);
								        lvLectures.invalidateViews();
									}
								});
							}catch(RuntimeException e1){
					            handler.post(new Runnable(){
									@Override
									public void run() {
										comingFromSettings = true;
										if(isConnected()){
											alertWrongData.show();
										}
										else{
											alertNetwork.show();
										}
									}
								});
							}
						}
					};
					pbCL.setVisibility(View.VISIBLE);
					new Thread(lecturesPopulator).start();
					break;
				}
			}
		}
		else if(parent.getId() == LV_LECTURES){
			TextView tvLecture = (TextView) parent.getItemAtPosition(position);
			tvLectureInfo.setText((String) tvLecture.getTag(R.id.tvLectureInfoConcat));
			btnWatch.setTag(R.id.videoUrl, (String) tvLecture.getTag(R.id.tvVideoUrl));
			btnWatch.setTag(R.id.lectureDataUrl, (String) tvLecture.getTag(R.id.tvLectureDataUrl));
			btnWatch.setTag(R.id.IdForBookmark, (String) tvLecture.getTag(R.id.tvIdForBookmark));
			btnDownload.setTag(R.id.lectureDir, (String) tvLecture.getTag(R.id.tvLectureDir));
			lectureDir = (String) tvLecture.getTag(R.id.tvIdForBookmark);
//			tvLectureInfo.setText("\nTopic: " + cl.get(currPos).get(position - 1).getTitolol() + "\nDate: "
//								+ cl.get(currPos).get(position - 1).getDatel() + "\nLecturer: "
//								+ cl.get(currPos).get(position - 1).getDocentel());
//			btnWatch.setTag(R.id.videoUrl, cl.get(currPos).get(position - 1).getUrllez());
//			btnWatch.setTag(R.id.lectureDataUrl, BASE_URL + cl.get(currPos).get(position - 1).getFolderl());
//			lectureDir = cl.get(currPos).get(position - 1).getFolderl();

			boolean[] availability = checkStorageAvailability();
			if(availability[0]){
				if(availability[1]){
					btnDownload.setEnabled(true);
				}
				else{
					btnDownload.setEnabled(false);
					makeToast("External Storage is write-protected.");
				}
			}
			else{
				btnDownload.setEnabled(false);
				makeToast("External storage is not present.");
			}
			rlLIContainer.setVisibility(View.VISIBLE);
			rlLIContainer.bringToFront();
			lvCourses.setEnabled(false);
			lvLectures.setEnabled(false);
		}
	}
	@Override
	public void onBackPressed() {
		if(rlLIContainer.getVisibility() == View.VISIBLE){
			rlLIContainer.setVisibility(View.GONE);
			lvCourses.setEnabled(true);
			lvLectures.setEnabled(true);
		}
		else{
			alertExit.show();
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		if(comingFromSettings){
	        courses = new ArrayList<TextView>();
			comingFromSettings = false;
			new Thread(coursesPopulator).start();
		}
	}
	private boolean isConnected() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    return cm.getActiveNetworkInfo() == null ? false : cm.getActiveNetworkInfo().isAvailable();
	}
	public int dp(int pixels){
		//return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels , getResources().getDisplayMetrics());
		return (int) (pixels / getResources().getDisplayMetrics().density + 0.5);
	}
	@Override
	public void onClick(View v) {
		if(v.getId() == WATCH){
			String videoUrl = (String) btnWatch.getTag(R.id.videoUrl);
			String lectureDataUrl = (String) btnWatch.getTag(R.id.lectureDataUrl);
			String idForBookmark = (String) btnWatch.getTag(R.id.IdForBookmark); 
			Bundle bundle = new Bundle();
			bundle.putString("videoUrl",videoUrl);
			bundle.putString("lectureDataUrl", lectureDataUrl);
			bundle.putString("idForBookmark", idForBookmark);
			bundle.putString("LectureInfo", tvLectureInfo.getText().toString());
			bundle.putBoolean("isLocal", false);
			Intent intent = new Intent(this, LODEActivity.class);
			rlLIContainer.setVisibility(View.GONE);
			lvCourses.setEnabled(true);
			lvLectures.setEnabled(true);
			intent.putExtras(bundle);
			startActivity(intent);
		}
		else if(v.getId() == DOWNLOAD){
			lectureDownloader = new LectureDownloader();
			lectureDownloader.execute(BASE_URL + lectureDir);
			rlLIContainer.setVisibility(View.GONE);
			lvCourses.setEnabled(true);
			lvLectures.setEnabled(true);
		}
	}
	private class LectureDownloader extends AsyncTask<String, Integer, String>{
		String status = "Determining download size ...";
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	    	handler.post(new Runnable() {
				@Override
				public void run() {
					dProgressDialog.setMessage(status);
					dProgressDialog.setProgress(0);
					dProgressDialog.setSecondaryProgress(0);
				}
			});
	        dProgressDialog.show();
	    }
	    @Override
	    protected String doInBackground(String... sUrl){
	    	if(urlGoAhead){
		    	List<URL> urls = new ArrayList<URL>();
		    	File downloadCheck = new File(SD_CARD + "/lode/"  + lectureDir + "/lecture.downloaded");
		    	if(downloadCheck.exists()){
		    		handler.post(new Runnable() {
						@Override
						public void run() {
				    		makeToast("Lecture has already been downloaded.");
						}
					});
		    	}
		    	else{
			        try {
			            // create a File object for the parent directory
			            File lectureDirectory = new File(SD_CARD + "/lode/"  + lectureDir + "/");
			            File slidesDirectory = new File(SD_CARD	+ "/lode/"  + lectureDir + "/img");
		            	urls.add(new URL(sUrl[0] + "/LECTURE.XML"));
		            	urls.add(new URL(sUrl[0] + "/TIMED_SLIDES.XML"));
		            	
				        tsParser = new LodeSaxDataParser(new URL(sUrl[0] + "/TIMED_SLIDES.XML"));
						try{
							ts = new ArrayList<TimedSlides>();
					        ts = tsParser.parseSlides();
					        Iterator<TimedSlides> tsIterator = ts.iterator();
					        while(tsIterator.hasNext()){
					        	String imageUrl = tsIterator.next().getImmagine();
					        	urls.add(new URL(sUrl[0] +"/" + imageUrl));
					        }
						}catch(RuntimeException e){
				            handler.post(new Runnable(){
								@Override
								public void run() {
									alertWrongData.show();
								}
							});
						}
			            // have the object build the directory structure, if needed.
			            lectureDirectory.mkdirs();
			            slidesDirectory.mkdirs();

			            Iterator<URL> urlIterator = urls.iterator();
			            File outputFile = null;
			            FileOutputStream output = null;
			            int fileLength = 0, lectureLength = 0;
			            long total = 0, lectureTotal = 0;
						while(urlIterator.hasNext()){
							if(!isCancelled()){
								URL nextUrl = urlIterator.next();
					            URLConnection connection = nextUrl.openConnection();
					            connection.connect();
//					            // this will be useful so that you can show a typical 0-100% progress bar
					            lectureLength += connection.getContentLength();
							}
							else{
				            	break;
							}
						}
						urlIterator = urls.iterator();
						while(urlIterator.hasNext()){
							if(!isCancelled()){
								URL nextUrl = urlIterator.next();
					            URLConnection connection = nextUrl.openConnection();
					            connection.connect();
//					            // this will be useful so that you can show a typical 0-100% progress bar
					            fileLength = connection.getContentLength();
//					            // download the file
					            InputStream input = new BufferedInputStream(nextUrl.openStream());
					            String fileName = nextUrl.toString().replace(sUrl[0], "");
					            if(fileName.contains("/img")){
					            	fileName = fileName.replace("/img", "");
						            outputFile = new File(slidesDirectory, fileName);
					            }
					            else{
					            	outputFile = new File(lectureDirectory, fileName);
					            }
					            if(!outputFile.exists()){
						            status = "Downloading " + fileName.replace("/", "");
//						            // now attach the OutputStream to the file object, instead of a String representation
						            output = new FileOutputStream(outputFile);
						            byte data[] = new byte[1024];
						            total = 0;
						            int count;
						            while ((count = input.read(data)) != -1){
						            	total += count;
//						                // publishing the progress....
						            	output.write(data, 0, count);
						            	publishProgress((int) ((lectureTotal * 100) / lectureLength), (int) ((total * 100) / fileLength));
						            }
						            lectureTotal += total;
						            output.flush();
						            output.close();
						            input.close();
					            }
					            else{
						            status = fileName.replace("/", "") + " already exists.";
					            	total = outputFile.length();
					            	publishProgress((int) ((lectureTotal * 100) / lectureLength), (int) ((total * 100) / fileLength));
					            	lectureTotal += total;
					            }
				            	publishProgress((int) ((lectureTotal * 100) / lectureLength), (int) ((total * 100) / fileLength));
							}
							else{
								break;
							}
						}
			            //Write FLAG to indicate lecture has been already been downloaded or there is no request for cancel.
			            if(!isCancelled()){
				            outputFile = new File(lectureDirectory, "lecture.downloaded");
				            output = new FileOutputStream(outputFile);
				            OutputStreamWriter downloadCheckWriter = new OutputStreamWriter(output);
				            downloadCheckWriter.write("Lecture has been successfully downloaded.");
				            downloadCheckWriter.flush();
				            downloadCheckWriter.close();
				            output.flush();
				            output.close();
			            }
			            else{
			            	final File deleteDir = lectureDirectory;
			            	if(downloadCancelled){
			            		Thread deleterThread = new Thread(){
			            			@Override
			            			public void run() {
					            		deleteDirAndContents(deleteDir);
			            			}
			            		};
			            		deleterThread.start();
			            	}
			            }
			        } catch (Exception e) {
				    	handler.post(new Runnable() {
							@Override
							public void run() {
					        	makeToast("Error downloading lecture.");
							}
						});
			        }
		    	}
	    	}
	        return null;
	    }
	    @Override
	    protected void onProgressUpdate(Integer... progress) {
	        super.onProgressUpdate(progress);
	        dProgressDialog.setMessage(status);
	        dProgressDialog.setProgress(progress[0]);
	        dProgressDialog.setSecondaryProgress(progress[1]);
	        //dProgressDialog.setSecondaryProgress(progress[1]);
	    }
	    @Override
	    protected void onPostExecute(String result) {
	    	super.onPostExecute(result);
	    	handler.post(new Runnable() {
				@Override
				public void run() {
					dProgressDialog.dismiss();
					status = "Downloading lecture";
					dProgressDialog.setMessage(status);
				}
			});
	    }
	}
	private boolean[] checkStorageAvailability(){
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		boolean[] availability = {mExternalStorageAvailable, mExternalStorageWriteable};
		return availability;
	}
	private void makeToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	void deleteDirAndContents(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles()){
	        	deleteDirAndContents(child);
	        }
	    fileOrDirectory.delete();
	}
}
