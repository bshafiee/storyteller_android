package behsaman.storytellerandroid.videoplayer;

import java.io.Serializable;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import behsaman.storytellerandroid.R;
import behsaman.storytellerandroid.StoryPageActivity;
import behsaman.storytellerandroid.datamodel.PieceModel;
import behsaman.storytellerandroid.datamodel.StoryModel;
import behsaman.storytellerandroid.networking.ServerIO;

public class VideoPlayerActivity extends BaseActivity {

	private static final String TAG = "VideoPlayerActivity";

	// Layout constants tested against to decide which activity layout to use
	private static final int LAYOUT_SIMPLE = 0;
	private static final int LAYOUT_DETAILED = 1;

	/**
	 * Reference to the instance for use with the dispatchKeyEvent override
	 * method.
	 */
	private Activity activity = this;

	/** The current activities configuration used to test screen orientation. */
	private Configuration configuration;

	/** The activities intent. */
	private Intent intent;

	/** Thread that runs in the background and checks for UI changes. */
	private Thread updateThread;

	/** A reference to the video description fragment. */
	private Fragment videodescriptionFragment;

	/** The video view where the video is rendered. */
	private VideoView videoView;

	/** A view that displays the video's length. */
	private TextView videoDuration;

	/** The videoview's media controller. */
	private MediaController controller;

	/** A video object containing information about the video. */
	private Video video;

	/** The layout this instance of the activity is using. */
	private int layout = -1;

	/** Story Viedo pieces */
	private ArrayList<Object> pieces = null;

	/** Story Model */
	private StoryModel model = null;

	/** Next piece to be played form Pieces array */
	private int nextPiece = 0;

	public static final String EXTRA_LAYOUT = "layout";
	
	//Video Info
	TextView videoTitle;
	TextView videoCredits;
	TextView videoDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		configuration = getResources().getConfiguration();
		intent = getIntent();

		// Get Layout parameter
		String layoutParam = intent.getStringExtra(EXTRA_LAYOUT);
		layout = Integer.parseInt(layoutParam ==null? "1":layoutParam);

		if (intent.getSerializableExtra(StoryPageActivity.STORY_MODEL_KEY) != null) {
			// Check if we should play a whole story
			loadStoryPieces();
		} else {
			// Loads in extras passed with the activity intent
			video = (Video) intent.getSerializableExtra(Video.class.getName());
		}

		// Sets the content view based on the layout
		switch (layout) {
		case LAYOUT_SIMPLE:
			setContentView(R.layout.activity_simple_videoplayer);
			break;
		case LAYOUT_DETAILED:
			setContentView(R.layout.activity_detailed_videoplayer);
			break;
		}

		// Load in references
		videoView = (VideoView) findViewById(R.id.fragmentvideoplayer_videoview);
		videoDuration = (TextView) findViewById(R.id.fragmentvideodescription_durationtextview);
		videoTitle = (TextView) findViewById(R.id.fragmentvideodescription_titletextview);
		videoCredits = (TextView) findViewById(R.id.fragmentvideodescription_authortextview);
		videoDescription = (TextView) findViewById(R.id.fragmentvideodescription_descriptiontextView);

		FragmentManager manager = getFragmentManager();
		videodescriptionFragment = manager
				.findFragmentById(R.id.fragment_videodetails);

		// Show the back button on the actionbar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Set the title on the actionbar to the video title
		setTitle(video.getTitle());

		// Create a custom media controller that ignores the back button
		controller = new MediaController(this) {
			@Override
			public boolean dispatchKeyEvent(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
					((Activity) activity).finish();

				return super.dispatchKeyEvent(event);
			}

		};

		// Attach the media controller
		videoView.setVideoURI(Uri.parse(video.getUrl()));
		videoView.setMediaController(controller);

		int videoPosition = 0;
		if (savedInstanceState != null)
			videoPosition = savedInstanceState.getInt("videoPosition");

		videoView.seekTo(videoPosition);
		videoView.start();

