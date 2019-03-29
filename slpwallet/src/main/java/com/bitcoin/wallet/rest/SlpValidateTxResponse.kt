package com.bitcoin.wallet.rest

/**
 * https://rest.bitcoin.com/#/slp/validateTxidBulk
 *
 * @author akibabu
 */
internal data class SlpValidateTxResponse(val txid: String, val valid: Boolean)