package Controllers;

import entite.Structure;
import entite.UnavailableStructure;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.event.Observes;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DualListModel;
import utils.LCUtil;

@Named("groundC")
@ViewScoped
public class GroundConditionController implements Serializable {

    private static final long serialVersionUID = 1L;

    static final List<String> MASTER_ITEMS = List.of(
        "Parcours 1-9",
        "Parcours 10-18",
        "Golf cars",
        "Chariot manuel",
        "Chariot électrique",
        "Chariot avec roues d'hiver",
        "Preferred lies",
        "Practice",
        "Practice sur herbe",
        "Putting green",
        "Approach green",
        "Driving range",
        "Winter greens",
        "Lessons"
    );

    @Inject private read.ReadUnavailableStructure readUnavailableStructure;
    @Inject private update.UpdateClubStructure    updateClubStructure;
    @Inject private context.ApplicationContext    appContext;

    // ── Wizard / Update state ─────────────────────────────────────────────────

    private Integer               workClubId        = null;
    private String                newCustomItemInput = "";
    private UnavailableStructure  groundCondition   = new UnavailableStructure();
    private DualListModel<String> pickList          = new DualListModel<>(new ArrayList<>(), new ArrayList<>());
    private Map<String, Boolean>  itemStatus        = new LinkedHashMap<>();
    private List<String>          customItemsBuffer = new ArrayList<>();
    private List<String>          itemsToPersist    = null;
    private boolean               showPreview       = false;

    // ── Display state (widget welcome page) ───────────────────────────────────

    private List<String>  openItems         = null;
    private List<String>  closedItems       = null;
    private LocalDateTime displayLastUpdate = null;
    private Integer       loadedForClubId   = null;

    public GroundConditionController() { }

    // ── Lifecycle : wizard ────────────────────────────────────────────────────

