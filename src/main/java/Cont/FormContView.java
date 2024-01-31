package Cont;
import java.text.SimpleDateFormat;

import com.vaadin.flow.component.checkbox.Checkbox;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.example.MainView;
import org.oop.app.Cont;
import org.oop.app.Credit;

@PageTitle("credit")
@Route(value = "credit", layout = MainView.class)
public class FormContView extends VerticalLayout implements HasUrlParameter<Integer>{
    private static final long serialVersionUID = 1L;

    // Definire model date
    private EntityManager 	em;
    private Credit cont = null;
    private Binder<Credit> 	binder = new BeanValidationBinder<>(Credit.class);

    // Definire componente view
    // Definire Form-Master
    private VerticalLayout formLayoutToolbar;
    private H1 titluForm = new H1("Form Credit");
    private IntegerField id = new IntegerField("ID credit:");
    private NumberField sold = new NumberField("Suma credit: ");
    private NumberField rataDobanda = new NumberField("Rata dobanda: ");
    private ComboBox<Integer> moneda = new ComboBox<>("Perioada: ");
    private Checkbox activ = new Checkbox("Aprobat: ");
    private Button 			cmdAdaugare = new Button("Adauga");
    private Button 			cmdSterge 	= new Button("Sterge");
    private Button 			cmdAbandon 	= new Button("Abandon");
    private Button 			cmdSalveaza = new Button("Salveaza");

    // Start Form
    public FormContView() {
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
        System.out.println("Cont ID: " + id);
        if (id != null) {
            // EDIT Item
            this.cont = em.find(Credit.class, id);
            System.out.println("Selected cont to edit:: " + cont);
            if (this.cont == null) {
                System.out.println("ADD cont:: " + cont);
                // NEW Item
                this.adaugaCont();
                this.cont.setId(id);
                this.cont.setPerioada(0); // Set a default value for sold
                this.cont.setSumaCredit(0.0); // Set a default value for moneda
                this.cont.setRataDobanda(0.0); // Set a default value for moneda
                this.cont.setAprobat(true); // Set a default value for activ
            }
        }
        this.refreshForm();
    }
    // init Data Model
    private void initDataModel(){
        System.out.println("DEBUG START FORM >>>  ");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("proiect");
        this.em = emf.createEntityManager();
        this.cont = em
                .createQuery("SELECT c FROM Credit c ORDER BY c.id", Credit.class)
                .getResultStream().findFirst().get();

        //
        moneda.setItems(2021, 2022, 2023, 2024);
        //
        binder.bind(id, "id");
        binder.bind(sold, "sumaCredit");
        binder.bind(rataDobanda, "rataDobanda");
        binder.bind(moneda, "perioada");
        binder.bind(activ, "aprobat");
        //
        refreshForm();
    }

    // init View Model
    private void initViewLayout() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        // Form-Master-Details -----------------------------------//
        // Form-Master
        FormLayout formLayout = new FormLayout();
        formLayout.add(id, sold , moneda, activ);
        formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
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

    // init Controller components
    private void initControllerActions() {
        // Transactional Master Actions
        cmdAdaugare.addClickListener(e -> {
            adaugaCont();
            refreshForm();
        });
        cmdSterge.addClickListener(e -> {
            stergeProdus();
            // Navigate back to NavigableGridProduseForm
            this.getUI().ifPresent(ui -> ui.navigate(
                    NavigableGridContView.class)

            );
            this.refreshForm();
        });
        cmdAbandon.addClickListener(e -> {
            // Navigate back to NavigableGridProduseForm
            this.getUI().ifPresent(ui -> ui.navigate(
                    NavigableGridContView.class, this.cont.getId())
            );
        });
        cmdSalveaza.addClickListener(e -> {
            salveazaProdus();
            // refreshForm();
            // Navigate back to NavigableGridProduseForm
            this.getUI().ifPresent(ui -> ui.navigate(
                    NavigableGridContView.class, this.cont.getId())
            );
        });
    }
    //
    private void refreshForm() {
        System.out.println("Produs curent: " + this.cont);
        if (this.cont != null) {
            binder.setBean(this.cont);
        }
    }

    // CRUD actions
    private void adaugaCont() {
        this.cont = new Credit();
        this.cont.setId(999);
        this.cont.setRataDobanda(0.0);
    }

    private void stergeProdus() {
        System.out.println("To remove: " + this.cont);
        if (this.em.contains(this.cont)) {
            this.em.getTransaction().begin();
            this.em.remove(this.cont);
            this.em.getTransaction().commit();
            this.em.refresh(this.cont);
        }
    }

    private void salveazaProdus() {
        try {
            this.em.getTransaction().begin();
            this.cont = this.em.merge(this.cont);
            this.em.getTransaction().commit();
            System.out.println("Produs Salvat");
        } catch (Exception ex) {
            if (this.em.getTransaction().isActive())
                this.em.getTransaction().rollback();
            System.out.println("*** EntityManager Validation ex: " + ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }
}
