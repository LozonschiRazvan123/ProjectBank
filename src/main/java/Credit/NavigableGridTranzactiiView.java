package Credit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import com.vaadin.flow.component.Component;
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
import org.oop.app.Card;
import org.oop.app.Client;
import org.oop.app.Cont;
import org.oop.app.IstoricTranzactii;

@PageTitle("tranzactii")
@Route(value = "tranzactii", layout = MainView.class)
public class NavigableGridTranzactiiView extends VerticalLayout implements HasUrlParameter<Integer> {
    private static final long serialVersionUID = 1L;

    // Definire model date
    private EntityManager em;
    private List<Cont> comenzi = new ArrayList<>();
    private Cont comanda = null;

    // Definire componente view
    private H1 titluForm = new H1("Lista credite");
    // Definire componente suport navigare
    private VerticalLayout gridLayoutToolbar;
    private TextField filterText = new TextField();
    private Button cmdEditComanda = new Button("Editeaza Credit...");
    private Button cmdAdaugaComanda = new Button("Adauga Credit...");
    private Button cmdStergeComanda = new Button("Sterge Credit");
    private Grid<Cont> grid = new Grid<>(Cont.class);
    // Form Master-Details

    // Start Form
    public NavigableGridTranzactiiView() {
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
        if (id != null) {
            this.comanda = em.find(Cont.class, id);
            System.out.println("Back comanda: " + comanda);
            if (this.comanda == null) {
                // DELETED Item
                if (!this.comenzi.isEmpty())
                    this.comanda = this.comenzi.get(0);
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

        List<Cont> lst = em
                .createQuery("SELECT c FROM Cont c ORDER BY c.id", Cont.class)
                .getResultList();
        this.comenzi.addAll(lst);

        if (lst != null && !lst.isEmpty()) {
            Collections.sort(this.comenzi, (c1, c2) -> c1.getId().compareTo(c2.getId()));
            this.comanda = comenzi.get(0);
            System.out.println("DEBUG: comanda init >>> " + comanda.getId());
            this.comanda.getTranzactii()
                    .sort((a1, a2) -> a1.getId().compareTo(a2.getId()));
        }
        //
        grid.setItems(this.comenzi);
        grid.asSingleSelect().setValue(this.comanda);
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
                cmdEditComanda, cmdAdaugaComanda, cmdStergeComanda);
        // Grid navigare
        grid.setColumns("id");
        //
        /*grid
                .addColumn(c -> c.getDataComanda() == null ? null : dateFormat.format(c.getDataComanda()))
                .setHeader("Data Comanda")
                .setKey("dataComanda");
        //*/
        grid.addColumn("sold");
        grid.addColumn("moneda");
        grid.addColumn("activ");
        grid.addColumn(c -> c.getClient().getNume())
                .setKey("client")
                .setHeader("Client");
        //
        grid.addComponentColumn(c -> createGridActionsButtons(c))
                .setHeader("Actiuni");
        // Init Layout navigare
        //gridLayoutToolbar = new VerticalLayout(gridToolbar, grid);
        // ---------------------------
        this.add(titluForm, gridToolbar, grid);
        //
    }

    // init Controller components
    private void initControllerActions() {
        // Navigation Actions
        filterText.addValueChangeListener(e -> updateList());
        cmdEditComanda.addClickListener(e -> {
            editComanda();
        });
        cmdAdaugaComanda.addClickListener(e -> {
            adaugaComanda();
        });
        cmdStergeComanda.addClickListener(e -> {
            stergeComanda();
            refreshForm();
        });
    }

    //
    private Component createGridActionsButtons(Cont comanda) {
        //
        Button cmdEditItem = new Button("Edit");
        cmdEditItem.addClickListener(e -> {
            grid.asSingleSelect().setValue(comanda);
            editComanda();
        });
        Button cmdDeleteItem = new Button("Sterge");
        cmdDeleteItem.addClickListener(e -> {
            System.out.println("Sterge item: " + comanda);
            grid.asSingleSelect().setValue(comanda);
            stergeComanda();
            refreshForm();
        });
        //
        return new HorizontalLayout(cmdEditItem, cmdDeleteItem);
    }

    //
    private void editComanda() {
        this.comanda = this.grid.asSingleSelect().getValue();
        System.out.println("Selected comanda:: " + comanda);
        if (this.comanda != null) {
            this.getUI().ifPresent(ui -> ui.navigate(
                    FormTranzactiiView.class, this.comanda.getId())
            );
        }
    }

    //
    private void updateList() {
        try {
            List<Cont> lstComandaeFiltrate = this.comenzi;

            if (filterText.getValue() != null) {
                lstComandaeFiltrate = this.comenzi.stream()
                        .filter(p -> p.getId().toString().contains(filterText.getValue()))
                        .toList();

                grid.setItems(lstComandaeFiltrate);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    private void refreshForm() {
        System.out.println("Comanda curent: " + this.comanda);
        if (this.comanda != null) {
            grid.setItems(this.comenzi);
            grid.select(this.comanda);
        }
    }

    // CRUD actions
    private void adaugaComanda() {
        this.getUI().ifPresent(ui -> ui.navigate(FormTranzactiiView.class, 999));
    }

    private void stergeComanda() {
        this.comanda = this.grid.asSingleSelect().getValue();
        System.out.println("To remove: " + this.comanda);

        // Obține toate cardurile care au cheia străină către contul curent
        List<Card> carduriDeSters = em.createQuery("SELECT c FROM Card c WHERE c.cont = :cont", Card.class)
                .setParameter("cont", this.comanda)
                .getResultList();

        // Obține toate tranzacțiile care au referința către contul curent
        List<IstoricTranzactii> tranzactiiDeSters = em.createQuery("SELECT t FROM IstoricTranzactii t WHERE t.cont = :cont", IstoricTranzactii.class)
                .setParameter("cont", this.comanda)
                .getResultList();

        try {
            em.getTransaction().begin();

            for (Card card : carduriDeSters) {
                em.remove(card);
            }

            for (IstoricTranzactii tranzactie : tranzactiiDeSters) {
                em.remove(tranzactie);
            }

            // Șterge rândul din tabela "cont"
            this.comenzi.remove(this.comanda);
            if (this.em.contains(this.comanda)) {
                em.remove(this.comanda);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            if (!this.comenzi.isEmpty()) {
                this.comanda = this.comenzi.get(0);
            } else {
                this.comanda = null;
            }
            refreshForm();
        }
    }
}
