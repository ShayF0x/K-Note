package fr.shayfox.k_note.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import fr.shayfox.k_note.R;

public class TimePickerWithSeconds extends DialogFragment {

    private View timePickerLayout;
    private NumberPicker hourPicker;
    private NumberPicker minPicker;
    private NumberPicker secPicker;

    private OnCancelOption onCancelOption;
    private OnTimeSetOption onTimeSetOption;
    private final String timeSetText = "Ok";

    private final String cancelText = "Cancel";

    /**
     * Which value will appear a the start of
     * the Dialog for the Hour picker.
     * Default value is 0.
     */
    int initialHour = 0;
    /**
     * Which value will appear a the start of
     * the Dialog for the Minute picker.
     * Default value is 0.
     */
    int initialMinute = 0;
    /**
     * Which value will appear a the start of
     * the Dialog for the Second picker.
     * Default value is 0.
     */
    int initialSeconds = 0;

    /**
     * Max value for the Hour picker.
     * Default value is 23.
     */
    int maxValueHour = 255;
    /**
     * Max value for the Minute picker.
     * Default value is 59.
     */
    int maxValueMinute = 59;
    /**
     * Max value for the Second picker.
     * Default value is 59.
     */
    int maxValueSeconds = 59;

    /**
     * Min value for the Hour picker.
     * Default value is 0.
     */
    int minValueHour = 0;
    /**
     * Min value for the Minute picker.
     * Default value is 0.
     */
    int minValueMinute = 0;
    /**
     * Min value for the Second picker.
     * Default value is 0.
     */
    int minValueSecond = 0;

    private String title = null;

    public TimePickerWithSeconds(int initialHour, int initialMinute, int initialSeconds) {
        this.initialHour = initialHour;
        this.initialMinute = initialMinute;
        this.initialSeconds = initialSeconds;
    }

    public TimePickerWithSeconds() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.PopupDialogTheme);
        timePickerLayout = requireActivity().getLayoutInflater().inflate(R.layout.time_picker_withsecond_dialog, null);
        setupTimePickerLayout();
        builder.setView(timePickerLayout);

        builder.setTitle(title);
        builder.setPositiveButton(timeSetText, (dialogInterface, i) -> {
            if(onTimeSetOption != null) {
                onTimeSetOption.onTimeSetOption(hourPicker.getValue(), minPicker.getValue(), secPicker.getValue());
            }
        }).setNeutralButton(cancelText, (dialogInterface, i) -> {
           if(onCancelOption != null) {
               onCancelOption.onCancelOption();
           }
        });

        return builder.create();
    }

    /**
     * Set the title displayed in the Dialog
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set a listener to be invoked when the Set Time button of the dialog is pressed.
     * If have set includeHours to false, the hour parameter here will be always 0.
     */
    public void setOnTimeSetOption (OnTimeSetOption onTimeSet) {
        onTimeSetOption = onTimeSet;
    }

    /**
     * Set a listener to be invoked when the Cancel button of the dialog is pressed.
     */
    public void setOnCancelOption (OnCancelOption onCancelOption) {
        this.onCancelOption = onCancelOption;
    }

    private void setupTimePickerLayout() {
        bindViews();

        setupMaxValues();
        setupMinValues();
        setupInitialValues();
    }

    private void bindViews() {
        hourPicker = timePickerLayout.findViewById(R.id.hours);
                minPicker = timePickerLayout.findViewById(R.id.minutes);
                secPicker = timePickerLayout.findViewById(R.id.seconds);
    }

    private void setupMaxValues () {
        hourPicker.setMaxValue(maxValueHour);
        minPicker.setMaxValue(maxValueMinute);
        secPicker.setMaxValue(maxValueSeconds);
    }

    private void setupMinValues () {
        hourPicker.setMinValue(minValueHour);
        minPicker.setMinValue(minValueMinute);
        secPicker.setMinValue(minValueSecond);
    }

    private void setupInitialValues () {
        hourPicker.setValue(initialHour);
        minPicker.setValue(initialMinute);
        secPicker.setValue(initialSeconds);
    }

    public interface OnCancelOption{
        void onCancelOption();
    }
    public interface OnTimeSetOption{
        void onTimeSetOption(int hour, int minute, int seconds);
    }
}
