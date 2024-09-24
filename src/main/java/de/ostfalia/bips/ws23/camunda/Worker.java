package de.ostfalia.bips.ws23.camunda;

import de.ostfalia.bips.ws23.camunda.database.domain.*;
import de.ostfalia.bips.ws23.camunda.database.repository.*;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SpringBootApplication(scanBasePackages = "de.ostfalia.bips.ws23.camunda")
public class Worker {

    private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);

    public static void main(String[] args) {
        SpringApplication.run(Worker.class, args);
    }

    private final AntwortRepository antwortRepository;
    private final FragebogenHasFrageRepository fragebogenHasFrageRepository;
    private final FragebogenRepository fragebogenRepository;
    private final FrageRepository frageRepository;
    private final KategoriegewichtInProjektRepository kategoriegewichtInProjektRepository;
    private final KategorieRepository kategorieRepository;
    private final LieferantRepository lieferantRepository;
    private final ProjektHasAntwortRepository projektHasAntwortRepository;
    private final ProjektHasLieferantRepository projektHasLieferantRepository;
    private final ProjektHasLieferantHasAntwortRepository projektHasLieferantHasAntwortRepository;
    private final ProjektRepository projektRepository;

    @Autowired
    public Worker(AntwortRepository antwortRepository,
                  FragebogenHasFrageRepository fragebogenHasFrageRepository, FragebogenRepository fragebogenRepository,
                  FrageRepository frageRepository,
                  KategoriegewichtInProjektRepository kategoriegewichtInProjektRepository,
                  KategorieRepository kategorieRepository, LieferantRepository lieferantRepository,
                  ProjektHasAntwortRepository projektHasAntwortRepository,
                  ProjektHasLieferantRepository projektHasLieferantRepository,
                  ProjektHasLieferantHasAntwortRepository projektHasLieferantHasAntwortRepository,
                  ProjektRepository projektRepository) {

        this.antwortRepository = antwortRepository;
        this.fragebogenHasFrageRepository = fragebogenHasFrageRepository;
        this.fragebogenRepository = fragebogenRepository;
        this.frageRepository = frageRepository;
        this.kategoriegewichtInProjektRepository = kategoriegewichtInProjektRepository;
        this.kategorieRepository = kategorieRepository;
        this.lieferantRepository = lieferantRepository;
        this.projektHasAntwortRepository = projektHasAntwortRepository;
        this.projektHasLieferantRepository = projektHasLieferantRepository;
        this.projektHasLieferantHasAntwortRepository = projektHasLieferantHasAntwortRepository;
        this.projektRepository = projektRepository;
    }

    /**
     *Service Task: Projekte laden
     * @param job
     * @return HashMap (projekte)
     */
    @JobWorker(type = "projekteLaden")
    public Map<String, Object> ladeProjekte(final ActivatedJob job) {
        // Projekte aus Datenbank laden
        final List<Projekt> projekte = projektRepository.findAll();
        // Strukturkonvertierung der Projektliste zur Weitergabe
        List<Map<String, Object>> projekteSelectListe = projekte.stream().map(projekt -> {
            Map<String, Object> selectOption = new HashMap<>();
            int projektId = projekt.getId();
            selectOption.put("label", "(ID: " + projektId + ") Name: " + "\"" + projekt.getName()
                    + "\"" + " --- Beschreibung: " + "\"" + projekt.getIdKomponente() + "\"");
            selectOption.put("value", projektId);

            return selectOption;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();

        result.put("projekte", projekteSelectListe);

        return result;

    }

    /**
     * ServiceTask: Frageboegen laden
     * @param job
     * @return HashMap (frageboegen aus der Datenbank)
     */
    @JobWorker(type = "frageboegenLaden")
    public Map<String, Object> fragebogenLaden(final ActivatedJob job) {
        final List<Fragebogen> frageboegen = fragebogenRepository.findAll();
        // Strukturkonvertierung der Fragebogenliste zur Weitergabe
        List<Map<String, Object>> frageboegenListe = frageboegen.stream().map(fragebogen -> {
            Map<String, Object> frageboegenMap = new HashMap<>();
            frageboegenMap.put("label", fragebogen.getBeschreibung()); // beschreibung
            frageboegenMap.put("value", fragebogen.getId()); // fragebogenId

            return frageboegenMap;
        }).collect(Collectors.toList());

        // Map zur Verwendung als Ruckgabewert erstellen
        Map<String, Object> result = new HashMap<>();
        // Frageboegen als Prozessvariable setzen
        result.put("frageboegen", frageboegenListe);

        return result;
    }

    /**
     * ServiceTask: Daten zum Fragebogen laden
     * @param job
     * @return HashMap (Infos zum ausgewaehlten Fragebogen)
     */
    @JobWorker(type = "ladeDatenZumFragebogen")
    public Map<String, Object> ladeDatenZumFragebogen(final ActivatedJob job) {
        return datenZumFragebogenLaden(job.getVariablesAsMap(), job.getVariablesAsMap());
    }

    /**
     * ServiceTask: Projekt speichern
     * @param client
     * @param job
     * Projekt wird in der Datenbank gespeichert (neues Projekt)
     */
    @JobWorker(type = "neuesProjektSpeichern")
    public void projektSpeichern(final JobClient client, final ActivatedJob job) {

        // Variablen aus dem Prozess extrahieren
        final Map<String, Object> variables = job.getVariablesAsMap();

        // Projekt anlegen und speichern
        Projekt neuesProjekt = new Projekt();
        neuesProjekt.setName(variables.get("projektname_neuesprojekt").toString());
        neuesProjekt.setIdKomponente(variables.get("projektbeschreibung_neuesprojekt").toString());
        Integer fragebogenId = Integer.valueOf(variables.get("fragebogen_selection").toString());
        Optional<Fragebogen> fbOptional = fragebogenRepository.findById(fragebogenId);
        Fragebogen fb = fbOptional.orElseThrow(() ->
                new EntityNotFoundException("Fragebogen wurde nicht gefunden."));

        neuesProjekt.setIdFragebogen(fb);
        projektRepository.save(neuesProjekt);

        List<Integer> gesammelte_antworten_koKriterium = new ArrayList<>();

        // Alle Taglisten der Form durchlaufen
        for (String key: variables.keySet()) {

            if (key.startsWith("taglist_") && variables.get(key) != null) {

                String taglist_KO_Kriterien = variables.get(key).toString();
                String[] parts = taglist_KO_Kriterien.substring(1, taglist_KO_Kriterien.length() - 1)
                        .split(", ");

                Map<String, String> map = new HashMap<>();
                int i = 1;
                for (String part: parts) {

                    String[] key_value_pairs = part.split(", ");

                    for (String pair : key_value_pairs) {
                        String[] entry = pair.split(": ");
                        if (entry.length == 2) {
                            map.put(entry[0].trim() + i, entry[1].trim());
                        }
                        if (map.containsKey("idFrage" + i) && map.containsKey("idAntwort" + i)) {
                            // Ids und antworttext extrahieren
                            Integer idFrage = Integer.parseInt(map.get("idFrage" + i));
                            Integer idAntwort = Integer.parseInt(map.get("idAntwort" + i));
                            // Antwortobjekt erstellen
                            Antwort antwort = new Antwort();
                            AntwortId id = new AntwortId();
                            id.setIdAntwort(idAntwort);
                            id.setIdFrage(idFrage);
                            antwort.setId(id);
                            gesammelte_antworten_koKriterium.add(antwort.getId().getIdAntwort());
                            i++;
                        }
                    }
                }
            }
        }
        // Projekt_Has_Antwort Objekte erstellen und K.O.-Kriterien setzen

        // Alle IDs der Fragen, die sich im aktuellen Fragebogen befinden laden
        List<Integer> fragen_im_fragebogen = fragebogenHasFrageRepository.findFragenByFragebogenId(fragebogenId);

        // Alle Antworten anhand der FragenIDs laden
        List<Antwort> antworten_der_fragen = antwortRepository.findByFragenIds(fragen_im_fragebogen);

        int projektId = neuesProjekt.getId();

        //Fuer alle Antworten des Fragebogens wird geguckt...
        for (Antwort antwort: antworten_der_fragen) {

            ProjektHasAntwort projektHasAntwort = new ProjektHasAntwort();
            ProjektHasAntwortId pha_Id = new ProjektHasAntwortId();
            pha_Id.setIdProjekt(projektId);
            pha_Id.setIdAntwort(antwort.getId().getIdAntwort());
            pha_Id.setIdFrage(antwort.getId().getIdFrage());

            projektHasAntwort.setId(pha_Id);
            projektHasAntwort.setIdProjekt(neuesProjekt);
            projektHasAntwort.setAntwort(antwort);

            // befindet sich die Antwort in der Tagliste?
            if (gesammelte_antworten_koKriterium.contains(antwort.getId().getIdAntwort())) { // ja
                projektHasAntwort.setIstKoKriterium((byte) 1); // KO-Kriterium wird gesetzt
                projektHasAntwortRepository.save(projektHasAntwort);

            } else { // nein
                projektHasAntwort.setIstKoKriterium((byte) 0); // Antwort ist kein KO-Kriterium
                projektHasAntwortRepository.save(projektHasAntwort);

            }
            // Objekt wird gespeichert
            projektHasAntwortRepository.save(projektHasAntwort);
        }
        kategorienGewichten(variables, neuesProjekt);

        variables.put("ausgewaehltesProjekt", neuesProjekt);
        variables.put("ausgewaehltesprojekt_id", neuesProjekt.getId());
        variables.put("ausgewaehltesprojekt_name", neuesProjekt.getName());

        client.newCompleteCommand(job.getKey()).variables(variables).send().join();
    }

    /**
     * ServiceTask: Projekt speichern
     * @param client
     * @param job
     * Projekt wird in der Datenbank gespeichert (bestehendes Projekt)
     */
    @JobWorker(type = "bestehendesProjektSpeichern")
    public void speichereBestehendesProjekt(final JobClient client, final ActivatedJob job) {

        // Variablen aus dem Prozess extrahieren
        final Map<String, Object> variables = job.getVariablesAsMap();
        Integer projektId = Integer.parseInt(variables.get("ausgewaehltesprojekt_id").toString());

        Optional<Projekt> projektOptional = projektRepository.findById(projektId);
        Projekt projekt = projektOptional.orElseThrow(() ->
                new EntityNotFoundException("Projekt wurde nicht gefunden."));

        kategorienGewichten(variables, projekt);

        variables.put("ausgewaehltesProjekt", projekt);
        variables.put("ausgewaehltesprojekt_id", projektId);

        //berechneNeuesRanking(projektId, variables);

        client.newCompleteCommand(job.getKey()).variables(variables).send().join();
    }

    /**
     * ServiceTask: Daten zum Projekt laden
     * @param job
     * @return HashMap (Daten zu einem bestehenden Projekt aus der Datenbank laden)
     */
    @JobWorker(type = "ladeDatenZumProjekt")
    public Map<String, Object> ladeDatenZumProjekt(final ActivatedJob job) {

        final Map<String, Object> variables = job.getVariablesAsMap();
        Integer projektId = Integer.parseInt(variables.get("select_projektAuswahl").toString());

        Optional<Projekt> projektOptional = projektRepository.findById(projektId);
        Projekt projekt = projektOptional.orElseThrow(() ->
                new EntityNotFoundException("Projekt wurde nicht gefunden."));

        Fragebogen fragebogen = projekt.getIdFragebogen();

        // Liste von den IDs aller Fragen, die im Fragebogen vorkommen, laden
        List<Integer> fragenIds = fragebogenHasFrageRepository.findFragenByFragebogenId(fragebogen.getId());
        // Liste von Fragen anhand der fragenIds erstellen

        Map<String, Object> result = new HashMap<>();

        variables.put("fragebogen_selection", fragebogen.getId());
        datenZumFragebogenLaden(variables, result);

        result.put("fragebogen_selection", fragebogen.getId());
        result.put("ausgewaehltesProjekt", projekt);
        result.put("ausgewaehltesprojekt_id", projekt.getId());
        result.put("ausgewaehltesprojekt_name", projekt.getName());

        return result;
    }

    /**
     * ServiceTask: Lieferanten laden
     * @param job
     * @return HashMap (alle Lieferanten laden, die in der Datenbank vorhanden sind)
     */
    @JobWorker(type = "lieferantenLaden")
    public Map<String, Object> lieferantenLaden(final ActivatedJob job) {
        // alle Lieferanten aus der Datenbank laden
        List<Lieferant> lieferanten = lieferantRepository.findAll();

        // Strukturkonvertierung der Lieferantenliste zur Weitergabe
        List<Map<String, Object>> lieferantenListe = lieferanten.stream().map(lieferant -> {
            Map<String, Object> lieferantenMap = new HashMap<>();
            lieferantenMap.put("label", lieferant.getName()); // name
            lieferantenMap.put("value", lieferant.getId()); // lieferantId
            lieferantenMap.put("adresse", lieferant.getAdresse());

            return lieferantenMap;
        }).collect(Collectors.toList());

        // Map zur Verwendung als Rueckgabewert erstellen
        Map<String, Object> result = new HashMap<>();
        result.put("lieferanten", lieferantenListe);

        return result;
    }

    /**
     * ServiceTask: Lieferant ueberpruefen
     * @param job
     * @return HashMap (ob Lieferant bereits im Projekt vorhanden -> true or false as value)
     */
    @JobWorker(type = "lieferantUeberpruefen")
    public Map<String, Object> lieferantUeberpruefen(final ActivatedJob job) {

        final Map<String, Object> variables = job.getVariablesAsMap();

        Integer projektId = Integer.parseInt(variables.get("ausgewaehltesprojekt_id").toString());
        Integer lieferantId = Integer.parseInt(variables.get("lieferant_selection").toString());


        // Pruefen, ob der Lieferant bereits im Projekt ist
        boolean istLieferantImProjekt = projektHasLieferantRepository.
                existsByProjektIdAndLieferantId(projektId, lieferantId);

        // Map zur Verwendung als Rueckgabewert erstellen
        Map<String, Object> result = new HashMap<>();
        result.put("istLieferantImProjekt", istLieferantImProjekt);

        return result;
    }

    /**
     * ServiceTask: Daten vom Lieferant laden
     * @param job
     * @return HashMap (Daten eines Lieferanten, der bereits im Projekt vorhanden ist laden)
     */
    @JobWorker(type = "ladeDatenVomLieferant")
    public Map<String, Object> ladeDatenVonLieferanten(final ActivatedJob job) {

        final Map<String, Object> variables = job.getVariablesAsMap();

        int projektId = Integer.parseInt(variables.get("ausgewaehltesprojekt_id").toString());
        int lieferantId = Integer.parseInt(variables.get("lieferant_selection").toString());
        Integer fragebogenId = Integer.parseInt(variables.get("fragebogen_selection").toString());

        Optional<Lieferant> lieferantOptional = lieferantRepository.findById(lieferantId);
        Lieferant lieferant = lieferantOptional.orElseThrow(() ->
                new EntityNotFoundException("Lieferant wurde nicht gefunden."));

        // Map zur Verwendungs als Rueckgabewert erstellen
        Map<String, Object> result = scoreBerechnen(lieferant.getId(), projektId, true);

        List<ProjektHasLieferantHasAntwort> phlha_antworten_lieferant = projektHasLieferantHasAntwortRepository
                .findByIdProjektAndIdLieferant(projektId, lieferantId);

        List<ProjektHasLieferantHasAntwort> sorted_phlha_antworten_lieferant = phlha_antworten_lieferant
                .stream().sorted(Comparator.comparing(p -> p.getAntwort().getId().getIdFrage())).toList();

        int f_index = 0;
        for (ProjektHasLieferantHasAntwort phlha : sorted_phlha_antworten_lieferant) {
            int idFrage = phlha.getId().getIdFrage();
            int idAntwort = phlha.getId().getIdAntwort();
            AntwortId id = new AntwortId();
            id.setIdAntwort(idAntwort);
            id.setIdFrage(idFrage);
            Optional<Antwort> antwortOptional = antwortRepository.findById(id);
            Antwort antwort = antwortOptional.orElseThrow(() ->
                    new EntityNotFoundException("Antwort wurde nicht gefunden."));
            Optional<Frage> frageOptional = frageRepository.findById(idFrage);
            Frage frage = frageOptional.orElseThrow(() ->
                   new EntityNotFoundException("Frage wurde nicht gefunden."));

            int frage_index = f_index % 5 + 1; // Index der Frage ist immer zwischen 1-5.
            int kategorie = frage.getKategorie().getId();

            result.put("select_" + "k" + kategorie + "_f" + frage_index,
            "idFrage: " + idFrage +
            ", idAntwort: " + idAntwort +
            ", Antwort: " + antwort.getAntworttext());

            f_index++;
        }

        // Gesamtzahl der Fragen im Fragebogen ermitteln
        int gesamtzahlFragen = 0;
        gesamtzahlFragen = fragebogenHasFrageRepository.countByFragebogenId(fragebogenId);

        // Zaehlen, wie viele Antworten der Lieferant für diesen Fragebogen gegeben hat
        int antwortenLieferantCount = 0;
        antwortenLieferantCount = projektHasLieferantHasAntwortRepository.
                findBereitsBeantwortet(fragebogenId, projektId, lieferantId);

        // Ueberpruefen, ob Anzahl der Antworten der Gesamtzahl der Fragen entspricht
        boolean istVollstaendig = antwortenLieferantCount == gesamtzahlFragen;

        result.put("istVollstaendig", istVollstaendig);

        koKriterienPruefen(result, fragebogenId, projektId, lieferantId);

        result.put("benutzername", lieferant.getName());

        return result;
    }

    /**
     * ServiceTask: Lieferant zum Projekt hinzufuegen
     * @param job
     * @return HashMap (Benutzerdaten vom neu angelegten Lieferanten)
     */
    @JobWorker(type = "lieferantZumProjektHinzufuegen")
    public Map<String, Object> lieferantZumProjekthinzufuegen(final ActivatedJob job) {

        // Job-Variablen extrahieren
        final Map<String, Object> variables = job.getVariablesAsMap();

        Integer projektId = Integer.parseInt(variables.get("ausgewaehltesprojekt_id").toString());
        Optional<Projekt> projektOptional = projektRepository.findById(projektId);
        Projekt projekt = projektOptional.orElseThrow(() ->
                new EntityNotFoundException("Projekt wurde nicht gefunden."));

        Integer lieferantId = Integer.parseInt(variables.get("lieferant_selection").toString());
        Optional<Lieferant> lieferantOptional = lieferantRepository.findById(lieferantId);
        Lieferant lieferant = lieferantOptional.orElseThrow(() ->
                new EntityNotFoundException("Lieferant mit ID ->" + lieferantId + "<- wurde " +
                        "nicht gefunden"));

        // Lieferant zum Projekt hinzufuegen
        ProjektHasLieferant projektHasLieferant = new ProjektHasLieferant();
        ProjektHasLieferantId phl_id = new ProjektHasLieferantId();
        phl_id.setIdProjekt(projektId);
        phl_id.setIdLieferant(lieferantId);
        projektHasLieferant.setId(phl_id);
        projektHasLieferant.setIdProjekt(projekt);
        projektHasLieferant.setIdLieferant(lieferant);

        // In der Datenbank speichern
        projektHasLieferantRepository.save(projektHasLieferant);
        LOGGER.info("Lieferant mit der ID: ->" + lieferantId + "<- wurde dem Projekt mit der ID: ->" + projektId +
                "<- hinzugefuegt." );

        // Erstellen einer Map fuer Rueckgabewerte
        Map<String, Object> result = new HashMap<>();
        result.put("benutzername", lieferant.getName());
        result.put("passwort", lieferant.getPasswort());

        return result;
    }

    /**
     * SendTask: Zugaenge an Lieferanten senden
     * @param job
     */
    @JobWorker(type = "sendeZugaengeAnLieferant")
    public void sendeZugaengeAnLieferant(final ActivatedJob job) {
        // Job-Variablen extrahieren
        Integer lieferantId = Integer.valueOf(job.getVariablesAsMap().get("lieferant_selection").toString());
        Optional<Lieferant> lieferantOptional = lieferantRepository.findById(lieferantId);
        Lieferant lieferant = lieferantOptional.orElseThrow(() ->
                new EntityNotFoundException("Lieferant mit ID " + lieferantId + " wurde nicht gefunden"));

        LOGGER.info("Zugangsinformationen gesendet an neuen Lieferanten: " + lieferant.getName());
    }

    /**
     * ServiceTask: Pruefe auf Vollstaendigkeit
     * @param job
     * @return HashMap (Anzahl der beantwortetetn Fragen;
     * Antworten eines Lieferanten vollstaendig? -> true or false as value)
     */
    @JobWorker(type = "aufVollstaendigkeitPruefen")
    public Map<String, Object> aufVollstaendigkeitpruefen(final ActivatedJob job) {
        // Job-Variablen extrahieren
        final Map<String, Object> variables = job.getVariablesAsMap();
        Integer fragebogenId = Integer.parseInt(variables.get("fragebogen_selection").toString());
        Integer projektId = Integer.parseInt(variables.get("ausgewaehltesprojekt_id").toString());
        Integer lieferantId = Integer.parseInt(variables.get("lieferant_selection").toString());;

        // Gesamtzahl der Fragen im Fragebogen ermitteln
        int gesamtzahlFragen = 0;
        gesamtzahlFragen = fragebogenHasFrageRepository.countByFragebogenId(fragebogenId);
        LOGGER.info("Gesamtanzahl Fragen: " + gesamtzahlFragen);

        // Zaehlen, wie viele Antworten der Lieferant für diesen Fragebogen gegeben hat
        int antwortenLieferantCount = 0;
        antwortenLieferantCount = projektHasLieferantHasAntwortRepository.
                findBereitsBeantwortet(fragebogenId, projektId, lieferantId);
        LOGGER.info("Gesamtanzahl bereits beantwortet: " + antwortenLieferantCount);

        // Ueberpruefen, ob Anzahl der Antworten der Gesamtzahl der Fragen entspricht

        boolean istVollstaendig = antwortenLieferantCount == gesamtzahlFragen;

        // Rueckgabewert setzen
        Map<String, Object> result = new HashMap<>();
        result.put("bisher_beantwortete_fragen", antwortenLieferantCount);
        result.put("istVollstaendig", istVollstaendig);

        return result;
    }

    /**
     * SendTask: E-Mail senden (Lieferant ueber Unvollstaendigkeit informieren)
     * @param job
     */
    @JobWorker(type = "sendeE-Mail")
    public void sendeEmail(final ActivatedJob job) {

        final Map<String, Object> variables = job.getVariablesAsMap();
        Integer lieferantId = Integer.valueOf(variables.get("lieferant_selection").toString());

        LOGGER.info("Hinweis auf Unvollstaendigkeit" +
                " des Fragebogens gesendet an Lieferanten mit der ID: " +
                "->" + lieferantId + "<-.");
    }

    /**
     * ServiceTask: Anmeldedaten ueberpruefen
     * @param job
     * @return HashMap (gueltig? -> true or false as value)
     */
    @JobWorker(type = "ueberpruefeAnmeldedaten")
    public Map<String, Object> ueberpruefeAnmeldedaten(final ActivatedJob job) {

        // Job-Variablen extrahieren
        final Map<String, Object> variables = job.getVariablesAsMap();

        String name = variables.get("benutzername").toString(); // auf Textfield in der Form zugreifen
        String passwort = variables.get("passwort").toString(); // auf Textfield in der Form zugreifen

        // Lieferanten anhand des Namens aus der Datenbank holen
        Optional<Lieferant> lieferantOptional = lieferantRepository.findByName(name);
        Lieferant lieferant = lieferantOptional.orElseThrow(() ->
                new EntityNotFoundException("Lieferant wurde nicht gefunden."));

        // Ueberpruefen, ob der Lieferant gefunden wurde und das Passwort übereinstimmt
        boolean istGueltig = lieferant.getPasswort().equals(passwort);

        // Rueckgabewert setzen
        Map<String, Object> result = new HashMap<>();
        result.put("istGueltig", istGueltig);

        return result;
    }

    /**
     * ServiceTask: Antworten laden
     * @param job
     * @return HashMap (bereits beantwortete Fragen werden in einer Prozessvariablen gespeichert)
     */
    @JobWorker(type = "ladeAntworten")
    public Map<String, Object> antwortenLaden(final ActivatedJob job) {
        // Job-Variablen extrahieren
        final Map<String, Object> variables = job.getVariablesAsMap();
        Integer projektId = Integer.parseInt(variables.get("ausgewaehltesprojekt_id").toString());
        Integer lieferantId = Integer.parseInt(variables.get("lieferant_selection").toString());

        final List<ProjektHasAntwort> list_antworten = projektHasAntwortRepository.findByProjektId(projektId);

        final List<ProjektHasLieferantHasAntwort> phlhas = projektHasLieferantHasAntwortRepository
                .findByIdProjektAndIdLieferant(projektId, lieferantId);
        // Strukturkonvertierung der Projektliste zur Weitergabe
        List<Map<String, Object>> antworten_lieferant_liste = phlhas.stream().map(phlha -> {
            Map<String, Object> map_antworten = new HashMap<>();
            AntwortId id = new AntwortId();
            id.setIdAntwort(phlha.getId().getIdAntwort());
            id.setIdFrage(phlha.getId().getIdFrage());

            Optional<Antwort> antwortOptional = antwortRepository.findById(id);
            Antwort antwort = antwortOptional.orElseThrow(() ->
                    new EntityNotFoundException("Antwort wurde nicht gefunden."));

            Optional<Frage> frageOptional = frageRepository.findById(antwort.getId().getIdFrage());
            Frage frage = frageOptional.orElseThrow(() ->
                    new EntityNotFoundException("Frage wurde nicht gefunden"));

            map_antworten.put("label", "Antwort zu Frage " + frage.getId());
            map_antworten.put("value", antwort.getId());

            return map_antworten;
        }).collect(Collectors.toList());

        // Ergebnis als Rueckgabewert speichern
        Map<String, Object> result = new HashMap<>();
        result.put("geladene_antworten", antworten_lieferant_liste);

        return result;
    }

    /**
     * ServiceTask: Antworten speichern
     * @param job
     */
    @JobWorker(type = "speichereAntworten")
    public void speichereAntworten(final ActivatedJob job) {

        final Map<String, Object> variables = job.getVariablesAsMap();

        Integer fragebogenId = Integer.parseInt(variables.get("fragebogen_selection").toString());
        Integer projektId = Integer.parseInt(variables.get("ausgewaehltesprojekt_id").toString());
        Integer lieferantId = Integer.valueOf(variables.get("lieferant_selection").toString());

        List<Antwort> antworten = new ArrayList<>();
        List<Integer> fragen = fragebogenHasFrageRepository.findFragenByFragebogenId(fragebogenId);

        // Alle Taglisten der Form durchlaufen
        for (String key: variables.keySet()) {
            if (key.startsWith("select_k")) {

                Object value = variables.get(key);
                if (value != null) {
                    String select_antwort_Json = variables.get(key).toString();
                    String regex_idAntwort = "idAntwort: (\\d+)";
                    String regex_idFrage = "idFrage: (\\d+)";

                    Pattern pattern_idAntwort = Pattern.compile(regex_idAntwort);
                    Pattern pattern_idFrage = Pattern.compile(regex_idFrage);

                    Matcher matcher_idAntwort = pattern_idAntwort.matcher(select_antwort_Json);
                    Matcher matcher_idFrage = pattern_idFrage.matcher(select_antwort_Json);

                    if (matcher_idAntwort.find() && matcher_idFrage.find()) {
                        Integer antwortId = Integer.parseInt(matcher_idAntwort.group(1));
                        Integer frageId = Integer.parseInt(matcher_idFrage.group(1));
                        AntwortId id = new AntwortId();
                        id.setIdAntwort(antwortId);
                        id.setIdFrage(frageId);
                        Optional<Antwort> optionalAntwort = antwortRepository.findById(id);
                        Antwort antwort = optionalAntwort.orElseThrow(() ->
                                new EntityNotFoundException("Antwort wurde nicht gefunden."));
                        antworten.add(antwort);

                    } else {
                        System.err.println("idAntwort wurde nicht gefunden.");
                    }
                }
            }
        }

        for (Integer frage : fragen) {
            for (Antwort antwort : antworten) {

                if (frage == antwort.getFrage().getId()) {
                    ProjektHasLieferantHasAntwortId id = new ProjektHasLieferantHasAntwortId();
                    id.setIdProjekt(projektId);
                    id.setIdLieferant(lieferantId);
                    id.setIdAntwort(antwort.getId().getIdAntwort());
                    id.setIdFrage(antwort.getId().getIdFrage());

                    ProjektHasLieferantHasAntwort projektHasLieferantHasAntwort = new ProjektHasLieferantHasAntwort();
                    projektHasLieferantHasAntwort.setId(id);

                    projektHasLieferantHasAntwortRepository.save(projektHasLieferantHasAntwort);
                }
            }
        }
    }

    /**
     * ServiceTask: Durchfuehrung und Speicherung einer Nutzwertanalyse
     * @param job
     * @return HashMap (alle relevanten Werte der Nutzwertanalyse)
     */
    @JobWorker(type = "nutzwertanalyseDurchfuehren")
    public Map<String, Object> nutzwertanalyseDurchfuehren(final ActivatedJob job) {
        // Job-Variablen extrahieren
        final Map<String, Object> variables = job.getVariablesAsMap();
        Integer lieferantId = Integer.parseInt(variables.get("lieferant_selection").toString());
        Integer projektId = Integer.parseInt(variables.get("ausgewaehltesprojekt_id").toString());
        Integer fragebogenId = Integer.parseInt(variables.get("fragebogen_selection").toString());

        Optional<Lieferant> lieferantOptional = lieferantRepository.findById(lieferantId);
        Lieferant lieferant = lieferantOptional.orElseThrow(() ->
                new EntityNotFoundException("Lieferant wurde nicht gefunden."));

        Map<String, Object> result = scoreBerechnen(lieferant.getId(), projektId, true);

        koKriterienPruefen(result, fragebogenId, projektId, lieferantId);

        float score = Float.parseFloat(result.get("score_supplier").toString());



        speichereRank(projektId, lieferantId, score);

        berechneNeuesRanking(projektId);

        StringBuilder sb = new StringBuilder();
        List<ProjektHasLieferant> lieferanten = projektHasLieferantRepository.findAllLieferantenByProjektId(projektId);
        lieferanten.sort(Comparator.comparing(ProjektHasLieferant::getRank));
        int i = 1;
        for (ProjektHasLieferant phl_lieferant : lieferanten) {
            Lieferant lieferant_tmp = lieferantRepository.findById(phl_lieferant
                            .getId().getIdLieferant()).get();
            sb.append("Rank " + i++ + ": " + "\"" + lieferant_tmp.getName() + "\"" + " ---> Score: " +
                    phl_lieferant.getScore() + "\n");

        }
        result.put("ranking_liste", sb);

        return result;
    }

    /**
     * SendTask: Benachrichtigung: "neues Ranking liegt vor"
     * @param job
     */
    @JobWorker(type = "sendNewRankingMessage")
    public void sendNewRankingMessage(final ActivatedJob job) {
        final Map<String, Object> variables = job.getVariablesAsMap();
        Integer lieferantId = Integer.parseInt(variables.get("lieferant_selection").toString());

        LOGGER.info("Es liegt ein neues Ranking für den Lieferanten mit der ID: " +
                "->" + lieferantId +"<- vor.");
    }

    /*
    Methode fuer die Berechnung der Summe der Maxima
     */
    public Float calcSumOfMaxPoints(Integer idKategorie, Integer idProjekt) {
        List<Object[]> maxPointsForEachQuestion = projektHasAntwortRepository
                .findMaxPointsForEachQuestion(idKategorie, idProjekt);

        float sumOfMaxPoints = 0.0f;
        for (Object[] result : maxPointsForEachQuestion) {
            if (result[1] != null) {
                float maxPoints = ((Number) result[1]).floatValue();
                sumOfMaxPoints += maxPoints;
            }
        }
        return sumOfMaxPoints / maxPointsForEachQuestion.size();
    }

    /*
     Methode, um eine gegebene Antwort auf KO-Kriterium zu ueberpruefen
     */
    public boolean istKoKriterium(Integer idProjekt, Integer idLieferant, Integer idAntwort) {

        Optional<Byte> istKoKriterium = projektHasLieferantHasAntwortRepository
                .istAntwortKoKriterium(idProjekt, idLieferant, idAntwort);
        return istKoKriterium.isPresent() && istKoKriterium.get() == 1;
    }

    /*
     Methode zur Berechnung relevanter Kennzahlen fuer die Nutzwertanalyse
     */
    public Map<String, Object> scoreBerechnen(int lieferantId, int projektId, boolean saveAll){
        //int lieferantId = lieferant.getId();
        Optional<Lieferant> lieferantOptional = lieferantRepository.findById(lieferantId);
        Lieferant lieferant = lieferantOptional.orElseThrow(() ->
                new EntityNotFoundException("Lieferant wurde nicht gefunden."));

        Map<String, Object>  result = new HashMap<>();

        BigDecimal mw_flexibilitaet = BigDecimal.valueOf(projektHasLieferantHasAntwortRepository
                .findAVGByCategory(lieferantId, projektId, 1)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal mw_zeit = BigDecimal.valueOf(projektHasLieferantHasAntwortRepository
                .findAVGByCategory(lieferantId, projektId, 2)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal mw_qualitaet = BigDecimal.valueOf(projektHasLieferantHasAntwortRepository
                .findAVGByCategory(lieferantId, projektId, 3)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal mw_informationsfaehigkeit = BigDecimal.valueOf(projektHasLieferantHasAntwortRepository
                .findAVGByCategory(lieferantId, projektId, 4)).setScale(2, RoundingMode.HALF_UP);

        /*
        Anzahl aller Lieferanten im Projekt berechnen
         */

        int anzahl_lieferanten_projekt = projektHasLieferantRepository.countLieferantenImProjekt(projektId);

        /*
        Scores der einzelnen Kategorien berechnen (gewichtet)
         */
        KategoriegewichtInProjektId id_flexibilitaet = new KategoriegewichtInProjektId();
        id_flexibilitaet.setIdKategorie(1);
        id_flexibilitaet.setIdProjekt(projektId);
        Optional<KategoriegewichtInProjekt> kategoriegewichtung_flexibilitaet_optional =
                kategoriegewichtInProjektRepository.findById(id_flexibilitaet);
        KategoriegewichtInProjekt kategoriegewichtung_flexibilitaet = kategoriegewichtung_flexibilitaet_optional
                .orElseThrow(() -> new EntityNotFoundException("Kategorie wurde nicht gefunden."));

        BigDecimal score_flexibilitaet = BigDecimal.valueOf(projektHasLieferantHasAntwortRepository
                .findSumOfPointsByCategory(lieferantId, projektId, 1))
                .multiply(BigDecimal.valueOf(kategoriegewichtung_flexibilitaet.getGewicht()/100))
                .setScale(2, RoundingMode.HALF_UP);

        KategoriegewichtInProjektId id_zeit = new KategoriegewichtInProjektId();
        id_zeit.setIdKategorie(2);
        id_zeit.setIdProjekt(projektId);
        Optional<KategoriegewichtInProjekt> kategoriegewichtung_zeit_optional =
                kategoriegewichtInProjektRepository.findById(id_zeit);
        KategoriegewichtInProjekt kategoriegewichtung_zeit = kategoriegewichtung_zeit_optional
                .orElseThrow(() -> new EntityNotFoundException("Kategorie wurde nicht gefunden."));


        BigDecimal score_zeit = BigDecimal.valueOf(projektHasLieferantHasAntwortRepository
                        .findSumOfPointsByCategory(lieferantId, projektId, 2))
                .multiply(BigDecimal.valueOf(kategoriegewichtung_zeit.getGewicht()/100))
                .setScale(2, RoundingMode.HALF_UP);

        KategoriegewichtInProjektId id_qualitaet = new KategoriegewichtInProjektId();
        id_qualitaet.setIdKategorie(3);
        id_qualitaet.setIdProjekt(projektId);
        Optional<KategoriegewichtInProjekt> kategoriegewichtung_qualitaet_optional =
                kategoriegewichtInProjektRepository.findById(id_qualitaet);
        KategoriegewichtInProjekt kategoriegewichtung_qualitaet = kategoriegewichtung_qualitaet_optional
                .orElseThrow(() -> new EntityNotFoundException("Kategorie wurde nicht gefunden."));

        BigDecimal score_qualitaet = BigDecimal.valueOf(projektHasLieferantHasAntwortRepository
                        .findSumOfPointsByCategory(lieferantId, projektId, 3))
                .multiply(BigDecimal.valueOf(kategoriegewichtung_qualitaet.getGewicht()/100))
                .setScale(2, RoundingMode.HALF_UP);

        KategoriegewichtInProjektId id_informationsfaehigkeit = new KategoriegewichtInProjektId();
        id_informationsfaehigkeit.setIdKategorie(4);
        id_informationsfaehigkeit.setIdProjekt(projektId);
        Optional<KategoriegewichtInProjekt> kategoriegewichtung_informationsfaehigkeit_optional =
                kategoriegewichtInProjektRepository.findById(id_informationsfaehigkeit);
        KategoriegewichtInProjekt kategoriegewichtung_informationsfaehigkeit =
                kategoriegewichtung_informationsfaehigkeit_optional.orElseThrow(() ->
                        new EntityNotFoundException("Kategorie wurde nicht gefunden."));

        BigDecimal score_informationsfaehigkeit = BigDecimal.valueOf(projektHasLieferantHasAntwortRepository
                        .findSumOfPointsByCategory(lieferantId, projektId, 4))
                .multiply(BigDecimal.valueOf(kategoriegewichtung_informationsfaehigkeit.getGewicht()/100))
                .setScale(2, RoundingMode.HALF_UP);
        System.out.println(""+ lieferantId + " : " + score_informationsfaehigkeit);


        /*
        Gesamtscore berechnen
         */
        BigDecimal sum_scores = score_flexibilitaet.add(score_zeit)
                .add(score_qualitaet).add(score_informationsfaehigkeit);
        float score = sum_scores.floatValue();
        System.out.println(sum_scores);

        BigDecimal max_score_flexibilitaet = BigDecimal.valueOf(calcSumOfMaxPoints(1, projektId) *
                (kategoriegewichtung_flexibilitaet.getGewicht()/100))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal max_score_zeit = BigDecimal.valueOf(calcSumOfMaxPoints(2, projektId) *
                (kategoriegewichtung_zeit.getGewicht()/100))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal max_score_qualitaet = BigDecimal.valueOf(calcSumOfMaxPoints(3, projektId) *
                (kategoriegewichtung_qualitaet.getGewicht()/100))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal max_score_informationsfaehigkeit = BigDecimal.valueOf(calcSumOfMaxPoints(4, projektId) *
                (kategoriegewichtung_informationsfaehigkeit.getGewicht()/100))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal sum_max_scores = max_score_flexibilitaet.add(max_score_zeit).add(max_score_qualitaet)
                .add(max_score_informationsfaehigkeit).setScale(2, RoundingMode.HALF_UP);
        float max_score = sum_max_scores.floatValue();
        //int rank = projektHasLieferantRepository.findRank(projektId,lieferantId);

        result.put("score_tmp", score);

        if(saveAll) {

            result.put("score_supplier", score);
            result.put("maxScore", max_score);
            result.put("ranking_supplier", projektHasLieferantRepository.findRank(projektId, lieferantId));
            result.put("allSuppliers", anzahl_lieferanten_projekt);


            result.put("score_flexibilitaet", score_flexibilitaet);
            result.put("maxScore_flexibilitaet", max_score_flexibilitaet);
            result.put("mittelwert_flexibilitaet", mw_flexibilitaet);

            result.put("score_zeit", score_zeit);
            result.put("maxScore_zeit", max_score_zeit);
            result.put("mittelwert_zeit", mw_zeit);

            result.put("score_qualitaet", score_qualitaet);
            result.put("maxScore_qualitaet", max_score_qualitaet);
            result.put("mittelwert_qualitaet", mw_qualitaet);

            result.put("score_informationsfaehigkeit", score_informationsfaehigkeit);
            result.put("maxScore_informationsfaehigkeit", max_score_informationsfaehigkeit);
            result.put("mittelwert_informationsfaehigkeit", mw_informationsfaehigkeit);

            result.put("benutzername", lieferant.getName());
        }

        return result;
    }

    /*
    Methode zur Gewichtung der Kategorien
     */
    public void kategorienGewichten(Map<String, Object> variables, Projekt projekt){
        int projektId = projekt.getId();

        Optional<Kategorie> flexibilitaetOptional = kategorieRepository.findById(1);
        Kategorie flexibilitaet = flexibilitaetOptional.orElseThrow(() ->
                new EntityNotFoundException("Kategorie wurde nicht gefunden."));

        Optional<Kategorie> zeitOptional = kategorieRepository.findById(2);
        Kategorie zeit = zeitOptional.orElseThrow(() ->
                new EntityNotFoundException("Kategorie wurde nicht gefunden."));

        Optional<Kategorie> qualitaetOptional = kategorieRepository.findById(3);
        Kategorie quailitaet = qualitaetOptional.orElseThrow(() ->
                new EntityNotFoundException("Kategorie wurde nicht gefunden."));

        Optional<Kategorie> informationsfaehigkeitOptional = kategorieRepository.findById(4);
        Kategorie informationsfaehigkeit = informationsfaehigkeitOptional.orElseThrow(() ->
                new EntityNotFoundException("Kategorie wurde nicht gefunden."));

        // Einzelne Kategorien gewichten
        KategoriegewichtInProjekt kategoriegewicht_flexibilitaet = KategoriegewichtInProjekt
                .erstelleKategoriegewicht(flexibilitaet, projektId, Float.parseFloat(variables
                        .get("kategoriegewichtung_flexibilitaet").toString()));

        KategoriegewichtInProjekt kategoriegewicht_zeit = KategoriegewichtInProjekt.erstelleKategoriegewicht
                (zeit, projektId, Float.parseFloat(variables
                        .get("kategoriegewichtung_zeit").toString()));

        KategoriegewichtInProjekt kategoriegewicht_qualitaet = KategoriegewichtInProjekt.erstelleKategoriegewicht
                (quailitaet, projektId, Float.parseFloat(variables
                        .get("kategoriegewichtung_qualitaet").toString()));

        KategoriegewichtInProjekt kategoriegewicht_informationsfeahigkeit = KategoriegewichtInProjekt
                .erstelleKategoriegewicht(informationsfaehigkeit, projektId, Float.parseFloat(variables
                        .get("kategoriegewichtung_informationsfaehigkeit").toString()));

        kategoriegewichtInProjektRepository.save(kategoriegewicht_flexibilitaet);
        kategoriegewichtInProjektRepository.save(kategoriegewicht_zeit);
        kategoriegewichtInProjektRepository.save(kategoriegewicht_qualitaet);
        kategoriegewichtInProjektRepository.save(kategoriegewicht_informationsfeahigkeit);
    }

    /*
    Methode, um ueber alle Fragen des Fragebogens zu iterieren und auf KO-Kriterium zu ueberpruefen
    (Antworten des ausgewaehlten Lieferanten werden verglichen)
     */
    public void koKriterienPruefen(Map<String, Object> result, int fragebogenId, int projektId, int lieferantId) {
        List<Integer> fragen = fragebogenHasFrageRepository.findFragenByFragebogenId(fragebogenId);

        for (Integer frage : fragen) {

            boolean is_ko_kriterium = istKoKriterium(projektId, lieferantId, projektHasLieferantHasAntwortRepository
                    .findAntwortIdByFrageId(projektId, lieferantId, frage));
            result.put("frage_" + (frage) + "_is_ko_kriterium", is_ko_kriterium);
        }
    }

    /*
    Methode zum Speichern relevanter Daten des Fragebogens in einer Prozessvariablen
     */
    public Map<String, Object> datenZumFragebogenLaden(Map<String, Object> variables, Map<String, Object> result){
        // Extrahieren (ID des ausgewaehlten Fragebogens)
        final Integer fragebogenId = Integer.parseInt(variables.get("fragebogen_selection").toString());
        // Liste von den IDs aller Fragen, die im Fragebogen vorkommen, laden
        List<Integer> fragenIds = fragebogenHasFrageRepository.findFragenByFragebogenId(fragebogenId);
        // Liste von Fragen anhand der fragenIds erstellen
        List<Frage> fragen = frageRepository.findAllById(fragenIds);

        // Strukturkonvertierung der Fragenliste zur Weitergabe
        List<Map<String, Object>> fragenListe = fragen.stream().map(frage -> {
            Map<String, Object> fragenMap = new HashMap<>();
            fragenMap.put("fragetext", frage.getFragetext()); // fragetext
            fragenMap.put("id_frage", String.valueOf(frage.getId())); // frageId

            // Moegliche Antworten fuer jede Frage abrufen
            List<Antwort> antworten = antwortRepository.findByFrageId(frage.getId());
            List<Map<String, Object>> antwortenListe = antworten.stream().map(antwort -> {
                Map<String, Object> antwortenMap = new HashMap<>();
                antwortenMap.put("antworttext", antwort.getAntworttext()); // antworttext
                antwortenMap.put("id_antwort", String.valueOf(antwort.getId().getIdAntwort())); // antwortId

                return antwortenMap;
            }).collect(Collectors.toList());

            fragenMap.put("antworten", antwortenListe);

            return fragenMap;
        }).collect(Collectors.toList());

        // Antwortenliste zu jeder Frage erstellen
        List<List<String>> alle_fragen_mit_antworten = fragen.stream().map(frage -> {
            List<Antwort> antworten = antwortRepository.findByFrageId(frage.getId());
            return antworten.stream().map(antwort -> "idFrage: " + antwort.getId().getIdFrage() + ", "
                    +  "idAntwort: " + antwort.getId().getIdAntwort() + ", "
                    + " " + "Antwort: " + antwort.getAntworttext()).collect(Collectors.toList());
        }).toList();

        for (int i = 0; i < alle_fragen_mit_antworten.size(); i++) {
            List<String> antworten = alle_fragen_mit_antworten.get(i);
            result.put("antworten_Frage_" + (i + 1), antworten);
        }
        result.put("datenZumFragebogen", fragenListe);

        return result;
    }

    /*
    Methode zum Speichern des Ranks eines Lieferanten
     */
    public void speichereRank(int projektId, int lieferantId, Float score) {
        ProjektHasLieferantId id = new ProjektHasLieferantId();
        id.setIdProjekt(projektId);
        id.setIdLieferant(lieferantId);
        ProjektHasLieferant projektHasLieferant = new ProjektHasLieferant();
        projektHasLieferant.setId(id);
        projektHasLieferantRepository.save(projektHasLieferant);
        projektHasLieferant.setScore(score);
        projektHasLieferantRepository.save(projektHasLieferant);
        int rank = projektHasLieferantRepository.findRank(projektId,lieferantId);
        projektHasLieferant.setRank(rank);
        projektHasLieferantRepository.save(projektHasLieferant);
    }

    public void berechneNeuesRanking(int idProjekt) {

        List<ProjektHasLieferant> phl = projektHasLieferantRepository.findAllLieferantenByProjektId(idProjekt);
        for (ProjektHasLieferant lieferant : phl) {

            ProjektHasLieferant projektHasLieferant = new ProjektHasLieferant();
            projektHasLieferant.setId(lieferant.getId());

            Map<String, Object> map = scoreBerechnen(lieferant.getId().getIdLieferant(), idProjekt, false);

            float sc = Float.parseFloat(map.get("score_tmp").toString());
            System.out.println(lieferant.getId().getIdLieferant());
            System.out.println(sc);
            projektHasLieferant.setScore(sc);
            projektHasLieferantRepository.save(projektHasLieferant);


        }
        phl = projektHasLieferantRepository.findAllLieferantenByProjektId(idProjekt);
        for (ProjektHasLieferant lieferant : phl) {

            speichereRank(idProjekt, lieferant.getId().getIdLieferant(), lieferant.getScore());
        }
    }
}



