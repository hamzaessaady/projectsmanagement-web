package tests;

import javax.faces.component.UIComponentBase;

public class MainTest {

	public static void main(String[] args) {
		//dumpEvents(new org.primefaces.component.inputtext.InputText());
        //dumpEvents(new org.primefaces.component.autocomplete.AutoComplete());
        dumpEvents(new org.primefaces.component.datatable.DataTable());
	}
	
	private static void dumpEvents(UIComponentBase comp) {
		System.out.println(
				comp + ":\n\tdefaultEvent: " + comp.getDefaultEventName() + ";\n\tEvents: " + comp.getEventNames());
	}

}
