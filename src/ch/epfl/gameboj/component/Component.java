package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

/**
 * @author David (270845)
 *
 */
public interface Component {
    public static final int NO_DATA = 0x100;
    
    /**
     * Cette méthode fournit l'élément se trouvant à l'adresse spécifiée
     * 
     * @param address l'adresse où se trouve l'élément désiré
     * @return l'élément se trouvant à l'adresse ou NO_DATA si le composnant ne possède aucune valeur à cette adresse
     * @throws IllegalArgumentException si l'adresse n'est pas un élément de 16 bits
     */
    public abstract int read(int address);

    /**
     * Cette méthode stocke la valeur donnée à l'adresse donnée, ou ne fais rien si le composants ne permet pas de stocker d'adresse à cette emplacement
     * 
     * @param address l'adresse où l'on veut stocker notre valeur
     * @param data la valeur que l'on veut stocker
     * @throws IllegalArgumentException si l'adresse n'est pas un élément de 16 bit ou si la valeur n'est pas un élément de 8 bit
     */
    public abstract void write(int address, int data);

    /**
     * @param bus
     */
    public default void attachTo(Bus bus) {
        bus.attach(this);
    }
}