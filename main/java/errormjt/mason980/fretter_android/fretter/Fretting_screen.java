package errormjt.mason980.fretter_android.fretter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import static android.R.attr.path;

public class Fretting_screen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fretting_screen);

        // to be used alongside other things in later releases
     //   Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
     //   setSupportActionBar(myToolbar);

        startFretter();
        // woohoo
    }

    private void startFretter() {

        // If I want the action bar I will need to throw this inside another view, probably
        GuitarView guitar = new GuitarView(getApplicationContext());
        guitar.beginGame();
        setContentView(guitar);

    }
    public boolean onCreateOptionsMenu(Menu menu) {
   //     MenuInflater inflater = getMenuInflater();
   //     inflater.inflate(R.menu.app_bar, menu);
        return true;
    }

    public void onGroupItemClick(MenuItem item) {       // currently defunct

        // One of the group items (using the onClick attribute) was clicked
        // The item parameter passed here indicates which item it is
        // All other menu item clicks are handled by onOptionsItemSelected()
/*
        switch (item.getItemId()) {
            case R.id.menu_goto:
                Intent intent = new Intent(this, MainMenuActivity.class);
                startActivity(intent);
                break;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                // super.onOptionsItemSelected(item);
                break;

        }
*/
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_goto:
                Intent intent = new Intent(this, MainMenuActivity.class);
                startActivity(intent);
                return  true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);


        }
    }

    public void goto_main_menu(View v) {

        //finish();
    }

    public void resize_guitar(View v) {
        // resize the guitar thing
        // maybe this should be in the main menu, and only the return to main menu button is on this page
    }

}
