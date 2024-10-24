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
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {
    private GestureLibrary objLib;

    private List<View> taskList = new LinkedList<>();
    private List<TextView> heartList = new LinkedList<>();
    private List<TextView> exclamationList = new LinkedList<>();
    private List<View> completedTaskList = new LinkedList<>();
    private GridLayout gridLayout;

    private FloatingActionButton fabAddTask;


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

        taskList.add(findViewById(R.id.tile1));
        taskList.add(findViewById(R.id.tile2));
        taskList.add(findViewById(R.id.tile3));
        taskList.add(findViewById(R.id.tile4));
        taskList.add(findViewById(R.id.tile5));

        heartList.add(findViewById(R.id.task1Heart));
        heartList.add(findViewById(R.id.task2Heart));
        heartList.add(findViewById(R.id.task3Heart));
        heartList.add(findViewById(R.id.task4Heart));
        heartList.add(findViewById(R.id.task5Heart));

        exclamationList.add(findViewById(R.id.task1Exclamation));
        exclamationList.add(findViewById(R.id.task2Exclamation));
        exclamationList.add(findViewById(R.id.task3Exclamation));
        exclamationList.add(findViewById(R.id.task4Exclamation));
        exclamationList.add(findViewById(R.id.task5Exclamation));

        fabAddTask = findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HandwritingRecognition.class);
                startActivity(intent);
            }
        });

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

    private void addNewTask() {
        if (taskList.size() < 6) {
            View newTask = new View(this);
            TextView newHeart = new TextView(this);
            TextView newExclamation = new TextView(this);

            gridLayout.addView(newTask);
            taskList.add(newTask);
            heartList.add(newHeart);
            exclamationList.add(newExclamation);

            Toast.makeText(this, "New task added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task limit reached", Toast.LENGTH_SHORT).show();
        }
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