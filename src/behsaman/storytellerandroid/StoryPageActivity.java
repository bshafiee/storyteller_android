package behsaman.storytellerandroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import behsaman.storytellerandroid.datamodel.PieceModel;
import behsaman.storytellerandroid.datamodel.STORY_TYPE;
import behsaman.storytellerandroid.datamodel.StoryModel;
import behsaman.storytellerandroid.networking.MyBinaryHttpResponseHandler;
import behsaman.storytellerandroid.networking.ServerIO;
import behsaman.storytellerandroid.utils.Utils;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class StoryPageActivity extends Activity {

	private static final String TAG = "StoryPageActivity";

	public static final String STORY_MODEL_KEY = "behsaman.storytellerandroid.StoryPageActivity.STORY_MODEL";
	public static final String STORY_PIECES_KEY = "behsaman.storytellerandroid.StoryPageActivity.STORY_PIECES";
	public static final String STORY_SELECTED_PIECE_KEY = "behsaman.storytellerandroid.StoryPageActivity.STORY_SELECTED_PIECE";
	public static final String UUID_KEY = "behsaman.storytellerandroid.StoryPageActivity.UUID";

	private static final int FETCH_TIMEOUT = 5000;

	private StoryModel model;
	ArrayList<Object> pieces = new ArrayList<Object>();
	private UUID generatedUUID = null;

	// Create ProgressBar
	private ProgressDialog fetchProgressBar;

	// Global Play Lock
	private boolean isPlaying = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_page);

		// Get Story 
		Intent intent = getIntent();
		this.model = (StoryModel) intent.getSerializableExtra(NewsfeedActivity.STORY_MODEL_KEY);

		this.updateView();
		this.updateContirbuteionStatus();
		this.updatePieces();
	}

	private void updatePieces() {
		// Invalid id
		if (model == null)
			return;

		RequestParams params = new RequestParams();
		params.add("story_id", model.getId().toString());
		params.add("from_index_inclusive", "0");
		params.add("limit", "-1");// all
		ServerIO.getInstance().post(ServerIO.GET_PIECE_URL, params,
				new JsonHttpResponseHandler() {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						ServerIO.getInstance().connectionError(StoryPageActivity.this);
					}
					
					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseBody, Throwable e) {
							ServerIO.getInstance().connectionError(StoryPageActivity.this);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable e, JSONArray errorResponse) {
						Log.e(TAG, "INJA6");
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							Throwable e, JSONObject errorResponse) {
						Log.e(TAG, "INJA5");
					}

					@Override
					public void onFailure(int statusCode, Throwable e,
							JSONArray errorResponse) {
						Log.e(TAG, "INJA4");
					}

					@Override
					public void onFailure(int statusCode, Throwable e,
							JSONObject errorResponse) {
						Log.e(TAG, "INJA3");
					}

					@Override
					public void onFailure(Throwable e, JSONArray errorResponse) {
						Log.e(TAG, "INJA2");
					}

					@Override
					public void onFailure(Throwable e, JSONObject errorResponse) {
						Log.e(TAG, "INJA1");
					}

					@Override
					public synchronized void onSuccess(final JSONObject result) {
						try {
							if (result.getInt("Status") != ServerIO.SUCCESS) {
								Log.e(TAG, result.getString("Error"));
								return;
							}
						} catch (JSONException e1) {
							Log.e(TAG, e1.getMessage());
						}
						

						JSONArray arr = null;
						try {
							arr = result.getJSONArray("data");
						} catch (JSONException e1) {
							Log.e(TAG, e1.getMessage());
						}
						JSONObject obj = null;
						for (int i = 0; i < arr.length(); i++) {
							try {
								obj = (JSONObject) arr.get(i);

								int id = obj.getInt("id");
								int index = obj.getInt("index");
								String text_val = obj.has("text_val") ? obj
										.getString("text_val") : null;
								String audio_val = obj.has("audio_file_addr") ? obj
										.getString("audio_file_addr") : null;
								String video_file_addr = obj
										.has("video_file_addr") ? obj
										.getString("video_file_addr") : null;
								String picture_file_addr = obj
										.has("picture_file_addr") ? obj
										.getString("picture_file_addr") : null;
								int creator_id = obj.getInt("creator_id");
								Date date = Utils.parseDate(
										StoryModel.DATE_FORMAT,
										obj.getString("created_on"));
								PieceModel p = new PieceModel(id, model.getId(),
										creator_id, index, text_val, audio_val,
										video_file_addr, picture_file_addr,
										date);
								pieces.add(p);
							} catch (JSONException e) {
								Log.e(TAG,
										"Error in getting JSONObject: "
												+ ((obj == null) ? "null" : obj
														.toString())
												+ "\tError:" + e.getMessage());
							}

						}
						
						// Story Specific Update
						if (model.getType() == STORY_TYPE.TEXT_ONLY)
							updateTextStoryPieces(pieces);
						else if (model.getType() == STORY_TYPE.AUDIO)
							updateAudioStoryPieces(pieces);
						else if (model.getType() == STORY_TYPE.COMICS)
							updateTextStoryPieces(pieces);
						else if (model.getType() == STORY_TYPE.VIDEO)
							updateVideoStoryPieces(pieces);
						else
							Log.e(TAG,"WHAT THE FFUCKKKKK ?"+model.getType());
					}
				});

	}

	private void updateAudioStoryPieces(ArrayList<Object> pieces) {
		final Activity storyPageActivity = this;

		// Add to UI
		ListView list = (ListView) findViewById(R.id.list_story_page_pieces);
		// Getting adapter by passing xml data ArrayList
		final LazyAdapterAudioPieces adapter = new LazyAdapterAudioPieces(
				storyPageActivity, (ArrayList<Object>) pieces);
		list.setAdapter(adapter);
		// Click event for single list row
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			}
		});
	}

	private void updateTextStoryPieces(ArrayList<Object> pieces) {
		final Activity storyPageActivity = this;
		// Add to UI
		ListView list = (ListView) findViewById(R.id.list_story_page_pieces);
		// Getting adapter by passing xml data ArrayList
		LazyAdapterTextPieces adapter = new LazyAdapterTextPieces(
				storyPageActivity, (ArrayList<Object>) pieces);
		list.setAdapter(adapter);

		final Context curContext = this;
		final ArrayList<Object> curPieces = pieces;
		// Click event for single list row
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Go to Show View
				Intent intent = new Intent(curContext,
						TextviewerSlideActivity.class);
				intent.putExtra(STORY_PIECES_KEY, curPieces);
				intent.putExtra(STORY_MODEL_KEY, model);
				intent.putExtra(STORY_SELECTED_PIECE_KEY, position);
				startActivity(intent);
			}
		});
	}
	
	private void updateVideoStoryPieces(ArrayList<Object> pieces) {
		final Activity storyPageActivity = this;
		// Add to UI
		ListView list = (ListView) findViewById(R.id.list_story_page_pieces);
		// Getting adapter by passing xml data ArrayList
		LazyAdapterTextPieces adapter = new LazyAdapterTextPieces(
				storyPageActivity, (ArrayList<Object>) pieces);
		list.setAdapter(adapter);

		final Context curContext = this;
		final ArrayList<Object> curPieces = pieces;
		// Click event for single list row
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Go to Show View
				Intent intent = new Intent(curContext,
						TextviewerSlideActivity.class);
				intent.putExtra(STORY_PIECES_KEY, curPieces);
				intent.putExtra(STORY_MODEL_KEY, model);
				intent.putExtra(STORY_SELECTED_PIECE_KEY, position);
				startActivity(intent);
			}
		});
	}
	
	private void updateView() {
		// Invalid id
		if (model == null)
			return;

		// Setting all values in listview
		TextView titleBox = (TextView) findViewById(R.id.tv_story_page_title);
		titleBox.setText(model.getTitle());
		String description = "Category: " + model.getCategory();
		description += "\nCreated By: " + model.getOwner_id();
		description += "\nPieces Left: "
				+ (model.getMax_num_pieces().getNumVal() - model.getNext_available_piece());
		description += "\nStory Type: " + (model.getType().toString());
		TextView infoBox = (TextView) findViewById(R.id.tv_story_page_info);
		infoBox.setText(description);
		Button readButton = (Button) findViewById(R.id.bt_story_page_read);
		switch (model.getType()) {
		case AUDIO:
			readButton.setText("Listen to this Story");
			break;
		case COMICS:
			readButton.setText("Read this Story");
			break;
		case TEXT_ONLY:
			readButton.setText("Read this Story");
			break;
		case VIDEO:
			readButton.setText("Watch this Story");
			break;
		default:
			break;
		}
	}
	private void updateContirbuteionStatus() {
		// Invalid id
		if (model == null)
			return;

		// Contrib Button
		Button contribButton = (Button) findViewById(R.id.bt_story_page_contribute);
		contribButton.setText("Request to Contribute");
		contribButton.setEnabled(false);

		// Send Contribution Request
		RequestParams params = new RequestParams();
		params.add("story_id", model.getId().toString());
		ServerIO.getInstance().post(ServerIO.HAS_REQ_CONTRIBUTION_URL, params,
				new JsonHttpResponseHandler() {
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						ServerIO.getInstance().connectionError(StoryPageActivity.this);
					}
			
					@Override
					public synchronized void onSuccess(JSONObject result) {
						try {
							if (result.getInt("Status") == ServerIO.FAILURE) {
								// No request for this story && Enable Contrib
								// button
								if (model.getMax_num_pieces().getNumVal() > model
										.getNext_available_piece()) {
									Button contribButton = (Button) findViewById(R.id.bt_story_page_contribute);
									contribButton
											.setText("Request to Contribute");
									contribButton.setEnabled(true);
								}
								return;
							}
						} catch (JSONException e1) {
							Log.e(TAG, e1.getMessage());
						}
						// we already sent a request
						try {
							JSONObject obj = result.getJSONObject("data");
							long request_date = obj.getLong("request_date");
							int queue_index = obj.getInt("queue_index");
							Long leftMins = new Date().getTime() - request_date;
							leftMins /= 1000;// second
							leftMins /= 60;// Mins
							Log.e(TAG, "Curr:" + new Date().getTime()
									+ "\tReqDate:" + request_date);
							generatedUUID = UUID.fromString(obj
									.getString("generatedUUID"));

							TextView infoBox = (TextView) findViewById(R.id.tv_story_page_contrib_info);
							infoBox.setText("You have " + leftMins
									+ " minutes left to send your piece!");
							// Contribute Button
							if (queue_index == 1)// our turn
							{
								Button contribButton = (Button) findViewById(R.id.bt_story_page_contribute);
								contribButton.setEnabled(true);
								contribButton.setText("Contribute");
							} else {
								Button contribButton = (Button) findViewById(R.id.bt_story_page_contribute);
								contribButton.setEnabled(false);
								contribButton.setText("Waiting");
							}

						} catch (Exception e) {
							Log.e(TAG, e.getMessage());
						}
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.story_page, menu);
		return true;
	}

	public void ContributeHandler(View v) {
		// Invalid id
		if (model == null)
			return;

		Button contribButton = (Button) findViewById(R.id.bt_story_page_contribute);
		if (contribButton.getText().equals("Request to Contribute"))
			sendContributeRequest();
		else
			goToContribetePage();

	}

	private void goToContribetePage() {
		if (model.getType() == STORY_TYPE.TEXT_ONLY)
			changeViewToNewPiece(model, generatedUUID, TextPieceActivity.class);
		else if(model.getType() == STORY_TYPE.COMICS)
			changeViewToNewPiece(model, generatedUUID, TextPieceActivity.class);
		else if (model.getType() == STORY_TYPE.AUDIO)
			changeViewToNewPiece(model, generatedUUID, AudioPieceActivity.class);
		else if (model.getType() == STORY_TYPE.VIDEO)
			changeViewToNewPiece(model, generatedUUID, VideoPieceActivity.class);
	}

	private void sendContributeRequest() {
		// Send Contribution Request
		RequestParams params = new RequestParams();
		params.add("story_id", model.getId().toString());
		ServerIO.getInstance().post(ServerIO.CONTRIBUTE_REQUEST_URL, params,
				new JsonHttpResponseHandler() {
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						ServerIO.getInstance().connectionError(StoryPageActivity.this);
					}
			
					@Override
					public synchronized void onSuccess(JSONObject result) {
						try {
							if (result.getInt("Status") == ServerIO.FAILURE) {
								Log.e(TAG, result.getString("Error"));
								return;
							}
						} catch (JSONException e1) {
							Log.e(TAG, e1.getMessage());
						}
						try {
							JSONObject obj = result.getJSONObject("data");
							generatedUUID = UUID.fromString(obj
									.getString("uuid"));
							int queueSize = obj.getInt("queueIndex");
							Date expDate = new Date(obj.getLong("expDate"));
							// Update View
							updateContirbuteionStatus();
							// Check Queue Size
							if (queueSize == 1) {// our turn
								goToContribetePage();
							} else {
								updateContirbuteionStatus();
							}
						} catch (JSONException e) {
							Log.e(TAG, e.getMessage());
						}
					}
				});

	}

	private void changeViewToNewPiece(StoryModel model, UUID uuid, Class c) {
		Intent intent = new Intent(this, c);
		intent.putExtra(STORY_MODEL_KEY, model);
		intent.putExtra(UUID_KEY, uuid.toString());
		startActivity(intent);
	}

	public void playAudioPieceHandler(View view) {
		// No Concurrent play
		if (isPlaying)
			return;

		String[] allowedContentTypes = new String[] {};
		final String dir = Utils.getCacheDir(this).getAbsolutePath();
		final PieceModel piece = getPieceById((Integer) view.getTag());
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout_story_page);
		List<View> views = findViewWithTagRecursively(layout, piece.getId());
		SeekBar tempSeekBar = null;
		for (View v : views)
			if (v instanceof SeekBar)
				tempSeekBar = (SeekBar) v;
		final SeekBar sBar = tempSeekBar;
		ServerIO.getInstance().download(piece.getAudio_file_addr(),
				new MyBinaryHttpResponseHandler(allowedContentTypes) {

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
							playAudioFile(f, sBar);
						} catch (Exception e) {
							Log.e(TAG, e.getMessage());
						}
					}
				});
	}

	private void playAudioFile(File file, final SeekBar seekBar) {
		final MediaPlayer player = new MediaPlayer();
		try {
			player.setDataSource(file.getAbsolutePath());
			player.prepare();
			seekBar.setProgress(0);
			seekBar.setMax(player.getDuration());
			final Handler seekHandler = new Handler();
			final Runnable run = new Runnable() {

				@Override
				public void run() {
					seekBar.setProgress(player.getCurrentPosition());
					seekHandler.postDelayed(this, 100);
				}
			};
			setPlaying(true);
			player.start();
			run.run();
			player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					seekHandler.removeCallbacks(run);
					player.release();
					setPlaying(false);
				}
			});
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private PieceModel getPieceById(int ID) {
		for (Object o : pieces) {
			if (((PieceModel) o).getId() == ID)
				return (PieceModel) o;
		}
		return null;
	}

	/**
	 * Get all the views which matches the given Tag recursively
	 * 
	 * @param root
	 *            parent view. for e.g. Layouts
	 * @param tag
	 *            tag to look for
	 * @return List of views
	 */
	public static List<View> findViewWithTagRecursively(ViewGroup root,
			Object tag) {
		List<View> allViews = new ArrayList<View>();

		final int childCount = root.getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View childView = root.getChildAt(i);

			if (childView instanceof ViewGroup) {
				allViews.addAll(findViewWithTagRecursively(
						(ViewGroup) childView, tag));
			} else {
				final Object tagView = childView.getTag();
				if (tagView != null && tagView.equals(tag))
					allViews.add(childView);
			}
		}

		return allViews;
	}

	private synchronized void setPlaying(boolean isPlay) {
		this.isPlaying = isPlay;
	}

	public void readStoryHandler(View v) {
		Intent intent = null;
		if (pieces == null) {
			showFailureToast("Sorry :(\nFailed to fetch Story Pieces.");
			return;
		} else if (pieces.size() == 0) {
			showFailureToast("This Story has not piece yet.");
			return;
		}
		switch (model.getType()) {
		case TEXT_ONLY:
			intent = new Intent(this, TextviewerSlideActivity.class);
			intent.putExtra(STORY_PIECES_KEY, pieces);
			intent.putExtra(STORY_MODEL_KEY, model);
			startActivity(intent);
			break;
		case AUDIO:
			intent = new Intent(this, AudiostoryPlayerActivity.class);
			intent.putExtra(STORY_PIECES_KEY, pieces);
			startActivity(intent);
			break;

		default:
			break;
		}

	}

	private void showFailureToast(String msg) {
		Context context = this;
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, msg, duration);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, NewsfeedActivity.class);
		startActivity(intent);
	}

}
