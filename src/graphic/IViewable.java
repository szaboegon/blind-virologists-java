package graphic;

/** Megjeleníthető objektumok által megvalósított interface*/
public interface IViewable {
    /** Értesíti a nézetek a változásokról*/
    void NotifyViews();

    /** Nézet hozzáadása*/
    void AddView(View view);

    /** Nézet eltávolítása*/
    void RemoveView(View view);
}
