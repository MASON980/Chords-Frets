package errormjt.mason980.fretter_android.fretter;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Mason on 18/07/2016.
 *
 *
 *      Displays guitar neck and chords, allows for interaction
 *
 *
 */

public class GuitarView extends View {

    String debug_text = "Start";
    String CHORD_STORAGE_FILENAME = "Chord_Frets.json";
    Fretting_screen host;

    boolean game_mode = false;   // should it show the finger placements
    boolean edit_mode = false;   // should it show the finger placements
    boolean sizing_mode = false;

    int[][][] CHORDS_RAW = {            // 0, 0 is top left of screen       --  first coord is string, second is fret   -- ordering currently isn't important
            {{1, 1}, {0, 2}, {5, 2}},       // G
            {{3, 1}, {4, 2}, {5, 1}},       // D
            {{1, 1}, {2, 1}},               // Em
            {{4, 0}, {2, 1}, {1, 2}},       // C
            {{3, 1}, {2, 1}, {4, 0}},       // Am
            {{1, 1}, {2, 1}, {3, 0}},       // E
            {{2, 1}, {3, 1}, {4, 1}},       // A

    };

    JSONArray chords_global = new JSONArray();
    int current_chord = 0;

    HashMap<Paint, Path > draw_structure = new HashMap();

    Paint button_paint;
    Paint highlight_paint;
    Paint text_paint;

    int STRINGS = 6;
    int FRETS = 3;

    ArrayList<Path> fingerPlacements = new ArrayList();
    ArrayList<Integer> placed = new ArrayList();

    float width = 100;
    float height = 100;
    float increment_x = 100;
    float increment_y = 100;

    public GuitarView(Context context) throws Exception {
        super(context);
    }

    public void setup (int s, int f, int x_s, int y_s, Fretting_screen h, boolean game, boolean edit) throws Exception {
        setStats(s, f, x_s, y_s);
        setHost(h);
        init(game, edit);
    }

    public void setStats(int s, int f, int x_s, int y_s) throws Exception {
        if (s > 0) {
            STRINGS = s;
        }
        if (f > 0) {
            FRETS = f;
        }

        if (x_s > 0 && y_s > 0) {
            int w = x_s;
            int h = y_s;
        //    onSizeChanged(w, h, w, h);      // probably be better changing the size of the encapsulating view
        }

    }

    public void setHost (Fretting_screen f) {
        host = f;
    }

    public void init(boolean game, boolean edit) throws Exception  {       // some initialisation

        game_mode = game;
        edit_mode = edit;

        button_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        button_paint.setStyle(Paint.Style.FILL);
        button_paint.setARGB(255, 255, 205, 200);

        highlight_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlight_paint.setStyle(Paint.Style.FILL);
        highlight_paint.setARGB(255, 155, 105, 100);

        text_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        text_paint.setStyle(Paint.Style.FILL);
        text_paint.setARGB(255, 0, 0, 0);
        text_paint.setTextSize(50);

        loadChords();

        int h = Resources.getSystem().getDisplayMetrics().heightPixels;
        int w = Resources.getSystem().getDisplayMetrics().widthPixels;
        onSizeChanged(w, h, w, h);
        //  prepare();            commented out recenlty
    }

    private void prepare() throws Exception  {        // more initialisation, can be called from onSizeChanged without stack overflow (I think, the two will call each other repeatedly)
        draw_structure.clear();
        drawGuitar();
        if (game_mode) {
            chordPositioned();
        } else if (edit_mode) {
            beginOther();
        }

    }

    public void beginGame() throws Exception  {
        game_mode = true;
        edit_mode = false;
        chordPositioned();
    }

    public void beginOther() throws Exception {      //  when user is inputting chords, or anything else     --  name subject to change
        if (isExternalStorageWritable()) {

        } else {
            Exception e = new Exception("External storage cannot be accessed, chords cannot be saved");
            throw e;
            // could let the user open this anyway and get errors when trying to save, would give them more freedom
        }
        game_mode = false;
        edit_mode = true;
        fingerPlacements.clear();
        placed.clear();

        ArrayList< int[] > full_chord = new ArrayList();

        for (int i = 0; i < STRINGS; i++) {
            for (int j = 0; j < FRETS; j++) {
                int[] arr = {i, j};
                full_chord.add(arr);
            }
        }
        int[][] full = new int[STRINGS*FRETS][2];
        full_chord.toArray(full);
        drawChord(full);
    }

