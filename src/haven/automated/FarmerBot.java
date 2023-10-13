package haven.automated;

import haven.*;
import haven.automated.helpers.AreaSelectCallback;
import haven.automated.helpers.FarmingStatic;
import haven.res.ui.tt.q.quality.Quality;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static haven.OCache.posres;

public class FarmerBot extends Window implements Runnable, AreaSelectCallback {

    private final GameUI gui;
    private final List<FarmerBot.CropField> fields;
    private final Label fieldsLabel;
    private final Button startButton;
    private final Label granaryLabel;
    private final Label selectedCropTypeLabel;

    private Gob granary;
    private boolean stop;
    private boolean selectGranary;
    private boolean harvest = true;
    private boolean plant = true;
    private boolean active;
    private int currentField;
    private int stage;
    private CropType selectedCropType;

    public FarmerBot(GameUI gui) {
        super(UI.scale(400, 250), "Farmer Bot");

        this.gui = gui;
        this.fields = new ArrayList<>();
        currentField = 0;
        stage = 0;
        add(new Button(UI.scale(60), "Field") {
            @Override
            public void click() {
                selectGranary = false;
                gui.map.registerAreaSelect((AreaSelectCallback) this.parent);
                gui.msg("Select single field.");
                gui.map.areaSelect = true;
            }
        }, UI.scale(15, 15));
        add(new Button(UI.scale(60), "Granary") {
            @Override
            public void click() {
                selectGranary = true;
                gui.map.registerAreaSelect((AreaSelectCallback) this.parent);
                gui.msg("Select area with granary.");
                gui.map.areaSelect = true;
            }
        }, UI.scale(80, 15));
        add(new Button(UI.scale(60), "Reset") {
            @Override
            public void click() {
                fields.clear();
                fieldsLabel.settext("Fields: 0");
                granary = null;
                granaryLabel.settext("Granary: ✘");
                active = false;
                startButton.change("Start");
                currentField = 0;
                stage = 0;
            }
        }, UI.scale(150, 15));
        fieldsLabel = new Label("Fields: 0");
        add(fieldsLabel, UI.scale(50, 50));
        granaryLabel = new Label("Granary: ✘");
        add(granaryLabel, UI.scale(120, 50));
        add(new CheckBox("Harvest") {
            {a = true;}
            public void set(boolean val) {
                harvest = val;
                a = val;
            }
        }, UI.scale(70, 80));
        add(new CheckBox("Plant") {
            {a = true;}
            public void set(boolean val) {
                plant = val;
                a = val;
            }
        }, UI.scale(140, 80));

        add(new Button(100, "Barley") {
            @Override
            public void click() {
                selectedCropType = CropType.BARLEY;
                selectedCropTypeLabel.settext(selectedCropType.toString());
            }
        }, UI.scale(0, 100));
        add(new Button(100, "Carrot") {
            @Override
            public void click() {
                selectedCropType = CropType.CARROT;
                selectedCropTypeLabel.settext(selectedCropType.toString());
            }
        }, UI.scale(110, 100));
        add(new Button(100, "Flax") {
            @Override
            public void click() {
                selectedCropType = CropType.FLAX;
                selectedCropTypeLabel.settext(selectedCropType.toString());
            }
        }, UI.scale(220, 100));
        add(new Button(100, "Hemp") {
            @Override
            public void click() {
                selectedCropType = CropType.HEMP;
                selectedCropTypeLabel.settext(selectedCropType.toString());
            }
        }, UI.scale(0, 130));
        add(new Button(100, "Leek") {
            @Override
            public void click() {
                selectedCropType = CropType.LEEK;
                selectedCropTypeLabel.settext(selectedCropType.toString());
            }
        }, UI.scale(110, 130));
        add(new Button(100, "Lettuce") {
            @Override
            public void click() {
                selectedCropType = CropType.LETTUCE;
                selectedCropTypeLabel.settext(selectedCropType.toString());
            }
        }, UI.scale(220, 130));
        add(new Button(100, "Millet") {
            @Override
            public void click() {
                selectedCropType = CropType.MILLET;
                selectedCropTypeLabel.settext(selectedCropType.toString());
            }
        }, UI.scale(0, 160));
        add(new Button(100, "Pipeweed") {
            @Override
            public void click() {
                selectedCropType = CropType.PIPEWEED;
                selectedCropTypeLabel.settext(selectedCropType.toString());
            }
        }, UI.scale(110, 160));
        add(new Button(100, "Poppy") {
            @Override
            public void click() {
                selectedCropType = CropType.POPPY;
                selectedCropTypeLabel.settext(selectedCropType.toString());
            }
        }, UI.scale(220, 160));
        add(new Button(100, "Pumpkin") {
            @Override
            public void click() {
                selectedCropType = CropType.PUMPKIN;
                selectedCropTypeLabel.settext(selectedCropType.toString());
            }
        }, UI.scale(0, 190));
        add(new Button(100, "Turnip") {
            @Override
            public void click() {
                selectedCropType = CropType.TURNIP;
                selectedCropTypeLabel.settext(selectedCropType.toString());
            }
        }, UI.scale(110, 190));
        add(new Button(100, "Wheat") {
            @Override
            public void click() {
                selectedCropType = CropType.WHEAT;
                selectedCropTypeLabel.settext(selectedCropType.toString());
            }
        }, UI.scale(220, 190));
        selectedCropTypeLabel = new Label(selectedCropType != null ? selectedCropType.toString() : "Crop not yet selected");
        add(selectedCropTypeLabel, UI.scale(0, 220));
        startButton = add(new Button(UI.scale(60), "Start") {
            @Override
            public void click() {
                if (!fields.isEmpty() && granary != null) {
                    active = !active;
                    if (active) {
                        this.change("Stop");
                    } else {
                        this.change("Start");
                    }
                } else {
                    gui.error("Need to select at least one field and granary.");
                }
            }
        }, UI.scale(110, 220));
    }

