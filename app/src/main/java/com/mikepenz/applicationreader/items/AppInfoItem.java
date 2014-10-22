package com.mikepenz.applicationreader.items;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.applicationreader.R;
import com.mikepenz.applicationreader.entity.AppInfo;
import com.mikepenz.fastadapter.items.ModelAbstractItem;

import java.util.List;

public class AppInfoItem extends ModelAbstractItem<AppInfo, AppInfoItem, AppInfoItem.ViewHolder> {

    public AppInfoItem(AppInfo icon) {
        super(icon);
    }

    /**
     * defines the type defining this item. must be unique. preferably an id
     *
     * @return the type
     */
    @Override
    public int getType() {
        return R.layout.row_application;
    }

    /**
     * defines the layout which will be used for this item in the list
     *
     * @return the layout for this item
     */
    @Override
    public int getLayoutRes() {
        return R.layout.row_application;
    }

    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        final AppInfo appInfo = getModel();
        viewHolder.name.setText(appInfo.getName());
        viewHolder.image.setImageDrawable(appInfo.getIcon());
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.name.setText(null);
        holder.image.setImageDrawable(null);
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    /**
     * our ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;

        ViewHolder(View view) {
            super(view);
            name = itemView.findViewById(R.id.countryName);
            image = itemView.findViewById(R.id.countryImage);
        }
    }
}