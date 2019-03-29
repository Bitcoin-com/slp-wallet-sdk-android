package com.bitcoin.slpwallet.presentation

import com.bitcoin.slpwallet.persistence.SlpTokenBalance
import java.math.BigDecimal

data class BalanceInfoImpl(
    override var tokenId: String,
    override var amount: BigDecimal,
    override var ticker: String?,
    override var name: String?,
    override var decimals: Int?
) : BalanceInfo {
    companion object {
        fun fromSlpTokenBalance(slpTokenBalance: SlpTokenBalance): BalanceInfoImpl {
            return BalanceInfoImpl(
                slpTokenBalance.tokenId,
                slpTokenBalance.amount,
                slpTokenBalance.ticker,
                slpTokenBalance.name,
                slpTokenBalance.decimals
            )
        }
    }
}