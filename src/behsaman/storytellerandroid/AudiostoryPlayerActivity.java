package behsaman.storytellerandroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.http.Header;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import behsaman.storytellerandroid.datamodel.PieceModel;
import behsaman.storytellerandroid.networking.MyBinaryHttpResponseHandler;
import behsaman.storytellerandroid.networking.ServerIO;
import behsaman.storytellerandroid.utils.Utils;
import behsaman.storytellerandroid.visualizer.VisualizerView;
import behsaman.storytellerandroid.visualizer.renderer.BarGraphRenderer;
import behsaman.storytellerandroid.visualizer.renderer.CircleBarRenderer;
import behsaman.storytellerandroid.visualizer.renderer.CircleRenderer;
import behsaman.storytellerandroid.visualizer.renderer.LineRenderer;

public class AudiostoryPlayerActivity extends Activity {

	private static final String TAG = "AudiostoryPlayerActivity";

	/**
	 * This is a simple 2 piece buffer audio fetcher: 1- wait to fetch first
	 * piece 2- first piece download complete: play first piece & start
	 * downloading second piece(if exist) 3- playing piece one finish download
	 * piece 2 finished: go to step 2 (consider piece 2 as piece one) download
	 * piece 2 not finished: go to step 1 (consider piece 2 as piece one)
	 */
	// Create ProgressBar
	private ProgressDialog fetchProgressbar;

	// Buffer Pieces
	private File bufferPieceOne = null, bufferPieceTwo = null;

	// Mediaplayer
	private MediaPlayer mPlayer = null;

	// Current playing piece
	private int curPiece = 0;

	// Story Pieces
	private ArrayList<Object> pieces = null;

	// Visualizer
	private VisualizerView mVisualizerView = null;
	
	//Is Playing?
	private boolean isPlaying = false;
	
