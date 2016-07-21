package errormjt.mason980.fretter_android.fretter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

    //  Currently not used


public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

    }

    public void openScreen(View v) {
        String screen_name = "Fretting_screen";

        Intent intent = new Intent(this, Fretting_screen.class);
        startActivity(intent);

        finish();
    }

}