    @Override
    public void run() {
        gui.msg("Farmer Bot start working");
        while (!stop) {
            if (active) {
                if (!FarmingStatic.cropDrop) {
                    FarmingStatic.cropDrop = true;
                }
                if (!harvest && !plant) {
                    startButton.change("start");
                    active = false;
                    gui.msg("Need to choose either harvest, plant or both");
                }

                if (stage < 2 && !harvest) {
                    stage = 2;
                }
                clearhand();
                if (!fields.isEmpty() && granary != null) {
                    FarmerBot.CropField currentField = getFieldByIndex(this.currentField);
                    checkHealthStaminaEnergy();
                    if (currentField == null) {
                        resetFarmBot();
                    } else {
                        if (currentField.closestCoord == null) {
                            FarmerBot.CropField field = getFieldByIndex(this.currentField);
                            currentField.closestCoord = new Coord(0, 0);
                            field.initializeHarvestingSegments();
                            field.initializePlantingSegments();
                        } else {
                            //need to empty inv before using so can equip scythe - uh just force scythe before clicking start maybe?
                            //also some kind of checkbox if tsacks or wbindles - self explanatory
                            handleStage(currentField);
                        }
                    }
                }
            } else {
                if (FarmingStatic.cropDrop) {
                    FarmingStatic.cropDrop = false;
                }
            }
            sleep(200);
        }
    }

    @Override
    public void areaselect(Coord a, Coord b) {
        Coord nw = a.mul(MCache.tilesz2);
        Coord se = b.mul(MCache.tilesz2);
        if (selectGranary) {
            handleGranarySelection(nw, se);
        } else {
            handleFieldSelection(nw, se);
        }
        gui.map.unregisterAreaSelect();
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
        if ((sender == this) && (Objects.equals(msg, "close"))) {
            stop = true;
            stop();
            reqdestroy();
            FarmingStatic.cropDrop = false;
            gui.farmerBot = null;
            gui.farmerBotThread = null;
        } else
            super.wdgmsg(sender, msg, args);
    }

