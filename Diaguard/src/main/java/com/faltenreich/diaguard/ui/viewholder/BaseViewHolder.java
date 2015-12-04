package com.faltenreich.diaguard.ui.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.faltenreich.diaguard.adapter.ListItem;

import butterknife.ButterKnife;

/**
 * Created by Faltenreich on 17.10.2015.
 */
public abstract class BaseViewHolder <T extends ListItem> extends RecyclerView.ViewHolder {

    private Context context;
    private View view;

    private T listItem;

    protected BaseViewHolder(View view) {
        super(view);
        this.view = view;
        this.context = view.getContext();
        ButterKnife.bind(this, view);
    }

    protected Context getContext() {
        return context;
    }

    protected View getView() {
        return view;
    }

    protected T getListItem() {
        return listItem;
    }

    public void bindData(T listItem) {
        this.listItem = listItem;
        bindData();
    }

    protected abstract void bindData();
}