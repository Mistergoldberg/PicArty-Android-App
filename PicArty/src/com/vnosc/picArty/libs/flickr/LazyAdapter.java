/**
 * 
 */
package com.vnosc.picArty.libs.flickr;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.gmail.yuyang226.flickr.photos.PhotoList;

/**
 * @author Toby Yu(yuyang226@gmail.com)
 *
 */
public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private PhotoList photos;
    @SuppressWarnings("unused")
	private static LayoutInflater inflater=null;
    
    public LazyAdapter(Activity a, PhotoList d) {
        activity = a;
        photos = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return photos.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}
    
//    public View getView(int position, View convertView, ViewGroup parent) {
//        View vi = convertView;
//        if(convertView == null)
//            vi = inflater.inflate(R.layout.row, null);
//
//        TextView text=(TextView)vi.findViewById(R.id.imageTitle);;
//        ImageView image=(ImageView)vi.findViewById(R.id.imageIcon);
//        Photo photo = photos.get(position);
//        text.setText(photo.getTitle());
//        if (image != null) {
//        	ImageDownloadTask task = new ImageDownloadTask(image);
//            Drawable drawable = new DownloadedDrawable(task);
//            image.setImageDrawable(drawable);
//            task.execute(photo.getSmallSquareUrl());
//        }
//        
//        ImageView viewIcon = (ImageView)vi.findViewById(R.id.viewIcon);
//        if (photo.getViews() >= 0) {
//        	viewIcon.setImageResource(R.drawable.views);
//        	TextView viewsText = (TextView)vi.findViewById(R.id.viewsText);
//        	viewsText.setText(String.valueOf(photo.getViews()));
//        } else {
//        	viewIcon.setImageBitmap(null);
//        }
//        
//        return vi;
//    }
}