    public void stop() {
        gui.map.wdgmsg("click", Coord.z, gui.map.player().rc.floor(posres), 1, 0);
        if (gui.map.pfthread != null) {
            gui.map.pfthread.interrupt();
        }
        this.destroy();
    }

    private void handleGranarySelection(Coord nw, Coord se) {
        List<Gob> granaries = AUtils.getGobsInSelectionStartingWith("gfx/terobjs/granary", nw, se, gui);
        if (granaries.size() == 1) {
            granary = granaries.get(0);
            granaryLabel.settext("Granary: ✔");
            gui.msg("Granary found.");
        } else {
            gui.msg("No granary in selected area.");
        }
    }

    private void handleFieldSelection(Coord nw, Coord se) {
        fields.add(new FarmerBot.CropField(fields.size(), nw, se));
        fieldsLabel.settext("Fields: " + fields.size());
        gui.msg("Area selected: " + (se.x - nw.x) / 11 + "x" + (se.y - nw.y) / 11);
    }

    private void stopToDrink() {
        ui.root.wdgmsg("gk", 27);
        if (gui.map.pfthread != null) {
            gui.map.pfthread.interrupt();
        }
    }

    private void handleStage(FarmerBot.CropField currentField) {
        switch (stage) {
            case 0:
                handleStage0(currentField);
                break;
            case 1:
                handleStage1(currentField);
                break;
            case 2:
                handleStage2(currentField);
                break;
            case 3:
                handleStage3(currentField);
                break;
        }
    }

    private void handleStage0(FarmerBot.CropField currentField) {
        sleep(100);
        if (gui.maininv.getFreeSpace() < 1) {
            depositIfFullInventory();
        } else {
            if (currentField.currentIndex == currentField.harvestingSegments.size()) {
                stage = 1;
                gui.msg("Field finished, Depositing seeds");
            } else {
                FarmerBot.FieldSegment currentFieldSegment = currentField.harvestingSegments.get(currentField.currentIndex);
                processField(currentField, currentFieldSegment);
            }
        }
    }

    private void handleStage1(FarmerBot.CropField currentField) {
        if (checkIfSeedsInInventory()) {
            depositAllSeeds();
        } else {
            if (plant) {
                new Thread(new EquipFromBelt(gui, "tsacks"), "EquipFromBelt").start();
                stage = 2;
                currentField.setCurrentIndex(0);
                gui.msg("Seeds deposited, planting.");
            } else {
                stage = 0;
                gui.msg("Planting skipped. Next field");
                this.currentField++;
            }
        }
    }

    private void handleStage2(FarmerBot.CropField currentField) {
        if (currentField.currentIndex == currentField.plantingSegments.size()) {
            stage = 3;
            this.currentField++;
            gui.msg("Field finished, Depositing seeds");
        } else {
            FarmerBot.FieldSegment currentFieldSegment = currentField.plantingSegments.get(currentField.currentIndex);
            List<Gob> gobs = AUtils.getGobsInSelectionStartingWith(getCropGobName(selectedCropType), currentFieldSegment.topLeft, currentFieldSegment.bottomRight, gui);
            if (!checkIfSeedsInInventory() && gobs.size() < currentFieldSegment.size) {
                getHighestQualitySeeds();
            } else if (checkIfSeedsInInventory() && gobs.size() < currentFieldSegment.size) {
                plantSeeds(currentFieldSegment);
            } else if (gobs.size() >= currentFieldSegment.size) {
                currentField.setCurrentIndex(currentField.currentIndex + 1);
            }
        }
    }

    private void handleStage3(FarmerBot.CropField currentField) {
        if (checkIfSeedsInInventory()) {
            depositAllSeeds();
        } else {
            if (harvest) {
                new Thread(new EquipFromBelt(gui, "scythe"), "EquipFromBelt").start();
                stage = 0;
            } else {
                stage = 2;
            }
            currentField.setCurrentIndex(0);
            gui.msg("Seeds deposited, going to next field.");
        }
    }

