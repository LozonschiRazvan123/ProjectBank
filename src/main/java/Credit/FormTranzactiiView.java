package Credit;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;
import java.util.List;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.textfield.TextField;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.example.MainView;
import org.oop.app.Client;
import org.oop.app.Cont;
import org.oop.app.Tranzactie;

@PageTitle("tranzactie")
@Route(value = "tranzactie", layout = MainView.class)
public class FormTranzactiiView extends VerticalLayout implements HasUrlParameter<Integer>{
    private static final long serialVersionUID = 1L;

    // Definire model date
    private EntityManager em;
    private Cont comanda = null;
    private List<Client> clienti = new ArrayList<>();
    private Binder<Cont> binder = new Binder<>(Cont.class);

    // Definire componente view
    // Definire Form-Master
    private VerticalLayout  formLayoutToolbar;
    private H1 				titluForm 	= new H1("Form Cont - Tranzactie");
    private IntegerField id = new IntegerField("ID cont:");
    private NumberField sold = new NumberField("Sold: ");
    private ComboBox<String> moneda = new ComboBox<>("Moneda: ");
    private Checkbox activ = new Checkbox("Activ: ");
    private ComboBox<Client> client = new ComboBox<>("Client");
    // Definire componente actiuni Form-Master-Controller
    private Button 			cmdAdaugare = new Button("Adauga");
    private Button 			cmdSterge 	= new Button("Sterge");
    private Button 			cmdAbandon 	= new Button("Abandon");
    private Button 			cmdSalveaza = new Button("Salveaza");
    // Definire Grid-Details
    private Grid<Tranzactie> 	articoleDetailGrid = new Grid<>(Tranzactie.class, false);
    private Button 			cmdAdaugaArticolComanda  = new Button("Adauga tranzactie");
    private Button 			cmdStergeArticolComanda  = new Button("Sterge tranzactie");


    // Start Form
    public FormTranzactiiView() {
        //
        initDataModel();
        //
        initViewLayout();
        //
        initControllerActions();
    }
    // Navigation Management
    @Override
    public void setParameter(BeforeEvent event,
                             @OptionalParameter Integer id) {
        System.out.println("Comanda ID: " + id);
        if (id != null) {
            // EDIT Item
            this.comanda = em.find(Cont.class, id);
            System.out.println("Selected comanda to edit:: " + comanda);
            if (this.comanda == null) {
                System.out.println("ADD comanda:: " + comanda);
                // NEW Item
                this.adaugaComanda();
                this.comanda.setId(id);
                //
            }
        }
        this.refreshForm();
    }
    // init Data Model
    private void initDataModel(){
        System.out.println("DEBUG START FORM >>>  ");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("proiect");
        this.em = emf.createEntityManager();
        this.comanda = em
                .createQuery("SELECT c FROM Cont c ORDER BY c.id", Cont.class)
                .getResultStream()
                .findFirst()
                .orElse(null);
        //
        System.out.println(">>> init model Clienti >>> ");
        this.clienti = em.createQuery("SELECT c FROM Client c", Client.class).getResultList();
        if (this.clienti != null && !this.clienti.isEmpty())
            Collections.sort(this.clienti, (c1, c2) -> c1.getId().compareTo(c2.getId()));
        binder.bind(client, "client");
        moneda.setItems("EUR", "USD", "RON");
        //
        binder.bind(id, "id");
        binder.bind(sold, "sold");
        binder.bind(moneda, "moneda");
        binder.bind(activ, "activ");
        this.client.setItems(this.clienti);
        /*binder.forField(dataComanda)
                .withConverter(new LocalDateToDateConverter())
                .bind("dataComanda");*/
        //
        refreshForm();
    }

    // init View Model
    private void initViewLayout() {
        //
        client.setItemLabelGenerator(c -> c.getNume());
        //
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        // Form-Master-Details -----------------------------------//
        // Form-Master
        FormLayout formLayout = new FormLayout();
        formLayout.add(id, sold, moneda, activ, client);
        formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
        formLayout.setMaxWidth("400px");
        // Toolbar-Actions-Master
        HorizontalLayout actionToolbar =
                new HorizontalLayout(cmdAdaugare, cmdSterge, cmdAbandon, cmdSalveaza);
        actionToolbar.setPadding(false);
        // Grid details
        articoleDetailGrid.addColumn("id");
        /*articoleDetailGrid.addColumn(p -> p.getProdus().getDenumire())
                .setKey("produs")
                .setHeader("Produs");*/
        articoleDetailGrid.addColumn("suma");
        articoleDetailGrid.addColumn("data");
        articoleDetailGrid.addColumn("descriere");
        articoleDetailGrid.addColumn("procesata");
        articoleDetailGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        //
        initDetailsGridEditor();
        //
        HorizontalLayout gridDetailsToolbar =
                new HorizontalLayout(cmdAdaugaArticolComanda, cmdStergeArticolComanda);
        // Grid Details Layout
        VerticalLayout releaseDetailGridLayout = new VerticalLayout(articoleDetailGrid, gridDetailsToolbar);

        //
        this.formLayoutToolbar = new VerticalLayout(formLayout, actionToolbar, releaseDetailGridLayout);
        // ---------------------------
        this.add(titluForm, formLayoutToolbar);
        //
    }

