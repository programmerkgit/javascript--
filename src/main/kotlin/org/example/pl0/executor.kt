package org.example.pl0

enum class Code {
    LIT,
    LOD,
    STO,
    CAL,
    RET,
    ICT,
    JMP,
    JPC,
    NEG,
    EQ,
    NOTEQ,
    GRT,
    LSS,
    GRTEQ,
    LSSEQ,
    ADD,
    SUB,
    MUL,
    DIV,
    ODD,
    WRT,
    WRL
}

private fun <T> MutableList<T>.pop(): T {
    return removeAt(lastIndex)
}

abstract class Instruction(val code: Code)
abstract class Instruction1(opCode: Code, val level: Int, val addr: Int) : Instruction(opCode) {
    override fun toString(): String {
        return "code: $code level: $level addr:$addr"
    }
}

class Lod(level: Int, addr: Int) : Instruction1(Code.LOD, level, addr)
class Sto(level: Int, addr: Int) : Instruction1(Code.STO, level, addr)

/* Call level 0, addr func*/
/**
 * @param addr 相対アドレス
 *
 */
class Cal(level: Int, addr: Int) : Instruction1(Code.CAL, level, addr)
class Ret(level: Int, pars: Int) : Instruction1(Code.RET, level, pars)
abstract class Instruction2(opCode: Code, var value: Int) : Instruction(opCode) {
    override fun toString(): String {
        return "code: $code value: $value"
    }
}

class Lit(value: Int) : Instruction2(Code.LIT, value)
class Ict(value: Int) : Instruction2(Code.ICT, value)
class Jmp(value: Int = 0) : Instruction2(Code.JMP, value)
class Jpc(value: Int = 0) : Instruction2(Code.JPC, value)
abstract class Instruction3(opCode: Code) : Instruction(opCode) {
    override fun toString(): String {
        return "code: $code"
    }
}

class Neg : Instruction3(Code.NEG)
class Eq : Instruction3(Code.EQ)
class NotEq : Instruction3(Code.NOTEQ)
class Grt : Instruction3(Code.GRT)
class Lss : Instruction3(Code.LSS)
class GrtEq : Instruction3(Code.GRTEQ)
class LssEq : Instruction3(Code.LSSEQ)
class Add : Instruction3(Code.ADD)
class Sub : Instruction3(Code.SUB)
class Mul : Instruction3(Code.MUL)
class Div : Instruction3(Code.DIV)
class Odd : Instruction3(Code.ODD)
class Wrt : Instruction3(Code.WRT)
class Wrl : Instruction3(Code.WRL)

/* mutable ?  */
class Executor(private val instructions: List<Instruction>) {
    var pc = 0;
    var stack = mutableListOf<Int>()
    val display = mutableMapOf<Int, Int>(0 to 0)
    fun execute() {
        /* top不要? */
        do {
            /* breakとcontinueの差は?? */
            when (val inst = instructions[pc++]) {
                is Lit -> stack.add(inst.value)
                is Lod -> stack.add(stack[display[inst.level]!! + inst.addr])
                is Sto -> stack[display[inst.level]!! + inst.addr] = stack.pop()
                is Cal -> {
                    val level = inst.level + 1
                    /* get or null? */
                    stack.add(display.getOrElse(level) { 0 })
                    display[level] = stack.lastIndex
                    stack.add(pc)
                    pc = inst.addr
                }
                is Ret -> {
                    val ret = stack.pop()
                    val top = display[inst.level]!!
                    display[inst.level] = stack[top]
                    pc = stack[top + 1]
                    /* displayの手前に引数分の変数領域がある */
                    stack = stack.slice(0 until top).toMutableList()
                    repeat(inst.addr) {
                        stack.removeAt(stack.lastIndex)
                    }
                    stack.add(ret)
                }
                is Ict -> repeat(inst.value) { stack.add(0) }
                is Jmp -> pc = inst.value
                is Jpc -> {
                    if (stack.pop() == 0) {
                        pc = inst.value
                    }
                }
                is Neg -> stack.add(-stack.pop())
                is Add -> stack.add(stack.pop() + stack.pop())
                is Sub -> {
                    val b = stack.pop()
                    val a = stack.pop()
                    stack.add(a - b)
                }
                is Mul -> stack.add(stack.pop() * stack.pop())
                is Div -> {
                    val b = stack.pop()
                    val a = stack.pop()
                    stack.add(a / b)
                }
                is Odd -> stack.add(stack.pop() and 1)
                is Eq -> stack.add(if (stack.pop() == stack.pop()) 1 else 0)
                is NotEq -> stack.add(if (stack.pop() != stack.pop()) 1 else 0)
                is Lss -> stack.add(if (stack.pop() > stack.pop()) 1 else 0)
                is Grt -> stack.add(if (stack.pop() < stack.pop()) 1 else 0)
                is LssEq -> stack.add(if (stack.pop() >= stack.pop()) 1 else 0)
                is GrtEq -> stack.add(if (stack.pop() <= stack.pop()) 1 else 0)
                is Wrt -> print(stack.pop())
                is Wrl -> println()
                else -> error("unexpected instruction")
            }
        } while (pc != 0)
    }
}