package wflor4.cs423.textrecognizer;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View {

    private Paint currentStrokePaint;
    private Paint canvasPaint;
    private Path currentStroke;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private static final float STROKE_WIDTH_DP = 8.0f;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();


    }

    private void init() {
        currentStrokePaint = new Paint();
        currentStrokePaint.setColor(Color.BLACK);
        currentStrokePaint.setAntiAlias(true);
        currentStrokePaint.setStrokeWidth(STROKE_WIDTH_DP);
        currentStrokePaint.setStyle(Paint.Style.STROKE);
        currentStrokePaint.setStrokeJoin(Paint.Join.ROUND);
        currentStrokePaint.setStrokeCap(Paint.Cap.ROUND);

        currentStroke = new Path();
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void setStrokeColor(int color) { // added this for color changing Esat
        currentStrokePaint.setColor(color);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(currentStroke, currentStrokePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                currentStroke.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                currentStroke.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                currentStroke.lineTo(x, y);
                drawCanvas.drawPath(currentStroke, currentStrokePaint);
                currentStroke.reset();
                break;
            default:
                break;
        }

        StrokeManager.addNewTouchEvent(event);
        invalidate();
        return true;
    }

    public void clear() {
        onSizeChanged(canvasBitmap.getWidth(), canvasBitmap.getHeight(),
                canvasBitmap.getWidth(), canvasBitmap.getHeight());
    }
}
