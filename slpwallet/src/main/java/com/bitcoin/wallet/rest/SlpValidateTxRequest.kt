package com.bitcoin.wallet.rest

/**
 * https://rest.bitcoin.com/#/slp/validateTxidBulk
 *
 * @author akibabu
 */
internal data class SlpValidateTxRequest(val txids: List<String>)