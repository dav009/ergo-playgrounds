package org.ergoplatform.playgrounds.examples.test

import org.scalatest.PropSpec
import org.ergoplatform.playground._
import  org.ergoplatform.playgroundenv.models._
import org.ergoplatform.appkit._
import org.ergoplatform.appkit.impl.InputBoxImpl
import scala.collection.JavaConverters._
import org.ergoplatform.{ErgoLikeTransaction, UnsignedErgoLikeTransaction, P2PKAddress, ErgoAddressEncoder, ErgoAddress}
import org.ergoplatform.appkit.impl.{UnsignedTransactionImpl, SignedTransactionImpl}

object SimpleTransaction {
   val blockchainSim = newBlockChainSimulationScenario(
      "SwapWithPartialAndThenTotalMatching"
   )

  val ergoClient = SimulatedErgoClient.create(blockchainSim)
  ergoClient.execute(
    (ctx:BlockchainContext) =>{

        val receiverParty          = blockchainSim.newParty("receiver", ctx)
  val senderParty          = blockchainSim.newParty("sender", ctx)
  senderParty.generateUnspentBoxes(toSpend = 10000000000L)

      val txBuilder = ctx.newTxBuilder()
       val amountToSpend: Long = Parameters.OneErg
       val newBox = txBuilder.outBoxBuilder()
        .value(amountToSpend)
        .contract(ctx.compileContract(
          ConstantsBuilder.create()
            // this looks ugly
            .item("recPk", receiverParty.wallet.getAddress.asP2PK().pubkey)
            .build(),
          "{ recPk }")
        ) 
         .build()

      // this should look like current app-kit
      // it should look like  val boxes: java.util.Optional[java.util.List[InputBox]] = wallet.getUnspentBoxes(totalToSpend)
      val boxes = ctx.getUnspentBoxesFor(senderParty.wallet.getAddress, 0, 0) 
 
      
       val tx: UnsignedTransaction = txBuilder
        .boxesToSpend(boxes)
        .outputs(newBox)
         .fee(Parameters.MinFee)
      // it should look like .sendChangeTo(prover.getP2PKAddress())
         .sendChangeTo(P2PKAddress(senderParty.wallet.getAddress.getPublicKey()))
        .build()


      
      val signed = senderParty.wallet.sign(tx)
       var receiverUnspentCoins =  blockchainSim.getUnspentCoinsFor(receiverParty.wallet.getAddress)
       assert(receiverUnspentCoins== (0 ))

     val txId: String = ctx.sendTransaction(signed)

      print("results")
      val senderPartyUnspentCoins = blockchainSim.getUnspentCoinsFor(senderParty.wallet.getAddress)
      receiverUnspentCoins =  blockchainSim.getUnspentCoinsFor(receiverParty.wallet.getAddress)
      assert(senderPartyUnspentCoins== (10000000000L - Parameters.MinFee - amountToSpend ))
       assert(receiverUnspentCoins== (amountToSpend ))
     // assert(1==12)

  }


  )



}

class SimpleTransactionSpec extends PropSpec {

  property("run") {
    SimpleTransaction
  }
}