    public void saveChord() {       // save the highlighted chord - edit mode
        File file = accessFileSystem("");

        if (file == null) {
            return;
        }

        if (placed.size() <= 0) {
            showErrorMessage("Chord is empty. Please enter some placements");
            return;
        }
        showErrorMessage("Chord saved.");

        JSONArray new_chord = new JSONArray();
        try {
            for (int i = 0; i < placed.size(); i++) {
                // placed is a number not a position
                JSONArray json_pos = new JSONArray();
                int pos = placed.get(i);
                int x = pos / FRETS;
                int y = pos % FRETS;
                json_pos = json_pos.put(x);
                json_pos = json_pos.put(y);
                new_chord = new_chord.put(json_pos);
            }
        } catch (Exception e) {
            showErrorMessage(e.getMessage());
            return;
        }
   //     checkUniqueness(new_chord, file);
        // convert chord to format
        // write to file
        try {
            loadChords();
        } catch (Exception e) {
            showErrorMessage(e.getMessage() + " : save chord");
        }
        String new_string = chordsToString(chords_global.put(new_chord));           //  wrong, chords_global shouldn't be anything because this is in edit mode     --  chords are loaded regardless it seems
        accessFileSystem(new_string);

    }

    private boolean checkUniqueness (JSONArray chord, File file) {        // checks if the chord is unique / is not already in the file     --  doesnt consider ordering
        try {
            for (int i = 0; i < chords_global.length(); i++) {
                if (chords_global.get(i).equals(chord)) {
                    return false;
                }
            }
        } catch (Exception e) {
            showErrorMessage(e.getMessage());
        }
        return true;
    }

    public void deleteAllChords() throws Exception {
        chords_global = new JSONArray();
        // then delete from filesystem
        accessFileSystem(chordsToString(chords_global));

        if (chords_global.length() == 0) {
            throw new Exception("All chords haven been deleted, add more with 'Add Chords' or reload the default chords with 'Begin'");
        }
    }
    public void deleteChord() throws Exception {     // delete the current chord - game mode
        File file = accessFileSystem("");

        if (file == null) {
            return;
        }
        try {
            //chords_global.remove(current_chord);
            JSONArray new_chords = new JSONArray();
            for (int i = 0; i < chords_global.length(); i++) {
                if (i != current_chord) {
                    new_chords.put(chords_global.get(i));
                }
            }
            chords_global = new_chords;
        } catch (Exception e) {
            showErrorMessage(e.getMessage() + " del");
        }

        // then delete from filesystem
        accessFileSystem(chordsToString(chords_global));

        if (chords_global.length() == 0) {
            throw new Exception("All chords haven been deleted, add more with 'Add Chords' or reload the default chords with 'Begin'");
        }

        try {
            chordPositioned();
        } catch (Exception e) {
            throw new Exception("All chords haven been deleted or no more fit, add more with 'Add Chords' or reload the default chords with 'Begin'");
//            showErrorMessage(e.getMessage() + " del_chord");
        }
        invalidate();

    }

    public void loadChords() throws Exception {      // load the chords from the file    --  just from the default file (for now)

        if (game_mode) {
            rawToChords();
        }
            // find file
        File file = accessFileSystem("");
        if (file == null) {
            return;
        }
        JSONArray variable = fileToChords(file);
        if (variable == null) {
            if (game_mode) {
                variable = fillFile();
            }
            if (edit_mode) {
                variable = new JSONArray();
            }
        }

        chords_global = variable;          //  should I keep the default chords in, or maybe make the file include the defaults

    }

