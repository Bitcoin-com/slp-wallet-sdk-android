package com.bitcoin.wallet.slp

import com.bitcoin.wallet.rest.BitcoinRestClient
import com.bitcoin.wallet.rest.TxDetailsRequest
import io.reactivex.Flowable
import io.reactivex.Single
import timber.log.Timber

/**
 * @author akibabu
 */
internal class SlpTokenDetailsFacade(private val dao: SlpTokenDetailsDao,
                                     private val bitcoinClient: BitcoinRestClient) {

    fun getTokenDetails(tokenIds: Set<SlpTokenId>): Single<List<SlpTokenDetails>> {

        val storedDetails = dao.findByIds(tokenIds)

        val tokenIdsInRepo = storedDetails.map { it.tokenId }.toSet()

        val txIds = tokenIds
            .filter { !tokenIdsInRepo.contains(it) }
            .map { it.hex }

        if (txIds.isEmpty()) {
            return Single.just(storedDetails)
        }

        val singles = txIds
            .chunked(20)
            .map { TxDetailsRequest(it) }
            .map {
                bitcoinClient.getTransactions(it)
                    .retry(3)
                    .flattenAsFlowable{ txs -> txs
                            .mapNotNull { SlpOpReturn.tryParse(it.txid, it.vout[0].scriptPubKey.hex) }
                    }
                    .filter { slpOpReturn ->
                        if (slpOpReturn !is SlpOpReturnGenesis) {
                            Timber.w("Unable to handle genesis tokenId=${slpOpReturn.tokenId}")
                            return@filter false
                        }
                        true
                    }
                    .map { (it as SlpOpReturnGenesis).toDetails }
                    .toList() // Get back to list to be able to save any successful requests as soon as possible
                    .doOnSuccess { dao.save(*it.toTypedArray()) }
            }
            .toMutableList()
        singles.add(Single.just(storedDetails))

        return Flowable.concat(singles.map { it.flattenAsFlowable { it } }).toList()
    }

}