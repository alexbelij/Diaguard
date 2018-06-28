package com.faltenreich.diaguard.ui.view.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.data.PreferenceHelper;
import com.faltenreich.diaguard.data.entity.Measurement;
import com.faltenreich.diaguard.util.ViewUtils;

public class CategoryPreference extends DialogPreference {

    public final static String ACTIVE = "_active";

    private ListView listView;

    public CategoryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_category);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        listView = view.findViewById(R.id.listview);
        CategoryListAdapter adapter = new CategoryListAdapter(getContext(),
                android.R.layout.simple_list_item_multiple_choice,
                getContext().getResources().getTextArray(R.array.categories));
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);

        for(int item = 0; item < Measurement.Category.values().length; item++) {
            Measurement.Category category = Measurement.Category.values()[item];
            listView.setItemChecked(item, PreferenceHelper.getInstance().isCategoryActive(category));
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            SparseBooleanArray checkedItems = listView.getCheckedItemPositions();

            if (checkedItems != null && checkedItems.indexOfValue(true) != -1) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                for (int item = 0; item < checkedItems.size(); item++)
                    editor.putBoolean(Measurement.Category.values()[item].name() + ACTIVE, checkedItems.valueAt(item));
                editor.apply();
            }
            else {
                ViewUtils.showToast(getContext(), R.string.validator_value_none);
            }
        }
    }
}