package com.bitcoin.slpwallet.presentation

/**
 * Because of bug in Badger Wallet expecting the token ID to be an address prefixed with "bitcoincash:"
 */
fun blockieAddressFromTokenId(tokenId: String): String {
    return tokenId.slice(IntRange(12, tokenId.count() - 1))
}