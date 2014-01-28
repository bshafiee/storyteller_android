package behsaman.storytellerandroid;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import behsaman.storytellerandroid.datamodel.StoryModel;
import behsaman.storytellerandroid.utils.ImageLoader;

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private ArrayList<StoryModel> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
    
    public LazyAdapter(Activity a, ArrayList<StoryModel> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public int getStoryID(int position) {
    	StoryModel story = data.get(position);
    	if(story!=null)
    		return story.getId();
    	else
    		return -1;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);

        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView story_desc = (TextView)vi.findViewById(R.id.story_news); 
        TextView creation_date = (TextView)vi.findViewById(R.id.creation_date); 
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); 
        
        StoryModel story = data.get(position);
        
        // Setting all values in listview
        title.setText(story.getTitle());
        String description = "Category: "+story.getCategory();
        description += "\t\tCreated By: "+story.getOwner_id();
        description += "\nPieces Left: "+(story.getMax_num_pieces().getNumVal() - story.getNext_available_piece());
        description += "\nStory Type: "+(story.getType());
        
        story_desc.setText(description);
        creation_date.setText(DateFormat.format(StoryModel.DATE_FORMAT, story.getCreated_on()));
        //imageLoader.DisplayImage(song.get(NewsfeedActivity.KEY_THUMB_URL), thumb_image);
        return vi;
    }
}