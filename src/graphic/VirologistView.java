package graphic;

import koporscho.StatusEffect;
import koporscho.Virologist;

/** Virológus nézetet megvalósító osztály*/
public class VirologistView extends View{
    /**
     * A nézetet újrarajzoló függvény
     * @param obj Egy IViewable példány
     */
    public void Redraw(IViewable obj){
        Virologist vir = (Virologist) obj;
        boolean bear = false;
        for (StatusEffect st: vir.GetStatusEffects()) {
            if(st.GetBear())
                bear = true;
        }
        if(bear)
            GUI.getInstance().getMapPanel().update(vir, true);
        GUI.getInstance().getMapPanel().update(vir, false);
    }
}