    private void rawToChords() {
        chords_global = new JSONArray();

        try {

            for (int i = 0; i < CHORDS_RAW.length; i++) {
                JSONArray chord = new JSONArray();

                for (int j = 0; j < CHORDS_RAW[i].length; j++) {
                    JSONArray pos = new JSONArray();
                    pos = pos.put(CHORDS_RAW[i][j][0]);
                    pos = pos.put(CHORDS_RAW[i][j][1]);
                    chord = chord.put(pos);
                }
                chords_global = chords_global.put(chord);
            }
        } catch (Exception e) {
            showErrorMessage(e.getMessage() + " load");
            return;
        }

    }

    private File accessFileSystem(String new_data) {
        if (isExternalStorageWritable()) {

        } else {
            //  show some alert dialog
            showErrorMessage("External storage cannot be accessed or is not writable.");
            return null;
            // return
        }
        File file = new File(getContext().getExternalFilesDir(null), CHORD_STORAGE_FILENAME);

        try {
            boolean ex = file.createNewFile();

        } catch (IOException e) {
            showErrorMessage("Error creating new file.");
            return null;
        }

        if (new_data == "[]" || new_data == "empty") {
            try {
                PrintWriter writer = new PrintWriter(file);
                writer.write("");
                writer.close();
            } catch (Exception e) {
                showErrorMessage(e.getMessage() + " :write empty");
                return null;
            }
        } else if (new_data != "" && new_data != null) {
            try {
                PrintWriter writer = new PrintWriter(file);
                writer.write(new_data);
                writer.close();
            } catch (Exception e) {
                showErrorMessage(e.getMessage() + " :write");
                return null;
            }
        }


        return file;
    }

