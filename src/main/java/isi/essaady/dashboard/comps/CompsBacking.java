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
    	String newValue = event.getObject().getName().trim();
    	
    	if(!newValue.equals(oldValue)) {
    		/* Testing the new edited value*/
	    	for (Competence comp : comps) {
				if(comp.getName().equalsIgnoreCase(newValue) && comp.getIdComp()!=compId) {
					event.getObject().setName(oldValue);
					FacesMessage msg = new FacesMessage(
							FacesMessage.SEVERITY_ERROR,
							"Error", "Competance already exists");
		            FacesContext.getCurrentInstance().addMessage(null, msg);
					return;
				}
			}
	    	
	    	/* Tests passed : Save changes to DB*/
	    	compBean.updateComp(event.getObject());
	    	FacesMessage msg = new FacesMessage(
	    			"Competence Edited",
	    			"The new value '"+ newValue + "' is successfully saved.");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
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
    		.put(event.getObject().getIdComp(), event.getObject().getName());
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
    	
        FacesMessage msg = new FacesMessage("Edit Cancelled", "No changes tracked");
        FacesContext.getCurrentInstance().addMessage(null, msg);
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
}
