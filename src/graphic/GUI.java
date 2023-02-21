package graphic;

import java.awt.*;
import java.awt.event.*;
import koporscho.*;
import koporscho.Character;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Az grafikus kezelőfelület megvalósítására szolgáló osztály.
 */
public class GUI extends JFrame{
    /** HashMap, ami egy adott mezőhöz tárolja a középpontját.*/
    private final HashMap<Field, Point> fieldCentres = new HashMap<>();

    /** Egy Game Controllert tárol.*/
    private final GameController gc = GameController.getInstance();

    /** Egy Game Controller nézetet tárol.*/
    private final GameControllerView gcView = new GameControllerView();

    /** Háttérszín tárolása - inicializálás fekete színnel*/
    private final Color colorBGR = Color.black;

    /** Az ablak szélességét tárolja.*/
    private int wWIDTH = 1536;

    /** Az ablak magasságát tárolja.*/
    private final int wHEIGHT = 552+350;

    /** Használt betűtípusokat tárolása.*/
    private Font font1 = null;
    private Font font2 = null;

    /** Felsorolás a játék egyes állapotaira.*/
    enum GUIState {
        DEFAULT, MOVE, APPLY_AGENT_STEP1, APPLY_AGENT_STEP2, CRAFT_AGENT, DROP_EQUIPMENT, CHOP, STEAL_EQUIPMENT_STEP1, STEAL_EQUIPMENT_STEP2, END_GAME
    }

    /** Hashmap, ami egy adott kép nevéhez egy adott dimenziót tárol.*/
    private final HashMap<String, Dimension> imgDim= new HashMap<>();

    /** Panel, ami a .*/
    private final JPanel contentPane = new JPanel();

    /** Egy GUIstate enum példány, alapértéke DEFAULT*/
    private GUIState state = GUIState.DEFAULT;

    /** A GUI egy példánya*/
    private static GUI instance = null;

    /** A GUI egy példányát adja vissza*/
    public static GUI getInstance() {
        if (instance == null) instance = new GUI();
        return instance;
    }

    /** Háttér panel egy példánya.*/
    private final Background bgrPanel;

    /** Eszköz panel egy példánya.*/
    private final EquipmentPanel eqPanel;

    /** Attribútum panel egy példánya.*/
    private final AttributesPanel attrPanel;

    /** Többhasználatú panel egy példánya.*/
    private final MultiUsePanel muPanel;

    /** Térkép panel egy példánya.*/
    private final Map mapPanel;

    /** Consol panel egy példánya.*/
    private final Console conPanel = new Console();

