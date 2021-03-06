package com.faltenreich.diaguard.feature.food.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.shared.data.preference.PreferenceHelper;
import com.faltenreich.diaguard.shared.data.async.DataLoader;
import com.faltenreich.diaguard.shared.data.async.DataLoaderListener;
import com.faltenreich.diaguard.shared.data.database.dao.FoodDao;
import com.faltenreich.diaguard.shared.data.database.entity.Food;
import com.faltenreich.diaguard.shared.event.Events;
import com.faltenreich.diaguard.shared.event.data.FoodDeletedEvent;
import com.faltenreich.diaguard.shared.event.data.FoodQueryEndedEvent;
import com.faltenreich.diaguard.shared.event.data.FoodQueryStartedEvent;
import com.faltenreich.diaguard.shared.event.data.FoodSavedEvent;
import com.faltenreich.diaguard.shared.event.ui.FoodSelectedEvent;
import com.faltenreich.diaguard.feature.food.detail.FoodDetailActivity;
import com.faltenreich.diaguard.feature.food.edit.FoodEditActivity;
import com.faltenreich.diaguard.feature.food.BaseFoodFragment;
import com.faltenreich.diaguard.shared.view.fragment.BaseFragment;
import com.faltenreich.diaguard.shared.networking.NetworkingUtils;
import com.faltenreich.diaguard.shared.view.ViewUtils;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.faltenreich.diaguard.R.id.food_search_list_empty;

/**
 * Created by Faltenreich on 11.09.2016.
 */
public class FoodSearchFragment extends BaseFragment implements SearchView.OnQueryTextListener, SearchView.OnMenuClickListener {

    public static final String FINISH_ON_SELECTION = "finishOnSelection";

    @BindView(R.id.food_search_unit) TextView unitTextView;
    @BindView(R.id.food_search_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.food_search_list) FoodSearchListView list;
    @BindView(R.id.search_view) SearchView searchView;

    @BindView(food_search_list_empty) ViewGroup emptyList;
    @BindView(R.id.food_search_empty_icon) ImageView emptyIcon;
    @BindView(R.id.food_search_empty_text) TextView emptyText;
    @BindView(R.id.food_search_empty_description) TextView emptyDescription;
    @BindView(R.id.food_search_empty_button) Button emptyButton;

    private boolean finishOnSelection;
    private SearchAdapter searchAdapter;

    public FoodSearchFragment() {
        super(R.layout.fragment_food_search, R.string.food);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initLayout();
    }

    @Override
    public void onResume() {
        super.onResume();
        Events.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Events.unregister(this);
    }

    private void init() {
        if (getActivity() != null && getActivity().getIntent() != null && getActivity().getIntent().getExtras() != null) {
            Bundle extras = getActivity().getIntent().getExtras();
            finishOnSelection = extras.getBoolean(FINISH_ON_SELECTION);
        }
    }

    private void initLayout() {
        unitTextView.setText(PreferenceHelper.getInstance().getLabelForMealPer100g(getContext()));

        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.green_light, R.color.green_lighter);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            query(searchView.getQuery().toString());
        });

        searchView.setOnQueryTextListener(this);
        searchView.setOnMenuClickListener(this);
        searchView.setHint(R.string.food_search);
        searchView.setArrowOnly(false);

        searchAdapter = new SearchAdapter(getContext());
        searchAdapter.addOnItemClickListener((view, position) -> {
            TextView textView = view.findViewById(R.id.textView_item_text);
            String query = textView.getText().toString();
            searchView.setQuery(query, true);
            searchView.close(true);
        });
        searchView.setAdapter(searchAdapter);

        initSuggestions();
    }

    private void initSuggestions() {
        DataLoader.getInstance().load(getContext(), new DataLoaderListener<List<SearchItem>>() {
            @Override
            public List<SearchItem> onShouldLoad() {
                ArrayList<SearchItem> searchItems = new ArrayList<>();
                for (String recentQuery : PreferenceHelper.getInstance().getInputQueries()) {
                    searchItems.add(new SearchItem(R.drawable.ic_history_old, recentQuery));
                }
                return searchItems;
            }
            @Override
            public void onDidLoad(List<SearchItem> searchItems) {
                searchAdapter.setSuggestionsList(searchItems);
            }
        });
    }

    private void showError(@DrawableRes int iconResId, @StringRes int textResId, @StringRes int descResId, @StringRes int buttonTextResId) {
        emptyList.setVisibility(View.VISIBLE);
        emptyIcon.setImageResource(iconResId);
        emptyText.setText(textResId);
        emptyDescription.setText(descResId);
        emptyButton.setText(buttonTextResId);
    }

    private void onFoodSelected(Food food) {
        if (finishOnSelection) {
            finish();
        } else {
            openFood(food);
        }
    }

    private void openFood(Food food) {
        Events.unregister(this);

        Intent intent = new Intent(getContext(), FoodDetailActivity.class);
        intent.putExtra(BaseFoodFragment.EXTRA_FOOD_ID, food.getId());
        startActivity(intent);
    }

    private void query(String query) {
        emptyList.setVisibility(View.GONE);
        list.newSearch(query);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.close(true);
        PreferenceHelper.getInstance().addInputQuery(query);
        query(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onMenuClick() {
        if (searchView.isSearchOpen()) {
            searchView.close(true);
        } else {
            finish();
        }
    }

    private void createFood() {
        startActivity(new Intent(getContext(), FoodEditActivity.class));
    }

    private void showEmptyList() {
        if (NetworkingUtils.isOnline(getContext())) {
            showError(R.drawable.ic_sad, R.string.error_no_data, R.string.error_no_data_desc, R.string.food_add_desc);
        } else {
            showError(R.drawable.ic_wifi, R.string.error_no_connection, R.string.error_no_connection_desc, R.string.try_again);
        }
    }

    @OnClick(R.id.fab)
    void onFabClick() {
        createFood();
    }

    @OnClick(R.id.food_search_empty_button)
    void onEmptyButtonClick() {
        // Workaround since CONNECTIVITY_ACTION broadcasts cannot be caught since API level 24
        boolean wasNetworkError = emptyText.getText().toString().equals(getString(R.string.error_no_connection));
        if (wasNetworkError) {
            query(searchView.getQuery().toString());
        } else {
            createFood();
        }
    }

    @OnClick(R.id.imageView_clear)
    void clearQuery() {
        searchView.setTextOnly(null);
        searchView.close(true);
        query(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FoodSelectedEvent event) {
        onFoodSelected(event.context);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FoodQueryStartedEvent event) {
        if (list.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FoodQueryEndedEvent event) {
        swipeRefreshLayout.setRefreshing(false);
        if (list.getItemCount() == 0) {
            showEmptyList();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FoodSavedEvent event) {
        clearQuery();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final FoodDeletedEvent event) {
        ViewUtils.showSnackbar(getView(), getString(R.string.food_deleted), v -> {
            Food food = event.context;
            food.setDeletedAt(null);
            FoodDao.getInstance().createOrUpdate(food);
            Events.post(new FoodSavedEvent(food));
        });
    }
}
