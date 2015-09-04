package com.faltenreich.diaguard.data.entity;

import com.faltenreich.diaguard.DiaguardApplication;
import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.data.PreferenceHelper;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Filip on 11.05.2015.
 */
@DatabaseTable
public class Pressure extends Measurement {

    public static final String SYSTOLIC = "systolic";
    public static final String DIASTOLIC = "diastolic";

    @DatabaseField
    private float systolic;

    @DatabaseField
    private float diastolic;

    public float getSystolic() {
        return systolic;
    }

    public void setSystolic(float systolic) {
        this.systolic = systolic;
    }

    public float getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(float diastolic) {
        this.diastolic = diastolic;
    }

    public Category getMeasurementType() {
        return Category.Pressure;
    }

    @Override
    public String toString() {
        return PreferenceHelper.getInstance().getMeasurementForUi(getMeasurementType(), systolic) + " " +
                DiaguardApplication.getContext().getString(R.string.to) + " " +
                PreferenceHelper.getInstance().getMeasurementForUi(getMeasurementType(), diastolic);
    }
}