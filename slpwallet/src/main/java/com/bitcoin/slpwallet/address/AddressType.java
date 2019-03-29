package com.bitcoin.slpwallet.address;

/**
 * @author akibabu
 */
enum AddressType {

    Pubkey((byte) 0),
    Script((byte) 1);
    private final byte value;

    AddressType(byte value) {
        this.value = value;
    }

    public byte getByte() {
        return value;
    }

    public static AddressType fromVersion(byte version) {
        switch (version >> 3 & 0x1f) {
            case 0:
                return AddressType.Pubkey;
            case 1:
                return AddressType.Script;
            default:
                throw new AddressFormatException("version=" + (version & 0xFF));
        }
    }

}
