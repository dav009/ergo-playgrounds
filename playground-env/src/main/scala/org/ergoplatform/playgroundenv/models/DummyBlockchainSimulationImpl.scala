package org.ergoplatform.playgroundenv.models
import scala.collection.JavaConverters._
import org.ergoplatform.appkit.impl.InputBoxImpl
import org.ergoplatform.ErgoBox.BoxId
import org.ergoplatform.{ErgoBox, ErgoLikeTransaction}
import org.ergoplatform.wallet.protocol.context.{ErgoLikeParameters, ErgoLikeStateContext}
import org.ergoplatform.appkit.Address
import scorex.crypto.authds.ADDigest
import scorex.crypto.hash.Digest32
import scorex.util.encode.Base16
import scorex.util._
import sigmastate.eval.{CGroupElement, CPreHeader, Colls}
import org.ergoplatform.restapi.client.PowSolutions;
import sigmastate.interpreter.CryptoConstants
import special.collection.Coll
import special.sigma.{Header, PreHeader}
import sigmastate.eval.Extensions._
import org.ergoplatform.appkit.{NetworkType}
import org.ergoplatform.appkit.impl.BlockchainContextBase
import org.ergoplatform.restapi.client.ApiClient;
import org.ergoplatform.restapi.client.NodeInfo;
import org.ergoplatform.appkit.impl.SignedTransactionImpl;
import scala.collection.mutable
import org.ergoplatform.playgroundenv.dsl.ObjectGenerators
import org.ergoplatform.playgroundenv.utils.TransactionVerifier
import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.ErgoLikeTransactionSerializer$;
import org.ergoplatform.appkit.{SignedTransaction, UnsignedTransactionBuilder, InputBox, ErgoProverBuilder, ErgoWallet, PreHeaderBuilder, CoveringBoxes, ErgoToken};
import org.ergoplatform.restapi.client.ApiClient;
import org.ergoplatform.restapi.client.NodeInfo;
import sigmastate.Values;
import sigmastate.serialization.SigmaSerializer$;
import sigmastate.utils.SigmaByteReader;
import java.util;
import org.ergoplatform.restapi.client
import java.util.function
import org.ergoplatform.appkit.ErgoClient
import org.ergoplatform.appkit.BlockchainContext
import org.ergoplatform.appkit.impl.{BlockchainContextBuilderImpl, ColdBlockchainContext}
import org.ergoplatform.restapi.client.{ApiClient, NodeInfo, Parameters}
import org.ergoplatform.appkit.impl.UnsignedTransactionBuilderImpl

import org.ergoplatform.restapi.client.BlockHeader
import org.ergoplatform.explorer.client.ExplorerApiClient;

import  org.ergoplatform.appkit.impl.BlockchainContextImpl
import scala.collection.JavaConverters._
import retrofit2.Retrofit;
import java.util.{List => JavaList}
import java.util.Collections
import org.ergoplatform.appkit.ErgoType
import java.util.ArrayList
import java.math.BigInteger;

object SimulatedErgoClient{
  def create(blockchainSim: BlockchainSimulation) = {
    new SimulatedErgoClient(new client.Parameters(), blockchainSim)
  }
}

