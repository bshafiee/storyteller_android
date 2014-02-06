package behsaman.storytellerandroid;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import behsaman.storytellerandroid.utils.ImageLoader;

public abstract class LazyAdapter extends BaseAdapter {
    
    protected Activity activity;
    protected ArrayList<Object> data;
    protected static LayoutInflater inflater=null;
    protected ImageLoader imageLoader; 
    
    public LazyAdapter(Activity a, ArrayList<Object> d) {
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
    
    public abstract View getView(int position, View convertView, ViewGroup parent);
}