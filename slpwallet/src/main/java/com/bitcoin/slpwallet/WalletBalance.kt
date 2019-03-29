package com.bitcoin.slpwallet

import com.bitcoin.slpwallet.slp.SlpTokenDetails
import java.math.BigDecimal

/**
 * @author akibabu
 */
internal data class WalletBalance(val nativeBalance: Long, val tokenBalance: Map<SlpTokenDetails, BigDecimal>)