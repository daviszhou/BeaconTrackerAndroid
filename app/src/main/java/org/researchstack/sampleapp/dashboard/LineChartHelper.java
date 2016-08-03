package org.researchstack.sampleapp.dashboard;
/*
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import org.researchstack.sampleapp.R;
*/
/**
 * Created by davis on 7/19/16.
 */
public class LineChartHelper {
/*
    private final LineChartView mChart;
    private final Context mContext;
    private final String[] mLabels;
    private final float[] mValues;
    private final int mStartPoint;
    private final int mEndPoint;
    private Tooltip mTip;
    private Runnable mBaseAction;

    public LineChartHelper(LineChartView view, Context context, String[] labels, float[] values, int startpoint, int endpoint) {
        mContext = context;
        mChart = view;
        mLabels = labels;
        mValues = values;
        mStartPoint = startpoint;
        mEndPoint = endpoint;
        this.show();
    }

    public void show() {

        // Tooltip
        mTip = new Tooltip(mContext, R.layout.linechart_three_tooltip, R.id.value);

        ((TextView) mTip.findViewById(R.id.value))
                .setTypeface(Typeface.createFromAsset(mContext.getAssets(), "OpenSans-Semibold.ttf"));

        mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mTip.setDimensions((int) Tools.fromDpToPx(65), (int) Tools.fromDpToPx(25));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

            mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

            mTip.setPivotX(Tools.fromDpToPx(65) / 2);
            mTip.setPivotY(Tools.fromDpToPx(25));
        }

        // Data

        LineSet dataset = new LineSet(mLabels, mValues);
        dataset.setColor(Color.parseColor("#758cbb"))
                .setFill(Color.parseColor("#2d374c"))
                .setDotsColor(Color.parseColor("#343f57"))
                .setDotsStrokeThickness(4)
                .setDotsStrokeColor(Color.parseColor("#758cbb"))
                .setThickness(4)
                .beginAt(mStartPoint)
                .endAt(mEndPoint);
        mChart.addData(dataset);

        //Chart

        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#2d374c"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(1));

        mChart.setBorderSpacing(Tools.fromDpToPx(15))
                .setLabelsColor(Color.parseColor("#6a84c3"))
                .setXAxis(false)
                .setYAxis(false)
                .setGrid(ChartView.GridType.HORIZONTAL, gridPaint);

        Animation anim = new Animation()
                .setEasing(new BounceEase());

        mChart.show(anim);
    }

    public void showOnClick(Runnable action) {
        mBaseAction = action;
        Runnable chartAction = new Runnable() {
            @Override
            public void run() {
                mBaseAction.run();
                mTip.prepare(mChart.getEntriesArea(0).get(3), mValues[3]);
                mChart.showTooltip(mTip, true);
            }
        };

        Animation anim = new Animation()
                .setEasing(new BounceEase());
    }

    public void dismiss(Runnable action) {
        mChart.dismissAllTooltips();
        mChart.dismiss(new Animation()
                .setEasing(new BounceEase())
                .setEndAction(action));
    }
    */
}