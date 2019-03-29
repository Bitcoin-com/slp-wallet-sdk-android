package com.bitcoin.wallet.rest

import com.bitcoin.wallet.address.AddressCash

/**
 * https://rest.bitcoin.com/#/address/utxoBulk
 *
 * @author akibabu
 */
internal data class AddressUtxosRequest private constructor(val addresses: List<String>) {

    constructor(addresses: Collection<AddressCash>) : this(addresses.map { it.toString() })
}