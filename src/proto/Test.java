package proto;

/** Test osztály, mely a különböző teszteseteket írja le*/
public class Test{
/** Teszt neve és a tesztet megvalósító függvény*/
    String name;
    String fileName;

    /**
     * A Test osztály két paraméteres konstruktora.
     * @param name A teszteset neve
     * @param fileName A tesztesetet tartalmazó fájl neve
     */
    public Test(String name, String fileName){
        this.name = name;
        this.fileName = fileName;
    }

    /** Teszteset futtatása*/
    public void run(){
        Prototype.runScript("runscript "+fileName);
    }
}
