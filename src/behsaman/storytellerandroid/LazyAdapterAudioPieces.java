package behsaman.storytellerandroid;

import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import behsaman.storytellerandroid.datamodel.PieceModel;

public class LazyAdapterAudioPieces extends LazyAdapter {
	
    public LazyAdapterAudioPieces(Activity a, ArrayList<Object> d) {
        super(a,d);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.audio_list_row, null);

        //TextView title = (TextView)vi.findViewById(R.id.title); // title
        //TextView story_desc = (TextView)vi.findViewById(R.id.story_news); 
        //TextView creation_date = (TextView)vi.findViewById(R.id.creation_date); 
        //ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); 
        ImageButton playButton = (ImageButton)vi.findViewById(R.id.bt_audio_row_play);
        SeekBar seekBar = (SeekBar)vi.findViewById(R.id.seekbar_audio_row);
        seekBar.setEnabled(false);
        
        
        PieceModel piece = (PieceModel) data.get(position);
        playButton.setTag(piece.getId());
        seekBar.setTag(piece.getId());
        
        //Log.e("Mytag", piece.getIndex()+"\t"+piece.getText_val()+"\t"+piece.getCreated_on().toString());
        // Setting all values in listview
        //title.setText(piece.getIndex().toString());
        //title.setText("");
        //story_desc.setText(piece.getText_val());
        //creation_date.setText(DateFormat.format(StoryModel.DATE_FORMAT, piece.getCreated_on()));
        //imageLoader.DisplayImage(song.get(NewsfeedActivity.KEY_THUMB_URL), thumb_image);
        return vi;
    }
}