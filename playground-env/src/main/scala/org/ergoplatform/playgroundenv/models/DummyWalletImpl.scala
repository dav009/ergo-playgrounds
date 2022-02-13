package org.ergoplatform.playgroundenv.models
import org.ergoplatform.appkit.{Address}

import org.ergoplatform.appkit.{AppkitProvingInterpreter, Iso, Mnemonic, ExtendedInputBox}
import org.ergoplatform.appkit.impl.InputBoxImpl
import org.ergoplatform.appkit.impl.SignedTransactionImpl;
import org.ergoplatform.appkit.impl.InputBoxImpl
import  org.ergoplatform.appkit.impl.{BlockchainContextImpl, BlockchainContextBase, UnsignedTransactionImpl}
import org.ergoplatform.appkit.{SignedTransaction, UnsignedTransactionBuilder, InputBox, ErgoProverBuilder, ErgoWallet, PreHeaderBuilder, CoveringBoxes, ErgoToken};
import org.ergoplatform.wallet.mnemonic.{Mnemonic => WMnemonic}
import org.ergoplatform.wallet.secrets.ExtendedSecretKey
import org.ergoplatform.wallet.interpreter.{TransactionHintsBag}
import org.ergoplatform.appkit.UnsignedTransaction;
import org.ergoplatform.{ErgoLikeTransaction, UnsignedErgoLikeTransaction, P2PKAddress, ErgoAddressEncoder, ErgoAddress}
import sigmastate.basics.DiffieHellmanTupleProverInput
import sigmastate.eval.CSigmaProp
import org.ergoplatform.playgroundenv.utils.TransactionVerifier
import java.util
import sigmastate.basics.{DLogProtocol}
import org.ergoplatform.appkit.BlockchainContext
import org.ergoplatform.appkit.JavaHelpers

class DummyWalletImpl(
  blockchain: DummyBlockchainSimulationImpl,
  ctx: BlockchainContextBase,
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

  override def sign(tx: UnsignedTransaction): SignedTransaction = {
    val txImpl = tx.asInstanceOf[UnsignedTransactionImpl]
    val boxesToSpend = JavaHelpers.toIndexedSeq(txImpl.getBoxesToSpend)
    val dataBoxes = JavaHelpers.toIndexedSeq(txImpl.getDataBoxes)
    import Iso._
    val dhtInputs      = new java.util.ArrayList[DiffieHellmanTupleProverInput](0)
    val dlogs =
      JListToIndexedSeq(identityIso[ExtendedSecretKey]).from(IndexedSeq(masterKey))
    val dLogSecrets = new util.ArrayList[DLogProtocol.DLogProverInput](0)
    val prover         =new AppkitProvingInterpreter(
      secretKeys=dlogs,
      dLogInputs=dLogSecrets,
      dhtInputs=dhtInputs,
      params=blockchain.parameters)
    val (signed, cost) = prover.sign(txImpl.getTx, boxesToSpend, dataBoxes, blockchain.stateContext, baseCost=0).get
    
     new SignedTransactionImpl(ctx, signed, cost)
  }
}
