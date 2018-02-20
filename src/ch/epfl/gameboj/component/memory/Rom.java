/**
 * 
 */
package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.Preconditions;

/**
 * Représente une mémoire morte
 * 
 * @author David (270845)
 *
 */
public final class Rom {
    
    private final byte[] data;
    
    /**
     * copie les éléments du paramètre dans le tableau data de la classe
     * 
     * @param data, un tableau de byte
     */
    public Rom(byte[] data) {
        Preconditions.checkArgument(data.length>=0);
        this.data = new byte[data.length];
        for (int i = 0; i < data.length; ++i) {
            this.data[i] = data[i];
        }
    }
    /**
     * @return la taille du tableau data
     */
    public int size() {
        return data.length;
    }
    /**
     * @param index l'index 
     * @return l'élément se trouvant à l'indexe spécifié
     */
    public int read(int index) {
       if (index>=data.length){
           throw new IndexOutOfBoundsException();
       }
       return Byte.toUnsignedInt(data[index]);
    }
}