    private void processField(FarmerBot.CropField currentField, FarmerBot.FieldSegment currentFieldSegment) {
        if (!AUtils.isPlayerInSelectedArea(currentFieldSegment.topLeft, currentFieldSegment.bottomRight, gui)) {
            try {
                Thread.sleep(300);
                gui.map.pfLeftClick(currentFieldSegment.initCoord, null);
                AUtils.waitPf(gui);
                Thread.sleep(300);
            } catch (InterruptedException ignored) {
            }
        } else {
            Gob closest = AUtils.getClosestCropInSelectionStartingWith(getCropGobName(selectedCropType), currentFieldSegment.topLeft, currentFieldSegment.bottomRight, gui, 1);
            if (closest == null) {
                currentField.setCurrentIndex(currentField.currentIndex + 1);
                gui.msg("Harvesting next row.");
            } else {
                if (gui.map.player().getv() == 0 && gui.prog == null) {
                    AUtils.rightClickGob(gui, closest, 1);
                    gui.map.wdgmsg("sel", currentFieldSegment.start.div(11), currentFieldSegment.end.sub(1, 1).div(11), 0);
                } else {
                    sleep(500);
                }
            }
        }
    }

    private void plantSeeds(FarmerBot.FieldSegment currentFieldSegment) {
        if (!AUtils.isPlayerInSelectedArea(currentFieldSegment.topLeft, currentFieldSegment.bottomRight, gui)) {
            try {
                Thread.sleep(300);
                gui.map.pfLeftClick(currentFieldSegment.initCoord, null);
                AUtils.waitPf(gui);
                Thread.sleep(300);
            } catch (InterruptedException ignored) {
            }
        } else {
            if (gui.map.player().getv() == 0 && gui.prog == null) {
                GItem firstSeedInInventory = null;
                for (WItem wItem : gui.maininv.getAllItems()) {
                    try {
                        System.out.println("wtf!!!!!");
                        System.out.println(wItem.item.getres().name.equals(getCropSeedName(selectedCropType)));
                        if (wItem.item.getres() != null && wItem.item.getres().name.equals(getCropSeedName(selectedCropType))) {
                            firstSeedInInventory = wItem.item;
                        }
                    } catch (Loading e) {
                    }
                }
                if (firstSeedInInventory != null) {
                    firstSeedInInventory.wdgmsg("iact", Coord.z, 1);
                    gui.map.wdgmsg("sel", currentFieldSegment.start.div(11), currentFieldSegment.end.sub(1, 1).div(11), 0);
                } else {
                    gui.error("Something went wrong.");
                }
            } else {
                sleep(500);
            }
        }
    }

    private boolean checkIfSeedsInInventory() {
        boolean seeds = false;
        for (WItem wItem : gui.maininv.getAllItems()) {
            try {
                if (wItem.item.getres() != null && wItem.item.getres().name.equals(getCropSeedName(selectedCropType))) {
                    seeds = true;
                }
            } catch (Loading e) {
            }
        }
        return seeds;
    }

    private void getHighestQualitySeeds() {
        try {
            Thread.sleep(300);
            if (FarmingStatic.grainSlots.isEmpty()) {
                gui.map.pfRightClick(granary, -1, 3, 0, null);
                AUtils.waitPf(gui);
                Thread.sleep(1000);
            } else if (FarmingStatic.grainSlots.size() == 10) {
                takeBestSeeds();
            }
        } catch (InterruptedException ignored) {
        }
    }

    private void takeBestSeeds() {
        Grainslot best = FarmingStatic.grainSlots.stream()
                .filter(grainslot -> grainslot.getRawinfo() != null)
                .filter(grainslot -> {
                    boolean crop = grainslot.info().stream().anyMatch(info -> info instanceof ItemInfo.Name && ((ItemInfo.Name) info).original.contains(getCropName(selectedCropType)));
                    boolean enoughForField = grainslot.info().stream().anyMatch(info -> info instanceof GItem.Amount && ((GItem.Amount) info).itemnum() >= gui.maininv.getFreeSpace() * 50);
                    double qualityTemp = grainslot.info().stream().filter(info -> info instanceof Quality).mapToDouble(info -> ((Quality) info).q).findFirst().orElse(0.0);
                    boolean betterQl = qualityTemp > 0;
                    return crop && betterQl;
                })
                .findFirst().orElse(null);

        if (best != null) {
            while (gui.maininv.getFreeSpace() > 0) {
                takeSeeds(best, gui.maininv.getFreeSpace());
                sleep(1000);
            }
        }
    }

