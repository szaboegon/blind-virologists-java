package graphic;

/** Nézet absztakt osztály*/
public abstract class View {
     /**
      * A nézetet újrarajzoló függvény
      * @param obj Egy IViewable példány
      */
     abstract public void Redraw(IViewable obj);
}
