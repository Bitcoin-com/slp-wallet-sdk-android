package com.bitcoin.wallet.address

import java.math.BigInteger

/**
 * @author akibabu
 */
internal data class KeyAddressPair(val address: Address, val privateKey: BigInteger)
