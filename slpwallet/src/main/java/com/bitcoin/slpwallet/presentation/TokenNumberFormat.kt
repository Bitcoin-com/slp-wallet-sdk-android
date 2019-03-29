package com.bitcoin.slpwallet.presentation

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat

fun getTokenNumberFormat(decimals: Int?, ticker: String?): NumberFormat {

    val nf: NumberFormat = NumberFormat.getCurrencyInstance()
    nf.isGroupingUsed = true

    val decimalFormat: DecimalFormat? = nf as? DecimalFormat
    if (decimalFormat != null) {
        var decimalFormatSymbols: DecimalFormatSymbols = decimalFormat.decimalFormatSymbols
        decimalFormatSymbols.currencySymbol = if (ticker != null) { " ${ticker} " } else { "" }
        nf.decimalFormatSymbols = decimalFormatSymbols
    }

    nf.maximumFractionDigits = decimals ?: 0
    nf.minimumFractionDigits = decimals ?: 0


    return nf
}