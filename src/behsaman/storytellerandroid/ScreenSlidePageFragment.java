/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package behsaman.storytellerandroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.Header;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import behsaman.storytellerandroid.datamodel.PieceModel;
import behsaman.storytellerandroid.datamodel.STORY_TYPE;
import behsaman.storytellerandroid.networking.MyBinaryHttpResponseHandler;
import behsaman.storytellerandroid.networking.ServerIO;
import behsaman.storytellerandroid.utils.Utils;

/**
 * A fragment representing a single step in a wizard. The fragment shows a dummy title indicating
 * the page number, along with some dummy text.
 *
 * <p>This class is used by the {@link CardFlipActivity} and {@link
 * TextviewerSlideActivity} samples.</p>
 */
public class ScreenSlidePageFragment extends Fragment {
    
	private static final String TAG = "ScreenSlidePageFragment";
	/**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_MODEL = "model";
    public static final String ARG_TYPE = "type";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    //private int mPageNumber;

    private PieceModel dataModel = null;
    private STORY_TYPE storyType = null;
    private static Context parentContext;
    
    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ScreenSlidePageFragment create(PieceModel model,STORY_TYPE type,Context parentCon) {
    	parentContext = parentCon;
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MODEL, model);
        args.putString(ARG_TYPE, type.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragment() {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataModel = (PieceModel) getArguments().getSerializable(ARG_MODEL);
        storyType = STORY_TYPE.valueOf(getArguments().getString(ARG_TYPE));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_screen_slide_page, container, false);

        // Set the title view to show the page number.
        ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                //getString(R.string.title_template_step, mPageNumber + 1));
        		"Piece "+dataModel.getIndex());
        ((TextView) rootView.findViewById(R.id.textview_fragment_textviewer)).setText(
                this.dataModel.getText_val());
        
        final ImageView imgView = (ImageView)rootView.findViewById(R.id.imageViewComic);
        
        //Comics has picture as well
        if(storyType == STORY_TYPE.TEXT_ONLY)
        	imgView.setVisibility(View.INVISIBLE);
        else
        {
        	//Load picture
        	//dataModel.get
        	// Start downloading piece one and show progress bar
			ServerIO.getInstance().download(dataModel.getPicture_file_addr(),
					new MyBinaryHttpResponseHandler() {
						@Override
						public void onFailure(int statusCode, Header[] headers,
								byte[] binaryData, Throwable error) {
							Log.e(TAG, "FAILLLEEEDDD:" + error.getMessage()
									+ "\tStatusCode:" + statusCode
									+ "\tBinaryData:" + binaryData);
							ServerIO.getInstance().connectionError(parentContext);
						}

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								byte[] binaryData) {
							String dir = Utils.getCacheDir(parentContext).getAbsolutePath();
							File f = new File(dir + "/"
									+ dataModel.getStory_id().toString()
									+ dataModel.getId().toString());
							try {
								FileOutputStream writer = new FileOutputStream(f);
								writer.write(binaryData);
								writer.close();
								FileInputStream inStream = new FileInputStream(f);
								Bitmap bitmap_skip = BitmapFactory.decodeStream(inStream);
								imgView.setImageBitmap(bitmap_skip);
							} catch (Exception e) {
								Log.e(TAG, e.getMessage());
							}
						}
					});
        }
        	
        

        return rootView;
    }

}
