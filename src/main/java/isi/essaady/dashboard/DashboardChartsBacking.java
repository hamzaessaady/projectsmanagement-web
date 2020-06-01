package isi.essaady.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.charts.donut.DonutChartModel;

@Named
@ViewScoped
public class DashboardChartsBacking implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private DonutChartModel donutModel;

	public DashboardChartsBacking() {
		super();
	}
	
    @PostConstruct
    public void init() {
        donutModel = new DonutChartModel();
        ChartData data = new ChartData();

        DonutChartDataSet dataSet = new DonutChartDataSet();
        List<Number> values = new ArrayList<>();
        values.add(8);
        values.add(56);
        values.add(34);
        dataSet.setData(values);

        List<String> bgColors = new ArrayList<>();
        bgColors.add("#fe875d");
        bgColors.add("rgb(54, 162, 235)");
        bgColors.add("rgb(255, 205, 86)");
        dataSet.setBackgroundColor(bgColors);

        data.addChartDataSet(dataSet);
        List<String> labels = new ArrayList<>();
        labels.add("Unstarted");
        labels.add("In progress");
        labels.add("Finished");
        data.setLabels(labels);

        donutModel.setData(data);
        
    }

    public DonutChartModel getDonutModel() {
        return donutModel;
    }


}
