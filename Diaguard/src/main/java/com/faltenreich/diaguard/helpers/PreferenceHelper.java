package com.faltenreich.diaguard.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.database.Measurement;
import com.faltenreich.diaguard.preferences.CategoryPreference;
import com.faltenreich.diaguard.preferences.FactorPreference;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Filip on 04.11.13.
 */

/**
 * Parameters are of Default Value
 * Return Values are of Custom Value
 */
public class PreferenceHelper {

    private Context context;
    private SharedPreferences sharedPreferences;

    public PreferenceHelper(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // GENERAL

    public boolean validateEventValue(Measurement.Category category, float value) {
        int resourceIdExtrema = context.getResources().getIdentifier(category.name().toLowerCase() +
                "_extrema", "array", context.getPackageName());

        if(resourceIdExtrema == 0)
            throw new Resources.NotFoundException("Resource \"category_extrema\" not found: IntArray with event value extrema");

        int[] extrema = context.getResources().getIntArray(resourceIdExtrema);

        if(extrema.length != 2)
            throw new IllegalStateException("IntArray with event value extrema has to contain two values");

        return value > extrema[0] && value < extrema[1];
    }

    // DATES

    public DateTimeFormatter getDateFormat() {
        String dateString = sharedPreferences.getString("dateformat",
                context.getResources().getString(R.string.dateformat_default));

        dateString = dateString.replace("YYYY", "yyyy");
        dateString = dateString.replace("mm", "MM");
        dateString = dateString.replace("DD", "dd");

        return DateTimeFormat.forPattern(dateString);
    }

    // BLOOD SUGAR

    public float getTargetValue() {
        return Float.valueOf(sharedPreferences.getString("target",
                context.getString(R.string.pref_therapy_targets_target_default)));
    }

    public boolean limitsAreHighlighted() {
        return sharedPreferences.getBoolean("targets_highlight", true);
    }

    public float getLimitHyperglycemia() {
        return Float.valueOf(sharedPreferences.getString("hyperclycemia",
                context.getString(R.string.pref_therapy_targets_hyperclycemia_default)));
    }

    public float getLimitHypoglycemia() {
        return Float.valueOf(sharedPreferences.getString("hypoclycemia",
                context.getString(R.string.pref_therapy_targets_hypoclycemia_default)));
    }

    public float getCorrectionValue() {
        return Float.parseFloat(sharedPreferences.getString("correction_value", "40"));
    }

    // TODO: Localization
    public DecimalFormat getDecimalFormat(Measurement.Category category) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');

        String formatString = "#0";
        switch (category) {
            case Bolus:
            case HbA1c:
            case Weight:
                formatString = "#0.#";
            default:
                float unitValue = getUnitValue(category);
                if(unitValue != 1)
                    formatString = "#0.#";
        }

        DecimalFormat format = new DecimalFormat(formatString);
        format.setDecimalFormatSymbols(symbols);
        return format;
    }

    // CATEGORIES

    public String getCategoryName(Measurement.Category category) {
        int position = Measurement.Category.valueOf(category.name()).ordinal();
        String[] categories = context.getResources().getStringArray(R.array.categories);
        return categories[position];
    }

    // UNITS

    public String[] getUnitsNames(Measurement.Category category) {
        String categoryName = category.name().toLowerCase();
        int resourceIdUnits = context.getResources().getIdentifier(categoryName +
                "_units", "array", context.getPackageName());
        return context.getResources().getStringArray(resourceIdUnits);
    }

    public String getUnitName(Measurement.Category category) {
        String sharedPref = sharedPreferences.
                getString("unit_" + category.name().toLowerCase(), "1");
        return getUnitsNames(category)[Arrays.asList(getUnitsValues(category)).indexOf(sharedPref)];
    }

    public String[] getUnitsAcronyms(Measurement.Category category) {
        String categoryName = category.name().toLowerCase();
        int resourceIdUnits = context.getResources().getIdentifier(categoryName +
                "_units_acronyms", "array", context.getPackageName());
        if(resourceIdUnits == 0)
            return null;
        else
            return context.getResources().getStringArray(resourceIdUnits);
    }

    public String getUnitAcronym(Measurement.Category category) {
        String[] acronyms = getUnitsAcronyms(category);
        if(acronyms == null)
            return getUnitName(category);
        else {
            String sharedPref = sharedPreferences.
                    getString("unit_" + category.name().toLowerCase(), "1");
            int indexOfAcronym = Arrays.asList(getUnitsValues(category)).indexOf(sharedPref);
            if(indexOfAcronym < acronyms.length) {
                return acronyms[indexOfAcronym];
            }
            else {
                return getUnitName(category);
            }
        }
    }

    public String[] getUnitsValues(Measurement.Category category) {
        String categoryName = category.name().toLowerCase();
        int resourceIdUnits = context.getResources().getIdentifier(categoryName +
                "_units_values", "array", context.getPackageName());
        return context.getResources().getStringArray(resourceIdUnits);
    }

    public float getUnitValue(Measurement.Category category) {
        String sharedPref = sharedPreferences.
                getString("unit_" + category.name().toLowerCase(), "1");
        String value = getUnitsValues(category)[Arrays.asList(getUnitsValues(category)).indexOf(sharedPref)];
        return Float.valueOf(value);
    }

    public float formatCustomToDefaultUnit(Measurement.Category category, float value) {
        return value / getUnitValue(category);
    }

    public float formatDefaultToCustomUnit(Measurement.Category category, float value) {
        return value * getUnitValue(category);
    }

    // FACTORS

    public enum Daytime {
        Morning,
        Noon,
        Evening,
        Night
    }

    public Daytime getCurrentDaytime() {
        DateTime now = new DateTime();
        int hour = now.getHourOfDay();

        if(hour >= 4 && hour < 10)
            return Daytime.Morning;
        else if(hour >= 10 && hour < 16)
            return Daytime.Noon;
        else if(hour >= 16 && hour < 22)
            return Daytime.Evening;
        else
            return Daytime.Night;
    }

    public float getFactorValue(Daytime daytime) {
        return sharedPreferences.getFloat(FactorPreference.FACTOR + daytime.toString(), 0);
    }

    // CATEGORIES

    public boolean isCategoryActive(Measurement.Category category) {
        return sharedPreferences.getBoolean(category.name() + CategoryPreference.ACTIVE, true);
    }

    public Measurement.Category[] getActiveCategories() {
        List<Measurement.Category> activeCategories = new ArrayList<Measurement.Category>();

        for(int item = 0; item < Measurement.Category.values().length; item++)
            if(isCategoryActive(Measurement.Category.values()[item]))
                activeCategories.add(Measurement.Category.values()[item]);

        return activeCategories.toArray(new Measurement.Category[activeCategories.size()]);
    }
}