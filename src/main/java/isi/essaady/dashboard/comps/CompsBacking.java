package isi.essaady.dashboard.comps;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.RowEditEvent;

import isi.essaady.ejbs.CompetenceBean;
import isi.essaady.entities.Competence;

@Named
@ViewScoped
public class CompsBacking implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private List<Competence> comps;
	private List<Competence> filteredComps;
	private Map<Integer, String>  oldNameColumns;
	private String newCompInput;
	private Competence selectedComp;
	
	@EJB
    private CompetenceBean compBean;
     
    @PostConstruct
    public void init() {
        comps = compBean.getAllComps();
        oldNameColumns = new HashMap<Integer, String>();
    }
    
    
    /**
     * Updates competences. Tests the new value before merging it.
     * It's called when a row is edited.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowEdit(RowEditEvent<Competence> event) {
    	int compId = event.getObject().getIdComp();
    	String oldValue = oldNameColumns.get(compId);
    	
    	if(event.getObject().getName()==null) {
    		event.getObject().setName(oldValue);
    		addMessage(FacesMessage.SEVERITY_WARN,"Warning","A value must be given.");
			return;
    	}
    	else {
        	String newValue = event.getObject().getName().trim();
        	if(!newValue.equals(oldValue)) {
        		/* Testing the new edited value*/
    	    	for (Competence comp : comps) {
    				if(comp.getName().equalsIgnoreCase(newValue) && comp.getIdComp()!=compId) {
    					event.getObject().setName(oldValue);
    					addMessage(FacesMessage.SEVERITY_ERROR,"Error",
    							"Competence already exists");
    					return;
    				}
    			}
    	    	
    	    	/* Tests passed : Save changes to DB*/
    	    	compBean.updateComp(event.getObject());
    	    	addMessage(FacesMessage.SEVERITY_INFO,"Competence Edited",
    	    			"The new value '"+ newValue + "' is successfully saved.");	
        	}   		
    	}
    }
    
    
    /**
     * Pushes the old column value to the oldNameColumns map.
     * It's called when a competence row switches to edit mode.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowEditInit(RowEditEvent<Competence> event) {
    	this.oldNameColumns
    		.put(event.getObject().getIdComp(), event.getObject().getName().trim());
    }
    
    
    /**
     * Pops the old column value from the oldNameColumns map.
     * It's called when a competence row edit is cancelled.
     * 
     * @param event	The handeled RowEditEvent.
     */
    public void onRowCancel(RowEditEvent<Competence> event) {
    	this.oldNameColumns
			.remove(event.getObject().getIdComp());
    	
    	addMessage(FacesMessage.SEVERITY_INFO,"Edit Cancelled", "No changes tracked");
    }
    
    
    /**
     * Adds a new row competence.
     * 
     */
    public void addNewComp() {
    	if(newCompInput==null) {
    		addMessage(FacesMessage.SEVERITY_WARN,"Warning", "A value must be given.");
			return;
    	}
    	else {
    		/* Test if new comp already exist */
        	newCompInput = newCompInput.trim();
        	for (Competence comp : comps) {
    			if(comp.getName().equalsIgnoreCase(newCompInput)){
    				addMessage(FacesMessage.SEVERITY_ERROR, "Error",
    						"Competance already exists");
    				return;
    			}
    		}
        	/* If test passes, persist the new competence */
            Competence newComp = new Competence(newCompInput);
            compBean.createComp(newComp);
            comps = compBean.getAllComps();
            addMessage(FacesMessage.SEVERITY_INFO, "New Competence added",
            		"The new competence '"+ newCompInput + "' is added successfully.");
    	}	
    }
    
    
    /**
     * Removes a selected row competence.
     * 
     * @param comp	Selected competence.
     */
    public void removeComp(Competence comp) {
    	compBean.deleteComp(comp);
    	comps.remove(comp);
    	addMessage(FacesMessage.SEVERITY_INFO, "Competence Deleted",
    			"Competence '"+comp.getName()+"' is successfully deleted.");
    }
    
    /**
     * Displays the Faces message. This is just a helper method
     * 
     * @param severity	The FacesMessage sevirity constant.
     * @param summary	Message summary.
     * @param detail	Message detail.
     */
    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesMessage message = new FacesMessage(severity, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    
    /*
     * GETTERS - SETTERS
     * */
    public List<Competence> getComps() {
		return comps;
	}

	public void setComps(List<Competence> comps) {
		this.comps = comps;
	}

	public List<Competence> getFilteredComps() {
		return filteredComps;
	}

	public void setFilteredComps(List<Competence> filteredComps) {
		this.filteredComps = filteredComps;
	}
    
	public Map<Integer, String> getOldNameColumns() {
			return this.oldNameColumns;
	}
	
	public String getNewCompInput() {
		return this.newCompInput;
	}
	
	public void setNewCompInput(String newCompInput) {
		this.newCompInput = newCompInput;
	}

	public Competence getSelectedComp() {
		return selectedComp;
	}

	public void setSelectedComp(Competence selectedComp) {
		this.selectedComp = selectedComp;
	}
	
	
}
