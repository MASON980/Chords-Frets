package errormjt.mason980.fretter_android.fretter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mason on 18/07/2016.
 *
 *
 *      Displays guitar chords among other guitar view things
 *
 *
 */

public class GuitarView extends View {

    String debug_text = "Start";

    boolean game = false;   // should it show the finger placements

    int[][][] CHORDS = {            // 0, 0 is top left of screen       --  first coord is string, second is fret   -- ordering currently isn't important
     //       {{1, 1}, {3, 1}, {5, 1}},
            {{1, 1}, {0, 2}, {5, 2}},       // G
            {{3, 1}, {4, 2}, {5, 1}},       // D
            {{1, 1}, {2, 1}},               // Em
            {{4, 0}, {2, 1}, {1, 2}},       // C
            {{3, 1}, {2, 1}, {4, 0}},       // Am
            {{1, 1}, {2, 1}, {3, 0}},       // E
            {{2, 1}, {3, 1}, {4, 1}},       // A


    };

    Map<Paint, Path > draw_structure_static;        // the paint and paths that do not change
    HashMap<Paint, Path > draw_structure = new HashMap();

    //ArrayList<Paint> paint_structure;
    //ArrayList<Path>

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

    public GuitarView(Context context) {
        super(context);
        init();
    }

    private void init() {       // some initialisation

/*
        height = Resources.getSystem().getDisplayMetrics().heightPixels;
        width = Resources.getSystem().getDisplayMetrics().widthPixels;
        increment_x = width / 6;
        increment_y = height / 6;
*/
        int h = Resources.getSystem().getDisplayMetrics().heightPixels;
        int w = Resources.getSystem().getDisplayMetrics().widthPixels;
        onSizeChanged(w, h, w, h);


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

        prepare();
    }

    private void prepare() {        // more initialisation, can be called from onSizeChanged without stack overflow (I think, the two will call each other repeatedly)
        draw_structure.clear();
        drawGuitar();
        if (game) {
            chordPositioned();
        }

    }

    public void beginGame() {
        chordPositioned();
        game = true;
    }

    public void beginOther() {      //  when user is inputting chords, or anything else     --  name subject to change
        game = false;
    }


    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        canvas.drawARGB(255, 165,42,42);  // make this some brown thing
/*
        for (Map.Entry<Paint, Path> entry : draw_structure_static.entrySet())
        {
            //ArrayList<Path> paths = entry.getValue();
            //Paint paint = entry.getKey();
            canvas.drawPath(entry.getValue(), entry.getKey());

            /*
            for (int i = 0; i < paths.size(); i++) {
                canvas.drawPath(paths.get(i), paint);
            }

        }
*/
        for (Map.Entry<Paint, Path> entry : draw_structure.entrySet())
        {
            canvas.drawPath(entry.getValue(), entry.getKey());
        }

//        canvas.drawPath(guitar_path,  paint);

