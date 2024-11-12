package wflor4.cs423.textrecognizer;

import android.content.ContentValues;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;
import com.google.mlkit.vision.digitalink.Ink;
import com.google.mlkit.vision.digitalink.RecognitionResult;

public class StrokeManager {

    private static DigitalInkRecognitionModel model = null;
    private static Ink.Builder inkBuilder = Ink.builder();
    private static Ink.Stroke.Builder strokeBuilder = null;

    public static void addNewTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        long t = System.currentTimeMillis();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                strokeBuilder = Ink.Stroke.builder();
                strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                break;
            case MotionEvent.ACTION_MOVE:
                if (strokeBuilder != null) {
                    strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (strokeBuilder != null) {
                    strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                    inkBuilder.addStroke(strokeBuilder.build());
                    strokeBuilder = null;
                }
                break;
        }
    }

    public static void download() {
        DigitalInkRecognitionModelIdentifier modelIdentifier = null;
        try {
            modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("tr-TR");
        } catch (MlKitException e) {
            Log.i(ContentValues.TAG, "Exception: " + e);
        }

        if (modelIdentifier == null) {
            Log.i(ContentValues.TAG, "Model not found");
            return;
        }

        model = DigitalInkRecognitionModel.builder(modelIdentifier).build();
        RemoteModelManager remoteModelManager = RemoteModelManager.getInstance();

        remoteModelManager.download(model, new DownloadConditions.Builder().build())
                .addOnSuccessListener(aVoid -> Log.i(ContentValues.TAG, "Model downloaded"))
                .addOnFailureListener(e -> Log.e(ContentValues.TAG, "Error downloading model: " + e));
    }

    public static void recognize(TextView textView) {
        if (model == null) {
            Log.e(ContentValues.TAG, "Model is not downloaded yet");
            return;
        }

        DigitalInkRecognizer recognizer = DigitalInkRecognition.getClient(
                DigitalInkRecognizerOptions.builder(model).build());

        Ink ink = inkBuilder.build();
        recognizer.recognize(ink)
                .addOnSuccessListener(result -> {
                    String recognizedText = result.getCandidates().get(0).getText();
                    String currentText = textView.getText().toString();
                    textView.setText(currentText + " " + recognizedText);  // Append recognized text to existing text
                })
                .addOnFailureListener(e -> Log.e(ContentValues.TAG, "Error during recognition: " + e));
    }

    public static void clear() {
        inkBuilder = Ink.builder();
    }
}
