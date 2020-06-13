package isi.essaady.dashboard.projs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.model.DualListModel;

import isi.essaady.ejbs.CompetenceBean;
import isi.essaady.ejbs.TaskBean;
import isi.essaady.entities.Competence;
import isi.essaady.entities.Project;
import isi.essaady.entities.Task;
import isi.essaady.helpers.Helpers;

@Named
@ViewScoped
public class ProjsTasksBacking implements Serializable {

	private static final long serialVersionUID = 1L;

	private Project proj;
	private List<Competence> comps;
	private Task selectedTask;
	private DualListModel<Competence> compsDualList;

	@EJB
	private CompetenceBean compBean;
	@EJB
	private TaskBean taskBean;

	@PostConstruct
	public void init() {
		proj = (Project) FacesContext.getCurrentInstance().getExternalContext()
									 .getFlash().get("proj");
		comps = compBean.getAllComps();
		compsDualList = new DualListModel<Competence>(comps,comps);
	}
	
	
	/**
     * Updates the competences of the selected task.
     * 
     */
	public void updateTaskComps() {
		
		if(isCompsChanged(selectedTask.getCompetences())) {
			Set<Competence> updatedComps = new HashSet<Competence>(this.compsDualList.getTarget());
			this.selectedTask.setCompetences(updatedComps);
			taskBean.updateTask(this.selectedTask);
			
			Helpers.addMessage(FacesMessage.SEVERITY_INFO,"Competences updated",
					"The task is succefully saved with the new updated competences");
		}
	}
	
	
	/**
     * Compares the new selected competences with the previous ones.
     * 
     * @param  oldComps  The old selected competences
     * @return  boolean  Test result. True if the competences are changed. 
     */
    public boolean isCompsChanged(Set<Competence> oldComps) {
    	
    	List<Competence> targetComps = this.compsDualList.getTarget();
    	
    	if(targetComps.size() == oldComps.size()) {
	    	List<String> oldCompsAsStrs = new ArrayList<String>();
	    	List<String> targetCompsAsStrs = new ArrayList<String>();
	    	
	    	oldComps.forEach(c -> oldCompsAsStrs.add(c.getName()));
	    	targetComps.forEach(ct -> targetCompsAsStrs.add(ct.getName()));
	    	
	    	return !oldCompsAsStrs.containsAll(targetCompsAsStrs);
    	}
    	
    	return true;
    }
    
    
    /**
     * Initializes the source and target lists of the pickList component.
     * Used by the setter of the selected task to do the initialization before
     * the competence modal shows.
     * 
     */
	public void initCompsPickList() {
		
		List<Competence> sourceCompsList = new ArrayList<Competence>(comps);
		List<Competence> targetCompsList = new ArrayList<Competence>(selectedTask.getCompetences());
		sourceCompsList.removeAll(targetCompsList);
		
		System.out.println(comps.size());
		System.out.println(sourceCompsList.size());
		
		this.compsDualList = new DualListModel<Competence>(sourceCompsList,targetCompsList);
	}
	
	

	/* GETTERS AND SETTERS */
	public Project getProj() {
		return proj;
	}

	public void setProj(Project proj) {
		this.proj = proj;
	}

	public Task getSelectedTask() {
		System.out.println("Get called ---");
		return selectedTask;
	}

	public void setSelectedTask(Task selectedTask) {

		this.selectedTask = selectedTask;
		initCompsPickList();
		

	}

	public DualListModel<Competence> getCompsDualList() {
		return compsDualList;
	}

	public void setCompsDualList(DualListModel<Competence> compsDualList) {
		this.compsDualList = compsDualList;
	}

}