    private JSONArray fileToChords(File file) throws Exception {                 // convert and format a files data into a variable which can be used by the program     --  inverse of chordsToString

        if (file.length() <= 3) {       //  '[0,0]' is the smallest possible viable thing, so anything shorter than that should be reset
            return null;
        }
        JSONArray variable = new JSONArray();
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();

            String text_string = text.toString();
            variable = new JSONArray(text_string);

        }
        catch (Exception e) {       // IOException      JSONException
            //You'll need to add proper error handling here
            showErrorMessage(e.getMessage() + "f-Chor");
            return null;
        }
        return variable;
    }

    public JSONArray fillFile() {
        rawToChords();
        accessFileSystem(chordsToString(chords_global));
        showErrorMessage("File has been filled");
        invalidate();
        return chords_global;

    }

    private String chordsToString(JSONArray chords) {             // convert and format the values into a string that can be written to disk              --      inverse of fileToChords
        if (chords.length() == 0) {
            return "empty";
        }
        return chords.toString();
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        canvas.drawARGB(255, 165,42,42);  // make this some brown thing

        for (Map.Entry<Paint, Path> entry : draw_structure.entrySet())
        {
            canvas.drawPath(entry.getValue(), entry.getKey());
        }

        for (int i = 0; i < fingerPlacements.size(); i++) {
            Paint paint;
            if (placed.contains(i)) {
                paint = highlight_paint;
            } else {
                paint = button_paint;
            }
            canvas.drawPath(fingerPlacements.get(i), paint);
        }


/*          my debug isn't working so I was using this

        String text = debug_text;
        for (int i = 0; i < positions.size(); i++) {
            for (int j = 0; j < positions.get(i).size(); j++) {
                text = "else";
                //text += "point:" + positions.get(i).get(j).toString() + "," + positions.get(i).get(j).toString() + " ";
            }
        }
        canvas.drawText (text, 0, text.length(), 100, 100, text_paint);
*/
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());

        float ww = (float)w - xpad;
        float hh = (float)h - ypad;

        width = ww;
        height = hh;
        increment_x = ww / STRINGS;
        increment_y = hh / FRETS;

        try {
            prepare();
        } catch (Exception e) {
            // todo
        }
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        if (game_mode) {
            return onGameTouch(event);
        } else if (edit_mode) {
            return onEditTouch(event);
        }
        return false;
    }

    private boolean onGameTouch (MotionEvent event) {        // event handling in game mode

        ArrayList<Integer> placements = new ArrayList();
        int placementCount = fingerPlacements.size();

        if (event.getAction() == MotionEvent.ACTION_UP) {

        } else {

            placements = findPlacements(event);
        }

        placed = placements;
        if (placements.size() >= placementCount) {		// all have been pressed
            try {
                chordPositioned();
            } catch (Exception e) {
                // todo
            }
        }
        invalidate ();
        return true;
    }

    private boolean onEditTouch (MotionEvent event) {    // event handling when in edit mode

        if (event.getAction() == MotionEvent.ACTION_UP) {

        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ArrayList<Integer> placements = new ArrayList();
            placements = findPlacements(event);

            for (int i = 0; i < placements.size(); i++) {
                int index = placed.indexOf(placements.get(i));

                // toggle them
                if (index != -1) {
                    placed.remove(index);
                } else {
                    placed.add(placements.get(i));
                }
            }
            invalidate ();
        }
        return true;
    }

    private ArrayList<Integer> findPlacements (MotionEvent event) {     // what placements are currently in place
        ArrayList<Integer> placements = new ArrayList();
        int placementCount = fingerPlacements.size();
        Integer count = event.getPointerCount();

        for (int i = 0; i < count; i++) {
            float x = event.getX(i);
            float y = event.getY(i);

            for (int j = 0; j < placementCount; j++) {
                Path p = fingerPlacements.get(j);
                RectF pBounds = new RectF();
                p.computeBounds(pBounds, true);

                if (pBounds.contains(x, y)) {    // it is within a bound
                    boolean unique_switch = true;
                    for (int k = 0; k < placements.size(); k++) {
                        if (placements.get(k) == j) {        // it is not within a unique bound
                            //unique_switch = false;
                        }
                    }
                    if (unique_switch) {
                        placements.add(j);
                    }
                } else {

                }
            }

        }
        return placements;
    }

    private void drawGuitar() {     // draw the guitar background thing,         maybe I could just do this in the xml file

        Paint string_paint = new Paint(Paint.ANTI_ALIAS_FLAG);          // Strings
        string_paint.setStyle(Paint.Style.FILL);
        string_paint.setARGB(155, 225, 225, 205);

        Paint string_shadow_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        string_shadow_paint.setStyle(Paint.Style.FILL);
        string_shadow_paint.setARGB(155, 155, 155, 165);

        Path string_path = new Path();
        Path string_shadow_path = new Path();

        for (int i = 0; i < STRINGS; i++) {

            float start_x = increment_x * (i + 0.5f);
            float start_y = 0;

            float end_x = start_x + 6;
            float end_y = start_y + height;
            float shadow_x = end_x + 4;

            string_path.moveTo(start_x, start_y);
            string_path.lineTo(start_x, end_y);
            string_path.lineTo(end_x, end_y);
            string_path.lineTo(end_x, start_y);


            string_shadow_path.moveTo(end_x, start_y);
            string_shadow_path.lineTo(end_x, end_y);
            string_shadow_path.lineTo(shadow_x, end_y);
            string_shadow_path.lineTo(shadow_x, start_y);
        }

        draw_structure.put(string_paint, string_path);
        draw_structure.put(string_shadow_paint, string_shadow_path);


        //  Frets
        Paint fret_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fret_paint.setStyle(Paint.Style.FILL);
        fret_paint.setARGB(255, 225, 225, 225);

        Paint fret_shadow_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fret_shadow_paint.setStyle(Paint.Style.FILL);
        fret_shadow_paint.setARGB(255, 185, 185, 185);

        Paint fret_highlight_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fret_highlight_paint.setStyle(Paint.Style.FILL);
        fret_highlight_paint.setARGB(255, 245, 245, 245);

        Path fret_path = new Path();
        Path fret_shadow_path = new Path();
        Path fret_highlight_path = new Path();

        for (int i = 0; i < FRETS; i++) {

            float start_x = 0;
            float start_y = increment_y * i;

            float end_x = start_x + width;
            float end_y = start_y + 4;
            float shadow_y = end_y + 3;
            float highlight_y = start_y - 3;

            string_path.moveTo(start_x, start_y);
            string_path.lineTo(start_x, end_y);
            string_path.lineTo(end_x, end_y);
            string_path.lineTo(end_x, start_y);

            fret_shadow_path.moveTo(start_x, start_y);
            fret_shadow_path.lineTo(start_x, shadow_y);
            fret_shadow_path.lineTo(end_x, shadow_y);
            fret_shadow_path.lineTo(end_x, start_y);

            fret_highlight_path.moveTo(start_x, start_y);
            fret_highlight_path.lineTo(start_x, highlight_y);
            fret_highlight_path.lineTo(end_x, highlight_y);
            fret_highlight_path.lineTo(end_x, start_y);
        }

        draw_structure.put(fret_shadow_paint, fret_path);
        draw_structure.put(string_shadow_paint, fret_shadow_path);
        draw_structure.put(fret_highlight_paint, fret_highlight_path);

    }


    private void drawChord(int[][] chord) {        // draw the given chord
        for (int i = 0; i < chord.length; i++) {
            Path path = drawFingerPlacement(chord[i][0], chord[i][1]);
            fingerPlacements.add(path);
        }
        invalidate();
    }

    private Path drawFingerPlacement (int x, int y) {        // draw the finger placement
        //	x and y being the string and fret

        float start_x = increment_x * (x+0.5f /* thats what the guitar has */ ) - (increment_x*0.4f);       // on the string
        float start_y = increment_y * y + (increment_y*0.2f);         // between the frets

        float end_x = start_x + (increment_x*0.8f);
        float end_y = start_y + (increment_y*0.6f);

        Path path = new Path();
/*
        RectF rectangle = new RectF(start_x, start_y, end_x - start_x, end_y - start_y);
        path.addRoundRect(rectangle, 5f, 10f, Path.Direction.CW);
*/
        path.moveTo(start_x, start_y);
        path.lineTo(start_x, end_y);
        path.lineTo(end_x, end_y);
        path.lineTo(end_x, start_y);

        return path;
    }

    private void chordPositioned () throws Exception  {       // the chord is in position, so choose and draw the next chord

        fingerPlacements.clear();
        placed.clear();
        int previous_chord = current_chord;

        long lo = Math.round(Math.random() * (chords_global.length() - 1));
        int index = (int) lo;       //  this    |COULD|     be a problem
        boolean outside = true;
        outside = checkChordBoundary(index);
        int starting_index = index;

        while (outside) {
            index++;
            if (index >= chords_global.length()) {
                index = 0;
            }
            if (index == starting_index) {
                // throw some error
                    throw new Exception("No chords fit onto this guitar. Please choose larger dimensions or add some smaller chords.");
            }
            outside = checkChordBoundary(index);
        }
        current_chord = index;
        drawChord(getChord(index));

    }

    private boolean checkChordBoundary (int index) {            // check if the chord is outside of the current number of strings and frets
        if (index >= chords_global.length()) {
            return true;
        }
        int[][] chord = getChord(index);

        for (int i = 0; i < chord.length; i++) {
            if (chord[i][0] >= STRINGS || chord[i][1] >= FRETS) {
                return true;
            }
        }
        return false;
    }

    //      https://developer.android.com/guide/topics/data/data-storage.html
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void showErrorMessage(String mess) {            // this is bad
        if (mess == ""  || mess == null) {
            mess = "no message";
        }

        host.showErrorMessage(mess);
    }

    private int[][] getChord (int index) {
        if (index >= chords_global.length()) {
            return null;
        }
        JSONArray temp;
        ArrayList< int[] > chord = new ArrayList();
        try {
            temp = chords_global.getJSONArray(index);

            for (int i = 0; i < temp.length(); i++) {
                int[] pos = {temp.getJSONArray(i).getInt(0), temp.getJSONArray(i).getInt(1)};

                chord.add(pos);
            }
        } catch (Exception e) {
            showErrorMessage(e.getMessage() + " get");
            return null;
        }
        int[][] chord_int = new int[chord.size()][2];
        chord.toArray(chord_int);
        return chord_int;

    }

    //  https://stackoverflow.com/questions/8276634/android-get-hosting-activity-from-a-view
    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

}
