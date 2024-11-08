package wflor4.cs423.textrecognizer;

import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureStroke;
import android.gesture.Prediction;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.activity.result.ActivityResultLauncher;

public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {
    private GestureLibrary objLib;
    private List<View> taskList = new LinkedList<>();
    private List<TextView> heartList = new LinkedList<>();
    private List<TextView> exclamationList = new LinkedList<>();
    private List<View> completedTaskList = new LinkedList<>();
    private GridLayout gridLayout;

    private FloatingActionButton fabAddTask;
    private ActivityResultLauncher<Intent> handwritingLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        objLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!objLib.load()) {
            finish();
        }

        GestureOverlayView objOverlay = (GestureOverlayView) findViewById(R.id.gestureOverlay);
        objOverlay.addOnGesturePerformedListener(this);

        gridLayout = findViewById(R.id.gridLayout);  // Correct casting to androidx.gridlayout.widget.GridLayout

        loadTasksFromPreferences();

        handwritingLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String taskTitle = result.getData().getStringExtra("savedTitle");
                        String taskBody = result.getData().getStringExtra("savedBody");
                        if (taskTitle != null && taskBody != null) {

                            createTaskTile(taskTitle, taskBody);
                        }
                    }
                    });

        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(v -> addNewTask());

    }

    private void addNewTask() {
        if (taskList.size() < 6) {
            Intent intent = new Intent(MainActivity.this, HandwritingRecognition.class);
            handwritingLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Task limit reached", Toast.LENGTH_SHORT).show();
        }
    }



    private void createTaskTile(String title, String description) {
        try {

            Log.d("Debug", "Creating task with title: " + title + " and description " + description);
            View newTile = getLayoutInflater().inflate(R.layout.task_tile, gridLayout, false );

            TextView titleView = newTile.findViewById(R.id.taskTitle);
            TextView descriptionView = newTile.findViewById(R.id.taskDescription);
            titleView.setText(title);
            descriptionView.setText(description);
//
//        int screenWidth = getResources().getDisplayMetrics().widthPixels;
//        int columnWidth = screenWidth/2;

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            newTile.setLayoutParams(params);

            gridLayout.addView(newTile);
            taskList.add(newTile);

            TextView heartView = newTile.findViewById(R.id.task1Heart);
            TextView exclamationView = newTile.findViewById(R.id.task1Exclamation);

            heartView.setVisibility(View.GONE);
            exclamationView.setVisibility(View.GONE);

            heartList.add(heartView);
            exclamationList.add(exclamationView);

            Log.d("Debug", "Task successfully added to gridLayout and taskList");
        } catch (Exception e) {
            Log.e("Debug", "Error in createTaskTile: " + e.getMessage());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        saveTasksToPreferences();
    }

    private void saveTasksToPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        for (int i = 0; i < taskList.size(); i++) {
            TextView titleView = taskList.get(i).findViewById(R.id.taskTitle);
            TextView descriptionView = taskList.get(i).findViewById(R.id.taskDescription);
            String title = titleView.getText().toString();
            String description = descriptionView.getText().toString();

            editor.putString("taskTitle_" + i, title);
            editor.putString("taskDescription_" + i, description);
        }

        editor.putInt("taskCount", taskList.size());
        editor.apply();
    }

    private void loadTasksFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        int taskCount = sharedPreferences.getInt("taskCount", 0);

        for (int i = 0; i < taskCount; i++) {
            String title = sharedPreferences.getString("taskTitle_" + i, "");
            String description = sharedPreferences.getString("taskDescription_" + i, "");

            createTaskTile(title, description);
        }
    }
    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> objPrediction = objLib.recognize(gesture);

        if (!objPrediction.isEmpty() && objPrediction.get(0).score > 4) {
            String gestureName = objPrediction.get(0).name;

            int[] gestureOverlayLocation = new int[2];
            overlay.getLocationOnScreen(gestureOverlayLocation);

            GestureStroke firstStroke = gesture.getStrokes().get(0);
            float[] firstStrokePoints = firstStroke.points;

            float lastX = firstStrokePoints[0] + gestureOverlayLocation[0];
            float lastY = firstStrokePoints[1] + gestureOverlayLocation[1];

            if (gestureName.startsWith("heart")) {
                handleHeartGesture(overlay, lastX, lastY);
            } else if (gestureName.startsWith("exclamation")) {
                handleExclamationGesture(overlay, lastX, lastY);
            } else if (gestureName.startsWith("square")) {
                addNewTask();
            } else if (gestureName.startsWith("cross")) {
                handleRemoveTaskGesture(overlay, lastX, lastY);
            } else if (gestureName.startsWith("checkmark")) {
                handleCompleteTaskGesture(overlay, lastX, lastY);
            }
        } else {
            Toast.makeText(this, "No gesture detected", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleHeartGesture(GestureOverlayView overlay, float lastX, float lastY) {
        for (int i = 0; i < taskList.size(); i++) {
            if (isGestureInView(taskList.get(i), lastX, lastY)) {
                toggleHeart(heartList.get(i), i + 1);
                return;
            }
        }
    }

    private void handleExclamationGesture(GestureOverlayView overlay, float lastX, float lastY) {
        for (int i = 0; i < taskList.size(); i++) {
            if (isGestureInView(taskList.get(i), lastX, lastY)) {
                toggleExclamation(exclamationList.get(i), i + 1);
                return;
            }
        }
    }

    private void handleRemoveTaskGesture(GestureOverlayView overlay, float lastX, float lastY) {
        for (int i = 0; i < taskList.size(); i++) {
            if (isGestureInView(taskList.get(i), lastX, lastY)) {
                removeTask(i);
                return;
            }
        }
    }

    private void removeTask(int taskIndex) {
        gridLayout.removeView(taskList.get(taskIndex));
        taskList.remove(taskIndex);
        heartList.remove(taskIndex);
        exclamationList.remove(taskIndex);
        Toast.makeText(this, "Task removed", Toast.LENGTH_SHORT).show();
    }

    private void toggleHeart(TextView heartView, int taskNumber) {
        boolean isFavorited = heartView.getVisibility() == View.VISIBLE;
        heartView.setVisibility(isFavorited ? View.GONE : View.VISIBLE);
        displayToast("Task " + taskNumber + (isFavorited ? " unfavorited" : " favorited"));
    }

    private void toggleExclamation(TextView exclamationView, int taskNumber) {
        boolean isUrgent = exclamationView.getVisibility() == View.VISIBLE;
        exclamationView.setVisibility(isUrgent ? View.GONE : View.VISIBLE);
        displayToast("Task " + taskNumber + (isUrgent ? " urgency removed" : " marked as urgent"));
    }

    private boolean isGestureInView(View view, float x, float y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        Rect rect = new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
        return rect.contains((int) x, (int) y);
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void handleCompleteTaskGesture(GestureOverlayView overlay, float lastX, float lastY) {
        for (int i = 0; i < taskList.size(); i++) {
            if (isGestureInView(taskList.get(i), lastX, lastY)) {
                completeTask(i);
                return;
            }
        }
    }
    private void completeTask(int taskIndex) {
        View completedTask = taskList.get(taskIndex);
        gridLayout.removeView(completedTask);  // Remove task from the GridLayout (UI)
        taskList.remove(taskIndex);  // Remove from the task list
        completedTaskList.add(completedTask);  // Add to the completed task list
        Toast.makeText(this, "Task marked as complete", Toast.LENGTH_SHORT).show();
    }

}