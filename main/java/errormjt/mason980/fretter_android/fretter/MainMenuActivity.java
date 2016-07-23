package errormjt.mason980.fretter_android.fretter;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class MainMenuActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    int STRING_START = 6;
    int string_number = STRING_START;
    int string_max = 18;
    int string_min = 1;

    int FRET_START = 3;
    int fret_number = FRET_START;
    int fret_max = 20;
    int fret_min = 1;

    int x_size = 0;
    int y_size = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        showErrorMessage();

        // populate spinners
        Integer[] fret_options = new Integer[fret_max - fret_min + 1];
        for (Integer i = fret_min; i <= fret_max; i++) {
            fret_options[i-fret_min] = i;
        }
        Spinner s_fret = (Spinner) findViewById(R.id.spnr_fret);
        ArrayAdapter<Integer> adapter_fret = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_spinner_item, fret_options);
        s_fret.setAdapter(adapter_fret);
        s_fret.setOnItemSelectedListener(this);
        s_fret.setSelection(FRET_START - fret_min);

        Integer[] string_options = new Integer[string_max - string_min + 1];
        for (Integer i = string_min; i <= string_max; i++) {
            string_options[i-string_min] = i;
        }
        Spinner s_string = (Spinner) findViewById(R.id.spnr_string);
        ArrayAdapter<Integer> adapter_string = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_spinner_item, string_options);
        s_string.setAdapter(adapter_string);
        s_string.setOnItemSelectedListener(this);
        s_string.setSelection(STRING_START - string_min);
    }

    private void showErrorMessage() {
        String message = getIntent().getStringExtra("error_message");
        if (message == ""  || message == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
        builder.setMessage(message).setTitle("ALERT");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void openGame(View v) {
        Intent intent = new Intent(this, Fretting_screen.class);
        intent.putExtra("string_number", string_number);
        intent.putExtra("fret_number", fret_number);
        intent.putExtra("x_size", x_size);
        intent.putExtra("y_size", y_size);

        intent.putExtra("game", true);

        startActivity(intent);
        finish();
    }

    public void openEdit(View v) {
        Intent intent = new Intent(this, Fretting_screen.class);
        intent.putExtra("string_number", string_number);
        intent.putExtra("fret_number", fret_number);
        intent.putExtra("x_size", x_size);
        intent.putExtra("y_size", y_size);

        intent.putExtra("game", false);

        startActivity(intent);
        finish();
    }

    public void resetStats(View v) {
        Spinner s_fret = (Spinner) findViewById(R.id.spnr_fret);
        s_fret.setSelection(FRET_START - fret_min);

        Spinner s_string = (Spinner) findViewById(R.id.spnr_string);
        s_string.setSelection(STRING_START - string_min);

    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        int num = (int) parent.getItemAtPosition(pos);
        if (parent.getId() == R.id.spnr_fret) {
            fret_number = num;
        } else if (parent.getId() == R.id.spnr_string) {
            string_number = num;
        }


    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


}