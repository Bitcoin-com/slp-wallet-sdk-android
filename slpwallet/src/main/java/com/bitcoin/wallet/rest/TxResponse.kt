package com.bitcoin.wallet.rest

/**
 * https://rest.bitcoin.com/v2/transaction/details
 *
 * @author akibabu
 */
internal data class TxResponse(val txid: String, val vout: List<TxOutputResponse>) {

    data class TxOutputResponse(val value: String, val n: Int, val scriptPubKey: ScriptPubKeyResponse) {

        data class ScriptPubKeyResponse(val hex: String)

    }

}