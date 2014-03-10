package behsaman.storytellerandroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import behsaman.storytellerandroid.datamodel.StoryModel;
import behsaman.storytellerandroid.networking.ServerIO;
import behsaman.storytellerandroid.utils.Utils;
import behsaman.storytellerandroid.videoplayer.Video;
import behsaman.storytellerandroid.videoplayer.VideoPlayerActivity;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class VideoPieceActivity extends Activity {

	private static final String TAG = "VideoPieceActivity";
	private StoryModel model = null;
	private UUID uuid = null;
	private Button selectButton;
	private Button watchButton;
	private Button sendButton;
	private String mFileName = null;
	private ProgressDialog progressDialog = null;
	private boolean isSending = false;
	private int progress = 0;

	private final static String IS_SENDING = "VIDEOPIECEACTIVITY_CURRENT_PROGRESS";
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.video_piece, menu);
		return true;
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		if(savedInstanceState==null)
			return;
		if(savedInstanceState.getBoolean(IS_SENDING))
			showProgressDialog(progress);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		Log.e(TAG,"IsSending?"+(isSending?"YES":"NO"));
		outState.putBoolean(IS_SENDING, isSending);
		cancelProgresDialog();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_piece);
		
		LinearLayout ll = new LinearLayout(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(params);

		selectButton = new Button(this);
		selectButton.setText("Select");
		params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		selectButton.setLayoutParams(params);
		ll.addView(selectButton, params);

		watchButton = new Button(this);
		watchButton.setText("Watch");
		params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		watchButton.setLayoutParams(params);
		ll.addView(watchButton, params);

		sendButton = new Button(this);
		sendButton.setText("Send Piece");
		params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		sendButton.setLayoutParams(params);
		ll.addView(sendButton, params);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendPiece();
			}
		});

		// Set COntent view
		setContentView(ll);

		// Get Intent Info
		model = (StoryModel) getIntent().getSerializableExtra(
				StoryPageActivity.STORY_MODEL_KEY);
		uuid = UUID.fromString(getIntent().getStringExtra(
				StoryPageActivity.UUID_KEY));

		// Handlers
		selectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pickImage(v);
			}
		});

		final Context context = this;
		watchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mFileName == null || mFileName.length() == 0) {
					showFailureToast("No video selected.");
					return;
				}
				Intent intent = new Intent(context, VideoPlayerActivity.class);
				// Create a video object to be passed to the activity
				Video video = new Video(mFileName);
				video.setTitle(model.getTitle());
				video.setAuthor(model.getOwner_id().toString());
				video.setDescription("Piece " + model.getNext_available_piece());

				// Launch the activity with some extras
				intent.putExtra(VideoPlayerActivity.EXTRA_LAYOUT, "1");
				intent.putExtra(Video.class.getName(), video);
				startActivity(intent);
			}
		});
	}

	private synchronized void showProgressDialog(int prog) {
		cancelProgresDialog();
		isSending = true;
		
		if(progressDialog == null)
			progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Uploading Video... :) ");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setIndeterminate(false);
		progressDialog.setProgress(prog);
		progressDialog.setMax(100);
		progressDialog.show();
	}
	
	private synchronized void cancelProgresDialog() {
		isSending = false;
		if(progressDialog!=null)
			progressDialog.dismiss();
		progressDialog = null;
	}
	
	private void updateProgresDialog(int prog) {
		progressDialog.setProgress(prog);
		progressDialog.setMessage(new Integer(prog).toString());
		Log.e(TAG,"-Progress:"+new Integer(prog).toString());
	}
	
	private void sendPiece() {
		if (mFileName == null || mFileName.length() == 0) {
			showFailureToast("No video selected.");
			return;
		}
		
		File myFile = new File(mFileName);
		RequestParams params = new RequestParams();
		try {
			params.put("video_file_value", myFile);
		} catch (FileNotFoundException e) {
		}
		// Send piece
		params.add("story_id", model.getId().toString());
		params.add("uuid", uuid.toString());
		params.add("piece_index", model.getNext_available_piece().toString());
		//Show Progress Bar
		showProgressDialog(0);
		ServerIO.getInstance().post(ServerIO.INSERT_STORY_PIECE_URL, params,
				new JsonHttpResponseHandler() {
					
					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						progress =(bytesWritten*100)/totalSize;
						updateProgresDialog(progress);
					}

					@Override
					public synchronized void onSuccess(JSONObject result) {
						cancelProgresDialog();
						// Delete file
						File temp = new File(mFileName);
						temp.delete();
						
						try {
							if (result.getInt("Status") == ServerIO.FAILURE) {
								Log.e(TAG, result.getString("Error"));
								changeViewToStoryPage(model.getId());
								return;
							}
						} catch (JSONException e1) {
							Log.e(TAG, e1.getMessage());
						}

						// Successful
						changeViewToStoryPage(model.getId());
					}
				});

	}

	private void changeViewToStoryPage(int story_id) {
		Intent intent = new Intent(this, StoryPageActivity.class);
		intent.putExtra(NewsfeedActivity.STORY_MODEL_KEY, this.model);
		startActivity(intent);
	}

	private static final int REQUEST_CODE = 1;

	public void pickImage(View view) {
		Intent intent = new Intent();
		intent.setType("video/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			try {
				InputStream is = getContentResolver().openInputStream(
						data.getData());
				File outputDir = this.getCacheDir(); // context being the
														// Activity pointer
				File outputFile = File
						.createTempFile("video", "mp4", outputDir);
				FileOutputStream writer = new FileOutputStream(outputFile);
				Utils.CopyStream(is, writer);
				writer.close();
				mFileName = outputFile.getAbsolutePath();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void showFailureToast(String msg) {
		Context context = this;
		CharSequence text = msg;
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

}
