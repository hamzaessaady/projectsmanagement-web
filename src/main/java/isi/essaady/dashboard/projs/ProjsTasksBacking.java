package isi.essaady.dashboard.projs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.primefaces.model.DualListModel;

import isi.essaady.ejbs.CompetenceBean;
import isi.essaady.ejbs.ProjectBean;
import isi.essaady.ejbs.TaskBean;
import isi.essaady.entities.Competence;
import isi.essaady.entities.Project;
import isi.essaady.entities.Task;
import isi.essaady.helpers.Constants;
import isi.essaady.helpers.Helpers;

@Named
@ViewScoped
public class ProjsTasksBacking implements Serializable {

	private static final long serialVersionUID = 1L;

	private Project proj;
	private List<Competence> comps;
	private Task selectedTask;
	private DualListModel<Competence> compsDualList;
	private List<Integer> disabledDays;
	
	@NotNull
	private String titleInput;
	private String descInput;
	@NotNull
	private Date startDateInput;
	@NotNull
	private Date endDateInput;
	@NotNull
	private int durationInput;

	@EJB
	private CompetenceBean compBean;
	@EJB
	private TaskBean taskBean;
	@EJB
	private ProjectBean projBean;

	@PostConstruct
	public void init() {
		proj = (Project) FacesContext.getCurrentInstance().getExternalContext()
									 .getFlash().get("proj");
		comps = compBean.getAllComps();
		compsDualList = new DualListModel<Competence>(comps,comps);
		startDateInput = proj.getStartDate();
		endDateInput = proj.getEndDate();
		durationInput = calcRemainingDuration();
		disabledDays = new ArrayList<>();
		disabledDays.add(0);
		disabledDays.add(6);
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
			
			//projBean.updateProj(this.proj);
			
			//proj.getTasks().forEach(t -> System.out.println("Task : "+t.getTitle()+" --> "+t.getCompetences().size()));
			
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

		this.compsDualList = new DualListModel<Competence>(sourceCompsList,targetCompsList);
	}
	
	 /**
     * Initializes the source and target lists of the pickList component.
     * Used by the setter of the selected task to do the initialization before
     * the competence modal shows.
     * 
     */
	public String addNewTask() {
		// Test dates
    	if(startDateInput.after(endDateInput)
    			|| startDateInput.before(proj.getStartDate())
    			|| endDateInput.after(proj.getEndDate())) {
    		Helpers.addMessage(FacesMessage.SEVERITY_ERROR,"Error",
					"Specified dates are invalide. End date must be greater than start date"
					+ " and between the project's start date and end date.");
			return null;
    	}
    	// Test Duration
    	int remainingHrs = calcRemainingDuration();
    	if(durationInput <= 0 && durationInput > remainingHrs) {
    		Helpers.addMessage(FacesMessage.SEVERITY_ERROR,"Error",
					"Duration must be greater than zero and less than "
    				+ remainingHrs + " .");
    		return null;
    	}
    	
    	int maxDuration = Helpers.calcMaxAllowedDuration(startDateInput, endDateInput);
    	if(durationInput > maxDuration) {
    		Helpers.addMessage(FacesMessage.SEVERITY_ERROR,"Error",
					"The given duration is not valid for the specified start and end dates.");
    		return null;
    	}
    	// Test Title
    	for (Task task : proj.getTasks() ) {
			if(task.getTitle().equalsIgnoreCase(titleInput.trim())){
				Helpers.addMessage(FacesMessage.SEVERITY_ERROR, "Error",
						"Task title already exists.");
				return null;
			}
		}
    	
    	// Create the new Proj object
    	Task newTask = new Task();
    	newTask.setProject(this.proj);
    	newTask.setTitle(titleInput.trim());
    	newTask.setStartDate(startDateInput);
    	newTask.setEndDate(endDateInput);
    	newTask.setDuration(durationInput);
    	newTask.setDescription(descInput);
    	// Save to DB
    	taskBean.createTask(newTask);
    	this.proj.addTask(newTask);
    	
    	// Keep message info displayed after redirection
    	FacesContext facesContext = FacesContext.getCurrentInstance();
    	Flash flash = facesContext.getExternalContext().getFlash();
    	flash.setKeepMessages(true);
    	FacesContext.getCurrentInstance().getExternalContext().getFlash()
					.put("proj", this.proj);
    	
    	Helpers.addMessage(FacesMessage.SEVERITY_INFO, "New Task added",
        		"The new task '"+ newTask.getTitle() + "' is added successfully.");

    	
    	return FacesContext.getCurrentInstance().getViewRoot().getViewId()
    			+  "?faces-redirect=true";	
    	
	}
	
	
	 /**
     * Initializes the source and target lists of the pickList component.

     */
	public int calcRemainingDuration() {
		
		AtomicInteger tasksDurationSum = new AtomicInteger(0);
		this.proj.getTasks().forEach(t -> tasksDurationSum.addAndGet(t.getDuration()));
		
		return this.proj.getDuration() - tasksDurationSum.get();
	}
	
