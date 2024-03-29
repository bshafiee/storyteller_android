package behsaman.storytellerandroid;

import java.util.ArrayList;
import java.util.Date;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import behsaman.storytellerandroid.datamodel.LOCK_TIME_MINS;
import behsaman.storytellerandroid.datamodel.MAX_MULTIMEDIA_PIECE_LENGTH_TYPE;
import behsaman.storytellerandroid.datamodel.MAX_NUM_PIECES_TYPE;
import behsaman.storytellerandroid.datamodel.MAX_TEXT_PIECE_LENGTH_TYPE;
import behsaman.storytellerandroid.datamodel.STORY_TYPE;
import behsaman.storytellerandroid.datamodel.StoryModel;
import behsaman.storytellerandroid.networking.ServerIO;
import behsaman.storytellerandroid.utils.Utils;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class NewsfeedActivity extends ActionBarActivity {
	private static final String TAG = "NewsfeedActivity";

	public static final String STORY_MODEL_KEY = "behsaman.storytellerandroid.NewsfeedActivity.STORYMODELKEY";

	private ListView list;
	private LazyAdapterStories adapter;
	private ArrayList<Object> stories;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_new_story:
			changeViewToCreateNewStory();
			return true;
		case R.id.action_logout:
			ServerIO.getInstance().logout(this);
		}
		
		return false;
	}

	public void changeViewToCreateNewStory() {
		Intent intent = new Intent(this, NewStoryActivity.class);
		startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newfeed);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		//Initialization all the application initialization codes
		super.onResume();
		updateStoryList();
	}

	private void updateStoryList() {
		final Activity newsActivity = this;
		RequestParams params = new RequestParams();
		params.add("limit", "100");
		ServerIO.getInstance().post(ServerIO.GET_STORY_URL, params,
				new JsonHttpResponseHandler() {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						ServerIO.getInstance().connectionError(
								NewsfeedActivity.this);
					}

					@Override
					public synchronized void onSuccess(JSONObject result) {
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
						stories = new ArrayList<Object>();
						for (int i = 0; i < arr.length(); i++) {
							try {
								JSONObject obj = (JSONObject) arr.get(i);
								int id = obj.getInt("id");
								int owner_id = obj.getInt("owner_id");
								Integer category_id = obj.getInt("category_id");
								String title = obj.getString("title");
								STORY_TYPE type = STORY_TYPE.valueOf(obj
										.getString("type"));
								MAX_NUM_PIECES_TYPE max_num_pieces = MAX_NUM_PIECES_TYPE
										.valueOf(obj
												.getString("max_num_pieces"));
								MAX_MULTIMEDIA_PIECE_LENGTH_TYPE max_multimedia_piece_length = MAX_MULTIMEDIA_PIECE_LENGTH_TYPE.valueOf(obj
										.getString("max_multimedia_piece_length"));
								MAX_TEXT_PIECE_LENGTH_TYPE max_text_piece_length = MAX_TEXT_PIECE_LENGTH_TYPE.valueOf(obj
										.getString("max_text_piece_length"));
								LOCK_TIME_MINS lock_time_mins = LOCK_TIME_MINS
										.valueOf(obj
												.getString("lock_time_mins"));
								int next_available_piece = obj
										.getInt("next_available_piece");
								Date created_on = Utils.parseDate(
										StoryModel.DATE_FORMAT,
										obj.getString("created_on"));

								final StoryModel story = new StoryModel(id,
										owner_id, category_id.toString(),
										title, type, max_num_pieces,
										max_multimedia_piece_length,
										max_text_piece_length, lock_time_mins,
										next_available_piece, created_on);
								stories.add(story);

								// Add to UI
								list = (ListView) findViewById(R.id.list);
								// Getting adapter by passing xml data ArrayList
								adapter = new LazyAdapterStories(newsActivity,
										stories);
								list.setAdapter(adapter);

								// Click event for single list row
								list.setOnItemClickListener(new OnItemClickListener() {

									@Override
									public void onItemClick(
											AdapterView<?> parent, View view,
											int position, long id) {
										LazyAdapterStories adaptor = (LazyAdapterStories) list
												.getAdapter();
										int story_id = adaptor
												.getStoryID(position);
										if (story_id > 0)
											ChangeViewToStoryPage((StoryModel) stories
													.get(position));
										else
											showFailureToast();

									}
								});
							} catch (JSONException e) {
								Log.e(TAG,
										"Error in getting JSONObject: "
												+ e.getMessage());
							}
						}
					}
				});
	}

	private void ChangeViewToStoryPage(StoryModel story) {
		Intent intent = new Intent(this, StoryPageActivity.class);
		intent.putExtra(STORY_MODEL_KEY, story);
		startActivity(intent);
	}

	public void showFailureToast() {
		Context context = this;
		CharSequence text = "Sorry, can't retrive story id :(";
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setMessage("Are you sure you want to exit?")
				.setCancelable(false)
				.setPositiveButton("Yes :(",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								NewsfeedActivity.this.finish();
								Intent intent = new Intent(Intent.ACTION_MAIN);
								intent.addCategory(Intent.CATEGORY_HOME);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							}
						}).setNegativeButton("No :)", null).show();
	}

}