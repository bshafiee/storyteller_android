package behsaman.storytellerandroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import behsaman.storytellerandroid.datamodel.StoryModel;
import behsaman.storytellerandroid.networking.ServerIO;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class AudioPieceActivity extends Activity {

	private static final String TAG = "AudioPieceActivity";
	private StoryModel model = null;
	private UUID uuid = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_piece);

		LinearLayout ll = new LinearLayout(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(params);

		mRecordButton = new RecordButton(this);
		params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		mRecordButton.setLayoutParams(params);
		ll.addView(mRecordButton, params);

		mPlayButton = new PlayButton(this);
		params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		mPlayButton.setLayoutParams(params);
		ll.addView(mPlayButton, params);

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
		model = (StoryModel) getIntent().getSerializableExtra(StoryPageActivity.STORY_MODEL_KEY);
		uuid = UUID.fromString(getIntent().getStringExtra(StoryPageActivity.UUID_KEY));
	}
	
	private void sendPiece() {
		File myFile = new File(mFileName);
		RequestParams params = new RequestParams();
		try {
		    params.put("audio_file_value", myFile);
		} catch(FileNotFoundException e) {}
		// Send piece
		params.add("story_id", model.getId().toString());
		params.add("uuid", uuid.toString());
		params.add("piece_index", model.getNext_available_piece().toString());
		ServerIO.getInstance().post(ServerIO.INSERT_STORY_PIECE_URL,
				params, new JsonHttpResponseHandler() {
					@Override
					public synchronized void onSuccess(JSONObject result) {
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
		intent.putExtra(NewsfeedActivity.STORY_ID, story_id);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.audio_piece, menu);
		return true;
	}

	private static final String LOG_TAG = "AudioRecordTest";
	private static String mFileName = null;

	private Button sendButton = null;

	private RecordButton mRecordButton = null;
	private MediaRecorder mRecorder = null;

	private PlayButton mPlayButton = null;
	private MediaPlayer mPlayer = null;

	private void onRecord(boolean start) {
		if (start) {
			startRecording();
		} else {
			stopRecording();
		}
	}

	private void onPlay(boolean start) {
		if (start) {
			startPlaying();
		} else {
			stopPlaying();
		}
	}

	private void startPlaying() {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}

	private void stopPlaying() {
		mPlayer.release();
		mPlayer = null;
	}

	private void startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}

		mRecorder.start();
	}

	private void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

	class RecordButton extends Button {
		boolean mStartRecording = true;

		private Handler handler = new Handler();
		TimerRunnable runnable = null;

		private class TimerRunnable implements Runnable {
			Long startTime = null;

			@Override
			public void run() {
				/* do what you need to do */
				if (startTime == null)
					startTime = System.currentTimeMillis();
				Long elapsed = (System.currentTimeMillis() - startTime) / 1000;
				setText(elapsed.toString());
				/* and here comes the "trick" */
				handler.postDelayed(this, 1000);
			}

			public void reset() {
				this.startTime = null;
			}

		};

		OnClickListener clicker = new OnClickListener() {
			public void onClick(View v) {
				onRecord(mStartRecording);
				if (mStartRecording) {
					setText("Stop recording");
					runnable = new TimerRunnable();
					handler.postDelayed(runnable, 1000);
				} else {
					handler.removeCallbacks(runnable);
					setText("Start recording");
				}
				mStartRecording = !mStartRecording;
			}
		};

		public RecordButton(Context ctx) {
			super(ctx);
			setText("Start recording");
			setOnClickListener(clicker);
		}

	}

	class PlayButton extends Button {
		boolean mStartPlaying = true;

		OnClickListener clicker = new OnClickListener() {
			public void onClick(View v) {
				onPlay(mStartPlaying);
				if (mStartPlaying) {
					setText("Stop playing");
				} else {
					setText("Start playing");
				}
				mStartPlaying = !mStartPlaying;
			}
		};

		public PlayButton(Context ctx) {
			super(ctx);
			setText("Start playing");
			setOnClickListener(clicker);
		}
	}

	public AudioPieceActivity() {
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/audiorecordtest.3gp";
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
}
