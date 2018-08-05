package com.tnissinen.robotcontrol;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Draws servo simulation with given rotation value
 */
public class SimulationView extends View {

    /**
     * Simulation mode (ROTATION, ARM, CLAW)
     */
    public enum mode {
        ROTATION, ARM, CLAW
    }

    private final Paint paint; // viivojen piirtämiseen

    private mode simulationMode;
    private int scaledUnit;
    private int circleSize;
    private int pivotPoint;

    private int rotation1 = 0;
    private int rotation2 = 0;

    public SimulationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // asetetaan alkuasetukset viivalle
        paint = new Paint();
        paint.setAntiAlias(true); // sileät kulmat
        paint.setColor(Color.BLACK); // musta oletusväri
        paint.setStyle(Paint.Style.STROKE); // jatkuva viiva
        paint.setStrokeWidth(3); // oletus leveys
        paint.setStrokeCap(Paint.Cap.ROUND); // pyöristetyt päät

        simulationMode = mode.ROTATION;
    }

    public void setRotationsAndUpdate(int rotation1, int rotation2){
        this.rotation1 = rotation1;
        this.rotation2 = rotation2;
        updateView();
    }

    @Override
    public void onSizeChanged(int w, int h, int OldW, int oldH) {

        scaledUnit = getHeight() / 6;
        circleSize = scaledUnit * 4;
        pivotPoint = scaledUnit * 3;
    }

    // piirtää kuvan uudestaan kun näkymää virkistetään
    @Override
    protected void onDraw(Canvas canvas) {

        if(simulationMode == mode.ROTATION){

            canvas.drawOval(scaledUnit, scaledUnit, circleSize + scaledUnit, circleSize + scaledUnit, paint);

            canvas.save();
            canvas.rotate(rotation1, pivotPoint, pivotPoint);

            canvas.drawLine(pivotPoint, pivotPoint, pivotPoint, scaledUnit, paint);

            canvas.restore();

        }
        else if(simulationMode == mode.ARM)
        {

        }
        else if(simulationMode == mode.CLAW)
        {

        }

    }

    /**
     * Updates view
     */
    public void updateView(){
        invalidate();
    }


}
