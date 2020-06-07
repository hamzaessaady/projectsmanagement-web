package isi.essaady.dashboard.comps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.annotation.ManagedProperty;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;

import isi.essaady.entities.Competence;


@Named
@ViewScoped
public class CompsChartBacking implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private BarChartModel barModel2;
	
	@SuppressWarnings("cdi-ambiguous-dependency")
	@Inject @ManagedProperty("#{compsBacking.comps}")
	private List<Competence> comps;
	
	@PostConstruct
    public void init() {
		createBarModel2();
	}
	
	
	public void createBarModel2() {
        barModel2 = new BarChartModel();
        ChartData data = new ChartData();
         
        BarChartDataSet barDataSet = new BarChartDataSet();
        barDataSet.setLabel("Collaborators");
        barDataSet.setBackgroundColor("rgba(255, 99, 132, 0.2)");
        barDataSet.setBorderColor("rgb(255, 99, 132)");
        barDataSet.setBorderWidth(1);
        List<Number> values = new ArrayList<>();
        for (Competence comp : comps) {
        	values.add(comp.getCollaborators().size());
		}
        barDataSet.setData(values);
         
        BarChartDataSet barDataSet2 = new BarChartDataSet();
        barDataSet2.setLabel("Tasks");
        barDataSet2.setBackgroundColor("rgba(255, 159, 64, 0.2)");
        barDataSet2.setBorderColor("rgb(255, 159, 64)");
        barDataSet2.setBorderWidth(1);
        List<Number> values2 = new ArrayList<>();
        for (Competence comp : comps) {
        	values2.add(comp.getTasks().size());
		}
        barDataSet2.setData(values2);
 
        data.addChartDataSet(barDataSet);
        data.addChartDataSet(barDataSet2);
         
        List<String> labels = new ArrayList<>();
        for (Competence comp : comps) {
        	labels.add(comp.getName());
		}
        data.setLabels(labels);
        barModel2.setData(data);
         
        //Options
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        ticks.setBeginAtZero(true);
        linearAxes.setTicks(ticks);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);
         
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Bar Chart");
        options.setTitle(title);
         
        barModel2.setOptions(options);
    }

	
	public BarChartModel getBarModel2() {
		return barModel2;
	}

	public void setBarModel2(BarChartModel barModel) {
		this.barModel2 = barModel;
	}
	

}
