package wflor4.cs423.textrecognizer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import yuku.ambilwarna.AmbilWarnaDialog;  //  using AmbilWarnaDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.Calendar;
//import yuku.ambilwarna.AmbilWarnaDialog;

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

        defaultColor = getIntent().getIntExtra("currentColor", ContextCompat.getColor(this, R.color.purple_700));

        btnColorPalette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();
            }
        });

        textViewT = findViewById(R.id.textResult_title); // Title CheckedTextView
        textView = findViewById(R.id.textResult);        // Body TextView

        String editTitle = getIntent().getStringExtra("editTitle");
        String editBody = getIntent().getStringExtra("editBody");
        String editDate = getIntent().getStringExtra("editDate");
        if (editDate != null) {
            btnCalendar.setText(editDate);
        }

        if (editTitle != null) {
            textViewT.setText(editTitle);
        }
        if (editBody != null) {
            textView.setText(editBody);
        }

        btnSave.setOnClickListener(v -> {
            Intent intent = new Intent();

            String title = textViewT.getText().toString();
            String body = textView.getText().toString();
            String date = ((Button) findViewById(R.id.calendar)).getText().toString();

            intent.putExtra("savedTitle", title);
            intent.putExtra("savedBody", body);
            intent.putExtra("selectedDate", date);
            intent.putExtra("selectedColor", defaultColor);

            if (editTitle != null || editBody != null) {
                intent.putExtra("isEditMode", true);
                int taskIndex = getIntent().getIntExtra("taskIndex", -1);
                intent.putExtra("taskIndex", taskIndex);
            }

            setResult(RESULT_OK, intent);
            finish();
        });

        Button btnClearDrawView = findViewById(R.id.btnClearDrawView);

//        hideTitleBar();

        StrokeManager.download();

        btnClearDrawView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.clear();  // Clear the DrawView
                StrokeManager.clear();  // Clear the StrokeManager
                Toast.makeText(HandwritingRecognition.this, "DrawView Cleared", Toast.LENGTH_SHORT).show();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioTitle) {
                    Toast.makeText(HandwritingRecognition.this, "Writing enabled: Currently Editing Title", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.radioBody) {
                    Toast.makeText(HandwritingRecognition.this, "Writing enabled: Currently Editing Body", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // set onClickListener for color palette button added by Esat
        btnColorPalette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();  // Now this should work correctly
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check which radio button is selected
                String selectedText = "";
                final TextView targetTextView;

                if (radioTitle.isChecked()) {
                    selectedText = textViewT.getText().toString();
                    targetTextView = textViewT;
                } else if (radioBody.isChecked()) {
                    selectedText = textView.getText().toString();
                    targetTextView = textView;
                } else {
                    Toast.makeText(HandwritingRecognition.this, "Please select Title or Body", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create an EditText to edit the selected text
                final EditText editText = new EditText(HandwritingRecognition.this);
                editText.setText(selectedText);
                editText.setSelection(editText.getText().length());  // Move cursor to the end of text

                // Show keyboard automatically when dialog opens
                editText.requestFocus();

                // Create an AlertDialog for editing
                AlertDialog dialog = new AlertDialog.Builder(HandwritingRecognition.this)
                        .setTitle("Edit Text")
                        .setView(editText)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Update the original TextView with the new text
                                targetTextView.setText(editText.getText().toString());
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();

                // Show the dialog
                dialog.show();

                // Show the keyboard after dialog is shown
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

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
                    if(textViewT.getText().toString().equals("Title")){
                        textViewT.setText("");
                    }
                    StrokeManager.recognize(textViewT);
                    StrokeManager.clear();
                    drawView.clear();
                } else if (radioBody.isChecked()) {
                    if(textView.getText().toString().equals("Body")){
                        textView.setText("");
                    }
                    StrokeManager.recognize(textView);
                    StrokeManager.clear();
                    drawView.clear();
                }
            }
        });

        // Save button: Save the title text
//        btnSave.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//
//                String title = textViewT.getText().toString();
//                String body = textView.getText().toString();
//                intent.putExtra("savedTitle", title);   // Pass the body text
//                intent.putExtra("savedBody", body);      // Indicate it's a body
//
//
//                setResult(RESULT_OK, intent);  // Set the result to OK and attach the Intent
////                Toast.makeText(HandwritingRecognition.this, "Intent", Toast.LENGTH_SHORT).show();
//
//                finish();  // Close this activity and return to MainActivity
//            }
//        });
        Toast.makeText(HandwritingRecognition.this, "Writing enabled: Currently Editing Title", Toast.LENGTH_SHORT).show();
    }

    // Method to show the DatePickerDialog
    private void showDatePickerDialog() {
        // Get the button by its ID
        Button buttonDatePicker = findViewById(R.id.calendar);

        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(HandwritingRecognition.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;

                        // Set the selected date as the button's text
                        buttonDatePicker.setText(selectedDate);

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