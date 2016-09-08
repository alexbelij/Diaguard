package com.faltenreich.diaguard.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.adapter.list.ListItemTimePreference;
import com.faltenreich.diaguard.ui.view.viewholder.TimeViewHolder;
import com.faltenreich.diaguard.util.NumberUtils;

/**
 * Created by Faltenreich on 04.09.2016.
 */
public class TimeAdapter extends BaseAdapter<ListItemTimePreference, TimeViewHolder> {

    public TimeAdapter(Context context) {
        super(context);
    }

    @Override
    public TimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TimeViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.list_item_time, parent, false));
    }

    @Override
    public void onBindViewHolder(final TimeViewHolder holder, int position) {
        holder.bindData(getItem(position));
        holder.value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                ListItemTimePreference preference = getItem(holder.getAdapterPosition());
                try {
                    preference.setValue(NumberUtils.parseNumber(editable.toString()));
                } catch (NumberFormatException exception) {
                    preference.setValue(-1);
                }
            }
        });
    }
}
