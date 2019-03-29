package com.bitcoin.wallet.tx

import com.bitcoin.wallet.address.Address
import com.bitcoin.wallet.encoding.ByteUtils
import com.bitcoin.wallet.slp.SLPWalletImpl
import com.bitcoin.wallet.slp.SlpOpReturnSend
import org.bitcoinj.core.*
import org.bitcoinj.core.Transaction.SigHash
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import timber.log.Timber

/**
 * @author akibabu
 */
internal class TxBuilder(val wallet: SLPWalletImpl) {

    val params = wallet.network.instance.parameters

    // Our source of UTXO's come with the cash address so we use that also to find the SLP key
    val cashAddressKeyMap = mapOf(
        Pair(wallet.bchAddressKey.address.toCash().toString(), ECKey.fromPrivate(wallet.bchAddressKey.privateKey)),
        Pair(wallet.slpKeyAddress.address.toCash().toString(), ECKey.fromPrivate(wallet.slpKeyAddress.privateKey)))

    internal inner class Builder {
        val tx = Transaction(params)

        // We need to sign everything last so store the input together with address so we can find the right key
        inner class InputWithAddress(val address: String, val input: TransactionInput)
        private val inputs = ArrayList<InputWithAddress>()

        init {
            tx.setVersion(2)
        }

        fun serialize(): String {
            inputs.forEach { tx.addInput(it.input) }
            inputs.forEachIndexed { index, inputWithAddress -> run {
                tx.getInput(index.toLong()).scriptSig = inputSignature(index, inputWithAddress.input,
                    inputWithAddress.address)
            } }
            Timber.d("Serialized tx $tx")
            tx.verify()
            return ByteUtils.Hex.encode(tx.bitcoinSerialize())
        }

        fun addInput(utxo: Utxo): Builder {
            inputs.add(InputWithAddress(utxo.cashAddress, createInput(utxo)))
            return this
        }

        fun addOutput(satoshi: Long, address: Address): Builder {
            tx.addOutput(Coin.valueOf(satoshi), org.bitcoinj.core.Address.fromBase58(params, address.toBase58()))
            return this
        }

        fun addOutput(opReturn: SlpOpReturnSend): Builder {
            tx.addOutput(Coin.ZERO, opReturn.createScript())
            return this
        }

        private fun inputSignature(inputIndex: Int, input: TransactionInput, cashAddress: String): Script {
            val privateKey = cashAddressKeyMap.getValue(cashAddress)
            val signature = tx.calculateWitnessSignature(
                inputIndex, privateKey,
                input.scriptSig.chunks[0].data,
                input.value, SigHash.ALL, false)
            return ScriptBuilder()
                .data(signature.encodeToBitcoin())
                .data(privateKey.pubKeyPoint.getEncoded(true))
                .build()
        }

        private fun createInput(utxo: Utxo): TransactionInput {
            val inputScript = ScriptBuilder()
                .data(utxo.scriptBytes)
                .build().program
            return TransactionInput(
                params, tx, inputScript,
                TransactionOutPoint(params, utxo.index.toLong(), Sha256Hash.wrap(utxo.txId)),
                Coin.valueOf(utxo.satoshi))
        }

    }

    companion object {
        fun outputFee(numOutputs: Int): Long {
            return numOutputs.toLong() * 34
        }

        const val DUST_LIMIT: Long = 546
    }

}
