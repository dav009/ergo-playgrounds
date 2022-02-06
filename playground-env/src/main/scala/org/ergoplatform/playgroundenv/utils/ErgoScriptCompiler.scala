package org.ergoplatform.playgroundenv.utils

//  org.ergoplatform.compiler
//import org.ergoplatform.compiler.ErgoContract
import org.ergoplatform.appkit.{ErgoContract}
import org.ergoplatform.appkit.impl.{ErgoTreeContract}
import sigmastate.interpreter.Interpreter.ScriptEnv
import sigmastate.lang.{SigmaCompiler, TransformingSigmaBuilder}
import org.ergoplatform.ErgoAddressEncoder.TestnetNetworkPrefix
import sigmastate.eval.CompiletimeIRContext
import sigmastate.eval.Evaluation
import sigmastate.SType
import sigmastate.lang.{CompilerSettings, SigmaCompiler, TransformingSigmaBuilder}
import sigmastate.SType.AnyOps
import org.ergoplatform.appkit.JavaHelpers
import java.util
import scala.collection.JavaConversions.mapAsJavaMap
import org.ergoplatform.appkit.impl.{ErgoTreeContract}
import sigmastate.Values.SigmaPropConstant
import sigmastate.basics.DLogProtocol.ProveDlog

object ErgoScriptCompiler {


 def contract(d:ProveDlog) = {
    new ErgoTreeContract(SigmaPropConstant(d))
  }

  //val compiler = SigmaCompiler( CompilerSettings(
  //      networkPrefix    = TestnetNetworkPrefix,
   //     builder          = TransformingSigmaBuilder,
    //    lowerMethodCalls = true
    //  ))

 // implicit var IR: CompiletimeIRContext = new CompiletimeIRContext()

  def compile(env: Map[String, Any], ergoScript: String): ErgoContract = {

    val ergoTree = JavaHelpers.compile(mapAsJavaMap(env.mapValues(_.asInstanceOf[Object])), ergoScript, TestnetNetworkPrefix)
    new ErgoTreeContract(ergoTree)
    //val liftedEnv = env.mapValues { v =>
    //  val tV      = Evaluation.rtypeOf(v).get
    //  val elemTpe = Evaluation.rtypeToSType(tV)
     // IR.builder.mkConstant[SType](v.asWrappedType, elemTpe)
   // }
   // val prop = compiler.compile(liftedEnv, ergoScript)

   // ErgoContract(_ => ???, prop)
  }
}
 
