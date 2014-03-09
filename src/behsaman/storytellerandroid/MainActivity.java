package behsaman.storytellerandroid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import behsaman.storytellerandroid.videoplayer.Video;
import behsaman.storytellerandroid.videoplayer.VideoPlayerActivity;

public class MainActivity extends ActionBarActivity {
	LinedEditText lined;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

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
		}
		return false;
	}

	public void changeViewToCreateNewStory() {
		Intent intent = new Intent(this, NewStoryActivity.class);
		startActivity(intent);

	}

	public void changeView(View v) {
		Intent intent = new Intent(this, NewsfeedActivity.class);
		startActivity(intent);
	}

	public void basichttp(View view) {
		Intent intent = null;
		intent = new Intent(this, VideoPlayerActivity.class);
		// Create a video object to be passed to the activity
		Video video = new Video("https://behrooz:Behrooz2@www.noveldevelopments.com:8443/static/video.mp4");
		video.setTitle("Big Buck Bunny");
		video.setAuthor("the Blender Institute");
		video.setDescription("A short computer animated film by the Blender Institute, part of the Blender Foundation. Like the foundation's previous film Elephants Dream, the film was made using Blender, a free software application for animation made by the same foundation. It was released as an Open Source film under Creative Commons License Attribution 3.0.");

		// Launch the activity with some extras
		intent.putExtra(VideoPlayerActivity.EXTRA_LAYOUT, "1");
		intent.putExtra(Video.class.getName(), video);
		startActivity(intent);
	}

	private static final int REQUEST_CODE = 1;
	private Bitmap bitmap;
	private ImageView imageView;

	public void pickImage(View view) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, REQUEST_CODE);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
			try {
				// We need to recyle unused bitmaps
				if (bitmap != null) {
					bitmap.recycle();
				}
				InputStream stream = getContentResolver().openInputStream(
						data.getData());
				bitmap = BitmapFactory.decodeStream(stream);
				stream.close();
				imageView.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