    // init Controller components
    private void initControllerActions() {
        // Transactional Master Actions
        cmdAdaugare.addClickListener(e -> {
            adaugaComanda();
            refreshForm();
            //this.switchView();
        });
        cmdSterge.addClickListener(e -> {
            stergeComanda();
            // Navigate back to NavigableGridComandae
            this.getUI().ifPresent(ui -> ui.navigate(
                    NavigableGridTranzactiiView.class)
            );
        });
        cmdAbandon.addClickListener(e -> {
            // Navigate back to NavigableGridComandae
            this.getUI().ifPresent(ui -> ui.navigate(
                    NavigableGridTranzactiiView.class, this.comanda.getId())
            );
        });
        cmdSalveaza.addClickListener(e -> {
            salveazaComanda();
            // refreshForm();
            // Navigate back to NavigableGridComandae
            this.getUI().ifPresent(ui -> ui.navigate(
                    NavigableGridTranzactiiView.class, this.comanda.getId())
            );
        });
        // Details Action
        cmdAdaugaArticolComanda.addClickListener(e -> adaugaArticolComanda());
        cmdStergeArticolComanda.addClickListener(e -> stergeArticolComanda());
    }
    // Details Actions
    public void adaugaArticolComanda() {
        if (this.comanda != null)
        {
            Tranzactie newArticolComanda =
                    new Tranzactie(0, 0.0, new Date(), "descriere", false, this.comanda);
            //this.comanda.adauga(this.produse.get(0), 0.0);
            this.comanda.getTranzactii().add(newArticolComanda);
            updateArticolComandaDetailGrid();
            //
            this.articoleDetailGrid.asSingleSelect().setValue(newArticolComanda);
            this.articoleDetailGrid.getEditor().editItem(newArticolComanda);
            //
            Focusable gridColumnEditor = (Focusable) this.articoleDetailGrid.getColumnByKey("id").getEditorComponent();
            gridColumnEditor.focus();
        }
    }

    public void stergeArticolComanda() {
        Tranzactie curentArticolComanda = articoleDetailGrid.asSingleSelect().getValue();
        if (curentArticolComanda != null) {
            this.comanda.getTranzactii().remove(curentArticolComanda);
            updateArticolComandaDetailGrid();
        }
    }
    //
    private void refreshForm() {
        System.out.println("Comanda curent: " + this.comanda);
        if (this.comanda != null) {
            binder.setBean(this.comanda);
            updateArticolComandaDetailGrid();
        }
    }

    private void updateArticolComandaDetailGrid() {
        if (this.comanda != null && this.comanda.getTranzactii() != null) {
            articoleDetailGrid.setItems(this.comanda.getTranzactii());
        }
        else {
            articoleDetailGrid.setItems(Collections.emptyList()); // or handle it as needed
        }
    }

    //
    private void initDetailsGridEditor() {
        // Init grid editor form
        Binder<Tranzactie> gridBinder = new Binder<>(Tranzactie.class);
        Editor<Tranzactie> gridEditor = articoleDetailGrid.getEditor();
        gridEditor.setBinder(gridBinder);
        // id articol editor
        IntegerField idArticolField = new IntegerField();
        idArticolField.setWidthFull();
        idArticolField.setReadOnly(false);
        gridBinder.bind(idArticolField, "id");
        articoleDetailGrid.getColumnByKey("id").setEditorComponent(idArticolField);

        // indicativ editor
        /*ComboBox<Produs> produsField = new ComboBox<Produs>();
        produsField.setWidthFull();
        produsField.setItemLabelGenerator(p -> p.getDenumire());
        produsField.setAllowCustomValue(false);
        produsField.setItems(this.produse);
        gridBinder.bind(produsField, "produs");
        articoleDetailGrid.getColumnByKey("produs").setEditorComponent(produsField);*/
        // descriere editor
        NumberField cantitateField = new NumberField();
        cantitateField.setValue(0.0);
        cantitateField.setWidthFull();
        gridBinder.bind(cantitateField, "suma");
        articoleDetailGrid.getColumnByKey("suma").setEditorComponent(cantitateField);

        DatePicker dataComandaPicker = new DatePicker();
        dataComandaPicker.setWidthFull();
        gridBinder.forField(dataComandaPicker)
                .withConverter(new LocalDateToDateConverter())
                .bind("data");
        articoleDetailGrid.getColumnByKey("data").setEditorComponent(dataComandaPicker);

        TextField descriereField = new TextField();
        descriereField.setWidthFull();
        gridBinder.bind(descriereField, "descriere");
        articoleDetailGrid.getColumnByKey("descriere").setEditorComponent(descriereField);

        Checkbox procesatCheckbox = new Checkbox();
        gridBinder.bind(procesatCheckbox, "procesata");
        articoleDetailGrid.getColumnByKey("procesata").setEditorComponent(procesatCheckbox);
        // activare editor la dublu click
        articoleDetailGrid.addItemDoubleClickListener(e -> {
            gridEditor.editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable) editorComponent).focus();
            }
        });
    }

    // CRUD actions
    private void adaugaComanda() {
        this.comanda = new Cont();
        this.comanda.setId(999);
        this.comanda.setSold(0.0);
        this.comanda.setMoneda("RON");
        this.comanda.setActiv(false);
        this.comanda.setClient(this.clienti.get(1));
    }

    private void stergeComanda() {
        System.out.println("To remove: " + this.comanda);
        if (this.em.contains(this.comanda)) {
            this.em.getTransaction().begin();
            this.em.remove(this.comanda);
            this.em.getTransaction().commit();
        }
    }

    private void salveazaComanda() {
        try {
            this.em.getTransaction().begin();
            this.comanda = this.em.merge(this.comanda);
            this.em.getTransaction().commit();
            System.out.println("Comanda Salvat");
        } catch (Exception ex) {
            if (this.em.getTransaction().isActive())
                this.em.getTransaction().rollback();
            System.out.println("*** EntityManager Validation ex: " + ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }
}