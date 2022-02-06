package org.ergoplatform.playgroundenv.models
import org.ergoplatform.appkit.{Address}

import org.ergoplatform.appkit.{AppkitProvingInterpreter, Iso, Mnemonic, ExtendedInputBox}
import org.ergoplatform.appkit.impl.InputBoxImpl
import org.ergoplatform.wallet.mnemonic.{Mnemonic => WMnemonic}
import org.ergoplatform.wallet.secrets.ExtendedSecretKey
import org.ergoplatform.wallet.interpreter.{TransactionHintsBag}
import org.ergoplatform.{ErgoLikeTransaction, UnsignedErgoLikeTransaction, P2PKAddress, ErgoAddressEncoder, ErgoAddress}
import sigmastate.basics.DiffieHellmanTupleProverInput
import sigmastate.eval.CSigmaProp
import org.ergoplatform.playgroundenv.utils.TransactionVerifier
import java.util
import sigmastate.basics.{DLogProtocol}

class DummyWalletImpl(
  blockchain: DummyBlockchainSimulationImpl,
  override val name: String
) extends Wallet {

   implicit val addressEncoder = ErgoAddressEncoder(
    ErgoAddressEncoder.TestnetNetworkPrefix
  )

  private val masterKey = {
    val m    = Mnemonic.generateEnglishMnemonic()
    val seed = WMnemonic.toSeed(org.ergoplatform.wallet.interface4j.SecretString.create(m), None)
    ExtendedSecretKey.deriveMasterKey(seed)
  }

  override val getAddress: Address = new Address(P2PKAddress(masterKey.publicKey.key))

  override def sign(tx: UnsignedErgoLikeTransaction): ErgoLikeTransaction = {
    println(s"......$name: Signing transaction ${tx.id}")
    import Iso._
    val dlogs =
      JListToIndexedSeq(identityIso[ExtendedSecretKey]).from(IndexedSeq(masterKey))
    val dhtInputs      = new java.util.ArrayList[DiffieHellmanTupleProverInput](0)
    val boxesToSpend   = tx.inputs.map(i => blockchain.getUnspentBox(i.boxId).asInstanceOf[InputBoxImpl]).toIndexedSeq

    val boxesToSpendExtended = boxesToSpend.map(b => ExtendedInputBox(b.getErgoBox, b.getExtension)).toIndexedSeq
    
    val dataInputBoxes = tx.dataInputs.map(i => blockchain.getBox(i.boxId)).toIndexedSeq

    val dLogSecrets = new util.ArrayList[DLogProtocol.DLogProverInput](0)
    val prover         = new AppkitProvingInterpreter(
      secretKeys=dlogs,
      dLogInputs=dLogSecrets,
      dhtInputs=dhtInputs,
      params=blockchain.parameters)

    val baseCost = 0
    prover.sign(tx, boxesToSpendExtended, dataInputBoxes, blockchain.stateContext, baseCost).get._1
  }
}