class SimulatedErgoClient(params: client.Parameters, blockchain: BlockchainSimulation) extends ErgoClient {
  override def execute[T](action: function.Function[BlockchainContext, T]): T = {
    val nodeInfo = new NodeInfo().parameters(params)
    val client = new ApiClient("https://somedummy.com")
    val retrofit = null
    val explorer = null
    val retrofitExplorer = null
    val headers = new ArrayList[BlockHeader]()
    val powSolutions = new PowSolutions();
    powSolutions.setPk("03224c2f2388ae0741be2c50727caa49bd62654dc1f36ee72392b187b78da2c717");
     powSolutions.w("0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
        powSolutions.n("20d68047ea27a031");
        powSolutions.d(BigInteger.ZERO);
    val blockId=  "78a76fb6c8ac11e7e9da01f2c916b82dd1220a370d7fcfe2df94caa675c21926"
    val firstHeader = new BlockHeader().height(667).nBits(19857408L).difficulty(BigInteger.TEN).id(blockId).parentId(blockId).adProofsRoot(blockId).stateRoot(blockId).transactionsRoot(blockId).version(2).extensionHash(blockId).powSolutions(powSolutions)
            .timestamp(0L)
            .votes("0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
    headers.add(firstHeader)

    val ctx = new SimulatedBlockchainContext(client, retrofit, explorer, retrofitExplorer, nodeInfo, headers, blockchain)
    val res = action.apply(ctx)
    res
  }
}

class SimulatedBlockchainContext(client: ApiClient, retrofit: Retrofit, explorer: ExplorerApiClient, retrofitExplorer: Retrofit, nodeInfo: NodeInfo, headers: JavaList[BlockHeader], blockchain: BlockchainSimulation) extends BlockchainContextImpl(client, retrofit, explorer, retrofitExplorer,NetworkType.MAINNET, nodeInfo, headers) {

  // ok
 // override def getNodeInfo: NodeInfo = {
 //  nodeInfo
  //}

  // ok 
  override def signedTxFromJson(json: String): SignedTransaction = ???


  // need to use blockcahin
  override def getBoxesById(boxIds: String*): Array[InputBox] = ???

  // ok
  //override def newProverBuilder(): ErgoProverBuilder = ???

  // need to use blockchain
  override def getHeight: Int = {
    blockchain.getHeight
  }

  // ok
  override def getWallet: ErgoWallet = ???

  // need to use blockchian
  override def sendTransaction(tx: SignedTransaction): String = {
    val ergoTx: ErgoLikeTransaction = tx.asInstanceOf[SignedTransactionImpl].getTx();
    blockchain.send(ergoTx)
    "something"
  }

  // ok
  //override def createPreHeader(): PreHeaderBuilder = ???

  //ok
  //override def getCoveringBoxesFor(address: Address,
  //                                 amountToSpend: Long,
  //                                 tokensToSpend: util.List[ErgoToken]): CoveringBoxes = ???

  // need to use blockchain
  // done
  override def getUnspentBoxesFor(address: Address,
                                  offset: Int,
    limit: Int): util.List[InputBox] = {
    val ergoBoxes = blockchain.selectUnspentBoxesFor(address)
    ergoBoxes.map{
      box => new InputBoxImpl(this, box)
    }.map(_.asInstanceOf[InputBox]).asJava
  }

}

case class DummyBlockchainSimulationImpl(scenarioName: String)
  extends BlockchainSimulation {

  private var boxes: mutable.ArrayBuffer[ErgoBox] = new mutable.ArrayBuffer[ErgoBox]()

  private var unspentBoxes: mutable.ArrayBuffer[ErgoBox] =
    new mutable.ArrayBuffer[ErgoBox]()
  private val tokenNames: mutable.Map[ModifierId, String] = mutable.Map()
  private var chainHeight: Int                            = 0
  private var nextBoxId: Short = 0
  private def getUnspentBoxesFor(address: Address): List[ErgoBox] =
    unspentBoxes.filter { b =>
      address.getErgoAddress.script == b.ergoTree
    }.toList

  def BlockChainContext()={

  }

  def stateContext: ErgoLikeStateContext = new ErgoLikeStateContext {

    override def sigmaLastHeaders: Coll[Header] = Colls.emptyColl

    override def previousStateDigest: ADDigest =
      Base16
        .decode("a5df145d41ab15a01e0cd3ffbab046f0d029e5412293072ad0f5827428589b9302")
        .map(ADDigest @@ _)
        .getOrElse(throw new Error(s"Failed to parse genesisStateDigest"))

    override def sigmaPreHeader: PreHeader = CPreHeader(
      version   = 0,
      parentId  = Colls.emptyColl[Byte],
      timestamp = 0,
      nBits     = 0,
      height    = chainHeight,
      minerPk   = CGroupElement(CryptoConstants.dlogGroup.generator),
      votes     = Colls.emptyColl[Byte]
    )
  }

  val parameters: ErgoLikeParameters = new ErgoLikeParameters {

    override def storageFeeFactor: Int = 1250000

    override def minValuePerByte: Int = 360

    override def maxBlockSize: Int = 524288

    override def tokenAccessCost: Int = 100

    override def inputCost: Int = 2000

    override def dataInputCost: Int = 100

    override def outputCost: Int = 100

    override def maxBlockCost: Long = 2000000

    override def softForkStartingHeight: Option[Int] = None

    override def softForkVotesCollected: Option[Int] = None

    override def blockVersion: Byte = 1
  }

  def generateUnspentBoxesFor(
    address: Address,
    toSpend: Long,
    tokensToSpend: List[TokenAmount]
  ): Unit = {
    tokensToSpend.foreach { t =>
      tokenNames += (t.token.tokenId.toArray.toModifierId -> t.token.tokenName)
    }
    val b = new ErgoBox(
      index          = nextBoxId,
      value          = toSpend,
      ergoTree       = address.getErgoAddress.script,
      creationHeight = chainHeight,
      transactionId = ErgoBox.allZerosModifierId,
     // additionalTokens =
     //   tokensToSpend.map(ta => (Digest32 @@ ta.token.tokenId.toArray, ta.tokenAmount))
    )
    nextBoxId = (nextBoxId + 1).toShort
    unspentBoxes.append(b)
    boxes.append(b)
  }

  def selectUnspentBoxesFor(
    address: Address
  ): List[ErgoBox] = {
    val treeToFind = address.getErgoAddress.script
    val filtered = unspentBoxes.filter { b =>
      b.ergoTree == treeToFind
    }.toList
    filtered
  }

  def getUnspentBox(id: BoxId): ErgoBox =
    unspentBoxes.find(b => java.util.Arrays.equals(b.id, id)).get

  def getBox(id: BoxId): ErgoBox =
    boxes.find(b => java.util.Arrays.equals(b.id, id)).get

  override def newParty(name: String, ctx: BlockchainContext): Party = {
    val party = DummyPartyImpl(this, ctx , name)
    val pk    = party.wallet.getAddress
    println(s"..$scenarioName: Creating new party: $name, pk: $pk")
    party
  }

  override def send(tx: ErgoLikeTransaction): Unit = {

    val boxesToSpend   = tx.inputs.map(i => getUnspentBox(i.boxId)).toIndexedSeq
    val dataInputBoxes = tx.dataInputs.map(i => getBox(i.boxId)).toIndexedSeq
    TransactionVerifier.verify(tx, boxesToSpend, dataInputBoxes, parameters, stateContext)

    val newBoxes: mutable.ArrayBuffer[ErgoBox] = new mutable.ArrayBuffer[ErgoBox]()
    newBoxes.appendAll(tx.outputs)
    newBoxes.appendAll(
      unspentBoxes.filterNot(b =>
        tx.inputs.map(_.boxId.toModifierId).contains(b.id.toModifierId)
      )
    )
    unspentBoxes = newBoxes
    boxes.appendAll(tx.outputs)
    println(s"..$scenarioName: Accepting transaction ${tx.id} to the blockchain")
  }

  override def newToken(name: String): TokenInfo = {
    val tokenId = ObjectGenerators.newErgoId
    tokenNames += (tokenId.toArray.toModifierId -> name)
    TokenInfo(tokenId, name)
  }

  def getUnspentCoinsFor(address: Address): Long =
    getUnspentBoxesFor(address).map(_.value).sum

  def getUnspentTokensFor(address: Address): List[TokenAmount] =
    getUnspentBoxesFor(address).flatMap { b =>
      b.additionalTokens.toArray.map { t =>
        TokenAmount(TokenInfo(t._1.toColl, tokenNames(t._1.toModifierId)), t._2)
      }
    }

  def getHeight: Int = chainHeight

  def setHeight(height: Int): Unit = { chainHeight = height }
}