		// Fill in the detail fragment if one exists
		if (layout == LAYOUT_DETAILED) {
			videoTitle.setText(video.getTitle());
			videoCredits.setText("By " + video.getAuthor());
			videoDescription.setText(video.getDescription());
			videoDuration.setText("--:--");

			videoView.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					// Show the duration once the file has been parsed
					videoDuration.setText(getTime(videoView.getDuration()));
				}
			});
		}

		// Set On Completion Viewview
		videoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				playNextPiece();
			}
		});

		updateLayout();
		startUpdateThread();
	}

	private void startPlaying() {
		// Attach the media controller
		videoView.setVideoURI(Uri.parse(video.getUrl()));
		videoView.setMediaController(controller);

		int videoPosition = 0;
		videoView.seekTo(videoPosition);
		videoView.start();
		// Fill in the detail fragment if one exists
		if (layout == LAYOUT_DETAILED) {
			videoTitle.setText(video.getTitle());
			videoCredits.setText("By " + video.getAuthor());
			videoDescription.setText(video.getDescription());
			videoDuration.setText("--:--");
		}
	}

	private void playNextPiece() {
		if(pieces ==null || nextPiece == pieces.size()) {
			return;
		}
		
		//Get Video
		video = getVideoFromPiece((PieceModel) pieces.get(nextPiece++));
		//Set Video player
		videoView.stopPlayback();
		//Start Playing
		startPlaying();
	}

	private void loadStoryPieces() {
		Serializable s = intent
				.getSerializableExtra(StoryPageActivity.STORY_PIECES_KEY);
		this.pieces = (s != null) ? (ArrayList<Object>) s : null;
		this.model = (StoryModel) intent
				.getSerializableExtra(StoryPageActivity.STORY_MODEL_KEY);
		this.nextPiece = intent.getIntExtra(StoryPageActivity.STORY_SELECTED_PIECE_KEY,0);
		
		if (pieces == null || pieces.size() == 0) {
			Log.e(TAG, "No piece sent to Video player.");
			return;
		}

		// Create a video object to be passed to the activity
		PieceModel firstPiece = (PieceModel) pieces.get(nextPiece++);
		video = getVideoFromPiece(firstPiece);
	}

	private Video getVideoFromPiece(PieceModel piece) {
		String address = ServerIO.getInstance().getBasicAuthURL(piece.getVideo_file_addr());
		Video video = new Video(address);
		video.setTitle(model.getTitle());
		video.setAuthor(model.getOwner_id().toString());
		video.setDescription("Piece " + piece.getIndex());

		return video;
	}

	@Override
	protected void onResume() {
		startUpdateThread();
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (updateThread != null)
			updateThread.interrupt();

		super.onPause();
	}

	@Override
	public void finish() {
		if (updateThread != null)
			updateThread.interrupt();

		super.finish();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (videoView.isPlaying())
			outState.putInt("videoPosition", videoView.getCurrentPosition());
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		updateLayout();
	}

	@SuppressLint("InlinedApi")
	private void updateLayout() {
		if (layout == LAYOUT_DETAILED
				&& configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// Hide the description fragment
			FragmentManager fm = getFragmentManager();
			fm.beginTransaction().hide(videodescriptionFragment).commit();

			// Hide the status bar
			WindowManager.LayoutParams attrs = getWindow().getAttributes();
			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(attrs);

			// Hide the software buttons
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				View main_layout = findViewById(android.R.id.content)
						.getRootView();
				main_layout
						.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			}

			// Hide the media controller
			controller.hide();

			// Hide the actionbar
			getActionBar().hide();
		} else if (layout == LAYOUT_DETAILED
				&& configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
			// Show the description fragment
			FragmentManager fm = getFragmentManager();
			fm.beginTransaction().show(videodescriptionFragment).commit();

			// Show the software buttons
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				View main_layout = findViewById(android.R.id.content)
						.getRootView();
				main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}

			// Show the status bar
			WindowManager.LayoutParams attrs = getWindow().getAttributes();
			attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(attrs);

			// Show the actionbar
			getActionBar().show();
		} else if (layout == LAYOUT_SIMPLE) {
			// Hide the status bar
			WindowManager.LayoutParams attrs = getWindow().getAttributes();
			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(attrs);

			// Hide the software buttons
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				View main_layout = findViewById(android.R.id.content)
						.getRootView();
				main_layout
						.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			}

			// Hide the media controller
			controller.hide();

			// Hide the actionbar
			getActionBar().hide();
		}
	}

	@SuppressLint("InlinedApi")
	private void startUpdateThread() {
		if (updateThread == null || updateThread.isInterrupted()) {
			updateThread = new Thread(new Runnable() {
				private View main_layout = findViewById(android.R.id.content)
						.getRootView();
				private Handler mHandler = new Handler();
				private boolean canShow = true;

				@Override
				public void run() {
					mHandler.postDelayed(this, 100);

					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
						int currentUi = main_layout.getSystemUiVisibility();

						if (currentUi == 0
								&& controller != null
								&& configuration != null
								&& configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
							try {
								if (!controller.isShowing() && canShow) {
									WindowManager.LayoutParams attrs = getWindow()
											.getAttributes();
									attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
									getWindow().setAttributes(attrs);

									controller.show();
									canShow = false;
								}
							} catch (WindowManager.BadTokenException ex) {
								// WindowManager$BadTokenException will be
								// caught and the app would not display
								// the 'Force Close' message
							} finally {
								if (!controller.isShowing()) {
									WindowManager.LayoutParams attrs = getWindow()
											.getAttributes();
									attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
									getWindow().setAttributes(attrs);

									main_layout
											.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
									canShow = true;
								}
							}
						} else if (layout == LAYOUT_DETAILED
								&& configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
							canShow = true;
						} else if (layout == LAYOUT_SIMPLE
								&& currentUi == 0
								&& controller != null
								&& configuration != null
								&& configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
							try {
								if (!controller.isShowing() && canShow) {
									WindowManager.LayoutParams attrs = getWindow()
											.getAttributes();
									attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
									getWindow().setAttributes(attrs);

									controller.show();
									canShow = false;
								}
							} catch (WindowManager.BadTokenException ex) {
								// WindowManager$BadTokenException will be
								// caught and the app would not display
								// the 'Force Close' message
							} finally {
								if (!controller.isShowing()) {
									WindowManager.LayoutParams attrs = getWindow()
											.getAttributes();
									attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
									getWindow().setAttributes(attrs);

									main_layout
											.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
									canShow = true;
								}
							}
						}
					}
				}
			});

			updateThread.start();
		}
	}

	/**
	 * Converts milliseconds given as an integer into a string formatted
	 * hh:mm:ss.
	 * 
	 * @param ms
	 *            Milliseconds to convert to formatted string hh:mm:ss.
	 * @return Formatted string.
	 */
	private String getTime(int ms) {
		// Convert the milliseconds to seconds, minutes, and hours
		int seconds = (int) (ms / 1000) % 60;
		int minutes = (int) ((ms / (1000 * 60)) % 60);
		int hours = (int) ((ms / (1000 * 60 * 60)) % 24);

		// Convert the values to strings
		StringBuilder fMinutes = new StringBuilder(String.valueOf(minutes));
		StringBuilder fSeconds = new StringBuilder(String.valueOf(seconds));
		StringBuilder fHours = new StringBuilder(String.valueOf(hours));

		// Insert a 0 in front of the values if they are single digit
		if (fSeconds.length() == 1)
			fSeconds.insert(0, "0");
		if (fMinutes.length() == 1)
			fMinutes.insert(0, "0");
		if (fHours.length() == 1)
			fHours.insert(0, "0");

		// Decide to display hours if it is over 0
		if (hours <= 0)
			return fMinutes + ":" + fSeconds;
		else
			return fHours + ":" + fMinutes + ":" + fSeconds;
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
