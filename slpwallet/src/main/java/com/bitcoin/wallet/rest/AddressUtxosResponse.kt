package com.bitcoin.wallet.rest

/**
 * https://rest.bitcoin.com/#/address/utxoBulk
 *
 * @author akibabu
 */
internal data class AddressUtxosResponse(val utxos: List<UtxoResponse>, val cashAddress: String) {
    data class UtxoResponse(val txid: String, val vout: Int, val satoshis: Long, val cashAddress: String?)

    fun flatMapEnrichWithAddress(): List<UtxoResponse> {
        return utxos
            .map { it.copy(cashAddress = cashAddress) }
    }
}
