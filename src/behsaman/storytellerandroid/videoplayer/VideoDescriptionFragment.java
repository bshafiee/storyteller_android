package behsaman.storytellerandroid.videoplayer;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import behsaman.storytellerandroid.R;

public class VideoDescriptionFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_videodescription, null);
		return view;
	}
	
}
