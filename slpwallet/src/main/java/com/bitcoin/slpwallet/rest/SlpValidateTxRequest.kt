package com.bitcoin.slpwallet.rest

/**
 * https://rest.bitcoin.com/#/slp/validateTxidBulk
 *
 * @author akibabu
 */
internal data class SlpValidateTxRequest(val txids: List<String>)