	//LastPiece played
	private boolean lastPieceDownloaded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audiostory_player);
		// Instantiate Progressbar
		fetchProgressbar = new ProgressDialog(this);
		// Get intent info
		Intent intent = getIntent();
		Serializable s = intent.getSerializableExtra(StoryPageActivity.STORY_PIECES_KEY);
		this.pieces = (s != null)?(ArrayList<Object>)s:null;
	}
	
	public void playHandler(View v) {
		if(isPlaying)
			return;
		mPlayer = new MediaPlayer();
		setIsPlaying(true);
		this.lastPieceDownloaded = false;
		this.curPiece = 0;
		// Init visualizer
		initVisualizer();
		// Step one
		stepOne();
		Log.e(TAG,"STEP ONEEEEEEEE");
	}

	private void initVisualizer() {
		if(mVisualizerView != null)
			return;
		// We need to link the visualizer view to the media player so that
		// it displays something
		mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);
		mVisualizerView.link(mPlayer);

		// Start with just line renderer
		addCircleRenderer();
	}

	// Methods for adding renderers to visualizer
	private void addBarGraphRenderers() {
		Paint paint = new Paint();
		paint.setStrokeWidth(50f);
		paint.setAntiAlias(true);
		paint.setColor(Color.argb(200, 56, 138, 252));
		BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(16,
				paint, false);
		mVisualizerView.addRenderer(barGraphRendererBottom);

		Paint paint2 = new Paint();
		paint2.setStrokeWidth(12f);
		paint2.setAntiAlias(true);
		paint2.setColor(Color.argb(200, 181, 111, 233));
		BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(4, paint2,
				true);
		mVisualizerView.addRenderer(barGraphRendererTop);
	}

	private void addCircleBarRenderer() {
		Paint paint = new Paint();
		paint.setStrokeWidth(8f);
		paint.setAntiAlias(true);
		paint.setXfermode(new PorterDuffXfermode(Mode.LIGHTEN));
		paint.setColor(Color.argb(255, 222, 92, 143));
		CircleBarRenderer circleBarRenderer = new CircleBarRenderer(paint, 32,
				true);
		mVisualizerView.addRenderer(circleBarRenderer);
	}

	private void addCircleRenderer() {
		Paint paint = new Paint();
		paint.setStrokeWidth(3f);
		paint.setAntiAlias(true);
		paint.setColor(Color.argb(255, 222, 92, 143));
		CircleRenderer circleRenderer = new CircleRenderer(paint, true);
		mVisualizerView.addRenderer(circleRenderer);
	}

	private void addLineRenderer() {
		Paint linePaint = new Paint();
		linePaint.setStrokeWidth(1f);
		linePaint.setAntiAlias(true);
		linePaint.setColor(Color.argb(88, 0, 128, 255));

		Paint lineFlashPaint = new Paint();
		lineFlashPaint.setStrokeWidth(5f);
		lineFlashPaint.setAntiAlias(true);
		lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
		LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint,
				true);
		mVisualizerView.addRenderer(lineRenderer);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.audiostory_player, menu);
		return true;
	}

	private synchronized void stepOne() {
		
		if (bufferPieceOne == null) {
			final String dir = Utils.getCacheDir(this).getAbsolutePath();
			final PieceModel piece = (PieceModel) pieces.get(curPiece);
			// Start downloading piece one and show progress bar
			ServerIO.getInstance().download(piece.getAudio_file_addr(),
					new MyBinaryHttpResponseHandler() {
						@Override
						public void onFailure(int statusCode, Header[] headers,
								byte[] binaryData, Throwable error) {
							Log.e(TAG, "FAILLLEEEDDD:" + error.getMessage()
									+ "\tStatusCode:" + statusCode
									+ "\tBinaryData:" + binaryData);
							ServerIO.getInstance().connectionError(AudiostoryPlayerActivity.this);
						}

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								byte[] binaryData) {
							File f = new File(dir + "/"
									+ piece.getStory_id().toString()
									+ piece.getId().toString());
							try {
								FileOutputStream writer = new FileOutputStream(
										f);
								writer.write(binaryData);
								writer.close();
								// End of step one
								curPiece++;
								bufferPieceOne = f;
								stepTwo();
							} catch (Exception e) {
								Log.e(TAG, e.getMessage());
							}
						}
					});
		} else {
			// End of step one
			curPiece++;
			stepTwo();
		}
	}

	private synchronized void stepTwo() {
		// Play buffer piece one
		try {
			mPlayer.setDataSource(bufferPieceOne.getAbsolutePath());
			mPlayer.prepare();
			/*
			 * seekBar.setProgress(0); seekBar.setMax(player.getDuration());
			 * final Handler seekHandler = new Handler(); final Runnable run =
			 * new Runnable() {
			 * 
			 * @Override public void run() {
			 * seekBar.setProgress(player.getCurrentPosition());
			 * seekHandler.postDelayed(this, 100); } };
			 */
			mPlayer.start();
			// run.run();
			mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					// seekHandler.removeCallbacks(run);

					// Clear buffer piece one
					bufferPieceOne = null;
					Log.e(TAG, "CUR:"+curPiece);
					// Start Downloading piece two
					if (curPiece == pieces.size() && bufferPieceTwo==null && lastPieceDownloaded)
						finishPlaying();
					else
						stepThree();
				}
			});
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		// Start Downloading piece two (if exist)
		if (curPiece >= pieces.size())
		{
			this.lastPieceDownloaded = true;
			return;// Nothing left to do
		}
		final String dir = Utils.getCacheDir(this).getAbsolutePath();
		final PieceModel piece = (PieceModel) pieces.get(curPiece);
		// Start downloading piece one and show progress bar
		ServerIO.getInstance().download(piece.getAudio_file_addr(),
				new MyBinaryHttpResponseHandler() {
					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] binaryData, Throwable error) {
						Log.e(TAG, "FAILLLEEEDDD:" + error.getMessage()
								+ "\tStatusCode:" + statusCode
								+ "\tBinaryData:" + binaryData);
						ServerIO.getInstance().connectionError(AudiostoryPlayerActivity.this);
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] binaryData) {

						File f = new File(dir + "/"
								+ piece.getStory_id().toString()
								+ piece.getId().toString());
						try {
							FileOutputStream writer = new FileOutputStream(f);
							writer.write(binaryData);
							writer.close();
							// End of downloading second piece
							curPiece++;
							bufferPieceTwo = f;
						} catch (Exception e) {
							Log.e(TAG,
									e.getMessage() == null ? "NULL EXCEPTION"
											: e.getMessage());
						}
						stepThree();
					}
				});

	}

	private synchronized void stepThree() {
		// Buffer two is ready && and buffer piece one is done with playing
		if (bufferPieceTwo != null && bufferPieceOne == null) // we have to
																// first play
																// buffer piece
																// 2
		{
			if(mPlayer!=null)
				mPlayer.release();
			mPlayer = new MediaPlayer();
			mVisualizerView.link(mPlayer);
			Log.d("TAG------->", "player is released & recreated");
			bufferPieceOne = new File(bufferPieceTwo.getAbsolutePath());
			bufferPieceTwo = null;
			stepTwo();
		} else { // buffertwo not ready
			// keep showing progress bar
		}
	}

	private void finishPlaying() {
		setIsPlaying(false);
		mPlayer.release();
		mPlayer = null;
		bufferPieceOne = null;
		bufferPieceTwo = null;
		Log.e(TAG, "Finishedddddd");
	}

	private void cleanUp() {
		if (mPlayer != null) {
			if(mVisualizerView!=null)
				mVisualizerView.release();
			if(mPlayer.isPlaying())
				mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
		bufferPieceOne = null;
		bufferPieceTwo = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		cleanUp();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		cleanUp();
		super.onDestroy();
	}

	private synchronized void setIsPlaying(boolean val) {
		this.isPlaying = val;
	}
	
}
