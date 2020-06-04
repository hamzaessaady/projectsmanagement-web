package isi.essaady.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.annotation.ManagedProperty;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.charts.donut.DonutChartModel;

import isi.essaady.entities.Project;

@Named
@RequestScoped
public class DashboardChartsBacking implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private DonutChartModel donutModel;
	
	
	@SuppressWarnings("cdi-ambiguous-dependency")
	@Inject @ManagedProperty("#{dashboardBacking.projects}")
	private List<Project> projects;

	public DashboardChartsBacking() {
		super();
	}
	
    @PostConstruct
    public void init() {
    	System.out.println(projects==null);
        donutModel = new DonutChartModel();
        ChartData data = new ChartData();
        DonutChartDataSet dataSet = new DonutChartDataSet();
        
        // Define the number of unstarted, inProgress and finished projects
        Date now = new Date();
        int unstarted= 0;
        int inProgress = 0;
        int finished = 0;
        for (Project project : projects) {
			if(now.before(project.getStartDate())) {
				unstarted++;
			} else if(now.before(project.getEndDate())) {
				inProgress++;
			} else {
				finished++;
			}
		}
        
        // populating the Chart
        List<Number> values = new ArrayList<>();
        values.add(unstarted);
        values.add(inProgress);
        values.add(finished);
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