        for (int i = 0; i < fingerPlacements.size(); i++) {
            Paint paint;
            if (placed.contains(i)) {
                paint = highlight_paint;
            } else {
                paint = button_paint;
            }
            canvas.drawPath(fingerPlacements.get(i), paint);
        }


/*
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

        // Account for padding
        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());


        float ww = (float)w - xpad;
        float hh = (float)h - ypad;

        // Figure out how big we can make the pie.
        width = ww;
        height = hh;
        increment_x = ww / STRINGS;
        increment_y = hh / FRETS;

        prepare();
        // reset all the positionings
        //init();

    /*
            From one of the official (I think) tutorials somewhere

        //
        // Set dimensions for text, pie chart, etc
        //
        // Account for padding
        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        // Account for the label
        if (mShowText) xpad += mTextWidth;

        float ww = (float) w - xpad;
        float hh = (float) h - ypad;

        // Figure out how big we can make the pie.
        float diameter = Math.min(ww, hh);
        mPieBounds = new RectF(
                0.0f,
                0.0f,
                diameter,
                diameter);
        mPieBounds.offsetTo(getPaddingLeft(), getPaddingTop());

        mPointerY = mTextY - (mTextHeight / 2.0f);
        float pointerOffset = mPieBounds.centerY() - mPointerY;

        // Make adjustments based on text position
        if (mTextPos == TEXTPOS_LEFT) {
            mTextPaint.setTextAlign(Paint.Align.RIGHT);
            if (mShowText) mPieBounds.offset(mTextWidth, 0.0f);
            mTextX = mPieBounds.left;

            if (pointerOffset < 0) {
                pointerOffset = -pointerOffset;
                mCurrentItemAngle = 225;
            } else {
                mCurrentItemAngle = 135;
            }
            mPointerX = mPieBounds.centerX() - pointerOffset;
        } else {
            mTextPaint.setTextAlign(Paint.Align.LEFT);
            mTextX = mPieBounds.right;

            if (pointerOffset < 0) {
                pointerOffset = -pointerOffset;
                mCurrentItemAngle = 315;
            } else {
                mCurrentItemAngle = 45;
            }
            mPointerX = mPieBounds.centerX() + pointerOffset;
        }

        mShadowBounds = new RectF(
                mPieBounds.left + 10,
                mPieBounds.bottom + 10,
                mPieBounds.right - 10,
                mPieBounds.bottom + 20);

        // Lay out the child view that actually draws the pie.
        mPieView.layout((int) mPieBounds.left,
                (int) mPieBounds.top,
                (int) mPieBounds.right,
                (int) mPieBounds.bottom);
        mPieView.setPivot(mPieBounds.width() / 2, mPieBounds.height() / 2);

        mPointerView.layout(0, 0, w, h);
        onDataChanged();
        */
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        ArrayList<Integer> placements = new ArrayList();
        int placementCount = fingerPlacements.size();

        if (event.getAction() == MotionEvent.ACTION_UP) {

        } else {

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
        }

        placed = placements;
        if (placements.size() >= placementCount) {		// all have been pressed
            chordPositioned();
        }
        invalidate ();
        return true;
    }

    private void drawGuitar() {     // draw the guitar background thing,         maybe I could just do this in the xml file
        // could be used for both the frets and the strings


        // Strings
        Paint string_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
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


    private void drawChord(int[][] chord) {        // draw the given chord,        the chord data may be held better in a different data type
        for (int i = 0; i < chord.length; i++) {
            Path path = drawFingerPlacement(chord[i][0], chord[i][1]);
            fingerPlacements.add(path);
        }
    }

    private Path drawFingerPlacement (int x, int y) {        // draw the finger placement, including drawing the button and giving it an event handler
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



    private void chordPositioned () {       // the chord is in position, draw the next chord
        fingerPlacements.clear();
        placed.clear();

        long lo = Math.round( Math.random() * (CHORDS.length-1) );
        int index = (int) lo;       //  this    |COULD|     be a problem
        drawChord(CHORDS[index]);


    }
/*
    @Override
    public boolean onTouch(View v, MotionEvent event){
        float x = 0;
        float y = 0;
        Path[] paths;
        Path selected;
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:

                //screen touch get x of the touch event
                x = event.getX();
                //screen touch get y of the touch event
                y =event.getY();
                for (Path p : paths) {
                    RectF pBounds=new RectF();
                    p.computeBounds(pBounds,true);
                    if(pBounds.contains(x,y)){
                        selected Path= p;// where selectedPath is declared Globally.
                        break;}


                }

         //       dv.invalidate();


                break;

            case MotionEvent.ACTION_UP:
                //screen touch get x of the touch event
                x = event.getX();
                //screen touch get y of the touch event
                y =event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                //screen touch get x of the touch event
                x = event.getX();
                //screen touch get y of the touch event
                y =event.getY();
                break;
        }

        return true;

    }
*/

}
