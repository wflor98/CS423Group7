package wflor4.cs423.textrecognizer;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Button btnStart = findViewById(R.id.btnStart);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to navigate to MainActivity
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);  // Start MainActivity
            }
        });
    }
}
