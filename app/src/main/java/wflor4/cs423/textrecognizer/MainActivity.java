package wflor4.cs423.textrecognizer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnRecognize;
    private Button btnClear;
    private DrawView drawView;
    private DrawView drawViewT;
    private TextView textView;
    private TextView textViewT;
    private RadioGroup radioGroup;
    private RadioButton radioTitle;
    private RadioButton radioBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        btnRecognize = findViewById(R.id.buttonRecognize);
        btnClear = findViewById(R.id.buttonClear);
        drawView = findViewById(R.id.draw_view);
        textView = findViewById(R.id.textResult);
        textViewT = findViewById(R.id.textResult_title);
        radioGroup = findViewById(R.id.radioGroup);
        radioTitle = findViewById(R.id.radioTitle);
        radioBody = findViewById(R.id.radioBody);

        hideTitleBar();

        StrokeManager.download();

        btnRecognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioTitle.isChecked()) {
                    // Recognize gesture for the title
                    StrokeManager.recognize(textViewT);
                    StrokeManager.clear();
                    drawView.clear();
                } else if (radioBody.isChecked()) {
                    // Recognize gesture for the body
                    StrokeManager.recognize(textView);
                    StrokeManager.clear();
                    drawView.clear();
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.clear();
                StrokeManager.clear();
            }
        });
    }

    private void hideTitleBar() {
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideTitleBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideTitleBar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideTitleBar();
    }
}
