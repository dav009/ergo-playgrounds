package org.ergoplatform.playgrounds.dsl

import org.ergoplatform.ErgoBox
import org.ergoplatform.ErgoBox.NonMandatoryRegisterId
import org.ergoplatform.playground.Coll
import org.ergoplatform.playgrounds.models.{InputBox, OutBox, OutBoxCandidate, TokenInfo}
import sigmastate.Values.{SValue, SigmaPropValue}

import scala.language.implicitConversions

trait BoxDsl {

  implicit def outBoxToInputBox(in: OutBox): InputBox =
    InputBox(in.value, in.tokens, in.script)

  implicit class ListOps(l: List[InputBox]) {
    def totalValue: Long       = l.map(_.value).sum
    def totalTokenAmount: Long = l.map(_.value).sum
  }

  val R4 = ErgoBox.R4

  def Box(value: Long, script: SigmaPropValue): OutBoxCandidate =
    OutBoxCandidate(value, script)

  def Box(value: Long, token: TokenInfo, script: SigmaPropValue): OutBoxCandidate =
    OutBoxCandidate(value, List(token), List(), script)

  def Box(
    value: Long,
    register: (NonMandatoryRegisterId, Any),
    script: SigmaPropValue
  ): OutBoxCandidate = OutBoxCandidate(value, List(), List(register), script)

  def Box(
    value: Long,
    token: (Coll[Byte], Long),
    register: (NonMandatoryRegisterId, Any),
    script: SigmaPropValue
  ): OutBoxCandidate = OutBoxCandidate(value, List(token), List(register), script)
}