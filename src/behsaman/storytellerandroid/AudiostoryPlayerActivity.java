package behsaman.storytellerandroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.Header;

import behsaman.storytellerandroid.datamodel.PieceModel;
import behsaman.storytellerandroid.networking.MyBinaryHttpResponseHandler;
import behsaman.storytellerandroid.networking.ServerIO;
import behsaman.storytellerandroid.utils.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;

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
	private MediaPlayer mPlayer = new MediaPlayer();

	// Current playing piece
	private int curPiece = 0;

	// Story Pieces
	ArrayList<Object> pieces = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audiostory_player);
		// Instantiate Progressbar
		fetchProgressbar = new ProgressDialog(this);
		//Get intent info
		Intent intent = getIntent();
 		this.pieces = (ArrayList<Object>) intent.getSerializableExtra(StoryPageActivity.STORY_PIECES_KEY);
		//Step one
		stepOne();
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
							for (Header h : headers)
								Log.e(TAG, "Header:" + h + "\t");
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
					
					//Clear buffer piece one
					bufferPieceOne= null;
					// Start Downloading piece two
					if (curPiece > pieces.size())
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
			return;// Nothing left to do
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
						for (Header h : headers)
							Log.e(TAG, "Header:" + h + "\t");
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
							Log.e(TAG, e.getMessage()==null?"NULL EXCEPTION":e.getMessage());
						}
						stepThree();
					}
				});

	}

	private synchronized void stepThree() {
		//Buffer two is ready && and buffer piece one is done with playing
		if (bufferPieceTwo != null && bufferPieceOne == null) // we have to first play buffer piece 2
		{
			mPlayer.release();
			mPlayer = new MediaPlayer();
			Log.d("TAG------->", "player is released & recreated");
			bufferPieceOne = new File(bufferPieceTwo.getAbsolutePath());
			bufferPieceTwo = null;
			stepTwo();
		} else { //buffertwo not ready
			//keep showing progress bar
		}
	}

	private void finishPlaying() {
		mPlayer.release();
		Log.e(TAG,"Finishedddddd");
	}

}
