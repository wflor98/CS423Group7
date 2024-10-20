package wflor4.cs423.textrecognizer;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.view.MotionEvent;
import java.util.ArrayList;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import android.view.View;
import android.util.Pair;
import android.widget.ImageView;

public class HeartGesture extends AppCompatActivity {

    private Paint paint;
    private Path gesturePath;
    private List<Pair<Float, Float>> gesturePoints;

    private View tile1, tile2, tile3, tile4, tile5;
    private ImageView heartIcon1, heartIcon2, heartIcon3, heartIcon4, heartIcon5;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tile1 = findViewById(R.id.tile1);
        tile2 = findViewById(R.id.tile2);
        tile3 = findViewById(R.id.tile3);
        tile4 = findViewById(R.id.tile4);
        tile5 = findViewById(R.id.tile5);

        heartIcon1 = findViewById(R.id.ic_heart1);
        heartIcon2 = findViewById(R.id.ic_heart2);
        heartIcon3 = findViewById(R.id.ic_heart3);
        heartIcon4 = findViewById(R.id.ic_heart4);
        heartIcon5 = findViewById(R.id.ic_heart5);


        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(8f);

        gesturePath = new Path();
        gesturePoints = new ArrayList<>();

        GestureOverlayView gestureOverlay = findViewById(R.id.gestureOverlay);
        gestureOverlay.addOnGestureListener(new OnGestureListener() {
            @Override
            public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
                gesturePath.reset();
                gesturePoints.clear();
                gesturePath.moveTo(event.getX(), event.getY());
                gesturePoints.add(new Pair<>(event.getX(), event.getY()));
            }

            @Override
            public void onGesture(GestureOverlayView gestureOverlayView, MotionEvent event) {
                gesturePath.lineTo(event.getX(), event.getY());
                gesturePoints.add(new Pair<>(event.getX(), event.getY()));
            }

            @Override
            public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
                overlay.invalidate();

                if (isHeartGesture(gesturePoints)) {
                    if (isGestureOverTile(event, tile1)) toggleHeartIcon(heartIcon1);
                    if (isGestureOverTile(event, tile2)) toggleHeartIcon(heartIcon2);
                    if (isGestureOverTile(event, tile3)) toggleHeartIcon(heartIcon3);
                    if (isGestureOverTile(event, tile4)) toggleHeartIcon(heartIcon4);
                    if (isGestureOverTile(event, tile5)) toggleHeartIcon(heartIcon5);

                else if (isCheckMarkGesture(gesturePoints)) {
                    handleGesture(event, "Task completed");
                    } else if (isCrossOutGesture(gesturePoints)) {
                        handleGesture(event, "Task removed");

                    }
                } else {
                    Toast.makeText(HeartGesture.this, "Gesture not recognized", Toast.LENGTH_SHORT).show();
                }

                gesturePath.reset();

            }

            @Override
            public void onGestureCancelled(GestureOverlayView gestureOverlayView, MotionEvent motionEvent) {

            }

        });}

    private boolean isCheckMarkGesture(List<Pair<Float, Float>> points) {
        if (points.size() < 10) return false;
        boolean hasUpRight = false;
        boolean hasDownRight = false;
        float prevX = points.get(0).first;
        float prevY = points.get(0).second;

        for (Pair<Float, Float> point: points) {
            float x = point.first;
            float y = point.second;

            if (x > prevX && y < prevY) {
                hasUpRight = true;
            } else if (x > prevX && y > prevY) {
                hasDownRight = true;
            }

            prevX = x;
            prevY = y;
        }

        return hasUpRight && hasDownRight;
    }

    private boolean isCrossOutGesture(List<Pair<Float, Float>> points) {
        if (points.size() < 10) return false;
        float firstY = points.get(0).second;
        for (Pair<Float, Float> point : points) {
            float y = point.second;

            if (Math.abs(y - firstY) > 50) {
                return false;
            }
        }
        return true;
    }

    private void handleGesture(MotionEvent event, String action) {
        if (isGestureOverTile(event, tile1)) {
            performActionOnTile(tile1, action);
        } if (isGestureOverTile(event, tile2)) {
            performActionOnTile(tile2, action);
        } if (isGestureOverTile(event, tile3)) {
            performActionOnTile(tile3, action);
        } if (isGestureOverTile(event, tile4)) {
            performActionOnTile(tile4, action);
        } if (isGestureOverTile(event, tile5)) {
            performActionOnTile(tile5, action);
        }
    }

    private void performActionOnTile(View tile, String action) {
        if (action.equals("Task removed")) {
            tile.setVisibility(View.GONE);
            Toast.makeText(HeartGesture.this, action, Toast.LENGTH_SHORT).show();
        } else if (action.equals("Task completed")) {
            Toast.makeText(HeartGesture.this, action, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isGestureOverTile(MotionEvent event, View tile) {
        int[] location = new int[2];
        tile.getLocationOnScreen(location);

        float x = event.getRawX();
        float y = event.getRawY();

        return x >= location[0] && x <= location[0] + tile.getWidth() &&
                y >= location[1] && y <= location[1] + tile.getHeight();
    }

    private boolean isHeartGesture(List<Pair<Float, Float>> points) {
        if (points.size() < 20) return false;

        List<String> directions = new ArrayList<>();
        float prevX = points.get(0).first;
        float prevY = points.get(0).second;

        for (Pair<Float, Float> point : points) {
            float x = point.first;
            float y = point.second;


            if (x > prevX && y < prevY) {
                directions.add("up-right");
            } else if (x < prevX && y < prevY) {
                directions.add("up-left");
            } else if (x > prevX && y > prevY) {
                directions.add("down-right");
            } else if (x < prevX && y > prevY) {
                directions.add("down-left");
            }

            prevX = x;
            prevY = y;

        }

        boolean hasLeftCurve = directions.contains("up-left");
        boolean hasRightCurve = directions.contains("up-right");
        boolean hasVShape = directions.contains("down-left") && directions.contains("down-right");

        return hasLeftCurve && hasRightCurve && hasVShape;
    }

    private void toggleHeartIcon(ImageView heartIcon) {
        if (heartIcon.getVisibility() == View.VISIBLE) {
            heartIcon.setVisibility(View.INVISIBLE);
        } else {
            heartIcon.setVisibility(View.VISIBLE);
        }
    }

}

