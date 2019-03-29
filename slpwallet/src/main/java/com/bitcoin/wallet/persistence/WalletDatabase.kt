package com.bitcoin.wallet.persistence

import com.bitcoin.wallet.slp.SlpTokenDetailsDao
import com.bitcoin.wallet.slp.SlpUtxoDao
import com.bitcoin.wallet.slp.SlpValidTxDao
import com.bitcoin.wallet.tx.UtxoDao

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