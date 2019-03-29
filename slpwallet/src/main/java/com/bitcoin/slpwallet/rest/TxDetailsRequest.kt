package com.bitcoin.slpwallet.rest

/**
 * https://rest.bitcoin.com/v2/transaction/details
 *
 * @author akibabu
 */
internal data class TxDetailsRequest(val txids: List<String>)