    private void takeSeeds(Grainslot best, int freeSpace) {
        for (int i = 0; i < freeSpace; i++) {
            best.wdgmsg("take");
        }
    }

    private void depositAllSeeds() {
        handleSeedsDeposit(false);
    }

    private void depositIfFullInventory() {
        handleSeedsDeposit(true);
    }

    private void handleSeedsDeposit(boolean checkFreeSpace) {
        try {
            int freeSpace = checkFreeSpace ? gui.maininv.getFreeSpace() : 0;
            Thread.sleep(300);
            if (freeSpace < 1 && FarmingStatic.grainSlots.isEmpty()) {
                gui.map.pfRightClick(granary, -1, 3, 0, null);
                AUtils.waitPf(gui); 
                Thread.sleep(1000);
            } else if (freeSpace < 1 && FarmingStatic.grainSlots.size() == 10) {
                iterateThroughSeeds();
            }
        } catch (InterruptedException ignored) {
        }
    }

    private void iterateThroughSeeds() {
        gui.maininv.getAllItems().stream()
                .filter(wItem -> wItem.item.getres() != null && wItem.item.getres().name.equals(getCropSeedName(selectedCropType)))
                .forEach(wItem -> {
                    try {
                        double quality = wItem.item.info().stream().filter(info -> info instanceof Quality).map(info -> ((Quality) info).q).findFirst().orElse(0.0);
                        int amount = wItem.item.info().stream().filter(info -> info instanceof GItem.Amount).mapToInt(info -> ((GItem.Amount) info).itemnum()).findFirst().orElse(0);

                        Grainslot firstEmpty = FarmingStatic.grainSlots.stream().filter(grainslot -> grainslot.getRawinfo() == null).findFirst().orElse(null);
                        Grainslot matchingQl = FarmingStatic.grainSlots.stream()
                                .filter(grainslot -> grainslot.getRawinfo() != null)
                                .filter(grainslot -> {
                                    boolean crop = grainslot.info().stream().anyMatch(info -> info instanceof ItemInfo.Name && ((ItemInfo.Name) info).original.contains(getCropName(selectedCropType)));
                                    boolean fitAll = grainslot.info().stream().anyMatch(info -> info instanceof GItem.Amount && ((GItem.Amount) info).itemnum() + amount <= 200000);
                                    boolean qlMatch = grainslot.info().stream().anyMatch(info -> info instanceof Quality && ((Quality) info).q == quality);
                                    return crop && fitAll && qlMatch;
                                })
                                .findFirst().orElse(null);

                        handleGrainslot(wItem, matchingQl, firstEmpty);
                    } catch (Loading ignored) {
                    }
                });
    }

    private void handleGrainslot(WItem wItem, Grainslot matchingQl, Grainslot firstEmpty) {
        if (matchingQl != null) {
            moveItem(wItem, matchingQl);
        } else if (firstEmpty != null) {
            moveItem(wItem, firstEmpty);
        } else {
            active = false;
            startButton.change("Start");
            gui.error("No space in granary. Stopping.");
        }
    }

    private void clearhand() {
        if (!gui.hand.isEmpty()) {
            if (gui.vhand != null) {
                gui.vhand.item.wdgmsg("drop", Coord.z);
            }
        }
        AUtils.rightClick(gui);
    }

    private void moveItem(WItem wItem, Grainslot grainslot) {
        wItem.item.wdgmsg("take", Coord.z);
        grainslot.wdgmsg("drop", 0);
    }