	/**
     * Removes a Task.
     * 
     * @param  task  The selected task to remove.
     */
    public void removeTask(Task task) {
    	taskBean.deleteTask(task);
    	this.proj.removeTask(task);
    	FacesContext.getCurrentInstance().getExternalContext().getFlash()
					.put("proj", this.proj);
    	Helpers.addMessage(FacesMessage.SEVERITY_INFO, "Task Deleted",
    			"Task'"+ task.getTitle()+"' is successfully deleted.");
    }
    
    
    public String getTaskStatus(Task task) {
    	Date now = new Date();
    	
    	if(now.before(task.getStartDate())) {
			return Constants.UNSTARTED;
		} else if(now.before(task.getEndDate())) {
			return Constants.IN_PROGRESS;
		} else {
			return Constants.FINISHED;
		}
    }
    
    
    /**
     * Determines the number of tasks in the corresponding 'finished', 'in progress' and
     * 'unstarted' states.
     * 
     * @return  Map<String, Integer>  The mapped states values
     */
    public Map<String, Integer> countTasksByStatus(){
    	Map<String, Integer> projsStatus = new HashMap<String, Integer>();
    	AtomicInteger finished = new AtomicInteger(0);
    	AtomicInteger inProgress = new AtomicInteger(0);
    	AtomicInteger unstarted = new AtomicInteger(0);
    	Date now = new Date();
    	
    	this.proj.getTasks().forEach(p -> {
    		if(now.before(p.getStartDate())) {
    			unstarted.incrementAndGet();
    		} else if(now.before(p.getEndDate())){
    			inProgress.incrementAndGet();
    		} else {
    			finished.incrementAndGet();
    		}
    	});
    	projsStatus.put(Constants.FINISHED, finished.get());
    	projsStatus.put(Constants.IN_PROGRESS, inProgress.get());
    	projsStatus.put(Constants.UNSTARTED, unstarted.get());
    	
    	return projsStatus;
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


	public String getTitleInput() {
		return titleInput;
	}


	public String getDescInput() {
		return descInput;
	}


	public Date getStartDateInput() {
		return startDateInput;
	}


	public Date getEndDateInput() {
		return endDateInput;
	}


	public int getDurationInput() {
		return durationInput;
	}


	public void setTitleInput(String titleInput) {
		this.titleInput = titleInput;
	}


	public void setDescInput(String descInput) {
		this.descInput = descInput;
	}


	public void setStartDateInput(Date startDateInput) {
		this.startDateInput = startDateInput;
	}


	public void setEndDateInput(Date endDateInput) {
		this.endDateInput = endDateInput;
	}


	public void setDurationInput(int durationInput) {
		this.durationInput = durationInput;
	}


	public List<Integer> getDisabledDays() {
		return disabledDays;
	}


	public void setDisabledDays(List<Integer> disabledDays) {
		this.disabledDays = disabledDays;
	}
	
	

}
