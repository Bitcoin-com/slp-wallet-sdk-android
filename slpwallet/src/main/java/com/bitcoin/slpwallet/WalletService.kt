package com.bitcoin.slpwallet

import com.bitcoin.slpwallet.slp.SlpTokenId
import io.reactivex.Single
import java.math.BigDecimal

/**
 * @author akibabu
 */
internal interface WalletService {

    /**
     * Get BCH and all SLP token balances
     */
    fun refreshBalance(): Single<WalletBalance>


    /**
     * @param tokenId 32 byte hash of the token genesis transaction in hexadecimal.
     *                Example: 3257135d7Singlec351f8b2f46ab2b5e610620beb7a957f3885ce1787cffa90582f503m
     *
     * @param toAddress SLP address format.
     *                  Example: simpleledger:qzk92nt0xdxc9qy3yj53h9rjw8dk0s9cqqsrzm5cny
     *
     * @param numTokens Number of tokens in human readable format (not accounting for token decimal precision).
     *                  Example: 134.12
     *
     * @return txid if successful, or stack trace if error
     */
    fun sendTokenRx(tokenId: SlpTokenId, amount: BigDecimal, toAddress: String): Single<String>

}
