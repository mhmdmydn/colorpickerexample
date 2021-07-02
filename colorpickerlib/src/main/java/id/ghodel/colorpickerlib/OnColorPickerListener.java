package id.ghodel.colorpickerlib;

public interface OnColorPickerListener {
    void onColorCancel(ColorPickerDialog dialog);
    void onColorChange(ColorPickerDialog dialog,int color);
    void onColorConfirm(ColorPickerDialog dialog,int color);
}
