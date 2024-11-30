package wflor4.cs423.textrecognizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// Esats Class for the Main Menu
public class HomeActivity extends AppCompatActivity {

    private List<Task> taskList = new ArrayList<>();
    private SearchView searchView;
    private LinearLayout searchResultLayout;
    private RecyclerView searchResultRecycler;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button buttonNavigate = findViewById(R.id.buttonNavigate);

        // Set up click listener for navigation
        buttonNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to MainActivity
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        // Initialize views
        searchView = findViewById(R.id.searchView);
        searchResultLayout = findViewById(R.id.searchResultLayout);

        // RecyclerView setup
        searchResultRecycler = new RecyclerView(this);
        searchResultRecycler.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );
        searchResultLayout.addView(searchResultRecycler);

        // Load tasks from SharedPreferences

    }

    @Override
    protected void onResume() {
        super.onResume();
        taskList.clear();
        loadTasksFromPreferences();

        // Set up search functionality
        setupSearchView();

        // Set up task card interactions
        setupTaskCardInteractions();

    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                displaySearchResults(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                displaySearchResults(newText);
                return false;
            }
        });
    }

    private void displaySearchResults(String query) {
        // Filter tasks based on query
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : taskList) {
            if (task.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredTasks.add(task);
            }
        }

        // Update the adapter and notify the RecyclerView
        taskAdapter.updateData(filteredTasks);
    }

    private void setupTaskCardInteractions() {
        taskAdapter = new TaskAdapter(taskList, this);
        searchResultRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchResultRecycler.setAdapter(taskAdapter);

        // Set up swipe gestures for task cards
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task task = taskList.get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    // Handle left swipe (e.g., delete task)
                    deleteTask(task);
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Handle right swipe (e.g., mark task as complete)
                    completeTask(task);
                }
            }
        }).attachToRecyclerView(searchResultRecycler);
    }

    private void deleteTask(Task task) {
        taskList.remove(task);
        SharedPreferences preferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        int index = taskList.indexOf(task);
        editor.remove("taskTitle_" + index);
        editor.remove("taskDescription_" + index);
        editor.remove("taskDate_" + index);
        editor.remove("taskColor_" + index);

        editor.apply();

        taskAdapter.notifyDataSetChanged();
    }

    private void completeTask(Task task) {
        // Mark the task as complete and update the UI
        task.setCompleted(true);
        taskAdapter.notifyDataSetChanged();
    }

    private void loadTasksFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        int taskCount = sharedPreferences.getInt("taskCount", 0);

        for (int i = 0; i < taskCount; i++) {
            String title = sharedPreferences.getString("taskTitle_" + i, "");
            String description = sharedPreferences.getString("taskDescription_" + i, "");
            String date = sharedPreferences.getString("taskDate_" + i, "");

            if (!title.isEmpty()) {
                taskList.add(new Task(title, description, date));
            }
        }
    }

}