    /**
     * Kontruktor, amely létrehozza a grafikus kezelőfelületet.
     *  Betűtípusok, menük és karakterek megjelenítése és beállítása.
     */
    private GUI() {
        try {
            font1 = Font.createFont(Font.TRUETYPE_FONT, new File("assets/VCR_OSD_MONO_1.001.ttf")).deriveFont(16f); //VCR_OSD_MONO_1.001.ttf
            font2 = Font.createFont(Font.TRUETYPE_FONT, new File("assets/3Dventure.ttf")).deriveFont(32f); //VCR_OSD_MONO_1.001.ttf
            int i = 1;
            for (Character c: gc.GetChQueue()) {
                String fname = String.format("assets/virologist%d.png",i++);
                BufferedImage img = ImageIO.read(new File(fname));
                imgMap.put((IViewable) c, img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**A vizuális kezelőpanelek létrehozása és beállítása*/
        bgrPanel = new Background();
        eqPanel = new EquipmentPanel();
        attrPanel = new AttributesPanel();
        muPanel = new MultiUsePanel();
        mapPanel = new Map();
        gc.AddView(gcView);
        for (koporscho.Character v: gc.GetChQueue()) {
            ((Virologist)v).AddView(new VirologistView());
        }
        JPanel UIPanel = new JPanel();

        UIPanel.setBackground(colorBGR);

        contentPane.setPreferredSize(new Dimension(wWIDTH, wHEIGHT));
        contentPane.setBackground(colorBGR);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(bgrPanel);
        UIPanel.setLayout(new BoxLayout(UIPanel, BoxLayout.X_AXIS));
        UIPanel.add(attrPanel);
        UIPanel.add(eqPanel);
        UIPanel.add(muPanel);
        UIPanel.add(conPanel);
        UIPanel.add(mapPanel);
        contentPane.add(UIPanel);
        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setResizable(false);
        setTitle("Koporscho TM: Blind Virologists");
        addKeyListener(new KL());
    }

    /**
     *  Implicict getter a háttérpanel lekérdezéséhez.
     */
    public Background getBgrPanel() {return bgrPanel;}

    /**
     *  Implicict getter az eszköz panel lekérdezéséhez.
     */
    public EquipmentPanel getEqPanel() {return eqPanel;}

    /**
     *  Implicict getter az tulajdonság panel lekérdezéséhez.
     */
    public AttributesPanel getAttrPanel() {return attrPanel;}

    /**
     *  Implicict getter a multi usepanel lekérdezéséhez.
     */
    public MultiUsePanel getMuPanel() {return muPanel;}

    /**
     *  Implicict getter a map panel lekérdezéséhez.
     */
    public Map getMapPanel() {return mapPanel;}

    /**
     * Az interface elem megvalósítására szolgáló osztály.
     */
    private abstract class InterfaceElement extends JPanel {

        /** Az adott osztály neve.*/
        protected String name;
        /** Az adott osztály képe.*/
        protected Image img = new BufferedImage(wWIDTH, wHEIGHT, BufferedImage.TYPE_INT_ARGB);
        /** Inicializáló függvény.*/
        public void init() {
            setPreferredSize(imgDim.get(name));
            update();
            repaint();
        }
        /** Az interfacet frissítő függvény.*/
        public void update() {
            img.getGraphics().clearRect(0, 0, img.getWidth(null), img.getHeight(null));
        }
        /** Az interfacet kirajzoló függvény.*/
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
        }
    }

    /**
     * A háttér kezelőfelületetének a megvalósítására szolgáló osztály. InterfaceElement az ősosztálya.
     */
    public class Background extends InterfaceElement{
        /** Az adott osztály képei.*/
        Image lab, storage, shelter, city, fieldImage;

        /** Kontruktor, amely az egyes mezők háttérképéit tölti be */
        public Background() {
            name = "background";
            setBackground(Color.RED);//colorBGR);
            try {
                lab = ImageIO.read(new File("assets/lab.png"));
                shelter = ImageIO.read(new File("assets/shelter.png"));
                storage = ImageIO.read(new File("assets/storage.png"));
                city = ImageIO.read(new File("assets/city.png"));
                wWIDTH = lab.getWidth(null);
                img = new BufferedImage(lab.getWidth(null), lab.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                imgDim.put(name,new Dimension(img.getWidth(null),img.getHeight(null)));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            init();
        }

        /** Egy adott mezőn állva, annak háttérképét frissítő függvény.*/
        public void update(Field f) {
            update();
            if(GameController.objectIDs.get(f).contains("City")){
                fieldImage = city;
            }
            if(GameController.objectIDs.get(f).contains("Shelter")){
                fieldImage = shelter;
            }
            if(GameController.objectIDs.get(f).contains("Lab")){
                fieldImage = lab;
            }
            if(GameController.objectIDs.get(f).contains("Storage")){
                fieldImage = storage;
            }

            if(fieldImage!=null) img.getGraphics().drawImage(fieldImage,0,0,null);

            repaint();
            }

        @Override
        public void update() {
            super.update();
        }
        /** Az háttér kezelőfelületét kirajzoló függvény.
         * @param g Egy grafika példány
         */
        @Override
        protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(img, 0, 0, this);
                this.repaint();
        }
    }

    /**
     * A tulajdonságok kezelőfelületetének a megvalósítására szolgáló osztály. InterfaceElement az ősosztálya.
     */
    public class AttributesPanel extends InterfaceElement {
        BufferedImage bgr, status, portrait;

        /** Az attribútum kezelőfelületét frissítő függvény.*/
        public void update() {
            super.update();
            Graphics gr = img.getGraphics();
            gr.drawImage(bgr,0,0,null);
            gr.drawImage(portrait,8,8,null);
        }

        /** Az attribútum kezelőfelületet frissítő függvény.
        *   A karakter, a rajta hatást kifejtő effektek és az általa birtokolt nyersanyagok mennyisége által változhat.
        */
        public void update(int ap, Materials currMat, Materials maxMat, ArrayList<StatusEffect> statuses, Virologist v) {
            portrait = (BufferedImage) imgMap.get(v);
            update();
            Graphics gr = img.getGraphics();
            gr.setFont(font1);
            String aminoStr = String.format("AminoAcid:  %d/%d", currMat.GetAminoAcid(), maxMat.GetAminoAcid());
            String nucleoStr = String.format("Nucleotide: %d/%d", currMat.GetNucleotide(), maxMat.GetNucleotide());
            String apStr = String.format("Action Points:    %d", ap);
            gr.drawString(aminoStr, 5,230+16);
            gr.drawString(nucleoStr, 5,246+16);
            gr.drawString(apStr, 5,262+16);
            int i = 0;
            int xOffs = 8;
            int yOffs = 8+128+32;
            Boolean[] drawn = {false,false,false,false,false,false,false,false};
            for(StatusEffect s: statuses) {
                if (s.GetParalyzed() && !drawn[0]) {
                    BufferedImage image = status.getSubimage(0, 0, 32, 32);
                    gr.drawImage(image, xOffs + i % 6 * 32, yOffs - i / 6 * 32, 32, 32, null);
                    i++;
                    drawn[0] = true;
                }
                if (s.GetDead() && !drawn[1]) {
                    BufferedImage image = status.getSubimage(32, 0, 32, 32);
                    gr.drawImage(image, xOffs + i % 6 * 32, yOffs - i / 6 * 32, 32, 32, null);
                    i++;
                    drawn[1] = true;
                }
                if (s.GetChorea() && !drawn[2]) {
                    BufferedImage image = status.getSubimage(64, 0, 32, 32);
                    gr.drawImage(image, xOffs + i % 6 * 32, yOffs - i / 6 * 32, 32, 32, null);
                    i++;
                    drawn[2] = true;
                }
                if (s.GetReflect() && !drawn[3]) {
                    BufferedImage image = status.getSubimage(96, 0, 32, 32);
                    gr.drawImage(image, xOffs + i % 6 * 32, yOffs - i / 6 * 32, 32, 32, null);
                    i++;
                    drawn[3] = true;
                }
                if (s.GetBagsize() > 0 && !drawn[4]) {
                    BufferedImage image = status.getSubimage(0, 32, 32, 32);
                    gr.drawImage(image, xOffs + i % 6 * 32, yOffs - i / 6 * 32, 32, 32, null);
                    i++;
                    drawn[4] = true;
                }
                if (s.GetAmnesia() && !drawn[5]) {
                    BufferedImage image = status.getSubimage(32, 32, 32, 32);
                    gr.drawImage(image, xOffs + i % 6 * 32, yOffs - i / 6 * 32, 32, 32, null);
                    i++;
                    drawn[5] = true;
                }
                if (s.GetImmunity() > 0 && !drawn[6]) {
                    BufferedImage image = status.getSubimage(64, 32, 32, 32);
                    gr.drawImage(image, xOffs + i % 6 * 32, yOffs - i / 6 * 32, 32, 32, null);
                    i++;
                    drawn[6] = true;
                }
                if (s.GetBear() && !drawn[7]) {
                    BufferedImage image = status.getSubimage(96, 32, 32, 32);
                    gr.drawImage(image, xOffs + i % 6 * 32, yOffs - i / 6 * 32, 32, 32, null);
                    i++;
                    drawn[7] = true;
                }
            }
            repaint();
        }

        /** Kontruktor, amely a tulajdonság kezelőfelületet létrehozza. */
        public AttributesPanel() {
            name = "attributesPanel";
            setOpaque(false);
            try {
                bgr = ImageIO.read(new File("assets/attrbgr.png"));
                status = ImageIO.read(new File("assets/statuseffects.png"));
                img = new BufferedImage(bgr.getWidth(null), bgr.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                imgDim.put(name,new Dimension(img.getWidth(null),img.getHeight(null)));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            init();
        }

        /** Az attribútum kezelőfelületét kirajzoló függvény.*/
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(img, 0, 0, this);
            this.repaint();
        }
    }

    /**
     * A felszerelés kezelőfelületetének a megvalósítására szolgáló osztály. InterfaceElement az ősosztálya.
     */
    public class EquipmentPanel extends InterfaceElement {
        /** Képeket tárolnak*/
        Image bgr, slot;

        /** Az éppen betöltött képet tárolja*/
        BufferedImage eqImg;

        /** Kontruktor, amely az eszköz kezelőfelületet létrehozza. */
        public EquipmentPanel() {
            name = "equipmentPanel";
            setOpaque(false);
            try {
                bgr = ImageIO.read(new File("assets/eqbgr.png"));
                slot = ImageIO.read(new File("assets/equipmentSlot.png"));
                eqImg = ImageIO.read(new File("assets/equipments.png"));
                img = new BufferedImage(bgr.getWidth(null), bgr.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                imgDim.put(name,new Dimension(img.getWidth(null),img.getHeight(null)));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            init();
        }

        /** Az eszköz kezelőfelületét frissítő függvény.*/
        public void update() {
            super.update();
            Graphics gr = img.getGraphics();
            gr.setFont(font1);
            gr.drawImage(bgr,0,0,null);
            gr.drawString("Equipment", 6, 25);
        }
        /** Az eszköz kezelőfelületét frissítő függvény.*/
        public void update(ArrayList<Equipment> eq) {
            update();
            Graphics gr = img.getGraphics();

            int xOffs = 15;
            int yOffs = 65;
            for (int j = 0; j < 3; j++) {
                gr.drawImage(slot,xOffs-4, yOffs-4 + j * 96,null);
            }
            int i = 0;
            for (int j = 0; j < eq.size(); j++) {
                Equipment e = eq.get(j);
                if (e.GetName().equals("axe")) {
                    BufferedImage image;
                    if(e.GetDurability() > 0) image = eqImg.getSubimage(0, 0, 64, 64);
                    else image = eqImg.getSubimage(128+2*64, 0, 64, 64);
                    gr.drawImage(image, xOffs, yOffs + i * 96, null);
                    i++;
                }
                if (e.GetName().equals("gloves")) {
                    BufferedImage image = eqImg.getSubimage(64, 0, 64, 64);
                    gr.drawImage(image, xOffs, yOffs + i * 96, null);
                    i++;
                }
                if (e.GetName().equals("cloak")) {
                    BufferedImage image = eqImg.getSubimage(128, 0, 64, 64);
                    gr.drawImage(image, xOffs, yOffs + i * 96, null);
                    i++;
                }
                if (e.GetName().equals("bag")) {
                    BufferedImage image = eqImg.getSubimage(128 + 64, 0, 64, 64);
                    gr.drawImage(image, xOffs, yOffs + i * 96, null);
                    i++;
                }
            }
            repaint();
        }

        /**
         * Az eszköz kezelőfelületét kirajzoló függvény.
         * @param g Egy grafika példány
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(img, 0, 0, this);
            this.repaint();
        }
    }

    /**
     * A felszerelés kezelőfelületetének a megvalósítására szolgáló osztály. InterfaceElement az ősosztálya.
     */
    public class MultiUsePanel extends InterfaceElement {
        /** Képeket tárolnak*/
        Image bgr, slot;
        /** Az éppen betöltött képeket tárolják*/
        BufferedImage inv;
        BufferedImage rec;
        BufferedImage field;

        /** */
        int state = 0;

        /** Kontruktor, amely a többhasználatú kezelőfelületet létrehozza. */
        public MultiUsePanel() {
            name = "multiUsePanel";
            setOpaque(false);
            try {
                bgr = ImageIO.read(new File("assets/multipanel.png"));
                slot = ImageIO.read(new File("assets/slot.png"));
                img = new BufferedImage(bgr.getWidth(null), bgr.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                inv = new BufferedImage(bgr.getWidth(null), bgr.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                rec = new BufferedImage(bgr.getWidth(null), bgr.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                field = new BufferedImage(bgr.getWidth(null), bgr.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                imgDim.put(name,new Dimension(img.getWidth(null),img.getHeight(null)));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            init();
        }

        /** Az többhasználatú kezelőfelületét frissítő függvény.*/
        public void update() {
            super.update();

            Graphics gInv = inv.getGraphics();
            Graphics gRec = rec.getGraphics();
            Graphics gField = field.getGraphics();

            Font big = font1.deriveFont(22.0f);

            gInv.setFont(big);
            gRec.setFont(big);
            gField.setFont(big);

            FontRenderContext frc = ((Graphics2D)gInv).getFontRenderContext();
            GlyphVector gv = big.createGlyphVector(frc, "Inventory");
            Rectangle2D box = gv.getVisualBounds();

            int xText = (int)(((getWidth() - box.getWidth()) / 2d) + (-box.getX())) - 5;
            int yText = 20;

            int xPadding = 4, yPadding = 2;
            gInv.drawImage(bgr,0,0,null);
            gInv.drawString("Q", (int) (xPadding * 2.5), yPadding + yText);
            gInv.drawString("E", this.getWidth() - xPadding * 8, yPadding + yText);
            gInv.drawString("Inventory", xText, yText);

            GlyphVector gr = big.createGlyphVector(frc, "Recipes");
            box = gr.getVisualBounds();
            xText = (int)(((getWidth() - box.getWidth()) / 2d) + (-box.getX())) - 5;
            gRec.drawImage(bgr,0,0,null);
            gRec.drawString("Q", (int) (xPadding * 2.5), yPadding + yText);
            gRec.drawString("E", this.getWidth() - xPadding * 8, yPadding + yText);
            gRec.drawString("Recipes", xText, yText);

            GlyphVector gf = big.createGlyphVector(frc, "Targets");
            box = gf.getVisualBounds();
            xText = (int)(((getWidth() - box.getWidth()) / 2d) + (-box.getX())) - 5;
            gField.drawImage(bgr,0,0,null);
            gField.drawString("Q",(int) (xPadding * 2.5), yPadding + yText);
            gField.drawString("E", this.getWidth() - xPadding * 8, yPadding + yText);
            gField.drawString("Targets", xText, yText);
        }

        /** Az többhasználatú kezelőfelületét frissítő függvény.*/
        public void update(Virologist v) {
            update();
            Graphics gInv = inv.getGraphics();
            Graphics gRec = rec.getGraphics();
            Graphics gField = field.getGraphics();

            Font big = font1.deriveFont(22.0f);
            gInv.setFont(big);
            gRec.setFont(big);
            gField.setFont(big);
            int xOffset = 35;
            int yOffset = 60;
            int yPadding = 30;
            ArrayList<Agent> getAgentInventory = v.GetAgentInventory();
            for (int i = 0; i < getAgentInventory.size(); i++) {
                gInv.drawImage(slot,xOffset - 20, (int) (yOffset + i * yPadding - yPadding * 0.7),null);
                gInv.drawString(getAgentInventory.get(i).GetName(), xOffset, yOffset + i * yPadding);
            }

            ArrayList<Agent> getRecipes = v.GetRecipes();
            for (int i = 0; i < getRecipes.size(); i++) {
                gRec.drawImage(slot,xOffset - 20,(int) (yOffset + i * yPadding - yPadding * 0.7),null);
                gRec.drawString(getRecipes.get(i).GetName(), xOffset, yOffset + i * yPadding);
            }

            ArrayList<Character> getCharacters = v.GetField().GetCharacters();
            for (int i = 0; i < getCharacters.size(); i++) {
                gField.drawImage(slot,xOffset - 20, (int) (yOffset + i * yPadding - yPadding * 0.7),null);
                gField.drawString(((Virologist) getCharacters.get(i)).GetName(), xOffset, yOffset + i * yPadding);
            }
            repaint();
        }

        /** Az többhasználatú kezelőfelületét kirajzoló függvény.*/
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics gr = img.getGraphics();
            switch(state) {
                case 0:
                    gr.drawImage(inv,0,0,null);break;
                case 1:
                    gr.drawImage(rec,0,0,null);break;
                case 2:
                    gr.drawImage(field,0,0,null);break;
            }
            g.drawImage(img, 0, 0, null);
            this.repaint();
        }
    }

    /**
     * A konzol kezelőfelületetének a megvalósítására szolgáló osztály. InterfaceElement az ősosztálya.
     */
    public class Console extends InterfaceElement {
        Image bgr;

        /** Kontruktor, amely konzol kezelőfelületet létrehozza. */
        public Console() {
            name = "console";
            setOpaque(false);
            try {
                bgr = ImageIO.read(new File("assets/consolebgr.png"));
                img = new BufferedImage(bgr.getWidth(null), bgr.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                imgDim.put(name,new Dimension(img.getWidth(null),img.getHeight(null)));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            init();
        }

        /**
         *  Az konzol kezelőfelületét kirajzoló függvény.
         * @param g Egy grafika példány
         */
        @Override
        protected void paintComponent(Graphics g) {
            update();
            super.paintComponent(g);
            Graphics gr = img.getGraphics();
            Font big = font1.deriveFont(22.0f);
            gr.setFont(big);
            renderOptions(gr);
            g.drawImage(img, 0, 0, null);
            this.repaint();
        }

        public void update() {
            super.update();
            Graphics gr = img.getGraphics();
            gr.drawImage(bgr,0,0,null);
        }

        /**
         * Az konzol kezelőfelületének az állapotait renderelő függvény.
         * Állapotok melyekben lehet a konzol:
         * DEFAULT: A felhasználó választhat az alább szereplő akciók között.
         * MOVE: A felhasználó választhat a lehetséges léphető mezők között.
         * APPLY_AGENT_STEP1: A felhasználó választhat a lehetséges célpontok között.
         * APPLY_AGENT_STEP2: A felhasználó választhat melyik ágenst használja a kiválasztott célponton.
         * CRAFT_AGENT: A felhasználó választhat melyik ágenst szeretné elkészíteni.
         * DROP_EQUIPMENT: A felhasználó választhat melyik felszerelését szeretné eldobni.
         * CHOP: A felhasználó választhat a lehetséges célpontok között, azon baltával műveletet végrehajtani.
         * STEAL_EQUIPMENT_STEP1: A felhasználó választhat a lehetséges célpontok között.
         * STEAL_EQUIPMENT_STEP2: A felhasználó választhat a lehetséges felszerelések között.
         * END_GAME: Vége a játéknak.
         */
        void renderOptions(Graphics g){
            if(!gc.GameRunning()) return;
            int xBase = 10, yBase = 35;
            int xPadding = 5, yPadding = 22;

            Virologist virologist = GameController.getInstance().GetCurrentVirologist();

            ArrayList<Field> fields = virologist.GetField().GetNeighbors();
            ArrayList<koporscho.Character> characters = virologist.GetField().GetCharacters();
            ArrayList<Agent> agentInventory = virologist.GetAgentInventory();
            ArrayList<Agent> agentRecipes = virologist.GetRecipes();
            ArrayList<Equipment> equipmentInventory = virologist.GetEquipment();
            ArrayList<Equipment> targetInventory = new ArrayList<>();
            if(targetStep1 > 0 && targetStep1 < characters.size())
                targetInventory = ((Virologist)virologist.GetField().GetCharacters().get(targetStep1)).GetEquipment();

            g.drawString(gc.GetCurrentVirologist().GetName()+"'s turn.", xBase + xPadding, yBase);

            if(gc.GetCurrentVirologist().GetApCurrent()==0) {
                g.drawString("No action points left.", xBase + xPadding, yBase+yPadding);
                g.drawString("Press any key to end turn.", xBase + xPadding, yBase+yPadding*2);
                return;
            }
            int c = 1;

            switch (state) {
                /** DEFAULT: A felhasználó választhat az alább szereplő akciók között.*/
                case DEFAULT:
                    g.drawString("1. Move", xBase + xPadding, yBase + yPadding * c++);
                    g.drawString("2. Interact", xBase + xPadding, yBase + yPadding * c++);
                    g.drawString("3. Apply Agent", xBase + xPadding, yBase + yPadding * c++);
                    g.drawString("4. Craft Agent", xBase + xPadding, yBase + yPadding * c++);
                    g.drawString("5. Drop Equipment", xBase + xPadding, yBase + yPadding * c++);
                    g.drawString("6. Chop", xBase + xPadding, yBase + yPadding * c++);
                    g.drawString("7. Steal Equipment", xBase + xPadding, yBase + yPadding * c);
                    break;
                /** MOVE: A felhasználó választhat a lehetséges léphető mezők között.*/
                case MOVE:
                    g.drawString("0. Cancel", xBase + xPadding, yBase + yPadding * c++);

                    for(int i=0; i < fields.size();i++){
                        g.drawString((i+1) + ". field " + GameController.objectIDs.get(fields.get(i)), xBase + xPadding, yBase + yPadding * c++); //allFields.indexOf(fields.get(i))+1
                        }
                    break;
                /**APPLY_AGENT_STEP1: A felhasználó választhat a lehetséges célpontok között.
                 * CHOP: A felhasználó választhat a lehetséges célpontok között, azon baltával műveletet végrehajtani.
                 * STEAL_EQUIPMENT_STEP1: A felhasználó választhat a lehetséges célpontok között.*/
                case APPLY_AGENT_STEP1:
                case CHOP:
                case STEAL_EQUIPMENT_STEP1:
                    g.drawString("0. Cancel", xBase + xPadding, yBase + yPadding * c++);
                    if(characters.size()==0){
                        g.drawString("No characters found.", xBase + xPadding, yBase + yPadding * c++);
                        break;
                    }
                    for(int i=0; i < characters.size();i++){
                        String name = ((Virologist)characters.get(i)).GetName();
                        name = name.isEmpty() ? "<UNIDENTIFIED>" : name;
                        g.drawString((i+1) + ". " + name, xBase + xPadding, yBase + yPadding * c++);
                    }
                    break;
                    /**APPLY_AGENT_STEP2: A felhasználó választhat melyik ágenst használja a kiválasztott célponton.*/
                case APPLY_AGENT_STEP2:
                    g.drawString("0. Cancel", xBase + xPadding, yBase + yPadding * c++);
                    for(int i=0; i < agentInventory.size();i++){
                        String name = agentInventory.get(i).GetName();
                        name = name.isEmpty() ? "<UNIDENTIFIED>" : name;
                        g.drawString((i+1) + ". " + name, xBase + xPadding, yBase + yPadding * c++);
                    }
                    break;
                case CRAFT_AGENT:
                    g.drawString("0. Cancel", xBase + xPadding, yBase + yPadding * c++);
                    for(int i=0; i < agentRecipes.size();i++){
                        String name = agentRecipes.get(i).GetName();
                        name = name.isEmpty() ? "<UNIDENTIFIED>" : name;
                        g.drawString((i+1) + ". " + name, xBase + xPadding, yBase + yPadding * c++);
                    }
                    break;
                case DROP_EQUIPMENT:
                    g.drawString("0. Cancel", xBase + xPadding, yBase + yPadding * c++);
                    if(characters.size()==0){
                        g.drawString("Equipment inventory is empty.", xBase + xPadding, yBase + yPadding * c++);
                        break;
                    }
                    for(int i=0; i<equipmentInventory.size(); i++){
                        String name= equipmentInventory.get(i).GetName();
                        name = name.isEmpty() ? "<UNIDENTIFIED>" : name;
                        g.drawString((i+1) + ". " + name, xBase + xPadding, yBase + yPadding * c++);
                    }
                    break;
                /** STEAL_EQUIPMENT_STEP2: A felhasználó választhat a lehetséges eszközök között.*/
                case STEAL_EQUIPMENT_STEP2:
                    g.drawString("0. Cancel", xBase + xPadding, yBase + yPadding * c++);
                    if(targetInventory.size()==0){
                        g.drawString("Target inventory is empty.", xBase + xPadding, yBase + yPadding * c++);
                        break;
                    }
                    for(int i=0; i < targetInventory.size(); i++){
                        String name= targetInventory.get(i).GetName();
                        name = name.isEmpty() ? "<UNIDENTIFIED>" : name;
                        g.drawString((i+1) + ". " + name, xBase + xPadding, yBase + yPadding * c++);
                    }
                /** END_GAME: Játék vége.*/
                case END_GAME: g.drawString("Game over. Press any key to exit.", xBase + xPadding, yBase + yPadding * c++);
                    break;
            }
        }
    }

    /**
     * A térkép megvalósításához használt osztály, az InterfaceElement leszármazottja.
     */
    public class Map extends InterfaceElement {
        HashMap<Virologist, Point> virLoc = new HashMap<>(); /** Virológusok és az aktuális pozíciójukat tároló HashMap*/
        HashSet<Virologist> bears = new HashSet<>(); /** Medvevírussal fertőzött Virológus karakterek tároló HashMap*/
        Image bgr; /**Térkép háttere*/
        String currID;

        /**A Map osztály paraméter nélküli konstruktora, amely betölti a háttérképet és a mezők adatait*/
        public Map() {
            name = "map";
            currID = "";
            setOpaque(false);
            try {
                bgr = ImageIO.read(new File("assets/"+gc.getMapName()+".png"));
                img = new BufferedImage(bgr.getWidth(null), bgr.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                img.getGraphics().setFont(font2);
                imgDim.put(name,new Dimension(img.getWidth(null),img.getHeight(null)));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            init();
            fieldCentersFill("saves/fc_"+gc.getMapName()+".txt");
        }

        /** A térképen történő változások kirajzolásáért felelős update függvény*/
        public void update() {
            Graphics gr = img.getGraphics();
            gr.drawImage(bgr,0,0,null);
            gr.setColor(Color.WHITE);
            gr.setFont(font2);
            HashMap<Point, Integer> pointOffSets = new HashMap<>();
            for(Virologist v: virLoc.keySet()) {
                Point p = virLoc.get(v);
                Integer offset = pointOffSets.get(p);
                int offs = offset == null ? 0 : offset;
                if(bears.contains(v)) {
                    gr.drawString("B", p.x, p.y + 18 * offs++);
                }
                else if(Objects.equals(currID, GameController.objectIDs.get(v))){
                    gr.drawString("V", p.x, p.y + 18 * offs++);
                }
                pointOffSets.put(p, offs);
            }
            if(state == GUIState.MOVE) {
                Virologist v = (Virologist) GameController.objectIDsInv.get(currID);
                ArrayList<Field> neighbors = v.GetField().GetNeighbors();
                for(int i = 0; i < neighbors.size();i++) {
                    String str = String.format("%d",i+1);
                    Point pt = fieldCentres.get(neighbors.get(i));
                    Integer offset = pointOffSets.get(pt);
                    int offs = offset == null ? 0 : offset;
                    gr.drawString(str, pt.x, pt.y+18*offs);
                }
            }
            repaint();
        }

        /** Virológusok és az aktuális pozíciójukat tároló HashMap frissítő függvénye.
         * @param v Egy virológus amelyet frissíteni kell.
         * @param bear Egy boolean, hogy az adott virológus medve-e.
         **/
        public void update(Virologist v, boolean bear) {
            currID = GameController.objectIDs.get(v);
            virLoc.put(v, fieldCentres.get(v.GetField()));
            if(bear) {
                bears.add(v);
            }
            update();
            repaint();
        }
        /**
         * A térkép kezelőfelületét kirajzoló függvény.
         * @param g Egy grafika példány.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics gr = img.getGraphics();
            g.drawImage(img, 0, 0, this);
            this.repaint();
        }
    }

    private int targetStep1 = 0; /** Lépésre kiválasztott mező azonosítója*/

    /** HashMap amely eltárol egy nézetet és egy hozzá tartozó képet.*/
    private final HashMap<IViewable, Image> imgMap = new HashMap<>();

    /**
     * Paraméterként kapott fájlnév alapján beolvassa a mezők középpontjait (a fieldCenteres HashMap-ben tárolva)
     * @param fname a forrásfájl neve
     */
    public void fieldCentersFill(String fname){
        try {
            File f = new File(fname);
            Scanner sc = new Scanner(f);
            ArrayList<String> parts = new ArrayList<>();

            while(sc.hasNextLine()) {
                String[] strings = sc.nextLine().split(",");
                parts.addAll(Arrays.asList(strings));
                fieldCentres.put((Field) GameController.objectIDsInv.get(parts.get(0)), new Point(Integer.parseInt(parts.get(1)),Integer.parseInt(parts.get(2))));
                parts = new ArrayList<>();
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Billentyűműveletek kezelését végző osztály, amely megvalósítja a KeyListener interface függvényeit.
     */
    public class KL implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {

        }
        /**
         * Felhasználói input kezelése a lenyomott gomb alapján.
         * Lehetséges opciók:
         * Többhasználatú panel állapotának változtatása - q,e
         * Karakter irányítása a megjelenő opciók alapján
         * Aktuális körön lévő játékos befejezi a körét
         * /**
         * Az konzol kezelőfelületének az állapotait renderelő függvény.
         * ÁllapotLehetséges állapotok:
         * DEFAULT: A felhasználó adott gomb megnyomása során adott akció állapotába lép.
         * MOVE: A felhasználó adott gomb megnyomása során adott mezőre lép.
         * APPLY_AGENT_STEP1: A felhasználó adott gomb megnyomása során adott virológust kiválasztja.
         * APPLY_AGENT_STEP2: A felhasználó adott gomb megnyomása során adott ágenst ken a kiválasztott virológusra.
         * CRAFT_AGENT: A felhasználó adott gomb megnyomása során adott eszközt készíti el.
         * DROP_EQUIPMENT: A felhasználó adott gomb megnyomása során adott eszközt dob el.
         * CHOP: A felhasználó adott gomb megnyomása során adott virológust támadja meg a baltával.
         * STEAL_EQUIPMENT_STEP1: A felhasználó adott gomb megnyomása során adott virológust kiválasztja.
         * STEAL_EQUIPMENT_STEP2: A felhasználó adott gomb megnyomása során adott eszközt lop el a kiválasztottvirológustól.
         * @param e Lenyomott gomb eseménye - billentyűkarakter alapján történő kezelés.
         */
        @Override
        public void keyPressed(KeyEvent e) {
            char input = e.getKeyChar();
            if (input == 'e') {
                muPanel.state++;
                if (muPanel.state == 3) {
                    muPanel.state = 0;
                }
            } else if (input == 'q') {
                muPanel.state--;
                if (muPanel.state == -1) {
                    muPanel.state = 2;
                }
            } else if(gc.EndTurn()) {
                return;
            } else {
                switch (state) {
                    /** DEFAULT: A felhasználó adott gomb megnyomása során adott akció állapotába lép.*/
                    case DEFAULT:
                        switch (input) {
                            case '1': state = GUIState.MOVE; break;
                            case '2': gc.Interact(); break;
                            case '3': state = GUIState.APPLY_AGENT_STEP1; break;
                            case '4': state = GUIState.CRAFT_AGENT; break;
                            case '5': state = GUIState.DROP_EQUIPMENT; break;
                            case '6': state = GUIState.CHOP; break;
                            case '7': state = GUIState.STEAL_EQUIPMENT_STEP1;break;
                            default:
                                break;
                        }break;
                    /** MOVE: A felhasználó adott gomb megnyomása során adott mezőre lép.*/
                    case MOVE:
                        switch (input) {
                            case '0': state = GUIState.DEFAULT; break;
                            case '1':
                                gc.Move(1);
                                state = GUIState.DEFAULT;
                                break;
                            case '2':
                                gc.Move(2);
                                state = GUIState.DEFAULT;
                                break;
                            case '3':
                                gc.Move(3);
                                state = GUIState.DEFAULT;
                                break;
                            case '4':
                                gc.Move(4);
                                state = GUIState.DEFAULT;
                                break;
                            case '5':
                                gc.Move(5);
                                state = GUIState.DEFAULT;
                                break;
                            case '6':
                                gc.Move(6);
                                state = GUIState.DEFAULT;
                                break;
                            case '7':
                                gc.Move(7);
                                state = GUIState.DEFAULT;
                                break;
                            case '8':
                                gc.Move(8);
                                state = GUIState.DEFAULT;
                                break;
                            case '9':
                                gc.Move(9);
                                state = GUIState.DEFAULT;
                                break;
                            default:
                                break;
                        }break;
                    /** APPLY_AGENT_STEP1: A felhasználó adott gomb megnyomása során adott virológust kiválasztja.*/
                    case APPLY_AGENT_STEP1:
                        switch (input) {
                            case '0':
                                state = GUIState.DEFAULT;
                                break;
                            case '1':
                                targetStep1 = 1;
                                state = GUIState.APPLY_AGENT_STEP2;
                                break;
                            case '2':
                                targetStep1 = 2;
                                state = GUIState.APPLY_AGENT_STEP2;
                                break;
                            case '3':
                                targetStep1 = 3;
                                state = GUIState.APPLY_AGENT_STEP2;
                                break;
                            case '4':
                                targetStep1 = 4;
                                state = GUIState.APPLY_AGENT_STEP2;
                                break;
                            case '5':
                                targetStep1 = 5;
                                state = GUIState.APPLY_AGENT_STEP2;
                                break;
                            case '6':
                                targetStep1 = 6;
                                state = GUIState.APPLY_AGENT_STEP2;
                                break;
                            case '7':
                                targetStep1 = 7;
                                state = GUIState.APPLY_AGENT_STEP2;
                                break;
                            case '8':
                                targetStep1 = 8;
                                state = GUIState.APPLY_AGENT_STEP2;
                                break;
                            case '9':
                                targetStep1 = 9;
                                state = GUIState.APPLY_AGENT_STEP2;
                                break;
                            default:
                                break;
                        }break;
                    /** APPLY_AGENT_STEP2: A felhasználó adott gomb megnyomása során adott ágenst ken a kiválasztott virológusra.*/
                    case APPLY_AGENT_STEP2:
                        switch (input) {
                            case '0':
                                state = GUIState.APPLY_AGENT_STEP1;
                                break;
                            case '1':
                                gc.ApplyAgent(targetStep1, 1);
                                state = GUIState.DEFAULT;
                                break;
                            case '2':
                                gc.ApplyAgent(targetStep1, 2);
                                state = GUIState.DEFAULT;
                                break;
                            case '3':
                                gc.ApplyAgent(targetStep1, 3);
                                state = GUIState.DEFAULT;
                                break;
                            case '4':
                                gc.ApplyAgent(targetStep1, 4);
                                state = GUIState.DEFAULT;
                                break;
                            case '5':
                                gc.ApplyAgent(targetStep1, 5);
                                state = GUIState.DEFAULT;
                                break;
                            case '6':
                                gc.ApplyAgent(targetStep1, 6);
                                state = GUIState.DEFAULT;
                                break;
                            case '7':
                                gc.ApplyAgent(targetStep1, 7);
                                state = GUIState.DEFAULT;
                                break;
                            case '8':
                                gc.ApplyAgent(targetStep1, 8);
                                state = GUIState.DEFAULT;
                                break;
                            case '9':
                                gc.ApplyAgent(targetStep1, 9);
                                state = GUIState.DEFAULT;
                                break;
                            default:
                                break;
                        }break;
                    /** CRAFT_AGENT: A felhasználó adott gomb megnyomása során adott eszközt készíti el.*/
                    case CRAFT_AGENT:
                        switch (input) {
                            case '0':
                                state = GUIState.DEFAULT;
                                break;
                            case '1':
                                gc.CraftAgent(1);
                                state = GUIState.DEFAULT;
                                break;
                            case '2':
                                gc.CraftAgent(2);
                                state = GUIState.DEFAULT;
                                break;
                            case '3':
                                gc.CraftAgent(3);
                                state = GUIState.DEFAULT;
                                break;
                            case '4':
                                gc.CraftAgent(4);
                                state = GUIState.DEFAULT;
                                break;
                            case '5':
                                gc.CraftAgent(5);
                                state = GUIState.DEFAULT;
                                break;
                            case '6':
                                gc.CraftAgent(6);
                                state = GUIState.DEFAULT;
                                break;
                            case '7':
                                gc.CraftAgent(7);
                                state = GUIState.DEFAULT;
                                break;
                            case '8':
                                gc.CraftAgent(8);
                                state = GUIState.DEFAULT;
                                break;
                            case '9':
                                gc.CraftAgent(9);
                                state = GUIState.DEFAULT;
                                break;
                            default:
                                break;
                        }break;
                    /** DROP_EQUIPMENT: A felhasználó adott gomb megnyomása során adott eszközt dob el.*/
                    case DROP_EQUIPMENT:
                        switch (input) {
                            case '0':
                                state = GUIState.DEFAULT;
                                break;
                            case '1':
                                gc.DropEquipment(1);
                                state = GUIState.DEFAULT;
                                break;
                            case '2':
                                gc.DropEquipment(2);
                                state = GUIState.DEFAULT;
                                break;
                            case '3':
                                gc.DropEquipment(3);
                                state = GUIState.DEFAULT;
                                break;
                            case '4':
                                gc.DropEquipment(4);
                                state = GUIState.DEFAULT;
                                break;
                            case '5':
                                gc.DropEquipment(5);
                                state = GUIState.DEFAULT;
                                break;
                            case '6':
                                gc.DropEquipment(6);
                                state = GUIState.DEFAULT;
                                break;
                            case '7':
                                gc.DropEquipment(7);
                                state = GUIState.DEFAULT;
                                break;
                            case '8':
                                gc.DropEquipment(8);
                                state = GUIState.DEFAULT;
                                break;
                            case '9':
                                gc.DropEquipment(9);
                                state = GUIState.DEFAULT;
                                break;
                            default:
                                break;
                        }break;
                    /** CHOP: A felhasználó adott gomb megnyomása során adott virológust támadja meg a baltával.*/
                    case CHOP:
                        switch (input) {
                            case '0':
                                state = GUIState.DEFAULT;
                                break;
                            case '1':
                                gc.Chop(1);
                                state = GUIState.DEFAULT;
                                break;
                            case '2':
                                gc.Chop(2);
                                state = GUIState.DEFAULT;
                                break;
                            case '3':
                                gc.Chop(3);
                                state = GUIState.DEFAULT;
                                break;
                            case '4':
                                gc.Chop(4);
                                state = GUIState.DEFAULT;
                                break;
                            case '5':
                                gc.Chop(5);
                                state = GUIState.DEFAULT;
                                break;
                            case '6':
                                gc.Chop(6);
                                state = GUIState.DEFAULT;
                                break;
                            case '7':
                                gc.Chop(7);
                                state = GUIState.DEFAULT;
                                break;
                            case '8':
                                gc.Chop(8);
                                state = GUIState.DEFAULT;
                                break;
                            case '9':
                                gc.Chop(9);
                                state = GUIState.DEFAULT;
                                break;
                            default:
                                break;
                        }break;
                    /** STEAL_EQUIPMENT_STEP1: A felhasználó adott gomb megnyomása során adott virológust kiválasztja.*/
                    case STEAL_EQUIPMENT_STEP1:
                        switch (input) {
                            case '0':
                                state = GUIState.DEFAULT;
                                break;
                            case '1':
                                targetStep1 = 1;
                                state = GUIState.STEAL_EQUIPMENT_STEP2;
                                break;
                            case '2':
                                targetStep1 = 2;
                                state = GUIState.STEAL_EQUIPMENT_STEP2;
                                break;
                            case '3':
                                targetStep1 = 3;
                                state = GUIState.STEAL_EQUIPMENT_STEP2;
                                break;
                            case '4':
                                targetStep1 = 4;
                                state = GUIState.STEAL_EQUIPMENT_STEP2;
                                break;
                            case '5':
                                targetStep1 = 5;
                                state = GUIState.STEAL_EQUIPMENT_STEP2;
                                break;
                            case '6':
                                targetStep1 = 6;
                                state = GUIState.STEAL_EQUIPMENT_STEP2;
                                break;
                            case '7':
                                targetStep1 = 7;
                                state = GUIState.STEAL_EQUIPMENT_STEP2;
                                break;
                            case '8':
                                targetStep1 = 8;
                                state = GUIState.STEAL_EQUIPMENT_STEP2;
                                break;
                            case '9':
                                targetStep1 = 9;
                                state = GUIState.STEAL_EQUIPMENT_STEP2;
                                break;
                            default:
                                break;
                        }break;
                    /** STEAL_EQUIPMENT_STEP2: A felhasználó adott gomb megnyomása során adott eszközt lop el a kiválasztottvirológustól.*/
                    case STEAL_EQUIPMENT_STEP2:
                        switch (input) {
                            case '0':
                                state = GUIState.STEAL_EQUIPMENT_STEP1;
                                break;
                            case '1':
                                gc.StealEquipment(targetStep1, 1);
                                state = GUIState.DEFAULT;
                                break;
                            case '2':
                                gc.StealEquipment(targetStep1, 2);
                                state = GUIState.DEFAULT;
                                break;
                            case '3':
                                gc.StealEquipment(targetStep1, 3);
                                state = GUIState.DEFAULT;
                                break;
                            case '4':
                                gc.StealEquipment(targetStep1, 4);
                                state = GUIState.DEFAULT;
                                break;
                            case '5':
                                gc.StealEquipment(targetStep1, 5);
                                state = GUIState.DEFAULT;
                                break;
                            case '6':
                                gc.StealEquipment(targetStep1, 6);
                                state = GUIState.DEFAULT;
                                break;
                            case '7':
                                gc.StealEquipment(targetStep1, 7);
                                state = GUIState.DEFAULT;
                                break;
                            case '8':
                                gc.StealEquipment(targetStep1, 8);
                                state = GUIState.DEFAULT;
                                break;
                            case '9':
                                gc.StealEquipment(targetStep1, 9);
                                state = GUIState.DEFAULT;
                                break;
                            default:
                                break;
                        }break;
                    default:
                        break;
                }
            }
            mapPanel.update();
        }

        /**
         * Egy gomb elengedését megvalósító függvény.
         * Nincsen használata.
         * @param e Lenyomott gomb eseménye - billentyűkarakter alapján történő kezelés.
         */
        @Override
        public void keyReleased(KeyEvent e) {
        }
    }
    /**
     * A grafikus megjelenítő egy állapotát állítja be a paraméterként beadott állapotra.
     * @param _state A beállítandó állapot.
     */
    public void setState(GUIState _state) {
        state = _state;
    }
}
