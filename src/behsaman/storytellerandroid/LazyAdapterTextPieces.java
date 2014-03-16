package behsaman.storytellerandroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import behsaman.storytellerandroid.datamodel.PieceModel;
import behsaman.storytellerandroid.datamodel.StoryModel;
import behsaman.storytellerandroid.networking.MyBinaryHttpResponseHandler;
import behsaman.storytellerandroid.networking.ServerIO;
import behsaman.storytellerandroid.utils.Utils;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class LazyAdapterTextPieces extends LazyAdapter {

	private static final String TAG = "LazyAdapterTextPieces";

	public LazyAdapterTextPieces(Activity a, ArrayList<Object> d) {
		super(a, d);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.list_row, null);

		TextView title = (TextView) vi.findViewById(R.id.title); // title
		TextView story_desc = (TextView) vi.findViewById(R.id.story_news);
		TextView creation_date = (TextView) vi.findViewById(R.id.creation_date);
		ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image);

		PieceModel piece = (PieceModel) data.get(position);
		// Log.e("Mytag",
		// piece.getIndex()+"\t"+piece.getText_val()+"\t"+piece.getCreated_on().toString());
		// Setting all values in listview
		// title.setText(piece.getIndex().toString());
		title.setText("");
		story_desc.setText(piece.getText_val());
		creation_date.setText(DateFormat.format(StoryModel.DATE_FORMAT,
				piece.getCreated_on()));
		// for A video piece
		if (piece.getVideo_file_addr() != null
				&& piece.getVideo_file_addr().length() > 0)
			loadThumbnail(piece, thumb_image);
		return vi;
	}

	private void loadThumbnail(final PieceModel piece, final ImageView thumb_image) {
		// Get Image
		RequestParams params = new RequestParams();
		params.add("story_id", piece.getStory_id().toString());
		params.add("piece_id", piece.getIndex().toString());
		params.add("width", ServerIO.VIDEO_THUMBNAIL_WIDTH.toString());
		params.add("height", ServerIO.VIDEO_THUMBNAIL_HEIGHT.toString());
		ServerIO.getInstance().post(ServerIO.GET_VIDEO_THUMBNAIL_URL, params,
				new JsonHttpResponseHandler() {
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Log.e(TAG, "Error in fetching thumbnail");
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

						JSONObject obj = null;
						try {
							obj = result.getJSONObject("data");
						} catch (JSONException e1) {
							Log.e(TAG, e1.getMessage());
						}

						try {
							String url = obj.getString("url");
							// Start Download task
							ServerIO.getInstance().download(url,
									new MyBinaryHttpResponseHandler() {
										@Override
										public void onFailure(int statusCode,
												Header[] headers,
												byte[] binaryData,
												Throwable error) {
											Log.e(TAG,
													"FAILLLEEEDDD:"
															+ error.getMessage()
															+ "\tStatusCode:"
															+ statusCode
															+ "\tBinaryData:"
															+ binaryData);
										}

										@Override
										public void onSuccess(int statusCode,
												Header[] headers,
												byte[] binaryData) {
											String dir = Utils.getCacheDir(
													activity.getApplicationContext())
													.getAbsolutePath();
											File f = new File(dir
													+ "/"
													+ piece.getStory_id()
															.toString()
													+ piece.getId()
															.toString());
											try {
												FileOutputStream writer = new FileOutputStream(f);
												writer.write(binaryData);
												writer.close();
												FileInputStream inStream = new FileInputStream(f);
												Bitmap bitmap_skip = BitmapFactory
														.decodeStream(inStream);
												thumb_image.setImageBitmap(bitmap_skip);
											} catch (Exception e) {
												Log.e(TAG, e.getMessage());
											}
										}
									});
							//imageLoader.DisplayImage(authenticatedURL,thumb_image);
						} catch (JSONException e1) {
							Log.e(TAG, e1.getMessage());
						}
					}
				});
	}

}