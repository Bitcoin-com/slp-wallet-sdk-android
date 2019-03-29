package com.bitcoin.slpwallet.persistence

import com.bitcoin.slpwallet.slp.SlpTokenDetailsDao
import com.bitcoin.slpwallet.slp.SlpUtxoDao
import com.bitcoin.slpwallet.slp.SlpValidTxDao
import com.bitcoin.slpwallet.tx.UtxoDao

/**
 * @author akibabu
 */
internal interface WalletDatabase {

    fun tokenBalanceDao(): SlpTokenBalanceDao
    fun utxoDao(): UtxoDao
    fun slpUtxoDao(): SlpUtxoDao
    fun slpTokenDetailsDao(): SlpTokenDetailsDao
    fun slpValidTxDao(): SlpValidTxDao

    fun awaitReady(): Boolean
    fun newWalletClear()

}