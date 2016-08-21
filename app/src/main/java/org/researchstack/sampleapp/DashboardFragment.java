package org.researchstack.sampleapp;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import org.researchstack.backbone.ui.graph.BarChartCard;
import org.researchstack.backbone.ui.graph.LineChartCard;
import org.researchstack.backbone.ui.graph.PieChartCard;
import org.researchstack.backbone.ui.graph.ProgressChartCard;
import org.researchstack.backbone.utils.ThemeUtils;
import org.researchstack.sampleapp.datamanager.DashboardHelper;
import org.researchstack.sampleapp.datamanager.TwoValueDataHolder;
import org.researchstack.skin.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class DashboardFragment extends Fragment
{
    private View emptyView;
    private DashboardHelper mDashboardHelper;
    private HashMap<String, ArrayList> mMapHolder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(org.researchstack.sampleapp.R.layout.fragment_dashboard, container, false); //inflate view inside fragment
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        emptyView = view.findViewById(R.id.dashboard_empty);

        initProgressChart(view);
    }

    private void initProgressChart(View view)
    {
        BarChartCard barCard = (BarChartCard) view.findViewById(R.id.dashboard_chart_bar);
        barCard.setTitle("Pie Flavors");
        barCard.setData(createBarChartData(), false);
        barCard.setExpandAction(o -> {
            Snackbar.make(view, "Expand Action", Snackbar.LENGTH_SHORT).show();
        });

        LineChartCard lineCard = (LineChartCard) view.findViewById(R.id.dashboard_chart_line);
        lineCard.setTitle("Daily steps");
        lineCard.setData(createLineChartData());
        lineCard.setExpandAction(o -> {
            Snackbar.make(view, "Expand Action", Snackbar.LENGTH_SHORT).show();
        });
    }

    public BarData createBarChartData()
    {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);

        mDashboardHelper = new DashboardHelper(getActivity());
        HashMap<String, ArrayList> frequencyHolder = mDashboardHelper.generateTimesPerDayMap();

        ArrayList<String> xVals = frequencyHolder.get("dayOfThisMonth");
        ArrayList<BarEntry> yVals1 = frequencyHolder.get("episodesPerDay");

        BarDataSet set1 = new BarDataSet(yVals1, "DataSet");
        set1.setColor(0xFF2196f3);
        set1.setBarSpacePercent(40f);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10);
        data.setValueTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> numberFormat.format(
                value));
        return data;
    }

    public LineData createLineChartData()
    {
        /*
        ArrayList<String> xValues = new ArrayList<>();
        for(int i = 0; i < 12; i++)
        {
            xValues.add(i + "");
        }

        ArrayList<Entry> entries = new ArrayList<>();

        for(int i = 0; i < 12; i++)
        {
            float mult = (5 + 1);
            int val = (int) (Math.random() * mult) + 1;
            entries.add(new Entry(val, i));
        }
        */

        mDashboardHelper = new DashboardHelper(getActivity());
        HashMap<String, ArrayList> frequencyHolder = mDashboardHelper.generateBeaconTotalDurationPerDayMap();

        ArrayList<String> xValues = frequencyHolder.get("dayOfThisMonth");
        ArrayList<Entry> entries = frequencyHolder.get("totalDurationPerDay");

        LineDataSet set = new LineDataSet(entries, "");
        set.setCircleColor(0xFF2196f3);
        set.setCircleRadius(4f);
        set.setDrawCircleHole(false);
        set.setColor(0xFF2196f3);
        set.setLineWidth(2f);
        set.setDrawValues(false);

        return new LineData(xValues, set);
    }

}