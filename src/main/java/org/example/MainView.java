package org.example;

import Cont.FormContView;
import Cont.NavigableGridContView;
import Credit.FormTranzactiiView;
import Credit.NavigableGridTranzactiiView;
import codSursa.FormClientView;
import codSursa.NavigableGridClientiView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

@Route
public class MainView extends VerticalLayout implements RouterLayout {

    public MainView() {
        setMenuBar();
    }

    private void setMenuBar() {
        MenuBar mainMenu = new MenuBar();

        // Meniu Home
        MenuItem homeMenu = mainMenu.addItem("Home");
        homeMenu.addClickListener(event -> UI.getCurrent().navigate(MainView.class));

        // Meniu Angajati
        MenuItem gridFormsClientiMenu = mainMenu.addItem("Angajati");
        SubMenu gridFormsClientiMenuBar = gridFormsClientiMenu.getSubMenu();
        gridFormsClientiMenuBar.addItem("Lista Angajati...",
                event -> UI.getCurrent().navigate(NavigableGridClientiView.class));
        gridFormsClientiMenuBar.addItem("Form Editare Angajati...",
                event -> UI.getCurrent().navigate(FormClientView.class));

        // Meniu Conturi
        MenuItem gridFormsConturiMenu = mainMenu.addItem("Credite");
        SubMenu gridFormsConturiMenuBar = gridFormsConturiMenu.getSubMenu();
        gridFormsConturiMenuBar.addItem("Lista Credite...",
                event -> UI.getCurrent().navigate(NavigableGridContView.class));
        gridFormsConturiMenuBar.addItem("Form Editare Credite...",
                event -> UI.getCurrent().navigate(FormContView.class));
        // Adăugați orice alte opțiuni pentru Cont aici

        MenuItem gridFormsTranzactiiMenu = mainMenu.addItem("Tranzactii");
        SubMenu gridFormsTranzactiiMenuBar = gridFormsTranzactiiMenu.getSubMenu();
        gridFormsTranzactiiMenuBar.addItem("Lista Tranzactii...",
                event -> UI.getCurrent().navigate(NavigableGridTranzactiiView.class));
        gridFormsTranzactiiMenuBar.addItem("Form Editare Tranzactii...",
                event -> UI.getCurrent().navigate(FormTranzactiiView.class));

        // Adăugați meniul la layout
        add(new HorizontalLayout(mainMenu));
    }
}
