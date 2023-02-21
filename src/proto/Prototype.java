package proto;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import koporscho.*;

public class Prototype {
    public static ArrayList<Test> menuOptions = new ArrayList<>();
    private static boolean fromFile = false;
    private static Scanner fileScanner;
    private static final Scanner scanner = new Scanner(System.in);
    private static int selectedOption;
    private static boolean running = true;
    public static HashMap<Object, String> objectIDs = new HashMap<>();
    public static HashMap<String, Object> objectIDsInv = new HashMap<>();

    public static ArrayList<Field> fields = new ArrayList<>();
    public static ArrayList<Virologist> virologists = new ArrayList<>();
    public static ArrayList<Agent> agents = new ArrayList<>();
    public static ArrayList<Equipment> equipment = new ArrayList<>();
    public static ArrayList<StatusEffect> statusEffects = new ArrayList<>();
    public static float diceRoll = 0.5f;

    private static final String fileName = "output.txt";
    public static BufferedWriter writer;

    public static boolean random = false;

    /**
     * A Prototípus futását megvalósító függvény, itt választhatóak ki a különböző tesztesetek futtatásra.
     */
    public static void run() {
        System.out.println("[Prototype running...]");
        System.out.println("Please select a test-case to run.\n");

        while(running){
            try {
                objectIDsInv.clear();
                objectIDs.clear();
                fields.clear();
                virologists.clear();
                agents.clear();
                equipment.clear();
                statusEffects.clear();
                // Printing the menu
                System.out.println("[0. Type test commands by hand.]");
                for (int i = 0; i < menuOptions.size(); i++)
                    System.out.println((i+1) + ". " + menuOptions.get(i).name + ".");
                System.out.println("[" + (menuOptions.size()+1) + ". Closes the program.]");

                // User input
                selectedOption = scanner.nextInt();

                // Valid option check
                if(selectedOption > 0 && selectedOption <= menuOptions.size()){
                    System.out.println(menuOptions.get(selectedOption-1).name + " [Selected]");
                    // Initialize instances before each test
                    // Run test
                    fromFile = true;
                    writer = new BufferedWriter(new FileWriter("outputs/" + fileName, false));
                    menuOptions.get(selectedOption-1).run();
                }

                // Fixed flags
                else if(selectedOption == 0) {
                    fromFile = false;
                    while(scanner.hasNext()) {
                        String line = scanner.nextLine();
                        if (line.startsWith("/")) {
                            break;
                        }
                        writer = new BufferedWriter(new FileWriter("outputs/" + fileName, false));
                        cmdProcess(line);
                    }
                }
                else if(selectedOption == menuOptions.size()+1) running = false;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A paraméterként kapott szöveges parancs felismerését végző függvény. Továbbítja a parancsat az azt tényleges végrehajtó függvénynek.
     * @param cmd
     * @throws IOException
     */
    public static void cmdProcess(String cmd) throws IOException {
        String[] proc = cmd.split(" ");
        switch (proc[0]) {
            case "move": move(cmd); break;
            case "virologist": createVirologist(cmd);  break;
            case "map": createMap(cmd);  break;
            case "materials": createMaterials(cmd);  break;
            case "statusEffect": createStatusEffect(cmd); break;
            case "agent":createAgent(cmd);  break;
            case "equipment":createEquipment(cmd);  break;
            case "interact":interactWithField(cmd);  break;
            case "placeEquipment":placeEquipment(cmd);  break;
            case "placeAgent":placeAgent(cmd);  break;
            case "placeMaterials":placeMaterials(cmd);  break;
            case "placeEffect":placeStatusEffect(cmd);  break;
            case "axe":virologistUsesAxe(cmd);  break;
            case "steal":stealEquipment(cmd);  break;
            case "drop":dropEquipment(cmd);  break;
            case "apply":applyAgentOnTarget(cmd);  break;
            case "craft":craftAgent(cmd);  break;
            case "script":runScript(cmd);  break;
            case "log":log(cmd);  break;
            case "endTurn":endTurn(cmd);  break;
        }
    }

    /**
     * A parancsban megadott virológust, a megadott mezőre lépteti.
     * @param cmd Szöveges parancs
     */
    private static void move(String cmd) {
        String[] proc = cmd.split(" ");
        Virologist v = (Virologist) objectIDsInv.get(proc[1]);
        Field f = (Field) objectIDsInv.get(proc[2]);
        v.Move(f);
    }

    /**
     * Létrehozza a parancsban megadott virológust.
     * @param cmd Szöveges parancs
     */
    private static  void createVirologist(String cmd) {
        String[] proc = cmd.split(" ");
        Virologist v = new Virologist("Majon");
        virologists.add(v);
        objectIDs.put(v, proc[1]);
        objectIDsInv.put(proc[1], v);
    }

    /**
     * Létrehozza a parancsban megadott pálya objektumot.
     * @param cmd
     */
    private static  void createMap(String cmd) {
        Scanner sc = fromFile ? fileScanner : scanner;
        String[] proc = cmd.split(" ");
        ArrayList<Field> temp = new ArrayList<>();
        for(int i = 1; i <= (proc.length-1); i+=2) {
            Field f = null;
            switch (proc[i]) {
                case "city" : f = new City();  break;
                case "lab" : f = new Lab(); break;
                case "shelter" : f =new Shelter(); break;
                case "storage" : f = new Storage(); break;
            }
            String id = proc[i+1];
            if(proc[i+1].endsWith(";"))
                id = proc[i+1].substring(0,proc[i+1].length()-1);
            objectIDs.put(f, id);
            objectIDsInv.put(id, f);
            temp.add(f);
            fields.add(f);
        }
        if(temp.size()>1) {
            for (Field f : temp) {
                System.out.println(objectIDs.get(f) + ": ");
                String[] neighbors = sc.nextLine().split(" ");
                ArrayList<Field> neighborsArr = new ArrayList<>();
                for (String id : neighbors) {
                    neighborsArr.add((Field) objectIDsInv.get(id));
                }
                f.SetNeighbors(neighborsArr);
            }
        }
    }

    /**
     * Létrehozza a parancsban megadott Materials objektumot.
     * @param cmd Szöveges parancs
     */
    private static void createMaterials(String cmd) {
        String[] proc = cmd.split(" ");
        Materials m = new Materials(Integer.parseInt(proc[2]), Integer.parseInt(proc[3]));
        objectIDs.put(m, proc[1]);
        objectIDsInv.put(proc[1], m);
    }

    /**
     * Létrehozza a parancsban megadott StatusEffect objektumot.
     * @param cmd Szöveges parancs
     */
    private static void createStatusEffect(String cmd) {
        String[] proc = cmd.split(" ");
        StatusEffect s = new StatusEffect();
        for(int i = 2; i < proc.length; i++) {
            String[] eff = proc[i].split(":");
            switch (eff[0]) {
                case "immunity" : s.SetImmunity(Float.parseFloat(eff[1]));break;
                case "amnesia" : s.SetAmnesia(Boolean.parseBoolean(eff[1]));break;
                case "paralyzed" : s.SetParalyzed(Boolean.parseBoolean(eff[1]));break;
                case "chorea" : s.SetChorea(Boolean.parseBoolean(eff[1]));break;
                case "bagsize" : s.SetBagsize(Integer.parseInt(eff[1]));break;
                case "reflect" : s.SetReflect(Boolean.parseBoolean(eff[1]));break;
                case "duration" : s.SetDuration(Integer.parseInt(eff[1]));break;
                case "bear" : s.SetBear(Boolean.parseBoolean(eff[1]));break;
                case "dead" : s.SetDead(Boolean.parseBoolean(eff[1]));break;
            }
        }
        statusEffects.add(s);
        objectIDs.put(s, proc[1]);
        objectIDsInv.put(proc[1], s);

    }

    /**
     * Létrehozza a parancsban megadott Agent objektumot.
     * @param cmd Szöveges parancs
     */
    private static void createAgent(String cmd) {
        String[] proc = cmd.split(" ");
        Agent a = new Agent((StatusEffect) objectIDsInv.get(proc[2]),(Materials) objectIDsInv.get(proc[3]));
        agents.add(a);
        objectIDs.put(a, proc[1]);
        objectIDsInv.put(proc[1], a);
    }

    /**
     * Létrehozza a parancsban megadott Equipment objektumot.
     * @param cmd Szöveges parancs
     */
    private static void createEquipment(String cmd) {
        String[] proc = cmd.split(" ");
        Equipment e = new Equipment((StatusEffect) objectIDsInv.get(proc[2]), Integer.parseInt(proc[3]));
        equipment.add(e);
        objectIDs.put(e, proc[1]);
        objectIDsInv.put(proc[1], e);
    }

    /**
     * Teszteli a mező, és a rajta álló virológus viselkedését, egymásrahatásukat.
     * @param cmd Szöveges parancs
     */
    private static void interactWithField(String cmd) {
        String[] proc = cmd.split(" ");
        Virologist v = (Virologist) objectIDsInv.get(proc[1]);
        Field f = (Field) objectIDsInv.get(proc[2]);
        Field fPrev = v.GetField();
        int prev = v.GetApCurrent();
        v.Move(f);
        v.SetApCurrent(prev);
        v.Interact();
        if(fPrev != null) {
            v.Move(fPrev);
            v.SetApCurrent(prev - 1);
        }
    }

    /**
     * Elhelyez egy eszközt a parancsban megadott virológuson vagy óvóhelyen. -v/-f
     * @param cmd Szöveges parancs
     */
    private static void placeEquipment(String cmd) {
        String[] proc = cmd.split(" ");
        Equipment eq = (Equipment) objectIDsInv.get(proc[1]);
        switch (proc[2]) {
            case "-v" : {
                Virologist v = (Virologist) objectIDsInv.get(proc[3]);
                v.AddEquipment(eq);
                break;
            }
            case "-f" : {
                Shelter f = (Shelter) objectIDsInv.get(proc[3]);
                f.AddEquipment(eq);
                break;
            }
        }
    }

    /**
     * Elhelyez egy ágenst a parancsban megadott virológuson vagy laborban. -v/-f
     * @param cmd Szöveges parancs
     */
    private static void placeAgent(String cmd) {
        String[] proc = cmd.split(" ");
        Agent a = (Agent) objectIDsInv.get(proc[1]);
        switch (proc[2]) {
            case "-v" : {
                Virologist v = (Virologist) objectIDsInv.get(proc[3]);
                if (Objects.equals(proc[4], "agentInventory"))
                    v.AddAgent(a);
                else
                    v.LearnRecipe(a);
                break;
            }
            case "-f" : {
                Lab f = (Lab) objectIDsInv.get(proc[3]);
                if (proc.length > 4 && Objects.equals(proc[4], "-i")){
                    f.setInfected(a, true);
                }
                else
                    f.AddGeneticCode(a);
                break;
            }
        }
    }

    /**
     * Elhelyez egy anyagot a parancsban megadott virológuson vagy raktáron. -v/-f
     * @param cmd Szöveges parancs
     */
    private static void placeMaterials(String cmd) {
        String[] proc = cmd.split(" ");
        Materials m = (Materials) objectIDsInv.get(proc[1]);
        switch (proc[2]) {
            case "-v" : {
                Virologist v = (Virologist) objectIDsInv.get(proc[proc.length - 1]);
                if (Objects.equals(proc[3], "-m"))
                    v.SetMaxMaterials(m);
                else
                    v.SetMaterials(m);
                break;
            }
            case "-f" : {
                Storage f = (Storage) objectIDsInv.get(proc[proc.length - 1]);
                if (Objects.equals(proc[3], "-m"))
                    f.SetTotalSupply(m);
                else
                    f.SetSupply(m);
                break;
            }
        }
    }

    /**
     * Elhelyez egy StatusEffectet a parancsban megadott virológuson.
     * @param cmd Szöveges parancs
     */
    private static void placeStatusEffect(String cmd) {
        String[] proc = cmd.split(" ");
        StatusEffect s = (StatusEffect) objectIDsInv.get(proc[1]);
        Virologist v = (Virologist) objectIDsInv.get(proc[2]);
        v.AddEffect(s);
    }

    /**
     * A virológus megöli a parancsban meghatározott másik virológust.
     * @param cmd Szöveges parancs
     */
    private static void virologistUsesAxe(String cmd) {
        String[] proc = cmd.split(" ");
        Virologist v1 = (Virologist) objectIDsInv.get(proc[1]);
        Virologist v2 = (Virologist) objectIDsInv.get(proc[2]);
        v1.Chop(v2);
    }

    /**
     * A parancsban megadott virológus ellop egy felszerelést egy másik virológustól. Kezeli a lopást.
     * @param cmd Szöveges parancs
     */
    private static void stealEquipment(String cmd) {
        String[] proc = cmd.split(" ");
        Virologist v1 = (Virologist) objectIDsInv.get(proc[1]);
        Equipment eq = (Equipment) objectIDsInv.get(proc[2]);
        Virologist v2 = (Virologist) objectIDsInv.get(proc[3]);
        v1.StealEquipment(v2, eq);
    }

    /**
     * A parancsban megadott felszerelést eldobja a virológus.
     * @param cmd Szöveges parancs
     */
    private static void dropEquipment(String cmd) {
        String[] proc = cmd.split(" ");
        Equipment eq = (Equipment) objectIDsInv.get(proc[1]);
        Virologist v1 = (Virologist) objectIDsInv.get(proc[2]);
        v1.RemoveEquipment(eq);
    }

    /**
     * A parancsban megadott virológus egy ágenst ken fel egy másik virológusra.
     * @param cmd Szöveges paransc
     */
    private static void applyAgentOnTarget(String cmd) {
        String[] proc = cmd.split(" ");
        Agent a = (Agent) objectIDsInv.get(proc[1]);
        Virologist v1 = (Virologist) objectIDsInv.get(proc[2]);
        Virologist v2 = (Virologist) objectIDsInv.get(proc[3]);
        random = true;
        if(proc.length>4) {
            diceRoll = Float.parseFloat(proc[5]);
            random = false;
        }
        v1.ApplyAgent(v2, a);
    }

    /**
     * Létrehoz egy parancsban megadott ágenst, a megadott virológus.
     * @param cmd Szöveges parancs
     */
    private static void craftAgent(String cmd) {
        String[] proc = cmd.split(" ");
        Agent a = (Agent) objectIDsInv.get(proc[1]);
        Virologist v1 = (Virologist) objectIDsInv.get(proc[2]);
        v1.CraftAgent(a);
    }

    /**
     * Parancsértelemző függvény, amely a szöveges parancsot elemeire bontja.
     * @param cmd Szöveges parancs
     */
    public static void runScript(String cmd) {
        String[] proc = cmd.split(" ");
        try {
            File myObj = new File("tests/" + proc[1]);
            fileScanner = new Scanner(myObj);
            while (fileScanner.hasNextLine()) {
                String data = fileScanner.nextLine();
                if(!Objects.equals(data, ""))
                    cmdProcess(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writer.close();
            evaluateOutput();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * A parancs kimenetét logoló függvény, amely a standard oututra és fájlba dokumentál.
     * @param cmd Szöveges parancs
     * @throws IOException
     */
    private static void log(String cmd) throws IOException {
        String[] proc = cmd.split(" ");
        if(proc.length>1 && Objects.equals(proc[1], "-a")) {
            for (Field f : fields) f.log();
            for (Agent a : agents) a.log();
            for (Equipment e : equipment) e.log();
            for (StatusEffect s : statusEffects) s.log();
        }
        else{
            switch (proc[1]){
                case "-f":((Field)objectIDsInv.get(proc[2])).log(); break;
                case "-a":((Agent)objectIDsInv.get(proc[2])).log(); break;
                case "-e":((Equipment)objectIDsInv.get(proc[2])).log(); break;
                case "-s":((StatusEffect)objectIDsInv.get(proc[2])).log(); break;
            }
        }
    }

    /**
     * A tényleges és elvárt kimenet összehasonlítását végző függvény.
     * @throws IOException
     */
    private  static void evaluateOutput() throws IOException{
        File correctOutput=new File("outputs/" + menuOptions.get(selectedOption-1).fileName);
        FileReader fr1=new FileReader(correctOutput);
        BufferedReader br1=new BufferedReader(fr1);
        String correctLine;

        File output=new File("outputs/"+ fileName);
        FileReader fr2=new FileReader(output);
        BufferedReader br2=new BufferedReader(fr2);
        String outputLine;

        boolean incorrect=false;
        int i=1;
        while(true) {
            correctLine=br1.readLine();
            outputLine=br2.readLine();
            
            if(correctLine==null || outputLine==null) break;
            correctLine=correctLine.replaceAll("\\s+","");
            outputLine=outputLine.replaceAll("\\s+","");
            if(!correctLine.equals(outputLine)) {
                incorrect = true;
                break;
            }
            i++;

        }
        if(incorrect) System.out.print("FAILURE ---> Test: (failed at line: " + i + ") ");
        else System.out.print("SUCCESS ---> Test: ");
        System.out.println(menuOptions.get(selectedOption-1).fileName);
        br1.close();
        br2.close();
    }

    /**
     * Egy játékos aktuális körét lezáró függvény.
     * @param cmd Szöveges parancs
     */
    private static void endTurn(String cmd) {
        String[] proc = cmd.split(" ");
        Virologist v = (Virologist) objectIDsInv.get(proc[1]);
        v.RefreshAP();
        v.Tick();
    }
}
