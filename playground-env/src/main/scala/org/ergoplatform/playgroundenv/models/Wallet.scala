package org.ergoplatform.playgroundenv.models
import org.ergoplatform.appkit.Address
import  org.ergoplatform.appkit.impl.BlockchainContextImpl
import org.ergoplatform.appkit.{UnsignedTransaction,SignedTransaction} ;

import org.ergoplatform.{ErgoLikeTransaction, UnsignedErgoLikeTransaction}

trait Wallet {

  def name: String

  def getAddress: Address

  def sign(tx: UnsignedTransaction): SignedTransaction



}