    private void checkHealthStaminaEnergy() {
        if (gui.getmeters("hp").get(1).a < 0.1) {
            System.out.println("Low HP, porting home.");
            gui.act("travel", "hearth");
            stop();
            try {
                Thread.sleep(8000);
            } catch (InterruptedException ignored) {
            }
        } else if (gui.getmeter("nrj", 0).a < 0.30) {
            gui.error("Energy critical. Farmer stopping.");
            stop();
        } else if (gui.getmeter("stam", 0).a < 0.40) {
            try {
                stopToDrink();
                AUtils.drinkTillFull(gui, 0.99, 0.99);
            } catch (InterruptedException e) {
                System.out.println("Drinking interrupted.");
            }
        }
    }

    public FarmerBot.CropField getFieldByIndex(int index) {
        for (FarmerBot.CropField field : fields) {
            if (field.fieldIndex == index) {
                return field;
            }
        }
        return null;
    }

    private void resetFarmBot() {
        gui.msg("Farming Finished.");
        fields.clear();
        fieldsLabel.settext("Fields: 0");
        granary = null;
        granaryLabel.settext("Granary: ✘");
        active = false;
        startButton.change("Start");
        currentField = 0;
        stage = 0;
    }

    private void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ignored) {
        }
    }

    private static class FieldSegment {
        private final Coord start;
        private final Coord end;
        private final Coord topLeft;
        private final Coord bottomRight;
        private final Coord initCoord;
        private final int height;
        private final int width;
        private final int size;

        public FieldSegment(Coord start, Coord end, boolean isStartLeft, int rows) {
            this.start = start;
            this.end = end;

            this.height = Math.abs(end.y - start.y) / 11;
            this.width = Math.abs(end.x - start.x) / 11;
            this.size = height * width;

            int topLeftX = Math.min(start.x, end.x);
            int topLeftY = Math.min(start.y, end.y);
            this.topLeft = new Coord(topLeftX, topLeftY);

            int bottomRightX = Math.max(start.x, end.x);
            int bottomRightY = Math.max(start.y, end.y);
            this.bottomRight = new Coord(bottomRightX, bottomRightY);

            if (isStartLeft) {
                if (rows == 1 || rows == 2) {
                    this.initCoord = start.add(4, 5);
                } else {
                    this.initCoord = start.add(4, 16);
                }
            } else {
                if (rows == 1) {
                    this.initCoord = start.add(-4, 5);
                } else {
                    this.initCoord = start.add(-4, 16);
                }
            }
        }
    }

    private enum CropType {
        BARLEY, BEETROOT, CARROT, FLAX, HEMP, LEEK, LETTUCE, MILLET, PIPEWEED, POPPY, PUMPKIN, RED_ONION, TURNIP, WHEAT, YELLOW_ONION
    }

    private String getCropGobName(CropType cropType) {
        return switch (cropType) {
            case BARLEY -> "gfx/terobjs/plants/barley";
            case BEETROOT -> "gfx/terobjs/plants/beet";
            case CARROT -> "gfx/terobjs/plants/carrot";
            case FLAX -> "gfx/terobjs/plants/flax";
            case HEMP -> "gfx/terobjs/plants/hemp";
            case LEEK -> "gfx/terobjs/plants/leek";
            case LETTUCE -> "gfx/terobjs/plants/lettuce";
            case MILLET -> "gfx/terobjs/plants/millet";
            case PIPEWEED -> "gfx/terobjs/plants/pipeweed";
            case POPPY -> "gfx/terobjs/plants/poppy";
            case PUMPKIN -> "gfx/terobjs/plants/pumpkin";
            case RED_ONION -> "gfx/terobjs/plants/redonion";
            case TURNIP -> "gfx/terobjs/plants/turnip";
            case WHEAT -> "gfx/terobjs/plants/wheat";
            case YELLOW_ONION -> "gfx/terobjs/plants/yellowonion";
        };
    }

    private String getCropName(CropType cropType) {
        return switch (cropType) {
            case BARLEY -> "Barley";
            case BEETROOT -> "Beetroot";
            case CARROT -> "Carrot";
            case FLAX -> "Flax";
            case HEMP -> "Hemp";
            case LEEK -> "Leek";
            case LETTUCE -> "Lettuce";
            case MILLET -> "Millet";
            case PIPEWEED -> "Pipeweed";
            case POPPY -> "Poppy";
            case PUMPKIN -> "Pumpkin";
            case RED_ONION -> "Red Onion";
            case TURNIP -> "Turnip";
            case WHEAT -> "Wheat";
            case YELLOW_ONION -> "Yellow Onion";
        };
    }

    private String getCropSeedName(CropType cropType) {
        return switch (cropType) {
            case BARLEY -> "gfx/invobjs/seed-barley"; //
//            case BEETROOT -> "gfx/invobjs/beet";
            case CARROT -> "gfx/invobjs/seed-carrot";
            case FLAX -> "gfx/invobjs/seed-flax";
            case HEMP -> "gfx/invobjs/seed-hemp";
            case LEEK -> "gfx/invobjs/seed-leek"; //
            case LETTUCE -> "gfx/invobjs/seed-lettuce"; //
            case MILLET -> "gfx/invobjs/seed-millet";
            case PIPEWEED -> "gfx/invobjs/seed-pipeweed"; //
            case POPPY -> "gfx/invobjs/seed-poppy"; //
            case PUMPKIN -> "gfx/invobjs/seed-pumpkin"; //
//            case RED_ONION -> "gfx/invobjs/redonion";
            case TURNIP -> "gfx/invobjs/seed-turnip"; //
            case WHEAT -> "gfx/invobjs/seed-wheat"; //
//            case YELLOW_ONION -> "gfx/invobjs/yellowonion";
            default -> "your_mother";
        };
    }

    private static class CropField {
        private final int fieldIndex;
        private final int size;
        private final int height;
        private final int width;
        private int currentIndex;

        private final Coord fieldNW;
        private final Coord fieldSE;
        private Coord closestCoord;
        private List<FarmerBot.FieldSegment> harvestingSegments;
        private List<FarmerBot.FieldSegment> plantingSegments;

        public CropField(int fieldIndex, Coord farmNW, Coord farmSE) {
            this.harvestingSegments = new ArrayList<>();
            this.plantingSegments = new ArrayList<>();
            this.fieldIndex = fieldIndex;
            this.height = (farmSE.y - farmNW.y) / 11;
            this.width = (farmSE.x - farmNW.x) / 11;
            this.size = height * width;
            this.fieldNW = farmNW;
            this.fieldSE = farmSE;
            this.currentIndex = 0;
        }

        public Coord getFieldNW() {
            return fieldNW;
        }

        public Coord getFieldSE() {
            return fieldSE;
        }

        public void setClosestCoord(Coord closestCoord) {
            this.closestCoord = closestCoord;
        }

        public void setCurrentIndex(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        public void initializeHarvestingSegments() {
            boolean isStartLeft = true;

            for (int i = 0; i < height; i += 3) {
                int top = fieldNW.y + i * 11;
                int bottom = Math.min(fieldNW.y + i * 11 + 33, fieldSE.y);

                Coord start = new Coord(isStartLeft ? fieldNW.x : fieldSE.x, top);
                Coord end = new Coord(isStartLeft ? fieldSE.x : fieldNW.x, bottom);

                int numberOfRows = (bottom - top) / 11;

                harvestingSegments.add(new FarmerBot.FieldSegment(start, end, isStartLeft, numberOfRows));

                isStartLeft = !isStartLeft;
            }
        }

        public void initializePlantingSegments() {
            boolean isStartLeft = true;
            for (int i = 0; i < height; i += 2) {
                int top = fieldNW.y + i * 11;
                int bottom = Math.min(fieldNW.y + i * 11 + 22, fieldSE.y);

                Coord start = new Coord(isStartLeft ? fieldNW.x : fieldSE.x, top);
                Coord end = new Coord(isStartLeft ? fieldSE.x : fieldNW.x, bottom);

                plantingSegments.add(new FarmerBot.FieldSegment(start, end, isStartLeft, (fieldNW.y + i * 11 + 22 > fieldSE.y) ? 1 : 2));

                isStartLeft = !isStartLeft;
            }
        }
    }
}
