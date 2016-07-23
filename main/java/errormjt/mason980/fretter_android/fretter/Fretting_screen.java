package errormjt.mason980.fretter_android.fretter;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import static android.R.attr.alertDialogStyle;
import static android.R.attr.path;

public class Fretting_screen extends AppCompatActivity {

    boolean game = true;
    GuitarView guitar;
    FrameLayout frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fretting_screen);



        getIntent().getIntExtra("y_size", 0);
        getIntent().getIntExtra("x_size", 0);
        int fret_number = getIntent().getIntExtra("fret_number", 3);
        int string_number = getIntent().getIntExtra("string_number", 6);

        game = getIntent().getBooleanExtra("game", true);

   //     frame = new FrameLayout(Fretting_screen.this);      // sizing here

        try {

            guitar = new GuitarView(getApplicationContext());
            guitar.setup(string_number, fret_number, 0, 0, Fretting_screen.this, game, !game);

            if (game) {
                guitar.beginGame();
            } else {
                guitar.beginOther();
            }
            setContentView(guitar);

        } catch (Exception e) {
            toMenu(e.getMessage());
        }
     //   setContentView(frame);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (game) {
            inflater.inflate(R.menu.app_bar_game, menu);
        } else {
            inflater.inflate(R.menu.app_bar_edit, menu);
        }
        return true;
    }

    private void toMenu(String str) {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("error_message", str);
        finish();
        startActivity(intent);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_goto:
                Intent intent = new Intent(this, MainMenuActivity.class);
                finish();
                startActivity(intent);
                return true;

            case R.id.save_chord:
                guitar.saveChord();
                return true;

            case R.id.delete_chord:
                try {
                    guitar.deleteChord();
                } catch (Exception e) {
                    toMenu(e.getMessage());
                }
                return true;

            case R.id.default_chords:
                guitar.fillFile();
                return true;

            case R.id.delete_all_chords:
                try {
                    guitar.deleteAllChords();
                } catch (Exception e) {
                    toMenu(e.getMessage());
                }
                return true;


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

    public void showErrorMessage(String mess) {            // this is bad
        if (mess == ""  || mess == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(Fretting_screen.this);
        builder.setMessage(mess).setTitle("ALERT");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
