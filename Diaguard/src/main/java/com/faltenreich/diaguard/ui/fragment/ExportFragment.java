package com.faltenreich.diaguard.ui.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.data.PreferenceHelper;
import com.faltenreich.diaguard.data.entity.Measurement;
import com.faltenreich.diaguard.event.Events;
import com.faltenreich.diaguard.event.PermissionDeniedEvent;
import com.faltenreich.diaguard.event.PermissionGrantedEvent;
import com.faltenreich.diaguard.ui.view.CategoryCheckBoxList;
import com.faltenreich.diaguard.ui.view.MainButton;
import com.faltenreich.diaguard.ui.view.MainButtonProperties;
import com.faltenreich.diaguard.util.FileUtils;
import com.faltenreich.diaguard.util.Helper;
import com.faltenreich.diaguard.util.SystemUtils;
import com.faltenreich.diaguard.util.ViewUtils;
import com.faltenreich.diaguard.util.export.Export;
import com.faltenreich.diaguard.util.export.FileListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Faltenreich on 27.10.2016.
 */

public class ExportFragment extends BaseFragment implements FileListener, MainButton {

    private static final int PADDING = (int) Helper.getDPI(R.dimen.padding);

    @BindView(R.id.button_datestart) Button buttonDateStart;
    @BindView(R.id.button_dateend) Button buttonDateEnd;
    @BindView(R.id.spinner_format) Spinner spinnerFormat;
    @BindView(R.id.checkbox_note) CheckBox checkBoxNotes;
    @BindView(R.id.export_list_categories) CategoryCheckBoxList categoryCheckBoxList;

    private ProgressDialog progressDialog;

    private DateTime dateStart;
    private DateTime dateEnd;

    public ExportFragment() {
        super(R.layout.fragment_export, R.string.export, -1);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
    }

    @Override
    public void onResume() {
        super.onResume();
        Events.register(this);
        initializeLayout();
    }

    @Override
    public void onDestroy() {
        Events.unregister(this);
        super.onDestroy();
    }

    public void initialize() {
        dateEnd = DateTime.now();
        dateStart = dateEnd.withDayOfMonth(1);
    }

    public void initializeLayout() {
        buttonDateStart.setText(Helper.getDateFormat().print(dateStart));
        buttonDateEnd.setText(Helper.getDateFormat().print(dateEnd));
        checkBoxNotes.setPadding(PADDING, PADDING, PADDING, PADDING);
        checkBoxNotes.setChecked(PreferenceHelper.getInstance().exportNotes());
        checkBoxNotes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceHelper.getInstance().setExportNotes(isChecked);
            }
        });
    }

    private boolean validate() {
        boolean isValid = true;

        if (categoryCheckBoxList.getSelectedCategories().length == 0) {
            ViewUtils.showSnackbar(getView(), getString(R.string.validator_value_empty_list));
            isValid = false;
        }

        return isValid;
    }

    private void exportIfInputIsValid() {
        if (validate()) {
            exportIfPermissionGranted();
        }
    }

    private void exportIfPermissionGranted() {
        if (SystemUtils.canWriteExternalStorage(getActivity())) {
            export();
        } else {
            SystemUtils.requestPermissionWriteExternalStorage(getActivity());
        }
    }

    private void export() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getString(R.string.export_progress));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Measurement.Category[] selectedCategories = categoryCheckBoxList.getSelectedCategories();
        PreferenceHelper.getInstance().setExportCategories(selectedCategories);

        if (spinnerFormat.getSelectedItemPosition() == 0) {
            Export.exportPdf(this, dateStart, dateEnd, selectedCategories);
        } else if (spinnerFormat.getSelectedItemPosition() == 1) {
            Export.exportCsv(this, false, dateStart, dateEnd, selectedCategories);
        }
    }

    @Override
    public void onProgress(String message) {
        if (progressDialog != null) {
            progressDialog.setMessage(message);
        }
    }

    @Override
    public void onComplete(File file, String mimeType) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        String confirmationText = String.format(getString(R.string.export_complete), file.getAbsolutePath());
        Toast.makeText(getContext(), confirmationText, Toast.LENGTH_LONG).show();
        openFile(file, mimeType);
    }

    private void openFile(File file, String mimeType) {
        try {
            FileUtils.openFile(file, mimeType, getContext());
        } catch (ActivityNotFoundException e) {
            ViewUtils.showSnackbar(getView(), getString(R.string.error_no_app));
            Log.e("Open " + mimeType, e.getMessage());
        }
    }

    @Override
    public MainButtonProperties getMainButtonProperties() {
        return MainButtonProperties.confirmButton(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportIfInputIsValid();
            }
        });
    }

    @OnClick(R.id.button_datestart)
    public void showStartDatePicker() {
        DatePickerFragment.newInstance(dateStart, null, dateEnd, new DatePickerFragment.DatePickerListener() {
            @Override
            public void onDatePicked(@Nullable DateTime dateTime) {
                dateStart = dateTime;
                buttonDateStart.setText(Helper.getDateFormat().print(dateStart));
            }
        }).show(getFragmentManager());
    }

    @OnClick(R.id.button_dateend)
    public void showEndDatePicker() {
        DatePickerFragment.newInstance(dateEnd, dateStart, null, new DatePickerFragment.DatePickerListener() {
            @Override
            public void onDatePicked(@Nullable DateTime dateTime) {
                dateEnd = dateTime;
                buttonDateEnd.setText(Helper.getDateFormat().print(dateEnd));
            }
        }).show(getFragmentManager());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PermissionGrantedEvent event) {
        if (event.context.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            export();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PermissionDeniedEvent event) {
        if (event.context.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ViewUtils.showToast(getContext(), R.string.permission_required_storage);
        }
    }
}
