package com.faltenreich.diaguard.feature.timeline.day.table;

import android.view.View;
import android.widget.ImageView;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.shared.data.database.entity.Category;
import com.faltenreich.diaguard.shared.view.image.ImageLoader;
import com.faltenreich.diaguard.shared.view.ViewUtils;
import com.faltenreich.diaguard.shared.view.recyclerview.viewholder.BaseViewHolder;

import butterknife.BindView;

class CategoryImageViewHolder extends BaseViewHolder<CategoryImageListItem> implements View.OnClickListener {

    @BindView(R.id.category_image) ImageView imageView;

    CategoryImageViewHolder(View view) {
        super(view);
        view.setOnClickListener(this);
    }

    @Override
    public void bindData() {
        int categoryImageResourceId = getListItem().getCategory().getIconImageResourceId();
        if (categoryImageResourceId > 0) {
            ImageLoader.getInstance().load(categoryImageResourceId, imageView);
        }
    }

    @Override
    public void onClick(View view) {
        Category category = getListItem().getCategory();
        ViewUtils.showToast(getContext(), getContext().getString(category.getStringResId()));
    }
}
