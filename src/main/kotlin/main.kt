/**
 *
 */
import Constants.Companion.allOperators
import Constants.Companion.operators
import Constants.Companion.scopingFunction
import kastree.ast.Node
import kastree.ast.Visitor
import kastree.ast.psi.Parser
import java.io.File
import java.io.BufferedReader
import java.io.FileWriter


//Adds updateMap to MutableMap class
fun MutableMap<String, Int?>.updateMap(oper: String, map: MutableMap<String, Int?>): MutableMap<String, Int?> {
//    val currentValue = map.get(oper)
//    if(currentValue != null) {
//        map[oper] = currentValue + 1
//    } else {
//        map[oper] = 1
//    }
    return map
}


var globalMap: MutableMap<String, Int?> = mutableMapOf()


fun main(args: Array<String>) {
    val folder = File("/Users/albertinin/Documents/TCC/rn-doctor/kotlin-code-new")
    val listOfFiles = folder.listFiles()

    val qtdOfFiles = listOfFiles.size
    var  notCompiled = 0

//    val file = """
//
//
//    object DataProviderManager {
//    fun registerDataProvider(provider: DataProvider) {
//        println("asd")
//    }
//    }
//        """
//    val fileCode = Parser.parseFile(file)
//    Visitor.visit(fileCode) { v, _ ->
//        Node.Decl.Structured.Form.OBJECT
//        println(v)
//    }

//    initCsv()

    val errors = File("/Users/albertinin/Documents/TCC/kotlin-code-analysis/errorJavaAndKotlinFiles.csv")
    val fileWriterError = FileWriter(errors, true)
    for (file in listOfFiles) {
        if (file.isFile) {
            try {
                runAnalysis(file)
            } catch (error: Exception) {
                println("File could not be compiled!")
                fileWriterError.append(file.name+"\n")
                println(file.name)
                notCompiled++
            }
        }
    }
    fileWriterError.flush()
    fileWriterError.close()

//    val newFile= File("/Users/albertinin/Documents/TCC/kotlin-code-analysis/java-followed-by-kotlin/globalResult.json")
//    val fileWriter = FileWriter(newFile)
//    fileWriter.appendln("Number of files: " + qtdOfFiles + ",\n")
//    fileWriter.appendln("Not compiled: " + notCompiled + ",\n")
//    fileWriter.appendln("Global: \""+ globalMap.toString()+"\"")
//    fileWriter.flush()
//    fileWriter.close()
//    println("Number of files: " + qtdOfFiles)
//    println("Not compiled: " + notCompiled)
//    updateCsv(globalMap, "Total")


}

fun runAnalysis(file: File){
    val path = file.absolutePath
    val bufferedReader: BufferedReader = File(path).bufferedReader()
    val inputString = bufferedReader.use { it.readText() }
    val fileCode = Parser.parseFile(inputString)
    val map: MutableMap<String, Int?> = mutableMapOf()

    visit(fileCode, map)

    val fileName = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'))
//    updateCsv(map, fileName)
//
//    if(!map.isEmpty()) {
//        val newFile = File("/Users/albertinin/Documents/TCC/kotlin-code-analysis/java-followed-by-kotlin/"+fileName+".json")
//        val fileWriter = FileWriter(newFile)
//        fileWriter.write(map.toString())
//        fileWriter.flush()
//        fileWriter.close()
//    }
}

fun visit(file: Node.File, map: MutableMap<String, Int?>){
    Visitor.visit(file) { v, _ ->
        when {
            v is Node.Expr.UnaryOp -> {
                val oper = v.oper.token
                if(oper == Node.Expr.UnaryOp.Token.NULL_DEREF) {
                    map.updateMap(oper.toString(), map)
                    globalMap.updateMap(oper.toString(), globalMap)
                }
            }

            v is Node.Decl.Structured -> {
                val oper = v.form
                if(oper == Node.Decl.Structured.Form.COMPANION_OBJECT){
                    map.updateMap(oper.toString(), map)
                    globalMap.updateMap(oper.toString(), globalMap)
                }
            }

            v is Node.Expr.Call.TrailLambda -> {
                val oper = "TRAIL_LAMBDA"
                map.updateMap(oper, map)
                globalMap.updateMap(oper, globalMap)
            }

            v is Node.Expr.BinaryOp -> {
                val oper = v.oper.toString()
                if(operators.contains(oper)) {
                    map.updateMap(oper, map)
                    globalMap.updateMap(oper, globalMap)
                }

                if((oper == "Token(token=NEQ)") && (v.rhs.toString() == "Const(value=null, form=NULL)")){
                    map.updateMap("(!= null)", map)
                    globalMap.updateMap("(!= null)", globalMap)
                }
            }

            v is Node.Expr.Call -> {
                val scopeFunction = v.expr.toString()
                if(scopingFunction.contains(scopeFunction)){
                    map.updateMap(scopeFunction, map)
                    globalMap.updateMap(scopeFunction, globalMap)
                }
            }
        }
    }
}


fun initCsv() {
    val newFile = File("/Users/albertinin/Documents/TCC/kotlin-code-analysis/java-followed-by-kotlin/globalResult.csv")
    val fileWriter = FileWriter(newFile, true)
    for (operator in allOperators) {
        fileWriter.append(operator)
        fileWriter.append(',')
    }
    fileWriter.append("filename")
    fileWriter.append("\n")
    fileWriter.flush()
    fileWriter.close()
}

fun updateCsv(map: MutableMap<String, Int?>, fileName: String) {
    val newFile = File("/Users/albertinin/Documents/TCC/kotlin-code-analysis/java-followed-by-kotlin/globalResult.csv")
    val fileWriter = FileWriter(newFile, true)
    for (operator in allOperators) {
        if(map.containsKey(operator)){
            fileWriter.append(map.getValue(operator).toString())
        }
        else {
            fileWriter.append("0")
        }
        fileWriter.append(',')

    }


    fileWriter.append(fileName)
    fileWriter.append("\n")
    fileWriter.flush()
    fileWriter.close()
}