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

import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DualListModel;

import isi.essaady.ejbs.CollabTaskPlanBean;
import isi.essaady.ejbs.CollaboratorBean;
import isi.essaady.ejbs.CompetenceBean;
import isi.essaady.ejbs.ProjectBean;
import isi.essaady.ejbs.TaskBean;
import isi.essaady.entities.CollabTaskPlan;
import isi.essaady.entities.Collaborator;
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
	private List<CollabTaskPlan> plans;
	private Task selectedTask;
	private DualListModel<Competence> compsDualList;
	private List<Integer> disabledDays;
	private int assignedHrsInput;
	Map<Integer, Integer> oldSpinnerColumns;
	
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
	@EJB
	private CollaboratorBean collabBean;
	@EJB
	private CollabTaskPlanBean planBean;

	@PostConstruct
	public void init() {
		proj = (Project) FacesContext.getCurrentInstance().getExternalContext()
									 .getFlash().get("proj");
		comps = compBean.getAllComps();
		plans = planBean.getAllCollabTaskPlan();
		compsDualList = new DualListModel<Competence>(comps,comps);
		startDateInput = proj.getStartDate();
		endDateInput = proj.getEndDate();
		durationInput = calcRemainingDuration();
		disabledDays = new ArrayList<>();
		disabledDays.add(0);
		disabledDays.add(6);
		oldSpinnerColumns = new HashMap<Integer,Integer>();
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
					"The task is succefully saved with the new updated competences.");
		}
	}
	
	
	/**
     * Compares the new selected competences with the previous ones.
     * 
     * @param  oldComps  The old selected competences
     * @return  boolean  True if the competences are changed. 
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
     * Adds a new task.
     * 
     * @return String Redirection to the current page. 
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
     * Calculates the remaining project's duration.
     * 
     * @return Integer The remaining duration.
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
    
    
    /**
     * Determines the the status of a given task.
     * 
     * @param  task  The selected task.
     * @return String The task's status.
     */
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
    
    
    /**
     * Updates the assigned hours of collaborators is the selected task.
     * It's called when a row is edited.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowEdit(RowEditEvent<CollabTaskPlan> event) {
    	
    	int rowIdCollab = event.getObject().getCollaborator().getIdCollab();
    	int oldValue = oldSpinnerColumns.get(rowIdCollab);
    	
		event.getObject().setAssignedHours(assignedHrsInput+oldValue);
		planBean.updateCollabTaskPlan(event.getObject());
		
		this.selectedTask.addCollabTaskPlan(event.getObject());
		this.proj.addTask(this.selectedTask);
		
		FacesContext.getCurrentInstance().getExternalContext().getFlash()
		.put("proj", this.proj);
		
    	Helpers.addMessage(FacesMessage.SEVERITY_INFO,"xxxx Edited",
    			"The new value '"+ assignedHrsInput + "' is successfully added and saved.");
    	    	
    	//PrimeFaces.current().ajax().update(":addCollabForm:tbl1");
    	
    }
    
    
    /**
     * Pushes the old title values columns to the oldTitleColumns map.
     * It's called when a project row switches to edit mode.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowEditInit(RowEditEvent<CollabTaskPlan> event) {
    	this.oldSpinnerColumns
			.put(event.getObject().getCollaborator().getIdCollab(),
					event.getObject().getAssignedHours());
    	
    	Helpers.addMessage(FacesMessage.SEVERITY_INFO,
    			"Maximum value is set to "+calcMaxPossibleWork(event.getObject()),"");
    }
    
    /**
     * Pops the old column values from the oldTitleColumns map.
     * It's called when a project row edit is cancelled.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowCancel(RowEditEvent<CollabTaskPlan> event) {
    	this.oldSpinnerColumns
			.remove(event.getObject().getCollaborator().getIdCollab());
	
    	//Helpers.addMessage(FacesMessage.SEVERITY_INFO,"Edit Cancelled", "No changes tracked");
    }
    
    
    /**
     * Fetches the selective collabs of the task
     * 
     * @return  List<Collaborator>  the selective collabs.
     * 
     * TODO Delegate this task to the DB because it performs a lot of calculations. 
     */
    public int calcMaxPossibleWork(CollabTaskPlan collabTaskPlan) {
    	List<CollabTaskPlan> selectiveCollabs = fetchSelectiveCollabs(this.selectedTask);
    	Map<Integer,Integer> collabsCurrentWork = new HashMap<Integer,Integer>();
    	
    	// Calculate curr work for each selective collab
    	selectiveCollabs.forEach(ctp ->
    		collabsCurrentWork.put(
    				ctp.getCollaborator().getIdCollab(),
    				calcCurrentWork(ctp.getCollaborator())));
    	
    	int maxWorkCollab = collabsCurrentWork.values()
    						.stream()
    						.max((v1,v2) -> v1 > v2 ? 1 : -1)
    						.get();
    
    	
    	int collabWorkMaj = maxWorkCollab
    							- calcCurrentWork(collabTaskPlan.getCollaborator())
    							- collabTaskPlan.getAssignedHours();
    	System.out.println("--- max(ax) - ai - hi : "+collabWorkMaj);
    	
    	if(collabWorkMaj<=0) {
    		return 0;
    	}
    	
    		
    	System.out.println("--- d/Cx.len : "+Math.floor((double)selectedTask.getDuration()/selectiveCollabs.size()));
    	System.out.println("--- d = "+selectedTask.getDuration());
    	System.out.println("--- Cx = "+selectiveCollabs.size());
    	return (int) Math.min(
    			collabWorkMaj,
    			Math.floor((double)selectedTask.getDuration()/selectiveCollabs.size()));
    }
    
    
    public int calcTaskRemainingDuration() {
    	if (this.selectedTask==null) return -1;
    	int sumDuration = 0;
    	
    	for(CollabTaskPlan plan : this.selectedTask.getCollabTaskPlans()) {
    		sumDuration += plan.getAssignedHours();
    	}
    	
    	return this.selectedTask.getDuration() - sumDuration;
    }
    
    
    /**
     * Fetches the selective collabs of the task
     * 
     * @return  List<Collaborator>  the selective collabs.
     * 
     * TODO Delegate this task to the DB because it performs a lot of calculations. 
     */
	public List<CollabTaskPlan> fetchSelectiveCollabs(Task task){
		if (task==null) return null;
		
		List<CollabTaskPlan> selectiveCollabs = new ArrayList<CollabTaskPlan>(
				task.getCollabTaskPlans());
		List<Collaborator> collabs = collabBean.getAllCollabs();
		selectiveCollabs.forEach(ctp -> collabs.remove(ctp.getCollaborator()));
		
		for (Collaborator collab : collabs) {
			Set<Competence> tasksComps = new HashSet<Competence>(task.getCompetences());
			tasksComps.retainAll(collab.getCompetences());
			
			if(tasksComps.size() != 0) {
				CollabTaskPlan newSelectiveCollab = new CollabTaskPlan();
				newSelectiveCollab.setAssignedHours(0);
				newSelectiveCollab.setTask(this.selectedTask);
				newSelectiveCollab.setCollaborator(collab);
				selectiveCollabs.add(newSelectiveCollab);
			}
		}
		
		return selectiveCollabs;
	}
	
	
	public int calcCurrentWork(Collaborator collab) {
		
		int currentWork = 0;
		
		for (CollabTaskPlan plan : this.plans) {
			if(plan.getCollaborator().getIdCollab() == collab.getIdCollab()) {
				currentWork += plan.getAssignedHours();
			}
		}
		
		return currentWork;
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


	public int getAssignedHrsInput() {
		return assignedHrsInput;
	}


	public void setAssignedHrsInput(int assignedHrsInput) {
		this.assignedHrsInput = assignedHrsInput;
	}


	public Map<Integer, Integer> getOldSpinnerColumns() {
		return oldSpinnerColumns;
	}


	public void setOldSpinnerColumns(Map<Integer, Integer> oldSpinnerColumns) {
		this.oldSpinnerColumns = oldSpinnerColumns;
	}
	
	

}
