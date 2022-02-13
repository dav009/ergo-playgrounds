package org.ergoplatform.playgroundenv.models

import org.ergoplatform.ErgoBox
import  org.ergoplatform.appkit.impl.BlockchainContextBase
import  org.ergoplatform.appkit.impl.BlockchainContextImpl
import org.ergoplatform.appkit.{InputBox, BlockchainContext}
import org.ergoplatform.appkit.{InputBox, BlockchainContext}
class DummyPartyImpl(blockchain: DummyBlockchainSimulationImpl, ctx:BlockchainContext,  override val name: String)
  extends Party {

  override val wallet: Wallet =
    new DummyWalletImpl(blockchain,  ctx.asInstanceOf[BlockchainContextBase], s"$name Wallet")

  override def generateUnspentBoxes(
    toSpend: Long,
    tokensToSpend: List[TokenAmount]
  ): Unit = {
    blockchain.generateUnspentBoxesFor(wallet.getAddress, toSpend, tokensToSpend)
    println(
      s"....$name: Generating unspent boxes for $toSpend nanoERGs and tokens: ${TokenAmount
        .prettyprintTokens(tokensToSpend)}"
    )
  }

  override def selectUnspentBoxes(
    toSpend: Long,
    tokensToSpend: List[TokenAmount]
  ): List[ErgoBox] =
    blockchain.selectUnspentBoxesFor(wallet.getAddress)

  override def printUnspentAssets(): Unit = {
    val coins  = blockchain.getUnspentCoinsFor(wallet.getAddress)
    val tokens = blockchain.getUnspentTokensFor(wallet.getAddress)
    println(
      s"....$name: Unspent coins: $coins nanoERGs; tokens: ${TokenAmount.prettyprintTokens(tokens)}"
    )
  }

}

object DummyPartyImpl {

  def apply(blockchain: DummyBlockchainSimulationImpl,ctx:BlockchainContext, name: String): DummyPartyImpl =
    new DummyPartyImpl(blockchain, ctx, name)
}
