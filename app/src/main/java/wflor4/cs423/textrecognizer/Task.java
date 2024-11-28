package wflor4.cs423.textrecognizer;

public class Task {
    private final String title;
    private final String description;
    private final String date;
    private boolean completed;

    public Task(String title, String description, String date) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.completed = false;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public void setCompleted(boolean b) {
        this.completed = b;
    }
}
