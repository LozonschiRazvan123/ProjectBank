package codSursa;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.MainView;
import org.oop.app.Angajat;
import org.oop.app.Client;

@PageTitle("angajat")
@Route(value = "angajat", layout = MainView.class)
public class FormClientView extends VerticalLayout implements HasUrlParameter<Integer>
{
    private EntityManager em;
    private Angajat angajat = null;
    private Binder<Angajat> binder = new BeanValidationBinder<>(Angajat.class);

    private VerticalLayout formLayoutToolbar;
    private H1 titluForm = new H1("Form Angajat");
    private IntegerField id = new IntegerField("ID Angajat:");
    private TextField nume = new TextField("Nume angajat: ");
    private TextField functie = new TextField("Functie: ");

    private Button cmdAdaugare = new Button("Adauga");
    private Button cmdSterge = new Button("Sterge");
    private Button cmdAbandon = new Button("Abandon");
    private Button cmdSalveaza = new Button("Salveaza");

    public FormClientView() {
        initDataModel();
        initViewLayout();
        initControllerActions();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent,  @OptionalParameter Integer id)
    {
        System.out.println("Angajat ID: " + id);
        if (id != null) {
            // EDIT Item
            this.angajat = em.find(Angajat.class, id);
            System.out.println("Selected angajat to edit:: " + angajat);
            if (this.angajat == null) {
                System.out.println("ADD angajat:: " + angajat);
                // NEW Item
                this.adaugaAngajat();
                this.angajat.setId(id);
                this.angajat.setNume("Angajat NOU " + id);
                this.angajat.setFunctie("Functie NOU " + id);
            }
        }
        this.refreshForm();
    }

    private void initDataModel(){
        System.out.println("DEBUG START FORM >>> ");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("proiect");
        this.em = emf.createEntityManager();
        this.angajat = em
                .createQuery("SELECT c FROM Angajat c ORDER BY c.id", Angajat.class)
                .getResultStream().findFirst().get();
//
        binder.forField(id).bind("id");
        binder.forField(nume).bind("nume");
        binder.forField(functie).bind("functie");
//
        refreshForm();
    }

    private void initViewLayout() {
// Form-Master-Details -----------------------------------//
// Form-Master
        FormLayout formLayout = new FormLayout();
        formLayout.add(id, nume, functie);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.setMaxWidth("400px");
// Toolbar-Actions-Master
        HorizontalLayout actionToolbar =
                new HorizontalLayout(cmdAdaugare, cmdSterge, cmdAbandon, cmdSalveaza);
        actionToolbar.setPadding(false);
//
        this.formLayoutToolbar = new VerticalLayout(formLayout, actionToolbar);
// ---------------------------
        this.add(titluForm, formLayoutToolbar);
//
    }

    private void initControllerActions() {
// Transactional Master Actions
        cmdAdaugare.addClickListener(e -> {
            adaugaAngajat();
            refreshForm();
        });
        cmdSterge.addClickListener(e -> {
            stergeAngajat();
// Navigate back to NavigableGridClienteForm
            this.getUI().ifPresent(ui -> ui.navigate( NavigableGridClientiView.class) );
        });
        cmdAbandon.addClickListener(e -> {
// Navigate back to NavigableGridClienteForm
            this.getUI().ifPresent(ui -> ui.navigate( NavigableGridClientiView.class, this.angajat.getId()) );
        });
        cmdSalveaza.addClickListener(e -> {
            salveazaAngajat();
// refreshForm();
// Navigate back to NavigableGridClienteForm
            this.getUI().ifPresent(ui -> ui.navigate( NavigableGridClientiView.class, this.angajat.getId()) );
        });
    }

    private void refreshForm() {
        System.out.println("Angajat curent: " + this.angajat);
        if (this.angajat != null) {
            binder.setBean(this.angajat);
        }
    }

    private void salveazaAngajat() {
        try {
            this.em.getTransaction().begin();
            this.angajat = this.em.merge(this.angajat);
            this.em.getTransaction().commit();
            System.out.println("Client Salvat");
        } catch (Exception ex) {
            if (this.em.getTransaction().isActive())
                this.em.getTransaction().rollback();
            System.out.println("*** EntityManager Validation ex: " + ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    private void adaugaAngajat() {
        this.angajat = new Angajat();
        this.angajat.setId(999); // ID arbitrar, inexistent în baza de date
        this.angajat.setNume("Client Nou");
        this.angajat.setFunctie("Client Nou");
    }
    // CRUD actions
    private void stergeAngajat() {
        System.out.println("To remove: " + this.angajat);
        if (this.em.contains(this.angajat)) {
            this.em.getTransaction().begin();
            this.em.remove(this.angajat);
            this.em.getTransaction().commit();
        }
    }

}