    @PostConstruct
    public void initWizard() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        workClubId        = null;
        newCustomItemInput = "";
        groundCondition   = new UnavailableStructure();
        pickList          = new DualListModel<>(new ArrayList<>(), new ArrayList<>());
        itemStatus        = new LinkedHashMap<>();
        customItemsBuffer = new ArrayList<>();
        itemsToPersist    = null;
        showPreview       = false;
    } // end method

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        initWizard();
    } // end method

    public String onFlow(FlowEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if ("ConfirmTab".equals(event.getNewStep())) itemsToPersist = null;
        return event.getNewStep();
    } // end method

    // ── Lifecycle : update page ───────────────────────────────────────────────

    public void initUpdate() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        groundCondition = new UnavailableStructure();
        itemStatus      = new LinkedHashMap<>();
        if (workClubId != null) loadItemStatus();
    } // end method

    // ── Ajax handlers ─────────────────────────────────────────────────────────

    public void onClubChange() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        groundCondition = new UnavailableStructure();
        itemStatus.clear();
        if (workClubId == null) return;
        loadItems();
    } // end method

    public void onClubChangeUpdate() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        groundCondition = new UnavailableStructure();
        itemStatus      = new LinkedHashMap<>();
        showPreview     = false;
        if (workClubId == null) return;
        loadItemStatus();
    } // end method

    // ── Custom items — wizard ─────────────────────────────────────────────────

    public void addCustomItem() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String trimmed = (newCustomItemInput == null) ? "" : newCustomItemInput.trim();
        if (trimmed.isEmpty()) return;

        boolean duplicate = MASTER_ITEMS.stream().anyMatch(m -> m.equalsIgnoreCase(trimmed))
                         || pickList.getSource().stream().anyMatch(s -> s.equalsIgnoreCase(trimmed))
                         || pickList.getTarget().stream().anyMatch(s -> s.equalsIgnoreCase(trimmed))
                         || customItemsBuffer.stream().anyMatch(c -> c.equalsIgnoreCase(trimmed));
        if (duplicate) {
            LCUtil.showMessageWarn("Item déjà existant", trimmed);
            newCustomItemInput = "";
            return;
        }
        customItemsBuffer.add(trimmed);
        itemsToPersist = null;
        newCustomItemInput = "";
        LOG.debug("custom item added to buffer: {}", trimmed);
    } // end method

    public void toggleItemStatus(String item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Boolean current = itemStatus.get(item);
        if (current != null) {
            itemStatus.put(item, !current);
            LOG.debug("toggled {} → {}", item, !current);
        }
    } // end method

    public void removeCustomItem(String item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        customItemsBuffer.remove(item);
        itemsToPersist = null;
        LOG.debug("removed custom item: {}", item);
    } // end method

    public void removeFromPersist(String item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        getItemsToPersist().remove(item);
        LOG.debug("removed from persist list: {}", item);
    } // end method

    public void onConfirmRowEdit(RowEditEvent<?> event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("confirm row edited, list size = {}", itemsToPersist == null ? 0 : itemsToPersist.size());
    } // end method


    // ── Item loading — wizard (DualListModel) ─────────────────────────────────

    private void loadItems() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        List<String> source = pickList.getSource();
        List<String> target = pickList.getTarget();
        source.clear();
        target.clear();
        source.addAll(MASTER_ITEMS);

        try {
            UnavailableStructure existing = readUnavailableStructure.readSilent(clubById(workClubId));
            if (existing != null) {
                Set<String> unavailableItems = buildUnavailableSet(existing);
                if (!unavailableItems.isEmpty()) {
                    source.clear();
                    target.clear();
                    for (String item : MASTER_ITEMS) {
                        if (unavailableItems.contains(item)) target.add(item);
                        else                                 source.add(item);
                    }
                    copyMeta(existing);
                    LOG.debug("loaded {} unavailable items from DB", unavailableItems.size());
                }
                customItemsBuffer.clear();
                Set<String> masterSet = new LinkedHashSet<>(MASTER_ITEMS);
                for (Structure s : existing.getStructureList())
                    if (!masterSet.contains(s.getItem())) customItemsBuffer.add(s.getItem());
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
        }
    } // end method

    // ── Item loading — update page (Map<String, Boolean>) ────────────────────

    private void loadItemStatus() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        itemStatus = new LinkedHashMap<>();

        try {
            UnavailableStructure existing = readUnavailableStructure.readSilent(clubById(workClubId));
            if (existing != null) {
                for (Structure s : existing.getStructureList())
                    itemStatus.put(s.getItem(), !Boolean.FALSE.equals(s.getStatus()));
                copyMeta(existing);
                LOG.debug("loaded {} items from GroundCondition", itemStatus.size());
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
        }
    } // end method

    // ── Save — wizard ─────────────────────────────────────────────────────────

    public void saveGroundCondition() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("source = {}", pickList.getSource());
        LOG.debug("target = {}", pickList.getTarget());
        try {
            reconstructFromItemsToPersist();
            persist();
            refresh();
            LCUtil.showMessageInfo("État du terrain enregistré");
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ── Save — update page ────────────────────────────────────────────────────

    public void saveGroundConditionUpdate() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            reconstructFromItemStatus();
            persist();
            refresh();
            LCUtil.showMessageInfo("État du terrain mis à jour");
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ── Reconstruct helpers ───────────────────────────────────────────────────

    private void reconstructFromItemsToPersist() {
        groundCondition.getStructureList().clear();
        for (String item : getItemsToPersist())
            groundCondition.getStructureList().add(makeItem(item, true));
    } // end method

    private void reconstructFromItemStatus() {
        groundCondition.getStructureList().clear();
        for (Map.Entry<String, Boolean> entry : itemStatus.entrySet())
            groundCondition.getStructureList().add(makeItem(entry.getKey(), entry.getValue()));
    } // end method

    private void persist() throws Exception {
        groundCondition.setLastUpdate(LocalDateTime.now());
        updateClubStructure.update(clubById(workClubId), groundCondition);
    } // end method

    // ── Display — widget welcome page ─────────────────────────────────────────

    private void ensureLoaded() {
        Integer clubId = (workClubId != null) ? workClubId
                       : (appContext.getClub() != null) ? appContext.getClub().getIdclub()
                       : null;
        if (clubId == null)                 { resetDisplayEmpty(); return; }
        if (clubId.equals(loadedForClubId)) return;
        LOG.debug("ensureLoaded for club {}", clubId);
        loadDisplay(clubId);
    } // end method

    private void loadDisplay(Integer clubId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        openItems         = new ArrayList<>();
        closedItems       = new ArrayList<>();
        displayLastUpdate = null;
        loadedForClubId   = clubId;

        try {
            UnavailableStructure us = readUnavailableStructure.readSilent(clubById(clubId));
            if (us != null && !us.getStructureList().isEmpty()) {
                for (Structure s : us.getStructureList()) {
                    if (Boolean.FALSE.equals(s.getStatus())) closedItems.add(s.getItem());
                    else                                      openItems.add(s.getItem());
                }
                displayLastUpdate = us.getLastUpdate();
                LOG.debug("loaded {} open, {} closed items", openItems.size(), closedItems.size());
            }
        } catch (SQLException e) {
            LOG.warn("Cannot load ground condition display for club {}: {}", clubId, e.getMessage());
        }
    } // end method

    private void resetDisplayEmpty() {
        openItems         = new ArrayList<>();
        closedItems       = new ArrayList<>();
        displayLastUpdate = null;
        loadedForClubId   = null;
    } // end method

    public void refresh() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        loadedForClubId = null;
    } // end method

    // ── Private helpers ───────────────────────────────────────────────────────

    private Set<String> buildUnavailableSet(UnavailableStructure us) {
        Set<String> unavailable = new LinkedHashSet<>();
        for (Structure s : us.getStructureList()) {
            unavailable.add(s.getItem());
        }
        return unavailable;
    } // end method

    private void copyMeta(UnavailableStructure us) {
        if (us.getLastUpdate() != null) groundCondition.setLastUpdate(us.getLastUpdate());
        if (us.getComment()    != null) groundCondition.setComment(us.getComment());
    } // end method

    private Structure makeItem(String item, boolean status) {
        Structure s = new Structure();
        s.setCourseId(0);
        s.setItem(item);
        s.setStatus(status);
        return s;
    } // end method

    private static entite.Club clubById(Integer clubId) {
        entite.Club club = new entite.Club();
        club.setIdclub(clubId);
        return club;
    } // end method

    // ── Getters / Setters — wizard / update ───────────────────────────────────

    public Integer getWorkClubId() {
        LOG.debug("getWorkClubId() = {}", workClubId);
        return workClubId;
    }
    public void setWorkClubId(Integer workClubId)               { this.workClubId = workClubId; }

    public UnavailableStructure getGroundCondition()            { return groundCondition; }
    public void setGroundCondition(UnavailableStructure gc)     { this.groundCondition = gc; }

    public DualListModel<String> getPickList()                  { return pickList; }
    public void setPickList(DualListModel<String> pickList)     { this.pickList = pickList; }

    public Map<String, Boolean> getItemStatus()                 { return itemStatus; }
    public void setItemStatus(Map<String, Boolean> is)          { this.itemStatus = is; }

    public List<String> getMasterItems()                        { return MASTER_ITEMS; }

    public List<String> getGroundConditionItems() {
        return new ArrayList<>(itemStatus.keySet());
    } // end method

    public List<String> getUnavailableItems() {
        return itemStatus.entrySet().stream()
                .filter(e -> Boolean.FALSE.equals(e.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    } // end method

    public List<String> getAllItems() {
        List<String> all = new ArrayList<>(MASTER_ITEMS);
        all.addAll(customItemsBuffer);
        return all;
    }

    public List<String> getItemsToPersist() {
        if (itemsToPersist == null) {
            itemsToPersist = new ArrayList<>(pickList.getTarget());
            itemsToPersist.addAll(customItemsBuffer);
        }
        return itemsToPersist;
    }

    public String getNewCustomItemInput()                       { return newCustomItemInput; }
    public void setNewCustomItemInput(String v)                 { this.newCustomItemInput = v; }

    public List<String> getCustomItemsBuffer()                  { return customItemsBuffer; }

    public int getUnavailableCount() {
        return pickList.getTarget().size() + customItemsBuffer.size();
    } // end method

    // ── Getters — display widget ──────────────────────────────────────────────

    public List<String>  getOpenItems()   { ensureLoaded(); return openItems;   }
    public List<String>  getClosedItems() { ensureLoaded(); return closedItems; }
    public LocalDateTime getLastUpdate()  { ensureLoaded(); return displayLastUpdate; }
    public boolean       isHasData()      { ensureLoaded(); return !openItems.isEmpty() || !closedItems.isEmpty(); }

    public boolean isShowPreview()           { return showPreview; }
    public void    setShowPreview(boolean v) { this.showPreview = v; }

    public void togglePreview() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        showPreview = !showPreview;
        if (showPreview) loadedForClubId = null;
    } // end method

} // end class
