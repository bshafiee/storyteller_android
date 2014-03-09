package behsaman.storytellerandroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import behsaman.storytellerandroid.datamodel.STORY_TYPE;
import behsaman.storytellerandroid.datamodel.StoryModel;
import behsaman.storytellerandroid.networking.ServerIO;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class TextPieceActivity extends Activity {

	private static final String TAG = "TextPieceActivity";

	private static final String CURRENT_TEXT_KEY = "TextPieceActivity.CURRENT_TEXT";
	private static final String CURRENT_CURSOR_POS_KEY = "TextPieceActivity.CURRENT_CURSOR_POS";
	private StoryModel model = null;
	private UUID uuid = null;
	// Components
	private LinedEditText textEditor;
	ImageButton comicbutton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text_piece);
		// UI Elements
		addUIComponents();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		this.textEditor.setText(savedInstanceState.getString(CURRENT_TEXT_KEY));
		this.textEditor.setSelection(savedInstanceState
				.getInt(CURRENT_CURSOR_POS_KEY));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(CURRENT_TEXT_KEY, this.textEditor.getText()
				.toString());
		outState.putInt(CURRENT_CURSOR_POS_KEY,
				this.textEditor.getSelectionStart());
	}

	private void addUIComponents() {
		// Get Intent Info
		model = (StoryModel) getIntent().getSerializableExtra(
				StoryPageActivity.STORY_MODEL_KEY);
		uuid = UUID.fromString(getIntent().getStringExtra(
				StoryPageActivity.UUID_KEY));

		LinearLayout layout_text_piece = (LinearLayout) findViewById(R.id.layout_text_piece);
		// Add Text Editor
		textEditor = new LinedEditText(this, null);
		LinearLayout.LayoutParams linLayout = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		int margins = 15;
		int height = (int) (getScreenHeight() * 6.0 / 9.0);
		linLayout.height = height;
		linLayout.setMargins(margins, margins, margins, margins);
		textEditor.setLayoutParams(linLayout);
		textEditor.setMinLines(12);
		textEditor.setHeight(height);

		// textEditor.setMaxHeight(layout_text_piece.getHeight()-layout_text_piece.getHeight()/4);
		layout_text_piece.addView(textEditor);
		// Add Text View
		final TextView tView = new TextView(this);
		tView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		tView.setText("0 of 500 Words");
		layout_text_piece.addView(tView);

		// Add Comics UI
		if (this.model.getType() == STORY_TYPE.COMICS)
			addComicsUI();

		// Button
		Button addPiecebutton = new Button(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		addPiecebutton.setLayoutParams(params);
		addPiecebutton.setText("Add Piece");
		layout_text_piece.addView(addPiecebutton);
		// Word Change event
		textEditor.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				int count = countWords(textEditor.getText().toString());
				tView.setText(count + " of 500");
			}
		});

		// Set Title
		TextView titleView = (TextView) findViewById(R.id.tv_text_piece_title);
		titleView.setText(model.getTitle());

		// On click handler
		addPiecebutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				submitPiece();
			}
		});

	}

	private void submitPiece() {
		String text_value = textEditor.getText().toString();
		if (text_value != null && text_value.length() > 0) {
			// Send piece
			RequestParams params = new RequestParams();
			params.add("story_id", model.getId().toString());
			params.add("uuid", uuid.toString());
			params.add("piece_index", model.getNext_available_piece()
					.toString());
			params.add("text_value", text_value);
			if (model.getType() == STORY_TYPE.COMICS) {
				File myFile = new File(bitmapFile);
				try {
					params.put("picture_file_value", myFile);
				} catch (FileNotFoundException e) {
				}
			}
			ServerIO.getInstance().post(ServerIO.INSERT_STORY_PIECE_URL,
					params, new JsonHttpResponseHandler() {
						@Override
						public void onFailure(int arg0, Header[] arg1,
								byte[] arg2, Throwable arg3) {
							ServerIO.getInstance().connectionError(
									TextPieceActivity.this);
						}

						@Override
						public synchronized void onSuccess(JSONObject result) {
							try {
								if (result.getInt("Status") == ServerIO.FAILURE) {
									Log.e(TAG, result.getString("Error"));
									showFailureToast();
									return;
								} else
									// Successful
									changeViewToStoryPage(model.getId());

							} catch (JSONException e1) {
								Log.e(TAG, e1.getMessage());
							}
						}
					});
		}
	}

	private void addComicsUI() {
		// Image Button
		comicbutton = new ImageButton(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		comicbutton.setImageResource(R.drawable.ic_comic);
		comicbutton.setLayoutParams(params);
		LinearLayout layout_text_piece = (LinearLayout) findViewById(R.id.layout_text_piece);
		layout_text_piece.addView(comicbutton);

		comicbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pickImage(v);
			}
		});
	}

	private static final int REQUEST_CODE = 1;
	private Bitmap comicBitmap;
	private String bitmapFile = null;

	public void pickImage(View view) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, REQUEST_CODE);

	}

	public String getImagePath(Uri uri) {
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		cursor.moveToFirst();
		String document_id = cursor.getString(0);
		document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
		cursor.close();

		cursor = getContentResolver().query(
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				null, MediaStore.Images.Media._ID + " = ? ",
				new String[] { document_id }, null);
		cursor.moveToFirst();
		String path = cursor.getString(cursor
				.getColumnIndex(MediaStore.Images.Media.DATA));
		cursor.close();

		return path;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
			try {
				// We need to recyle unused bitmaps
				if (comicBitmap != null) {
					comicBitmap.recycle();
				}
				InputStream stream = getContentResolver().openInputStream(
						data.getData());
				bitmapFile = getImagePath(data.getData());
				comicBitmap = BitmapFactory.decodeStream(stream);
				stream.close();
				comicbutton.setImageBitmap(comicBitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void changeViewToStoryPage(int story_id) {
		Intent intent = new Intent(this, StoryPageActivity.class);
		intent.putExtra(NewsfeedActivity.STORY_MODEL_KEY, this.model);
		startActivity(intent);
	}

	public int countWords(String in) {
		if (in == null)
			return 0;
		String trim = in.trim();
		if (trim.isEmpty())
			return 0;
		return trim.split("\\s+").length; // separate string around spaces
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.text_piece, menu);
		return true;
	}

	@SuppressLint("NewApi")
	public int getScreenHeight() {
		int Measuredwidth = 0;
		int Measuredheight = 0;
		Point size = new Point();
		WindowManager w = getWindowManager();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			w.getDefaultDisplay().getSize(size);

			Measuredwidth = size.x;
			Measuredheight = size.y;
		} else {
			Display d = w.getDefaultDisplay();
			Measuredwidth = d.getWidth();
			Measuredheight = d.getHeight();
		}
		return Measuredheight;
	}

	public void showFailureToast() {
		Context context = this;
		CharSequence text = "Sorry, an error occured while adding your piece :(";
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
