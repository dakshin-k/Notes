package com.dakshin.notes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<MenuItem> implements View.OnClickListener{
    ArrayList<MenuItem> dataSet;
    Context mContext;
    private static class ViewHolder {
        TextView txtName;
       // TextView txtType;
        ImageView info;
    }

    public CustomAdapter(ArrayList<MenuItem> data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }
    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        MenuItem dataModel=(MenuItem) object;

        switch (v.getId())
        {
            case R.id.listitemicon:
                Toast.makeText(mContext, "This is supposed to have an icon!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private int lastPosition = -1;

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        MenuItem dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.txtName =  convertView.findViewById(R.id.listitemtext);
            viewHolder.info =  convertView.findViewById(R.id.listitemicon);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

       /* Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;*/

        viewHolder.txtName.setText(dataModel.getName());
        viewHolder.info.setTag(position);

        String type=dataModel.getType();
        switch (type) {
            case "folder":
                viewHolder.info.setImageResource(R.drawable.folder);
                break;
            case "pdf":
                viewHolder.info.setImageResource(R.drawable.pdf);
                break;
            case "jpg":
            case "png":
                viewHolder.info.setImageResource(R.drawable.picture);
                break;
            case "docx":
            case "doc":
                viewHolder.info.setImageResource(R.drawable.document);
                break;
            case "ppt":
            case "pptx":
                viewHolder.info.setImageResource(R.drawable.ppt);
                break;
            case "txt":
                viewHolder.info.setImageResource(R.drawable.txt);
                break;
        }
        // Return the completed view to render on screen
        return convertView;
    }


}
