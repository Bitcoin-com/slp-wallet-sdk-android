package com.bitcoin.slpwallet.presentation

import java.math.BigDecimal

interface BalanceInfo {
    var tokenId: String
    var amount: BigDecimal
    var ticker: String?
    var name: String?
    var decimals: Int?
}