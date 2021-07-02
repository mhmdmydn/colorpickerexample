package id.ghodel.colorpickerexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import id.ghodel.colorpickerlib.ColorPickerDialog;
import id.ghodel.colorpickerlib.OnColorPickerListener;

public class MainActivity extends AppCompatActivity{

    private Button btnPick;
    private ColorPickerDialog colorPickerDialog;
    private boolean supportAlpha;
    private ConstraintLayout consLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPick = findViewById(R.id.btn_pick);
        consLayout = findViewById(R.id.cons_layout);

        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerDialog = new ColorPickerDialog(MainActivity.this,getResources().getColor(R.color.teal_200),supportAlpha, onColorPickerListener).show();
            }
        });

    }

    private OnColorPickerListener onColorPickerListener = new OnColorPickerListener() {
        @Override
        public void onColorCancel(ColorPickerDialog dialog) {

        }

        @Override
        public void onColorChange(ColorPickerDialog dialog, int color) {
            if ( consLayout != null){
                consLayout.setBackgroundColor(color);
                colorPickerDialog.setButtonTextColor(color);
            }
        }

        @Override
        public void onColorConfirm(ColorPickerDialog dialog, int color) {
            if (btnPick != null){
                btnPick.setTextColor(color);
            }
        }
    };

}