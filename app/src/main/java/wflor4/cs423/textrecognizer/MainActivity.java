package wflor4.cs423.textrecognizer;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureStroke;
import android.gesture.Prediction;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {
    private GestureLibrary objLib;

    // Define your task tile views, heart, exclamation, and task lists
    private View tile1, tile2, tile3, tile4, tile5;
    private TextView heart1, heart2, heart3, heart4, heart5;
    private TextView exclamation1, exclamation2, exclamation3, exclamation4, exclamation5;

    private boolean isFavoritedTask1 = false;
    private boolean isUrgentTask1 = false;
    private boolean isFavoritedTask2 = false;
    private boolean isUrgentTask2 = false;
    private boolean isFavoritedTask3 = false;
    private boolean isUrgentTask3 = false;
    private boolean isFavoritedTask4 = false;
    private boolean isUrgentTask4 = false;
    private boolean isFavoritedTask5 = false;
    private boolean isUrgentTask5 = false;

    // Task lists
    private List<View> taskList = new LinkedList<>();
    private List<View> completedTaskList = new LinkedList<>();
    private GridLayout gridLayout;  // Correct androidx GridLayout

    private FloatingActionButton fabAddTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        objLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!objLib.load()) {
            finish();
        }

        // Initialize GestureOverlayView
        GestureOverlayView objOverlay = (GestureOverlayView) findViewById(R.id.gestureOverlay);
        objOverlay.addOnGesturePerformedListener(this);

        // Initialize GridLayout
        gridLayout = findViewById(R.id.gridLayout);  // Correct casting to androidx.gridlayout.widget.GridLayout

        // Find task tiles by ID
        tile1 = findViewById(R.id.tile1);
        tile2 = findViewById(R.id.tile2);
        tile3 = findViewById(R.id.tile3);
        tile4 = findViewById(R.id.tile4);
        tile5 = findViewById(R.id.tile5);

        taskList.add(tile1);
        taskList.add(tile2);
        taskList.add(tile3);
        taskList.add(tile4);
        taskList.add(tile5);

        // Find the heart TextViews
        heart1 = findViewById(R.id.task1Heart);
        heart2 = findViewById(R.id.task2Heart);
        heart3 = findViewById(R.id.task3Heart);
        heart4 = findViewById(R.id.task4Heart);
        heart5 = findViewById(R.id.task5Heart);

        // Find the exclamation mark TextViews
        exclamation1 = findViewById(R.id.task1Exclamation);
        exclamation2 = findViewById(R.id.task2Exclamation);
        exclamation3 = findViewById(R.id.task3Exclamation);
        exclamation4 = findViewById(R.id.task4Exclamation);
        exclamation5 = findViewById(R.id.task5Exclamation);

        fabAddTask = findViewById(R.id.fab_add_task);

        // Set an OnClickListener to start the HandwritingRecognition activity
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the HandwritingRecognition activity
                Intent intent = new Intent(MainActivity.this, HandwritingRecognition.class);
                startActivity(intent);  // Start the activity
            }
        });

    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> objPrediction = objLib.recognize(gesture);

        if (!objPrediction.isEmpty() && objPrediction.get(0).score > 3) {
            String gestureName = objPrediction.get(0).name;

            // Get the overlay's position on the screen
            int[] gestureOverlayLocation = new int[2];
            overlay.getLocationOnScreen(gestureOverlayLocation);

            // Iterate through all strokes (if multi-stroke gestures are used)
            GestureStroke firstStroke = gesture.getStrokes().get(0);
            float[] firstStrokePoints = firstStroke.points;

            // Adjust the X and Y coordinates based on the overlay's location
            float lastX = firstStrokePoints[0] + gestureOverlayLocation[0];  // Corrected X
            float lastY = firstStrokePoints[1] + gestureOverlayLocation[1];  // Corrected Y

            // Handle different gestures based on the recognized gesture name
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

    // Helper method to handle heart gestures
    private void handleHeartGesture(GestureOverlayView overlay, float lastX, float lastY) {
        for (int i = 0; i < taskList.size(); i++) {
            if (isGestureInView(taskList.get(i), lastX, lastY)) {
                TextView heartView = findHeartView(i);  // Find the heart TextView
                toggleHeart(heartView, i + 1);  // Toggle heart emoji
                return;
            }
        }
    }

    // Helper method to handle exclamation gestures
    private void handleExclamationGesture(GestureOverlayView overlay, float lastX, float lastY) {
        for (int i = 0; i < taskList.size(); i++) {
            if (isGestureInView(taskList.get(i), lastX, lastY)) {
                TextView exclamationView = findExclamationView(i);  // Find the exclamation TextView
                toggleExclamation(exclamationView, i + 1);  // Toggle exclamation mark
                return;
            }
        }
    }

    // Helper method to toggle heart emoji on or off
    private void toggleHeart(TextView heartView, int taskNumber) {
        boolean isFavorited;
        switch (taskNumber) {
            case 1:
                isFavorited = isFavoritedTask1 = !isFavoritedTask1;
                break;
            case 2:
                isFavorited = isFavoritedTask2 = !isFavoritedTask2;
                break;
            case 3:
                isFavorited = isFavoritedTask3 = !isFavoritedTask3;
                break;
            case 4:
                isFavorited = isFavoritedTask4 = !isFavoritedTask4;
                break;
            case 5:
                isFavorited = isFavoritedTask5 = !isFavoritedTask5;
                break;
            default:
                return;
        }

        // Show or hide the heart emoji
        if (isFavorited) {
            heartView.setVisibility(View.VISIBLE);
            displayToast("Task " + taskNumber + " favorited");
        } else {
            heartView.setVisibility(View.GONE);
            displayToast("Task " + taskNumber + " unfavorited");
        }
    }

    // Helper method to toggle exclamation mark on or off
    private void toggleExclamation(TextView exclamationView, int taskNumber) {
        boolean isUrgent;
        switch (taskNumber) {
            case 1:
                isUrgent = isUrgentTask1 = !isUrgentTask1;
                break;
            case 2:
                isUrgent = isUrgentTask2 = !isUrgentTask2;
                break;
            case 3:
                isUrgent = isUrgentTask3 = !isUrgentTask3;
                break;
            case 4:
                isUrgent = isUrgentTask4 = !isUrgentTask4;
                break;
            case 5:
                isUrgent = isUrgentTask5 = !isUrgentTask5;
                break;
            default:
                return;
        }

        // Show or hide the exclamation mark
        if (isUrgent) {
            exclamationView.setVisibility(View.VISIBLE);
            displayToast("Task " + taskNumber + " marked as urgent");
        } else {
            exclamationView.setVisibility(View.GONE);
            displayToast("Task " + taskNumber + " urgency removed");
        }
    }

    // Find the heart TextView for a task
    private TextView findHeartView(int taskIndex) {
        switch (taskIndex) {
            case 0: return heart1;
            case 1: return heart2;
            case 2: return heart3;
            case 3: return heart4;
            case 4: return heart5;
            default: return null;
        }
    }

    // Find the exclamation TextView for a task
    private TextView findExclamationView(int taskIndex) {
        switch (taskIndex) {
            case 0: return exclamation1;
            case 1: return exclamation2;
            case 2: return exclamation3;
            case 3: return exclamation4;
            case 4: return exclamation5;
            default: return null;
        }
    }

    // Helper method to check if a gesture is within the bounds of a view (tile)
    private boolean isGestureInView(View view, float x, float y) {
        // Get the absolute position of the view on the screen
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        // Check if the gesture coordinates are within the bounds of the view
        Rect rect = new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
        return rect.contains((int) x, (int) y);
    }

    // Display toast message
    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Helper method to add a new task
    private void addNewTask() {
        if (taskList.size() < 6) {
            // Dynamically create a new task tile and add it to the list
            View newTask = new View(this);  // In a real case, you would inflate an actual task layout.
            gridLayout.addView(newTask);  // Add the new task to the GridLayout
            taskList.add(newTask);  // Add to the task list for gesture recognition
            Toast.makeText(this, "New task added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task limit reached", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to handle cross (remove) gesture
    private void handleRemoveTaskGesture(GestureOverlayView overlay, float lastX, float lastY) {
        for (int i = 0; i < taskList.size(); i++) {
            if (isGestureInView(taskList.get(i), lastX, lastY)) {
                removeTask(i);
                return;
            }
        }
    }

    // Helper method to handle checkmark (complete) gesture
    private void handleCompleteTaskGesture(GestureOverlayView overlay, float lastX, float lastY) {
        for (int i = 0; i < taskList.size(); i++) {
            if (isGestureInView(taskList.get(i), lastX, lastY)) {
                completeTask(i);
                return;
            }
        }
    }

    // Method to remove task
    private void removeTask(int taskIndex) {
        View taskToRemove = taskList.get(taskIndex);
        gridLayout.removeView(taskToRemove);  // Remove task from the GridLayout (UI)
        taskList.remove(taskIndex);  // Remove task from the task list
        Toast.makeText(this, "Task removed", Toast.LENGTH_SHORT).show();
    }

    // Method to mark task as complete
    private void completeTask(int taskIndex) {
        View completedTask = taskList.get(taskIndex);
        gridLayout.removeView(completedTask);  // Remove task from the GridLayout (UI)
        taskList.remove(taskIndex);  // Remove from the task list
        completedTaskList.add(completedTask);  // Add to the completed task list
        Toast.makeText(this, "Task marked as complete", Toast.LENGTH_SHORT).show();
    }

}