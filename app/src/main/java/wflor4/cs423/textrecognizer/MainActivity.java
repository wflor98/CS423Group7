package wflor4.cs423.textrecognizer;

import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureStroke;
import android.gesture.Prediction;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
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
    private boolean gestureDetectionEnabled = true; // flag to track gesture detection status


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

        objOverlay.setGestureStrokeLengthThreshold(1f);
        objOverlay.setGestureStrokeAngleThreshold(180f);
        objOverlay.setGestureStrokeSquarenessTreshold(0.5f);
        objOverlay.setGestureStrokeWidth(10f);
        gridLayout = findViewById(R.id.gridLayout);  // Correct casting to androidx.gridlayout.widget.GridLayout

        loadTasksFromPreferences();

        handwritingLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String taskTitle = result.getData().getStringExtra("savedTitle");
                        String taskBody = result.getData().getStringExtra("savedBody");
                        String taskDate = result.getData().getStringExtra("selectedDate");
                        int selectedColor = result.getData().getIntExtra("selectedColor", ContextCompat.getColor(this, R.color.default_task_background));

                        boolean isEditMode = result.getData().getBooleanExtra("isEditMode", false);
                        int taskIndex = result.getData().getIntExtra("taskIndex", -1);

                        if (isEditMode && taskIndex >= 0 && taskIndex < taskList.size()) {
                            updateTaskTile(taskIndex, taskTitle, taskBody, taskDate, selectedColor);
                        } else {
                            if (taskTitle != null && !taskTitle.trim().isEmpty() && !taskTitle.equalsIgnoreCase("Title")) {
                                createTaskTile(taskTitle, taskBody, taskDate, selectedColor);
                            } else {
                                Toast.makeText(MainActivity.this, "Please provide a valid title for the task.", Toast.LENGTH_SHORT).show();
                                addNewTask();
                            }
                        }
                    }
                }
        );


        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(v -> addNewTask());

        SwitchCompat switchGesture = findViewById(R.id.switch_gesture);
        GestureOverlayView gestureOverlay = findViewById(R.id.gestureOverlay);

        switchGesture.setChecked(true);
        gestureDetectionEnabled = true;
        gestureOverlay.setVisibility(View.VISIBLE);

        switchGesture.setOnCheckedChangeListener((buttonView, isChecked) -> {
            gestureDetectionEnabled = isChecked;

            if (isChecked) {
                gestureOverlay.setVisibility(View.VISIBLE);
            } else {
                gestureOverlay.setVisibility(View.GONE);
            }

            Toast.makeText(MainActivity.this,
                    "Gesture detection " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

    }

    private void updateTaskTile(int taskIndex, String newTitle, String newBody, String newDate, int color) {
        if (taskIndex >= 0 && taskIndex < taskList.size()) {
            View taskTile = taskList.get(taskIndex);
            TextView titleView = taskTile.findViewById(R.id.taskTitle);
            TextView bodyView = taskTile.findViewById(R.id.taskDescription);
            TextView dateView = taskTile.findViewById(R.id.taskDate);

            titleView.setText(newTitle);
            bodyView.setText(newBody);

            if (newDate != null && !newDate.trim().isEmpty()) {
                dateView.setText("Due: " + newDate);
                dateView.setVisibility(View.VISIBLE);
            } else {
                dateView.setVisibility(View.GONE);
            }

            taskTile.setBackgroundColor(color);
        }
    }

    private void addNewTask() {
        Intent intent = new Intent(MainActivity.this, HandwritingRecognition.class);
        handwritingLauncher.launch(intent);
    }


    private void createTaskTile(String title, String description, String date, int color) {
        try {
            Log.d("Debug", "Creating task with title: " + title + ", description: " + description + ", date: " + date);
            View newTile = getLayoutInflater().inflate(R.layout.task_tile, gridLayout, false);

            TextView titleView = newTile.findViewById(R.id.taskTitle);
            TextView descriptionView = newTile.findViewById(R.id.taskDescription);
            TextView dateView = newTile.findViewById(R.id.taskDate);

            titleView.setText(title);
            descriptionView.setText(description);

            if (date == null || date.trim().isEmpty() || date.equals("00/00/00")) {
                dateView.setVisibility(View.GONE);
            } else {
                dateView.setText("Due: " + date);
                dateView.setVisibility(View.VISIBLE);
            }

            newTile.setBackgroundColor(color);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            //params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = 300; // makes all of the tile the same size.
                                 // the tiles are all different size when there is no due date
                                 // looks chaotic. We can change this back later.
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            newTile.setLayoutParams(params);

            newTile.setOnLongClickListener(view -> {
                if (!gestureDetectionEnabled) {
                    showTaskOptionsMenu(view);
                }
                return true;
            });

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

    private void showTaskOptionsMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.task_options_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int taskIndex = taskList.indexOf(view);
            int itemId = menuItem.getItemId();
            if (itemId == R.id.menu_favor) {
                toggleHeart(heartList.get(taskIndex), taskIndex + 1);
                return true;
            } else if (itemId == R.id.menu_urgent) {
                toggleExclamation(exclamationList.get(taskIndex), taskIndex + 1);
                return true;
            } else if (itemId == R.id.menu_edit) {
                editTask(taskIndex);
                return true;
            } else if (itemId == R.id.menu_delete) {
                removeTask(taskIndex);
                return true;
            } else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void editTask(int taskIndex) {
        Intent intent = new Intent(MainActivity.this, HandwritingRecognition.class);

        TextView titleView = taskList.get(taskIndex).findViewById(R.id.taskTitle);
        TextView bodyView = taskList.get(taskIndex).findViewById(R.id.taskDescription);
        TextView dateView = taskList.get(taskIndex).findViewById(R.id.taskDate);

        intent.putExtra("editTitle", titleView.getText().toString());
        intent.putExtra("editBody", bodyView.getText().toString());
        intent.putExtra("editDate", dateView.getText().toString().replace("Due: ", ""));
        intent.putExtra("taskIndex", taskIndex);

        handwritingLauncher.launch(intent);
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
            TextView dateView = taskList.get(i).findViewById(R.id.taskDate);
            View taskTile = taskList.get(i);

            String title = titleView.getText().toString();
            String description = descriptionView.getText().toString();
            String date = dateView.getText().toString().replace("Due: ", "");
            int color = ((ColorDrawable) taskTile.getBackground()).getColor();

            editor.putString("taskTitle_" + i, title);
            editor.putString("taskDescription_" + i, description);
            editor.putString("taskDate_" + i, date);
            editor.putInt("taskColor_" + i, color);
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
            String date = sharedPreferences.getString("taskDate_" + i, "");
            int color = sharedPreferences.getInt("taskColor_" + i, ContextCompat.getColor(this, R.color.default_task_background));

            createTaskTile(title, description, date, color);
        }
    }
    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        if (!gestureDetectionEnabled) {
            Log.d("Debug", "Gesture detection is disabled");
            return;
        }
        Log.d("Debug", "onGesturePerformed");

        if (isExclamationGesture(gesture)) {
            Log.d("Debug", "Exclamation gesture detected");
            int[] gestureOverlayLocation = new int[2];
            overlay.getLocationOnScreen(gestureOverlayLocation);

            GestureStroke firstStroke = gesture.getStrokes().get(0);
            float[] firstStrokePoints = firstStroke.points;

            float lastX = firstStrokePoints[0] + gestureOverlayLocation[0];
            float lastY = firstStrokePoints[1] + gestureOverlayLocation[1];

            handleExclamationGesture(overlay, lastX, lastY);
        } else if (isCrossGesture(gesture)) {
            Log.d("Debug", "Cross gesture detected");
            int[] gestureOverlayLocation = new int[2];
            overlay.getLocationOnScreen(gestureOverlayLocation);
            Log.d("Debug", "Cross gesture detected");

            GestureStroke firstStroke = gesture.getStrokes().get(0);
            float[] firstStrokePoints = firstStroke.points;

            float lastX = firstStrokePoints[0] + gestureOverlayLocation[0];
            float lastY = firstStrokePoints[1] + gestureOverlayLocation[1];

            handleRemoveTaskGesture(overlay, lastX, lastY);
        } else {
            Log.d("Debug", "not ! or X ");

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
//            } else if (gestureName.startsWith("exclamation")) {
//                handleExclamationGesture(overlay, lastX, lastY);
            } else if (gestureName.startsWith("square")) {
                addNewTask();
//            } else if (gestureName.startsWith("cross")) {
//                handleRemoveTaskGesture(overlay, lastX, lastY);
            } else if (gestureName.startsWith("checkmark")) {
                handleCompleteTaskGesture(overlay, lastX, lastY);
            }
        } else {
            Toast.makeText(this, "No gesture detected", Toast.LENGTH_SHORT).show();
        }
        }
    }

    private boolean isExclamationGesture(Gesture gesture) {
        if (gesture.getStrokes().size() != 2) return false;

        GestureStroke lineStroke = gesture.getStrokes().get(0);
        GestureStroke dotStroke = gesture.getStrokes().get(1);

        float [] linePoints = lineStroke.points;
        float [] dotPoints = dotStroke.points;

        float lineStartX = linePoints[0];
        float lineEndX = linePoints[linePoints.length - 2];
        float lineStartY = linePoints[1];
        float lineEndY = linePoints[linePoints.length - 1];

        float lineDeltaX = Math.abs(lineStartX - lineEndX);
        float lineDeltaY = Math.abs(lineStartY - lineEndY);

        boolean isVerticalLine = lineDeltaX < 30 && lineDeltaY > 40;

        float dotStartX = dotPoints[0];
        float dotEndX = dotPoints[dotPoints.length - 2];
        float dotStartY = dotPoints[1];
        float dotEndY = dotPoints[dotPoints.length - 1];

        float dotDistance = (float) Math.sqrt(Math.pow(dotEndX - dotStartX, 2) + Math.pow(dotEndY - dotStartY, 2));
        boolean isDotSmall = dotDistance < 40;

        boolean isDotBelowLine = dotStartY > lineEndY && Math.abs(dotStartX - lineEndX) < 30;
        return isVerticalLine && isDotBelowLine && isDotSmall;
    }

    private boolean isCrossGesture(Gesture gesture) {
        if (gesture.getStrokes().size() != 2) return false;

        GestureStroke stroke1 = gesture.getStrokes().get(0);
        GestureStroke stroke2 = gesture.getStrokes().get(1);

        float[] points1 = stroke1.points;
        float[] points2 = stroke2.points;

        float startX1 = points1[0];
        float startY1 = points1[1];
        float endX1 = points1[points1.length - 2];
        float endY1 = points1[points1.length -1];

        float startX2 = points2[0];
        float startY2 = points2[1];
        float endX2 = points2[points2.length - 2];
        float endY2 = points2[points2.length - 1];

        double angle1 = Math.atan2(endY1 - startY1, endX1 - startX1);
        double angle2 = Math.atan2(endY2 - startY2, endX2 - startX2);

        boolean isCrossed = Math.abs(Math.toDegrees(angle1 - angle2)) > 45 && Math.abs(Math.toDegrees(angle1 - angle2)) < 135;

        float midX1 = (startX1 + endX1) / 2;
        float midY1 = (startY1 + endY1) / 2;
        float midX2 = (startX2 + endX2) / 2;
        float midY2 = (startY2 + endY2) / 2;

        boolean isIntersectingNearCenter = Math.hypot(midX1 - midX2, midY1 - midY2) < 50;

        return isCrossed && isIntersectingNearCenter;
    }

    private void handleHeartGesture(GestureOverlayView overlay, float lastX, float lastY) {
        for (int i = 0; i < taskList.size(); i++) {
            if (isGestureInView(taskList.get(i), lastX, lastY)) {
                Log.d("Debug", "Heart found. lastX: " + lastX + " lastY: " + lastY);
                toggleHeart(heartList.get(i), i + 1);
                return;
            }
        }
    }

    private void handleExclamationGesture(GestureOverlayView overlay, float lastX, float lastY) {
        Log.d("Debug", "handleExclamationGesture");

        for (int i = 0; i < taskList.size(); i++) {
            if (isGestureInView(taskList.get(i), lastX, lastY)) {
                Log.d("Debug", "Exclamation task found");
                toggleExclamation(exclamationList.get(i), i + 1);
                return;
            } else {
                Log.d("Debug", "Exclamation task not found lastX: " + lastX + " lastY: " + lastY);

            }
        }
    }

    private void handleRemoveTaskGesture(GestureOverlayView overlay, float lastX, float lastY) {
        Log.d("Debug", "handleRemoveTaskGesture");

        for (int i = 0; i < taskList.size(); i++) {
            Log.d("Debug", "Task: " + i);

            if (isGestureInView(taskList.get(i), lastX, lastY)) {
                Log.d("Debug", "Remove task found");
                removeTask(i);
                return;
            } else {
                Log.d("Debug", "Remove task not found: " + lastX + " lastY: " + lastY);

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
        int padding = 25;
        Log.d("Debug", "Tile location: " + location[0] + " " + location[1]);
        Rect rect = new Rect(location[0], location[1], location[0] + view.getWidth() + padding, location[1] + view.getHeight() + padding);

        Log.d("Debug", "Tile location contains: " + view.getWidth() + " " + view.getHeight());
        Log.d("Debug", "Tile location contains: " + rect.contains((int) x, (int) y));

        return rect.contains((int) x, (int) y);
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void handleCompleteTaskGesture(GestureOverlayView overlay, float lastX, float lastY) {
        for (int i = 0; i < taskList.size(); i++) {
            if (isGestureInView(taskList.get(i), lastX, lastY)) {
                Log.d("Debug", "Heart found. lastX: " + lastX + " lastY: " + lastY);
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