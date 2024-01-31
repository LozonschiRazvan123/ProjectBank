package Cont;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.example.MainView;
import org.oop.app.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@PageTitle("credite")
@Route(value = "credite", layout = MainView.class)
public class NavigableGridContView extends VerticalLayout implements HasUrlParameter<Integer> {
    private static final long serialVersionUID = 1L;

    // Definire model date
    private EntityManager em;
    private List<Credit> conturi = new ArrayList<>();
    private Credit cont = null;

    // Definire componente view
    private H1 titluForm = new H1("Lista Credite");
    // Definire componente suport navigare
    private VerticalLayout gridLayoutToolbar;
    private TextField filterText = new TextField();
    private Button cmdEditProdus = new Button("Editeaza credit...");
    private Button cmdAdaugaProdus = new Button("Adauga credit...");
    private Button cmdStergeProdus = new Button("Sterge credit");
    private Grid<Credit> grid = new Grid<>(Credit.class);

    // Start Form
    public NavigableGridContView() {
        //
        initDataModel();
        //
        initViewLayout();
        //
        initControllerActions();
    }

    // Navigation Management
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Integer id) {
        if (id != null) {
            this.cont = em.find(Credit.class, id);
            System.out.println("Back cont: " + cont);
            if (this.cont == null) {
                // DELETED Item
                if (!this.conturi.isEmpty())
                    this.cont = this.conturi.get(0);
            }
            // else: EDITED or NEW Item
        }
        this.refreshForm();
    }

    // init Data Model
    private void initDataModel() {
        System.out.println("DEBUG START FORM >>>  ");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("proiect");
        em = emf.createEntityManager();

        List<Credit> lst = em
                .createQuery("SELECT c FROM Credit c ORDER BY c.id", Credit.class)
                .getResultList();
        conturi.addAll(lst);

        if (lst != null && !lst.isEmpty()) {
            Collections.sort(this.conturi, (p1, p2) -> p1.getId().compareTo(p2.getId()));
            this.cont = conturi.get(0);
            System.out.println("DEBUG: cont init >>> " + cont.getId());
        }
        //
        grid.setItems(this.conturi);
        grid.asSingleSelect().setValue(this.cont);
    }

    // init View Model
    private void initViewLayout() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        // Layout navigare -------------------------------------//
        // Toolbar navigare
        filterText.setPlaceholder("Filter by id...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        HorizontalLayout gridToolbar = new HorizontalLayout(filterText,
                cmdEditProdus, cmdAdaugaProdus, cmdStergeProdus);
        // Grid navigare
        grid.setColumns("id", "sumaCredit", "perioada", "rataDobanda", "aprobat");
        grid.addComponentColumn(item -> createGridActionsButtons(item)).setHeader("Actiuni");
        // Init Layout navigare
        gridLayoutToolbar = new VerticalLayout(gridToolbar, grid);
        // ---------------------------
        this.add(titluForm, gridLayoutToolbar);
        //
    }

    // init Controller components
    private void initControllerActions() {
        // Navigation Actions
        filterText.addValueChangeListener(e -> updateList());
        cmdEditProdus.addClickListener(e -> {
            editCont();
        });
        cmdAdaugaProdus.addClickListener(e -> {
            adaugaProdus();
        });
        cmdStergeProdus.addClickListener(e -> {
            stergeProdus();
            refreshForm();
        });
    }

    //
    private Component createGridActionsButtons(Credit item) {
        //
        Button cmdEditItem = new Button("Edit");
        cmdEditItem.addClickListener(e -> {
            grid.asSingleSelect().setValue(item);
            editCont();
        });
        Button cmdDeleteItem = new Button("Sterge");
        cmdDeleteItem.addClickListener(e -> {
            System.out.println("Sterge item: " + item);
            grid.asSingleSelect().setValue(item);
            stergeProdus();
            refreshForm();
        });
        //
        return new HorizontalLayout(cmdEditItem, cmdDeleteItem);
    }

    //
    private void editCont() {
        this.cont = this.grid.asSingleSelect().getValue();
        System.out.println("Selected cont:: " + cont);
        if (this.cont != null) {
            UI.getCurrent().navigate(FormContView.class, this.cont.getId());
        }
    }

    //
    private void updateList() {
        try {
            List<Credit> lstProduseFiltrate = this.conturi;

            if (filterText.getValue() != null && !filterText.getValue().isEmpty()) {
                lstProduseFiltrate = this.conturi.stream()
                        .filter(p -> String.valueOf(p.getId()).contains(filterText.getValue()))
                        .toList();
            }

            grid.setItems(lstProduseFiltrate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    private void refreshForm() {
        System.out.println("Cont curent: " + this.cont);
        if (this.cont != null) {
            grid.setItems(this.conturi);
            grid.select(this.cont);
        }
    }

    // CRUD actions
    private void adaugaProdus() {
        this.getUI().ifPresent(ui -> ui.navigate(FormContView.class, 999));
    }

    private void stergeProdus() {
        this.cont = this.grid.asSingleSelect().getValue();
        System.out.println("To remove: " + this.cont);
        em.getTransaction().begin();

        this.conturi.remove(this.cont);
        if (this.em.contains(this.cont)) {
            em.remove(this.cont);
        }

        em.getTransaction().commit();
        /*} catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            if (!this.conturi.isEmpty()) {
                this.cont = this.conturi.get(0);
            } else {
                this.cont = null;
            }*/
        refreshForm();
    }
}