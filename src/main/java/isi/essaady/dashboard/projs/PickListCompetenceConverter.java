package isi.essaady.dashboard.projs;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;

import isi.essaady.entities.Competence;

@FacesConverter(value = "competenceConverter")
public class PickListCompetenceConverter implements Converter {
	
@SuppressWarnings("unchecked")
@Override
  public Object getAsObject(FacesContext fc, UIComponent comp, String value) {
      DualListModel<Competence> model = (DualListModel<Competence>) ((PickList) comp).getValue();
      for (Competence competence : model.getSource()) {
          if (competence.getName().equals(value)) {
              return competence;
          }
      }
      for (Competence competence : model.getTarget()) {
          if (competence.getName().equals(value)) {
              return competence;
          }
      }
      return null;
  }

  @Override
  public String getAsString(FacesContext fc, UIComponent comp, Object value) {
      return ((Competence) value).getName();
  }
}