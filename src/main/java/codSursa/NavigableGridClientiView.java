package codSursa;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.example.MainView;
import org.oop.app.Angajat;
import org.oop.app.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@PageTitle("angajati")
@Route(value = "angajati", layout = MainView.class)
public class NavigableGridClientiView extends VerticalLayout implements HasUrlParameter<Integer>
{
    // Definire model date
    private EntityManager em;
    private List<Angajat> angajati = new ArrayList<>();
    private Angajat angajat = null;
    private Binder<Angajat> binder = new BeanValidationBinder<>(Angajat.class);
    // Definire componente view
    private H1 titluForm = new H1("Lista Angajati");
    // Definire componente suport navigare
    private VerticalLayout gridLayoutToolbar;
    private TextField filterText = new TextField();
    private Button cmdEditClient = new Button("Editeaza angajati...");
    private Button cmdAdaugaClient = new Button("Adauga angajat...");
    private Button cmdStergeClient = new Button("Sterge angajat");
    private Grid<Angajat> grid = new Grid<>(Angajat.class);
    public NavigableGridClientiView()
    {
        initDataModel();
        initViewLayout();
        initControllerActions();
    }

    private void initDataModel(){
        System.out.println("DEBUG START FORM >>> ");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("proiect");
        em = emf.createEntityManager();
        List<Angajat> lst = em
                .createQuery("SELECT c FROM Angajat c ORDER BY c.id", Angajat.class)
                .getResultList();
        System.out.println(lst.size()+"-------lungime lista");
        Metamodel metamodel = emf.getMetamodel();

        for (EntityType<?> entityClass : metamodel.getEntities()) {
            System.out.println("Entity Class: " + entityClass.getName());
        }
        angajati.addAll(lst);
        if (lst != null && !lst.isEmpty()){
            Collections.sort(this.angajati, (c1, c2) -> c1.getId().compareTo(c2.getId()));
            this.angajat = angajati.get(0);
            System.out.println("DEBUG: angajat init >>> " + angajat.getId());
        }
        grid.setItems(this.angajati);
        binder.setBean(this.angajat);
        grid.asSingleSelect().setValue(this.angajat);
    }

    private void initViewLayout() {
// Layout navigare -------------------------------------//
// Toolbar navigare
        filterText.setPlaceholder("Filter by nume...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        HorizontalLayout gridToolbar = new HorizontalLayout(filterText,
                cmdEditClient, cmdAdaugaClient, cmdStergeClient);
// Grid navigare
        grid.setColumns("id", "nume", "functie");
        grid.addComponentColumn(item -> createGridActionsButtons(item)).setHeader("Actiuni");
// Init Layout navigare
        gridLayoutToolbar = new VerticalLayout(gridToolbar, grid);
// ---------------------------
        this.add(titluForm, gridLayoutToolbar);
//

    }
    private Component createGridActionsButtons(Angajat item) {
//
        Button cmdEditItem = new Button("Edit");
        cmdEditItem.addClickListener(e -> {
            grid.asSingleSelect().setValue(item);
            editAngajat();
        });
        Button cmdDeleteItem = new Button("Sterge");
        cmdDeleteItem.addClickListener(e -> {
            System.out.println("Sterge item: " + item);
            grid.asSingleSelect().setValue(item);
            stergeAngajat();
            refreshForm();
        } );
//
        return new HorizontalLayout(cmdEditItem, cmdDeleteItem);
    }

    private void initControllerActions() {
// Navigation Actions
        filterText.addValueChangeListener(e -> updateList());
        cmdEditClient.addClickListener(e -> {
            editAngajat();
        });
        cmdAdaugaClient.addClickListener(e -> {
            adaugaAngajat();
        });
        cmdStergeClient.addClickListener(e -> {
            stergeAngajat();
            refreshForm();
        });
    }

    private void adaugaAngajat() {
        this.getUI().ifPresent(ui -> ui.navigate(FormClientView.class, 999));
    }
    // Editare: delegare catre Formular detalii client
    private void editAngajat() {
        this.angajat = this.grid.asSingleSelect().getValue();
        System.out.println("Selected angajat:: " + angajat);
        if (this.angajat != null) {
            this.getUI().ifPresent(ui -> ui.navigate(
                    FormClientView.class, this.angajat.getId())
            );
        }
    }

    private void stergeAngajat() {
        this.angajat = this.grid.asSingleSelect().getValue();
        System.out.println("To remove: " + this.angajat);
        this.angajati.remove(this.angajat);
        if (this.em.contains(this.angajat)) {
            this.em.getTransaction().begin();
            this.em.remove(this.angajat);
            this.em.getTransaction().commit();
        }
        if (!this.angajati.isEmpty())
            this.angajat = this.angajati.get(0);
        else
            this.angajat = null;
    }

    private void updateList() {
        try {
            List<Angajat> lstClientiFiltered = this.angajati;

            if (filterText.getValue() != null) {
                lstClientiFiltered = this.angajati.stream()
                        .filter(c -> c.getNume().contains(filterText.getValue()))
                        .toList();

                grid.setItems(lstClientiFiltered);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshForm() {
        System.out.println("Angajat curent: " + this.angajat);
        if (this.angajat != null) {
            grid.setItems(this.angajati);
            binder.setBean(this.angajat);
            grid.select(this.angajat);
        }
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent,  @OptionalParameter Integer id)
    {
        if (id != null) {
            this.angajat = em.find(Angajat.class, id);
            System.out.println("Back angajat: " + angajat);
            if (this.angajat == null) {
                // DELETED Item
                if (!this.angajati.isEmpty())
                    this.angajat = this.angajati.get(0);
            }
            // else: EDITED or NEW Item
        }
        this.refreshForm();
    }
}
