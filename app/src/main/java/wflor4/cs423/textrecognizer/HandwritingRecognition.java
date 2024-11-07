package wflor4.cs423.textrecognizer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import yuku.ambilwarna.AmbilWarnaDialog;  //  using AmbilWarnaDialog



import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

import yuku.ambilwarna.AmbilWarnaDialog;

public class HandwritingRecognition extends AppCompatActivity {

    private Button btnRecognize, btnClear, btnEdit, btnCalendar, btnSave;
    private DrawView drawView;
    private TextView textView, textViewT;
    private RadioGroup radioGroup;
    private RadioButton radioTitle, radioBody;

    int defaultColor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        // Bind UI elements to their respective IDs
        btnRecognize = findViewById(R.id.buttonRecognize);
        btnClear = findViewById(R.id.buttonClear);
        btnEdit = findViewById(R.id.buttonEdit);  // Added buttonEdit
        btnCalendar = findViewById(R.id.calendar);  // Calendar button
        btnSave = findViewById(R.id.checkmark);  // Save button
        Button btnX = findViewById(R.id.cancel);  // X button
        drawView = findViewById(R.id.draw_view);


        textView = findViewById(R.id.textResult);
        textViewT = findViewById(R.id.textResult_title);
        radioGroup = findViewById(R.id.radioGroup);
        radioTitle = findViewById(R.id.radioTitle);
        radioBody = findViewById(R.id.radioBody);
        Button btnColorPalette = findViewById(R.id.buttonColorPalette);  // Initialize the color picker button

        defaultColor = ContextCompat.getColor(HandwritingRecognition.this, R.color.black);


        hideTitleBar();

        StrokeManager.download();

        // set onClickListener for color palette button added by Esat
        btnColorPalette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();  // Now this should work correctly
            }
        });

        // Edit button logic by Esat Duman
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioTitle.isChecked()) {
                    // Toggle textViewT to be editable
                    textViewT.setFocusableInTouchMode(true);
                    textViewT.setFocusable(true);
                    textViewT.requestFocus();
                    textViewT.setCursorVisible(true);  // Show the cursor for editing
                    Toast.makeText(HandwritingRecognition.this, "Editing Title", Toast.LENGTH_SHORT).show();
                } else if (radioBody.isChecked()) {
                    // Toggle textView to be editable
                    textView.setFocusableInTouchMode(true);
                    textView.setFocusable(true);
                    textView.requestFocus();
                    textView.setCursorVisible(true);  // Show the cursor for editing
                    Toast.makeText(HandwritingRecognition.this, "Editing Body", Toast.LENGTH_SHORT).show();
                }
            }
        });

// Clear button logic to reset both textViews to non-editable state
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.clear();
                StrokeManager.clear();
                if (radioTitle.isChecked()) {
                    textViewT.setText("");
                    // Make textViewT non-editable again
                    textViewT.setFocusable(false);
                    textViewT.setCursorVisible(false);
                } else if (radioBody.isChecked()) {
                    textView.setText("");
                    // Make textView non-editable again
                    textView.setFocusable(false);
                    textView.setCursorVisible(false);
                }
            }
        });





        // Clear button logic
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.clear();
                StrokeManager.clear();
                if (radioTitle.isChecked()) {
                    textViewT.setText("");
                } else if (radioBody.isChecked()) {
                    textView.setText("");
                }
            }
        });

        // X button: navigate back to MainActivity
        btnX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to MainActivity
                Intent intent = new Intent(HandwritingRecognition.this, MainActivity.class);
                startActivity(intent);
                finish();  // Close current activity
            }
        });

        // Calendar button: show DatePickerDialog
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDatePickerDialog();
                getSupportActionBar().hide();
            }
        });

        // Recognize button logic
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


        // Save button: Save the title text
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                String title = textViewT.getText().toString();
                String body = textView.getText().toString();
                intent.putExtra("savedTitle", title);   // Pass the body text
                intent.putExtra("savedBody", body);      // Indicate it's a body


                setResult(RESULT_OK, intent);  // Set the result to OK and attach the Intent
//                Toast.makeText(HandwritingRecognition.this, "Intent", Toast.LENGTH_SHORT).show();

                finish();  // Close this activity and return to MainActivity
            }
        });

    }

    // Method to show the DatePickerDialog
    private void showDatePickerDialog() {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(HandwritingRecognition.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        Toast.makeText(HandwritingRecognition.this, "Selected date: " + selectedDate, Toast.LENGTH_SHORT).show();
                    }
                }, year, month, day);
        datePickerDialog.show();
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

    // Method to open the color picker dialog
    private void openColorPicker() {
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                ///method does not need implementation leave alone
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultColor = color;  // Save the selected color
                drawView.setStrokeColor(color);
            }
        });
        ambilWarnaDialog.show();  // Show the color picker dialog
    }
}
