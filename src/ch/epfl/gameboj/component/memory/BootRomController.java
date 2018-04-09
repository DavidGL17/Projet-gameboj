/**
 * @author Melvin Malonga-Matouba (288405)
 */
package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.memory.BootRom;


public final class BootRomController implements Component {
	private Cartridge cartridge;
	private boolean bootIsDisabled = false;
	
	public BootRomController(Cartridge cartridge) {
	    Objects.requireNonNull(cartridge);
		this.cartridge=cartridge;
	}

	@Override
	public int read(int address) {
		if (!bootIsDisabled) {
			if (address>=AddressMap.BOOT_ROM_START && address<AddressMap.BOOT_ROM_END) {
				return Byte.toUnsignedInt(BootRom.DATA[address]);
			}
		}
		return cartridge.read(address);
	}

	@Override
	public void write(int address, int data) {
		if (!bootIsDisabled && address==AddressMap.REG_BOOT_ROM_DISABLE) {
            bootIsDisabled = true;
        }
        cartridge.write(address, data);
    }

}
